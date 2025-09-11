package com.peihua.touchmonitor.ui.screen.function.collect

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun CollectScreen(modifier: Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_collect_folder),
        navigateUp = {
            popBackStack()
        }) {

    }
}