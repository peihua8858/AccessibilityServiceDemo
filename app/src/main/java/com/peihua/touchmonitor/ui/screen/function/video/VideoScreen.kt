package com.peihua.touchmonitor.ui.screen.function.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.ActionDropMenu
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.components.text.AutoLineHeightScaleText
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.forEach
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.viewmodel.MediaModel
import com.peihua.touchmonitor.viewmodel.MediaUiAction
import com.peihua.touchmonitor.viewmodel.MediaViewModel
import com.peihua.touchmonitor.viewmodel.QueryType
import com.peihua.touchmonitor.viewmodel.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(modifier: Modifier, viewModel: MediaViewModel = viewModel()) {
    viewModel.mediaType = QueryType.QUERY_TYPE_VIDEO
    val uiAction = viewModel.userAction
    val uiState = viewModel.mUiState
    val menus = SortType.createSortList(LocalContext.current)
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    MultiStatePagingScreen(modifier, R.string.text_videos, result, actions = {
        ActionDropMenu(
            modifier = Modifier, models = menus, {
                it.value == uiState.value.sortType
            },
            iconRes = R.drawable.ic_sort
        ) {
            uiAction.invoke(MediaUiAction.Sort(it.value))
            result.refresh()
        }
    }) {
        VideoScreenContent(result = it)
    }
}

@Composable
fun VideoScreenContent(modifier: Modifier = Modifier, result: LazyPagingItems<MediaModel>) {
    val dp16 = dimensionResource(R.dimen.dp_16)
    val dp8 = dimensionResource(R.dimen.dp_8)
    val dp2 = dimensionResource(R.dimen.dp_2)
    val bottomMargin = dimensionResource(R.dimen.dp_4)
    val context = LocalContext.current
    val isLandscape = context.isLandscape
    val columns = if (isLandscape) 6 else 3
    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(dp8),
        horizontalArrangement = Arrangement.spacedBy(dp16),
        verticalArrangement = Arrangement.spacedBy(dp16)
    ) {
        result.forEach { index, item ->
            dLog { "index:$index,item:$item" }
            if (item is MediaModel.Header) {
                val header = item.mediaHeader
                item(span = { GridItemSpan(columns) }) {
                    AutoLineHeightScaleText(modifier = Modifier, text = header.title)
                }
            }else if(item is MediaModel.Item){
                val mediaData = item.mediaData
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                //  跳转到系统视频播放
                                navigateTo2(AppRouter.VideoPlayerScreen.route, ("videoPath" to mediaData.filePath))
                            },
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth(),
                            model = mediaData.thumbnailsBitmap,
                            contentDescription = "",
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(dp16, dp16),
                            painter = painterResource(R.drawable.ic_play_circle_24),
                            contentDescription = ""
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    shape = ShapeDefaults.Small.copy(
                                        topStart = CornerSize(dp2),
                                        topEnd = CornerSize(dp2),
                                        bottomStart = CornerSize(0),
                                        bottomEnd = CornerSize(0),
                                    )
                                )
                                .padding(horizontal = bottomMargin),
                            verticalArrangement = Arrangement.Top
                        ) {
                            AutoLineHeightScaleText(
                                modifier = Modifier.basicMarquee(),
                                text = mediaData.fileName,
                                maxLines = 1,
                                color = Color.White,
                                fontSize = dimensionSpResource(id = R.dimen.sp_6)
                            )
                            AutoLineHeightScaleText(
                                modifier = Modifier,
                                text = mediaData.fileSize ?: "",
                                maxLines = 1,
                                color = Color.White,
                                fontSize = dimensionSpResource(id = R.dimen.sp_6)
                            )
                        }
                    }

                }
            }
        }
    }
}