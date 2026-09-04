package com.peihua.dragswap

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 加在 LazyVerticalGrid / LazyColumn / LazyRow 等 Lazy 布局上，长按后进入拖动。
 * 手势坐标与 [DragSwapState] 的命中测试同源，因此该 Modifier 必须作用于布局本身，
 * 不能加在外层容器上。
 */
@Composable
fun Modifier.dragSwapContainer(state: DragSwapState): Modifier {
    val haptic = LocalHapticFeedback.current
    return this.pointerInput(state) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                state.onDragStart(offset)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount)
            },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragCancel() },
        )
    }
}
