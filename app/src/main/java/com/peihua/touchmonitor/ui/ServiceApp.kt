package com.peihua.touchmonitor.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDirections
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.model.SystemSettings
import com.peihua.touchmonitor.ui.applications.AppScreen
import com.peihua.touchmonitor.ui.logcat.LogDetailScreen
import com.peihua.touchmonitor.ui.logcat.LogScreen
import com.peihua.touchmonitor.ui.screen.dialog.MessageDialog
import com.peihua.touchmonitor.ui.screen.function.DayNewsScreen
import com.peihua.touchmonitor.ui.screen.function.apk.ApkScreen
import com.peihua.touchmonitor.ui.screen.function.appmanager.AppDetailScreen
import com.peihua.touchmonitor.ui.screen.function.appmanager.MainAppExtractorScreen
import com.peihua.touchmonitor.ui.screen.function.audio.AudioPlayerScreen
import com.peihua.touchmonitor.ui.screen.function.audio.AudioScreen
import com.peihua.touchmonitor.ui.screen.function.autoScroller.ShortVideoScreen
import com.peihua.touchmonitor.ui.screen.function.collect.CollectScreen
import com.peihua.touchmonitor.ui.screen.function.devices.DeviceInfoScreen
import com.peihua.touchmonitor.ui.screen.function.devices.ScreenDeadPixelsScreen
import com.peihua.touchmonitor.ui.screen.function.devices.ScreenTimeScreen
import com.peihua.touchmonitor.ui.screen.function.document.DocumentScreen
import com.peihua.touchmonitor.ui.screen.function.download.DownloadScreen
import com.peihua.touchmonitor.ui.screen.function.images.GifImageDecompositionScreen
import com.peihua.touchmonitor.ui.screen.function.images.ImagePixelizationScreen
import com.peihua.touchmonitor.ui.screen.function.images.PhotoToBlackAndWhiteScreen
import com.peihua.touchmonitor.ui.screen.function.images.PhotoToSketchScreen
import com.peihua.touchmonitor.ui.screen.function.images.PhotoWatermarkScreen
import com.peihua.touchmonitor.ui.screen.function.images.QrCodeGeneratorScreen
import com.peihua.touchmonitor.ui.screen.function.images.VideoToGifScreen
import com.peihua.touchmonitor.ui.screen.function.picture.PhotoPreviewScreen
import com.peihua.touchmonitor.ui.screen.function.picture.PictureScreen
import com.peihua.touchmonitor.ui.screen.function.search.SearchScreen
import com.peihua.touchmonitor.ui.screen.function.video.VideoPlayerScreen
import com.peihua.touchmonitor.ui.screen.function.video.VideoScreen
import com.peihua.touchmonitor.ui.screen.function.zip.ZipScreen
import com.peihua.touchmonitor.ui.screen.settings.AboutScreen
import com.peihua.touchmonitor.ui.screen.settings.SettingsScreen
import com.peihua.touchmonitor.ui.screen.settings.SystemSettingsStore
import com.peihua.touchmonitor.ui.screen.share.ShareScreen
import com.peihua.touchmonitor.ui.screen.storage.StorageScreen
import com.peihua.touchmonitor.ui.theme.AppTheme
import com.peihua.touchmonitor.utils.dLog

@SuppressLint("StaticFieldLeak")
private lateinit var appRouter: NavHostController

fun navigateTo(route: String) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(route)
}

fun navigateBack() {
    assert(::appRouter.isInitialized)
    appRouter.navigateUp()
}

fun popBackStack() {
    assert(::appRouter.isInitialized)
    appRouter.popBackStack()
}

fun navigateTo(route: String, builder: NavOptionsBuilder.() -> Unit) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(route, builder)
}

fun navigateTo(route: String, params: Pair<String, String>) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(route.replace("{${params.first}}", params.second))
}

fun navigateTo2(route: String,vararg params: Pair<String, Any>) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(route)
    appRouter.currentBackStackEntry?.savedStateHandle?.apply {
        params.forEach {
            this[it.first] = it.second
        }
    }
}
fun navigateTo(directions: NavDirections, navigatorExtras: Navigator.Extras) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(directions, navigatorExtras)
}

fun navigateTo(directions: NavDirections, navOptions: NavOptions?) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(directions, navOptions)
}

fun navigateTo(directions: NavDirections) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(directions)
}

/**
 * 返回指定的route并回调参数
 */
fun popBackStack(
    route: String,
    autoPop: Boolean = true,
    callback: (Bundle.() -> Unit)? = null,
) {
    appRouter.popBackStack(route, autoPop, callback)
}

