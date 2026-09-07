package com.peihua.touchmonitor.data.repository

import com.peihua.touchmonitor.data.db.AppDatabase
import com.peihua.touchmonitor.data.db.FactoryImpl
import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.model.DownloadTaskStatus
import com.peihua.touchmonitor.model.MediaSegment
import kotlinx.coroutines.flow.Flow

/**
 * m3u8 下载任务与分片的唯一数据入口。
 *
 * 所有状态变更走定向 UPDATE 而不是整行 `@Update`：后者需要持有长生命周期的实体对象，
 * 写回时会覆盖用户在 UI 上改过的字段（如 saveFileName），且内存计数与真实完成数容易脱节。
 */
class M3u8Repository(
    private val database: AppDatabase = FactoryImpl().database,
) {
    private val taskDao get() = database.downloadTaskDao()
    private val segmentDao get() = database.mediaSegmentDao()

    fun observeTasks(): Flow<List<DownloadTask>> = taskDao.observeAll()

    suspend fun task(taskId: Long): DownloadTask? = taskDao.selectById(taskId)

    suspend fun createTask(
        url: String,
        saveFileName: String,
        resolution: String = "",
        referer: String? = null,
        maxThreadCount: Int = 0,
    ): Long {
        val now = System.currentTimeMillis()
        return taskDao.insertReturningId(
            DownloadTask(
                id = 0L,
                url = url,
                filePath = "",
                saveFileName = saveFileName,
                totalMediaSegment = 0L,
                finishMediaSegment = 0L,
                downloadDuration = 0L,
                status = DownloadTaskStatus.NEW.name,
                resolution = resolution,
                stage = DownloadTaskStage.NEW.name,
                maxThreadCount = maxThreadCount,
                finishedTime = 0L,
                createTime = now,
                updateTime = now,
                referer = referer,
            )
        )
    }

    /**
     * 事务在 DAO 层（见 [MediaSegmentDao.replaceForTask]）
     */
    suspend fun replaceSegments(taskId: Long, segments: List<MediaSegment>) {
        segmentDao.replaceForTask(taskId, segments, System.currentTimeMillis())
    }

    suspend fun pending(taskId: Long, maxRetry: Int): List<MediaSegment> =
        segmentDao.selectPending(taskId, maxRetry)

    suspend fun finishedSegments(taskId: Long): List<MediaSegment> = segmentDao.selectFinished(taskId)

    suspend fun finishedPathsOrdered(taskId: Long): List<String> = segmentDao.selectFinishedPaths(taskId)

    suspend fun countFinished(taskId: Long): Int = segmentDao.countFinished(taskId)

    suspend fun countPermanentlyFailed(taskId: Long, maxRetry: Int): Int =
        segmentDao.countPermanentlyFailed(taskId, maxRetry)

    suspend fun sumDurationMs(taskId: Long): Long = segmentDao.sumDurationMs(taskId)

    suspend fun markSegmentFinished(id: Long, filePath: String, byteSize: Long, costMillis: Long) =
        segmentDao.markFinished(id, filePath, byteSize, costMillis)

    suspend fun bumpRetry(id: Long, delta: Int) = segmentDao.bumpRetry(id, delta)

    /**
     * 按 500 一批切分，避免撞上 SQLite 绑定变量上限
     */
    suspend fun resetFinished(ids: List<Long>) {
        ids.chunked(500).forEach { segmentDao.resetFinished(it) }
    }

    suspend fun setStage(
        taskId: Long,
        stage: DownloadTaskStage,
        status: DownloadTaskStatus,
        errorMessage: String? = null,
    ) = taskDao.updateStage(taskId, stage.name, status.name, errorMessage, System.currentTimeMillis())

    suspend fun refreshProgress(taskId: Long) =
        taskDao.refreshProgress(taskId, System.currentTimeMillis())

    suspend fun setOutput(taskId: Long, filePath: String, saveFileName: String) =
        taskDao.updateOutput(taskId, filePath, saveFileName, System.currentTimeMillis())

    suspend fun deleteTask(taskId: Long) {
        segmentDao.deleteTaskWithSegments(taskId)
    }

    suspend fun markAllInterrupted(): Int = taskDao.markAllInterrupted(
        runningStatus = DownloadTaskStatus.RUNNING.name,
        stoppedStatus = DownloadTaskStatus.STOPPED_ERROR.name,
        message = "进程被终止，可继续",
    )
}
