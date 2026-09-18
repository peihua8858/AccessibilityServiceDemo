package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText

@Composable
fun CollectScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_collect)
    ) {
        ScaleText(stringResource(id = R.string.text_collect))
    }
}