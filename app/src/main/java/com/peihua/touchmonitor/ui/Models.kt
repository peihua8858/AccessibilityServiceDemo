package com.peihua.touchmonitor.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.format.Formatter
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.DataStore
import com.peihua.touchmonitor.model.json
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.AlipaySettings
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.AllSettings
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.DouYinHuoShanSettings
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.DouYinJiSuSettings
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.DouYinSettings
import com.peihua.touchmonitor.ui.screen.function.autoScroller.settings.MeiTuanSettings
import com.peihua.touchmonitor.utils.formatToDate


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
    New(settings = Settings("new", Orientation.Vertical, true)),

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
    var isSelected: Boolean = false,
    var isHistory: Boolean = false,
    var appInfo: ApplicationInfo? = null,
    var settings: Settings,
) {
    fun saveToDb() {
        settingsStore.update(settings)
    }
}


@Entity(tableName = "Settings", indices = [Index(value = ["packageName"], unique = true)])
data class Settings(
    @PrimaryKey
    val packageName: String,
    val orientation: Orientation,
    @ColumnInfo(name = "isDoubleSaver", defaultValue = "false")
    val isDoubleSaver: Boolean,
    val delayTimes: MutableList<Int> = arrayListOf(),
    @ColumnInfo(name = "isRandomReverse", defaultValue = "false")
    val isRandomReverse: Boolean = false,
    @ColumnInfo(name = "isSkipAdOrLive", defaultValue = "false")
    val isSkipAdOrLive: Boolean = true,
    @ColumnInfo(name = "isBrightnessMin", defaultValue = "false")
    val isBrightnessMin: Boolean = false,
    @ColumnInfo(name = "isSoundMute", defaultValue = "false")
    val isSoundMute: Boolean = false,
) {
    companion object {
        val default: Settings = Settings("", Orientation.Vertical, true)
    }
}

@ProvidedTypeConverter
class ListToStringConverter {
    @TypeConverter
    fun StringToList(value: String): MutableList<Int> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun ListToString(value: MutableList<Int>?): String? {
        return json.encodeToString(value)
    }
}

@get:Synchronized
val settingsStore: DataStore<Settings> by lazy {
    DataStore(
        ServiceApplication.application,
        Settings.default,
        typeToken = object : TypeToken<Settings>() {},
    )
}

@Entity(tableName = "History", indices = [Index(value = ["packageName"], unique = true)])
data class History(
    @PrimaryKey
    val packageName: String,
    val useCont: Int = 0,
)

data class AppInfoModel(
    val name: String,
    var packageName: String,
    val icon: Drawable?,
    val packInfo: PackageInfo,
    val fileSize: Long=0L,
    val launchClass: String = "",
    val installSource: String=""
) {
    val versionName: String
        get() = packInfo.versionName?:"Unknown"
    val versionCode: Long
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packInfo.longVersionCode
        } else {
            packInfo.versionCode.toLong()
        }
    val applicationInfo: ApplicationInfo?
        get() = packInfo.applicationInfo
    val firstInstallTime: String
        get() = packInfo.firstInstallTime.formatToDate("yyyy-MM-dd HH:mm:ss")
    val lastUpdateTime: String
        get() = packInfo.lastUpdateTime.formatToDate("yyyy-MM-dd HH:mm:ss")
    val isSystemApp: Boolean
        get() = false
    val uid: Int
        get() = applicationInfo?.uid?:0
    val path: String
        get() = applicationInfo?.sourceDir?:""
    val lowApi: String
        get() = applicationInfo?.minSdkVersion.toString()
    val targetApi: String
        get() = applicationInfo?.targetSdkVersion.toString()

}