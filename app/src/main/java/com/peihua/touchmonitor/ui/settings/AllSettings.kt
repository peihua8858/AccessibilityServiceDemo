package com.peihua.touchmonitor.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.utils.dLog

private data class OrientationModel(val orientation: Orientation, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AllSettings(modifier: Modifier, model: AppModel, modelChange: (AppModel) -> Unit) {
    val context = LocalContext.current
    val settings = model.settings
    val colorScheme = MaterialTheme.colorScheme
    val isExpanded = remember { mutableStateOf(false) }
    val models = arrayOf(
        OrientationModel(Orientation.Vertical, stringResource(R.string.vertical)),
        OrientationModel(Orientation.Horizontal, stringResource(R.string.horizontal))
    )
    val selOption = models.find { it.orientation == settings.orientation } ?: models[0]
    val selectedOption = remember { mutableStateOf(selOption) }
    selectedOption.value = selOption
    val doubleSaver = remember { mutableStateOf(settings.isDoubleSaver) }
    doubleSaver.value = settings.isDoubleSaver
    val skipAdOrLive = remember { mutableStateOf(settings.isSkipAdOrLive) }
    skipAdOrLive.value = settings.isSkipAdOrLive
    val isBrightnessMin = remember { mutableStateOf(settings.isBrightnessMin) }
    isBrightnessMin.value = settings.isBrightnessMin
    val isRandomReverse = remember { mutableStateOf(settings.isRandomReverse) }
    isRandomReverse.value = settings.isRandomReverse
    val delayTimes = remember { mutableStateListOf<Long>() }
    delayTimes.clear()
    settings.delayTimes.forEach {
        delayTimes.add(it)
    }
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
    val saveDelayTimesClick = { index: Long ->
        if (delayTimes.contains(index)) {
            delayTimes.remove(index)
        } else {
            delayTimes.add(index)
        }
        model.settings = settings.copy(delayTimes = delayTimes)
        modelChange(model)
    }
    Column(
        modifier
            .verticalScroll(rememberScrollState())
    ) {
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
                textStyle = MaterialTheme.typography.labelMedium,
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
                                fontSize = 20.sp,
                                color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
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
        Spacer(Modifier.size(16.dp))
        Column {
            Text(stringResource(R.string.delay_time))
            FlowRow(modifier = Modifier.padding(top = 4.dp), maxItemsInEachRow = 4) {
                for (index in 1L..32L) {
                    val isSelected = delayTimes.contains(index)
                    val borderColor = if (isSelected) Color.Blue else Color.Gray
                    val backgroundColor = if (isSelected) Color.Blue else Color.Transparent
                    val textColor = if (isSelected) Color.White else Color.Black
                    Text(
                        text = index.toString(),
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(
                                bottom = if (30 - index > 4) 16.dp else 0.dp,
                                start = 8.dp,
                                end = 8.dp
                            )
                            .border(
                                1.dp,
                                borderColor,
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(shape = RectangleShape, color = backgroundColor)
                            .clickable {
                                saveDelayTimesClick(index)
                            }
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                            .weight(1f)
                    )
                }
            }
        }
        Spacer(Modifier.size(16.dp))
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
                text = stringResource(R.string.double_click_like),
                fontSize = 20.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(4.dp))
            Checkbox(
                doubleSaver.value,
                onCheckedChange = {
                    saveDoubleClick(it)
                })
        }
        Spacer(Modifier.size(16.dp))
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
                text = stringResource(R.string.skip_ad_or_live),
                fontSize = 20.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(4.dp))
            Checkbox(
                skipAdOrLive.value,
                onCheckedChange = {
                    saveSkipAdOrLiveClick(it)
                })
        }
        Spacer(Modifier.size(16.dp))
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
                text = stringResource(R.string.brightness_min),
                fontSize = 20.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(4.dp))
            Checkbox(
                isBrightnessMin.value,
                onCheckedChange = {
                    saveBrightnessMinClick(it)
                })
        }

        Spacer(Modifier.size(16.dp))
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
                text = stringResource(R.string.random_reverse),
                fontSize = 20.sp,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.size(4.dp))
            Checkbox(
                isRandomReverse.value,
                onCheckedChange = {
                    saveRandomReverseClick(it)
                })
        }
    }
}