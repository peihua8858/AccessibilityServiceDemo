package com.peihua.touchmonitor.utils

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFprobeKit
import com.peihua.touchmonitor.data.download.M3u8Exception
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.abs

/**
 * ffmpeg 封装。只做一件事：把有序的明文 TS 分片 concat 成 mp4。
 */
object FfmpegUtil {

    /**
     * @param sourceFiles 已按媒体序号排好序的分片绝对路径
     * @param onProgress 已合并的媒体时间，单位毫秒
     */
    suspend fun mergeTs(
        sourceFiles: List<String>,
        targetFile: File,
        workDir: File,
        onProgress: (Long) -> Unit = {},
    ) {
        if (sourceFiles.isEmpty()) {
            throw M3u8Exception.MergeFailed("没有可合并的分片")
        }
        // 0 字节分片不能静默跳过：那正是"合并成功但视频少一段"的来源
        sourceFiles.forEach { path ->
            val f = File(path)
            if (!f.isFile || f.length() == 0L) {
                throw M3u8Exception.MergeFailed("分片文件缺失或为空，无法合并：$path")
            }
        }

        // 清单放 workDir 而不是 createTempFile：Android 上 java.io.tmpdir 在部分设备
        // 指向不可写的 /data/local/tmp
        val listFile = File(workDir, CONCAT_LIST_NAME)
        listFile.writeText(sourceFiles.joinToString("\n") { "file '${it.escapeForConcat()}'" })

        targetFile.parentFile?.let { it.ensureDirExist }

        try {
            // AAC 在 TS 里是 ADTS 封装，-c copy 进 MP4 必须转 ASC，否则报错或无声。
            // 但流里没有 AAC 时这个 bsf 自身会报错，所以失败后不带它重试一次。
            val first = runFfmpeg(buildArgs(listFile, targetFile, adtsToAsc = true), onProgress)
            if (!first.returnCode.isValueSuccess) {
                eLog { "合并失败（code=${first.returnCode}），去掉 aac_adtstoasc 重试" }
                val second = runFfmpeg(buildArgs(listFile, targetFile, adtsToAsc = false), onProgress)
                if (!second.returnCode.isValueSuccess) {
                    throw M3u8Exception.MergeFailed(
                        "ffmpeg 合并失败（code=${second.returnCode}）",
                        RuntimeException(second.allLogsAsString.takeLast(MAX_LOG_CHARS)),
                    )
                }
            }
        } finally {
            listFile.delete()
        }

        if (!targetFile.isFile || targetFile.length() == 0L) {
            throw M3u8Exception.MergeFailed("ffmpeg 返回成功但没有产出文件")
        }
        dLog { "合并完成：${targetFile.absolutePath}（${targetFile.length().toHumanReadableBytes()}）" }
    }

    /**
     * fMP4 合并：init segment（ftyp + moov）+ 所有媒体分片（moof + mdat）
     * 按序二进制拼接即为合法 ISO BMFF 文件，再用 ffmpeg remux 加 faststart。
     */
    suspend fun mergeFmp4(
        initSegment: File,
        sourceFiles: List<String>,
        targetFile: File,
        workDir: File,
        onProgress: (Long) -> Unit = {},
    ) {
        if (!initSegment.isFile || initSegment.length() == 0L) {
            throw M3u8Exception.MergeFailed("fMP4 init segment 缺失或为空")
        }
        if (sourceFiles.isEmpty()) {
            throw M3u8Exception.MergeFailed("没有可合并的分片")
        }
        sourceFiles.forEach { path ->
            val f = File(path)
            if (!f.isFile || f.length() == 0L) {
                throw M3u8Exception.MergeFailed("分片文件缺失或为空，无法合并：$path")
            }
        }

        targetFile.parentFile?.let { it.ensureDirExist }

        val combined = File(workDir, "combined_fmp4.mp4")
        try {
            combined.outputStream().buffered(64 * 1024).use { out ->
                initSegment.inputStream().use { it.copyTo(out) }
                sourceFiles.forEach { path ->
                    File(path).inputStream().use { it.copyTo(out) }
                }
            }
            dLog { "fMP4 拼接完成：${combined.length().toHumanReadableBytes()}，开始 remux" }

            val args = arrayOf(
                "-y",
                "-i", combined.absolutePath,
                "-c", "copy",
                "-movflags", "+faststart",
                targetFile.absolutePath,
            )
            val session = runFfmpeg(args, onProgress)
            if (!session.returnCode.isValueSuccess) {
                throw M3u8Exception.MergeFailed(
                    "fMP4 remux 失败（code=${session.returnCode}）",
                    RuntimeException(session.allLogsAsString.takeLast(MAX_LOG_CHARS)),
                )
            }
        } finally {
            combined.delete()
        }

        if (!targetFile.isFile || targetFile.length() == 0L) {
            throw M3u8Exception.MergeFailed("fMP4 remux 返回成功但没有产出文件")
        }
        dLog { "fMP4 合并完成：${targetFile.absolutePath}（${targetFile.length().toHumanReadableBytes()}）" }
    }

