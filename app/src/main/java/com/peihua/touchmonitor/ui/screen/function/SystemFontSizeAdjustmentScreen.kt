package com.peihua.touchmonitor.ui.screen.function

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CustomSliderTips
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua8858.tools.utils.dLog
import kotlin.math.abs
import kotlin.math.roundToInt

private const val FONT_SCALE_MIN = 0.85f
private const val FONT_SCALE_MAX = 1.30f
private val fontScaleBuckets = floatArrayOf(0.85f, 1.0f, 1.15f, 1.30f)

private fun readFontScale(context: Context): Float =
    try {
        Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE, 1f)
    } catch (error: Exception) {
        dLog { "read FONT_SCALE failed: $error" }
        1f
    }

private fun snapToFontScale(value: Float): Float =
    fontScaleBuckets.minByOrNull { abs(it - value) } ?: 1f

@Composable
fun SystemFontSizeAdjustmentScreen(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colorScheme = MaterialTheme.colorScheme
    var canWrite by remember { mutableStateOf(Settings.System.canWrite(context)) }
    var currentScale by remember { mutableFloatStateOf(snapToFontScale(readFontScale(context))) }
    var sliderValue by remember { mutableFloatStateOf(currentScale) }

    // 从"修改系统设置"授权页返回时刷新权限与当前缩放值
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canWrite = Settings.System.canWrite(context)
                currentScale = snapToFontScale(readFontScale(context))
                sliderValue = currentScale
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val applyFontScale = { scale: Float ->
        try {
            Settings.System.putFloat(context.contentResolver, Settings.System.FONT_SCALE, scale)
            currentScale = scale
        } catch (error: Exception) {
            dLog { "write FONT_SCALE failed: $error" }
        }
    }

    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_system_font_size_adjustment)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!canWrite) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.errorContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.text_write_settings_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onErrorContainer
                    )
                    OutlinedButton(onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
                        } catch (error: Exception) {
                            dLog { "ACTION_MANAGE_WRITE_SETTINGS unsupported: $error" }
                        }
                    }) {
                        Text(text = stringResource(R.string.text_go_grant))
                    }
                }
            }
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = "字体 Aa",
                fontSize = (16 * sliderValue).sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
            CustomSliderTips(
                modifier = Modifier.fillMaxWidth(),
                value = sliderValue,
                title = stringResource(R.string.text_font_scale_current),
                titleOrientation = Orientation.Vertical,
                steps = 2,
                bubbleSize = DpSize(48.dp, 36.dp),
                thumbText = { "${(it * 100).roundToInt()}%" },
                valueRange = FONT_SCALE_MIN..FONT_SCALE_MAX,
                onChangValue = { sliderValue = it },
                // 拖动中只更新本地预览，松手才写系统值——每次写入都会触发全局配置变化并重建界面，拖动中写入会打断手势
                onValueChangeFinished = {
                    val snapped = snapToFontScale(sliderValue)
                    sliderValue = snapped
                    if (abs(currentScale - snapped) >= 0.01f) {
                        applyFontScale(snapped)
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    R.string.font_scale_small to 0.85f,
                    R.string.font_scale_standard to 1.0f,
                    R.string.font_scale_large to 1.15f,
                    R.string.font_scale_extra_large to 1.30f,
                ).forEach { (labelRes, scale) ->
                    val selected = abs(sliderValue - scale) < 0.01f
                    Text(
                        text = stringResource(labelRes) + "\n" + (scale * 100).roundToInt() + "%",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) colorScheme.primary
                        else colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = canWrite && abs(currentScale - 1f) >= 0.01f,
                onClick = {
                    sliderValue = 1f
                    applyFontScale(1f)
                }) {
                Text(text = stringResource(R.string.text_restore_default))
            }
        }
    }
}
