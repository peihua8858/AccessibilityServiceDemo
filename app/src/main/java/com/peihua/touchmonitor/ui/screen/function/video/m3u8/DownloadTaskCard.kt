package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.viewmodel.DownloadCard

@Composable
fun DownloadTaskCard(
    card: DownloadCard,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onRetry: () -> Unit,
    onOpen: () -> Unit,
    onExport: () -> Unit,
    onDelete: (deleteFile: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = card.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { card.fraction },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(text = card.stageText) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = listOf(card.percentText, card.segmentText, card.sizeText)
                        .filter { it.isNotEmpty() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (card.rateText.isNotEmpty() || card.etaText.isNotEmpty()) {
                Text(
                    text = listOf(card.rateText, card.etaText)
                        .filter { it.isNotEmpty() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            card.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    // 手动暂停/服务超时也会写 errorMessage，但它们是"可继续"而不是"失败"，
                    // 一律涂红会误导用户
                    color = if (card.isFailed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    // 三方库的异常信息可能很长，不截断会把卡片撑爆
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (card.canPause) {
                    IconButton(onClick = onPause) {
                        Icon(imageVector = Icons.Filled.Pause, contentDescription = "暂停")
                    }
                }
                if (card.canResume) {
                    IconButton(onClick = onResume) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "继续")
                    }
                }
                if (card.canRetry) {
                    IconButton(onClick = onRetry) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "重试")
                    }
                }
                if (card.canOpen) {
                    TextButton(onClick = onOpen) { Text(text = "打开") }
                    IconButton(onClick = onExport) {
                        Icon(imageVector = Icons.Filled.SaveAlt, contentDescription = "保存到相册")
                    }
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "删除")
                }
            }
        }
    }

    if (confirmDelete) {
        DeleteConfirmDialog(
            hasFile = card.canOpen,
            onDismiss = { confirmDelete = false },
            onConfirm = { deleteFile ->
                confirmDelete = false
                onDelete(deleteFile)
            },
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    hasFile: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit,
) {
    var deleteFile by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "删除任务") },
        text = {
            Column {
                Text(text = "确认删除这个下载任务？未下载完的临时分片会一起清理。")
                if (hasFile) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = deleteFile, onCheckedChange = { deleteFile = it })
                        Text(text = "同时删除已下载的视频文件")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(deleteFile) }) { Text(text = "删除") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = "取消") } },
    )
}
