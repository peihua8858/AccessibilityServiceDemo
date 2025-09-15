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
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
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