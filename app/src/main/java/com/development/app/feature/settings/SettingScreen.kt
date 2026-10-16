package com.development.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

/** 设置列表：当前只有「深色模式」一项，右侧摘要展示其状态。 */
@Composable
fun SettingRoute(
    onDarkModeClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Scaffold(topBar = { SettingsTopBar(title = "设置", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SettingItem(
                title = "深色模式",
                summary = themeMode.summary(),
                onClick = onDarkModeClick
            )
            HorizontalDivider()
        }
    }
}

/** 主题模式在设置列表里的摘要文案。 */
private fun ThemeMode.summary(): String = when (this) {
    ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
    ThemeMode.LIGHT -> "关闭"
    ThemeMode.DARK -> "开启"
}

@Composable
private fun SettingItem(title: String, summary: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
