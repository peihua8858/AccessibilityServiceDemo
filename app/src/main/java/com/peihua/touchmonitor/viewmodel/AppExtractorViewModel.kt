package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
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
                    it.packageName,
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