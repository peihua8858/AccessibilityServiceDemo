package com.peihua.touchmonitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.utils.finish

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        AppTopBar(title = { "Touch Monitor" }, navigateUp = {
            context.finish()
        })
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {


            }

            IconButton(
                modifier = Modifier
                    .padding(bottom = 32.dp, end = 16.dp)
                    .align(Alignment.BottomEnd)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    ),
                onClick = {
                    // 引导用户到系统辅助功能设置
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "辅助功能授权"
                )
            }
        }

    }
}

