package com.peihua.touchmonitor.ui.screen.function

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.rememberDeviceOrientation
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * 水平仪
 */
@Composable
fun HorizonScreen(modifier: Modifier) {
    Toolbar(modifier = Modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_horizon)) {
        val orientation = rememberDeviceOrientation()
        if (orientation == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ScaleText(
                    text = stringResource(R.string.text_orientation_sensor_unavailable),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = dimensionSpResource(R.dimen.sp_14)
                )
            }
            return@Toolbar
        }
        val vertical = orientation.pitch.roundToInt()
        val horizontal = orientation.roll.roundToInt()
        val stateColor = if (vertical == 0 && horizontal == 0) LevelColor else TiltColor
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                LevelDial(
                    pitch = orientation.pitch,
                    roll = orientation.roll,
                    color = stateColor,
                    modifier = Modifier.size(dimensionResource(R.dimen.dp_237))
                )
            }
            HorizontalDivider(
                thickness = dimensionResource(R.dimen.dp_1),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.dp_28),
                        vertical = dimensionResource(R.dimen.dp_43)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Readout(value = vertical, label = stringResource(R.string.vertical))
                Readout(value = horizontal, label = stringResource(R.string.horizontal))
            }
        }
    }
}

@Composable
private fun RowScope.Readout(value: Int, label: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScaleText(
            text = "$value°",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = dimensionSpResource(R.dimen.sp_34)
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_22)))
        ScaleText(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dimensionSpResource(R.dimen.sp_17)
        )
    }
}

@Composable
private fun LevelDial(pitch: Float, roll: Float, color: Color, modifier: Modifier) {
    val targetColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val ringStroke = radius * RING_STROKE
        drawCircle(
            color = color,
            radius = radius - ringStroke / 2f,
            style = Stroke(width = ringStroke)
        )
        drawCircle(
            color = targetColor,
            radius = radius * TARGET_RADIUS,
            style = Stroke(width = radius * TARGET_STROKE)
        )
        val bubbleRadius = radius * BUBBLE_RADIUS
        drawCircle(
            color = color,
            radius = bubbleRadius,
            center = center + bubbleOffset(pitch, roll, radius, radius - ringStroke - bubbleRadius)
        )
    }
}

/** 倾角线性映射为气泡位移，并限制在圆环内侧 */
private fun bubbleOffset(pitch: Float, roll: Float, radius: Float, limit: Float): Offset {
    val offset = Offset(roll / MAX_ANGLE * radius, pitch / MAX_ANGLE * radius)
    val distance = hypot(offset.x, offset.y)
    return if (distance > limit) offset * (limit / distance) else offset
}

private val LevelColor = Color(0xFF00FF00)
private val TiltColor = Color(0xFFE03524)

private const val MAX_ANGLE = 90f

// 以下比例均以圆盘半径为单位，取自设计稿测量值
private const val RING_STROKE = 0.048f
private const val TARGET_RADIUS = 0.206f
private const val TARGET_STROKE = 0.005f
private const val BUBBLE_RADIUS = 0.202f
