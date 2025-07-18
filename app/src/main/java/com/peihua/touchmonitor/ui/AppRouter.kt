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
}