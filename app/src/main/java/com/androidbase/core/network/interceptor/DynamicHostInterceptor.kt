package com.androidbase.core.network.interceptor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 动态域名切换（参考 AndroidBuyer 的 DHUrlInterceptor）。
 *
 * 接口通过 `@Headers("url:key")` 指定域名 key，本拦截器在请求发出前将其替换为 [hosts] 中对应的域名，
 * 并移除该临时头。未指定 key（或 key 未命中）时按 Retrofit 的 baseUrl 原样请求。
 *
 * 用法：
 * ```
 * @Headers("url:upload")
 * @POST("uploadfile")
 * suspend fun upload(...): ApiResponse<UploadDto>
 * ```
 */
class DynamicHostInterceptor(
    private val hosts: Map<String, String>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val key = request.header(HEADER_KEY) ?: return chain.proceed(request)

        val baseUrl = hosts[key]?.toHttpUrlOrNull()
            ?: return chain.proceed(request.newBuilder().removeHeader(HEADER_KEY).build())

        val newUrl = request.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        return chain.proceed(
            request.newBuilder()
                .removeHeader(HEADER_KEY)
                .url(newUrl)
                .build()
        )
    }

    companion object {
        /** 与 `@Headers` 中使用的 key 前缀保持一致 */
        const val HEADER_KEY = "url"

        /** 构造 `@Headers` 中使用的值，例如 `headerValue("upload")` → `url:upload` */
        fun headerValue(key: String): String = "$HEADER_KEY:$key"
    }
}

/** 便于按域名 key 取出 baseUrl 字符串（供 @Headers 使用）。 */
fun Map<String, String>.baseUrlOf(key: String): HttpUrl? = this[key]?.toHttpUrlOrNull()
