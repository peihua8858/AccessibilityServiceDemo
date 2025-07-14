package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardViewItem(
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
    icon: (@Composable () -> Unit) = {},
    title: (@Composable () -> Unit) = {},
) {
    if (orientation == Orientation.Vertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            title()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            title()
        }
    }
}