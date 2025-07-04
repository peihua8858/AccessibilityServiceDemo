package com.peihua.touchmonitor.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

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