package com.peihua.touchmonitor.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.utils.showToast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import androidx.core.net.toUri

/**
 * type 以字符串落盘：项目 GsonFactory 的枚举适配器会把枚举序列化成 {}，
 * 枚举直接存会丢失类型导致多条收藏失效。
 */
object FavoriteTypeConstants {
    const val FUNCTION = "FUNCTION"
    const val URL = "URL"
}

data class FavoriteItem(
    val id: String = "",
    val type: String = FavoriteTypeConstants.FUNCTION,
    val name: String = "",
    val route: String = "",
    val url: String = "",
)

val FavoriteItem.isFunction: Boolean
    get() = type == FavoriteTypeConstants.FUNCTION

enum class FavoriteToggleResult { ADDED, REMOVED, LIMIT_REACHED }

object FavoriteStore {
    const val MAX_FAVORITE_COUNT = 5

    private val store = DataStore(
        application = ServiceApplication.application,
        default = emptyList<FavoriteItem>(),
        fileName = "Favorites.json",
        typeToken = object : TypeToken<List<FavoriteItem>>() {},
    )

    val itemsFlow: Flow<List<FavoriteItem>> = store.data

    suspend fun getItems(): List<FavoriteItem> = store.data.first()

    suspend fun toggleFunction(item: FavoriteItem): FavoriteToggleResult {
        val list = store.data.first()
        return when {
            list.any { it.id == item.id } -> {
                store.update { l -> l.filterNot { it.id == item.id } }
                FavoriteToggleResult.REMOVED
            }

            list.size >= MAX_FAVORITE_COUNT -> FavoriteToggleResult.LIMIT_REACHED

            else -> {
                store.update { l -> l + item }
                FavoriteToggleResult.ADDED
            }
        }
    }

    suspend fun addUrl(name: String, url: String): Boolean {
        if (store.data.first().size >= MAX_FAVORITE_COUNT) return false
        store.update { l ->
            l + FavoriteItem(
                id = UUID.randomUUID().toString(),
                type = FavoriteTypeConstants.URL,
                name = name,
                url = url,
            )
        }
        return true
    }

    fun remove(id: String) {
        store.update { list -> list.filterNot { it.id == id } }
    }
}

/**
 * 可被收藏的内置功能目录：收藏页按 id 找回功能并展示名称。
 * 新增可收藏功能时在此登记，FunctionScreen 的功能项也以此为准。
 */
data class FunctionFavoriteMeta(
    val id: String,
    val name: String,
    val route: String,
    val stringRes: Int,
)

object FunctionFavoriteCatalog {
    val items: List<FunctionFavoriteMeta> by lazy {
        val res = ServiceApplication.application.resources
        fun meta(
            id: String,
            route: AppRouter,
            stringRes: Int,
            suffix: String = "",
        ) = FunctionFavoriteMeta(
            id = id,
            name = res.getString(stringRes) + suffix,
            route = route.route,
            stringRes = stringRes,
        )

        listOf(
            meta("scaleRulerScreen", AppRouter.ScaleRulerScreen, R.string.text_scale_ruler),
            meta("compassScreen", AppRouter.CompassScreen, R.string.text_compass),
            meta("horizonScreen", AppRouter.HorizonScreen, R.string.text_horizon),
            meta("angleMeterScreen", AppRouter.AngleMeterScreen, R.string.text_angle_meter),
            meta(
                "numberBaseConverterScreen",
                AppRouter.NumberBaseConverterScreen,
                R.string.text_number_base_converter
            ),
            meta("simplePaintScreen", AppRouter.SimplePaintScreen, R.string.text_simple_paint),
            meta("ledScreen", AppRouter.LedScreen, R.string.text_led_subtitle),
            meta("screenTime", AppRouter.ScreenTimeScreen, R.string.text_time_screen),
            meta("dayNews", AppRouter.DayNewsScreen, R.string.text_daily_60_seconds_early_report),
            meta("appManager", AppRouter.AppManagerScreen, R.string.text_app_kit),
            meta("screenDeadPixels", AppRouter.ScreenDeadPixelsScreen, R.string.text_check_screen_bad_point),
            meta("deviceInfo", Dialog.DeviceInfoScreen, R.string.text_see_device_info),
            meta("desktopVideoScreen", AppRouter.DesktopVideoScreen, R.string.text_desktop_video_wallpaper),
            meta(
                "systemFontSizeAdjustmentScreen",
                AppRouter.SystemFontSizeAdjustmentScreen,
                R.string.text_system_font_size_adjustment
            ),
            meta("qrCodeGenerator", AppRouter.QrCodeGeneratorScreen, R.string.text_qr_code_generator),
            meta("mineGridCutImage", AppRouter.NineGridCutImageScreen, R.string.text_nine_grid_cut),
            meta(
                "nineGridPictureComposite",
                AppRouter.NineGridPictureCompositeScreen,
                R.string.text_nine_grid_picture_composite
            ),
            meta("photoWatermark", AppRouter.PhotoWatermarkScreen, R.string.text_photo_watermark),
            meta("videoToGif", AppRouter.VideoToGifScreen, R.string.text_video_to_gif),
            meta(
                "gifImageDecomposition",
                AppRouter.GifImageDecompositionScreen,
                R.string.text_gif_image_decomposition
            ),
            meta("imagePixelization", AppRouter.ImagePixelizationScreen, R.string.text_image_pixelization),
            meta("photoToSketch", AppRouter.PhotoToSketchScreen, R.string.text_photo_to_sketch),
            meta(
                "photoToBlackAndWhite",
                AppRouter.PhotoToBlackAndWhiteScreen,
                R.string.text_photo_to_black_and_white
            ),
            meta("m3u8Downloader", AppRouter.M3u8Downloader, R.string.text_m3u8_downloader),
            meta("m3u8Downloader_2", AppRouter.M3u8Downloader, R.string.text_m3u8_downloader, "2"),
        )
    }

    fun find(id: String): FunctionFavoriteMeta? = items.firstOrNull { it.id == id }
}

fun openFavorite(context: Context, item: FavoriteItem) {
    if (item.isFunction) {
        when {
            item.route == Dialog.DeviceInfoScreen.route -> navigateTo(Dialog.DeviceInfoScreen.route)
            FunctionFavoriteCatalog.find(item.id) != null -> navigateTo(item.route)
            else -> showToast(R.string.text_function_developing)
        }
    } else {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, item.url.toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure {
            if (it is ActivityNotFoundException) {
                showToast(R.string.text_collect_no_browser)
            }
        }
    }
}
