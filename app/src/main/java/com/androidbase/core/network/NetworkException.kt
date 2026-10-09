package com.androidbase.core.network

import com.androidbase.core.common.ApiResult
import com.androidbase.core.network.model.BizCode
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 统一的网络/业务异常，携带业务码、HTTP 状态码与原始异常。
 */
class NetworkException(
    val bizCode: String,
    override val message: String,
    val httpCode: Int? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 异常归一化引擎（参考 AndroidBuyer 的 DHExceptionEngine）。
 * 将 OkHttp / Retrofit / 序列化 / IO 等各类异常收敛为带可读文案的 [NetworkException]，
 * 上层无需再各自 try-catch 判断异常类型。
 */
object NetworkExceptionEngine {

    fun handle(throwable: Throwable): NetworkException = when (throwable) {
        is NetworkException -> throwable
        is HttpException -> NetworkException(
            bizCode = BizCode.SERVER_ERROR,
            message = throwable.message(),
            httpCode = throwable.code(),
            cause = throwable
        )
        is UnknownHostException, is ConnectException -> NetworkException(
            bizCode = BizCode.NETWORK_ERROR,
            message = "网络连接失败，请检查网络设置",
            cause = throwable
        )
        is SocketTimeoutException -> NetworkException(
            bizCode = BizCode.NETWORK_ERROR,
            message = "网络请求超时，请稍后重试",
            cause = throwable
        )
        is SerializationException -> NetworkException(
            bizCode = BizCode.PARSE_ERROR,
            message = "数据解析失败",
            cause = throwable
        )
        is IOException -> NetworkException(
            bizCode = BizCode.NETWORK_ERROR,
            message = "网络异常，请稍后重试",
            cause = throwable
        )
        else -> NetworkException(
            bizCode = BizCode.UNKNOWN,
            message = throwable.message ?: "未知错误",
            cause = throwable
        )
    }
}

/** 将异常转换为统一的失败结果。 */
fun NetworkException.toApiResult(): ApiResult.Error = ApiResult.Error(message, httpCode, this, bizCode)
