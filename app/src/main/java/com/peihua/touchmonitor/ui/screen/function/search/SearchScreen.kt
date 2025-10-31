package com.peihua.touchmonitor.ui.screen.function.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.compose.utils.openWithFile
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.SearchModel
import com.peihua.touchmonitor.ui.components.LoadMoreView
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.components.search.TopSearchBar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.utils.LaunchedLoadMore
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.SearchUiAction
import com.peihua.touchmonitor.viewmodel.SearchViewModel

enum class SearchType {
    ALL,
    IMAGE,
    AUDIO,
    VIDEO,
    DOCUMENT,
    APK,
    APPLICATION
}

@Composable
fun SearchScreen(
    modifier: Modifier,
    searchType: SearchType = SearchType.ALL,
    viewModel: SearchViewModel = viewModel(),
) {
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val action = viewModel.userAction
    val uiState = viewModel.mUiState
    SearchScreenContent(modifier, uiState.collectAsState().value.query, result) {
        action(SearchUiAction.Search(it, searchType))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    modifier: Modifier,
    keywords: String = "",
    result: LazyPagingItems<SearchModel>,
    search: (String) -> Unit,
) {
    //请求数据
    val input = remember { mutableStateOf(keywords) }
    val searchState = rememberSearchBarState()
    Toolbar(
        modifier = Modifier.fillMaxSize(),
        title = {
            TopSearchBar(
                state = searchState, modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.labelLargeNormal,
                onQueryChange = {
                    input.value = it
                    search(it)
                },
                onSearch = {
                    input.value = it
                    search(it)
                },
            )

        },
        navigateUp = { popBackStack() },
    ) {
        MultiStatePagingScreen(modifier, result) {
            SearchScreenContent(modifier, result = it)
        }
    }
}

@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    result: LazyPagingItems<SearchModel>,
) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = state,
        contentPadding = PaddingValues(dp8),
    ) {
        items(result) { item ->
            val model = item ?: return@items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.openWithFile(model.filePath)
//                            navigateTo2(AppRouter.AudioPlayerScreen.route, ("audioPath" to photo.filePath))
                    }
                    .padding(vertical = dp8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .padding(end = dp8)
                        .size(dimensionResource(id = R.dimen.dp_32)),
                    painter = if (model.icon == null) painterResource(R.drawable.ic_audio_file_24)
                    else rememberDrawablePainter(model.icon),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        modifier = Modifier,
                        text = model.displayName
                    )
                    Text(
                        modifier = Modifier,
                        text = model.fileSize
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
        item { result.LoadMoreView() }
    }
    state.LaunchedLoadMore(result)
}