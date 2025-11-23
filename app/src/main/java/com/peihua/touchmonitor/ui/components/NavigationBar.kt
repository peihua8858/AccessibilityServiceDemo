package com.peihua.touchmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua8858.tools.utils.dLog

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .windowInsetsPadding(windowInsets)
                    .defaultMinSize(minHeight = dimensionResource(id = R.dimen.dp_64))
                    .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun NavigationBarItem(
    modifier: Modifier = Modifier,
    navigationSuiteType: NavigationSuiteType =
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo()),
    selected: Boolean,
    painter: Painter,
    title: String,
    onClick: () -> Unit,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
) {
    dLog { "NavigationBarItem navigationSuiteType: $navigationSuiteType" }
    when (navigationSuiteType) {
        NavigationSuiteType.NavigationBar,
        NavigationSuiteType.ShortNavigationBarCompact,
        NavigationSuiteType.ShortNavigationBarMedium,
            -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
                    .clickable(onClick = onClick),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.dp_20)),
                    painter = painter,
                    tint = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
                    contentDescription = title
                )
                Text(
                    modifier = Modifier,
                    text = title,
                    fontSize = dimensionSpResource(id = R.dimen.sp_12),
                    color = if (selected) colors.selectedTextColor else colors.unselectedTextColor,
                )
            }
        }

        NavigationSuiteType.WideNavigationRailExpanded,
        NavigationSuiteType.NavigationDrawer,-> {
            val dp56 = dimensionResource(id = R.dimen.dp_24)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.dp_36))
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .size(dp56),
                    painter = painter,
                    tint = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
                    contentDescription = title
                )
                Text(
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen.dp_8)),
                    text = title,
                    fontSize = dimensionSpResource(id = R.dimen.sp_16),
                    color = if (selected) colors.selectedTextColor else colors.unselectedTextColor,
                )
            }
        }

        NavigationSuiteType.WideNavigationRailCollapsed,
        NavigationSuiteType.NavigationRail,/*最小宽度80dp*/ -> {//240dp~360dp
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                val dp24 = dimensionResource(id = R.dimen.dp_20)
                if (this.maxWidth < 56.dp) {
                    Icon(
                        modifier = Modifier
                            .size(dp24),
                        painter = painter,
                        tint = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
                        contentDescription = title
                    )
                } else {
                    Row(
                        modifier = modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(dp24),
                            painter = painter,
                            tint = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
                            contentDescription = title
                        )
                        Text(
                            modifier = Modifier,
                            text = title,
                            fontSize = dimensionSpResource(id = R.dimen.sp_12),
                            color = if (selected) colors.selectedTextColor else colors.unselectedTextColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.NavigationBarItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    painter: Painter,
    title: String,
    onClick: () -> Unit,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
) {
    Column(
        modifier = modifier
            .weight(1f)
            .align(Alignment.CenterVertically)
            .defaultMinSize(minHeight = dimensionResource(id = R.dimen.dp_64))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_10)))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.dp_24))
                .align(Alignment.CenterHorizontally),
            painter = painter,
            tint = if (selected) colors.selectedIconColor else colors.unselectedIconColor,
            contentDescription = title
        )
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = title,
            fontSize = dimensionSpResource(id = R.dimen.sp_12),
            color = if (selected) colors.selectedTextColor else colors.unselectedTextColor,
        )
    }
}