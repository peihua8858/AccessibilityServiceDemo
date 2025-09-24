package com.peihua.touchmonitor.ui.screen.function.document

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
import com.fz.common.array.isNonEmpty
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.LoadMoreView
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.utils.LaunchedLoadMore
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.DocumentViewModel
import com.peihua.touchmonitor.viewmodel.MediaModel
import com.peihua.touchmonitor.viewmodel.QueryType

@Composable
fun AllDocumentScreen(
    modifier: Modifier,
    types: Array<String> = arrayOf(),
    viewModel: DocumentViewModel = viewModel()
) {
    if (types.isNonEmpty()) {
        viewModel.types = types
    }
    viewModel.mediaType = QueryType.QUERY_TYPE_DOCUMENT
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isUserRefresh = remember { mutableStateOf(false) }
    MultiStatePagingScreen(modifier, result, isUserRefresh) {
        AllDocumentScreenContent(result = it)
    }
}

@Composable
fun AllDocumentScreenContent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
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
                val audio = item.mediaData
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.openWithFile(audio.filePath)
//                            navigateTo2(AppRouter.AudioPlayerScreen.route, ("audioPath" to photo.filePath))
                        }
                        .padding(vertical = dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_32)),
                        painter = painterResource(R.drawable.ic_audio_file_24),
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier,
                            text = audio.fileName
                        )
                        Text(
                            modifier = Modifier,
                            text = audio.fileSize ?: ""
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
