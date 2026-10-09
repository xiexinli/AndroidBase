---
name: android-native-dev
description: Android native development guide tailored for this repository. Covers this project's Kotlin DSL Gradle single-module structure, AGP/Kotlin/SDK versions, pure Jetpack Compose plus Hilt setup, and Android build or verification workflows. Use before AndroidBase Android development or troubleshooting.
---

# Android Native Dev

Use this as the Android development guide for this repository.

## 1. Repository First

Read `references/project-facts.md` before applying the generic guidance below.

When generic Android guidance conflicts with repository facts, follow the repository facts.

This repository is a runnable single-module template app:

- It already has a Gradle wrapper and a complete Android project structure.
- It uses Kotlin DSL Gradle files (`build.gradle.kts`), not Groovy.
- It is a single-module app (`:app`); there are no extra library modules.
- It has no product flavors, so build against `debug` / `release` directly.
- It uses pure Jetpack Compose + Material 3 UI (no XML layouts, no dataBinding/viewBinding).
- Dependency versions live in `gradle/libs.versions.toml` (Version Catalog).

### 1.1 Default Verification Path

Use the narrowest command that proves the change:

- Fast compile proof: `./gradlew :app:compileDebugKotlin`
- Packaging proof: `./gradlew :app:assembleDebug`
- Unit test proof: `./gradlew :app:testDebugUnitTest`
- There are no flavors, so the plain `debug` task names are unambiguous

### 1.2 Project-Specific Gotchas

- There are no flavors; `compileDebugKotlin` / `assembleDebug` / `testDebugUnitTest` are unambiguous.
- `assemble*` requires a valid Android SDK path in `local.properties` (not committed).
- Adding a dependency means editing BOTH `gradle/libs.versions.toml` and the `dependencies { }` block.
- There is no Detekt/Ktlint wiring yet, so compile output does not carry lint noise.
- If Gradle cache locks under `~/.gradle` are blocked by sandbox permissions, rerun the same command with escalation rather than changing tasks.

## 2. Project Snapshot

Use this snapshot as the default mental model for this repository:

| Area | Repository Fact |
|------|------------------|
| Build files | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts` |
| Dependency versions | `gradle/libs.versions.toml` (Version Catalog) |
| AGP | `8.8.2` |
| Kotlin | `2.1.0` (android + compose + serialization plugins) |
| KSP / Hilt | `2.1.0-1.0.29` / `2.56.2` |
| SDK levels | `compileSdk=35`, `targetSdk=35`, `minSdk=24` |
| JVM toolchain | `17` |
| Modules | single `:app` |
| Flavors | none |
| Build types | `debug`, `release` |
| UI stack | Kotlin, pure Jetpack Compose + Material 3, Hilt ViewModels |
| Key libraries | Retrofit `2.11.0`, OkHttp `4.12.0`, kotlinx.serialization `1.8.0`, Room `2.6.1`, DataStore `1.1.2`, WorkManager `2.10.0`, Coil `3.1.0`, Timber `5.0.1` |

Generic Kotlin, Compose, design, accessibility, and testing guidance remains below as reusable reference material.

## 3. Kotlin Development Standards

### 3.1 Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Class/Interface | PascalCase | `UserRepository`, `MainActivity` |
| Function/Variable | camelCase | `getUserName()`, `isLoading` |
| Constant | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Package | lowercase | `com.example.myapp` |
| Composable | PascalCase | `@Composable fun UserCard()` |

### 3.2 Code Standards (Important)

**Null Safety**:
```kotlin
// ❌ Avoid: Non-null assertion !! (may crash)
val name = user!!.name

// ✅ Recommended: Safe call + default value
val name = user?.name ?: "Unknown"

// ✅ Recommended: let handling
user?.let { processUser(it) }
```

**Exception Handling**:
```kotlin
// ❌ Avoid: Random try-catch in business layer swallowing exceptions
fun loadData() {
    try {
        val data = api.fetch()
    } catch (e: Exception) {
        // Swallowing exception, hard to debug
    }
}

