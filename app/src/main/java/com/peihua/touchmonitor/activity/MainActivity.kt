package com.peihua.touchmonitor.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.concurrent.AtomicBoolean
import com.peihua.touchmonitor.ui.ServiceApp
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    val keepOnScreenCondition = AtomicBoolean(true)
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        window.navigationBarColor = android.graphics.Color.BLACK
        // Keep the splash screen visible for this Activity.
        splashScreen.setKeepOnScreenCondition { keepOnScreenCondition.get() }
        var isReady = false
        enableEdgeToEdge()
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            LaunchedEffect(null) {
                delay(300)
                isReady = true
                keepOnScreenCondition.compareAndSet(true,false)
            }
            ServiceApp(Modifier)
        }
    }
}