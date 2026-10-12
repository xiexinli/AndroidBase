# UI 模块边界迁移 Implementation Plan

**Goal:** 将主应用页面与页面状态迁移至 `:app`，令 `:base` 只提供公共能力，并让 `:demo` 自主承载其示例 UI。

**Architecture:** `:app` 与 `:demo` 分别拥有自己的 UI/ViewModel，均通过 `:base` 的 domain 接口/use case 访问共享数据能力。`:base` 保留 core/data/domain 和通用 UI 组件。

**Tech Stack:** Kotlin 2.1.0、Compose BOM 2025.02.00、Navigation Compose 2.8.8、Hilt 2.56.2、JUnit4、kotlinx-coroutines-test 1.10.1。

**Spec:** `docs/spec/2026-10-12-ui-module-boundary.md`

## Global Constraints
- 不让 `:base` 依赖任何宿主应用。
- `:app` 和 `:demo` 互不依赖，均只依赖 `:base`。
- 不改变帖子分页、缓存回退及主界面用户可见行为。
- 依赖版本只在 Version Catalog 定义；架构说明必须与实际目录一致。

### Task 1: 主应用接管 UI — 完成
- 将 base 的 feature 页面/导航/ViewModel 源文件移至 `app/src/main/java/com/development/app/feature/` 并更新包名、引用。
- 在 app 显式声明 navigation-compose、hilt-navigation-compose、lifecycle compose 等直接使用的依赖。
- 将分页 ViewModel 单测与所需测试依赖移至 app。
- 验证：`:app:testDebugUnitTest :app:assembleDebug`。

### Task 2: demo 独立持有示例页面状态 — 完成
- 将 `HomeUiState`/HomeViewModel 的 demo 所需最小数据加载行为在 `demo` 本地实现，调用 `GetPostsUseCase`；修改折叠屏页面引用。
- 验证 demo 源码不再引用 `com.development.base.feature`，运行 `:demo:assembleDebug`。

### Task 3: 清理 base 页面层并对齐文档 — 完成
- 删除 base 中已迁走的页面与页面级 ViewModel，保留 core/data/domain。
- 修改 `AGENTS.md`、`.dhcoder/rules/architecture.md`、android-native-dev 技能说明及 README 的模块边界。
- 验证 `:base:testDebugUnitTest`、app/demo 编译打包及 `git diff --check`。
