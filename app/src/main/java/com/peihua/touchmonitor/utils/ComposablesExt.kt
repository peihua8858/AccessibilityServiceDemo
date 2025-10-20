package com.peihua.touchmonitor.utils

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
import androidx.compose.runtime.saveable.rememberSaveable

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
fun <T> rememberSaveable(
    vararg inputs: Any?,
    stateSaver: Saver<T, out Any>,
    value: T,
): MutableState<T> {
    return rememberSaveable(inputs, stateSaver = stateSaver) { mutableStateOf(value) }
}