/**
 * 回到上级页面，并回调参数
 */
fun popBackStack(
    autoPop: Boolean = true,
    callback: (SavedStateHandle.() -> Unit)? = null,
) {
    appRouter.popBackStack(autoPop, callback)
}

@get:Composable
val stackEntry: NavBackStackEntry?
    @SuppressLint("UnrememberedGetBackStackEntry")
    get() = appRouter.currentBackStackEntry

/**
 * 返回指定的route并回调参数
 */
fun NavHostController.popBackStack(
    route: String,
    autoPop: Boolean = true,
    callback: (Bundle.() -> Unit)? = null,
) {
    getBackStackEntry(route).arguments?.let {
        callback?.invoke(it)
    }
    if (autoPop) {
        popBackStack()
    }
}

/**
 * 回到上级页面，并回调参数
 */
fun NavHostController.popBackStack(
    autoPop: Boolean = true,
    callback: (SavedStateHandle.() -> Unit)? = null,
) {
    dLog { "popBackStack>>>>>>>previousBackStackEntry:${previousBackStackEntry}" }
    dLog { "popBackStack>>>>>>>previousBackStackEntry.savedStateHandle:${previousBackStackEntry?.savedStateHandle}" }
    previousBackStackEntry?.savedStateHandle?.apply {
        callback?.invoke(this)
    }
//    previousBackStackEntry?.arguments?.let {
//        callback?.invoke(it)
//    }
    if (autoPop) {
        popBackStack()
    }
}

@Composable
fun ServiceApp(modifier: Modifier = Modifier, defaultPage: AppRouter = AppRouter.Home) {
    val navController = rememberNavController()
    appRouter = navController
    val settings = remember { mutableStateOf(SystemSettings.default) }
    LaunchedEffect(settings.value) {
        SystemSettingsStore.getSystemSettingsFlow().collect {
            settings.value = it
            dLog { "SystemSettingsStore.getSystemSettingsFlow()>>>>>>>sysSettings:$it" }
            ServiceApplication.updateLanguage(it.language)
        }
    }
    val systemUiController = rememberSystemUiController()
    systemUiController.setNavigationBarColor(Color.Black)
    AppTheme(settings.value.theme) { model, colorScheme ->
        AppNavHost(navController = navController, modifier = modifier, defaultPage)
    }
}

