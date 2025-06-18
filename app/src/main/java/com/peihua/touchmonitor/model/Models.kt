package com.peihua.touchmonitor.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.json.Json

data class SettingsModel(
    val name: String,
    @SerializedName("package")
    val packageName: String,
    val displayName: String,
)

data class AppInfo(
    val name: String,
    var packageName: String,
    val icon: Drawable?,
    var isHistory: Boolean = false,
    var isSystemApp: Boolean = false,
    val packageInfo: PackageInfo?,
){
    init {
        if (packageInfo != null) {
            packageName = packageInfo.packageName
            if (packageInfo.applicationInfo != null) {
                isSystemApp =
                    (packageInfo.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            }
        }
    }
}

val json = Json { ignoreUnknownKeys = true }