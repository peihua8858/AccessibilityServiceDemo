package com.peihua.touchmonitor.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.peihua.touchmonitor.R
import com.peihua8858.tools.utils.dLog
import java.lang.reflect.Type


enum class ThemeMode(val index: Int, val image:ImageVector, @StringRes val nameIds: Int) {
    /**
     *  根据用户选择的内容使用浅色或深色主题系统设置。
     */
    System(0, Icons.Default.Brightness4, R.string.theme_system),

    /**
     * 始终使用 Light 模式，而不管系统首选项如何。
     */
    Light(1, Icons.Default.LightMode,R.string.theme_light),

    /**
     * 始终使用深色模式（如果可用），而不管系统首选项如何。
     */
    Dark(2, Icons.Default.DarkMode,R.string.theme_dark),
}

class ThemeModeDeserializer : JsonDeserializer<ThemeMode>, JsonSerializer<ThemeMode> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ThemeMode? {
        json ?: return null
        dLog { "ThemeModeDeserializer>>>deserialize>>>>json:$json" }
        return ThemeMode.entries.find { it.index == json.asInt }.dLog { "ThemeModeDeserializer>>>deserialize>>>>ThemeMode:$this" }
    }

    override fun serialize(
        src: ThemeMode?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement? {
       src ?: return null
        dLog { "ThemeModeDeserializer>>>serialize>>>>src:$src" }
        return JsonPrimitive(src.index)
    }
}