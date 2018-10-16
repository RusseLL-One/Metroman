@file:Suppress("DEPRECATION")

package com.one.russell.metronomekotlin

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.support.v4.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit
import io.reactivex.subjects.PublishSubject
import java.util.*
import android.os.*
import com.one.russell.metronomekotlin.views.BeatView
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import android.os.Build
import android.widget.Toast

const val ACTION_STOP_CLICKING = "stop_clicking"

class TickService : Service() {

    private val binder = ServiceBinder()
    private var listener: MainActivity.TickListener? = null
    private var clickPlayer: ClickPlayer = ClickPlayer()
    var isPlaying = false
    var isTrainingGoing = false
    var trainingMessage = ""
    var completionPercentage = 0f
    private var bpm: Int = 100
    private var prevBpm: Int = 0
    private var beatsPerBar = 4
    private var beat = -1
    private var beatSequence = ArrayList<BeatView>()
    private var isMuted = false
    private var isFlasherEnabled = false
    private var isVibrateEnabled = false
    private var vibrator: Vibrator? = null
    private var cameraKitkat: Camera? = null

    private val clickObservable: PublishSubject<Long> = PublishSubject.create()
    private var clickDisposable: Disposable? = null

    var clickConsumer = Consumer<Long> {
        val isNextBar = isNextBar()
        click(isNextBar)
    }

