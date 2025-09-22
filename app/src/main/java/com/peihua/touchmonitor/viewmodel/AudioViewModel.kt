package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.request

class AudioViewModel(application: Application) : BaseMediaViewModel(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<MediaHeader>>>(ResultData.Initialize())
    override val columns: Array<String>
        get() = arrayOf(*super.columns, MediaStore.Video.Media.DURATION)
    fun requestAudio(@SortType sortType: Int = SortType.SORT_TYPE_DATE_ASC) {
        request(pictureState) {
            queryCursor(QUERY_TYPE_AUDIO,sortType= sortType) { cursor, mediaData ->
                val duration = cursor.getLong(MediaStore.Video.Media.DURATION)
                mediaData.duration = getDurationString(duration)
                mediaData
            }
        }
    }

}