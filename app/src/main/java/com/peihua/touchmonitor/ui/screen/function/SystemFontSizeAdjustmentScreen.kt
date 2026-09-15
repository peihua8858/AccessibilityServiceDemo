package com.peihua.touchmonitor.ui.screen.function

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun SystemFontSizeAdjustmentScreen(modifier: Modifier) {
    Toolbar(modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_system_font_size_adjustment)) {
        Column(modifier = modifier.fillMaxSize()) {

        }
    }
}
