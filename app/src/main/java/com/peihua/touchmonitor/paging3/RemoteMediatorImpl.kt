package com.peihua.touchmonitor.paging3

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.peihua.touchmonitor.utils.isWorkThread
import com.peihua8858.tools.log.Logcat
import com.peihua8858.tools.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class RemoteMediatorImpl<T : Any>(
    private val pagingSource: PagingSource<Int, T>,
    private val loadParams: suspend (LoadType, PagingState<Int, T>) -> LoadParams<Int>,
    private val withTransaction: suspend (LoadType, LoadResult.Page<Int, T>) -> Unit = { _, _ -> }
) : RemoteMediator<Int, T>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, T>
    ): MediatorResult {
        if (isWorkThread()) {
            Logcat.d("RemotePagingSource", " >>>loadType = $loadType")
            try {
                val params = loadParams(loadType, state)
                val result = pagingSource.load(params = params) as LoadResult.Page<Int, T>
                val nextKey = result.nextKey
                val endOfPaginationReached = nextKey == null
                withTransaction(loadType, result)
                dLog { "RemotePagingSource >>>endOfPaginationReached:$endOfPaginationReached" }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } catch (exception: Throwable) {
                exception.printStackTrace()
                return MediatorResult.Error(exception)
            }
        } else {
            return withContext(Dispatchers.IO) {
                load(loadType, state)
            }
        }
    }
}

@OptIn(ExperimentalPagingApi::class)
class RemoteMediatorImpl2<T : Any>(
    private val withTransaction: (LoadType, LoadResult.Page<Int, T>) -> Unit = { _, _ -> },
    private val loadData: suspend (LoadType, PagingState<Int, T>) -> LoadResult.Page<Int, T>
) : RemoteMediator<Int, T>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, T>
    ): MediatorResult {
        if (isWorkThread()) {
            Logcat.d("RemotePagingSource", " >>>loadType = $loadType")
            try {
                val result = loadData(loadType, state)
                val nextKey = result.nextKey
                val endOfPaginationReached = nextKey == null
                withTransaction(loadType, result)
                dLog { "RemotePagingSource >>>endOfPaginationReached:$endOfPaginationReached" }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } catch (exception: Throwable) {
                exception.printStackTrace()
                return MediatorResult.Error(exception)
            }
        } else {
            return withContext(Dispatchers.IO) {
                load(loadType, state)
            }
        }
    }
}