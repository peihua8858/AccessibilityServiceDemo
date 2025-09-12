package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.request

class PictureViewModel(application: Application) : BaseMediaViewModel(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<MediaHeader>>>(ResultData.Initialize())
    fun requestImages(sortType: Int) {
        request(pictureState) {
            queryCursor(QUERY_TYPE_IMAGE, sortType)
        }
    }
}