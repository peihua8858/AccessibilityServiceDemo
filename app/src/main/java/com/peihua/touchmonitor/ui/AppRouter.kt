package com.peihua.touchmonitor.ui

import androidx.navigation.NamedNavArgument

sealed class AppRouter(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    /**
     * 首页
     */
    data object Home : AppRouter("home")
    /**
     * app list
     */
    data object Applications : AppRouter("applications")

}