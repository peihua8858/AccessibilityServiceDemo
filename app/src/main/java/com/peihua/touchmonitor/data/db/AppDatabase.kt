package com.peihua.touchmonitor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.db.dao.HistoryDao
import com.peihua.touchmonitor.data.db.dao.SettingsDao
import com.peihua.touchmonitor.ui.History
import com.peihua.touchmonitor.ui.ListToStringConverter
import com.peihua.touchmonitor.ui.Settings
import kotlinx.coroutines.Dispatchers

@Database(entities = [Settings::class, History::class], version = 1)
@TypeConverters(ListToStringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun historyDao(): HistoryDao
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
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

private val mFactory: Factory by lazy {
    Factory(app = ServiceApplication.application)
}

fun FactoryImpl(): Factory {
    return mFactory
}
