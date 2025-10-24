package com.peihua.touchmonitor.utils

import android.graphics.Bitmap
import androidx.core.net.toUri
import com.fz.common.file.cacheFile
import com.fz.common.file.createFileName
import com.fz.common.file.isFile
import com.peihua.touchmonitor.ServiceApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import kotlin.coroutines.resume

private val sf = SimpleDateFormat("yyyy-MM-dd")

/**
 * 根据时间戳创建文件名
 *
 * @return
 */
fun String.createFolderFileName(): String {
    val millis = System.currentTimeMillis()
    return this + sf.format(millis)
}

fun String.createFolderFile(): File {
    val fileCache = createFolderFileName()
    val parentPath = ServiceApplication.application.cacheFile("files")
    return File(parentPath, fileCache)
}

fun String.createFile(extension: String): File {
    val fileCache = createFileName(extension)
    val parentPath = ServiceApplication.application.cacheFile("files")
    return File(parentPath, fileCache)
}


val String.isContentUri get() = this.startsWith("content://")
fun String.decodePathOptionsFile(screenWidth: Int, screenHeight: Int): Bitmap? {
    if (this.isFile()) {
        return this.fileProvider.decodePathOptionsFile(screenWidth, screenHeight)
    }
    if (this.isContentUri) {
        return this.toUri().decodePathOptionsFile(screenWidth, screenHeight)
    }
    return null
}


suspend fun String.adjustBitmapOrientationAsync(): Bitmap? {
    return try {
        suspendCancellableCoroutine<Bitmap?> { continuation ->
            continuation.resume(this.adjustBitmapOrientation())
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun String.adjustBitmapOrientation(): Bitmap? {
    return File(this).adjustBitmapOrientation()

}