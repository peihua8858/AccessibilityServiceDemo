package com.peihua.touchmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.graphics.Color
import kotlin.reflect.KProperty

@Composable
fun <T> rememberStateSet(): SnapshotStateSet<T> {
    return rememberStateSet(arrayListOf())
}

@Composable
fun <T> rememberStateSet(data: List<T>): SnapshotStateSet<T> {
    val delayTimes = remember { mutableStateSetOf<T>() }
    if (data.isNotEmpty()) {
        delayTimes.addAll(data)
    }
    return delayTimes
}


@Composable
fun <T> rememberStateList(): SnapshotStateList<T> {
    return rememberStateList(arrayListOf())
}

@Composable
fun <T> rememberStateList(data: List<T>): SnapshotStateList<T> {
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
fun rememberFloatState(value: Float): MutableFloatState {
    return remember { mutableFloatStateOf(value) }
}

@Composable
fun rememberIntState(value: Int): MutableIntState {
    return remember { mutableIntStateOf(value) }
}

@Composable
fun rememberLongState(value: Long): MutableLongState {
    return remember { mutableLongStateOf(value) }
}

@Composable
fun rememberDoubleState(value: Double): MutableDoubleState {
    return remember { mutableDoubleStateOf(value) }
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