package com.development.base.core.network.interceptor

import com.development.base.core.network.DeviceInfo
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Locale

/**
 * 统一添加公共请求头（参考 AndroidBuyer 的 DHNetworkIntercaptor）。
 *
 * 鉴权头由 [com.development.base.core.network.AuthInterceptor] 负责，这里只放与身份无关的公共头，
 * 二者职责分离，便于单独测试与替换。
 */
class CommonHeaderInterceptor(private val deviceInfo: DeviceInfo) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("X-App-Platform", "android")
            .header("X-App-Version", deviceInfo.versionName)
            .header("Accept-Language", Locale.getDefault().toLanguageTag())
            .header("User-Agent", deviceInfo.userAgent())
            .build()
        return chain.proceed(request)
    }
}
