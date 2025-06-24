package com.peihua.touchmonitor

import android.app.Application
import com.peihua.touchmonitor.utils.LogCat

class ServiceApplication : Application() {
    companion object {
        private var app: ServiceApplication? = null
        @JvmStatic
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
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            LogCat.writeLog("error", e.stackTraceToString())
        }
    }
}