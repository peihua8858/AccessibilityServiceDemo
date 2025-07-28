package com.peihua.touchmonitor.ui.screen.account


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.viewmodel.AccountViewModel

@Composable
fun LoginScreen(modifier: Modifier = Modifier, viewModel: AccountViewModel = viewModel()) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .verticalScroll(rememberScrollState())
    ) {
    }
}