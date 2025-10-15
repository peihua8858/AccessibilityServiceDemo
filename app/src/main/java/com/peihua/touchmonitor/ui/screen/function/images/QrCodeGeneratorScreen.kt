package com.peihua.touchmonitor.ui.screen.function.images

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.Toolbar
import qrgenerator.qrkitpainter.QrKitBrush
import qrgenerator.qrkitpainter.QrKitColors
import qrgenerator.qrkitpainter.QrKitShapes
import qrgenerator.qrkitpainter.customBrush
import qrgenerator.qrkitpainter.rememberQrKitPainter
import qrgenerator.qrkitpainter.solidBrush


@Composable
fun QrCodeGeneratorScreen(modifier: Modifier = Modifier) {
    val painter = rememberQrKitPainter(data = "1234567890", qrOptions = {
        shapes = QrKitShapes()
        colors = QrKitColors(
            darkBrush = QrKitBrush.customBrush {
                Brush.linearGradient(
                    0f to Color.Red,
                    1f to Color.Red,
                    end = Offset(it, it)
                )
            },
//            darkBrush = QrKitBrush.solidBrush(color = Color.Red),
            lightBrush = QrKitBrush.solidBrush(color = Color.Green),
            ballBrush = QrKitBrush.solidBrush(color = Color.Red),
//            frameBrush = QrKitBrush.solidBrush(color = Color.Red),
            frameBrush = QrKitBrush.customBrush {
                Brush.linearGradient(
                    0f to Color.Red,
                    1f to Color.Green,
                    start = Offset(0f, 0f),
                    end = Offset(it, it)
                )
            },

        )
    })

    Toolbar(modifier = modifier, title = "二维码生成") {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

    }
}