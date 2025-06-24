package com.peihua.touchmonitor.utils

import java.util.Locale

private const val KB = 1024
private const val MB = 1024 * 1024
private const val GB = 1024 * 1024 * 1024
fun formatSpeed(speed: Float): String {
    if (speed < KB) {
        return "${format(speed)} KB/s"
    }
    if (speed < MB) {
        return "${format(speed / KB)} MB/s"
    }
    if (speed < GB) {
        return "${format(speed / MB)} GB/s"
    }
    return "${format(speed)} KB/s"
}

fun formatSize(size: Float): String {
    if (size < KB) {
        return "${format(size)} KB"
    }
    if (size < MB) {
        return "${format(size / KB)} MB"
    }
    if (size < GB) {
        return "${format(size / MB)} GB"
    }
    return "${format(size)} KB"
}

fun format(speed: Float): String {
    return String.format(Locale.ENGLISH, "%.2f", speed)
}