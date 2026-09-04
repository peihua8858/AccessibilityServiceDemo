package com.peihua.dragswap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private const val DRAG_SCALE = 1.08f

/**
 * 网格中可拖动的 item。[key] 必须与 `items(list, key)` 传入的 key 一致，组件据此判断自身身份。
 *
 * @param content isDragging 表示自身正被拖动；isTarget 表示自身是当前的交换目标（仅 [DragMode.Swap]）。
 * 两者的视觉表现交由调用方决定。
 */
@Composable
fun LazyGridItemScope.DragSwapItem(
    state: DragSwapState,
    key: Any,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(isDragging: Boolean, isTarget: Boolean) -> Unit,
) {
    val isDragging = state.draggingKey == key
    // 被拖动块由 graphicsLayer 手动位移，若同时开启格位动画会与位移叠加抖动
    val placement = if (state.mode == DragMode.Reorder && !isDragging) {
        Modifier.animateItem()
    } else {
        Modifier.animateItem(placementSpec = null)
    }
    DragSwapItemContent(state, key, isDragging, modifier.then(placement), content)
}

/** LazyColumn / LazyRow 中可拖动的 item，语义同网格版本。 */
@Composable
fun LazyItemScope.DragSwapItem(
    state: DragSwapState,
    key: Any,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(isDragging: Boolean, isTarget: Boolean) -> Unit,
) {
    val isDragging = state.draggingKey == key
    val placement = if (state.mode == DragMode.Reorder && !isDragging) {
        Modifier.animateItem()
    } else {
        Modifier.animateItem(placementSpec = null)
    }
    DragSwapItemContent(state, key, isDragging, modifier.then(placement), content)
}

@Composable
private fun DragSwapItemContent(
    state: DragSwapState,
    key: Any,
    isDragging: Boolean,
    modifier: Modifier,
    content: @Composable BoxScope.(isDragging: Boolean, isTarget: Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                if (isDragging) {
                    translationX = state.draggingOffset.x
                    translationY = state.draggingOffset.y
                    scaleX = DRAG_SCALE
                    scaleY = DRAG_SCALE
                    shadowElevation = 8.dp.toPx()
                }
            }
    ) {
        content(isDragging, state.targetKey == key)
    }
}
