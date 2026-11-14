package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.data.FavoriteItem
import com.peihua.touchmonitor.data.FavoriteStore
import com.peihua.touchmonitor.data.FunctionFavoriteCatalog
import com.peihua.touchmonitor.data.isFunction
import com.peihua.touchmonitor.data.openFavorite
import com.peihua.touchmonitor.ui.components.AdaptiveContent
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.AppColor
import com.peihua.touchmonitor.utils.showToast
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectScreen(modifier: Modifier = Modifier) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var deletingItem by remember { mutableStateOf<FavoriteItem?>(null) }
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val textColor = colorScheme.onSecondaryContainer
    val textBgColor = colorScheme.secondaryContainer
    val favorites by FavoriteStore.itemsFlow.collectAsState(initial = emptyList())
    val functionItems = favorites.filter { it.isFunction }
    val urlItems = favorites.filterNot { it.isFunction }

    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_collect),
        navigationIcon = {},
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_collect)
                )
            }
        }
    ) {
        AdaptiveContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (favorites.isEmpty()) {
                    EmptyView()
                } else {
                    ScaleText(
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        text = stringResource(
                            R.string.text_collect_count_hint,
                            favorites.size,
                            FavoriteStore.MAX_FAVORITE_COUNT
                        ),
                    )
                    if (functionItems.isNotEmpty()) {
                        SectionTitle(stringResource(R.string.text_collect_function_title))
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            functionItems.forEach { item ->
                                FavoriteChip(
                                    item = item,
                                    textColor = textColor,
                                    backgroundColor = textBgColor,
                                    onClick = { openFavorite(context, item) },
                                    onLongClick = { FavoriteStore.remove(item.id) },
                                )
                            }
                        }
                    }
                    if (urlItems.isNotEmpty()) {
                        SectionTitle(stringResource(R.string.text_collect_custom_title))
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            urlItems.forEach { item ->
                                FavoriteChip(
                                    item = item,
                                    textColor = textColor,
                                    backgroundColor = textBgColor,
                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            imageVector = Icons.Default.Link,
                                            tint = textColor,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = { openFavorite(context, item) },
                                    onLongClick = { deletingItem = item },
                                    trailing = {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .size(20.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(14.dp),
                                                imageVector = Icons.Default.Close,
                                                tint = textColor,
                                                contentDescription =
                                                stringResource(R.string.action_confirm_delete)
                                            )
                                        }
                                    },
                                    trailingOnClick = { deletingItem = item },
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAddDialog) {
        AddCustomFavoriteDialog(onDismiss = { showAddDialog = false })
    }
    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text(stringResource(R.string.dialog_delete_collect_title)) },
            text = { Text(stringResource(R.string.dialog_delete_collect_content)) },
            confirmButton = {
                TextButton(onClick = {
                    FavoriteStore.remove(item.id)
                    deletingItem = null
                }) {
                    Text(stringResource(R.string.action_confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text(stringResource(R.string.text_cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    ScaleText(
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        text = text,
    )
}

@Composable
private fun FavoriteChip(
    item: FavoriteItem,
    textColor: Color,
    backgroundColor: Color,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    trailingOnClick: (() -> Unit)? = null,
) {
    val displayName = remember(item) {
        if (item.isFunction) {
            FunctionFavoriteCatalog.find(item.id)?.name ?: item.name
        } else {
            item.name
        }
    }
    Row(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Icon(
                modifier = Modifier.size(14.dp),
                painter = painterResource(id = R.drawable.ic_star),
                tint = AppColor.color_ffd600,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        ScaleText(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(4.dp))
            if (trailingOnClick != null) {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = trailingOnClick,
                ) {
                    trailing()
                }
            } else {
                trailing()
            }
        }
    }
}

@Composable
private fun EmptyView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.ic_star_gray),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant),
            contentDescription = null
        )
        ScaleText(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.text_collect_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AddCustomFavoriteDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_add_collect)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.text_collect_hint_name)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text(stringResource(R.string.text_collect_hint_url)) },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedUrl = url.trim()
                if (trimmedUrl.isEmpty()) {
                    showToast(R.string.text_collect_add_hint)
                    return@TextButton
                }
                val finalUrl = if (trimmedUrl.startsWith("http://") ||
                    trimmedUrl.startsWith("https://")
                ) {
                    trimmedUrl
                } else {
                    "https://$trimmedUrl"
                }
                val finalName = name.trim().ifEmpty { trimmedUrl }
                scope.launch {
                    val success = FavoriteStore.addUrl(finalName, finalUrl)
                    showToast(if (success) R.string.tip_collect_success else R.string.tip_collect_max_limit)
                    if (success) onDismiss()
                }
            }) {
                Text(stringResource(R.string.text_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel))
            }
        }
    )
}
