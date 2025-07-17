package com.peihua.touchmonitor

import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityNodeInfo
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.utils.CommonDeviceLocks
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.screenHeight
import com.peihua.touchmonitor.utils.screenWidth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自动化观看短视频
 */
class AutomaticallyWatchShortVideosWorker(
    private val service: AppAccessibilityService,
    private var settings: Settings,
) :
    Runnable, CoroutineScope by WorkScope() {
    private var isProcesserRunning = false
    private val times = arrayOf(
        7,//7秒
        8,
        9,
        10,
        11,
        12,
        13,
        8,
        9,
        10,
        8,
        9,
        10,
        8,
        9,
        10,
        5,
        8,
        9,
        10,
        8,
        9,
        10,
        8,
        9,
        10,
    )
    private val isUpSwipe = arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
    private val isDownSwipe = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
    private val mDelayTimes = settings.delayTimes
    private var oldTime: Int
    private var maxTime: Int

    init {
        if (mDelayTimes.isEmpty()) {
            mDelayTimes.addAll(times.toList())
        }
        oldTime = mDelayTimes.min()
        maxTime = mDelayTimes.max()
    }

    fun changeSettings(settings: Settings) {
        mDelayTimes.clear()
        if (mDelayTimes.isEmpty()) {
            mDelayTimes.addAll(times.toList())
        }
        oldTime = mDelayTimes.min()
        maxTime = mDelayTimes.max()
    }

    override fun run() {
        launch {
            dLog { "Service start>>>>" }
            while (isProcesserRunning) {
                dLog { "withLock>>>>start" }
                extGesture(settings)
            }
        }
    }

    fun onStart() {
        isProcesserRunning = true
        run()
    }

    fun onStop() {
        isProcesserRunning = false
        cancel()
    }

    private suspend fun extGesture(settings: Settings?) {
        dLog { "withLock>>>>settings:${settings?.packageName}" }
        val rootNode = service.rootInActiveWindow
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
        val isRandomReverse = settings?.isRandomReverse ?: false
        dLog { ">>>>>isRandomReverse:$isRandomReverse" }
        val isSwipe = if (isRandomReverse) isUpSwipe.random() else 1
        dLog { ">>>>>isRandomReverse:$isRandomReverse,isSwipe:$isSwipe" }
        dLog { "withLock>>>>awaitPerformSwipeGesture await" }
        val scrollResult = awaitPerformSwipeGesture(isSwipe == 1)
        if (settings?.isSkipAdOrLive == true && scrollResult) {
            waiteTimeMillis(1_000)
            //跳过广告或直播
            val rootNode = service.rootInActiveWindow
            if (rootNode != null) {
                val liveNode = rootNode.findAccessibilityNodeInfosByText("直播中")
                val adNode = rootNode.findAccessibilityNodeInfosByText("广告")
                dLog { "withLock>>>>liveNode:${liveNode}" }
                if (!liveNode.isNullOrEmpty() || !adNode.isNullOrEmpty()) {
                    dLog { "withLock>>>>awaitPerformSwipeGesture await" }
                    val scrollResult = awaitPerformSwipeGesture(true)
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
            val doubleClick = performDoubleClickGesture()
            dLog { "withLock>>>>performDoubleClickGesture await end doubleClick:$doubleClick" }
        }
        waiteTime()
    }

    private suspend fun waiteTime() {
        dLog { "exec next line" }
        var tempTime = mDelayTimes.random()
        if (oldTime == maxTime) {
            oldTime = mDelayTimes.min()
        }
        while (oldTime > tempTime) {
            tempTime = mDelayTimes.random()
        }
        oldTime = tempTime
        waiteTimeMillis(tempTime * 1000L) // 每2秒执行一次
        dLog { "withLock>>>>end" }
    }

    private suspend fun waiteTimeMillis(time: Long) {
        dLog { "waite time$time" }
        delay(time)
    }

    // For background processing
    @Suppress("SuspendFunctionOnCoroutineScope")
    private suspend fun CoroutineScope.awaitPerformSwipeGesture(isUpSwipe: Boolean = true): Boolean {
        val width = service.screenWidth
        val height = service.screenHeight
        val centerX: Float = width / 2f
        val centerY: Float = height / 2f
        // 根据屏幕的密度调整滑动手势的参数
        val dpi = service.resources.displayMetrics.densityDpi
        val swipeFactor = if (dpi in DisplayMetrics.DENSITY_LOW..DisplayMetrics.DENSITY_HIGH) {
            1.2f
        } else if (dpi in DisplayMetrics.DENSITY_XHIGH..DisplayMetrics.DENSITY_XXHIGH) {
            0.9f
        } else if (dpi > DisplayMetrics.DENSITY_XXHIGH) {
            0.8f
        } else {
            1.0f
        }.coerceIn(0.5f, 1.5f) // 确保最小值和最大值
        dLog { "screenWidth:${width},screenHeight:${height},dpi:$dpi,swipeFactor:$swipeFactor,isUpSwipe:$isUpSwipe,centerX:$centerX,centerY:$centerY" }
        val deferred = CompletableDeferred<Boolean>()
        try {
            dLog { "center position:($centerX,$centerY)" }
            val path = Path()
            //起点坐标。
            val startY = ((centerY * swipeFactor * if (isUpSwipe) 1.6 else 0.4).toFloat())
            path.moveTo(centerX,startY )
            //终点坐标。[640,564][640,187.5] = [640,376.5]
            val endY= ((centerY * swipeFactor * if (isUpSwipe) 0.4 else 1.6).toFloat())
            path.lineTo(centerX, endY)
            dLog { "dispatchGesture,startX:${centerX},startY:$startY,endX:$centerX,endY:$endY, path:$path" }
            val builder = GestureDescription.Builder()
            val gestureDescription = builder.addStroke(
                GestureDescription.StrokeDescription(path, 0, (1000 * swipeFactor).toLong())
            ).build()
            // 执行手势并尝试处理结果
            val gestureCallback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    // 滑动成功
                    dLog { "dispatchGesture ScrollUp onCompleted." }
                    path.close()
                    deferred.complete(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    dLog { "dispatchGesture ScrollUp cancel." }
                    deferred.complete(false)
                    // 手势取消或失败，检查原因
                }
            }
            service.dispatchGesture(gestureDescription, gestureCallback, null);
        } catch (throwable: Throwable) {
            deferred.completeExceptionally(throwable)
        }
        return deferred.await()
    }

    private suspend fun CoroutineScope.performDoubleClickGesture(): Boolean {
        val width = service.screenWidth
        val height = service.screenHeight
        val centerX: Float = width / 2f
        val centerY: Float = height / 2f
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
            service.dispatchGesture(gestureDescription, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    dLog { "Double-click gesture completed successfully" }
                    deferred.complete(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    dLog { "Double-click gesture cancelled" }
                    deferred.complete(false)
                }
            }, null)
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
        }
        return deferred.await()
    }
}