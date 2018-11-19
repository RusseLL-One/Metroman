package com.one.russell.metroman

import android.app.Application

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun getAppInstance(): Application {
            return instance
        }

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}