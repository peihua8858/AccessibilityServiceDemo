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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.navigation.compose.rememberNavController
import com.peihua.touchmonitor.ui.applications.AppScreen
import com.peihua.touchmonitor.ui.logcat.LogDetailScreen
import com.peihua.touchmonitor.ui.logcat.LogScreen
import com.peihua.touchmonitor.ui.screen.function.apkkit.AppExtractorScreen
import com.peihua.touchmonitor.ui.screen.function.autoScroller.ShortVideoScreen
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

fun navigateTo2(route: String, params: Pair<String, String>) {
    assert(::appRouter.isInitialized)
    appRouter.navigate(route)
    appRouter.currentBackStackEntry?.savedStateHandle?.apply {
        this[params.first] = params.second
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
    AppTheme { model, colorScheme ->
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
        composable(route = AppRouter.AppExtractorScreen.route) {
            AppExtractorScreen(modifier)
        }
    }
}