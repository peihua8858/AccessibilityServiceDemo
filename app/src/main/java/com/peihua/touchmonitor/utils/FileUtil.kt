@file:JvmName("FileUtil")
@file:JvmMultifileClass

package com.peihua.touchmonitor.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Looper
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.math.max

suspend fun InputStream?.writeToFile(
    file: File?,
    bufferSize: Int = 4096,
    callback: (progress: Int, isComplete: Boolean) -> Unit = { process, isComplete -> },
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
    val context: CoroutineContext = if (Looper.myLooper() == Looper.getMainLooper()) {
        Dispatchers.IO
    } else {
        coroutineContext
    }
    return withContext(context) {
        try {
            return@withContext FileOutputStream(file).use { fos ->
                return@use this@writeToFile.use { fis ->
                    val buffer = ByteArray(bufferSize)
                    var length: Int
                    val total = fis.available()
                    var progress = 0
                    while (fis.read(buffer).also {
                            length = it
                            progress += length
                            callback(length, progress == total)
                        } > 0 && isActive) {
                        fos.write(buffer, 0, length)
                    }
                    callback(length, true)
                    dLog { "writeToFile, save file  to $file successful" }
                    fos.flush()
                    true
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            dLog { "writeToFile, save file  to $file failed" }
            return@withContext false
        }
    }
}

suspend fun InputStream?.writeToFile(
    os: OutputStream?,
    bufferSize: Int = 4096,
    isCloseOs: Boolean = true,
    callback: (progress: Long,speed:Long) -> Unit = { process,speed -> },
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
    parent: String, zos: ZipOutputStream, zipLevel: Int,
    callback: (progress: Long) -> Unit = { process -> },
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
                    f.writeToZip(parentTemp, zos, zipLevel)
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
        writeToZip(parentTemp, zos, zipLevel, callback)
    }
}

suspend fun File.writeToZip(
    parent: String,
    zos: ZipOutputStream,
    zipLevel: Int,
    callback: (progress: Long,speed:Long) -> Unit = { process,speed -> },
): Boolean {
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
        return fis.writeToZip(zos, callback = callback)
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

suspend fun InputStream?.writeToZip(
    zos: ZipOutputStream,
    bufferSize: Int = 4096,
    isCloseZip: Boolean = true,
    callback: (progress: Long,speed:Long) -> Unit = { process,speed -> },
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
    bufferSize: Int = 4096,
    callback: (progress: Long,speed:Long) -> Unit = { process,speed -> },
): Boolean {
    try {
        val context: CoroutineContext = if (Looper.myLooper() == Looper.getMainLooper()) {
            Dispatchers.IO
        } else {
            coroutineContext
        }
        val fis = this
        return withContext(context) {
            val buffer = ByteArray(bufferSize)
            var length: Int
            var progress = 0L
            while ((fis.read(buffer).also { length = it }) != -1 && isActive) {
                ios.write(buffer, 0, length)
                progress += length.toLong()
                callback(progress,length.toLong())
            }
            callback(progress,length.toLong())
            ios.flush()
            dLog { "writeToFile, save file  to $ios successful" }
            true
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        dLog { "writeToFile, save file  to $ios failed,e:${e.message}" }
        return false
    }
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

fun Bitmap.adjustBitmapOrientation(filePath: String): Bitmap? {
    var exifInterface: ExifInterface? = null
    try {
        exifInterface = ExifInterface(filePath)
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
    if (rotation == 0) {
        return this
    }
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat())
    return Bitmap.createBitmap(
        this,
        0,
        0,
        getWidth(),
        getHeight(),
        matrix,
        true
    )
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