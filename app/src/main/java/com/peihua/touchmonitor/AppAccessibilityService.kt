package com.peihua.touchmonitor

import android.accessibilityservice.AccessibilityService
import android.media.AudioManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.ui.Settings
import com.peihua.touchmonitor.ui.settingsStore
import com.peihua.touchmonitor.utils.CommonDeviceLocks
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.wLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppAccessibilityService : AccessibilityService(), CoroutineScope by WorkScope() {
    private var isProcesserRunning = false
    private var isServiceRunning = false
    private val deviceLocks = CommonDeviceLocks()
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
        isServiceRunning = false
        isProcesserRunning = false
        changeSystemSettings(true)
        cancel()
    }

    override fun onDestroy() {
        isServiceRunning = false
        isProcesserRunning = false
        super.onDestroy()
        changeSystemSettings(true)
        cancel()
    }

    private val settings = mutableStateOf(Settings("", Orientation.Vertical, true))
    private var isChangeBrightness = false
    private var backupBrightness = 0
    private var backupSoundMute = false
    private var backupBrightnessMode =
        android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

    private fun changeSystemSettings(isRest: Boolean) {
        if (isRest) {
            changeBrightness(false)
            deviceLocks.release()
        } else {
            changeBrightness(settings.value.isBrightnessMin)
            deviceLocks.acquire(this)
        }
    }

    private fun changeBrightness(isBrightnessMin: Boolean) {
        if (android.provider.Settings.System.canWrite(this)) {
            if (isBrightnessMin) {
                isChangeBrightness = true
                backupBrightness = getSystemLight()
                backupBrightnessMode = getSystemLightMode()
                setSystemLightMode(android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                setSystemLight(0)
                backupSoundMute = getSoundMute()
                setSoundMute(true)
            } else if (isChangeBrightness) {
                isChangeBrightness = false
                setSystemLightMode(backupBrightnessMode)
                setSystemLight(backupBrightness)
                setSoundMute(backupSoundMute)
            }
        }
    }

    private var mProcessRunner: AutomaticallyWatchShortVideosWorker? = null
    private fun chaneState(isProcesserRunning: Boolean) {
        dLog { "Service start>>>> this.isProcesserRunning:${this.isProcesserRunning},isProcesserRunning:$isProcesserRunning" }
        if (this.isProcesserRunning != isProcesserRunning) {
            this.isProcesserRunning = isProcesserRunning
            changeSystemSettings(!isProcesserRunning)
            if (isProcesserRunning) {
                mProcessRunner = AutomaticallyWatchShortVideosWorker(this, settings.value)
                mProcessRunner?.onStart()
                dLog { "start  processer>>>>>" }
            } else {
                dLog { "stop processer>>>> " }
                mProcessRunner?.onStop()
                mProcessRunner = null
            }
        }
    }

    private fun runningService() {
        dLog { "runningService>>>>start" }
        val result = settingsStore.data
        launch {
            result.collect {
                settings.value = it
                mProcessRunner?.changeSettings(it)
            }
        }
        launch {
            dLog { "runningService>>>><>《》《》<<<<<start" }
            settings.value = result.first()
            isServiceRunning = true
            dLog { "runningService>>>><><<<<<start" }
            while (isServiceRunning) {
                val currentPackageName = rootInActiveWindow?.packageName
                val isProcesserRunning = settings.value.packageName == currentPackageName
                dLog { "settings.value:${settings.value}" }
                dLog { "Service start>>>> currentPackageName:$currentPackageName,isProcesserRunning:$isProcesserRunning" }
                chaneState(isProcesserRunning)
                delay(1000)
            }
        }
    }


    /**
     * 调节当前屏幕亮度
     */
    private fun setSystemLight(brightness: Int) {
        android.provider.Settings.System.putInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS,
            brightness
        )
    }

    /**
     * 获取当前屏幕亮度
     */
    private fun getSystemLight(): Int {
        return android.provider.Settings.System.getInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS,
            0
        )
    }

    private fun getSystemLightMode(): Int {
        return android.provider.Settings.System.getInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
            0
        )
    }

    private fun setSystemLightMode(mode: Int) {
        android.provider.Settings.System.putInt(
            contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
            mode
        )
    }

    private fun getSoundMute(): Boolean {
        val audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        return audioManager?.isStreamMute(AudioManager.STREAM_MUSIC) == true
    }

    private fun setSoundMute(isMute: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
        if (isMute) {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        } else {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
        }
    }

    fun AudioManager?.setStreamMute(streamType: Int, state: Boolean) {
        wLog { "setStreamMute is deprecated. adjustStreamVolume should be used instead." }
        val direction = if (state) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
        if (streamType == AudioManager.USE_DEFAULT_STREAM_TYPE) {
            this?.adjustSuggestedStreamVolume(direction, streamType, 0)
        } else {
            this?.adjustStreamVolume(streamType, direction, 0)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // 启动定时执行手势
        runningService()
        dLog { "onServiceConnected" }
    }
}