package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.isNonEmpty
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.data.db.AppDatabase
import com.peihua.touchmonitor.data.db.Factory
import com.peihua.touchmonitor.data.db.FactoryImpl
import com.peihua.touchmonitor.data.db.dao.HistoryDao
import com.peihua.touchmonitor.data.db.dao.SettingsDao
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.ui.AppProvider
import com.peihua.touchmonitor.ui.History
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.ui.settingsStore
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.getString
import com.peihua.touchmonitor.utils.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _settings = mutableStateListOf<AppModel>()
    val settingsState: MutableState<ResultData<List<AppModel>>> =
        mutableStateOf(ResultData.Initialize())
    val factory: Factory
        get() = FactoryImpl()
    val database: AppDatabase
        get() = factory.createRoomDatabase()
    val historyDao: HistoryDao
        get() = database.historyDao()
    val settingsDao: SettingsDao
        get() = database.settingsDao()

    /**
     * 查询数据
     */
    fun requestData(selectPackage: String? = null) {
        request(settingsState) {
            _settings.clear()
            val result = queryData(selectPackage)
            result.forEach { _settings.add(it) }
            _settings
        }
    }

    suspend fun queryData(selectPackage: String? = null): List<AppModel> {
        return withContext(Dispatchers.IO) {
            val values = AppProvider.entries
            val result = ArrayList<AppModel>()
            val historySettings = settingsDao.queryAll()
            val settings = runBlocking {
                settingsStore.data.first()
            }
            dLog { "queryData>>>historySettings:$historySettings" }
            dLog { "queryData>>>settings:$settings" }
            historySettings.forEach {
                val provider = values.find { value -> value.settings.packageName == it.packageName }
                val newModel = (provider ?: AppProvider.New).createModel(it)
                if (newModel != null) {
                    if (selectPackage.isNullOrEmpty()) {
                        newModel.isSelected = newModel.pkgName == settings.packageName
                    } else {
                        newModel.isSelected = newModel.pkgName == selectPackage
                    }
                    result.add(newModel)
                }
            }

            for (value in values) {
                val findModel = result.find { it.pkgName == value.settings.packageName }
                if (findModel != null) {
                    continue
                }
                val model = value.createModel(value.settings)
                if (model != null) {
                    if (selectPackage.isNullOrEmpty()) {
                        model.isSelected = model.pkgName == settings.packageName
                    } else {
                        model.isSelected = model.pkgName == selectPackage
                    }
                    result.add(model)
                }
            }
            val selectedModel = result.find { it.isSelected }
            //未选中，则表明result中不存在 settings存储的包名
            if (selectedModel == null) {
                val findModel =
                    if (selectPackage.isNonEmpty() && selectPackage != settings.packageName) {
                        val selSettings = settings.copy(packageName = selectPackage)
                        AppProvider.New.createModel(selSettings)
                    } else {
                        AppProvider.New.createModel(settings.copy())
                    }
                if (findModel != null) {
                    findModel.isHistory = true
                    findModel.isSelected = true
                    result.add(0, findModel)
                }
            } else {
                selectedModel.isHistory = true
            }

            result.add(
                0,
                AppModel(
                    AppProvider.ALL,
                    "",
                    getString(R.string.no_limit),
                    icon = application.packageManager.defaultActivityIcon,
                    settings = if (settings.packageName.isEmpty()) settings else AppProvider.ALL.settings
                )
            )
            result.add(
                AppModel(
                    AppProvider.Other,
                    "other",
                    getString(R.string.other),
                    icon = application.packageManager.defaultActivityIcon,
                    settings = AppProvider.Other.settings
                )
            )
            result
        }
    }

    var saveDbJob: Job? = null
    fun saveToDb(model: AppModel, isSaveToHistory: Boolean) {
        if (saveDbJob != null) {
            saveDbJob?.cancel()
            saveDbJob = null
        }
        saveDbJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            model.saveToDbSync()
            if (isSaveToHistory) {
                val findItem = historyDao.queryAll().find { it.packageName == model.pkgName }
                if (findItem != null) {
                    historyDao.update(
                        findItem.copy(
                            useCont = findItem.useCont + 1,
                        )
                    )
                } else {
                    val history = History(model.pkgName, 1)
                    historyDao.insert(history)
                }
            }
            val findSettingsItem = settingsDao.queryAll().find { it.packageName == model.pkgName }
            if (findSettingsItem != null) {
                settingsDao.update(model.settings)
            } else {
                settingsDao.insert(model.settings)
            }
        }
    }

    private fun AppProvider.createModel(settings: Settings): AppModel? {
        val pkgName = settings.packageName
        try {
            val packageManager = application.packageManager
            val appInfo = application.packageManager
                .getPackageInfo(pkgName, PackageManager.GET_META_DATA).applicationInfo
            val icon = appInfo?.loadIcon(packageManager)
            val displayName =
                appInfo?.loadLabel(packageManager)?.toString() ?: ""
            return AppModel(
                this,
                pkgName,
                displayName,
                icon ?: packageManager.defaultActivityIcon,
                appInfo = appInfo,
                settings = settings
            )
        } catch (e: Throwable) {
            e.dLog { "getPackageInfo error,${e.stackTraceToString()}" }
        }
        return null
    }
}