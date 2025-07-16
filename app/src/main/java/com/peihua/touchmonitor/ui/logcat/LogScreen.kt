package com.peihua.touchmonitor.ui.logcat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.popBackStack

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
        mTabs[index].content(m.padding(dimensionResource(id = R.dimen.dp_32)), state)
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