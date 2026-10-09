# Android Verification Task Map

## Default Variant

This repository has **no product flavors**. The app module is `:app` and the build types are `debug` / `release`.

So the plain `debug` task names are unambiguous — use them directly:

- `:app:compileDebugKotlin`
- `:app:assembleDebug`
- `:app:testDebugUnitTest`

## Common Commands

### Fast diff sanity

```bash
git diff --check -- <touched files...>
```

Use for:

- whitespace errors
- merge-marker mistakes
- malformed patch artifacts

### App Kotlin or Compose change

```bash
./gradlew :app:compileDebugKotlin
```

Use for:

- Kotlin changes in `app/src/main/java`
- Compose screen / theme changes
- resource changes under `app/src/main/res`

Notes:

- This is the default compile proof for app changes.
- It is fast and does not run lint or packaging.

### App unit tests

```bash
./gradlew :app:testDebugUnitTest
```

Use for:

- changed unit tests
- business / domain logic with unit coverage

### Broader packaging proof

```bash
./gradlew :app:assembleDebug
```

Use when:

- compile is not strong enough
- manifest, packaging, generated outputs, or integration wiring changed

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

### Sandbox blocks Gradle lock files

Symptom:

- `~/.gradle/wrapper/...zip.lck (Operation not permitted)`

Fix:

- rerun the same command with escalated permissions

## Decision Heuristics

- Start narrow.
- Escalate command scope only when the diff demands stronger proof.
- Keep compile verification and visual/manual verification separate in the final report.
