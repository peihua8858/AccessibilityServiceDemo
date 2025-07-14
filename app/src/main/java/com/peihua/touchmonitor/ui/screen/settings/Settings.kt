package com.peihua.touchmonitor.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R


@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Text(stringResource(id = R.string.text_settings))
}