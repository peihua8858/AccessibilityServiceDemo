package com.peihua.touchmonitor.ui.screen.dialog


import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import com.godaddy.android.colorpicker.rememberColorSaveable
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.rememberState
import io.mhssn.colorpicker.ext.toHex

/**
 * 颜色选择器
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    @StringRes title: Int,
    defaultColor: Color = Color.White,
    onPositive: Pair<Any, (Color) -> Unit>? = R.string.text_ok to { popBackStack() },
    onNegative: Pair<Any, () -> Unit>? = R.string.text_cancel to { popBackStack() },
) {
    val positive: Pair<Any, (Color) -> Unit> = onPositive ?: (R.string.text_ok to { popBackStack() })
    val negative: Pair<Any, () -> Unit> = onNegative ?: (R.string.text_ok to { popBackStack() })
    val colorState = remember { mutableStateOf(defaultColor) }
    ColorPickerDialog(
        modifier, stringResource(id = title), defaultColor,
        onPositive = positive.first to {
            positive.second(colorState.value)
        }, onNegative = negative.first to negative.second
    )
}

/**
 * 颜色选择器
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    title: String,
    defaultColor: Color = Color.White,
    onPositive: Pair<Any, (Color) -> Unit>? = stringResource(id = R.string.text_ok) to { popBackStack() },
    onNegative: Pair<Any, () -> Unit>? = stringResource(id = R.string.text_cancel) to { popBackStack() },
) {
    val positive: Pair<Any, (Color) -> Unit> = onPositive ?: (stringResource(id = R.string.text_ok) to { popBackStack() })
    val colorState = rememberColorSaveable(HsvColor.from(defaultColor))
    val colorInput = remember { mutableStateOf(defaultColor.toHex()) }
    BaseDialog(modifier, title, {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.dp_16))
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            dLog { "colorInput:${colorInput.value}, colorState.value:${colorState.value}" }
            ClassicColorPicker(
                modifier = Modifier.size(dimensionResource(id = R.dimen.dp_200)),
                colorState = colorState,
                showAlphaBar = false,
                onColorChanged = {
                    colorState.value = it
                    colorInput.value = it.toColor().toHex()
                })
            OutlinedTextField(
                value = colorInput.value,
                onValueChange = {
                    colorInput.value = it
                    if (it != colorState.value.toColor().toHex() && (it.length == 6 || it.length == 8)) {
                        val color = Color(it.toLong(16))
                        colorState.value = HsvColor.from(color)
                        dLog { "colorInput:$it, color:$color,color.hex:${color.toHex()} , colorState.value:${colorState.value}" }
                    }
                },
                leadingIcon = {
                    Text(text = "#")
                },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16))
            )
        }

    }, onPositive = positive.first to {
        positive.second(colorState.value.toColor())
    }, onNegative = onNegative)
}