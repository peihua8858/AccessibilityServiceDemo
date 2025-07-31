package com.peihua.touchmonitor.ui.screen.function.appmanager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.fz.common.utils.getFileFromContentUri
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.utils.getDocumentFileBySegments
import com.peihua.touchmonitor.utils.getFieldFromContentUri
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

abstract class FileItem : Comparable<FileItem> {
    val context: Context = ServiceApplication.application
    val contentResolver: ContentResolver = context.contentResolver
    abstract val name: String?

    abstract val isFile: Boolean

    abstract val isDirectory: Boolean

    abstract fun exists(): Boolean

    @Throws(Exception::class)
    abstract fun renameTo(newName: String): Boolean

    open fun mkdirs(): Boolean {
        return false
    }

    open fun createDirectory(name: String): DocumentFile? {
        return null
    }

    open val isDocumentFile: Boolean
        get() = false

    open val isFileInstance: Boolean
        get() = false

    open val isShareUriInstance: Boolean
        get() = false

    abstract fun canGetRealPath(): Boolean

    abstract val path: String

    abstract fun delete(): Boolean

    abstract fun listFileItems(): MutableList<FileItem>

    abstract fun length(): Long

    abstract fun lastModified(): Long

    abstract val parent: FileItem?

    open val isHidden: Boolean
        get() = false

    @get:Throws(Exception::class)
    abstract val inputStream: InputStream?

    @get:Throws(Exception::class)
    abstract val outputStream: OutputStream?

    open fun getDocumentFile(): DocumentFile? {
        return null
    }

    open fun getFile(): File? {
        return null
    }

    open val contentUri: Uri?
        get() = null

    override fun compareTo(o: FileItem): Int {
        when (sort_config) {
            0 -> {
//                try {
//                    if (PinyinUtil.getFirstSpell(this.name.toString()).toLowerCase().compareTo(
//                            PinyinUtil.getFirstSpell(
//                                o.name.toString()
//                            ).toLowerCase()
//                        ) > 0
//                    ) return 1
//                    if (PinyinUtil.getFirstSpell(this.name.toString()).toLowerCase().compareTo(
//                            PinyinUtil.getFirstSpell(
//                                o.name.toString()
//                            ).toLowerCase()
//                        ) < 0
//                    ) return -1
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
            }

            1 -> {
//                try {
//                    if (PinyinUtil.getFirstSpell(this.name.toString()).toLowerCase().compareTo(
//                            PinyinUtil.getFirstSpell(
//                                o.name.toString()
//                            ).toLowerCase()
//                        ) > 0
//                    ) return -1
//                    if (PinyinUtil.getFirstSpell(this.name.toString()).toLowerCase().compareTo(
//                            PinyinUtil.getFirstSpell(
//                                o.name.toString()
//                            ).toLowerCase()
//                        ) < 0
//                    ) return 1
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
            }

            else -> {}
        }
        return 0
    }

    abstract override fun toString(): String

    companion object {
        private var sort_config: Int = 0

        @Synchronized
        fun setSort_config(value: Int) {
            sort_config = value
        }

        fun createFileItemInstance(path: String): FileItem {
            return StandardFileItem(path)
        }

        fun createFileItemInstance(file: File): FileItem {
            return StandardFileItem(file)
        }

        @Throws(Exception::class)
        fun createFileItemInstance(treeUri: Uri, segments: String): FileItem {
            return DocumentFileItem(treeUri, segments)
        }

        fun createFileItemInstance(documentFile: DocumentFile): FileItem {
            return DocumentFileItem(documentFile)
        }

        fun createFileItemInstance(contentUri: Uri): FileItem {
            return ShareUriFileItem(contentUri)
        }
    }
}

class StandardFileItem(file: File) : FileItem() {
    private var file: File

    constructor(path: String) : this(File(path))

    init {
        this.file = file
        if (!file.exists()) {
            file.parentFile?.mkdirs()
        }
    }

    override val name: String
        get() = file.getName()
    override val isFile: Boolean
        get() = file.isFile()

    override val isDirectory: Boolean
        get() = file.isDirectory()

    override fun exists(): Boolean {
        return file.exists()
    }

    @Throws(java.lang.Exception::class)
    override fun renameTo(newName: String): Boolean {
        val destFile = File(file.getParentFile(), newName)
        if (destFile.exists()) {
            throw Exception(destFile.absolutePath + " already exists")
        }
        if (file.renameTo(destFile)) {
            file = destFile
            return true
        } else {
            throw Exception("error renaming to " + destFile.absolutePath)
        }
    }

    override fun canGetRealPath(): Boolean {
        return true
    }

    override val path: String
        get() = file.absolutePath

    override fun delete(): Boolean {
        return file.delete()
    }

    override fun listFileItems(): MutableList<FileItem> {
        val arrayList: ArrayList<FileItem> = ArrayList()
        try {
            val files = file.listFiles()
            if (files == null) return arrayList
            for (f in files) arrayList.add(StandardFileItem(f))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return arrayList
    }

    override fun length(): Long {
        return file.length()
    }

    override fun lastModified(): Long {
        return file.lastModified()
    }

    override val parent: FileItem?
        get() {
            val parent = file.getParentFile()
            if (parent != null) {
                return StandardFileItem(parent)
            }
            return null
        }

    @get:Throws(java.lang.Exception::class)
    override val inputStream: InputStream
        get() = FileInputStream(file)

    @get:Throws(java.lang.Exception::class)
    override val outputStream: OutputStream
        get() = java.io.FileOutputStream(file)

    override val isFileInstance: Boolean
        get() = true

    override fun mkdirs(): Boolean {
        return file.mkdirs()
    }

    override fun getFile(): File {
        return file
    }

    override val isHidden: Boolean
        get() = file.isHidden()

    override fun toString(): String {
        return file.absolutePath
    }
}


class DocumentFileItem : FileItem {

