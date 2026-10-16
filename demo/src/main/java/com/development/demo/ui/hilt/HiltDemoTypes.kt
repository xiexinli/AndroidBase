package com.development.demo.ui.hilt

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

/**
 * Hilt 演示页用到的注入类型集合。
 *
 * 这里刻意把「自有类」「接口 + 实现」「多绑定元素」「辅助注入」放在同一个文件里，
 * 便于与 [HiltShowcaseContent] 中的场景说明逐条对照；真实业务代码应按职责拆分文件。
 */

/** 接口：由 `@Binds` 绑定实现，由 [InjectConstructorGreeter] 提供实例。 */
internal interface Greeter {
    fun greet(): String
}

/**
 * `@Inject constructor` 场景：自己写的类，Hilt 可以直接构造。
 *
 * 没有任何作用域注解 ⇒ 每次注入都是一个新实例（见 [UnscopedToken]）。
 */
internal class InjectConstructorGreeter @Inject constructor() : Greeter {
    override fun greet(): String = "来自 @Inject constructor 的问候"
}

/** 无作用域（unscoped）示例：用于和 `@Singleton` 对比「两次注入是不是同一个对象」。 */
internal class UnscopedToken @Inject constructor() {
    /** 实例标识，只服务于演示。 */
    val id: String = Integer.toHexString(System.identityHashCode(this))
}

/**
 * `@Provides` 场景：没有 `@Inject` 构造函数（模拟无法改源码的第三方类），
 * 只能由 Hilt 模块里的 `@Provides` 方法创建。
 */
internal class DemoFormatter(private val prefix: String) {
    fun format(value: String): String = prefix + " -> " + value
}

/** 多绑定（`@IntoSet` / `@IntoMap`）的元素类型。 */
internal interface DemoFeature {
    val name: String
}

/** 由 `@Binds @IntoSet` 贡献到集合里的元素。 */
internal class BoundSetFeature @Inject constructor() : DemoFeature {
    override val name: String = "Set 元素 · 由 @Binds 贡献"
}

/**
 * 辅助注入（assisted injection）示例。
 *
 * [taskName] 由调用方在运行期传入（`@Assisted`），[greeter] 由 Hilt 自动注入。
 */
internal class HiltDemoTask @AssistedInject constructor(
    @Assisted private val taskName: String,
    private val greeter: Greeter,
) {
    fun describe(): String = taskName + " / " + greeter.greet()
}

/** `@AssistedFactory` 不需要写 `@Provides`，Hilt 会自动为它生成绑定。 */
@AssistedFactory
internal interface HiltDemoTaskFactory {
    fun create(taskName: String): HiltDemoTask
}
