package com.development.demo.ui.hilt.di

import com.development.base.core.network.DeviceInfo
import com.development.demo.ui.hilt.DemoFormatter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * `@EntryPoint` 场景：给「拿不到注入入口」的代码手动取依赖的桥。
 *
 * 接口里的每个抽象方法就是一次查询，返回类型必须在对应组件里已有绑定。
 * 本页在 [com.development.demo.ui.hilt.HiltActivity] 中用
 * `EntryPointAccessors.fromApplication(...)` 取 [DeviceInfo]，并验证它与字段注入是同一个单例。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface HiltDemoEntryPoint {
    fun deviceInfo(): DeviceInfo

    fun demoFormatter(): DemoFormatter
}
