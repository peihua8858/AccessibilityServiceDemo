package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.IntDef
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.savedstate.SavedStateRegistryOwner
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.ui.components.MenuItem
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.formatFileSize
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
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
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

            QUERY_TYPE_ZIP -> MediaStore.Files.getContentUri("external")
            else -> MediaStore.Files.getContentUri("external")
        }
        return queryCursor(uri, convert)
    }

    fun queryCursor(
        uri: Uri,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
    ): ArrayList<T> {
        val result = arrayListOf<T>()
        val cursor = contentResolver.query(uri, columns, null, null, "LOWER(date_modified) DESC")
        dLog { ">>>>>cursor.count:${cursor?.count}" }
        cursor?.use {
            while (it.moveToNext()) {
                if (filter(it)) {
                    val path = it.getString(MediaStore.MediaColumns.DATA)
                    val fileName = it.getString(MediaStore.MediaColumns.DISPLAY_NAME)
                    val fileSize = it.getLong(MediaStore.MediaColumns.SIZE)
                    val dateModified = it.getLong(MediaStore.MediaColumns.DATE_MODIFIED)
                    val formatTime = (dateModified * 1000).formatPictureDate()
                    dLog { ">>>>>formatTime:$formatTime" }
                    dLog { ">>>>>path:$path,\nfileName:$fileName,\nfileSize:$fileSize,\ndateModified:$dateModified" }
                    convert(it, result, path, fileName, formatTime, dateModified, fileSize)
                }
            }
        }
        dLog { ">>>>>result.size:${result.size}" }
        return result
    }

    open val columns: Array<String>
        get() = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
        )
    open val filter: (Cursor) -> Boolean
        get() = { true }

    private val contentResolver: ContentResolver
        get() = application.contentResolver

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

open class BaseMediaViewModel(application: Application) : BaseQueryViewModel<MediaHeader>(application) {
    fun queryCursor(
        queryType: Int,
        @SortType sortType: Int,
        convert: (Cursor, MediaData) -> MediaData = { cursor, media -> media },
    ): ArrayList<MediaHeader> {
        val titleArray = arrayListOf<String>()
        val result = queryCursor(queryType) { cursor, result, path, fileName, formatTime, dateTime, fileSize ->
            var media = MediaData(
                dateValue = dateTime,
                fileName = fileName,
                filePath = path,
                size = fileSize,
                fileSize = fileSize.formatFileSize()
            )
            media = convert(cursor, media)
            if (titleArray.contains(formatTime)) {
                val index = titleArray.indexOf(formatTime)
                dLog { ">>>>>formatTime:$formatTime,index:$index" }
                result[index].addMediaData(media)
            } else {
                dLog { ">>>>>formatTime:$formatTime" }
                val photoHeader = MediaHeader(title = formatTime).addMediaData(media)
                titleArray.add(formatTime)
                photoHeader.time=System.currentTimeMillis()
                result.add(photoHeader)
            }
        }
        result.sortList(sortType)
        return result
    }

    protected fun ArrayList<MediaHeader>.sortList(@SortType sortType: Int): ArrayList<MediaHeader> {
        dLog { ">>>>>sortType:$sortType,sortList:${this.size}" }
        val comparator = when (sortType) {
            SortType.SORT_TYPE_NAME_ASC -> {
                // 按文件名升序
                Comparator { o1, o2 -> o1.fileName.compareTo(o2.fileName, true) }
            }

            SortType.SORT_TYPE_NAME_DESC -> {
                // 按文件名降序
                Comparator { o1, o2 -> o2.fileName.compareTo(o1.fileName, true) }
            }

            SortType.SORT_TYPE_SIZE_ASC -> {
                // 按文件大小升序
                Comparator { o1, o2 -> o1.size.compareTo(o2.size) }
            }

            SortType.SORT_TYPE_SIZE_DESC -> {
                // 按文件大小降序
                Comparator { o1, o2 -> o2.size.compareTo(o1.size) }
            }

            SortType.SORT_TYPE_DATE_ASC -> {
                // 按文件日期升序
                Comparator { o1, o2 -> o1.dateValue.compareTo(o2.dateValue) }
            }

            SortType.SORT_TYPE_DATE_DESC -> {
                // 按文件日期降序
                Comparator { o1, o2 -> o2.dateValue.compareTo(o1.dateValue) }
            }

            else -> {
                // 按文件日期升序
                Comparator<MediaData> { o1, o2 -> o1.dateValue.compareTo(o2.dateValue) }
            }
        }
        for((index,item) in this.withIndex()) {
            item.mediaList.sortWith(comparator = comparator)
        }
        return this
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    SortType.SORT_TYPE_NAME_ASC,
    SortType.SORT_TYPE_NAME_DESC,
    SortType.SORT_TYPE_SIZE_ASC,
    SortType.SORT_TYPE_SIZE_DESC,
    SortType.SORT_TYPE_DATE_ASC,
    SortType.SORT_TYPE_DATE_DESC
)
annotation class SortType {
    companion object {
        const val SORT_TYPE_NAME_ASC = 1
        const val SORT_TYPE_NAME_DESC = 2
        const val SORT_TYPE_SIZE_ASC = 3
        const val SORT_TYPE_SIZE_DESC = 4
        const val SORT_TYPE_DATE_ASC = 5
        const val SORT_TYPE_DATE_DESC = 6

        fun createSortList(context: Context): List<MenuItem<Int>> {
            return listOf(
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_date_asc),
                    value = SORT_TYPE_DATE_ASC
                ),
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_date_desc),
                    value = SORT_TYPE_DATE_DESC
                ),
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_name_asc),
                    value = SORT_TYPE_NAME_ASC
                ),
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_name_desc),
                    value = SORT_TYPE_NAME_DESC
                ),
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_size_asc),
                    value = SORT_TYPE_SIZE_ASC
                ),
                MenuItem(
                    displayName = context.getString(R.string.text_sort_type_size_desc),
                    value = SORT_TYPE_SIZE_DESC
                ),
            )
        }
    }

//    class ViewModelFactory(
//        owner: SavedStateRegistryOwner,
//    ) : AbstractSavedStateViewModelFactory(owner, null) {
//
//        override fun <T : ViewModel> create(
//            key: String,
//            modelClass: Class<T>,
//            handle: SavedStateHandle
//        ): T {
//            if (modelClass.isAssignableFrom(GithubViewModel::class.java)) {
//                @Suppress("UNCHECKED_CAST")
//                return GithubViewModel(repository, handle) as T
//            }
//            throw IllegalArgumentException("Unknown ViewModel class")
//        }
//    }
}