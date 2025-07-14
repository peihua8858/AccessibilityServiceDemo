package com.peihua.touchmonitor.utils

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.peihua.touchmonitor.ServiceApplication


fun showToast(message: String) {
    Toast.makeText(ServiceApplication.application, message, Toast.LENGTH_SHORT).show()
}

fun showToast(messageId: Int) {
    Toast.makeText(ServiceApplication.application, messageId, Toast.LENGTH_SHORT).show()
}

@Composable
fun ShowToast(message: String) {
    val context = LocalContext.current
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun ShowToast(messageId: Int) {
    val context = LocalContext.current
    Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
}