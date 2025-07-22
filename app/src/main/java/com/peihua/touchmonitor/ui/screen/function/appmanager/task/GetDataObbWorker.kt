package com.peihua.touchmonitor.ui.screen.function.appmanager.task

import android.Manifest
import android.os.Build
import androidx.core.content.PermissionChecker
import androidx.documentfile.provider.DocumentFile
import com.fz.common.file.getFileSize
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.ui.AppInfoModel
import com.peihua.touchmonitor.ui.screen.function.appmanager.DocumentFileItem
import com.peihua.touchmonitor.utils.WorkScope
import com.peihua.touchmonitor.utils.canReadPathByDocumentFile
import com.peihua.touchmonitor.utils.externalStoragePath
import com.peihua.touchmonitor.utils.getDocumentFile
import com.peihua.touchmonitor.utils.getDocumentFileBySegments
import com.peihua.touchmonitor.utils.getDocumentFileOf
import com.peihua.touchmonitor.utils.getFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap

/**
 * 获取obb
 * @author dingpeihua
 * @date 2021/2/19 17:45
 */
class GetDataObbWorker(private val items: List<AppInfoModel>) : CoroutineScope by WorkScope() {
    constructor(item: AppInfoModel) : this(listOf<AppInfoModel>(item))

    fun execute(): Deferred<DataObbSizeInfo> {
        return async {
            var data = 0L
            var obb = 0L
            val containsDataCollection: ArrayList<AppInfoModel> = ArrayList()
            val containsObbCollection: ArrayList<AppInfoModel> = ArrayList()
            val hashMap: HashMap<String, Pair<Long, Long>> = HashMap()
            for ((index, item) in items.withIndex()) {
                if (!isActive) {
                    break
                }
                var dataObbSizeInfo = CACHE_DATA_OBB_SIZE.get(item.packageName)
                if (dataObbSizeInfo == null) {
                    dataObbSizeInfo = getDataObbSizeInfo(item)
                    CACHE_DATA_OBB_SIZE.put(item.packageName, dataObbSizeInfo)
                }
                hashMap.put(item.packageName, dataObbSizeInfo)
                if (dataObbSizeInfo.first > 0) {
                    containsDataCollection.add(item)
                }
                if (dataObbSizeInfo.second > 0) {
                    containsObbCollection.add(item)
                }
                data += dataObbSizeInfo.first
                obb += dataObbSizeInfo.second
            }
            return@async DataObbSizeInfo(containsDataCollection, containsObbCollection, hashMap,data, obb)
        }
    }

    private fun getDataObbSizeInfo(item: AppInfoModel): Pair<Long, Long> {
        var data: Long = 0
        var obb: Long = 0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
            && (PermissionChecker.checkSelfPermission(
                ServiceApplication.application,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED)
        ) {
            data = (externalStoragePath + "/android/data/" + item.packageName).getFileSize()
            obb = (externalStoragePath + "/android/obb/" + item.packageName).getFileSize()
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//                val dataDocumentFile = URI_DATA.getDocumentFile()
                if (dataDocumentFile.canReadPathByDocumentFile()) {
                    data = dataDocumentFile.getDocumentFileBySegments(
                        item.packageName,
                        false
                    ).getFileSize()
                }
//                val obbDocumentFile = URI_OBB.getDocumentFile()
                if (obbDocumentFile.canReadPathByDocumentFile()) {
                    data = obbDocumentFile.getDocumentFileBySegments(item.packageName,
                        false
                    ).getFileSize()
                }
            } else {
                val dataDocumentFile = URI_DATA.getDocumentFileOf(item.packageName)
                data = dataDocumentFile.getFileSize()
                val obbDocumentFile = URI_OBB.getDocumentFileOf(item.packageName)
                obb = obbDocumentFile.getFileSize()
            }
        }
        return Pair(data, obb)
    }

    companion object {
        private val CACHE_DATA_OBB_SIZE: ConcurrentHashMap<String, Pair<Long, Long>> =
            ConcurrentHashMap<String, Pair<Long, Long>>()
        const val URI_DATA: String =
            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata"
        const val URI_OBB: String =
            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb"
        val dataDocumentFile: DocumentFile? = URI_DATA.getDocumentFile()
        val obbDocumentFile: DocumentFile? = URI_OBB.getDocumentFile()
    }
}

data class DataObbSizeInfo(
    val dataItems: List<AppInfoModel>,
    val obbItems: List<AppInfoModel>,
    val sizeMap:Map<String, Pair<Long, Long>>,
    val data: Long,
    val obb: Long,
)