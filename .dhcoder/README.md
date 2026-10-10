# AndroidBase AI 配置说明

> 本文档说明 AndroidBase 中 AI 助手配置的**每一个文件**：作用、生效时机、搭配关系与注意事项。
> 这些配置从 AndroidBuyer 的 `AgentKit/` + `agent/` 抽取，并按本工程（Kotlin DSL 三模块 `:base` + `:app` + `:demo`、纯 Compose、Hilt、kotlinx.serialization、Timber）改写。

## 0. 先读这里：生效模型（重要）

这些文件**不是会被编译或自动执行的代码**，它们的「生效」= **AI 助手在合适的时机读取它们**。因此：

1. **发现目录取决于客户端**：
   - 本工程的约定目录是 **`.dhcoder/`**（沿用工程原有的 `.dhcoder/skills/`）。
   - 如果你的工具是 Claude Code，它默认只看 **`.claude/skills`**；Codex 默认看 **`.agents/skills`**。
   - 若工具不读 `.dhcoder/`，需要做一次映射（软链或复制），否则 skill 不会被自动发现。
2. **入口唯一**：所有路由都从根目录 `AGENTS.md` 出发，它定义「什么时候读哪个文件」。
3. **懒加载**：`rules/` 在改代码前读；`skills/<x>/SKILL.md` 在任务命中触发词时读；skill 的 `references/`、`scripts/`、`templates/` 只在 SKILL.md 里按需引用时才读。

---

## 1. 根目录入口文件

| 文件 | 1. 作用 | 2. 生效时机 | 3. 是否需搭配 | 4. 其他有价值信息 |
|---|---|---|---|---|
| `AGENTS.md` | AI 的总入口与唯一事实源：项目概览、技术栈、目录、构建命令、架构约束、**AI 工作流**（会话启动顺序 / 任务分级 L0–L3 / 执行闭环 / 必须遵守 / 严禁 / 输出要求 / Skill 路由）、代码评审入口、事实源索引 | 会话开始即读取；任务过程中反复回查 | 是。它是 `.dhcoder/rules/`、`.dhcoder/skills/`、`REVIEW.md` 的索引入口 | 本次新增了「0. 事实源与 AI 资产索引」与整节「AI 工作流」；改流程时优先改它 |
| `CLAUDE.md` | Claude Code 的桥接文件，只指向 `AGENTS.md` | 用 Claude Code 打开工程时 | 是。不写实体规则，只做跳转 | 保持极薄的桥接，避免与 `AGENTS.md` 出现两份规则 |
| `REVIEW.md` | 评审任务入口：评审默认只读、扩展上下文情形、验证升级路径、相关资产清单 | 任务为 review / diff review / MR review 时 | 是。承接 `AGENTS.md` 的评审路由，指向 `android-review` skill | 本次新增；只负责「把评审任务引到 AGENTS.md + android-review」 |
| `README.md` | 工程对外说明；本次新增「网络通用能力」与「AI 助手配置」两节 | 人阅读/上手时 | 否 | 只描述有什么，不参与 AI 路由 |

---

## 2. `.dhcoder/config.yaml`

| 项 | 内容 |
|---|---|
| 1. 作用 | 声明 AI 输出的语言约定：面向用户的评审文案必须用简体中文，路径/标识符/枚举值保持原样 |
| 2. 生效时机 | 工具读取仓库配置时；主要影响 review 类输出的语言 |
| 3. 搭配 | 与 `AGENTS.md`、`android-review` 配合；是「输出语言」的事实源 |
| 4. 备注 | 目前仅含 `output.language: zh-Hans`。想改默认语言/输出口径，改这里 |

---

## 3. `.dhcoder/rules/`（编码前必读）

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `tech_stack.md` | 本工程技术栈速览：Kotlin/AGP/Compose/Hilt/Retrofit/kotlinx.serialization/Room/Coil/Timber 版本与 SDK 级别 | 修改代码前（`AGENTS.md` 会话启动第 2 步） | 与 `gradle/libs.versions.toml` 配套；版本以 catalog 为准 | 本次新增；buyer 对应 `agent/Rules/tech_stack.md`，但内容按本工程重写 |
| `architecture.md` | 分层与依赖方向（`feature → domain ← data`、仅 data 可触网/DB）、新增功能规范、UI 状态、网络封装、禁止事项 | 写业务代码、做架构判断前 | 与 `AGENTS.md` 架构约束一节互补；评审时由 `android-review` 引用 | 本次新增；评审路由直接指向它 |
| `code-style.md` | Kotlin 风格、控制流、协程、复杂度/体量、格式，以及本项目硬约束（kotlinx.serialization、Timber、Version Catalog、KSP） | 编码前 / 评审时 | 与 `android-native-dev` skill 配合（冲突以 skill 为准）；`android-review` 会读它 | 本次新增；buyer 原版绑 Detekt，本工程尚未接入 Detekt/Ktlint，故写成约定 |