    override fun onCreate() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            cameraKitkat = Camera.open()
        } catch (e: RuntimeException) {
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_CLICKING) {
            stop()
        }
        return Service.START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    private fun createNotification() {
        val intent = Intent(this, TickService::class.java)
        intent.action = ACTION_STOP_CLICKING

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(NotificationChannel("metronome", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT))
            NotificationCompat.Builder(this, "metronome")
        } else {
            NotificationCompat.Builder(this)
        }

        builder.setContentTitle("Metroman is playing")
                .setContentText("Press here to stop")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.icon_240)
                .setContentIntent(PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.color = resources.getColor(R.color.colorAccent, theme)
        } else {
            builder.color = resources.getColor(R.color.colorAccent)
        }

        startForeground(530, builder.build())
    }

    fun setTickListener(listener: MainActivity.TickListener) {
        this.listener = listener
    }

    fun setSoundPreset(id: Int?) {
        if (id != null) {
            clickPlayer.setSoundPreset(id)
        }
    }

    fun setFlasherEnabled(value: Boolean?) {
        if (value != null) {
            isFlasherEnabled = value
            if (cameraKitkat == null && value == true) {
                try {
                    cameraKitkat = Camera.open()
                } catch (e: RuntimeException) {
                }
            }
        }
    }

    fun setVibrateEnabled(value: Boolean?) {
        if (value != null) {
            isVibrateEnabled = value
        }
    }

    fun setBeatsSequence(seq: ArrayList<BeatView>) {
        beatsPerBar = seq.size

        beatSequence = seq
    }

    internal inner class ServiceBinder : Binder() {
        val service: TickService
            get() = this@TickService
    }

    fun isNextBar(): Boolean {
        beat++
        if (beat >= beatsPerBar) {
            beat = 0
        }
        return beat == 0
    }

    fun click(isNextBar: Boolean) {
        if (isFlasherEnabled) {
            toggleFlashLight()
        }

        if (isVibrateEnabled && isNextBar) {
            if (Build.VERSION.SDK_INT >= 26)
                vibrator?.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
            else
                vibrator?.vibrate(50L)
        }

        listener?.onTick(beatSequence[beat].beatType, beat, 60000 / bpm)
        if (prevBpm != bpm) {
            clickObservable.onNext((60000 / bpm).toLong())
            prevBpm = bpm
        }
        if (!isMuted) {
            clickPlayer.click(beatSequence[beat].beatType)
        }
    }

    fun setBpm(bpm: Int?) {
        if (bpm != null) {
            this.bpm = bpm
        }
    }

    fun play() {
        prevBpm = 0 //Для того, чтобы интервал изменился после первого клика в методе click()
        clickDisposable = clickObservable.switchMap { interval ->
            Observable.interval(interval, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(clickConsumer)
        }.subscribe()

        //Вызвываем интервал с длительностью 0 для того, чтобы первый клик прозвучал моментально
        clickObservable.onNext(0L)
        isPlaying = true

        listener?.onStartClicking()
        createNotification()
    }

    fun stop() {
        stopForeground(true)
        clickConsumer = Consumer {
            val isNextBar = isNextBar()
            click(isNextBar)
        }
        isTrainingGoing = false
        trainingMessage = ""
        listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
        listener?.onControlsBlock(false)
        listener?.onStopClicking()
        clickDisposable?.dispose()
        isPlaying = false
        beat = -1
    }

    private fun toggleFlashLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val cameraManager = applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

                for (id in cameraManager.cameraIdList) {
                    if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!!) {
                        cameraManager.setTorchMode(id, true)

                        Single.fromCallable {}.subscribeOn(Schedulers.io())
                                .delay(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                                .doOnSuccess {
                                    cameraManager.setTorchMode(id, false)
                                }
                                .subscribe()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "LED flasher is not available: " + e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            try {
                val paramsOn = cameraKitkat?.parameters
                paramsOn?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                cameraKitkat?.parameters = paramsOn
                cameraKitkat?.startPreview()
                Single.fromCallable {}.subscribeOn(Schedulers.io())
                        .delay(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .doOnSuccess {
                            val paramsOff = cameraKitkat?.parameters
                            paramsOff?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                            cameraKitkat?.parameters = paramsOff
                            cameraKitkat?.stopPreview()
                        }
                        .subscribe()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "LED flasher is not available: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startTraining(params: Bundle) {
        isTrainingGoing = true
        val trainingType: TrainingType
        try {
            val trainingTypeStr = params.getString("trainingType", "NONE")
            trainingType = TrainingType.valueOf(trainingTypeStr)
        } catch (e: IllegalArgumentException) {
            return
        }
        if (isPlaying) {
            stop()
        }

        isMuted = false

        when (trainingType) {
            TrainingType.TEMPO_INCREASING_BY_BARS -> {
                val startBpm = params.getInt("startBpm", MIN_BPM)
                val endBpm = params.getInt("endBpm", MAX_BPM)
                val bars = params.getInt("bars", 1)
                val increment = params.getInt("increment", 10)

                bpm = startBpm
                listener?.onControlsBlock(true)
                listener?.onBpmChange(bpm)
                trainingMessage = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                clickConsumer = object : Consumer<Long> {
                    var barCount = 0

                    override fun accept(t: Long?) {
                        val isNextBar = isNextBar()
                        click(isNextBar)

                        if (isNextBar) {
                            barCount++
                        }
                        if (barCount >= bars) {
                            bpm += increment
                            if (bpm >= endBpm) bpm = endBpm
                            listener?.onBpmChange(bpm)
                            completionPercentage = (bpm.toFloat() - startBpm.toFloat()) /
                                    (endBpm.toFloat() - startBpm.toFloat())
                            listener?.onTrainingUpdate(completionPercentage)
                            barCount = 0
                        }
                        if (bpm >= endBpm) {
                            isTrainingGoing = false
                            clickConsumer = Consumer {
                                val isNext = isNextBar()
                                click(isNext)
                            }
                            trainingMessage = ""
                            listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                            listener?.onControlsBlock(false)
                        }
                    }
                }
            }
            TrainingType.TEMPO_INCREASING_BY_TIME -> {
                val startBpm = params.getInt("startBpm", MIN_BPM)
                val endBpm = params.getInt("endBpm", MAX_BPM)
                val minutes = params.getInt("minutes", 5)

                val startTime = System.currentTimeMillis()
                val endTime = startTime + minutes * 60 * 1000
                val timeInterval = endTime - startTime
                val bpmInterval = endBpm - startBpm
                bpm = startBpm
                listener?.onControlsBlock(true)
                listener?.onBpmChange(bpm)
                trainingMessage = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                clickConsumer = Consumer { _ ->
                    val isNextBar = isNextBar()
                    click(isNextBar)

                    val currentTime = System.currentTimeMillis()
                    completionPercentage = (currentTime - startTime).toFloat() / timeInterval.toFloat()
                    bpm = (startBpm + (bpmInterval * completionPercentage)).toInt()
                    listener?.onBpmChange(bpm)
                    listener?.onTrainingUpdate(completionPercentage)

                    if (currentTime >= endTime) {
                        isTrainingGoing = false
                        clickConsumer = Consumer {
                            val isNext = isNextBar()
                            click(isNext)
                        }
                        trainingMessage = ""
                        listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                        listener?.onControlsBlock(false)
                    }
                }
            }
            TrainingType.BAR_DROPPING_RANDOM -> {
                val chance = params.getInt("barChance", 30)

                trainingMessage = App.getAppInstance().resources.getString(R.string.random_bar_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = 1f
                listener?.onTrainingUpdate(completionPercentage)

                clickConsumer = Consumer { _ ->
                    val isNextBar = isNextBar()

                    if (isNextBar) {
                        val percentage = Random(System.currentTimeMillis())
                        isMuted = percentage.nextInt(100) < chance
                    }

                    click(isNextBar)
                }
            }
            TrainingType.BAR_DROPPING_BY_COUNT -> {
                val normalBars = params.getInt("normalBars", 3)
                val mutedBars = params.getInt("mutedBars", 1)

                trainingMessage = App.getAppInstance().resources.getString(R.string.bar_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = 1f
                listener?.onTrainingUpdate(completionPercentage)

                clickConsumer = object : Consumer<Long> {
                    var barCount = 0

                    override fun accept(t: Long?) {
                        val isNextBar = isNextBar()

                        if (isNextBar) {
                            barCount++
                            if (barCount >= normalBars) {
                                isMuted = true
                                if (barCount >= normalBars + mutedBars) {
                                    barCount = 0
                                    isMuted = false
                                }
                            }
                        }
                        click(isNextBar)
                    }
                }
            }
            TrainingType.BEAT_DROPPING -> {
                val chance = params.getInt("beatChance", 30)

                trainingMessage = App.getAppInstance().resources.getString(R.string.random_beat_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = 1f
                listener?.onTrainingUpdate(completionPercentage)

                clickConsumer = Consumer { _ ->
                    val percentage = Random(System.currentTimeMillis())
                    isMuted = percentage.nextInt(100) < chance

                    val isNextBar = isNextBar()
                    click(isNextBar)
                }
            }
        }
        play()
    }
}