package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.fz.common.array.isNonEmpty
import com.fz.common.file.formatSize
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.formatToDate
import com.peihua.touchmonitor.utils.request
import java.io.File

class StorageViewModel(application: Application) : AndroidViewModel(application) {
    val storageState = mutableStateOf<ResultData<MutableList<MediaData>>>(ResultData.Initialize())
    fun request(filePath: String) {
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
                                dateFormat=item.lastModified().formatToDate("yyyy-MM-dd HH:mm:ss"),
                                isDirectory = item.isDirectory
                            )
                        )
                    }
                }
            }
            result
        }
    }
}