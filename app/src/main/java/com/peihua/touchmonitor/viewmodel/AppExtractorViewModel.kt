package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.fz.common.file.getFileSize
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
                refreshAppList()
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

    val applications: MutableState<ResultData<List<AppInfoModel>>> =
        mutableStateOf(ResultData.Initialize())

    fun refreshAppList() {
        request(applications) {
            val packageManager = application.packageManager
            val appList = mutableListOf<AppInfoModel>()
            packageManager.getInstalledPackages(0).forEach {
                val appInfo = AppInfoModel(
                    it.applicationInfo?.loadLabel(packageManager).toString(),
                    it.packageName,
                    it.applicationInfo?.loadIcon(packageManager),
                    it
                )
                appList.add(appInfo)
            }
            appList
        }
    }

    override fun onCleared() {
        super.onCleared()
        application.unregisterReceiver(receiverApp)
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