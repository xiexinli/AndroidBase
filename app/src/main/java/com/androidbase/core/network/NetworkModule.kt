package com.androidbase.core.network

import com.androidbase.BuildConfig
import com.androidbase.core.network.interceptor.CommonHeaderInterceptor
import com.androidbase.core.network.interceptor.DynamicHostInterceptor
import com.androidbase.core.network.interceptor.TimeoutInterceptor
import com.androidbase.data.remote.PostApi
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

    @Provides @Singleton fun commonHeaderInterceptor(): CommonHeaderInterceptor = CommonHeaderInterceptor()

    @Provides @Singleton fun dynamicHostInterceptor(): DynamicHostInterceptor = DynamicHostInterceptor(HostConfig.hosts)

    @Provides @Singleton fun timeoutInterceptor(): TimeoutInterceptor = TimeoutInterceptor(HostConfig.timeoutRules)

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

    @Provides @Singleton fun retrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL).client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()

    @Provides @Singleton fun postApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)
}
