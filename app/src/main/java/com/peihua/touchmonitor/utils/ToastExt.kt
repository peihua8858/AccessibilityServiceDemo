package com.peihua.touchmonitor.utils

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.ServiceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


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

@Composable
fun ShowSnackBar(@StringRes messageId: Int,dismissAction: @Composable () -> Unit={}) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = stringResource(messageId)
    LaunchedEffect(Unit) {
        snackbarHostState.showSnackbar(message)
    }
}
fun SnackbarHostState.showSnackBar(@StringRes messageId: Int,) {
    SnackBarImpl(this).show(messageId)
}
fun SnackbarHostState.showSnackBar(message: String) {
    SnackBarImpl(this).show(message)
}

private class SnackBarImpl(private val snackbarHostState: SnackbarHostState): CoroutineScope by MainScope(){
    fun show(message: String) {
        launch {
            snackbarHostState.showSnackbar(message)
        }
    }
    fun show(@StringRes messageId: Int,) {
       show(ServiceApplication.application.getString(messageId))
    }
}