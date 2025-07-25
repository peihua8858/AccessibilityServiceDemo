package com.peihua.touchmonitor.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.fz.common.utils.showToast
import java.io.File
import androidx.core.net.toUri

fun Context.dimenOffset(dip: Int): Int {
    return resources.getDimensionPixelOffset(dip)
}

fun View.dimenOffset(dip: Int): Int {
    return resources.getDimensionPixelSize(dip)
}

fun View.getColor(color: Int): Int {
    return ContextCompat.getColor(context, color)
}

fun Context.registerReceiverCompat(
    receiver: BroadcastReceiver,
    filter: IntentFilter,
): Intent? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter)
    } else {
        registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )

    }
}

fun Context.finish() {
    (this as? android.app.Activity)?.finish()
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels
val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

object ContextExt {

    @JvmStatic
    fun Context.isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}

fun Context.installApk(apkPath: String) {
    installApk(File(apkPath))
}

fun Context.installApk(apkFile: File) {
    installApk(FileProvider.getUriForFile(this, "$packageName.provider", apkFile))
}

fun Context.installApk(uri: Uri) {
    try {
        val mediaType = "application/vnd.android.package-archive"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (Build.VERSION.SDK_INT < 29) {
            intent.setDataAndType(uri, mediaType)
        } else {
            intent.setDataAndType(uri, mediaType)
        }
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.installLocalApk(uri: Uri?) {
    val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    if (Build.VERSION.SDK_INT < 30) {
        intent.setData(uri)
        startActivity(intent)
    } else {
        val file = getFileFromUri(uri)
        if (file == null) {
            return
        }
        intent.setData(FileProvider.getUriForFile(this, "$packageName.provider", file))
        startActivity(intent)
    }
    finish()
}


fun Context.startStorageSettingsActivity(){
    if (isR) {
        val intent =Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.setData(("package:$packageName").toUri())
        try {
            startActivity(intent)
        } catch (e: Exception) {
            dLog { "fail e:${e.stackTraceToString()}" }
            try {
                intent.action =Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            } catch (e: Exception) {
                dLog { "fail e:${e.stackTraceToString()}" }
                intent.action =Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                startActivity(intent)
            }
        }
    } else if(isN) {
        val intent =Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(("package:$packageName").toUri())
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
    }
}