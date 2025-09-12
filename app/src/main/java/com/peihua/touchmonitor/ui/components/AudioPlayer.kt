package com.peihua.touchmonitor.ui.components

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.peihua.touchmonitor.AudioPlayerController

@Composable
fun AudioPlayer(audioUrl: String, albumArtUrl: String) {
    var duration by remember { mutableIntStateOf(0) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(
        MediaPlayer().apply {
            setOnPreparedListener {
                duration = it?.duration ?: 0
                seekTo(duration)// 更新音频总时长
                it.start()
            }
            setOnCompletionListener {

            }
            setDataSource(audioUrl)
            prepare()
        }
    ) }

    // Release resources
    fun releaseAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    // UI layout
    AudioPlayerController(modifier = Modifier, mediaPlayer = mediaPlayer!!)
    // Release resources on dispose
    DisposableEffect(Unit) {
        onDispose { releaseAudio() }
    }
}

