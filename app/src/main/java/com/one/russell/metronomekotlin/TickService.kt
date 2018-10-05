package com.one.russell.metronomekotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
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

    val clickObservable: PublishSubject<Long> = PublishSubject.create()
    var clickDisposable: Disposable? = null
    var clickConsumer = Consumer<Long> { click() }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
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

    fun setBpm(bpm: Int) {
        this.bpm = bpm
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun startTraining(startBpm: Int, endBpm: Int, bars: Int, increment: Int) {
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
        clickConsumer = Consumer { click() }
        listener?.onControlsBlock(false)
        clickDisposable?.dispose()
        isPlaying = false
        beat = -1
    }
}