package com.peihua.touchmonitor.ui.components.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.R
import kotlinx.coroutines.delay

object SearchBarDefaults {
    @Stable
    internal fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
        when {
            !enabled -> disabledTextColor
            isError -> errorTextColor
            focused -> focusedTextColor
            else -> unfocusedTextColor
        }
    internal fun TextFieldColors.containerColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
        when {
            !enabled -> disabledContainerColor
            isError -> errorContainerColor
            focused -> focusedContainerColor
            else -> unfocusedContainerColor
        }
    internal val SearchBarVerticalPadding: Dp = 8.dp
    // Search bar has 16dp padding between icons and start/end, while by default text field has 12dp.
    private val SearchBarIconOffsetX: Dp = 4.dp
    internal fun Modifier.textFieldBackground(color: ColorProducer, shape: Shape): Modifier =
        this.drawWithCache {
            val outline = shape.createOutline(size, layoutDirection, this)
            onDrawBehind { drawOutline(outline, color = color()) }
        }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InputField(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearch: (String) -> Unit,
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        textStyle: TextStyle = LocalTextStyle.current,
        placeholder: @Composable (() -> Unit)? = null,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        colors: TextFieldColors = inputFieldColors(),
        interactionSource: MutableInteractionSource? = null,
    ) {
        @Suppress("NAME_SHADOWING")
        val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

        val focused = interactionSource.collectIsFocusedAsState().value
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

//    val searchSemantics = getString(Strings.SearchBarSearch)
//    val suggestionsAvailableSemantics = getString(Strings.SuggestionsAvailable)

        val textColor =
            LocalTextStyle.current.color.takeOrElse {
                colors.textColor(enabled, isError = false, focused = focused)
            }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (it.isFocused) onExpandedChange(true) }
             /*.semantics {
                 contentDescription = searchSemantics
                 if (expanded) {
                     stateDescription = suggestionsAvailableSemantics
                 }
             }*/,
            enabled = enabled,
            singleLine = true,
            textStyle = textStyle.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(colors.cursorColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            interactionSource = interactionSource,
            decorationBox =
                @Composable { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = query,
                        innerTextField = innerTextField,
                        enabled = enabled,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        contentPadding= PaddingValues(0.dp),
                        placeholder = placeholder,
                        leadingIcon =
                            leadingIcon?.let { leading ->
                                { Box(Modifier.offset(x = SearchBarIconOffsetX)) { leading() } }
                            },
                        trailingIcon =
                            trailingIcon?.let { trailing ->
                                { Box(Modifier.offset(x = -SearchBarIconOffsetX)) { trailing() } }
                            },
                        shape = RoundedCornerShape(25),
                        colors = colors,
                        container = {
                            val containerColor =
                                animateColorAsState(
                                    targetValue =
                                        colors.containerColor(
                                            enabled = enabled,
                                            isError = false,
                                            focused = focused,
                                        ),
//                                animationSpec = MotionSchemeKeyTokens.FastEffects.value(),
                                )
                            Box(
                                Modifier.textFieldBackground(containerColor::value, inputFieldShape)
                            )
                        },
                    )
                },
        )

        val shouldClearFocus = !expanded && focused
        LaunchedEffect(expanded) {
            if (shouldClearFocus) {
                // Not strictly needed according to the motion spec, but since the animation
                // already has a delay, this works around b/261632544.
                delay(100.toLong())
                focusManager.clearFocus()
            }
        }
    }
}