package com.peihua.touchmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.activity.MainActivity
import com.peihua.touchmonitor.data.download.TaskProgress
import com.peihua.touchmonitor.notification.NotificationChannels
import com.peihua.touchmonitor.utils.toHumanReadableRate

/**
 * 下载通知。只发一条汇总通知，不按任务发 —— 系统对同包通知入队有速率限制，
 * 多任务时逐个刷新会被丢弃并打 "Package enqueue rate is ..."。
 */
internal object DownloadNotifications {

    /** 常驻进度通知的内容摘要 */
    data class Summary(val activeCount: Int, val finished: Int, val total: Int, val bytesPerSec: Long)

    fun summarize(progress: Collection<TaskProgress>): Summary {
        val active = progress.filter { it.isActive }
        return Summary(
            activeCount = active.size,
            finished = active.sumOf { it.finishedSegments },
            total = active.sumOf { it.totalSegments },
            bytesPerSec = active.sumOf { it.bytesPerSec },
        )
    }

    fun ongoing(context: Context, summary: Summary): Notification {
        NotificationChannels.ensureDownloadChannel(context)
        val title = if (summary.activeCount > 1) {
            context.getString(R.string.download_notification_title_multi, summary.activeCount)
        } else {
            context.getString(R.string.download_notification_title)
        }
        val text = buildString {
            if (summary.total > 0) append("${summary.finished}/${summary.total}  ")
            append(summary.bytesPerSec.toHumanReadableRate())
        }
        return NotificationCompat.Builder(context, NotificationChannels.DOWNLOAD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setLocalOnly(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(summary.total.coerceAtLeast(1), summary.finished, summary.total <= 0)
            .setContentIntent(openApp(context))
            .addAction(
                R.drawable.ic_launcher_foreground,
                context.getString(R.string.download_notification_pause_all),
                command(context, M3u8DownloadService.ACTION_PAUSE_ALL, REQ_PAUSE_ALL),
            )
            .build()
    }

    /** 完成态用另一个通知 id：可滑掉、点击进 App，绝不把常驻通知改成"已完成"留着 */
    fun complete(context: Context, text: String): Notification {
        NotificationChannels.ensureDownloadChannel(context)
        return NotificationCompat.Builder(context, NotificationChannels.DOWNLOAD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.download_notification_complete))
            .setContentText(text)
            .setAutoCancel(true)
            .setLocalOnly(true)
            .setContentIntent(openApp(context))
            .build()
    }

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            REQ_OPEN_APP,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun command(context: Context, action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            context,
            requestCode,
            Intent(context, M3u8DownloadService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private const val REQ_OPEN_APP = 1001
    private const val REQ_PAUSE_ALL = 1002
}
