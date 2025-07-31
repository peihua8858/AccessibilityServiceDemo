package com.peihua.touchmonitor.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.fz.common.text.isNonEmpty
import com.peihua.touchmonitor.ServiceApplication
import java.io.File

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
                val columnIndex = cursor.getColumnIndex(column[0])
                val filePath = cursor.getString(columnIndex)
                if (filePath.isNonEmpty()) {
                    val file = File(filePath)
                    if (file.exists()) {
                        return file
                    }
                }
                dLog { "getFileFromContentUri>>>>>>filePath :$filePath,columnIndex:$columnIndex" }
                null
            } catch (e: Throwable) {
                e.printStackTrace()
                dLog { "getFileFromContentUri>>>>>>e:${e.message}" }
                null
            }
        }
    }
}