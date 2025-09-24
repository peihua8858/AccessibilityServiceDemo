package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.getString
import java.io.File

class DocumentViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : MediaViewModel(application, savedStateHandle) {
    var types: Array<String> = arrayOf(
        ".pdf",
        ".xml",
        ".java",
        ".php",
        ".html",
        ".htm",
        ".text/x-asm",
        ".pl",
        ".xls",
        ".xlsx",
        ".xld",
        ".xlc",
        ".ppt",
        ".pptx",
        ".ppsx",
        ".pptm",
        ".doc",
        ".docx",
        ".msg",
        ".odt",
        ".txt",
        ".tex",
        ".text",
        ".wpd",
        ".wps"
    )

    init {
        showDate = false
    }

    override val filter: (Cursor) -> Boolean
        get() = {
            val path = it.getString(MediaStore.MediaColumns.DATA)
            val extension = File(path).extension
            dLog { "extension:$extension" }
            types.contains(".$extension")
        }
}