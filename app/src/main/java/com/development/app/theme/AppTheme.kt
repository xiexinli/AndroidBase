package com.development.app.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.development.base.core.ui.AndroidBaseTheme
import com.development.base.core.ui.appBackgroundColor
import com.development.base.domain.model.ThemeMode

/** [ThemeMode] 在当前系统深色状态下最终是否使用深色。 */
fun ThemeMode.isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.FOLLOW_SYSTEM -> systemInDarkTheme
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * 宿主应用主题：把持久化的 [ThemeMode] 解析为深/浅色后交给 [AndroidBaseTheme]。
 *
 * [ThemeMode.FOLLOW_SYSTEM] 时按系统深色设置决定；切换后同一进程内的所有页面都会随之更新。
 *
 * @param transparentWindow 页面是否为覆盖在其他页面之上的透明窗口（如 WebView 弹窗）。
 *   这种窗口不填系统栏底色，否则会在弹窗上方多出一条实色条，只同步图标明暗。
 */
@Composable
fun AppTheme(
    transparentWindow: Boolean = false,
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())
    AndroidBaseTheme(darkTheme = darkTheme) {
        SystemBarAppearance(darkTheme = darkTheme, fillWindowColor = !transparentWindow)
        content()
    }
}

/**
 * 在 `setContent` 之前调用：把窗口启动背景色对齐当前主题。
 *
 * 窗口背景在 Compose 首帧之前就会绘制，而 XML 主题固定是浅色的 `Theme.Material.Light`，
 * 深色模式下打开页面会先闪一下浅色；这里提前换成主题背景色。
 * 透明窗口（如 WebView 弹窗）不要调用，否则会挡住弹窗后面的内容。
 */
fun ComponentActivity.applyThemeWindowBackground(themeMode: ThemeMode) {
    val dark = themeMode.isDark(isSystemInDarkMode())
    window.setBackgroundDrawable(ColorDrawable(appBackgroundColor(dark).toArgb()))
}

/** 当前系统是否处于深色模式。 */
private fun ComponentActivity.isSystemInDarkMode(): Boolean {
    val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}

/**
 * 让系统栏跟随应用主题：底色取当前配色，图标明暗随深浅色取反。
 *
 * targetSdk 35 起应用在 Android 15+ 被强制边到边，系统栏区域由应用内容着色，改底色既无效也不需要；
 * 更早版本的系统栏是窗口主题着色的独立色块，只切图标明暗会出现「深色应用 + 浅色系统栏」，必须同时改底色。
 */
@Suppress("DEPRECATION")
@Composable
private fun SystemBarAppearance(darkTheme: Boolean, fillWindowColor: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    val barColor = MaterialTheme.colorScheme.background.toArgb()
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        if (fillWindowColor && Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
        }
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
