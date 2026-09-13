package com.peihua.touchmonitor.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 圆形角度尺（Jetpack Compose 版）
 *
 * 功能与原 CycleRulerView 一致：
 *  - 底部中央绘制半圆弧，圆弧上按度数绘制刻度
 *  - 每隔 10 度绘制数字，5 度刻度加长
 *  - 圆心处绘制内外圆环，并显示当前角度值
 *  - 手指按下/拖动时根据位置实时计算角度
 *
 * @param angle 当前角度（0~180），0 在左，180 在右
 * @param onAngleChange 角度变化回调
 */
@Composable
fun CycleRulerView(
    modifier: Modifier = Modifier,
    angle: Int = 0,
    onAngleChange: (Int) -> Unit = {},

    /** 刻度线和数字的颜色 */
    tickColor: Color = Color(0xFF00A0E9),
    /** 中心角度文字颜色 */
    angleTextColor: Color = Color.Black,
    /** 中心外圈描边颜色 */
    outerStrokeColor: Color = Color(0xFF999999),
    /** 背景色 */
    backgroundColor: Color = Color.White,
    /** 圆心白色填充色 */
    centerFillColor: Color = Color.White,

    /** 最长刻度线长度（10 的倍数处） */
    tickLength: Dp = 15.dp,
    /** 刻度线宽 */
    tickStrokeWidth: Dp = 2.dp,
    /** 刻度数字文字大小 */
    tickTextSize: TextUnit = 11.sp,
    /** 角度数字文字大小 */
    angleTextSize: TextUnit = 40.sp,
    /** 中心圆外半径 */
    centerOuterRadius: Dp = 46.dp,
    /** 中心圆内半径 */
    centerInnerRadius: Dp = 40.dp,
    /** 中心圆环描边宽度 */
    centerStrokeWidth: Dp = 4.dp,
) {
    val density = LocalDensity.current

    val tickLengthPx = with(density) { tickLength.toPx() }
    val tickStrokeWidthPx = with(density) { tickStrokeWidth.toPx() }
    val tickTextSizePx = with(density) { tickTextSize.toPx() }
    val angleTextSizePx = with(density) { angleTextSize.toPx() }
    val centerOuterPx = with(density) { centerOuterRadius.toPx() }
    val centerInnerPx = with(density) { centerInnerRadius.toPx() }
    val centerStrokePx = with(density) { centerStrokeWidth.toPx() }

    // 复用的原生 Paint
    val tickTextPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    val angleTextPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }

    // 触摸/拖动：按下和拖动都实时更新角度
    val pointerModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { change ->
                    if (change.pressed) {
                        val newAngle = calculateAngle(
                            position = change.position,
                            width = size.width.toFloat(),
                            height = size.height.toFloat(),
                        )
                        onAngleChange(newAngle)
                        change.consume()
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.then(pointerModifier)) {
        val w = size.width
        val h = size.height
        val offset = (h - w / 2f) / 2f
        val cx = w / 2f
        val cy = h - offset
        val radius = w / 2f

        // ---------- 背景 ----------
        drawRect(color = backgroundColor, size = size)

        // ---------- 刻度 ----------
        tickTextPaint.apply {
            textSize = tickTextSizePx
            color = tickColor.toArgb()
            textAlign = Paint.Align.CENTER
        }

        val nativeCanvas = drawContext.canvas.nativeCanvas

        for (deg in 0..180) {
            // 屏幕角度：180 度对应左侧，0 度对应右侧
            val screenRad = Math.toRadians((180 - deg).toDouble())
            val cosA = cos(screenRad).toFloat()
            val sinA = sin(screenRad).toFloat()

            // 刻度长度按刻度等级缩放
            val lengthFactor = when {
                deg % 10 == 0 -> 1.0f
                deg % 5 == 0 -> 0.75f
                else -> 0.5f
            }
            val len = tickLengthPx * lengthFactor

            val outerX = cx + cosA * radius
            val outerY = cy - sinA * radius
            val innerX = cx + cosA * (radius - len)
            val innerY = cy - sinA * (radius - len)

            drawLine(
                color = tickColor,
                start = Offset(outerX, outerY),
                end = Offset(innerX, innerY),
                strokeWidth = tickStrokeWidthPx,
            )

            // 每隔 10 度画数字
            if (deg % 10 == 0) {
                val textRadius = radius - tickLengthPx - tickTextSizePx * 0.6f
                val textX = cx + cosA * textRadius
                val textY = cy - sinA * textRadius

                nativeCanvas.save()
                nativeCanvas.translate(textX, textY)
                // 让文字始终沿半径方向可读
                nativeCanvas.rotate((180 - deg - 90).toFloat())
                nativeCanvas.drawText(deg.toString(), 0f, 0f, tickTextPaint)
                nativeCanvas.restore()
            }
        }

        // ---------- 中心圆环 ----------
        // 原实现中心圆在 (width/2, height*3/5) 位置
        val centerCircleY = h * 3f / 5f

        drawCircle(
            color = centerFillColor,
            radius = centerOuterPx,
            center = Offset(cx, centerCircleY),
        )
        drawCircle(
            color = centerFillColor,
            radius = centerInnerPx,
            center = Offset(cx, centerCircleY),
        )
        drawCircle(
            color = outerStrokeColor,
            radius = centerOuterPx,
            center = Offset(cx, centerCircleY),
            style = Stroke(width = centerStrokePx),
        )
        drawCircle(
            color = tickColor,
            radius = centerInnerPx,
            center = Offset(cx, centerCircleY),
            style = Stroke(width = centerStrokePx),
        )

        // ---------- 中心角度文字 ----------
        angleTextPaint.apply {
            textSize = angleTextSizePx
            color = angleTextColor.toArgb()
            textAlign = Paint.Align.CENTER
        }
        val angleStr = angle.toString()
        val fm = angleTextPaint.fontMetrics
        val baselineY = centerCircleY - (fm.ascent + fm.descent) / 2f
        nativeCanvas.drawText(angleStr, cx, baselineY, angleTextPaint)

        // ---------- 指示线（从圆心指向当前角度） ----------
        val indicatorRad = Math.toRadians((180 - angle).toDouble())
        val indicatorX = cx + cos(indicatorRad).toFloat() * radius
        val indicatorY = cy - sin(indicatorRad).toFloat() * radius

        drawLine(
            color = tickColor,
            start = Offset(cx, cy),
            end = Offset(indicatorX, indicatorY),
            strokeWidth = tickStrokeWidthPx * 2f,
        )
    }
}

/**
 * 根据触摸位置计算角度：
 *  - 左侧 → 0 度
 *  - 正上方 → 90 度
 *  - 右侧 → 180 度
 *  - 触摸点在圆心下方时钳制到最近的一端
 */
private fun calculateAngle(position: Offset, width: Float, height: Float): Int {
    val offset = (height - width / 2f) / 2f
    val cx = width / 2f
    val cy = height - offset

    val dx = position.x - cx
    val dy = position.y - cy

    // dy > 0 表示在圆心下方（超出弧范围），按左右钳制
    if (dy > 0f) {
        return if (dx < 0f) 0 else 180
    }

    // 数学坐标：y 向上
    val mathDeg = Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())).toFloat()
    // 数学角 180° → 显示 0°，数学角 0° → 显示 180°，数学角 90° → 显示 90°
    val displayAngle = (180f - mathDeg).coerceIn(0f, 180f)
    return displayAngle.roundToInt()
}