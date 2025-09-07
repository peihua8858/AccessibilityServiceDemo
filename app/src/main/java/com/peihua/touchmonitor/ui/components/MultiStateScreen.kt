package com.peihua.touchmonitor.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData

@Composable
fun <T> MultiStateScreen(
    modifier: Modifier,
    @StringRes titleRes: Int,
    result: ResultData<T>,
    refresh: () -> Unit,
    content: @Composable (T) -> Unit
) {
    MultiStateScreen(modifier, stringResource(titleRes), result, refresh, content)
}

@Composable
fun <T> MultiStateScreen(
    modifier: Modifier,
    title: String,
    result: ResultData<T>,
    refresh: () -> Unit,
    content: @Composable (T) -> Unit
) {
    Toolbar(
        modifier = modifier,
        title = title,
        navigateUp = {
            popBackStack()
        }) {
        Column(
            modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
        ) {
            when (result) {
                is ResultData.Success -> {
                    val data = result.data
                    if (data is List<*> && data.isEmpty()) {
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