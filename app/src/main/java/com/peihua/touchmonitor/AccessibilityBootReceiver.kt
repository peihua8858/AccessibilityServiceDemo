package com.peihua.touchmonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.startAccessibilitySettings

class AccessibilityBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        dLog { "AccessibilityBootReceiver onReceive" }
       context?.startAccessibilitySettings()
    }
}