---

## 4. `.dhcoder/skills/` — 通用工作流 Skills

> **所有 skill 的统一规则**：`SKILL.md` 是入口，其 sidebar 的 `references/`、`scripts/`、`templates/` 只有被 SKILL.md 明确引用时才加载。
> 「生效时机」列指的是**触发该 skill 的任务类型**。

### 4.1 develop/排障工作流

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `diagnose/SKILL.md` | 硬 Bug / 性能回退的纪律化诊断闭环：建反馈环 → 复现 → 假设 → 插桩 → 修复 → 回归 | 用户报 Bug、报错、崩溃、跑不起来、性能退化 | 与 `tdd`（写回归测试）、`improve-codebase-architecture`（若发现架构阻碍修复）搭配 | 强调「没有可靠反馈环就不要进入下一步」 |
| `diagnose/scripts/hitl-loop.template.sh` | 人工介入（HITL）复现循环的脚本模板：Agent 跑脚本、用户在终端按提示操作 | 无法自动复现、必须人工点按时 | 由 `diagnose` SKILL.md 引用 | 模板，需复制后按场景改步骤 |
| `tdd/SKILL.md` | 红-绿-重构的 TDD 闭环；反对「一次写完所有测试」的横向切片 | 用户要求测试先行、提到 red-green-refactor、要集成测试 | 主入口；按需引用下列附件 | 核心主张：测试只测公共接口行为，不测实现细节 |
| `tdd/tests.md` | 好坏测试的判定与示例 | 写测试时 | 被 `tdd/SKILL.md` 引用 | — |
| `tdd/mocking.md` | 何时该 mock：只在系统边界 mock | 写测试需要 mock 时 | 被 `tdd/SKILL.md` 引用 | — |
| `tdd/deep-modules.md` | 「深模块 = 小接口 + 多实现」，用于设计可测接口 | 规划接口时 | 被 `tdd/SKILL.md` 引用 | 出自《A Philosophy of Software Design》 |
| `tdd/interface-design.md` | 面向可测性的接口设计 | 设计接口时 | 被 `tdd/SKILL.md` 引用 | — |
| `tdd/refactoring.md` | 测试通过后的重构候选清单 | 全绿之后 | 被 `tdd/SKILL.md` 引用 | 原则：RED 状态下不重构 |

### 4.2 架构与全局认知

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `zoom-out/SKILL.md` | 让 AI「升一层抽象」，用领域词汇画出相关模块与调用者地图 | 对该区域代码不熟、需要全局结构时 | 独立使用 | 极短的一句话型 skill；`disable-model-invocation` 需显式调用 |
| `improve-codebase-architecture/SKILL.md` | 寻找「深化（deepening）」机会：把紧耦合的浅模块合并/加深，提升可测性与可导航性 | 用户明确要做架构优化、解耦、重构 | 主入口，依赖 `CONTEXT.md` 与 `docs/adr/`；引用下列附件 | 与 `grill-with-docs` 共享领域词汇 |
| `improve-codebase-architecture/DEEPENING.md` | 按依赖类型给出安全深化步骤 | 选定深化候选后 | 被 SKILL.md 引用 | 依赖 `LANGUAGE.md` 的术语 |
| `improve-codebase-architecture/INTERFACE-DESIGN.md` | 「设计两遍」：为候选模块并行生成多个接口方案对比 | 需要比较接口方案时 | 被 SKILL.md 引用 | 出自 Ousterhout《设计两遍》 |
| `improve-codebase-architecture/LANGUAGE.md` | 统一术语表：module / interface / seam / adapter / leverage | 该 skill 的所有建议中 | 被 SKILL.md 及其附件引用 | 要求严格用词，不要替换成「component/service/API」 |
| `prototype/SKILL.md` | 一次性原型：在动手前验证设计——逻辑分支走终端小应用，UI 分支走多方案切换页 | 需求不稳定、想先做可运行草案、探索设计选项 | 主入口，按问题类型路由到下列两文件 | 产物是「用完即弃」的 |
| `prototype/LOGIC.md` | 逻辑原型：交互式终端小应用，手动驱动状态模型 | 问题是业务逻辑/状态迁移/数据结构 | 被 SKILL.md 引用 | — |
| `prototype/UI.md` | UI 原型：一条路由下多个截然不同的界面方案，底部浮条切换 | 问题是「长什么样」 | 被 SKILL.md 引用 | — |

