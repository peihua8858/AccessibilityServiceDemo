package com.peihua.touchmonitor.ui.screen.function.scaleruler

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.RulerView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack

/**
 * 刻度尺
 */
@Composable
fun ScaleRulerScreen(modifier: Modifier) {
    Toolbar(modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_horizon)) {
        RulerView(Modifier)
    }
}
