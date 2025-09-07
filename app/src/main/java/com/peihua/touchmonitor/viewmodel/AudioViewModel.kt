package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.ContentResolver
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.application
import com.peihua.touchmonitor.model.PhotoData
import com.peihua.touchmonitor.model.PhotoHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.request

class AudioViewModel(application: Application) : BaseQueryViewModel<PhotoHeader>(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<PhotoHeader>>>(ResultData.Initialize())
    fun requestImages(sortType: Int) {
        request(pictureState) {
            val titleArray = arrayListOf<String>()
            val result = queryCursor(QUERY_TYPE_AUDIO) {cursor, result, path, fileName, formatTime, dateTime, fileSize ->
                val picture = PhotoData(
                    dateValue = dateTime,
                    fileName = fileName,
                    filePath = path,
                    size = fileSize
                )
                if (titleArray.contains(formatTime)) {
                    val index = titleArray.indexOf(formatTime)
                    dLog { ">>>>>formatTime:$formatTime,index:$index" }
                    result[index].addPhotoData(picture)
                } else {
                    dLog { ">>>>>formatTime:$formatTime" }
                    val photoHeader = PhotoHeader(title = formatTime).addPhotoData(picture)
                    titleArray.add(formatTime)
                    result.add(photoHeader)
                }
            }
            result.sortList(sortType)
        }
    }

    private fun ArrayList<PhotoHeader>.sortList(sortType: Int): ArrayList<PhotoHeader> {
        val comparator = when (sortType) {
            1 -> {
                Comparator { o1, o2 -> o1.fileName.compareTo(o2.fileName, true) }
            }

            2 -> {
                Comparator { o1, o2 -> o2.fileName.compareTo(o1.fileName, true) }
            }

            3 -> {
                Comparator { o1, o2 -> o1.size.compareTo(o2.size) }
            }

            4 -> {
                Comparator { o1, o2 -> o2.size.compareTo(o1.size) }
            }

            5 -> {
                Comparator { o1, o2 -> o1.dateValue.compareTo(o2.dateValue) }
            }

            6 -> {
                Comparator { o1, o2 -> o2.dateValue.compareTo(o1.dateValue) }
            }

            else -> {
                Comparator<PhotoData> { o1, o2 -> o1.dateValue.compareTo(o2.dateValue) }
            }
        }
        for ((index, item) in this.withIndex()) {
            item.photoList.sortWith(comparator = comparator)
        }
        return this
    }

    private val contentResolver: ContentResolver
        get() = application.contentResolver
}