package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.peihua.compose.file.getFileSize
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.request

class AppExtractorViewModel(application: Application) : AndroidViewModel(application) {
    private val receiverApp = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_PACKAGE_ADDED == action
                || Intent.ACTION_PACKAGE_REMOVED == action
                || Intent.ACTION_PACKAGE_REPLACED == action
            ) {
                refreshAllAppList()
            }
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package")
        application.registerReceiver(receiverApp, intentFilter)
    }

    val userApplications: MutableState<ResultData<List<AppInfoModel>>> =
        mutableStateOf(ResultData.Initialize())
    val sysApplications: MutableState<ResultData<List<AppInfoModel>>> =
        mutableStateOf(ResultData.Initialize())

    fun requestUserAppList() {
        request(userApplications) {
            queryApplication(application.packageManager, AppType.USER)
//            val packageManager = application.packageManager
//            val appList = mutableListOf<AppInfoModel>()
//            packageManager.getInstalledPackages(0).forEach {
//                val applicationInfo = it.applicationInfo ?: return@forEach
//                val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
//                if (!isSystemApp) {
//                    val appInfo = AppInfoModel(
//                        name = it.applicationInfo?.loadLabel(packageManager).toString(),
//                        packageName = it.packageName,
//                        icon = it.applicationInfo?.loadIcon(packageManager),
//                        packInfo = it,
//                        fileSize = it.applicationInfo?.sourceDir.getFileSize()
//                    )
//                    appList.add(appInfo)
//                }
//            }
//            appList
        }
    }

    fun requestSystemAppList() {
        request(sysApplications) {
            queryApplication(application.packageManager, AppType.SYSTEM)
//            val packageManager = application.packageManager
//            val appList = mutableListOf<AppInfoModel>()
//            packageManager.getInstalledPackages(0).forEach {
//                val applicationInfo = it.applicationInfo ?: return@forEach
//                val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
//                if (isSystemApp) {
//                    val appInfo = AppInfoModel(
//                        name = it.applicationInfo?.loadLabel(packageManager).toString(),
//                        packageName = it.packageName,
//                        icon = it.applicationInfo?.loadIcon(packageManager),
//                        packInfo = it,
//                        fileSize = it.applicationInfo?.sourceDir.getFileSize()
//                    )
//                    appList.add(appInfo)
//                }
//            }
//            appList
        }
    }

    fun refreshAllAppList() {
        requestSystemAppList()
        requestUserAppList()
    }

    override fun onCleared() {
        super.onCleared()
        application.unregisterReceiver(receiverApp)
    }
    companion object {

        fun queryApplication(
            packageManager: PackageManager,
            type: AppType = AppType.ALL,
        ): MutableList<AppInfoModel> {
            val packageManager = packageManager
            val appList = mutableListOf<AppInfoModel>()
            packageManager.getInstalledPackages(0).forEach {
                val applicationInfo = it.applicationInfo ?: return@forEach
                val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val appInfo = AppInfoModel(
                    name = it.applicationInfo?.loadLabel(packageManager).toString(),
                    packageName = it.packageName,
                    icon = it.applicationInfo?.loadIcon(packageManager),
                    packInfo = it,
                    fileSize = it.applicationInfo?.sourceDir.getFileSize()
                )
                when (type) {
                    AppType.SYSTEM -> {
                        if (isSystemApp) {
                            appList.add(appInfo)
                        }
                    }

                    AppType.USER -> {
                        if (!isSystemApp) {
                            appList.add(appInfo)
                        }
                    }

                    AppType.ALL -> {
                        appList.add(appInfo)
                    }
                }
            }
            return appList
        }
    }
}

class AppDetailViewModel(application: Application) : AndroidViewModel(application) {
    val appInfo: MutableState<ResultData<AppInfoModel>> = mutableStateOf(ResultData.Initialize())
    fun refreshAppInfo(packageName: String) {
        request(appInfo) {
            val packageManager = application.packageManager
            val appInfo = packageManager.getPackageInfo(packageName, 0)
            AppInfoModel(
                appInfo.applicationInfo?.loadLabel(packageManager).toString(),
                packageName,
                appInfo.applicationInfo?.loadIcon(packageManager),
                appInfo,
                fileSize = appInfo.applicationInfo?.sourceDir.getFileSize(),
                launchClass = getLaunchClass(packageName),
                installSource = getInstallSource(packageName)
            )
        }
    }

    private fun getInstallSource(packageName: String): String {
        val packageManager = application.packageManager
        val installer_package_name = packageManager.getInstallerPackageName(packageName)
        val installer_name = packageManager.getAppNameByPackageName(installer_package_name)
        return installer_name.ifEmpty {
            if (!installer_package_name.isNullOrEmpty()) {
                installer_package_name
            } else {
                application.getString(R.string.word_unknown)
            }
        }
    }

    fun PackageManager.getAppNameByPackageName(packageName: String?): String {
        if (packageName.isNullOrEmpty()) {
            return ""
        }
        try {
            return getApplicationLabel(
                getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getLaunchClass(packageName: String): String {
        val packageManager = application.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return intent?.component?.className ?: application.getString(R.string.word_none)
    }


}

enum class AppType {
    ALL, USER, SYSTEM
}