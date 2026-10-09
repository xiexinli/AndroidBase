# AndroidBase 技术栈

> 本文件是 AndroidBase 的技术栈速览，供 AI 助手在改代码前读取。
> 版本以 `gradle/libs.versions.toml`（Version Catalog）为唯一事实源。

## 语言与构建

- Kotlin **2.1.0**，AGP **8.8.2**，KSP **2.1.0-1.0.29**。
- Gradle **Kotlin DSL**（`build.gradle.kts` / `settings.gradle.kts`），单模块 `:app`。
- `compileSdk 35` / `targetSdk 35` / `minSdk 24`，JVM toolchain **17**。
- 依赖版本集中在 `gradle/libs.versions.toml`，**禁止**在 `build.gradle.kts` 写裸版本号。

## UI

- **纯 Jetpack Compose + Material 3**（无 XML 布局、无 ViewBinding/DataBinding）。
- Compose BOM `2025.02.00`，Navigation Compose，`hiltViewModel()`，`collectAsStateWithLifecycle()`。
- 图片统一走 `core/ui/AppImage`（封装 Coil 3），不在业务层直接写 `AsyncImage`。
- 主题在 `core/ui/Theme.kt`。

## 网络

- Retrofit **2.11.0** + OkHttp **4.12.0** + **kotlinx.serialization**（**不是** Gson/Moshi）。
- 统一封装位于 `core/network`：`ApiResult`、`ApiResponse` 信封、`apiEnvelopeCall`、`NetworkExceptionEngine`、公共头/鉴权/超时/动态域名拦截器、`NetworkMonitor`。
- 仅 `data` 层可直接访问网络；UI/ViewModel 不得直接触达 Retrofit。

## 数据与存储

- Room **2.6.1**（KSP 编译），实体/DAO 在 `data/local`。
- DataStore Preferences **1.1.2**，Token 等偏好存于 `core/datastore/UserPreferences`。
- 后台任务：WorkManager **2.10.0** + Hilt Worker（`core/worker`）。

## 依赖注入与日志

- **Hilt 2.56.2**（KSP 编译）：`@HiltViewModel`、`@HiltWorker`、`@Module`/`@InstallIn`。
- 日志统一 **Timber 5.0.1**，不使用 `android.util.Log`。

## 测试

- JUnit4 **4.13.2** 单元测试（`app/src/test`）。
- 尚未接入 UI 测试 / androidTest。