### 4.3 需求澄清与规划

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `prd-to-spec/SKILL.md` | 把 PRD 转成可据以施工的技术 Spec（需求澄清 → 功能/流程/数据/接口/异常/验收） | 拿到产品需求、写实现计划或动代码之前 | 下一步接 `writing-plans` | **本工程原有**；Spec 保存到 `docs/spec/YYYY-MM-DD-<feature>.md` |
| `writing-plans/SKILL.md` | 把 Spec 拆成 bite-sized 的实现计划（含文件结构、任务边界、无占位符要求、自检） | 有 Spec/需求、要落地多步任务时 | 接 `prd-to-spec`；执行阶段可选子 Agent | **本工程原有**；计划保存到 `docs/superpowers/plans/` |
| `writing-plans/plan-document-reviewer-prompt.md` | 派发「计划评审子 Agent」的提示词模板 | 需要复核计划完整性时 | 被 `writing-plans` 引用 | — |
| `grill-me/SKILL.md` | 就一个方案/设计对用户穷追猛问，直到达成共识 | 用户想「拷问」自己的方案（grill me） | 独立使用 | 与 `grill-with-docs` 区别：后者会读并更新文档 |
| `grill-with-docs/SKILL.md` | 结合既有领域模型拷问方案，并**就地更新** `CONTEXT.md` 与 ADR | 需要方案对照项目词汇与已记录决策 | 主入口，引用 `ADR-FORMAT.md`、`CONTEXT-FORMAT.md` | 决策落文档，不只在对话里 |
| `grill-with-docs/ADR-FORMAT.md` | ADR 格式与编号约定（`docs/adr/0001-slug.md`） | 产生架构决策记录时 | 被 SKILL.md 引用 | 目录按需惰性创建 |
| `grill-with-docs/CONTEXT-FORMAT.md` | `CONTEXT.md`（领域词汇表）的结构格式 | 需要沉淀领域词汇时 | 被 SKILL.md 引用 | 与 `improve-codebase-architecture` 共享 |
| `app-grill-pm/SKILL.md` | 面向 App 的「PM 拷问」：实施前澄清产品行为、用户流程、设计边界、验收标准 | PRD/功能简述在实施前需要产品澄清 | 主入口，引用下列两个 references | 只澄清产品语义，不出技术方案 |
| `app-grill-pm/references/prd-review-checklist.md` | 需求与设计澄清检查清单（逐功能点走「已完整/需澄清」） | 做产品澄清盘点时 | 被 SKILL.md 引用 | — |
| `app-grill-pm/references/question-patterns.md` | 提问套路模板（如何问出缺失信息） | 逐题闭合产品规则时 | 被 SKILL.md 引用 | — |
| `prd-code-impact-architect/SKILL.md` | PRD 代码影响分析子 skill：输出代码落点、模块边界、方案草稿、可执行验收点 | 通常由上层 PRD→Spec 流程调用；仅在「只要影响分析」时独立使用 | 引用下列三个模板文件 | 本次移植但**上游 `prd-to-app-spec` 未移植**，见 §6 |
| `prd-code-impact-architect/references/prd-extract-template.md` | PRD 抽取模板（只放需求事实） | 生成 `prd-extract.md` 时 | 被 SKILL.md 引用 | — |
| `prd-code-impact-architect/references/change-map-template.md` | 需求→代码映射表模板 | 生成 `change-map.md` 时 | 被 SKILL.md 引用 | — |
| `prd-code-impact-architect/references/architecture-proposal-template.md` | 最小实现方案 + 验证计划模板 | 生成 `architecture-proposal.md` 时 | 被 SKILL.md 引用 | — |

