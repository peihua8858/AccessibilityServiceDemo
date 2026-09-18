package com.peihua.touchmonitor.ui.screen.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.utils.rememberFloatState
import com.peihua.touchmonitor.utils.rememberState
import com.peihua8858.tools.utils.isNonEmpty

@Composable
fun BaseDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    onDismissRequest: () -> Unit,
    onPositive: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_ok) to { onDismissRequest() },
    onNeutral: Pair<Any, () -> Unit>? = null,
    onNegative: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_cancel) to { onDismissRequest() },
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseDialog(
        modifier,
        stringResource(id = title),
        onDismissRequest,
        onPositive,
        onNeutral,
        onNegative,
        content
    )
}

@Composable
fun BaseDialog(
    modifier: Modifier = Modifier,
    title: String,
    onDismissRequest: () -> Unit,
    onPositive: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_ok) to { onDismissRequest() },
    onNeutral: Pair<Any, () -> Unit>? = null,
    onNegative: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_cancel) to { onDismissRequest() },
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        BaseDialogScreen(modifier, title, onDismissRequest, onPositive, onNeutral, onNegative) {
            content()
        }
    }
}


@Composable
fun BaseDialogScreen(
    modifier: Modifier = Modifier,
    title: String,
    onDismissRequest: () -> Unit,
    onPositive: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_ok) to { onDismissRequest() },
    onNeutral: Pair<Any, () -> Unit>? = null,
    onNegative: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_cancel) to { onDismissRequest() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val positive = onPositive ?: (stringResource(id = R.string.text_ok) to { onDismissRequest() })
    val negative =
        onNegative ?: (stringResource(id = R.string.text_cancel) to { onDismissRequest() })
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(
                colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            )
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )
        content()
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = colorScheme.secondary),
                shape = RoundedCornerShape(8.dp),
                onClick = negative.second
            ) {
                Text(
                    text = (negative.first as? String ?: stringResource(
                        negative.first as? Int ?: R.string.text_cancel
                    ))
                )
            }
            if (onNeutral != null) {
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onNeutral.second
                ) {
                    Text(
                        text = (onNeutral.first as? String
                            ?: stringResource(onNeutral.first as Int))
                    )
                }
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                onClick = positive.second
            ) {
                Text(
                    text = (positive.first as? String ?: stringResource(
                        positive.first as? Int ?: R.string.text_ok
                    ))
                )
            }
        }
    }
}


/**
 * 消息对话框
 */
@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onDismissRequest: () -> Unit,
    onPositive: Pair<String, () -> Unit>? = stringResource(id = R.string.text_ok) to { onDismissRequest() },
    onNegative: Pair<String, () -> Unit>? = stringResource(id = R.string.text_cancel) to { onDismissRequest() },
) {
    BaseDialogScreen(modifier, title, onDismissRequest, onPositive, onNegative) {
        Text(text = content, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    @StringRes content: Int,
    onDismissRequest: () -> Unit,
    onPositive: Pair<String, () -> Unit>? = stringResource(id = R.string.text_ok) to { onDismissRequest() },
    onNegative: Pair<String, () -> Unit>? = stringResource(id = R.string.text_cancel) to { onDismissRequest() },
) {
    MessageDialog(
        modifier,
        stringResource(id = title),
        stringResource(id = content),
        onDismissRequest,
        onPositive,
        onNegative
    )
}

@Composable
fun LoadingDialog(modifier: Modifier = Modifier, title: String) {
    Dialog(onDismissRequest = {}) {

    }
}

@Composable
fun ProgressDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    progress: Float = -1f,
    onDismiss: () -> Unit = {},
) {
    Dialog(onDismissRequest = {
        onDismiss()
    }) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp)
        ) {
            if (title.isNonEmpty()) {
                ScaleText(text = title)
            }
            if (progress < 0f) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ProgressDialogScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    progress: Float = -1f,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp)
    ) {
        if (title.isNonEmpty()) {
            ScaleText(text = title)
        }
        if (progress < 0f) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun rememberShowProgressDialog(
    title: String = "",
    progress: MutableFloatState = rememberFloatState(-1f),
): MutableState<Boolean> {
    val state = rememberState(false)
    if (state.value) {
        ProgressDialog(title = title, progress = progress.floatValue)
    }
    return state
}