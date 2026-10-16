package com.development.demo.ui.composables

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.development.base.core.ui.AndroidBaseTheme
import com.development.demo.ui.list.ListActivity

/** 目录页路由。 */
private const val CatalogRoute = "composables/catalog"

/** 二级页面路由参数名。 */
private const val CategoryIdArg = "categoryId"

/** 二级页面路由模板。 */
private const val CategoryRoute = "composables/category/{$CategoryIdArg}"

/**
 * Compose 主要 Composable 用法示例的**入口 Activity**。
 *
 * 内部用一个 [NavHost] 组织两级页面：
 * 1. [ComposablesCatalogScreen]：目录，列出所有分类，并带有分页列表示例的入口；
 * 2. [ComposablesCategoryScreen]：某个分类的用法示例与易错点实例。
 *
 * 分页列表示例是完整页面，放在独立的 [ListActivity]，这里只负责跳转。
 * 本 Activity 只是纯 UI 展示、不注入任何依赖，因此不需要 `@AndroidEntryPoint`。
 */
class ComposablesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidBaseTheme {
                Surface { ComposablesShowcase(onOpenListDemo = { openListDemo() }) }
            }
        }
    }

    private fun openListDemo() {
        startActivity(Intent(this, ListActivity::class.java))
    }
}

@Composable
private fun ComposablesShowcase(onOpenListDemo: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CatalogRoute) {
        composable(CatalogRoute) {
            ComposablesCatalogScreen(
                onOpenCategory = { categoryId -> navController.navigate("composables/category/$categoryId") },
                onOpenListDemo = onOpenListDemo,
            )
        }
        composable(
            route = CategoryRoute,
            arguments = listOf(navArgument(CategoryIdArg) { type = NavType.StringType }),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString(CategoryIdArg)
            ComposablesCategoryScreen(
                category = ComposablesCategories.firstOrNull { it.id == categoryId },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
