package com.development.app.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.development.app.theme.AppTheme
import com.development.app.theme.AppThemeState
import com.development.app.theme.applyThemeWindowBackground
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 设置页面：可独立启动的 Activity，内部两级导航。
 *
 * 一级是设置列表（[SettingRoutes.List]），二级是深色模式（[SettingRoutes.DarkMode]）。
 */
@AndroidEntryPoint
class SettingActivity : ComponentActivity() {

    @Inject lateinit var appThemeState: AppThemeState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 先定窗口启动背景，避免深色模式下打开本页瞬间闪一下浅色。
        applyThemeWindowBackground(appThemeState.themeMode)
        setContent {
            AppTheme {
                Surface(Modifier.fillMaxSize()) {
                    SettingNavHost(onBack = ::finish)
                }
            }
        }
    }

    companion object {
        /** 创建设置页面的启动 Intent。 */
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, SettingActivity::class.java)
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }
}

/** 设置页面内部的两级路由。 */
private object SettingRoutes {
    const val List = "settings"
    const val DarkMode = "settings/dark_mode"
}

@Composable
private fun SettingNavHost(onBack: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = SettingRoutes.List) {
        composable(SettingRoutes.List) {
            SettingRoute(
                onDarkModeClick = { navController.navigate(SettingRoutes.DarkMode) },
                onBack = onBack
            )
        }
        composable(SettingRoutes.DarkMode) {
            DarkModeRoute(onBack = { navController.popBackStack() })
        }
    }
}

/** 设置页面两级共用的标题栏。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        }
    )
}
