package com.peihua.touchmonitor.ui.screen.function.images


import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
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
import com.peihua.compose.file.createFileName
import com.peihua.compose.utils.adjustBitmapOrientation
import com.peihua.compose.utils.rememberState
import com.peihua.compose.utils.saveBitmapToGallery
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CustomSliderTips
import com.peihua.touchmonitor.ui.components.SliderDefaults
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.rememberFloatState
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 图片像素化
 */
@Composable
fun ImagePixelizationScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val selectedUri = rememberState<Uri>(Uri.EMPTY)
    val showLoadingDialog = rememberShowProgressDialog()
    val sketchBitmapDrawable = rememberState<BitmapDrawable?>(null)
    val pixelState = rememberFloatState(12f)
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val processPhoto ={uri:Uri->
        isLoading.value = true
        // 开始转素描
        val bitmap = uri.adjustBitmapOrientation()
        if (bitmap != null) {
            // 开始转素描
            val pixelationFilter = GPUImagePixelationFilter()
            pixelationFilter.setPixel(pixelState.floatValue)
            val gpuImage = GPUImage(context)
            gpuImage.setImage(bitmap)
            gpuImage.setFilter(pixelationFilter)
            val sketchBitmap = gpuImage.bitmapWithFilterApplied
            sketchBitmapDrawable.value = sketchBitmap.toDrawable(resources)
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
    LaunchedEffect(pixelState.floatValue) {
        if (selectedUri.value != Uri.EMPTY) {
            processPhoto(selectedUri.value)
        }
    }
    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(id = R.string.text_image_pixelization)
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
            CustomSliderTips(
                modifier = Modifier.padding(16.dp),
                title = stringResource(id = R.string.text_pixel_size),
                value = pixelState.floatValue,
                valueRange = 12f..40f,
                colors = SliderDefaults.colors().copy(
                    inactiveTickColor = MaterialTheme.colorScheme.secondaryContainer,
                    activeTickColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
                steps = 28,
                thumbText = {
                    it.roundToInt().toString()
                }

            ) {
                pixelState.floatValue = it.roundToInt().toFloat()
            }
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
                            val outFileName = "pixel_".createFileName("jpg")
                            contentResolver.saveBitmapToGallery(it.bitmap, outFileName, "")
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