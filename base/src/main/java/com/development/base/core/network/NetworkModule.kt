package com.development.base.core.network

import com.development.base.BuildConfig
import com.development.base.core.network.interceptor.CommonHeaderInterceptor
import com.development.base.core.network.interceptor.DynamicHostInterceptor
import com.development.base.core.network.interceptor.TimeoutInterceptor
import com.development.base.data.remote.PostApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络层的 Hilt 绑定。
 *
 * 域名与超时规则来自宿主 App 提供的 [HostConfig]（见该类 KDoc），本模块只消费不定义。
 */
@Module @InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 20L
    private const val WRITE_TIMEOUT_SECONDS = 20L

    @Provides @Singleton fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides @Singleton fun commonHeaderInterceptor(deviceInfo: DeviceInfo): CommonHeaderInterceptor =
        CommonHeaderInterceptor(deviceInfo)

    @Provides @Singleton fun dynamicHostInterceptor(hostConfig: HostConfig): DynamicHostInterceptor =
        DynamicHostInterceptor(hostConfig.hosts)

    @Provides @Singleton fun timeoutInterceptor(hostConfig: HostConfig): TimeoutInterceptor =
        TimeoutInterceptor(hostConfig.timeoutRules)

    @Provides @Singleton fun client(
        auth: AuthInterceptor,
        commonHeader: CommonHeaderInterceptor,
        dynamicHost: DynamicHostInterceptor,
        timeout: TimeoutInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(timeout)
        .addInterceptor(dynamicHost)
        .addInterceptor(commonHeader)
        .addInterceptor(auth)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides @Singleton fun retrofit(client: OkHttpClient, json: Json, hostConfig: HostConfig): Retrofit =
        Retrofit.Builder()
            .baseUrl(hostConfig.defaultBaseUrl).client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()

    @Provides @Singleton fun postApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)
}
