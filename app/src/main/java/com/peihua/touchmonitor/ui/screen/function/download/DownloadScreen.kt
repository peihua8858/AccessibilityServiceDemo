package com.peihua.touchmonitor.ui.screen.function.download

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.screen.storage.StorageScreen

@Composable
fun DownloadScreen(modifier: Modifier) {
    StorageScreen(
        modifier, stringResource(id = R.string.text_download),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
    )
}