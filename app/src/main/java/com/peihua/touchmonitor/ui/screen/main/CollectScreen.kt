package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.ToolbarNoNav

@Composable
fun CollectScreen(modifier: Modifier = Modifier) {
    ToolbarNoNav(
        modifier = modifier,
        title = stringResource(id = R.string.text_collect)
    ) {
        Text(stringResource(id = R.string.text_collect))
    }
}