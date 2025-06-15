package com.peihua.touchmonitor

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.ui.AppLocalContext
import com.peihua.touchmonitor.ui.MainScreen
import com.peihua.touchmonitor.ui.theme.ToutchMonitorTheme
import com.peihua8858.permissions.core.requestPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLocalContext.context = application
        requestPermissions(Manifest.permission.QUERY_ALL_PACKAGES) {
            onGranted {
                setContent {
                    ToutchMonitorTheme {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                        ) { innerPadding ->
                            MainScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
            onDenied {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
                finish()
            }
        }

    }
}
