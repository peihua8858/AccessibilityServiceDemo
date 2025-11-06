package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText


@Composable
fun RoundTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier
            .border(
                dimensionResource(id = R.dimen.dp_1),
                MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .widthIn(min = dimensionResource(id = R.dimen.dp_128)),
        onClick = onClick) {
        ScaleText(
            text = text,
            style = typography.titleMedium,
        )
    }
}