package com.peihua.dragswap

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * item 拖动状态机，与具体 Lazy 布局无关 —— 通过 [ItemBoundsProvider] 适配网格或线性列表。
 *
 * 命中测试基于 layoutInfo，因此只认当前可见的 item；且假定布局主轴无 contentPadding，
 * 否则 layoutInfo 偏移与手势坐标会有固定差值。
 */
class DragSwapState internal constructor(
    val mode: DragMode,
    private val boundsProvider: ItemBoundsProvider,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    /** 正在被拖动的 item 的 key，无拖动时为 null。 */
    var draggingKey: Any? by mutableStateOf(null)
        private set

    /** [DragMode.Swap] 下当前悬停的目标 item 的 key，用于高亮提示。 */
    var targetKey: Any? by mutableStateOf(null)
        private set

    /** 被拖动 item 相对其原始格位的累计位移。 */
    var draggingOffset: Offset by mutableStateOf(Offset.Zero)
        private set

    private var draggingIndex = -1
    private var targetIndex = -1

    /** 被拖动 item 当前格位的矩形，[DragMode.Reorder] 下会随重排重新锚定。 */
    private var anchor = Rect.Zero

    fun onDragStart(position: Offset) {
        val item = boundsProvider.visibleItems().firstOrNull { position in it.bounds }
        if (item == null) {
            reset()
            return
        }
        draggingIndex = item.index
        draggingKey = item.key
        anchor = item.bounds
        draggingOffset = Offset.Zero
        targetIndex = -1
        targetKey = null
    }

    fun onDrag(delta: Offset) {
        if (draggingIndex < 0) return
        draggingOffset += delta
        val center = anchor.center + draggingOffset
        val hovered = boundsProvider.visibleItems()
            .firstOrNull { it.index != draggingIndex && center in it.bounds }
        when (mode) {
            DragMode.Swap -> {
                targetIndex = hovered?.index ?: -1
                targetKey = hovered?.key
            }

            DragMode.Reorder -> {
                if (hovered != null) {
                    onMove(draggingIndex, hovered.index)
                    // 重新锚定到新格位，并反向补偿位移，使被拖动块的视觉位置保持跟手不跳变
                    draggingOffset += anchor.topLeft - hovered.bounds.topLeft
                    anchor = hovered.bounds
                    draggingIndex = hovered.index
                }
            }
        }
    }

    fun onDragEnd() {
        if (mode == DragMode.Swap && draggingIndex >= 0 && targetIndex >= 0) {
            onMove(draggingIndex, targetIndex)
        }
        reset()
    }

    fun onDragCancel() = reset()

    private fun reset() {
        draggingIndex = -1
        targetIndex = -1
        draggingKey = null
        targetKey = null
        draggingOffset = Offset.Zero
        anchor = Rect.Zero
    }
}

/** 用于 LazyVerticalGrid / LazyHorizontalGrid。 */
@Composable
fun rememberDragSwapGridState(
    gridState: LazyGridState = rememberLazyGridState(),
    mode: DragMode = DragMode.Swap,
    onMove: (from: Int, to: Int) -> Unit,
): DragSwapState {
    val latestOnMove = rememberUpdatedState(onMove)
    return remember(gridState, mode) {
        DragSwapState(mode, gridState.asItemBoundsProvider()) { from, to ->
            latestOnMove.value(from, to)
        }
    }
}

/** 用于 LazyColumn / LazyRow。 */
@Composable
fun rememberDragSwapListState(
    listState: LazyListState = rememberLazyListState(),
    mode: DragMode = DragMode.Swap,
    onMove: (from: Int, to: Int) -> Unit,
): DragSwapState {
    val latestOnMove = rememberUpdatedState(onMove)
    return remember(listState, mode) {
        DragSwapState(mode, listState.asItemBoundsProvider()) { from, to ->
            latestOnMove.value(from, to)
        }
    }
}
