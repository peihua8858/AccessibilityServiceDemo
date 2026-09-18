package com.peihua.touchmonitor.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.LanguageModel
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.model.ThemeModel
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.CheckboxListTile
import com.peihua.touchmonitor.ui.components.DropdownMenuBox
import com.peihua.touchmonitor.ui.components.DropdownMenuBoxDefaults
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.surface
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.DefaultTextStyle
import com.peihua.touchmonitor.ui.theme.ThemeMode
import com.peihua.touchmonitor.ui.theme.labelMediumNormal
import com.peihua.touchmonitor.utils.isAtLeastS
import com.peihua.touchmonitor.utils.rememberState
import com.peihua.touchmonitor.viewmodel.SystemSettingsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SystemSettingsViewModel = viewModel(),
) {
    val languageCodes = stringArrayResource(R.array.language_list_value)
    val languageNames = stringArrayResource(R.array.language_list)
    val languages = languageCodes.mapIndexed { index, item ->
        LanguageModel(
            langCode = item,
            name = languageNames[index],
        )
    }
    val defaultLanguageModel = LanguageModel.default.copy(name = languageNames[0])
    val systemSettings = rememberState(SystemSettings(language = defaultLanguageModel))
    LaunchedEffect(systemSettings) {
        val settings = SystemSettingsStore.getSystemSettings()
        for ((index, item) in languages.withIndex()) {
            if (item.langCode == settings.language.langCode) {
                systemSettings.value = settings.copy(language = item)
                break
            }
        }
    }
    val colorScheme = MaterialTheme.colorScheme
    val themeModels = ThemeMode.entries.mapIndexed { index, mode -> ThemeModel(theme = mode) }
    val menuItemColors = DropdownMenuBoxDefaults.itemColors().copy(
        textColor = colorScheme.onSurfaceVariant,
        selectedTextColor = colorScheme.onSecondaryContainer,
        selectedBackgroundColor = colorScheme.secondaryContainer,
    )
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.settings),
        navigateUp = {
            popBackStack()
        }) {
        Column(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            DropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .surface(10.dp, 10.dp, 0.dp, 0.dp, elevation =1.dp)
                    .padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    ),
                data = languages.toMutableList(),
                value = systemSettings.value.language.name,
                label = {
                    Row {
                        Icon(
                            painterResource(R.drawable.ic_language_32),
                            "",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        ScaleText(stringResource(R.string.txt_language))
                    }
                },
                defaultSelectedItem = systemSettings.value.language,
                isSelected = { index, item ->
                    item.langCode == systemSettings.value.language.langCode
                },
                itemColors = menuItemColors,
                itemText = { isSelected, item ->
                    ScaleText(
                        modifier = Modifier
                            .fillMaxSize(),
                        text = item.name,
                        color = menuItemColors.textColor(isSelected),
                    )
                },
                onItemClick = {
                    systemSettings.value = systemSettings.value.copy(language = it)
                    SystemSettingsStore.updateSystemSettings(
                        systemSettings.value
                    )
                },
            )
            DropdownMenuBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .surface(10.dp, 10.dp, 0.dp, 0.dp, elevation =1.dp)
                    .padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    ),
                data = themeModels.toMutableList(),
                value = stringResource(systemSettings.value.themeModel.nameIds),
                label = {
                    Row {
                        Icon(
                            painterResource(R.drawable.ic_theme_32),
                            "",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        ScaleText(stringResource(R.string.theme))
                    }
                },
                defaultSelectedItem = systemSettings.value.themeModel,
                isSelected = { index, item ->
                    item.theme == systemSettings.value.themeModel.theme
                },
                prefix = {
                    Icon(
                        systemSettings.value.themeModel.theme.image,
                        "",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                },
                itemColors = menuItemColors,
                itemText = { isSelected, item ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 4.dp,
                                bottom = 4.dp
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
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        ScaleText(
                            modifier = Modifier
                                .constrainAs(title) {
                                    start.linkTo(icon.end)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                }
                                .padding(start = 8.dp),
                            text = stringResource(item.theme.nameIds),
                            color = menuItemColors.textColor(isSelected)
                        )
                    }
                },
                onItemClick = {
                    systemSettings.value = systemSettings.value.copy(themeModel = it)
                    SystemSettingsStore.updateSystemSettings(
                        systemSettings.value
                    )
                },
            )
            if (isAtLeastS) {
                SettingsCheckBox(
                    modifier = Modifier,
                    title = stringResource(R.string.text_dynamic_color),
                    selected = systemSettings.value.themeModel.dynamicColor,
                    showTopLine = false,
                    showBottomLine = false,
                    onCheckedChange = {
                        val themeModel = systemSettings.value.themeModel.copy(dynamicColor = it)
                        systemSettings.value = systemSettings.value.copy(themeModel = themeModel)
                        SystemSettingsStore.updateSystemSettings(
                            systemSettings.value.copy(themeModel = themeModel)
                        )
                    }
                )
            }
            SettingsItemView(
                modifier = Modifier,
                title = stringResource(R.string.text_export_path),
                showTopLine = !isAtLeastS,
                showBottomLine = false,
                value = systemSettings.value.exportPath,
            )
            SettingsItemView(
                modifier = Modifier,
                showTopLine = false,
                title = stringResource(R.string.text_about),
                value = "",
            ) {
                navigateTo(AppRouter.AboutScreen.route)
            }
        }
    }
}

@Composable
private fun SettingsCheckBox(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    showTopLine: Boolean = true,
    showBottomLine: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .surface(
                if (showTopLine) 10.dp else 0.dp,
                if (showTopLine) 10.dp else 0.dp,
                if (showBottomLine) 10.dp else 0.dp,
                if (showBottomLine) 10.dp else 0.dp,
                elevation =1.dp
            )
            .clickable {
                onCheckedChange(!selected)
            },
        verticalArrangement = Arrangement.Center
    ) {
//        if (showTopLine) {
//            HorizontalDivider()
//        }
        CheckboxListTile(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    bottom = 16.dp
                ),
            indication = null,
            checked = selected,
            onCheckedChange = onCheckedChange,
            title = {
                ScaleText(text = title)
            }
        )
//        if (showBottomLine) {
//            HorizontalDivider()
//        }
    }
}

@Composable
private fun SettingsItemView(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    tint: Color = LocalContentColor.current,
    showTopLine: Boolean = true,
    showBottomLine: Boolean = true,
    onclick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .surface(
                if (showTopLine) 10.dp else 0.dp,
                if (showTopLine) 10.dp else 0.dp,
                if (showBottomLine) 10.dp else 0.dp,
                if (showBottomLine) 10.dp else 0.dp,
                elevation =1.dp
            )
            .clickable(onClick = onclick),
        verticalArrangement = Arrangement.Center
    ) {
//        if (showTopLine) {
//            HorizontalDivider()
//        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    top = 16.dp,
                    end = 8.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScaleText(
                modifier = Modifier
                    .padding(end = 8.dp),
                textAlign = TextAlign.Start,
                text = title,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                ScaleText(
                    modifier = Modifier,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMediumNormal,
                    text = value,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right_24),
                tint = if (tint != Color.Unspecified) {
                    tint
                } else {
                    LocalContentColor.current
                },
                contentDescription = null
            )
        }
//        if (showBottomLine) {
//            HorizontalDivider()
//        }
    }
}