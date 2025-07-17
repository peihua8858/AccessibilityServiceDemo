package com.peihua.touchmonitor.ui.screen.function.autoScroller

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.activity.AutoScrollScreenActivity
import com.peihua.touchmonitor.activity.HomeScreenActivity
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.ui.AppProvider
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.ExtendedListTile
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.RotatingView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.stackEntry
import com.peihua.touchmonitor.ui.theme.DefaultTextStyle
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.writeLogFile
import com.peihua.touchmonitor.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.text.isNullOrEmpty


/**
 * 刷屏器配置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortVideoScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val result = viewModel.settingsState.value
    val bundle = stackEntry?.savedStateHandle
    val selectPackage = bundle?.get<String>("packageName")
    //请求数据
    val refresh = {
        viewModel.requestData(selectPackage)
    }
    dLog { "MainScreen>>>>>>>selectPackage1:$selectPackage" }
    if (!selectPackage.isNullOrEmpty()) {
        bundle.remove<String>("packageName")
        dLog { "MainScreen>>>>>>>selectPackage2:$selectPackage" }
        refresh()
    }
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = dimensionResource(id = R.dimen.dp_16),
                end = dimensionResource(id = R.dimen.dp_16)
            )

    ) {
        AppTopBar(title = { stringResource(R.string.settings) }, navigateUp = {
            if (context is AutoScrollScreenActivity) {
                context.startActivity(
                    Intent(context, HomeScreenActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                context.finish()
            } else {
                popBackStack()
            }
        }, actions = {
            IconButton(onClick = {
                navigateTo(AppRouter.LogScreen.route)
            }) {
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.dp_24)),
                    painter = painterResource(id = R.drawable.ic_logcat),
                    contentDescription = stringResource(R.string.text_log)
                )
            }
        })
        when (result) {
            is ResultData.Success -> {
                if (result.data.isEmpty()) {
                    return
                }
                dLog { "MainScreen>>>AllSettings>>>>provider<><><>" }
                ShortVideoScreenContent(Modifier.weight(1f), result.data) { item, isSaveToHistory ->
                    viewModel.saveToDb(item, isSaveToHistory)
                }
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
private fun ShortVideoScreenContent(
    modifier: Modifier,
    models: List<AppModel>,
    saveDb: (AppModel, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isExpanded = remember { mutableStateOf(false) }
    val selectedModel = models.find { it.isSelected } ?: models[0]
    val selectedOption = remember { mutableStateOf(selectedModel) }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = dimensionResource(id = R.dimen.dp_16))
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
                                ConstraintLayout(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = dimensionResource(id = R.dimen.dp_4),
                                            bottom = dimensionResource(id = R.dimen.dp_4)
                                        )
                                ) {
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
                                            .size(dimensionResource(id = R.dimen.dp_24))
                                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)))
                                    )

                                    ScaleText(
                                        modifier = Modifier
                                            .constrainAs(title) {
                                                start.linkTo(icon.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .padding(start = dimensionResource(id = R.dimen.dp_8)),
                                        text = item.displayName,
                                        style = DefaultTextStyle,
                                        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                    )
                                    if (item.isHistory) {
                                        ScaleText(
                                            modifier = Modifier.constrainAs(desc) {
                                                end.linkTo(parent.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            },
                                            text = stringResource(R.string.use_history),
                                            style = DefaultTextStyle,
                                            color = colorScheme.error,
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
                                    isExpanded.value = !isExpanded.value
                                    selectedOption.value = item
                                    saveDb(item, true)
                                }
                            },
                        )
                    }
                }
            }
            ExtendedListTile(
                modifier = Modifier
                    .padding(top = dimensionResource(id = R.dimen.dp_16))
                    .fillMaxWidth(),
                isExtended = false,
                title = { isExtended ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = dimensionResource(id = R.dimen.dp_56))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(start = dimensionResource(id = R.dimen.dp_8), end = dimensionResource(id = R.dimen.dp_8)),
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        // 旋转角度，up 旋转 180 度，down 旋转 0 度
                        val rotationAngle = if (isExtended) 180f else 0f
                        RotatingView(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            rotationAngle = rotationAngle
                        )
                        ScaleText(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            text = stringResource(R.string.text_settings),
                        )
                    }

                }) {
                dLog { "MainScreen>>>AllSettings>>>>111provider:${selectedOption.value.provider}" }
                selectedOption.value.provider.contentView(
                    Modifier.padding(dimensionResource(id = R.dimen.dp_8)),
                    selectedOption.value
                ) {
                    saveDb(it, false)
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen.dp_32)),
            onClick = {
                saveDb(selectedOption.value, false)
                // 引导用户到系统辅助功能设置
                try {
                    context.toAccessibilitySettingActivity("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
                } catch (e: Exception) {
                    dLog { "MainScreen>>>>>>>error:${e.stackTraceToString()}" }
                    try {
                        context.toAccessibilitySettingActivity(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    } catch (e: Exception) {
                        writeLogFile { e.stackTraceToString() }
                    }
                }
            }) {
            ScaleText(stringResource(R.string.accessibility_service_authorization))
        }
    }
}

private fun Context.toAccessibilitySettingActivity(action: String) {
    try {
        val intent = Intent(action)
        intent.setData("package:${packageName}".toUri())
        startActivity(intent)
    } catch (e: Exception) {
        dLog { "MainScreen>>>>>>>error:${e.stackTraceToString()}" }
        val intent = Intent(action)
        startActivity(intent)
    }
}