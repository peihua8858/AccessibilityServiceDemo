package com.peihua.touchmonitor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.concurrent.AtomicBoolean
import com.peihua.touchmonitor.ui.ServiceApp
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val keepOnScreenCondition = AtomicBoolean(true)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepOnScreenCondition.get() }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(null) {
                delay(300)
                keepOnScreenCondition.compareAndSet(true, false)
            }
            ServiceApp(Modifier)
        }
    }
}