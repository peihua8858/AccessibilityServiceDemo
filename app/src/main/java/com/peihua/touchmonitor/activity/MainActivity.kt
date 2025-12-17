package com.peihua.touchmonitor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.concurrent.AtomicBoolean
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.ServiceApp
import com.peihua.touchmonitor.utils.showToast
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val keepOnScreenCondition = AtomicBoolean(true)
    var lastClickTime = System.currentTimeMillis()
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepOnScreenCondition.get() }
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val time = System.currentTimeMillis()
                //说明是主页
                if (time - lastClickTime >= 2000) {
                    //如果两次单击事件大于2秒，则提示用户再单击一次
                    lastClickTime = time
                    showToast(R.string.app_exit_tips)
                } else {
                    //退出app但不杀死app，直接进入系统桌面
                    moveTaskToBack(true)
                }
            }
        })
        setContent {
            LaunchedEffect(null) {
                delay(300)
                keepOnScreenCondition.compareAndSet(true, false)
            }
            ServiceApp(Modifier)
        }
    }
}