package com.peihua.touchmonitor.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.db.dao.DownloadTaskDao
import com.peihua.touchmonitor.data.db.dao.HistoryDao
import com.peihua.touchmonitor.data.db.dao.MediaSegmentDao
import com.peihua.touchmonitor.data.db.dao.SettingsDao
import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.MediaSegment
import com.peihua.touchmonitor.ui.History
import com.peihua.touchmonitor.ui.ListToStringConverter
import com.peihua.touchmonitor.ui.Settings
import kotlinx.coroutines.Dispatchers

@Database(entities = [Settings::class, History::class, DownloadTask::class, MediaSegment::class], version = 3, autoMigrations = [
    AutoMigration(from = 1, to = 2, spec = V1ToV2Migration::class),
])
@TypeConverters(ListToStringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadTaskDao(): DownloadTaskDao
    abstract fun mediaSegmentDao(): MediaSegmentDao
}

internal const val dbFileName = "AppStore.db"


class Factory(private val app: Context) {

    fun createRoomDatabase(): AppDatabase {
        val dbFile = app.getDatabasePath(dbFileName)
        return Room.databaseBuilder<AppDatabase>(
            context = app,
            name = dbFile.absolutePath,
        )
            .addTypeConverter(ListToStringConverter())
            .setDriver(AndroidSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }
}
class V1ToV2Migration : AutoMigrationSpec{
    override fun onPostMigrate(connection: SQLiteConnection) {
    }
}

private val mFactory: Factory by lazy {
    Factory(app = ServiceApplication.application)
}

fun FactoryImpl(): Factory {
    return mFactory
}
