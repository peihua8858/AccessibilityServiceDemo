package com.peihua.touchmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt


@get:Composable
val <T : Number> T.toDp: Dp
    get() {
        val density = LocalDensity.current
        val pxValue = this.toFloat()
        return (pxValue / density.density).dLog { "toDp>$pxValue/${density.density}=${this}" }.dp
    }

@get:Composable
val <T : Number> T.toSp: TextUnit
    get() {
        val density = LocalDensity.current
        val pxValue = this.toFloat()
        return (pxValue / density.density).dLog { "toSp>r$pxValue/${density.density}=$this" }.sp
    }

fun Dp.roundToPx():Int {
    return (this.value).roundToInt()
}