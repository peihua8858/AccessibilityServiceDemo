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
import com.peihua.touchmonitor.model.MediaHeader
import com.peihua.touchmonitor.paging3.PagingSourceImpl
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getVideoThumbnailFromMediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class VideoViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : BaseMediaViewModel(application) {

    private val gridViewPagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,  // 可根据需要调整
        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    )

    //paging3 分页器
    private val config = PagingConfig(
        pageSize = 6,
        initialLoadSize = 6,  // 可根据需要调整
        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    )
    val data = Pager(config, null) {
        PagingSourceImpl(config) { page, pageSize, bundle ->
            requestGridPagingData(pageSize, 1)
        }
    }.flow.cachedIn(viewModelScope)

    val mUiState: StateFlow<UiState>
    val userAction: (UiAction) -> Unit
    val pagingDataFlow: Flow<PagingData<MediaHeader>>

    init {
        val initialSortType = savedStateHandle[LAST_SORT_TYPE] ?: 3
        dLog { "initialSortType:$initialSortType" }
        savedStateHandle[LAST_SORT_TYPE] = initialSortType
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searchAction = actionStateFlow
            .filterIsInstance<UiAction.Sort>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .onStart { emit(UiAction.Sort(sortType = initialSortType)) }
        val scrollAction = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 1)
            .onStart { emit(UiAction.Scroll(sortType = initialSortType)) }
        mUiState = combine(searchAction, scrollAction, ::Pair)
            .flowOn(Dispatchers.IO)
            .map { (sort, scroll) ->
                dLog { "sort.sortType:${sort.sortType},scroll.sortType:${scroll.sortType}" }
                UiState(sortType = sort.sortType, currentSortType = scroll.sortType)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                UiState(sortType = initialSortType, currentSortType = initialSortType)
            )
        pagingDataFlow =
            searchAction.flatMapLatest { requestVideos(it.sortType) }
                .flowOn(Dispatchers.IO)
                .cachedIn(viewModelScope)
        userAction = {
            viewModelScope.launch { actionStateFlow.emit(it) }
        }
    }

    override val columns: Array<String>
        get() = arrayOf(*super.columns, MediaStore.Video.Media.DURATION)

    @OptIn(ExperimentalPagingApi::class)
    fun requestVideos(sortType: Int): Flow<PagingData<MediaHeader>> {
        val bundle = Bundle()
        bundle.putInt("SORT_TYPE", sortType)
        dLog { "sortType:$sortType" }
        val source = PagingSourceImpl(
            gridViewPagingConfig, bundle,
            refreshKey = { null }) { page, pageSize, bundle ->
            requestGridPagingData(pageSize, bundle.getInt("SORT_TYPE", 5))
        }
        return Pager(gridViewPagingConfig, initialKey = 1) { source }.flow
    }

    fun requestGridPagingData(loadSize: Int, sortType: Int): MutableList<MediaHeader> {
        dLog { "sortType:$sortType" }
        return queryCursor(QUERY_TYPE_VIDEO, sortType) { cursor, mediaData ->
            val duration = cursor.getLong("duration")
            mediaData.duration = getDurationString(duration)
            val fileUri = mediaData.filePath.toUri()
            dLog { "getVideoThumbnail>>>fileUri: $fileUri" }
            val bitmap = application.getVideoThumbnailFromMediaMetadataRetriever(fileUri, Size(640, 480))
            dLog { "getVideoThumbnail: $bitmap" }
            mediaData.thumbnailsBitmap = bitmap
            mediaData.time = System.currentTimeMillis()
            mediaData
        }
    }

}

private const val LAST_SORT_TYPE: String = "last_sort_type"

sealed class UiAction {
    data class Sort(val sortType: Int) : UiAction()
    data class Scroll(val sortType: Int) : UiAction()
}

data class UiState(
    val sortType: Int,
    val currentSortType: Int
)

