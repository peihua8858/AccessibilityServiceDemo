package com.peihua.touchmonitor.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.fz.common.file.copyToFile
import com.peihua.selector.result.SystemPhotoCropVisualMediaRequestBuilder
import com.peihua.touchmonitor.ServiceApplication


val Uri?.mimeTypeFromFilePath: String?
    get() {
        this ?: return null
        val extension = this.toString().substringAfterLast('.', "")
        dLog { "openWithFile>>>>extension：$extension" }
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
    }

fun Uri.takePersistableUriPermission(): Uri {
    val context = ServiceApplication.application
    context.contentResolver.takePersistableUriPermission(
        this,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
    return this
}


fun Context.saveFileByUri(uri: Uri?): Uri? {
    if (uri == null) return null
    val fis = contentResolver.openInputStream(uri)
    if (fis != null) {
        val file = "IMG_".createFile("jpg")
        if (!file.exists()) {
            file.parentFile?.mkdirs()
        }
        file.copyToFile(fis)
//            val file = getRealPathFromURI(uri) ?: return
        val mImageUri = if (isAtLeastN) {
            /*7.0以上要通过FileProvider将File转化为Uri*/
            file.fileProvider
        } else {
            /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
            Uri.fromFile(file)
        }
        return mImageUri
    }
    return null
}

fun Context.insertUri(values: ContentValues = ContentValues()): Uri? {
    return contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    )
}

