package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getString
import com.peihua.touchmonitor.utils.request

class ZipViewModel(application: Application) : BaseMediaViewModel(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<MediaHeader>>>(ResultData.Initialize())
    fun requestAudio(@SortType sortType: Int = SortType.SORT_TYPE_DATE_ASC) {
        request(pictureState) {
            queryCursor(QUERY_TYPE_ZIP, sortType)
        }
    }

    override val filter: (Cursor) -> Boolean
        get() = {
            val path = it.getString(MediaStore.MediaColumns.DATA)
            path.endsWith("zip", true)
        }

}