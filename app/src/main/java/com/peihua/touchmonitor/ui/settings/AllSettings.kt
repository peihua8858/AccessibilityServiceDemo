package com.peihua.touchmonitor.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppModel
import com.peihua.touchmonitor.utils.checkPermissions
import com.peihua.touchmonitor.utils.dLog

private data class OrientationModel(val orientation: Orientation, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class)
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
    val doubleSaver = remember { mutableStateOf(settings.isDoubleSaver) }
    val skipAdOrLive = remember { mutableStateOf(settings.isSkipAdOrLive) }
    val isBrightnessMin = remember { mutableStateOf(settings.isBrightnessMin) }
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
    Column(modifier) {
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
    }
}