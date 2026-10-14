# AndroidBase Agent 效率与复杂任务闭环升级技术 Spec

> 来源：用户要求“按复杂任务升级 Agent 配置” ｜ 日期：2026-10-14 ｜ 基线：AndroidBase `3cd75d2`；开始时工作区干净

## 1. 目标

不改变 Android 应用行为、依赖或构建配置，保留 L0/L1 快路径；为多步骤开发任务补齐可执行的配置审计与模块验证入口，并明确 L2/L3 的 Spec、实施计划、审批和独立评审门禁，减少重复探索和手动选择验证命令。

配置审计是低成本的预防性维护工具；主要效率收益来自按模块验证入口和清晰的复杂任务路径。

## 2. 已确认事实

- `AGENTS.md` 是唯一工作流入口。当前等级为 L0–L3：L2 执行 Spec → 用户确认 → 分步计划 → 实施 → 验证；L3 另需独立评审。
- Agent 配置源为 `.dhcoder/`；`.claude/skills/` 与 `.agents/skills/` 内的 Skill 链接指向 `.dhcoder/skills/`。
- 工程有 `:base`、`:app`、`:demo` 三个无 flavor 模块；`android-verify/references/task-map.md` 已列出模块验证 task。
- 仓库没有根级 `scripts/` 目录或 Agent 配置校验脚本。
- `:base` 与 `:app` 有本地 JVM 单元测试；`:demo` 当前没有 `src/test` 源文件。应用和 demo 的打包产物是 APK，base 的打包产物是 AAR。
- 当前 AGENTS、REVIEW 和 task-map 对单测所在模块的描述未完全反映 `:app` 已有测试，需要纳入这次一致性更新。

## 3. 范围

### 包含

1. 新增无第三方依赖的 Agent 配置审计工具，检查真实 Skill frontmatter、AGENTS 路由覆盖、Claude/Codex Skill 入口软链和项目 Agent 文档中的本地 Markdown 链接。
2. 新增按模块运行最小验证的脚本，支持 `base`、`app`、`demo`、`all`、单测、assemble 和 dry-run。
3. 更新 `AGENTS.md`、`REVIEW.md`、`README.md`、`.dhcoder/README.md`、`android-verify/references/task-map.md`：对齐 L2/L3 路径、分模块测试事实、脚本入口和独立复核规范。
4. 为两个脚本添加不依赖 Android SDK、不执行真实 Gradle 构建的可重复测试。

### 不包含

- 不修改 Android 业务代码、Gradle 文件、依赖、版本目录、签名配置或应用行为。
- 不移植完整 SDD 状态机、任务索引/归档体系或外部 Agent 平台。
- 不新增客户端专属 subagent 格式。L3 独立评审优先由未参与实施的独立 Agent/上下文执行；当前客户端不可委派时，使用新的只读复核回合，并记录该降级方式。
- 不拆分约 700 行的 `android-native-dev/SKILL.md`；此项另行评估，避免和流程/工具改动混杂。
- 不执行设备安装或 instrumentation tests；项目目前未接入此类测试。

## 4. 复杂任务流程要求

- **L0**：只读问答/状态检查，不改文件，不产生外部副作用。
- **L1**：低风险、单目标、局部文件改动；会话内 mini-spec → 实施 → 最小验证与 self-check，不强制创建完整 Spec/计划文件。
- **L2**：多文件、多步骤、跨模块、改变对外契约/持久化/依赖配置、存在多方案权衡，或事实不足以证明为 L1。依次完成技术 Spec、用户确认、具体到文件和验证命令的实施计划、用户批准计划、实施与验证。
- **L3**：登录/鉴权/隐私安全、关键数据/存储迁移、多个模块的大范围改造或显著外部影响。执行 L2 门禁；Human Acceptance 前增加一次未参与实施的独立只读评审，对照已批准 Spec、实际 diff 和验证证据。
- 用户确认 Spec/计划不替代文件范围授权、破坏性命令确认或外部副作用授权。低风险配置/文档任务使用静态与脚本测试，不要求 Android 全量构建。

## 5. 脚本接口

### 配置审计

