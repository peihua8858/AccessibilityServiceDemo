package com.peihua.touchmonitor.ui

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class AppRouter(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList(),
) {
    /**
     * 首页
     */
    data object Home : AppRouter("home")

    /**
     * app list
     */
    data object Applications : AppRouter("applications")

    /**
     * 日志列表
     */
    data object LogScreen : AppRouter("log")

    /**
     * 日志详情
     */
    data object LogDetail : AppRouter("logDetail")

    /**
     * 自动刷屏器
     */
    data object AutoScroller : AppRouter("autoScroller")

    /**
     * 应用管理
     */
    data object AppManagerScreen : AppRouter("appManager")

    /**
     * 应用详情
     */
    data object AppDetailScreen :
        AppRouter("appDetail/{packageName}", listOf(navArgument("packageName") { type = NavType.StringType }))

    /**
     * APK管理
     */
    data object ApkManagerScreen : AppRouter("apkManager")

    /**
     * 设置
     */
    data object SettingsScreen : AppRouter("settings")

    /**
     * 图片列表页面
     */
    data object PictureScreen : AppRouter("picture")

    /**
     * 音频列表页面
     */
    data object AudioScreen : AppRouter("audio")

    /**
     * 视频列表页面
     */
    data object VideoScreen : AppRouter("video")

    /**
     * 文档列表页面
     */
    data object DocumentScreen : AppRouter("document")

    /**
     * 压缩文件列表页面
     */
    data object ZipScreen : AppRouter("zip")

    /**
     * 下载文件夹页面
     */
    data object DownloadScreen : AppRouter("download")

    /**
     * 收藏夹列表页面
     */
    data object CollectScreen : AppRouter("collect")

    /**
     * 搜索页面
     */
    data object SearchScreen : AppRouter("search")

    /**
     * 视频播放页面
     */
    data object VideoPlayerScreen : AppRouter("videoPlayer")

    /**
     * 图片预览页面
     */
    data object PhotoPreviewScreen : AppRouter("photoPreview")

    /**
     * 音频播放页面
     */
    data object AudioPlayerScreen : AppRouter("audioPlayer")

    /**
     * 存储页
     */
    data object StorageScreen :
        AppRouter(
            "storage/{packageName}",
            listOf(navArgument("type") { type = NavType.IntType })
        ) {
        const val TYPE_DOWNLOAD = 0
        const val TYPE_ROOT = 1

    }
}