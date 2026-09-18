package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.peihua.touchmonitor.utils.showToast

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
                        stringResource(R.string.text_scale_ruler) to { navigateTo(AppRouter.ScaleRulerScreen) },
                        stringResource(R.string.text_compass) to { navigateTo(AppRouter.CompassScreen) },
                        stringResource(R.string.text_horizon) to { navigateTo(AppRouter.HorizonScreen) },
                        stringResource(R.string.text_angle_meter) to { navigateTo(AppRouter.AngleMeterScreen) },
                        stringResource(R.string.text_simple_paint) to { navigateTo(AppRouter.SimplePaintScreen) },
                        stringResource(R.string.text_led_subtitle) to { navigateTo(AppRouter.LedScreen) },
                        stringResource(R.string.text_time_screen) to { navigateTo(AppRouter.ScreenTimeScreen) },
                        stringResource(R.string.text_daily_60_seconds_early_report) to { navigateTo(AppRouter.DayNewsScreen) },
                    ),
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
                        stringResource(R.string.text_app_kit) to { navigateTo(AppRouter.AppManagerScreen) },
                        stringResource(R.string.text_check_screen_bad_point) to { navigateTo(AppRouter.ScreenDeadPixelsScreen) },
                        stringResource(R.string.text_see_device_info) to { navigateTo(Dialog.DeviceInfoScreen) },
                        stringResource(R.string.text_desktop_video_wallpaper) to { navigateTo(AppRouter.DesktopVideoScreen) },
                        //stringResource(R.string.text_system_font_size_adjustment) to { navigateTo(AppRouter.SystemFontSizeAdjustmentScreen) },
                    ),
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
                        stringResource(R.string.text_qr_code_generator) to { navigateTo(AppRouter.QrCodeGeneratorScreen) },
                        stringResource(R.string.text_nine_grid_cut) to { navigateTo(AppRouter.NineGridCutImageScreen) },
                        stringResource(R.string.text_nine_grid_picture_composite) to { navigateTo(AppRouter.NineGridPictureCompositeScreen) },
                        stringResource(R.string.text_photo_watermark) to { navigateTo(AppRouter.PhotoWatermarkScreen) },
                        stringResource(R.string.text_video_to_gif) to { navigateTo(AppRouter.VideoToGifScreen) },
                        stringResource(R.string.text_gif_image_decomposition) to { navigateTo(AppRouter.GifImageDecompositionScreen) },
                        stringResource(R.string.text_image_pixelization) to { navigateTo(AppRouter.ImagePixelizationScreen) },
                        stringResource(R.string.text_photo_to_sketch) to { navigateTo(AppRouter.PhotoToSketchScreen) },
                        stringResource(R.string.text_photo_to_black_and_white) to { navigateTo(AppRouter.PhotoToBlackAndWhiteScreen) },
                    ),
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
                        stringResource(R.string.text_m3u8_downloader) to { navigateTo(AppRouter.M3u8Downloader) },
                        stringResource(R.string.text_m3u8_downloader) + "2" to {
                            navigateTo2(AppRouter.M3u8Downloader, AppRouter.M3u8Downloader.TYPE to 1)
                        },
                    ),
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
    onClick: () -> Unit = {},
) {
    ScaleText(
        text = text,
        textAlign = TextAlign.Center,
        color = textColor,
        maxLines = 1,
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
            .clickable(onClick = onClick)
            .padding(
                start = 8.dp,
                top = 4.dp,
                end = 8.dp,
                bottom = 4.dp
            )
    )
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
    items: List<Pair<String, (() -> Unit)?>>,
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
        for ((index, item) in items.withIndex()) {
            ItemTextView(
                modifier = Modifier,
                text = item.first,
                textColor = textColor,
                backgroundColor = backgroundColor,
                onClick = {
                    item.second?.invoke() ?: showToast(R.string.text_function_developing)
                }
            )
        }
    }
}