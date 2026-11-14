package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.data.FavoriteItem
import com.peihua.touchmonitor.data.FavoriteStore
import com.peihua.touchmonitor.data.FavoriteTypeConstants
import com.peihua.touchmonitor.data.FavoriteToggleResult
import com.peihua.touchmonitor.data.FunctionFavoriteMeta
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.AdaptiveContent
import com.peihua.touchmonitor.ui.components.ExtendedListTileNoBorder
import com.peihua.touchmonitor.ui.components.RotatingView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.theme.AppColor
import com.peihua.touchmonitor.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_function)
    ) {
        AdaptiveContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
            val colorScheme = MaterialTheme.colorScheme
            val textColor = colorScheme.onSecondaryContainer
            val textBgColor = colorScheme.secondaryContainer
            val bgContainerColor = colorScheme.surfaceContainerLow
            val favorites by FavoriteStore.itemsFlow.collectAsState(initial = emptyList())
            val scope = rememberCoroutineScope()
            val isFavorited = remember(favorites) {
                favorites.filter { it.type == FavoriteTypeConstants.FUNCTION }
                    .mapTo(HashSet()) { it.id }
            }
            @Composable
            fun fav(
                titleRes: Int,
                route: AppRouter,
                id: String,
                nameSuffix: String = "",
                onClick: () -> Unit,
            ) : FunctionChip {
                val meta = FunctionFavoriteMeta(id, stringResource(titleRes), route.route, titleRes)
                return FunctionChip(
                    meta = meta,
                    title = meta.name + nameSuffix,
                    onClick = onClick,
                    onLongClick = {
                        val item = FavoriteItem(
                            id = id,
                            type = FavoriteTypeConstants.FUNCTION,
                            name = meta.name,
                            route = route.route,
                        )
                        scope.launch {
                            val msgRes = when (FavoriteStore.toggleFunction(item)) {
                                FavoriteToggleResult.ADDED -> R.string.tip_collect_success
                                FavoriteToggleResult.REMOVED -> R.string.tip_uncollect_success
                                FavoriteToggleResult.LIMIT_REACHED -> R.string.tip_collect_max_limit
                            }
                            showToast(msgRes)
                        }
                    },
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_daily_tools),
                        painter = painterResource(id = R.drawable.ic_daily_tools),
                        tintColor = colorScheme.primary,
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        fav(R.string.text_scale_ruler, AppRouter.ScaleRulerScreen, "scaleRulerScreen") {
                            navigateTo(AppRouter.ScaleRulerScreen)
                        },
                        fav(R.string.text_compass, AppRouter.CompassScreen, "compassScreen") {
                            navigateTo(AppRouter.CompassScreen)
                        },
                        fav(R.string.text_horizon, AppRouter.HorizonScreen, "horizonScreen") {
                            navigateTo(AppRouter.HorizonScreen)
                        },
                        fav(R.string.text_angle_meter, AppRouter.AngleMeterScreen, "angleMeterScreen") {
                            navigateTo(AppRouter.AngleMeterScreen)
                        },
                        fav(R.string.text_simple_paint, AppRouter.SimplePaintScreen, "simplePaintScreen") {
                            navigateTo(AppRouter.SimplePaintScreen)
                        },
                        fav(R.string.text_led_subtitle, AppRouter.LedScreen, "ledScreen") {
                            navigateTo(AppRouter.LedScreen)
                        },
                        fav(R.string.text_time_screen, AppRouter.ScreenTimeScreen, "screenTime") {
                            navigateTo(AppRouter.ScreenTimeScreen)
                        },
                        fav(R.string.text_daily_60_seconds_early_report, AppRouter.DayNewsScreen, "dayNews") {
                            navigateTo(AppRouter.DayNewsScreen)
                        },
                    ),
                    isFavorited = isFavorited,
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_system_tool),
                        painter = painterResource(id = R.drawable.ic_system_tools),
                        tintColor = colorScheme.secondary,
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        fav(R.string.text_app_kit, AppRouter.AppManagerScreen, "appManager") {
                            navigateTo(AppRouter.AppManagerScreen)
                        },
                        fav(
                            R.string.text_check_screen_bad_point,
                            AppRouter.ScreenDeadPixelsScreen,
                            "screenDeadPixels"
                        ) {
                            navigateTo(AppRouter.ScreenDeadPixelsScreen)
                        },
                        fav(R.string.text_see_device_info, Dialog.DeviceInfoScreen, "deviceInfo") {
                            navigateTo(Dialog.DeviceInfoScreen)
                        },
                        fav(
                            R.string.text_desktop_video_wallpaper,
                            AppRouter.DesktopVideoScreen,
                            "desktopVideoScreen"
                        ) {
                            navigateTo(AppRouter.DesktopVideoScreen)
                        },
                        fav(
                            R.string.text_system_font_size_adjustment,
                            AppRouter.SystemFontSizeAdjustmentScreen,
                            "systemFontSizeAdjustmentScreen"
                        ) {
                            navigateTo(AppRouter.SystemFontSizeAdjustmentScreen)
                        },
                    ),
                    isFavorited = isFavorited,
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_photo_tools),
                        painter = painterResource(id = R.drawable.ic_photo_tools),
                        tintColor = colorScheme.tertiary,
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        fav(R.string.text_qr_code_generator, AppRouter.QrCodeGeneratorScreen, "qrCodeGenerator") {
                            navigateTo(AppRouter.QrCodeGeneratorScreen)
                        },
                        fav(R.string.text_nine_grid_cut, AppRouter.NineGridCutImageScreen, "mineGridCutImage") {
                            navigateTo(AppRouter.NineGridCutImageScreen)
                        },
                        fav(
                            R.string.text_nine_grid_picture_composite,
                            AppRouter.NineGridPictureCompositeScreen,
                            "nineGridPictureComposite"
                        ) {
                            navigateTo(AppRouter.NineGridPictureCompositeScreen)
                        },
                        fav(R.string.text_photo_watermark, AppRouter.PhotoWatermarkScreen, "photoWatermark") {
                            navigateTo(AppRouter.PhotoWatermarkScreen)
                        },
                        fav(R.string.text_video_to_gif, AppRouter.VideoToGifScreen, "videoToGif") {
                            navigateTo(AppRouter.VideoToGifScreen)
                        },
                        fav(
                            R.string.text_gif_image_decomposition,
                            AppRouter.GifImageDecompositionScreen,
                            "gifImageDecomposition"
                        ) {
                            navigateTo(AppRouter.GifImageDecompositionScreen)
                        },
                        fav(
                            R.string.text_image_pixelization,
                            AppRouter.ImagePixelizationScreen,
                            "imagePixelization"
                        ) {
                            navigateTo(AppRouter.ImagePixelizationScreen)
                        },
                        fav(R.string.text_photo_to_sketch, AppRouter.PhotoToSketchScreen, "photoToSketch") {
                            navigateTo(AppRouter.PhotoToSketchScreen)
                        },
                        fav(
                            R.string.text_photo_to_black_and_white,
                            AppRouter.PhotoToBlackAndWhiteScreen,
                            "photoToBlackAndWhite"
                        ) {
                            navigateTo(AppRouter.PhotoToBlackAndWhiteScreen)
                        },
                    ),
                    isFavorited = isFavorited,
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_video_tools),
                        painter = rememberVectorPainter(Icons.Default.VideoLibrary),
                        tintColor = colorScheme.primaryContainer,
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        fav(R.string.text_m3u8_downloader, AppRouter.M3u8Downloader, "m3u8Downloader") {
                            navigateTo(AppRouter.M3u8Downloader)
                        },
                        fav(
                            R.string.text_m3u8_downloader,
                            AppRouter.M3u8Downloader,
                            "m3u8Downloader_2",
                            nameSuffix = "2"
                        ) {
                            navigateTo2(AppRouter.M3u8Downloader, AppRouter.M3u8Downloader.TYPE to 1)
                        },
                    ),
                    isFavorited = isFavorited,
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
        }
    }
}
}

