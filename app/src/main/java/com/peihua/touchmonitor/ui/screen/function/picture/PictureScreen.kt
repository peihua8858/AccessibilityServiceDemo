package com.peihua.touchmonitor.ui.screen.function.picture

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Scale
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.viewmodel.PictureViewModel

@Composable
fun PictureScreen(modifier: Modifier, viewModel: PictureViewModel = viewModel()) {
    val result = viewModel.pictureState.value
    val sortType = 1
    //请求数据
    val refresh = {
        viewModel.requestImages(sortType)
    }
    MultiStateScreen(modifier, R.string.text_images, result, refresh) {
        PictureScreenContent(result = it)
    }
}

@Composable
fun PictureScreenContent(modifier: Modifier = Modifier, result: MutableList<MediaHeader>) {
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
            itemsIndexed(item.mediaList) { index, photo ->
                AsyncImage(
                    modifier = Modifier
                        .clickable {
                            navigateTo2(AppRouter.PhotoPreviewScreen.route, ("photoPath" to (photo.filePath ?: "")))
                        }
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                    model = photo.filePath,
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}