package com.one.russell.metroman

import android.app.Application

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun getAppInstance(): Application {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}