package com.peihua.touchmonitor.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.peihua.touchmonitor.ui.settings.AlipaySettings
import com.peihua.touchmonitor.ui.settings.AllSettings
import com.peihua.touchmonitor.ui.settings.DouYinHuoShanSettings
import com.peihua.touchmonitor.ui.settings.DouYinJiSuSettings
import com.peihua.touchmonitor.ui.settings.DouYinSettings
import com.peihua.touchmonitor.ui.settings.MeiTuanSettings
import com.peihua.touchmonitor.utils.WorkScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath


enum class AppProvider(
    var model: AppModel,
    val contentView:
    @Composable (modifier: Modifier, provider: AppProvider, modelChange: (AppProvider) -> Unit) -> Unit = { modifier, provider, modelChange ->
        AllSettings(modifier, provider, modelChange)
    },
) {
    ALL(
        AppModel(
            "",
            "全部",
            "",
            settings = Settings("", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            AllSettings(modifier, provider, modelChange)
        }),

    /**
     * 支付宝
     */
    AliPay(
        AppModel(
            "com.eg.android.AlipayGphone", "支付宝", "",
            settings = Settings("com.eg.android.AlipayGphone", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            AlipaySettings(modifier, provider, modelChange)
        }),

    /**
     * 抖音
     */
    DouYin(
        AppModel(
            "com.ss.android.ugc.aweme",
            "抖音",
            "",
            settings = Settings("com.ss.android.ugc.aweme", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            DouYinSettings(modifier, provider, modelChange)
        }),

    /**
     * 抖音火山版
     */
    DouYinHuoShan(
        AppModel(
            "com.ss.android.ugc.live",
            "抖音火山版",
            "",
            settings = Settings("com.ss.android.ugc.live", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            DouYinHuoShanSettings(modifier, provider, modelChange)
        }),

    /**
     * 抖音极速版
     */
    DouYinJiSu(
        AppModel(
            "com.ss.android.ugc.aweme.lite",
            "抖音极速版",
            "",
            settings = Settings("com.ss.android.ugc.aweme.lite", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            DouYinJiSuSettings(modifier, provider, modelChange)
        }),

    /**
     * 美团
     */
    MeiTuan(
        AppModel(
            "com.sankuai.meituan",
            "美团",
            "",
            settings = Settings("com.sankuai.meituan", Orientation.Vertical, true)
        ),
        contentView = { modifier, provider, modelChange ->
            MeiTuanSettings(modifier, provider, modelChange)
        }),

    /**
     * 今日头条极速版
     */
    ArticleLite(
        AppModel(
            "com.ss.android.article.lite",
            "今日头条极速版",
            "",
            settings = Settings("com.ss.android.article.lite", Orientation.Vertical, true)
        ),
    ),

    /**
     * 今日头条
     */
    ArticleNews(
        AppModel(
            "com.ss.android.article.news",
            "今日头条",
            "",
            settings = Settings("com.ss.android.article.news", Orientation.Vertical, true)
        )
    ),

    /**
     * 抖音精选
     */
    yumme(
        AppModel(
            "com.ss.android.yumme.video",
            "抖音精选",
            "",
            settings = Settings("com.ss.android.yumme.video", Orientation.Vertical, true)
        )
    ),

    /**
     * 西瓜视频
     */
    XiGuaShiPin(
        AppModel(
            "com.ss.android.article.video",
            "西瓜视频",
            "",
            settings = Settings("com.ss.android.article.video", Orientation.Vertical, true)
        ),
    ),

    ;

    val displayName: String
        get() = model.displayName
}

data class AppModel(
    val pkgName: String,
    var displayName: String,
    val model: String,
    var icon: Drawable? = null,
    var appInfo: ApplicationInfo? = null,
    var settings: Settings
) {
    fun saveToDb() {
        DataManager.saveSettings(settings)
    }
}


@Serializable
data class Settings(
    val packageName: String,
    val orientation: Orientation,
    val isDoubleSaver: Boolean,
)

class SettingsStore(private val storePath: String) : CoroutineScope by WorkScope() {
    private val storeFile = "$storePath/settings.json"
    private val db = DataStoreFactory.create(
        storage = OkioStorage<Settings>(
            fileSystem = FileSystem.SYSTEM,
            serializer = SettingsJsonSerializer,
            producePath = {
                storeFile.toPath()
            },
        ),
    )
    val data: Flow<Settings> = db.data
    fun getData(block: (Settings) -> Unit) {
        launch {
            data.collect {
                block(it)
            }
        }
    }

    suspend fun getResult(): List<Settings> = data.toList()

    fun updateSettings(settings: Settings) {
        launch {
            db.updateData { settings }
        }
    }

    internal object SettingsJsonSerializer : OkioSerializer<Settings> {
        override val defaultValue: Settings
            get() = Settings("", Orientation.Vertical, true)

        override suspend fun readFrom(source: BufferedSource): Settings {
            return json.decodeFromString<Settings>(source.readUtf8())
        }

        override suspend fun writeTo(
            t: Settings,
            sink: BufferedSink,
        ) {
            sink.use {
                it.writeUtf8(json.encodeToString(Settings.serializer(), t))
            }
        }
    }
}

@get:Composable
val App_Models: ArrayList<AppProvider>
    get() {
        val context = LocalContext.current
        val values = AppProvider.entries
        val result = ArrayList<AppProvider>()
        for (value in values) {
            val pkgName = value.model.pkgName
            try {
                val appInfo = context.packageManager
                    .getPackageInfo(pkgName, PackageManager.GET_META_DATA).applicationInfo
                value.model.appInfo = appInfo
                value.model.apply {
                    icon = appInfo?.loadIcon(context.packageManager)
                    displayName = appInfo?.loadLabel(context.packageManager)?.toString() ?: displayName
                }
                result.add(value)
            } catch (e: Exception) {

            }
        }
        result.add(0, AppProvider.ALL)
        return result
    }

val json = Json { ignoreUnknownKeys = true }