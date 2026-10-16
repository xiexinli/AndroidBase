package com.development.app.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.base.domain.model.ThemeMode
import com.development.base.domain.usecase.ThemeSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val StopTimeoutMillis = 5_000L

/**
 * 应用主题状态：读取并写入持久化的 [ThemeMode]。
 *
 * 主题壳与设置页面都复用本 ViewModel；多个实例订阅同一份偏好，任一处修改都会同步到全部页面。
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeSettings: ThemeSettingsUseCase
) : ViewModel() {
    /** 当前主题模式；null 表示偏好尚未读出，UI 按 [ThemeMode.Default] 处理。 */
    val themeMode: StateFlow<ThemeMode?> = themeSettings.themeMode
        .map<ThemeMode, ThemeMode?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(StopTimeoutMillis), null)

    /** 保存主题模式。 */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeSettings.setThemeMode(mode) }
    }
}
