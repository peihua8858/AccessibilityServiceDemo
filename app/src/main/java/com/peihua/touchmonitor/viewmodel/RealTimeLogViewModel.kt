package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fz.common.text.isNonEmpty
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingDeque


class RealTimeLogViewModel(application: Application) : AndroidViewModel(application),
    CoroutineScope by WorkScope() {
    var running: Array<String> = arrayOf("logcat", "-s", "adb logcat *: W")
    var logcatEngine: LogcatEngine? = LogcatEngine(running)
    val updateState = mutableStateOf<ResultData<String>>(ResultData.Initialize())
    val selMsgState = mutableStateOf<ResultData<String>>(ResultData.Initialize())
    val _messages = mutableStateListOf<String>()
    val enInputState = mutableStateOf(true)
    val mUiState: StateFlow<RealTimeLogUiState>
    val userAction: (RealTimeLogUiAction) -> Unit
    val pagingDataFlow: Flow<String>

    init {
        val actionStateFlow = MutableSharedFlow<RealTimeLogUiAction>()
        // 发送消息的流
        val sendMsgAction = actionStateFlow
            .filterIsInstance<RealTimeLogUiAction.QueryOrSendMsg>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .catch { e ->
                dLog { "Error in stream: ${e.message}" }  // 日志输出
            }
        val scrollAction = actionStateFlow
            .filterIsInstance<RealTimeLogUiAction.Scroll>()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000), 1)

        // 处理发送消息的流
        pagingDataFlow = sendMsgAction
            .flatMapLatest {
                dLog { "MessageViewModel>>>>>> load messages>>$it" }
                sendMessage(it.query)
            } // 替换为你的发送消息的函数
            .flowOn(Dispatchers.IO)
            .catch { e ->
                dLog { "Error in stream: ${e.message}" }  // 日志输出
            }
        // 组合 UI 状态
        val combineFlow = combine(
            sendMsgAction,
            scrollAction,
        ) { sendMsgAction, scrollAction ->
            RealTimeLogUiState(
                query = sendMsgAction.query,
                lastQueryScrolled = scrollAction.currentQuery,
                hasNotScrolledForCurrentSearch = sendMsgAction.query != scrollAction.currentQuery
            )
        }

        // 组合 UI 状态
        mUiState = combineFlow.flowOn(Dispatchers.IO)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                RealTimeLogUiState(query = "", lastQueryScrolled = "")// 初始化状态
            )
        // 用户动作的处理
        userAction = {
            viewModelScope.launch {
                dLog { "User Action Triggered: $it" }  // 添加调试日志
                actionStateFlow.emit(it)
            }
        }
        launch {
            pagingDataFlow.collect {
                dLog { "Paging Data Collected: $it" }  // 添加调试日志
                _messages.add(0, it)
            }
        }
    }

    fun sendMessage(query: String): Flow<String> {
        return flow {
            val mProcess = Runtime.getRuntime().exec(running)
            mProcess?.inputStream?.bufferedReader()?.forEachLine {
                launch {
                    emit(it)
                }
            }
        }
    }

    fun onStart() {
        logcatEngine = LogcatEngine(running)
        launch {
            logcatEngine?.onStart()?.collect {
                _messages.add(0, it)
            }
        }
    }

    fun onPause() {
        logcatEngine?.onPause()
    }

    fun onResume() {
        logcatEngine?.onResume()
    }

    fun onStop() {
        logcatEngine?.onStop()
        logcatEngine = null
    }

    fun clearLog() {
        _messages.clear()
    }

    override fun onCleared() {
        super.onCleared()
        logcatEngine?.onStop()
        logcatEngine = null
    }
}

sealed class RealTimeLogUiAction() {
    data class QueryOrSendMsg(val query: String = "") : RealTimeLogUiAction()
    data class Scroll(val currentQuery: String) : RealTimeLogUiAction()
}

data class RealTimeLogUiState(
    val query: String,
    val lastQueryScrolled: String,
    val hasNotScrolledForCurrentSearch: Boolean = false,
)

class LogcatEngine(private val running: Array<String>) : CoroutineScope by WorkScope() {
    private var mProcess: Process? = null
    private var isRunning = false
    private var isPaused = false
    private val blockingDequeue = LinkedBlockingDeque<String>(200)
    fun onStart(): Flow<String> {
        if (mProcess == null) {
            val mPId = android.os.Process.myPid()
            val mPID = java.lang.String.valueOf(mPId)
//            val cmds = "logcat  *:e *:d | grep \"($mPID)\""
            val cmds = "logcat  *:e *:d "
            mProcess = Runtime.getRuntime().exec(cmds)
        }
        isRunning = true
        val inputStream = mProcess?.inputStream
        launch {
            if (inputStream == null) {
                return@launch
            }
            val mReader =
                inputStream.bufferedReader(Charsets.UTF_8)
            mReader.use {
                while (isRunning) {
                    try {
                        var line = it.readLine()
                        if (line.isNonEmpty()) {
                            if (!line.endsWith("\n")) {
//                                line += "\n"
                            }
                            blockingDequeue.offer(line)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return flow {
            while (isRunning) {
                if (isPaused) {
                    delay(200)
                    continue
                }
                val line = blockingDequeue.poll()
                if (line.isNonEmpty()) {
                    emit(line)
                }
            }
        }
    }

    fun onPause() {
        isPaused = true
    }

    fun onResume() {
        isPaused = false
    }

    fun onStop() {
        isRunning = false
        cancel()
        mProcess?.destroy()
        mProcess = null
    }
}