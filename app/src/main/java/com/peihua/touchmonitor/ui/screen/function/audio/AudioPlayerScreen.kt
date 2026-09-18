package com.peihua.touchmonitor.ui.screen.function.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.AudioPlayer
import com.peihua.touchmonitor.ui.components.NavigationIcon2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.LocalToolColors

@Composable
fun AudioPlayerScreen(modifier: Modifier, audioPath: String) {
    val toolColors = LocalToolColors.current
    Box(
        modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
//        AndroidView(modifier = modifier.fillMaxSize(), factory = {
//            VideoView(it).apply {
//                setVideoPath(audioPath)
//                setMediaController(MediaController(it))
//                start()
//            }
//        })
        AudioPlayer(audioPath,"")
        NavigationIcon2(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .size(24.dp)
                .background(toolColors.mediaScrim, shape = RoundedCornerShape(8.dp))
                .clip(shape = RoundedCornerShape(8.dp))
                .align(Alignment.TopStart),
            tintColor = Color.White
        ) {
            popBackStack()
        }
    }
}