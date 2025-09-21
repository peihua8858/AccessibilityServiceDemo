package com.peihua.touchmonitor.paging3

import android.os.Bundle
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.peihua.touchmonitor.utils.LogCat
import com.peihua.touchmonitor.utils.dLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PagingSourceImpl<T : Any>(
    val config: PagingConfig,
    private val bundle: Bundle = Bundle.EMPTY,
    private val refreshKey: (PagingState<Int, T>) -> Int? = { state ->
        state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            LogCat.d("PagingSourceImpl", " >>>currentPage = ${page?.nextKey}, pageSize = $page")
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1) // 返回刷新会使用的键
        }
    },
    private val loadData: (Int, Int, Bundle) -> List<T>
) : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return refreshKey(state)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult.Page<Int, T> {
        return withContext(Dispatchers.IO) {
            val currentPage = params.key ?: 1
            val loadSize = params.loadSize
            val maxSize = config.maxSize
            val response = loadData(currentPage, loadSize, bundle)
            val curTotalSize = currentPage * loadSize
            val nextKey = if (curTotalSize >= maxSize || response.isEmpty()) {
                null
            } else {
                currentPage + 1
            }
            LogCat.d(
                "PagingSourceImpl",
                " >>>currentPage = $currentPage, loadSize = $loadSize,nextKey = $nextKey,maxSize = $maxSize"
            )
            return@withContext LoadResult.Page(
                data = response,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = nextKey
            )
        }
    }
}