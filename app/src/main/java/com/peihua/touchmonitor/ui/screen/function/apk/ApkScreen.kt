package com.peihua.touchmonitor.ui.screen.function.apk

import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.ApkModel
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.utils.installApk
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.viewmodel.ApkViewModel
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.isLandscape

@Composable
fun ApkScreen(modifier: Modifier = Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_install_package_manager),
        navigateUp = {
            popBackStack()
        }) {
        ApkScreenContent(Modifier)
    }
}

@Composable
fun ApkScreenContent(modifier: Modifier = Modifier, viewModel: ApkViewModel = viewModel()) {
    val result = viewModel.apkList.value
    //请求数据
    val refresh = {
        viewModel.getApkList()
    }
    MultiStateScreen(modifier = modifier, result, refresh) {
        ApkListScreenContent(Modifier, it)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApkListScreenContent(
    modifier: Modifier = Modifier,
    models: List<ApkModel>,
) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier) {
        items(models) { item ->
            ApkItemView(
                Modifier
                    .padding(bottom = 16.dp)
                    .combinedClickable(onClick = {
                        context.installApk(item.path)
                    }, onLongClick = {
//                        context.shareCertainFiles(item.path)
                        navigateTo2(Dialog.ShareDialog.route, ("filePath" to item.path))
                    }), item
            )
        }
    }
}

@Composable
private fun ApkItemView(
    modifier: Modifier,
    item: ApkModel,
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
        val hasDisplayName = item.displayName.isNotEmpty()
        if (hasDisplayName) {
            ScaleText(
                modifier = Modifier
                    .constrainAs(title) {
                        start.linkTo(icon.end)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        horizontalBias = 0f
                        horizontalChainWeight = 1f
                    }
                    .padding(start = 8.dp),
                text = stringResource(R.string.text_app_name, item.displayName),
                maxLines = 1,
                style = MaterialTheme.typography.labelLargeNormal
            )
        }
        ScaleText(
            modifier = Modifier
                .constrainAs(pkgName) {
                    start.linkTo(icon.end)
                    top.linkTo(if (hasDisplayName) title.bottom else parent.top)
                    end.linkTo(parent.end)
                    horizontalBias = 0f
                    width = Dimension.fillToConstraints
                    horizontalChainWeight = 1f
                }
                .padding(
                    start = 8.dp,
                    top = 4.dp
                ),
            text = stringResource(R.string.text_file_name, item.apkName),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLargeNormal
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
                        start = 8.dp,
                        top = 4.dp
                    ),
                text = stringResource(R.string.text_version, item.versionName),
                maxLines = 1,
                style = MaterialTheme.typography.labelLargeNormal
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
                    start = 8.dp,
                    top = 4.dp
                ),
            text = stringResource(
                R.string.text_file_size,
                Formatter.formatFileSize(context, item.fileSize)
            ),
            maxLines = 1,
            style = MaterialTheme.typography.labelLargeNormal
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