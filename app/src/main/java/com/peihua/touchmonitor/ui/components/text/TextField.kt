package com.peihua.touchmonitor.ui.components.text

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import com.peihua.touchmonitor.ui.components.defaultErrorSemantics

@Stable
internal fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
    when {
        !enabled -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }

@Stable
internal fun TextFieldColors.cursorColor(isError: Boolean): Color =
    if (isError) errorCursorColor else cursorColor

@SuppressLint("PrivateResource")
@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelPosition: TextFieldLabelPosition = TextFieldLabelPosition.Attached(),
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues =
        if (label == null || labelPosition is TextFieldLabelPosition.Above) {
            TextFieldDefaults.contentPaddingWithoutLabel()
        } else {
            TextFieldDefaults.contentPaddingWithLabel()
        },
    interactionSource: MutableInteractionSource? = null,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            colors.textColor(enabled, isError, focused)
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            state = state,
            modifier =
                modifier
                    .defaultErrorSemantics(isError, stringResource(R.string.default_error_message)),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            inputTransformation = inputTransformation,
            outputTransformation = outputTransformation,
            scrollState = scrollState,
            decorator =
                TextFieldDefaults.decorator(
                    state = state,
                    enabled = enabled,
                    lineLimits = lineLimits,
                    outputTransformation = outputTransformation,
                    interactionSource = interactionSource,
                    labelPosition = labelPosition,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    isError = isError,
                    colors = colors,
                    contentPadding = contentPadding,
                    container = {
                        TextFieldDefaults.Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    },
                ),
        )
    }
}
//@Composable
//fun TextField(
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
//    BasicTextField(
//        value = text,
//        onValueChange = {
//            text = it
//            onTextChange.invoke(it)
//        },
//        singleLine = true,
//        cursorBrush = SolidColor(colors.cursorColor),
//        modifier = Modifier
//            .weight(1f)
//            .padding(start = 10.dp),
//        textStyle = textFieldStyle,
//        decorationBox = { innerTextField ->
//            if (text.isBlank() && hint.isNotNullEmpty())
//                Box(
//                    modifier = Modifier
//                        .fillMaxHeight(),
//                    contentAlignment = Alignment.CenterStart
//                ) {
//                    innerTextField()
//                    CustomText(hint ?: "", 16f.sp, colorResource(id = R.color.color_BFBFBF))
//                    Text(
//                        hint ?: "",
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        style = hintTextStyle,
//                    )
//                } else innerTextField()
//
//        },
//        keyboardActions = KeyboardActions {
//            keyboardActions(text)
//        },
//        keyboardOptions = keyboardOptions
//    )
//}
