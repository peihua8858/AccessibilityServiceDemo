package com.peihua.touchmonitor.ui.screen.function.audio

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
import androidx.compose.foundation.lazy.items
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
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.utils.openWithFile
import com.peihua.touchmonitor.viewmodel.AudioViewModel

@Composable
fun AudioScreen(modifier: Modifier, viewModel: AudioViewModel = viewModel()) {
    val result = viewModel.pictureState.value
    val sortType = 1
    //请求数据
    val refresh = {
        viewModel.requestAudio(sortType)
    }
    MultiStateScreen(modifier, R.string.text_audio, result, refresh) {
        AudioScreenContent(result = it)
    }
}

@Composable
fun AudioScreenContent(modifier: Modifier = Modifier, result: MutableList<MediaHeader>) {
    val dp8 = dimensionResource(R.dimen.dp_8)
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(dp8),
    ) {
        for (item in result) {
            item {
                Text(
                    modifier = Modifier,
                    text = item.title
                )
            }
            items(item.mediaList) { photo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.openWithFile(photo.filePath)
//                            navigateTo2(AppRouter.AudioPlayerScreen.route, ("audioPath" to photo.filePath))
                        }
                        .padding(vertical = dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_32)),
                        painter = painterResource(R.drawable.ic_audio_file_24),
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