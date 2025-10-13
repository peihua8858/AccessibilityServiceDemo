package com.peihua.touchmonitor.ui.screen.function.devices

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.copyToClipBoard
import com.peihua.touchmonitor.utils.formatToDate

@Composable
fun DeviceInfoScreen(modifier: Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val text: String = "主板：${Build.BOARD}\n\n" +
            "系统版本：${Build.VERSION.RELEASE}\n\n" +
            "系统启动程序版本号：${Build.BOOTLOADER}\n\n" +
            "系统定制商：${Build.BRAND}\n\n" +
            "32位CPU指令集：${Build.SUPPORTED_32_BIT_ABIS.contentToString()}\n\n" +
            "64位CPU指令：${Build.SUPPORTED_64_BIT_ABIS.contentToString()}\n\n" +
            "设置参数：${Build.DEVICE}\n\n" +
            "显示屏参数：${Build.DISPLAY}\n\n" +
            "无线电固件版本：${Build.getRadioVersion()}\n\n" +
            "硬件识别码：${Build.FINGERPRINT}\n\n" +
            "硬件名称：${Build.HARDWARE}\n\n" +
            "HOST：${Build.HOST}\n\n" +
            "修订版本列表：${Build.ID}\n\n" +
            "硬件制造商：${Build.MANUFACTURER}\n\n" +
            "硬件序列号：${Build.SERIAL}\n\n" +
            "手机制造商：${Build.PRODUCT}\n\n" +
            "描述构建标的签描：${Build.TAGS}\n\n" +
            "构建时间：${Build.TIME.formatToDate("yyyy-MM-dd HH:mm:ss")}\n\n" +
            "构建类型：${Build.TYPE}\n\n" +
            "用户：${Build.USER}"
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
            .fillMaxWidth()

    ) {
        Text(
            text = "设备信息",
            style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16))
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DeviceInfoItem(modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_16)), key = "主板", value = Build.BOARD)
            DeviceInfoItem(key = "系统版本", value = Build.VERSION.RELEASE)
            DeviceInfoItem(key = "系统启动程序版本号", value = Build.BOOTLOADER)
            DeviceInfoItem(key = "系统定制商", value = Build.BRAND)
            DeviceInfoItem(key = "32位CPU指令集", value = Build.SUPPORTED_32_BIT_ABIS.contentToString())
            DeviceInfoItem(key = "64位CPU指令", value = Build.SUPPORTED_64_BIT_ABIS.contentToString())
            DeviceInfoItem(key = "设置参数", value = Build.DEVICE)
            DeviceInfoItem(key = "显示屏参数", value = Build.DISPLAY)
            DeviceInfoItem(key = "无线电固件版本", value = Build.getRadioVersion())
            DeviceInfoItem(key = "硬件识别码", value = Build.FINGERPRINT)
            DeviceInfoItem(key = "硬件名称", value = Build.HARDWARE)
            DeviceInfoItem(key = "HOST", value = Build.HOST)
            DeviceInfoItem(key = "修订版本列表", value = Build.ID)
            DeviceInfoItem(key = "硬件制造商", value = Build.MANUFACTURER)
            DeviceInfoItem(key = "硬件序列号", value = Build.SERIAL)
            DeviceInfoItem(key = "手机制造商", value = Build.PRODUCT)
            DeviceInfoItem(key = "描述构建标的签描", value = Build.TAGS)
            DeviceInfoItem(key = "构建时间", value = Build.TIME.formatToDate("yyyy-MM-dd HH:mm:ss"))
            DeviceInfoItem(key = "构建类型", value = Build.TYPE)
            DeviceInfoItem(key = "用户", value = Build.USER)
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.dp_56)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.textButtonColors().copy(contentColor = colorScheme.secondary),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                onClick = {
                    popBackStack()
                }) {
                Text(text = stringResource(id = R.string.text_cancel))
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            TextButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
                onClick = {
                    context.copyToClipBoard(text)
                }) {
                Text(text = stringResource(id = R.string.text_copy))
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(modifier: Modifier = Modifier, key: String, value: String?) {
    Text(
        text = "$key：$value",
        modifier = modifier.padding(
            start = dimensionResource(id = R.dimen.dp_16),
            end = dimensionResource(id = R.dimen.dp_16),
            bottom = dimensionResource(id = R.dimen.dp_16)
        )
    )
}