package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.labelMediumNormal
import com.peihua.touchmonitor.utils.rememberState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> DropdownMenuBox(
    modifier: Modifier = Modifier,
    data: MutableList<T> = arrayListOf<T>(),
    defaultSelectedItem: T = data[0],
    value: String = defaultSelectedItem.toString(),
    onValueChange: (String) -> Unit = {},
    anchorType: ExposedDropdownMenuAnchorType = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
    label: String? = null,
    readOnly: Boolean = true,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textColors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    placeholder: @Composable (() -> Unit)? = null,
    itemColors: MenuItemColors = DropdownMenuBoxDefaults.itemColors(),
    itemText: @Composable (Boolean, T) -> Unit = { isSelected, item ->
        ScaleText(
            item.toString(),
            color = itemColors.textColor(isSelected)
        )
    },
    onItemClick: (T) -> Unit = {},
) {
    DropdownMenuBox(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        anchorType = anchorType,
        label = { label?.let { ScaleText(label) } },
        readOnly = readOnly,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textColors = textColors,
        placeholder = placeholder,
        data = data,
        defaultSelectedItem = defaultSelectedItem,
        itemColors = itemColors,
        itemText = itemText,
        onItemClick = onItemClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> DropdownMenuBox(
    modifier: Modifier = Modifier,
    data: MutableList<T> = arrayListOf<T>(),
    defaultSelectedItem: T = data[0],
    value: String = defaultSelectedItem.toString(),
    onValueChange: (String) -> Unit = {},
    anchorType: ExposedDropdownMenuAnchorType = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
    label: @Composable (() -> Unit)? = null,
    readOnly: Boolean = true,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textColors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    placeholder: @Composable (() -> Unit)? = null,
    itemColors: MenuItemColors = DropdownMenuBoxDefaults.itemColors(),
    inputBox: @Composable ExposedDropdownMenuBoxScope.() -> Unit = {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            readOnly = readOnly,
            placeholder = placeholder,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = textColors,
            textStyle = MaterialTheme.typography.labelMediumNormal,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(anchorType)
        )
    },
    itemText: @Composable (Boolean, T) -> Unit = { isSelected, item ->
        ScaleText(
            item.toString(),
            color = itemColors.textColor(isSelected)
        )
    },
    onItemClick: (T) -> Unit = {},
) {
    val selectedItem = rememberState(defaultSelectedItem)
    val isExpanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        expanded = isExpanded.value,
        onExpandedChange = { isExpanded.value = it },
    ) {
        inputBox()
        ExposedDropdownMenu(
            expanded = isExpanded.value,
            onDismissRequest = { isExpanded.value = false },
        ) {
            data.forEach { item ->
                val isSelected = selectedItem.value == item
                DropdownMenuItem(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(itemColors.backgroundColor(isSelected)),
                    colors = itemColors.itemColors(),
                    text = {
                        itemText(isSelected, item)
                    },
                    onClick = {
                        selectedItem.value = item
                        isExpanded.value = false
                        onItemClick(item)
                    },
                )
            }
        }
    }
}


object DropdownMenuBoxDefaults {
    @Composable
    fun itemColors(): MenuItemColors {
        val itemColors = MenuDefaults.itemColors()
        val colorScheme = MaterialTheme.colorScheme
        return MenuItemColors(
            textColor = itemColors.textColor,
            leadingIconColor = itemColors.leadingIconColor,
            trailingIconColor = itemColors.trailingIconColor,
            disabledTextColor = itemColors.disabledTextColor,
            disabledLeadingIconColor = itemColors.disabledLeadingIconColor,
            disabledTrailingIconColor = itemColors.disabledTrailingIconColor,
            selectedTextColor = colorScheme.onPrimary,
            backgroundColor = colorScheme.surfaceContainer,
            selectedBackgroundColor = colorScheme.surfaceContainerHigh,
        )
    }
}

class MenuItemColors(
    val textColor: Color,
    val leadingIconColor: Color,
    val trailingIconColor: Color,
    val disabledTextColor: Color,
    val disabledLeadingIconColor: Color,
    val disabledTrailingIconColor: Color,
    val selectedTextColor: Color,
    val backgroundColor: Color,
    val selectedBackgroundColor: Color,
) {
    fun copy(
        textColor: Color = this.textColor,
        leadingIconColor: Color = this.leadingIconColor,
        trailingIconColor: Color = this.trailingIconColor,
        disabledTextColor: Color = this.disabledTextColor,
        disabledLeadingIconColor: Color = this.disabledLeadingIconColor,
        disabledTrailingIconColor: Color = this.disabledTrailingIconColor,
        selectedTextColor: Color = this.selectedTextColor,
        backgroundColor: Color = this.backgroundColor,
        selectedBackgroundColor: Color = this.selectedBackgroundColor,
    ) = MenuItemColors(
        textColor.takeOrElse { this.textColor },
        leadingIconColor.takeOrElse { this.leadingIconColor },
        trailingIconColor.takeOrElse { this.trailingIconColor },
        disabledTextColor.takeOrElse { this.disabledTextColor },
        disabledLeadingIconColor.takeOrElse { this.disabledLeadingIconColor },
        disabledTrailingIconColor.takeOrElse { this.disabledTrailingIconColor },
        selectedTextColor.takeOrElse { this.selectedTextColor },
        backgroundColor.takeOrElse { this.backgroundColor },
        selectedBackgroundColor.takeOrElse { this.selectedBackgroundColor },
    )

    @Composable
    fun itemColors(): androidx.compose.material3.MenuItemColors =
        MenuDefaults.itemColors().copy(
            textColor = textColor,
            leadingIconColor = leadingIconColor,
            trailingIconColor = trailingIconColor,
            disabledTextColor = disabledTextColor,
            disabledLeadingIconColor = disabledLeadingIconColor,
            disabledTrailingIconColor = disabledTrailingIconColor
        )

    fun backgroundColor(isSelected: Boolean): Color =
        if (isSelected) selectedBackgroundColor else backgroundColor

    fun textColor(isSelected: Boolean): Color =
        if (isSelected) selectedTextColor else textColor
}