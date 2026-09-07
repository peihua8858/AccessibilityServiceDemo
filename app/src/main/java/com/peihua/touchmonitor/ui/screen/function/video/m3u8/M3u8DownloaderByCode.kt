package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.utils.getUriForFileByFileProvider
import com.peihua.touchmonitor.utils.isAtLeastT
import com.peihua.touchmonitor.utils.isGrantedPermission
import com.peihua.touchmonitor.utils.showToast
import com.peihua.touchmonitor.viewmodel.M3u8DownloadViewModel
import com.peihua8858.compose.tools.rememberState
import java.io.File

@Composable
fun M3u8DownloaderByCode(
    modifier: Modifier = Modifier,
    viewModel: M3u8DownloadViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val m3u8Url = rememberState("")
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) showToast("未授予通知权限，下载会继续但看不到进度通知")
    }

    Column(modifier = modifier) {
        UrlInputBar(
            url = m3u8Url.value,
            onUrlChange = { m3u8Url.value = it },
            onPaste = { m3u8Url.value = context.readClipboardText() ?: m3u8Url.value },
            onDownload = {
                // 未授权时服务仍能跑，只是通知不可见，用户会以为"没反应"。
                // 权限结果异步返回，授权后下一次 1s 刷新就会把通知补上，所以不阻塞下载。
                if (isAtLeastT && !context.isGrantedPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.enqueue(m3u8Url.value)
                m3u8Url.value = ""
            },
        )
        HorizontalDivider()

        if (state.cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "还没有下载任务，粘贴 m3u8 地址开始",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }

        if (state.hasActive) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "总速率 ${state.totalRateText}", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = viewModel::pauseAll) { Text(text = "全部暂停") }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // key 必须有：每秒都有进度更新，没有 key 会让 item 复用错乱
            items(state.cards, key = { it.id }) { card ->
                DownloadTaskCard(
                    card = card,
                    onResume = { viewModel.resume(card.id) },
                    onPause = { viewModel.pause(card.id) },
                    onRetry = { viewModel.retry(card.id) },
                    onOpen = { context.openVideo(card.filePath) },
                    onExport = { viewModel.exportToGallery(card.id) },
                    onDelete = { deleteFile -> viewModel.delete(card.id, deleteFile) },
                )
            }
        }
    }
}

@Composable
private fun UrlInputBar(
    url: String,
    onUrlChange: (String) -> Unit,
    onPaste: () -> Unit,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text(text = "m3u8 地址") },
            trailingIcon = { TextButton(onClick = onPaste) { Text(text = "粘贴") } },
        )
        Button(
            modifier = Modifier.padding(start = 8.dp),
            enabled = url.isNotBlank(),
            onClick = onDownload,
        ) {
            Text(text = "下载")
        }
    }
}

private fun Context.readClipboardText(): String? =
    getSystemService<ClipboardManager>()
        ?.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(this)
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

private fun Context.openVideo(filePath: String) {
    val file = File(filePath)
    if (!file.isFile) {
        showToast("文件不存在")
        return
    }
    val uri = getUriForFileByFileProvider(file) ?: return
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, "video/mp4")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
        .onFailure { showToast("没有可用的播放器") }
}
