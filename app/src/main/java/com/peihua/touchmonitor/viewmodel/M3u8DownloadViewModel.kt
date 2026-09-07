package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peihua.touchmonitor.data.download.DownloadContainer
import com.peihua.touchmonitor.data.download.GalleryExporter
import com.peihua.touchmonitor.data.download.TaskProgress
import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.model.DownloadTaskStatus
import com.peihua.touchmonitor.service.M3u8DownloadService
import com.peihua.touchmonitor.utils.showToast
import com.peihua.touchmonitor.utils.toHumanReadableBytes
import com.peihua.touchmonitor.utils.toHumanReadableRate
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File

/**
 * 下载列表用 Room `Flow<List<DownloadTask>>` 而不是 Paging：
 * 任务是几十条级别，分页收益为零；而 PagingSource 在表被写时会整体 invalidate，
 * 我们每秒都在写 download_task，等于每秒重建一次 PagingData，滚动位置和动画全遭殃。
 */
class M3u8DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DownloadContainer.repository
    private val engine = DownloadContainer.engine

    val uiState: StateFlow<M3u8UiState> =
        combine(repo.observeTasks(), engine.progress) { tasks, progress ->
            val active = progress.values.filter { it.isActive }
            val state = M3u8UiState(
                cards = tasks.map { it.toCard(progress[it.id]) },
                totalRateText = if (active.isEmpty()) "" else active.sumOf { it.bytesPerSec }.toHumanReadableRate(),
                hasActive = active.isNotEmpty(),
            )
            state
        }.flowOn(Dispatchers.Default)
            .conflate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), M3u8UiState.Empty)

    fun enqueue(rawUrl: String) {
        val url = rawUrl.trim()
        val httpUrl = url.toHttpUrlOrNull()
        if (httpUrl == null) {
            showToast("请输入合法的 m3u8 地址")
            return
        }
        viewModelScope.launch {
            val taskId = repo.createTask(url = url, saveFileName = httpUrl.defaultFileName())
            dLog { "任务已创建：id=$taskId, url=$url，准备启动下载服务" }
            M3u8DownloadService.start(getApplication(), taskId)
        }
    }

    fun resume(taskId: Long) = M3u8DownloadService.start(getApplication(), taskId)

    fun pause(taskId: Long) = M3u8DownloadService.pause(getApplication(), taskId)

    fun retry(taskId: Long) = M3u8DownloadService.restart(getApplication(), taskId)

    fun pauseAll() = M3u8DownloadService.pauseAll(getApplication())

    fun delete(taskId: Long, deleteFile: Boolean) {
        viewModelScope.launch {
            // 先停任务再删记录，否则引擎还在往已删除的行上写进度
            engine.stop(taskId)
            val filePath = repo.task(taskId)?.filePath
            repo.deleteTask(taskId)
            DownloadContainer.paths.clearTmp(taskId)
            if (deleteFile && !filePath.isNullOrBlank()) {
                File(filePath).delete()
            }
        }
    }

    fun exportToGallery(taskId: Long) {
        viewModelScope.launch {
            val path = repo.task(taskId)?.filePath
            if (path.isNullOrBlank()) {
                showToast("文件不存在")
                return@launch
            }
            runCatching { GalleryExporter.export(getApplication(), File(path)) }
                .onSuccess { showToast("已保存到相册") }
                .onFailure {
                    eLog { "导出相册失败：${it.message}" }
                    showToast("保存失败：${it.message}")
                }
        }
    }
}

@Immutable
data class M3u8UiState(
    val cards: List<DownloadCard>,
    val totalRateText: String,
    val hasActive: Boolean,
) {
    companion object {
        val Empty = M3u8UiState(emptyList(), "", false)
    }
}

/**
 * 格式化全部在 ViewModel 里做完，composition 里不做字符串拼接 ——
 * 进度每秒更新，composition 里格式化等于每秒重新分配几十个字符串。
 */
