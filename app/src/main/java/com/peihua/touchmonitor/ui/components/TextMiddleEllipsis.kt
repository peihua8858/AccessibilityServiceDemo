import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private const val ELLIPSIS = "..."

@Composable
fun TextMiddleEllipsis(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    maxLines: Int = 1,
    color: Color = Color.Unspecified,
    textAlign: TextAlign = TextAlign.Unspecified
) {
    val density = LocalDensity.current
    val textStyle = LocalTextStyle.current.merge(
        TextStyle(
            fontSize = fontSize,
            color = color,
            textAlign = textAlign
        )
    )

    // 状态管理
    var truncatedText by remember(text) { mutableStateOf(text) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var isTruncated by remember { mutableStateOf(false) }

    // 计算最大宽度（像素）
    val maxWidthPx = remember(maxWidth) {
        if (maxWidth == Dp.Unspecified) Float.POSITIVE_INFINITY
        else with(density) { maxWidth.toPx() }
    }

    // 文本布局回调
    val onTextLayout = { result: TextLayoutResult ->
        layoutResult = result

        // 检查文本是否超出可用宽度
        val hasOverflow = result.size.width > maxWidthPx

        if (hasOverflow && text.length > 3) {
            isTruncated = true

            // 从中间开始寻找合适的截断点
            val mid = text.length / 2
            var startOffset = mid
            var endOffset = mid
            var found = false

            // 尝试最多20次寻找合适的截断点
            for (attempt in 0..20) {
                truncatedText = buildString {
                    append(text.substring(0, startOffset))
                    append(ELLIPSIS)
                    append(text.substring(endOffset))
                }

                // 使用布局结果估算文本宽度
                val newWidth = estimateTextWidth(
                    text = truncatedText,
                    currentResult = result,
                    originalText = text
                )

                // 检查是否适应宽度
                if (newWidth <= maxWidthPx) {
                    found = true
                    break
                }

                // 向两边扩展截断范围
                if (attempt % 2 == 0) {
                    startOffset = (startOffset - 1).coerceAtLeast(0)
                } else {
                    endOffset = (endOffset + 1).coerceAtMost(text.length)
                }
            }

            // 如果找不到合适位置，使用默认截断
            if (!found) {
                truncatedText = text.take(text.length / 3) + ELLIPSIS + text.takeLast(text.length / 3)
            }
        } else {
            isTruncated = false
        }
    }

    Box(
        modifier = modifier
            .then(if (maxWidth != Dp.Unspecified) Modifier.width(maxWidth) else Modifier)
            .drawWithContent {
                // 首先绘制文本内容
                drawContent()

                // 如果文本被截断，绘制省略号
                if (isTruncated) {
                    val result = layoutResult ?: return@drawWithContent

                    // 计算省略号位置（在文本中间）
                    val ellipsisPosition = truncatedText.indexOf(ELLIPSIS)
                    if (ellipsisPosition != -1) {
                        // 获取省略号前的文本边界
                        val preEllipsis = truncatedText.substring(0, ellipsisPosition)
                        val preEllipsisEnd = if (preEllipsis.isNotEmpty()) preEllipsis.length - 1 else 0
                        val preEllipsisRect = result.getBoundingBox(preEllipsisEnd)

                        // 获取省略号后的文本边界
                        val postEllipsisStart = ellipsisPosition + 3
                        val postEllipsisRect = if (postEllipsisStart < truncatedText.length) {
                            result.getBoundingBox(postEllipsisStart)
                        } else {
                            preEllipsisRect
                        }

                        // 计算省略号绘制位置
                        val ellipsisX = (preEllipsisRect.right + postEllipsisRect.left) / 2
                        val ellipsisY = result.getLineTop(0) + result.size.height / 2

                        // 绘制三个点作为省略号
                        val dotSpacing = with(density) { 4.dp.toPx() }
                        listOf(-dotSpacing, 0f, dotSpacing).forEach { offset ->
                            drawCircle(
                                color = textStyle.color,
                                radius = with(density) { 1.5.dp.toPx() },
                                center = Offset(ellipsisX + offset, ellipsisY)
                            )
                        }
                    }
                }
            }
    ) {
        Text(
            text = truncatedText,
            style = textStyle,
            maxLines = maxLines,
            overflow = TextOverflow.Clip,
            onTextLayout = onTextLayout
        )
    }
}

// 估算文本宽度的辅助函数（不使用可组合函数）
private fun estimateTextWidth(
    text: String,
    currentResult: TextLayoutResult,
    originalText: String
): Float {
    // 简化的估算逻辑：
    // 1. 计算原始文本的平均字符宽度
    val originalWidth = currentResult.size.width
    val originalCharCount = originalText.length
    val avgCharWidth = originalWidth / originalCharCount

    // 2. 估算新文本的宽度
    return (text.length * avgCharWidth).toFloat()
}