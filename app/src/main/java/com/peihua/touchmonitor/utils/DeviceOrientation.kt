package com.peihua.touchmonitor.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.peihua8858.tools.utils.dLog

/**
 * 设备姿态，单位为度。
 * @param azimuth 朝向，0 表示正北，顺时针增大
 * @param pitch 前后倾角，屏幕顶边抬起为负
 * @param roll 左右倾角
 */
data class DeviceOrientation(val azimuth: Float, val pitch: Float, val roll: Float)

/**
 * 监听旋转矢量传感器得到设备姿态，已按屏幕方向重映射并做平滑处理。
 * 返回 null 表示设备没有可用的方向传感器。
 */
@Composable
fun rememberDeviceOrientation(): DeviceOrientation? {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val sensorManager = remember(context) { context.getSystemService(SensorManager::class.java) }
    val sensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }
    var orientation by remember { mutableStateOf(DeviceOrientation(0f, 0f, 0f)) }
    val displayRotation = remember(configuration) { context.displayRotation() }
    DisposableEffect(lifecycleOwner, sensorManager, sensor, displayRotation) {
        if (sensorManager == null || sensor == null) {
            return@DisposableEffect onDispose { }
        }
        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val angles = FloatArray(3)
        val (axisX, axisY) = displayAxes(displayRotation)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                dLog { "remappedMatrix:${remappedMatrix.joinToString(",")}" }
                SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)
                dLog { "axisX:${axisX},axisY:$axisY" }
                dLog { "remappedMatrix:${remappedMatrix.joinToString(",")}" }
                dLog { "remappedMatrix.length:${remappedMatrix.size}" }
                dLog { "rotationMatrix:${rotationMatrix.joinToString(",")}" }
                SensorManager.getOrientation(remappedMatrix, angles)
                val current = orientation

                dLog { "current:$current,angles:${angles.joinToString(",")}" }
                orientation = DeviceOrientation(
                    azimuth = smoothAzimuth(current.azimuth, (angles[0].toDegrees() + 360f) % 360f),
                    pitch = smoothAngle(current.pitch, (if(angles[1].isNaN())1f else angles[1]).toDegrees()),
                    roll = smoothAngle(current.roll, angles[2].toDegrees())
                )
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
    return if (sensor == null) null else orientation
}

private fun Float.toDegrees(): Float = Math.toDegrees(toDouble()).toFloat()

private fun smoothAngle(current: Float, target: Float): Float =
    current + (target - current) * SMOOTHING

/** 沿最短路径向目标角度靠近，抑制磁力计读数抖动 */
private fun smoothAzimuth(current: Float, target: Float): Float {
    val delta = ((target - current + 540f) % 360f) - 180f
    return (current + delta * SMOOTHING + 360f) % 360f
}

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

private const val SMOOTHING = 0.25f
