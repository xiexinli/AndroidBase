# Android Verification Task Map

## Default Variant

This repository has **no product flavors**. It has two modules — `:base` (`com.android.library`, package `com.development.base`) and `:app` (`com.android.application` shell, package `com.development.app`) — and the build types are `debug` / `release`.

So the plain `debug` task names are unambiguous — prefix them with the module you touched:

| Module | Tasks |
|--------|-------|
| `:base` | `:base:compileDebugKotlin`, `:base:assembleDebug`（AAR）, `:base:testDebugUnitTest` |
| `:app` | `:app:compileDebugKotlin`, `:app:assembleDebug`（APK）, `:app:testDebugUnitTest` |

Unit tests live in `:base` (`base/src/test/java/com/development/base/`).

## Common Commands

### Fast diff sanity

```bash
git diff --check -- <touched files...>
```

Use for:

- whitespace errors
- merge-marker mistakes
- malformed patch artifacts

### Library Kotlin / Compose change (`:base`)

```bash
./gradlew :base:compileDebugKotlin
```

Use for:

- Kotlin changes in `base/src/main/java`
- Compose screen / theme changes
- resource changes under `base/src/main/res` (if any)

Notes:

- This is the default compile proof for `:base` changes.
- It is fast and does not run lint or packaging.

### App shell change (`:app`)

```bash
./gradlew :app:compileDebugKotlin
```

Use for:

- Kotlin changes in `app/src/main/java`（`AppApplication`、`MainActivity`、`di/AppNetworkModule`）
- manifest / resource changes under `app/src/main/`

Notes:

- A `:app` change almost always pulls in `:base` compilation as well.

### Unit tests

```bash
./gradlew :base:testDebugUnitTest
```

Use for:

- changed unit tests
- business / domain logic with unit coverage

### Broader packaging proof

```bash
./gradlew :app:assembleDebug      # APK（含 :base）
./gradlew :base:assembleDebug     # :base 的 AAR
```

Use when:

- compile is not strong enough
- manifest, packaging, generated outputs, or integration wiring changed
- Hilt graph wiring changed (missing-binding errors surface here, not in plain compile)

Cost:

- slower than compile
- use only when needed

## Common Failure Patterns

### Missing Android SDK

Symptom:

- Gradle fails because `local.properties` has no valid `sdk.dir`

Fix:

- set `sdk.dir` in `local.properties` (not committed) or open the project in Android Studio

### Dependency resolution error

Symptom:

- unresolved dependency / version conflict

Fix:

- check `gradle/libs.versions.toml`; add or fix the catalog entry, then reference it in `dependencies { }`

### Missing Hilt binding after a `:base` API change

Symptom:

- `[Dagger/MissingBinding]` for a type that `:base` expects the host to provide (e.g. `HostConfig`)

Fix:

- provide it from the host app, e.g. `app/src/main/java/com/development/app/di/AppNetworkModule.kt`

### `@HiltAndroidApp` placed in a library module

Symptom:

- `:base:kspDebugKotlin` fails with
  `[Hilt] Application class ... annotated with @HiltAndroidApp must be defined in a Gradle android application module`

Fix:

- keep the Hilt entry point (`@HiltAndroidApp`) in `:app`; put shared initialization in `:base` as an `open class BaseApplication : Application()` and let `:app` extend it (`@HiltAndroidApp class AppApplication : BaseApplication()`)

### Sandbox blocks Gradle lock files

Symptom:

- `~/.gradle/wrapper/...zip.lck (Operation not permitted)`

Fix:

- rerun the same command with escalated permissions

## Decision Heuristics

- Start narrow.
- Pick the module you touched: `:base` for infrastructure/business code, `:app` for the shell.
- Escalate command scope only when the diff demands stronger proof.
- Keep compile verification and visual/manual verification separate in the final report.
