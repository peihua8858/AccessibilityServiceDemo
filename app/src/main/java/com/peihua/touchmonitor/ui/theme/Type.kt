package com.peihua.touchmonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.2.sp),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

val Typography.labelSmallNormal: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Normal)

val Typography.labelMediumNormal: TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.Normal)

val Typography.labelLargeNormal: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.Normal)

@get:Composable
val DefaultTextStyle: TextStyle
    get() = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
