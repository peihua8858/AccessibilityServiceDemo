package com.peihua.touchmonitor.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract



@OptIn(ExperimentalContracts::class)
public inline fun <C, R> C.ifNull(defaultValue: () -> R): R where C : R {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }
    return this ?: defaultValue()
}