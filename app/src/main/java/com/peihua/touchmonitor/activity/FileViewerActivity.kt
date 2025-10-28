package com.peihua.touchmonitor.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.peihua.compose.file.deleteFileOrDir
import com.peihua.compose.file.writeToFile
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.eLog
import com.peihua.compose.utils.installLocalApk
import com.peihua.compose.utils.showToast
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.LoadingView
import com.peihua.touchmonitor.ui.components.surface
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.fileProvider
import com.peihua.touchmonitor.utils.installApk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class FileViewerActivity : ComponentActivity(), CoroutineScope by WorkScope() {
    private var mTreeUri: Uri? = null
    private var mType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize()) {
                LoadingView(
                    Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .surface(
                            radius = dimensionResource(id = R.dimen.dp_8),
                            elevation = dimensionResource(id = R.dimen.dp_3)
                        )
                )
            }
        }
        dLog { "getRealPathFromURI>>>>>>intent:$intent" }
        val uri = intent.data
        dLog { "getRealPathFromURI>>>>>>intent.data:$uri" }
        dLog { "getRealPathFromURI>>>>>>intent.getType:${intent.type}" }
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
                    parsePackageInfo(uri)
                } else {
                    parsePackageInfoForApi31AndAbove(uri)
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
            if (paramUri == null) {
                showToast("解析包出问题1")
                eLog { "解析包出问题1" }
                return null
            }
            val parentFile = getExternalFilesDir("")?.absolutePath
            val filePath = parentFile + paramUri.path
            dLog { "parseUri>>>>>>filePath:$filePath" }
            val packageManager = getPackageManager()
            val packageInfo = packageManager.getPackageArchiveInfo(filePath, 1)
            if (packageInfo == null) {
                showToast("解析包出问题2")
                eLog { "解析包出问题2" }
                return null
            }
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo?.sourceDir = filePath
            applicationInfo?.publicSourceDir = filePath
            return paramUri
        }
        return null
    }

    suspend fun copyFile(uri: Uri): Uri? {
        return try {
            val directory = getExternalFilesDir("share_apk")
            val tempFile = File.createTempFile("ShareTempApk", ".apk", directory)
            dLog { "copyFile>>>>>>tempFile:${tempFile.absolutePath}" }
            contentResolver.openInputStream(uri).writeToFile(tempFile, 8192)
            return tempFile.fileProvider
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * api 30以下解析包信息
     * @param uri
     */
    suspend fun parsePackageInfo(uri: Uri): Uri? {
        dLog { "installer>>>$uri" }
        val newUri = copyFile(uri)
        val externalFilesDir = getExternalFilesDir("")?.absolutePath
        val path = externalFilesDir + "" + newUri?.path
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(path, 1)
        if (packageInfo == null) {
            showToast("解析包出问题3")
            eLog { "解析包出问题3" }
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo?.sourceDir = path
        applicationInfo?.publicSourceDir = path
        return newUri
    }

    /**
     * api 31以上解析包信息
     * @param uri
     */
    suspend fun parsePackageInfoForApi31AndAbove(uri: Uri?): Uri? {
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
        val externalFilesDir = getExternalFilesDir("")?.absolutePath
        val path = externalFilesDir + "" + uri.path
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(path, 1)
        if (packageInfo == null) {
            showToast("解析包出问题8")
            eLog { "解析包出问题8" }
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo?.sourceDir = path
        applicationInfo?.publicSourceDir = path
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
        if (pickedFiles.isEmpty()) {
            return null
        }
        val uriPath = uri.path
        var pickedFile: DocumentFile? = null
        var pickedFileUri: Uri = uri
        for (file in pickedFiles) {
            pickedFile = file
            pickedFileUri = pickedFile.uri
            dLog { "DocumentFile :${pickedFile?.name}" }
            val path = pickedFileUri.path
            if (pickedFile.isFile && path == uriPath) {
                break
            }
        }
        if (pickedFile == null || !pickedFile.exists() || !pickedFile.canRead()) {
            return null
        }
        val contentResolver = getContentResolver()
        val newFile = createTempFile(uri)
        if (newFile == null) {
            return null
        }
        contentResolver.openInputStream(pickedFileUri).writeToFile(newFile, 8192)
        val newFileUri = newFile.fileProvider
        val externalFilesDir = getExternalFilesDir("")
        val dirPath = externalFilesDir?.absolutePath + newFileUri.path
        val packageManager = getPackageManager()
        val packageInfo = packageManager.getPackageArchiveInfo(dirPath, 1)
        if (packageInfo == null) {
            showToast("解析包出问题")
            return null
        }
        val applicationInfo = packageInfo.applicationInfo
        applicationInfo?.sourceDir = dirPath
        applicationInfo?.publicSourceDir = dirPath
        val singleUri = DocumentFile.fromSingleUri(this, newFileUri)
        if (singleUri == null || !singleUri.exists()) {
            return null
        }
        return saveDocumentFile(newFileUri)
    }

    fun createTempFile(uri: Uri): File? {
        try {
            val r0 = getExternalFilesDir("share_apk")
            return File.createTempFile("ShareTempAPk", ".apk", r0)
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
            contentResolver.openInputStream(uri).writeToFile(contentResolver.openOutputStream(temp.uri), 8192)
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
}