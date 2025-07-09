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