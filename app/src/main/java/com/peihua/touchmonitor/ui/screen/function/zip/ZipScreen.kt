package com.peihua.touchmonitor.ui.screen.function.zip

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.ActionDropMenu
import com.peihua.touchmonitor.ui.components.LoadMoreView
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.LaunchedLoadMore
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.MediaModel
import com.peihua.touchmonitor.viewmodel.MediaUiAction
import com.peihua.touchmonitor.viewmodel.SortType
import com.peihua.touchmonitor.viewmodel.ZipViewModel
import com.peihua8858.tools.utils.openWithFile

@Composable
fun ZipScreen(modifier: Modifier, viewModel: ZipViewModel = viewModel()) {
    val uiAction = viewModel.userAction
    val uiState = viewModel.mUiState
    val menus = SortType.createSortList(LocalContext.current)
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isUserRefresh = remember { mutableStateOf(false) }
    MultiStatePagingScreen(modifier, R.string.text_compression, result, isUserRefresh, actions = {
        ActionDropMenu(
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.dp_16)), models = menus, selected ={
                it.value == uiState.value.sortType
            },
            iconRes = R.drawable.ic_sort
        ) {
            isUserRefresh.value = true
            uiAction.invoke(MediaUiAction.Sort(it.value))
            result.refresh()
        }
    }) {
        ZipScreenContent(result = it)
    }
}

@Composable
fun ZipScreenContent(
    modifier: Modifier = Modifier, state: LazyListState = rememberLazyListState(),
    result: LazyPagingItems<MediaModel>,
) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = state,
        contentPadding = PaddingValues(dp8),
    ) {
        items(result) { item ->
            if (item is MediaModel.Header) {
                val header = item.mediaHeader
                Text(
                    modifier = Modifier,
                    text = header.title
                )
            } else if (item is MediaModel.Item) {
                val mediaData = item.mediaData
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onClick = {
                            context.openWithFile(mediaData.filePath)
                        }, onLongClick = {
                            if (mediaData.isFile) {
                                navigateTo2(Dialog.ShareDialog.route, ("filePath" to mediaData.filePath))
                            }
                        })
                        .padding(vertical = dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_32)),
                        painter = painterResource(R.mipmap.ic_zip_file),
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier,
                            text = mediaData.fileName
                        )
                        Text(
                            modifier = Modifier,
                            text = mediaData.fileSize ?: ""
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }

        }
        item { result.LoadMoreView() }
    }
    state.LaunchedLoadMore(result)
}