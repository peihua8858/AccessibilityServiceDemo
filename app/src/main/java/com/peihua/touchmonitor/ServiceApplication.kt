package com.peihua.touchmonitor

import android.app.Application

class ServiceApplication : Application() {
    companion object {
        private var app: ServiceApplication? = null
        val application: ServiceApplication
            get() {
                if (app == null) {
                    throw NullPointerException()
                }
                return app!!
            }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}