/**
 * 导航Host
 * 页面切换右进右出
 * @param navController 导航控制器
 * @param modifier 修饰符
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    defaultPage: AppRouter,
) {
    NavHost(
        modifier = modifier,
        navController = navController, startDestination = defaultPage.route,
        enterTransition = {
            slideIn(tween(400, easing = LinearOutSlowInEasing)) { fullSize ->
                IntOffset(fullSize.width, 0)
            }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOut(tween(700, easing = FastOutSlowInEasing)) { fullSize ->
                IntOffset(fullSize.width, 0)
            }
        }
    ) {
        composable(route = AppRouter.Home.route) {
            MainScreen(modifier)
        }
        composable(route = AppRouter.Applications.route) {
            AppScreen(modifier)
        }
        composable(route = AppRouter.LogScreen.route) {
            LogScreen(modifier)
        }
        composable(
            route = AppRouter.LogDetail.route,
            arguments = AppRouter.LogDetail.navArguments
        ) {
            val filePath = it.savedStateHandle.get<String>("filePath") ?: ""
            dLog { "LogDetailScreen>>>>>>>filePath:$filePath" }
            LogDetailScreen(modifier, filePath)
        }
        composable(route = AppRouter.AutoScroller.route) {
            ShortVideoScreen(modifier)
        }
        composable(route = AppRouter.AppManagerScreen.route) {
            MainAppExtractorScreen(modifier)
        }
        composable(
            route = AppRouter.AppDetailScreen.route,
            arguments = AppRouter.AppDetailScreen.navArguments
        ) {
            val packageName = it.arguments?.getString("packageName") ?: ""
            dLog { "AppDetailScreen>>>>>>>packageName:$packageName" }
            AppDetailScreen(modifier, packageName)
        }
        composable(route = AppRouter.ApkManagerScreen.route) {
            ApkScreen(modifier)
        }
        composable(route = AppRouter.SettingsScreen.route) {
            SettingsScreen(modifier)
        }
        composable(route = AppRouter.PictureScreen.route) {
            PictureScreen(modifier)
        }
        composable(route = AppRouter.AudioScreen.route) {
            AudioScreen(modifier)
        }
        composable(route = AppRouter.VideoScreen.route) {
            VideoScreen(modifier)
        }
        composable(route = AppRouter.CollectScreen.route) {
            CollectScreen(modifier)
        }
        composable(route = AppRouter.DocumentScreen.route) {
            DocumentScreen(modifier)
        }
        composable(route = AppRouter.SearchScreen.route) {
            SearchScreen(modifier)
        }
        composable(route = AppRouter.ZipScreen.route) {
            ZipScreen(modifier)
        }
        composable(route = AppRouter.DownloadScreen.route) {
            DownloadScreen(modifier)
        }
        composable(
            route = AppRouter.VideoPlayerScreen.route,
            arguments = AppRouter.VideoPlayerScreen.navArguments
        ) {
            val videoPath = it.savedStateHandle.get<String>("videoPath") ?: ""
            dLog { "VideoPlayerScreen>>>>>>>filePath:$videoPath" }
            VideoPlayerScreen(modifier, videoPath)
        }
        composable(
            route = AppRouter.PhotoPreviewScreen.route,
            arguments = AppRouter.PhotoPreviewScreen.navArguments
        ) {
            val photoPath = it.savedStateHandle.get<String>("photoPath") ?: ""
            dLog { "PhotoPreviewScreen>>>>>>>filePath:$photoPath" }
            PhotoPreviewScreen(modifier, photoPath)
        }
        composable(route = AppRouter.AudioPlayerScreen.route) {
            val audioPath = it.savedStateHandle.get<String>("audioPath") ?: ""
            dLog { "PhotoPreviewScreen>>>>>>>filePath:$audioPath" }
            AudioPlayerScreen(modifier, audioPath)
        }
        composable(
            route = AppRouter.StorageScreen.route,
            arguments = AppRouter.LogDetail.navArguments
        ) {
            it.savedStateHandle.apply {
                val title = get<String>("title") ?: ""
                val path = get<String>("path") ?: ""
                dLog { "AppDetailScreen>>>>>>>title:$title,path:$path" }
                StorageScreen(modifier, title, path)
            }
        }
        composable(route = AppRouter.AboutScreen.route) {
            AboutScreen(modifier)
        }
        composable(route = AppRouter.ScreenDeadPixelsScreen.route) {
            ScreenDeadPixelsScreen(modifier)
        }
        composable(route = AppRouter.ScreenTimeScreen.route) {
            ScreenTimeScreen(modifier)
        }
        composable(route = AppRouter.QrCodeGeneratorScreen.route) {
            QrCodeGeneratorScreen(modifier)
        }
        composable(route = AppRouter.PhotoWatermarkScreen.route) {
            PhotoWatermarkScreen(modifier)
        }
        composable(route = AppRouter.VideoToGifScreen.route) {
            VideoToGifScreen(modifier)
        }
        composable(route = AppRouter.GifImageDecompositionScreen.route) {
            GifImageDecompositionScreen(modifier)
        }
        composable(route = AppRouter.ImagePixelizationScreen.route) {
            ImagePixelizationScreen(modifier)
        }
        composable(route = AppRouter.PhotoToSketchScreen.route) {
            PhotoToSketchScreen(modifier)
        }
        composable(route = AppRouter.PhotoToBlackAndWhiteScreen.route) {
            PhotoToBlackAndWhiteScreen(modifier)
        }
        composable(route = AppRouter.DayNewsScreen.route) {
            DayNewsScreen(modifier)
        }
        dialog(route = Dialog.ShareDialog.route) {
            val filePath = it.savedStateHandle.get<String>(Dialog.ShareDialog.KEY_FILE_PATH) ?: ""
            dLog { "ShareScreen>>>>>>>filePath:$filePath" }
            ShareScreen(modifier.background(Color.Transparent), filePath)
        }
        dialog(route = Dialog.DeviceInfoScreen.route) {
            DeviceInfoScreen(modifier.background(Color.Transparent))
        }
        dialog(route = Dialog.MessageDialog.route) {
            val title = it.savedStateHandle.get<String>(Dialog.MessageDialog.KEY_TITLE) ?: ""
            val content = it.savedStateHandle.get<String>(Dialog.MessageDialog.KEY_MESSAGE) ?: ""
            val onPositive = it.savedStateHandle.get<Pair<String, () -> Unit>>(Dialog.MessageDialog.KEY_ON_POSITIVE) 
            val onNegative = it.savedStateHandle.get<Pair<String, () -> Unit>>(Dialog.MessageDialog.KEY_ON_NEGATIVE)
            MessageDialog(modifier.background(Color.Transparent), title, content, onPositive, onNegative)
        }
    }
}