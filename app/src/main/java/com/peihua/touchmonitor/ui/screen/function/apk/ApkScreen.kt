package com.peihua.touchmonitor.ui.screen.function.apk

import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
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
import com.peihua.touchmonitor.model.ApkModel
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.labelSmallNormal
import com.peihua.touchmonitor.utils.ContextExt.isLandscape
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.installApk
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.ApkViewModel

@Composable
fun ApkScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_install_package_manager),
        navigateUp = {
            popBackStack()
        }) {
        ApkScreenContent()
    }
}

@Composable
fun ApkScreenContent(modifier: Modifier = Modifier, viewModel: ApkViewModel = viewModel()) {
    val result = viewModel.apkList.value
    //请求数据
    val refresh = {
        viewModel.getApkList()
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
                ApkListScreenContent(Modifier, result.data)
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
private fun ApkListScreenContent(
    modifier: Modifier = Modifier,
    models: List<ApkModel>,
) {
    val context = LocalContext.current
    val isLandscape = context.isLandscape()
    val iconSize =
        if (isLandscape) dimensionResource(id = R.dimen.dp_48) else dimensionResource(id = R.dimen.dp_48)
    LazyColumn(modifier = modifier) {
        items(models) { item ->
            ApkItemView(
                Modifier
                    .padding(bottom = dimensionResource(id = R.dimen.dp_16))
                    .clickable {
                        context.installApk(item.path)
                    }, item, iconSize
            )
        }
    }
}

@Composable
private fun ApkItemView(
    modifier: Modifier,
    item: ApkModel,
    iconSize: Dp = dimensionResource(id = R.dimen.dp_96),
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
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
        )
        val hasDisplayName = item.displayName.isNotEmpty()
        if (hasDisplayName) {
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
                text = stringResource(R.string.text_app_name, item.displayName),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmallNormal
            )
        }
        ScaleText(
            modifier = Modifier
                .constrainAs(pkgName) {
                    start.linkTo(icon.end)
                    top.linkTo(if(hasDisplayName)title.bottom else parent.top)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = dimensionResource(id = R.dimen.dp_8),
                    top = dimensionResource(id = R.dimen.dp_4)
                ),
            text = stringResource(R.string.text_file_name, item.apkName),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmallNormal
        )
        val hasVersion = item.versionName.isNotEmpty()
        if (hasVersion) {
            ScaleText(
                modifier = Modifier
                    .constrainAs(version) {
                        start.linkTo(icon.end)
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
        }
        ScaleText(
            modifier = Modifier
                .constrainAs(fileSize) {
                    start.linkTo(icon.end)
                    top.linkTo(if (hasVersion) version.bottom else pkgName.bottom)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = dimensionResource(id = R.dimen.dp_8),
                    top = dimensionResource(id = R.dimen.dp_4)
                ),
            text = stringResource(
                R.string.text_file_size,
                Formatter.formatFileSize(context, item.fileSize)
            ),
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