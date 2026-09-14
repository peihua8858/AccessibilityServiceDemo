package com.peihua.touchmonitor.ui.screen.function

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CycleRulerView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.rememberIntState

@Composable
fun AngleMeterScreen(modifier: Modifier){
    Toolbar(modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_angle_meter)) {
        val angle = rememberIntState(0)
        Column(modifier = modifier.fillMaxSize()) {
            CycleRulerView(Modifier.fillMaxSize(),angle.intValue, onAngleChange = { angle.intValue = it })
        }
    }

}