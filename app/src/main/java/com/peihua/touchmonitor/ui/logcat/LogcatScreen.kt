package com.peihua.touchmonitor.ui.logcat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.viewmodel.RealTimeLogViewModel


@Composable
fun LogcatScreen(
    modifier: Modifier = Modifier,
    viewModel: RealTimeLogViewModel = viewModel(),
) {
    val isPaused = remember { mutableStateOf(false) }
    val isRunning = remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.size(32.dp))
            IconButton(onClick = {
                if (isPaused.value) {
                    viewModel.onPause()
                } else {
                    viewModel.onResume()
                }
                isPaused.value = !isPaused.value
            }) {
                Icon(
                    painter = painterResource(
                        if (isPaused.value) {
                            R.drawable.ic_play_circle_24
                        } else {
                            R.drawable.ic_pause_circle_24
                        }
                    ),
                    contentDescription = stringResource(if (isPaused.value) R.string.text_resume else R.string.text_pause)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(onClick = {
                viewModel.clearLog()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.text_clear)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Button(onClick = {
                if (isRunning.value) {
                    viewModel.onStop()
                } else {
                    viewModel.onStart()
                }
                isRunning.value = !isRunning.value
            }) {
                Text(text = stringResource(if (isRunning.value) R.string.text_stop else R.string.text_start))
            }
            Spacer(modifier = Modifier.size(32.dp))
        }
        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(viewModel._messages) { item ->
                Text(text = item, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp , end = 16.dp))
            }
        }
    }
}