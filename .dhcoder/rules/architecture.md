# AndroidBase 架构约定

## 1. 分层与依赖方向

三模块 Clean Architecture：可复用基础设施、数据和领域代码位于 `:base`（`base/src/main/java/com/development/base/`）；主应用 UI 位于 `:app`（`app/src/main/java/com/development/app/`），`:demo` 的演示 UI 位于 `demo/src/main/java/com/development/demo/`。`:app` 与 `:demo` 互不依赖，均只依赖 `:base`。`:base` 内分三类：

- `core/`：跨层基础设施（network、database、datastore、ui、worker、common）。
- `data/`：`remote` DTO、`local` Entity/DAO、`repository` 实现。**仅此层可访问网络与数据库。**
- `domain/`：`model`、`repository` 接口、`usecase`。**纯 Kotlin，不依赖任何 Android 框架。**

宿主页面层依赖 `:base` 的 domain/usecase；`:base` 内为 `data → domain`，data 可依赖 core。**禁止** domain 反向依赖上层或 Android 框架。具体 Compose Screen、导航图、页面 ViewModel 和页面 UI state 必须位于宿主模块；`:base/core/ui` 可提供主题、图片等通用 UI 能力。

## 2. 新增功能规范

- 主应用新页面：在 `app/src/main/java/com/development/app/feature/<name>/` 下新增 `XxxScreen.kt` 与 `XxxViewModel.kt`；demo 页面只放在 `demo`。
- ViewModel 必须 `@HiltViewModel`，构造注入 UseCase/Repository。
- 数据获取走 `domain/usecase`，不在 ViewModel 里直接调 Retrofit 或 DAO。
- 需要在 `domain/repository` 定义接口，在 `data/repository` 实现，并通过 Hilt `@Binds` 绑定。

## 3. UI 状态

- ViewModel 持有**不可变** `UiState`（`StateFlow`），UI 用 `collectAsStateWithLifecycle()` 收集。
- 禁止 UI 直接持有可变状态；一次性事件用 `SharedFlow`/`Channel`。
- 通用状态可复用 `core/common/UiState`（`Idle/Loading/Success/Error`）。

## 4. 网络封装

- 统一返回 `ApiResult<T>`（成功/错误/异常）。
- 带业务信封的接口用 `ApiResponse<T>` + `apiEnvelopeCall`；裸数据接口用 `apiCall`。
- 异常统一由 `NetworkExceptionEngine` 归一化，`ApiResult.Error.bizCode` 携带业务码。
- 超时/域名规则集中维护在 `core/network/HostConfig.kt`（数据类，由宿主 App 提供实例）。

## 5. 目录与命名

- 包名全小写：`:base` 为 `com.development.base.<层>.<模块>`，`:app` / `:demo` 为 `com.development.<模块名>.<模块>`。
- `:base` 不得反向依赖任何宿主模块；`:app` 与 `:demo` 之间也不得互相依赖。宿主需要提供的运行期配置（如域名）由 `:base` 定义数据类、宿主在 Hilt 模块中构造注入。
- 一个文件一个主要职责；跨层共享的常量放 `core`。
- 资源命名下划线风格；Compose 中避免硬编码字符串与颜色，优先主题令牌。

## 6. 禁止事项

- 绕过统一架构自建并行网络/存储路径。
- 在生产代码中混入 mock、临时调试入口或测试桩。
- 修改生成文件或依赖锁文件（除非用户明确要求）。
