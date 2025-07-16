package com.peihua.touchmonitor.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AppTopBar
import com.peihua.touchmonitor.ui.components.NavigationBar
import com.peihua.touchmonitor.ui.screen.main.AccountScreen
import com.peihua.touchmonitor.ui.screen.main.CollectScreen
import com.peihua.touchmonitor.ui.screen.main.FunctionScreen
import com.peihua.touchmonitor.ui.screen.main.HomeScreen
import com.peihua.touchmonitor.ui.theme.AppColor
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.dimensionSpResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val selectItem = remember { mutableIntStateOf(0) }
    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(title = { stringResource(id = R.string.app_name) }, navigationIcon = {})
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48))) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .background(if (selectItem.intValue == 0) colorScheme.surface else Color.White)
                        .clickable {
                            if (selectItem.intValue == 0) {
                                return@clickable
                            }
                            selectItem.intValue = 0
                            navController.navigate(MainRouter.Home.route) {
                                popUpTo(MainRouter.Home.route) {
                                    inclusive = true
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_24))
                            .align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.ic_home),
                        tint = if (selectItem.intValue == 0) colorScheme.onSurface else AppColor.color_747878,
                        contentDescription = stringResource(id = R.string.text_home)
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(id = R.string.text_home),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        color = if (selectItem.intValue == 0) colorScheme.onSurface else AppColor.color_747878,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .background(if (selectItem.intValue == 1) colorScheme.surface else Color.White)
                        .clickable {
                            if (selectItem.intValue == 1) {
                                return@clickable
                            }
                            selectItem.intValue = 1
                            navController.navigate(MainRouter.Function.route) {
                                popUpTo(MainRouter.Function.route) {
                                    inclusive = true
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_24))
                            .align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.ic_function_24),
                        tint = if (selectItem.intValue == 1) colorScheme.onSurface else AppColor.color_747878,
                        contentDescription = stringResource(id = R.string.text_function)
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(id = R.string.text_function),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        color = if (selectItem.intValue == 1) colorScheme.onSurface else AppColor.color_747878,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .background(if (selectItem.intValue == 2) colorScheme.surface else Color.White)
                        .clickable {
                            if (selectItem.intValue == 2) {
                                return@clickable
                            }
                            selectItem.intValue = 2
                            navController.navigate(MainRouter.Collect.route) {
                                popUpTo(MainRouter.Collect.route) {
                                    inclusive = true
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_24))
                            .align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.ic_star_gray),
                        tint = if (selectItem.intValue == 2) colorScheme.onSurface else AppColor.color_747878,
                        contentDescription = stringResource(id = R.string.text_collect)
                    )
                    Text(
                        text = stringResource(id = R.string.text_collect),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        color = if (selectItem.intValue == 2) colorScheme.onSurface else AppColor.color_747878,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .background(if (selectItem.intValue == 3) colorScheme.surface else Color.White)
                        .clickable {
                            if (selectItem.intValue == 3) {
                                return@clickable
                            }
                            selectItem.intValue = 3
                            navController.navigate(MainRouter.Account.route) {
                                popUpTo(MainRouter.Account.route) {
                                    inclusive = true
                                }
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.dp_24))
                            .align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.ic_me_gray),
                        tint = if (selectItem.intValue == 3) colorScheme.onSurface else AppColor.color_747878,
                        contentDescription = stringResource(id = R.string.text_account)
                    )
                    Text(
                        text = stringResource(id = R.string.text_account),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = dimensionSpResource(id = R.dimen.sp_12),
                        color = if (selectItem.intValue == 3) colorScheme.onSurface else AppColor.color_747878,
                    )
                }
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                MainContent(modifier = Modifier.fillMaxSize(), navController)
            }
        }
    )
}

@Composable
private fun MainContent(modifier: Modifier = Modifier, navController: NavHostController) {
    MainNavHost(navController, modifier)
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