// ✅ Recommended: Let exceptions propagate, handle at appropriate layer
suspend fun loadData(): Result<Data> {
    return try {
        Result.success(api.fetch())
    } catch (e: Exception) {
        Result.failure(e)  // Wrap and return, let caller decide handling
    }
}

// ✅ Recommended: Unified handling in ViewModel
viewModelScope.launch {
    runCatching { repository.loadData() }
        .onSuccess { _uiState.value = UiState.Success(it) }
        .onFailure { _uiState.value = UiState.Error(it.message) }
}
```

### 3.3 Threading & Coroutines (Critical)

**Thread Selection Principles**:

| Operation Type | Thread | Description |
|----------------|--------|-------------|
| UI Updates | `Dispatchers.Main` | Update View, State, LiveData |
| Network Requests | `Dispatchers.IO` | HTTP calls, API requests |
| File I/O | `Dispatchers.IO` | Local storage, database operations |
| Compute Intensive | `Dispatchers.Default` | JSON parsing, sorting, encryption |

**Correct Usage**:
```kotlin
// In ViewModel
viewModelScope.launch {
    // Default Main thread, can update UI State
    _uiState.value = UiState.Loading
    
    // Switch to IO thread for network request
    val result = withContext(Dispatchers.IO) {
        repository.fetchData()
    }
    
    // Automatically returns to Main thread, update UI
    _uiState.value = UiState.Success(result)
}

// In Repository (suspend functions should be main-safe)
suspend fun fetchData(): Data = withContext(Dispatchers.IO) {
    api.getData()
}
```

**Common Mistakes**:
```kotlin
// ❌ Wrong: Updating UI on IO thread
viewModelScope.launch(Dispatchers.IO) {
    val data = api.fetch()
    _uiState.value = data  // Crash or warning!
}

// ❌ Wrong: Executing time-consuming operation on Main thread
viewModelScope.launch {
    val data = api.fetch()  // Blocking main thread! ANR
}

// ✅ Correct: Fetch on IO, update on Main
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) { api.fetch() }
    _uiState.value = data
}
```

### 3.4 Visibility Rules

```kotlin
// Default is public, declare explicitly when needed
class UserRepository {           // public
    private val cache = mutableMapOf<String, User>()  // Visible only within class
    internal fun clearCache() {} // Visible only within module
}

// data class properties are public by default, be careful when used across modules
data class User(
    val id: String,       // public
    val name: String
)
```

### 3.5 Common Syntax Pitfalls

```kotlin
// ❌ Wrong: Accessing uninitialized lateinit
class MyViewModel : ViewModel() {
    lateinit var data: String
    fun process() = data.length  // May crash
}

// ✅ Correct: Use nullable or default value
class MyViewModel : ViewModel() {
    var data: String? = null
    fun process() = data?.length ?: 0
}

// ❌ Wrong: Using return in lambda
list.forEach { item ->
    if (item.isEmpty()) return  // Returns from outer function!
}

// ✅ Correct: Use return@forEach
list.forEach { item ->
    if (item.isEmpty()) return@forEach
}
```

### 3.6 Server Response Data Class Fields Must Be Nullable

```kotlin
// ❌ Wrong: Fields declared as non-null (server may not return them)
@Serializable
data class UserResponse(
    val id: String = "",
    val name: String = "",
    val avatar: String = ""
)

// ✅ Correct: nullable fields + kotlinx.serialization (NOT Gson @SerializedName)
@Serializable
data class UserResponse(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("avatar")
    val avatar: String? = null
)
```

### 3.7 Lifecycle Resource Management

```kotlin
// ❌ Wrong: Only adding Observer, not removing
class MyView : View {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity?.lifecycle?.addObserver(this)
    }
    // Memory leak!
}

// ✅ Correct: Paired add and remove
class MyView : View {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity?.lifecycle?.addObserver(this)
    }

    override fun onDetachedFromWindow() {
        activity?.lifecycle?.removeObserver(this)
        super.onDetachedFromWindow()
    }
}
```

### 3.8 Logging Level Usage

```kotlin
import timber.log.Timber   // 统一使用 Timber，禁止 android.util.Log

