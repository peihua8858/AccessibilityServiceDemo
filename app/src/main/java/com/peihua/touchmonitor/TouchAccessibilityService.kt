package com.peihua.touchmonitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.peihua.touchmonitor.utils.CommonDeviceLocks
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.screenHeight
import com.peihua.touchmonitor.utils.screenWidth
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
        6_000L,
        7_000L,
        8_000L,
        9_000L,
        2_000L,
        3_000L,
        4_000L,
        2_000L,
        3_000L,
        4_000L,
        5_000L,
        2_000L,
        3_000L,
        4_000L,
    )
    private val isUpSwipe = arrayOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 0)
    private val mLock = ReentrantLock()
    private val mCondition = mLock.newCondition()

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 处理事件
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            dLog { "rootNode:$rootNode" }
        }
    }

    override fun onInterrupt() {
        // 处理中断
        isRunning = false
    }

    // 定时执行手势
    private fun startSwipeTask() {
        launch {
            isRunning = true
            var oldTime = times.min()
            var oldSwipe = false
            val maxTime = times.max()
            while (isRunning) {
                val width = screenWidth
                val height = screenHeight
                var isSwipe = isUpSwipe.random()
                var time = times.random()
                dLog { "withLock>>>> await" }
                awaitPerformSwipeGesture(width / 2f, height / 2f, isSwipe == 1)
                dLog { "withLock>>>> await end" }
                dLog { "exec next line" }
                if (oldTime == maxTime) {
                    oldTime = times.min()
                }
                while (oldTime < time) {
                    time = times.random()
                }
                oldTime = time
                delay(time) // 每2秒执行一次
            }
        }
    }
    // For background processing
    @Suppress("SuspendFunctionOnCoroutineScope")
    private suspend fun CoroutineScope.awaitPerformSwipeGesture(centerX: Float,
                                                                           centerY: Float,
                                                                           isUpSwipe: Boolean = true,): Boolean {
        val deferred = CompletableDeferred<Boolean>()
            try {
                dLog { "center position:($centerX,$centerY)" }
                val path = Path();
                path.moveTo(centerX.toFloat(), ((centerY * if (isUpSwipe) 1.5 else 0.5).toFloat())); //起点坐标。
                path.lineTo(centerX.toFloat(), ((centerY * if (isUpSwipe) 0.5 else 1.5).toFloat())); //终点坐标。
                val builder = GestureDescription.Builder();

                val gestureDescription = builder.addStroke(
                    GestureDescription.StrokeDescription(path, 0, 800)
                ).build()
                var result =false;
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
                result=  dispatchGesture(gestureDescription, gestureCallback, null);
            } catch (throwable: Throwable) {
                deferred.completeExceptionally(throwable)
            }
        return deferred.await()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 开启屏保长亮
        deviceLocks.acquire(this)
        // 启动定时执行手势
        startSwipeTask()
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