package com.peihua.touchmonitor.ui.screen.function.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopSearchBar
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.ui.components.LoadMoreView
import com.peihua.touchmonitor.ui.components.MultiStatePagingScreen
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.search.SearchBarDefaults
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.DefaultTextStyle
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.ui.theme.labelMediumNormal
import com.peihua.touchmonitor.ui.theme.labelSmallNormal
import com.peihua.touchmonitor.utils.LaunchedLoadMore
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.SearchUiAction
import com.peihua.touchmonitor.viewmodel.SearchViewModel

@Composable
fun SearchScreen(modifier: Modifier, viewModel: SearchViewModel = viewModel()) {
    val result = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val action = viewModel.userAction
    val uiState = viewModel.mUiState
    SearchScreenContent(modifier, uiState.collectAsState().value.query, result) {
        action(SearchUiAction.Search(it))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(modifier: Modifier, keywords: String = "", result: LazyPagingItems<MediaData>, search: (String) -> Unit) {
    //请求数据
    val input = remember { mutableStateOf(keywords) }
    val searchState = rememberSearchBarState()
    Toolbar(
        modifier = Modifier.fillMaxSize(),
        title = {
            TopSearchBar(
                state = searchState,
                modifier = Modifier
//                    .padding(top = dimensionResource(R.dimen.dp_8), bottom = dimensionResource(R.dimen.dp_8))
//                    .height(dimensionResource(id = R.dimen.dp_40))
                    .fillMaxWidth(),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = input.value, modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.labelLargeNormal,
                        onQueryChange = {
                            input.value = it
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dp_24))
                                    .clickable {
                                        search(input.value)
                                    },
                                painter = painterResource(id = R.drawable.ic_search_24),
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            Icon(
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dp_24))
                                    .clickable {
                                        input.value = ""
                                    },
                                painter = painterResource(id = R.drawable.ic_clear_24),
                                contentDescription = ""
                            )
                        },
                        onSearch = {
                            input.value = it
                            search(it)
                        }, onExpandedChange = {

                        }, expanded = true
                    )
                })
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
    result: LazyPagingItems<MediaData>,
) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = state,
        contentPadding = PaddingValues(dp8),
    ) {
        items(result) { item ->
            val audio = item ?: return@items
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
        item { result.LoadMoreView() }
    }
    state.LaunchedLoadMore(result)
}