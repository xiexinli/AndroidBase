# AndroidBase

基于 **Kotlin + Jetpack Compose + Clean Architecture + MVVM** 的可运行 Android 基础工程。

采用双模块：**`:base`**（`com.android.library`，包名 `com.development.base`）承载全部基础设施与样例，可被其它业务工程直接依赖；**`:app`**（`com.android.application`，包名 `com.development.app`）是薄应用壳，只放 Hilt 入口（`AppApplication`）、`MainActivity`、资源与宿主配置。

## 已实现

- **UI / 状态**：Compose、Material 3、`ViewModel + StateFlow + UiState`；首页展示加载、成功、错误和缓存回退状态。
- **依赖注入**：Hilt，已覆盖 Application、Activity、ViewModel、网络、数据库、仓库与 Worker。
- **网络**：Retrofit + OkHttp + Kotlinx Serialization；统一 `ApiResult`、超时、Debug 请求日志、公共 Header、DataStore Token 注入。
- **网络通用能力**：统一响应信封 `ApiResponse<T>` + 业务码 `BizCode`、`apiEnvelopeCall` 请求封装、异常归一化 `NetworkExceptionEngine`、公共头/动态域名/超时三类拦截器、网络状态监听 `NetworkMonitor`、通用 `UiState`。
- **图片**：Coil Compose 统一 `AppImage` 组件。
- **本地存储**：Room（示例 Post 缓存）和 DataStore（Token）。
- **数据层**：`remote → repository → Room → domain`；网络异常时保留并展示本地缓存。
- **后台任务**：提供可注入的 `SyncWorker` 样例，可按业务使用 WorkManager 调度。
- **工程治理**：Version Catalog、Debug / Release 环境、基础单元测试、Timber 日志。

## 目录

```text
base/src/main/java/com/development/base/     # :base —— 可复用的依赖库
├── core/       # network、database、datastore、ui、worker、common
├── data/       # remote DTO、local Entity/DAO、Repository 实现
├── domain/     # model、Repository 接口、UseCase
└── feature/    # 页面与 ViewModel

app/src/main/java/com/development/app/       # :app —— 应用壳
├── AppApplication.kt                        # @HiltAndroidApp，继承 :base 的 BaseApplication
├── MainActivity.kt
└── di/AppNetworkModule.kt                   # 向 :base 提供域名等运行期配置
```

## 运行

1. 使用 Android Studio 打开项目根目录；确认 SDK 路径位于 `local.properties`。
2. 同步 Gradle 后运行 `app` 的 `debug` 变体。
3. 首页默认请求 `https://jsonplaceholder.typicode.com/posts`，并通过 Room 缓存结果；图片来自 Picsum。

## 接入真实后端

- 修改 `app/build.gradle.kts` 中的 `API_BASE_URL`，或为 `debug/staging/release` 分别定义；该值由 `app/src/main/java/com/development/app/di/AppNetworkModule.kt` 组装成 `HostConfig` 注入 `:base`（`:base` 不硬编码域名，多域名与超时规则也在 `HostConfig` 上补充）。
- 在 `data/remote` 新增 API 与 DTO；在 `data/repository` 实现业务仓库。
- 登录成功后使用 `UserPreferences.saveAccessToken(token)` 保存令牌；请求会自动携带 `Authorization: Bearer <token>`。
- 当前刷新 Token 是业务占位：请依据后端协议在 OkHttp `Authenticator` 或专门的 TokenRefreshManager 中实现串行刷新，刷新失败后 `UserPreferences.clear()` 并导航至登录页。

## 网络通用能力（`core/network`）

参考 AndroidBuyer 的网络层抽取的通用能力，已适配本工程的 Kotlinx Serialization + Hilt 技术栈：

