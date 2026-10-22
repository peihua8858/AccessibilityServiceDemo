package com.peihua.touchmonitor.data

import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.ServiceApplication
import kotlinx.coroutines.flow.first

data class VideoWallpaperConfig(
    val videoUri: String = "",
    val muted: Boolean = true,
)

object VideoWallpaperStore {
    private val store = DataStore(
        application = ServiceApplication.application,
        default = VideoWallpaperConfig(),
        fileName = "VideoWallpaper.json",
        typeToken = object : TypeToken<VideoWallpaperConfig>() {},
    )

    val configFlow = store.data

    suspend fun getConfig(): VideoWallpaperConfig = store.data.first()

    fun update(value: VideoWallpaperConfig) = store.update(value)
}
