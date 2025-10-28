//@file:JvmName("FileUtil")
//@file:JvmMultifileClass
//
//package com.peihua.touchmonitor.utils
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Matrix
//import android.webkit.MimeTypeMap
//import androidx.exifinterface.media.ExifInterface
//import com.peihua.compose.utils.dLog
//import com.peihua.compose.utils.eLog
//import com.peihua.selector.crop.util.BitmapLoadUtils
//import kotlinx.coroutines.suspendCancellableCoroutine
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.nio.ByteBuffer
//import java.util.zip.CRC32
//import java.util.zip.ZipEntry
//import java.util.zip.ZipOutputStream
//import kotlin.coroutines.resume
//import kotlin.math.max
//
//
//fun File.decodeFileToBitmap(screenWidth: Int, screenHeight: Int): Bitmap? {
//    try {
//        val mScreenWidth = screenWidth
//        val mScreenHeight = screenHeight
//        val o = BitmapFactory.Options()
//        o.inJustDecodeBounds = true
//        BitmapFactory.decodeStream(FileInputStream(this), null, o)
//        dLog { "decodeFileToBitmap, o: $o" }
//        val width_tmp = o.outWidth
//        val height_tmp = o.outHeight
//        var scale = 1
//        if (width_tmp <= mScreenWidth && height_tmp <= mScreenHeight) {
//            scale = 1
//        } else {
//            val widthFit: Double = width_tmp * 1.0 / mScreenWidth
//            val heightFit: Double = height_tmp * 1.0 / mScreenHeight
//            val fit = max(widthFit, heightFit)
//            scale = (fit + 0.5).toInt()
//        }
//        dLog { "decodeFileToBitmap, scale: $scale,width_tmp:$width_tmp,height_tmp:$height_tmp" }
//        var bitmap: Bitmap? = null
//        if (scale == 1) {
//            bitmap = BitmapFactory.decodeStream(FileInputStream(this))
//        } else {
//            val o2 = BitmapFactory.Options()
//            o2.inSampleSize = scale
//            bitmap = BitmapFactory.decodeStream(FileInputStream(this), null, o2)
//        }
//        if (bitmap != null) {
//            eLog { "scale = " + scale + " bitmap.size = " + (bitmap.getRowBytes() * bitmap.getHeight()) }
//        }
//        dLog { "decodeFileToBitmap, bitmap: $bitmap" }
//        return bitmap
//    } catch (e: Throwable) {
//        eLog { "fileNotFoundException, e: $e" }
//    }
//    return null
//}
//
//
//fun File.adjustBitmapOrientation(): Bitmap? {
//    return try {
//        var bitmap = BitmapFactory.decodeStream(FileInputStream(this))
//        adjustBitmapOrientation(bitmap)
//    } catch (e: Throwable) {
//        e.printStackTrace()
//        null
//    }
//}
//
//fun File.adjustBitmapOrientation(decodeBitmap: Bitmap): Bitmap? {
//    return try {
//        val matrix = orientationMatrix
//        dLog { "adjustBitmapOrientation, adjust degree " + matrix + "to 0." }
//        BitmapLoadUtils.transformBitmap(decodeBitmap, matrix)
//    } catch (e: Throwable) {
//        e.printStackTrace()
//        null
//    }
//}
//val File.orientationMatrix: Matrix
//    get() {
//        val matrix = Matrix()
//        try {
//            val exif = ExifInterface(this)
//            val orientation =
//                exif.getAttributeInt(
//                    ExifInterface.TAG_ORIENTATION,
//                    ExifInterface.ORIENTATION_NORMAL
//                )
//
//            when (orientation) {
//                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
//                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
//                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
//                    matrix.setRotate(180f)
//                    matrix.postScale(-1f, 1f)
//                }
//
//                ExifInterface.ORIENTATION_TRANSPOSE -> {
//                    matrix.setRotate(90f)
//                    matrix.postScale(-1f, 1f)
//                }
//
//                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
//                ExifInterface.ORIENTATION_TRANSVERSE -> {
//                    matrix.setRotate(-90f)
//                    matrix.postScale(-1f, 1f)
//                }
//
//                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return matrix
//    }
//
///**
// * 获取一个文件的CRC32值
// */
//@get:Throws(java.lang.Exception::class)
//val File.cRC32: CRC32
//    get() = FileInputStream(this).cRC32
//
//@get:Throws(java.lang.Exception::class)
//val InputStream.cRC32: CRC32
//    get() {
//        this.use {
//            val crc = CRC32()
//            val bytes = ByteArray(1024)
//            var length: Int
//            while ((read(bytes).also { length = it }) != -1) {
//                crc.update(bytes, 0, length)
//            }
//            return crc
//        }
//    }
//val File.mimeTypeFromFilePath: String?
//    get() {
//        return name.mimeTypeFromFilePath
//    }
//
//val String.mimeTypeFromFilePath: String?
//    get() {
//        val extension = substringAfterLast('.', "")
//        dLog { "openWithFile>>>>extension：$extension" }
//        return MimeTypeMap.getSingleton()
//            .getMimeTypeFromExtension(extension)
//    }