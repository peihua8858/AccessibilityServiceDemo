package com.peihua.touchmonitor.ui.screen.function.appmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.labelSmallNormal
import com.peihua.touchmonitor.utils.ContextExt.isLandscape
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.AppExtractorViewModel

@Composable
fun MainAppExtractorScreen(modifier: Modifier = Modifier) {
    val application = stringResource(id = R.string.text_application)
    val apk = stringResource(id = R.string.text_install_package)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppTopBar(title = { stringResource(id = R.string.text_app_manager) }, navigateUp = {
            popBackStack()
        })
        TabPager(modifier = modifier, tabs = listOf(application, apk)) { modifier, state, index ->
            when (index) {
                0 -> {
                    AppExtractorScreen(modifier)
                }

                1 -> {
                    ApkPackageScreen(modifier)
                }
            }

        }
    }
}

@Composable
private fun AppExtractorScreen(
    modifier: Modifier = Modifier,
    viewModel: AppExtractorViewModel = viewModel(),
) {
    val result = viewModel.applications.value
    //请求数据
    val refresh = {
        viewModel.refreshAppList()
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(
                start = dimensionResource(id = R.dimen.dp_16),
                end = dimensionResource(id = R.dimen.dp_16)
            )
    ) {

        when (result) {
            is ResultData.Success -> {
                AppListScreenContent(Modifier, result.data)
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
private fun AppListScreenContent(
    modifier: Modifier = Modifier,
    models: List<AppInfoModel>,
) {
    val context = LocalContext.current
    val isLandscape = context.isLandscape()
    val iconSize =
        if (isLandscape) dimensionResource(id = R.dimen.dp_48) else dimensionResource(id = R.dimen.dp_48)
    LazyColumn(modifier = modifier) {
        items(models) { item ->
            AppItemView(Modifier.clickable {
                navigateTo(
                    AppRouter.AppDetailScreen.route,
                    "packageName" to item.packageName
                )
            }, item, iconSize)
        }
    }
}

@Composable
private fun AppItemView(
    modifier: Modifier,
    item: AppInfoModel,
    iconSize: Dp = dimensionResource(id = R.dimen.dp_96),
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val drawable = item.icon
        val (icon, title, pkgName, version, line) = createRefs()
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
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
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
                .padding(start = dimensionResource(id = R.dimen.dp_8)),
            text = stringResource(R.string.text_name, item.name),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmallNormal
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
                    start = dimensionResource(id = R.dimen.dp_8),
                    top = dimensionResource(id = R.dimen.dp_4)
                ),
            text = stringResource(R.string.text_package, item.packageName),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmallNormal
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
                    start = dimensionResource(id = R.dimen.dp_8),
                    top = dimensionResource(id = R.dimen.dp_4)
                ),
            text = stringResource(R.string.text_version, item.versionName),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmallNormal
        )
        Spacer(
            Modifier
                .size(dimensionResource(id = R.dimen.dp_20))
                .constrainAs(line) {
                    start.linkTo(parent.start)
                    top.linkTo(version.bottom)
                    end.linkTo(parent.end)
                })
    }
}

@Composable
private fun ApkPackageScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
    }
}