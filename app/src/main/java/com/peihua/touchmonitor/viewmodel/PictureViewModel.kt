package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.request

class PictureViewModel(application: Application) : BaseMediaViewModel(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<MediaHeader>>>(ResultData.Initialize())
    fun requestImages(@SortType sortType: Int = SortType.SORT_TYPE_DATE_ASC) {
        request(pictureState) {
            val (size,result) = queryCursor(QUERY_TYPE_IMAGE, sortType=sortType)
            result
        }
    }
}