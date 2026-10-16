package com.development.base.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore("theme_preferences")

/** 主题偏好的本地存取；独立于登录态 DataStore，避免登出时被一并清除。 */
@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    /** 已保存的主题模式名称，未设置时为 null。 */
    val themeMode: Flow<String?> = context.themeDataStore.data.map { it[themeModeKey] }

    suspend fun saveThemeMode(mode: String) {
        context.themeDataStore.edit { it[themeModeKey] = mode }
    }
}
