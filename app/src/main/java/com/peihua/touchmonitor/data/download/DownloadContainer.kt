package com.peihua.touchmonitor.data.download

import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.data.repository.M3u8Repository

/**
 * 下载相关单例的持有者。
 *
 * 引擎必须是进程内唯一实例：UI 直接读 [M3u8DownloadEngine.progress]，
 * 前台 Service 下命令，两边看到的是同一份状态，省掉 ServiceConnection 的生命周期地狱。
 */
object DownloadContainer {

    val repository: M3u8Repository by lazy { M3u8Repository() }

    val paths: DownloadPaths by lazy { DownloadPaths(ServiceApplication.application) }

    val engine: M3u8DownloadEngine by lazy { M3u8DownloadEngine(repository, paths) }
}
