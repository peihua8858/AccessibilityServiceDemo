package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getString
import com.peihua.touchmonitor.utils.request

class ZipViewModel(application: Application, savedStateHandle: SavedStateHandle) : MediaViewModel(application, savedStateHandle) {
    init {
        mediaType = QUERY_TYPE_ZIP
    }

    override val filter: (Cursor) -> Boolean
        get() = {
            val path = it.getString(MediaStore.MediaColumns.DATA)
            path.endsWith("zip", true)
        }

}