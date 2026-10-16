package com.development.demo.ui.hilt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.development.base.core.network.DeviceInfo
import com.development.base.core.ui.AndroidBaseTheme
import com.development.demo.ui.hilt.di.HiltDemoEntryPoint
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject
import javax.inject.Provider

/**
 * Hilt 用法示例的入口 Activity。
 *
 * 入口在 demo 首页（[com.development.demo.ui.main.MainActivity]）的目录里，不单独注册 LAUNCHER。
 *
 * 本页把 Hilt 的主要使用场景都跑一遍：
 * 字段注入、`@Singleton` 与无作用域对比、`@Binds`、`@Provides`、
 * `Lazy` / `Provider`、`@AssistedFactory`、`@EntryPoint`；
 * ViewModel 侧的场景（限定符、多绑定等）由 [HiltDemoViewModel] 承担。
 */
@AndroidEntryPoint
class HiltActivity : ComponentActivity() {

    /** 同一个 `@Singleton` 类型注入两次，用于验证「应用级只有一个实例」。 */
    @Inject internal lateinit var deviceInfoA: DeviceInfo

    @Inject internal lateinit var deviceInfoB: DeviceInfo

    /** 由 `@Binds` 提供的接口实现。 */
    @Inject internal lateinit var greeter: Greeter

    /** 由 `@Provides` 提供、且自身还依赖另一个绑定的对象。 */
    @Inject internal lateinit var formatter: DemoFormatter

    /** 无作用域类型注入两次：应当是两个不同实例。 */
    @Inject internal lateinit var unscopedA: UnscopedToken

    @Inject internal lateinit var unscopedB: UnscopedToken

    /** 延迟注入：第一次取值才创建，之后复用。 */
    @Inject internal lateinit var lazyToken: Lazy<UnscopedToken>

    /** 每次取值都重新解析依赖图。 */
    @Inject internal lateinit var tokenProvider: Provider<UnscopedToken>

    /** `@AssistedFactory` 由 Hilt 自动绑定，无需模块。 */
    @Inject internal lateinit var taskFactory: HiltDemoTaskFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注入发生在 super.onCreate() 之后，这里访问注入字段是安全的。
        val evidence = collectEvidence()
        setContent {
            AndroidBaseTheme {
                Surface {
                    HiltShowcaseScreen(activityEvidence = evidence, onBack = { finish() })
                }
            }
        }
    }

    private fun collectEvidence(): List<HiltEvidence> {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            HiltDemoEntryPoint::class.java,
        )
        val lazyFirst = lazyToken.get()
        val lazySecond = lazyToken.get()
        return listOf(
            HiltEvidence(
                "@AndroidEntryPoint 字段注入",
                "DeviceInfo.userAgent() = " + deviceInfoA.userAgent(),
            ),
            HiltEvidence(
                "@Singleton（应用级）",
                "两处注入同一实例 = " + (deviceInfoA === deviceInfoB),
            ),
            HiltEvidence(
                "@Binds 接口 → 实现",
                greeter.greet(),
            ),
            HiltEvidence(
                "@Provides 提供对象",
                formatter.format("Activity"),
            ),
            HiltEvidence(
                "无作用域（每次新建）",
                "两处注入同一实例 = " + (unscopedA === unscopedB),
            ),
            HiltEvidence(
                "Lazy 延迟注入",
                "两次 get() 同一实例 = " + (lazyFirst === lazySecond),
            ),
            HiltEvidence(
                "Provider 每次解析",
                "两次 get() 同一实例 = " + (tokenProvider.get() === tokenProvider.get()),
            ),
            HiltEvidence(
                "@AssistedFactory 运行期传参",
                taskFactory.create("Activity 任务").describe(),
            ),
            HiltEvidence(
                "@EntryPoint 手动取依赖",
                "与字段注入同一实例 = " + (entryPoint.deviceInfo() === deviceInfoA),
            ),
        )
    }
}
