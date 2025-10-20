package com.peihua.touchmonitor.ui.components.shape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.utils.toPx

class ArrowDownShape(
    private val radius: Dp,
    private val arrowSize: Dp,
) : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val radiusPx = radius.toPx(density)
        val arrowSizePx = arrowSize.toPx(density)
        val path = Path().apply {
            moveTo(0f, 0f)
            //line 1 top
            lineTo(size.width - radiusPx, 0f)
            quadraticTo(
                x2 = size.width, y2 = radiusPx, x1 =
                    size.width, y1 = 0f
            )
            //line 2 right
            lineTo(size.width, size.height - arrowSizePx - radiusPx)
            quadraticTo(
                x2 = size.width - radiusPx, y2 =
                    size.height - arrowSizePx, x1 = size.width, y1 = size.height -
                        arrowSizePx
            )
            //line 3 bottom
            lineTo(size.width / 2f + arrowSizePx, size.height - arrowSizePx)
            lineTo(size.width / 2f, size.height)
            lineTo(size.width / 2f - arrowSizePx, size.height - arrowSizePx)
            lineTo(radiusPx, size.height - arrowSizePx)
            quadraticTo(
                x2 = 0f, y2 = size.height - radiusPx -
                        arrowSizePx, x1 = 0f, y1 = size.height - arrowSizePx
            )
            //line 4 left
            lineTo(0f, radiusPx)
            quadraticTo(x2 = radiusPx, y2 = 0f, x1 = 0f, y1 = 0f)
            close()
        }

        return Outline.Generic(path)
    }
}

@Composable
fun BubbledText(text: String) {
    val bubbleShape = remember { ArrowDownShape(radius = 8.dp, arrowSize = 12.dp) } // 设置圆角和箭头大小
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary, shape = bubbleShape)
            .padding(16.dp, bottom = 24.dp, top = 8.dp, end = 16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview()
@Composable
fun MainScreen() {
    MaterialTheme {
        Surface {
            BubbledText("这是一个带有箭头的圆角气泡！")
        }
    }
}