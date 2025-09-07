package com.peihua.touchmonitor.ui.screen.function.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.PhotoHeader
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.function.picture.PictureScreenContent
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.utils.screenWidthDp
import com.peihua.touchmonitor.viewmodel.VideoViewModel

@Composable
fun VideoScreen(modifier: Modifier, viewModel: VideoViewModel = viewModel()){
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_videos),
        navigateUp = {
            popBackStack()
        }) {

    }
    val result = viewModel.pictureState.value
    val sortType = 1
    //请求数据
    val refresh = {
        viewModel.requestImages(sortType)
    }
    MultiStateScreen(modifier, R.string.text_videos, result, refresh) {
        VideoScreenContent(result = it)
    }
}

@Composable
fun VideoScreenContent(modifier: Modifier = Modifier, result: MutableList<PhotoHeader>) {
    val dp16 = dimensionResource(R.dimen.dp_16)
    val dp8 = dimensionResource(R.dimen.dp_8)
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
        for (item in result) {
            item(span = { GridItemSpan(columns) }) {
                Text(
                    modifier = Modifier,
                    text = item.title
                )
            }
            itemsIndexed(item.photoList) { index, photo ->
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(),
                    model = photo.filePath, contentDescription = ""
                )
            }
        }
    }
}