package com.peihua.touchmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua8858.tools.utils.dLog
import kotlin.math.roundToInt


@get:Composable
val <T : Number> T.toDp: Dp
    get() {
        val density = LocalDensity.current
        val pxValue = this.toFloat()
        return (pxValue / density.density).dLog { "toDp>$pxValue/${density.density}=${this}" }.dp
    }

fun <T: Number> T.toDp(density: Density): Dp {
    val pxValue = toFloat()
    return with(density) {
        (pxValue / this.density).dLog { "toDp>r$pxValue/${density.density}=$this" }.dp
    }
}

@get:Composable
val <T : Number> T.toSp: TextUnit
    get() {
        return toSp(LocalDensity.current)
    }

fun <T: Number> T.toSp(density: Density): TextUnit {
    val pxValue = toFloat()
    return with(density) {
        (pxValue / this.density).dLog { "toSp>r$pxValue/${density.density}=$this" }.sp
    }
}

@Composable
fun Dp.roundToPx(): Int {
    return roundToPx(LocalDensity.current)
}

fun Dp.roundToPx(density: Density): Int {
    return with(density) { toPx().roundToInt() }
}

@Composable
fun Dp.toPx(): Float {
    return toPx(LocalDensity.current)
}

fun Dp.toPx(density: Density): Float {
    return with(density) { toPx() }
}