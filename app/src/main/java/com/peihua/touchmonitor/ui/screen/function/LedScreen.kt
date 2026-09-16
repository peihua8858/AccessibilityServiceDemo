package com.peihua.touchmonitor.ui.screen.function

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.rememberColorSaveable
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.showToast
import com.peihua.touchmonitor.utils.toHex
import com.peihua8858.tools.activity.findActivity
import kotlinx.coroutines.isActive

private enum class LedMode { Normal, Scrolling }

@Composable
fun LedScreen(modifier: Modifier) {
    var text by rememberSaveable("")
    var mode by rememberSaveable(LedMode.Normal)
    val backgroundColor = rememberColorSaveable(Color.Black)
    val textColor = rememberColorSaveable(Color.White)
    var fontSize by rememberSaveable(112f)
    var scrollingSpeed by rememberSaveable(100f)
    var isPlaying by rememberSaveable(false)

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val view = LocalView.current
    var originalOrientation by rememberSaveable(activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

    DisposableEffect(isPlaying, activity, view) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        if (isPlaying) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            insetsController?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = true
        } else {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
        }
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
            view.keepScreenOn = false
            if (activity?.isChangingConfigurations != true) {
                activity?.requestedOrientation = originalOrientation
            }
        }
    }

    BackHandler(enabled = isPlaying) {
        isPlaying = false
    }

    if (isPlaying) {
        LedPlayback(
            modifier = modifier,
            text = text,
            mode = mode,
            backgroundColor = backgroundColor.value,
            textColor = textColor.value,
            fontSize = fontSize,
            scrollingSpeed = scrollingSpeed
        )
        return
    }

    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = { popBackStack() },
        title = stringResource(R.string.text_led_subtitle)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.replace("\n", "") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.text_led_input_hint)) },
                    placeholder = { Text(stringResource(R.string.text_led_input_hint)) },
                    leadingIcon = {
                        Text(
                            text = "Tᵀ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(onClick = { text = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.text_clear)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp)
                )

                LedModeSelector(
                    mode = mode,
                    onModeChange = { mode = it }
                )

                ColorSettingRow(
                    label = stringResource(R.string.text_led_background_color),
                    color = backgroundColor.value,
                    onClick = {
                        navigateTo2(
                            Dialog.ColorPickerDialog.route,
                            Dialog.TITLE to R.string.text_led_background_color,
                            Dialog.ColorPickerDialog.DEFAULT_COLOR to backgroundColor.value.toHex(),
                            Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                                backgroundColor.value = color
                                popBackStack()
                            })
                        )
                    }
                )

                ColorSettingRow(
                    label = stringResource(R.string.text_led_text_color),
                    color = textColor.value,
                    onClick = {
                        navigateTo2(
                            Dialog.ColorPickerDialog.route,
                            Dialog.TITLE to R.string.text_led_text_color,
                            Dialog.ColorPickerDialog.DEFAULT_COLOR to textColor.value.toHex(),
                            Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                                textColor.value = color
                                popBackStack()
                            })
                        )
                    }
                )

                SliderSettingRow(
                    label = stringResource(R.string.text_led_font_size),
                    value = fontSize,
                    valueRange = 32f..240f,
                    onValueChange = { fontSize = it }
                )

                if (mode == LedMode.Scrolling) {
                    SliderSettingRow(
                        label = stringResource(R.string.text_led_scrolling_speed),
                        value = scrollingSpeed,
                        valueRange = 30f..360f,
                        onValueChange = { scrollingSpeed = it }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    if (text.isBlank()) {
                        showToast(R.string.text_led_empty)
                    } else {
                        originalOrientation = activity?.requestedOrientation
                            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        isPlaying = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Text(
                    text = stringResource(R.string.text_ok),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LedModeSelector(
    mode: LedMode,
    onModeChange: (LedMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        LedModeItem(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.text_led_normal_mode),
            selected = mode == LedMode.Normal,
            onClick = { onModeChange(LedMode.Normal) }
        )
        LedModeItem(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.text_led_scrolling_mode),
            selected = mode == LedMode.Scrolling,
            onClick = { onModeChange(LedMode.Scrolling) }
        )
    }
}

@Composable
private fun LedModeItem(
    modifier: Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (selected) selectedColor.copy(alpha = 0.08f) else Color.Transparent
            )
            .then(
                if (selected) Modifier.border(1.dp, selectedColor)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun ColorSettingRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

@Composable
private fun SliderSettingRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(end = 18.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                valueRange = valueRange
            )
        }
    }
}

@Composable
private fun LedPlayback(
    modifier: Modifier,
    text: String,
    mode: LedMode,
    backgroundColor: Color,
    textColor: Color,
    fontSize: Float,
    scrollingSpeed: Float,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (mode == LedMode.Normal) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center
            )
        } else {
            ScrollingLedText(
                text = text,
                color = textColor,
                fontSize = fontSize,
                speed = scrollingSpeed
            )
        }
    }
}

@Composable
private fun ScrollingLedText(
    text: String,
    color: Color,
    fontSize: Float,
    speed: Float,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val screenWidth = with(density) { maxWidth.toPx() }
        var textWidth by remember { mutableIntStateOf(0) }
        val offset = remember { mutableFloatStateOf(screenWidth) }

        LaunchedEffect(screenWidth, speed, text, fontSize) {
            // 与旧 tween 时长换算保持一致的实际滚动速度
            val pixelsPerSecond = with(density) { speed.dp.toPx() } / 0.3f
            offset.floatValue = screenWidth
            var lastFrame = 0L
            while (isActive) {
                withFrameNanos { frame ->
                    val width = textWidth
                    val elapsed = if (lastFrame == 0L) 0f else (frame - lastFrame) / 1_000_000_000f
                    lastFrame = frame
                    if (width > 0) {
                        var next = offset.floatValue - pixelsPerSecond * elapsed.coerceAtMost(0.05f)
                        if (next <= -width) next += screenWidth + width
                        offset.floatValue = next
                    }
                }
            }
        }

        Text(
            text = text,
            modifier = Modifier.graphicsLayer {
                translationX = offset.floatValue
                // 超大字号的字形每帧重新光栅化会掉帧，这里让文字缓存成离屏图层，逐帧只做位移
                compositingStrategy = CompositingStrategy.Offscreen
            },
            color = color,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { textWidth = it.size.width }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
