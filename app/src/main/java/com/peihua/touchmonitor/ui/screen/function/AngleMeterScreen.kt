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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CycleRulerView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.isGrantedPermission
import com.peihua.touchmonitor.utils.rememberIntState
import com.peihua.touchmonitor.utils.showToast
import com.peihua8858.permissions.core.requestPermission

/**
 * 内容顺时针旋转的角度：Activity 保持竖屏不动，量角器底边落在屏幕最左侧，
 * 用户把手机向左侧倒过来即可正着看到横屏量角器。
 */
private const val CONTENT_ROTATION = 90f

/** 内容旋转 90° 后，相机取景需要按同样的量补偿，等价于系统未锁定方向时的 ROTATION_90 */
private const val PREVIEW_ROTATION = Surface.ROTATION_90

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
        var cameraEnabled by remember { mutableStateOf(false) }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black)
        ) {
            // 旋转不改变布局尺寸，所以按宽高互换来量：旋转后的包围盒正好铺满整屏
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredSize(width = maxHeight, height = maxWidth)
                    .rotate(CONTENT_ROTATION)
            ) {
                if (cameraEnabled) {
                    CameraPreview(
                        modifier = Modifier.matchParentSize(),
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
            .background(color = Color(0x99FFFFFF))
            .drawBehind {
                drawRect(
                    color = Color(0xFF3C7FEC),
                    topLeft = Offset(0f, size.height - accentLine),
                    size = Size(size.width, accentLine)
                )
            }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(if (enabled) R.string.text_camera_on else R.string.text_camera_off),
            color = Color.White,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun CameraPreview(modifier: Modifier = Modifier, onUnavailable: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            // SurfaceView 走独立图层，父级的旋转变换会失效，这里必须用 TextureView 实现
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    DisposableEffect(previewView, lifecycleOwner) {
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
                .setTargetRotation(PREVIEW_ROTATION)
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
