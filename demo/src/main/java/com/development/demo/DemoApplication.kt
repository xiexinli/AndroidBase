package com.development.demo

import com.development.base.BaseApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * demo 的应用入口。
 *
 * `@HiltAndroidApp` 必须定义在 application 模块，因此这里只承接注解；
 * Timber 等公共初始化由 `:base` 的 [BaseApplication] 完成。
 */
@HiltAndroidApp
class DemoApplication : BaseApplication()
