package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import coil3.compose.AsyncImage
import kotlin.math.absoluteValue

@Composable
fun ZoomableImage(model: Any, modifier: Modifier) {
    var scale by remember { mutableFloatStateOf(1f) } // 当前缩放比例
    var offsetX by remember { mutableFloatStateOf(0f) } // 水平偏移
    var offsetY by remember { mutableFloatStateOf(0f) } // 垂直偏移
    var isScaling by remember { mutableStateOf(false) }
    val maxScale = 3f    // 最大缩放比例
    val minScale = 1f    // 最小缩放比例

    Box(modifier = modifier) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput("transform") {
                    // 双指缩放
                    detectTransformGestures { _, pan, zoom, _ ->
                        // 这段代码不会和 detectTapGestures 冲突
                        if (zoom != 1f) {
                            isScaling = true
                            scale = (scale * zoom).coerceIn(minScale, maxScale) // 更新缩放比例
                            offsetX += pan.x // 更新水平偏移
                            offsetY += pan.y // 更新垂直偏移
                        } else if (isScaling) {
                            isScaling = false // 手指抬起时重置
                        }
                    }
                }
                .pointerInput("doubleTap") {
                    // 处理双击缩放和双指缩放
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale < maxScale) {
                                scale * 2f // 双击放大
                            } else {
                                minScale // 如果已经放大到最大，重置为最小
                            }
                        }
                    )
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}



@Composable
fun ZoomableImage2(model: Any, modifier: Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imgSize: Size = Size.Unspecified
    val maxScale = 3f    // 最大缩放比例
    val minScale = 1f    // 最小缩放比例

    Box(modifier = modifier) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    imgSize = size
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    // 页面切换时需要恢复scale、offset
//                    if (pagerScope != null) {
//                        val pageOffset =
//                            pagerScope.calculateCurrentOffsetForPage(page = page).absoluteValue
//                        if (pageOffset == 1f) {
//                            scale = 1f
//                            offset = Offset.Zero
//                        }
//                    }
                }
                .pointerInput("transform") {
                    // 此处为自定义手势检测，主要是增加返回值判断是否需要消费
                    detectTransformGestures { _, pan, zoom, _ ->
                        var requestIgnoreConsume = false
                        scale = (zoom * scale).coerceAtLeast(1f)
                        scale = if (scale > 5f) 5f else scale
                        offset = calOffset(imgSize, scale, offset + pan) {
                            requestIgnoreConsume = it
                        }
//                        return@detectTransformGestures requestIgnoreConsume
                    }
                }
                .pointerInput("tap") {
                    detectTapGestures(
                        onDoubleTap = { point ->
                            val center = Offset(imgSize.width / 2, imgSize.height / 2)
                            val realPoint = offset + center - point
                            scale = if (scale <= 1f) 2f else 1f
                            offset = if (scale <= 1f) Offset.Zero else {
                                calOffset(imgSize, scale, realPoint * 2f)
                            }
                        }
                    )
                }
        )
    }
}


private fun calOffset(
    imgSize: Size,
    scale: Float,
    offsetChanged: Offset,
    isInvalid: (Boolean) -> Unit = {}
): Offset {
    if (imgSize == Size.Unspecified) return Offset.Zero
    val px = imgSize.width * (scale - 1f) / 2f
    val py = imgSize.height * (scale - 1f) / 2f
    var np = offsetChanged
    val xDiff = np.x.absoluteValue - px
    val yDiff = np.y.absoluteValue - py
    if (xDiff > 0)
        np = np.copy(x = px * np.x.absoluteValue / np.x)
    if (yDiff > 0)
        np = np.copy(y = py * np.y.absoluteValue / np.y)
    isInvalid(xDiff > 0 && xDiff > yDiff)
    return np
}