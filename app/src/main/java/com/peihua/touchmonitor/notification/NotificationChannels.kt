package com.peihua.touchmonitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.utils.isAtLeastO

/**
 * 通知渠道 id 必须是稳定的英文常量。
 *
 * 原来的实现用 `getString(R.string.app_name)` 当渠道 id，切换语言就会产生一个新渠道、旧渠道残留；
 * 取渠道时又用 `notificationChannelsCompat.first()`，一旦存在多个渠道就会拿错
 * （无障碍的前台通知可能落到 IMPORTANCE_LOW 的下载渠道上，变成静默且被折叠）。
 */
object NotificationChannels {
    const val ACCESSIBILITY = "accessibility_service"
    const val DOWNLOAD = "m3u8_download"

    const val ID_ACCESSIBILITY = 0x195288
    const val ID_DOWNLOAD_ONGOING = 0x195289
    const val ID_DOWNLOAD_COMPLETE = 0x19528A

    fun ensureAccessibilityChannel(context: Context) {
        ensure(
            context,
            NotificationChannels.ACCESSIBILITY,
            R.string.channel_name_accessibility,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
    }

    fun ensureDownloadChannel(context: Context) {
        ensure(
            context,
            NotificationChannels.DOWNLOAD,
            R.string.channel_name_download,
            NotificationManager.IMPORTANCE_LOW,
        )
    }

    private fun ensure(context: Context, id: String, nameRes: Int, importance: Int) {
        if (!isAtLeastO) return
        val manager = NotificationManagerCompat.from(context)
        if (manager.getNotificationChannelCompat(id) != null) return
        manager.createNotificationChannel(
            NotificationChannel(id, context.getString(nameRes), importance)
        )
    }
}
