package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.os.Bundle
import android.provider.MediaStore
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
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.paging3.PagingSourceImpl
import com.peihua.touchmonitor.utils.dLog
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
) : BaseMediaViewModel(application) {
    var mediaType: Int = QUERY_TYPE_IMAGE
    var showDate: Boolean = true
    private val gridViewPagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,  // 可根据需要调整
//        maxSize = 100, // 可选，最大加载数据量
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

    fun requestGridPagingData(page: Int, loadSize: Int, sortType: Int): Pair<Int, MutableList<MediaModel>> {
        dLog { "sortType:$sortType" }
        val data = ArrayList<MediaModel>()
        val (size, result) = queryCursor(mediaType, page, loadSize, sortType = sortType) { cursor, media ->
            when (mediaType) {
                QUERY_TYPE_IMAGE, QUERY_TYPE_ZIP -> {
                    //无需其他字段
                }

                else -> {
                    val duration = cursor.getLong(MediaStore.MediaColumns.DURATION)
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
            media
        }
        result.forEach {
            if (showDate) {
                data.add(MediaModel.Header(MediaHeader(it.title)))
            }
            it.mediaList.forEach {
                data.add(MediaModel.Item(it))
            }
        }
        return size to data
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
