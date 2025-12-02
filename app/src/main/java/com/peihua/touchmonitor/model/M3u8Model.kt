package com.peihua.touchmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.DataStore

data class DownloadConfig(val downloadDir: String, val defaultThreadCount: Int, val defaultTimeoutRetryCount: Int) {
    companion object {
        val default: DownloadConfig = DownloadConfig("", 5, 30)
    }
}

@get:Synchronized
val downloadStore: DataStore<DownloadConfig> by lazy {
    DataStore(
        ServiceApplication.application,
        DownloadConfig.default,
        typeToken = object : TypeToken<DownloadConfig>() {},
        fileName = "download_config.json"
    )
}

/**
 * 下载任务数据库实体类
 */
@Entity(tableName = "download_task")
data class DownloadTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val url: String,
    val filePath: String,
    val saveFileName: String,
    val totalMediaSegment: Long,
    val finishMediaSegment: Long,
    val downloadDuration: Long,
    /**
     * 任务状态，是停止还是运行中
     */
    val status: String,
    val resolution: String,
    /**
     * 下载所处的阶段
     *
     * @see DownloadTaskStageEnum
     */
    val stage: String,
    val maxThreadCount: Int,
    val createTime: Long,
    val updateTime: Long,
)

/**
 * 媒体分片信息存储实体类
 */
@Entity(tableName = "media_segment")
data class MediaSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val taskId: Long,
    val url: String,
    val finished: Boolean,
    val duration: Long,
    val downloadDuration: Long,
    val filePath: String,
)


/**
 *
 * 下载任务生命周期状态枚举
 *
 *
 * 下载任务生命周期分为 5 个阶段：新建->m3u8 索引文件解析->下载->媒体片段合并->完成，
 * 有的阶段可能包含多个状态，但同时只能处于一个状态，具体见下面的枚举值。
 *
 * @author dingpeihua
 * @date 2025/12/1 11:28
 **/
enum class DownloadTaskStageEnum(val status: String) {
    NEW("新建"),
    M3U8_PARSING("m3u8 解析中"),
    M3U8_PARSED("m3u8 已经解析完成"),
    M3U8_PARSE_FAILED("m3u8 解析失败"),
    DOWNLOADING("正在下载中"),
    DOWNLOAD_FAILED("下载失败"),
    DOWNLOAD_FINISHED("下载完成"),
    SEGMENT_MERGING("媒体片段合并中"),
    SEGMENT_MERGED("媒体片段合并完成"),
    SEGMENT_MERGE_FAILED("媒体片段合并失败"),
    STOPPED("手动停止"),
    FINISHED("完成");

    companion object {
        fun isRunning(statusEnum: DownloadTaskStageEnum): Boolean {
            return statusEnum != NEW && statusEnum != FINISHED && statusEnum != M3U8_PARSE_FAILED && statusEnum != DOWNLOAD_FAILED
        }
    }
}


/**
 *
 * 下载任务状态枚举
 *
 *
 * 下载任务状态分为 3 个：新建，运行中，手动停止和异常停止，
 * 该枚举与 [DownloadTaskStageEnum] 作用不同，
 * [DownloadTaskStageEnum] 用于记录下载处于哪个阶段，而该枚举记录任务的状态
 * @author dingpeihua
 * @date 2025/12/1 11:28
 **/
enum class DownloadTaskStatusEnum(val status: String) {
    NEW("新建"),
    RUNNING("运行中"),
    STOPPED_MANUAL("停止"),
    STOPPED_ERROR("失败"),
    FINISHED("完成");
}