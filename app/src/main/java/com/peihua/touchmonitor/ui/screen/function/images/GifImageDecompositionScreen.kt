package com.peihua.touchmonitor.ui.screen.function.images


import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.gif.GifSplitter
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.getFileFromContentUri
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.utils.items
import com.peihua.touchmonitor.utils.openInputStream
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.rememberSaveableList
import com.peihua.touchmonitor.utils.rememberState
import com.peihua.touchmonitor.utils.saveBitmapToGallery
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * gif图片分解
 */
@Composable
fun GifImageDecompositionScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val selectedUri = rememberSaveable<Uri>(Uri.EMPTY)
    val showLoadingDialog = rememberShowProgressDialog()
    val bitmapDrawables = rememberSaveableList<BitmapDrawable>(arrayListOf())
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val processPhoto = { uri: Uri ->
        isLoading.value = true
        val inputStream = uri.openInputStream()
        if (inputStream != null) {
            val gifSplitter = GifSplitter()
            val gifBitmaps = gifSplitter.splitGif(inputStream)
            gifBitmaps.forEach {
                bitmapDrawables.add(it.toDrawable(resources))
            }
        }
        isLoading.value = false
    }
    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoVisualMedia()) {
        if (it != null) {
            selectedUri.value = it
            scope.launch {
                processPhoto(it)
            }
        }
    }
    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(id = R.string.text_gif_image_decomposition)
    ) {
        Column {
            Box(
                modifier = modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading.value) {
                    // Display a progress bar while loading
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(80.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(if (isLandscape) 8 else 6),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bitmapDrawables) { index, item ->
                        AsyncImage(
                            modifier = Modifier,
                            contentScale = ContentScale.Inside,
                            model = item,
                            contentDescription = "",
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(modifier = Modifier.weight(1f), onClick = {
                    selectPhotoLauncher.launch(PhotoVisualMediaRequest(PhotoVisualMedia.MultipleMimeType("image/gif")))
                }) {
                    ScaleText(text = stringResource(id = R.string.text_select_photo))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(modifier = Modifier.weight(1f), onClick = {
                    scope.launch {
                        showLoadingDialog.value = true
                        val contentResolver = context.contentResolver
                        val file = contentResolver.getFileFromContentUri(selectedUri.value)
                        dLog { ">>>>>>file: ${file?.absolutePath}" }
                        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        val folder = (file?.nameWithoutExtension ?: "GifSplitter")
                        for ((index, item) in bitmapDrawables.withIndex()) {
                            val outFileName = folder + "_" + index + ".jpg"
                            contentResolver.saveBitmapToGallery(
                                uri,
                                folder,
                                item.bitmap,
                                outFileName,
                                ""
                            )
                        }
                        showLoadingDialog.value = false
                    }
                }) {
                    ScaleText(text = stringResource(id = R.string.text_decompose_gif))
                }
            }
        }
    }
}