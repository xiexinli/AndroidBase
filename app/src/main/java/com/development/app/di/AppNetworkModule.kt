package com.development.app.di

import com.development.app.BuildConfig
import com.development.base.core.network.HostConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 宿主 App 提供给 `:base` 的网络配置。
 *
 * `:base` 不感知任何业务域名，默认域名由这里注入；接入真实后端时改
 * `app/build.gradle.kts` 的 `API_BASE_URL`，或在 [hostConfig] 里按 buildType 分支。
 * 多域名与超时规则也在 [HostConfig] 上按需补充。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Provides
    @Singleton
    fun hostConfig(): HostConfig = HostConfig(
        defaultBaseUrl = BuildConfig.API_BASE_URL
    )
}