// Info: Key checkpoints in normal flow
Timber.i("loadData: started, userId = %s", userId)

// Warning: Abnormal but recoverable situations
Timber.w("loadData: cache miss, fallback to network")

// Error: Failure/error situations
Timber.e(error, "loadData failed")
```

| Level | Use Case |
|-------|----------|
| `i` (Info) | Normal flow, method entry, key parameters |
| `w` (Warning) | Recoverable exceptions, fallback handling, null returns |
| `e` (Error) | Request failures, caught exceptions, unrecoverable errors |

---

## 4. Jetpack Compose Standards

### 4.1 @Composable Context Rules

```kotlin
// ❌ Wrong: Calling Composable from non-Composable function
fun showError(message: String) {
    Text(message)  // Compile error!
}

// ✅ Correct: Mark as @Composable
@Composable
fun ErrorMessage(message: String) {
    Text(message)
}

// ❌ Wrong: Using suspend outside LaunchedEffect
@Composable
fun MyScreen() {
    val data = fetchData()  // Error!
}

// ✅ Correct: Use LaunchedEffect
@Composable
fun MyScreen() {
    var data by remember { mutableStateOf<Data?>(null) }
    LaunchedEffect(Unit) {
        data = fetchData()
    }
}
```

### 4.2 State Management

```kotlin
// Basic State
var count by remember { mutableStateOf(0) }

// Derived State (avoid redundant computation)
val isEven by remember { derivedStateOf { count % 2 == 0 } }

// Persist across recomposition (e.g., scroll position)
val scrollState = rememberScrollState()

// State in ViewModel
class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
}
```

### 4.3 Common Compose Mistakes

```kotlin
// ❌ Wrong: Creating objects in Composable (created on every recomposition)
@Composable
fun MyScreen() {
    val viewModel = MyViewModel()  // Wrong!
}

// ✅ Correct: Use hiltViewModel() (Hilt) or remember
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    // hiltViewModel() comes from androidx.hilt:hilt-navigation-compose
    // ...
}
```

---

## 5. Resources & Icons

### 5.1 App Icon Requirements

Must provide multi-resolution icons:

| Directory | Size | Purpose |
|-----------|------|---------|
| mipmap-mdpi | 48x48 | Baseline |
| mipmap-hdpi | 72x72 | 1.5x |
| mipmap-xhdpi | 96x96 | 2x |
| mipmap-xxhdpi | 144x144 | 3x |
| mipmap-xxxhdpi | 192x192 | 4x |

Recommended: Use Adaptive Icon (Android 8+):

```xml
<!-- res/mipmap-anydpi-v26/ic_launcher.xml -->
<adaptive-icon>
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
```

### 5.2 Resource Naming Conventions

| Type | Prefix | Example |
|------|--------|---------|
| Layout | layout_ | `layout_main.xml` |
| Image | ic_, img_, bg_ | `ic_user.png` |
| Color | color_ | `color_primary` |
| String | - | `app_name`, `btn_submit` |

### 5.3 Avoid Android Reserved Names (Important)

Variable names, resource IDs, colors, icons, and XML elements **must not** use Android reserved words or system resource names. Using reserved names causes build errors or resource conflicts.

**Common Reserved Names to Avoid**:

| Category | Reserved Names (Do NOT Use) |
|----------|----------------------------|
| Colors | `background`, `foreground`, `transparent`, `white`, `black` |
| Icons/Drawables | `icon`, `logo`, `image`, `drawable` |
| Views | `view`, `text`, `button`, `layout`, `container` |
| Attributes | `id`, `name`, `type`, `style`, `theme`, `color` |
| System | `app`, `android`, `content`, `data`, `action` |

**Examples**:

```xml
<!-- ❌ Wrong: Using reserved names -->
<color name="background">#FFFFFF</color>
<color name="icon">#000000</color>

