package com.peihua.touchmonitor.ui.screen.function.appmanager

import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.ErrorView
import com.peihua.touchmonitor.ui.components.IconText
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.TitleValueView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.viewmodel.AppDetailViewModel

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
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = dimensionResource(id = R.dimen.dp_16),
                end = dimensionResource(id = R.dimen.dp_16)
            )
    ) {
        AppTopBar(title = { stringResource(id = R.string.text_app_manager) }, navigateUp = {
            popBackStack()
        })
        when (result) {
            is ResultData.Success -> {
                AppInfoScreenContent(Modifier, result.data)
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

@Composable
private fun AppInfoScreenContent(modifier: Modifier = Modifier, model: AppInfoModel) {
    val context = LocalContext.current
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
                text = stringResource(id = R.string.text_running),
                painter = painterResource(id = R.drawable.ic_play_arrow_24),
                tint = Colors.Cyan[800]
            ) {
                // 打开应用
            }
            IconText(
                text = stringResource(id = R.string.text_export),
                painter = painterResource(id = R.drawable.ic_download_24),
                tint = Colors.Cyan[800]
            ) {
                // 导出应用
            }
            IconText(
                text = stringResource(id = R.string.text_share),
                painter = painterResource(id = R.drawable.ic_share_24),
                tint = Colors.Cyan[600]
            ) {
                // 分享应用
            }
            IconText(
                text = stringResource(id = R.string.text_app_detail),
                painter = painterResource(id = R.drawable.ic_info_24),
                tint = Colors.Cyan[600]
            ) {
                // 查看信息
            }
            IconText(
                text = stringResource(id = R.string.text_app_store),
                painter = painterResource(id = R.drawable.icon_market),
            ) {
                // 从应用市场打开
            }
            IconText(
                text = stringResource(id = R.string.text_app_uninstall),
                painter = painterResource(id = R.drawable.ic_delete_24),
                tint = Colors.Grey[800]
            ) {
                // 卸载应用
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
            )
            TitleValueView(
                title = stringResource(id = R.string.text_version_name),
                value = model.versionName,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_version_code),
                value = model.versionCode.toString(),
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_file_size),
                value = Formatter.formatFileSize(context, model.fileSize),
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_first_install_time),
                value = model.firstInstallTime,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_last_update_time),
                value = model.lastUpdateTime,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_install_source),
                value = model.installSource,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_low_api),
                value = model.lowApi,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_target_api),
                value = model.targetApi,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_system_app),
                value = stringResource(if (model.isSystemApp) R.string.text_yes else R.string.text_no),
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_uid),
                value = model.uid.toString(),
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_path),
                value = model.path,
            )
            TitleValueView(
                title = stringResource(id = R.string.text_app_launch_class),
                value = model.launchClass,
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
                orientation = Orientation.Vertical
            )
        }
    }
}