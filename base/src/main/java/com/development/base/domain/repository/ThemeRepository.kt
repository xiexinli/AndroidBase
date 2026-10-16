package com.development.base.domain.repository

import com.development.base.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** 主题偏好读写。 */
interface ThemeRepository {
    /** 当前主题模式，未设置时为 [ThemeMode.Default]。 */
    val themeMode: Flow<ThemeMode>

    /** 持久化主题模式。 */
    suspend fun setThemeMode(mode: ThemeMode)
}
