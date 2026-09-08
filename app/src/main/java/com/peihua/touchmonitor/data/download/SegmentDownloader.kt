package com.peihua.touchmonitor.data.download

import com.peihua.touchmonitor.model.DownloadTask
import com.peihua.touchmonitor.model.MediaSegment
import com.peihua.touchmonitor.utils.HttpClientUtil
import com.peihua.touchmonitor.utils.copyToCancellable
import com.peihua8858.tools.utils.wLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class DownloadedSegment(val file: File, val byteSize: Long, val costMillis: Long)

/**
 * 单分片下载：取回 → （必要时）解密 → 原子落盘 → 内容校验。
 *
 * 解密发生在落盘之前，所以磁盘上的 `.ts` 一律是明文，合并阶段完全不感知加密，
 * 续传与重试逻辑也不受影响。
 */
class SegmentDownloader(private val keyProvider: M3u8KeyProvider) {

    suspend fun download(
        task: DownloadTask,
        segment: MediaSegment,
        dir: File,
        bytesCounter: AtomicLong,
        isFmp4: Boolean = false,
    ): DownloadedSegment {
        val startedAt = System.currentTimeMillis()
        val suffix = if (isFmp4) ".m4s" else ".ts"
        val target = File(dir, "%09d%s".format(segment.seq, suffix))
        val part = File(dir, target.name + PART_SUFFIX)

        val response = HttpClientUtil.get(segment.url, task.referer)
        try {
            // Content-Length 必须在 body 关闭前读，否则拿不到
            val expected = response.body.contentLength()
            val networkBytes = if (segment.keyUri != null) {
                writeDecrypted(task, segment, response, part, bytesCounter)
            } else {
                writePlain(segment, response, part, bytesCounter, isFmp4)
            }
            // 字节数比对是检测截断响应的唯一可靠手段
            if (expected >= 0 && networkBytes != expected) {
                part.delete()
                throw M3u8Exception.SegmentTruncated(segment.url, expected, networkBytes)
            }
        } finally {
            response.close()
        }

        // 先 rename 成功再落库。顺序反了的话进程被杀会留下「已完成但文件不完整」的记录
        if (!part.renameTo(target)) {
            part.delete()
            throw M3u8Exception.SegmentCorrupted(segment.url, "临时文件重命名失败")
        }
        return DownloadedSegment(target, target.length(), System.currentTimeMillis() - startedAt)
    }

    private suspend fun writePlain(
        segment: MediaSegment,
        response: Response,
        part: File,
        bytesCounter: AtomicLong,
        isFmp4: Boolean = false,
    ): Long = withContext(Dispatchers.IO) {
        var head: ByteArray? = null
        val written = response.body.byteStream().use { input ->
            part.outputStream().buffered().use { output ->
                input.copyToCancellable(output, bytesCounter) { head = it }
            }
        }
        validateContent(segment, response, head, written, part, isFmp4)
        written
    }

    /**
     * 整块 `doFinal` 而不是 `CipherInputStream`：后者在末块 padding 出错时静默返回 -1、
     * 不抛异常，又变成「静默产出坏分片」。分片按 HLS 惯例是几秒的小文件，
     * 整块读入内存的峰值可接受。
     */
    private suspend fun writeDecrypted(
        task: DownloadTask,
        segment: MediaSegment,
        response: Response,
        part: File,
        bytesCounter: AtomicLong,
    ): Long = withContext(Dispatchers.IO) {
        val keyUri = requireNotNull(segment.keyUri)
        val key = keyProvider.keyFor(task.id, keyUri, task.referer)
        val iv = keyProvider.ivFor(segment.keyIv, segment.seq)

        val cipherText = response.body.bytes()
        bytesCounter.addAndGet(cipherText.size.toLong())
        validateContent(
            segment,
            response,
            cipherText.copyOf(minOf(cipherText.size, HEAD_SNIFF_SIZE)),
            cipherText.size.toLong(),
            part,
        )

        val plain = try {
            Cipher.getInstance(AES_TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            }.doFinal(cipherText)
        } catch (e: java.security.GeneralSecurityException) {
            throw M3u8Exception.DecryptFailed(segment.url, e)
        }
        if (plain.isEmpty()) throw M3u8Exception.DecryptFailed(segment.url)
        // padding 校验有 1/256 的概率漏掉错误密钥，用首字节做二次确认。
        // 纯音频分片是 ADTS 而不是 TS，首字节合法地是 0xFF，所以两者都放过。
        if (!plain.hasMediaSyncByte()) {
            throw M3u8Exception.DecryptFailed(segment.url)
        }
        part.writeBytes(plain)
        // 速率按网络字节算，返回值用于与 Content-Length 比对
        cipherText.size.toLong()
    }

    /**
     * CDN 返回错误页时 HTTP 状态码常常是 200，这是「下载成功但视频打不开」的常见来源。
     */
    private fun validateContent(
        segment: MediaSegment,
        response: Response,
        head: ByteArray?,
        written: Long,
        part: File,
        isFmp4: Boolean = false,
    ) {
        if (written == 0L) {
            part.delete()
            throw M3u8Exception.SegmentCorrupted(segment.url, "响应为空")
        }
        val sample = head ?: return
        val text = String(sample, Charsets.ISO_8859_1).trimStart()
        val looksLikeErrorPage = ERROR_PAGE_PREFIXES.any { text.startsWith(it, ignoreCase = true) }
        val htmlContentType = response.header("Content-Type")
            ?.startsWith("text/html", ignoreCase = true) == true && written < HTML_ERROR_MAX_BYTES
        if (looksLikeErrorPage || htmlContentType) {
            part.delete()
            throw M3u8Exception.SegmentCorrupted(segment.url, "CDN 返回了错误页而非视频分片")
        }
        if (!isFmp4 && segment.keyUri == null && !sample.hasMediaSyncByte()) {
            wLog { "分片首字节不像 TS/ADTS：${segment.url}" }
        }
    }

    /** TS 同步字节 0x47，或 ADTS 音频帧同步字（前 12 位全 1） */
    private fun ByteArray.hasMediaSyncByte(): Boolean {
        if (isEmpty()) return false
        if (this[0] == TS_SYNC_BYTE) return true
        return size >= 2 && this[0] == ADTS_SYNC_BYTE &&
                (this[1].toInt() and 0xF0) == 0xF0
    }

    private companion object {
        const val PART_SUFFIX = ".part"
        const val TS_SYNC_BYTE: Byte = 0x47
        const val ADTS_SYNC_BYTE: Byte = 0xFF.toByte()
        const val HEAD_SNIFF_SIZE = 64
        const val HTML_ERROR_MAX_BYTES = 4096
        // HLS 的 PKCS7 在 AES 块长下与 PKCS5 等价，Conscrypt 自带该实现
        const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
        val ERROR_PAGE_PREFIXES = listOf("<!DOCTYPE", "<html", "<?xml", "{\"")
    }
}
