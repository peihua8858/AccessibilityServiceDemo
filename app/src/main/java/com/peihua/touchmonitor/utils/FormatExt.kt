package com.peihua.touchmonitor.utils

import java.util.Locale

private const val KB = 1024
private const val MB = 1024 * 1024
private const val GB = 1024 * 1024 * 1024
fun formatSpeed(speed: Float): String {
    if (speed < KB) {
        return "${formatFloat(speed)} KB/s"
    }
    if (speed < MB) {
        return "${formatFloat(speed / KB)} MB/s"
    }
    if (speed < GB) {
        return "${formatFloat(speed / MB)} GB/s"
    }
    return "${formatFloat(speed)} KB/s"
}

fun formatSize(size: Float): String {
    if (size < KB) {
        return "${formatFloat(size)} KB"
    }
    if (size < MB) {
        return "${formatFloat(size / KB)} MB"
    }
    if (size < GB) {
        return "${formatFloat(size / MB)} GB"
    }
    return "${formatFloat(size)} KB"
}

fun formatFloat(speed: Float): String {
    return String.format(Locale.ENGLISH, "%.2f", speed)
}
fun formatInt(value: Int): String {
    return String.format(Locale.ENGLISH, "%02d", value)
}