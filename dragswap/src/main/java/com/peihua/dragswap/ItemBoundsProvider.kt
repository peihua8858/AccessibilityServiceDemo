package com.peihua.dragswap

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

internal class ItemBounds(val index: Int, val key: Any, val bounds: Rect)

/**
 * 把各种 Lazy 布局的 layoutInfo 归一成"可见 item 的矩形列表"。
 * 坐标原点与加在布局自身上的 pointerInput 一致（均为布局左上角），因此两者可直接做命中测试。
 */
internal fun interface ItemBoundsProvider {
    fun visibleItems(): List<ItemBounds>
}

/** 适配 LazyVerticalGrid / LazyHorizontalGrid：item 本身就是二维矩形。 */
internal fun LazyGridState.asItemBoundsProvider() = ItemBoundsProvider {
    layoutInfo.visibleItemsInfo.map { item ->
        ItemBounds(
            index = item.index,
            key = item.key,
            bounds = Rect(
                offset = Offset(item.offset.x.toFloat(), item.offset.y.toFloat()),
                size = Size(item.size.width.toFloat(), item.size.height.toFloat()),
            ),
        )
    }
}

/** 适配 LazyColumn / LazyRow：item 只有主轴偏移与长度，交叉轴按视口铺满。 */
internal fun LazyListState.asItemBoundsProvider() = ItemBoundsProvider {
    val info = layoutInfo
    val viewport = info.viewportSize
    info.visibleItemsInfo.map { item ->
        val start = item.offset.toFloat()
        val end = (item.offset + item.size).toFloat()
        val bounds = if (info.orientation == Orientation.Vertical) {
            Rect(0f, start, viewport.width.toFloat(), end)
        } else {
            Rect(start, 0f, end, viewport.height.toFloat())
        }
        ItemBounds(item.index, item.key, bounds)
    }
}
