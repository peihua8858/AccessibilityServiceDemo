package com.peihua.touchmonitor.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.ui.AppProvider

@Composable
fun MeiTuanSettings(modifier: Modifier, provider: AppProvider, modelChange: (AppProvider) -> Unit) {
    Column(modifier) {
        AllSettings(Modifier,provider,modelChange)
    }
}