package com.peihua.touchmonitor.ui.screen.function.autoScroller.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.ui.AppModel

/**
 * 支付宝
 */
@Composable
fun AlipaySettings(modifier: Modifier, provider: AppModel, modelChange: (AppModel) -> Unit) {
    Column(modifier) {
        AllSettings(Modifier,provider,modelChange)
    }
}