package com.peihua.touchmonitor.ui.screen.function.devices

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.screen.dialog.BaseDialog
import com.peihua.touchmonitor.utils.copyToClipBoard
import com.peihua.touchmonitor.utils.formatToDate

@Composable
fun DeviceInfoScreen(modifier: Modifier) {
    val context = LocalContext.current
    val result = mutableListOf<Pair<String, String>>()
    result.add("主板" to Build.BOARD)
    result.add("系统版本" to Build.VERSION.RELEASE)
    result.add("系统启动程序版本号" to Build.BOOTLOADER)
    result.add("系统定制商" to Build.BRAND)
    result.add("32位CPU指令集" to Build.SUPPORTED_32_BIT_ABIS.contentToString())
    result.add("64位CPU指令" to Build.SUPPORTED_64_BIT_ABIS.contentToString())
    result.add("设置参数" to Build.DEVICE)
    result.add("显示屏参数" to Build.DISPLAY)
    result.add("无线电固件版本" to Build.getRadioVersion())
    result.add("硬件识别码" to Build.FINGERPRINT)
    result.add("硬件名称" to Build.HARDWARE)
    result.add("HOST" to Build.HOST)
    result.add("修订版本列表" to Build.ID)
    result.add("硬件制造商" to Build.MANUFACTURER)
    result.add("硬件序列号" to Build.SERIAL)
    result.add("手机制造商" to Build.PRODUCT)
    result.add("描述构建标的签描" to Build.TAGS)
    result.add("构建时间" to Build.TIME.formatToDate("yyyy-MM-dd HH:mm:ss"))
    result.add("构建类型" to Build.TYPE)
    result.add("用户" to Build.USER)
    BaseDialog(
        modifier = modifier,
        title = "设备信息",
        onDismissRequest = { popBackStack() },
        onPositive = stringResource(id = R.string.text_copy) to {
            val text = result.joinToString("\n") { "${it.first}:${it.second}" }
            context.copyToClipBoard(text)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            for ((index, item) in result.withIndex()) {
                if (index == 0) {
                    DeviceInfoItem(
                        modifier = Modifier.padding(top = 16.dp),
                        key = item.first, value = item.second
                    )
                } else {
                    DeviceInfoItem(key = item.first, value = item.second)
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(modifier: Modifier = Modifier, key: String, value: String?) {
    ScaleText(
        text = "$key：$value",
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    )
}