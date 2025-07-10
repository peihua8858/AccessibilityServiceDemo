package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.peihua.touchmonitor.model.AppInfo
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.request
import androidx.lifecycle.application
import com.peihua.touchmonitor.data.db.AppDatabase
import com.peihua.touchmonitor.data.db.Factory
import com.peihua.touchmonitor.data.db.FactoryImpl
import com.peihua.touchmonitor.data.db.dao.HistoryDao
import com.peihua.touchmonitor.data.db.dao.SettingsDao
import com.peihua.touchmonitor.ui.History
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.ui.settingsStore
import kotlinx.coroutines.delay

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {
    val applications: MutableState<ResultData<List<AppInfo>>> =
        mutableStateOf(ResultData.Initialize())
    val factory: Factory
        get() = FactoryImpl()
    val database: AppDatabase
        get() = factory.createRoomDatabase()
    val historyDao: HistoryDao
        get() = database.historyDao()
    val settingsDao: SettingsDao
        get() = database.settingsDao()

    fun requestData() {
        request(applications) {
            val packageManager = application.packageManager
            val mRetrieveFlags = PackageManager.MATCH_DISABLED_COMPONENTS or
                    PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS
            val applications = packageManager.getInstalledApplications(mRetrieveFlags)
            val appInfoList = mutableListOf<AppInfo>()
            for (applicationInfo in applications) {
                if (includeInCount(packageManager, applicationInfo)) {
                    val appName = applicationInfo.loadLabel(packageManager) as String
                    val packageName: String = applicationInfo.packageName
                    val appIcon = applicationInfo.loadIcon(packageManager)
                    val appInfo = AppInfo(
                        appName, packageName, appIcon,
                        packageInfo = packageManager.getPackageInfo(packageName, 0)
                    )
                    appInfoList.add(appInfo)
                }
            }
            appInfoList
        }
    }

    private fun includeInCount(
        pm: PackageManager,
        info: ApplicationInfo,
    ): Boolean {
        if ((info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true
        }
        if ((info.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true
        }
        val launchIntent = Intent(Intent.ACTION_MAIN, null)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(info.packageName)
        val intents = pm.queryIntentActivities(
            launchIntent,
            (PackageManager.MATCH_DISABLED_COMPONENTS
                    or PackageManager.MATCH_DIRECT_BOOT_AWARE
                    or PackageManager.MATCH_DIRECT_BOOT_UNAWARE)
        )
        return intents.isNotEmpty()
    }

    fun saveToDb(item: AppInfo) {
        // 保存到数据库
        request {
            val findItem = historyDao.queryAll().find { it.packageName == item.packageName }
            if (findItem != null) {
                historyDao.update(
                    findItem.copy(
                        useCont = findItem.useCont + 1,
                    )
                )
            } else {
                val history = History(item.packageName, 1)
                historyDao.insert(history)
            }
        }
        settingsStore.update {
            val findSettingsItem =
                settingsDao.queryAll().find { it.packageName == item.packageName }
            if (findSettingsItem != null) {
                settingsDao.update(findSettingsItem)
                findSettingsItem
            } else {
                Settings.default.copy(packageName = item.packageName)
            }
        }
    }
}