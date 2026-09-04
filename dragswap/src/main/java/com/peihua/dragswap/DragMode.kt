package com.peihua.dragswap

/**
 * 拖动放手后列表的变更语义。
 */
enum class DragMode {
    /** 源与目标两两对调，其余 item 位置不变。拖动过程中不重排，放手时一次性交换。 */
    Swap,

    /** 源插入到目标位置，其间的 item 依次后移。拖动过程中实时重排。 */
    Reorder,
}

/**
 * 按 [mode] 语义就地变更列表。索引越界时不做任何变更 —— 拖动期间列表可能被异步加载改变。
 */
fun <T> MutableList<T>.applyDragMove(mode: DragMode, from: Int, to: Int) {
    if (from == to || from !in indices || to !in indices) return
    when (mode) {
        DragMode.Swap -> {
            val origin = this[from]
            this[from] = this[to]
            this[to] = origin
        }

        DragMode.Reorder -> add(to, removeAt(from))
    }
}
