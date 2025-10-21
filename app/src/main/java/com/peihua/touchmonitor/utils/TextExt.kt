package com.peihua.touchmonitor.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun <C : CharSequence> C?.ifNullOrEmpty(defaultValue: () -> C): C {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }
    return if (isNullOrEmpty()) defaultValue() else this
}


@OptIn(ExperimentalContracts::class)
fun CharSequence?.ifEmptyOrBlank(defaultValue: () -> CharSequence): CharSequence {
    contract {
        returns(false) implies (this@ifEmptyOrBlank != null)
    }
    return this ?: defaultValue()
}

//@OptIn(ExperimentalContracts::class)
fun String?.ifEmptyOrBlank(defaultValue: () -> String): String {
//    contract {
//        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
//    }
    return if (isNullOrEmpty()) defaultValue() else this
}