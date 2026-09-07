package com.peihua.touchmonitor.data.download

import androidx.compose.runtime.Immutable
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.model.DownloadTaskStatus
import java.util.concurrent.atomic.AtomicLong

/**
 * 任务的实时进度快照。速率与 ETA 只活在内存里，永不入库 —— 每片都写
 * `download_task` 会让 `observeAll()` 每片重发一次，列表每分钟重组几百次。
 */
@Immutable
data class TaskProgress(
    val taskId: Long,
    val stage: DownloadTaskStage,
    val status: DownloadTaskStatus,
    val finishedSegments: Int,
    val totalSegments: Int,
    val downloadedBytes: Long,
    val bytesPerSec: Long,
    /** 负数表示无法估算 */
    val etaMillis: Long,
    val message: String? = null,
    /** 估算的总字节数，0 表示尚未能估算 */
    val totalExpectedBytes: Long = 0L,
) {
    val isActive: Boolean get() = status == DownloadTaskStatus.RUNNING

    val fraction: Float
        get() = if (totalSegments <= 0) 0f
        else (finishedSegments.toFloat() / totalSegments).coerceIn(0f, 1f)
}

/**
 * 单任务速率统计。8 路并发下原始的每秒字节数波动极大，用 EWMA 平滑，
 * 否则 UI 上的数字会乱跳。
 */
internal class RateMeter {
    /** 累计网络字节数（密文长度），由各分片下载协程并发累加 */
    val counter = AtomicLong(0L)

    private var lastTotal = 0L
    private var smoothed = 0.0

    fun sample(elapsedMillis: Long): Long {
        val total = counter.get()
        val delta = total - lastTotal
        lastTotal = total
        if (elapsedMillis <= 0L) return smoothed.toLong()
        val instant = delta * 1000.0 / elapsedMillis
        smoothed = if (smoothed == 0.0) instant else EWMA_OLD * smoothed + EWMA_NEW * instant
        return smoothed.toLong()
    }

    private companion object {
        const val EWMA_OLD = 0.6
        const val EWMA_NEW = 0.4
    }
}
