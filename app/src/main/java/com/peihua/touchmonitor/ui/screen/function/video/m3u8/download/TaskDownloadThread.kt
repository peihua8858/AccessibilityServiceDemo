package com.peihua.touchmonitor.ui.screen.function.video.m3u8.download

import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.model.DownloadTaskStatus
import com.peihua.touchmonitor.model.MediaSegment
import com.peihua.touchmonitor.ui.screen.function.video.m3u8.M3U8Parser
import com.peihua.touchmonitor.utils.HttpClientUtil.getAsInputStream
import com.peihua.touchmonitor.viewmodel.M3u8Repository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

/**
 * 任务下载线程
 *
 * @author cloudgyb
 * @since 2025/06/30 16:50
 */
class TaskDownloadThread(private val task: DownloadTask) : Thread() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val m3U8Parser = M3U8Parser()
    private val repository = M3u8Repository()

    //    private final DownloadTaskService downloadTaskService = DownloadTaskService.getInstance();
    private val eventNotifier: DownloadTaskStatusChangeEventNotifier = DownloadTaskStatusChangeEventNotifier.INSTANCE
    private val isStopped = AtomicBoolean(true)

    /**
     * 下载字节计数器，用于计算速率
     */
    private val bytesCounter = AtomicLong()

    init {
        setName("TaskDownloadManageThread " + task.id)
    }

    override fun run() {
        isStopped.set(false)
        try {
            val beginTime = System.currentTimeMillis()
            var stage = task.stage
            var downloadTaskStageEnum = DownloadTaskStage.valueOf(stage)
            val needM3u8Parse = isNeedM3u8Parse(downloadTaskStageEnum)
            //先解析 m3u8 ，如果没有解析过
            if (needM3u8Parse) {
                if (isStopped.get()) {
                    return
                }
                m3u8IndexParse()
            }
            stage = task.stage
            downloadTaskStageEnum = DownloadTaskStage.valueOf(stage)
            //如果 m3u8 解析失败了
            if (needM3u8Parse && DownloadTaskStage.M3U8_PARSE_FAILED == downloadTaskStageEnum) {
                return
            }
            // 如果需要进行片段下载
            if (DownloadTaskStage.M3U8_PARSED == downloadTaskStageEnum || DownloadTaskStage.DOWNLOADING == downloadTaskStageEnum || DownloadTaskStage.DOWNLOAD_FAILED == downloadTaskStageEnum) {
                if (isStopped.get()) {
                    return
                }
                downloadMediaSegments(task)
            }
            stage = task.stage
            downloadTaskStageEnum = DownloadTaskStage.valueOf(stage)
            // 片段下载是否失败
            if (DownloadTaskStage.DOWNLOAD_FAILED == downloadTaskStageEnum) {
                return
            }
            // 如果需要媒体片段合并
            if (DownloadTaskStage.DOWNLOAD_FINISHED == downloadTaskStageEnum || DownloadTaskStage.SEGMENT_MERGING == downloadTaskStageEnum || DownloadTaskStage.SEGMENT_MERGE_FAILED == downloadTaskStageEnum
            ) {
                if (isStopped.get()) {
                    return
                }
                mergerMediaSegment(task)
            }
            stage = task.stage
            downloadTaskStageEnum = DownloadTaskStage.valueOf(stage)
            // 是否合并失败
            if (DownloadTaskStage.SEGMENT_MERGE_FAILED == downloadTaskStageEnum) {
                return
            }
            if (isStopped.get()) {
                return
            }
            // 成功（完成）
            task.stage = DownloadTaskStage.FINISHED.name
            task.status = DownloadTaskStatus.FINISHED.name
            task.finishedTime = Date().getTime()
            val endTime = System.currentTimeMillis()
            task.downloadDuration = task.downloadDuration + (endTime - beginTime)
            repository.updateById(task)
            publishStatus(DownloadTaskStatus.FINISHED, 100.0, DownloadTaskStage.FINISHED)
        } finally {
            val reason = if (isStopped.get()) "手动停止" else "下载完成"
            logger.info("任务(ID:{})下载线程终止退出({})！", task.id, reason)
        }
    }

    private fun mergerMediaSegment(task: DownloadTask) {
        task.stage = DownloadTaskStage.SEGMENT_MERGING.name
        task.status = DownloadTaskStatus.RUNNING.name
        repository.updateById(task)
        publishStatus(DownloadTaskStatus.RUNNING, 100.0, DownloadTaskStage.SEGMENT_MERGING)
        val tid = task.id
        try {
            // 合并媒体片段
            val list: MutableList<MediaSegment?>? = repository.getByTaskId(tid, true)
            val fileSegments = list!!.stream().map<String>(MediaSegment::filePath).collect(Collectors.toList())
            val downloadDir: String? = ApplicationStore.getSystemConfig().getDownloadDir()
            var saveFilename = task.saveFileName
            if (saveFilename == null || saveFilename.trim { it <= ' ' }.isEmpty()) {
                saveFilename = tid.toString()
            }
            saveFilename = saveFilename.replace(" ", "") + ".mp4" // windows 打开文件时，文件名不能有空格
            val targetFilePath = downloadDir + File.separator + saveFilename
            FfmpegUtil.mergeTS(fileSegments, targetFilePath, true)
            task.stage = DownloadTaskStage.SEGMENT_MERGED.name
            task.status = DownloadTaskStatus.RUNNING.name
            task.filePath = targetFilePath
            task.saveFileName = saveFilename
            repository.updateById(task)
            publishStatus(DownloadTaskStatus.RUNNING, 0.0, DownloadTaskStage.SEGMENT_MERGED)
        } catch (e: Exception) {
            task.stage = DownloadTaskStage.SEGMENT_MERGE_FAILED.name
            task.status = DownloadTaskStatus.STOPPED_ERROR.name
            repository.updateById(task)
            publishStatus(DownloadTaskStatus.STOPPED_ERROR, 0.0, DownloadTaskStage.SEGMENT_MERGE_FAILED)
        }
    }

    private fun downloadMediaSegments(task: DownloadTask) {
        task.stage = DownloadTaskStage.DOWNLOADING.name
        task.status = DownloadTaskStatus.RUNNING.name
        startDownloadRateUpdateThread()
        repository.updateById(task)
        publishStatus(
            DownloadTaskStatus.RUNNING,
            getProgress(task),
            DownloadTaskStage.DOWNLOADING
        )
        val tid = task.id
        try {
            // 下载片段
            val futures = ArrayList<Future<MediaSegment?>>()
            var maxThreadCount = task.maxThreadCount
            maxThreadCount =
                if (maxThreadCount == 0) ApplicationStore.getSystemConfig().getDefaultThreadCount() else maxThreadCount
            logger.info("使用最大{}个线程去下载任务（ID:{}）", maxThreadCount, tid)
            while (true) {
                if (isStopped.get()) {
                    return
                }
                val mediaSegmentEntities: MutableList<MediaSegment>? =
                    repository.getByTaskId(tid, false, maxThreadCount.toBoolean())
                if (mediaSegmentEntities!!.isEmpty()) break
                futures.clear()
                for (mediaSegmentEntity in mediaSegmentEntities) {
                    val future: Future<MediaSegment?>? = threadPool.submit<MediaSegment?>(Callable {
                        val staterTime = System.currentTimeMillis()
                        val url = mediaSegmentEntity.url
                        if (logger.isInfoEnabled()) {
                            logger.info("开始下载任务(ID:{})媒体片段{}", mediaSegmentEntity.taskId, url)
                        }
                        val inputStream = getAsInputStream(url)
                        val tempDir: File = File(
                            ApplicationStore.getTmpDir(),
                            "m3u8_" + task.createTime.getTime()
                        )
                        FileUtil.ensureDirExist(tempDir)
                        val tempFile = File(tempDir, mediaSegmentEntity.id.toString() + ".ts")
                        val fos = FileOutputStream(tempFile)
                        DataStreamUtil.copy(inputStream, fos, true, true, bytesCounter)
                        mediaSegmentEntity.finished = true
                        mediaSegmentEntity.filePath = tempFile.getAbsolutePath()
                        val endTime = System.currentTimeMillis()
                        val duration = endTime - staterTime
                        mediaSegmentEntity.downloadDuration = duration
                        repository.updateById(mediaSegmentEntity)
                        if (logger.isInfoEnabled()) {
                            logger.info("任务(ID:{})媒体片段下载完成{}", mediaSegmentEntity.taskId, url)
                        }
                        mediaSegmentEntity
                    })
                    futures.add(future!!)
                }
                // 等待所有的媒体片段下载完成
                for (future in futures) {
                    try {
                        future.get()
                        task.finishMediaSegment = task.finishMediaSegment + 1
                        task.status = DownloadTaskStatus.RUNNING.name
                        repository.updateById(task)
                        publishStatus(
                            DownloadTaskStatus.RUNNING,
                            getProgress(task),
                            DownloadTaskStage.DOWNLOADING
                        )
                    } catch (e: Exception) {
                        logger.error(e.message)
                        if (isStopped.get()) {
                            logger.info("手动停止。。。")
                            return
                        }
                    }
                }
            }

            task.stage = DownloadTaskStage.DOWNLOAD_FINISHED.name
            task.status = DownloadTaskStatus.RUNNING.name
            repository.updateById(task)
            publishStatus(DownloadTaskStatus.RUNNING, 100.0, DownloadTaskStage.DOWNLOAD_FINISHED)
        } catch (e: Exception) {
            task.stage = DownloadTaskStage.DOWNLOAD_FAILED.name
            task.status = DownloadTaskStatus.STOPPED_ERROR.name
            repository.updateById(task)
            publishStatus(
                DownloadTaskStatus.STOPPED_ERROR, getProgress(task),
                DownloadTaskStage.DOWNLOAD_FAILED
            )
        }
    }

    private fun startDownloadRateUpdateThread() {
        threadPool.submit(Runnable {
            while (!isStopped.get()) {
                publishDownloadRate(bytesCounter.get(), 1000)
                bytesCounter.set(0L)
                try {
                    sleep(1000)
                } catch (ignore: InterruptedException) {
                }
            }
        })
    }

    private fun isNeedM3u8Parse(downloadTaskStageEnum: DownloadTaskStage?): Boolean {
        return DownloadTaskStage.NEW == downloadTaskStageEnum || DownloadTaskStage.M3U8_PARSING == downloadTaskStageEnum || DownloadTaskStage.M3U8_PARSE_FAILED == downloadTaskStageEnum
    }

    /**
     * m3u8 索引文件解析
     */
    private fun m3u8IndexParse() {
        val tid = task.id.toInt()
        val url = task.url
        logger.info("开始解析任务对应的 m3u8 url: {} tid:{}", url, tid)
        task.stage = DownloadTaskStage.M3U8_PARSING.name
        task.status = DownloadTaskStatus.RUNNING.name
        repository.updateById(task)
        publishStatus(
            DownloadTaskStatus.RUNNING, null,
            DownloadTaskStage.M3U8_PARSING
        )
        try {
            val res: Future<MutableList<MediaSegment?>> = threadPool.submit<T?>(
                Runnable { m3U8Parser.playlistParse(url) }
            )
            val mediaSegments = res.get()
            if (!mediaSegments.isEmpty()) {
                repository.saveAllMediaSegments(tid.toLong(), mediaSegments)
            } else {
                throw RuntimeException("解析 m3u8 索引文件失败，文件内容为空！")
            }
            task.totalMediaSegment = mediaSegments.size
            task.finishMediaSegment = 0
            task.stage = DownloadTaskStage.M3U8_PARSED.name
            task.status = DownloadTaskStatus.RUNNING.name
            publishStatus(DownloadTaskStatus.RUNNING, null, DownloadTaskStage.M3U8_PARSED)
        } catch (e: Exception) {
            task.stage = DownloadTaskStage.M3U8_PARSE_FAILED.name
            task.status = DownloadTaskStatus.STOPPED_ERROR.name
            publishStatus(DownloadTaskStatus.STOPPED_ERROR, null, DownloadTaskStage.M3U8_PARSE_FAILED)
            logger.error("解析任务对应的 m3u8 url: {} tid:{} 失败！", url, tid, e)
        }
        repository.updateById(task)
    }

    private fun publishDownloadRate(length: Long, duration: Long) {
        if (this.isStopped.get()) return
        val seconds = duration / 1000.0
        val rate = length / seconds.toLong()
        val rateHumanReadable: String? = FileUtil.bytesToHumanReadable(rate)
        eventNotifier.publish(DownloadRateChangeEvent(this.task.id, rateHumanReadable))
    }

    private fun publishStatus(statusEnum: DownloadTaskStatus?, progress: Double?, stageEnum: DownloadTaskStage?) {
        val progressAndStatus: ProgressAndStatus = ProgressAndStatus(statusEnum, progress, stageEnum)
        eventNotifier.publish(
            DownloadTaskStatusChangeEvent(
                DownloadTaskStatus(this.task.id, progressAndStatus)
            )
        )
    }

    fun stopDownload() {
        isStopped.set(true)
        // 产生中断，让等待的 Future 退出等待
        this.interrupt()
    }

    private class TaskDownloadThreadFactory : ThreadFactory {
        private val threadCounter = AtomicInteger(0)

        override fun newThread(runnable: Runnable?): Thread {
            val thread = Thread(runnable)
            thread.setName("TaskDownloadThread " + threadCounter.getAndIncrement())
            return thread
        }
    }

    companion object {
        private val cpuCores = Runtime.getRuntime().availableProcessors()
        private val threadPool = ThreadPoolExecutor(
            cpuCores * 10, cpuCores * 20, 60, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable?>(1000), TaskDownloadThreadFactory(),
            ThreadPoolExecutor.AbortPolicy()
        )

        init {
            threadPool.allowCoreThreadTimeOut(true)
        }

        private fun getProgress(task: DownloadTask): Double {
            return task.finishMediaSegment.toDouble() / task.totalMediaSegment
        }
    }
}
