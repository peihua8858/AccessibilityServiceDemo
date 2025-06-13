package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.chatbox.shared.components.text.ScaleText
import com.peihua.touchmonitor.ui.icons.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier, title: @Composable () -> String,
    navigateUp: () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {
        NavigationIcon(navigateUp = navigateUp)
    }, actions: @Composable RowScope.() -> Unit = {}
) {
    val typography = MaterialTheme.typography
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                ScaleText(
                    style = typography.titleMedium,
                    text = title(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally) // 水平居中
                        .align(Alignment.Center),
                    fontSize = 20.sp,
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions,
        modifier = modifier
    )
}

/**
 * 导航图标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationIcon(
    imageVector: ImageVector = AppIcons.IosArrowBack,
    navigateUp: () -> Unit = {}
) {
    Icon(
        modifier = Modifier
            .size(32.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable { navigateUp() },
        imageVector = imageVector, contentDescription = ""
    )
}