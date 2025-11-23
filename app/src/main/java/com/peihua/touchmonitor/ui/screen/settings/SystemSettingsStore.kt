package com.peihua.touchmonitor.ui.screen.settings

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.DataStore
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.ui.theme.ThemeMode
import com.peihua.touchmonitor.ui.theme.ThemeModeDeserializer
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.flow.first
import java.lang.reflect.Type

object SystemSettingsStore {
    private val systemSettings = DataStore(
        application = ServiceApplication.application,
        default = SystemSettings.default,
        fileName = "SystemSettings.json",
        typeToken = object : TypeToken<SystemSettings>() {},
        typePairs = arrayOf(Pair(ThemeMode::class.java, ThemeModeDeserializer()),
//            Pair(String::class.java, StringTypeAdapter())
        )
    )

    suspend fun getSystemSettings(): SystemSettings {
        return systemSettings.data.first()
    }

    fun getSystemSettingsFlow() = systemSettings.data

    /**
     * 更新系统设置
     */
    fun updateSystemSettings(block: suspend (SystemSettings) -> SystemSettings) {
        systemSettings.update(block)
    }

    fun updateSystemSettings(value: SystemSettings) {
        systemSettings.update(value)
    }
}

class StringTypeAdapter : JsonSerializer<String>, JsonDeserializer<String> {
    override fun serialize(
        src: String,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement {
        return JsonPrimitive(src)
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): String? {
        dLog { "deserialize: $json" }
        if (json != null && !json.isJsonNull) {
            try {
                if (json.isJsonArray) {
                    return json.getAsJsonArray().toString()
                } else if (json.isJsonObject) {
                    return json.getAsJsonObject().toString()
                } else {
                    val result = json.asString
                    return if ("null".equals(result, ignoreCase = true)) "" else json.getAsString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        } else {
            return ""
        }
    }
}