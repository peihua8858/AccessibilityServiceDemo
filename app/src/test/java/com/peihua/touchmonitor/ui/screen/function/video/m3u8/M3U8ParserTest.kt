package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import com.peihua.touchmonitor.data.download.M3u8Exception
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 解析层离线单测。用注入的 fetch 替代网络，不依赖真机也不依赖 MockWebServer。
 */
class M3U8ParserTest {

    private val base = "https://cdn.example.com/hls/720p/index.m3u8"
    private val masterUrl = "https://cdn.example.com/hls/master.m3u8"

    /** `#EXT-X-TARGETDURATION` 是 RFC 8216 必填项，vendored 的 builder 缺它会直接抛异常 */
    private fun media(body: String): String =
        "#EXTM3U\n#EXT-X-TARGETDURATION:10\n${body.trimIndent()}\n#EXT-X-ENDLIST"

    private fun parse(
        url: String = base,
        preferred: String = "",
        vararg pages: Pair<String, String>,
    ): ParsedPlaylist {
        val map = pages.toMap()
        val parser = M3U8Parser { u, _ ->
            map[u] ?: error("unexpected fetch: $u\navailable: ${map.keys}")
        }
        return runBlocking { parser.parse(url, preferred) }
    }

    private fun parseMedia(body: String, url: String = base): ParsedPlaylist =
        parse(url = url, pages = arrayOf(url to media(body)))

    // region 基础

    @Test
    fun `明文列表_相对分片按同目录解析`() {
        val result = parseMedia(
            """
            #EXTINF:4.000,
            seg0.ts
            #EXTINF:3.900,
            seg1.ts
            """
        )

        assertEquals(2, result.segments.size)
        assertEquals("https://cdn.example.com/hls/720p/seg0.ts", result.segments[0].url)
        assertEquals("https://cdn.example.com/hls/720p/seg1.ts", result.segments[1].url)
        assertFalse(result.isFmp4)
        assertNull(result.segments[0].keyUri)
    }

    @Test
    fun `时长按毫秒存储_不被截断成整秒`() {
        // 桌面版存秒且是 Long，会把 9.9s 截成 9
        val result = parseMedia("#EXTINF:9.900,\nseg0.ts")

        assertEquals(9900L, result.segments[0].durationMs)
    }

    @Test
    fun `CRLF换行不会导致EXTM3U匹配失败`() {
        // readPlaylist(String) 按 "\n" 切分，残留的 "\r" 会让 "#EXTM3U" 匹配失败
        val playlist =
            "#EXTM3U\r\n#EXT-X-TARGETDURATION:10\r\n#EXTINF:4.000,\r\nseg0.ts\r\n#EXT-X-ENDLIST\r\n"

        assertEquals(1, parse(pages = arrayOf(base to playlist)).segments.size)
    }

    @Test
    fun `未知厂商tag不会让解析失败`() {
        // STRICT 模式下这里会抛 PlaylistParserException，是「明明能下却报解析失败」的主因
        val result = parseMedia(
            """
            #EXT-X-FOO:BAR=1
            #EXT-X-CUSTOM-VENDOR-THING
            #EXTINF:4.000,
            seg0.ts
            """
        )

        assertEquals(1, result.segments.size)
    }

    @Test(expected = M3u8Exception.ParseFailed::class)
    fun `非m3u8内容直接报错`() {
        parse(pages = arrayOf(base to "<html><body>404</body></html>"))
    }

    @Test(expected = M3u8Exception.ParseFailed::class)
    fun `零分片列表报错而不是静默成功`() {
        parse(pages = arrayOf(base to "#EXTM3U\n#EXT-X-TARGETDURATION:4\n#EXT-X-ENDLIST"))
    }

    @Test(expected = M3u8Exception.ParseFailed::class)
    fun `缺少TARGETDURATION时报解析失败而不是崩溃`() {
        // vendored 的 immutables builder 抛的是 IllegalStateException，必须被包装
        parse(pages = arrayOf(base to "#EXTM3U\n#EXTINF:4.000,\nseg0.ts\n#EXT-X-ENDLIST"))
    }

    // endregion

    // region URL 解析

