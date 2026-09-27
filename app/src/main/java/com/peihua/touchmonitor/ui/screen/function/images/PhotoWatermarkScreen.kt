package com.peihua.touchmonitor.ui.screen.function.images


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.CustomSliderTips
import com.peihua.touchmonitor.ui.components.SliderDefaults
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.ui.screen.function.images.PhotoWatermarkScreen.WatermarkHorizontal
import com.peihua.touchmonitor.ui.screen.function.images.PhotoWatermarkScreen.WatermarkVertical
import com.peihua.touchmonitor.utils.rememberColorSaveable
import com.peihua.touchmonitor.utils.rememberFloatState
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.rememberState
import com.peihua8858.tools.file.createFileName
import com.peihua8858.tools.utils.adjustBitmapOrientation
import com.peihua8858.tools.utils.saveBitmapToGallery
import io.mhssn.colorpicker.ext.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * 图片水印
 */
object PhotoWatermarkScreen {
    enum class WatermarkHorizontal { Left, Center, Right }

    enum class WatermarkVertical { Top, Center, Bottom }
}

/**
 * 图片添加文字水印
 */
@Composable
fun PhotoWatermarkScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val showLoadingDialog = rememberShowProgressDialog()

    val selectedUri = rememberSaveable<Uri>(Uri.EMPTY)
    val watermarkText = rememberSaveable("")
    val watermarkColor = rememberColorSaveable(Color.White)
    val sizeRatio = rememberFloatState(6f)
    val opacity = rememberFloatState(80f)
    val horizontal = rememberState(WatermarkHorizontal.Right)
    val vertical = rememberState(WatermarkVertical.Bottom)
    val baseBitmap = rememberState<Bitmap?>(null)
    val resultDrawable = rememberState<BitmapDrawable?>(null)
    val isLoading = rememberSaveable(false)

    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoVisualMedia()) {
        if (it != null) {
            selectedUri.value = it
        }
    }

    // 源图只解码一次并缓存，避免每次拖动都重解码导致闪动
    fun loadSource(uri: Uri) {
        scope.launch {
            isLoading.value = true
            baseBitmap.value = uri.adjustBitmapOrientation()
            isLoading.value = false
        }
    }

    fun renderWatermark() {
        val base = baseBitmap.value ?: return
        scope.launch {
            isLoading.value = true
            try {
                resultDrawable.value = base.drawWatermark(
                    text = watermarkText.value,
                    color = watermarkColor.value,
                    // 滑块值 1..12 换算为图片宽度比例(1%~12%)
                    sizeRatio = sizeRatio.floatValue / 100f,
                    // 滑块值 1..255 换算为 0..1 透明度
                    opacity = (opacity.floatValue / 255f).coerceIn(0f, 1f),
                    horizontal = horizontal.value,
                    vertical = vertical.value,
                ).toDrawable(resources)
            } catch (error: Throwable) {
                error.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // 选中图片后只解码一次基础位图
    LaunchedEffect(selectedUri.value) {
        if (selectedUri.value != Uri.EMPTY) {
            loadSource(selectedUri.value)
        }
    }

    // 参数变化时消抖(150ms)后重绘水印，避免拖动滑块连续刷新导致闪动
    LaunchedEffect(
        baseBitmap.value,
        watermarkText.value,
        watermarkColor.value,
        sizeRatio.floatValue,
        opacity.floatValue,
        horizontal.value,
        vertical.value,
    ) {
        if (baseBitmap.value == null) return@LaunchedEffect
        delay(150)
        renderWatermark()
    }

    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_photo_watermark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
//                if (isLoading.value) {
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .size(80.dp),
//                        color = MaterialTheme.colorScheme.tertiary,
//                    )
//                }
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside,
                    model = resultDrawable.value,
                    contentDescription = "",
                )
            }
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = watermarkText.value,
                    onValueChange = {
                        watermarkText.value = it
                    },
                    label = { Text(text = stringResource(R.string.text_watermark_content_hint)) },
                )
                CustomSliderTips(
                    modifier = Modifier.padding(top = 8.dp),
                    title = stringResource(R.string.text_watermark_size),
                    value = sizeRatio.floatValue,
                    valueRange = 1f..12f,
                    thumbText = { "${it.roundToInt()}%" },
                ) {
                    sizeRatio.floatValue = it
                }
                CustomSliderTips(
                    modifier = Modifier.padding(top = 8.dp),
                    title = stringResource(R.string.text_watermark_opacity),
                    value = opacity.floatValue,
                    colors = SliderDefaults.colors().copy(
                        inactiveTickColor = MaterialTheme.colorScheme.secondaryContainer,
                        activeTickColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                    valueRange = 1f..255f,
                    thumbText = { "${((it / 255f) * 100).roundToInt()}%" },
                ) {
                    opacity.floatValue = it
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.text_watermark_color),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = watermarkColor.value.toHex(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            navigateTo2(
                                Dialog.ColorPickerDialog.route,
                                Dialog.TITLE to R.string.text_watermark_color,
                                Dialog.ColorPickerDialog.DEFAULT_COLOR to watermarkColor.value.toHex(),
                                Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                                    watermarkColor.value = color
                                    popBackStack()
                                }),
                            )
                        },
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.text_watermark_horizontal),
                        modifier = Modifier.weight(1f),
                    )
                    WatermarkPositionRow(
                        options = listOf(
                            WatermarkHorizontal.Left to R.string.text_left,
                            WatermarkHorizontal.Center to R.string.text_center,
                            WatermarkHorizontal.Right to R.string.text_right,
                        ),
                        selected = horizontal.value,
                        onSelect = { horizontal.value = it },
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.text_watermark_vertical),
                        modifier = Modifier.weight(1f),
                    )
                    WatermarkPositionRow(
                        options = listOf(
                            WatermarkVertical.Top to R.string.text_top,
                            WatermarkVertical.Center to R.string.text_center,
                            WatermarkVertical.Bottom to R.string.text_bottom,
                        ),
                        selected = vertical.value,
                        onSelect = { vertical.value = it },
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(modifier = Modifier.weight(1f), onClick = {
                        selectPhotoLauncher.launch(PhotoVisualMediaRequest(PhotoVisualMedia.ImageOnly))
                    }) {
                        Text(text = stringResource(R.string.text_select_photo))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(modifier = Modifier.weight(1f), onClick = {
                        val drawable = resultDrawable.value ?: return@Button
                        scope.launch {
                            showLoadingDialog.value = true
                            runCatching {
                                val contentResolver = context.contentResolver
                                val outFileName = "watermark_".createFileName("jpg")
                                contentResolver.saveBitmapToGallery(
                                    source = drawable.bitmap,
                                    title = outFileName,
                                    description = "",
                                )
                            }
                            showLoadingDialog.value = false
                        }
                    }) {
                        Text(text = stringResource(R.string.text_save_photo))
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> WatermarkPositionRow(
    options: List<Pair<T, Int>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (value, labelRes) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(labelRes),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

/**
 * 在原图上绘制文字水印
 */
private fun Bitmap.drawWatermark(
    text: String,
    color: Color,
    sizeRatio: Float,
    opacity: Float,
    horizontal: WatermarkHorizontal,
    vertical: WatermarkVertical,
): Bitmap {
    val result = copy(Bitmap.Config.ARGB_8888, true)
    if (text.isBlank()) return result
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.argb(
            (opacity.coerceIn(0f, 1f) * 255).roundToInt(),
            (color.red * 255).roundToInt(),
            (color.green * 255).roundToInt(),
            (color.blue * 255).roundToInt(),
        )
        this.textSize = width * sizeRatio.coerceIn(0.001f, 1f)
        this.typeface = Typeface.DEFAULT_BOLD
    }
    val fontMetrics = paint.fontMetrics
    val textWidth = paint.measureText(text)
    val textHeight = fontMetrics.descent - fontMetrics.ascent
    val padding = width * 0.04f
    val x = when (horizontal) {
        WatermarkHorizontal.Left -> padding
        WatermarkHorizontal.Center -> (width - textWidth) / 2f
        WatermarkHorizontal.Right -> width - padding - textWidth
    }
    val baseline = when (vertical) {
        WatermarkVertical.Top -> padding - fontMetrics.ascent
        WatermarkVertical.Center -> (height - textHeight) / 2f - fontMetrics.ascent
        WatermarkVertical.Bottom -> height - padding - fontMetrics.descent
    }
    canvas.drawText(text, x, baseline, paint)
    return result
}
