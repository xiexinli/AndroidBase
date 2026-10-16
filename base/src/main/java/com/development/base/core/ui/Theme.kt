package com.development.base.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3859E9),
    secondary = Color(0xFF5F5B71),
    tertiary = Color(0xFF006C4C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB3C4FF),
    secondary = Color(0xFFC8C3DC),
    tertiary = Color(0xFF55DCB0)
)

/** 应用统一主题；[darkTheme] 为 true 时使用深色配色。 */
@Composable
fun AndroidBaseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}

/**
 * 主题背景色。
 *
 * 供拿不到 Compose 主题的地方取用（如 Activity 建窗时设置启动窗口背景色），
 * 保证与 [AndroidBaseTheme] 的 `colorScheme.background` 同源，不在业务代码里另写一份色值。
 */
fun appBackgroundColor(darkTheme: Boolean): Color =
    (if (darkTheme) DarkColors else LightColors).background
