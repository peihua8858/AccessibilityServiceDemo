package com.peihua.touchmonitor.data.download

import com.peihua.touchmonitor.utils.HttpClientUtil
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * AES-128 密钥获取与 IV 推导。内存 → 磁盘 → 网络三级。
 *
 * 磁盘缓存不是性能优化而是正确性优化：key URL 常带时效性 token，
 * 用户「暂停 → 明天继续」时没有缓存就会全片解密失败。
 */
class M3u8KeyProvider(private val paths: DownloadPaths) {

    private val memory = ConcurrentHashMap<String, ByteArray>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun keyFor(taskId: Long, keyUri: String, referer: String?): ByteArray {
        memory[keyUri]?.let { return it }
        // 同一 key 被 8 路分片同时请求时只拉一次
        val lock = locks.getOrPut(keyUri) { Mutex() }
        return lock.withLock {
            memory[keyUri]?.let { return@withLock it }
            val cacheFile = File(paths.keyDir(taskId), "${keyUri.sha1()}.key")
            val key = cacheFile.readCachedKey() ?: fetchKey(keyUri, referer).also { fetched ->
                runCatching { cacheFile.writeBytes(fetched) }
            }
            memory[keyUri] = key
            key
        }
    }

    /**
     * 缺省 IV 推导（RFC 8216 §5.2）：该分片的媒体序号作为 128 位大端整数。
     */
    fun ivFor(keyIv: String?, seq: Long): ByteArray {
        keyIv?.takeIf { it.isNotBlank() }?.let { return it.parseHexIv() }
        return ByteArray(AES_BLOCK_SIZE).also { iv ->
            for (i in 0 until 8) {
                iv[15 - i] = ((seq shr (8 * i)) and 0xFF).toByte()
            }
        }
    }

    private suspend fun fetchKey(keyUri: String, referer: String?): ByteArray {
        val bytes = try {
            HttpClientUtil.getAsBytes(keyUri, referer)
        } catch (e: IOException) {
            throw M3u8Exception.KeyFetchFailed(keyUri, e.message ?: "网络错误", e)
        }
        if (bytes.size != AES_BLOCK_SIZE) {
            throw M3u8Exception.KeyFetchFailed(keyUri, "密钥长度异常，期望 16 字节实际 ${bytes.size} 字节")
        }
        return bytes
    }

    private fun File.readCachedKey(): ByteArray? =
        takeIf { it.isFile && it.length() == AES_BLOCK_SIZE.toLong() }?.runCatching { readBytes() }?.getOrNull()

    private fun String.parseHexIv(): ByteArray {
        val hex = removePrefix("0x").removePrefix("0X")
        if (hex.length != AES_BLOCK_SIZE * 2) {
            throw M3u8Exception.ParseFailed("#EXT-X-KEY 的 IV 长度异常：$this")
        }
        return ByteArray(AES_BLOCK_SIZE) { i ->
            val hi = Character.digit(hex[i * 2], 16)
            val lo = Character.digit(hex[i * 2 + 1], 16)
            if (hi < 0 || lo < 0) throw M3u8Exception.ParseFailed("#EXT-X-KEY 的 IV 不是合法十六进制：$this")
            ((hi shl 4) or lo).toByte()
        }
    }

    private fun String.sha1(): String =
        MessageDigest.getInstance("SHA-1").digest(toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object {
        const val AES_BLOCK_SIZE = 16
    }
}
