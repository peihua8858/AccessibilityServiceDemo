package com.peihua.touchmonitor.ui.screen.function.picture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.NavigationIcon2
import com.peihua.touchmonitor.ui.components.ZoomableImage
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun PhotoPreviewScreen(modifier: Modifier, photoPath: String) {
    Box(
        modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ZoomableImage(model = photoPath, modifier = Modifier.fillMaxSize())
        NavigationIcon2(
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.dp_16), start = dimensionResource(id = R.dimen.dp_16))
                .size(dimensionResource(id = R.dimen.dp_24))
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8))
                )
                .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                .align(Alignment.TopStart),
            tintColor = Color.White
        ) {
            popBackStack()
        }
    }
}