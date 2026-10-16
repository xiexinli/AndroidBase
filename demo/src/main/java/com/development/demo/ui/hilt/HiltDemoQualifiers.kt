package com.development.demo.ui.hilt

import javax.inject.Qualifier

/**
 * 自定义限定符示例。
 *
 * 当同一个类型（这里是 `String`）存在多个绑定时，用限定符区分提供端与注入端。
 *
 * `@Retention(AnnotationRetention.BINARY)` 是 Dagger 对限定符的推荐设置：
 * 编译期可见即可，不必保留到运行期反射。
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DemoApiKey
