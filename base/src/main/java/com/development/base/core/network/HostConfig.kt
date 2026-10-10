package com.development.base.core.network

import com.development.base.core.network.interceptor.TimeoutInterceptor

/** `hosts` 中默认域名使用的 key。 */
const val HOST_KEY_DEFAULT = "default"

/**
 * 网络域名与超时配置。
 *
 * `:base` 库**不硬编码任何业务域名**，由宿主 App 在自己的 Hilt 模块中提供本类实例，例如：
 * ```
 * @Module @InstallIn(SingletonComponent::class)
 * object AppNetworkModule {
 *     @Provides @Singleton
 *     fun hostConfig() = HostConfig(defaultBaseUrl = BuildConfig.API_BASE_URL)
 * }
 * ```
 * 未提供时 Hilt 会在编译期报「missing binding」，从而避免域名漏配。
 *
 * 多域名映射与按接口定制的超时规则也集中维护在这里：
 * - [hosts]：key 与接口上的 `@Headers("url:key")` 对应，交给
 *   [com.development.base.core.network.interceptor.DynamicHostInterceptor] 切换域名。
 * - [timeoutRules]：命中 URL 片段即覆盖 OkHttpClient 的默认超时，交给
 *   [com.development.base.core.network.interceptor.TimeoutInterceptor]。
 */
data class HostConfig(
    /** 默认域名，即 Retrofit 的 baseUrl。 */
    val defaultBaseUrl: String,
    /** 动态域名映射，默认只含 [HOST_KEY_DEFAULT]。 */
    val hosts: Map<String, String> = mapOf(HOST_KEY_DEFAULT to defaultBaseUrl),
    /** 按 URL 片段定制的超时规则。 */
    val timeoutRules: List<TimeoutInterceptor.TimeoutRule> = emptyList()
)
