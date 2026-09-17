package com.peihua.touchmonitor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@get:Composable
val customShapes
    get() = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp), // 小型圆角设为 8dp
        medium = RoundedCornerShape(12.dp), // 中型圆角设为 12dp
        large = RoundedCornerShape(16.dp), // 大型圆角设为 16dp
        extraLarge = RoundedCornerShape(20.dp),
    )


@Composable
fun SliderColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
    )
}