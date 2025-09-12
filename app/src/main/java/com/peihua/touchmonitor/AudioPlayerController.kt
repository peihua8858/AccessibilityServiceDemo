package com.peihua.touchmonitor

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Track
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Formatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerController(modifier: Modifier,mediaPlayer: MediaPlayer) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) } // 当前播放位置
    var duration by remember { mutableIntStateOf(0) } // 音频的总时长
    var progress by remember { mutableFloatStateOf(0f) }
    var bufferProgress by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableStateOf("00:00") }
    var totalTime by remember { mutableStateOf("00:00") }
    val mFormatBuilder = StringBuilder()
    val mFormatter = Formatter(mFormatBuilder, java.util.Locale.getDefault())
    fun stringForTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
    // LaunchedEffect 用于在播放状态变化时更新当前播放位置
    LaunchedEffect(isPlaying) {
        duration = mediaPlayer.duration // 获取音频总时长
        while (isPlaying) {
            currentPosition = mediaPlayer.currentPosition
            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
            currentTime = stringForTime(currentPosition)
            totalTime = stringForTime(duration)
            delay(500) // 每 500 毫秒更新一次当前播放位置
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        val size =DpSize(24.dp, 24.dp)
        // 播放时间
        // 进度条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentTime)
            Slider(
                value = progress,
                onValueChange = { value ->
                    currentPosition = (value * duration).toInt()
                    mediaPlayer.seekTo(currentPosition) // 设置新的播放进度
                    ""
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = colors,
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(size.width)
                            )
                    )
                },
                track = { sliderState ->
                    Track(
                        colors = colors,
                        sliderState = sliderState,
                        thumbTrackGapSize = 0.dp,
                        trackInsideCornerSize = 0.dp
                    )
                }
            )
            Text(totalTime)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = {

            }) {
                Image(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track"
                ) // 替换为实际图标 URL
            }
            IconButton(onClick = {
                if (isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                }
                isPlaying = !isPlaying
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            IconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next Track") // 替换为实际图标 URL
            }
        }
    }
}

