package com.peihua.touchmonitor.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * HTTP 状态码异常。[code] 供重试策略区分「永久失败」与「瞬时失败」。
 */
class HttpStatusException(
    val code: Int,
    val url: String,
    val retryAfterSeconds: Long? = null,
) : IOException("请求失败，HTTP 状态码：$code（$url）")

object HttpClientUtil {
    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        // 单次调用不设总时限，大分片在弱网下可能远超 30s
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .dispatcher(
            // 默认 maxRequestsPerHost 只有 5，而分片几乎全在同一 host，
            // 不放开的话上层写多少路信号量都只能跑 5 路
            Dispatcher().apply {
                maxRequests = 64
                maxRequestsPerHost = 16
            }
        )
        .build()

    /**
     * 发起 GET 请求，调用方负责关闭返回的 [Response]。协程取消时会 cancel 底层
     * [Call]，这是唯一能真正中断阻塞在 socket read 上的手段。
     */
    suspend fun get(
        url: String,
        referer: String? = null,
        userAgent: String = DEFAULT_USER_AGENT,
    ): Response {
        val call = client.newCall(buildRequest(url, referer, userAgent))
        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!cont.isCancelled) cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        val retryAfter = response.header("Retry-After")?.toLongOrNull()
                        response.close()
                        cont.resumeWithException(HttpStatusException(response.code, url, retryAfter))
                        return
                    }
                    // 已取消时直接关掉，避免泄漏连接；剩余的窄窗口由 call.cancel() 兜底
                    if (cont.isActive) cont.resume(response) else response.close()
                }
            })
        }
    }

    suspend fun getAsString(url: String, referer: String? = null): String =
        get(url, referer).use { it.body.string() }

    suspend fun getAsBytes(url: String, referer: String? = null): ByteArray =
        get(url, referer).use { it.body.bytes() }

    /**
     * 发起 HEAD 请求，只取响应头（Content-Length 等），不下载 body。
     * 用于提前估算文件大小。失败时返回 -1。
     */
    suspend fun headContentLength(
        url: String,
        referer: String? = null,
    ): Long {
        val request = Request.Builder()
            .url(url)
            .head()
            .header("user-agent", DEFAULT_USER_AGENT)
            .apply { referer?.takeIf { it.isNotBlank() }?.let { header("Referer", it) } }
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body.contentLength() else -1L
            }
        } catch (_: IOException) {
            -1L
        }
    }

    private fun buildRequest(url: String, referer: String?, userAgent: String): Request =
        Request.Builder()
            .url(url)
            .get()
            .header("user-agent", userAgent)
            .apply { referer?.takeIf { it.isNotBlank() }?.let { header("Referer", it) } }
            .build()
}
