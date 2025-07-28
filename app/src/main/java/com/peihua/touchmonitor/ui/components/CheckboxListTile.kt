package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun CheckboxListTile(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    controlAffinity: ListTileControlAffinity = ListTileControlAffinity.leading,
    title: @Composable () -> Unit
) {
    Row(
        modifier = modifier.clickable{
            onCheckedChange(!checked)
        },
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    ) {
        if (controlAffinity == ListTileControlAffinity.leading) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            title()
        } else {
            title()
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}