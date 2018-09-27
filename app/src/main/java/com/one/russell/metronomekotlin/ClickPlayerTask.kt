package com.one.russell.metronomekotlin

import android.os.AsyncTask
import android.support.v4.app.FragmentActivity
import java.lang.ref.WeakReference

class ClickPlayerTask(activity: FragmentActivity) : AsyncTask<Void, Void, Void>() {
    private var player: ClickPlayer? = null
    var weakActivity: WeakReference<FragmentActivity> = WeakReference(activity)

    var isPlaying = false
        private set

    init {
        val weakActivity = weakActivity.get()
        if(weakActivity != null) {
            player = ClickPlayer(weakActivity)
        }
    }

    fun setBeatSize(size: Int) {
        player?.setBeatSize(size)
    }

    fun setAccentSound(id: Int) {
        player?.setAccentSound(id)
    }

    fun setBeatSound(id: Int) {
        player?.setBeatSound(id)
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
