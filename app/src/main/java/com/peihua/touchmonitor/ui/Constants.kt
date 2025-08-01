package com.peihua.touchmonitor.ui

import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.utils.externalStoragePath

object Constants {
    const val TAG = "TouchMonitor"

    /**
     * 导出路径
     */
    const val EXPORT_PATH = "Export"
    val ANDROID_DATA = ServiceApplication.application.filesDir.absolutePath

    /**
     * 内部存储路径
     * "Android/data/${BuildConfig.APPLICATION_ID}/files"
     */
    @JvmStatic
    val INTERNAL_PATH = ANDROID_DATA

    /**
     * 内部存储导出路径
     */
    @JvmStatic
    val INTERNAL_EXPORT_PATH = "$INTERNAL_PATH/$EXPORT_PATH"

    /**
     * 外部存储导出路径
     */
    @JvmStatic
    val EXTERNAL_EXPORT_PATH = "$externalStoragePath/$EXPORT_PATH"

    /**
     * 默认导出路径
     */
    val DEFAULT_EXPORT_PATH = "$externalStoragePath/$INTERNAL_EXPORT_PATH"
}