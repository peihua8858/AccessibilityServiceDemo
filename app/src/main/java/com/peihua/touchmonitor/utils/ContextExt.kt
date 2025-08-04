package com.peihua.touchmonitor.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.fz.common.utils.showToast
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.screen.function.appmanager.FileItem
import java.io.File
import java.util.Locale


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
    installApk(apkFile.fileProvider)
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
        intent.setData(file.fileProvider)
        startActivity(intent)
    }
    finish()
}


fun Context.startStorageSettingsActivity() {
    if (isR) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.setData(("package:$packageName").toUri())
        try {
            startActivity(intent)
        } catch (e: Exception) {
            dLog { "fail e:${e.stackTraceToString()}" }
            try {
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            } catch (e: Exception) {
                dLog { "fail e:${e.stackTraceToString()}" }
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                startActivity(intent)
            }
        }
    } else if (isN) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(("package:$packageName").toUri())
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
    }
}

fun Context.shareCertainFiles(fileItem: FileItem) {
    dLog { "shareCertainFiles file:${fileItem.path}" }
    dLog { "shareCertainFiles file:${fileItem.contentUri}" }
    dLog { "shareCertainFiles file:${fileItem.getDocumentFile()?.uri}" }
    val uri = if (fileItem.isFileInstance) {
        val file = fileItem.getFile() ?: return
        if (file.absolutePath.lowercase(Locale.getDefault()).startsWith("/data/app")) {
            Uri.fromFile(file)
        } else {
            try {
                file.fileProvider
            } catch (e: Exception) {
                e.printStackTrace()
                Uri.fromFile(file)
            }
        }

    } else if (fileItem.isDocumentFile) {
        fileItem.getDocumentFile()?.uri
    } else if (fileItem.isShareUriInstance) {
        fileItem.contentUri
    } else null
    if (uri == null) return
    dLog { "shareCertainFiles file:${uri}" }
    shareCertainFiles(uri, getString(R.string.share_title))
}

fun Context.shareCertainFiles(filePath: String, title: String = "") {
    shareCertainFiles(File(filePath), title)
}

fun Context.shareCertainFiles(file: File, title: String = "") {
    shareCertainFiles(file.fileProvider, title)
}

fun Context.shareCertainFiles(uri: Uri, title: String = "") {
    shareCertainFiles(arrayListOf(uri), title)
}

fun Context.shareCertainFiles(uris: MutableList<Uri>, title: String) {
    if (uris.isEmpty()) return
    val intent = Intent()
    //intent.setType("application/vnd.android.package-archive");
    intent.setType("application/x-zip-compressed")
    if (uris.size > 1) {
        intent.setAction(Intent.ACTION_SEND_MULTIPLE)
        intent.putExtra(Intent.EXTRA_STREAM, ArrayList<Uri>(uris))
    } else {
        intent.setAction(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uris[0])
    }
    val tempTitle = title.ifEmpty { getString(R.string.share_title) }
    intent.putExtra(Intent.EXTRA_SUBJECT, tempTitle)
    intent.putExtra(Intent.EXTRA_TEXT, tempTitle)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        val chooser = Intent.createChooser(intent, "Share File")
        val resInfoList =
            this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            uris.forEach {
                this.grantUriPermission(
                    packageName,
                    it,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        chooser.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        showToast(e.toString())
    }
}