package com.peihua.touchmonitor.utils

/**
 * 返回匹配给定 [predicate] 的第一个元素，如果没有找到这样的元素，则返回 `null`。
 * @param <T>       泛型参数，集合中放置的元素数据类型
 * @param predicate 给定条件操作符
 * @return 如果集合不为空返回输出字符串，否则返回"null"
 */
inline fun <K, V> Map<K, V>.findValue(predicate: (Map.Entry<K, V>) -> Boolean): V? {
    for (item in this) {
        if (predicate(item)) {
            return item.value
        }
    }
    return null
}
fun String.toLongList(split: String = ","): MutableList<Long> {
    return this.split(split).map { it.toLong() }.toMutableList()
}

fun String.toIntList(split: String = ","): MutableList<Int> {
    return this.split(split).map { it.toInt() }.toMutableList()
}

fun String.toFloatList(split: String = ","): MutableList<Float> {
    return this.split(split).map { it.toFloat() }.toMutableList()
}

fun String.toDoubleList(split: String = ","): MutableList<Double> {
    return this.split(split).map { it.toDouble() }.toMutableList()
}

fun String.toLongArray(split: String = ","): LongArray {
    return this.split(split).map { it.toLong() }.toLongArray()
}

fun String.toIntArray(split: String = ","): IntArray {
    return this.split(split).map { it.toInt() }.toIntArray()
}

fun String.toFloatArray(split: String = ","): FloatArray {
    return this.split(split).map { it.toFloat() }.toFloatArray()
}

fun String.toDoubleArray(split: String = ","): DoubleArray {
    return this.split(split).map { it.toDouble() }.toDoubleArray()
}