package com.peihua.touchmonitor.ui.components.text

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarDefaults.inputFieldShape
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

//@Composable
//fun CustomTextField(
//    modifier: Modifier = Modifier,
//    hint: String? = null,
//    showCleanIcon: Boolean = false,
//    onTextChange: String.() -> Unit = {},
//    leadingIcon: @Composable (() -> Unit)? = null,
//    trailingIcon: @Composable (() -> Unit)? = null,
//    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
//    keyboardActions: String.() -> Unit = {},
//    defaultTextStyle: TextStyle,
//    textFieldStyle: TextStyle = defaultTextStyle,
//    defaultHintTextStyle: TextStyle,
//    hintTextStyle: TextStyle = defaultHintTextStyle,
//
//    ) {
//    var text by remember { mutableStateOf("") }
//    Row(
//        modifier,
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        leadingIcon?.invoke()
//        BasicTextField(
//            value = text,
//            onValueChange = {
//                text = it
//                onTextChange.invoke(it)
//            },
//            cursorBrush = SolidColor(colorResource(id = R.color.color_currency)),
//            singleLine = true,
//            modifier = Modifier
//                .weight(1f)
//                .padding(start = 10.dp),
//            textStyle = textFieldStyle,
//            decorationBox = { innerTextField ->
//                if (text.isBlank() && hint.isNotNullEmpty())
//                    Box(
//                        modifier = Modifier
//                            .fillMaxHeight(),
//                        contentAlignment = Alignment.CenterStart
//                    ) {
//                        innerTextField()
//                        CustomText(hint ?: "", 16f.sp, colorResource(id = R.color.color_BFBFBF))
//                        Text(
//                            hint ?: "",
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            style = hintTextStyle,
//                        )
//                    } else innerTextField()
//
//            },
//            keyboardActions = KeyboardActions {
//                keyboardActions(text)
//            },
//            keyboardOptions = keyboardOptions
//        )
//        trailingIcon?.invoke()
//        if (showCleanIcon)
//            ImageIcon(R.drawable.icon_edit_clean, 24.dp) {
//                text = ""
//            }
//    }
//}
