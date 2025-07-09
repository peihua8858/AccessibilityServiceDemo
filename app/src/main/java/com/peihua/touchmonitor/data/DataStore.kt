package com.peihua.touchmonitor.data

import android.app.Application
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

/**
 * 私有数据存储
 * @param <T> 对象类型
 * @param application 当前应用
 */
abstract class AbstractDataStore<T>(
    protected val application: Application,
) : BaseDataStore<T>() {
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
) : AbstractDataStore<T>(application) {
//    override val typeToken: TypeToken<T> = object : TypeToken<T>() {}
}

//
///**
// * 列表对象存储
// * @param application 当前应用
// */
//class ListDataStore<T>(
//    application: Application,
//    override val typeToken: TypeToken<ListItem<T>>,
//    override val fileName: String = "SettingsList.json",
//) : AbstractDataStore<ListDataStore.ListItem<T>>(application) {
//
//    override val default: ListItem<T>
//        get() = ListItem()
//
//    fun updateList(item: T) {
//        launch {
//            db.updateData {
//                it.data.add(item)
//                it
//            }
//        }
//    }
//    data class ListItem<T>(val data: MutableList<T> = mutableListOf()){
//        fun add(item: T) {
//            data.add(item)
//        }
//    }
//}
//
///**
// * Map对象存储
// * @param application 当前应用
// */
//class MapDataStore<K, T>(
//    application: Application,
//    override val typeToken: TypeToken<MapItem<K, T>>,
//    override val fileName: String = "SettingsMap.json",
//) : AbstractDataStore<MapDataStore.MapItem<K, T>>(application) {
//
//    fun update(key: K, settings: T) {
//        launch {
//            db.updateData {
//                it.put(key, settings)
//                it
//            }
//        }
//    }
//
//    override val default: MapItem<K, T>
//        get() = MapItem()
//    data class MapItem<K,T>(val data: MutableMap<K, T> = mutableMapOf()){
//        fun put(key: K, value: T) {
//            data.put(key, value)
//        }
//        fun findValue(predicate: (Map.Entry<K, T>) -> Boolean): T? {
//            for (item in data) {
//                if (predicate(item)) {
//                    return item.value
//                }
//            }
//            return null
//        }
//
//        override fun hashCode(): Int {
//            //生成hashCode
//            return data.hashCode()
//        }
//
//        override fun equals(other: Any?): Boolean {
//            if (this === other) return true
//            if (javaClass != other?.javaClass) return false
//
//            other as MapItem<*, *>
//
//            if (data != other.data) return false
//
//            return true
//        }
//    }
//}