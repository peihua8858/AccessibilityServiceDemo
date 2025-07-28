package com.peihua.touchmonitor.ui.screen.settings

import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.DataStore
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.ui.theme.ThemeMode
import com.peihua.touchmonitor.ui.theme.ThemeModeDeserializer
import kotlinx.coroutines.flow.first

object SystemSettingsStore {
    private val systemSettings = DataStore(
        application = ServiceApplication.application,
        default = SystemSettings.default,
        fileName = "SystemSettings.json",
        typeToken = object : TypeToken<SystemSettings>() {},
        typePairs = arrayOf(Pair(ThemeMode::class.java, ThemeModeDeserializer()))
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