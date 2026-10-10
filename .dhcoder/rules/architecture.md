# AndroidBase 架构约定

## 1. 分层与依赖方向

三模块 Clean Architecture：全部业务与基础设施代码位于 `:base` 库
（`base/src/main/java/com/development/base/`），`:app` 与 `:demo` 只作为应用壳
（分别 `app/src/main/java/com/development/app/`、`demo/src/main/java/com/development/demo/`，
含 Hilt 入口 `*Application`、`MainActivity` 与宿主配置），二者互不依赖、都只依赖 `:base`。`:base` 内分四层：

- `core/`：跨层基础设施（network、database、datastore、ui、worker、common）。
- `data/`：`remote` DTO、`local` Entity/DAO、`repository` 实现。**仅此层可访问网络与数据库。**
- `domain/`：`model`、`repository` 接口、`usecase`。**纯 Kotlin，不依赖任何 Android 框架。**
- `feature/`：按业务分包（如 `home`），含 Compose Screen 与 ViewModel。

依赖方向：`feature → domain ← data`；`feature`/`data` 可依赖 `core`。**禁止** `domain` 反向依赖上层或 Android 框架。

## 2. 新增功能规范

- 新页面：在 `feature/<name>/` 下新增 `XxxScreen.kt` 与 `XxxViewModel.kt`。
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
