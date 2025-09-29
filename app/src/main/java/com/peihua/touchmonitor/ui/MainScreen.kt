package com.peihua.touchmonitor.ui

import android.os.Bundle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.NavigationBarItem
import com.peihua.touchmonitor.ui.screen.main.AccountScreen
import com.peihua.touchmonitor.ui.screen.main.CollectScreen
import com.peihua.touchmonitor.ui.screen.main.FunctionScreen
import com.peihua.touchmonitor.ui.screen.main.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val selectItem = rememberSaveable { mutableIntStateOf(0) }
    val layoutDirection = LocalLayoutDirection.current
    val navigationBarItemColors = NavigationBarItemDefaults.colors().copy(
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary
    )
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = navigationBarItemColors,
        navigationRailItemColors = NavigationRailItemDefaults.colors(),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors()
    )

    val listener = object : OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?,
        ) {
            selectItem.intValue = when (destination.route) {
                MainRouter.Home.route -> 0
                MainRouter.Function.route -> 1
                MainRouter.Collect.route -> 2
                MainRouter.Account.route -> 3
                else -> 0
            }
        }
    }
    navController.addOnDestinationChangedListener(listener)
    DisposableEffect(navController) {
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    NavigationSuiteScaffold(
        modifier = modifier,
//        navigationSuiteType = NavigationSuiteType.ShortNavigationBarCompact,
            //NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo()),
        navigationSuiteColors = NavigationSuiteDefaults.colors(),
        navigationItems = {
            NavigationBarItem(
                selected = selectItem.intValue == 0,
                onClick = {
                    if (selectItem.intValue == 0) {
                        return@NavigationBarItem
                    }
                    selectItem.intValue = 0
                    navController.navigate(MainRouter.Home.route) {
                        popUpTo(MainRouter.Home.route) {
                            inclusive = true
                        }
                    }
                },
                painter = painterResource(id = R.drawable.ic_home),
                title = stringResource(id = R.string.text_home),
                colors = navigationBarItemColors
            )
            NavigationBarItem(
                selected = selectItem.intValue == 1, onClick = {
                    if (selectItem.intValue == 1) {
                        return@NavigationBarItem
                    }
                    selectItem.intValue = 1
                    navController.navigate(MainRouter.Function.route) {
                        popUpTo(MainRouter.Function.route) {
                            inclusive = true
                        }
                    }
                },
                painter = painterResource(id = R.drawable.ic_function_24),
                title = stringResource(id = R.string.text_function),
                colors = navigationBarItemColors
            )
            NavigationBarItem(
                selected = selectItem.intValue == 2, onClick = {
                    if (selectItem.intValue == 2) {
                        return@NavigationBarItem
                    }
                    selectItem.intValue = 2
                    navController.navigate(MainRouter.Collect.route) {
                        popUpTo(MainRouter.Collect.route) {
                            inclusive = true
                        }
                    }
                },
                painter = painterResource(id = R.drawable.ic_star_gray),
                title = stringResource(id = R.string.text_collect),
                colors = navigationBarItemColors
            )
            NavigationBarItem(
                selected = selectItem.intValue == 3, onClick = {
                    if (selectItem.intValue == 3) {
                        return@NavigationBarItem
                    }
                    selectItem.intValue = 3
                    navController.navigate(MainRouter.Account.route) {
                        popUpTo(MainRouter.Account.route) {
                            inclusive = true
                        }
                    }
                },
                painter = painterResource(id = R.drawable.ic_me_gray),
                title = stringResource(id = R.string.text_account),
                colors = navigationBarItemColors
            )
        }) {
        Box(modifier = Modifier.fillMaxSize()) {
            MainNavHost(navController, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * 导航Host
 * 页面切换右进右出
 * @param navController 导航控制器
 * @param modifier 修饰符
 */
@Composable
private fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController, startDestination = MainRouter.Home.route,
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
        composable(route = MainRouter.Home.route) {
            HomeScreen(modifier)
        }
        composable(route = MainRouter.Function.route) {
            FunctionScreen(modifier)
        }
        composable(route = MainRouter.Collect.route) {
            CollectScreen(modifier)
        }
        composable(
            route = MainRouter.Account.route,
        ) {
            AccountScreen(modifier)
        }
    }
}

private sealed class MainRouter(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList(),
) {
    /**
     * 首页
     */
    data object Home : MainRouter("home")

    /**
     * 功能
     */
    data object Function : MainRouter("function")

    /**
     * 收藏
     */
    data object Collect : MainRouter("collect")

    data object Account : MainRouter("account")
}