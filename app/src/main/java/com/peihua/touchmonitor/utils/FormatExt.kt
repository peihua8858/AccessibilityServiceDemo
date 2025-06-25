package com.peihua.touchmonitor.utils

import java.util.Locale

private const val KB = 1024f
private const val MB = KB * KB
private const val GB = MB * KB


fun Float.formatSpeed(): String {
    return this.toDouble().formatSpeed()
}

fun Double.formatSpeed(): String {
    if (this == 0.0) {
        return "0 B/s"
    }
    return when {
        this < KB -> {
            String.format(Locale.US, "%.2f B/s", this)
        }

        this < MB -> {
            String.format(Locale.US, "%.2f KB/s", this / KB)
        }

        this < GB -> {
            String.format(Locale.US, "%.2f MB/s", this / MB)
        }

        else -> {
            String.format(Locale.US, "%.2f GB/s", this / GB)
        }
    }
}

fun Long.formatFileSize(): String {
    if (this == 0L) {
        return "0 B"
    }
    return when {
        this < KB -> {
            String.format(Locale.US, "%.2f B", this.toFloat())
        }

        this < MB -> {
            String.format(Locale.US, "%.2f KB", this / KB)
        }

        this < GB -> {
            String.format(Locale.US, "%.2f MB", this / MB)
        }

        else -> {
            String.format(Locale.US, "%.2f GB", this / GB)
        }
    }
}

fun formatFloat(speed: Float): String {
    return String.format(Locale.ENGLISH, "%.2f", speed)
}

fun formatInt(value: Int): String {
    return String.format(Locale.ENGLISH, "%02d", value)
}