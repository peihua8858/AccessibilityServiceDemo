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

    data object LogScreen : AppRouter("log")
//    data object LogDetail :
//        AppRouter("log_detail/{path}", listOf(navArgument("path") { type = NavType.StringType }))
    /**
     * 消息
     */
    data class LogDetail(val filePath: String) :
        AppRouter("logDetail/$filePath", listOf(navArgument("filePath") {
            type = NavType.StringType
            defaultValue = ""
        }))
}