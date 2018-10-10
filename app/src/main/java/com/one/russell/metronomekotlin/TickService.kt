package com.one.russell.metronomekotlin

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit
import io.reactivex.subjects.PublishSubject
import java.util.*
import android.widget.Toast
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.one.russell.metronomekotlin.Views.BeatView


const val ACTION_STOP_CLICKING = "stop_clicking"

class TickService : Service() {

    private val binder = ServiceBinder()
    private var listener: MainActivity.TickListener? = null
    private var clickPlayer: ClickPlayer = ClickPlayer()
    var isPlaying = false
    private var bpm: Int = 100
    private var prevBpm: Int = 0
    private var beatsPerBar = 4
    private var beat = -1
    private var beatSequence = ArrayList<BeatView>()
    private var isMuted = false

    val clickObservable: PublishSubject<Long> = PublishSubject.create()
    var clickDisposable: Disposable? = null
    var clickConsumer = Consumer<Long> { click() }

    override fun onCreate() {
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
        } else
            NotificationCompat.Builder(this)

        builder.setContentTitle("Metroman is playing")
                .setContentText("Press here to stop")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.icon_240)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentIntent(PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))

        startForeground(530, builder.build())
    }

    fun setTickListener(listener: MainActivity.TickListener) {
        this.listener = listener
    }

    fun setAccentSound(id: Int?) {
        if (id != null) {
            clickPlayer.setAccentSound(id)
        }
    }

    fun setBeatSound(id: Int?) {
        if (id != null) {
            clickPlayer.setBeatSound(id)
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

    fun click(): Boolean {
        //toggleFlashLight()
        beat++
        val isNextBar = beat + 1 == beatsPerBar
        if (beat >= beatsPerBar) beat = 0
        listener?.onTick(beatSequence[beat].beatType, beat, 60000 / bpm)
        if (prevBpm != bpm) {
            clickObservable.onNext((60000 / bpm).toLong())
            prevBpm = bpm
        }
        if (!isMuted) {
            clickPlayer.click(beatSequence[beat].beatType)
        }
        //toggleFlashLight()
        return isNextBar
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
        clickConsumer = Consumer { click() }
        listener?.onTrainingToggle("", false)
        listener?.onControlsBlock(false)
        listener?.onStopClicking()
        clickDisposable?.dispose()
        isPlaying = false
        beat = -1
    }

    var toggle = false
    fun toggleFlashLight() {
        toggle = !toggle
        try {
            val cameraManager = applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for (id in cameraManager.cameraIdList) {

                    if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)!!) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cameraManager.setTorchMode(id, toggle)
                        }
                    }
                }
            }
        } catch (e2: Exception) {
            Toast.makeText(applicationContext, "Torch Failed: " + e2.message, Toast.LENGTH_SHORT).show()
        }


    }

    fun startTraining(params: Bundle) {
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
                val message = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(message, true)
                clickConsumer = object : Consumer<Long> {
                    var barCount = 0

                    override fun accept(t: Long?) {
                        val isNextBar = click()

                        if (isNextBar) {
                            barCount++
                        }
                        if (barCount >= bars) {
                            bpm += increment
                            if (bpm >= endBpm) bpm = endBpm
                            listener?.onBpmChange(bpm)
                            val completionPercentage = (bpm.toFloat() - startBpm.toFloat()) /
                                    (endBpm.toFloat() - startBpm.toFloat())
                            listener?.onTrainingUpdate(completionPercentage)
                            barCount = 0
                        }
                        if (bpm >= endBpm) {
                            clickConsumer = Consumer { click() }
                            listener?.onTrainingToggle("", false)
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
                val message = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(message, true)
                clickConsumer = Consumer { _ ->
                    click()

                    val currentTime = System.currentTimeMillis()
                    val timePercentage = (currentTime - startTime).toFloat() / timeInterval.toFloat()
                    bpm = (startBpm + (bpmInterval * timePercentage)).toInt()
                    listener?.onBpmChange(bpm)
                    listener?.onTrainingUpdate(timePercentage)

                    if (currentTime >= endTime) {
                        clickConsumer = Consumer { click() }
                        listener?.onTrainingToggle("", false)
                        listener?.onControlsBlock(false)
                    }
                }
            }
            TrainingType.BAR_DROPPING_RANDOM -> {
                val chance = params.getInt("chance", 30)

                val message = App.getAppInstance().resources.getString(R.string.random_bar_drop_in_progress)
                listener?.onTrainingToggle(message, true)
                listener?.onTrainingUpdate(1f)

                clickConsumer = Consumer { _ ->
                    val isNextBar = click()

                    if (isNextBar) {
                        val percentage = Random(System.currentTimeMillis())
                        isMuted = percentage.nextInt(100) < chance
                    }
                }
            }
            TrainingType.BAR_DROPPING_BY_COUNT -> {
                val normalBars = params.getInt("normalBars", 3)
                val mutedBars = params.getInt("mutedBars", 1)

                val message = App.getAppInstance().resources.getString(R.string.bar_drop_in_progress)
                listener?.onTrainingToggle(message, true)
                listener?.onTrainingUpdate(1f)

                clickConsumer = object : Consumer<Long> {
                    var barCount = 0

                    override fun accept(t: Long?) {
                        val isNextBar = click()

                        if (isNextBar) {
                            barCount++
                        }

                        if (barCount >= normalBars) {
                            isMuted = true
                            if (barCount >= normalBars + mutedBars) {
                                barCount = 0
                                isMuted = false
                            }
                        }
                    }
                }
            }
            TrainingType.BEAT_DROPPING -> {
                val chance = params.getInt("chance", 30)

                val message = App.getAppInstance().resources.getString(R.string.random_beat_drop_in_progress)
                listener?.onTrainingToggle(message, true)
                listener?.onTrainingUpdate(1f)

                clickConsumer = Consumer { _ ->
                    val percentage = Random(System.currentTimeMillis())
                    isMuted = percentage.nextInt(100) < chance

                    click()
                }
            }
        }
        play()
    }
}