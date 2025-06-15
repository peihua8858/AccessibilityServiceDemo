package com.peihua.touchmonitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.ui.AppLocalContext
import com.peihua.touchmonitor.ui.DataManager
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.utils.CommonDeviceLocks
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.screenHeight
import com.peihua.touchmonitor.utils.screenWidth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock


class TouchAccessibilityService : AccessibilityService(), CoroutineScope by WorkScope() {
    private var isRunning = false
    private val deviceLocks = CommonDeviceLocks()
    private val times = arrayOf(
        2_000L,
        3_000L,
        4_000L,
        2_000L,
        3_000L,
        4_000L,
        5_000L,
        3_000L,
        4_000L,
        5_000L,
        6_000L,
        7_000L,
        3_000L,
        4_000L,
        8_000L,
        9_000L,
        3_000L,
        4_000L,
        2_000L,
        3_000L,
        4_000L,
        5_000L,
        3_000L,
        4_000L,
        3_000L,
        4_000L,
        5_000L,
        5_000L,
        5_000L,
        5_000L,
    )
    private val isUpSwipe = arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
    private val isDownSwipe = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
    private val mLock = ReentrantLock()
    private val mCondition = mLock.newCondition()
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
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    val settings = mutableStateOf<Settings>(Settings("", Orientation.Vertical, true))

    // 定时执行手势
    private fun startSwipeTask() {
        launch {
            val result = DataManager.querySettings()
            result.collect {
                settings.value = it
            }
        }
        launch {
            isRunning = true
            var oldTime = times.min()
            var oldSwipe = false
            val maxTime = times.max()
            dLog { "Service start>>>>" }

            while (isRunning) {
                dLog { "withLock>>>>start" }
                extGesture(settings.value)
                dLog { "exec next line" }
                var time = times.random()
                if (oldTime == maxTime) {
                    oldTime = times.min()
                }
                while (oldTime < time) {
                    time = times.random()
                }
                oldTime = time
                delay(time) // 每2秒执行一次
                dLog { "withLock>>>>end" }
            }
        }
    }

    private suspend fun extGesture(settings: Settings?) {
        dLog { "withLock>>>>settings:${settings?.packageName}" }
        val rootNode = rootInActiveWindow
        dLog { "withLock>>>>rootNode:${rootNode}" }
        dLog { "withLock>>>>rootNode:${rootNode?.packageName}" }
        if (settings != null) {
            if (rootNode != null) {
                dLog { "withLock>>>>currentPackage:${rootNode.packageName}" }
                if (settings.packageName.isNotEmpty() && settings.packageName != rootNode.packageName) {
                    dLog { "withLock>>>>setting packageName:${settings.packageName}" }
                    return
                }
            }
        }
        val width = screenWidth
        val height = screenHeight
        var isSwipe = isUpSwipe.random()

        dLog { "withLock>>>>awaitPerformSwipeGesture await" }
        val scrollResult = awaitPerformSwipeGesture(width / 2f, height / 2f, isSwipe == 1)
        dLog { "withLock>>>>awaitPerformSwipeGesture await end scrollResult:$scrollResult" }
        val isDoubleSaver = settings?.isDoubleSaver == true
        if (isDoubleSaver && isDownSwipe.random() == 1) {
            delay(times.random()) // 每2秒执行一次
            dLog { "withLock>>>>performDoubleClickGesture await" }
            val doubleClick = performDoubleClickGesture(width / 2f, height / 2f)
            dLog { "withLock>>>>performDoubleClickGesture await end doubleClick:$doubleClick" }
        }
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

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 开启屏保长亮
        deviceLocks.acquire(this)
        // 启动定时执行手势
        startSwipeTask()
        AppLocalContext.context = application
        dLog { "onServiceConnected" }
    }

    // 定义并执行滑动手势的方法
    private fun performSwipeGesture(
        centerX: Float,
        centerY: Float,
        isUpSwipe: Boolean = true,
    ): Boolean {
        dLog { "center position:($centerX,$centerY)" }
        val path = Path();
        path.moveTo(centerX.toFloat(), ((centerY * if (isUpSwipe) 1.5 else 0.5).toFloat())); //起点坐标。
        path.lineTo(centerX.toFloat(), ((centerY * if (isUpSwipe) 0.5 else 1.5).toFloat())); //终点坐标。
        val builder = GestureDescription.Builder();

        val gestureDescription = builder.addStroke(
            GestureDescription.StrokeDescription(path, 0, 800)
        ).build()
        //添加双击手势
        builder.addStroke(
            GestureDescription.StrokeDescription(path, 0, 800)
        )
        // 执行手势并尝试处理结果
        val gestureCallback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                // 滑动成功
                dLog { "dispatchGesture ScrollUp onCompleted." }
                path.close()
                mCondition.signal()
//                mLock.unlock()

            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                dLog { "dispatchGesture ScrollUp cancel." }
                mCondition.signal()
//                mLock.unlock()
                // 手势取消或失败，检查原因

            }
        }
        val isDispatched = dispatchGesture(gestureDescription, gestureCallback, null);
        return isDispatched;
    }
}