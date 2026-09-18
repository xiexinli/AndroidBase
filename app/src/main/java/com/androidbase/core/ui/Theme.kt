package com.androidbase.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(primary = Color(0xFF3859E9), secondary = Color(0xFF5F5B71), tertiary = Color(0xFF006C4C))
@Composable fun AndroidBaseTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = Colors, content = content) }
