# UI 模块边界迁移技术 Spec

> 来源：用户确认的架构要求 ｜ 日期：2026-10-12

## 1. 已确认结论
- Compose 页面、导航、页面级 ViewModel 和 UI state 属于宿主应用；当前主应用 UI 全部放入 `:app`。
- `:base` 仅保留可复用公共能力、基础设施、数据层与领域层，包括主题/图片加载组件等可复用 UI 能力。
- `:demo` 继续独立依赖 `:base`；折叠屏演示页面及其 UI state/ViewModel 放在 `:demo`，不得依赖 `:app` 或 `:base` 的页面包。
- 用户可见行为保持不变：Home/IM/Account 三 Tab、分页刷新与加载、详情导航、demo 折叠屏示例。

## 2. 迁移范围
- 移动主应用 `MainRoute`、Home/详情/IM/Account 页面及其 ViewModel/UI state 到 `app/src/main/java/com/development/app/feature/`。
- 分页数据 API、Repository、UseCase、Room、通用 `DefaultPageSize` 留在 `:base`。
- `:demo` 自己实现所需的帖子加载 ViewModel/state，继续通过 `:base` 的 domain use case 获取数据。
- 删除 `:base` 中具体页面层及页面级测试；将 HomeViewModel 分页单测迁至 `:app`。
- 更新模块架构说明与依赖；不改 API/缓存/分页语义。

## 3. 依赖边界
- `:app → :base`；`:demo → :base`；`:app` 与 `:demo` 不互相依赖。
- `:base` 不引用 `:app` 或 `:demo`，不包含具体页面/导航/页面 ViewModel。
- `:base/core/ui` 中 `AndroidBaseTheme`、`AppImage` 作为公共能力保留。

## 4. 验收标准
- [x] `base/src/main/java` 不再包含具体页面、导航壳、页面 ViewModel；公共 core/data/domain 保留。
- [x] 主应用的三 Tab、详情页、刷新/分页交互实现位于 `:app`。
- [x] demo 不引用 `com.development.base.feature.*`，仍由 `:base` 提供数据能力。
- [x] `./gradlew :base:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :demo:assembleDebug` 成功。
- [x] `git diff --check` 通过，文档不再声称页面 UI 位于 `:base`。
