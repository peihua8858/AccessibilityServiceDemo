package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.peihua.touchmonitor.ui.components.text.ScaleText

@Composable
fun ProgressDialog(title: String, progress: Float, onDismiss: () -> Unit) {
    val showDialog = remember{ mutableStateOf(true) }
    Dialog(onDismissRequest = {
        showDialog.value = false // 用户取消时的处理
        onDismiss()
    }) {
//        LoadingView(Modifier.size(dimensionResource(id = R.dimen.dp_128))) // 加载视图内容
        ScaleText(text = title)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}