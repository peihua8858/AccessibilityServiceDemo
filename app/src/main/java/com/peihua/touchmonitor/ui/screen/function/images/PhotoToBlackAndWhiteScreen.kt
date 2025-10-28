package com.peihua.touchmonitor.ui.screen.function.images


import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.compose.file.cacheFile
import com.peihua.compose.file.createFileName
import com.peihua.compose.file.writeBitmapToFile
import com.peihua.compose.utils.adjustBitmapOrientation
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.decodePathOptionsFile
import com.peihua.compose.utils.saveBitmapToGallery
import com.peihua.compose.utils.toBlackAndWhite
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.rememberSaveable
import kotlinx.coroutines.launch

@Composable
fun PhotoToBlackAndWhiteScreen(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val resources = LocalResources.current
    val logoPath = rememberSaveable(Uri.EMPTY)
    val logoDrawable = remember { mutableStateOf<Drawable?>(null) }
    val painter = rememberDrawablePainter(logoDrawable.value)
    val scope = rememberCoroutineScope()
    LaunchedEffect(logoPath.value) {
        dLog { "logoPath:${logoPath.value}" }
        if (logoPath.value != Uri.EMPTY) {
//            val drawable = logoPath.value.adjustBitmapOrientation()
//                ?.toDrawable(resources)
//            logoDrawable.value = drawable
        }
        dLog { "logoPath:${logoDrawable.value}" }
    }
    val launcher = rememberLauncherForActivityResult(PhotoVisualMedia()) {
        if (it != null) {
            scope.launch {
                val bitmap = it.adjustBitmapOrientation()?.toBlackAndWhite()
                logoDrawable.value = bitmap?.toDrawable(resources)
                val imageFile = bitmap.writeBitmapToFile(context.cacheFile("files"))
                logoPath.value = imageFile?.toUri()
                dLog { "logoPath:${imageFile?.absolutePath}" }
            }
        }
    }
    Toolbar(
        modifier,
        title = stringResource(id = R.string.text_photo_to_black_and_white),
        navigateUp = { popBackStack() }) {

        Column(Modifier.fillMaxSize()) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                model = logoPath.value,
//                painter = painter,
                contentDescription = null
            )
//            Image(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                painter = painter,
//                contentDescription = null
//            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.dp_56)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors()
                        .copy(contentColor = colorScheme.secondary),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                    onClick = {
                        launcher.launch(PhotoVisualMediaRequest(PhotoVisualMedia.ImageOnly))
                    }
                ) {
                    Text(text = stringResource(id = R.string.text_select_photo))
                }
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                    onClick = {
                        scope.launch {
                            try {
                                val outFileName = "QR_".createFileName("jpg")
                                val contentResolver = context.contentResolver
                                val bitmap = logoPath.value.decodePathOptionsFile(Int.MAX_VALUE,Int.MAX_VALUE)?:return@launch
                                contentResolver.saveBitmapToGallery(bitmap, outFileName, "")
                            } catch (error: Throwable) {
                                error.printStackTrace()
                            }
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.text_save_photo))
                }
            }
        }
    }
}