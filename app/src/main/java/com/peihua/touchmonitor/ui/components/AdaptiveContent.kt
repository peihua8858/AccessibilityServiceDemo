package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 按窗口宽度断点弹性伸缩内容宽度：
 * - Compact(< 600dp，手机）：内容全宽。
 * - Medium(600~840dp）：两侧留小边距，宽度随屏幕弹性增长。
 * - Expanded(≥ 840dp）：两侧留更大边距并封顶到 [maxContentWidth]，超出部分居中留白。
 *
 * 取代按 values-sw*dp 线性缩放字体/圆角的旧适配方式：不缩放内容尺寸，只约束行宽。
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 1080.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val available = maxWidth
        val horizontalMargin = when {
            available < 600.dp -> 0.dp
            available < 840.dp -> 24.dp
            else -> 48.dp
        }
        val contentWidth = (available - horizontalMargin * 2).coerceAtMost(maxContentWidth)
        Box(
            modifier = modifier.width(contentWidth),
            content = content,
        )
    }
}
