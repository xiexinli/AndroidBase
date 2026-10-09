# AndroidBase Project Facts

Read this file first when using `android-native-dev` in this repository.

## Build System

- Build files are **Kotlin DSL**:
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `app/build.gradle.kts`
- Gradle wrapper is already present in the repository.
- This is a runnable template project, not an empty scaffold: a working Compose + Hilt + Retrofit + Room + Coil + WorkManager sample is already wired end to end.

## Module Structure

```
AndroidBase/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml   # 依赖版本的唯一事实源（Version Catalog）
└── app/                        # 唯一模块（单模块工程）
```

Single module `:app`. Do not invent extra Gradle modules unless the user explicitly asks.

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
- `API_BASE_URL` is injected via `buildConfigField` in `app/build.gradle.kts`.
- `buildFeatures { compose = true; buildConfig = true }`.

## UI Stack

- **Pure Jetpack Compose + Material 3** — no XML layouts, no ViewBinding/DataBinding.
- Navigation Compose + `hiltViewModel()` for screen-scoped ViewModels.
- `collectAsStateWithLifecycle()` for state collection.

## Architecture

Clean Architecture single module, three layers under `app/src/main/java/com/androidbase/`:

```
com/androidbase/
├── core/     # network、database、datastore、ui、worker、common（跨层基础设施）
├── data/     # remote DTO、local Entity/DAO、Repository 实现（仅此层可访问网络/数据库）
├── domain/   # model、Repository 接口、UseCase（纯 Kotlin，不依赖 Android 框架）
└── feature/  # 页面 Screen 与 ViewModel（按业务 feature 分包）
```

Dependency direction: `feature → domain ← data`; `feature`/`data` may depend on `core`; `domain` must not depend on any Android framework.

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
./gradlew :app:compileDebugKotlin
```

Use broader proof only when needed:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Repository-Specific Gotchas

- There are no flavors here, so `compileDebugKotlin` / `assembleDebug` / `testDebugUnitTest` are unambiguous — use them directly.
- `assemble*` needs a valid Android SDK path in `local.properties` (not committed).
- Dependency resolution errors usually mean a missing/incorrect entry in `gradle/libs.versions.toml`, not a build-script typo.
- Adding a dependency requires editing the Version Catalog and the `dependencies { }` block — both.