    /**
     * 用产出文件的实际时长与分片时长之和交叉校验，能抓住"concat 成功但只拼出几秒"。
     *
     * @return 时长毫秒，探测失败返回 null（探测失败本身不该让任务失败）
     */
    suspend fun probeDurationMs(file: File): Long? = withContext(Dispatchers.IO) {
        runCatching {
            val session = FFprobeKit.getMediaInformation(file.absolutePath)
            session.mediaInformation?.duration?.toDoubleOrNull()?.times(1000)?.toLong()
        }.getOrNull()
    }

    /** 偏差是否在容忍范围内。[expectedMs] <= 0 时无从比较，直接放过。 */
    fun durationWithinTolerance(actualMs: Long, expectedMs: Long): Boolean {
        if (expectedMs <= 0L || actualMs <= 0L) return true
        return abs(actualMs - expectedMs).toDouble() / expectedMs <= DURATION_TOLERANCE
    }

    private fun buildArgs(listFile: File, target: File, adtsToAsc: Boolean): Array<String> =
        buildList {
            // -y 必须有：否则输出文件已存在时 ffmpeg 会等交互式确认，在无 stdin 环境里永久挂住
            add("-y")
            add("-f"); add("concat")
            add("-safe"); add("0")
            add("-i"); add(listFile.absolutePath)
            add("-c"); add("copy")
            if (adtsToAsc) {
                add("-bsf:a"); add("aac_adtstoasc")
            }
            add("-movflags"); add("+faststart")
            add(target.absolutePath)
        }.toTypedArray()

    /**
     * 必须用 `executeWithArgumentsAsync` 而不是 `execute(cmd)`：后者把 argv 拼成字符串再按
     * 空白切分，路径含空格就散架；而且阻塞版对协程取消完全无效。
     */
    private suspend fun runFfmpeg(
        args: Array<String>,
        onProgress: (Long) -> Unit,
    ): FFmpegSession = suspendCancellableCoroutine { cont ->
        // sessionId 只能在 async 调用返回后拿到，先注册取消回调再读 holder，缩小竞态窗口
        val holder = arrayOfNulls<FFmpegSession>(1)
        cont.invokeOnCancellation {
            holder[0]?.let { FFmpegKit.cancel(it.sessionId) }
        }
        dLog { "ffmpeg ${args.joinToString(" ")}" }
        val session = FFmpegKit.executeWithArgumentsAsync(
            args,
            { completed -> if (cont.isActive) cont.resume(completed) },
            null,
            { stats -> onProgress(stats.time.toLong()) },
        )
        holder[0] = session
        if (cont.isCancelled) FFmpegKit.cancel(session.sessionId)
    }

    /** concat demuxer 的 `file '...'` 语法里单引号要转义成 `'\''` */
    private fun String.escapeForConcat(): String = replace("'", "'\\''")

    private const val CONCAT_LIST_NAME = "concat_list.txt"
    private const val MAX_LOG_CHARS = 4000
    private const val DURATION_TOLERANCE = 0.05
}
