package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import com.peihua.touchmonitor.ui.components.text.ScaleText

@Composable
fun ErrorView(modifier: Modifier = Modifier, retry: () -> Unit) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(
            Modifier
                .align(Alignment.Center)
        ) {
            ScaleText(
                text = "请求失败,",
                style = typography.titleMedium,
            )
            //text 下划线
            ScaleText(
                text = "请点击重试",
                style = typography.titleMedium,
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        retry()
                    })
        }
    }
}
@Composable
fun LoadingView(modifier: Modifier=Modifier){
    Box(modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}