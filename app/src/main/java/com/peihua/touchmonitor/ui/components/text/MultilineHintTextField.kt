package com.peihua.touchmonitor.ui.components.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun MultilineHintTextField(
    modifier: Modifier = Modifier,
    value: String,
    maxLines: Int = 4,
    hintText: String = "",
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    BasicTextField(
        value = value,
        maxLines = maxLines,
        textStyle = textStyle,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            //decorationBox：装饰盒
            //从内部实现多行提示文本
            Box(modifier = modifier) {
                innerTextField()
                if (value.isEmpty()) {
                    Text(
                        text = hintText,
                        color = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                }
            }
        }
    )
}