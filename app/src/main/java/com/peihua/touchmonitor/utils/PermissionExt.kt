package com.peihua.touchmonitor.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.fz.common.utils.fromHtml
import com.peihua.touchmonitor.R

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

fun Context.checkStorgePermission(): Boolean {
    val result = if (isR) {
        checkPermissions(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    } else {
        return checkPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    if (!result) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_storage_title)
            .setMessage(getString(R.string.dialog_storage_message).fromHtml())
            .setPositiveButton(R.string.dialog_storage_positive) { _, _ ->
                if (isR) {
                    startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION))
                } else {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
            .setNegativeButton(R.string.dialog_storage_negative, null)
            .show()
    }
    return result
}