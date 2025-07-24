package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.fz.common.file.getFileSize
import com.peihua.touchmonitor.model.ApkModel
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.request
import java.io.File


class ApkViewModel(application: Application) : AndroidViewModel(application) {
    val apkList: MutableState<ResultData<MutableList<ApkModel>>> =
        mutableStateOf(ResultData.Initialize())

    fun getApkList() {
        request(apkList) {
            application.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                null,
                null,
                null,
                null
            )?.use { cursor ->
                val apkList = arrayListOf<ApkModel>()
                if (cursor.moveToFirst()) {
                    val dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                    val packageManager = application.packageManager
                    do {
                        try {
                            val apkPath = cursor.getString(dataIndex)
                            if (!apkPath.isNullOrBlank() && (apkPath.endsWith(".apk") || apkPath.contains(
                                    ".apk.1"
                                ))
                            ) {
                                val packageInfo = application.getApkInfo(apkPath)
                                if (packageInfo != null) {
                                    val applicationInfo = packageInfo.applicationInfo
                                    if (applicationInfo != null) {
                                        applicationInfo.sourceDir = apkPath
                                        applicationInfo.publicSourceDir = apkPath
                                        apkList.add(
                                            ApkModel(
                                                displayName = packageManager.getApplicationLabel(
                                                    applicationInfo
                                                ).toString(),
                                                apkName = File(apkPath).name,
                                                path = apkPath,
                                                icon = packageManager.getApplicationIcon(
                                                    applicationInfo
                                                ),
                                                fileSize = apkPath.getFileSize(),
                                                packInfo = packageInfo
                                            )
                                        )
                                    }
                                }else{
                                    apkList.add(
                                        ApkModel(
                                            displayName = "",
                                            apkName = File(apkPath).name,
                                            path = apkPath,
                                            icon = packageManager.defaultActivityIcon,
                                            fileSize = apkPath.getFileSize(),
                                            packInfo = null
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } while (cursor.moveToNext())
                }
                apkList
            } ?: arrayListOf()
        }
    }

    public fun Context.getApkInfo(apkPath: String): PackageInfo? {
        val packageManager = getPackageManager();
        val packageInfo =
            packageManager.getPackageArchiveInfo(apkPath, 0)
        return packageInfo;
    }

}