package com.peihua.touchmonitor.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.peihua.touchmonitor.ui.History
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fruittie: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fruitties: List<History>)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(model: History)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(model: List<History>)

    @Query("SELECT * FROM History")
    fun getAllAsFlow(): Flow<List<History>>

    @Query("SELECT * FROM History")
    suspend fun queryAll(): List<History>
    @Query("SELECT * FROM History WHERE packageName = :id")
    suspend fun query(id: String): History

    @Query("SELECT COUNT(*) as count FROM History")
    suspend fun count(): Int

    @Query("SELECT * FROM History WHERE packageName in (:ids)")
    suspend fun loadAll(ids: List<String>): List<History>

    @Query("SELECT * FROM History WHERE packageName in (:ids)")
    suspend fun loadMapped(ids: List<String>): Map<
            @MapColumn(columnName = "packageName")
            String,
            History,
            >
}