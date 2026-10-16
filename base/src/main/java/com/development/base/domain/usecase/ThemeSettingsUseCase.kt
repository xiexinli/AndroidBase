package com.development.base.domain.usecase

import com.development.base.domain.model.ThemeMode
import com.development.base.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** 读取与保存应用主题模式。 */
class ThemeSettingsUseCase @Inject constructor(private val repository: ThemeRepository) {
    /** 当前主题模式。 */
    val themeMode: Flow<ThemeMode> = repository.themeMode

    /** 保存主题模式。 */
    suspend fun setThemeMode(mode: ThemeMode) = repository.setThemeMode(mode)
}
