package com.development.base.data.repository

import com.development.base.core.datastore.ThemePreferences
import com.development.base.domain.model.ThemeMode
import com.development.base.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(private val preferences: ThemePreferences) : ThemeRepository {
    override val themeMode: Flow<ThemeMode> = preferences.themeMode.map { ThemeMode.fromName(it) }

    override suspend fun setThemeMode(mode: ThemeMode) = preferences.saveThemeMode(mode.name)
}
