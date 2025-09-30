package com.peihua.touchmonitor.ui.screen.storage

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.StorageViewModel

@Composable
fun StorageScreen(
    modifier: Modifier,
    title: String,
    path: String,
    viewModel: StorageViewModel = viewModel()
) {
    val result = viewModel.storageState.value
    //请求数据
    val refresh = {
        viewModel.request(path)
    }
    val changeFolder = { nextPath: String ->
        viewModel.request(nextPath)
    }
    MultiStateScreen(modifier, title, result, refresh) {
        StorageScreenContent(result = it, changeFolder = changeFolder)
    }
}


@Composable
fun StorageScreenContent(
    modifier: Modifier = Modifier,
    result: MutableList<MediaData>,
    changeFolder: (String) -> Unit
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