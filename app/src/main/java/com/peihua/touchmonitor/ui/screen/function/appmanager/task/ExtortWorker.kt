package com.peihua.touchmonitor.ui.screen.function.appmanager.task

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.screen.function.appmanager.FileItem
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.appExternalStoragePath
import com.peihua.touchmonitor.utils.cRC32
import com.peihua.touchmonitor.utils.externalStoragePath
import com.peihua.touchmonitor.utils.findDocumentFile
import com.peihua.touchmonitor.utils.getDocumentFileBySegments
import com.peihua.touchmonitor.utils.getExportPathDocumentFile
import com.peihua.touchmonitor.utils.outputStreamForDocumentFile
import com.peihua.touchmonitor.utils.writeToFile
import com.peihua.touchmonitor.utils.writeToZip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExtortWorker(
    private val context: Context,
    private val items: List<AppInfoModel>,
    callback: (TaskProcessModel<ExtortWorker>.() -> Unit),
) : CoroutineScope by WorkScope() {
    constructor(
        context: Context,
        item: AppInfoModel,
        callback: (TaskProcessModel<ExtortWorker>.() -> Unit),
    ) : this(context, listOf<AppInfoModel>(item), callback)

    private val model = TaskProcessModel<ExtortWorker>().apply(callback)

    /**
     * 本次导出任务的目的存储路径是否为外置存储
     */
    private val isExternal = false
    private val zipLevel: Int = -1
    private var totalLength: Long = 0L
    private var mProgress: Long = 0L
    private var mCurrentWritingFile: FileItem? = null
    private var mCurrentWritingPath: String? = null

    init {

    }

    fun start() {
        model.invokeStart(this)
        launch {
            try {
                val dataObbWorker = GetDataObbWorker(items)
                val dataObbSizeInfo = dataObbWorker.execute().await()
                val totalLength = getTotalLength(dataObbSizeInfo)
                var startTime = System.currentTimeMillis()
                for ((index, item) in items.withIndex()) {
                    if (!isActive) {
                        break
                    }
                    if (!item.exportData && !item.exportObb) {
                        val outputStream: OutputStream?
                        if (isExternal) {
                            val documentFile =
                                getWritingDocumentFileForAppItem(
                                    context,
                                    item,
                                    "apk",
                                    index + 1
                                )
                            mCurrentWritingPath = documentFile?.uri?.toString()
                            mCurrentWritingFile =
                                documentFile?.let { FileItem.createFileItemInstance(it) }
                            outputStream = documentFile?.outputStreamForDocumentFile
                        } else {
                            val writePath =
                                getAbsoluteWritePath(context, item, "apk", index + 1)
                            mCurrentWritingPath = writePath
                            mCurrentWritingFile = FileItem.createFileItemInstance(writePath)
                            outputStream = FileOutputStream(writePath)
                        }
                        val file = File(item.sourcePath)
                        val input: InputStream = FileInputStream(file) //读入原文件
                        input.writeToFile(outputStream, 1024 * 10) { progress, speed ->
                            mProgress += speed
                            val endTime = System.currentTimeMillis()
                            if (endTime - startTime >= 1000) {
                                startTime = endTime
                                model.invokeSpeed(this@ExtortWorker, speed)
                                model.invokeProgress(
                                    this@ExtortWorker,
                                    mProgress,
                                    totalLength
                                )
                            }
                        }
                    } else {
                        val outputStream: OutputStream?
                        if (isExternal) {
                            val documentFile =
                                getWritingDocumentFileForAppItem(
                                    context,
                                    item,
                                    "zip",
                                    index + 1
                                )
                            mCurrentWritingPath = documentFile?.uri?.toString()
                            mCurrentWritingFile =
                                documentFile?.let { FileItem.createFileItemInstance(it) }
                            outputStream = documentFile?.outputStreamForDocumentFile
                        } else {
                            val writePath =
                                getAbsoluteWritePath(context, item, "zip", index + 1)
                            mCurrentWritingPath = writePath
                            mCurrentWritingFile = FileItem.createFileItemInstance(writePath)
                            outputStream = FileOutputStream(writePath)
                        }

                        val zos = ZipOutputStream(BufferedOutputStream(outputStream))
                        zos.setComment("Packaged by com.peihua8858.assists \nhttps://github.com/peihua8858/assists")
                        if (zipLevel >= 0 && zipLevel <= 9) zos.setLevel(zipLevel)
                        writeToZip(item.fileItem, "", zos, zipLevel)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && PermissionChecker.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) == PermissionChecker.PERMISSION_GRANTED
                        ) {
                            if (item.exportData) {
                                writeToZip(
                                    FileItem.createFileItemInstance(File(externalStoragePath + "/android/data/" + item.packageName)),
                                    "Android/data/",
                                    zos,
                                    zipLevel
                                )
                            }
                            if (item.exportObb) {
                                writeToZip(
                                    FileItem.createFileItemInstance(File(externalStoragePath + "/android/obb/" + item.packageName)),
                                    "Android/obb/",
                                    zos,
                                    zipLevel
                                )
                            }
                        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            if (item.exportData) {
                                var dataFileItem: FileItem? = null
                                try {
                                    val dataDocumentFile = GetDataObbWorker.dataDocumentFile
                                    if (dataDocumentFile != null) {
                                        val pkgDataDocumentFile =
                                            dataDocumentFile.getDocumentFileBySegments(
                                                item.packageName,
                                                false
                                            )
                                        if (pkgDataDocumentFile != null) {
                                            dataFileItem =
                                                FileItem.createFileItemInstance(
                                                    pkgDataDocumentFile
                                                )
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.i(javaClass.getSimpleName(), e.toString())
                                }
                                if (dataFileItem != null) {
                                    writeToZip(dataFileItem, "Android/data/", zos, zipLevel)
                                }
                            }
                            if (item.exportObb) {
                                var obbFileItem: FileItem? = null
                                try {
                                    val obbDocumentFile = GetDataObbWorker.obbDocumentFile
                                    if (obbDocumentFile != null) {
                                        val pkgObbDocumentFile =
                                            obbDocumentFile.getDocumentFileBySegments(
                                                item.packageName,
                                                false
                                            )
                                        if (pkgObbDocumentFile != null) {
                                            obbFileItem =
                                                FileItem.createFileItemInstance(
                                                    pkgObbDocumentFile
                                                )
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.i(javaClass.getSimpleName(), e.toString())
                                }
                                if (obbFileItem != null) {
                                    writeToZip(obbFileItem, "Android/obb/", zos, zipLevel)
                                }
                            }
                        } else {
                            if (item.exportData) {
                                var dataFileItem: FileItem? = null
                                try {
                                    val dataDocumentFile = GetDataObbWorker.dataDocumentFile
                                    if (dataDocumentFile != null) {
                                        dataFileItem =
                                            FileItem.createFileItemInstance(dataDocumentFile)
                                    }
                                } catch (e: Exception) {
                                    Log.i(javaClass.getSimpleName(), e.toString())
                                }
                                if (dataFileItem != null) {
                                    writeToZip(dataFileItem, "Android/data/", zos, zipLevel)
                                }
                            }
                            if (item.exportObb) {
                                var obbFileItem: FileItem? = null
                                try {
                                    val obbDocumentFile = GetDataObbWorker.obbDocumentFile
                                    if (obbDocumentFile != null) {
                                        obbFileItem =
                                            FileItem.createFileItemInstance(obbDocumentFile)
                                    }
                                } catch (e: Exception) {
                                    Log.i(javaClass.getSimpleName(), e.toString())
                                }
                                if (obbFileItem != null) {
                                    writeToZip(obbFileItem, "Android/obb/", zos, zipLevel)
                                }
                            }
                        }
                        zos.flush()
                        zos.close()
                    }
                }
                //后续处理
                model.invokeComplete(this@ExtortWorker)
            } catch (e: Exception) {
                mCurrentWritingFile?.delete()
                model.invokeComplete(this@ExtortWorker, e)
            }
            if (!isActive) {
                mCurrentWritingFile?.delete()
            }
        }
    }

    private suspend fun writeToZip(
        fileItem: FileItem?,
        parent: String?,
        zos: ZipOutputStream?,
        zipLevel: Int,
    ) {
        if (fileItem == null || parent == null || zos == null) return
        if (!isActive) return
        if (!fileItem.exists()) return
        var tempParent = parent
        if (fileItem.isDirectory) {
            tempParent += fileItem.name + File.separator
            val fileItemList: MutableList<FileItem> = fileItem.listFileItems()
            if (fileItemList.isNotEmpty()) {
                for (f in fileItemList) {
                    writeToZip(f, tempParent, zos, zipLevel)
                }
            } else {
                try {
                    zos.putNextEntry(ZipEntry(tempParent))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (fileItem.isFile) {
            var startTime = System.currentTimeMillis()
            val fis = fileItem.inputStream
            val zipEntry = ZipEntry(parent + fileItem.name)
            if (zipLevel == 0) {
                zipEntry.setMethod(ZipOutputStream.STORED)
                zipEntry.setCompressedSize(fileItem.length())
                zipEntry.setSize(fileItem.length())
                zipEntry.setCrc(fis?.cRC32?.value ?: 0L)
            }
            zos.putNextEntry(zipEntry)
            fis.writeToFile(zos, isCloseOs = false) { progress, speed ->
                mProgress += speed
                val endTime = System.currentTimeMillis()
                if (endTime - startTime >= 1000) {
                    startTime = endTime
                    model.invokeSpeed(this@ExtortWorker, speed)
                    model.invokeProgress(this@ExtortWorker, mProgress, totalLength)
                }
            }
        }
    }

    private fun getTotalLength(sizeInfo: DataObbSizeInfo?): Long {
        var total = 0L
        items.forEach { item ->
            val pair = sizeInfo?.sizeMap?.get(item.packageName)
            total += item.fileSize
            if (pair != null) {
                if (item.exportData) total += pair.first
                if (item.exportObb) total += pair.second
            }
        }
        return total
    }


    @Throws(java.lang.Exception::class)
    fun getWritingDocumentFileForAppItem(
        context: Context,
        appItem: AppInfoModel,
        extension: String,
        sequenceNumber: Int,
    ): DocumentFile? {
        val writingFileName = context.getWriteFileNameForAppItem(
            appItem,
            extension,
            sequenceNumber
        )
        val parent = getExportPathDocumentFile(context, "", "".toUri())
        val documentFile = parent.findDocumentFile(writingFileName)
        if (documentFile != null && documentFile.exists()) documentFile.delete()
        return parent?.createFile(
            if ("apk".equals(
                    extension,
                    ignoreCase = true
                )
            ) "application/vnd.android.package-archive" else "application/x-zip-compressed",
            writingFileName
        )
    }

    /**
     * api19及以上使用App所属外置存储作为默认导出路径(/storage/emulated/0/android/data/com.github.ghmxr.apkextractor/files)，对于旧版本已授权过并升级到此的，sp取值不变
     */
    var PREFERENCE_SAVE_PATH_DEFAULT: String? = null

    init {
        PREFERENCE_SAVE_PATH_DEFAULT = if (PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            "$externalStoragePath/Backup"
        } else {
            context.appExternalStoragePath + "/Backup"
        }
    }

    /**
     * 为AppItem获取一个内置存储绝对写入路径
     *
     * @param extension "apk"或者"zip"
     */
    fun getAbsoluteWritePath(
        context: Context,
        item: AppInfoModel,
        extension: String,
        sequenceNumber: Int,
    ): String {
        return ("$PREFERENCE_SAVE_PATH_DEFAULT/" + context.getWriteFileNameForAppItem(
            item,
            extension,
            sequenceNumber
        ))
    }

    fun Context.getWriteFileNameForAppItem(
        item: AppInfoModel,
        extension: String,
        sequenceNumber: Int,
    ): String {
        return "${item.packageName}_${sequenceNumber}.${extension}"
    }
}

class TaskProcessModel<T> {
    private var onStart: ((T) -> Unit)? = null
    private var onComplete: ((T, Throwable?) -> Unit)? = null
    private var onProgress: ((T, Long, Long) -> Unit)? = null
    private var onSpeed: ((T, Long) -> Unit)? = null
    infix fun onStart(onStart: ((T) -> Unit)?): TaskProcessModel<T> {
        this.onStart = onStart
        return this
    }

    infix fun onComplete(onComplete: ((T, Throwable?) -> Unit)?): TaskProcessModel<T> {
        this.onComplete = onComplete
        return this
    }


    infix fun onProgress(onProgress: ((T, Long, Long) -> Unit)?): TaskProcessModel<T> {
        this.onProgress = onProgress
        return this
    }

    infix fun onSpeed(onSpeed: ((T, Long) -> Unit)?): TaskProcessModel<T> {
        this.onSpeed = onSpeed
        return this
    }

    fun invokeStart(model: T) {
        this.onStart?.invoke(model)
    }

    fun invokeComplete(model: T, e: Throwable? = null) {
        this.onComplete?.invoke(model, e)
    }

    fun invokeProgress(model: T, current: Long, total: Long) {
        this.onProgress?.invoke(model, current, total)
    }

    fun invokeSpeed(model: T, speed: Long) {
        this.onSpeed?.invoke(model, speed)
    }
}