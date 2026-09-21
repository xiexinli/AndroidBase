---
name: prd-to-spec
description: 将 PRD（产品需求文档）转换为结构化技术 Spec。适用于拿到产品需求后、编写实现计划或动代码之前。
---

# PRD → 技术 Spec

## Overview

把产品 PRD 转成工程师可直接据以编写实现计划的技术规格文档。
转换不是简单改写，而是先做**需求澄清与歧义标记**，再逐层拆解到功能、流程、数据、接口、异常与验收标准。
面向本仓库（AndroidBase）时，Spec 需贴近 Clean Architecture 分层（domain/data/feature/core）。

**开场声明：** "I'm using the prd-to-spec skill to convert this PRD into a technical spec."

**保存位置：** `docs/spec/YYYY-MM-DD-<feature-name>.md`（用户指定路径优先）。

## 转换流程

1. **需求澄清与歧义标记**
   - 逐条通读 PRD，找出含糊表述、前后冲突、未定义术语。
   - 不得臆造需求：无法确认的内容标记为 `⚠️ 待确认`，单独列出 `待澄清清单`。
2. **功能拆解**
   - 按用户可感知的功能点拆分，给出每个功能的：入口、交互流程、涉及页面/组件、状态。
3. **业务流程与状态机**
   - 对关键流程给出 `mermaid` 的 `sequenceDiagram` 或 `stateDiagram-v2`。
   - 明确每个状态的进入条件、停留行为、退出条件。
4. **接口 / 数据模型 / 权限 / 异常**
   - 列出涉及的后端接口（方法、路径、请求/响应字段、错误码）。
   - 定义核心数据模型字段与类型（对应 domain model / data DTO）。
   - 权限与登录态要求；异常场景（超时、断网、空数据、弱网）与兜底策略。
5. **验收标准**
   - 每条需求对应可验证的验收点，用 Given/When/Then 或检查清单表达，供后续写实现计划与测试直接引用。

## Spec 模板

````markdown
# [Feature] 技术 Spec

> 来源 PRD：[路径] ｜ 版本：[vX.X] ｜ 转换日期：YYYY-MM-DD

## 1. 需求澄清
- 已确认结论：…
- ⚠️ 待确认：…

## 2. 功能拆解
| 功能 | 入口 | 交互流程 | 状态 |
|------|------|----------|------|

## 3. 业务流程
```mermaid
sequenceDiagram
```

## 4. 数据模型
```kotlin
@Serializable
data class XxxDto(
  val id: String,
  ...
)
```

## 5. 接口约定
| 接口 | 方法 | 路径 | 关键字段 | 错误码 |

## 6. 异常与兜底
- 超时：…
- 断网：回退本地缓存（Room/DataStore）
- 空数据：…
- 权限不足：…

## 7. 验收标准
- [ ] Given …, When …, Then …
- [ ] …
````

## 与后续流程的衔接

- Spec 完成后，下一步使用 `.dhcoder/skills/writing-plans` 编写分步实现计划。
- 拆解粒度过大（跨多个独立子系统）时，建议按子系统拆成多个 Spec，各自独立计划与交付。
