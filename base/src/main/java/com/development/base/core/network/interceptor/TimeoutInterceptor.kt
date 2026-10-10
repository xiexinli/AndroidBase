package com.development.base.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 按 URL 片段定制超时时间（参考 AndroidBuyer 的 DHTimeInterceptor）。
 *
 * 默认超时在 OkHttpClient 上统一设置；个别接口（如上传、活动领取）需要单独放宽或收紧时，
 * 通过 [rules] 配置即可，无需为每个接口单独建客户端。
 */
class TimeoutInterceptor(
    private val rules: List<TimeoutRule> = emptyList()
) : Interceptor {

    data class TimeoutRule(
        val urlContains: String,
        val connectSeconds: Int,
        val readSeconds: Int,
        val writeSeconds: Int = readSeconds
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val rule = rules.firstOrNull { request.url.toString().contains(it.urlContains) }
            ?: return chain.proceed(request)
        return chain
            .withConnectTimeout(rule.connectSeconds, TimeUnit.SECONDS)
            .withReadTimeout(rule.readSeconds, TimeUnit.SECONDS)
            .withWriteTimeout(rule.writeSeconds, TimeUnit.SECONDS)
            .proceed(request)
    }
}
