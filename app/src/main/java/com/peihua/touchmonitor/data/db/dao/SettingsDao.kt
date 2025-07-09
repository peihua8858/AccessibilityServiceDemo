package com.peihua.touchmonitor.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.peihua.touchmonitor.ui.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface  SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fruittie: Settings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fruitties: List<Settings>)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(model: Settings)
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(model: List<Settings>)
    @Query("SELECT * FROM Settings")
    suspend fun queryAll(): List<Settings>

    @Query("SELECT * FROM Settings")
    fun getAllAsFlow(): Flow<List<Settings>>

    @Query("SELECT COUNT(*) as count FROM Settings")
    suspend fun count(): Int

    @Query("SELECT * FROM Settings WHERE packageName in (:ids)")
    suspend fun loadAll(ids: List<String>): List<Settings>

    @Query("SELECT * FROM Settings WHERE packageName in (:ids)")
    suspend fun loadMapped(ids: List<String>): Map<
            @MapColumn(columnName = "packageName")
            String,
            Settings,
            >
}