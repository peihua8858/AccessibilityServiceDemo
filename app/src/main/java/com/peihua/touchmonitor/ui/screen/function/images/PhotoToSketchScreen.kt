package com.peihua.touchmonitor.ui.screen.function.images


import android.graphics.drawable.BitmapDrawable
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
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua8858.compose.tools.rememberState
import com.peihua8858.tools.file.createFileName
import com.peihua8858.tools.utils.adjustBitmapOrientation
import com.peihua8858.tools.utils.saveBitmapToGallery
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 图片转素描
 */
@Composable
fun PhotoToSketchScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val showLoadingDialog = rememberShowProgressDialog()
    val sketchBitmapDrawable = rememberSaveable<BitmapDrawable?>(null)
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoVisualMedia()) {
        if (it != null) {
            scope.launch {
                isLoading.value = true
                // 开始转素描
                val bitmap = it.adjustBitmapOrientation()
                if (bitmap != null) {
                    // 开始转素描
                    val gpuImage = GPUImage(context)
                    gpuImage.setImage(bitmap)
                    gpuImage.setFilter(GPUImageSketchFilter())
                    val sketchBitmap = gpuImage.bitmapWithFilterApplied
                    sketchBitmapDrawable.value = sketchBitmap.toDrawable(resources)
                }
                isLoading.value = false
            }
        }
    }
    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(id = R.string.text_photo_to_sketch)
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
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Inside,
                    model = sketchBitmapDrawable.value,
                    contentDescription = "",
                )
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
                        sketchBitmapDrawable.value?.let {
                            val contentResolver = context.contentResolver
                            val outFileName = "Sketch_".createFileName("jpg")
                            contentResolver.saveBitmapToGallery(
                                source = it.bitmap,
                                title = outFileName,
                                description = ""
                            )
                        }
                        delay(3000)
                        showLoadingDialog.value = false
                    }
                }) {
                    ScaleText(text = stringResource(id = R.string.text_save_photo))
                }
            }
        }
    }
}