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
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.fz.common.file.deleteFileOrDir
import com.fz.common.text.isNonEmpty
import com.fz.common.utils.showToast
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.eLog
import com.peihua.touchmonitor.utils.writeToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class FileViewerActivity : ComponentActivity(), CoroutineScope by WorkScope() {
    private var mTreeUri: Uri? = null
    private var mType: Int = 0

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize()) {
                LoadingView(
                    Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .background(
                            shape = RectangleShape,
                            color = Color.White
                        )
                )
            }
        }
        dLog { "getRealPathFromURI>>>>>>intent:$intent" }
        val uri = intent.data
        dLog { "getRealPathFromURI>>>>>>intent.data:$uri" }
        if (uri == null) {
            finish()
            return
        }
        launch {
            val parentFile = getExternalFilesDir("share_apk")
            parentFile.deleteFileOrDir()
            try {
                val treeUri = intent.getStringExtra("treeUri")
                mTreeUri = treeUri?.toUri()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mType = intent.getIntExtra("type", 0)
            if (mType == 1) {
                val uri = if (Build.VERSION.SDK_INT < 30) {
                    OooOoO0(uri)
                } else {
                    OooOoO(uri)
                }
                installLocalApk(uri)
                finish()
                return@launch
            }
            dLog { "detailDocumentByUri>>>>>>uri.path:${uri.path}" }
            installApk(parseUri(uri))
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri == null) {
            return
        }
        launch {
            installApk(parseUri(uri))
        }
    }

    suspend fun parseUri(uri: Uri): Uri? {
        val scheme = uri.scheme
        if ("content" == scheme) {
            dLog { "paramUri_pre>>>$uri" }
            val paramUri = copyFile(uri)
            dLog { "paramUri>>>$paramUri" }
            val parentFile = getExternalFilesDir("")
            val builder = StringBuilder()
            builder.append(parentFile)
            if (paramUri == null) {
                showToast("解析包出问题1")
                eLog { "解析包出问题1" }
                return null
            }
            builder.append(paramUri.path)
            dLog { "parseUri>>>>>>builder:$builder" }
            val packageManager = getPackageManager()
            val packageInfo = packageManager.getPackageArchiveInfo(builder.toString(), 1)
            if (packageInfo == null) {
                showToast("解析包出问题2")
                eLog { "解析包出问题2" }
                return null
            }
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo?.sourceDir = builder.toString()
            applicationInfo?.publicSourceDir = builder.toString()
            return paramUri
        }
        return null
    }

    suspend fun copyFile(uri: Uri): Uri? {
        return try {
            contentResolver.openInputStream(uri).use {
                val r2 = getExternalFilesDir("share_apk")
                var lastPathSegment = uri.lastPathSegment
                val length = lastPathSegment?.length ?: 0
                if (length < 3) {
                    lastPathSegment = "temp"
                }
                val tempFile = File.createTempFile(lastPathSegment, ".apk", r2)
                dLog { "copyFile>>>>>>tempFile:${tempFile.absolutePath}" }
                it.writeToFile(tempFile)
                return FileProvider.getUriForFile(this, "$packageName.provider", tempFile)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun OooOoO0(uri: Uri): Uri? {
        dLog { "installer>>>$uri" }
        val newUri = copyFile(uri)
        val externalFilesDir = getExternalFilesDir("")
        val builder = java.lang.StringBuilder()
        builder.append(externalFilesDir)
        builder.append(newUri?.path)
        val path = builder.toString()
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(path, 1)
        if (packageInfo == null) {
            showToast("解析包出问题3")
            eLog { "解析包出问题3" }
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo!!.sourceDir = path
        applicationInfo.publicSourceDir = path
        return newUri
    }

    suspend fun OooOoO(uri: Uri?): Uri? {
        if (uri == null) {
            showToast("解析包出问题7")
            eLog { "解析包出问题7" }
            return null
        }
        val scheme = uri.scheme
        if (scheme == null) {
            showToast("解析包出问题7")
            eLog { "解析包出问题7" }
            return null
        }
        if ("file" == scheme) {
            if (Build.VERSION.SDK_INT >= 30) {
                return parseDocumentFile(mTreeUri, uri).dLog { "newUri>>>${this?.path}" }
            }
        }
        val builder = java.lang.StringBuilder()
        val externalFilesDir = getExternalFilesDir("")
        builder.append(externalFilesDir)
        builder.append(uri.path)
        val path = builder.toString()
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(path, 1)
        if (packageInfo == null) {
            showToast("解析包出问题8")
            eLog { "解析包出问题8" }
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo!!.sourceDir = path
        applicationInfo.publicSourceDir = path
        val singleUri = DocumentFile.fromSingleUri(this, uri)
        if (singleUri == null || !singleUri.exists()) {
            return null
        }
        return saveDocumentFile(uri)
    }

    suspend fun parseDocumentFile(treeUri: Uri?, uri: Uri): Uri? {
        if (treeUri == null) {
            return null
        }
        dLog { "treeUri>>>$treeUri" }
        val pickedDir = DocumentFile.fromTreeUri(this, treeUri)
        dLog { "pickedDir>>> $pickedDir" }
        if (pickedDir == null || !pickedDir.exists() || !pickedDir.isDirectory) {
            return null
        }
        val pickedFiles = pickedDir.listFiles()
        var length = pickedFiles.size
        if (0 == length) {
            return null
        }
        var index = 0
        var pickedFile: DocumentFile? = null
        while (length > index) {
            pickedFile = pickedFiles[index]
            dLog { "DocumentFile :${pickedFile?.name}" }
            val pickedFileUri = pickedFile.uri
            val r6 = pickedFileUri.path
            val r7 = uri.path
            if (pickedFile.isFile && r6 == r7) {
                break
            }
            index = index + 1
        }
        if (pickedFile == null || !pickedFile.exists() || !pickedFile.canRead()) {
            return null
        }
        val contentResolver = getContentResolver()
        val r0 = pickedFile.uri
        val newFile = createTempFile(uri)
        if (newFile == null) {
            return null
        }
        try {
            contentResolver.openInputStream(r0).use { ois ->
                ois.writeToFile(newFile, 8192)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val newFileUri = FileProvider.getUriForFile(this, "$packageName.provider", newFile)
        if (newFileUri == null) {
            return null
        }
        val builder = java.lang.StringBuilder()
        val externalFilesDir = getExternalFilesDir("")
        builder.append(externalFilesDir)
        builder.append(newFileUri.path)
        val r10 = uri.toString()
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(r10, 1)
        if (packageInfo == null) {
            showToast("解析包出问题")
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo!!.sourceDir = r10
        applicationInfo.publicSourceDir = r10
        val singleUri = DocumentFile.fromSingleUri(this, newFileUri)
        if (singleUri == null || !singleUri.exists()) {
            return null
        }
        return saveDocumentFile(newFileUri)
    }

    fun createTempFile(uri: Uri): File? {
        try {
            val r0 = getExternalFilesDir("share_apk")
            return File.createTempFile(uri.lastPathSegment ?: "temp", ".apk", r0)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun saveDocumentFile(uri: Uri): Uri? {
        val documentFile = DocumentFile.fromSingleUri(this, uri)
        if (documentFile == null || !documentFile.exists()) {
            return null
        }
        val file = getExternalFilesDir("share_apk")
        if (file == null) {
            return null
        }
        val shareApkFile = DocumentFile.fromFile(file)
        val temp = shareApkFile.createFile("", documentFile.name ?: "temp")
        if (temp == null) {
            return null
        }
        try {
            contentResolver.openInputStream(uri).use { ois ->
                contentResolver.openOutputStream(temp.uri).use { oos ->
                    ois.writeToFile(oos, 8192)
                }
            }
            return temp.uri
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    fun installLocalApk(uri: Uri?) {
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT < 30) {
            intent.setData(uri)
            startActivity(intent)
        } else {
            val file = getFileFromUri(uri)
            if (file == null) {
                return
            }
            intent.setData(FileProvider.getUriForFile(this, "$packageName.provider", file))
            startActivity(intent)
        }
        finish()
    }

    fun Context.installApk(uri: Uri?) {
        if (uri == null) {
            showToast("解析包出问题4")
            eLog { "解析包出问题4" }
            finish()
            return
        }
        try {
            val mediaType = "application/vnd.android.package-archive"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT < 29) {
                intent.setDataAndType(uri, mediaType)
            } else {
                intent.setDataAndType(uri, mediaType)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
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