### 4.4 任务拆解与分流

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `to-prd/SKILL.md` | 把当前对话上下文**合成**成 PRD（不追问用户），并发布到 issue tracker | 想把讨论沉淀成 PRD 时 | 依赖 `setup-matt-pocock-skills` 提供的 tracker/标签配置 | 内含 PRD 模板（问题/方案/用户故事/实现决策/测试决策/范围外） |
| `to-issues/SKILL.md` | 把计划/PRD 拆成**可独立认领的纵向切片** issue（tracer bullet） | 想把计划拆成工单/issue 时 | 同 `to-prd`，依赖 tracker 配置 | 切片分 HITL / AFK |
| `triage/SKILL.md` | 用状态机给 issue 分流：`needs-triage / needs-info / ready-for-agent / ready-for-human / wontfix` | 建 issue、审 bug/需求、准备给 AFK Agent 时 | 引用 `AGENT-BRIEF.md`、`OUT-OF-SCOPE.md`；依赖 tracker 配置；需要时接 `grill-with-docs` | 产出评论必须带「AI 生成」免责声明 |
| `triage/AGENT-BRIEF.md` | 如何写「Agent 简报」（交给 AFK Agent 的权威契约） | issue 进入 `ready-for-agent` 时 | 被 `triage/SKILL.md` 引用 | — |
| `triage/OUT-OF-SCOPE.md` | `.out-of-scope/` 知识库：记录被拒功能的理由 | 把功能判为 wontfix 时 | 被 `triage/SKILL.md` 引用 | 机构记忆，避免重复讨论 |
| `caveman/SKILL.md` | 极简输出模式：砍掉填充词/冠词/客套，token 约省 75%，技术准确性不变 | 用户明确要求极简输出时 | 独立使用 | 会显著改变回复风格，慎默认开启 |

### 4.5 文件化任务管理

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `planning-with-files/SKILL.md` | Manus 风格的文件化规划：用 `task_plan.md` / `findings.md` / `progress.md` 三个文件承载任务上下文 | 多步任务、研究型任务、预计 >5 次工具调用时 | 主入口；引用下列模板/脚本 | 支持 `/clear` 后的会话恢复 |
| `planning-with-files/templates/task_plan.md` | 任务计划模板（目标、阶段、进度） | 初始化任务工作区时 | 由 `init-session.sh` 复制生成 | 相当于「磁盘上的工作记忆」 |
| `planning-with-files/templates/findings.md` | 发现与决策记录模板 | 同上 | 同上 | 外部记忆，突破上下文窗口 |
| `planning-with-files/templates/progress.md` | 进度日志模板（按时间记做了什么） | 同上 | 同上 | 回答「我做了什么」，便于中断续做 |
| `planning-with-files/scripts/init-session.sh` | 初始化上述三个文件（macOS/Linux） | 新任务开始 | 配合 templates | — |
| `planning-with-files/scripts/init-session.ps1` | 同上（Windows PowerShell） | 同上 | 同上 | — |
| `planning-with-files/scripts/check-complete.sh`、`check-complete.ps1` | 检查 `task_plan.md` 各阶段是否完成（`.sh` 为 macOS/Linux，`.ps1` 为 Windows） | 收尾/Stop hook 时 | 读 task_plan.md | 恒 exit 0，用 stdout 报状态 |
| `planning-with-files/scripts/session-catchup.py` | 会话恢复脚本：读取三文件重建上下文 | `/clear` 或换会话后继续 | 读三文件 | Python3 |
| `planning-with-files/examples.md` | 三文件法实战示例 | 学习用法时 | 被 SKILL.md 引用 | — |
| `planning-with-files/reference.md` | Manus 上下文工程六原则 | 理解设计动机时 | 被 SKILL.md 引用 | — |

