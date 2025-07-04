package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <T> TabPager(
    modifier: Modifier = Modifier,
    tabs: List<T>,
    isFixedModel: Boolean = tabs.size <= 7,
    pagerState: PagerState = rememberPagerState() {
        tabs.size
    },
    tabIndicator: @Composable (tabPositions: List<TabPosition>, PagerState) -> Unit = { tabPositions, state ->
        PagerTabIndicator(tabPositions = tabPositions, pagerState = state)
    },
    pageContent: @Composable (Modifier, PagerState, Int) -> Unit
) {

    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isFixedModel) {
            TabRow(
                modifier = modifier
                    .fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    tabIndicator(tabPositions, pagerState)
                }
            ) {
                scope.TabContent(tabs, pagerState)
            }
        } else {
            ScrollableTabRow(
                modifier = modifier
                    .fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage, indicator = {
                    tabIndicator(it, pagerState)
                }) {
                scope.TabContent(tabs, pagerState)
            }
        }
        HorizontalPager(state = pagerState, modifier = modifier) {
            pageContent(
                modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally),
                pagerState, it
            )
        }
    }
}

@Composable
private fun <T> CoroutineScope.TabContent(mTabs: List<T>, pagerState: PagerState) {
    mTabs.forEachIndexed { index, item ->
        TabView(
            Modifier.zIndex(1f),
            item,
            index,
            pagerState.currentPage == index
        ) { i ->
            launch {
                pagerState.animateScrollToPage(i)
            }
        }
    }

}
