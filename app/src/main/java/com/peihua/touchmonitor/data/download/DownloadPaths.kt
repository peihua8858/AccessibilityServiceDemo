package com.peihua.touchmonitor.data.download

import android.content.Context
import android.os.Environment
import com.peihua.touchmonitor.utils.ensureDirExist
import java.io.File

/**
 * 下载相关的目录规则。
 *
 * 临时分片绝不能放 `cacheDir` —— 系统在存储紧张时会清理，正在下载的分片会凭空消失。
 * `getExternalFilesDir` 零权限、容量大、不会被系统清理，且 `res/xml/file_paths.xml`
 * 已声明 `external-files-path`，产物可以直接走 FileProvider 分享/播放。
 */
class DownloadPaths(private val app: Context) {

    private val root: File
        get() = app.getExternalFilesDir(null) ?: app.filesDir

    fun tmpDir(taskId: Long): File = File(root, "$TMP_PREFIX/$taskId").also { it.ensureDirExist }

    fun keyDir(taskId: Long): File = File(tmpDir(taskId), "keys").also { it.ensureDirExist }

    /** ffmpeg 输出必须是真实文件路径，不能是 content:// */
    fun outputDir(): File =
        File(app.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: root, "m3u8")
            .also { it.ensureDirExist }

    /** 同名时追加 `-1`、`-2`，避免静默覆盖用户上次下载的视频 */
    fun uniqueOutputFile(rawName: String): File {
        val name = rawName.sanitizeFileName().ifBlank { "video_${System.currentTimeMillis()}" }
        val dir = outputDir()
        var candidate = File(dir, "$name.mp4")
        var index = 1
        while (candidate.exists()) {
            candidate = File(dir, "$name-$index.mp4")
            index++
        }
        return candidate
    }

    fun clearTmp(taskId: Long) {
        File(root, "$TMP_PREFIX/$taskId").deleteRecursively()
    }

    private companion object {
        const val TMP_PREFIX = "m3u8/tmp"
        val ILLEGAL_FILE_NAME_CHARS = Regex("""[\\/:*?"<>|\r\n\t]""")
    }

    private fun String.sanitizeFileName(): String =
        replace(ILLEGAL_FILE_NAME_CHARS, "_").trim().take(120)
}
