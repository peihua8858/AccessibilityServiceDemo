package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.peihua.touchmonitor.data.settingsStore
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.ui.AppProvider
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _settings = mutableStateListOf<AppModel>()
    val settingsState: MutableState<ResultData<List<AppModel>>> =
        mutableStateOf(ResultData.Initialize())

    /**
     * 查询数据
     */
    fun requestData() {
        request(settingsState) {
            _settings.clear()
            val result = queryData()
            result.forEach { _settings.add(it) }
            _settings
        }
    }

    suspend fun queryData(): List<AppModel> {
        application.packageManager
        return withContext(Dispatchers.IO) {
            val values = AppProvider.entries
            val result = ArrayList<AppModel>()
            for (value in values) {
                val model = value.createModel(value.settings)
                if (model != null) {
                    result.add(model)
                }
            }
            val settings = runBlocking {
                settingsStore.data.first()
            }
            val findModel = result.find { it.pkgName == settings.packageName }
            findModel?.let {
                it.isHistory = true
                it.settings = settings
            }
            if (findModel == null) {
                val model = AppProvider.Other.createModel(settings)
                if (model != null) {
                    model.isHistory = true
                    result.add(0, model)
                }
            }
            result.add(
                0,
                AppModel(AppProvider.ALL, "", "不限制", settings = AppProvider.ALL.settings)
            )
            result.add(
                AppModel(
                    AppProvider.Other,
                    "other",
                    "其他",
                    settings = AppProvider.Other.settings
                )
            )
            result
        }
    }

    private fun AppProvider.createModel(settings: Settings): AppModel? {
        val pkgName = settings.packageName
        try {
            val appInfo = application.packageManager
                .getPackageInfo(pkgName, PackageManager.GET_META_DATA).applicationInfo
            val icon = appInfo?.loadIcon(application.packageManager)
            val displayName =
                appInfo?.loadLabel(application.packageManager)?.toString() ?: ""
            return AppModel(this, pkgName, displayName, icon,appInfo= appInfo, settings = settings)
        } catch (e: Throwable) {
            e.dLog { "getPackageInfo error,${e.stackTraceToString()}" }
        }
        return null
    }

    fun saveToDb(model: AppModel) {
        settingsStore.updateSettings(model.settings)
    }
}