package com.peihua.touchmonitor.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import androidx.core.graphics.withTranslation
import androidx.core.graphics.withRotation

/** 单位：英寸 */
const val UNIT_INCH = 0

/** 单位：厘米 */
const val UNIT_CM = 1

/**
 * 刻度尺控件（Jetpack Compose 版）
 *
 * 功能与原 RulerView 一致：
 *  - 从顶部向下按物理尺寸（ydpi）绘制刻度，刻度线贴着右边缘向左延伸
 *  - 支持英寸 / 厘米两种单位切换
 *  - 多点触控时在每个手指位置绘制圆圈，并显示最高/最低两指之间的距离
 */
@Composable
fun RulerView(
    modifier: Modifier = Modifier,
    /** 单位：UNIT_INCH / UNIT_CM */
    unitType: Int = UNIT_INCH,
    /** 没有触摸点时的默认提示文字 */
    defaultText: String = "",
    /** 刻度数字的文字大小（对应原 tickTextSize） */
    tickTextSize: TextUnit = 40.sp,
    /** 刻度线宽（对应原 tickStrokeWidth） */
    tickStrokeWidth: Dp = 4.dp,
    /** 最长刻度线的长度（对应原 tickMaxLength） */
    maxTickLength: Dp = 100.dp,
    /** 刻度线 / 刻度数字的颜色 */
    tickColor: Color = Color.Black,
    /** 中间测量结果的文字大小 */
    measureTextSize: TextUnit = 60.sp,
    /** 中间测量结果的颜色 */
    measureTextColor: Color = Color.Black,
    /** 背景色 */
    backgroundColor: Color = Color.White,
    /** 手指圆圈 / 连接线的颜色 */
    dotColor: Color = Color.Black,
    /** 手指圆圈半径 */
    dotRadius: Dp = 60.dp,
    /** 手指圆圈、连接线的线宽 */
    dotStrokeWidth: Dp = 8.dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val resources = LocalResources.current

    // 物理屏幕的纵向 DPI，用于换算英寸/厘米
    val ydpi = remember(context, density) {
        val real = resources.displayMetrics.ydpi
        if (real.isFinite() && real > 0f) real else density.density * 160f
    }

    // dp / sp -> px
    val tickTextSizePx = with(density) { tickTextSize.toPx() }
    val tickStrokeWidthPx = with(density) { tickStrokeWidth.toPx() }
    val maxTickLengthPx = with(density) { maxTickLength.toPx() }
    val measureTextSizePx = with(density) { measureTextSize.toPx() }
    val dotRadiusPx = with(density) { dotRadius.toPx() }
    val dotStrokeWidthPx = with(density) { dotStrokeWidth.toPx() }

    // 复用的原生 Paint（只在第一次组合时创建）
    val tickPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    val measurePaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    val dotPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }

    // pointerId -> 触点位置
    val touches = remember { mutableStateMapOf<Long, Offset>() }

    val pointerModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val pressedIds = HashSet<Long>(event.changes.size)
                event.changes.forEach { change ->
                    if (change.pressed) {
                        val id = change.id.value
                        touches[id] = change.position
                        pressedIds.add(id)
                    }
                }
                // 移除已经抬起的手指
                touches.keys.retainAll(pressedIds)
            }
        }
    }

    Canvas(modifier = modifier.then(pointerModifier)) {
        val w = size.width
        val h = size.height

        // ---------- 背景 ----------
        drawRect(color = backgroundColor, size = size)

        // ---------- 准备画笔 ----------
        tickPaint.apply {
            strokeWidth = tickStrokeWidthPx
            textSize = tickTextSizePx
            color = tickColor.toArgb()
        }
        measurePaint.apply {
            textSize = measureTextSizePx
            color = measureTextColor.toArgb()
            textAlign = Paint.Align.CENTER
        }
        dotPaint.apply {
            color = dotColor.toArgb()
            strokeWidth = dotStrokeWidthPx
            style = Paint.Style.STROKE
        }

        val nativeCanvas = drawContext.canvas.nativeCanvas

        // ---------- 刻度 ----------
        val pxPerUnit = if (unitType == UNIT_INCH) ydpi else ydpi / 2.54f
        val stepPerTick = if (unitType == UNIT_INCH) 0.25f else 0.1f
        val pxPerTick = pxPerUnit * stepPerTick

        if (pxPerTick > 0f) {
            var index = 0
            while (index * pxPerTick <= h) {
                val y = index * pxPerTick
                val length = tickScaleFactor(index, unitType) * maxTickLengthPx
                val x = w - length

                // 刻度线：贴着右边缘向左画
                drawLine(
                    color = tickColor,
                    start = Offset(x, y),
                    end = Offset(w, y),
                    strokeWidth = tickStrokeWidthPx,
                )

                // 整数位置画数字（旋转 90°）
                val isWhole = if (unitType == UNIT_INCH) index % 4 == 0 else index % 10 == 0
                if (isWhole) {
                    val label = if (unitType == UNIT_INCH) (index / 4).toString()
                    else (index / 10).toString()

                    nativeCanvas.withTranslation(
                        x - tickTextSizePx,
                        y - tickPaint.measureText(label) / 2f,
                    ) {
                        rotate(90f)
                        drawText(label, 0f, 0f, tickPaint)
                    }
                }
                index++
            }
        }

        // ---------- 手指触点 ----------
        var topPoint: Offset? = null
        var bottomPoint: Offset? = null
        touches.values.forEach { p ->
            if (topPoint == null || topPoint.y < p.y) topPoint = p
            if (bottomPoint == null || bottomPoint.y > p.y) bottomPoint = p
        }

        // 画圆圈
        touches.values.forEach { p ->
            drawCircle(
                color = dotColor,
                radius = dotRadiusPx,
                center = p,
                style = Stroke(width = dotStrokeWidthPx),
            )
        }

        // 从圆圈右侧引一条线到屏幕右边缘
        topPoint?.let {
            drawLine(
                color = dotColor,
                start = Offset(it.x + dotRadiusPx, it.y),
                end = Offset(w, it.y),
                strokeWidth = dotStrokeWidthPx,
            )
        }
        bottomPoint?.let {
            drawLine(
                color = dotColor,
                start = Offset(it.x + dotRadiusPx, it.y),
                end = Offset(w, it.y),
                strokeWidth = dotStrokeWidthPx,
            )
        }

        // ---------- 中间的测量结果 ----------
        val text = if (topPoint != null && bottomPoint != null) {
            val distance = abs(topPoint.y - bottomPoint.y) / pxPerUnit
            formatMeasure(distance, unitType)
        } else {
            defaultText
        }

        nativeCanvas.withRotation(90f, w / 2f, h / 2f) {
            drawText(text, w / 2f, h / 2f, measurePaint)
        }
    }
}

/**
 * 刻度长度比例：
 *  英寸：每 4 格为一个整英寸（1.0），偶数格 0.75，其余 0.5
 *  厘米：每 10 格为一个整厘米（1.0），5 的倍数 0.75，其余 0.5
 */
private fun tickScaleFactor(index: Int, unitType: Int): Float {
    return if (unitType == UNIT_INCH) {
        when {
            index % 4 == 0 -> 1.0f
            index % 2 == 0 -> 0.75f
            else -> 0.5f
        }
    } else {
        when {
            index % 10 == 0 -> 1.0f
            index % 5 == 0 -> 0.75f
            else -> 0.5f
        }
    }
}

/** 格式化测量结果，例如 "2.500 Inches" / "3.000 CM" */
private fun formatMeasure(value: Float, unitType: Int): String {
    val unitLabel = when (unitType) {
        UNIT_INCH -> if (value > 1.0f) "Inches" else "Inch"
        UNIT_CM -> "CM"
        else -> ""
    }
    return String.format("%.3f %s", value, unitLabel)
}