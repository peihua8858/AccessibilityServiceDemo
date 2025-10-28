package com.peihua.touchmonitor.ui.screen.function.appmanager.task

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.peihua.compose.file.cRC32
import com.peihua.compose.file.writeToFile
import com.peihua.compose.utils.dLog
import com.peihua.compose.utils.ifNullOrEmpty
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.Constants
import com.peihua.touchmonitor.ui.screen.function.appmanager.FileItem
import com.peihua.touchmonitor.ui.screen.settings.SystemSettingsStore
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.externalStoragePath
import com.peihua.touchmonitor.utils.findDocumentFile
import com.peihua.touchmonitor.utils.getDocumentFileBySegments
import com.peihua.touchmonitor.utils.getExportPathDocumentFile
import com.peihua.touchmonitor.utils.isGrantedStoragePermission
import com.peihua.touchmonitor.utils.outputStreamForDocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
    callback: (TaskProcessModel<FileItem>.() -> Unit),
) : CoroutineScope by WorkScope() {
    constructor(
        context: Context,
        item: AppInfoModel,
        callback: (TaskProcessModel<FileItem>.() -> Unit),
    ) : this(context, listOf<AppInfoModel>(item), callback)

    private val model = TaskProcessModel<FileItem>().apply(callback)

    /**
     * 本次导出任务的目的存储路径是否为外置存储
     */
    private val isExternal = false
    private val zipLevel: Int = -1
    private var totalLength: Long = 0L
    private var mProgress: Long = 0L
    private var mCurrentWritingFile: FileItem = FileItem.createFileItemInstance("")
    private var mCurrentWritingPath: String? = null
    private val byteLength = 1024 * 10
    fun start() {
        model.invokeStart()
        launch {
            doExport()
        }
    }

    /**
     * 开始导出
     */
    private suspend fun doExport(): FileItem {
        try {
            val settings: SystemSettings = SystemSettingsStore.getSystemSettings()
            val exportPath = if(context.isGrantedStoragePermission()) settings.exportPath else Constants.DEFAULT_EXPORT_PATH
            val dataObbWorker = GetDataObbWorker(items)
            val dataObbSizeInfo = dataObbWorker.execute().await()
            val totalLength = getTotalLength(dataObbSizeInfo)
            var startTime = System.currentTimeMillis()
            dLog { "getTotalLength, totalLength $totalLength,isActive:$isActive" }
            for ((index, item) in items.withIndex()) {
                if (!isActive) {
                    mCurrentWritingFile.delete()
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
                            ) ?: continue
                        mCurrentWritingPath = documentFile.uri.toString()
                        mCurrentWritingFile = FileItem.createFileItemInstance(documentFile)
                        outputStream = documentFile.outputStreamForDocumentFile
                    } else {
                        val writeFileName = context.getWriteFileNameForAppItem(
                            item,
                            "apk", index + 1
                        )
                        val writePath ="$exportPath/$writeFileName"
                        mCurrentWritingPath = writePath
                        mCurrentWritingFile = FileItem.createFileItemInstance(writePath)
                        outputStream = FileOutputStream(writePath)
                    }
                    val file = File(item.sourcePath)
                    val input: InputStream = FileInputStream(file) //读入原文件
                    val result = input.writeToFile(outputStream, byteLength) { progress, speed ->
                        mProgress += speed
                        val endTime = System.currentTimeMillis()
                        if (endTime - startTime >= 1000) {
                            startTime = endTime
                            model.invokeSpeed(mCurrentWritingFile, speed)
                            model.invokeProgress(
                                mCurrentWritingFile,
                                mProgress,
                                totalLength
                            )
                        }
                    }
                    dLog { "writeToFile, save file  to ${mCurrentWritingFile.path} ${if (result) "successful" else "Failure"}" }
                } else {
                    val outputStream: OutputStream?
                    if (isExternal) {
                        val documentFile =
                            getWritingDocumentFileForAppItem(
                                context,
                                item,
                                "zip",
                                index + 1
                            ) ?: continue
                        mCurrentWritingPath = documentFile.uri.toString()
                        mCurrentWritingFile = FileItem.createFileItemInstance(documentFile)
                        outputStream = documentFile.outputStreamForDocumentFile
                    } else {
                        val writeFileName = context.getWriteFileNameForAppItem(
                            item,
                            "zip", index + 1
                        )
                        val writePath ="$exportPath/$writeFileName"
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
                                            FileItem.createFileItemInstance(pkgObbDocumentFile)
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
            model.invokeComplete(mCurrentWritingFile)
            return mCurrentWritingFile
        } catch (e: Throwable) {
            mCurrentWritingFile.delete()
            model.invokeComplete(mCurrentWritingFile, e)
        }
        return mCurrentWritingFile
    }

    fun extortAsync(): Deferred<FileItem> {
        return async { doExport() }
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
            fis.writeToFile(zos, bufferSize = byteLength, isCloseOs = false) { progress, speed ->
                mProgress += speed
                val endTime = System.currentTimeMillis()
                if (endTime - startTime >= 1000) {
                    startTime = endTime
                    model.invokeSpeed(mCurrentWritingFile, speed)
                    model.invokeProgress(mCurrentWritingFile, mProgress, totalLength)
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
        val fileName = context.getWriteFileNameForAppItem(
            appItem,
            extension,
            sequenceNumber
        )
        val parent = getExportPathDocumentFile(context, "", "".toUri())
        val documentFile = parent.findDocumentFile(fileName)
        if (documentFile != null && documentFile.exists()) documentFile.delete()
        return parent?.createFile(
            if ("apk".equals(extension, ignoreCase = true)
            ) "application/vnd.android.package-archive" else "application/x-zip-compressed",
            fileName
        )
    }

    fun Context.getWriteFileNameForAppItem(
        item: AppInfoModel,
        extension: String,
        sequenceNumber: Int,
    ): String {
        val name = item.name.ifNullOrEmpty { item.packageName }
        return "${name}_${item.versionName}_${sequenceNumber}.${extension}"
    }
}

class TaskProcessModel<T> {
    private var onStart: (() -> Unit)? = null
    private var onComplete: ((T, Throwable?) -> Unit)? = null
    private var onProgress: ((T, Long, Long) -> Unit)? = null
    private var onSpeed: ((T, Long) -> Unit)? = null
    infix fun onStart(onStart: (() -> Unit)?): TaskProcessModel<T> {
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

    fun invokeStart() {
        this.onStart?.invoke()
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