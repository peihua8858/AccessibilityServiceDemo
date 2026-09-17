package com.peihua.touchmonitor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.ServiceApp

/**
 * 首页
 */
class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServiceApp()
        }
    }
}

/**
 * 自动刷屏
 */
class AutoScrollScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServiceApp(defaultPage = AppRouter.AutoScroller)
        }
    }
}