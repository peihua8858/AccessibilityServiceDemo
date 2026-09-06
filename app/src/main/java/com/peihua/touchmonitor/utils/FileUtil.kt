@file:JvmName("FileUtil")
@file:JvmMultifileClass

package com.peihua.touchmonitor.utils

import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.eLog
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicLong


val File.ensureDirExist: Boolean
    get() {
        return if (!exists()) {
            dLog { "目录${absolutePath}不存在，创建它" }
            val isCreate = mkdirs()
            if (isCreate) {
                dLog { "目录${absolutePath}已创建" }
                true
            } else {
                eLog { "目录${absolutePath}创建失败" }
                false
            }
        }else true
    }

@Throws(IOException::class)
fun InputStream?.copy(out: OutputStream,bytesCounter: AtomicLong?=null) {
    this?.let { it ->
        BufferedInputStream(it).use { input ->
            BufferedOutputStream(out).use { output ->
                var len = 0
                val buffer = ByteArray(1024)
                while (input.read(buffer).also { len = it } != -1) {
                    output.write(buffer, 0, len)
                    bytesCounter?.addAndGet(len.toLong())
                }
                output.flush()
            }
        }
    }
}