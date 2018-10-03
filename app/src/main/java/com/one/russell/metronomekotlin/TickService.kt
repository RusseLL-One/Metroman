package com.one.russell.metronomekotlin

import android.app.Service
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
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
    private var prevBpm:Int = 0

    val clickObservable: PublishSubject<Long> = PublishSubject.create()
    var clickDisposable: Disposable? = null

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    fun setTickListener(listener: MainActivity.TickListener) {
        this.listener = listener
    }

    fun setAccentSound(id: Int?) {
        if(id != null) {
            clickPlayer.setAccentSound(id)
        }
    }

    fun setBeatSound(id: Int?) {
        if(id != null) {
            clickPlayer.setBeatSound(id)
        }
    }

    fun setBeatSize(size: Int) {
        clickPlayer.setBeatSize(size)
    }

    internal inner class ServiceBinder : Binder() {
        val service: TickService
            get() = this@TickService
    }

    fun click() {
        Log.d("qwe", "click method, thread:" + Thread.currentThread().name)
        listener?.onTick(true, 60000 / bpm)
        if(prevBpm != bpm) {
            clickObservable.onNext((60000 / bpm).toLong())
            prevBpm = bpm
        }
        clickPlayer.play()
    }

    fun setBpm(bpm: Int) {
        this.bpm = bpm
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun play() {

        clickDisposable = clickObservable.switchMap { interval ->
            Observable.interval(interval, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { click() }
        }.subscribe()

        clickObservable.onNext(0L)
        isPlaying = true
    }

    fun stop() {
        clickDisposable?.dispose()
        isPlaying = false
    }
}