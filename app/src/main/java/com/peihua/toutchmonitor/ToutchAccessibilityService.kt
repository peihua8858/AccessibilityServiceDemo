package com.peihua.toutchmonitor

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.peihua.toutchmonitor.utils.WorkScope
import com.peihua.toutchmonitor.utils.dLog
import com.peihua.toutchmonitor.utils.screenHeight
import com.peihua.toutchmonitor.utils.screenWidth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class ToutchAccessibilityService : AccessibilityService(), CoroutineScope by WorkScope() {
    private var isRunning = false
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 处理事件
    }

    override fun onInterrupt() {
        // 处理中断
        isRunning = false
    }

    // 定义并执行滑动手势的方法
//    private fun performSwipeGesture() {
//        dLog { "performSwipeGesture" }
//        val path = Path().apply {
//            moveTo(500f, 1000f) // 起始点
//            lineTo(500f, 300f)  // 终点
//        }
//
//        val strokeDescription = GestureDescription.StrokeDescription(path, 0, 500)
//        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()
//
//        dispatchGesture(gestureDescription, null, null)
//    }
    private fun performSwipeGesture() {
        dLog { "performSwipeGesture" }
        val path = Path().apply {
            moveTo(100f, 300f) // 起始点，可根据需求调整
            lineTo(100f, 100f)  // 终点，可根据需求调整
        }

        val strokeDescription = GestureDescription.StrokeDescription(path, 0L, 500L)
        val gestureDescription = GestureDescription.Builder().addStroke(strokeDescription).build()

        // 执行手势并尝试处理结果
        val gestureCallback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                // 滑动成功
                dLog { "onCompleted" }
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                // 手势取消或失败，检查原因
                dLog { "onCancelled" }
            }
        }

        val result = dispatchGesture(gestureDescription, gestureCallback, null)
        dLog { "result:$result" }
    }

    // 定时执行手势
    private fun startSwipeTask() {
        launch {
            isRunning = true
            while (isRunning) {
                val width = screenWidth
                val height = screenHeight
                performSwipeGesture(width / 2f, height / 2f)
                delay(2000) // 每2秒执行一次
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startSwipeTask()
        dLog { "onServiceConnected" }
    }

    private fun performSwipeGesture(centerX: Float, centerY: Float): Boolean {
        dLog { "center position:($centerX,$centerY)" }
        val path = Path();
        path.moveTo(centerX.toFloat(), ((centerY * 1.5).toFloat())); //起点坐标。
        path.lineTo(centerX.toFloat(), ((centerY * 0.5).toFloat())); //终点坐标。
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