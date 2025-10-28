package com.peihua.touchmonitor.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.peihua.compose.utils.fromHtml
import com.peihua.compose.utils.startStorageSettingsActivity
import com.peihua.touchmonitor.R
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


fun Context.checkPermissions(vararg permission: String): Boolean {
    if (permission.isEmpty()) {
        return true
    }
    for (p in permission) {
        if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

@OptIn(ExperimentalStdlibApi::class)
fun Context.isGrantedPermission(permission: String): Boolean {
    if (permission.isEmpty() || !isAtLeastM) {
        return true
    }
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

@OptIn(ExperimentalContracts::class)
fun Context.isGrantedPermission(vararg permissions: String): Boolean {
    contract { returns() }
    if (permissions.isEmpty() || !isAtLeastM) {
        return true
    }
    return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
}


@OptIn(ExperimentalContracts::class)
fun Context.isGrantedStoragePermission(): Boolean {
    if (isAtLeastR) {
        return Environment.isExternalStorageManager()
    }
    return isGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

fun isGrantedWindowPermission(context: Context?): Boolean {
    if (isAtLeastM) {
        return Settings.canDrawOverlays(context)
    }
    return true
}

@OptIn(ExperimentalContracts::class)
fun Context.checkStorgePermission(): Boolean {
    val result = isGrantedStoragePermission()
    if (!result) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_storage_title)
            .setMessage(getString(R.string.dialog_storage_message).fromHtml())
            .setPositiveButton(R.string.dialog_storage_positive) { _, _ ->
                startStorageSettingsActivity()
            }
            .setNegativeButton(R.string.dialog_storage_negative, null)
            .show()
    }
    return result
}