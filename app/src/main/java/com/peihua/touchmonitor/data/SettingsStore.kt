package com.peihua.touchmonitor.data

import android.app.Application
import androidx.compose.foundation.gestures.Orientation
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.ui.json
import com.peihua.touchmonitor.utils.WorkScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath

class SettingsStore(storePath: String) : CoroutineScope by WorkScope() {
    constructor(application: Application) : this(application.filesDir.absolutePath)

    private val storeFile = "$storePath/settings.json"
    private val db = DataStoreFactory.create(
        storage = OkioStorage<Settings>(
            fileSystem = FileSystem.SYSTEM,
            serializer = SettingsJsonSerializer,
            producePath = {
                storeFile.toPath()
            },
        ),
    )
    val data: Flow<Settings> = db.data
    fun getData(block: (Settings) -> Unit) {
        launch {
            data.collect {
                block(it)
            }
        }
    }

    suspend fun getResult(): List<Settings> = data.toList()

    fun updateSettings(settings: Settings) {
        launch {
            db.updateData { settings }
        }
    }

    internal object SettingsJsonSerializer : OkioSerializer<Settings> {
        override val defaultValue: Settings
            get() = Settings("", Orientation.Vertical, true)

        override suspend fun readFrom(source: BufferedSource): Settings {
            return json.decodeFromString<Settings>(source.readUtf8())
        }

        override suspend fun writeTo(
            t: Settings,
            sink: BufferedSink,
        ) {
            sink.use {
                it.writeUtf8(json.encodeToString(Settings.serializer(), t))
            }
        }
    }
}

val settingsStore: SettingsStore by lazy { SettingsStore(ServiceApplication.application) }