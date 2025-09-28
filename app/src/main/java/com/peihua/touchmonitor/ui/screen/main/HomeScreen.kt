package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.Card
import com.peihua.touchmonitor.ui.components.CardViewItem
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.theme.AppColor
import com.peihua.touchmonitor.utils.checkStorgePermission
import com.peihua.touchmonitor.utils.showToast

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_home)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dp_16))
        ) {
            HomeHorList(
                modifier = Modifier,
                titles = listOf(
                    stringResource(id = R.string.text_auto_scroll) to AppRouter.AutoScroller,
                    stringResource(id = R.string.text_images) to AppRouter.PictureScreen,
                    stringResource(id = R.string.text_audio) to AppRouter.AudioScreen,
                    stringResource(id = R.string.text_videos) to AppRouter.VideoScreen,
                    stringResource(id = R.string.text_documents) to AppRouter.DocumentScreen
                ),
                painters = listOf(
                    painterResource(id = R.drawable.ic_home_scroller),
                    painterResource(id = R.drawable.ic_home_images_24),
                    painterResource(id = R.drawable.ic_home_audio_24),
                    painterResource(id = R.mipmap.ic_video_home),
                    painterResource(id = R.mipmap.ic_doc_home)
                ),
                iconBgColors = listOf(
                    AppColor.color_e30b5a,
                    AppColor.color_fdff0e66,
                    AppColor.color_e30b5a,
                    AppColor.color_f63505,
                    AppColor.color_7b1fa2
                )

            ) { item, index ->
                item.second?.let {
                    if (it != AppRouter.AutoScroller) {
                        if (!context.checkStorgePermission()) {
                            return@let
                        }
                    }
                    navigateTo(it.route)
                } ?: showToast(R.string.text_function_developing)
                true
            }
            HomeHorList(
                modifier = Modifier,
                titles = listOf(
                    stringResource(id = R.string.text_apk) to AppRouter.ApkManagerScreen,
                    stringResource(id = R.string.text_compression) to AppRouter.ZipScreen,
                    stringResource(id = R.string.text_download) to AppRouter.DownloadScreen,
                    stringResource(id = R.string.text_collect_folder) to AppRouter.CollectScreen,
                    stringResource(id = R.string.text_search) to AppRouter.SearchScreen
                ),
                painters = listOf(
                    painterResource(id = R.mipmap.ic_apk_home),
                    painterResource(id = R.mipmap.ic_zip_home),
                    painterResource(id = R.drawable.ic_download_24),
                    painterResource(id = R.mipmap.ic_fav_home),
                    painterResource(id = R.drawable.ic_home_search_24)
                ),
                iconBgColors = listOf(
                    AppColor.color_08bf54,
                    AppColor.color_2039c5,
                    AppColor.color_2979ff,
                    AppColor.color_ffd600,
                    AppColor.color_7b1fa2
                )
            )
            HomeHorList(
                modifier = Modifier,
                titles = listOf(
                    stringResource(id = R.string.text_app_manager) to AppRouter.AppManagerScreen,
                    "" to null,
                    "" to null,
                    "" to null,
                    "" to null
                ),
                painters = listOf(painterResource(id = R.drawable.ic_home_app_manager_24)),
                iconBgColors = listOf(AppColor.color_e30b5a)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_16)))
        }
    }
}

@Composable
private fun HomeHorList(
    modifier: Modifier,
    titles: List<Pair<String, AppRouter?>>,
    painters: List<Painter>,
    iconBgColors: List<Color>,
    onItemClick: (Pair<String, AppRouter?>, Int) -> Boolean = { _, _ -> false },
) {
    val context = LocalContext.current
    Row(modifier = modifier.padding(top = dimensionResource(id = R.dimen.dp_16))) {
        for ((index, item) in titles.withIndex()) {
            if (item.first.isEmpty()) {
                Spacer(Modifier.weight(1f))
            } else {
                HomeCard(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .weight(1f),
                    title = item.first,
                    painter = painters[index],
                    iconBgColor = iconBgColors[index]
                ) {
                    if (!onItemClick(item, index)) {
                        item.second?.let {
                            if (context.checkStorgePermission()) {
                                navigateTo(it.route)
                            }
                        } ?: showToast(R.string.text_function_developing)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCard(
    modifier: Modifier,
    title: String,
    painter: Painter,
    iconBgColor: Color,
    onClick: () -> Unit = {},
) {
    CardViewItem(
        modifier = modifier
            .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
            .clickable(onClick = onClick),
        icon = {
            Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                Icon(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .size(dimensionResource(id = R.dimen.dp_48))
                        .background(iconBgColor, shape = RectangleShape)
                        .padding(dimensionResource(id = R.dimen.dp_8)),
                    tint = Color.White,
                    painter = painter,
                    contentDescription = title
                )
            }
        }, title = {

        })
}