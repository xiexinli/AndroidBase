package com.development.demo.di

import com.development.base.core.network.HostConfig
import com.development.demo.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * demo 模块提供给 `:base` 的网络配置。
 *
 * `:base` 不感知任何业务域名，默认域名由这里注入；缺了它会在
 * `:demo:hiltJavaCompileDebug` 阶段报 `Dagger/MissingBinding`。
 */
@Module
@InstallIn(SingletonComponent::class)
object DemoNetworkModule {

    @Provides
    @Singleton
    fun hostConfig(): HostConfig = HostConfig(
        defaultBaseUrl = BuildConfig.API_BASE_URL
    )
}
