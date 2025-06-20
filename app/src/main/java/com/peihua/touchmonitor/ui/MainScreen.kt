package com.peihua.touchmonitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.finish
import com.peihua.touchmonitor.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val result = viewModel.settingsState.value
    val bundle = stackEntry?.savedStateHandle
    val selectPackage = bundle?.get<String>("packageName")
    //请求数据
    val refresh = {
        viewModel.requestData(selectPackage)
    }
    "".dLog { "MainScreen>>>>>>>selectPackage1:$selectPackage" }
    if (!selectPackage.isNullOrEmpty()) {
        bundle.remove<String>("packageName")
        "".dLog { "MainScreen>>>>>>>selectPackage2:$selectPackage" }
        refresh()
    }
    Column(
        modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        AppTopBar(title = { stringResource(R.string.settings) }, navigateUp = {
            context.finish()
        })
        when (result) {
            is ResultData.Success -> {
                if (result.data.isEmpty()) {
                    return
                }
                MainScreenContent(Modifier, result.data)
            }

            is ResultData.Failure -> {
                ErrorView { refresh() }
            }

            is ResultData.Initialize -> {
                refresh()
            }

            is ResultData.Starting -> {
                LoadingView()
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(modifier: Modifier, models: List<AppModel>) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isExpanded = remember { mutableStateOf(false) }
    val selectedModel = models.find { it.isSelected } ?: models[0]
    val selectedOption = remember { mutableStateOf(selectedModel) }
    val scope = rememberCoroutineScope()
    val model = selectedOption.value
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
                                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                                    val drawable = item.icon
                                    val (icon, title, desc) = createRefs()
                                    Image(
                                        if (drawable == null)
                                            rememberAsyncImagePainter(R.mipmap.ic_launcher)
                                        else rememberDrawablePainter(drawable), "",
                                        modifier = Modifier
                                            .constrainAs(icon) {
                                                start.linkTo(parent.start)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    ScaleText(
                                        modifier = Modifier
                                            .constrainAs(title) {
                                                start.linkTo(icon.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .padding(start = 8.dp),
                                        text = item.displayName,
                                        fontSize = 20.sp,
                                        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    if (item.isHistory) {
                                        ScaleText(
                                            modifier = Modifier.constrainAs(desc) {
                                                end.linkTo(parent.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            },
                                            text = stringResource(R.string.use_history),
                                            fontSize = 16.sp,
                                            color = colorScheme.error,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            },
                            onClick = {
                                if (item.provider == AppProvider.Other) {
                                    isExpanded.value = !isExpanded.value
                                    scope.launch {
                                        navigateTo(AppRouter.Applications.route)
                                    }
                                } else {
                                    selectedOption.value = item
                                    isExpanded.value = !isExpanded.value
                                    item.saveToDb()
                                }
                            },
                        )
                    }
                }
            }
            model.provider.contentView(Modifier, model) {
                it.saveToDb()
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
                contentDescription = stringResource(R.string.accessibility_service_authorization),
            )
        }
    }
}

