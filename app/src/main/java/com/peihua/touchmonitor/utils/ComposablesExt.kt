package com.peihua.touchmonitor.utils

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.cache
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color

@Composable
fun <T> rememberStateSet(): MutableSet<T> {
    return rememberStateSet(arrayListOf())
}

@Composable
fun <T> rememberStateSet(data: List<T>): MutableSet<T> {
    val delayTimes = remember { mutableStateSetOf<T>() }
    if (data.isNotEmpty()) {
        delayTimes.addAll(data)
    }
    return delayTimes
}


@Composable
fun <T> rememberStateList(): MutableList<T> {
    return rememberStateList(arrayListOf())
}

@Composable
fun <T> rememberStateList(data: List<T>): MutableList<T> {
    val delayTimes = remember { mutableStateListOf<T>() }
    if (data.isNotEmpty()) {
        delayTimes.addAll(data)
    }
    return delayTimes
}

@Composable
fun <T> rememberState(value: T): MutableState<T> {
    return remember { mutableStateOf(value) }
}

@Composable
fun <T> rememberSaveable(
    value: T,
): MutableState<T> {
    return rememberSaveable { mutableStateOf(value) }
}

@Composable
fun rememberColorSaveable(
    value: Color,
): MutableState<Color> {
    return rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(value) }
}

val ColorSaver = run {
    val redKey = "Red"
    val greenKey = "Green"
    val blueKey = "Blue"
    mapSaver(
        save = { mapOf(redKey to it.red, greenKey to it.green, blueKey to it.blue) },
        restore = {
            Color(
                red = it[redKey] as Float,
                green = it[greenKey] as Float,
                blue = it[blueKey] as Float
            )
        }
    )
}

@Composable
fun <T> rememberSaveable(
    vararg inputs: Any?,
    stateSaver: Saver<T, out Any>,
    value: T,
): MutableState<T> {
    return rememberSaveable(inputs, stateSaver = stateSaver) { mutableStateOf(value) }
}
