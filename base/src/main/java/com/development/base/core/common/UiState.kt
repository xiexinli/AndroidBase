package com.development.base.core.common

/**
 * 通用页面状态（参考 AndroidBuyer 的 UiState）。
 * 需要 UI 展示「加载中 / 成功 / 失败」的 feature 可直接复用，避免每个 ViewModel 各写一套。
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}
