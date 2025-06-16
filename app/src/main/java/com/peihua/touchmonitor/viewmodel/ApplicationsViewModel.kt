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
import kotlinx.coroutines.delay

class ApplicationsViewModel(application: Application) : AndroidViewModel(application) {
    val applications: MutableState<ResultData<List<AppInfo>>> =
        mutableStateOf(ResultData.Initialize())

    fun requestData() {
        request(applications) {
            delay(3000)
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
}