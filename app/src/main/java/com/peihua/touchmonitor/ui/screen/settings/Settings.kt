package com.peihua.touchmonitor.ui.screen.settings

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.LanguageModel
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.model.ThemeModel
import com.peihua.touchmonitor.ui.components.CheckboxListTile
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.DefaultTextStyle
import com.peihua.touchmonitor.ui.theme.ThemeMode
import com.peihua.touchmonitor.viewmodel.SystemSettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SystemSettingsViewModel = viewModel(),
) {
    val defaultLanguageModel = LanguageModel.getDefault()
    val systemSettings =
        remember { mutableStateOf(SystemSettings(language = defaultLanguageModel)) }
    LaunchedEffect(null) {
       val settings = SystemSettingsStore.getSystemSettings()
        systemSettings.value = settings
    }
    val colorScheme = MaterialTheme.colorScheme
    val isThemeExpanded = remember { mutableStateOf(false) }
    val isLanguageExpanded = remember { mutableStateOf(false) }
    val themeModels = ThemeMode.entries.mapIndexed { index, mode -> ThemeModel(theme = mode) }
//    val themeModel = remember { mutableStateOf(themeModels[0]) }
    val languageCodes = stringArrayResource(R.array.language_list_value)
    val languageNames = stringArrayResource(R.array.language_list)
    val languages = languageCodes.mapIndexed { index, item ->
        LanguageModel(
            langCode = item,
            name = languageNames[index],
        )
    }
//    val selLanguage = remember { mutableStateOf(languages[0]) }
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.settings),
        navigateUp = {
            popBackStack()
        }) {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16))) {
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = isLanguageExpanded.value,
                onExpandedChange = { isLanguageExpanded.value = it },
            ) {
                OutlinedTextField(
                    value = systemSettings.value.language.name,
                    onValueChange = {
                    },
                    label = {
                        Row {
                            Icon(
                                painterResource(R.drawable.ic_language_32),
                                "",
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dp_16))
                                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)))
                            )
                            ScaleText(stringResource(R.string.txt_language))
                        }
                    },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = isLanguageExpanded.value,
                    onDismissRequest = { isLanguageExpanded.value = false },
                ) {
                    for ((index, item) in languages.withIndex()) {
                        val selected = systemSettings.value.language.langCode == item.langCode
                        DropdownMenuItem(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (selected) colorScheme.secondaryContainer else Color.Transparent),
                            text = {
                                ScaleText(
                                    text = languageNames[index],
                                    color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            onClick = {
                                isLanguageExpanded.value = !isLanguageExpanded.value
                                systemSettings.value = systemSettings.value.copy(language = item)
                                SystemSettingsStore.updateSystemSettings(
                                    systemSettings.value
                                )
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_8)))
            ExposedDropdownMenuBox(
                modifier = Modifier.fillMaxWidth(),
                expanded = isThemeExpanded.value,
                onExpandedChange = { isThemeExpanded.value = it },
            ) {
                OutlinedTextField(
                    value = stringResource(systemSettings.value.theme.nameIds),
                    onValueChange = {
                    },
                    leadingIcon = {
                        Icon(
                            systemSettings.value.theme.image,
                            "",
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.dp_16))
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)))
                        )
                    },
                    label = {
                        Row {
                            Image(
                                painterResource(R.drawable.ic_theme_32),
                                "",
                                modifier = Modifier
                                    .size(dimensionResource(id = R.dimen.dp_16))
                                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)))
                            )
                            ScaleText(stringResource(R.string.theme))
                        }
                    },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = isThemeExpanded.value,
                    onDismissRequest = { isThemeExpanded.value = false },
                ) {
                    themeModels.forEach { item ->
                        DropdownMenuItem(
                            text = {
                                ConstraintLayout(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = dimensionResource(id = R.dimen.dp_4),
                                            bottom = dimensionResource(id = R.dimen.dp_4)
                                        )
                                ) {
                                    val (icon, title) = createRefs()
                                    Icon(
                                        item.theme.image, "",
                                        modifier = Modifier
                                            .constrainAs(icon) {
                                                start.linkTo(parent.start)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .size(dimensionResource(id = R.dimen.dp_16))
                                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_4)))
                                    )

                                    ScaleText(
                                        modifier = Modifier
                                            .constrainAs(title) {
                                                start.linkTo(icon.end)
                                                top.linkTo(parent.top)
                                                bottom.linkTo(parent.bottom)
                                            }
                                            .padding(start = dimensionResource(id = R.dimen.dp_8)),
                                        text = stringResource(item.theme.nameIds),
                                        style = DefaultTextStyle,
                                        color = if (systemSettings.value.theme.model == item.theme) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                    )
                                }

                            },
                            onClick = {
                                isThemeExpanded.value = false
                                systemSettings.value = systemSettings.value.copy(theme = item)
                                SystemSettingsStore.updateSystemSettings(
                                    systemSettings.value
                                )
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_8)))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                CheckboxListTile(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    checked = systemSettings.value.theme.dynamicColor,
                    onCheckedChange = {
                        val theme = systemSettings.value.theme.copy(dynamicColor = it)
                        systemSettings.value = systemSettings.value.copy(theme = theme)
                        SystemSettingsStore.updateSystemSettings(
                            systemSettings.value.copy(theme = theme)
                        )
                    },
                    title = {
                        ScaleText(text = stringResource(R.string.text_dynamic_color))
                    }
                )
            }
        }
    }
}