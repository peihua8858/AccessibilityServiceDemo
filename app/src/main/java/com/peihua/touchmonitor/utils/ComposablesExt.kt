package com.peihua.touchmonitor.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
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
