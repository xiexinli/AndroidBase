package com.development.demo.ui.hilt

/** 「本页实测结果」里的一行：场景名 + 真实注入出来的结果。 */
internal data class HiltEvidence(val label: String, val value: String)

/**
 * 一个 Hilt 使用场景。
 *
 * 页面把每个场景拆成四段：一句话摘要、示例代码、注意事项、补充信息。
 * 抽成数据是为了让 [HiltShowcaseScreen] 只负责渲染，文案改动不影响 UI 逻辑。
 */
internal data class HiltScenario(
    val title: String,
    val summary: String,
    val code: String,
    val notes: List<String>,
    val extras: List<String>,
)

/** 全部场景清单，按「先入口、再绑定、再进阶」的顺序排列。 */
internal val HiltScenarios: List<HiltScenario> = listOf(
    HiltScenario(
        title = "1. @HiltAndroidApp：应用级依赖容器",
        summary = "一切 Hilt 依赖的根容器，只能标注在 application 模块的 Application 子类上。",
        code = """
            // demo/src/main/java/com/development/demo/DemoApplication.kt
            @HiltAndroidApp
            class DemoApplication : BaseApplication()
        """.trimIndent(),
        notes = listOf(
            "注解必须落在带 com.android.application 插件的模块里；写在 :base 这类 library 中会在 ksp 阶段直接报错。",
            "报错原文：Application class ... annotated with @HiltAndroidApp must be defined in a Gradle android application module。",
            "必须在 AndroidManifest 的 <application android:name=\"...\"> 里注册，否则 Hilt 完全不生效。",
            "一个应用只能有一个 @HiltAndroidApp，多写会得到重复的组件定义。",
        ),
        extras = listOf(
            "本工程的做法：公共初始化留在 :base 的 open class BaseApplication，:app / :demo 各写一行子类只承接注解。",
            "该注解会生成 SingletonComponent（应用级组件）与 Hilt_ 前缀的基类。",
            "生成代码在 build/generated/ksp/<variant>/ 下（本工程用 KSP，不用 kapt）。",
        ),
    ),
    HiltScenario(
        title = "2. @AndroidEntryPoint：组件成为注入入口",
        summary = "Activity / Fragment / Service / BroadcastReceiver / View 上加它，才能使用字段注入。",
        code = """
            @AndroidEntryPoint
            class HiltActivity : ComponentActivity() {

                @Inject lateinit var deviceInfo: DeviceInfo

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)   // 注入发生在 super.onCreate 之后
                    // 这里才能安全访问 deviceInfo
                }
            }
        """.trimIndent(),
        notes = listOf(
            "字段注入必须是 public/internal 的 lateinit var，不能是 val、不能 private，否则 Hilt 无法赋值。",
            "依赖在 super.onCreate() 之后才可用；在此之前访问会抛 UninitializedPropertyAccessException。",
            "Fragment 的宿主 Activity 必须也标 @AndroidEntryPoint，否则运行期抛 IllegalStateException。",
            "Compose 的 @Composable 函数不是 Android 组件，不能在里面写字段注入。",
        ),
        extras = listOf(
            "Hilt 会为每个被标注的类生成 Hilt_<类名> 基类，因此该 Activity 不能再继承其他自定义 Activity。",
            "支持的类型：Activity、Fragment、View、Service、BroadcastReceiver（Receiver 只在 onReceive 内可用）。",
            "本页实测：HiltActivity 用字段注入拿到了 DeviceInfo、Greeter、Lazy、Provider、@AssistedFactory。",
        ),
    ),
    HiltScenario(
        title = "3. @Inject constructor：构造注入自有类",
        summary = "自己写的、构造方式简单的类，标注构造函数后即可被 Hilt 自动创建。",
        code = """
            class InjectConstructorGreeter @Inject constructor() : Greeter {
                override fun greet(): String = "来自 @Inject constructor 的问候"
            }
        """.trimIndent(),
        notes = listOf(
            "构造函数参数本身也必须是可注入的，否则编译期报 [Dagger/MissingBinding]。",
            "一个类型只能有一个 @Inject 构造函数。",
            "不写作用域注解时，每次注入都会 new 出一个新实例。",
        ),
        extras = listOf(
            "这是最省事的注入方式：无模块、无样板代码，优先用它，不够用时才写 @Provides。",
            "本页实测：接口 Greeter 拿到的实现就是它（经 @Binds 绑定）。",
        ),
    ),
    HiltScenario(
        title = "4. @Binds：把接口绑到实现",
        summary = "在抽象模块里把接口映射到它的实现，零样板代码，是接口绑定的首选写法。",
        code = """
            @Module
            @InstallIn(SingletonComponent::class)
            abstract class HiltDemoBindingModule {

                @Binds
                abstract fun bindGreeter(impl: InjectConstructorGreeter): Greeter
            }
        """.trimIndent(),
        notes = listOf(
            "只能写在 abstract class 模块里，方法必须是 abstract，有且只有一个参数（实现）并返回接口类型。",
            "参数类型自身必须可注入：要么有 @Inject 构造函数，要么在别处被 @Provides。",
            "同一个接口只能有一份绑定，重复绑定（@Binds 与 @Provides 同时给）会编译失败。",
        ),
        extras = listOf(
            "@Binds 与 @Provides 结果等价，但生成的是抽象调用，没有额外工厂开销，更省方法数。",
            "本工程 :base 的 RepositoryModule 就用它把 PostRepositoryImpl 绑到 PostRepository。",
        ),
    ),
    HiltScenario(
        title = "5. @Provides：构造第三方 / 无法注解的对象",
        summary = "Retrofit、OkHttp、Room 等改不了源码的类，用 @Provides 方法手写创建逻辑。",
        code = """
            @Module
            @InstallIn(SingletonComponent::class)
            object HiltDemoModule {

                @Provides
                @Singleton
                @Named("greetingPrefix")
                fun provideGreetingPrefix(): String = "Hi(来自 @Provides)"

                @Provides
                @Singleton
                fun provideDemoFormatter(
                    @Named("greetingPrefix") prefix: String,
                ): DemoFormatter = DemoFormatter(prefix)
            }
        """.trimIndent(),
        notes = listOf(
            "@Provides 方法可以带参数，参数就是它的依赖，Hilt 会先满足参数再调用本方法。",
            "方法返回类型就是被绑定的类型；不能返回 Unit。",
            "要复用实例必须加作用域注解（如 @Singleton），否则每次注入都重新执行一次方法。",
        ),
        extras = listOf(
            "方法可以写在 object 模块里，也可以是 abstract class 模块里的 companion object 方法。",
            "本工程 :base 的 NetworkModule 用同样方式提供 Json、OkHttpClient、Retrofit、PostApi。",
        ),
    ),
    HiltScenario(
        title = "6. @InstallIn：模块装到哪个组件",
        summary = "决定这份绑定在哪个生命周期组件里可见、被谁复用。",
        code = """
            @Module
            @InstallIn(SingletonComponent::class)      // 应用级，全局唯一
            object HiltDemoModule { /* ... */ }

            @Module
            @InstallIn(ViewModelComponent::class)      // 跟随每个 ViewModel
            object HiltDemoViewModelModule { /* ... */ }
        """.trimIndent(),
        notes = listOf(
            "绑定只能在「所在组件及其下游组件」中被注入：SingletonComponent 的绑定可以注入 ViewModel，反过来不行。",
            "一个模块只能 @InstallIn 一个组件；想复用到多个组件要拆成多个模块。",
            "@Module 与 @InstallIn 必须成对出现，漏写会报 Module is not installed。",
        ),
        extras = listOf(
            "标准组件阶梯（父 → 子）：SingletonComponent → ActivityRetainedComponent → ViewModelComponent "
                + "→ ActivityComponent → FragmentComponent → ViewComponent / ServiceComponent。",
            "没安装到任何组件的模块，其绑定不会进入依赖图，注入时报 MissingBinding。",
        ),
    ),
    HiltScenario(
        title = "7. 作用域 @Singleton 与「无作用域」",
        summary = "有作用域的绑定在同一组件内只有一个实例；无作用域则每次注入都是新对象。",
        code = """
            @Singleton
            class DeviceInfo @Inject constructor(@ApplicationContext private val context: Context)

            class UnscopedToken @Inject constructor()   // 没有作用域注解
        """.trimIndent(),
        notes = listOf(
            "作用域写在被注入的类型上或 @Provides 方法上，两处等价；写在注入点上无效。",
            "无作用域不代表「每次都创建」：它被注入进一个 @Singleton 对象时，那份单例内部持有的实例是固定的。",
            "作用域不能跨组件：@Singleton 的类型不能依赖 @ActivityScoped 的类型（组件方向反了）。",
        ),
        extras = listOf(
            "本页实测：DeviceInfo 在两处注入拿到同一实例（true），UnscopedToken 两处拿到不同实例（false）。",
            "作用域只是「同一组件内的复用」，不是全局缓存，也不是线程同步机制。",
        ),
    ),
    HiltScenario(
        title = "8. 各组件作用域与生命周期",
        summary = "@ActivityRetainedScoped / @ActivityScoped / @ViewModelScoped / @ServiceScoped 覆盖不同存活范围。",
        code = """
            @ActivityScoped         class A @Inject constructor()   // 跟随 Activity
            @ActivityRetainedScoped class B @Inject constructor()   // 跨配置变更保留
            @ViewModelScoped        class C @Inject constructor()   // 跟随 ViewModel
            @ServiceScoped          class D @Inject constructor()   // 跟随 Service
        """.trimIndent(),
        notes = listOf(
            "@ActivityRetainedScoped 的对象在旋转屏幕后依然存活，适合「需要跨越配置变更、又不属于 ViewModel」的缓存。",
            "@ViewModelScoped 的实例在同一个 ViewModel 内共享，不同 ViewModel 各有一份。",
            "这些作用域要求同名组件里已有对应模块：@ViewModelScoped 的绑定必须在 @InstallIn(ViewModelComponent) 的模块中提供。",
        ),
        extras = listOf(
            "与 viewModelScope 的关系：ViewModel 是 ViewModelComponent 的宿主，@ViewModelScoped 对象随它一起销毁。",
            "作用域越窄、组件越短命、重建越频繁——按「真的需要共享」来选，不要一律 @Singleton。",
        ),
    ),
    HiltScenario(
        title = "9. 限定符：@Named 与自定义 @Qualifier",
        summary = "同一个类型有多种提供方式时，用限定符区分（本页的注入对象里有两个 String 绑定）。",
        code = """
            @Qualifier
            @Retention(AnnotationRetention.BINARY)
            annotation class DemoApiKey

            @Provides @DemoApiKey fun provideDemoApiKey(): String = "demo-key-2026"

            @Provides @Named("greetingPrefix") fun provideGreetingPrefix(): String = "Hi"
        """.trimIndent(),
        notes = listOf(
            "限定符必须同时写在「提供端」和「注入端」，只写一边会报 MissingBinding。",
            "自定义限定符要标 @Qualifier，并按 Dagger 官方建议保留到二进制（@Retention(BINARY)）。",
            "优先自定义限定符而不是 @Named(\"字符串\")：拼错字符串没有类型提示，自定义注解能被 IDE 与重构识别。",
        ),
        extras = listOf(
            "Hilt 预置了两个限定符：@ApplicationContext 与 @ActivityContext，用来区分两种 Context。",
            "本页实测：apiKey 与 greetingPrefix 分别来自上面两个提供方法，互不串用。",
        ),
    ),
    HiltScenario(
        title = "10. @ApplicationContext / @ActivityContext：Context 注入",
        summary = "Hilt 预置了两种 Context 绑定；构造注入 Context 时几乎总是用它们。",
        code = """
            @Singleton
            class DeviceInfo @Inject constructor(
                @ApplicationContext private val context: Context,
            )

            class ActivityScopedThing @Inject constructor(
                @ActivityContext private val activityContext: Context,
            )
        """.trimIndent(),
        notes = listOf(
            "直接注入裸 Context 会报 MissingBinding，必须加限定符。",
            "@ActivityContext 只能用在 Activity 组件及其下游；在 @Singleton 对象里注入它会直接编译失败。",
            "长寿对象（单例、Repository、Worker）只能持有 @ApplicationContext，持有 Activity 会内存泄漏。",
        ),
        extras = listOf(
            "本页的 DeviceInfo 就是 @Singleton + @ApplicationContext 的标准写法。",
            ":base 是库模块：DeviceInfo 通过 PackageManager 读取运行期宿主的 applicationId / versionName，而不是读自己的 BuildConfig。",
        ),
    ),
    HiltScenario(
        title = "11. 多绑定：@IntoSet / @IntoMap",
        summary = "把多个绑定聚合进同一个 Set 或 Map，注入端只依赖集合类型。",
        code = """
            @Provides @IntoSet fun provideSetFeature(): DemoFeature = ...

            @Binds @IntoSet abstract fun bindSetFeature(impl: BoundSetFeature): DemoFeature

            @Provides @IntoMap @StringKey("auth") fun provideAuthFeature(): DemoFeature = ...

            @Provides @IntoMap @StringKey("sync") fun provideSyncFeature(): DemoFeature = ...
        """.trimIndent(),
        notes = listOf(
            "注入端写 Set<DemoFeature> / Map<String, DemoFeature>；Map 的键类型由 @StringKey、@ClassKey、@IntKey、@LongKey 决定。",
            "集合里每个元素都是独立绑定，作用域要各自标注；给集合本身加作用域不改变元素。",
            "元素不会「顺带」注册成单类型绑定：只标了 @IntoSet 的 DemoFeature 不能被单独注入为 DemoFeature。",
            "Kotlin 里注入集合必须压掉通配符：写成 Set<@JvmSuppressWildcards DemoFeature> / "
                + "Map<String, @JvmSuppressWildcards DemoFeature>，否则会报 MissingBinding（本页踩过这个坑）。",
        ),
        extras = listOf(
            "常见用途：插件 / 规则注册表、自定义 WorkerFactory、多来源数据聚合。",
            "@Multibinds 可声明一个允许为空的集合绑定；@BindsOptionalOf 用于可选绑定（注入 Optional<T>）。",
            "本页实测：Set 有 2 个元素（分别来自 @Provides 与 @Binds），Map 有 auth / sync 两个键。",
        ),
    ),
    HiltScenario(
        title = "12. 辅助注入：@AssistedInject / @AssistedFactory",
        summary = "一部分依赖由 Hilt 提供，另一部分由调用方在运行期传参。",
        code = """
            class HiltDemoTask @AssistedInject constructor(
                @Assisted private val taskName: String,   // 运行期传入
                private val greeter: Greeter,             // Hilt 提供
            )

            @AssistedFactory
            interface HiltDemoTaskFactory {
                fun create(taskName: String): HiltDemoTask
            }
        """.trimIndent(),
        notes = listOf(
            "@Assisted 标注运行期参数，其余参数照常由 Hilt 注入；参数顺序可以任意排列。",
            "工厂接口标 @AssistedFactory，不需要写 @Provides，Hilt 会自动绑定它。",
            "工厂方法返回类型必须是那个 @AssistedInject 的类，参数要与 @Assisted 参数一一对应（类型与顺序一致）。",
        ),
        extras = listOf(
            "典型场景：Worker、需要外部参数的对象（比如带 id 的页面级任务对象）。",
            "本工程 :base 的 SyncWorker 用同一机制：@HiltWorker + @AssistedInject，context 与 params 都是 @Assisted。",
        ),
    ),
    HiltScenario(
        title = "13. @HiltViewModel 与 hiltViewModel()",
        summary = "ViewModel 构造注入的官方方式；Compose 里用 hiltViewModel() 取实例。",
        code = """
            @HiltViewModel
            class HiltDemoViewModel @Inject constructor(
                private val greeter: Greeter,
                private val features: Set<DemoFeature>,
            ) : ViewModel()

            @Composable
            fun HiltRoute(viewModel: HiltDemoViewModel = hiltViewModel()) {
                val state by viewModel.state.collectAsStateWithLifecycle()
            }
        """.trimIndent(),
        notes = listOf(
            "ViewModel 必须标 @HiltViewModel，否则 hiltViewModel() 会在运行期抛异常（宿主未实现 GeneratedComponentManager）。",
            "hiltViewModel() 来自 androidx.hilt:hilt-navigation-compose；在 Navigation Compose 中会按导航返回栈条目作用域缓存实例。",
            "ViewModel 里只依赖 UseCase / Repository，不要持有 Activity Context。",
        ),
        extras = listOf(
            "宿主模块需显式声明 hilt-navigation-compose：本工程 :demo 已声明，:base 里是 implementation、不外抛。",
            "本页实测：顶部「本页实测结果」中来自 HiltDemoViewModel 的几行，就是它构造注入的结果。",
        ),
    ),
    HiltScenario(
        title = "14. Lazy 与 Provider：延迟注入与多次获取",
        summary = "注入「取值器」而不是值本身，可以推迟创建，或在需要时反复取。",
        code = """
            @Inject lateinit var lazyToken: Lazy<UnscopedToken>          // 首次 get() 才创建，然后缓存
            @Inject lateinit var tokenProvider: Provider<UnscopedToken>  // 每次 get() 重新解析依赖图
        """.trimIndent(),
        notes = listOf(
            "Lazy<T> 只创建一次并缓存，适合构造代价大、可能用不到的依赖。",
            "Provider<T> 每次 get() 都重新解析：无作用域时是新实例，@Singleton 时仍是同一个实例。",
            "两者只改变「怎么取」，不改变绑定本身的作用域。",
        ),
        extras = listOf(
            "Dagger 生成的代码本身常以 Provider 形式持有依赖，这是它的默认性能优化。",
            "本页实测：Provider 两次 get() 不是同一实例；Lazy 两次 get() 是同一实例。",
        ),
    ),
    HiltScenario(
        title = "15. @EntryPoint 与 EntryPointAccessors：手动取依赖",
        summary = "ContentProvider、自定义 View、Application 等拿不到注入入口的地方，从容器手动取。",
        code = """
            @EntryPoint
            @InstallIn(SingletonComponent::class)
            interface HiltDemoEntryPoint {
                fun deviceInfo(): DeviceInfo
            }

            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext, HiltDemoEntryPoint::class.java,
            )
            val deviceInfo = entryPoint.deviceInfo()
        """.trimIndent(),
        notes = listOf(
            "接口里每个抽象方法就是一次查询，返回类型必须在对应组件里已有绑定。",
            "EntryPointAccessors 有三个入口：fromApplication / fromActivity / fromFragment，选错组件会运行期失败。",
            "它只是「桥」：能正常注入的地方不要用它，否则依赖关系会退化成手动查找。",
        ),
        extras = listOf(
            "典型场景：ContentProvider.onCreate（早于 Application.onCreate）、第三方 SDK 回调、自定义 WorkerFactory。",
            "本页实测：EntryPoint 取到的 DeviceInfo 与字段注入的是同一个单例。",
        ),
    ),
    HiltScenario(
        title = "16. @HiltWorker：WorkManager 集成",
        summary = "让 Worker 也能构造注入依赖。",
        code = """
            @HiltWorker
            class SyncWorker @AssistedInject constructor(
                @Assisted context: Context,
                @Assisted params: WorkerParameters,
                private val repository: PostRepository,
            ) : CoroutineWorker(context, params)
        """.trimIndent(),
        notes = listOf(
            "context 与 params 必须带 @Assisted（Worker 由 WorkManager 创建），其余依赖照常注入。",
            "必须另外提供 HiltWorkerFactory，并让 Application 实现 Configuration.Provider；否则 WorkManager 用默认工厂，"
                + "@HiltWorker 不生效甚至崩溃。",
            "本工程 :base 已有 SyncWorker，但尚未接 HiltWorkerFactory / Configuration.Provider，属于待补的集成点。",
        ),
        extras = listOf(
            "需要 androidx.hilt:hilt-work 与 ksp(androidx.hilt:hilt-compiler)：:base 已声明，但未向上暴露给宿主。",
            "若要用自定义工厂，需在 Manifest 里去掉 WorkManagerInitializer（androidx.startup），改为手动初始化。",
        ),
    ),
    HiltScenario(
        title = "17. 测试中的 Hilt：@HiltAndroidTest / @BindValue",
        summary = "在仪器化测试里替换绑定，用假实现驱动真实依赖图。",
        code = """
            @HiltAndroidTest
            @UninstallModules(HiltDemoBindingModule::class)
            class HiltDemoTest {

                @get:Rule val hiltRule = HiltAndroidRule(this)

                @BindValue @JvmField
                val greeter: Greeter = FakeGreeter()

                @Before fun setUp() { hiltRule.inject() }
            }
        """.trimIndent(),
        notes = listOf(
            "仪器化测试需要 HiltTestApplication（@Config(application = ...) 或自定义 AndroidJUnitRunner），否则测试进程里没有容器。",
            "@BindValue 是「不写模块就替换绑定」的快捷方式，只能用在测试类字段上。",
            "@UninstallModules 卸载被测模块后，必须自己补上被卸载的绑定，否则报 MissingBinding。",
        ),
        extras = listOf(
            "纯 JVM 单测（src/test）用不上 Hilt——它依赖 Android 组件；VM 逻辑手工传假依赖更轻。",
            "本项目 :base 的单元测试就是这种风格（直接构造 ApiResult / ApiResponse 做断言）。",
            "需要额外依赖 hilt-android-testing 与 ksp(hilt-compiler) 的 androidTest 配置。",
        ),
    ),
    HiltScenario(
        title = "18. 常见坑与排查清单",
        summary = "最常撞到的编译期 / 运行期错误与成因。",
        code = """
            // 编译期 [Dagger/MissingBinding] DeviceInfo cannot be provided
            //   → 没有 @Inject 构造函数、没有 @Provides，或模块忘了 @InstallIn
            // 编译期 @HiltAndroidApp must be defined in a Gradle android application module
            //   → 注解写在了 library 模块
            // 运行期 UninitializedPropertyAccessException: lateinit property ...
            //   → 在 super.onCreate() 之前访问了注入字段
        """.trimIndent(),
        notes = listOf(
            "注解处理一律用 KSP（ksp(libs.hilt.compiler)），不要用 kapt：kapt 更慢，且与 Kotlin 2.x 的兼容性更差。",
            "@Inject 的字段必须是非 private 的 lateinit var；写成 val 或 private 会静默失败或编译报错。",
            ":base 不得反向依赖宿主：需要宿主提供的东西（如域名）由 :base 定义数据类、宿主在 Hilt 模块里构造注入。",
            "Composable 里不能注入，页面依赖只能经 hiltViewModel() 或作为参数传入。",
        ),
        extras = listOf(
            "找不到绑定时的排查顺序：类型有没有 @Inject 构造函数 → 有没有 @Provides/@Binds "
                + "→ 模块有没有 @InstallIn → 组件方向对不对 → 限定符两端是否一致。",
            "读 build/generated/ksp/<variant>/ 下生成的 Hilt_ 与 _Factory 类，可以确认 Hilt 实际解析出的依赖图。",
        ),
    ),
    HiltScenario(
        title = "19. 进阶：自定义组件与可选绑定",
        summary = "标准组件不够用时，Hilt 仍允许自定义组件与可选 / 可空绑定。",
        code = """
            @DefineComponent(parent = SingletonComponent::class)
            interface UserComponent { /* ... */ }

            @Module @InstallIn(SingletonComponent::class)
            abstract class OptionalModule {
                @BindsOptionalOf abstract fun maybeLogger(impl: Logger): Logger
                @Multibinds abstract fun features(): Set<DemoFeature>
            }
        """.trimIndent(),
        notes = listOf(
            "@DefineComponent / @DefineComponent.Builder 用于自定义组件（如登录态、用户级作用域），需要手动管理生命周期。",
            "@BindsOptionalOf 让注入端写 Optional<T>，绑定不存在时也不会报错。",
            "@Multibinds 声明「可能为空」的集合绑定，避免集合为空时出现 MissingBinding。",
        ),
        extras = listOf(
            "这些是 Dagger 原生能力，Hilt 只是在其上提供了标准组件与 Android 集成，两者可以混用。",
            "在本工程这类模板项目里，先用标准组件把依赖关系理清，确有需要再上自定义组件。",
        ),
    ),
    HiltScenario(
        title = "20. Hilt 与 Dagger 的关系 / 生成代码",
        summary = "Hilt 是 Dagger 之上的约定层：固定组件、固定作用域、内置 Android 集成。",
        code = """
            // 依赖方向：你的代码 → Hilt 注解 → Dagger 注解处理器（KSP）→ 生成注入代码
            // 生成物位置：build/generated/ksp/<variant>/...
            //   Hilt_<Component>           组件实现
            //   <Type>_Factory / _ProvidesFactory  各绑定的工厂
        """.trimIndent(),
        notes = listOf(
            "所有注入都在编译期完成（无反射），因此注入点写错会在编译期暴露，而不是等到运行期崩溃。",
            "Hilt 帮你固定了组件的层级与命名，代价是不方便像纯 Dagger 那样任意定义组件。",
            "一个类型只能被一份绑定覆盖；重复绑定要么合并，要么用限定符区分。",
        ),
        extras = listOf(
            "工作顺序：先写 @Inject 构造函数 → 需要接口解耦时加 @Binds → 第三方对象用 @Provides → 用限定符/多绑定处理多态。",
            "想确认某个依赖到底由谁提供，直接搜生成代码里的 _Factory 文件名最快。",
        ),
    ),
)
