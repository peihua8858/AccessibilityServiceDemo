package com.peihua.touchmonitor.ui.screen.function.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.dimensionSpResource
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 指南针功能
 */
@Composable
fun CompassScreen(modifier: Modifier) {
    Toolbar(modifier = Modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_compass)) {
        val heading = rememberCompassHeading()
        if (heading == null) {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ScaleText(
                    text = stringResource(R.string.text_compass_unavailable),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = dimensionSpResource(R.dimen.sp_14)
                )
            }
            return@Toolbar
        }
        val directions = stringArrayResource(R.array.compass_directions)
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimensionResource(R.dimen.dp_24)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ScaleText(
                text = directions[headingToDirectionIndex(heading)],
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = dimensionSpResource(R.dimen.sp_34)
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_24)))
            CompassDial(
                heading = heading,
                directions = directions,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun CompassDial(heading: Float, directions: Array<String>, modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val foreground = MaterialTheme.colorScheme.onBackground
    val outlineColor = foreground.copy(alpha = 0.7f)
    val ringColor = foreground.copy(alpha = 0.35f)
    val tickColor = foreground.copy(alpha = 0.22f)
    val labelColor = foreground.copy(alpha = 0.5f)
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        drawOuterFrame(radius, outlineColor, foreground)
        drawCircle(
            color = ringColor,
            radius = radius * RING_RADIUS,
            style = Stroke(width = radius * RING_STROKE)
        )
        rotate(degrees = -heading) {
            drawTicks(radius, tickColor)
            drawDialLabels(radius, textMeasurer, directions, labelColor)
        }
        drawNorthNeedle(radius, heading)
        drawHeadingText(radius, heading, textMeasurer, foreground)
    }
}

/** 顶部固定指针与底部留缺口的外圈装饰弧 */
private fun DrawScope.drawOuterFrame(radius: Float, arcColor: Color, pointerColor: Color) {
    val arcRadius = radius * OUTER_ARC_RADIUS
    drawArc(
        color = arcColor,
        startAngle = OUTER_ARC_START,
        sweepAngle = OUTER_ARC_SWEEP,
        useCenter = false,
        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
        size = Size(arcRadius * 2f, arcRadius * 2f),
        style = Stroke(width = radius * OUTER_ARC_STROKE)
    )
    drawPath(
        path = trianglePath(
            tipRadius = radius * POINTER_TIP_RADIUS,
            baseRadius = radius * POINTER_BASE_RADIUS,
            halfWidth = radius * POINTER_HALF_WIDTH
        ),
        color = pointerColor
    )
}

private fun DrawScope.drawTicks(radius: Float, color: Color) {
    val inner = radius * TICK_INNER_RADIUS
    val outer = radius * TICK_OUTER_RADIUS
    val strokeWidth = radius * TICK_STROKE
    for (index in 0 until 360 step TICK_STEP_DEGREES) {
        val radians = Math.toRadians(index - 90.0)
        val cos = cos(radians).toFloat()
        val sin = sin(radians).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x + cos * inner, center.y + sin * inner),
            end = Offset(center.x + cos * outer, center.y + sin * outer),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawDialLabels(
    radius: Float,
    textMeasurer: TextMeasurer,
    directions: Array<String>,
    color: Color,
) {
    for (degrees in 0 until 360 step LABEL_STEP_DEGREES) {
        val isCardinal = degrees % 90 == 0
        val style = TextStyle(
            color = if (degrees == 0) NorthColor else color,
            fontSize = (radius * if (isCardinal) CARDINAL_FONT_SCALE else LABEL_FONT_SCALE).toSp()
        )
        val text = if (isCardinal) directions[degrees / 45] else degrees.toString()
        val layout = textMeasurer.measure(text, style)
        // 刻度盘随方位旋转，标签再各自反向摆正，使其在指向正上方时可正常阅读
        rotate(degrees = degrees.toFloat()) {
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    center.x - layout.size.width / 2f,
                    center.y - radius * LABEL_OUTER_RADIUS
                )
            )
        }
    }
}

/** 从正上方沿最短方向连到刻度盘正北的红色弧线，末端为箭头 */
private fun DrawScope.drawNorthNeedle(radius: Float, heading: Float) {
    val ringRadius = radius * RING_RADIUS
    var sweep = -heading % 360f
    if (sweep < -180f) sweep += 360f
    drawArc(
        color = NorthColor,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
        size = Size(ringRadius * 2f, ringRadius * 2f),
        style = Stroke(width = radius * NEEDLE_ARC_STROKE)
    )
    rotate(degrees = -heading) {
        drawPath(
            path = trianglePath(
                tipRadius = radius * NEEDLE_TIP_RADIUS,
                baseRadius = radius * NEEDLE_BASE_RADIUS,
                halfWidth = radius * NEEDLE_HALF_WIDTH
            ),
            color = NorthColor
        )
    }
}

