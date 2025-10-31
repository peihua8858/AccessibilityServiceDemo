package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import android.util.Size
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.peihua.compose.file.mimeTypeFromFilePath
import com.peihua.compose.paging3.PagingSourceImpl
import com.peihua.compose.utils.dLog
import com.peihua.touchmonitor.model.MediaData
import com.peihua.touchmonitor.model.SearchModel
import com.peihua.touchmonitor.ui.screen.function.search.SearchType
import com.peihua.touchmonitor.utils.formatFileSize
import com.peihua.touchmonitor.utils.getLong
import com.peihua.touchmonitor.utils.getString
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
import java.io.File
import androidx.core.graphics.drawable.toDrawable

class SearchViewModel(application: Application, private val savedStateHandle: SavedStateHandle) :
    BaseQueryViewModel<MediaData>(application) {
    val pagingDataFlow: Flow<PagingData<SearchModel>>
    val mUiState: StateFlow<SearchUiState>
    val userAction: (SearchUiAction) -> Unit
    private val gridViewPagingConfig = PagingConfig(
        pageSize = 20,
        initialLoadSize = 20,  // 可根据需要调整
//        maxSize = 100, // 可选，最大加载数据量
        enablePlaceholders = false // 根据需要设置
    )
    private var mFilter: ArrayList<String> = arrayListOf()
    private var mSelection: String = ""
    private var mSelectionArgs: Array<String> = arrayOf()
    override val selection: String?
        get() = mSelection
    override val selectionArgs: Array<String>?
        get() = mSelectionArgs
    override val filter: (Cursor) -> Boolean
        get() = {
            val path = it.getString(MediaStore.MediaColumns.DATA)
            val extension = File(path).extension
            dLog { "extension:$extension" }
            mFilter.contains(".$extension") || mFilter.isEmpty()
        }

    init {
        val initialQuery = savedStateHandle[LAST_QUERY] ?: ""
        dLog { "initialQuery:$initialQuery" }
        savedStateHandle[LAST_QUERY] = initialQuery
        val actionStateFlow = MutableSharedFlow<SearchUiAction>()
        val searchAction = actionStateFlow
            .filterIsInstance<SearchUiAction.Search>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .onStart { emit(SearchUiAction.Search(query = initialQuery)) }
        val scrollAction = actionStateFlow
            .filterIsInstance<SearchUiAction.Scroll>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 1)
            .onStart { emit(SearchUiAction.Scroll(query = initialQuery)) }
        mUiState = combine(searchAction, scrollAction, ::Pair)
            .flowOn(Dispatchers.IO)
            .map { (search, scroll) ->
                dLog { "search.query:${search.query},scroll.query:${scroll.query}" }
                SearchUiState(query = search.query, currentQuery = scroll.query)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                SearchUiState(query = initialQuery, currentQuery = initialQuery)
            )
        pagingDataFlow = searchAction.flatMapLatest {
            requestSearch(it.query, it.searchType)
        }
            .flowOn(Dispatchers.IO)
            .cachedIn(viewModelScope)
        userAction = { query ->
            viewModelScope.launch { actionStateFlow.emit(query) }
        }
    }

    fun requestSearch(query: String, searchType: SearchType): Flow<PagingData<SearchModel>> {
        val bundle = Bundle()
        bundle.putString("keywords", query)
        dLog { "keywords:$query" }
        return Pager(gridViewPagingConfig, initialKey = 1) {
            PagingSourceImpl(
                gridViewPagingConfig, bundle,
                refreshKey = { null }) { page, pageSize, bundle ->
                val result = requestGridPagingData(
                    searchType,
                    page,
                    pageSize,
                    bundle.getString("keywords", "")
                )
                result
            }
        }.flow
    }

