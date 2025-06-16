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
import androidx.navigation.NavDirections
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.peihua.touchmonitor.ui.applications.AppScreen
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
    callback: (Bundle.() -> Unit)? = null,
) {
    previousBackStackEntry?.arguments?.let {
        callback?.invoke(it)
    }
    if (autoPop) {
        popBackStack()
    }
}

@Composable
fun ServiceApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    appRouter = navController
    AppTheme {
        AppNavHost(navController = navController, modifier = modifier)
    }
}

/**
 * 导航Host
 * 页面切换右进右出
 * @param navController 导航控制器
 * @param modifier 修饰符
 */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController, startDestination = AppRouter.Home.route,
        enterTransition = {
            slideIn(tween(700, easing = LinearOutSlowInEasing)) { fullSize ->
                IntOffset(fullSize.width, 0)
            }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(1500))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500))
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
    }
}