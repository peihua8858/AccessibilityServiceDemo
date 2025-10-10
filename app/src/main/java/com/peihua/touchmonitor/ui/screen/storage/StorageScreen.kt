package com.peihua.touchmonitor.ui.screen.storage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.StorageViewModel

@Composable
fun StorageScreen(
    modifier: Modifier,
    title: String,
    path: String,
    viewModel: StorageViewModel = viewModel(),
) {
    val result = viewModel.storageState.value
    val headerResult = viewModel.folderState
    //请求数据
    val refresh = {
        viewModel.request(path)
    }
    val changeFolder = { nextPath: String ->
        viewModel.request(nextPath)
    }
    MultiStateScreen(
        modifier, title, result, refresh,
        navigateUp = {
            if (headerResult.size > 2) {
                headerResult.removeAt(headerResult.size - 1)
                val dir = headerResult[headerResult.size - 1]
                viewModel.request(dir.second, dir.first)
            } else {
                popBackStack()
            }
        },
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.dp_48))
                    .background(Colors.Grey[100])
                    .padding(start = dimensionResource(R.dimen.dp_16), end = dimensionResource(R.dimen.dp_16)),
                verticalAlignment = Alignment.CenterVertically
            ) { StorageScreenHeader(result = headerResult, changeFolder = changeFolder) }
        }) {
        StorageScreenContent(result = it, changeFolder = changeFolder)
    }
}

@Composable
fun StorageScreenHeader(result: SnapshotStateList<Pair<String, String>>, changeFolder: (String) -> Unit) {
    result.forEach {
        Text(text = "${it.first}>", modifier = Modifier.clickable { changeFolder(it.second) })
    }
}


@Composable
fun StorageScreenContent(
    modifier: Modifier = Modifier,
    result: MutableList<MediaData>,
    changeFolder: (String) -> Unit,
) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(dp8),
        ) {
            items(result) { photo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (photo.isDirectory) {
                                changeFolder(photo.filePath)
                            } else {
                                context.openWithFile(photo.filePath)
                            }
                        }
                        .combinedClickable(onClick = {
                            if (photo.isDirectory) {
                                changeFolder(photo.filePath)
                            } else {
                                context.openWithFile(photo.filePath)
                            }
                        }, onLongClick = {
                            if (photo.isFile) {
                                navigateTo2(AppRouter.ShareScreen.route, ("filePath" to photo.filePath))
                            }
                        })
                        .padding(vertical = dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_32)),
                        painter = painterResource(R.mipmap.ic_zip_file),
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier,
                            text = photo.fileName
                        )
                        Text(
                            modifier = Modifier,
                            text = photo.fileSize ?: ""
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }

    }
}