package com.peihua.touchmonitor.ui.screen.function.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun ScreenDeadPixelsScreen(modifier: Modifier = Modifier) {
    var index = remember { mutableIntStateOf(0) }
    val colorState = remember { mutableStateOf(Color.Red) }
    //隐藏状态栏
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = false
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorState.value)
            .clickable {
                index.intValue++
                when (index.intValue) {
                    0 -> colorState.value = Color.Red
                    1 -> colorState.value = Color.Green
                    2 -> colorState.value = Color.Blue
                    3 -> colorState.value = Color.White
                    else -> {
                        systemUiController.isSystemBarsVisible = true
                        popBackStack()
                    }
                }
            }
    ) {

    }
}