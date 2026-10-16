package com.development.demo.ui.hilt.di

import com.development.demo.ui.hilt.DemoApiKey
import com.development.demo.ui.hilt.DemoFeature
import com.development.demo.ui.hilt.DemoFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import javax.inject.Named
import javax.inject.Singleton

/**
 * `@Provides` 场景的 Hilt 模块。
 *
 * 只服务于 `:demo` 的 Hilt 用法示例，提供的都是示例自有类型，
 * 因此安装到 `SingletonComponent` 不会与业务绑定冲突。
 */
@Module
@InstallIn(SingletonComponent::class)
internal object HiltDemoModule {

    /** `@Named` 限定符：一个 `String` 绑定。 */
    @Provides
    @Singleton
    @Named("greetingPrefix")
    fun provideGreetingPrefix(): String = "Hi(来自 @Provides)"

    /** 自定义 `@Qualifier`：另一个 `String` 绑定，靠注解与上面区分。 */
    @Provides
    @Singleton
    @DemoApiKey
    fun provideDemoApiKey(): String = "demo-key-2026"

    /** `@Provides` 带参数：参数就是它的依赖，Hilt 会先满足参数再调用本方法。 */
    @Provides
    @Singleton
    fun provideDemoFormatter(@Named("greetingPrefix") prefix: String): DemoFormatter =
        DemoFormatter(prefix)

    /** `@IntoSet` 多绑定：与 `@Binds @IntoSet` 的元素一起汇入 `Set<DemoFeature>`。 */
    @Provides
    @IntoSet
    fun provideProvidedSetFeature(): DemoFeature = object : DemoFeature {
        override val name: String = "Set 元素 · 由 @Provides 贡献"
    }

    /** `@IntoMap` + `@StringKey` 多绑定：汇入 `Map<String, DemoFeature>`。 */
    @Provides
    @IntoMap
    @StringKey("auth")
    fun provideAuthFeature(): DemoFeature = object : DemoFeature {
        override val name: String = "Map 元素 · 鉴权"
    }

    @Provides
    @IntoMap
    @StringKey("sync")
    fun provideSyncFeature(): DemoFeature = object : DemoFeature {
        override val name: String = "Map 元素 · 同步"
    }
}
