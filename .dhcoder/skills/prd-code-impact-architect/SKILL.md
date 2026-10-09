---
name: prd-code-impact-architect
description: PRD 代码影响分析子 Skill。通常由 prd-to-app-spec 在获得 Markdown 需求文本后调用，用于输出代码落点、模块边界、技术方案草稿和可执行验收点。仅当用户明确只要求影响面分析、不创建正式 App Spec、不实施代码且不执行仓库开发闭环时才可独立使用；不得替代 prd-to-app-spec 处理 PRD→Spec、功能开发、验证或归档任务。
---

# PRD 代码影响与架构方案

## 概述

这个 Skill 用来把 PRD 转成“可实施”的技术方案草稿，并且方案必须建立在当前代码事实上。

路由边界：
- PRD→正式 App Spec、功能实施、验证、评审或归档：使用 `prd-to-app-spec`，本 Skill 仅作为其子流程。
- 用户明确只要求代码影响分析，且明确不创建 Spec、不实施：本 Skill 可独立使用。
- 非 Markdown PRD 的格式转换不由本 Skill 负责。

默认输出三份文档：
- `prd-extract.md`：PRD 事实提取
- `change-map.md`：功能点到代码落点映射
- `architecture-proposal.md`：可评审技术方案草稿

如果仓库有任务文档规范，优先写到当前任务目录；根目录索引文件只保留索引信息。

## 输入约定

必需输入：
- PRD 原文（文件路径、粘贴文本或可访问内容）
- 目标仓库根目录

可选输入：
- 优先级功能点
- 明确不做范围
- 发版约束（日期、风险等级、灰度要求）

如果没有 PRD 内容，必须先停下来索取 PRD，禁止猜测需求。

## 工作流

### 第一步：提取 PRD 事实

只提取 PRD 明确写出的信息：
- 用户可见的功能改动
- 接口/数据要求
- 状态和生命周期变化
- 异常与边界规则
- 验收口径

使用模板：`references/prd-extract-template.md`

### 第二步：映射代码影响面

扫描目标仓库，把每条需求映射到明确代码位置。根据仓库技术栈识别：
- 页面 / View / Controller / ViewModel / Model / Component
- 路由入口、导航入口、DeepLink 或模块公开接口
- 网络请求、数据源、Repository / Service / API 层
- 账号、权限、会话、配置、本地存储或缓存链路
- 埋点、日志、监控与跟踪点（如存在）

先用 `rg` 快速检索，映射要写清楚“模块 + 文件 + 用途”。

使用模板：`references/change-map-template.md`

### 第三步：生成架构方案草稿

输出一个最小且可执行的方案：
- 模块边界和责任归属
- 数据流和依赖方向
- 接口/协议变更点
- 新旧链路并存约束
- 明确不做项
- 可执行验证计划（命令/测试/可观察行为）

遵循项目硬约束：
- 不做兜底补丁
- 不做猜测性启发式修补
- 只做与需求直接相关的最小改动
- 除非 PRD 明确要求，否则不改核心链路

使用模板：`references/architecture-proposal-template.md`

### 第四步：架构师交接包

交付一个架构师可直接评审的简明包：
- 需求摘要（改什么）
- 影响面映射（改哪里）
- 技术方案（怎么改）
- 风险清单（可能坏在哪）
- 验证清单（如何证明完成）

## 自检清单

提交前必须确认：
- 每条 PRD 需求都映射到了至少一个代码落点；没有落点的必须标记“待确认”
- 映射结果没有违反仓库约束
- 没有超出 PRD 的额外需求
- 验证项可执行（命令、测试或可观察行为）

## 推荐命令

先根据目标仓库语言与架构调整检索词。可从这些通用入口开始：

```bash
rg --files
rg -n "route|router|deeplink|navigation|api|service|repository|account|auth|permission|cache|storage|analytics|tracking"
rg -n "TODO|FIXME|mock|Mock"
```

移动端仓库可额外检索 View / Controller / ViewModel / Model / DeepLink / Router 等关键词；Web 仓库可额外检索 route / page / component / hook / api / server action 等关键词。

## 输出风格

- 结论先行
- 中文大白话
- 不角色扮演
- 不写冗长背景
- 不使用 P0/P1/P2 这类分级术语
