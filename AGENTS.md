# AndroidBase — AI 开发指南

> 本文件是给 AI 编程助手（Claude Code、Codex、Cursor 等）的项目上下文。
> 开始在本仓库写代码之前，请先完整阅读本文件。

## 0. 事实源与 AI 资产索引

| 信息 | 唯一事实源 |
|---|---|
| 项目开发约定（本文件） | `AGENTS.md` |
| 技术栈 | `.dhcoder/rules/tech_stack.md`、`gradle/libs.versions.toml` |
| 架构与分层边界 | `.dhcoder/rules/architecture.md` |
| 编码风格 | `.dhcoder/rules/code-style.md` |
| 通用 Skills | `.dhcoder/skills/` |
| 评审入口 | `REVIEW.md` |
| 输出语言 | `.dhcoder/config.yaml`（简体中文） |

> `AGENTS.md` 是任务路由与上下文披露顺序的唯一入口；调整流程时优先改本文件。

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

- **Token 注入**：`AuthInterceptor` 从 DataStore 读 token，自动加 `Authorization: Bearer <token>`；已显式携带 Authorization 的请求不覆盖。与身份无关的公共头在 `CommonHeaderInterceptor`。
- **统一响应/异常**：带业务信封的接口用 `core/network` 的 `ApiResponse<T>` + `apiEnvelopeCall`；异常经 `NetworkExceptionEngine` 归一化为 `NetworkException`（`ApiResult.Error.bizCode` 携带业务码）。
- **动态域名 / 超时**：接口写 `@Headers("url:key")` 由 `DynamicHostInterceptor` 切换域名；按接口超时规则、域名映射统一维护在 `core/network/HostConfig.kt`。
- **网络状态**：`NetworkMonitor`（需 `ACCESS_NETWORK_STATE`，已在 Manifest 声明）提供 `isConnected()` 与 `isOnline` Flow，请求前判断或 UI 断网提示。
- **刷新 Token**：目前是业务占位。需在 OkHttp `Authenticator` 或专门的 `TokenRefreshManager` 实现串行刷新；刷新失败后 `UserPreferences.clear()` 并导航到登录页。
- **环境切换**：`API_BASE_URL` 由 `app/build.gradle.kts` 的 `buildConfigField` 注入（当前为 jsonplaceholder 示例地址）。接入真实后端时改这里，或按 debug/staging/release 分环境。
- **Debug 变体**：`applicationId` 带 `.debug` 后缀，`versionName` 带 `-debug`。
- **首页样例**：请求 `https://jsonplaceholder.typicode.com/posts`，经 Room 缓存回退；图片来自 Picsum。

## 测试

- 目前仅 **JUnit4** 单元测试（`app/src/test`，含基础 `ApiResult` 用例）。
- 新增领域逻辑 / 仓库逻辑时**优先补单元测试**；UI 测试与 androidTest 尚未接入。

## AI 工作流

### 会话启动顺序（Just-in-Time，禁止无目的加载整个仓库）

1. 读取本文件 `AGENTS.md`。
2. 读取 `.dhcoder/rules/` 下与当前任务相关的规则（改代码前：`tech_stack.md`、`architecture.md`、`code-style.md`）。
3. 按任务关键词读取命中的 `.dhcoder/skills/<skill>/SKILL.md`，再读其按需路由的 references。
4. 代码评审前额外读取 `REVIEW.md`。

### 任务分级

| 等级 | 场景 | 流程 |
|---|---|---|
| L0 | 问答、只读分析、状态查询，无任何实现性副作用 | 只给结论，不改文件 |
| L1 | 低风险局部小改（1–3 个同模块文件，不改对外接口/数据/路由） | 会话内 3–5 行 mini-spec → 实施 → Self Check |
| L2 | 多文件、多步骤或需要方案权衡 | 先 Spec（`prd-to-spec`）→ 用户确认 → 分步计划（`writing-plans`）→ 实施 → 验证 |
| L3 | 核心链路或大范围改造 | L2 流程 + 一次独立评审（`android-review`） |

- 需求有多种解释、涉及核心链路或显著外部影响时，不论等级都必须先与用户确认。
- 分级只决定工作流，不替代文件范围、破坏性操作或权限授权。

### 执行闭环

1. 先做只读审查，再做最小闭环改动。
2. 编码前说清假设和不确定项。
3. 最小实现，不预埋未来扩展，不顺手重构无关内容。
4. 改完必须自检并给出验证证据（命令 + 结果），不能只写“已验证”。

### 必须遵守

- 新增或修改代码必须遵守 `.dhcoder/rules/` 中的架构与编码约束。
- 只做用户明确要求，只改与任务直接相关的代码。
- 完成时记录验证证据：命令、结果、失败原因或可观测行为。

### 严禁事项

- 未经明确要求做大规模重构或跨模块迁移。
- 绕过统一架构自建并行路径。
- 生产代码捏造数据或混入 mock/临时调试入口。
- 修改生成文件或依赖锁文件（除非用户明确要求）。

### 输出要求（L1 及以上）

1. 改动摘要
2. 受影响文件
3. 验证情况
4. 风险与后续建议
5. 本次 Skill 复用/新增情况（无则写“无”）

### Skill 分层与路由

- Skills 位于 `.dhcoder/skills/<name>/SKILL.md`；同名时项目 Skill 优先。
- 选定 Skill 后必须在执行前完整读取其 `SKILL.md`。
- 通用 Skill 触发规则：
  - `diagnose`：出现 Bug、报错、失败或性能回退。
  - `tdd`：要求测试先行，或功能/修复需要先写测试。
  - `to-prd` / `to-issues`：把讨论沉淀为 PRD，或拆成可独立执行的任务。
  - `triage`：对需求/缺陷做状态分流。
  - `zoom-out`：需要先看全局结构与模块关系。
  - `prototype`：需求不稳定，先做可运行草案验证方向。
  - `improve-codebase-architecture`：明确要做架构优化或解耦。
  - `prd-to-spec` → `writing-plans`：需求 → 技术 Spec → 分步实现计划。
  - `grill-me` / `grill-with-docs`：需要高强度审视方案与边界。
  - `caveman`：用户明确要求极简输出。
  - Android 专项：`android-native-dev`（开发/排障）、`android-verify`（编译/测试/验证）、`android-review`（评审）。

## 代码评审

评审默认只读；任务为 review / diff review / MR review 时，先读 `REVIEW.md`，再按 `.dhcoder/skills/android-review/SKILL.md` 执行。

## 变更纪律

- 提交信息遵循 Conventional Commits（`feat:` / `fix:` / `refactor:` / `docs:` / `test:`）。
- 大改动先写实现计划（见 `.dhcoder/skills/writing-plans`），再动代码。
- 需求级改动：先 PRD → 技术 Spec（见 `.dhcoder/skills/prd-to-spec`），再计划，再实现。
