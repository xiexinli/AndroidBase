package com.development.demo.ui.hilt.di

import com.development.demo.ui.hilt.BoundSetFeature
import com.development.demo.ui.hilt.DemoFeature
import com.development.demo.ui.hilt.Greeter
import com.development.demo.ui.hilt.InjectConstructorGreeter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * `@Binds` 场景的 Hilt 模块。
 *
 * `@Binds` 只能写在 `abstract class` 模块里：方法必须是 `abstract`，
 * 参数是「实现」，返回值是「接口」，实现类自身必须可被注入。
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class HiltDemoBindingModule {

    /** 接口 → 实现的绑定。 */
    @Binds
    abstract fun bindGreeter(impl: InjectConstructorGreeter): Greeter

    /** `@Binds` 也可以参与多绑定：把元素直接汇入 `Set<DemoFeature>`。 */
    @Binds
    @IntoSet
    abstract fun bindSetFeature(impl: BoundSetFeature): DemoFeature
}
