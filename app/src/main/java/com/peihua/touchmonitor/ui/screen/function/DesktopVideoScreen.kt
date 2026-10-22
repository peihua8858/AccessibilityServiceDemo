package com.peihua.touchmonitor.ui.screen.function

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.data.VideoWallpaperConfig
import com.peihua.touchmonitor.data.VideoWallpaperStore
import com.peihua.touchmonitor.service.VideoWallpaperService
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DesktopVideoScreen(modifier: Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val config by VideoWallpaperStore.configFlow.collectAsState(initial = VideoWallpaperConfig())
    val isActive = remember { mutableStateOf(false) }
    val fileName = remember { mutableStateOf<String?>(null) }

    val pickVideo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (error: SecurityException) {
                    dLog { "takePersistableUriPermission failed: $error" }
                }
                VideoWallpaperStore.update(config.copy(videoUri = uri.toString()))
            }
        }

    LaunchedEffect(Unit) {
        val wallpaperInfo = WallpaperManager.getInstance(context).wallpaperInfo
        isActive.value =
            wallpaperInfo?.component?.className == VideoWallpaperService::class.java.name
    }

    LaunchedEffect(config.videoUri) {
        fileName.value = if (config.videoUri.isEmpty()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                queryVideoDisplayName(context, config.videoUri.toUri())
            }
        }
    }

    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_desktop_video_wallpaper)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = fileName.value
                        ?: stringResource(R.string.text_no_video_selected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (fileName.value == null) colorScheme.onSurfaceVariant
                    else colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
                OutlinedButton(onClick = {
                    pickVideo.launch(arrayOf("video/*"))
                }) {
                    Text(text = stringResource(R.string.text_select_video))
                }
            }
            if (config.videoUri.isNotEmpty()) {
                val previewPlayer = remember(config.videoUri) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(config.videoUri))
                        repeatMode = Player.REPEAT_MODE_ONE
                        volume = 0f
                        prepare()
                        play()
                    }
                }
                DisposableEffect(config.videoUri) {
                    onDispose { previewPlayer.release() }
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = previewPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        }
                    },
                    update = { view -> view.player = previewPlayer })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.text_wallpaper_muted),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(checked = config.muted, onCheckedChange = { muted ->
                    VideoWallpaperStore.update(config.copy(muted = muted))
                })
            }
            if (isActive.value) {
                Text(
                    text = stringResource(R.string.text_wallpaper_active),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.primary
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = config.videoUri.isNotEmpty(),
                onClick = {
                    val component = ComponentName(context, VideoWallpaperService::class.java)
                    try {
                        context.startActivity(
                            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
                        )
                    } catch (error: ActivityNotFoundException) {
                        dLog { "ACTION_CHANGE_LIVE_WALLPAPER unsupported: $error" }
                        try {
                            context.startActivity(
                                Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                            )
                        } catch (fallback: ActivityNotFoundException) {
                            dLog { "ACTION_LIVE_WALLPAPER_CHOOSER unsupported: $fallback" }
                        }
                    }
                }) {
                Text(text = stringResource(R.string.text_set_as_wallpaper))
            }
        }
    }
}

private fun queryVideoDisplayName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
