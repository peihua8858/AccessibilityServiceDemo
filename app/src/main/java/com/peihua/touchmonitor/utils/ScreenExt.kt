package com.peihua.touchmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.peihua.compose.utils.isLandscape

@get:Composable
val isLandscape: Boolean
    get(){
        val context = LocalContext.current
        return context.isLandscape
    }