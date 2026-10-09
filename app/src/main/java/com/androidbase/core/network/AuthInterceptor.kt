package com.androidbase.core.network

import com.androidbase.core.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 鉴权拦截器：为请求注入 `Authorization: Bearer <token>`。
 *
 * 与身份无关的公共头（Accept/UA/版本等）由
 * [com.androidbase.core.network.interceptor.CommonHeaderInterceptor] 负责，二者职责分离。
 * 若请求已显式携带 Authorization（如第三方接口），则不覆盖。
 */
class AuthInterceptor @Inject constructor(
    private val preferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(HEADER_AUTHORIZATION) != null) return chain.proceed(request)

        val token = runBlocking { preferences.accessToken.first() }
        if (token.isBlank()) return chain.proceed(request)

        return chain.proceed(
            request.newBuilder()
                .header(HEADER_AUTHORIZATION, "Bearer $token")
                .build()
        )
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
    }
}
