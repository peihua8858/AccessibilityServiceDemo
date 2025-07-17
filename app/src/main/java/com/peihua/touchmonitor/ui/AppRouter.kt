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
     * app 提取器
     */
    data object AppExtractorScreen : AppRouter("appExtractor")
//    /**
//     * 消息
//     */
//    data object LogDetail :
//        AppRouter("logDetail?filePath={filePath}", listOf(navArgument("filePath") {
//            type = NavType.StringType
//            defaultValue = ""
//        }))
}