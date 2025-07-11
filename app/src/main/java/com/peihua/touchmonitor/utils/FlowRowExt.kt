package com.peihua.touchmonitor.utils

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.runtime.Composable

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun<T> FlowRowScope.Items(items: List<T>, content: @Composable FlowRowScope.(T) -> Unit) {
    for (item in items) {
        content(item)
    }
}
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun FlowRowScope.Items(count:Int, content: @Composable FlowRowScope.(Int) -> Unit) {
    for (index in 0 until count) {
        content(index)
    }
}
