package com.peihua.touchmonitor.ui.components

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : IMenuItem> ActionDropMenu(
    modifier: Modifier,
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    models: List<T>,
    selected: (T) -> Boolean,
    @DrawableRes iconRes: Int,
    changeValue: (T) -> Unit,
) {
    ActionDropMenu(
        modifier = modifier,
        expandedHeight = expandedHeight,
        models = models,
        selected = selected,
        changeValue = changeValue,
    ) {
        Icon(
            modifier = Modifier.size(dimensionResource(id = R.dimen.dp_24)),
            painter = painterResource(id = iconRes),
            contentDescription = ""
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : IMenuItem> ActionDropMenu(
    modifier: Modifier,
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    models: List<T>,
    selected: (T) -> Boolean,
    changeValue: (T) -> Unit,
    content: @Composable () -> Unit,
) {
    val isExtended = remember { mutableStateOf(false) }
    val selFirst = models.first { selected(it) }
    val selectedOption = remember { mutableStateOf(selFirst) }
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .width(expandedHeight)
            .height(expandedHeight)
            .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
            .clickable {
                isExtended.value = !isExtended.value
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
    DropdownMenu(
        modifier = Modifier,
        expanded = isExtended.value,
        onDismissRequest = { isExtended.value = false },
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
                        maxLines = 1,
                        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {
                    selectedOption.value = item
                    isExtended.value = !isExtended.value
                    changeValue(item)
                },
            )
        }
    }
}

interface IMenuItem {
    val displayName: String
}

@Parcelize
data class MenuItem<T : Serializable>(
    override val displayName: String,
    val value: T,
) : IMenuItem, Parcelable