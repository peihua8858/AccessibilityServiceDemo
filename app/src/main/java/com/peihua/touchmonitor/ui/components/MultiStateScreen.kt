package com.peihua.touchmonitor.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.ShowToast
import com.peihua.touchmonitor.utils.dLog
import kotlinx.coroutines.flow.Flow

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
    content: @Composable (T) -> Unit,
) {
    Toolbar(
        modifier = modifier,
        title = title,
        actions = actions,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        hostState = hostState,
    ) {
        Column(
            modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
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
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    content: @Composable (LazyPagingItems<T>) -> Unit
) {
    MultiStatePagingScreen(
        modifier,
        title = stringResource(titleRes),
        result = result,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        actions = actions,
        hostState = hostState,
        content = content
    )
}

@Composable
fun <T : Any> MultiStatePagingScreen(
    modifier: Modifier,
    title: String,
    result: LazyPagingItems<T>,
    navigateUp: () -> Unit = { popBackStack() },
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    },
    actions: @Composable RowScope.() -> Unit = {},
    hostState: SnackbarHostState = remember { snackbarHostState },
    content: @Composable (LazyPagingItems<T>) -> Unit
) {
    val refreshing =
        rememberPullToRefreshState(isRefreshing = result.loadState.refresh is LoadState.Loading)

    Toolbar(
        modifier = modifier,
        title = title,
        actions = actions,
        navigateUp = navigateUp,
        navigationIcon = navigationIcon,
        hostState = hostState
    ) {
        PullToRefresh(
            state = refreshing,
            onRefresh = {
                result.refresh()
            },
            modifier = modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
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
                    if (result.itemCount == 0) {
                        ErrorView(retry = result::refresh)
                    } else {
                        ShowToast("刷新失败")
                    }
                }

                else -> {
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


