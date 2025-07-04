package com.peihua.touchmonitor

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.peihua.touchmonitor.ui.ServiceApp
import com.peihua8858.permissions.core.requestPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions(Manifest.permission.QUERY_ALL_PACKAGES, Manifest.permission.WRITE_SETTINGS) {
            onGranted {
                setContent {
                    ServiceApp()
                }
            }
            onDenied {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
                finish()
            }
        }

    }
}
