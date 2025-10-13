package com.peihua.touchmonitor.ui.screen.function.appmanager

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.IconText
import com.peihua.touchmonitor.ui.components.LoadingRoundView
import com.peihua.touchmonitor.ui.components.LoadingViewFillMaxSize
import com.peihua.touchmonitor.ui.components.TitleValueView
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.function.appmanager.task.ExtortWorker
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.copyToClipBoard
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.shareCertainFiles
import com.peihua.touchmonitor.utils.showToast
import com.peihua.touchmonitor.viewmodel.AppDetailViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@Composable
fun AppDetailScreen(
    modifier: Modifier = Modifier,
    packageName: String,
    viewModel: AppDetailViewModel = viewModel(),
) {
    val result = viewModel.appInfo.value
    //请求数据
    val refresh = {
        viewModel.refreshAppInfo(packageName)
    }
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_app_manager),
        navigateUp = {
            popBackStack()
        }
    ) {
        Column(
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    start = dimensionResource(id = R.dimen.dp_16),
                    end = dimensionResource(id = R.dimen.dp_16)
                )
        ) {
            when (result) {
                is ResultData.Success -> {
                    AppInfoScreenContent(Modifier, result.data)
                }

                is ResultData.Failure -> {
                    ErrorView(retry = refresh)
                }

                is ResultData.Initialize -> {
                    refresh()
                }

                is ResultData.Starting -> {
                    LoadingViewFillMaxSize()
                }
            }
        }
    }
}

@Composable
private fun AppInfoScreenContent(
    modifier: Modifier = Modifier,
    model: AppInfoModel,
) {
    val context = LocalContext.current
    val exportAppPkg = remember { mutableStateOf(false to false) }
    if (exportAppPkg.value.first) {
        val isShare = exportAppPkg.value.second
        ExportApp(model, isShare) {
            exportAppPkg.value = false to false
        }
    }
    val onClickInfo = { title: String, value: String ->
        context.copyToClipBoard(value) {
           showToast(R.string.text_copy_success)
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val drawable = model.icon
        Image(
            if (drawable == null)
                rememberAsyncImagePainter(R.mipmap.ic_launcher)
            else rememberDrawablePainter(drawable), "",
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .size(dimensionResource(id = R.dimen.dp_128))
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
        )
        ScaleText(
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
            text = model.name + "(${model.versionName})",
        )
        Card(
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.dp_8),
                    bottom = dimensionResource(id = R.dimen.dp_8)
                ),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.dp_2))
        ) {
            IconText(
                text = stringResource(id = R.string.text_run),
                painter = painterResource(id = R.drawable.ic_play_arrow_24),
                tint = Colors.Cyan[800]
            ) {
                // 打开应用
                try {
                    val intent = context.packageManager.getLaunchIntentForPackage(model.packageName)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    showToast(e.toString())
                }
            }
            IconText(
                text = stringResource(id = R.string.text_export),
                painter = painterResource(id = R.drawable.ic_download_24),
                tint = Colors.Cyan[800]
            ) {
                // 导出应用
                exportAppPkg.value = true to false
            }
            IconText(
                text = stringResource(id = R.string.text_share),
                painter = painterResource(id = R.drawable.ic_share_24),
                tint = Colors.Cyan[600]
            ) {
                // 分享应用
                exportAppPkg.value = true to true
            }
            IconText(
                text = stringResource(id = R.string.text_app_detail),
                painter = painterResource(id = R.drawable.ic_info_24),
                tint = Colors.Cyan[600]
            ) {
                // 查看信息
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.fromParts("package", model.packageName, null))
                context.startActivity(intent)
            }
            IconText(
                text = stringResource(id = R.string.text_app_store),
                painter = painterResource(id = R.drawable.ic_play_store),
            ) {
                // 从应用市场打开
                try {
                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            ("market://details?id=" + model.packageName).toUri()
                        )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    showToast(e.toString())
                }
            }
            IconText(
                text = stringResource(id = R.string.text_app_uninstall),
                painter = painterResource(id = R.drawable.ic_delete_24),
                tint = Colors.Grey[800]
            ) {
                // 卸载应用
                try {
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_DELETE)
                    intent.setData(("package:" + model.packageName).toUri())
                    context.startActivity(intent)
                } catch (e: Exception) {
                    showToast(e.toString())
                }
            }
        }
        Card(
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.dp_8),
                    bottom = dimensionResource(id = R.dimen.dp_8)
                ),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.dp_2))
        ) {
            TitleValueView(
                title = stringResource(id = R.string.text_package_name),
                value = model.packageName,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_version_name),
                value = model.versionName,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_version_code),
                value = model.versionCode.toString(),
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_file_size),
                value = Formatter.formatFileSize(context, model.fileSize),
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_first_install_time),
                value = model.firstInstallTime,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_last_update_time),
                value = model.lastUpdateTime,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_install_source),
                value = model.installSource,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_low_api),
                value = model.lowApi,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_target_api),
                value = model.targetApi,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_system_app),
                value = stringResource(if (model.isSystemApp) R.string.text_yes else R.string.text_no),
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_uid),
                value = model.uid.toString(),
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_path),
                value = model.path,
                onClick = onClickInfo
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_launch_class),
                value = model.launchClass,
                onClick = onClickInfo
            )
        }
        Spacer(Modifier.height(dimensionResource(id = R.dimen.dp_8)))
        ScaleText(text = stringResource(id = R.string.app_signature))
        Card(
            modifier = Modifier
                .padding(
                    top = dimensionResource(id = R.dimen.dp_8),
                    bottom = dimensionResource(id = R.dimen.dp_8)
                ),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.dp_2))
        ) {
            TitleValueView(
                title = stringResource(id = R.string.app_signature_issuer),
                value = model.launchClass,
                orientation = Orientation.Vertical,
                onClick = onClickInfo

            )
        }
    }
}

@Composable
fun ExportApp(item: AppInfoModel, isShare: Boolean = false, onComplete: () -> Unit = {}) {
    // 状态管理
    val progress = remember { mutableFloatStateOf(0f) }
    val showDialog = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val worker = ExtortWorker(context, item) {
        onStart { /* 可以在这里处理开始状态，比如设置标志或更新 UI */ }
        onSpeed { w, speed ->
            dLog { "speed: $speed" }
        }
        onComplete { file, e ->
            onComplete()
            dLog { "exportApp, save file to $e successful" }
            showDialog.value = false // 隐藏 loading
            if (isShare) {
                scope.launch {
                    navigateTo2(Dialog.ShareDialog.route, ("filePath" to file.path))
                }
//                context.shareCertainFiles(file)
            }
        }
        onProgress { w, total, current ->
            dLog { "progress: $current/$total" }
            progress.floatValue = current.toFloat() / total
        }
    }

    val callback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                worker.cancel() // 取消导出操作
                showDialog.value = false // 隐藏 loading
            }
        }
    }

    // 显示 loading 弹窗
    if (showDialog.value) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showDialog.value = false // 用户取消时的处理
            worker.cancel() // 取消 worker
            callback.remove()
        }) {
            LoadingRoundView() // 加载视图内容
        }
    }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    DisposableEffect(callback) {
        dispatcher?.addCallback(callback)
        onDispose {
            showDialog.value = false // 用户取消时的处理
            worker.cancel() // 取消 worker
            callback.remove()
            onComplete()
        }
    }
    worker.start()
//    //启动 worker
//    LaunchedEffect(Unit) {
//        val result = worker.extort().await() // 开始导出
//        dLog { "exportApp, save file  to $result successful" }
//        onComplete()
//    }
}

