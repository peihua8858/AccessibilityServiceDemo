package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.formatPictureDate
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getString
import com.peihua.touchmonitor.utils.isQ
import java.util.concurrent.TimeUnit

abstract class BaseQueryViewModel<T>(application: Application) : AndroidViewModel(application) {
    companion object {
        const val QUERY_TYPE_IMAGE = 1
        const val QUERY_TYPE_AUDIO = 2
        const val QUERY_TYPE_VIDEO = 3
        const val QUERY_TYPE_DOCUMENT = 4
        const val QUERY_TYPE_ZIP = 5

    }

    fun queryCursor(
        queryType: Int,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit
    ): ArrayList<T> {
        val uri = when (queryType) {
            QUERY_TYPE_IMAGE -> {
                if (isQ) MediaStore.Images.Media.getContentUri("external")
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            QUERY_TYPE_AUDIO -> {
                if (isQ) MediaStore.Audio.Media.getContentUri("external")
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            QUERY_TYPE_VIDEO -> {
                if (isQ) MediaStore.Video.Media.getContentUri("external")
                else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            else -> if (isQ) MediaStore.Images.Media.getContentUri("external")
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return queryCursor(uri, convert)
    }

    fun queryCursor(
        uri: Uri,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit
    ): ArrayList<T> {
        val result = arrayListOf<T>()
        val data = "_data"
        val cursor = contentResolver.query(uri, columns, null, null, "LOWER(date_modified) DESC")
        cursor?.use {
            while (it.moveToNext()) {
                val path = it.getString(data)
                val fileName = it.getString("_display_name")
                val fileSize = it.getLong("_size")
                val dateModified = it.getLong("date_modified")
                val formatTime = (dateModified * 1000).formatPictureDate()
                dLog { ">>>>>formatTime:$formatTime" }
                convert(it, result, path, fileName, formatTime, dateModified, fileSize)
            }
        }
        return result
    }

    private val contentResolver: ContentResolver
        get() = application.contentResolver

    open val columns: Array<String>
        get() = arrayOf("_data", "date_modified", "_display_name", "_size")

    protected fun getDurationString(duration: Long): String {
        val timeUnit = TimeUnit.MILLISECONDS
        val hours = timeUnit.toHours(duration)
        val minutes = timeUnit.toMinutes(duration)
        val seconds = timeUnit.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(timeUnit.toMinutes(duration))
        val minute = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        val second = if (seconds < 10) {
            "0$seconds"
        } else {
            seconds.toString()
        }
        val hour = if (hours < 10) {
            "0$hours"
        } else {
            hours.toString()
        }
        if (hours == 0L) {
            return "$minute:$second"
        }
        return "$hour:$minute:$second"
    }
}