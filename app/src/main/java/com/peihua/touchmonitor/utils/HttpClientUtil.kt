package com.peihua.touchmonitor.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.time.Duration

/**
 * Http客户端工具类
 *
 * @author cloudgyb
 * 2021/5/17 16:01
 */
object HttpClientUtil {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(5))
        .build()

    @Throws(IOException::class)
    fun getAsByte(url: String): ByteArray {
        return execGet(url).bytes()
    }

    @JvmStatic
    fun getAsInputStream(url: String): InputStream? {
        return try {
            execGet(url).byteStream()
        } catch (e: Exception) {
            ByteArrayInputStream.nullInputStream()
        }
    }

    @Throws(IOException::class)
    fun getAsString(url: String): String {
        return execGet(url).string()
    }

    @Throws(IOException::class)
    private fun execGet(url: String): ResponseBody {
        val uri = URI.create(url)
        val request = Request.Builder()
            .url(url)
            .get()
            .header(
                "user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36"
            )
            .header("Referer", uri.toASCIIString())
            .build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("该视频无法下载！HTTP状态码：" + response.code)
        }
        return response.body
    }
}
