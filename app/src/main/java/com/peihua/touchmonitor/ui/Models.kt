package com.peihua.touchmonitor.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.peihua.touchmonitor.data.settingsStore
import com.peihua.touchmonitor.ui.settings.AlipaySettings
import com.peihua.touchmonitor.ui.settings.AllSettings
import com.peihua.touchmonitor.ui.settings.DouYinHuoShanSettings
import com.peihua.touchmonitor.ui.settings.DouYinJiSuSettings
import com.peihua.touchmonitor.ui.settings.DouYinSettings
import com.peihua.touchmonitor.ui.settings.MeiTuanSettings
import com.peihua.touchmonitor.utils.dLog
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


enum class AppProvider(
    var settings: Settings,
    val contentView:
    @Composable (modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) -> Unit = { modifier, model, modelChange ->
        AllSettings(modifier, model, modelChange)
    },
) {
    ALL(
        settings = Settings("All", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            AllSettings(modifier, model, modelChange)
        }),

    /**
     * 支付宝
     */
    AliPay(
        settings = Settings("com.eg.android.AlipayGphone", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            AlipaySettings(modifier, model, modelChange)
        }),

    /**
     * 抖音
     */
    DouYin(
        settings = Settings("com.ss.android.ugc.aweme", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            DouYinSettings(modifier, model, modelChange)
        }),

    /**
     * 抖音火山版
     */
    DouYinHuoShan(
        settings = Settings("com.ss.android.ugc.live", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            DouYinHuoShanSettings(modifier, model, modelChange)
        }),

    /**
     * 抖音极速版
     */
    DouYinJiSu(
        settings = Settings("com.ss.android.ugc.aweme.lite", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            DouYinJiSuSettings(modifier, model, modelChange)
        }),

    /**
     * 美团
     */
    MeiTuan(
        settings = Settings("com.sankuai.meituan", Orientation.Vertical, true),
        contentView = { modifier, model, modelChange ->
            MeiTuanSettings(modifier, model, modelChange)
        }),

    /**
     * 今日头条极速版
     */
    ArticleLite(settings = Settings("com.ss.android.article.lite", Orientation.Vertical, true)),

    /**
     * 今日头条
     */
    ArticleNews(settings = Settings("com.ss.android.article.news", Orientation.Vertical, true)),

    /**
     * 抖音精选
     */
    Yumme(settings = Settings("com.ss.android.yumme.video", Orientation.Vertical, true)),

    /**
     * 西瓜视频
     */
    XiGuaShiPin(settings = Settings("com.ss.android.article.video", Orientation.Vertical, true)),

    /**
     * 其他应用程序
     */
    Other(settings = Settings("other", Orientation.Vertical, true)),
    ;
}

data class AppModel(
    val provider: AppProvider,
    val pkgName: String,
    var displayName: String,
    var icon: Drawable? = null,
    var isHistory: Boolean = false,
    var appInfo: ApplicationInfo? = null,
    var settings: Settings,
) {
    fun saveToDb() {
        settingsStore.updateSettings(settings)
    }
}


@Serializable
data class Settings(
    val packageName: String,
    val orientation: Orientation,
    val isDoubleSaver: Boolean,
)

@get:Composable
val Applications: ArrayList<AppModel>
    get() {
        val context = LocalContext.current
        val values = AppProvider.entries
        val result = ArrayList<AppModel>()
        for (value in values) {
            val pkgName = value.settings.packageName
            try {
                val appInfo = context.packageManager
                    .getPackageInfo(pkgName, PackageManager.GET_META_DATA).applicationInfo
                val icon = appInfo?.loadIcon(context.packageManager)
                val displayName =
                    appInfo?.loadLabel(context.packageManager)?.toString() ?: ""
                result.add(
                    AppModel(
                        value,
                        pkgName,
                        displayName,
                        icon,
                        appInfo = appInfo,
                        settings = value.settings
                    )
                )
            } catch (e: Throwable) {
                e.dLog { "getPackageInfo error,${e.stackTraceToString()}" }
            }
        }
        result.add(0, AppModel(AppProvider.ALL, "", "不限制", settings = AppProvider.ALL.settings))
        result.add(AppModel(AppProvider.Other, "", "Other", settings = AppProvider.Other.settings))
        return result

    }

val json = Json { ignoreUnknownKeys = true }