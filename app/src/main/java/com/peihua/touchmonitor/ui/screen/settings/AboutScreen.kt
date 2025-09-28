package com.peihua.touchmonitor.ui.screen.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.dimensionSpResource

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val sb = StringBuilder()
    configuration.screenLayout
    sb.append("屏幕宽度（单位：dp）：${configuration.screenWidthDp},")
        .append("\n")
        .append("屏幕高度（单位：dp）：${configuration.screenHeightDp}")
        .append("\n")
        .append("最小屏幕宽度（单位：dp）：sw${configuration.smallestScreenWidthDp}dp")
        .append("\n")
        .append("像素密度DPI：${configuration.densityDpi}")
        .append("\n")
        .append("像素密度：${density.density}")
        .append("\n")
        .append("字体缩放系数：${density.fontScale}")
        .append("\n")
        .append("屏幕密度：${configuration.densityDpi / 160f}")
        .append("\n")
        .append("屏幕布局:")
    val layoutDir: Int = (configuration.screenLayout and Configuration.SCREENLAYOUT_LAYOUTDIR_MASK)
    when (layoutDir) {
        Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED -> sb.append(" undefined")
        Configuration.SCREENLAYOUT_LAYOUTDIR_LTR -> sb.append(" ldltr")
        Configuration.SCREENLAYOUT_LAYOUTDIR_RTL -> sb.append(" ldrtl")
        else -> {
            sb.append(layoutDir shr Configuration.SCREENLAYOUT_LAYOUTDIR_SHIFT)
        }
    }
    sb.append("\n")
        .append("屏幕大小:")
    when ((configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK)) {
        Configuration.SCREENLAYOUT_SIZE_UNDEFINED -> sb.append(" undefined")
        Configuration.SCREENLAYOUT_SIZE_SMALL -> sb.append(" smll")
        Configuration.SCREENLAYOUT_SIZE_NORMAL -> sb.append(" nrml")
        Configuration.SCREENLAYOUT_SIZE_LARGE -> sb.append(" lrg")
        Configuration.SCREENLAYOUT_SIZE_XLARGE -> sb.append(" xlrg")
        else -> {
            sb.append(configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK)
        }
    }
    sb.append("\n")
        .append("屏幕纵横比:")
    when ((configuration.screenLayout and Configuration.SCREENLAYOUT_LONG_MASK)) {
        Configuration.SCREENLAYOUT_LONG_UNDEFINED -> sb.append(" undefined")
        Configuration.SCREENLAYOUT_LONG_NO -> sb.append(" notlong")
        Configuration.SCREENLAYOUT_LONG_YES -> sb.append(" long")
        else -> {
            sb.append(configuration.screenLayout and Configuration.SCREENLAYOUT_LONG_MASK)
        }
    }
    sb.append("\n")
        .append("屏幕是否圆形:${configuration.isScreenRound}")
    val orientation: Int = configuration.orientation
    sb.append("\n").append("屏幕方向:")
    when (orientation) {
        Configuration.ORIENTATION_UNDEFINED -> sb.append(" undefined")
        Configuration.ORIENTATION_LANDSCAPE -> sb.append(" 横向")
        Configuration.ORIENTATION_PORTRAIT -> sb.append(" 纵向")
        else -> {
            sb.append(orientation)
        }
    }
    val uiMode: Int = configuration.uiMode
    sb.append("\n")
        .append("UI模型:")
    when ((uiMode and Configuration.UI_MODE_TYPE_MASK)) {
        Configuration.UI_MODE_TYPE_UNDEFINED -> sb.append(" undefined")
        Configuration.UI_MODE_TYPE_NORMAL -> sb.append(" normal")
        Configuration.UI_MODE_TYPE_DESK -> sb.append(" desk")
        Configuration.UI_MODE_TYPE_CAR -> sb.append(" car")
        Configuration.UI_MODE_TYPE_TELEVISION -> sb.append(" television")
        Configuration.UI_MODE_TYPE_APPLIANCE -> sb.append(" appliance")
        Configuration.UI_MODE_TYPE_WATCH -> sb.append(" watch")
        Configuration.UI_MODE_TYPE_VR_HEADSET -> sb.append(" vrheadset")
        else -> {
            sb.append(uiMode and Configuration.UI_MODE_TYPE_MASK)
        }
    }
    sb.append("\n").append("亮度模型:")
    when ((uiMode and Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_UNDEFINED -> sb.append(" undefined")
        Configuration.UI_MODE_NIGHT_NO -> sb.append(" notnight")
        Configuration.UI_MODE_NIGHT_YES -> sb.append(" night")
        else -> {
            sb.append(uiMode and Configuration.UI_MODE_NIGHT_MASK)
        }
    }
    val touchscreen: Int = configuration.touchscreen
    sb.append("\n").append("设备附加的触摸屏:")
    when (touchscreen) {
        Configuration.TOUCHSCREEN_UNDEFINED -> sb.append(" undefined")
        Configuration.TOUCHSCREEN_NOTOUCH -> sb.append(" -touch")
        Configuration.TOUCHSCREEN_STYLUS -> sb.append(" stylus")
        Configuration.TOUCHSCREEN_FINGER -> sb.append(" finger")
        else -> {
            sb.append(touchscreen)
        }
    }
    sb.append("\n")
        .append("configuration:${configuration.toString()}")
    Toolbar(modifier = modifier, title = stringResource(id = R.string.text_about), navigateUp = {
        popBackStack()
    }) {
        Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16))) {
            ScaleText(
                text = sb.toString(),
                fontSize = dimensionSpResource(id = R.dimen.sp_12),
            )
        }
    }
}