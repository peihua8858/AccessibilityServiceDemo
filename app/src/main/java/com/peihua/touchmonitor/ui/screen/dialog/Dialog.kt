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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.fz.common.text.isNonEmpty
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack

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
    BaseDialog(modifier, stringResource(id = title), onDismissRequest, onPositive, onNeutral, onNegative, content)
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
    val negative = onNegative ?: (stringResource(id = R.string.text_cancel) to { onDismissRequest() })
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(
                Color.White,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8))
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
                .padding(dimensionResource(id = R.dimen.dp_16))
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
                .height(dimensionResource(id = R.dimen.dp_56)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.textButtonColors()
                    .copy(contentColor = colorScheme.secondary),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                onClick = negative.second
            ) {
                Text(text = (negative.first as? String ?: stringResource(negative.first as? Int ?: R.string.text_cancel)))
            }
            if (onNeutral != null) {
                VerticalDivider(modifier = Modifier.fillMaxHeight())
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                    onClick = onNeutral.second
                ) {
                    Text(text = (onNeutral.first as? String ?: stringResource(onNeutral.first as Int)))
                }
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                onClick = positive.second
            ) {
                Text(text = (positive.first as? String ?: stringResource(positive.first as? Int ?: R.string.text_ok)))
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
        Text(text = content, modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16)))
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
    MessageDialog(modifier, stringResource(id = title), stringResource(id = content), onDismissRequest, onPositive, onNegative)
}

@Composable
fun LoadingDialog(modifier: Modifier = Modifier, title: String) {
    Dialog(onDismissRequest = {}) {

    }
}

@Composable
fun ProgressDialog(modifier: Modifier = Modifier, title: String = "", progress: Float = -1f, onDismiss: () -> Unit = {}) {
    Dialog(onDismissRequest = {
        onDismiss()
    }) {
        Column(
            modifier = modifier
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                .background(Color.White)
                .padding(dimensionResource(id = R.dimen.dp_16))
        ) {
            if (title.isNonEmpty()) {
                ScaleText(text = title)
            }
            if (progress < 0f) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.dp_32))
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