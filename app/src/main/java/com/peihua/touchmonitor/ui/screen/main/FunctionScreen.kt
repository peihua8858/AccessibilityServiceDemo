package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.ExtendedListTileNoBorder
import com.peihua.touchmonitor.ui.components.RotatingView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.ui.theme.labelSmallNormal
import com.peihua.touchmonitor.utils.showToast

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_function)
    ) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dp_16))
        ) {
            val isDarkTheme = isSystemInDarkTheme()
            val textColor = if (isDarkTheme) Colors.Grey[300] else Colors.Red[300]
            val textBgColor = if (isDarkTheme) Colors.Grey[700] else Colors.Red[50]
            val bgContainerColor = if (isDarkTheme) Colors.Grey[900] else Color.White
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_daily_tools),
                        painter = painterResource(id = R.drawable.ic_daily_tools),
                        tintColor = Colors.Red[400],
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        stringResource(R.string.text_scale_ruler) to null,
                        stringResource(R.string.text_compass) to null,
                        stringResource(R.string.text_horizon) to null,
                        stringResource(R.string.text_angle_meter) to null,
                        stringResource(R.string.text_simple_paint) to null,
                        stringResource(R.string.text_led_subtitle) to null,
                        stringResource(R.string.text_time_screen) to { navigateTo(AppRouter.ScreenTimeScreen) },
                        stringResource(R.string.text_daily_60_seconds_early_report) to { navigateTo(AppRouter.DayNewsScreen) },
                    ),
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.dp_16))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_system_tool),
                        painter = painterResource(id = R.drawable.ic_system_tools),
                        tintColor = Colors.Indigo[600],
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        stringResource(R.string.text_app_kit) to { navigateTo(AppRouter.AppManagerScreen) },
                        stringResource(R.string.text_check_screen_bad_point) to { navigateTo(AppRouter.ScreenDeadPixelsScreen) },
                        stringResource(R.string.text_see_device_info) to { navigateTo(Dialog.DeviceInfoScreen) },
//                        stringResource(R.string.text_desktop_video_wallpaper) to null,
//                        stringResource(R.string.text_system_font_size_adjustment) to null,
                    ),
                    textColor = textColor,
                    backgroundColor = textBgColor
                )
            }
            ExtendedListTileNoBorder(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.dp_16))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_photo_tools),
                        painter = painterResource(id = R.drawable.ic_photo_tools),
                        tintColor = Colors.Green[600],
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        stringResource(R.string.text_qr_code_generator) to { navigateTo(AppRouter.QrCodeGeneratorScreen) },
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
                    .padding(top = dimensionResource(id = R.dimen.dp_16))
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)))
                    .background(
                        bgContainerColor,
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))
                    ),
                isExtended = true,
                title = { isExtended ->
                    TitleView(
                        text = stringResource(R.string.text_video_tools),
                        painter = rememberVectorPainter(Icons.Default.VideoLibrary),
                        tintColor = Colors.Blue[600],
                        isExtended = isExtended
                    )
                }) {
                FlowRowList(
                    modifier = Modifier,
                    items = listOf(
                        stringResource(R.string.text_m3u8_downloder) to { navigateTo(AppRouter.M3u8Downloader) },
                        stringResource(R.string.text_m3u8_downloder) + "2" to {
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
        style = MaterialTheme.typography.labelSmallNormal,
        maxLines = 1,
        modifier = modifier
            .padding(
                top = dimensionResource(id = R.dimen.dp_8),
                start = dimensionResource(id = R.dimen.dp_8),
                end = dimensionResource(id = R.dimen.dp_8)
            )
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)))
            .background(
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)),
                color = backgroundColor
            )
            .clickable(onClick = onClick)
            .padding(
                start = dimensionResource(id = R.dimen.dp_8),
                top = dimensionResource(id = R.dimen.dp_4),
                end = dimensionResource(id = R.dimen.dp_8),
                bottom = dimensionResource(id = R.dimen.dp_4)
            )
    )
}

@Composable
private fun TitleView(text: String, painter: Painter, tintColor: Color, isExtended: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(id = R.dimen.dp_56))
            .padding(
                start = dimensionResource(id = R.dimen.dp_8),
                end = dimensionResource(id = R.dimen.dp_8)
            ),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(dimensionResource(id = R.dimen.dp_24)),
            painter = painter,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = null
        )
        ScaleText(
            modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.dp_8))
                .align(Alignment.CenterVertically)
                .weight(1f),
            style = MaterialTheme.typography.labelLargeNormal,
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
            start = dimensionResource(id = R.dimen.dp_8),
            end = dimensionResource(id = R.dimen.dp_8),
            bottom = dimensionResource(id = R.dimen.dp_8)
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