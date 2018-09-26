package com.one.russell.metronomekotlin

import android.os.AsyncTask
import android.content.Context
import java.lang.ref.WeakReference

class ClickPlayerTask(context: Context, bpm: Int) : AsyncTask<Void, Void, Void>() {
    private var player: ClickPlayer? = null
    var weakActivity: WeakReference<Context> = WeakReference(context)

    var isPlaying = false
        private set

    init {
        val weakContext = weakActivity.get()
        if(weakContext != null) {
            player = ClickPlayer(weakContext)
            player?.setBPM(bpm)
        }
    }

    fun setBPM(bpm: Int) {
        player?.setBPM(bpm)
    }

    fun setBeatSize(size: Int) {
        player?.setBeatSize(size)
    }

    fun initSound() {
        player?.initSound()
    }

    fun stop() {
        isPlaying = false
    }

    override fun doInBackground(objects: Array<Void>): Void? {
        isPlaying = true
        while (isPlaying) {
            player?.play()
        }
        player?.release()
        return null
    }

}
