package com.peihua.touchmonitor.utils

import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.peihua.touchmonitor.ServiceApplication
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

private const val KB = 1024f
private const val MB = KB * KB
private const val GB = MB * KB

private val PICTURE_FORMAT_DATE = SimpleDateFormat("MMM dd, yyyy", Locale.CHINA)
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
    return Formatter.formatFileSize(ServiceApplication.application, this)
}

fun Context.formatFileSize(size:Long): String {
    return Formatter.formatFileSize(this, size)
}

fun formatFloat(speed: Float): String {
    return String.format(Locale.ENGLISH, "%.2f", speed)
}

fun formatInt(value: Int): String {
    return String.format(Locale.ENGLISH, "%02d", value)
}

/**
 * 将格式化为[format]的字符串格式化为时间戳
 */
fun String.formatToDate(format: String): Long {
    if (this.isEmpty()) {
        return 0
    }
    return SimpleDateFormat(format, Locale.getDefault()).parse(this)?.time ?: 0
}

fun Long.formatToDate(format: String): String {
    if (this == 0L) {
        return ""
    }
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
}

fun Long.formatPictureDate(): String {
    if (this == 0L) {
        return ""
    }
    return PICTURE_FORMAT_DATE.format(this)
}
