package com.androidbase.core.network

import com.androidbase.BuildConfig
import com.androidbase.core.network.interceptor.TimeoutInterceptor

/**
 * 网络域名与超时配置（参考 AndroidBuyer 的 HttpConfig）。
 *
 * 接入真实后端时，在此集中维护多域名映射与按接口定制的超时规则；
 * 接口侧只写 `@Headers("url:key")`，无需在业务代码里拼接完整 URL。
 */
object HostConfig {

    /** 默认域名，来自 `app/build.gradle.kts` 的 `buildConfigField("String", "API_BASE_URL", ...)`。 */
    val defaultBaseUrl: String = BuildConfig.API_BASE_URL

    /**
     * 动态域名映射：key 与 `@Headers("url:key")` 对应（见 [interceptor.DynamicHostInterceptor]）。
     * 按业务补充，例如：
     * ```
     * "upload" to "https://upload.example.com/",
     * "login"  to "https://login.example.com/",
     * ```
     */
    val hosts: Map<String, String> = mapOf(
        "default" to defaultBaseUrl
    )

    /**
     * 按 URL 片段定制的超时规则，命中即覆盖 OkHttpClient 的默认超时。示例：
     * ```
     * TimeoutRule("upload", connectSeconds = 30, readSeconds = 60),
     * ```
     */
    val timeoutRules: List<TimeoutInterceptor.TimeoutRule> = emptyList()
}
