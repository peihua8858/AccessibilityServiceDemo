package com.peihua.touchmonitor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.peihua.touchmonitor.R

@get:Composable
val customShapes
    get() = Shapes(
        extraSmall = RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)),
        small = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)), // 小型圆角设为 8dp
        medium = RoundedCornerShape(dimensionResource(id = R.dimen.dp_12)), // 中型圆角设为 12dp
        large = RoundedCornerShape(dimensionResource(id = R.dimen.dp_16)), // 大型圆角设为 16dp
        extraLarge = RoundedCornerShape(dimensionResource(id = R.dimen.dp_20)),
    )


@Composable
fun SliderColors(): SliderColors {
    return SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
    )
}