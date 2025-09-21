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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.components.ActionDropMenu
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.text.AutoLineHeightScaleText
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.forEach
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.SortType
import com.peihua.touchmonitor.viewmodel.UiAction
import com.peihua.touchmonitor.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(modifier: Modifier, viewModel: VideoViewModel = viewModel()) {
    val uiAction = viewModel.userAction
    val uiState = viewModel.mUiState
    val menus = SortType.createSortList(LocalContext.current)
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    LaunchedEffect(result.loadState) {
        result .forEach {
            dLog { "VideoScreen>>>>item:$it" }
        }
    }

    MultiStatePagingScreen(modifier, R.string.text_videos, result, actions = {
        ActionDropMenu(
            modifier = Modifier, models = menus, {
                it.value == uiState.value.sortType
            },
            iconRes = R.drawable.ic_sort
        ) {
            uiAction.invoke(UiAction.Sort(it.value))
        }
    }) {
        VideoScreenContent(result = it)
    }
}

@Composable
fun VideoScreenContent(modifier: Modifier = Modifier, result: LazyPagingItems<MediaHeader>) {
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
            item(span = { GridItemSpan(columns) }) {
                AutoLineHeightScaleText(modifier = Modifier, text = item.title)
            }
            for((index,photo) in item.mediaList.withIndex()) {
                dLog { "mediaList>>>>index:$index,item:${photo.fileName},${photo.fileSize}" }
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                //  跳转到系统视频播放
                                navigateTo2(AppRouter.VideoPlayerScreen.route, ("videoPath" to photo.filePath))
                            },
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth(),
                            model = photo.thumbnailsBitmap,
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
                                text = photo.fileName,
                                maxLines = 1,
                                color = Color.White,
                                fontSize = dimensionSpResource(id = R.dimen.sp_6)
                            )
                            AutoLineHeightScaleText(
                                modifier = Modifier,
                                text = photo.fileSize ?: "",
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