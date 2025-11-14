package com.peihua.touchmonitor.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Size
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.isNonEmpty
import com.peihua.touchmonitor.ServiceApplication
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

/**
 * 传入的file须为主存储下的文件，且对file有完整的读写权限
 */
fun Context.getUriForFileByFileProvider(file: File): Uri? {
    return FileProvider.getUriForFile(this, "$packageName.fileProvider", file)
}

/**
 * 传入的file须为主存储下的文件，且对file有完整的读写权限
 */
val File.fileProvider: Uri
    get() {
        val context = ServiceApplication.application
        return FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", this)
    }

/**
 * 传入的file须为主存储下的文件，且对file有完整的读写权限
 */
val String.fileProvider: Uri
    get() {
        return File(this).fileProvider
    }

/**
 * 传入的file须为主存储下的文件，且对file有完整的读写权限
 */
val Uri.fileProvider: Uri
    get() {
        val context = ServiceApplication.application
        return this.buildUpon().authority("${context.packageName}.fileProvider").build()
    }

/**
 * 根据uri获取文件
 * @author dingpeihua
 * @date 2021/1/28 9:35
 * @version 1.0
 */
fun Context.getFieldFromUri(uri: String?, columnName: String): String? {
    return uri?.let {
        return getFieldFromUri(it.toUri(), columnName)
    }
}

/**
 * 根据uri获取文件
 * @author dingpeihua
 * @date 2021/1/28 9:35
 * @version 1.0
 */
fun Context.getFieldFromUri(uri: Uri?, columnName: String): String? {
    return if (uri == null) {
        null
    } else when (uri.scheme) {
        "content" -> getFieldFromContentUri(uri, columnName)
        "file" -> uri.path?.let {
            File(it).path
        }

        null -> {
            val file = File(uri.toString())
            if (file.exists()) {
                file.name
            } else null
        }

        else -> null
    }
}

/**
 * 通过内容解析中查询uri中的文件路径
 */
fun Context.getFieldFromContentUri(contentUri: Uri?, columnName: String): String? {
    val contentResolver = contentResolver ?: return null
    return contentResolver.getFieldFromContentUri(contentUri, columnName)
}

/**
 * 通过内容解析中查询uri中的文件路径
 */
fun ContentResolver.getFieldFromContentUri(contentUri: Uri?, columnName: String): String? {
    return contentUri?.let { uri ->
        val column = arrayOf(columnName)
        val sel: String
        val cursor = try {
            val wholeID = DocumentsContract.getDocumentId(uri)
            val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            // where id is equal to
            sel = MediaStore.Images.Media._ID + "=?"
            query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, arrayOf(id), null
            )
        } catch (e: Throwable) {
            query(
                uri, column, null,
                null, null
            )
        }
        return cursor?.use {
            val columnIndex = cursor.getColumnIndex(column[0])
            cursor.moveToFirst()
            val result = cursor.getString(columnIndex)
            if (result.isNonEmpty()) {
                return result
            }
            null
        }
    }
}


fun Context.getFileFromUri(uri: Uri?): File? {
    return if (uri == null) {
        null
    } else when (uri.scheme) {
        "content" -> getFileFromContentUri(uri)
        "file" -> uri.path?.let {
            File(it)
        }

        null -> {
            val file = File(uri.toString())
            if (file.exists()) {
                file
            } else null
        }

        else -> null
    }
}

fun Context.getFileFromContentUri(contentUri: Uri?): File? {
    val contentResolver = contentResolver ?: return null
    return contentResolver.getFileFromContentUri(contentUri)
}

fun ContentResolver.getFileFromContentUri(contentUri: Uri?): File? {
    return contentUri?.let { uri ->
        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel: String
        val cursor = try {
            val wholeID = DocumentsContract.getDocumentId(uri)
            val id =
                wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            // where id is equal to
            sel = MediaStore.Images.Media._ID + "=?"
            query(
                MediaStore.Files.getContentUri(sel),
                column, sel, arrayOf(id), null
            )
        } catch (e: Throwable) {
            query(
                uri, column, null,
                null, null
            )
        }
        return cursor?.use {
            try {
                val columnNames = cursor.columnNames
                cursor.moveToFirst()
                dLog { " getRealPathFromURI>>>>>>cursor.columnNames:${columnNames.contentToString()}" }
//                val columnIndex = cursor.getString(column[0])
                val filePath = cursor.getString(column[0])
                if (filePath.isNonEmpty()) {
                    val file = File(filePath)
                    if (file.exists()) {
                        return file
                    }
                }
                dLog {
                    "getFileFromContentUri>>>>>>filePath :$filePath,columnIndex:${
                        cursor.getColumnIndex(
                            column[0]
                        )
                    }"
                }
                null
            } catch (e: Throwable) {
                e.printStackTrace()
                dLog { "getFileFromContentUri>>>>>>e:${e.message}" }
                null
            }
        }
    }

}


/**
 * 获取图片缩略图
 */
