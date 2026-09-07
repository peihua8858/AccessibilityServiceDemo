package com.peihua.touchmonitor

import android.app.Application
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.fz.imageloader.glide.ImageGlideFetcher
import com.peihua.touchmonitor.data.download.DownloadContainer
import com.peihua.touchmonitor.model.LanguageModel
import com.peihua8858.tools.log.Logcat
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import com.peihua8858.tools.utils.writeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        com.fz.imageloader.ImageLoader.getInstance().createProcessor(ImageGlideFetcher())
        this.oldDefaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Logcat.writeLog(this, "", e.stackTraceToString())
            e.printStackTrace()
            oldDefaultExceptionHandler?.uncaughtException(t, e)
        }
        // 进程被杀后 DB 里残留的 RUNNING 是脏状态，否则 UI 会显示一堆假的"运行中"
        CoroutineScope(Dispatchers.IO).launch {
            runCatching { DownloadContainer.repository.markAllInterrupted() }
                .onFailure { eLog { "重置中断任务状态失败：${it.message}" } }
        }
//        AppContext.apply { set(applicationContext) }
    }
}