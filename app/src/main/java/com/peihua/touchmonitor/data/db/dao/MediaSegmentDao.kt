package com.peihua.touchmonitor.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("select * from media_segment where taskId= :tid and finished= :isFinished")
    suspend fun selectByTaskIdAndFinished(tid: Long, isFinished: Boolean): MutableList<MediaSegment>

    @Query("select * from media_segment where taskId= :tid and finished= :isFinished limit :size")
    suspend fun selectByTaskIdAndFinished(tid: Long, isFinished: Boolean, size: Int): MutableList<MediaSegment>

}