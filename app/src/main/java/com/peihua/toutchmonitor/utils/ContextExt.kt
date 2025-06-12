package com.peihua.toutchmonitor.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

fun Context.dimenOffset(dip: Int): Int {
    return resources.getDimensionPixelOffset(dip)
}

fun View.dimenOffset(dip: Int): Int {
    return resources.getDimensionPixelSize(dip)
}

fun View.getColor(color: Int): Int {
    return ContextCompat.getColor(context, color)
}

fun Context.registerReceiverCompat(
    receiver: BroadcastReceiver,
    filter: IntentFilter,
): Intent? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter)
    } else {
        registerReceiver(
            receiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )

    }
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels
val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

object ContextExt {

    @JvmStatic
    fun Context.isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}