- 命令：`python3 scripts/agent/audit_config.py`。
- 使用 Python 标准库；不安装依赖、不访问网络；可选 `--root <path>` 供隔离夹具测试，默认仓库根目录由脚本位置推导。
- 扫描范围固定为仓库根 `AGENTS.md`、`CLAUDE.md`、`REVIEW.md`、`README.md` 与 `.dhcoder/**/*.md`，不扫描产品 Spec/计划等其它业务文档。
- frontmatter 按该仓库当前简单 YAML 子集解析：要求 `SKILL.md` 有 `---` 边界、单行非空 `name:`/`description:`，name 必须与 Skill 目录名一致；不要求完整 YAML parser。
- AGENTS 路由检查仅解析 `### Skill 分层与路由` 到下一个同级/更高级标题的区段；Skill 名必须作为独立的 Markdown inline-code 标识出现，不能用普通子串命中。
- 检查 `.claude/skills/<name>`、`.agents/skills/<name>` 为可解析软链，且目标唯一解析到 `.dhcoder/skills/<name>`；发现入口缺失或链接漂移时报告错误。
- Markdown 链接按源文件目录解析相对路径；跳过 `http(s)://`、`mailto:` 和纯锚点链接。对明确的格式示例路径建立小型、按源文件限定的 allowlist，当前包含 `.dhcoder/skills/grill-with-docs/CONTEXT-FORMAT.md` 中的 `./src/ordering/CONTEXT.md`、`./src/billing/CONTEXT.md`、`./src/fulfillment/CONTEXT.md`。其它断链必须报错。
- 成功返回 0；发现问题返回非零，并指出文件与检查项。结果面向人类，不自动修改文件。

### 模块验证

- 命令：`scripts/agent/verify.sh <base|app|demo|all> [--tests] [--assemble] [--dry-run]`；允许从任意工作目录调用。
- 普通模式先运行 `git diff --check`，再用单次 Gradle wrapper 调用执行所选任务，保证共同依赖 task 可由 Gradle 去重；遇错即停并传播失败状态。
- 模块映射：
  - `base` → `:base:compileDebugKotlin`
  - `app` → `:app:compileDebugKotlin`
  - `demo` → `:demo:compileDebugKotlin`
  - `all` → 上述三个 compile task
- `--tests`：仅当对应模块 `src/test` 下存在 Kotlin/Java 源文件时追加 `:module:testDebugUnitTest`；无测试源时输出明确 `SKIP`，不将空测试任务伪报为测试覆盖。当前 `:demo` 应被跳过。
- `--assemble`：对选中模块添加 `:base:assembleDebug`、`:app:assembleDebug`、`:demo:assembleDebug` 中对应任务。`all --assemble` 包含三者；base 的 AAR 是独立产物，不因 app/demo 依赖 base 而省略。所有 task 在一个 Gradle 调用中执行。
- `--dry-run` 只打印 `git diff --check` 与 Gradle 命令，不执行二者，退出 0；用它验证命令组合。
- 非法模块或参数打印用法并以非零退出；脚本不安装、不写项目文件、不访问网络。

## 6. 拟议文件边界

- 修改：`AGENTS.md`、`REVIEW.md`、`README.md`、`.dhcoder/README.md`、`.dhcoder/skills/android-verify/references/task-map.md`。
- 新增：`scripts/agent/audit_config.py`、`scripts/agent/verify.sh`、`scripts/agent/tests/test_audit_config.py`、`scripts/agent/tests/test_verify.sh`。
- 不修改：`app/`、`base/`、`demo/`、`build.gradle.kts`、`settings.gradle.kts`、`gradle/`、`android-native-dev/SKILL.md`。

## 7. 验收标准

- [ ] 配置审计在当前仓库通过；隔离测试中，缺失 `SKILL.md`、缺少/目录名不匹配的 frontmatter、Skill 未登记路由、缺失/错误软链、失效项目内 Markdown 相对链接均返回非零并给出定位；外链、纯锚点和 allowlist 示例不误报。
- [ ] 路由检查只读取指定路由章节，并以完整 inline-code Skill 名称比对；目录中每个有效 Skill 都须出现。
- [ ] `verify.sh --dry-run` 对 `base`、`app`、`demo`、`all` 输出精确的模块 task；`--tests` 对有测试源模块追加 task、对空测试模块显示 SKIP；`--assemble` 包含所选模块的正确产物 task，`all` 包含 base AAR 与 app/demo APK。
- [ ] `verify.sh` 拒绝未知模块/参数；自动化夹具用临时 Git 仓库与假 `gradlew` 验证普通模式先执行 diff check、Gradle 收到合并后的 task 列表、Gradle 失败时脚本以非零退出并停止后续操作；`bash -n` 通过。
- [ ] AGENTS、REVIEW、README、task-map 对 L2/L3 门禁、测试模块归属和脚本用法相互一致；只读任务与 L1 快路径没有被加重。
- [ ] `.dhcoder/README.md` 登记工具职责与维护方式；新增/修改 Markdown 的仓库内链接有效。
- [ ] `git diff --check` 通过；不执行 Android 构建，因为改动限于配置、文档和辅助脚本。

## 8. 实施计划前的评审结论

独立只读复核未发现阻断项，但指出了初稿中扫描范围/示例链接、`:app` 单测事实、失败传播测试方法和 assemble 产物边界未明确等问题。本修订版已明确这些边界；计划阶段仍须按实际实现结构精确锁定脚本测试夹具与文档修改位置。

## 9. 后续流程

用户确认本 Spec 后，使用 `.dhcoder/skills/writing-plans` 编写精确到文件、实现顺序、测试用例和命令的分步计划；计划完成后再次等待用户批准，批准后才进入实现。