package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.ui.components.shape.ArrowDownShape
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.utils.toDp
import com.peihua.touchmonitor.utils.toPx
import com.peihua8858.tools.utils.dLog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSliderTips(
    modifier: Modifier = Modifier,
    value: Float,
    title: String,
    titleOrientation: Orientation = Orientation.Horizontal,
    colors: SliderColors = SliderDefaults.colors(),
    steps: Int = 0,
    thumbText: @Composable (Float) -> String = {
        it.toString()
    },
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onChangValue: (Float) -> Unit,
) {
    if (titleOrientation == Orientation.Vertical) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            ScaleText(text = title, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            CustomSlider(
                modifier = Modifier,
                value = value,
                colors = colors,
                steps = steps,
                thumbText = thumbText,
                valueRange = valueRange,
                onChangValue = onChangValue
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = modifier
        ) {
            ScaleText(text = title, style = MaterialTheme.typography.labelLarge)
            CustomSlider(
                modifier = Modifier.padding(start = 4.dp),
                value = value,
                colors = colors,
                steps = steps,
                thumbText = thumbText,
                valueRange = valueRange,
                onChangValue = onChangValue
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSlider(
    modifier: Modifier = Modifier,
    value: Float,
    colors: SliderColors = SliderDefaults.colors(),
    steps: Int = 0,
    thumbText: @Composable (Float) -> String = {
        it.toString()
    },
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    bubbleSize: DpSize = DpSize(36.dp, 36.dp),
    onChangValue: (Float) -> Unit,
) {
    val thumbSize = DpSize(20.dp, 20.dp)
    var sliderValue by remember { mutableFloatStateOf(value) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var sliderWidth by remember { mutableIntStateOf(0) } // 初始化滑块宽度为0
    val bubbleOffset = (sliderWidth - thumbSize.width.toPx()) / 2f // 气泡的水平偏移量
    val range = (valueRange.endInclusive - valueRange.start).toInt()

    Box(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 滑块
        Slider(
            modifier = modifier
                .offset(y = 10.dp)
                .onSizeChanged {
                    sliderWidth = it.width
                }
                .fillMaxWidth(),
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onChangValue(it)
            },
            colors = colors,
            steps = steps,
            valueRange = valueRange,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    modifier = Modifier,
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = thumbSize
                )
            },
            track = { sliderState ->
                Track(
                    colors = colors, sliderState = sliderState,
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 0.dp
                )
            },
        )
        val curValue = sliderValue - valueRange.start
        val startPosition = ((curValue.toFloat() / range) * 100f).roundToInt()
        val offsetX = (startPosition * ((sliderWidth - 20.dp.toPx()) / 100f) - bubbleOffset).toDp
        dLog { "startPosition:$startPosition,curValue:$curValue,range:$range,offsetX:$offsetX,sliderWidth:$sliderWidth,sliderValue${sliderValue},valueRange.start:${valueRange.start}" }
        // 气泡提示
        Text(
            text = thumbText(sliderValue),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .offset(x = offsetX, y = (-16).dp)
                .width(bubbleSize.width)
                .height(bubbleSize.height)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = ArrowDownShape(
                        radius = 4.dp,
                        arrowSize = 6.dp
                    )
                ),

            color = Color.White
        )
    }
}

@Composable
fun Thumb(
    modifier: Modifier = Modifier,
    text: String,
    colors: SliderColors = SliderDefaults.colors(),
    thumbSize: DpSize,
) {
    Column(
        modifier = modifier.padding(bottom = thumbSize.height),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                //绘制一个向下箭头的气泡
                .clip(RoundedCornerShape(8.dp))
                .background(color = colors.thumbColor)
        ) {
            ScaleText(
                style = MaterialTheme.typography.labelLarge,
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
        DrawInvertedTriangle(sz = thumbSize.width / 2, color = colors.thumbColor)
        Box(
            modifier = Modifier
                .size(thumbSize)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(thumbSize.width)
                )
        )
    }
}

@Composable
private fun DrawInvertedTriangle(sz: Dp, color: Color) {
    Canvas(modifier = Modifier.size(sz)) {
        // 绘制一个向下箭头的气泡
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2, size.height * 2 / 3)
            close()
            drawPath(this, color = color)
        }
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

internal fun SliderColors.thumbColor(enabled: Boolean): Color =
    if (enabled) thumbColor else disabledThumbColor

@Stable
object SliderDefaults {
    /**
     * Creates a [SliderColors] that represents the different colors used in parts of the [Slider]
     * in different states.
     */
    @Composable
    fun colors() = androidx.compose.material3.SliderDefaults.colors()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    internal fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: SliderColors = colors(),
        enabled: Boolean = true,
        thumbSize: DpSize = DpSize(4.dp, 4.dp),
    ) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }
        Spacer(
            modifier
                .size(thumbSize)
                .hoverable(interactionSource = interactionSource)
                .background(colors.thumbColor(enabled), CircleShape)
        )
    }

}