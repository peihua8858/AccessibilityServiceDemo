package com.peihua.touchmonitor.data

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.fz.gson.GsonFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import java.lang.reflect.Type

/**
 * 私有数据存储
 * @param <T> 对象类型
 */
abstract class BaseDataStore<T>(vararg typePairs: Pair<Class<*>, Any>) :
    CoroutineScope by WorkScope() {
    abstract val default: T
    abstract val typeToken: TypeToken<T>
    abstract val storeFile: String

    protected val db by lazy {
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = JsonSerializer(typeToken.type, default, *typePairs),
                producePath = {
                    storeFile.toPath()
                },
            ),
        )
    }
    val data: Flow<T>
        get() = db.data

    fun getData(block: (T) -> Unit) {
        launch {
            data.collect {
                block(it)
            }
        }
    }

    suspend fun getResult(): List<T> {
        return data.toList()
    }

    suspend fun updateSync(settings: T) {
        db.updateData { settings }
    }

    fun update(settings: T) {
        launch {
            updateSync(settings)
        }
    }

    suspend fun updateSync(block: suspend (T) -> T) {
        db.updateData { block(it) }
    }

    fun update(block: suspend (T) -> T) {
        launch {
            updateSync { block(it) }
        }
    }

    class JsonSerializer<T>(
        private val type: Type,
        private val default: T,
        vararg typePairs: Pair<Class<*>, Any>,
    ) : OkioSerializer<T> {
        private val factory: GsonFactory.Factory = GsonFactory.createFactory()
        private val mGson: Gson

        init {
            typePairs.forEach {
                factory.registerTypeAdapter(it.first, it.second)
            }
            mGson = factory.build()
        }

        override val defaultValue: T
            get() = default

        override suspend fun readFrom(source: BufferedSource): T {
            return try {
                val result: T = mGson.fromJson(source.readUtf8(), type)
                dLog { "JsonSerializer>>>readFrom>>>>result:$result" }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                default
            }
        }

        override suspend fun writeTo(
            t: T,
            sink: BufferedSink,
        ) {
            sink.use {
                val result = mGson.toJson(t, type)
                dLog { "JsonSerializer>>>writeTo>>>>result:$result" }
                it.writeUtf8(result)
            }
        }
    }
}