    private val documentFile: DocumentFile

    constructor(treeUri: Uri, segments: String?) {
        val documentFile =
            DocumentFile.fromTreeUri(ServiceApplication.application, treeUri)
        if (documentFile == null) throw java.lang.Exception("Can not get documentFile by the treeUri")
        this.documentFile =
            documentFile.getDocumentFileBySegments(segments, false)
                ?: throw Exception("Can not get documentFile by the segments")
    }

    constructor(documentFile: DocumentFile) {
        this.documentFile = documentFile
    }

    override val name: String?
        get() = documentFile.name

    override val isFile: Boolean
        get() = documentFile.isFile

    override val isDirectory: Boolean
        get() = documentFile.isDirectory

    override fun exists(): Boolean {
        return documentFile.exists()
    }

    override fun renameTo(newName: String): Boolean {
        return documentFile.renameTo(newName)
    }

    override fun canGetRealPath(): Boolean {
        return true
    }

    override val path: String
        get() {
            val uriPath: String? = documentFile.uri.path
            if (uriPath == null || uriPath.isEmpty()) return ""
            return "external/" + uriPath.substring(uriPath.lastIndexOf(":") + 1)
        }

    override fun delete(): Boolean {
        return documentFile.delete()
    }

    override fun listFileItems(): MutableList<FileItem> {
        val arrayList: ArrayList<FileItem> = ArrayList()
        try {
            val documentFiles = documentFile.listFiles()
            for (documentFile in documentFiles) {
                arrayList.add(DocumentFileItem(documentFile))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return arrayList
    }

    override fun length(): Long {
        return documentFile.length()
    }

    override fun lastModified(): Long {
        return documentFile.lastModified()
    }

    override val parent: FileItem?
        get() {
            val parent = documentFile.parentFile
            if (parent != null) {
                return DocumentFileItem(parent)
            }
            return null
        }

    @get:Throws(java.lang.Exception::class)
    override val inputStream: InputStream?
        get() = ServiceApplication.application.contentResolver
            .openInputStream(documentFile.uri)

    @get:Throws(java.lang.Exception::class)
    override val outputStream: OutputStream?
        get() = ServiceApplication.application.contentResolver
            .openOutputStream(documentFile.uri)

    override fun createDirectory(name: String): DocumentFile? {
        return documentFile.createDirectory(name)
    }

    override val isDocumentFile: Boolean
        get() = true

    override fun getDocumentFile(): DocumentFile {
        return documentFile
    }

    override fun toString(): String {
        return documentFile.uri.toString()
    }
}


class ShareUriFileItem(override val contentUri: Uri) : FileItem() {

    override val name: String?
        get() {
            if (ContentResolver.SCHEME_FILE.equals(contentUri.scheme, ignoreCase = true)) {
                return contentUri.lastPathSegment
            }
            val nameQueried = contentResolver.getFieldFromContentUri(
                contentUri,
                MediaStore.Files.FileColumns.DISPLAY_NAME
            )
            if (!TextUtils.isEmpty(nameQueried)) return nameQueried
            val file = contentResolver.getFileFromContentUri(contentUri)
            if (file != null) {
                return file.getName()
            }
            try {
                val documentFile = DocumentFile.fromSingleUri(context, contentUri)
                if (documentFile != null) {
                    val fileName: String? = documentFile.name
                    if (!TextUtils.isEmpty(fileName)) {
                        return fileName
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return "unknown.file"
        }

    override val isFile: Boolean
        get() = false

    override val isDirectory: Boolean
        get() = false

    override fun exists(): Boolean {
        return length() > 0L
    }

    @Throws(java.lang.Exception::class)
    override fun renameTo(newName: String): Boolean {
        return false
    }

    override fun canGetRealPath(): Boolean {
        return contentResolver.getFileFromContentUri(contentUri) != null
    }

    override val path: String
        get() {
            if (ContentResolver.SCHEME_FILE.equals(contentUri.scheme, ignoreCase = true)) {
                return contentUri.path?:""
            }
            val file = contentResolver.getFileFromContentUri(contentUri)
            if (file != null) return file.absolutePath
            return contentUri.toString()
        }

    override fun delete(): Boolean {
        return false
    }

    override fun listFileItems(): MutableList<FileItem> {
        return ArrayList<FileItem>()
    }

    override fun length(): Long {
        try {
            if (ContentResolver.SCHEME_FILE.equals(contentUri.scheme, ignoreCase = true)) {
                return File(contentUri.path).length()
            }
            val length = contentResolver.getFieldFromContentUri(
                contentUri, MediaStore.Files.FileColumns.SIZE
            )
            if (length != null && !TextUtils.isEmpty(length)) return length.toLong()
            val inputStream: InputStream = this.inputStream!!
            val available: Int = inputStream.available()
            inputStream.close()
            return available.toLong()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0L
    }

    override fun lastModified(): Long {
        return 0L
    }

    override val parent: FileItem?
        get() = null

    @get:Throws(java.lang.Exception::class)
    override val inputStream: InputStream?
        get() {
            if (ContentResolver.SCHEME_FILE.equals(contentUri.scheme, ignoreCase = true)) {
                return java.io.FileInputStream(contentUri.path)
            }
            return contentResolver.openInputStream(contentUri)
        }

    @get:Throws(java.lang.Exception::class)
    override val outputStream: OutputStream?
        get() = contentResolver.openOutputStream(contentUri)

    override val isShareUriInstance: Boolean
        get() = true

    override fun toString(): String {
        return contentUri.toString()
    }
}