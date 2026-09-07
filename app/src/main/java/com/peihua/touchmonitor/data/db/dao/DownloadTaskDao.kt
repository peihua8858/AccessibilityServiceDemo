package com.peihua.touchmonitor.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.peihua.touchmonitor.model.DownloadTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadTaskDao : IDao<DownloadTask, Long> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: DownloadTask)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun updateById(entity: DownloadTask): Int

    @Delete(entity = DownloadTask::class)
    override suspend fun deleteById(entity: DownloadTask): Int

    @Query("SELECT * FROM download_task WHERE id = :id")
    override suspend fun selectById(id: Long): DownloadTask?

    @Query("SELECT * FROM download_task")
    override suspend fun selectAll(): MutableList<DownloadTask>?

    @Query("SELECT * FROM download_task LIMIT :pageSize OFFSET :pageNum")
    override fun selectPage(pageNum: Int, pageSize: Int): PagingSource<Int, DownloadTask>?

    /**
     * Room 把 `@Insert` 的返回值映射成 rowid，这是拿到自增主键的唯一正确做法
     */
    @Insert
    suspend fun insertReturningId(entity: DownloadTask): Long

    @Query("SELECT * FROM download_task ORDER BY createTime DESC")
    fun observeAll(): Flow<List<DownloadTask>>

    @Query("DELETE FROM download_task WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("UPDATE download_task SET stage = :stage, status = :status, errorMessage = :errorMessage, updateTime = :now WHERE id = :id")
    suspend fun updateStage(id: Long, stage: String, status: String, errorMessage: String?, now: Long)

    @Query("UPDATE download_task SET totalMediaSegment = :total, updateTime = :now WHERE id = :id")
    suspend fun updateTotalSegments(id: Long, total: Long, now: Long)

    @Query("UPDATE download_task SET filePath = :filePath, saveFileName = :saveFileName, finishedTime = :now, updateTime = :now WHERE id = :id")
    suspend fun updateOutput(id: Long, filePath: String, saveFileName: String, now: Long)

    /**
     * 进度从 media_segment 现算而不是内存里 ++，保证幂等且崩溃后不会出现超过 100% 的进度
     */
    @Query(
        """UPDATE download_task SET
             finishMediaSegment = (SELECT COUNT(*) FROM media_segment WHERE taskId = :id AND finished = 1),
             totalBytes = (SELECT IFNULL(SUM(byteSize), 0) FROM media_segment WHERE taskId = :id AND finished = 1),
             updateTime = :now
           WHERE id = :id"""
    )
    suspend fun refreshProgress(id: Long, now: Long)

    /**
     * 进程被杀后 RUNNING 是脏状态，启动时统一标成可继续，否则 UI 会显示一堆假的"运行中"
     */
    @Query("UPDATE download_task SET status = :stoppedStatus, errorMessage = :message WHERE status = :runningStatus")
    suspend fun markAllInterrupted(
        runningStatus: String,
        stoppedStatus: String,
        message: String,
    ): Int
}
