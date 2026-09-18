package com.androidbase.core.common

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val code: Int? = null, val cause: Throwable? = null) : ApiResult<Nothing>
}

inline fun <T> apiCall(block: () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (error: retrofit2.HttpException) {
    ApiResult.Error(error.message(), error.code(), error)
} catch (error: Exception) {
    ApiResult.Error(error.message ?: "网络请求失败，请稍后重试", cause = error)
}
