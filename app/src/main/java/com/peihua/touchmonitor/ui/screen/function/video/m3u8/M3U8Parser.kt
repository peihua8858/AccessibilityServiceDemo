package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import com.peihua.touchmonitor.data.download.M3u8Exception
import com.peihua.touchmonitor.utils.HttpClientUtil
import com.peihua8858.tools.utils.dLog
import io.lindstrom.m3u8.model.KeyMethod
import io.lindstrom.m3u8.model.MasterPlaylist
import io.lindstrom.m3u8.model.MediaPlaylist
import io.lindstrom.m3u8.model.SegmentKey
import io.lindstrom.m3u8.model.Variant
import io.lindstrom.m3u8.parser.MasterPlaylistParser
import io.lindstrom.m3u8.parser.MediaPlaylistParser
import io.lindstrom.m3u8.parser.ParsingMode
import io.lindstrom.m3u8.parser.PlaylistParserException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import kotlin.math.roundToLong

/** 已选定单条码流并展开完毕的播放列表 */
data class ParsedPlaylist(
    /** 真正含分片的媒体列表地址（master 时是选中 variant 的地址） */
    val playlistUrl: String,
    val resolution: String,
    val bandwidth: Long,
    val segments: List<ParsedSegment>,
    val isFmp4: Boolean,
    val initSegmentUrl: String?,
)

data class ParsedSegment(
    /** 绝对媒体序号 = `#EXT-X-MEDIA-SEQUENCE` + 下标，既是排序键也是 AES 缺省 IV 的来源 */
    val seq: Long,
    val url: String,
    val durationMs: Long,
    /** null 表示该分片未加密 */
    val keyMethod: String? = null,
    val keyUri: String? = null,
    /** `#EXT-X-KEY` 里的 IV 原文（0x...），null 表示按 [seq] 推导 */
    val keyIv: String? = null,
)

/**
 * M3U8 索引文件解析器。
 *
 * [fetch] 可注入以便离线单测，默认走 [HttpClientUtil]。
 */
