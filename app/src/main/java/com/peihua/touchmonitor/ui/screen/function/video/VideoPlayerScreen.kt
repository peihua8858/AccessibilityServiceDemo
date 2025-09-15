package com.peihua.touchmonitor.ui.screen.function.video

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.NavigationIcon2
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun VideoPlayerScreen(modifier: Modifier, videoPath: String) {
    Scaffold(
        modifier = modifier
    ) {
        AndroidView(modifier = modifier.fillMaxSize(), factory = { context ->
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val player = ExoPlayer.Builder(context).build()
            player.setMediaItem(MediaItem.fromUri(videoPath))
            PlayerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(params)
                this.player = player
//                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                player.prepare()
                player.play()
            }
        })
        Box(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            NavigationIcon2(
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.dp_16),
                        start = dimensionResource(id = R.dimen.dp_16)
                    )
                    .size(dimensionResource(id = R.dimen.dp_24))
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8))
                    )
                    .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                    .align(Alignment.TopStart),
                tintColor = Color.White
            ) {
                popBackStack()
            }
        }
    }
}