| 能力 | 位置 | 说明 |
| --- | --- | --- |
| 统一响应信封 | `model/ApiResponse.kt` | `ApiResponse<T>`（`state`/`message`/`serverTime`/`data`）与业务码 `BizCode`，配套 `toApiResult()` 映射 |
| 统一请求封装 | `ApiCallers.kt` | `apiEnvelopeCall { ... }` 包裹带信封的请求，自动收敛异常与业务码 |
| 异常归一化 | `NetworkException.kt` | `NetworkExceptionEngine` 把 HTTP/连接/超时/解析异常转为带可读文案的 `NetworkException` |
| 公共请求头 | `interceptor/CommonHeaderInterceptor.kt` | Accept、UA、App 版本、语言等与身份无关的头 |
| 鉴权头 | `AuthInterceptor.kt` | DataStore Token → `Authorization: Bearer`，已自带则不覆盖 |
| 动态域名 | `interceptor/DynamicHostInterceptor.kt` + `HostConfig.kt` | 接口写 `@Headers("url:key")` 即切换域名 |
| 超时定制 | `interceptor/TimeoutInterceptor.kt` + `HostConfig.kt` | 按 URL 片段覆盖默认超时 |
| 网络状态 | `NetworkMonitor.kt` | `isConnected()`/`isWifi()`/`networkTypeName()` 同步查询，`isOnline` Flow 监听变化 |
| 页面状态 | `core/common/UiState.kt` | 通用 `Idle/Loading/Success/Error` |

典型用法：

```kotlin
// 1) 接口返回统一信封
@Headers("url:default")
@FormUrlEncoded
@POST("api/order/list")
suspend fun orderList(@FieldMap params: Map<String, String?>): ApiResponse<OrderListDto>

// 2) Repository 里统一封装（自动处理业务码 + 异常）
suspend fun refreshOrders(): ApiResult<List<Order>> = withContext(io) {
    apiEnvelopeCall(block = { api.orderList(params) }, transform = { it.items.map(::toDomain) })
}
```

> 多域名/超时规则集中维护在 `core/network/HostConfig.kt`（数据类），由宿主 App 在自己的 Hilt 模块中构造并提供实例；`:base` 只消费不定义域名。

## AI 助手配置（`.dhcoder/`）

参考 AndroidBuyer 的 AgentKit 抽取、并按本工程实际技术栈改写后的通用 AI 配置：

| 资产 | 说明 |
| --- | --- |
| `AGENTS.md` | 项目上下文与 AI 工作流（任务分级 L0–L3、执行闭环、输出要求、Skill 路由） |
| `.dhcoder/rules/` | `tech_stack.md`、`architecture.md`、`code-style.md` |
| `.dhcoder/skills/` | 通用工作流 skills + Android 专项 skills |
| `.dhcoder/config.yaml` | 输出语言（简体中文） |
| `REVIEW.md` | 代码评审入口 |

已内置的 skills：

- 通用工作流：`diagnose`、`tdd`、`to-prd`、`to-issues`、`triage`、`zoom-out`、`prototype`、`improve-codebase-architecture`、`planning-with-files`、`grill-me`、`grill-with-docs`、`caveman`、`app-grill-pm`、`prd-code-impact-architect`、`setup-matt-pocock-skills`
- 需求 → 交付：`prd-to-spec`、`writing-plans`
- Android 专项：`android-native-dev`（开发/排障）、`android-verify`（编译/测试/验证）、`android-review`（评审）

> 未内置的 AndroidBuyer 专有资产（cart/order/checkout/home-module review、engage-sdk、r8-analyzer、GitLab AI review）以及其 AgentKit SDD 治理框架（task-level / prd-to-app-spec / subagents / WORKFLOW.md），因与本工程业务无关或依赖其自有工具链而**未**移植。

## 后续建议

分页列表引入 Paging 3；`:base` 已按 `core/data/domain/feature` 分层，业务增长后可继续把 `feature/<name>` 拆为独立 Gradle 模块并依赖 `:base`；同时加入 Detekt/Ktlint、Crashlytics/Sentry、CI 与 UI 测试。