//    @OptIn(ExperimentalPagingApi::class)
//    fun requestSearch(keywords: String): Flow<PagingData<MediaData>> {
//        val bundle = Bundle()
//        bundle.putString("keywords", keywords)
//        dLog { "keywords:$keywords" }
//        return Pager(gridViewPagingConfig, initialKey = 1) {
//            PagingSourceImpl(
//                gridViewPagingConfig, bundle,
//                refreshKey = { null }) { page, pageSize, bundle ->
//                val result = requestGridPagingData(page, pageSize, bundle.getString("keywords", ""))
//                result
//            }
//        }.flow
//    }

    fun requestGridPagingData(
        searchType: SearchType = SearchType.ALL,
        page: Int,
        loadSize: Int,
        keywords: String,
    ): Pair<Int, MutableList<SearchModel>> {
        dLog { "keywords:${keywords.trim()}" }
        if (keywords.isNotEmpty() && keywords.isNotBlank()) {
            mSelection =
                MediaStore.MediaColumns.DISPLAY_NAME + " like ? or " + MediaStore.MediaColumns.DATA + " like ? "
            mSelectionArgs = arrayOf("%${keywords.trim()}%", "%${keywords.trim()}%")
        }
        mFilter.clear()
        val queryType = when (searchType) {
            SearchType.ALL -> QUERY_TYPE_SEARCH
            SearchType.IMAGE -> QUERY_TYPE_IMAGE
            SearchType.VIDEO -> QUERY_TYPE_VIDEO
            SearchType.AUDIO -> QUERY_TYPE_AUDIO
            SearchType.DOCUMENT -> {
                mFilter.addAll(DocumentViewModel.documentTypes)
                QUERY_TYPE_DOCUMENT
            }

            SearchType.APK -> {
                mFilter.add(".apk")
                mFilter.add(".xapk")
                mFilter.add(".apk.1")
                QUERY_TYPE_APK
            }

            SearchType.APPLICATION -> QUERY_TYPE_SEARCH
        }
        val result = arrayListOf<SearchModel>()
        if (page == 1 && (searchType == SearchType.ALL || searchType == SearchType.APPLICATION)) {
            result.addAll(queryApplication(keywords))
        }
        if (searchType != SearchType.APPLICATION) {
            result.addAll(queryMediaData(queryType, page, loadSize))
        }
        return result.size to result
    }

    private fun queryApplication(keywords: String): MutableList<SearchModel> {
        return AppExtractorViewModel.queryApplication(application.packageManager, AppType.ALL)
            .filter {
                it.packageName.contains(keywords) || it.name.contains(keywords) || it.path.contains(
                    keywords
                ) || keywords.isEmpty()
            }
            .map {
                SearchModel(
                    it.name, it.packageName, it.path, it.icon,
                    fileSize = Formatter.formatFileSize(application, it.fileSize)
                )
            }.toMutableList()
    }

    private fun queryMediaData(
        queryType: Int,
        page: Int,
        loadSize: Int,
    ): MutableList<SearchModel> {
        return queryCursor(
            queryType,
            page,
            loadSize
        ) { cursor, result, path, fileName, formatTime, dateTime, fileSize ->
            var mimeType = cursor.getString(MediaStore.MediaColumns.MIME_TYPE)
            if (mimeType.isEmpty()) {
                mimeType = path.mimeTypeFromFilePath ?: ""
            }
            val media = MediaData(
                dateValue = dateTime,
                fileName = fileName,
                filePath = path,
                size = fileSize,
                fileSize = fileSize.formatFileSize(),
                mimeType = cursor.getString(MediaStore.MediaColumns.MIME_TYPE),
                dateFormat = formatTime
            )
            when (mimeType) {
                //视频
                "video/*", "audio/*" -> {
                    val duration = cursor.getLong(MediaStore.MediaColumns.DURATION)
                    media.duration = getDurationString(duration)
                    if (mimeType == "video/*") {
                        val fileUri = media.filePath.toUri()
                        dLog { "getVideoThumbnail>>>fileUri: $fileUri" }
                        val bitmap = application.getVideoThumbnailFromMediaMetadataRetriever(
                            fileUri,
                            Size(640, 480)
                        )
                        dLog { "getVideoThumbnail: $bitmap" }
                        media.thumbnailsBitmap = bitmap
                    }
                }

                else -> {

                }
            }
            media
        }.map {
            SearchModel(
                it.fileName,
                it.filePath,
                "",
                fileSize = it.fileSize ?: "",
                icon = it.thumbnailsBitmap?.toDrawable(application.resources)
            )
        }.toMutableList()
    }

    companion object {
        private const val LAST_QUERY: String = "last_query"
    }
}

sealed class SearchUiAction {
    data class Search(val query: String, val searchType: SearchType = SearchType.ALL) :
        SearchUiAction()

    data class Scroll(val query: String, val searchType: SearchType = SearchType.ALL) :
        SearchUiAction()
}

data class SearchUiState(
    val query: String,
    val currentQuery: String,
)