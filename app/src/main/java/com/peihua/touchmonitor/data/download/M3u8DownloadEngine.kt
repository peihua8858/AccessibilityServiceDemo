package com.peihua.touchmonitor.data.download

import com.peihua.touchmonitor.data.repository.M3u8Repository
import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.model.DownloadTaskStatus
import com.peihua.touchmonitor.model.MediaSegment
import com.peihua.touchmonitor.model.downloadStore
import com.peihua.touchmonitor.ui.screen.function.video.m3u8.M3U8Parser
import com.peihua.touchmonitor.ui.screen.function.video.m3u8.ParsedSegment
import com.peihua.touchmonitor.utils.FfmpegUtil
import com.peihua.touchmonitor.utils.HttpClientUtil
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * 下载任务编排：阶段机、并发闸门、重试策略、进度发布。
 *
 * 不用线程池 + `Thread.interrupt()` 那一套：interrupt 不打断阻塞在 socket read 上的线程，
 * 所以"暂停"是假的；而线程池版的 stop/start 会在旧线程真正结束前就允许新线程启动，
 * 两个线程写同一个分片文件导致内容交错损坏。协程版靠 `cancel() + join()` 串行化。
 */
class M3u8DownloadEngine(
    private val repo: M3u8Repository,
    private val paths: DownloadPaths,
    private val parser: M3U8Parser = M3U8Parser(),
    private val downloader: SegmentDownloader = SegmentDownloader(M3u8KeyProvider(paths)),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val jobs = ConcurrentHashMap<Long, Job>()
    private val runtimes = ConcurrentHashMap<Long, TaskRuntime>()

    /** 所有任务共享，防止 N 个任务 × M 个分片把网络和 CDN 打爆 */
    private val globalGate = Semaphore(MAX_GLOBAL_CONCURRENCY)

    private val _progress = MutableStateFlow<Map<Long, TaskProgress>>(emptyMap())

    /**
     * StateFlow 天然 conflated + replay-1：UI 旋转、导航返回、Service 重建后重新订阅
     * 立刻拿到当前状态，不需要补发全量事件也不需要从 DB 反查。
     */
    val progress: StateFlow<Map<Long, TaskProgress>> = _progress.asStateFlow()

    private val _hasActive = MutableStateFlow(false)
    val hasActive: StateFlow<Boolean> = _hasActive.asStateFlow()

    private var ticker: Job? = null

    /**
     * 幂等：已在运行的任务重复调用不会起第二个 Job。
     *
     * 标 internal 且只由前台 Service 调用，避免引擎在没有服务保护的情况下偷跑。
     */
    internal fun start(taskId: Long) {
        dLog { "引擎收到启动命令：taskId=$taskId" }
        jobs.compute(taskId) { id, existing ->
            if (existing?.isActive == true) {
                dLog { "任务 $taskId 已在运行，忽略重复启动" }
                return@compute existing
            }
            scope.launch { runTask(id) }
        }
        // 必须立刻置位：前台服务以 hasActive 判空闲，等 runTask 里第一次 setStage 才更新
        // 会给“刚下命令就自停”留出窗口
        updateHasActive()
        ensureTicker()
    }

    fun stop(taskId: Long) {
        jobs.remove(taskId)?.cancel()
    }

    /** 必须 join：不等旧 Job 真正结束就重启，会有两个协程写同一批分片文件 */
    suspend fun restart(taskId: Long) {
        jobs.remove(taskId)?.apply {
            cancel()
            join()
        }
        start(taskId)
    }

    fun stopAll(reason: String) {
        val ids = jobs.keys.toList()
        ids.forEach { id ->
            runtimes[id]?.message = reason
            jobs.remove(id)?.cancel()
        }
    }

    // ---------------------------------------------------------------- 阶段机

    private suspend fun runTask(taskId: Long) {
        dLog { "任务 $taskId 开始执行" }
        val rt = runtimes.getOrPut(taskId) { TaskRuntime(taskId) }
        try {
            val task = repo.task(taskId) ?: run {
                eLog { "任务 $taskId 在数据库中不存在，终止" }
                return
            }
            rt.reset()
            rt.total = task.totalMediaSegment.toInt()
            rt.finished.set(repo.countFinished(taskId))
            dLog { "任务 $taskId 初始状态：total=${rt.total}, finished=${rt.finished.get()}, stage=${task.stage}" }
            publish(rt)

            val parsed = parseStage(task, rt)
            downloadStage(parsed, rt)
            mergeStage(parsed, rt)
        } catch (ce: CancellationException) {
            // 必须重抛，否则协程取消语义被吞掉；状态更新要在 NonCancellable 里做
            withContext(NonCancellable) {
                setStage(rt, DownloadTaskStage.STOPPED, DownloadTaskStatus.STOPPED_MANUAL, rt.message)
            }
            throw ce
        } catch (e: M3u8Exception) {
            eLog { "任务 $taskId 失败：${e.userMessage}" }
            setStage(rt, e.stage, DownloadTaskStatus.STOPPED_ERROR, e.userMessage)
        } catch (e: Throwable) {
            // 打完整堆栈：三方库（如 ffmpeg-kit）常把 UnsatisfiedLinkError 包一层，
            // 只看 message 会丢掉真正的 dlopen 失败原因
            eLog { "任务 $taskId 异常：${e.stackTraceToString()}" }
            setStage(
                rt,
                DownloadTaskStage.DOWNLOAD_FAILED,
                DownloadTaskStatus.STOPPED_ERROR,
                e.message ?: "未知错误",
            )
        } finally {
            jobs.remove(taskId)
            withContext(NonCancellable) { repo.refreshProgress(taskId) }
            rt.rate = 0L
            publish(rt)
            updateHasActive()
        }
    }

    /** 已解析过的任务直接续传，不重复请求 m3u8 */
    private suspend fun parseStage(task: DownloadTask, rt: TaskRuntime): DownloadTask {
        if (task.totalMediaSegment > 0L) return task

        setStage(rt, DownloadTaskStage.M3U8_PARSING, DownloadTaskStatus.RUNNING)
        val parsed = parser.parse(task.url, task.resolution, task.referer)
        if (parsed.isFmp4) throw M3u8Exception.UnsupportedFmp4()

        repo.replaceSegments(task.id, parsed.segments.map { it.toEntity(task.id) })
        rt.total = parsed.segments.size
        setStage(rt, DownloadTaskStage.M3U8_PARSED, DownloadTaskStatus.RUNNING)
        return repo.task(task.id) ?: throw M3u8Exception.ParseFailed("任务已被删除")
    }

    private suspend fun downloadStage(task: DownloadTask, rt: TaskRuntime) {
        setStage(rt, DownloadTaskStage.DOWNLOADING, DownloadTaskStatus.RUNNING)
        val dir = paths.tmpDir(task.id)
        reconcileFiles(task.id, dir)

        rt.total = task.totalMediaSegment.toInt()
        rt.finished.set(repo.countFinished(task.id))
        rt.downloadStartedAt = System.currentTimeMillis()

        val pending = repo.pending(task.id, MAX_RETRY)
        if (pending.isEmpty() && rt.finished.get() == 0) {
            // 堵住"0 分片直接 FINISHED"
            throw M3u8Exception.ParseFailed("m3u8 解析结果为空")
        }

        // 桌面版是 ThreadPoolExecutor(cores*10, cores*20)，8 核手机就是 80~160 线程：
        // 几十 MB 纯栈、被 CDN 判异常 429、移动网络下吞吐反而下降。上限 8。
        val perTask = (
            task.maxThreadCount.takeIf { it > 0 }
                ?: downloadStore.data.first().defaultThreadCount
            ).coerceIn(1, MAX_GLOBAL_CONCURRENCY)
        val gate = Semaphore(perTask)
        dLog { "任务 ${task.id} 开始下载：待下 ${pending.size} 片，并发 $perTask" }

        // 用 HEAD 请求探测第一个分片大小，立即估算总字节数，让进度条从第一秒就能动
        if (pending.isNotEmpty()) {
            val probeSize = HttpClientUtil.headContentLength(pending.first().url, task.referer)
            if (probeSize > 0) {
                rt.totalExpectedBytes = probeSize * rt.total
                dLog { "任务 ${task.id} 探测分片大小=${probeSize}, 估算总大小=${rt.totalExpectedBytes}" }
                publish(rt)
            }
        }

        // 必须用 coroutineScope 而不是 scope.async：后者让分片协程脱离任务自身的 Job，
        // stop(taskId) 取消任务时分片还在继续下载，"暂停"又变成假的
        coroutineScope {
            pending.map { segment ->
                async(Dispatchers.IO) {
                    globalGate.withPermit {
                        gate.withPermit { downloadWithRetry(task, segment, dir, rt) }
                    }
                }
            }.awaitAll()
        }

        val failed = repo.countPermanentlyFailed(task.id, MAX_RETRY)
        if (failed > ALLOW_SKIP_SEGMENTS) {
            // 宁可失败，也不产出静默残缺的视频
            throw M3u8Exception.DownloadIncomplete(failed)
        }
        setStage(rt, DownloadTaskStage.DOWNLOAD_FINISHED, DownloadTaskStatus.RUNNING)
    }

    private suspend fun mergeStage(task: DownloadTask, rt: TaskRuntime) {
        setStage(rt, DownloadTaskStage.SEGMENT_MERGING, DownloadTaskStatus.RUNNING)
        val segmentPaths = repo.finishedPathsOrdered(task.id)
        val output = paths.uniqueOutputFile(task.saveFileName.ifBlank { "video_${task.id}" })

        FfmpegUtil.mergeTs(segmentPaths, output, paths.tmpDir(task.id)) { mergedMs ->
            rt.mergedMs = mergedMs
        }

        // 交叉校验时长，能抓住"concat 成功但只拼出几秒"
        val expectedMs = repo.sumDurationMs(task.id)
        val actualMs = FfmpegUtil.probeDurationMs(output)
        val warning = if (actualMs != null && !FfmpegUtil.durationWithinTolerance(actualMs, expectedMs)) {
            "产出时长 ${actualMs / 1000}s 与预期 ${expectedMs / 1000}s 偏差较大，请检查文件"
        } else {
            null
        }

        repo.setOutput(task.id, output.absolutePath, output.name)
        setStage(rt, DownloadTaskStage.SEGMENT_MERGED, DownloadTaskStatus.RUNNING)
        paths.clearTmp(task.id)
        setStage(rt, DownloadTaskStage.FINISHED, DownloadTaskStatus.FINISHED, warning)
    }

    // ------------------------------------------------------------ 单分片重试

    private suspend fun downloadWithRetry(
        task: DownloadTask,
        segment: MediaSegment,
        dir: File,
        rt: TaskRuntime,
    ) {
        var attempt = 0
        while (true) {
            try {
                dLog { "开始下载分片 seq=${segment.seq}, url=${segment.url.take(80)}" }
                val result = downloader.download(task, segment, dir, rt.meter.counter)
                dLog { "分片完成 seq=${segment.seq}, size=${result.byteSize}, cost=${result.costMillis}ms" }
                repo.markSegmentFinished(
                    segment.id,
                    result.file.absolutePath,
                    result.byteSize,
                    result.costMillis,
                )
                rt.finished.incrementAndGet()
                // 第一个分片完成时，用它的大小估算总字节数，让进度条立刻能动
                if (rt.totalExpectedBytes == 0L && rt.total > 0) {
                    rt.totalExpectedBytes = result.byteSize * rt.total
                }
                // 分片完成时立即刷新 DB 并发布进度，不依赖 ticker
                repo.refreshProgress(task.id)
                publish(rt)
                return
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Throwable) {
                attempt++
                if (e.isPermanentFailure()) {
                    // 一次判死：把 retryCount 直接顶到上限，之后 selectPending 本身就不再返回它，
                    // 进程重启也不会重新无限重试
                    repo.bumpRetry(segment.id, MAX_RETRY)
                    eLog { "分片永久失败（seq=${segment.seq}）：${e.message}" }
                    return
                }
                repo.bumpRetry(segment.id, 1)
                if (segment.retryCount + attempt >= MAX_RETRY) {
                    eLog { "分片重试耗尽（seq=${segment.seq}）：${e.message}" }
                    return
                }
                val backoff = e.retryAfterMillis()
                    ?: (BASE_BACKOFF_MS shl (attempt - 1)).coerceAtMost(MAX_BACKOFF_MS)
                dLog { "分片重试 $attempt（seq=${segment.seq}），退避 ${backoff}ms" }
                delay(backoff + Random.nextLong(JITTER_MS))
            }
        }
    }

    /**
     * 断点续传对账：删掉所有 `.part`，把"标记完成但文件已不存在或为 0 字节"的记录打回未完成。
     * 几千次 `File.exists()` 只要几十毫秒。
     */
    private suspend fun reconcileFiles(taskId: Long, dir: File) {
        dir.listFiles()?.forEach { if (it.name.endsWith(PART_SUFFIX)) it.delete() }
        val broken = repo.finishedSegments(taskId)
            .filter { seg ->
                seg.filePath.isBlank() || File(seg.filePath).let { !it.isFile || it.length() == 0L }
            }
            .map { it.id }
        if (broken.isNotEmpty()) {
            dLog { "任务 $taskId 对账：${broken.size} 个分片文件丢失，打回重下" }
            repo.resetFinished(broken)
            repo.refreshProgress(taskId)
        }
    }

    // -------------------------------------------------------------- 进度发布

    private suspend fun setStage(
        rt: TaskRuntime,
        stage: DownloadTaskStage,
        status: DownloadTaskStatus,
        errorMessage: String? = null,
    ) {
        rt.stage = stage
        rt.status = status
        rt.message = errorMessage
        repo.setStage(rt.taskId, stage, status, errorMessage)
        publish(rt)
        updateHasActive()
    }

    private fun publish(rt: TaskRuntime) {
        val snapshot = rt.snapshot()
        _progress.value = _progress.value + (rt.taskId to snapshot)
    }

    private fun updateHasActive() {
        _hasActive.value = jobs.values.any { it.isActive }
    }

    /**
     * 全局单个 1s ticker。桌面版给每个任务 submit 一个 `while(!isStopped) sleep(1000)` 忙等线程，
     * 而 isStopped 只在手动停止时置 true、正常完成永不置 —— 每个完成过的任务留一个永久活着的
     * 线程，10 个任务后池子被占满抛 RejectedExecutionException，表现为"下载卡死"。
     */
    private fun ensureTicker() {
        if (ticker?.isActive == true) return
        ticker = scope.launch {
            try {
                var last = System.currentTimeMillis()
                while (jobs.values.any { it.isActive }) {
                    delay(TICK_INTERVAL_MS)
                    val now = System.currentTimeMillis()
                    val elapsed = now - last
                    last = now
                    jobs.keys.toList().forEach { id ->
                        val rt = runtimes[id] ?: return@forEach
                        rt.rate = rt.meter.sample(elapsed)
                        // 进度字段每秒最多写一次；速率与 ETA 只在内存里，永不入库，
                        // 否则 observeAll() 会每片重发一次，列表每分钟重组几百次
                        repo.refreshProgress(id)
                        publish(rt)
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Throwable) {
                eLog { "ticker 异常退出：${e.stackTraceToString()}" }
            }
            updateHasActive()
        }
    }

    private companion object {
        const val MAX_GLOBAL_CONCURRENCY = 8
        const val MAX_RETRY = 5
        const val ALLOW_SKIP_SEGMENTS = 0
        const val BASE_BACKOFF_MS = 500L
        const val MAX_BACKOFF_MS = 20_000L
        const val JITTER_MS = 500L
        const val TICK_INTERVAL_MS = 1_000L
        const val PART_SUFFIX = ".part"
    }
}

private fun ParsedSegment.toEntity(taskId: Long) = MediaSegment(
    id = 0L,
    taskId = taskId,
    url = url,
    finished = false,
    duration = durationMs,
    downloadDuration = 0L,
    filePath = "",
    seq = seq,
    keyMethod = keyMethod,
    keyUri = keyUri,
    keyIv = keyIv,
)

/** 单个任务的内存运行态。速率与 ETA 只活在这里。 */
private class TaskRuntime(val taskId: Long) {
    val meter = RateMeter()
    val finished = AtomicInteger(0)

    @Volatile
    var total = 0

    @Volatile
    var stage = DownloadTaskStage.NEW

    @Volatile
    var status = DownloadTaskStatus.NEW

    @Volatile
    var message: String? = null

    @Volatile
    var rate = 0L

    @Volatile
    var mergedMs = 0L

    @Volatile
    var downloadStartedAt = 0L

    @Volatile
    var totalExpectedBytes = 0L

    fun reset() {
        message = null
        rate = 0L
        mergedMs = 0L
        downloadStartedAt = 0L
        totalExpectedBytes = 0L
    }

    fun snapshot(): TaskProgress {
        val done = finished.get()
        return TaskProgress(
            taskId = taskId,
            stage = stage,
            status = status,
            finishedSegments = done,
            totalSegments = total,
            downloadedBytes = meter.counter.get(),
            bytesPerSec = rate,
            etaMillis = estimateEta(done),
            message = message,
            totalExpectedBytes = totalExpectedBytes,
        )
    }

    private fun estimateEta(done: Int): Long {
        if (done <= 0 || total <= done || downloadStartedAt == 0L) return -1L
        val elapsed = System.currentTimeMillis() - downloadStartedAt
        if (elapsed <= 0L) return -1L
        return elapsed / done * (total - done)
    }
}