### 4.6 首次配置

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `setup-matt-pocock-skills/SKILL.md` | 首次接入：向 `AGENTS.md`/`CLAUDE.md` 写入 `## Agent skills` 块，并在 `docs/agents/` 落 tracker、标签、领域文档布局配置 | 首次使用 `to-issues`/`to-prd`/`triage`/`diagnose`/`tdd`/`improve-codebase-architecture`/`zoom-out` 之前 | 是 `to-prd`/`to-issues`/`triage` 的前置 | ⚠️ **它会修改 `AGENTS.md`/`CLAUDE.md`**，运行前先确认 |
| `setup-matt-pocock-skills/domain.md` | 领域文档如何被 skills 消费（先读什么） | 需要领域文档约定时 | 被 SKILL.md 引用 | — |
| `setup-matt-pocock-skills/issue-tracker-github.md` | GitHub issue tracker 约定 | 用 GitHub 时 | 被 SKILL.md 引用 | 三选一 |
| `setup-matt-pocock-skills/issue-tracker-gitlab.md` | GitLab issue tracker 约定 | 用 GitLab 时 | 被 SKILL.md 引用 | 三选一 |
| `setup-matt-pocock-skills/issue-tracker-local.md` | 本地 Markdown issue 约定（`.scratch/`） | 无远端 tracker 时 | 被 SKILL.md 引用 | 三选一，最轻量 |
| `setup-matt-pocock-skills/triage-labels.md` | 把 skill 的五个标准角色映射到本仓实际标签串 | 配置分流标签时 | 被 `triage` 等引用 | 标签名可随仓而异 |

---

## 5. `.dhcoder/skills/` — Android 专项 Skills

| 文件 | 1. 作用 | 2. 生效时机 | 3. 搭配 | 4. 其他 |
|---|---|---|---|---|
| `android-native-dev/SKILL.md` | 本工程的 Android 开发总纲：仓库事实、默认验证路径、Kotlin/Compose 规范、资源命名、构建报错速查、Material 3、测试分层 | Android 开发、功能实现、重构、构建排障 | 必须先读 `references/project-facts.md`；引用其 10 个 references | 已改写：Kotlin DSL、三模块（`:base` + `:app` + `:demo`）、无 flavor、纯 Compose、kotlinx.serialization、Timber |
| `android-native-dev/references/project-facts.md` | **本工程的仓库事实**（build 形态、版本、架构分层、约定、验证命令、坑） | 用 `android-native-dev` 时最先读 | 被 SKILL.md 第 1 节强制引用 | 事实与通用建议冲突时以本文件为准 |
| `android-native-dev/references/visual-design.md` | M3 颜色/字体/间距/高度/形状详细规格 | 设计/评审 UI 时 | 被 SKILL.md §7 引用 | 通用 Android 最佳实践 |
| `android-native-dev/references/motion-system.md` | M3 动画与过渡规格 | 做动效时 | 同上 | — |
| `android-native-dev/references/accessibility.md` | 无障碍要求（contentDescription、对比度、触达尺寸等） | 评审/实现 UI 时 | 同上 | — |
| `android-native-dev/references/adaptive-screens.md` | 大屏/平板/折叠屏/多窗口适配 | 适配大屏时 | 同上 | — |
| `android-native-dev/references/performance-stability.md` | Android Vitals 阈值与性能/稳定性最佳实践 | 性能优化、发版前 | 同上 | 含崩溃率/ANR 率阈值 |
| `android-native-dev/references/privacy-security.md` | 隐私与安全实践 | 涉及权限、数据、合规时 | 同上 | — |
| `android-native-dev/references/functional-requirements.md` | 音频/视频/通知等功能性要求 | 涉及这些能力时 | 同上 | — |
| `android-native-dev/references/design-style-guide.md` | 按 App 品类给出视觉风格画像 | 确定 App 视觉方向时 | 同上 | — |
| `android-native-dev/references/testing.md` | 各测试层的详细示例与 Gradle Managed Device 配置 | 写测试时 | 被 SKILL.md §8 引用 | 与 `tdd` skill 互补 |
| `android-native-dev/agents/openai.yaml` | Codex 侧的展示名/简介/默认提示词 | Codex 加载该 skill 时 | 与 SKILL.md 同目录 | 元数据，不影响逻辑 |
| `android-verify/SKILL.md` | 验证工作流：先看 diff → 选最小验证 → 跑精确 Gradle task → 结构化汇报（Verified/Blocked/Noise/Risk） | 需要编译、单元测试、构建验证时 | 评审时与 `android-review` 合用；Bug 时与 `diagnose` 合用 | 已改写为无 flavor 的 `debug` 任务 |
| `android-verify/references/task-map.md` | 本工程验证命令地图与常见失败模式（缺 SDK、依赖解析、Gradle 锁） | 选验证命令时 | 被 SKILL.md 第 1 步引用 | ⚠️ 只适用本工程，换工程要重写 |
| `android-verify/agents/openai.yaml` | Codex 元数据 | 同上 | 同上 | — |
| `android-review/SKILL.md` | 通用评审流程：定范围 → 先看 diff → 按需扩上下文 → 读规则 → 输出高信号发现 | diff review / MR review / 自动化评审 | 读 `REVIEW.md` 与 `.dhcoder/rules/`；需要证据时调 `android-verify` | 已移除 DHGate 模块路由，改为分层检查 |
| `android-review/agents/openai.yaml` | Codex 元数据 | 同上 | 同上 | — |

