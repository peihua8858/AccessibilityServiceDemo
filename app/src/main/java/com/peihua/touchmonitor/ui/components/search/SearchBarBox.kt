package com.peihua.touchmonitor.ui.components.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberRangeSliderState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.evaluateY
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.vLog
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.DropdownMenuBoxDefaults
import com.peihua.touchmonitor.ui.components.MenuItemColors
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.components.combinedClickable
import com.peihua.touchmonitor.ui.components.search.SearchBarDefaults.placeholderTextStyle
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.utils.rememberState
import com.peihua.touchmonitor.utils.rememberStateList
import com.peihua.touchmonitor.utils.toDp
import com.peihua.touchmonitor.utils.toPx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SearchBarBox(
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {
        ScaleText(

            text = stringResource(id = R.string.text_search),
            style = SearchBarDefaults.placeholderTextStyle(),
        )
    },
    data: SnapshotStateList<T> = rememberStateList<T>(),
    onSearchChange: (String) -> Unit = {},
    onSearch: (String) -> Unit = {},
    itemText: @Composable (T) -> Unit = { item ->
        ScaleText(
            item.toString(),
        )
    },
    onItemLongClick: (T) -> Unit = {},
    onItemClick: (T) -> Unit = {},
) {
    val isExpanded = remember { mutableStateOf(false) }
    val searchValue = remember { mutableStateOf("") }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = isExpanded.value,
        onExpandedChange = { isExpanded.value = it },
    ) {
        SearchBarDefaults.InputField(
            query = searchValue.value,
            onQueryChange = {
                searchValue.value = it
                onSearchChange(it)
            },
            onSearch = onSearch,
            expanded = isExpanded.value,
            onExpandedChange = { isExpanded.value = it },
            placeholder = placeholder,
            trailingIcon = {
                SearchBarDefaults.TrailingIcon(
                    Modifier
                        .size(24.dp)
                        .clickable {
                            isExpanded.value = false
                            onSearch(searchValue.value)
                        })
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        )
        if (data.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = { isExpanded.value = false },
            ) {
                data.forEach { item ->
                    Box(
                        modifier = Modifier.combinedClickable(
                            onLongClick = {
                                isExpanded.value = false
                                onItemLongClick(item)
                            }
                        ) {
                            isExpanded.value = false
                            onItemClick(item)
                        },
                    ){
                        itemText(item)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    state: SearchBarState = rememberSearchBarState(),
    expanded: Boolean = true,
    query: MutableState<String> = rememberState(""),
    onExpandedChange: (Boolean) -> Unit = {},
    shape: Shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)),
    colors: SearchBarColors = androidx.compose.material3.SearchBarDefaults.colors(),
    tonalElevation: Dp = androidx.compose.material3.SearchBarDefaults.TonalElevation,
    shadowElevation: Dp = androidx.compose.material3.SearchBarDefaults.ShadowElevation,
    windowInsets: WindowInsets = androidx.compose.material3.SearchBarDefaults.windowInsets,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = {},
    leadingIcon: @Composable (() -> Unit)? = {
        Icon(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.dp_24))
                .clickable {
                    onSearch(query.value)
                },
            painter = painterResource(id = R.drawable.ic_search_24),
            contentDescription = ""
        )
    },
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.dp_24))
                .clickable {
                    query.value = ""
                },
            painter = painterResource(id = R.drawable.ic_clear_24),
            contentDescription = ""
        )
    },
    textColors: TextFieldColors = inputFieldColors(),
    scrollBehavior: SearchBarScrollBehavior? = null,
) {
    TopSearchBar(
        state = state,
        modifier = modifier,
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
        inputField = {
            SearchBarDefaults.InputField(
                query = query.value,
                modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle,
                colors = textColors,
                onQueryChange = {
                    query.value = it
                    onQueryChange(it)
                },
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    if (query.value.isNotEmpty()) {
                        trailingIcon?.invoke()
                    }
                },
                onSearch = {
                    query.value = it
                    onSearch(it)
                }, onExpandedChange = onExpandedChange,
                expanded = expanded
            )
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarTitle(
    modifier: Modifier = Modifier,
    title: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    val isExtended = rememberState(false)
    val dimension = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        ScaleText(
            modifier = Modifier
                .align(Alignment.Center),
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        // 搜索按钮
        Box(
            modifier = modifier
                .align(Alignment.CenterEnd)
                .width(TopAppBarDefaults.TopAppBarExpandedHeight)
                .height(TopAppBarDefaults.TopAppBarExpandedHeight)
                .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                .clickable {
                    isExtended.value = !isExtended.value
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(dimensionResource(id = R.dimen.dp_24)),
                painter = painterResource(id = R.drawable.ic_search_24),
                contentDescription = ""
            )
        }
        val visibleState = remember { MutableTransitionState(false) }
        visibleState.targetState = isExtended.value
        val transition = rememberTransition(visibleState)
        val move = transition.animateFloat { if (it) 1f else 0f }
        TopSearchBar(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .offset(x = (TopAppBarDefaults.TopAppBarExpandedHeight.toPx(dimension) * move.value).toDp)
            /*.graphicsLayer {
                clip = true
                val width =
                    this.size.width - TopAppBarDefaults.TopAppBarExpandedHeight.toPx(
                        dimension
                    )
                translationX = width - (width * move.value)
//                    alpha = move.value
                dLog { "translationX: $translationX,move:$move,width:${width}" }
            }*/,
            expanded = isExtended.value,
            onSearch = {
                onSearch(it)
                isExtended.value = false
            },
            onQueryChange = onQueryChange
        )
//        // Expandable TopSearchBar
        AnimatedVisibility(
            visible = isExtended.value,
            enter = expandHorizontally(
                animationSpec = tween(300),
                expandFrom = Alignment.CenterHorizontally
            ),
            exit = shrinkHorizontally(
                animationSpec = tween(300),
                shrinkTowards = Alignment.CenterHorizontally
            )
        ) {
            TopSearchBar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(),
                expanded = isExtended.value,
                onSearch = {
                    onSearch(it)
                    isExtended.value = false
                },
                onQueryChange = onQueryChange
            )
        }
    }
}