    @Test
    fun `绝对路径_父级相对_协议相对三种分片地址都正确`() {
        val urls = parseMedia(
            """
            #EXTINF:4.000,
            /hls/abs/seg0.ts
            #EXTINF:4.000,
            ../up/seg1.ts
            #EXTINF:4.000,
            //other.example.com/seg2.ts
            #EXTINF:4.000,
            https://third.example.com/seg3.ts
            """
        ).segments.map { it.url }

        assertEquals(
            listOf(
                "https://cdn.example.com/hls/abs/seg0.ts",
                "https://cdn.example.com/hls/up/seg1.ts",
                "https://other.example.com/seg2.ts",
                "https://third.example.com/seg3.ts",
            ),
            urls,
        )
    }

    // endregion

    // region master 多码流

    @Test
    fun `master多码流只取一条最高清晰度`() {
        val master = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x480
            480p/index.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=2000000,RESOLUTION=1280x720
            720p/index.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080
            1080p/index.m3u8
        """.trimIndent()

        val result = parse(
            url = masterUrl,
            pages = arrayOf(
                masterUrl to master,
                "https://cdn.example.com/hls/1080p/index.m3u8" to media("#EXTINF:4.000,\nseg0.ts"),
            ),
        )

        // 老实现是 for (variant) { addAll(...) }，会把整部片下 3 遍再拼成 3 倍时长
        assertEquals(1, result.segments.size)
        assertEquals("1920x1080", result.resolution)
        assertEquals(5_000_000L, result.bandwidth)
        assertEquals("https://cdn.example.com/hls/1080p/index.m3u8", result.playlistUrl)
    }

    @Test
    fun `指定清晰度时优先命中该码流`() {
        val master = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=2000000,RESOLUTION=1280x720
            720p/index.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=5000000,RESOLUTION=1920x1080
            1080p/index.m3u8
        """.trimIndent()

        val result = parse(
            url = masterUrl,
            preferred = "1280x720",
            pages = arrayOf(
                masterUrl to master,
                "https://cdn.example.com/hls/720p/index.m3u8" to media("#EXTINF:4.000,\nseg0.ts"),
            ),
        )

        assertEquals("1280x720", result.resolution)
    }

    @Test
    fun `纯音频轨被过滤掉`() {
        val master = """
            #EXTM3U
            #EXT-X-STREAM-INF:BANDWIDTH=128000,CODECS="mp4a.40.2"
            audio/index.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=2000000,RESOLUTION=1280x720,CODECS="avc1.4d401f,mp4a.40.2"
            720p/index.m3u8
        """.trimIndent()

        val result = parse(
            url = masterUrl,
            pages = arrayOf(
                masterUrl to master,
                "https://cdn.example.com/hls/720p/index.m3u8" to media("#EXTINF:4.000,\nseg0.ts"),
            ),
        )

        assertEquals("1280x720", result.resolution)
    }

    // endregion

    // region 媒体序号

    @Test
    fun `seq取绝对媒体序号而不是数组下标`() {
        // seq 同时是排序键和 AES 缺省 IV 的来源，用下标推 IV 会得到全片乱码
        val result = parseMedia(
            """
            #EXT-X-MEDIA-SEQUENCE:1000
            #EXTINF:4.000,
            seg0.ts
            #EXTINF:4.000,
            seg1.ts
            """
        )

        assertEquals(listOf(1000L, 1001L), result.segments.map { it.seq })
    }

    @Test
    fun `没有MEDIA-SEQUENCE时seq从0开始`() {
        val result = parseMedia("#EXTINF:4.000,\nseg0.ts\n#EXTINF:4.000,\nseg1.ts")

        assertEquals(listOf(0L, 1L), result.segments.map { it.seq })
    }

    // endregion

    // region AES-128