private fun DrawScope.drawHeadingText(
    radius: Float,
    heading: Float,
    textMeasurer: TextMeasurer,
    color: Color,
) {
    val layout = textMeasurer.measure(
        text = "${heading.roundToInt() % 360}°",
        style = TextStyle(color = color, fontSize = (radius * HEADING_FONT_SCALE).toSp())
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            center.x - layout.size.width / 2f,
            center.y - layout.size.height / 2f
        )
    )
}

/** 以圆心正上方为顶点、指向外侧的三角形 */
private fun DrawScope.trianglePath(
    tipRadius: Float,
    baseRadius: Float,
    halfWidth: Float,
): Path = Path().apply {
    moveTo(center.x, center.y - tipRadius)
    lineTo(center.x - halfWidth, center.y - baseRadius)
    lineTo(center.x + halfWidth, center.y - baseRadius)
    close()
}

/**
 * 当前设备朝向，单位为度（0 表示正北，顺时针增大）。
 * 返回 null 表示设备没有可用的方向传感器。
 */
@Composable
private fun rememberCompassHeading(): Float? {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val sensorManager = remember(context) { context.getSystemService(SensorManager::class.java) }
    val sensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }
    var heading by remember { mutableFloatStateOf(0f) }
    val displayRotation = remember(configuration) { context.displayRotation() }
    DisposableEffect(lifecycleOwner, sensorManager, sensor, displayRotation) {
        if (sensorManager == null || sensor == null) {
            return@DisposableEffect onDispose { }
        }
        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val (axisX, axisY) = displayAxes(displayRotation)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)
                SensorManager.getOrientation(remappedMatrix, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                heading = smoothHeading(heading, (azimuth + 360f) % 360f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START ->
                    sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

                Lifecycle.Event.ON_STOP -> sensorManager.unregisterListener(listener)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(listener)
        }
    }
    return if (sensor == null) null else heading
}

/** 沿最短路径向目标角度靠近，抑制磁力计读数抖动 */
private fun smoothHeading(current: Float, target: Float): Float {
    val delta = ((target - current + 540f) % 360f) - 180f
    return (current + delta * HEADING_SMOOTHING + 360f) % 360f
}

private fun headingToDirectionIndex(heading: Float): Int =
    ((heading + 22.5f) / 45f).toInt() % 8

private fun displayAxes(displayRotation: Int): Pair<Int, Int> = when (displayRotation) {
    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
}

private fun Context.displayRotation(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.rotation ?: Surface.ROTATION_0
    } else {
        @Suppress("DEPRECATION")
        getSystemService(WindowManager::class.java).defaultDisplay.rotation
    }

private val NorthColor = Color(0xFFFF3D00)

private const val HEADING_SMOOTHING = 0.25f
private const val TICK_STEP_DEGREES = 2
private const val LABEL_STEP_DEGREES = 30

// 以下比例均以圆盘半径为单位，取自设计稿测量值
private const val OUTER_ARC_RADIUS = 0.907f
private const val OUTER_ARC_STROKE = 0.008f
private const val OUTER_ARC_START = 123.5f
private const val OUTER_ARC_SWEEP = 293f
private const val POINTER_TIP_RADIUS = 0.99f
private const val POINTER_BASE_RADIUS = 0.896f
private const val POINTER_HALF_WIDTH = 0.054f
private const val RING_RADIUS = 0.699f
private const val RING_STROKE = 0.010f
private const val NEEDLE_ARC_STROKE = 0.012f
private const val NEEDLE_TIP_RADIUS = 0.781f
private const val NEEDLE_BASE_RADIUS = 0.687f
private const val NEEDLE_HALF_WIDTH = 0.065f
private const val TICK_OUTER_RADIUS = 0.659f
private const val TICK_INNER_RADIUS = 0.589f
private const val TICK_STROKE = 0.010f
private const val LABEL_OUTER_RADIUS = 0.570f
private const val LABEL_FONT_SCALE = 0.062f
private const val CARDINAL_FONT_SCALE = 0.078f
private const val HEADING_FONT_SCALE = 0.342f
