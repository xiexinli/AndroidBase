# AndroidBase

基于 **Kotlin + Jetpack Compose + Clean Architecture + MVVM** 的可运行 Android 基础工程。

## 已实现

- **UI / 状态**：Compose、Material 3、`ViewModel + StateFlow + UiState`；首页展示加载、成功、错误和缓存回退状态。
- **依赖注入**：Hilt，已覆盖 Application、Activity、ViewModel、网络、数据库、仓库与 Worker。
- **网络**：Retrofit + OkHttp + Kotlinx Serialization；统一 `ApiResult`、超时、Debug 请求日志、公共 Header、DataStore Token 注入。
- **图片**：Coil Compose 统一 `AppImage` 组件。
- **本地存储**：Room（示例 Post 缓存）和 DataStore（Token）。
- **数据层**：`remote → repository → Room → domain`；网络异常时保留并展示本地缓存。
- **后台任务**：提供可注入的 `SyncWorker` 样例，可按业务使用 WorkManager 调度。
- **工程治理**：Version Catalog、Debug / Release 环境、基础单元测试、Timber 日志。

## 目录

```text
app/src/main/java/com/androidbase/
├── core/       # network、database、datastore、ui、worker、common
├── data/       # remote DTO、local Entity/DAO、Repository 实现
├── domain/     # model、Repository 接口、UseCase
└── feature/    # 页面与 ViewModel
```

## 运行

1. 使用 Android Studio 打开项目根目录；确认 SDK 路径位于 `local.properties`。
2. 同步 Gradle 后运行 `app` 的 `debug` 变体。
3. 首页默认请求 `https://jsonplaceholder.typicode.com/posts`，并通过 Room 缓存结果；图片来自 Picsum。

## 接入真实后端

- 修改 `app/build.gradle.kts` 中的 `API_BASE_URL`，或为 `debug/staging/release` 分别定义。
- 在 `data/remote` 新增 API 与 DTO；在 `data/repository` 实现业务仓库。
- 登录成功后使用 `UserPreferences.saveAccessToken(token)` 保存令牌；请求会自动携带 `Authorization: Bearer <token>`。
- 当前刷新 Token 是业务占位：请依据后端协议在 OkHttp `Authenticator` 或专门的 TokenRefreshManager 中实现串行刷新，刷新失败后 `UserPreferences.clear()` 并导航至登录页。

## 后续建议

分页列表引入 Paging 3；业务增长后按 `core/data/domain/feature` 拆为 Gradle 模块，并加入 Detekt/Ktlint、Crashlytics/Sentry、CI 与 UI 测试。
