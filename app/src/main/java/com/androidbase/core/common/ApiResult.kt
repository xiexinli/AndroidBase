package com.androidbase.core.common

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    /**
     * @param message 面向用户的错误提示
     * @param code HTTP 状态码（如 404/500），非网络错误时为 null
     * @param cause 原始异常，便于日志与排查
     * @param bizCode 业务错误码（见 core.network.model.BizCode），非业务错误时为 null
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val cause: Throwable? = null,
        val bizCode: String? = null
    ) : ApiResult<Nothing>
}

/**
 * 包裹返回裸数据的调用：把 HTTP 异常与运行时异常收敛为 [ApiResult.Error]。
 * 返回统一业务信封的接口请使用 `core.network.apiEnvelopeCall`。
 */
inline fun <T> apiCall(block: () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (error: retrofit2.HttpException) {
    ApiResult.Error(error.message(), error.code(), error)
} catch (error: Exception) {
    ApiResult.Error(error.message ?: "网络请求失败，请稍后重试", cause = error)
}
