package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.utils.toDp

@Composable
fun ErrorView(
    modifier: Modifier = Modifier,
    retry: () -> Unit,
    content: @Composable () -> Unit = {
        Icon(
            modifier = Modifier.size(128.dp),
            painter = painterResource(id = R.drawable.ic_load_fail),
            contentDescription = null
        )
        ScaleText(
            text = stringResource(id = R.string.text_load_fail),
            style = typography.titleMedium,
        )
    },
) {
    val contentHeight = remember { mutableIntStateOf(0) }
    val topPx = contentHeight.intValue / 5f
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                contentHeight.intValue = it.height
            }) {
        Column(
            modifier = Modifier.padding(top = topPx.toDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
            Spacer(modifier = Modifier.size(16.dp))
            //text 下划线
            RoundTextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { retry() },
                text = stringResource(id = R.string.text_retry),
            )
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun LoadingViewFillMaxSize(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun LoadingRoundView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .defaultMinSize(
                minWidth = 64.dp,
                minHeight = 64.dp
            )
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center)
        )
    }
}


@Composable
fun EmptyView(
    modifier: Modifier = Modifier,
    retry: () -> Unit,
    content: @Composable () -> Unit = {
        Icon(
            modifier = Modifier.size(128.dp),
            painter = painterResource(id = R.drawable.ic_empty_data),
            contentDescription = null
        )
        ScaleText(
            text = stringResource(id = R.string.text_no_data_found),
            style = typography.titleMedium,
        )
    },
) {
    val contentHeight = remember { mutableIntStateOf(0) }
    val topPx = contentHeight.intValue / 5f
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                contentHeight.intValue = it.height
            }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topPx.toDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
            Spacer(modifier = Modifier.size(16.dp))
            //text 下划线
            RoundTextButton(
                modifier = Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                    .width(width = 128.dp),
                text = stringResource(id = R.string.text_retry),
                onClick = { retry() })
        }
    }
}