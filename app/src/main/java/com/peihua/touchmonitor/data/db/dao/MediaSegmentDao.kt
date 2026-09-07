package com.peihua.touchmonitor.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.peihua.touchmonitor.model.MediaSegment

@Dao
interface MediaSegmentDao : IDao<MediaSegment, Long> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: MediaSegment)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun updateById(entity: MediaSegment): Int

    @Delete(entity = MediaSegment::class)
    override suspend fun deleteById(entity: MediaSegment): Int

    @Query("SELECT * FROM media_segment WHERE id = :id")
    override suspend fun selectById(id: Long): MediaSegment?

    @Query("SELECT * FROM media_segment")
    override suspend fun selectAll(): MutableList<MediaSegment>?

    @Query("SELECT * FROM media_segment LIMIT :pageSize OFFSET :pageNum")
    override fun selectPage(pageNum: Int, pageSize: Int): PagingSource<Int, MediaSegment>?

    /**
     * 单次调用即单事务，切勿改成 for 循环逐条 insert
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<MediaSegment>)

    @Query("DELETE FROM media_segment WHERE taskId = :tid")
    suspend fun deleteByTaskId(tid: Long): Int

    /**
     * 重试次数打满的分片不会再被返回，进程重启也不会重新无限重试
     */
    @Query("SELECT * FROM media_segment WHERE taskId = :tid AND finished = 0 AND retryCount < :maxRetry ORDER BY seq ASC")
    suspend fun selectPending(tid: Long, maxRetry: Int): List<MediaSegment>

    @Query("SELECT filePath FROM media_segment WHERE taskId = :tid AND finished = 1 AND filePath <> '' ORDER BY seq ASC")
    suspend fun selectFinishedPaths(tid: Long): List<String>

    @Query("SELECT * FROM media_segment WHERE taskId = :tid AND finished = 1 ORDER BY seq ASC")
    suspend fun selectFinished(tid: Long): List<MediaSegment>

    @Query("SELECT COUNT(*) FROM media_segment WHERE taskId = :tid AND finished = 1")
    suspend fun countFinished(tid: Long): Int

    @Query("SELECT COUNT(*) FROM media_segment WHERE taskId = :tid AND finished = 0 AND retryCount >= :maxRetry")
    suspend fun countPermanentlyFailed(tid: Long, maxRetry: Int): Int

    @Query("SELECT IFNULL(SUM(duration), 0) FROM media_segment WHERE taskId = :tid")
    suspend fun sumDurationMs(tid: Long): Long

    @Query("UPDATE media_segment SET finished = 1, filePath = :filePath, byteSize = :byteSize, downloadDuration = :costMillis WHERE id = :id")
    suspend fun markFinished(id: Long, filePath: String, byteSize: Long, costMillis: Long)

    @Query("UPDATE media_segment SET retryCount = retryCount + :delta WHERE id = :id")
    suspend fun bumpRetry(id: Long, delta: Int)

    /**
     * 调用方需按 500 一批切分：SQLite 绑定变量上限为 999(pre-3.32)/32766
     */
    @Query("UPDATE media_segment SET finished = 0, filePath = '', byteSize = 0 WHERE id IN (:ids)")
    suspend fun resetFinished(ids: List<Long>)

    @Query("UPDATE download_task SET totalMediaSegment = :total, updateTime = :now WHERE id = :tid")
    suspend fun updateTaskTotalSegments(tid: Long, total: Long, now: Long)

    @Query("DELETE FROM download_task WHERE id = :tid")
    suspend fun deleteTaskRow(tid: Long): Int

    /**
     * 删旧 + 批插 + 写总数必须同事务，否则会留下"已解析但 0 分片"的中间态。
     *
     * 事务写在 DAO 而不是 Repository：`androidx.room.withTransaction` 走
     * SupportSQLiteOpenHelper，而本库用 `setDriver(AndroidSQLiteDriver())` 构建，
     * 没有 openHelper，调用即抛 "no SupportSQLiteOpenHelper.Factory was configured"。
     * `@Transaction` 由 Room 按 driver API 生成，是这套配置下唯一可用的事务方式。
     */
    @Transaction
    suspend fun replaceForTask(tid: Long, segments: List<MediaSegment>, now: Long) {
        deleteByTaskId(tid)
        insertAll(segments)
        updateTaskTotalSegments(tid, segments.size.toLong(), now)
    }

    @Transaction
    suspend fun deleteTaskWithSegments(tid: Long) {
        deleteByTaskId(tid)
        deleteTaskRow(tid)
    }
}
