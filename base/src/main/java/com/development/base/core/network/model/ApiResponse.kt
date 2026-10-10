package com.development.base.core.network.model

import com.development.base.core.common.ApiResult
import kotlinx.serialization.Serializable

/**
 * 统一接口响应信封（参考 AndroidBuyer 的 DHResultDto）。
 *
 * 后端约定：所有业务接口外层统一返回 `state`（业务码）/ `message`（提示）/ `serverTime`（服务端时间）/ `data`（业务数据）。
 * 拦截器与仓库层据此判定业务成功与失败，避免每个接口各自判断。
 */
@Serializable
data class ApiResponse<T>(
    val state: String? = null,
    val message: String? = null,
    val serverTime: Long = 0L,
    val data: T? = null
) {
    /** 业务是否成功，成功码见 [BizCode.SUCCESS] */
    val isSuccess: Boolean get() = state == BizCode.SUCCESS
}

/**
 * 业务错误码（参考 AndroidBuyer 的 ErrorCode，按需增删）。
 * 业务码为字符串，与后端协议保持一致即可。
 */
object BizCode {
    const val SUCCESS = "0x0000"          // 成功
    const val INVALID_SESSION = "0x0002"  // SessionKey 失效，需要重新登录
    const val AUTH_FAILED = "0x0009"      // 身份验证失败
    const val SERVER_ERROR = "0x0013"     // 服务端异常
    const val SIGN_INVALID = "0x0016"     // 签名无效
    const val PARSE_ERROR = "0x0106"      // 数据解析异常
    const val NETWORK_ERROR = "0x1111"    // 网络异常
    const val UNKNOWN = "0x9999"          // 未知异常
}

/**
 * 将统一响应体映射为 [ApiResult]。
 * 业务成功但 [ApiResponse.data] 为空时视为错误，避免上层对 null 的重复判空。
 */
fun <T> ApiResponse<T>.toApiResult(): ApiResult<T> {
    if (!isSuccess) {
        return ApiResult.Error(message ?: "请求失败，请稍后重试", bizCode = state)
    }
    val body = data ?: return ApiResult.Error("服务器返回数据为空", bizCode = state)
    return ApiResult.Success(body)
}

/** 在 [toApiResult] 基础上附带 DTO → 领域模型的映射。 */
fun <T, R> ApiResponse<T>.toApiResult(transform: (T) -> R): ApiResult<R> =
    when (val result = toApiResult()) {
        is ApiResult.Success -> ApiResult.Success(transform(result.data))
        is ApiResult.Error -> result
    }
