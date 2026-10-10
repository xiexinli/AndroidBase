package com.development.base.core.network

import com.development.base.core.common.ApiResult
import com.development.base.core.network.model.ApiResponse
import com.development.base.core.network.model.toApiResult
import kotlinx.coroutines.CancellationException

/**
 * 统一处理「带业务信封」的请求（参考 AndroidBuyer 的 createCall）：
 * 捕获网络/解析异常并归一化，按 [ApiResponse.state] 判定业务成功与失败。
 *
 * 与 [com.development.base.core.common.apiCall] 的区别：`apiCall` 用于返回裸数据的接口，
 * 本方法用于返回 [ApiResponse] 信封的接口。
 *
 * 协程调度交由 Repository 层（`withContext(io)`）处理，此处只做异常与业务码收敛。
 */
suspend inline fun <T> apiEnvelopeCall(crossinline block: suspend () -> ApiResponse<T>): ApiResult<T> = try {
    block().toApiResult()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    NetworkExceptionEngine.handle(e).toApiResult()
}

/**
 * [apiEnvelopeCall] 的映射版本：成功后通过 [transform] 将 DTO 映射为领域模型。
 */
suspend inline fun <T, R> apiEnvelopeCall(
    crossinline block: suspend () -> ApiResponse<T>,
    noinline transform: (T) -> R
): ApiResult<R> = try {
    block().toApiResult(transform)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    NetworkExceptionEngine.handle(e).toApiResult()
}
