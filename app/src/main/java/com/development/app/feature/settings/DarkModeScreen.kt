package com.development.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.development.app.theme.ThemeSettingsViewModel
import com.development.base.domain.model.ThemeMode

/** 关闭「跟随系统」时禁用的选项透明度。 */
private const val DisabledAlpha = 0.38f

/**
 * 深色模式二级页面：顶部是「跟随系统」开关，下面是「手动选择」的浅色/深色单选。
 *
 * 跟随系统开启时手动选项禁用；关闭时回落到默认的浅色模式。
 */
@Composable
fun DarkModeRoute(
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val mode = themeMode ?: ThemeMode.Default
    val followSystem = mode == ThemeMode.FOLLOW_SYSTEM

    Scaffold(topBar = { SettingsTopBar(title = "深色模式", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FollowSystemItem(
                checked = followSystem,
                onCheckedChange = { checked ->
                    viewModel.setThemeMode(if (checked) ThemeMode.FOLLOW_SYSTEM else ThemeMode.Default)
                }
            )
            HorizontalDivider()
            SectionHeader("手动选择")
            HorizontalDivider()
            ThemeOptionItem(
                title = "浅色模式",
                selected = mode == ThemeMode.LIGHT,
                enabled = !followSystem,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
            )
            HorizontalDivider()
            ThemeOptionItem(
                title = "深色模式",
                selected = mode == ThemeMode.DARK,
                enabled = !followSystem,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun FollowSystemItem(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "跟随系统",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "开启后，应用内夜间模式状态和系统保持一致",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
private fun ThemeOptionItem(title: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else DisabledAlpha)
        )
        Spacer(Modifier.weight(1f))
        RadioButton(selected = selected, onClick = null, enabled = enabled)
    }
}
