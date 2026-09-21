# AndroidBase — AI 开发指南

> 本文件是给 AI 编程助手（Claude Code、Codex、Cursor 等）的项目上下文。
> 开始在本仓库写代码之前，请先完整阅读本文件。

## 项目概览

基于 **Kotlin + Jetpack Compose + Clean Architecture + MVVM** 的可运行 Android 基础工程。
单模块（`:app`），定位为新业务项目的起点模板：已打通 UI / 状态 / DI / 网络 / 缓存 / 后台任务的全链路样例。

## 技术栈（版本以 `gradle/libs.versions.toml` 为唯一事实源）

- Kotlin **2.1.0**、AGP **8.8.2**、KSP
- Jetpack Compose（BOM **2025.02.00**）+ Material 3 + Navigation Compose
- Hilt **2.56.2**（依赖注入，KSP 编译）
- Retrofit **2.11.0** + OkHttp **4.12.0** + kotlinx.serialization（**不是** Gson/Moshi）
- Room **2.6.1** + DataStore Preferences
- Coil **3.1.0**（Compose 图片加载）
- WorkManager + Hilt Worker（后台任务）
- Timber（日志，不直接使用 `android.util.Log`）
- `minSdk 24` / `targetSdk 35` / `compileSdk 35`，JVM toolchain **17**

## 目录结构（`app/src/main/java/com/androidbase/`）

```
com/androidbase/
├── core/       # network、database、datastore、ui、worker、common（跨层基础设施）
├── data/       # remote DTO、local Entity/DAO、Repository 实现（仅此层可访问网络/数据库）
├── domain/     # model、Repository 接口、UseCase（纯 Kotlin，不依赖 Android 框架）
└── feature/    # 页面 Screen 与 ViewModel（按业务 feature 分包，如 home）
```

## 构建 / 测试命令

```bash
./gradlew :app:assembleDebug          # 编译 Debug
./gradlew :app:installDebug           # 安装到设备
./gradlew testDebugUnitTest           # 跑单元测试
```
- 打开项目需本地 Android SDK，路径在 `local.properties`（不入库）。
- 全部依赖版本集中在 `gradle/libs.versions.toml`（Version Catalog），**禁止**在 `build.gradle.kts` 写裸版本号。

## 架构约束（新增代码必须遵守）

1. **依赖方向**：`feature → domain ← data`；`feature`/`data` 可依赖 `core`；`domain` 不依赖任何 Android 框架。
2. **UI 状态**：ViewModel 持有不可变 `UiState`（`StateFlow`），UI 侧用 `collectAsStateWithLifecycle` 收集；禁止 UI 直接持有可变状态。
3. **网络统一封装**：接口返回统一 `ApiResult<T>`（成功/错误/异常）；Repository 层负责 `remote → Room 缓存 → domain model`，网络失败时回退本地缓存。
4. **依赖注入**：一律用 Hilt。ViewModel 用 `@HiltViewModel`，Worker 用 `@HiltWorker`，模块用 `@Module`/`@InstallIn`。
5. **图片**：统一走 `core/ui/AppImage`（封装 Coil），不在业务层直接写 `AsyncImage`。
6. **日志**：统一 Timber；按 debug/release 初始化。
7. **序列化**：kotlinx.serialization（`@Serializable`），不用 Gson/Moshi。
8. **KSP**：Room、Hilt 均用 KSP（不是 kapt）。

## 关键实现要点与坑

- **Token 注入**：`AuthInterceptor` 从 DataStore 读 token，自动加 `Authorization: Bearer <token>`。
- **刷新 Token**：目前是业务占位。需在 OkHttp `Authenticator` 或专门的 `TokenRefreshManager` 实现串行刷新；刷新失败后 `UserPreferences.clear()` 并导航到登录页。
- **环境切换**：`API_BASE_URL` 由 `app/build.gradle.kts` 的 `buildConfigField` 注入（当前为 jsonplaceholder 示例地址）。接入真实后端时改这里，或按 debug/staging/release 分环境。
- **Debug 变体**：`applicationId` 带 `.debug` 后缀，`versionName` 带 `-debug`。
- **首页样例**：请求 `https://jsonplaceholder.typicode.com/posts`，经 Room 缓存回退；图片来自 Picsum。

## 测试

- 目前仅 **JUnit4** 单元测试（`app/src/test`，含基础 `ApiResult` 用例）。
- 新增领域逻辑 / 仓库逻辑时**优先补单元测试**；UI 测试与 androidTest 尚未接入。

## 变更纪律

- 提交信息遵循 Conventional Commits（`feat:` / `fix:` / `refactor:` / `docs:` / `test:`）。
- 大改动先写实现计划（见 `.dhcoder/skills/writing-plans`），再动代码。
- 需求级改动：先 PRD → 技术 Spec（见 `.dhcoder/skills/prd-to-spec`），再计划，再实现。
