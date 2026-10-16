package com.development.app.theme

import com.development.base.core.common.IoDispatcher
import com.development.base.domain.model.ThemeMode
import com.development.base.domain.usecase.ThemeSettingsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题模式的同步快照。
 *
 * Compose 里的主题要等首帧之后才生效，赶不上「窗口创建前」和「首帧之前」这两个时间点；
 * 而这两处都需要同步拿到当前主题（设置启动窗口背景、给首帧播种），因此单独维护一份。
 *
 * 值来自持久化偏好，进程启动后由后台协程持续更新；偏好尚未读出时为 [ThemeMode.Default]。
 */
@Singleton
class AppThemeState @Inject constructor(
    themeSettings: ThemeSettingsUseCase,
    @IoDispatcher dispatcher: CoroutineDispatcher
) {
    /** 当前主题模式；可在任意线程同步读取。 */
    @Volatile
    var themeMode: ThemeMode = ThemeMode.Default
        private set

    init {
        CoroutineScope(SupervisorJob() + dispatcher).launch {
            themeSettings.themeMode.collect { themeMode = it }
        }
    }
}