@Composable
private fun ItemTextView(
    modifier: Modifier,
    text: String,
    textColor: Color,
    backgroundColor: Color,
    favorited: Boolean,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(
                start = 8.dp,
                top = 4.dp,
                end = 8.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScaleText(
            text = text,
            textAlign = TextAlign.Center,
            color = textColor,
            maxLines = 1,
        )
        if (favorited) {
            Icon(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp),
                painter = painterResource(id = R.drawable.ic_star),
                tint = AppColor.color_ffd600,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun TitleView(text: String, painter: Painter, tintColor: Color, isExtended: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(
                start = 8.dp,
                end = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(24.dp),
            painter = painter,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = null
        )
        ScaleText(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
                .weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = tintColor,
            text = text,
        )
        // 旋转角度，up 旋转 180 度，down 旋转 0 度
        val rotationAngle = if (isExtended) 180f else 0f
        RotatingView(
            modifier = Modifier.align(Alignment.CenterVertically),
            tintColor = tintColor,
            rotationAngle = rotationAngle
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowList(
    modifier: Modifier = Modifier,
    items: List<FunctionChip>,
    isFavorited: Set<String>,
    textColor: Color,
    backgroundColor: Color,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE,
    overflow: FlowRowOverflow = FlowRowOverflow.Visible,
) {
    FlowRow(
        modifier = modifier.padding(
            start = 8.dp,
            end = 8.dp,
            bottom = 8.dp
        ),
        maxItemsInEachRow = maxItemsInEachRow,
        maxLines = maxLines,
        overflow = overflow
    ) {
        for (item in items) {
            ItemTextView(
                modifier = Modifier,
                text = item.title,
                textColor = textColor,
                backgroundColor = backgroundColor,
                favorited = item.meta.id in isFavorited,
                onClick = item.onClick,
                onLongClick = item.onLongClick,
            )
        }
    }
}

private class FunctionChip(
    val meta: FunctionFavoriteMeta,
    val title: String = meta.name,
    val onClick: () -> Unit,
    val onLongClick: () -> Unit,
)