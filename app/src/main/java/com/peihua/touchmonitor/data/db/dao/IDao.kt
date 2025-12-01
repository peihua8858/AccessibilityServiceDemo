package com.peihua.touchmonitor.data.db.dao

import androidx.paging.PagingSource

interface IDao<E : Any, ID> {
    suspend fun insert(entity: E): Int

    suspend fun updateById(entity: E): Int

    suspend fun deleteById(id: ID): Int

    suspend fun selectById(id: ID): E?

    suspend fun selectAll(): MutableList<E>?

    fun selectPage(pageNum: Int, pageSize: Int): PagingSource<Int, E>?
}