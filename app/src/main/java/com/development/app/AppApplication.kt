package com.development.app

import com.development.base.BaseApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * 宿主应用入口。
 *
 * Hilt 要求 `@HiltAndroidApp` 必须定义在 application 模块，无法下沉到 `:base`；
 * 因此这里只承接注解，其余初始化逻辑全部复用 `:base` 的 [BaseApplication]。
 */
@HiltAndroidApp
class AppApplication : BaseApplication()
