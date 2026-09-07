package com.peihua.touchmonitor.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
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

@Database(
    entities = [Settings::class, History::class, DownloadTask::class, MediaSegment::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = V1ToV2Migration::class),
    ]
)
@TypeConverters(ListToStringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadTaskDao(): DownloadTaskDao
    abstract fun mediaSegmentDao(): MediaSegmentDao
}

internal const val dbFileName = "AppStore.db"


class Factory(private val app: Context) {

    val database: AppDatabase by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { build() }

    private fun build(): AppDatabase {
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

internal class V3ToV4Migration : AutoMigrationSpec {

    override fun onPostMigrate(connection: SQLiteConnection) {
//        connection.execSQL("ALTER TABLE `download_task` ADD COLUMN `errorMessage` TEXT DEFAULT NULL")
//        connection.execSQL("ALTER TABLE `download_task` ADD COLUMN `referer` TEXT DEFAULT NULL")
//        connection.execSQL("ALTER TABLE `download_task` ADD COLUMN `totalBytes` INTEGER NOT NULL DEFAULT 0")
//        connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_media_segment` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER NOT NULL, `url` TEXT NOT NULL, `finished` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `downloadDuration` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `seq` INTEGER NOT NULL DEFAULT 0, `keyMethod` TEXT, `keyUri` TEXT, `keyIv` TEXT, `byteSize` INTEGER NOT NULL DEFAULT 0, `retryCount` INTEGER NOT NULL DEFAULT 0)")
//        connection.execSQL("INSERT INTO `_new_media_segment` (`id`,`taskId`,`url`,`finished`,`duration`,`downloadDuration`,`filePath`) SELECT `id`,`taskId`,`url`,`finished`,`duration`,`downloadDuration`,`filePath` FROM `media_segment`")
//        connection.execSQL("DROP TABLE `media_segment`")
//        connection.execSQL("ALTER TABLE `_new_media_segment` RENAME TO `media_segment`")
//        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_media_segment_taskId_seq` ON `media_segment` (`taskId`, `seq`)")
    }
}