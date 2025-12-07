package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.peihua.touchmonitor.data.db.AppDatabase
import com.peihua.touchmonitor.data.db.Factory
import com.peihua.touchmonitor.data.db.FactoryImpl
import com.peihua.touchmonitor.data.db.dao.DownloadTaskDao
import com.peihua.touchmonitor.data.db.dao.MediaSegmentDao
import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.MediaSegment
import com.peihua.touchmonitor.paging3.PagingSourceImpl
import com.peihua.touchmonitor.ui.screen.function.video.m3u8.DataModel
import com.peihua8858.tools.utils.dLog
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

class M3u8DownloadViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val repository = M3u8Repository()
    private val pagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,  // 可根据需要调整
//        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    )
    val mUiState: StateFlow<MediaUiState>
    val userAction: (MediaUiAction) -> Unit
    val pagingDataFlow: Flow<PagingData<DownloadTask>>

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
        pagingDataFlow = searchAction.flatMapLatest {
            requestDownloadFile(it.sortType)
        }
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
        userAction = {
            viewModelScope.launch { actionStateFlow.emit(it) }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun requestDownloadFile(sortType: Int): Flow<PagingData<DownloadTask>> {
        val bundle = Bundle()
        bundle.putInt("SORT_TYPE", sortType)
        dLog { "sortType:$sortType" }
        return Pager(pagingConfig, initialKey = 1) {
            PagingSourceImpl(
                pagingConfig, bundle,
                refreshKey = { null }) { page, pageSize, bundle ->
                val result = requestGridPagingData(page, pageSize, bundle.getInt("SORT_TYPE", 5))
                result
            }
        }.flow
    }

    fun requestGridPagingData(page: Int, loadSize: Int, sortType: Int): Pair<Int, MutableList<DownloadTask>> {
        val data = arrayOf(
//            DownloadTask(1, "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
            DataModel("2", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
            DataModel("3", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
            DataModel("4", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
        )
        return 1 to ArrayList()
    }

    fun downloadM3u8(value: String) {

    }

    companion object {
        private const val LAST_SORT_TYPE: String = "last_sort_type"
    }
}

class M3u8Repository() {
    private val factory: Factory
        get() = FactoryImpl()
    private val database: AppDatabase
        get() = factory.createRoomDatabase()
    private val downloadTaskDao: DownloadTaskDao
        get() = database.downloadTaskDao()
    private val mediaSegmentDao: MediaSegmentDao
        get() = database.mediaSegmentDao()

    suspend fun updateId(task: DownloadTask) {
        downloadTaskDao.updateById(entity = task)
    }
    suspend fun updateById(task: DownloadTask){

    }

    suspend fun saveAllMediaSegments(taskId: Long, task: MediaSegment) {
        task.taskId = taskId
        mediaSegmentDao.insert(task)
    }

    suspend fun getByTaskId(tid: Long, isFinished: Boolean, size: Int): MutableList<MediaSegment> {
        return mediaSegmentDao.selectByTaskIdAndFinished(tid, isFinished, size)
    }
    suspend fun getByTaskId(tid: Long, isFinished: Boolean): MutableList<MediaSegment> {
        return mediaSegmentDao.selectByTaskIdAndFinished(tid, isFinished)
    }
}


sealed class M3u8UiAction {
    data class Download(val sortType: Int) : MediaUiAction()
    data class Scroll(val sortType: Int) : MediaUiAction()
}

data class M3u8UiState(
    val sortType: Int,
    val currentSortType: Int,
)