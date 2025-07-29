package com.peihua.touchmonitor.ui.logcat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun LogScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_log),
        navigateUp = {
            popBackStack()
        }) {
        CrashLogScreen()
    }
}

//@Composable
//fun LogcatContent(modifier: Modifier = Modifier) {
//    val mTabs = listOf(
//        LogTabItem(stringResource(R.string.log_cat)) { m, state -> LogcatScreen(m) },
//        LogTabItem(stringResource(R.string.log_crash)) { m, state -> CrashLogScreen(m) },
//    )
//    val pagerState = rememberPagerState(
//        initialPage = 0,
//        initialPageOffsetFraction = 0f
//    ) { mTabs.size }
//    TabPager(modifier = modifier, tabs = mTabs, pagerState = pagerState) { m, state, index ->
//        mTabs[index].content(m.padding(dimensionResource(id = R.dimen.dp_32)), state)
//    }
//}
//
//private data class LogTabItem(
//    val title: String,
//    val content: @Composable (Modifier, PagerState) -> Unit
//) {
//    override fun toString(): String {
//        return title
//    }
//}