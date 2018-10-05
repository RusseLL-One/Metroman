package com.one.russell.metronomekotlin

import android.app.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.concurrent.TimeUnit
import io.reactivex.subjects.PublishSubject
import com.one.russell.metronomekotlin.R.mipmap.ic_launcher



const val ACTION_STOP_CLICKING = "stop_clicking"
const val NOTIFICATION_ID = 21

class TickService : Service() {

    private val binder = ServiceBinder()
    private var listener: MainActivity.TickListener? = null
    private var clickPlayer: ClickPlayer = ClickPlayer()
    var isPlaying = false
    private var bpm: Int = 100
    private var prevBpm: Int = 0
    private var barCount = 0
    private var beatsPerBar = 4
    private var beat = -1
    private var beatSequence = ArrayList<BeatView>()
    var notificationManager: NotificationManager? = null

    val clickObservable: PublishSubject<Long> = PublishSubject.create()
    var clickDisposable: Disposable? = null
    var clickConsumer = Consumer<Long> { click() }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_CLICKING) {
            stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    private fun createNotification() {
        val builder = NotificationCompat.Builder(this)
        builder.setContentTitle("Metroman is playing")
        builder.setContentText("Press button below to stop")
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setSmallIcon(R.drawable.ic_launcher_background)

        val stopSelf = Intent(this, TickService::class.java)
        stopSelf.action = ACTION_STOP_CLICKING
        val pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_NO_CREATE)
        builder.addAction(R.drawable.ic_launcher_background, "Stop", pStopSelf)
        builder.setAutoCancel(true)

        notificationManager?.notify(NOTIFICATION_ID, builder.build())
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
        //todo сделать определение акцента здесь
        beat++
        val isNextBar = if (beat >= beatsPerBar) {
            beat = 0
            true
        } else {
            false
        }
        listener?.onTick(beatSequence[beat].beatType, beat, 60000 / bpm)
        if (prevBpm != bpm) {
            clickObservable.onNext((60000 / bpm).toLong())
            prevBpm = bpm
        }
        clickPlayer.click(beatSequence[beat].beatType)
        return isNextBar
    }

    fun setBpm(bpm: Int?) {
        if (bpm != null) {
            this.bpm = bpm
        }
    }

    fun startTraining(params: Bundle) {
        val trainingType = params.getString("trainingType", "TEMPO_INCREASING")
        val startBpm = params.getInt("startBpm", MIN_BPM)
        val endBpm = params.getInt("endBpm", MAX_BPM)
        val bars = params.getInt("bars", 1)
        val increment = params.getInt("increment", 10)

        if (isPlaying) {
            stop()
        }
        bpm = startBpm
        listener?.onControlsBlock(true)
        listener?.onBpmChange(bpm)
        clickConsumer = Consumer { _ ->
            val isNextBar = click()

            if(isNextBar) {
                barCount++
            }
            if (barCount >= bars) {
                bpm += increment
                if(bpm >= endBpm) bpm = endBpm
                listener?.onBpmChange(bpm)
                barCount = 0
            }
            if(bpm >= endBpm) {
                clickConsumer = Consumer { click() }
                listener?.onControlsBlock(false)
            }
        }
        play()
    }

    /*override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            if (intent.action == "pause naverno") {
                 stop()
                return Service.STOP_FOREGROUND_REMOVE
            }
        }
        return Service.START_STICKY
    }*/

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


        /*val intent = Intent(this, TickService::class.java)
        intent.action = "pause naverno"

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(NotificationChannel("metronome", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT))
            NotificationCompat.Builder(this, "metronome")
        } else
            NotificationCompat.Builder(this)


        startForeground(530,
                builder.setContentTitle("Metroman is playing")
                        .setContentText("Tap to stop this shit")
                        //.setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .build()
        )*/
    }

    fun stop() {
        stopSelf()
        clickConsumer = Consumer { click() }
        listener?.onControlsBlock(false)
        listener?.onStopClicking()
        clickDisposable?.dispose()
        notificationManager?.cancel(NOTIFICATION_ID)
        isPlaying = false
        beat = -1
    }
}