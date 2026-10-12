package com.development.app.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.development.app.feature.account.AccountRoute
import com.development.app.feature.home.HomeRoute
import com.development.app.feature.home.PostDetailDestination
import com.development.app.feature.home.PostDetailRoute
import com.development.app.feature.im.ImRoute

/** 底部导航的顶层页面。 */
private enum class MainTab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Filled.Home),
    Im("im", "IM", Icons.Filled.Email),
    Account("account", "Account", Icons.Filled.Person)
}

/**
 * 应用主页：底部三个 Tab（Home / IM / Account）加页面栈。
 *
 * Tab 之间切换用 `saveState` / `restoreState` 保留各自的状态（如列表滚动位置）；
 * 进入详情页等非顶层页面时隐藏底部导航栏。
 */
@Composable
fun MainRoute() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = MainTab.entries.firstOrNull { it.route == backStackEntry?.destination?.route }

    Scaffold(bottomBar = { selectedTab?.let { MainBottomBar(it) { tab -> navController.switchTab(tab) } } }) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.Home.route) {
                HomeRoute(onPostClick = { postId -> navController.navigate(PostDetailDestination.route(postId)) })
            }
            composable(MainTab.Im.route) { ImRoute() }
            composable(MainTab.Account.route) { AccountRoute() }
            composable(
                route = PostDetailDestination.Route,
                arguments = listOf(navArgument(PostDetailDestination.PostIdArg) { type = NavType.IntType })
            ) {
                PostDetailRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun MainBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

/** 切换 Tab：同一 Tab 不重复入栈，并恢复它上次的页面状态。 */
private fun NavHostController.switchTab(tab: MainTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
