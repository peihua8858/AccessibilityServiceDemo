package com.peihua.touchmonitor.ui.screen.function.picture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.NavigationIcon2
import com.peihua.touchmonitor.ui.components.ZoomableImage
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun PhotoPreviewScreen(modifier: Modifier, photoPath: String) {
    Scaffold(
        modifier = modifier
    ) {
        Box(
            modifier.padding(it).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ZoomableImage(model = photoPath, modifier = Modifier.fillMaxSize())
            NavigationIcon2(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp)
                    .size(24.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(shape = RoundedCornerShape(8.dp))
                    .align(Alignment.TopStart),
                tintColor = Color.White
            ) {
                popBackStack()
            }
        }
    }
}