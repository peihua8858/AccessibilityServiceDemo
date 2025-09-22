package com.peihua.touchmonitor.ui.screen.function.picture

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.ActionDropMenu
import com.peihua.touchmonitor.ui.components.LoadMoreView
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.LaunchedLoadMore
import com.peihua.touchmonitor.utils.forEach
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.viewmodel.MediaModel
import com.peihua.touchmonitor.viewmodel.MediaUiAction
import com.peihua.touchmonitor.viewmodel.MediaViewModel
import com.peihua.touchmonitor.viewmodel.QueryType
import com.peihua.touchmonitor.viewmodel.SortType

@Composable
fun PictureScreen(modifier: Modifier, viewModel: MediaViewModel = viewModel()) {
    viewModel.mediaType = QueryType.QUERY_TYPE_IMAGE
    val uiAction = viewModel.userAction
    val uiState = viewModel.mUiState
    val menus = SortType.createSortList(LocalContext.current)
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isUserRefresh = remember { mutableStateOf(false) }
    MultiStatePagingScreen(modifier, R.string.text_images, result,isUserRefresh, actions = {
        ActionDropMenu(
            modifier = Modifier, models = menus, {
                it.value == uiState.value.sortType
            },
            iconRes = R.drawable.ic_sort
        ) {
            isUserRefresh.value = true
            uiAction.invoke(MediaUiAction.Sort(it.value))
            result.refresh()
        }
    }) {
        PictureScreenContent(result = it)
    }
}

@Composable
fun PictureScreenContent(modifier: Modifier = Modifier,
                         state: LazyGridState = rememberLazyGridState(),
                         result: LazyPagingItems<MediaModel>) {
    val dp16 = dimensionResource(R.dimen.dp_16)
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    val isLandscape = context.isLandscape
    val columns = if (isLandscape) 6 else 3
    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        state = state,
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(dp8),
        horizontalArrangement = Arrangement.spacedBy(dp16),
        verticalArrangement = Arrangement.spacedBy(dp16)
    ) {
        result.forEach { index, item ->
            if (item is MediaModel.Header) {
                val header = item.mediaHeader
                item(span = { GridItemSpan(columns) }) {
                    Text(
                        modifier = Modifier,
                        text = header.title
                    )
                }
            }else if (item is MediaModel.Item) {
                val mediaData = item.mediaData
                item {
                    AsyncImage(
                        modifier = Modifier
                            .clickable {
                                navigateTo2(AppRouter.PhotoPreviewScreen.route, ("photoPath" to mediaData.filePath))
                            }
                            .aspectRatio(1f)
                            .fillMaxWidth(),
                        model = mediaData.filePath,
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        item(span = { GridItemSpan(columns) }) { result.LoadMoreView() }
    }
    // 自动加载下一页逻辑
    state.LaunchedLoadMore(result)
}