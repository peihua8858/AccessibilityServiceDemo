package com.peihua.touchmonitor.ui.screen.function.images


import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.peihua.selector.result.PhotoVisualMediaRequest
import com.peihua.selector.result.contract.PhotoVisualMedia
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.CustomSliderTips
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.rememberShowProgressDialog
import com.peihua.touchmonitor.utils.rememberFloatState
import com.peihua.touchmonitor.utils.rememberSaveable
import com.peihua.touchmonitor.utils.rememberState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * 视频转gif
 */
@Composable
fun VideoToGifScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedUri = rememberSaveable<Uri>(Uri.EMPTY)
    val inputFile = rememberState<File?>(null)
    val outputGif = rememberState<File?>(null)
    val converting = rememberState(false)
    val showLoadingDialog = rememberShowProgressDialog()
    val fps = rememberFloatState(12f)
    val width = rememberFloatState(480f)
    val startTime = rememberFloatState(0f)
    val duration = rememberFloatState(3f)

    val selectVideoLauncher = rememberLauncherForActivityResult(PhotoVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri.value = uri
            outputGif.value = null
            scope.launch(Dispatchers.IO) {
                val cacheFile = File(context.cacheDir, "video_input_${System.currentTimeMillis()}.mp4")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        cacheFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    inputFile.value = cacheFile
                } catch (e: Exception) {
                    cacheFile.delete()
                }
            }
        }
    }
    fun convert() {
        val input = inputFile.value ?: return
        if (converting.value) return
        val outFile = File(context.cacheDir, "video_to_gif_${System.currentTimeMillis()}.gif")
        converting.value = true
        showLoadingDialog.value = true
        FFmpegKit.executeWithArgumentsAsync(
            buildConvertArgs(input, outFile, fps.floatValue, width.floatValue, startTime.floatValue, duration.floatValue),
            { completed ->
                converting.value = false
                showLoadingDialog.value = false
                val ok = completed.returnCode.isValueSuccess
                if (ok) outputGif.value = outFile
            },
            null,
            null,
        )
    }

    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_video_to_gif)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = {
                    selectVideoLauncher.launch(PhotoVisualMediaRequest(PhotoVisualMedia.VideoOnly))
                }
            ) {
                Text(stringResource(R.string.text_select_video))
            }

            if (inputFile.value != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f).height(48.dp),
                        onClick = { convert() },
                        enabled = !converting.value
                    ) {
                        Text(stringResource(R.string.text_convert_video_to_gif))
                    }
                }
            }

            if (inputFile.value != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(stringResource(R.string.text_gif_frame_rate), style = MaterialTheme.typography.titleMedium)
                CustomSliderTips(
                    modifier = Modifier.fillMaxWidth(),
                    value = fps.floatValue,
                    title = "${stringResource(R.string.text_gif_frame_rate)}: ${fps.floatValue.roundToInt()}",
                    steps = 28,
                    thumbText = { "${it.roundToInt()} fps" },
                    valueRange = 1f..30f,
                    onChangValue = { fps.floatValue = it }
                )
                Text(stringResource(R.string.text_gif_width), style = MaterialTheme.typography.titleMedium)
                CustomSliderTips(
                    modifier = Modifier.fillMaxWidth(),
                    value = width.floatValue,
                    title = "${stringResource(R.string.text_gif_width)}: ${width.floatValue.roundToInt()}",
                    steps = 47,
                    thumbText = { "${it.roundToInt()} px" },
                    valueRange = 120f..1080f,
                    onChangValue = { width.floatValue = it }
                )
                Text(stringResource(R.string.text_gif_start_time), style = MaterialTheme.typography.titleMedium)
                CustomSliderTips(
                    modifier = Modifier.fillMaxWidth(),
                    value = startTime.floatValue,
                    title = "${stringResource(R.string.text_gif_start_time)}: ${startTime.floatValue.roundToInt()}",
                    steps = 120,
                    thumbText = { "${it.roundToInt()} s" },
                    valueRange = 0f..120f,
                    onChangValue = { startTime.floatValue = it }
                )
                Text(stringResource(R.string.text_gif_duration), style = MaterialTheme.typography.titleMedium)
                CustomSliderTips(
                    modifier = Modifier.fillMaxWidth(),
                    value = duration.floatValue,
                    title = "${stringResource(R.string.text_gif_duration)}: ${duration.floatValue.roundToInt()}",
                    steps = 29,
                    thumbText = { "${it.roundToInt()} s" },
                    valueRange = 1f..30f,
                    onChangValue = { duration.floatValue = it }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            if (outputGif.value != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = outputGif.value,
                        contentDescription = stringResource(R.string.text_video_to_gif),
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    onClick = {
                        outputGif.value?.let {
                            saveGifToGallery(context, it)
                        }
                    }
                ) {
                    Text(stringResource(R.string.text_save))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun buildConvertArgs(
    input: File,
    output: File,
    fps: Float,
    width: Float,
    start: Float,
    duration: Float,
): Array<String> =
    buildList {
        // -y 必须有：否则输出已存在时 ffmpeg 会等待交互式确认导致挂住
        add("-y")
        add("-ss"); add(formatNumber(start))
        add("-t"); add(formatNumber(duration))
        add("-i"); add(input.absolutePath)
        add("-vf"); add("fps=${formatNumber(fps)},scale=${width.roundToInt()}:-1:flags=lanczos")
        add(output.absolutePath)
    }.toTypedArray()

private fun formatNumber(v: Float): String =
    if (v % 1f == 0f) v.toInt().toString() else v.toString()

private fun saveGifToGallery(context: android.content.Context, gifFile: File) {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.DISPLAY_NAME, "video_gif_${System.currentTimeMillis()}.gif")
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TouchTools")
        values.put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        try {
            context.contentResolver.openOutputStream(it)?.use { os ->
                gifFile.inputStream().use { ins -> ins.copyTo(os) }
            }
        } catch (e: Exception) {
            context.contentResolver.delete(it, null, null)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(it, values, null, null)
        }
    }
}
