package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.media.ThumbnailUtils
import android.provider.CloudMediaProviderContract
import android.provider.MediaStore
import android.util.Size
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.application
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getVideoThumbnail
import com.peihua.touchmonitor.utils.getVideoThumbnailFromMediaMetadataRetriever
import com.peihua.touchmonitor.utils.request
import java.io.File

class VideoViewModel(application: Application) : BaseMediaViewModel(application) {
    val pictureState = mutableStateOf<ResultData<MutableList<MediaHeader>>>(ResultData.Initialize())
    override val columns: Array<String>
        get() = arrayOf(*super.columns, MediaStore.Video.Media.DURATION)

    fun requestImages(sortType: Int) {
        request(pictureState) {
            queryCursor(QUERY_TYPE_VIDEO, sortType) { cursor, mediaData ->
                val duration = cursor.getLong("duration")
                mediaData.duration = getDurationString(duration)
                val fileUri = mediaData.filePath?.toUri()
                dLog { "getVideoThumbnail>>>fileUri: $fileUri" }
                val bitmap = application.getVideoThumbnailFromMediaMetadataRetriever(fileUri, Size(640, 480))
                dLog { "getVideoThumbnail: $bitmap" }
                mediaData.thumbnailsBitmap = bitmap
                mediaData
            }
        }
    }
}