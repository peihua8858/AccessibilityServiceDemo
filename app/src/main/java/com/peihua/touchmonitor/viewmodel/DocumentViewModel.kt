package com.peihua.touchmonitor.viewmodel

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.SavedStateHandle
import com.peihua.compose.utils.dLog
import com.peihua.touchmonitor.utils.getString
import java.io.File

open class DocumentViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : MediaViewModel(application, savedStateHandle) {
    var types: Array<String> = documentTypes

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
    companion object{
        val documentTypes = arrayOf(
            ".pdf", ".xml", ".java", ".php", ".html", ".htm", ".text/x-asm", ".pl", ".xls", ".xlsx",
            ".xld", ".xlc", ".ppt", ".pptx", ".ppsx", ".pptm", ".doc", ".docx", ".msg", ".odt", ".txt",
            ".tex", ".text", ".wpd", ".wps"
        )
    }
}

class PdfViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".pdf")
    }
}

class WordViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".doc", ".docx")
    }
}

class ExcelViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".xls", ".xlsx", ".xld", ".xlc")
    }
}

class PptViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".ppt",".pptx", ".ppsx", ".pptm")
    }
}

class TextViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".text/x-asm", ".txt", ".tex", ".text")
    }
}

class XmlViewModel(application: Application, savedStateHandle: SavedStateHandle) : DocumentViewModel(application, savedStateHandle) {
    init {
        types = arrayOf(".xml")
    }
}