fun Context?.getPictureThumbnail(
    fileId: Long, fileUri: Uri?, size: Size,
): Bitmap? {
    val contentResolver = this?.contentResolver ?: return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileUri ?: return null
            contentResolver.loadThumbnail(fileUri, size, null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                contentResolver, fileId,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    } catch (e: Exception) {
        null
    }
}


fun Context?.getVideoThumbnail(
    fileId: Long, fileUri: Uri?, size: Size,
): Bitmap? {
    val contentResolver = this?.contentResolver ?: return null
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileUri ?: return null
            contentResolver.loadThumbnail(fileUri, size, null)
        } else {
            MediaStore.Video.Thumbnails.getThumbnail(
                contentResolver, fileId,
                MediaStore.Video.Thumbnails.MINI_KIND, null
            )

        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取视频缩略图
 */
fun Context.getVideoThumbnailFromMediaMetadataRetriever(uri: Uri?, size: Size): Bitmap? {
    uri ?: return null
    try {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, uri)
        val thumbnailBytes = mediaMetadataRetriever.embeddedPicture
        if (isAtLeastS) {
            thumbnailBytes?.let {
                return ImageDecoder.decodeBitmap(ImageDecoder.createSource(it));
            }
        }
        val width =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toFloat() ?: size.width.toFloat()
        val height =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toFloat() ?: size.height.toFloat()
        val widthRatio = size.width.toFloat() / width
        val heightRatio = size.height.toFloat() / height
        val ratio = max(widthRatio, heightRatio)
        if (ratio > 1) {
            val requestedWidth = width * ratio
            val requestedHeight = height * ratio
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val frame = mediaMetadataRetriever.getScaledFrameAtTime(
                    -1, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC,
                    requestedWidth.toInt(), requestedHeight.toInt()
                )
                mediaMetadataRetriever.close()
                return frame
            }
        }
        val frame = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.close()
        return frame
    } catch (e: Throwable) {
        e.printStackTrace()
        try {
            return if (isAtLeastQ) {
                contentResolver.loadThumbnail(uri, size, null)
            } else {
                decodeResizedBitmap(uri, size)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }
}

/**
 * 获取缩略图
 */
private fun Context?.decodeResizedBitmap(uri: Uri, size: Size): Bitmap? {
    val contentResolver = this?.contentResolver ?: return null
    val boundsStream = contentResolver.openInputStream(uri)
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(boundsStream, null, options)
    boundsStream?.close()
    if (options.outHeight != 0) {
        // we've got bounds
        val widthSample = options.outWidth / size.width
        val heightSample = options.outHeight / size.height
        val sample = min(widthSample, heightSample)
        if (sample > 1) {
            options.inSampleSize = sample
        }
    }
    options.inJustDecodeBounds = false
    val decodeStream = contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(decodeStream, null, options)
    decodeStream?.close()
    return bitmap
}


/**
 * 保存图片[source]到系统相册
 * @param source 图片对象
 * @param title 文件显示名称
 * @param description 文件描述
 */
fun ContentResolver.saveBitmapToGallery(
    uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    folderName: String,
    source: Bitmap,
    title: String,
    description: String,
): Uri? {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.TITLE, title)
    values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
    values.put(MediaStore.Images.Media.DESCRIPTION, description)
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    if (folderName.isNonEmpty()) {
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$folderName")
    }
    // Add the date meta data to ensure the image is added at the front of the gallery
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    try {
        return insert(uri, values)?.let {
            openOutputStream(it)?.use {
                source.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            val id = ContentUris.parseId(it)
            // Wait until MINI_KIND thumbnail is generated.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadThumbnail(it, Size(50, 50), CancellationSignal())
            } else {
                val miniThumb =
                    MediaStore.Images.Thumbnails.getThumbnail(
                        this,
                        id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                    )
                // This is for backward compatibility.
                storeThumbnail(miniThumb, id, 50f, 50f, MediaStore.Images.Thumbnails.MICRO_KIND)
            }
            // Everything went well above, publish it!
            values.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            update(it, values, null, null);
            return it
        }
    } catch (e: java.lang.Exception) {
        return null
    }
}

private fun ContentResolver.storeThumbnail(
    source: Bitmap,
    id: Long,
    width: Float,
    height: Float,
    kind: Int,
): Bitmap? {
    // create the matrix to scale it
    val matrix = Matrix()
    val scaleX = width / source.width
    val scaleY = height / source.height
    matrix.setScale(scaleX, scaleY)
    val thumb = Bitmap.createBitmap(
        source, 0, 0,
        source.width,
        source.height, matrix,
        true
    )
    val values = ContentValues(4)
    values.put(MediaStore.Images.Thumbnails.KIND, kind)
    values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
    values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
    values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)
    return insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)?.let {
        try {
            val thumbOut = openOutputStream(it)?.use {
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            thumb
        } catch (ex: FileNotFoundException) {
            null
        } catch (ex: IOException) {
            null
        }
    }
}