package com.peihua.touchmonitor.data

import android.app.Application
import com.google.gson.reflect.TypeToken

/**
 * 私有数据存储
 * @param <T> 对象类型
 * @param application 当前应用
 */
abstract class AbstractDataStore<T>(
    protected val application: Application,
    vararg typePairs: Pair<Class<*>, Any>,
) : BaseDataStore<T>(*typePairs) {
    open val fileName: String = "Settings.json"
    override val storeFile: String
        get() = application.filesDir.absolutePath + "/" + fileName
}

/**
 * 单个对象存储
 * @param fileName 文件名
 * @param application 当前应用
 */
class DataStore<T>(
    application: Application,
    override val default: T,
    override val typeToken: TypeToken<T>,
    override val fileName: String = "Settings.json",
    vararg typePairs: Pair<Class<*>,Any>,
) : AbstractDataStore<T>(application, *typePairs)