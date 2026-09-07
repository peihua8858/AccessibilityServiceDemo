package com.peihua.touchmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.data.download.DownloadContainer
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.notification.NotificationChannels
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

/**
 * m3u8 下载的前台服务。
 *
 * 只做三件事：把进程保活、把引擎状态映射成通知、把 UI 的命令转成引擎调用。
 * 真正的下载逻辑全在 [com.peihua.touchmonitor.data.download.M3u8DownloadEngine]。
 *
 * UI 不 bind 这个服务：状态通过进程内单例 `DownloadContainer.engine.progress` 读，
 * 命令通过 `startForegroundService` 下，省掉 ServiceConnection 的生命周期地狱。
 */
class M3u8DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val engine get() = DownloadContainer.engine
    private var wakeLock: PowerManager.WakeLock? = null
    private var started = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        observeProgress()
        observeIdle()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dLog { "服务收到命令：action=${intent?.action}, taskId=${intent?.taskId()}" }
        // 必须是第一行，在任何 suspend/DB 操作之前：超过 10 秒会抛
        // ForegroundServiceDidNotStartInTimeException
        if (!promoteToForeground()) {
            eLog { "前台服务启动失败，下载任务将无法执行" }
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent?.action) {
            ACTION_START -> intent.taskId()?.let { engine.start(it) }
            ACTION_PAUSE -> intent.taskId()?.let { engine.stop(it) }
            ACTION_PAUSE_ALL -> engine.stopAll(getString(R.string.download_paused_manual))
            ACTION_RESTART -> intent.taskId()?.let { id -> scope.launch { engine.restart(id) } }
            else -> dLog { "未知命令：${intent?.action}" }
        }
        acquireWakeLock()
        // 绝不用 START_STICKY：被杀后系统用 null intent 重启服务，此时 App 在后台，
        // Android 12+ 禁止后台启动 FGS，会直接崩 ForegroundServiceStartNotAllowedException
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }

    /** Android 15 起 dataSync 每天累计约 6 小时，超时必须自己收尾，否则被系统强杀 */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onTimeout(startId: Int, fgsType: Int) = handleTimeout()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onTimeout(startId: Int) = handleTimeout()

    private fun handleTimeout() {
        eLog { "前台服务时长超限，暂停全部任务" }
        // 状态已经在 DB 里，UI 上呈现为"可继续"而不是"失败"
        engine.stopAll(getString(R.string.download_paused_timeout))
        shutdown()
    }

    private fun promoteToForeground(): Boolean {
        if (started) return true
        val notification = DownloadNotifications.ongoing(
            this,
            DownloadNotifications.summarize(engine.progress.value.values),
        )
        return try {
            ServiceCompat.startForeground(
                this,
                NotificationChannels.ID_DOWNLOAD_ONGOING,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                },
            )
            started = true
            true
        } catch (e: Exception) {
            // 31+ 的 ForegroundServiceStartNotAllowedException 与各种 SecurityException
            eLog { "无法启动前台服务：${e.message}" }
            false
        }
    }

    /** 通知刷新必须节流：系统对同包通知入队 >10/s 会直接丢弃 */
    @OptIn(FlowPreview::class)
    private fun observeProgress() {
        scope.launch {
            engine.progress
                .map { DownloadNotifications.summarize(it.values) }
                .sample(NOTIFY_INTERVAL_MS)
                .distinctUntilChanged()
                .collect { summary ->
                    if (!started) return@collect
                    runCatching {
                        NotificationManagerCompat.from(this@M3u8DownloadService).notify(
                            NotificationChannels.ID_DOWNLOAD_ONGOING,
                            DownloadNotifications.ongoing(this@M3u8DownloadService, summary),
                        )
                    }
                }
        }
    }

    /** 以 engine.hasActive 为唯一真相判断空闲 */
    @OptIn(FlowPreview::class)
    private fun observeIdle() {
        scope.launch {
            engine.hasActive
                .debounce(IDLE_DEBOUNCE_MS)
                .collect { active ->
                    if (!active && started) {
                        dLog { "没有活跃任务，服务自停" }
                        notifyComplete()
                        shutdown()
                    }
                }
        }
    }

    private fun notifyComplete() {
        val finishedCount = engine.progress.value.values.count {
            it.stage == DownloadTaskStage.FINISHED
        }
        if (finishedCount <= 0) return
        runCatching {
            NotificationManagerCompat.from(this).notify(
                NotificationChannels.ID_DOWNLOAD_COMPLETE,
                DownloadNotifications.complete(
                    this,
                    getString(R.string.download_notification_complete_count, finishedCount),
                ),
            )
        }
    }

    private fun shutdown() {
        releaseWakeLock()
        // 不用 stopSelf(startId)：多命令场景下 startId 语义会打架
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        started = false
        stopSelf()
    }

    /**
     * 前台服务不等于持有 wake lock：灭屏进 Doze 后 CPU/网络仍可能被切。
     */
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService<PowerManager>() ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            // 兜底超时，避免异常路径下永久持有
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        wakeLock = null
    }

    private fun Intent.taskId(): Long? = getLongExtra(EXTRA_TASK_ID, -1L).takeIf { it > 0L }

    companion object {
        const val ACTION_START = "com.peihua.touchmonitor.action.DOWNLOAD_START"
        const val ACTION_PAUSE = "com.peihua.touchmonitor.action.DOWNLOAD_PAUSE"
        const val ACTION_PAUSE_ALL = "com.peihua.touchmonitor.action.DOWNLOAD_PAUSE_ALL"
        const val ACTION_RESTART = "com.peihua.touchmonitor.action.DOWNLOAD_RESTART"
        const val EXTRA_TASK_ID = "task_id"

        private const val WAKE_LOCK_TAG = "m3u8:download"
        private const val WAKE_LOCK_TIMEOUT_MS = 6 * 60 * 60 * 1000L
        private const val NOTIFY_INTERVAL_MS = 1_000L
        private const val IDLE_DEBOUNCE_MS = 2_000L

        fun start(context: Context, taskId: Long) = send(context, ACTION_START, taskId)

        fun pause(context: Context, taskId: Long) = send(context, ACTION_PAUSE, taskId)

        fun restart(context: Context, taskId: Long) = send(context, ACTION_RESTART, taskId)

        fun pauseAll(context: Context) = send(context, ACTION_PAUSE_ALL, null)

        private fun send(context: Context, action: String, taskId: Long?) {
            val intent = Intent(context, M3u8DownloadService::class.java).setAction(action)
            taskId?.let { intent.putExtra(EXTRA_TASK_ID, it) }
            runCatching { ContextCompat.startForegroundService(context, intent) }
                .onFailure { eLog { "启动下载服务失败：${it.message}" } }
        }
    }
}
