package com.peihua.touchmonitor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
fun RotatingArrow(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(24.dp, 24.dp),
    isExtended: Boolean,
) {
    // 旋转角度，up 旋转 180 度，down 旋转 0 度
    val rotationAngle = if (isExtended) 180f else 0f
    // 使用 animateFloatAsState 进行动画
    val rotationState = animateFloatAsState(
        rotationAngle,
        animationSpec = tween(durationMillis = 800)
    )
    Box(
        modifier = modifier
            .size(size), // 设置图标的大小
        contentAlignment = Alignment.Center // 中心对齐
    ) {
        Image(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotationState.value)
        )
    }
}