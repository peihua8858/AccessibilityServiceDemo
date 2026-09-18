package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.labelLargeNormal

@Composable
fun IconText(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 28.dp),
    painter: Painter,
    text: String,
    tint: Color? = null,
    style: TextStyle = MaterialTheme.typography.labelLargeNormal,
    orientation: Orientation = Orientation.Horizontal,
    clickable:() -> Unit = {},
) {

    if (orientation == Orientation.Horizontal) {
        Row(
            modifier = modifier
                .heightIn(min = 28.dp)
                .clickable(onClick = clickable),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(36.dp)
                    .padding(
                        start = 8.dp,
                        end = 8.dp
                    ),
                painter = painter,
                colorFilter = if (tint != null) ColorFilter.tint(tint) else null,
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
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp),
                painter = painter,
                colorFilter = if (tint != null) ColorFilter.tint(tint) else null,
                contentDescription = text
            )
            ScaleText(text = text, style = style)
        }
    }
}