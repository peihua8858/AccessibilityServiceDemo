package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun ExtendedListTile(
    modifier: Modifier = Modifier,
    isExtended: Boolean,
    title: @Composable (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val isExtended = remember { mutableStateOf(isExtended) }
    Column(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExtended.value = !isExtended.value
                }) {
            title(isExtended.value)
        }
        if (isExtended.value) {
            content()
        }
    }
}

