package com.peihua.touchmonitor.utils

import android.os.Build


/**
 * [Build.VERSION.SDK_INT]>= Android 13
 */
inline val isTiramisu: Boolean
    get() = Build.VERSION.SDK_INT >= 33

/**
 * [Build.VERSION.SDK_INT]>= Android 9
 */
inline val isOreo: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * [Build.VERSION.SDK_INT]>= Android 9
 */
inline val isPie: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
inline val isUpsideDownCake: Boolean
    get() = Build.VERSION.SDK_INT >= 34
inline val isQ: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/**
 * [Build.VERSION.SDK_INT]>= Android 11
 */
inline val isR: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
inline val isM: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
inline val isN: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N