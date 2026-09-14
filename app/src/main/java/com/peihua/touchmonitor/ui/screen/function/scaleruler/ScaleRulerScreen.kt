package com.peihua.touchmonitor.ui.screen.function.scaleruler

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.RulerView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.UNIT_CM
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
        title = stringResource(R.string.text_scale_ruler)) {
        Column(modifier = modifier.fillMaxSize().padding(2.dp)) {
            RulerView(Modifier.fillMaxSize(),UNIT_CM)
        }
    }
}