@Immutable
data class DownloadCard(
    val id: Long,
    val title: String,
    val url: String,
    val stageText: String,
    val fraction: Float,
    val percentText: String,
    val segmentText: String,
    val rateText: String,
    val etaText: String,
    val sizeText: String,
    val canPause: Boolean,
    val canResume: Boolean,
    val canOpen: Boolean,
    val canRetry: Boolean,
    val isFailed: Boolean,
    val filePath: String,
    val errorMessage: String?,
)

private fun DownloadTask.toCard(progress: TaskProgress?): DownloadCard {
    val stageEnum = runCatching { DownloadTaskStage.valueOf(stage) }
        .getOrDefault(DownloadTaskStage.NEW)
    val statusEnum = runCatching { DownloadTaskStatus.valueOf(status) }
        .getOrDefault(DownloadTaskStatus.NEW)

    val total = progress?.totalSegments?.takeIf { it > 0 } ?: totalMediaSegment.toInt()
    val done = maxOf(progress?.finishedSegments ?: 0, finishMediaSegment.toInt())
    // 字节级进度：分片下载过程中进度条也能平滑推进，不必等整个分片下完才跳
    val finishedBytes = totalBytes.coerceAtLeast(0L)
    val inProgressBytes = ((progress?.downloadedBytes ?: 0L) - finishedBytes).coerceAtLeast(0L)
    val fraction = when {
        stageEnum == DownloadTaskStage.FINISHED -> 1f
        // 有已完成分片：用实际平均值估算总大小（自动修正 HEAD 探测误差）
        total > 0 && done > 0 && finishedBytes > 0 -> {
            val avgSegSize = finishedBytes / done.toLong()
            val estimatedTotal = avgSegSize * total
            ((finishedBytes + inProgressBytes).toFloat() / estimatedTotal).coerceIn(0f, 1f)
        }
        // 无已完成分片但有 HEAD 探测估算：用字节级进度
        total > 0 && (progress?.totalExpectedBytes ?: 0L) > 0 -> {
            ((finishedBytes + inProgressBytes).toFloat() / (progress?.totalExpectedBytes ?: 1L)).coerceIn(0f, 1f)
        }
        total > 0 -> (done.toFloat() / total).coerceIn(0f, 1f)
        else -> 0f
    }
    val running = statusEnum == DownloadTaskStatus.RUNNING
    val finished = stageEnum == DownloadTaskStage.FINISHED

    return DownloadCard(
        id = id,
        title = saveFileName.ifBlank { "任务 $id" },
        url = url,
        stageText = stageEnum.status,
        fraction = fraction,
        percentText = "${(fraction * 100).toInt()}%",
        segmentText = if (total > 0) "$done/$total 片" else "",
        rateText = if (running) progress?.bytesPerSec?.toHumanReadableRate().orEmpty() else "",
        etaText = if (running) formatEta(progress?.etaMillis ?: -1L) else "",
        sizeText = maxOf(totalBytes, progress?.downloadedBytes ?: 0L)
            .takeIf { it > 0L }?.toHumanReadableBytes().orEmpty(),
        canPause = running,
        canResume = !running && !finished,
        canOpen = finished && filePath.isNotBlank(),
        canRetry = !running && stageEnum in RETRYABLE_STAGES,
        isFailed = stageEnum in RETRYABLE_STAGES,
        filePath = filePath,
        errorMessage = errorMessage,
    )
}

private val RETRYABLE_STAGES = setOf(
    DownloadTaskStage.M3U8_PARSE_FAILED,
    DownloadTaskStage.DOWNLOAD_FAILED,
    DownloadTaskStage.SEGMENT_MERGE_FAILED,
)

private fun formatEta(millis: Long): String {
    if (millis <= 0L) return ""
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "剩余 ${minutes}分${seconds}秒" else "剩余 ${seconds}秒"
}

/** 用 m3u8 的文件名（去掉 .m3u8）作为默认标题，拿不到就用时间戳 */
private fun okhttp3.HttpUrl.defaultFileName(): String {
    val segment = pathSegments.lastOrNull { it.isNotBlank() }?.substringBeforeLast('.')
    return segment?.takeIf { it.isNotBlank() && !it.equals("index", true) }
        ?: "video_${System.currentTimeMillis()}"
}