    @Test
    fun `KEY只在开头声明时向后传播到所有分片`() {
        val segments = parseMedia(
            """
            #EXT-X-KEY:METHOD=AES-128,URI="/hls/key?token=abc"
            #EXTINF:4.000,
            seg0.ts
            #EXTINF:4.000,
            seg1.ts
            #EXTINF:4.000,
            seg2.ts
            """
        ).segments

        // 直接信 ms.segmentKey() 的话只有第 1 片有 key，其余全是密文垃圾，
        // 而 ffmpeg concat 仍会「成功」，产出只有开头能播的 mp4
        assertEquals(3, segments.size)
        assertTrue(segments.all { it.keyUri == "https://cdn.example.com/hls/key?token=abc" })
        assertTrue(segments.all { it.keyMethod == "AES-128" })
        assertTrue(segments.all { it.keyIv == null })
    }

    @Test
    fun `KEY中途轮换只影响其后的分片`() {
        val keys = parseMedia(
            """
            #EXT-X-KEY:METHOD=AES-128,URI="key1.key"
            #EXTINF:4.000,
            seg0.ts
            #EXTINF:4.000,
            seg1.ts
            #EXT-X-KEY:METHOD=AES-128,URI="key2.key"
            #EXTINF:4.000,
            seg2.ts
            """
        ).segments.map { it.keyUri }

        assertEquals(
            listOf(
                "https://cdn.example.com/hls/720p/key1.key",
                "https://cdn.example.com/hls/720p/key1.key",
                "https://cdn.example.com/hls/720p/key2.key",
            ),
            keys,
        )
    }

    @Test
    fun `METHOD=NONE中途关闭加密`() {
        val segments = parseMedia(
            """
            #EXT-X-KEY:METHOD=AES-128,URI="key1.key"
            #EXTINF:4.000,
            seg0.ts
            #EXT-X-KEY:METHOD=NONE
            #EXTINF:4.000,
            seg1.ts
            """
        ).segments

        assertEquals("https://cdn.example.com/hls/720p/key1.key", segments[0].keyUri)
        assertNull(segments[1].keyUri)
        assertNull(segments[1].keyMethod)
    }

    @Test
    fun `显式IV原样保留`() {
        val result = parseMedia(
            """
            #EXT-X-KEY:METHOD=AES-128,URI="key.key",IV=0x0123456789ABCDEF0123456789ABCDEF
            #EXTINF:4.000,
            seg0.ts
            """
        )

        assertEquals("0x0123456789ABCDEF0123456789ABCDEF", result.segments[0].keyIv)
    }

    @Test(expected = M3u8Exception.UnsupportedEncryption::class)
    fun `SAMPLE-AES提前拒绝`() {
        parseMedia(
            """
            #EXT-X-KEY:METHOD=SAMPLE-AES,URI="key.key"
            #EXTINF:4.000,
            seg0.ts
            """
        )
    }

    @Test(expected = M3u8Exception.DrmProtected::class)
    fun `Widevine提前拒绝`() {
        parseMedia(
            """
            #EXT-X-KEY:METHOD=SAMPLE-AES,URI="skd://x",KEYFORMAT="urn:uuid:edef8ba9-79d6-4ace-a3c8-27dcd51d21ed"
            #EXTINF:4.000,
            seg0.ts
            """
        )
    }

    @Test(expected = M3u8Exception.ParseFailed::class)
    fun `AES-128缺少URI时报错`() {
        parseMedia(
            """
            #EXT-X-KEY:METHOD=AES-128
            #EXTINF:4.000,
            seg0.ts
            """
        )
    }

    // endregion

    // region fMP4

    @Test
    fun `EXT-X-MAP被识别为fMP4并解析出init分片`() {
        val result = parseMedia(
            """
            #EXT-X-MAP:URI="init.mp4"
            #EXTINF:4.000,
            seg0.m4s
            #EXTINF:4.000,
            seg1.m4s
            """
        )

        assertTrue(result.isFmp4)
        assertEquals("https://cdn.example.com/hls/720p/init.mp4", result.initSegmentUrl)
    }

    @Test
    fun `没有MAP但分片后缀是m4s也判为fMP4`() {
        assertTrue(parseMedia("#EXTINF:4.000,\nseg0.m4s").isFmp4)
    }

    @Test
    fun `带查询参数的ts分片不会被误判为fMP4`() {
        assertFalse(parseMedia("#EXTINF:4.000,\nseg0.ts?v=1.mp4x").isFmp4)
    }

    // endregion
}
