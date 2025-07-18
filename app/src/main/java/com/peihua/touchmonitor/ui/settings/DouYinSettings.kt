package com.peihua.touchmonitor.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.ui.AppProvider

/**
 * 抖音
 */
@Composable
fun DouYinSettings(modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) {
    Column(modifier) {
        AllSettings(Modifier,model,modelChange)
    }
}