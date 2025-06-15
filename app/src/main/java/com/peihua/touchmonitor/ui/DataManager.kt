package com.peihua.touchmonitor.ui

import com.peihua.touchmonitor.ServiceApplication
import kotlinx.coroutines.flow.Flow

object DataManager {
    private var mSettingsStore: SettingsStore? = null
    private val settingsStore: SettingsStore
        get() {
            if (mSettingsStore == null) {
                synchronized(SettingsStore::class.java) {
                    if (mSettingsStore == null) {
                        mSettingsStore = SettingsStore(ServiceApplication.application.filesDir.absolutePath)
                    }
                }
            }
            return mSettingsStore!!
        }

    fun saveSettings(settings: Settings) {
        settingsStore.updateSettings(settings)
    }

    fun querySettings(block: (Settings) -> Unit) {
        settingsStore.getData(block)
    }

    fun querySettings(): Flow<Settings> {
        return settingsStore.data
    }

    suspend fun getSettings(): List<Settings> = settingsStore.getResult()
}