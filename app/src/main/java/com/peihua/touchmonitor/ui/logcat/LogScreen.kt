package com.peihua.touchmonitor.ui.logcat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.LogModel
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.viewmodel.LogViewModel

@Composable
fun LogScreen(modifier: Modifier = Modifier, viewModel: LogViewModel = viewModel()) {
    val result = viewModel.logState.value
    //请求数据
    val refresh = {
        viewModel.requestData()
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        AppTopBar(title = { stringResource(R.string.text_log) }, navigateUp = {
            popBackStack()
        })
        when (result) {
            is ResultData.Success -> {
                if (result.data.isEmpty()) {
                    return
                }
                LogScreenContent(Modifier, result.data)
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


@Composable
private fun LogScreenContent(modifier: Modifier, models: List<LogModel>) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(models) { item ->
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable {
                    navigateTo(AppRouter.LogDetail(item.path).route)
                }) {
                ScaleText(text = item.content, fontSize = 16.sp)
            }
        }
    }
}