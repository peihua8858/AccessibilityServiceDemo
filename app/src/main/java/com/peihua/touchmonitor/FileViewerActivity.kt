package com.peihua.touchmonitor

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.fz.common.file.getFileNameByUri
import com.fz.common.text.isNonEmpty
import com.peihua.touchmonitor.utils.dLog
import java.io.File

class FileViewerActivity : ComponentActivity() {
    val columnNames =arrayOf(
        "mime_type",
        "_display_name",
        "document_id",
        MediaStore.Images.ImageColumns.DATA
    )
    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dLog { "getRealPathFromURI>>>>>>intent.data:${intent.data}" }
        val data = intent.data
        if (data != null) {
            getRealPathFromURI(data)
            val fileName = getFileNameByUri(data)
           val file = getFileFromContentUri(data)
            dLog { "getRealPathFromURI>>>>>>file:${file?.absolutePath}" }
            dLog { "getRealPathFromURI>>>>>>fileName:$fileName" }
            if (fileName.isNonEmpty()) {
                if (fileName.contains(".apk")) {
                    installAPK(data)
                } else {
                    startActivity(intent)
                }
            }
        }
        finish()
    }
    /**
     * 启动安装APK
     */
    fun Context.installAPK(uri: Uri) {

        // For other schemes, let's be conservative about
        // the data we include -- only the host and port, not the query params, path or
        // fragment, because those can often have sensitive info.
        val builder = StringBuilder()
        builder.append(uri.scheme)
        val host: String? = uri.host
        dLog { "getRealPathFromURI>>>>>>host:$host" }
        val port: Int = uri.port
        val path: String? = uri.path
        dLog { "getRealPathFromURI>>>>>>path:$path" }
        val authority: String? = uri.authority
        dLog { "getRealPathFromURI>>>>>>authority:$authority" }
        if (authority != null) builder.append("//")
        if (host != null) builder.append(BuildConfig.APPLICATION_ID)
        if (port != -1) builder.append(":").append(port)
        if (authority!=null) {
            builder.append("/").append(authority)
        }
        if (path!=null) {
            builder.append("/").append(path)
        }
        val apkFile =uri.buildUpon().authority(BuildConfig.APPLICATION_ID).build()
        dLog { "getRealPathFromURI>>>>>>apkFile:$apkFile" }
        val installApkIntent = Intent()
        installApkIntent.action = Intent.ACTION_VIEW
        installApkIntent.addCategory(Intent.CATEGORY_DEFAULT)
        installApkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            installApkIntent.setDataAndType(apkFile, "application/vnd.android.package-archive")
            installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            installApkIntent.setDataAndType(
                apkFile,
                "application/vnd.android.package-archive"
            )
        }
        if (packageManager.queryIntentActivities(installApkIntent, 0).isNotEmpty()) {
            startActivity(installApkIntent)
        }
    }
    fun Context.getRealPathFromURI(contentUri: Uri): File? {
        return contentResolver.query(
            contentUri,
            null, null, null, null
        ).use { cursor ->
            val path = if (cursor == null) {
                contentUri.path
            } else {
                val columnNames = cursor.columnNames
                cursor.moveToFirst()
                dLog { " getRealPathFromURI>>>>>>cursor.columnNames:${columnNames.contentToString()}" }
                for ((i, name) in columnNames.withIndex()) {
                    dLog { "getRealPathFromURI>>>>>>name:$name" }
                    try {
                        val index = cursor.getColumnIndexOrThrow(name)
                        val text = cursor.getString(index)
                        dLog { "getRealPathFromURI>>>>>>columnName:$name,text:$text,index:$index" }
                    } catch (e: Exception) {
                       e.printStackTrace()
                    }
                }
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                cursor.getString(index)
            }
            val file = File(path)
            if (file.exists()) {
                file
            } else null
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
                val id = wholeID.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
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
                val columnIndex = cursor.getColumnIndex(column[0])
                cursor.moveToFirst()
                val filePath = cursor.getString(columnIndex)
                if (filePath.isNonEmpty()) {
                    return File(filePath)
                }
                null
            }
        }
    }
}