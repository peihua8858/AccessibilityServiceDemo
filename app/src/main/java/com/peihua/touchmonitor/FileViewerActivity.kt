package com.peihua.touchmonitor

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.fz.common.file.createFile
import com.fz.common.file.getFileNameByUri
import com.fz.common.text.isNonEmpty
import com.fz.common.utils.getDiskCacheDir
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.writeToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.io.path.createTempFile

class FileViewerActivity : ComponentActivity(), CoroutineScope by WorkScope() {
    val columnNames = arrayOf(
        "mime_type",
        "_display_name",
        "document_id",
        MediaStore.Images.ImageColumns.DATA
    )

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dLog { "getRealPathFromURI>>>>>>intent.data:${intent.data}" }
        dLog { "getRealPathFromURI>>>>>>intent:${intent.toString()}" }
        setContent {
            LoadingView()
        }
        val data = intent.data
        if (data != null) {
//            getRealPathFromURI(data)
            val fileName = getFileNameByUri(data)
            dLog { "getRealPathFromURI>>>>>>fileName:$fileName" }
//            val file = getFileFromContentUri(data)
            launch {
                val fileDescriptor = contentResolver.openFileDescriptor(data, "r")
                val parentFile = getExternalFilesDir("share_apk.apk")
                if (parentFile != null&&fileDescriptor!=null) {
                    val file = File.createTempFile("temp", ".apk", parentFile)
                    val outputFile = fileDescriptor.use {
                        FileInputStream(it.fileDescriptor).use { fis ->
                            fis.writeToFile(file)
                            file
                        }
                    }
                    dLog { "getRealPathFromURI>>>>>>outputFile:$outputFile" }
                    installAPK(outputFile?.toUri())
                    finish()
                }else {
                    dLog { "getRealPathFromURI>>>>>>parentFile is null" }
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    /**
     * 启动安装APK
     */
    fun Context.installAPK(uri: Uri?) {
        if (uri == null) {
            return
        }
        // For other schemes, let's be conservative about
        // the data we include -- only the host and port, not the query params, path or
        // fragment, because those can often have sensitive info.
        dLog { "getRealPathFromURI>>>>>>apkFile:$uri" }
        val installApkIntent = Intent()
        installApkIntent.action = Intent.ACTION_INSTALL_PACKAGE
        installApkIntent.addCategory(Intent.CATEGORY_DEFAULT)
        installApkIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        var file = getFileFromUri(uri)
        if (file == null) {
            var path = uri.path
            dLog { "getRealPathFromURI>>>>>>path:$path" }
            if (path.isNullOrEmpty()) {
                return
            }
            if (path.startsWith("/external_storage_root/") || path.startsWith("external_storage_root/")) {
                path = path.replace(
                    "external_storage_root",
                    Environment.getExternalStorageDirectory()?.path ?: ""
                )
            }
            file = File(path)
            if (!file.exists()) {
                dLog { "getRealPathFromURI>>>>>>file $path is not exists" }
            }
            dLog { "getRealPathFromURI>>>>>>path:${file.absolutePath}" }
        }
        val tempUri = FileProvider.getUriForFile(this, "${packageName}.fileProvider", file)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            installApkIntent.setDataAndType(tempUri, "application/vnd.android.package-archive")
            installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            installApkIntent.setDataAndType(
                tempUri,
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
}