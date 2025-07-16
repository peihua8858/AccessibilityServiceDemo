package com.peihua.touchmonitor.ui.screen.main

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.Card
import com.peihua.touchmonitor.ui.components.CardViewItem
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.theme.AppColor
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.showToast

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val content = StringBuilder()
    configuration.orientation
    configuration.screenLayout
    content.append("屏幕宽度（单位：dp）：${configuration.screenWidthDp},")
        .append("\n")
        .append("屏幕高度（单位：dp）：${configuration.screenHeightDp}")
        .append("\n")
        .append("屏幕的总体方向：${if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "横向" else "纵向"}")
        .append("\n")
        .append("最小屏幕宽度（单位：dp）：${configuration.smallestScreenWidthDp}")
        .append("\n")
        .append("像素密度DPI：${configuration.densityDpi}")
        .append("\n")
        .append("像素密度：${density.density}")
        .append("\n")
        .append("字体缩放系数：${density.fontScale}")
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.dp_16))
    ) {
        Row(modifier = Modifier) {
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        navigateTo(AppRouter.AutoScroller.route)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_e30b5a, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_home_scroller),
                            contentDescription = stringResource(id = R.string.text_auto_scroll)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_auto_scroll)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_fdff0e66, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_home_images_24),
                            contentDescription = stringResource(id = R.string.text_images)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_images)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_e30b5a, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_home_audio_24),
                            contentDescription = stringResource(id = R.string.text_audio)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_audio)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_f63505, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.mipmap.ic_video_home),
                            contentDescription = stringResource(id = R.string.text_videos)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_videos)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_7b1fa2, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.mipmap.ic_doc_home),
                            contentDescription = stringResource(id = R.string.text_documents)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_documents)
                    )
                })
        }
        Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_16))) {
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_08bf54, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.mipmap.ic_apk_home),
                            contentDescription = stringResource(id = R.string.text_apk)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_apk)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_2039c5, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.mipmap.ic_zip_home),
                            contentDescription = stringResource(id = R.string.text_compression)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_compression)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_2979ff, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_download_24),
                            contentDescription = stringResource(id = R.string.text_download)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_download)
                    )
                })
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_ffd600, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            painter = painterResource(id = R.mipmap.ic_fav_home),
                            tint = Color.White,
                            contentDescription = stringResource(id = R.string.text_collect_folder)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_collect_folder)
                    )
                })

            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_7b1fa2, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_home_search_24),
                            contentDescription = stringResource(id = R.string.text_search)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_search)
                    )
                })
        }
        Row(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_16))) {
            CardViewItem(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = dimensionResource(id = R.dimen.dp_8),
                        end = dimensionResource(id = R.dimen.dp_8)
                    )
                    .clickable {
                        showToast(R.string.text_function_developing)
                    },
                icon = {
                    Card(modifier = Modifier, elevation = dimensionResource(id = R.dimen.dp_3)) {
                        Icon(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .size(dimensionResource(id = R.dimen.dp_48))
                                .background(AppColor.color_e30b5a, shape = RectangleShape)
                                .padding(dimensionResource(id = R.dimen.dp_8)),
                            tint = Color.White,
                            painter = painterResource(id = R.drawable.ic_home_app_manager_24),
                            contentDescription = stringResource(id = R.string.text_app_manager)
                        )
                    }
                }, title = {
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_8)),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        text = stringResource(id = R.string.text_app_manager)
                    )
                })
            Box(
                Modifier
                    .weight(1f)
            ) { }
            Box(
                Modifier
                    .weight(1f)
            ) { }
            Box(
                Modifier
                    .weight(1f)
            ) { }
            Box(
                Modifier
                    .weight(1f)
            ) { }
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_16)))
        Text(
            text = content.toString(),
            fontSize = dimensionSpResource(id = R.dimen.sp_12),
        )
    }

}