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
    override suspend fun insert(entity: MediaSegment): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun updateById(entity: MediaSegment): Int

    @Delete(entity = MediaSegment::class)
    override suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM media_segment WHERE id = :id")
    override suspend fun selectById(id: Long): MediaSegment?

    @Query("SELECT * FROM media_segment")
    override suspend fun selectAll(): MutableList<MediaSegment>?

    @Query("SELECT * FROM media_segment LIMIT :pageSize OFFSET :pageNum")
    override fun selectPage(pageNum: Int, pageSize: Int): PagingSource<Int, MediaSegment>?
}