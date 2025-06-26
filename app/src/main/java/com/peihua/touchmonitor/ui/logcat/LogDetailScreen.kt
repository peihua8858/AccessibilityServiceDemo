package com.peihua.touchmonitor.ui.logcat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.viewmodel.LogViewModel

@Composable
fun LogDetailScreen(
    modifier: Modifier = Modifier,
    filePath: String,
    viewModel: LogViewModel = viewModel(),
) {
    val result = viewModel.logDetailState.value
    //请求数据
    val refresh = {
        viewModel.requestData(filePath)
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        AppTopBar(title = { stringResource(R.string.text_log_detail) }, navigateUp = {
            popBackStack()
        })
        when (result) {
            is ResultData.Success -> {
                ScaleText(
                    text = result.data, modifier = modifier
                        .verticalScroll(rememberScrollState())
                )
            }

            is ResultData.Failure -> {
                ErrorView { refresh() }
            }

            is ResultData.Initialize -> {
                refresh()
            }

            is ResultData.Starting -> {
                LoadingView()
            }
        }
    }

}