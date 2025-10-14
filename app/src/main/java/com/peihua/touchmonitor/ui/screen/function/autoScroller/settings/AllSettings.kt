package com.peihua.touchmonitor.ui.screen.function.autoScroller.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.ui.theme.labelMediumNormal
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.rememberStateSet
import kotlinx.coroutines.launch

private data class OrientationModel(val orientation: Orientation, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AllSettings(modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsState = remember { mutableStateOf(model.settings) }
    val settings = model.settings
    settingsState.value = settings
    dLog { "settings.packageName:${settings.packageName}" }
    val colorScheme = MaterialTheme.colorScheme
    val isExpanded = remember { mutableStateOf(false) }
    val models = arrayOf(
        OrientationModel(Orientation.Vertical, stringResource(R.string.vertical)),
        OrientationModel(Orientation.Horizontal, stringResource(R.string.horizontal))
    )
    val selOption = models.find { it.orientation == settings.orientation } ?: models[0]
    val selectedOption = remember { mutableStateOf(selOption) }
    selectedOption.value = selOption
    val slidingSpeed = remember { mutableStateOf(settings.slidingSpeed.toString()) }
    val doubleSaver = remember { mutableStateOf(settings.isDoubleSaver) }
    doubleSaver.value = settings.isDoubleSaver
    val skipAdOrLive = remember { mutableStateOf(settings.isSkipAdOrLive) }
    skipAdOrLive.value = settings.isSkipAdOrLive
    val isBrightnessMin = remember { mutableStateOf(settings.isBrightnessMin) }
    isBrightnessMin.value = settings.isBrightnessMin
    val isRandomReverse = remember { mutableStateOf(settings.isRandomReverse) }
    isRandomReverse.value = settings.isRandomReverse
    val isSoundMute = remember { mutableStateOf(settings.isSoundMute) }
    val delayTimes = rememberStateSet(settings.delayTimes)
    dLog { "settings.delayTimes:${settings.delayTimes.toList()}" }
    val saveDoubleClick = { it: Boolean ->
        doubleSaver.value = it
        model.settings = settings.copy(isDoubleSaver = it)
        modelChange(model)
    }
    val saveSkipAdOrLiveClick = { it: Boolean ->
        skipAdOrLive.value = it
        model.settings = settings.copy(isSkipAdOrLive = it)
        modelChange(model)
    }
    val saveBrightnessMinClick = { it: Boolean ->
        dLog { "startActivity>>>> canWrite:${Settings.System.canWrite(context)}" }
        if (!Settings.System.canWrite(context)) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
            dLog { "startActivity>>>> 没有WRITE_SETTINGS权限" }
        } else {
            isBrightnessMin.value = it
            model.settings = settings.copy(isBrightnessMin = it)
            modelChange(model)
        }
    }
    val saveRandomReverseClick = { it: Boolean ->
        isRandomReverse.value = it
        model.settings = settings.copy(isRandomReverse = it)
        modelChange(model)
    }
    val saveDelayTimesClick = { result: List<Int> ->
        model.settings = settings.copy(delayTimes = result.toMutableList())
        modelChange(model)
        settingsState.value = settings.copy(delayTimes = result.toMutableList())
    }
    val saveSoundMute = { it: Boolean ->
        isSoundMute.value = it
        model.settings = settings.copy(isSoundMute = it)
        modelChange(model)
    }
    Column(modifier.verticalScroll(rememberScrollState())) {
        ExposedDropdownMenuBox(
            modifier = Modifier
                .fillMaxWidth(),
            expanded = isExpanded.value,
            onExpandedChange = { isExpanded.value = it },
        ) {
            OutlinedTextField(
                value = selectedOption.value.displayName,
                onValueChange = {
                },
                label = { ScaleText(stringResource(R.string.scroll_orientation)) },
                readOnly = true,
                textStyle = MaterialTheme.typography.labelMediumNormal,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = { isExpanded.value = false },
            ) {
                models.forEach { item ->
                    val selected = selectedOption.value == item
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (selected) colorScheme.secondaryContainer else Color.Transparent),
                        text = {
                            ScaleText(
                                text = item.displayName,
                                color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {
                            selectedOption.value = item
                            isExpanded.value = !isExpanded.value
                            model.settings = settings.copy(orientation = item.orientation)
                            modelChange(model)
                        },
                    )
                }
            }
        }
        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_16)))
        OutlinedTextField(
            value = slidingSpeed.value,
            onValueChange = {
                if (it.all { it.isDigit() } || it.isEmpty()) {
                    if (it.isEmpty() || it.toLong() in 1..1000) {
                        slidingSpeed.value = it
                        if (it.isEmpty()) {
                            return@OutlinedTextField
                        }
                        model.settings = settings.copy(slidSpeed = it.toInt())
                        scope.launch {
                            modelChange(model)
                        }
                    }
                }
            },
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        tint = colorScheme.error,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.dp_18)),
                        contentDescription = null
                    )
                    ScaleText(
                        text = stringResource(R.string.sliding_speed_tips),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(),
                        color = colorScheme.error
                    )
                }
            },
            label = { ScaleText(stringResource(R.string.sliding_speed)) },
            textStyle = MaterialTheme.typography.labelMediumNormal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_8)))
        DelayTimesFlowRow(timeState = delayTimes, changeValues = saveDelayTimesClick)
        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    doubleSaver.value = !doubleSaver.value
                    saveDoubleClick(doubleSaver.value)
                }) {
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                color = colorScheme.onSurface,
                text = stringResource(R.string.double_click_like),
            )
            Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
            Checkbox(
                doubleSaver.value,
                onCheckedChange = {
                    saveDoubleClick(it)
                })
        }
