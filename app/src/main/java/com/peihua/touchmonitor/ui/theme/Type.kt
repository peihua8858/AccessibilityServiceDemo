package com.peihua.touchmonitor.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.utils.dimensionSpResource


internal val LocalTypography = staticCompositionLocalOf { Typography() }

@Composable
fun Typography(scale: Float = 1f): Typography {
    val typography = LocalTypography.current
    return MaterialTheme.typography.copy(
        displayLarge = typography.displayLarge.copy(
            fontSize = dimensionSpResource(R.dimen.sp_57) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_64),
            //-0.2.sp
//            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_0)
        ),
        displayMedium = typography.displayMedium.copy(
            fontSize = dimensionSpResource(R.dimen.sp_45) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_52),
        ),
        displaySmall = typography.displaySmall.copy(
            fontSize = dimensionSpResource(R.dimen.sp_36) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_44),
        ),
        headlineLarge = typography.headlineLarge.copy(
            fontSize = dimensionSpResource(R.dimen.sp_32) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_40),
        ),
        headlineMedium = typography.headlineMedium.copy(
            fontSize = dimensionSpResource(R.dimen.sp_28) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_36),
//            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_0)
        ),
        headlineSmall = typography.headlineSmall.copy(
            fontSize = dimensionSpResource(R.dimen.sp_24) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_32),
//            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_0)
        ),
        titleLarge = typography.titleLarge.copy(
            fontSize = dimensionSpResource(R.dimen.sp_22) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_28),
//            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_0)
        ),
        titleMedium = typography.titleMedium.copy(
            fontSize = dimensionSpResource(R.dimen.sp_16) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_24),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_2)
        ),
        titleSmall = typography.titleSmall.copy(
            fontSize = dimensionSpResource(R.dimen.sp_14) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_20),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_1)
        ),
        bodyLarge = typography.bodyLarge.copy(
            fontSize = dimensionSpResource(R.dimen.sp_16) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_24),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_5)
        ),
        bodyMedium = typography.bodyMedium.copy(
            fontSize = dimensionSpResource(R.dimen.sp_14) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_20),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_2)
        ),
        bodySmall = typography.bodySmall.copy(
            fontSize = dimensionSpResource(R.dimen.sp_12) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_16),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_4)
        ),
        labelLarge = typography.labelLarge.copy(
            fontSize = dimensionSpResource(R.dimen.sp_14) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_20),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_1)
        ),
        labelMedium = typography.labelMedium.copy(
            fontSize = dimensionSpResource(R.dimen.sp_12) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_16),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_5)
        ),
        labelSmall = typography.labelSmall.copy(
            fontSize = dimensionSpResource(R.dimen.sp_11) * scale,
            lineHeight = dimensionSpResource(id = R.dimen.sp_16),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_5)
        ),
    )
}

@get:Composable
val Typography.labelSmallNormal: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Normal)


@get:Composable
val Typography.labelMediumNormal: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Normal)

@get:Composable
val DefaultTextStyle: TextStyle
    get() {
        return LocalTextStyle.current.copy(
            fontWeight = FontWeight.Normal,
            fontSize = dimensionSpResource(id = R.dimen.sp_14),
            lineHeight = dimensionSpResource(id = R.dimen.sp_20),
            letterSpacing = dimensionSpResource(id = R.dimen.sp_0_1)
        )
    }
