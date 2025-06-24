package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.fz.common.file.read
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.model.LogModel
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.request
import java.io.File

class LogViewModel(application: Application) : AndroidViewModel(application) {
    val logState: MutableState<ResultData<List<LogModel>>> = mutableStateOf(ResultData.Initialize())
    val logDetailState: MutableState<ResultData<String>> = mutableStateOf(ResultData.Initialize())

    companion object {
        val APP_LOG_PATH = ServiceApplication.application.getExternalFilesDir(null)
        val APP_LOG_DIR = "Logcat"
        const val FILE_PATH = "/data/system/dropbox/"
    }

    fun requestData() {
        request(logState) {
            val parentFile = File(FILE_PATH)
            val logs = ArrayList<LogModel>()
            parentFile.listFiles()?.forEach {
                val model = LogModel(it.lastModified(), it.name, it.absolutePath)
                logs.add(model)
            }
            val appLogDir = File(APP_LOG_PATH, APP_LOG_DIR)
            appLogDir.listFiles()?.forEach {
                val model = LogModel(it.lastModified(), it.name, it.absolutePath)
                logs.add(model)
            }
            logs
        }
    }

    fun requestData(filePath: String) {
        request(logDetailState) {
            dLog { "requestData:$filePath" }
            val result = filePath.read() ?: ""
            dLog { "requestData:$result" }
            result
        }
    }
}