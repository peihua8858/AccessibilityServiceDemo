package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.SliderDefaults.colors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.roundToPx
import com.peihua.touchmonitor.utils.toDp
import com.peihua.touchmonitor.utils.toPx

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
    onChangValue: (Float) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(value) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    var sliderWidth by remember { mutableIntStateOf(0) } // 初始化滑块宽度为0
    val bubbleOffset = with(LocalDensity.current) { sliderWidth / 2 } // 气泡的水平偏移量
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        val startPosition = ((sliderValue - valueRange.start)/(valueRange.endInclusive - valueRange.start)*100).toInt()
        dLog { "startPosition:$startPosition,sliderValue$sliderValue,valueRange.start:${valueRange.start}" }
        // 气泡提示
        Text(
            text = thumbText(sliderValue),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (startPosition * (sliderWidth / 100) - bubbleOffset+20).toDp, y = (-10).dp)
                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(2.dp),
            color = Color.White
        )

        // 滑块
        Slider(
            modifier = modifier
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
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = DpSize(20.dp, 20.dp)
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
    }

}

@Composable
fun SliderWithBubble() {
    // 管理滑块的值
    var sliderValue by remember { mutableStateOf(0f) }

    // 获取当前屏幕宽度
    val sliderWidth = 250.dp // 可以根据需要设置滑块的宽度
    val sliderWidthPx = with(LocalDensity.current) { sliderWidth.toPx() }
    val bubbleOffset = with(LocalDensity.current) { sliderWidth.toPx() / 2 } // 气泡的水平偏移量

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        // 气泡提示
        Text(
            text = sliderValue.toInt().toString(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (sliderValue * (sliderWidthPx / 100) - bubbleOffset).toDp, y = (-32).dp)
                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            color = Color.White
        )

        // 滑块
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..100f,
            steps = 99, // 99个步进位置
            modifier = Modifier.width(sliderWidth)
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CustomSlider(
//    modifier: Modifier = Modifier,
//    value: Float,
//    colors: SliderColors = SliderDefaults.colors(),
//    steps: Int = 0,
//    thumbText: @Composable (Float) -> String = {
//        it.toString()
//    },
//    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
//    onChangValue: (Float) -> Unit,
//) {
//    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
//    val sliderPosition = remember { mutableFloatStateOf(value) }
//    Slider(
//        modifier = modifier
//            .drawWithContent {
//
//            }
//            .fillMaxWidth(),
//        value = sliderPosition.floatValue,
//        onValueChange = {
//            sliderPosition.floatValue = it
//            onChangValue(it)
//        },
//        colors = colors,
//        steps = steps,
//        valueRange = valueRange,
//        interactionSource = interactionSource,
//        thumb = {
//            Thumb(
//                text = thumbText(sliderPosition.floatValue),
//                colors = colors,
//                thumbSize = DpSize(20.dp, 20.dp)
//            )
//        },
//        track = { sliderState ->
//            Track(
//                colors = colors, sliderState = sliderState,
//                thumbTrackGapSize = 0.dp,
//                trackInsideCornerSize = 0.dp
//            )
//        },
//    )
//}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Thumb(
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    text: String,
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

//    val size =
//        if (interactions.isNotEmpty()) {
////            if (sliderState.orientation == Vertical) {
////                thumbSize.copy(height = thumbSize.height / 2)
////            } else {
//                thumbSize.copy(width = thumbSize.width / 2)
////            }
//        } else {
//            thumbSize
//        }
    Column(
        modifier = modifier.padding(bottom = thumbSize.height * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Surface(
            shape = MessageShape(),
            color = Color.Transparent,
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .background(color = Color.Black.copy(alpha = 0.6f))
            ) {
                ScaleText(
                    style = MaterialTheme.typography.labelLarge,
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(4.dp)

                )
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

internal fun SliderColors.thumbColor(enabled: Boolean): Color =
    if (enabled) thumbColor else disabledThumbColor

class MessageShape(
    private val cornerRadius: Float = 20f,
    private val pointerLength: Float = 20f,
) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            //line 1 top
            lineTo(size.width - cornerRadius, 0f)
            quadraticTo(
                x2 = size.width, y2 = cornerRadius, x1 =
                    size.width, y1 = 0f
            )
            //line 2 right
            lineTo(size.width, size.height - pointerLength - cornerRadius)
            quadraticTo(
                x2 = size.width - cornerRadius, y2 =
                    size.height - pointerLength, x1 = size.width, y1 = size.height -
                        pointerLength
            )
            //line 3 bottom
            lineTo(size.width / 2f + pointerLength, size.height - pointerLength)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width / 2f - pointerLength, size.height - pointerLength)
            lineTo(cornerRadius, size.height - pointerLength)
            quadraticTo(
                x2 = 0f, y2 = size.height - cornerRadius -
                        pointerLength, x1 = 0f, y1 = size.height - pointerLength
            )
            //line 4 left
            lineTo(0f, cornerRadius)
            quadraticTo(x2 = cornerRadius, y2 = 0f, x1 = 0f, y1 = 0f)
            close()
        }
        return Outline.Generic(path)
    }
}


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