package com.peihua.touchmonitor.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.ui.AppModel

/**
 * 抖音极速版
 */
@Composable
fun DouYinJiSuSettings(modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) {
    Column(modifier) {
        AllSettings(Modifier,model,modelChange)
    }
}