package com.peihua.touchmonitor.ui.screen.function.appmanager.task

import android.content.Context
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.utils.WorkScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ExtortWorker(
    private val context: Context,
    private val items: List<AppInfoModel>,
    callback: (TaskProcessModel<ExtortWorker>.() -> Unit),
) : CoroutineScope by WorkScope() {
    constructor(
        context: Context,
        item: AppInfoModel,
        callback: (TaskProcessModel<ExtortWorker>.() -> Unit),
    ) : this(context, listOf<AppInfoModel>(item), callback)

    private val model = TaskProcessModel<ExtortWorker>().apply(callback)

    fun start() {
        model.invokeStart(this)
        launch {
            val totalLength = totalLength
            for ((index,item) in items.withIndex() ) {

            }
        }
    }
    private val totalLength: Long
        get(){
            return items.sumOf { it.fileSize }
        }
}

class TaskProcessModel<T> {
    private var onStart: ((T) -> Unit)? = null
    private var onSuccess: ((T) -> Unit)? = null
    private var onError: ((T) -> Unit)? = null
    private var onProgress: ((T, Long, Long) -> Unit)? = null
    infix fun onStart(onStart: ((T) -> Unit)?): TaskProcessModel<T> {
        this.onStart = onStart
        return this
    }

    infix fun onSuccess(onSuccess: ((T) -> Unit)?): TaskProcessModel<T> {
        this.onSuccess = onSuccess
        return this
    }

    infix fun onError(onError: ((T) -> Unit)?): TaskProcessModel<T> {
        this.onError = onError
        return this
    }

    infix fun onProgress(onProgress: ((T, Long, Long) -> Unit)?): TaskProcessModel<T> {
        this.onProgress = onProgress
        return this
    }

    fun invokeStart(model: T) {
        this.onStart?.invoke(model)
    }

    fun invokeSuccess(model: T) {
        this.onSuccess?.invoke(model)
    }

    fun invokeError(model: T) {
        this.onError?.invoke(model)
    }

    fun invokeProgress(model: T, current: Long, total: Long) {
        this.onProgress?.invoke(model, current, total)
    }
}