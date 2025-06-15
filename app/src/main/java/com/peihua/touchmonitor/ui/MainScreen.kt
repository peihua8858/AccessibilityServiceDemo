package com.peihua.touchmonitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.chatbox.shared.components.text.ScaleText
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.utils.finish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isExpanded = remember { mutableStateOf(false) }
    val models = App_Models
    val selectedOption = remember { mutableStateOf(AppProvider.ALL) }
    LaunchedEffect(null) {
        DataManager.querySettings { settings ->
            val result = models.find { it.model.pkgName == settings.packageName }
            if (result != null) {
                result.model.settings = settings
                selectedOption.value = result
            }
        }
    }
    val modelProvider = selectedOption.value
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        AppTopBar(title = { "Touch Monitor" }, navigateUp = {
            context.finish()
        })
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                //选择的应用
                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = isExpanded.value,
                    onExpandedChange = { isExpanded.value = it },
                ) {
                    OutlinedTextField(
                        value = selectedOption.value.displayName,
                        onValueChange = {
                        },
                        label = { ScaleText(stringResource(R.string.app_provider)) },
                        readOnly = true,
                        textStyle = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded.value,
                        onDismissRequest = { isExpanded.value = false },
                    ) {
                        models.forEach { item ->
                            val selected = selectedOption.value == item
                            DropdownMenuItem(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (selected) colorScheme.secondaryContainer else Color.Transparent),
                                text = {
                                    Row {
                                        val drawable = item.model.icon
                                        Image(
                                            if (drawable == null)
                                                rememberAsyncImagePainter(R.mipmap.ic_launcher)
                                            else rememberDrawablePainter(drawable), "",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        ScaleText(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = item.displayName,
                                            fontSize = 20.sp,
                                            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                },
                                onClick = {
                                    selectedOption.value = item
                                    isExpanded.value = !isExpanded.value
                                    item.model.saveToDb()
                                },
                            )
                        }
                    }
                }
                modelProvider.contentView(Modifier, modelProvider) {
                    it.model.saveToDb()
                }
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