class M3U8Parser(
    private val fetch: suspend (url: String, referer: String?) -> String =
        { url, referer -> HttpClientUtil.getAsString(url, referer) },
) {
    // 两个 parser 都是无状态可复用的；必须用 LENIENT，
    // 否则真实站点常见的厂商私有 tag 会让整个解析直接抛异常
    private val masterParser = MasterPlaylistParser(ParsingMode.LENIENT)
    private val mediaParser = MediaPlaylistParser(ParsingMode.LENIENT)

    /**
     * @param preferredResolution 形如 `1920x1080`，命中则优先选该码流，否则取最高清晰度
     */
    suspend fun parse(
        url: String,
        preferredResolution: String = "",
        referer: String? = null,
    ): ParsedPlaylist = withContext(Dispatchers.IO) {
        val text = fetchPlaylist(url, referer)
        val variant = if (text.contains(TAG_STREAM_INF)) {
            selectVariant(readMaster(text, url).variants(), preferredResolution)
        } else {
            null
        }
        if (variant == null) {
            return@withContext buildPlaylist(url, text, preferredResolution, 0L)
        }
        val mediaUrl = url.toHttpUrl(url).resolveOrThrow(variant.uri())
        dLog { "master 列表选中码流：${variant.resolutionText()} / ${variant.bandwidth()}bps -> $mediaUrl" }
        buildPlaylist(mediaUrl, fetchPlaylist(mediaUrl, referer), variant.resolutionText(), variant.bandwidth())
    }

    private suspend fun fetchPlaylist(url: String, referer: String?): String {
        val text = try {
            fetch(url, referer)
        } catch (e: IOException) {
            throw M3u8Exception.ParseFailed("无法下载 m3u8 索引文件：${e.message}", e)
        }
        // readPlaylist(String) 按 "\n" 切分，CRLF 会残留 "\r" 让 "#EXTM3U" 匹配失败
        val normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim()
        if (!normalized.startsWith(TAG_EXTM3U)) {
            throw M3u8Exception.ParseFailed("目标地址不是 m3u8 索引文件")
        }
        return normalized
    }

    private fun readMaster(text: String, url: String): MasterPlaylist = try {
        masterParser.readPlaylist(text)
    } catch (e: PlaylistParserException) {
        throw M3u8Exception.ParseFailed("m3u8 主列表解析失败（$url）：${e.message}", e)
    } catch (e: IllegalStateException) {
        throw M3u8Exception.ParseFailed("m3u8 主列表不完整（$url）：${e.message}", e)
    }

    private fun buildPlaylist(
        mediaUrl: String,
        text: String,
        resolution: String,
        bandwidth: Long,
    ): ParsedPlaylist {
        val playlist: MediaPlaylist = try {
            mediaParser.readPlaylist(text)
        } catch (e: PlaylistParserException) {
            throw M3u8Exception.ParseFailed("m3u8 媒体列表解析失败（$mediaUrl）：${e.message}", e)
        } catch (e: IllegalStateException) {
            // vendored 的 immutables builder 缺少必填属性（如 TARGETDURATION）时抛的是这个
            throw M3u8Exception.ParseFailed("m3u8 媒体列表不完整（$mediaUrl）：${e.message}", e)
        }
        val raw = playlist.mediaSegments()
        if (raw.isEmpty()) {
            throw M3u8Exception.ParseFailed("m3u8 索引文件中没有任何分片")
        }

        val base = mediaUrl.toHttpUrl(mediaUrl)
        val mediaSequence = playlist.mediaSequence()

        // vendored 的 MediaPlaylistParser 在 onURI 里重置 segmentBuilder，
        // 所以 #EXT-X-KEY / #EXT-X-MAP 只挂在声明它的那一片上，必须自己向后传播。
        // 直接信 ms.segmentKey() 的后果是只有第一片能解密，其余全是密文垃圾，
        // 而 ffmpeg concat 仍会「成功」，产出只有开头能播的 mp4。
        var currentKey: SegmentKey? = null
        var initSegmentUrl: String? = null

        val segments = raw.mapIndexed { index, ms ->
            ms.segmentKey().orElse(null)?.let { currentKey = it }
            ms.segmentMap().orElse(null)?.let { map ->
                if (initSegmentUrl == null) initSegmentUrl = base.resolveOrThrow(map.uri())
            }
            val key = currentKey?.takeIf { it.method() != KeyMethod.NONE }?.also { validateKey(it) }
            ParsedSegment(
                seq = mediaSequence + index,
                url = base.resolveOrThrow(ms.uri()),
                durationMs = (ms.duration() * 1000).roundToLong(),
                keyMethod = key?.method()?.toString(),
                keyUri = key?.uri()?.orElse(null)?.let { base.resolveOrThrow(it) },
                keyIv = key?.iv()?.orElse(null),
            )
        }

        val isFmp4 = initSegmentUrl != null || segments.any { it.url.isFmp4Segment() }
        dLog { "解析完成：${segments.size} 个分片，加密=${segments.any { it.keyUri != null }}，fMP4=$isFmp4" }
        return ParsedPlaylist(
            playlistUrl = mediaUrl,
            resolution = resolution,
            bandwidth = bandwidth,
            segments = segments,
            isFmp4 = isFmp4,
            initSegmentUrl = initSegmentUrl,
        )
    }

    /** 提前拒绝不支持的加密：宁可 3 秒失败，也不要下载 40 分钟产出垃圾 */
    private fun validateKey(key: SegmentKey) {
        val keyFormat = key.keyFormat().orElse(KEY_FORMAT_IDENTITY)
        if (!keyFormat.equals(KEY_FORMAT_IDENTITY, ignoreCase = true)) {
            throw M3u8Exception.DrmProtected(keyFormat)
        }
        if (key.method() != KeyMethod.AES_128) {
            throw M3u8Exception.UnsupportedEncryption(key.method().toString())
        }
        if (!key.uri().isPresent) {
            throw M3u8Exception.ParseFailed("#EXT-X-KEY 缺少 URI，无法获取解密密钥")
        }
    }

    /**
     * 只能选一条码流。含 1080p/720p/480p/audio 的 master 若全下，
     * 会 concat 成 4 倍时长、清晰度反复跳变的文件。
     */
    private fun selectVariant(variants: List<Variant>, preferred: String): Variant? {
        if (variants.isEmpty()) return null
        // 过滤纯音频轨：没有 resolution 且 codecs 只含 mp4a
        val playable = variants.filterNot { v ->
            !v.resolution().isPresent && v.codecs().isNotEmpty() &&
                    v.codecs().all { it.startsWith("mp4a") }
        }.ifEmpty { variants }

        if (preferred.isNotBlank()) {
            playable.firstOrNull { it.resolutionText() == preferred }?.let { return it }
        }
        return playable.maxWith(
            compareBy({ it.resolution().map { r -> r.height() }.orElse(0) }, { it.bandwidth() })
        )
    }

    private fun Variant.resolutionText(): String =
        resolution().map { "${it.width()}x${it.height()}" }.orElse("")

    private fun String.toHttpUrl(context: String): HttpUrl =
        toHttpUrlOrNull() ?: throw M3u8Exception.ParseFailed("无效的 m3u8 地址：$context")

    /**
     * 用 [HttpUrl.resolve] 而不是字符串拼接。拼接对 `/hls/a.ts`、`../a.ts`、
     * `//cdn.x.com/a.ts` 三种极常见形式全错，密钥 URI 尤其常见 `/hls/key?token=xxx`。
     */
    private fun HttpUrl.resolveOrThrow(ref: String): String =
        resolve(ref)?.toString() ?: throw M3u8Exception.ParseFailed("无法解析地址：$ref")

    private fun String.isFmp4Segment(): Boolean {
        val path = toHttpUrlOrNull()?.encodedPath ?: this
        return FMP4_SUFFIXES.any { path.endsWith(it, ignoreCase = true) }
    }

    private companion object {
        const val TAG_EXTM3U = "#EXTM3U"
        const val TAG_STREAM_INF = "#EXT-X-STREAM-INF"
        const val KEY_FORMAT_IDENTITY = "identity"
        val FMP4_SUFFIXES = listOf(".m4s", ".mp4", ".m4v", ".cmfv")
    }
}
