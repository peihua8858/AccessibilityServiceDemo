package com.peihua.touchmonitor.ui.screen.settings

import com.peihua.touchmonitor.ui.theme.ThemeMode


data class SystemSettings(
    val themeMode: ThemeMode,
    val isDarkTheme: Boolean,
    val dynamicColor: Boolean)