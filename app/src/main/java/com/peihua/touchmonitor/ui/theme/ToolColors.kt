package com.peihua.touchmonitor.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ToolColors(
    val ledDefaultBackground: Color,
    val ledDefaultText: Color,
    val qrForeground: Color,
    val qrBackground: Color,
    val rulerTick: Color,
    val mediaScrim: Color,
)

val DefaultToolColors = ToolColors(
    ledDefaultBackground = Color.Black,
    ledDefaultText = Color.White,
    qrForeground = Color.Black,
    qrBackground = Color.White,
    rulerTick = Color(0xFF00A0E9),
    mediaScrim = Color.Black.copy(alpha = 0.5f),
)

val LocalToolColors = staticCompositionLocalOf { DefaultToolColors }