<!-- ✅ Correct: Add prefix or specific naming -->
<color name="app_background">#FFFFFF</color>
<color name="icon_primary">#000000</color>
```

```kotlin
// ❌ Wrong: Variable names conflict with system
val icon = R.drawable.my_icon
val background = Color.White

// ✅ Correct: Use descriptive names
val appIcon = R.drawable.my_icon
val screenBackground = Color.White
```

```xml
<!-- ❌ Wrong: Drawable name conflicts -->
<ImageView android:src="@drawable/icon" />

<!-- ✅ Correct: Add prefix -->
<ImageView android:src="@drawable/ic_home" />
```

---

## 6. Build Error Diagnosis & Fixes

### 6.1 Common Error Quick Reference

| Error Keyword | Cause | Fix |
|---------------|-------|-----|
| `Unresolved reference` | Missing import or undefined | Check imports, verify dependencies |
| `Type mismatch` | Type incompatibility | Check parameter types, add conversion |
| `Cannot access` | Visibility issue | Check public/private/internal |
| `@Composable invocations` | Composable context error | Ensure caller is also @Composable |
| `Duplicate class` | Dependency conflict | Use `./gradlew dependencies` to investigate |
| `AAPT: error` | Resource file error | Check XML syntax and resource references |

### 6.2 Fix Best Practices

1. **Read the complete error message first**: Locate file and line number
2. **Check recent changes**: Problems usually in latest modifications
3. **Clean Build**: `./gradlew clean :app:assembleDebug`
4. **Check dependency versions**: Version conflicts are common causes
5. **Refresh dependencies if needed**: Clear cache and rebuild

### 6.3 Debugging Commands

```bash
# Clean and build
./gradlew clean :app:assembleDebug

# View dependency tree (investigate conflicts)
./gradlew :app:dependencies

# View detailed errors
./gradlew :app:assembleDebug --stacktrace

