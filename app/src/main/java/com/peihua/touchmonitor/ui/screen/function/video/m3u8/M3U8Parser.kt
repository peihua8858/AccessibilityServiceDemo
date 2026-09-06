package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import com.peihua.touchmonitor.utils.HttpClientUtil
import com.peihua.touchmonitor.utils.addUrlSchemePrefixIfNeed
import com.peihua.touchmonitor.utils.baseUrl
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import io.lindstrom.m3u8.model.MasterPlaylist
import io.lindstrom.m3u8.model.MediaPlaylist
import io.lindstrom.m3u8.model.Variant
import io.lindstrom.m3u8.parser.MasterPlaylistParser
import io.lindstrom.m3u8.parser.MediaPlaylistParser
import io.lindstrom.m3u8.parser.PlaylistParserException
import java.io.IOException
import java.io.InputStream
import java.util.function.Consumer

/**
 * M3U8 索引文件解析工具类
 *
 * @author cloudgyb
 * @since 2.0.0
 */
class M3U8Parser {

    fun playlistParse(url: String): MutableList<MediaSegment> {
        var masterPlaylist: MutableList<String>
        val mediaSegments: MutableList<MediaSegment> = ArrayList()
        try {
            dLog { "开始尝试解析${url}为主播放列表" }
            masterPlaylist = masterPlaylistUrlParse(url)
            dLog { "尝试解析${url}为主播放列表完成，包含${masterPlaylist.size}个播放列表！" }
        } catch (e: PlaylistParserException) {
            dLog { "尝试解析${url}为主播放列表失败! ${e.stackTraceToString()}" }
            // 作为媒体播放列表进行处理
            masterPlaylist = mutableListOf(url)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        dLog { "开始尝试解析为媒体播放列表..." }
        for (mpl in masterPlaylist) {
            try {
                dLog { "开始尝试解析${mpl}为媒体播放列表..." }
                val mediaPlaylistUrls = mediaPlaylistParse(mpl)
                mediaSegments.addAll(mediaPlaylistUrls)
            } catch (e: PlaylistParserException) {
                eLog { "尝试解析${mpl}为媒体播放列表失败! ${e.stackTraceToString()}" }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        dLog { "解析为媒体播放列表，一共${mediaSegments.size}个媒体片段!" }
        return mediaSegments
    }

    @Suppress("unused")
    fun playlistUrlParse(url: String): MutableList<String?> {
        var masterPlaylist: MutableList<String>
        val mediaPlaylist: MutableList<String?> = ArrayList()
        try {
            dLog { "开始尝试解析${url}为主播放列表" }
            masterPlaylist = masterPlaylistUrlParse(url)
            dLog { "尝试解析${url}为主播放列表完成，包含${masterPlaylist.size}个播放列表！" }
        } catch (e: PlaylistParserException) {
            eLog { "尝试解析${url}为主播放列表失败！${e.stackTraceToString()}" }
            // 作为媒体播放列表进行处理
            masterPlaylist = mutableListOf(url)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        dLog { "开始尝试解析为媒体播放列表..." }
        for (mpl in masterPlaylist) {
            try {
                dLog { "开始尝试解析${mpl}为媒体播放列表..." }
                val mediaPlaylistUrls = mediaPlaylistUrlParse(mpl)
                mediaPlaylist.addAll(mediaPlaylistUrls)
            } catch (e: PlaylistParserException) {
                eLog { "尝试解析${mpl}为媒体播放列表失败！${e.stackTraceToString()}" }
            } catch (e: IOException) {
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
        dLog { "解析为媒体播放列表，一共${mediaPlaylist.size}个媒体片段！" }
        return mediaPlaylist
    }

    @Throws(IOException::class, InterruptedException::class)
    fun masterPlaylistUrlParse(url: String): MutableList<String> {
        val baseUrl = url.baseUrl
        val parser = MasterPlaylistParser()
        val inputStream: InputStream? = HttpClientUtil.getAsInputStream(url)
        var masterPlaylistUrls: MutableList<String>? = null
        // Parse playlist
        val playlist: MasterPlaylist = parser.readPlaylist(inputStream)
        val variants: MutableList<Variant>? = playlist.variants()
        if (variants != null) {
            masterPlaylistUrls = variants.map { it.uri() }.map { u -> u.addUrlSchemePrefixIfNeed(baseUrl) }
                .toMutableList()
        }
        return masterPlaylistUrls!!
    }

    @Throws(IOException::class, InterruptedException::class)
    fun mediaPlaylistUrlParse(url: String): MutableList<String?> {
        val baseUrl = url.baseUrl
        val parser = MediaPlaylistParser()
        val inputStream: InputStream? = HttpClientUtil.getAsInputStream(url)
        var mediaPlaylistUrls: MutableList<String?>? = null
        // Parse playlist
        val playlist: MediaPlaylist = parser.readPlaylist(inputStream)
        val s: String? = parser.writePlaylistAsString(playlist)
        println(s)
        val mediaSegments: MutableList<io.lindstrom.m3u8.model.MediaSegment>? = playlist.mediaSegments()
        if (mediaSegments != null) {
            mediaPlaylistUrls = mediaSegments.map{ obj -> obj.uri() }
                .map { u -> u.addUrlSchemePrefixIfNeed(baseUrl) }.toMutableList()
        }
        return mediaPlaylistUrls!!
    }

    @Throws(IOException::class, InterruptedException::class)
    fun mediaPlaylistParse(url: String): MutableList<MediaSegment> {
        val baseUrl: String = url.baseUrl
        val parser = MediaPlaylistParser()
        val inputStream: InputStream? = HttpClientUtil.getAsInputStream(url)
        // Parse playlist
        val playlist: MediaPlaylist = parser.readPlaylist(inputStream)
        val rawMediaSegments: MutableList<io.lindstrom.m3u8.model.MediaSegment>? = playlist.mediaSegments()
        val mediaSegments = ArrayList<MediaSegment>()
        rawMediaSegments?.forEach(Consumer { ms ->
            val uri = ms.uri()
            val duration = ms.duration()
            val fullUrl: String = uri.addUrlSchemePrefixIfNeed(baseUrl)
            val mediaSegment = MediaSegment(fullUrl, duration)
            mediaSegments.add(mediaSegment)
        })
        return mediaSegments
    }
}

data class MediaSegment(val url: String, val duration: Double)
