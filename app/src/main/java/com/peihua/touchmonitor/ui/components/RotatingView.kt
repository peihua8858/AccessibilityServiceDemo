package com.peihua.touchmonitor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R

/**
 * 旋转动画组件
 */
@Composable
fun RotatingView(
    modifier: Modifier = Modifier,
    size: DpSize = DpSize(dimensionResource(id = R.dimen.dp_24), dimensionResource(id = R.dimen.dp_24)),
    rotationAngle: Float = 0f,
    durationMillis: Int = 800,
    content: @Composable () -> Unit = {
        Image(
            modifier = Modifier
                .fillMaxSize(),
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
        )
    },
) {
    // 使用 animateFloatAsState 进行动画
    val rotationState = animateFloatAsState(
        rotationAngle,
        animationSpec = tween(durationMillis = durationMillis)
    )
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer(rotationZ = rotationState.value), // 设置图标的大小
        contentAlignment = Alignment.Center // 中心对齐
    ) {
        content()
    }
}