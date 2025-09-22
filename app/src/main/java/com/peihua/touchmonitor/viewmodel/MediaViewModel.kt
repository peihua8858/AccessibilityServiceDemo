package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.os.Bundle
import android.util.Size
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.paging3.PagingSourceImpl
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.formatFileSize
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getVideoThumbnailFromMediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class MediaViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : BaseQueryViewModel<MediaData>(application) {
    var mediaType: Int = QUERY_TYPE_IMAGE
    private val gridViewPagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,  // 可根据需要调整
        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    )

    val mUiState: StateFlow<MediaUiState>
    val userAction: (MediaUiAction) -> Unit
    val pagingDataFlow: Flow<PagingData<MediaModel>>

    init {
        val initialSortType = savedStateHandle[LAST_SORT_TYPE] ?: 3
        dLog { "initialSortType:$initialSortType" }
        savedStateHandle[LAST_SORT_TYPE] = initialSortType
        val actionStateFlow = MutableSharedFlow<MediaUiAction>()
        val searchAction = actionStateFlow
            .filterIsInstance<MediaUiAction.Sort>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .onStart { emit(MediaUiAction.Sort(sortType = initialSortType)) }
        val scrollAction = actionStateFlow
            .filterIsInstance<MediaUiAction.Scroll>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 1)
            .onStart { emit(MediaUiAction.Scroll(sortType = initialSortType)) }
        mUiState = combine(searchAction, scrollAction, ::Pair)
            .flowOn(Dispatchers.IO)
            .map { (sort, scroll) ->
                dLog { "sort.sortType:${sort.sortType},scroll.sortType:${scroll.sortType}" }
                MediaUiState(sortType = sort.sortType, currentSortType = scroll.sortType)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                MediaUiState(sortType = initialSortType, currentSortType = initialSortType)
            )
        pagingDataFlow =
            searchAction.flatMapLatest {
                titleArray.clear()
                requestVideos(it.sortType)
            }
                .flowOn(Dispatchers.IO)
                .cachedIn(viewModelScope)
        userAction = {
            viewModelScope.launch { actionStateFlow.emit(it) }
        }
    }


    @OptIn(ExperimentalPagingApi::class)
    fun requestVideos(sortType: Int): Flow<PagingData<MediaModel>> {
        val bundle = Bundle()
        bundle.putInt("SORT_TYPE", sortType)
        dLog { "sortType:$sortType" }
        return Pager(gridViewPagingConfig, initialKey = 1) {
            PagingSourceImpl(
                gridViewPagingConfig, bundle,
                refreshKey = { null }) { page, pageSize, bundle ->
                val result = requestGridPagingData(page, pageSize, bundle.getInt("SORT_TYPE", 5))
                result
            }
        }.flow
    }

    private val titleArray = arrayListOf<String>()
    fun requestGridPagingData(page: Int, loadSize: Int, sortType: Int): MutableList<MediaModel> {
        dLog { "sortType:$sortType" }
        val data = ArrayList<MediaModel>()
        val result = queryCursor(mediaType, page, loadSize, sortType = sortType) { cursor, media ->
            media
        }
        result.forEach {
            if (!titleArray.contains(it.dateFormat)) {
                titleArray.add(it.dateFormat)
                data.add(MediaModel.Header(MediaHeader(it.dateFormat)))
            }
            data.add(MediaModel.Item(it))
        }
        return data
    }

    fun queryCursor(
        queryType: Int,
        offset: Int = 1,
        limit: Int = Int.MAX_VALUE,
        @SortType sortType: Int,
        convert: (Cursor, MediaData) -> MediaData = { cursor, media -> media },
    ): ArrayList<MediaData> {
        val result = queryCursor2(queryType, offset, limit) { cursor, result, path, fileName, formatTime, dateTime, fileSize ->
            var media = MediaData(
                dateValue = dateTime,
                fileName = fileName,
                filePath = path,
                size = fileSize,
                fileSize = fileSize.formatFileSize(),
                dateFormat = formatTime
            )
            media = convert(cursor, media)
            when (mediaType) {
                QUERY_TYPE_IMAGE, QUERY_TYPE_ZIP -> {
                    //无需其他字段
                }

                else -> {
                    val duration = cursor.getLong("duration")
                    media.duration = getDurationString(duration)
                    if (mediaType == QUERY_TYPE_VIDEO) {
                        val fileUri = media.filePath.toUri()
                        dLog { "getVideoThumbnail>>>fileUri: $fileUri" }
                        val bitmap = application.getVideoThumbnailFromMediaMetadataRetriever(fileUri, Size(640, 480))
                        dLog { "getVideoThumbnail: $bitmap" }
                        media.thumbnailsBitmap = bitmap
                    }
                }
            }
            result.add(media)
        }
        result.sortList(sortType)
        return result
    }

    protected fun ArrayList<MediaData>.sortList(@SortType sortType: Int): ArrayList<MediaData> {
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
        this.sortWith(comparator = comparator)
        return this
    }

    companion object {
        private const val LAST_SORT_TYPE: String = "last_sort_type"
    }
}

sealed class MediaUiAction {
    data class Sort(val sortType: Int) : MediaUiAction()
    data class Scroll(val sortType: Int) : MediaUiAction()
}

data class MediaUiState(
    val sortType: Int,
    val currentSortType: Int,
)

sealed class MediaModel {
    data class Item(val mediaData: MediaData) : MediaModel()
    data class Header(val mediaHeader: MediaHeader) : MediaModel()
}
