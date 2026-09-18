package com.peihua.touchmonitor.ui.screen.function

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.ProgressDialog
import com.peihua.touchmonitor.utils.rememberColorSaveable
import com.peihua.touchmonitor.utils.rememberState
import com.peihua.touchmonitor.utils.saveBitmapToGallery
import com.peihua.touchmonitor.utils.showToast
import com.peihua.touchmonitor.utils.toHex
import com.peihua8858.tools.file.createFileName
import com.peihua8858.tools.utils.dLog
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BoardColor = Color.White
private val StrokeWidths = listOf(2.dp, 6.dp, 12.dp, 20.dp, 32.dp)
private const val GalleryFolderName = "SimplePaint"

private enum class PaintTool { Pen, Eraser }

private class PaintStroke(val points: List<Offset>, val color: Color, val width: Dp) {
    val path: Path by lazy(LazyThreadSafetyMode.NONE) { points.toSmoothPath() }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimplePaintScreen(modifier: Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val captureController = rememberCaptureController()

    val strokes = remember { mutableStateOf(emptyList<PaintStroke>()) }
    val undoStack = remember { mutableStateListOf<List<PaintStroke>>() }
    val redoStack = remember { mutableStateListOf<List<PaintStroke>>() }
    val drawingPoints = remember { mutableStateListOf<Offset>() }
    val tool = rememberState(PaintTool.Pen)
    val penColor = rememberColorSaveable(Color.Black)
    val penWidth = rememberState(StrokeWidths[1])
    val eraserWidth = rememberState(StrokeWidths[3])
    val showWidthMenu = rememberState(false)
    val isSaving = rememberState(false)

    val activeWidth = if (tool.value == PaintTool.Pen) penWidth else eraserWidth
    val activeColor = if (tool.value == PaintTool.Pen) penColor.value else BoardColor

    fun commit(next: List<PaintStroke>) {
        undoStack.add(strokes.value)
        redoStack.clear()
        strokes.value = next
    }

    Toolbar(
        modifier = modifier.fillMaxSize(),
        navigateUp = {
            popBackStack()
        },
        title = stringResource(R.string.text_simple_paint),
        actions = {
            Box {
                IconButton(onClick = { showWidthMenu.value = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stroke_width_24),
                        contentDescription = stringResource(R.string.text_paint_stroke_width)
                    )
                }
                DropdownMenu(
                    expanded = showWidthMenu.value,
                    onDismissRequest = { showWidthMenu.value = false }) {
                    StrokeWidths.forEach { width ->
                        DropdownMenuItem(
                            text = {
                                Box(
                                    modifier = Modifier
                                        .size(if (width < 4.dp) 4.dp else width)
                                        .clip(CircleShape)
                                        .background(if (tool.value == PaintTool.Pen) penColor.value else colorScheme.outline)
                                )
                            },
                            trailingIcon = {
                                if (width == activeWidth.value) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            onClick = {
                                activeWidth.value = width
                                showWidthMenu.value = false
                            })
                    }
                }
            }
            IconButton(onClick = {
                navigateTo2(
                    Dialog.ColorPickerDialog.route,
                    Dialog.TITLE to R.string.text_paint_color,
                    Dialog.ColorPickerDialog.DEFAULT_COLOR to penColor.value.toHex(),
                    Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                        penColor.value = color
                        tool.value = PaintTool.Pen
                        popBackStack()
                    }),
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = stringResource(R.string.text_paint_color)
                )
            }
            IconButton(onClick = {
                if (strokes.value.isEmpty()) {
                    showToast(R.string.text_paint_empty)
                } else {
                    scope.launch {
                        isSaving.value = true
                        val uri = try {
                            val bitmap = captureController.captureAsync().await().asAndroidBitmap()
                            withContext(Dispatchers.IO) {
                                context.contentResolver.saveBitmapToGallery(
                                    folderName = GalleryFolderName,
                                    source = bitmap,
                                    title = "Paint_".createFileName("jpg"),
                                    description = ""
                                )
                            }
                        } catch (error: Throwable) {
                            dLog { "save paint board failed: $error" }
                            null
                        }
                        isSaving.value = false
                        showToast(if (uri != null) R.string.text_save_success else R.string.text_save_fail)
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.text_save)
                )
            }
        }) {
        Column(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .capturable(captureController)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        drawingPoints.clear()
                        drawingPoints.add(down.position)
                        val color = if (tool.value == PaintTool.Pen) penColor.value else BoardColor
                        val width =
                            if (tool.value == PaintTool.Pen) penWidth.value else eraserWidth.value
                        while (true) {
                            val change = awaitPointerEvent().changes
                                .firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            drawingPoints.add(change.position)
                        }
                        commit(strokes.value + PaintStroke(drawingPoints.toList(), color, width))
                        drawingPoints.clear()
                    }
                }) {
                drawRect(color = BoardColor)
                strokes.value.forEach { drawStroke(it) }
                if (drawingPoints.isNotEmpty()) {
                    drawStroke(PaintStroke(drawingPoints.toList(), activeColor, activeWidth.value))
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = undoStack.isNotEmpty(),
                    onClick = {
                        redoStack.add(strokes.value)
                        strokes.value = undoStack.removeAt(undoStack.lastIndex)
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(R.string.text_paint_undo)
                    )
                }
                IconButton(
                    enabled = redoStack.isNotEmpty(),
                    onClick = {
                        undoStack.add(strokes.value)
                        strokes.value = redoStack.removeAt(redoStack.lastIndex)
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = stringResource(R.string.text_paint_redo)
                    )
                }
                ToolIconButton(
                    selected = tool.value == PaintTool.Pen,
                    onClick = { tool.value = PaintTool.Pen }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.text_paint_pen)
                    )
                }
                ToolIconButton(
                    selected = tool.value == PaintTool.Eraser,
                    onClick = { tool.value = PaintTool.Eraser }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_eraser_24),
                        contentDescription = stringResource(R.string.text_paint_eraser)
                    )
                }
                IconButton(onClick = {
                    if (strokes.value.isNotEmpty()) {
                        commit(emptyList())
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.text_clear)
                    )
                }
            }
        }
        if (isSaving.value) {
            ProgressDialog()
        }
    }
}

@Composable
private fun ToolIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent),
        content = content
    )
}

private fun DrawScope.drawStroke(stroke: PaintStroke) {
    val width = stroke.width.toPx()
    if (stroke.points.size < 2) {
        drawCircle(stroke.color, width / 2f, stroke.points.first())
        return
    }
    drawPath(
        path = stroke.path,
        color = stroke.color,
        style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/** 相邻采样点之间用二次贝塞尔过渡，否则手指快速滑动时折线会明显发棱 */
private fun List<Offset>.toSmoothPath(): Path {
    val path = Path()
    val first = firstOrNull() ?: return path
    path.moveTo(first.x, first.y)
    for (index in 1 until size) {
        val previous = this[index - 1]
        val current = this[index]
        path.quadraticTo(
            previous.x,
            previous.y,
            (previous.x + current.x) / 2f,
            (previous.y + current.y) / 2f
        )
    }
    val last = last()
    path.lineTo(last.x, last.y)
    return path
}
