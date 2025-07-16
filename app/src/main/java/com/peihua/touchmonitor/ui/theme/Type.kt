package com.peihua.touchmonitor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf


internal val LocalTypography = staticCompositionLocalOf { Typography() }

@Composable
fun Typography(scale: Float = 1f): Typography {
    val typography = LocalTypography.current
    return MaterialTheme.typography.copy(
        displayLarge = typography.displayLarge.copy(
            fontSize = typography.displayLarge.fontSize * scale,
        ),
        displayMedium = typography.displayMedium.copy(
            fontSize = typography.displayMedium.fontSize * scale,
        ),
        displaySmall = typography.displaySmall.copy(
            fontSize = typography.displaySmall.fontSize * scale,
        ),
        headlineLarge = typography.headlineLarge.copy(
            fontSize = typography.headlineLarge.fontSize * scale,
        ),
        headlineMedium = typography.headlineMedium.copy(
            fontSize = typography.headlineMedium.fontSize * scale,
        ),
        headlineSmall = typography.headlineSmall.copy(
            fontSize = typography.headlineSmall.fontSize * scale,
        ),
        titleLarge = typography.titleLarge.copy(
            fontSize = typography.titleLarge.fontSize * scale,
        ),
        titleMedium = typography.titleMedium.copy(
            fontSize = typography.titleMedium.fontSize * scale,
        ),
        titleSmall = typography.titleSmall.copy(
            fontSize = typography.titleSmall.fontSize * scale,
        ),
        bodyLarge = typography.bodyLarge.copy(
            fontSize = typography.bodyLarge.fontSize * scale,
        ),
        bodyMedium = typography.bodyMedium.copy(
            fontSize = typography.bodyMedium.fontSize * scale,
        ),
        bodySmall = typography.bodySmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
            fontSize = typography.bodySmall.fontSize * scale,
//            lineHeight = dimensionResource(id = R.dimen.sp_16),
//            letterSpacing = 0.4.sp
        ),
        labelLarge = typography.labelLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
            fontSize = typography.labelLarge.fontSize * scale,
//            lineHeight = dimensionResource(id = R.dimen.sp_24),
//            letterSpacing = 0.5.sp
        ),
        labelMedium = typography.labelMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
            fontSize = typography.labelMedium.fontSize * scale,
//            lineHeight = dimensionResource(id = R.dimen.sp_20),
//            letterSpacing = 0.1.sp
        ),
        labelSmall = typography.labelSmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
            fontSize = typography.labelSmall.fontSize * scale,
//            lineHeight = dimensionResource(id = R.dimen.sp_16),
//            letterSpacing = 0.5.sp
        ),
    )
}
//@Composable
//fun ChatBoxTypography(scale: Float = 1f): Typography {
//    val typography = MaterialTheme.typography.copy()
//    return MaterialTheme.typography.copy(
//        displayLarge = typography.displayLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
//            fontSize = 34.sp,
//            lineHeight = 40.sp,
//            letterSpacing = 0.sp
//        ),
//        displayMedium = typography.displayMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
//            fontSize = 30.sp,
//            lineHeight = 36.sp,
//            letterSpacing = 0.sp
//        ),
//        displaySmall = typography.displaySmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
//            fontSize = 26.sp,
//            lineHeight = 32.sp,
//            letterSpacing = 0.sp
//        ),
//        headlineLarge = typography.headlineLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
//            fontSize = 32.sp,
//            lineHeight = 40.sp,
//            letterSpacing = 0.sp
//        ),
//        headlineMedium = typography.headlineMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
//            fontSize = 28.sp,
//            lineHeight = 36.sp,
//            letterSpacing = 0.sp
//        ),
//        headlineSmall = typography.headlineSmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
//            fontSize = dimensionResource(id = R.dimen.sp_24),
//            lineHeight = 32.sp,
//            letterSpacing = 0.sp
//        ),
//        titleLarge = typography.titleLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
//            fontSize = dimensionResource(id = R.dimen.sp_18),
//            lineHeight = 28.sp,
//            letterSpacing = 0.sp
//        ),
//        titleMedium = typography.titleMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
//            fontSize = dimensionResource(id = R.dimen.sp_16),
//            lineHeight = dimensionResource(id = R.dimen.sp_24),
//            letterSpacing = 0.1.sp
//        ),
//        titleSmall = typography.titleSmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
//            fontSize = dimensionResource(id = R.dimen.sp_14),
//            lineHeight = dimensionResource(id = R.dimen.sp_20),
//            letterSpacing = 0.1.sp
//        ),
//        bodyLarge = typography.bodyLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
//            fontSize = dimensionResource(id = R.dimen.sp_16),
//            lineHeight = dimensionResource(id = R.dimen.sp_24),
//            letterSpacing = 0.5.sp
//        ),
//        bodyMedium = typography.bodyMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
//            fontSize = dimensionResource(id = R.dimen.sp_14),
//            lineHeight = dimensionResource(id = R.dimen.sp_20),
//            letterSpacing = 0.25.sp
//        ),
//        bodySmall = typography.bodySmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
//            fontSize = dimensionResource(id = R.dimen.sp_12),
//            lineHeight = dimensionResource(id = R.dimen.sp_16),
//            letterSpacing = 0.4.sp
//        ),
//        labelLarge = typography.labelLarge.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Bold,
//            fontSize = dimensionResource(id = R.dimen.sp_16),
//            lineHeight = dimensionResource(id = R.dimen.sp_24),
//            letterSpacing = 0.5.sp
//        ),
//        labelMedium = typography.labelMedium.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Medium,
//            fontSize = dimensionResource(id = R.dimen.sp_14),
//            lineHeight = dimensionResource(id = R.dimen.sp_20),
//            letterSpacing = 0.1.sp
//        ),
//        labelSmall = typography.labelSmall.copy(
//            fontFamily = FontFamily.Default,
//            fontWeight = FontWeight.Normal,
//            fontSize = dimensionResource(id = R.dimen.sp_12),
//            lineHeight = dimensionResource(id = R.dimen.sp_16),
//            letterSpacing = 0.5.sp
//        ),
//    )
//}

