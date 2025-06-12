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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TouchAccessibilityService : AccessibilityService(), CoroutineScope by WorkScope() {
    private var isRunning = false
    private val deviceLocks = CommonDeviceLocks()
    private val times = arrayOf(
        1_000L,
        2_000L,
        3_000L,
        4_000L,
        5_000L,
        6_000L,
        7_000L,
        8_000L,
        9_000L,
        1_000L,
        2_000L,
        3_000L,
        4_000L,
        5_000L,
    )
    private val isUpSwipe = arrayOf(true,true,true,true, false)

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
            while (isRunning) {
                val width = screenWidth
                val height = screenHeight
                performSwipeGesture(width / 2f, height / 2f,isUpSwipe.random())
                delay(times.random()) // 每2秒执行一次
            }
        }
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
                path.close()
                dLog { "dispatchGesture ScrollUp onCompleted." }
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                // 手势取消或失败，检查原因
                dLog { "dispatchGesture ScrollUp cancel." }
            }
        }
        val isDispatched = dispatchGesture(gestureDescription, gestureCallback, null);
        return isDispatched;
    }
}