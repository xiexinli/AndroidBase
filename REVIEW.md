# AndroidBase Review Entry

本文件是本仓库的 **review 入口文档**。

当任务是常规 review、diff review、MR review 或 AI 自动 review 时，下一步读取 `AGENTS.md`，并按其中「代码评审」一节的指引加载对应 skill。

## 评审默认姿态

- 评审默认**只读**，不修改代码（除非用户明确要求修复）。
- 先看 diff 确定范围，再按需扩展上下文，不做全仓漫游。
- 每条发现锚定到具体文件 / 方法 / 符号 / 代码路径；严重问题需给证据。
- 没有具体问题就直说，不要为凑数堆砌低价值发现。

## 需要扩展上下文的情形

- public 方法或对外契约变化
- 状态流、事件分发、回调、生命周期变化
- 需要结合宿主布局 / 调用方才能判断的资源绑定
- 登录、支付、隐私、埋点等核心链路
- 基类、通用工具、共享组件

## 验证升级

优先最小验证：`git diff --check` → `./gradlew :app:compileDebugKotlin`（`:base` 改动可改用 `:base:compileDebugKotlin`）→ 定向单元测试 `./gradlew :base:testDebugUnitTest`。静态阅读无法证明运行时行为时，如实说明证据缺口，不要因此升级严重度。

## 相关资产

- `AGENTS.md`：任务分级、执行闭环与 skill 路由
- `.dhcoder/rules/`：技术栈、架构、编码风格
- `.dhcoder/skills/android-review/SKILL.md`：通用评审流程
- `.dhcoder/skills/android-verify/SKILL.md`：编译/测试/lint 验证
- `.dhcoder/skills/diagnose/SKILL.md`：Bug 与性能回退诊断
