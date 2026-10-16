package com.development.demo.ui.hilt

import androidx.lifecycle.ViewModel
import com.development.base.core.common.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Hilt 用法示例的 ViewModel。
 *
 * 它一次性把「构造注入 + 限定符 + 多绑定 + 辅助注入 + Provider」这几类场景都注入进来，
 * 并在初始化时把每个场景的**真实结果**整理成 [HiltEvidence]，由页面顶部统一展示。
 *
 * 本类不访问网络与数据库，示例只关心「依赖是怎么被 Hilt 组装出来的」。
 */
@HiltViewModel
internal class HiltDemoViewModel @Inject constructor(
    private val greeter: Greeter,
    @DemoApiKey private val apiKey: String,
    @Named("greetingPrefix") private val greetingPrefix: String,
    private val formatter: DemoFormatter,
    // 多绑定集合在 Kotlin 里会被编译成 `Set<? extends DemoFeature>` / `Map<String, ? extends DemoFeature>`，
    // 与 Dagger 生成的 `Set<DemoFeature>` 对不上，必须在类型参数上压掉通配符。
    private val features: Set<@JvmSuppressWildcards DemoFeature>,
    private val featureMap: Map<String, @JvmSuppressWildcards DemoFeature>,
    private val taskFactory: HiltDemoTaskFactory,
    private val unscopedProvider: Provider<UnscopedToken>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _evidence = MutableStateFlow<List<HiltEvidence>>(emptyList())

    /** 本 ViewModel 注入到的依赖的实测结果。 */
    val evidence: StateFlow<List<HiltEvidence>> = _evidence.asStateFlow()

    init {
        _evidence.value = buildEvidence()
    }

    private fun buildEvidence(): List<HiltEvidence> {
        val task = taskFactory.create("演示任务")
        val tokenA = unscopedProvider.get()
        val tokenB = unscopedProvider.get()
        return listOf(
            HiltEvidence(
                "@HiltViewModel 构造注入",
                "Greeter = " + greeter.greet(),
            ),
            HiltEvidence(
                "自定义限定符 @DemoApiKey",
                "apiKey = " + apiKey,
            ),
            HiltEvidence(
                "@Named(\"greetingPrefix\")",
                "prefix = " + greetingPrefix,
            ),
            HiltEvidence(
                "@Provides 带依赖参数",
                formatter.format("DemoFormatter"),
            ),
            HiltEvidence(
                "@IntoSet 多绑定",
                features.joinToString { it.name },
            ),
            HiltEvidence(
                "@IntoMap @StringKey 多绑定",
                featureMap.entries.joinToString { it.key + "=" + it.value.name },
            ),
            HiltEvidence(
                "@AssistedInject 辅助注入",
                task.describe(),
            ),
            HiltEvidence(
                "Provider 多次获取（无作用域）",
                "两次 get() 是同一实例 = " + (tokenA === tokenB),
            ),
            HiltEvidence(
                "@IoDispatcher 限定符（来自 :base）",
                "就是 Dispatchers.IO = " + (ioDispatcher === Dispatchers.IO),
            ),
        )
    }
}