//        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    skipAdOrLive.value = !skipAdOrLive.value
                    saveSkipAdOrLiveClick(skipAdOrLive.value)
                }) {
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                color = colorScheme.onSurface,
                text = stringResource(R.string.skip_ad_or_live),
            )
            Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
            Checkbox(
                skipAdOrLive.value,
                onCheckedChange = {
                    saveSkipAdOrLiveClick(it)
                })
        }
//        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isBrightnessMin.value = !isBrightnessMin.value
                    saveBrightnessMinClick(isBrightnessMin.value)
                }) {
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                color = colorScheme.onSurface,
                text = stringResource(R.string.brightness_min),
            )
            Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
            Checkbox(
                isBrightnessMin.value,
                onCheckedChange = {
                    saveBrightnessMinClick(it)
                })
        }
//        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isSoundMute.value = !isSoundMute.value
                    saveSoundMute(isSoundMute.value)
                }) {
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                color = colorScheme.onSurface,
                text = stringResource(R.string.sound_mute),
            )
            Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
            Checkbox(
                isSoundMute.value,
                onCheckedChange = {
                    saveSoundMute(it)
                })
        }
//        Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isRandomReverse.value = !isRandomReverse.value
                    saveRandomReverseClick(isRandomReverse.value)
                }) {
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                color = colorScheme.onSurface,
                text = stringResource(R.string.random_reverse),
            )
            Spacer(Modifier.size(dimensionResource(id = R.dimen.dp_4)))
            Checkbox(
                isRandomReverse.value,
                onCheckedChange = {
                    saveRandomReverseClick(it)
                })
        }
    }
}

@Composable
private fun DelayTimesFlowRow(
    modifier: Modifier = Modifier,
    timeState: MutableSet<Int> = rememberStateSet(),
    changeValues: (List<Int>) -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    dLog { ">>>55555>delayTimes:${timeState.toList()}" }
    Column(modifier = modifier) {
        ScaleText(
            stringResource(R.string.delay_time),
        )
        FlowRow(
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_4)),
            maxItemsInEachRow = 4
        ) {
            key(timeState) {
                for (index in 1..32) {
                    val isSelected = timeState.contains(index)
                    val borderColor = if (isSelected) colorScheme.primary else colorScheme.inverseSurface
                    val backgroundColor = if (isSelected) colorScheme.secondaryContainer else Color.Transparent
                    val textColor = if (isSelected) colorScheme.onSecondaryContainer else colorScheme.inverseSurface
                    ScaleText(
                        text = index.toString(),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(
                                bottom = if (30 - index > 4) dimensionResource(id = R.dimen.dp_16) else 0.dp,
                                start = dimensionResource(id = R.dimen.dp_8),
                                end = dimensionResource(id = R.dimen.dp_8)
                            )
                            .border(
                                dimensionResource(id = R.dimen.dp_1),
                                borderColor,
                                RoundedCornerShape(dimensionResource(id = R.dimen.dp_8))
                            )
                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                            .background(shape = RectangleShape, color = backgroundColor)
                            .clickable {
                                if (timeState.contains(index)) {
                                    timeState.remove(index)
                                } else {
                                    timeState.add(index)
                                }
                                changeValues(timeState.toList())
                            }
                            .padding(
                                start = dimensionResource(id = R.dimen.dp_16),
                                top = dimensionResource(id = R.dimen.dp_8),
                                end = dimensionResource(id = R.dimen.dp_16),
                                bottom = dimensionResource(id = R.dimen.dp_8)
                            )
                            .weight(1f)
                    )
                }

            }
        }
    }
}
