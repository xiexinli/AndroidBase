package com.development.app.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.development.base.core.ui.AndroidBaseTheme
import com.development.base.domain.model.ThemeMode

/**
 * 宿主应用主题：把持久化的 [ThemeMode] 解析为深/浅色后交给 [AndroidBaseTheme]。
 *
 * [ThemeMode.FOLLOW_SYSTEM] 时按系统深色设置决定；切换后同一进程内的所有页面都会随之更新。
 */
@Composable
fun AppTheme(
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val darkTheme = when (themeMode ?: ThemeMode.Default) {
        ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    SystemBarAppearance(darkTheme)
    AndroidBaseTheme(darkTheme = darkTheme, content = content)
}

/** 同步系统栏图标明暗：深色主题下改用浅色图标，避免与背景同色看不清。 */
@Composable
private fun SystemBarAppearance(darkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
