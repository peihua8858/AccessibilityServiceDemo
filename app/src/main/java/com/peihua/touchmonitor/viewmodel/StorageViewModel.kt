package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.peihua.compose.array.isNonEmpty
import com.peihua.compose.file.formatSize
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.formatToDate
import com.peihua.touchmonitor.utils.request
import java.io.File

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    val storageState = mutableStateOf<ResultData<MutableList<MediaData>>>(ResultData.Initialize())
    private val homeDir = Environment.getExternalStorageDirectory().absolutePath
    val folderState = mutableStateListOf<Pair<String, String>>()
    fun request(filePath: String) {
        val directory = filePath.subSequence(filePath.lastIndexOf("/") + 1, filePath.length).toString()
        request(filePath, directory)
    }

    fun request(filePath: String, directory: String) {
        if (folderState.isEmpty() || filePath == homeDir) {
            folderState.clear()
            folderState.add("Home" to filePath)
        }
        if (!folderState.contains(directory to filePath)) {
            folderState.add(directory to filePath)
        } else {
            val cIndex = folderState.indexOf(directory to filePath)
            var index = folderState.size - 1
            while (index > cIndex) {
                folderState.removeAt(index)
                index--
            }
        }
        request(storageState) {
            val result = arrayListOf<MediaData>()
            val file = File(filePath)
            if (file.exists()) {
                val files = file.listFiles()
                if (files.isNonEmpty()) {
                    for ((index, item) in files.withIndex()) {
                        result.add(
                            MediaData(
                                fileName = item.name,
                                filePath = item.absolutePath,
                                fileSize = item.formatSize(),
                                dateValue = item.lastModified(),
                                dateFormat = item.lastModified().formatToDate("yyyy-MM-dd HH:mm:ss"),
                                isDirectory = item.isDirectory,
                                isFile = item.isFile
                            )
                        )
                    }
                }
            }
            result
        }
    }
}