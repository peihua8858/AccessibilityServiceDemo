package com.peihua.touchmonitor.ui.screen.function.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.ui.AppModel

/**
 * 美团
 */
@Composable
fun MeiTuanSettings(modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) {
    Column(modifier) {
        AllSettings(Modifier,model,modelChange)
    }
}