---

## 6. 组合用法（推荐路径）

```
① 需求接入
   prd-to-spec ──► writing-plans ──► 实施（android-native-dev）
   （产品语义不清时先走 app-grill-pm；方案要拷问时走 grill-me / grill-with-docs）

② 日常改代码
   AGENTS.md → .dhcoder/rules/{tech_stack,architecture,code-style}.md → 编码 → android-verify

③ 排障
   diagnose（＋ android-verify 取证据；发现架构阻碍转 improve-codebase-architecture）

④ 评审
   REVIEW.md → android-review（＋ android-verify；＋ .dhcoder/rules/）

⑤ 多步/长任务
   planning-with-files（task_plan / findings / progress 三文件）

⑥ 任务拆解与分流
   to-prd / to-issues / triage（首次需先跑 setup-matt-pocock-skills 配置 tracker 与标签）
```

---

## 7. 与 AndroidBuyer 的对应关系

| AndroidBase（新增/改写） | AndroidBuyer 来源 |
|---|---|
| `.dhcoder/rules/tech_stack.md` | `agent/Rules/tech_stack.md`（内容重写） |
| `.dhcoder/rules/architecture.md` | `agent/Rules/ARCHITECTURE.md`（内容重写） |
| `.dhcoder/rules/code-style.md` | `agent/Rules/code-style-rule.md`（Detekt 约束改写为约定） |
| `.dhcoder/config.yaml` | `agent/config.yaml`（照搬） |
| `.dhcoder/skills/<通用 skill>/` | `AgentKit/Skills/<同名>/`（照搬） |
| `.dhcoder/skills/android-native-dev/` | `agent/skills/android-native-dev/`（改写 `SKILL.md` 仓库段 + 重写 `project-facts.md`） |
| `.dhcoder/skills/android-verify/` | `agent/skills/android-verify/`（重写 `SKILL.md` + `task-map.md`） |
| `.dhcoder/skills/android-review/` | `agent/skills/android-review/`（改写，去模块路由） |
| `REVIEW.md` | `REVIEW.md`（改写） |
| `AGENTS.md` 工作流章节 | `AgentKit/AGENTS.md`（提炼为 L0–L3 精简版） |

**有意未移植**（依赖 buyer 自有工具链或属业务专用）：
`AgentKit/Tools`、`docs/sdd/*`、`Subagents/`、`task-level`、`prd-to-app-spec`、`doc-to-markdown`、
以及业务 skills：`cart-module-review`、`order-module-review`、`checkout-flow-review`、
`android-home-module-review`、`android-gitlab-ai-review`、`android-cli`、`r8-analyzer`、`engage-sdk-integration`。

> 注意：`prd-code-impact-architect`（4.3）的正常上游 `prd-to-app-spec` **未移植**，因此它目前只能在「用户明确只要代码影响分析」时独立使用。若需要完整 PRD→Spec→实施闭环，需补移植 SDD 框架（见下）。

---

## 8. 维护指南

- **新增 skill**：建 `.dhcoder/skills/<name>/SKILL.md`，frontmatter 至少含 `name` 与 `description`；在 `AGENTS.md` 的「Skill 分层与路由」里登记触发规则。
- **改名/移动**：同步更新 `AGENTS.md`、`REVIEW.md` 与其它 skill 中对它的引用。
- **规则变更**：优先改 `.dhcoder/rules/` 或对应 skill，再回写 `AGENTS.md` 的索引。
- **换客户端目录**：若工具读 `.claude/skills` 或 `.agents/skills`，把 `.dhcoder/skills` 软链/复制过去即可，内容无需改动。

## 9. 可选的下一步

1. 移植 SDD 治理框架（`task-level` + `prd-to-app-spec` + `Subagents/` + `docs/sdd` 模板），补齐 4.3 的上游闭环。
2. 增加 `.claude/skills` / `.agents/skills` 兼容入口。
3. 接入 Detekt/Ktlint，让 `code-style.md` 的约束可被自动校验。
