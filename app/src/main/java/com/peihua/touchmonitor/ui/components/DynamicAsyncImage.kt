package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.theme.LocalTintTheme
import com.peihua.touchmonitor.utils.dLog

/**
 * A wrapper around [AsyncImage] which determines the colorFilter based on the theme
 */
@Composable
fun DynamicAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter = painterResource(id = R.drawable.c_placeholder_default),
) {
    val iconTint = LocalTintTheme.current.iconTint
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        val onState = { state: AsyncImagePainter.State ->
            dLog { "onState: $state" }
            when (state) {
                is AsyncImagePainter.State.Success -> isLoading = false
                is AsyncImagePainter.State.Error ->{
                    isError = true
                    state.result.throwable.printStackTrace()
                }
                is AsyncImagePainter.State.Loading -> isLoading = true
                is AsyncImagePainter.State.Empty -> isLoading = false
            }
        }
        val loadPainter = rememberAsyncImagePainter(model, onState = onState)
        Image(
            contentScale = ContentScale.Crop,
            painter = if (isError.not()) loadPainter else placeholder,
            contentDescription = contentDescription,
            colorFilter = if (iconTint != Unspecified) ColorFilter.tint(iconTint) else null,
        )
    }
}