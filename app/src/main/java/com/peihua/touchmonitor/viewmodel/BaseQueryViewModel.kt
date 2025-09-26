package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.IntDef
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.ui.components.MenuItem
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.formatFileSize
import com.peihua.touchmonitor.utils.formatPictureDate
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getString
import com.peihua.touchmonitor.utils.isAtLeastO
import com.peihua.touchmonitor.utils.isAtLeastQ
import com.peihua.touchmonitor.utils.isAtLeastR
import java.util.concurrent.TimeUnit

object QueryType {
    const val QUERY_TYPE_IMAGE = 1
    const val QUERY_TYPE_AUDIO = 2
    const val QUERY_TYPE_VIDEO = 3
    const val QUERY_TYPE_DOCUMENT = 4
    const val QUERY_TYPE_ZIP = 5
}

abstract class BaseQueryViewModel<T>(application: Application) : AndroidViewModel(application) {
    companion object {
        const val QUERY_TYPE_IMAGE = QueryType.QUERY_TYPE_IMAGE
        const val QUERY_TYPE_AUDIO = QueryType.QUERY_TYPE_AUDIO
        const val QUERY_TYPE_VIDEO = QueryType.QUERY_TYPE_VIDEO
        const val QUERY_TYPE_DOCUMENT = QueryType.QUERY_TYPE_DOCUMENT
        const val QUERY_TYPE_ZIP = QueryType.QUERY_TYPE_ZIP
        const val COLUMN_COUNT = "count"
        val ORDER_BY = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
        @JvmStatic
        protected val PROJECTION = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.ORIENTATION,
        )

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
        @JvmStatic
        protected val ALL_PROJECTION = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.ORIENTATION,
            MediaStore.MediaColumns.DATE_MODIFIED,
            "COUNT(*) AS $COLUMN_COUNT"
        )
    }

    fun queryCursor(
        queryType: Int,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        orderBy: String = ORDER_BY,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
    ): ArrayList<T> {
        val uri = when (queryType) {
            QUERY_TYPE_IMAGE -> {
                if (isAtLeastQ) MediaStore.Images.Media.getContentUri("external")
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            QUERY_TYPE_AUDIO -> {
                if (isAtLeastQ) MediaStore.Audio.Media.getContentUri("external")
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            QUERY_TYPE_VIDEO -> {
                if (isAtLeastQ) MediaStore.Video.Media.getContentUri("external")
                else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            QUERY_TYPE_ZIP, QUERY_TYPE_DOCUMENT -> MediaStore.Files.getContentUri("external")
            else -> MediaStore.Files.getContentUri("external")
        }
        return queryCursor(uri, offset, limit, orderBy, convert = convert)
    }

    fun queryCursor(
        uri: Uri,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        orderBy: String = ORDER_BY,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
    ): ArrayList<T> {
        val result = arrayListOf<T>()
        val orderBy = orderBy.ifEmpty { ORDER_BY }
        val cursor = if (isAtLeastQ) contentResolver.query(uri, columns, queryArgsBundle(orderBy, offset, limit), null)
        else contentResolver.query(uri, columns, selection, selectionArgs, "$orderBy LIMIT $limit offset ${(offset - 1) * limit}")
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

    val selection: String?
        get() {
            return null
        }
    val selectionArgs: Array<String>?
        get() = null

    fun queryArgsBundle(orderBy: String, offset: Int, limit: Int): Bundle {
        val queryArgs = Bundle()
        if (isAtLeastO) {
            if (selection != null) {
                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            }
            if (selectionArgs != null) {
                queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, orderBy)
            if (isAtLeastR) {
                if (limit > 0 && offset >= 0) {
                    queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, "$limit offset ${(offset - 1) * limit}")
                }
            }
        }
        return queryArgs
    }

    fun sortOrder(columnName: String, isDesc: Boolean): String {
//        return "LOWER(date_modified) DESC LIMIT $pageSize , ${(page - 1) * pageSize}"
        return "$columnName ${if (isDesc) "DESC" else "ASC"}"
    }


    open val columns: Array<String>
        get() = if (isAtLeastQ) PROJECTION else ALL_PROJECTION
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

    fun queryCursor(
        queryType: Int,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        @SortType sortType: Int,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
    ): ArrayList<T> {
        val orderBy = createOrderBy(sortType)
        return queryCursor(queryType, offset, limit, orderBy, convert)
    }

    fun createOrderBy(sortType: Int): String {
        return when (sortType) {
            SortType.SORT_TYPE_NAME_ASC -> sortOrder(MediaStore.MediaColumns.DISPLAY_NAME, false)
            SortType.SORT_TYPE_NAME_DESC -> sortOrder(MediaStore.MediaColumns.DISPLAY_NAME, true)
            SortType.SORT_TYPE_SIZE_ASC -> sortOrder(MediaStore.MediaColumns.SIZE, false)
            SortType.SORT_TYPE_SIZE_DESC -> sortOrder(MediaStore.MediaColumns.SIZE, true)
            SortType.SORT_TYPE_DATE_DESC -> sortOrder(MediaStore.MediaColumns.DATE_MODIFIED, true)
            SortType.SORT_TYPE_DATE_ASC -> sortOrder(MediaStore.MediaColumns.DATE_MODIFIED, false)
            else -> sortOrder(MediaStore.MediaColumns.DATE_MODIFIED, false)
        }
    }

    fun queryCursor2(
        queryType: Int,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        convert: (Cursor, ArrayList<T>, String, String, String, Long, Long) -> Unit,
    ): ArrayList<T> {
        return queryCursor(queryType, offset, limit, ORDER_BY, convert)
    }
}

open class BaseMediaViewModel(application: Application) : BaseQueryViewModel<MediaHeader>(application) {
    fun queryCursor(
        queryType: Int,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        @SortType sortType: Int,
        convert: (Cursor, MediaData) -> MediaData = { cursor, media -> media },
    ): Pair<Int, ArrayList<MediaHeader>> {
        val titleArray = arrayListOf<String>()
        var mediaDataSize = 0
        val result = queryCursor(queryType, offset, limit) { cursor, result, path, fileName, formatTime, dateTime, fileSize ->
            var media = MediaData(
                dateValue = dateTime,
                fileName = fileName,
                filePath = path,
                size = fileSize,
                fileSize = fileSize.formatFileSize(),
                mimeType = cursor.getString(MediaStore.MediaColumns.MIME_TYPE),
                dateFormat = formatTime
            )
            media = convert(cursor, media)
            mediaDataSize++
            if (titleArray.contains(formatTime)) {
                val index = titleArray.indexOf(formatTime)
                dLog { ">>>>>formatTime:$formatTime,index:$index" }
                result[index].addMediaData(media)
            } else {
                dLog { ">>>>>formatTime:$formatTime" }
                val photoHeader = MediaHeader(title = formatTime).addMediaData(media)
                titleArray.add(formatTime)
                result.add(photoHeader)
            }
        }
        return mediaDataSize to result.sortList(sortType)
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
        for ((index, item) in this.withIndex()) {
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