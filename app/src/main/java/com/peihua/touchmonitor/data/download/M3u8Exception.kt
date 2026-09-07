package com.peihua.touchmonitor.data.download

import com.peihua.touchmonitor.model.DownloadTaskStage
import com.peihua.touchmonitor.utils.HttpStatusException
import java.io.IOException

/**
 * m3u8 下载链路上的领域异常。[userMessage] 会直接展示给用户，[stage] 决定任务落到哪个失败阶段。
 *
 * [permanent] 为 true 表示重试无意义，分片会被一次性判死，不再消耗退避时间。
 */
sealed class M3u8Exception(
    val stage: DownloadTaskStage,
    val userMessage: String,
    cause: Throwable? = null,
) : Exception(userMessage, cause) {

    open val permanent: Boolean get() = true

    class ParseFailed(message: String, cause: Throwable? = null) :
        M3u8Exception(DownloadTaskStage.M3U8_PARSE_FAILED, message, cause)

    class UnsupportedEncryption(method: String) :
        M3u8Exception(DownloadTaskStage.M3U8_PARSE_FAILED, "暂不支持的加密方式：$method，仅支持 AES-128")

    class DrmProtected(keyFormat: String) :
        M3u8Exception(DownloadTaskStage.M3U8_PARSE_FAILED, "该视频受 DRM 保护（$keyFormat），无法下载")

    class UnsupportedFmp4 :
        M3u8Exception(DownloadTaskStage.M3U8_PARSE_FAILED, "暂不支持 fMP4（#EXT-X-MAP）分片格式")

    /** CDN 用 200 返回了错误页，是「下载成功但视频打不开」的常见来源 */
    class SegmentCorrupted(url: String, reason: String) :
        M3u8Exception(DownloadTaskStage.DOWNLOAD_FAILED, "分片内容异常（$reason）：$url")

    /** 响应被截断，重试有意义 */
    class SegmentTruncated(url: String, expected: Long, actual: Long) :
        M3u8Exception(
            DownloadTaskStage.DOWNLOAD_FAILED,
            "分片下载不完整，期望 $expected 字节实际 $actual 字节：$url",
        ) {
        override val permanent: Boolean get() = false
    }

    class KeyFetchFailed(keyUri: String, reason: String, cause: Throwable? = null) :
        M3u8Exception(DownloadTaskStage.DOWNLOAD_FAILED, "获取解密密钥失败（$reason）：$keyUri", cause) {
        override val permanent: Boolean get() = false
    }

    class DecryptFailed(url: String, cause: Throwable? = null) :
        M3u8Exception(DownloadTaskStage.DOWNLOAD_FAILED, "分片解密失败，密钥或 IV 不正确：$url", cause)

    class DownloadIncomplete(failedSegments: Int) :
        M3u8Exception(DownloadTaskStage.DOWNLOAD_FAILED, "有 $failedSegments 个分片下载失败，已放弃")

    class MergeFailed(message: String, cause: Throwable? = null) :
        M3u8Exception(DownloadTaskStage.SEGMENT_MERGE_FAILED, message, cause)
}

/** 永久性 HTTP 状态码：重试只是浪费流量 */
private val PERMANENT_HTTP_CODES = setOf(400, 401, 403, 404, 405, 410, 451)

/** 判断异常是否值得重试。CancellationException 由调用方在此之前处理，不会走到这里。 */
fun Throwable.isPermanentFailure(): Boolean = when (this) {
    is M3u8Exception -> permanent
    is HttpStatusException -> code in PERMANENT_HTTP_CODES
    is IOException -> false
    else -> true
}

/** 服务端明确要求的等待秒数（429 / 503 的 `Retry-After`） */
fun Throwable.retryAfterMillis(): Long? =
    (this as? HttpStatusException)?.retryAfterSeconds?.times(1000)
