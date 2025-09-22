package com.peihua.touchmonitor.ui.screen.function.zip

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.peihua.touchmonitor.ui.components.ActionDropMenu
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.forEach
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.MediaModel
import com.peihua.touchmonitor.viewmodel.MediaUiAction
import com.peihua.touchmonitor.viewmodel.SortType
import com.peihua.touchmonitor.viewmodel.ZipViewModel

@Composable
fun ZipScreen(modifier: Modifier, viewModel: ZipViewModel = viewModel()) {
    val uiAction = viewModel.userAction
    val uiState = viewModel.mUiState
    val menus = SortType.createSortList(LocalContext.current)
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    MultiStatePagingScreen(modifier, R.string.text_compression, result, actions = {
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
        ZipScreenContent(result = it)
    }
}

@Composable
fun ZipScreenContent(modifier: Modifier = Modifier, result: LazyPagingItems<MediaModel>) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
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
                        .clickable {
                            context.openWithFile(mediaData.filePath)
//                            navigateTo2(AppRouter.AudioPlayerScreen.route, ("audioPath" to photo.filePath))
                        }
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
    }
}