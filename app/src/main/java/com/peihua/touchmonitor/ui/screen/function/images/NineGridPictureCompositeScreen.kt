package com.peihua.touchmonitor.ui.screen.function.images

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.peihua.dragswap.DragMode
import com.peihua.dragswap.DragSwapItem
import com.peihua.dragswap.applyDragMove
import com.peihua.dragswap.dragSwapContainer
import com.peihua.dragswap.rememberDragSwapGridState
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
import com.peihua.touchmonitor.utils.rememberSaveableList
import com.peihua8858.compose.tools.rememberState
import com.peihua8858.tools.collections.toArrayList
import com.peihua8858.tools.file.createFileName
import com.peihua8858.tools.utils.adjustBitmapOrientation
import com.peihua8858.tools.utils.createFolderFile
import com.peihua8858.tools.utils.getParcelableArrayListExtraCompat
import com.peihua8858.tools.utils.saveBitmapToGallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * 九宫格图片合成
 */
@Composable
fun NineGridPictureCompositeScreen(modifier: Modifier = Modifier) {
    var isLoading = rememberState(false)
    val selectedUris = rememberSaveableList<Uri>()
    val showLoadingDialog = rememberShowProgressDialog()
    val drawables = rememberSaveableList<BitmapDrawable>()
    val gapState = rememberState("2")
    val columnsState = rememberState("3")
    val rowsState = rememberState("3")
    val bitmapSlicer = NinePicBitmapSlicer(3, 3)
    bitmapSlicer.gap = gapState.value.ifEmpty { "2" }.toInt()
    bitmapSlicer.columns = columnsState.value.ifEmpty { "3" }.toInt()
    bitmapSlicer.rows = rowsState.value.ifEmpty { "3" }.toInt()
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope { Dispatchers.IO }

    val cropImageLauncher = rememberLauncherForActivityResult(PhotoCropVisualMedia()) {
        val intent = it.data
        if (intent != null) {
            val uris = intent.getParcelableArrayListExtraCompat<Uri>(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            selectedUris.addAll(uris)
            scope.launch {
                val bitmaps = arrayListOf<Bitmap>()
                uris.forEach {
                    val result = it.adjustBitmapOrientation()
                    if (result != null) {
                        drawables.add(result.toDrawable(resources))
                        bitmaps.add(result)
                    }
                }
            }
        }
    }
    val selectPhotoLauncher = rememberLauncherForActivityResult(PhotoMultipleVisualMedia()) {
        if (it.isNotEmpty()) {
            val outputFile = "IMG_".createFolderFile()
            val outputUri = Uri.fromFile(outputFile)
            cropImageLauncher.launch(
                PhotoCropVisualMediaRequestBuilder(it.toArrayList(), outputUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(bitmapSlicer.widthRate, bitmapSlicer.heightRate)
                    .build()
            )
        }
    }
    val gridState = rememberLazyGridState()
    val dragState = rememberDragSwapGridState(gridState, DragMode.Swap) { from, to ->
        drawables.applyDragMove(DragMode.Swap, from, to)
    }
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
                    state = gridState,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(360.dp)
                        .height(360.dp)
                        .dragSwapContainer(dragState),
                    columns = GridCells.Fixed(bitmapSlicer.columns),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(drawables, { it.hashCode() }) { item ->
                        DragSwapItem(
                            state = dragState, key = item.hashCode(),
                            modifier = Modifier.size(120.dp)
                        ) { _, isTarget ->
                            AsyncImage(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .then(
                                        if (isTarget) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                                        } else {
                                            Modifier
                                        }
                                    ),
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
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = rowsState.value.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { ScaleText(text = stringResource(id = R.string.text_rows)) },
                    onValueChange = {
                        if (it.isEmpty()) {
                            rowsState.value = ""
                            return@OutlinedTextField
                        }
                        if (it.toInt() < 1) {
                            rowsState.value = "1"
                        } else if (it.toInt() > 9) {
                            rowsState.value = "9"
                        } else {
                            rowsState.value = it
                        }
                    })
                OutlinedTextField(
                    modifier = Modifier.padding(start = 4.dp),
                    value = columnsState.value.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { ScaleText(text = stringResource(id = R.string.text_columns)) },
                    onValueChange = {
                        if (it.isEmpty()) {
                            columnsState.value = ""
                            return@OutlinedTextField
                        }
                        if (it.toInt() < 1) {
                            columnsState.value = "1"
                        } else if (it.toInt() > 9) {
                            columnsState.value = "9"
                        } else {
                            columnsState.value = it
                        }
                    })
                OutlinedTextField(
                    modifier = Modifier.padding(start = 4.dp),
                    value = gapState.value.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { ScaleText(text = stringResource(id = R.string.text_gap)) },
                    onValueChange = {
                        if (it.isEmpty()) {
                            gapState.value = ""
                            return@OutlinedTextField
                        }
                        if (it.toInt() > 50) {
                            gapState.value = "50"
                        } else if (it.toInt() < 0) {
                            gapState.value = "0"
                        } else {
                            gapState.value = it
                        }
                    })
            }
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
                            .setForceCustomUi(true)
                            .build()
                    )
                }) {
                    ScaleText(text = stringResource(id = R.string.text_select_photo))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(modifier = Modifier.weight(1f), onClick = {
                    scope.launch {
                        showLoadingDialog.value = true
                        val bitmap = bitmapSlicer.mergeBitmaps(drawables.map { it.bitmap }, bitmapSlicer.widthRate, bitmapSlicer.heightRate)
                        val contentResolver = context.contentResolver
                        val outFileName = "nineComp_".createFileName("jpg")
                        contentResolver.saveBitmapToGallery(
                            source = bitmap,
                            title = outFileName,
                            description = ""
                        )
                        showLoadingDialog.value = false
                    }
                }) {
                    ScaleText(text = stringResource(id = R.string.text_save_photo))
                }
            }
        }
    }
}