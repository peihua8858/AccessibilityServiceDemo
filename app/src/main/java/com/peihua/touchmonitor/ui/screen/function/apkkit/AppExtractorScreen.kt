package com.peihua.touchmonitor.ui.screen.function.apkkit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ContextExt.isLandscape
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.viewmodel.AppExtractorViewModel

@Composable
fun AppExtractorScreen(modifier: Modifier = Modifier,viewModel: AppExtractorViewModel = viewModel()) {
    val result = viewModel.applications.value
    //请求数据
    val refresh = {
        viewModel.refreshAppList()
    }
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = dimensionResource(id = R.dimen.dp_16), end = dimensionResource(id = R.dimen.dp_16))
    ) {
        AppTopBar(title = { "Touch Monitor" }, navigateUp = {
            popBackStack()
        })
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
private fun AppListScreenContent(modifier: Modifier = Modifier, models: List<AppInfoModel>) {
    val context = LocalContext.current
    val isLandscape = context.isLandscape()
    val iconSize = if (isLandscape) dimensionResource(id = R.dimen.dp_96) else dimensionResource(id = R.dimen.dp_96)
    LazyVerticalGrid(
        modifier = modifier,
        //如果是平板或者大屏则使用4列，否则2列
        columns = GridCells.Fixed(if (isLandscape) 4 else 2),
        contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.dp_16))
    ) {
        items(models) { item ->
            AppItemView(Modifier.clickable {
                popBackStack{
                    set("packageName", item.packageName)
//                    putString("packageName", item.packageName)
                }
            }, item, iconSize)
        }
    }
}

@Composable
private fun AppItemView(modifier: Modifier, item: AppInfoModel, iconSize: Dp = dimensionResource(id = R.dimen.dp_96)) {
    val colorScheme = MaterialTheme.colorScheme
    ConstraintLayout(modifier = modifier) {
        val drawable = item.icon
        val (icon, title, desc, line) = createRefs()
        Image(
            if (drawable == null)
                rememberAsyncImagePainter(R.mipmap.ic_launcher)
            else rememberDrawablePainter(drawable), "",
            modifier = Modifier
                .constrainAs(icon) {
                    start.linkTo(title.start)
                    top.linkTo(parent.top)
                    end.linkTo(title.end)
                }
                .size(iconSize)
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
        )

        ScaleText(
            modifier = Modifier
                .constrainAs(title) {
                    start.linkTo(parent.start)
                    top.linkTo(icon.bottom)
                    end.linkTo(parent.end)
                    horizontalChainWeight = 1f
                }
                .padding(top = dimensionResource(id = R.dimen.dp_4)),
            text = item.name,
            fontSize = dimensionSpResource(id = R.dimen.sp_20),
//            color = if (item.isHistory) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
//        val ids = if (item.isHistory) desc else title
//        if (item.isHistory) {
//            ScaleText(
//                modifier = Modifier
//                    .constrainAs(desc) {
//                        start.linkTo(title.start)
//                        top.linkTo(title.bottom)
//                        end.linkTo(title.end)
//                        horizontalChainWeight = 1f
//                    }
//                    .padding(top = dimensionResource(id = R.dimen.dp_4)),
//                text = "最近使用",
//                fontSize = dimensionSpResource(id = R.dimen.sp_16),
//                color = colorScheme.onSecondaryContainer,
//                style = MaterialTheme.typography.labelMedium
//            )
//        }
        Spacer(
            Modifier
                .size(dimensionResource(id = R.dimen.dp_20))
                .constrainAs(line) {
                    start.linkTo(parent.start)
                    top.linkTo(title.bottom)
                    end.linkTo(parent.end)
                })
    }
}