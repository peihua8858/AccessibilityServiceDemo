package com.peihua.touchmonitor

import android.app.Application
import com.peihua.touchmonitor.utils.writeLogFile

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
    private var oldDefaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    override fun onCreate() {
        super.onCreate()
        app = this
        this.oldDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            writeLogFile { e.stackTraceToString() }
            e.printStackTrace()
            oldDefaultExceptionHandler?.uncaughtException(t, e)
        }
    }
}