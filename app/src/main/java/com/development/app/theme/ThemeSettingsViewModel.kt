package com.development.app.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.development.base.domain.model.ThemeMode
import com.development.base.domain.usecase.ThemeSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用主题状态：读取并写入持久化的 [ThemeMode]。
 *
 * 初始值取自 [AppThemeState] 的同步快照，而不是等偏好回调，避免深色模式下首帧按浅色渲染、
 * 打开页面时闪一下浅色；主题壳与设置页面都复用本 ViewModel。
 */
@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val themeSettings: ThemeSettingsUseCase,
    appThemeState: AppThemeState
) : ViewModel() {
    private val _themeMode = MutableStateFlow(appThemeState.themeMode)

    /** 当前主题模式。 */
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch { themeSettings.themeMode.collect { _themeMode.value = it } }
    }

    /** 保存主题模式。 */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeSettings.setThemeMode(mode) }
    }
}
