# AndroidBase Project Facts

Read this file first when using `android-native-dev` in this repository.

## Build System

- Build files are **Kotlin DSL**:
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `base/build.gradle.kts`（`:base` library）
  - `app/build.gradle.kts`（`:app` application shell）
  - `demo/build.gradle.kts`（`:demo` application shell，仅依赖 `:base`）
- Gradle wrapper is already present in the repository.
- This is a runnable template project, not an empty scaffold: a working Compose + Hilt + Retrofit + Room + Coil + WorkManager sample is already wired end to end.

## Module Structure

```
AndroidBase/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml   # 依赖版本的唯一事实源（Version Catalog）
├── base/                       # com.android.library，包名 com.development.base —— 可复用依赖库
├── app/                        # com.android.application，包名 com.development.app —— 应用壳
└── demo/                       # com.android.application，包名 com.development.demo —— 只依赖 :base 的示例应用
```

Three modules: `:base`（library，承载 core/data/domain 公共能力，含 `open class BaseApplication` 承担 Timber 等公共初始化）、`:app` 与 `:demo`（两个 application，各自承载自己的页面 UI；`*Application`（`@HiltAndroidApp`）继承 `:base` 的 `BaseApplication`，并含 `MainActivity`、res 与宿主 Hilt 配置，均通过 `implementation(project(":base"))` 依赖 `:base`，彼此不互相依赖）。Hilt 强制要求 `@HiltAndroidApp` 必须位于 application 模块，无法下沉到 library。Do not invent extra Gradle modules unless the user explicitly asks.

## Toolchain Snapshot

| Area | Value |
|------|-------|
| AGP | `8.8.2` |
| Kotlin | `2.1.0` |
| KSP | `2.1.0-1.0.29` |
| Hilt | `2.56.2` |
| Compose BOM | `2025.02.00` |
| Retrofit / OkHttp | `2.11.0` / `4.12.0` |
| kotlinx.serialization | `1.8.0` |
| Room / DataStore | `2.6.1` / `1.1.2` |
| WorkManager | `2.10.0` |
| Coil | `3.1.0` |
| Timber | `5.0.1` |
| compileSdk / targetSdk | `35` / `35` |
| minSdk | `24` |
| JVM toolchain | `17` |

All versions live in `gradle/libs.versions.toml`. **Never** hardcode a dependency version directly in a `build.gradle.kts`.

## App Build Shape

- No product flavors.
- Build types: `debug`, `release`.
- `debug` adds `applicationIdSuffix = ".debug"` and `versionNameSuffix = "-debug"`.
- `API_BASE_URL` is injected via `buildConfigField` in `app/build.gradle.kts` / `demo/build.gradle.kts`, then assembled into `HostConfig` by the host's `di/AppNetworkModule.kt` / `di/DemoNetworkModule.kt` and provided to `:base`。`:base` 不硬编码任何域名。
- `buildFeatures { compose = true; buildConfig = true }`.

## UI Stack

- **Pure Jetpack Compose + Material 3** — no XML layouts, no ViewBinding/DataBinding.
- Navigation Compose + `hiltViewModel()` for screen-scoped ViewModels.
- `collectAsStateWithLifecycle()` for state collection.

## Architecture

公共 Clean Architecture 能力 lives in the `:base` library under `base/src/main/java/com/development/base/`:

```
com/development/base/
├── core/     # network、database、datastore、ui、worker、common（跨层基础设施）
├── data/     # remote DTO、local Entity/DAO、Repository 实现（仅此层可访问网络/数据库）
└── domain/   # model、Repository 接口、UseCase（纯 Kotlin，不依赖 Android 框架）
```

`:app` 和 `:demo` 各自拥有应用 UI（包括 Screen、导航和页面 ViewModel），且不互相依赖；公共 `Application` 初始化在 `:base` 的 `BaseApplication`（`open`，无 Hilt 注解）。`:base/core/ui` 可向宿主提供 `AndroidBaseTheme`、`AppImage` 等共享 UI 能力，但不包含具体页面。

Dependency direction: app/demo presentation depends on `:base` domain/use cases; inside `:base`, data depends on domain and may depend on core; domain must not depend on Android framework. Concrete screens, navigation, page ViewModels, and page UI state must stay in their owning app. `:base/core/ui` may expose reusable theme/image components.

## Non-Negotiable Conventions

- Serialization: **kotlinx.serialization** (`@Serializable`), NOT Gson/Moshi.
- Logging: **Timber**, NOT `android.util.Log`.
- DI: **Hilt** everywhere (`@HiltViewModel`, `@HiltWorker`, `@Module`/`@InstallIn`).
- Annotation processing: **KSP**, not kapt.
- Images: go through `core/ui/AppImage`, never raw `AsyncImage` in feature code.
- Network: unified `ApiResult` + `core/network` helpers (see `agent/rules` equivalents in `.dhcoder/rules/`).

## Python Tooling

None. This repository has no `.venv` and no Python helper scripts. Do not assume one exists.

## Default Verification Commands

Use the narrowest proof for the change:

```bash
git diff --check -- <touched files...>
./gradlew :base:compileDebugKotlin      # :base 代码改动
./gradlew :app:compileDebugKotlin       # :app 壳改动
./gradlew :demo:compileDebugKotlin      # :demo 壳改动
```

Use broader proof only when needed:

```bash
./gradlew :app:assembleDebug            # 打包 :app 的 APK（含 :base）
./gradlew :demo:assembleDebug           # 打包 :demo 的 APK（含 :base）
./gradlew :base:assembleDebug           # 单独产出 :base 的 AAR
./gradlew :base:testDebugUnitTest       # 单元测试位于 :base
```

## Repository-Specific Gotchas

- There are no flavors here, so `compileDebugKotlin` / `assembleDebug` / `testDebugUnitTest` are unambiguous — prefix them with the module (`:base:`, `:app:` or `:demo:`). Core/data/domain unit tests live in `:base`; app presentation tests live in `:app` and demo presentation tests in `:demo`.
- `assemble*` needs a valid Android SDK path in `local.properties` (not committed).
- Dependency resolution errors usually mean a missing/incorrect entry in `gradle/libs.versions.toml`, not a build-script typo.
- Adding a dependency requires editing the Version Catalog and the `dependencies { }` block — both.
