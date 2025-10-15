package com.peihua.touchmonitor.ui.screen.function.devices

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.formatToDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ScreenTimeScreen(modifier: Modifier = Modifier) {
    val times = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val textMeasurer = rememberTextMeasurer()
    val timeFontSize = dimensionSpResource(R.dimen.sp_48)
    val dateFontSize = dimensionSpResource(R.dimen.sp_16)
    val timeStyle = TextStyle(color = Color.White, fontSize = timeFontSize)
    val dateStyle = TextStyle(color = Color.White, fontSize = dateFontSize)
    val time = times.longValue.formatToDate("HH:mm:ss")
    val date = times.longValue.formatToDate("yyyy 年 MM 月 dd 日")
    val timeSize = textMeasurer.measure(time, timeStyle).size
    val dateSize = textMeasurer.measure(time, dateStyle).size
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val timeOffset = Offset(
            center.x - timeSize.width / 2f,
            center.y - timeSize.height / 2f
        )
        drawText(
            textMeasurer = textMeasurer,
            topLeft = timeOffset,
            style = TextStyle(color = Color.White, fontSize = timeFontSize),
            text = time
        )
        drawText(
            textMeasurer = textMeasurer,
            topLeft = Offset(
                center.x - dateSize.width,
                center.y - dateSize.height / 2f + timeSize.height + 16.dp.value
            ),
            style = TextStyle(color = Color.White, fontSize = dateFontSize),
            text = date
        )
        drawIntoCanvas {
        }
    }
    LaunchedEffect(key1 = null) {
        while (isActive) {
            delay(1000)
            times.longValue = System.currentTimeMillis()
        }
    }
}