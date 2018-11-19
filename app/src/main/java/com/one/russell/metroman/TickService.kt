@file:Suppress("DEPRECATION")

package com.one.russell.metroman

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.support.v4.app.NotificationCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import java.util.*
import android.os.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import android.os.Build
import android.widget.Toast

const val ACTION_STOP_CLICKING = "stop_clicking"

class TickService : Service() {

    private val binder = ServiceBinder()
    private var listener: MainActivity.TickListener? = null
    var isPlaying = false
    var isTrainingGoing = false
    var trainingMessage = ""
    var completionPercentage = 0f
    private var beatSequence = ArrayList<BeatType>()
    private var isFlasherEnabled = false
    private var isVibrateEnabled = false
    private var vibrator: Vibrator? = null
    private var cameraKitkat: Camera? = null

    //Used in native code
    @Suppress("UNUSED")
    fun onTick(beat: Int, bpm: Int, completionPercentage: Float) {
        listener?.onTick(beat, 60000 / bpm)

        if (isFlasherEnabled) {
            performFlash()
        }

        if (isVibrateEnabled && beat == 0) {
            if (Build.VERSION.SDK_INT >= 26)
                vibrator?.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
            else
                vibrator?.vibrate(50L)
        }

        if (isTrainingGoing) {
            listener?.onBpmChange(bpm)
            listener?.onTrainingUpdate(completionPercentage)

            if (completionPercentage == 1f) {
                isTrainingGoing = false
                trainingMessage = ""

                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                listener?.onControlsBlock(false)
            }
        }
    }

    override fun onCreate() {
        native_init(assets)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            cameraKitkat = Camera.open()
        } catch (e: RuntimeException) {
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_CLICKING) {
            stop(false)
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
            native_set_soundpreset(id)
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

    fun setBeatsSequence(seq: ArrayList<BeatType>) {
        beatSequence = seq

        if (beatSequence.size > 0) {
            native_set_beatsequence(seq)
        }
    }

    internal inner class ServiceBinder : Binder() {
        val service: TickService
            get() = this@TickService
    }

    fun setBpm(bpm: Int?) {
        if (bpm != null) {
            native_set_bpm(bpm)
        }
    }

    fun play() {
        native_start_clicking()

        isPlaying = true

        listener?.onStartClicking()
        createNotification()
    }

    fun stop(isAutostop: Boolean) {
        native_stop_clicking()

        stopForeground(true)

        isTrainingGoing = false
        trainingMessage = ""
        listener?.onTrainingToggle(null, isTrainingGoing)
        listener?.onControlsBlock(false)
        listener?.onStopClicking(isAutostop)
        isPlaying = false
    }

    private fun performFlash() {
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
        val trainingType: TrainingType
        try {
            val trainingTypeStr = params.getString("trainingType", "NONE")
            trainingType = TrainingType.valueOf(trainingTypeStr)
        } catch (e: IllegalArgumentException) {
            return
        }
        if (isPlaying) {
            stop(true)
        }

        isTrainingGoing = true

        when (trainingType) {
            TrainingType.TEMPO_INCREASING_BY_BARS -> {
                val startBpm = params.getInt("startBpm", MIN_BPM)
                val endBpm = params.getInt("endBpm", MAX_BPM)
                val bars = params.getInt("bars", 1)
                val increment = params.getInt("increment", 10)

                listener?.onControlsBlock(true)
                listener?.onBpmChange(startBpm)
                trainingMessage = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)

                native_start_tempo_increasing_by_bars(startBpm, endBpm, bars, increment)
            }
            TrainingType.TEMPO_INCREASING_BY_TIME -> {
                val startBpm = params.getInt("startBpm", MIN_BPM)
                val endBpm = params.getInt("endBpm", MAX_BPM)
                val minutes = params.getInt("minutes", 5)

                listener?.onControlsBlock(true)
                listener?.onBpmChange(startBpm)
                trainingMessage = App.getAppInstance().resources.getString(R.string.tempo_increasing_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)

                native_start_tempo_increasing_by_time(startBpm, endBpm, minutes)
            }
            TrainingType.BAR_DROPPING_RANDOM -> {
                val chance = params.getInt("barChance", 30)

                trainingMessage = App.getAppInstance().resources.getString(R.string.random_bar_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = chance.toFloat() / 100
                listener?.onTrainingUpdate(completionPercentage)

                native_start_bar_dropping_by_random(chance)
            }
            TrainingType.BAR_DROPPING_BY_COUNT -> {
                val normalBars = params.getInt("normalBars", 3)
                val mutedBars = params.getInt("mutedBars", 1)

                trainingMessage = App.getAppInstance().resources.getString(R.string.bar_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = 0f
                listener?.onTrainingUpdate(completionPercentage)

                native_start_bar_dropping_by_count(normalBars, mutedBars)
            }
            TrainingType.BEAT_DROPPING -> {
                val chance = params.getInt("beatChance", 30)

                trainingMessage = App.getAppInstance().resources.getString(R.string.random_beat_drop_in_progress)
                listener?.onTrainingToggle(trainingMessage, isTrainingGoing)
                completionPercentage = chance.toFloat() / 100
                listener?.onTrainingUpdate(completionPercentage)

                native_start_beat_dropping(chance)
            }
        }
        play()
    }

    // Native methods
    private external fun native_init(assetManager: AssetManager)
    private external fun native_start_clicking()
    private external fun native_stop_clicking()
    private external fun native_set_bpm(bpm: Int)
    private external fun native_set_beatsequence(sequence: ArrayList<BeatType>)
    private external fun native_set_soundpreset(id: Int)
    private external fun native_start_tempo_increasing_by_bars(startBpm: Int, endBpm: Int, bars: Int, increment: Int)
    private external fun native_start_tempo_increasing_by_time(startBpm: Int, endBpm: Int, minutes: Int)
    private external fun native_start_bar_dropping_by_random(chance: Int)
    private external fun native_start_bar_dropping_by_count(normalBars: Int, mutedBars: Int)
    private external fun native_start_beat_dropping(chance: Int)
}