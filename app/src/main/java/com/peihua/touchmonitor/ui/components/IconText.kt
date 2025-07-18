package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.labelLargeNormal

@Composable
fun IconText(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = dimensionResource(id = R.dimen.dp_28)),
    painter: Painter,
    text: String,
    tint: Color = LocalContentColor.current,
    style: TextStyle = MaterialTheme.typography.labelLargeNormal,
    orientation: Orientation = Orientation.Horizontal,
    clickable: () -> Unit = {},
) {
    if (orientation == Orientation.Horizontal) {
        Row(
            modifier = modifier
                .heightIn(min = dimensionResource(id = R.dimen.dp_28))
                .clickable(onClick = clickable),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    ),
                painter = painter,
                tint = tint,
                contentDescription = text
            )
            ScaleText(text = text, style = style)
        }
    } else {
        Column(
            modifier = modifier
                .clickable(onClick = clickable),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.dp_32))
                    .padding(dimensionResource(id = R.dimen.dp_8)),
                painter = painter,
                tint = tint,
                contentDescription = text
            )
            ScaleText(text = text, style = style)
        }
    }
}