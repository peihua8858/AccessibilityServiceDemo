@file:JvmName("FileUtil")
@file:JvmMultifileClass

package com.peihua.touchmonitor.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.math.max

suspend fun InputStream?.writeToFile(
    file: File?,
    bufferSize: Int = 4096,
    isCloseOs: Boolean = true,
    callback: (progress: Long, speed: Long) -> Unit = { process, isComplete -> },
): Boolean {
    val parentFile = file?.parentFile
    if (file == null || this == null || parentFile == null) {
        return false
    }
    if (file.exists()) {
        file.delete()
    }
    if (parentFile.exists().not()) {
        parentFile.mkdirs()
    }
    val os = FileOutputStream(file)
    return writeToFile(os, bufferSize, isCloseOs, callback)
}

suspend fun InputStream?.writeToFile(
    os: OutputStream?,
    bufferSize: Int = 4096,
    isCloseOs: Boolean = true,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
): Boolean {
    if (os == null || this == null) {
        return false
    }
    return use {
        if (isCloseOs) {
            os.use {
                writeToFileNoClose(os, bufferSize, callback)
            }
        } else {
            writeToFileNoClose(os, bufferSize, callback)
        }
    }
}

suspend fun File?.writeToZip(
    parent: String,
    zos: ZipOutputStream,
    bufferSize: Int = 4096,
    zipLevel: Int = 0,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
) {
    if (this == null) {
        return
    }
    var parentTemp = parent
    if (isDirectory) {
        parentTemp += this.getName() + File.separator
        val fileItemList = this.listFiles()
        if (fileItemList != null) {
            if (fileItemList.size > 0) {
                for (f in fileItemList) {
                    f.writeToZip(parentTemp, zos, bufferSize, zipLevel, callback)
                }
            } else {
                try {
                    zos.putNextEntry(ZipEntry(parentTemp))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    } else if (isFile) {
        try {
            val zipEntry = ZipEntry(parent + getName())
            val totalLength = length()
            if (zipLevel == 0) {
                zipEntry.setMethod(ZipOutputStream.STORED)
                zipEntry.setCompressedSize(totalLength)
                zipEntry.setSize(totalLength)
                zipEntry.setCrc(this.cRC32.value)
            }
            zos.putNextEntry(zipEntry)
            val fis = inputStream()
            fis.writeToZip(zos, bufferSize, callback = callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

suspend fun InputStream?.writeToZip(
    zos: ZipOutputStream,
    bufferSize: Int = 4096,
    isCloseZip: Boolean = true,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
): Boolean {
    if (this == null) {
        return false
    }
    return this.use { fins ->
        if (isCloseZip) {
            zos.use { zois ->
                fins.writeToFileNoClose(zois, bufferSize, callback)
            }
        } else {
            writeToFileNoClose(zos, bufferSize, callback)
        }
    }
}

/**
 * InputStream 写入 OutputStream,且不做关闭处理，由外部自行关闭
 */
suspend fun InputStream.writeToFileNoClose(
    ios: OutputStream,
    bufferSize: Int = 1024 * 8,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
): Boolean {
    return try {
        suspendCancellableCoroutine { continuation ->
            val buffer = ByteArray(bufferSize)
            var length: Int
            var progress = 0L
            val totalLength = available().toLong()
            while ((this.read(buffer).also { length = it }) != -1 && continuation.isActive) {
                ios.write(buffer, 0, length)
                progress += length.toLong()
                callback(progress, length.toLong())
                dLog { "writeToFile, save file   progress:${progress.formatFileSize()},totalLength:${totalLength.formatFileSize()} length:${length}" }
            }
            callback(progress, length.toLong())
            ios.flush()
            dLog { "writeToFile, save file  to $ios successful" }
            continuation.resume(true)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        dLog { "writeToFile, save file  to $ios failed,e:${e.message}" }
        false
    }
}

suspend fun File.copyToFile(
    destinationFile: File,
    bufferSize: Int = 1024 * 20,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
) {
    FileInputStream(this).copyToFile(FileOutputStream(destinationFile), bufferSize, callback)
}

suspend fun FileInputStream.copyToFile(
    fos: FileOutputStream,
    bufferSize: Int = 1024 * 20,
    callback: (progress: Long, speed: Long) -> Unit = { process, speed -> },
): Boolean {
    return try {
        suspendCancellableCoroutine { continuation ->
            this.use { inputStream ->
                fos.use { outputStream ->
                    val channelInput = inputStream.channel
                    val channelOutput = outputStream.channel
                    val buffer = ByteBuffer.allocate(bufferSize)
                    var progress = 0L
                    var length: Int
                    while ((channelInput.read(buffer)
                            .also { length = it }) > 0 && continuation.isActive
                    ) {
                        progress += length.toLong()
                        buffer.flip() // 切换到读模式
                        channelOutput.write(buffer)
                        buffer.clear() // 清空缓冲区以供下次使用
                        callback(progress, length.toLong())
                    }
                    callback(progress, length.toLong())
                    fos.flush()
                    continuation.resume(true)
                }
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }
}

fun format(speed: Float): String {
    return String.format(Locale.ENGLISH, "%.2f", speed)
}

fun String.decodePathOptionsFile(screenWidth: Int, screenHeight: Int): Bitmap? {
    try {
        val mScreenWidth = screenWidth
        val mScreenHeight = screenHeight
        val file = File(this)
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(FileInputStream(file), null, o)
        val width_tmp = o.outWidth
        val height_tmp = o.outHeight
        var scale = 1
        if (width_tmp <= mScreenWidth && height_tmp <= mScreenHeight) {
            scale = 1
        } else {
            val widthFit: Double = width_tmp * 1.0 / mScreenWidth
            val heightFit: Double = height_tmp * 1.0 / mScreenHeight
            val fit = max(widthFit, heightFit)
            scale = (fit + 0.5).toInt()
        }
        var bitmap: Bitmap? = null
        if (scale == 1) {
            bitmap = BitmapFactory.decodeStream(FileInputStream(file))
        } else {
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            bitmap = BitmapFactory.decodeStream(FileInputStream(file), null, o2)
        }
        if (bitmap != null) {
            eLog { "scale = " + scale + " bitmap.size = " + (bitmap.getRowBytes() * bitmap.getHeight()) }
        }
        return bitmap
    } catch (e: Throwable) {
        eLog { "fileNotFoundException, e: $e" }
    }
    return null
}

suspend fun String.adjustBitmapOrientation(): Bitmap? {
    return try {
        suspendCancellableCoroutine<Bitmap> { continuation ->
            var exifInterface: ExifInterface? = null
            var bitmap = BitmapFactory.decodeFile(this)
            try {
                exifInterface = ExifInterface(this)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            var rotation = 0
            if (exifInterface != null) {
                val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
                    else -> {}
                }
            }
            dLog { "adjustBitmapOrientation, adjust degree " + rotation + "to 0." }
            bitmap = if (rotation == 0) {
                bitmap
            } else {
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
                )
            }
            continuation.resume(bitmap)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

}


/**
 * 获取一个文件的CRC32值
 */
@get:Throws(java.lang.Exception::class)
val File.cRC32: CRC32
    get() = FileInputStream(this).cRC32

@get:Throws(java.lang.Exception::class)
val InputStream.cRC32: CRC32
    get() {
        this.use {
            val crc = CRC32()
            val bytes = ByteArray(1024)
            var length: Int
            while ((read(bytes).also { length = it }) != -1) {
                crc.update(bytes, 0, length)
            }
            return crc
        }
    }