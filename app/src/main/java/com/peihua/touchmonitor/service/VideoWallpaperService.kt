package com.peihua.touchmonitor.service

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.peihua.touchmonitor.data.VideoWallpaperConfig
import com.peihua.touchmonitor.data.VideoWallpaperStore
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VideoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VideoWallpaperEngine()

    private inner class VideoWallpaperEngine : Engine() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private var player: ExoPlayer? = null
        private var config: VideoWallpaperConfig? = null
        private var surfaceReady = false
        private var visible = false

        init {
            scope.launch {
                VideoWallpaperStore.configFlow.collect { next ->
                    val previous = config
                    config = next
                    applyConfig(previous)
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            player?.playWhenReady = visible
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            surfaceReady = true
            setupPlayerIfNeeded()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int,
        ) {
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            surfaceReady = false
            // 回调返回后不得再访问 surface：先摘除再释放
            player?.clearVideoSurfaceHolder(holder)
            releasePlayer()
        }

        override fun onDestroy() {
            releasePlayer()
            scope.cancel()
        }

        private fun applyConfig(previous: VideoWallpaperConfig?) {
            val current = config ?: return
            if (current.videoUri.isEmpty()) {
                releasePlayer()
                return
            }
            val volume = if (current.muted) 0f else 1f
            when {
                player == null -> setupPlayerIfNeeded()
                previous?.videoUri != current.videoUri -> {
                    releasePlayer()
                    setupPlayerIfNeeded()
                }

                else -> player?.volume = volume
            }
        }

        private fun setupPlayerIfNeeded() {
            val current = config ?: return
            if (!surfaceReady || current.videoUri.isEmpty() || player != null) return
            val uri = current.videoUri.toUri()
            player = ExoPlayer.Builder(applicationContext).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = if (current.muted) 0f else 1f
                setVideoSurfaceHolder(surfaceHolder)
                prepare()
                playWhenReady = visible
            }
            dLog { "video wallpaper playback started: $uri" }
        }

        private fun releasePlayer() {
            player?.release()
            player = null
        }
    }
}
