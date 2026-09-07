@file:JvmName("FileUtil")
@file:JvmMultifileClass

package com.peihua.touchmonitor.utils

import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import kotlinx.coroutines.ensureActive
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.coroutineContext


val File.ensureDirExist: Boolean
    get() {
        return if (!exists()) {
            dLog { "目录${absolutePath}不存在，创建它" }
            val isCreate = mkdirs()
            if (isCreate) {
                dLog { "目录${absolutePath}已创建" }
                true
            } else {
                eLog { "目录${absolutePath}创建失败" }
                false
            }
        }else true
    }

@Throws(IOException::class)
fun InputStream?.copy(out: OutputStream,bytesCounter: AtomicLong?=null) {
    this?.let { it ->
        BufferedInputStream(it).use { input ->
            BufferedOutputStream(out).use { output ->
                var len = 0
                val buffer = ByteArray(1024)
                while (input.read(buffer).also { len = it } != -1) {
                    output.write(buffer, 0, len)
                    bytesCounter?.addAndGet(len.toLong())
                }
                output.flush()
            }
        }
    }
}

private const val COPY_BUFFER_SIZE = 64 * 1024
private const val HEAD_SNIFF_SIZE = 64

/**
 * 可被协程取消打断的流拷贝。每轮 [ensureActive] 是「暂停能真的停」的另一半保障
 * （前一半是 OkHttp 的 `Call.cancel()`）。
 *
 * @param onHead 收到的第一批字节（最多 [HEAD_SNIFF_SIZE] 个），用于识别 CDN 返回的错误页
 * @return 实际写出的字节数
 */
@Throws(IOException::class)
suspend fun InputStream.copyToCancellable(
    out: OutputStream,
    bytesCounter: AtomicLong? = null,
    onHead: ((ByteArray) -> Unit)? = null,
): Long {
    val buffer = ByteArray(COPY_BUFFER_SIZE)
    var total = 0L
    var headTaken = false
    while (true) {
        coroutineContext.ensureActive()
        val len = read(buffer)
        if (len == -1) break
        if (len > 0 && !headTaken) {
            headTaken = true
            onHead?.invoke(buffer.copyOf(minOf(len, HEAD_SNIFF_SIZE)))
        }
        out.write(buffer, 0, len)
        total += len
        bytesCounter?.addAndGet(len.toLong())
    }
    out.flush()
    return total
}

fun Long.toHumanReadableBytes(): String = when {
    this < 1024 -> "${this}B"
    this < 1024 * 1024 -> String.format(Locale.US, "%.1fKB", this / 1024.0)
    this < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1fMB", this / (1024.0 * 1024))
    else -> String.format(Locale.US, "%.2fGB", this / (1024.0 * 1024 * 1024))
}

fun Long.toHumanReadableRate(): String = "${toHumanReadableBytes()}/s"