package com.peihua.touchmonitor.data.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.peihua.touchmonitor.utils.ensureDirExist
import com.peihua.touchmonitor.utils.isAtLeastQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 把私有目录里的成品导出到相册。
 *
 * 默认只写 `getExternalFilesDir`（零权限、卸载即清），导出做成用户显式动作 ——
 * 下载器不该默默往用户相册里塞文件。
 */
object GalleryExporter {

    private val RELATIVE_DIR = "${Environment.DIRECTORY_MOVIES}/M3u8Downloader"

    suspend fun export(context: Context, source: File): Uri = withContext(Dispatchers.IO) {
        require(source.isFile && source.length() > 0L) { "源文件不存在或为空" }
        if (isAtLeastQ) exportViaMediaStore(context, source) else exportViaPublicDir(context, source)
    }

    /** API 29+ 零权限：先 IS_PENDING=1 占位，拷完再置 0，避免其他 App 看到半截文件 */
    private fun exportViaMediaStore(context: Context, source: File): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, source.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, RELATIVE_DIR)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("无法在相册中创建文件")
        try {
            resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } }
                ?: error("无法写入相册")
        } catch (e: Throwable) {
            resolver.delete(uri, null, null)
            throw e
        }
        resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        return uri
    }

    /** API 24-28 没有 RELATIVE_PATH，只能自己拷到公共目录再登记 DATA */
    private fun exportViaPublicDir(context: Context, source: File): Uri {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "M3u8Downloader",
        ).also { it.ensureDirExist }
        val target = File(dir, source.name)
        source.inputStream().use { input -> target.outputStream().use { input.copyTo(it) } }
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, target.name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.DATA, target.absolutePath)
        }
        return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("无法在相册中登记文件")
    }
}
