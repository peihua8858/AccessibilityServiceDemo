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
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.math.max

suspend fun InputStream?.writeToFile(
    file: File?,
    bufferSize: Int = 4096,
    callback: (progress: Int) -> Unit = {},
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
                    while (fis.read(buffer).also {
                            length = it
                            callback(length)
                        } > 0 && isActive) {
                        fos.write(buffer, 0, length)
                    }
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