# Refresh dependencies
./gradlew --refresh-dependencies
```

---

## 7. Material Design 3 Guidelines

Review Android UI files for compliance with Material Design 3 Guidelines and Android best practices.

### Design Philosophy

#### M3 Core Principles

| Principle | Description |
|-----------|-------------|
| **Personal** | Dynamic color based on user preferences and wallpaper |
| **Adaptive** | Responsive across all screen sizes and form factors |
| **Expressive** | Bold colors and typography with personality |
| **Accessible** | Inclusive design for all users |

#### M3 Expressive (Latest)

The latest evolution adds emotion-driven UX through:
- Vibrant, dynamic colors
- Intuitive motion physics
- Adaptive components
- Flexible typography
- Contrasting shapes (35 new shape options)

### App Style Selection

**Critical Decision**: Match visual style to app category and target audience.

| App Category | Visual Style | Key Characteristics |
|--------------|--------------|---------------------|
| Utility/Tool | Minimalist | Clean, efficient, neutral colors |
| Finance/Banking | Professional Trust | Conservative colors, security-focused |
| Health/Wellness | Calm & Natural | Soft colors, organic shapes |
| Kids (3-5) | Playful Simple | Bright colors, large targets (56dp+) |
| Kids (6-12) | Fun & Engaging | Vibrant, gamified feedback |
| Social/Entertainment | Expressive | Brand-driven, gesture-rich |
| Productivity | Clean & Focused | Minimal, high contrast |
| E-commerce | Conversion-focused | Clear CTAs, scannable |

See [Design Style Guide](references/design-style-guide.md) for detailed style profiles.

### Quick Reference: Key Specifications

#### Color Contrast Requirements

| Element | Minimum Ratio |
|---------|---------------|
| Body text | **4.5:1** |
| Large text (18sp+) | **3:1** |
| UI components | **3:1** |

#### Touch Targets

| Type | Size |
|------|------|
| Minimum | 48 × 48dp |
| Recommended (primary actions) | 56 × 56dp |
| Kids apps | 56dp+ |
| Spacing between targets | 8dp minimum |

#### 8dp Grid System

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4dp | Icon padding |
| sm | 8dp | Tight spacing |
| md | 16dp | Default padding |
| lg | 24dp | Section spacing |
| xl | 32dp | Large gaps |
| xxl | 48dp | Screen margins |

#### Typography Scale (Summary)

| Category | Sizes |
|----------|-------|
| Display | 57sp, 45sp, 36sp |
| Headline | 32sp, 28sp, 24sp |
| Title | 22sp, 16sp, 14sp |
| Body | 16sp, 14sp, 12sp |
| Label | 14sp, 12sp, 11sp |

#### Animation Duration

| Type | Duration |
|------|----------|
| Micro (ripples) | 50-100ms |
| Short (simple) | 100-200ms |
| Medium (expand/collapse) | 200-300ms |
| Long (complex) | 300-500ms |

#### Component Dimensions

| Component | Height | Min Width |
|-----------|--------|-----------|
| Button | 40dp | 64dp |
| FAB | 56dp | 56dp |
| Text Field | 56dp | 280dp |
| App Bar | 64dp | - |
| Bottom Nav | 80dp | - |

### Anti-Patterns (Must Avoid)

#### UI Anti-Patterns
- More than 5 bottom navigation items
- Multiple FABs on same screen
- Touch targets smaller than 48dp
- Inconsistent spacing (non-8dp multiples)
- Missing dark theme support
- Text on colored backgrounds without contrast check

#### Performance Anti-Patterns
- Startup time > 2 seconds without progress indicator
- Frame rate < 60 FPS (> 16ms per frame)
- Crash rate > 1.09% (Google Play threshold)
- ANR rate > 0.47% (Google Play threshold)

#### Accessibility Anti-Patterns
- Missing contentDescription on interactive elements
- Element type in labels (e.g., "Save button" instead of "Save")
- Complex gestures in kids apps
- Text-only buttons for non-readers

### Review Checklist

- [ ] 8dp spacing grid compliance
- [ ] 48dp minimum touch targets
- [ ] Proper typography scale usage
- [ ] Color contrast compliance (4.5:1+ for text)
- [ ] Dark theme support
- [ ] contentDescription on all interactive elements
- [ ] Startup < 2 seconds or shows progress
- [ ] Visual style matches app category

### Design References

| Topic | Reference |
|-------|-----------|
| Colors, Typography, Spacing, Shapes | [Visual Design](references/visual-design.md) |
| Animation & Transitions | [Motion System](references/motion-system.md) |
| Accessibility Guidelines | [Accessibility](references/accessibility.md) |
| Large Screens & Foldables | [Adaptive Screens](references/adaptive-screens.md) |
| Android Vitals & Performance | [Performance & Stability](references/performance-stability.md) |
| Privacy & Security | [Privacy & Security](references/privacy-security.md) |
| Audio, Video, Notifications | [Functional Requirements](references/functional-requirements.md) |
| App Style by Category | [Design Style Guide](references/design-style-guide.md) |

---

## 8. Testing

> **Note**: Only add test dependencies when the user explicitly asks for testing.

A well-tested Android app uses layered testing: fast local unit tests for logic, instrumentation tests for UI and integration, and Gradle Managed Devices to run emulators reproducibly on any machine — including CI.

### 8.1 Test Dependencies

Before adding test dependencies, inspect the project's existing versions to avoid conflicts:

1. Check `gradle/libs.versions.toml` — if present, add test deps using the project's version catalog style
2. Check existing `build.gradle` files for already-pinned dependency versions
3. Match version families using the table below

**Version Alignment Rules**:

| Test Dependency                              | Must Align With                                  | How to Check                                                          |
|----------------------------------------------|--------------------------------------------------|-----------------------------------------------------------------------|
| `kotlinx-coroutines-test`                    | Project's `kotlinx-coroutines-core` version      | Search for `kotlinx-coroutines` in build files or version catalog     |
| `compose-ui-test-junit4`                     | Project's Compose BOM or `compose-compiler`      | Search for `compose-bom` or `compose.compiler` in build files         |
| `espresso-*`                                 | All Espresso artifacts must use the same version  | Search for `espresso` in build files                                  |
| `androidx.test:runner`, `rules`, `ext:junit` | Should use compatible AndroidX Test versions      | Search for `androidx.test` in build files                             |
| `mockk`                                      | Must support the project's Kotlin version         | Check the Kotlin version in root `build.gradle` or other shared build config |

**Dependencies Reference** — add only the groups you need:

```kotlin
dependencies {
    // --- Local unit tests (src/test/) ---
    testImplementation("junit:junit:<version>")                          // 4.13.2+
    testImplementation("org.robolectric:robolectric:<version>")          // 4.16.1+
    testImplementation("io.mockk:mockk:<version>")                      // match Kotlin version
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:<version>")  // match coroutines-core
    testImplementation("androidx.arch.core:core-testing:<version>")      // InstantTaskExecutorRule for LiveData
    testImplementation("app.cash.turbine:turbine:<version>")             // Flow/StateFlow testing

    // --- Instrumentation tests (src/androidTest/) ---
    androidTestImplementation("androidx.test.ext:junit:<version>")
    androidTestImplementation("androidx.test:runner:<version>")
    androidTestImplementation("androidx.test:rules:<version>")
    androidTestImplementation("androidx.test.espresso:espresso-core:<version>")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:<version>")   // RecyclerView, Drawer
    androidTestImplementation("androidx.test.espresso:espresso-intents:<version>")   // Intent verification
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:<version>")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:<version>")

    // --- Compose UI tests (only if project uses Compose) ---
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")      // version from Compose BOM
    debugImplementation("androidx.compose.ui:ui-test-manifest")          // required for createComposeRule
}
```

> **Note**: If the project uses a Compose BOM, `ui-test-junit4` and `ui-test-manifest` don't need explicit versions — the BOM manages them.

Enable Robolectric resource support in the `android` block:

```kotlin
android {
    testOptions {
        unitTests.isIncludeAndroidResources = true  // required for Robolectric
    }
}
```

### 8.2 Testing by Layer

| Layer              | Location           | Runs On                 | Speed                | Use For                                          |
|--------------------|--------------------|-------------------------|----------------------|--------------------------------------------------|
| Unit (JUnit)       | `src/test/`        | JVM                     | ~ms                  | ViewModels, repos, mappers, validators           |
| Unit + Robolectric | `src/test/`        | JVM + simulated Android | ~100ms               | Code needing Context, resources, SharedPrefs     |
| Compose UI (local) | `src/test/`        | JVM + Robolectric       | ~100ms               | Composable rendering & interaction               |
| Espresso           | `src/androidTest/` | Device/Emulator         | ~seconds             | View-based UI flows, Intents, DB integration     |
| Compose UI (device)| `src/androidTest/` | Device/Emulator         | ~seconds             | Full Compose UI flows with real rendering        |
| UI Automator       | `src/androidTest/` | Device/Emulator         | ~seconds             | System dialogs, notifications, multi-app         |
| Managed Device     | `src/androidTest/` | Gradle-managed AVD      | ~minutes (first run) | CI, matrix testing across API levels             |

See [Testing](references/testing.md) for detailed examples, code patterns, and Gradle Managed Device configuration.

### 8.3 Testing Commands

```bash
# Local unit tests (fast, no emulator)
./gradlew test                          # all modules
./gradlew :app:testDebugUnitTest        # app module, debug variant

# Single test class
./gradlew :app:testDebugUnitTest --tests "com.androidbase.feature.home.HomeViewModelTest"

# Instrumentation tests (requires device or managed device)
./gradlew connectedDebugAndroidTest     # on connected device
./gradlew pixel6Api34DebugAndroidTest   # on managed device

# Both together
./gradlew test connectedDebugAndroidTest

# Test with coverage report (JaCoCo)
./gradlew testDebugUnitTest jacocoTestReport
```
