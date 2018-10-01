package com.one.russell.metronomekotlin

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class TickService : Service() {

    private val binder = ServiceBinder()
    private var listener: MainActivity.TickListener? = null

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    fun setTickListener(listener: MainActivity.TickListener) {
        this.listener = listener
    }

    internal inner class ServiceBinder : Binder() {
        val service: TickService
            get() = this@TickService
    }
}