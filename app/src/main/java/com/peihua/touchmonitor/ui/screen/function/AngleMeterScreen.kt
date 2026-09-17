package com.peihua.touchmonitor.ui.screen.function

import android.Manifest
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CycleRulerView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.LocalToolColors
import com.peihua.touchmonitor.utils.isGrantedPermission
import com.peihua.touchmonitor.utils.rememberIntState
import com.peihua.touchmonitor.utils.showToast
import com.peihua8858.permissions.core.requestPermission

/**
 * 容器竖着时内容需要顺时针旋转的角度：Activity 不跟着转，用户把手机向左侧倒过来即可正着看。
 * 容器本身已是横向（横屏设备、大屏分屏）时不需要旋转。
 */
private const val CONTENT_ROTATION = 90f

@Composable
fun AngleMeterScreen(modifier: Modifier) {
    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_angle_meter)
    ) {
        val angle = rememberIntState(0)
        val toolColors = LocalToolColors.current
        var cameraEnabled by remember { mutableStateOf(false) }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
        ) {
            // 内容盒子始终以长边为宽（即量角器底边方向），容器竖着时再整体旋转 90°：
            // 旋转不改变布局尺寸，宽高互换后的包围盒正好铺满整屏
            val rotation = if (maxHeight > maxWidth) CONTENT_ROTATION else 0f
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredSize(
                        width = maxOf(maxWidth, maxHeight),
                        height = minOf(maxWidth, maxHeight)
                    )
                    .rotate(rotation)
            ) {
                if (cameraEnabled) {
                    CameraPreview(
                        modifier = Modifier.matchParentSize(),
                        targetRotation = if (rotation == 0f) {
                            Surface.ROTATION_0
                        } else {
                            Surface.ROTATION_90
                        },
                        onUnavailable = {
                            cameraEnabled = false
                            showToast(R.string.text_camera_unavailable)
                        }
                    )
                }
                CycleRulerView(
                    modifier = Modifier.fillMaxSize(),
                    angle = angle.intValue,
                    onAngleChange = { angle.intValue = it },
                    tickColor = toolColors.rulerTick,
                    backgroundColor = Color.Transparent,
                )
                CameraToggle(
                    enabled = cameraEnabled,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 24.dp, top = 16.dp),
                    onToggle = { cameraEnabled = it }
                )
            }
        }
    }
}

@Composable
private fun CameraToggle(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val accentLine = with(LocalDensity.current) { 3.dp.toPx() }
    Box(
        modifier = modifier
            .clickable {
                when {
                    enabled -> onToggle(false)
                    context.isGrantedPermission(Manifest.permission.CAMERA) -> onToggle(true)
                    else -> (context as? ComponentActivity)?.requestPermission(Manifest.permission.CAMERA) {
                        onGranted { onToggle(true) }
                        onDenied { showToast(R.string.text_camera_permission_tips) }
                        onShowRationale { showToast(R.string.text_camera_permission_tips) }
                    }
                }
            }
            .background(color = colorScheme.surfaceContainerHigh)
            .drawBehind {
                drawRect(
                    color = colorScheme.primary,
                    topLeft = Offset(0f, size.height - accentLine),
                    size = Size(size.width, accentLine)
                )
            }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(if (enabled) R.string.text_camera_on else R.string.text_camera_off),
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    targetRotation: Int,
    onUnavailable: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            // SurfaceView 走独立图层，父级的旋转变换会失效，这里必须用 TextureView 实现
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    DisposableEffect(previewView, lifecycleOwner, targetRotation) {
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val cameraProvider = runCatching { future.get() }.getOrNull()
            if (cameraProvider == null) {
                onUnavailable()
                return@addListener
            }
            provider = cameraProvider
            val preview = Preview.Builder()
                .setTargetRotation(targetRotation)
                .build()
                .apply { surfaceProvider = previewView.surfaceProvider }
            runCatching {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            }.onFailure { onUnavailable() }
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll() }
    }
    AndroidView(modifier = modifier, factory = { previewView })
}
