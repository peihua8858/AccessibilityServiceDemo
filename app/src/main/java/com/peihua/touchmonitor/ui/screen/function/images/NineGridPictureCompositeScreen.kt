package com.peihua.touchmonitor.ui.screen.function.images

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.peihua.compose.collections.toArrayList
import com.peihua.compose.file.createFileName
import com.peihua.compose.utils.adjustBitmapOrientation
import com.peihua.compose.utils.createFolderFile
import com.peihua.compose.utils.getParcelableArrayListExtraCompat
import com.peihua.compose.utils.rememberState
import com.peihua.compose.utils.saveBitmapToGallery
import com.peihua.selector.result.PhotoCropVisualMediaRequestBuilder
import com.peihua.selector.result.PhotoVisualMediaRequestBuilder
import com.peihua.selector.result.contract.PhotoCropVisualMedia
import com.peihua.selector.result.contract.PhotoMultipleVisualMedia
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.bitmap.NinePicBitmapSlicer
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.rememberSaveableList
import com.peihua.touchmonitor.utils.toDp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

/**
 * 九宫格图片合成
 */
@Composable
fun NineGridPictureCompositeScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val selectedUris = rememberSaveableList<Uri>()
    val showLoadingDialog = rememberShowProgressDialog()
    val drawables = rememberSaveableList<BitmapDrawable>()
    val sketchBitmapDrawable = rememberSaveable<BitmapDrawable?>(null)
    val bitmapSlicer = NinePicBitmapSlicer(3, 3)
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }

    val cropImageLauncher = rememberLauncherForActivityResult(PhotoCropVisualMedia()) {
        val intent = it.data
        if (intent != null) {
            val uris = intent.getParcelableArrayListExtraCompat<Uri>(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            selectedUris.addAll(uris)
            scope.launch {
                val bitmaps =arrayListOf<Bitmap>()
                uris.forEach {
                    val result = it.adjustBitmapOrientation()
                    if (result != null) {
                        drawables.add(result.toDrawable(resources))
                        bitmaps.add(result)
                    }
                }
//                val bitmap = bitmapSlicer.mergeBitmaps(bitmaps, 2048, 2048)
//                sketchBitmapDrawable.value = bitmap.toDrawable(resources)
            }
        }
    }
    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoMultipleVisualMedia()) {
        val outputFile = "IMG_".createFolderFile()
        val outputUri = Uri.fromFile(outputFile)
        cropImageLauncher.launch(
            PhotoCropVisualMediaRequestBuilder(it.toArrayList(), outputUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1024, 1024)
                .build()
        )
    }
    val state = rememberReorderableLazyGridState(onMove = { from, to ->
        drawables.apply {
            add(to.index, removeAt(from.index))
        }
    })
    Toolbar(
        modifier = modifier,
        navigateUp = {
            popBackStack()
        },
        title = stringResource(id = R.string.text_nine_grid_picture_composite)
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
                    state = state.gridState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(bitmapSlicer.widthRate.toDp)
                        .height(bitmapSlicer.heightRate.toDp)
                        .reorderable(state)
                        .detectReorderAfterLongPress(state),
                    columns = GridCells.Fixed(bitmapSlicer.columns),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(drawables,{it.hashCode()}) {item->
                        ReorderableItem(state= state, key = item.hashCode(),
                            modifier = Modifier.animateItem()) { isDragging->
                            AsyncImage(
                                modifier = Modifier.aspectRatio(1f),
                                contentScale = ContentScale.Crop,
                                model = item,
                                contentDescription = "",
                            )
                        }

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
                    selectPhotoLauncher.launch(
                        PhotoVisualMediaRequestBuilder(PhotoVisualMedia.ImageOnly)
                            .setMaxItemCount(bitmapSlicer.columns * bitmapSlicer.rows)
                            .setSelectedUris(selectedUris.toArrayList())
                            .build()
                    )
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
                        showLoadingDialog.value = false
                    }
                }) {
                    ScaleText(text = stringResource(id = R.string.text_save_photo))
                }
            }
        }
    }
}