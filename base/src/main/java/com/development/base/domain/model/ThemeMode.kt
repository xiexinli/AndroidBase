package com.development.base.domain.model

/** 应用主题模式。 */
enum class ThemeMode {
    /** 跟随系统深色设置。 */
    FOLLOW_SYSTEM,

    /** 始终使用浅色主题。 */
    LIGHT,

    /** 始终使用深色主题。 */
    DARK;

    companion object {
        /** 默认主题模式：不开启深色，使用浅色。 */
        val Default: ThemeMode = LIGHT

        /** 由持久化的名称解析；缺失或无法识别时回退 [Default]。 */
        fun fromName(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: Default
    }
}
