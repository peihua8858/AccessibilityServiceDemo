package com.peihua.touchmonitor.utils

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * 获取外部存储主路径
 */
val externalStoragePath: String
    get() {
        try {
            return Environment.getExternalStorageDirectory().absolutePath
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ""
    }

val Context.appExternalStoragePath: String
    get() {
        val externalFilesDirs = getExternalFilesDirs(null)
        if (externalFilesDirs != null && externalFilesDirs.size > 0) {
            return externalFilesDirs.getOrNull(0)?.absolutePath ?: ""
        }
        return ""
    }