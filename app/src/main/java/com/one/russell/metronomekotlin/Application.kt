package com.one.russell.metronomekotlin

import android.app.Application

class Application : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getAppContext() {
        instance.applicationContext
    }

}