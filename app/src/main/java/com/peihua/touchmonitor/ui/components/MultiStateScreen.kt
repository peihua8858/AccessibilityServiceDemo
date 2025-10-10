package com.peihua.touchmonitor.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.ShowToast
import com.peihua.touchmonitor.utils.dLog

@Composable
fun <T> MultiStateScreen(
    modifier: Modifier,
    @StringRes titleRes: Int,
    result: ResultData<T>,
    refresh: () -> Unit,
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    header: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    MultiStateScreen(
        modifier,
        title = stringResource(titleRes),
        result = result,
        refresh = refresh,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        actions = actions,
        hostState = hostState,
        header = header,
        content = content
    )
}

@Composable
fun <T> MultiStateScreen(
    modifier: Modifier,
    title: String,
    result: ResultData<T>,
    refresh: () -> Unit,
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    header: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    Toolbar(
        modifier = Modifier.fillMaxSize(),
        title = title,
        actions = actions,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        hostState = hostState,
    ) {
        MultiStateScreen(modifier = modifier, result = result, refresh = refresh, header = header, content = content)
    }
}

@Composable
fun <T> MultiStateScreen(
    modifier: Modifier,
    result: ResultData<T>,
    refresh: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    val dp16 = dimensionResource(R.dimen.dp_16)
    Column {
        if (header != null) {
            header()
        }
        Column(
            modifier
                .fillMaxSize()
                .padding(
                    start = dp16,
                    top = if (header == null) dp16 else 0.dp,
                    end = dp16, bottom = dp16
                )
        ) {
            when (result) {
                is ResultData.Success -> {
                    val data = result.data
                    dLog { ">>>>>data:${data}" }
                    if (data is List<*> && data.isEmpty()) {
                        dLog { ">>>>>data:${data.size}" }
                        EmptyView(modifier, retry = refresh)
                    } else {
                        content(data)
                    }
                }

                is ResultData.Failure -> {
                    ErrorView(retry = refresh)
                }

                is ResultData.Initialize -> {
                    refresh()
                }

                is ResultData.Starting -> {
                    LoadingViewFillMaxSize()
                }
            }
        }
    }
}

@Composable
fun <T : Any> MultiStatePagingScreen(
    modifier: Modifier,
    @StringRes titleRes: Int,
    result: LazyPagingItems<T>,
    isUserRefresh: MutableState<Boolean> = remember { mutableStateOf(false) },
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    header: (@Composable () -> Unit)? = null,
    content: @Composable (LazyPagingItems<T>) -> Unit,
) {
    MultiStatePagingScreen(
        modifier,
        title = stringResource(titleRes),
        result = result,
        isUserRefresh = isUserRefresh,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        actions = actions,
        hostState = hostState,
        header = header,
        content = content
    )
}

@Composable
fun <T : Any> MultiStatePagingScreen(
    modifier: Modifier,
    title: String,
    result: LazyPagingItems<T>,
    isUserRefresh: MutableState<Boolean> = remember { mutableStateOf(false) },
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    header: (@Composable () -> Unit)? = null,
    content: @Composable (LazyPagingItems<T>) -> Unit,
) {
    Toolbar(
        modifier = Modifier.fillMaxSize(),
        title = title,
        actions = actions,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        hostState = hostState
    ) {
        MultiStatePagingScreen(modifier = modifier, result = result, isUserRefresh = isUserRefresh, header = header, content = content)
    }
}

@Composable
fun <T : Any> MultiStatePagingScreen(
    modifier: Modifier,
    result: LazyPagingItems<T>,
    isUserRefresh: MutableState<Boolean> = remember { mutableStateOf(false) },
    header: (@Composable () -> Unit)? = null,
    content: @Composable (LazyPagingItems<T>) -> Unit,
) {
    val isRefreshing = result.loadState.refresh is LoadState.Loading
    val refreshing =
        rememberPullToRefreshState(isRefreshing = isRefreshing && isUserRefresh.value)
    val dp16 = dimensionResource(R.dimen.dp_16)
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (header != null) {
            header()
        }
        PullToRefresh(
            state = refreshing,
            onRefresh = {
                isUserRefresh.value = true
                result.refresh()
            },
            modifier = modifier
                .fillMaxSize()
                .padding(
                    start = dp16,
                    top = if (header == null) dp16 else 0.dp,
                    end = dp16, bottom = dp16
                )
        ) {
            val loadState: CombinedLoadStates = result.loadState
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    if (result.itemCount == 0) {
                        LoadingViewFillMaxSize()
                    }
                    content(result)
                    return@PullToRefresh
                }

                is LoadState.Error -> {
                    isUserRefresh.value = false
                    if (result.itemCount == 0) {
                        ErrorView(retry = result::retry)
                    } else {
                        ShowToast(R.string.text_request_fail)
                    }
                }

                else -> {
                    isUserRefresh.value = false
                }
            }
            dLog { ">>>>>result:${result}" }
            if (result.itemCount == 0) {
                dLog { ">>>>>result:${result.itemCount}" }
                EmptyView(modifier, retry = result::refresh)
            } else {
                content(result)
            }
        }
    }
}

@Composable
fun <T : Any> LazyPagingItems<T>.LoadMoreView(modifier: Modifier = Modifier) {
    if (this.itemCount == 0 || this.loadState.refresh is LoadState.Loading) {
        return
    }
    val appendState = this.loadState.append
    if (appendState is LoadState.Error) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_16)),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.text_load_fail) + ":${appendState.error.localizedMessage}",
                modifier = Modifier
            )
            Text(
                stringResource(R.string.text_retry),
                color = colorResource(id = R.color.light_blue_600),
                modifier = Modifier
                    .clickable {
                        retry()
                    }
            )
        }
    } else if (appendState.endOfPaginationReached.not()) {
        Box(modifier = modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(dimensionResource(R.dimen.dp_16))
            )
        }
    }
}


