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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.peihua.selector.result.PhotoCropVisualMediaRequestBuilder
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoCropVisualMedia
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.bitmap.NinePicBitmapSlicer
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.getFileFromContentUri
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.rememberSaveableList
import com.peihua.touchmonitor.utils.saveBitmapToGallery
import com.peihua.touchmonitor.utils.toDp
import com.peihua8858.compose.tools.rememberState
import com.peihua8858.tools.utils.adjustBitmapOrientation
import com.peihua8858.tools.utils.createFile
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 九宫格切图
 */
@Composable
fun NineGridCutImageScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val selectedUri = rememberSaveable<Uri>(Uri.EMPTY)
    val context = LocalContext.current
    val showLoadingDialog = rememberShowProgressDialog()
    val drawables = rememberSaveableList<BitmapDrawable>()
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val bitmapSlicer = NinePicBitmapSlicer(3, 3)
    val cropImageLauncher = rememberLauncherForActivityResult(PhotoCropVisualMedia()) {
        val uri = it.data?.data ?: Uri.EMPTY
        if (uri != Uri.EMPTY) {
            scope.launch {
                isLoading.value = true
                // 开始转素描
                val bitmap = uri.adjustBitmapOrientation()
                if (bitmap != null) {
                    // 开始转素描
                    val result = bitmapSlicer.splitBitmap(bitmap)
                    result.forEach {
                        drawables.add(it.toDrawable(resources))
                    }
                }
                isLoading.value = false
            }
        }
    }
    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoVisualMedia()) {
        if (it != null) {
            val outputFile = "IMG_".createFile("jpg")
            val outputUri = Uri.fromFile(outputFile)
            cropImageLauncher.launch(
                PhotoCropVisualMediaRequestBuilder(it, outputUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(bitmapSlicer.widthRate, bitmapSlicer.heightRate)
                    .build()
            )
            selectedUri.value = it
        }
    }
    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(id = R.string.text_nine_grid_cut)
    ) {
        Column {
            Box(
                modifier = modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .weight(1f),
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(360.dp)
                        .height(360.dp),
                    columns = GridCells.Fixed(bitmapSlicer.columns),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(drawables) {
                        AsyncImage(
                            modifier = Modifier.aspectRatio(1f),
                            contentScale = ContentScale.Crop,
                            model = it,
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
                    selectPhotoLauncher.launch(PhotoVisualMediaRequest(PhotoVisualMedia.ImageOnly))
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
                        val folder = (file?.nameWithoutExtension ?: "NineCutPicture")
                        for ((index, item) in drawables.withIndex()) {
                            val outFileName = folder + "_" + (index + 1) + ".jpg"
                            contentResolver.saveBitmapToGallery(uri, folder, item.bitmap, outFileName, "")
                        }
                        showLoadingDialog.value = false
                    }
                }) {
                    ScaleText(text = stringResource(id = R.string.text_save_photo))
                }
            }
        }
    }
}