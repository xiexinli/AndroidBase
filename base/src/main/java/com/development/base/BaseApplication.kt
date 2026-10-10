package com.development.base

import android.app.Application
import timber.log.Timber

/**
 * 基础 Application：承载 `:base` 范围内的全局初始化（当前为 Timber 日志）。
 *
 * 为什么这里没有 `@HiltAndroidApp`：Hilt 强制要求该注解必须定义在 **application 模块**
 * （含 `com.android.application` 插件的 `build.gradle`），在 library 模块会直接编译失败：
 * `Application class ... annotated with @HiltAndroidApp must be defined in a Gradle android
 * application module`。因此宿主 App 提供一个一行子类承接 Hilt 入口即可：
 * ```
 * @HiltAndroidApp
 * class AppApplication : BaseApplication()
 * ```
 * 宿主若有额外初始化，覆盖 [onCreate] / [initTimber] 后调用 `super` 即可。
 */
open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    /** 初始化 Timber：Debug 打印日志，Release 保持静默。 */
    protected open fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
