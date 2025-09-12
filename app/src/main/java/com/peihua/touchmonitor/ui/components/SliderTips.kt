package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.text.ScaleText

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
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val sliderPosition = remember { mutableFloatStateOf(value) }
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = sliderPosition.floatValue,
        onValueChange = {
            sliderPosition.floatValue = it
            onChangValue(it)
        },
        colors = colors,
        steps = steps,
        valueRange = valueRange,
        interactionSource = interactionSource,
        thumb = {
            Thumb(
                text = thumbText(sliderPosition.floatValue),
                colors = colors,
                thumbSize = DpSize(24.dp, 24.dp)
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

@Composable
 fun Thumb(
    modifier: Modifier = Modifier,
    text: String,
    colors: SliderColors = SliderDefaults.colors(),
    thumbSize: DpSize,
) {
    Column(
        modifier = modifier.padding(bottom = thumbSize.height * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                //绘制一个向下箭头的气泡
                .clip(RoundedCornerShape(8.dp))
                .background(color = colors.thumbColor)
                .padding(4.dp)
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
