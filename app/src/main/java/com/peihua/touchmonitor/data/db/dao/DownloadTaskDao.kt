package com.peihua.touchmonitor.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.peihua.touchmonitor.model.DownloadTask
@Dao
interface DownloadTaskDao : IDao<DownloadTask, Long> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: DownloadTask): Int
    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun updateById(entity: DownloadTask): Int

    @Delete(entity = DownloadTask::class)
    override suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM download_task WHERE id = :id")
    override suspend fun selectById(id: Long): DownloadTask?

    @Query("SELECT * FROM download_task")
    override suspend fun selectAll(): MutableList<DownloadTask>?

    @Query("SELECT * FROM download_task LIMIT :pageSize OFFSET :pageNum")
    override fun selectPage(pageNum: Int, pageSize: Int): PagingSource<Int, DownloadTask>?
}