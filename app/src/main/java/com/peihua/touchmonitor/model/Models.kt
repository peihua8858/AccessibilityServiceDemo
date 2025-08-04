package com.peihua.touchmonitor.model

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName
import com.peihua.touchmonitor.ui.Constants
import com.peihua.touchmonitor.ui.theme.ThemeMode
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
) {
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

data class LogModel(
    val time: Long,
    val content: String,
    val path: String,
)

data class ApkModel(
    val displayName: String,
    val apkName: String,
    val path: String,
    val icon: Drawable?,
    val fileSize: Long = 0L,
    val packInfo: PackageInfo?,
) {
    val versionName: String
        get() = packInfo?.versionName ?: ""
}

data class SystemSettings (
    val theme: ThemeModel = ThemeModel(),
    val language: LanguageModel = LanguageModel.default,
    val exportPath: String = Constants.EXTERNAL_EXPORT_PATH,
) {
    companion object {
        val default: SystemSettings = SystemSettings(language = LanguageModel.default)
    }
}

data class ThemeModel(
    val theme: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
) {
    val index: Int
        get() = theme.index
    val image: ImageVector
        get() = theme.image

    @get:StringRes
    val nameIds: Int
        get() = theme.nameIds
    val model: ThemeMode
        get() = theme
}

data class LanguageModel(
    val name: String,
    val langCode: String,
    val countryCode: String = "",
) {
    companion object {
        val default: LanguageModel = LanguageModel("", "system")
    }
}