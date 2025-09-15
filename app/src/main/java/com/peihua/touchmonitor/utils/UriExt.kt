package com.peihua.touchmonitor.utils

import android.net.Uri
import android.webkit.MimeTypeMap


val Uri?.mimeTypeFromFilePath: String?
    get() {
        this?:return null
        val extension = this.toString().substringAfterLast('.', "")
        dLog { "openWithFile>>>>extension：$extension" }
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
    }