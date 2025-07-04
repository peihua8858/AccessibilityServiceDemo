package com.peihua.touchmonitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.graphics.Path
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.data.settingsStore
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.utils.CommonDeviceLocks
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.screenHeight
import com.peihua.touchmonitor.utils.screenWidth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class TouchAccessibilityService : AccessibilityService(), CoroutineScope by WorkScope() {
    private var isRunning = false
    private val deviceLocks = CommonDeviceLocks()
    private val times = arrayOf(
        7_000L,
        8_000L,
        9_000L,
        10_000L,
        11_000L,
        12_000L,
        13_000L,
        8_000L,
        9_000L,
        10_000L,
        8_000L,
        9_000L,
        10_000L,
        8_000L,
        9_000L,
        10_000L,
        5_000L,
        8_000L,
        9_000L,
        10_000L,
        8_000L,
        9_000L,
        10_000L,
        8_000L,
        9_000L,
        10_000L,
    )
    private val isUpSwipe = arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
    private val isDownSwipe = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
    private var mPackageName: CharSequence = ""
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 处理事件
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("AppTracker", "当前前台应用: " + event.packageName)
            // 可以根据包名执行不同操作
            mPackageName = event.packageName
        }
    }

    override fun onInterrupt() {
        // 处理中断
        isRunning = false
        deviceLocks.release()
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
        deviceLocks.release()
        if (settings.value.isBrightnessMin && backupBrightness > 0) {
            setSystemLight(backupBrightness)
        }
    }

    val settings = mutableStateOf(Settings("", Orientation.Vertical, true))
    var oldTime = times.min()
    val maxTime = times.max()
    var backupBrightness = 0

    // 定时执行手势
    private fun startSwipeTask() {
        val result = settingsStore.data
        launch {
            result.collect {
                settings.value = it
                if (it.isBrightnessMin) {
                    backupBrightness = getSystemLight()
                    setSystemLight(0)
                }
               val mm: DisplayManager= getSystemService(Activity.DISPLAY_SERVICE) as DisplayManager
            }
        }

        launch {
            val settingsResult = result.first()
            if (settingsResult.isBrightnessMin) {
                backupBrightness = getSystemLight()
                setSystemLight(5)
            }
            settings.value = settingsResult
            isRunning = true
            dLog { "Service start>>>>" }
            while (isRunning) {
                dLog { "withLock>>>>start" }
                extGesture(settings.value)

            }
        }
    }

    private suspend fun extGesture(settings: Settings?) {
        val width = screenWidth
        val height = screenHeight
        dLog { "withLock>>>>settings:${settings?.packageName}" }
        val rootNode = rootInActiveWindow
        dLog { "withLock>>>>rootNode:${rootNode}" }
        dLog { "withLock>>>>rootNode:${rootNode?.packageName}" }
        if (rootNode != null) {
            if (settings != null) {
                dLog { "withLock>>>>currentPackage:${rootNode.packageName}" }
                if (settings.packageName.isNotEmpty() && settings.packageName != rootNode.packageName) {
                    dLog { "withLock>>>>setting packageName:${settings.packageName}" }
                    return
                }
            }
        }

        val isSwipe = isUpSwipe.random()
        dLog { "withLock>>>>awaitPerformSwipeGesture await" }
        val scrollResult = awaitPerformSwipeGesture(width / 2f, height / 2f, isSwipe == 1)
        if (settings?.isSkipAdOrLive == true && scrollResult) {
            waiteTimeMillis(1_000)
            //跳过广告或直播
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val liveNode = rootNode.findAccessibilityNodeInfosByText("直播中")
                val adNode = rootNode.findAccessibilityNodeInfosByText("广告")
                dLog { "withLock>>>>liveNode:${liveNode}" }
                if (!liveNode.isNullOrEmpty() || !adNode.isNullOrEmpty()) {
                    dLog { "withLock>>>>awaitPerformSwipeGesture await" }
                    val scrollResult = awaitPerformSwipeGesture(width / 2f, height / 2f, true)
                    dLog { "withLock>>>>awaitPerformSwipeGesture await end scrollResult:$scrollResult" }
                    waiteTimeMillis(2_000)
                    return
                }
            }
        }
        dLog { "withLock>>>>awaitPerformSwipeGesture await end scrollResult:$scrollResult" }
        val isDoubleSaver = settings?.isDoubleSaver == true
        if (isDoubleSaver && isDownSwipe.random() == 1) {
            waiteTime() // 每2秒执行一次
            dLog { "withLock>>>>performDoubleClickGesture await" }
            val doubleClick = performDoubleClickGesture(width / 2f, height / 2f)
            dLog { "withLock>>>>performDoubleClickGesture await end doubleClick:$doubleClick" }
        }
        waiteTime()
    }

    private suspend fun waiteTime() {
        dLog { "exec next line" }
        var tempTime = times.random()
        if (oldTime == maxTime) {
            oldTime = times.min()
        }
        while (oldTime > tempTime) {
            tempTime = times.random()
        }
        oldTime = tempTime
        waiteTimeMillis(tempTime) // 每2秒执行一次
        dLog { "withLock>>>>end" }
    }

    private suspend fun waiteTimeMillis(time: Long) {
        dLog { "waite time$time" }
        delay(time)
    }

    // For background processing
    @Suppress("SuspendFunctionOnCoroutineScope")
    private suspend fun CoroutineScope.awaitPerformSwipeGesture(
        centerX: Float,
        centerY: Float,
        isUpSwipe: Boolean = true,
    ): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        try {
            dLog { "center position:($centerX,$centerY)" }
            val path = Path();
            path.moveTo(
                centerX.toFloat(),
                ((centerY * if (isUpSwipe) 1.5 else 0.5).toFloat())
            ); //起点坐标。
            path.lineTo(
                centerX.toFloat(),
                ((centerY * if (isUpSwipe) 0.5 else 1.5).toFloat())
            ); //终点坐标。
            val builder = GestureDescription.Builder();

            val gestureDescription = builder.addStroke(
                GestureDescription.StrokeDescription(path, 0, 800)
            ).build()
            var result = false;
            // 执行手势并尝试处理结果
            val gestureCallback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    // 滑动成功
                    dLog { "dispatchGesture ScrollUp onCompleted." }
                    path.close()
                    deferred.complete(result)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    dLog { "dispatchGesture ScrollUp cancel." }
                    deferred.complete(result)
                    // 手势取消或失败，检查原因

                }
            }
            result = dispatchGesture(gestureDescription, gestureCallback, null);
        } catch (throwable: Throwable) {
            deferred.completeExceptionally(throwable)
        }
        return deferred.await()
    }

    private suspend fun CoroutineScope.performDoubleClickGesture(
        centerX: Float,
        centerY: Float,
    ): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        try {// 第一次点击路径
            val path1 = Path().apply {
                moveTo(centerX, centerY)
                lineTo(centerX + 1, centerY)  // 小移动来表示点击，不然会被认为无效路径
            }

            // 第二次点击路径，略过一段时间
            val path2 = Path().apply {
                moveTo(centerX, centerY)
                lineTo(centerX + 1, centerY)  // 小移动
            }

            val strokeDescription1 = GestureDescription.StrokeDescription(path1, 0L, 50L)
            val strokeDescription2 =
                GestureDescription.StrokeDescription(path2, 100L, 50L)  // 偏移时间100ms

            val gestureBuilder = GestureDescription.Builder()
                .addStroke(strokeDescription1)
                .addStroke(strokeDescription2)

            val gestureDescription = gestureBuilder.build()
            var result = false
            result = dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    dLog { "Double-click gesture completed successfully" }
                    deferred.complete(result)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    dLog { "Double-click gesture cancelled" }
                    deferred.complete(result)
                }
            }, null)
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
        }
        return deferred.await()
    }

    /**
     * 调节当前屏幕亮度
     */
    fun setSystemLight(brightness: Int) {
        android.provider.Settings.System.putInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS,
            brightness
        )
    }

    /**
     * 获取当前屏幕亮度
     */
    fun getSystemLight(): Int {
        return android.provider.Settings.System.getInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS,
            0
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 开启屏保长亮
        deviceLocks.acquire(this)
        // 启动定时执行手势
        startSwipeTask()
        dLog { "onServiceConnected" }
    }
}