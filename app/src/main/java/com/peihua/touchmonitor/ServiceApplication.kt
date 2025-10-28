package com.peihua.touchmonitor

import android.app.Application
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.writeCrashLogFile
import com.peihua.touchmonitor.model.LanguageModel

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
         fun updateLanguage(language: LanguageModel) {
            dLog { "changeLanguage: $language" }
            setLocale(application, language.langCode)
        }

        fun setLocale(context: Context, language: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales =
                    LocaleList.forLanguageTags(language)
            } else {
                val locale = java.util.Locale(language)
                java.util.Locale.setDefault(locale)
                val resources = context.resources
                val configuration = resources.configuration
                configuration.setLocale(locale)
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
        }
    }
    private var oldDefaultExceptionHandler: Thread.UncaughtExceptionHandler? = null
    override fun onCreate() {
        super.onCreate()
        app = this
        this.oldDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            writeCrashLogFile { e.stackTraceToString() }
            e.printStackTrace()
            oldDefaultExceptionHandler?.uncaughtException(t, e)
        }
//        AppContext.apply { set(applicationContext) }
    }
}