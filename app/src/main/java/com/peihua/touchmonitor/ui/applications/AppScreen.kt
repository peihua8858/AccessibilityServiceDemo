package com.peihua.touchmonitor.ui.applications

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.AppInfo
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingViewFillMaxSize
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.viewmodel.ApplicationsViewModel
import com.peihua8858.tools.utils.isLandscape

@Composable
fun AppScreen(modifier: Modifier = Modifier, viewModel: ApplicationsViewModel = viewModel()) {
    val result = viewModel.applications.value
    //请求数据
    val refresh = {
        viewModel.requestData()
    }
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_app_manager),
        navigateUp = {
            popBackStack()
        }) {
        Column(
            modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            when (result) {
                is ResultData.Success -> {
                    AppScreenContent(Modifier, result.data) {
                        viewModel.saveToDb(it)
                    }
                }

                is ResultData.Failure -> {
                    ErrorView(retry = refresh)
                }

                is ResultData.Initialize -> {
                    refresh()
                }

                is ResultData.Starting -> {
                    LoadingViewFillMaxSize()
                }
            }
        }
    }
}

@Composable
private fun AppScreenContent(
    modifier: Modifier = Modifier,
    models: List<AppInfo>,
    saveToDb: (AppInfo) -> Unit,
) {
    val context = LocalContext.current
    val isLandscape = context.isLandscape
    val iconSize =96.dp
    LazyVerticalGrid(
        modifier = modifier,
        //如果是平板或者大屏则使用4列，否则2列
        columns = GridCells.Fixed(if (isLandscape) 4 else 2),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(models) { item ->
            AppItemView(Modifier.clickable {
                saveToDb(item)
                popBackStack {
                    set("packageName", item.packageName)
//                    putString("packageName", item.packageName)
                }
            }, item, iconSize)
        }
    }
}

@Composable
private fun AppItemView(
    modifier: Modifier,
    item: AppInfo,
    iconSize: Dp = 96.dp,
) {
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
                .clip(RoundedCornerShape(8.dp))
        )

        ScaleText(
            modifier = Modifier
                .constrainAs(title) {
                    start.linkTo(parent.start)
                    top.linkTo(icon.bottom)
                    end.linkTo(parent.end)
                    horizontalChainWeight = 1f
                }
                .padding(top = 4.dp),
            text = item.name,
            fontSize = 20.sp,
            color = if (item.isHistory) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
        )
        val ids = if (item.isHistory) desc else title
        if (item.isHistory) {
            ScaleText(
                modifier = Modifier
                    .constrainAs(desc) {
                        start.linkTo(title.start)
                        top.linkTo(title.bottom)
                        end.linkTo(title.end)
                        horizontalChainWeight = 1f
                    }
                    .padding(top = 4.dp),
                text = "最近使用",
                fontSize = 16.sp,
                color = colorScheme.onSecondaryContainer,
            )
        }
        Spacer(
            Modifier
                .size(20.dp)
                .constrainAs(line) {
                    start.linkTo(parent.start)
                    top.linkTo(ids.bottom)
                    end.linkTo(parent.end)
                })
    }
}
