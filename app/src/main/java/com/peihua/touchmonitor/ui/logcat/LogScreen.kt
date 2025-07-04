package com.peihua.touchmonitor.ui.logcat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
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
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.viewmodel.LogViewModel

@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(), topBar = {
            AppTopBar(title = { stringResource(R.string.text_log) }, navigateUp = {
                popBackStack()
            })
        }) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LogcatContent()
        }
    }
}

@Composable
fun LogcatContent(modifier: Modifier = Modifier) {
    val mTabs = listOf(
        LogTabItem(stringResource(R.string.log_cat)) { m, state -> LogcatScreen(m) },
        LogTabItem(stringResource(R.string.log_crash)) { m, state -> CrashLogScreen(m) },
    )
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { mTabs.size }
    TabPager(modifier = modifier, tabs = mTabs, pagerState = pagerState) { m, state, index ->
        mTabs[index].content(m.padding(32.dp), state)
    }
}

private data class LogTabItem(
    val title: String,
    val content: @Composable (Modifier, PagerState) -> Unit
) {
    override fun toString(): String {
        return title
    }
}