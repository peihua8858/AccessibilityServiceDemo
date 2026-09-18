package com.peihua.touchmonitor.ui.screen.function.appmanager

import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.components.search.SearchBarDefaults
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.function.apk.ApkScreenContent
import com.peihua.touchmonitor.ui.screen.function.search.SearchType
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.ui.theme.labelMediumNormal
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.AppExtractorViewModel
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.isLandscape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppExtractorScreen(modifier: Modifier = Modifier) {
    val userApplication = stringResource(id = R.string.text_user_application)
    val systemApplication = stringResource(id = R.string.text_system_application)
    val apk = stringResource(id = R.string.text_install_package)
    val tabs: MutableList<Pair<String, @Composable (PagerState, Int) -> Unit>> =
        mutableListOf(
            userApplication to { s, index -> UserAppScreen(Modifier) },
            systemApplication to { s, index -> SystemAppScreen(Modifier) },
            apk to { s, index -> ApkScreenContent(Modifier) },
        )
    val pagerState = rememberPagerState { tabs.size }
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_app_manager),
        elevation = 0.dp,
        navigateUp = {
            popBackStack()
        },
        actions = {
            SearchBarDefaults.TrailingIcon(
                modifier = Modifier
                    .clickable {
                        when(pagerState.currentPage){
                            0 -> navigateTo2(AppRouter.SearchScreen.route, "searchType" to SearchType.APPLICATION)
                            1 -> navigateTo2(AppRouter.SearchScreen.route, "searchType" to SearchType.APPLICATION)
                            2 -> navigateTo2(AppRouter.SearchScreen.route, "searchType" to SearchType.APK)
                        }
                    })
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top
        ) {
            TabPager(modifier = modifier, pagerState = pagerState, tabs = tabs)
        }
    }
}

@Composable
private fun UserAppScreen(
    modifier: Modifier = Modifier,
    viewModel: AppExtractorViewModel = viewModel(),
) {
    val result = viewModel.userApplications.value
    //请求数据
    val refresh = {
        viewModel.requestUserAppList()
    }
    MultiStateScreen(modifier = modifier, result, refresh) {
        AppListScreenContent(Modifier, it)
    }
}

@Composable
fun SystemAppScreen(
    modifier: Modifier = Modifier,
    viewModel: AppExtractorViewModel = viewModel(),
) {
    val result = viewModel.sysApplications.value
    //请求数据
    val refresh = {
        viewModel.requestSystemAppList()
    }
    MultiStateScreen(modifier = modifier, result, refresh) {
        AppListScreenContent(Modifier, it)
    }
}

@Composable
private fun AppListScreenContent(
    modifier: Modifier = Modifier,
    models: List<AppInfoModel>,
) {
    val context = LocalContext.current
    val isLandscape = context.isLandscape
    LazyColumn(modifier = modifier) {
        items(models) { item ->
            AppItemView(
                Modifier
                    .padding(bottom = 16.dp)
                    .clickable {
                        navigateTo(
                            AppRouter.AppDetailScreen.route,
                            "packageName" to item.packageName
                        )
                    }, item
            )
        }
    }
}

@Composable
private fun AppItemView(
    modifier: Modifier,
    item: AppInfoModel,
    iconSize: Dp = 64.dp,
) {
    val context = LocalContext.current
    val textStyle = LocalTextStyle.current
    dLog { "111textColor:" + textStyle.color }
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val drawable = item.icon
        val (icon, title, pkgName, version, fileSize, line) = createRefs()
        Image(
            if (drawable == null)
                rememberAsyncImagePainter(R.mipmap.ic_launcher)
            else rememberDrawablePainter(drawable), "",
            modifier = Modifier
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .size(iconSize)
                .clip(RoundedCornerShape(8.dp))
        )
        ScaleText(
            modifier = Modifier
                .constrainAs(title) {
                    start.linkTo(icon.end)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(start = 8.dp),
            text = stringResource(R.string.text_name, item.name),
            maxLines = 1,
        )
        ScaleText(
            modifier = Modifier
                .constrainAs(pkgName) {
                    start.linkTo(icon.end)
                    top.linkTo(title.bottom)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = 8.dp,
                    top = 4.dp
                ),
            text = stringResource(R.string.text_package, item.packageName),
            maxLines = 1,
        )
        ScaleText(
            modifier = Modifier
                .constrainAs(version) {
                    start.linkTo(title.start)
                    top.linkTo(pkgName.bottom)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = 8.dp,
                    top = 4.dp
                ),
            text = stringResource(R.string.text_version, item.versionName),
            maxLines = 1,
        )
        ScaleText(
            modifier = Modifier
                .constrainAs(fileSize) {
                    start.linkTo(title.start)
                    top.linkTo(version.bottom)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = 8.dp,
                    top = 4.dp
                ),
            text = stringResource(
                R.string.text_file_size,
                Formatter.formatFileSize(context, item.fileSize)
            ),
            maxLines = 1,
        )
        Spacer(
            Modifier
                .size(20.dp)
                .constrainAs(line) {
                    start.linkTo(parent.start)
                    top.linkTo(version.bottom)
                    end.linkTo(parent.end)
                })
    }
}