---
name: android-review
description: Run this repository's generic code review workflow. Use for code review, diff review, MR review, interactive review, or automated review wrappers that need diff-first scope handling, context expansion rules, and verification escalation.
---

# Review

Use this skill as the generic review workflow driver. Keep review semantics in `REVIEW.md`; this skill owns the review process.

## Workflow

1. Confirm the review scope: current diff, specified commit, specified files, or specified module.
2. Inspect changed code before broad repository exploration.
3. Expand context only as needed to verify ownership, lifecycle, resources, contracts, state flow, or regression risk.
4. Read `REVIEW.md` and the relevant `.dhcoder/rules/` files before producing findings.
5. Output high-signal findings only; do not turn the review into a full-repo audit.

Do not start from repo-wide wandering when the diff already defines the entry point.

## Scope Expansion

Expand context for:

- public method or contract changes
- state flow, event dispatch, callback, or lifecycle changes
- resources that require host layout or code checks
- login, payment, privacy, analytics, or other critical paths
- base classes, helpers, common utils, and shared components

When expanding, answer:

- what behavior changed
- who owns and triggers it
- whether lifecycle, threading, null-safety, and resource binding still hold
- whether the risk is introduced, triggered, or clearly amplified by this change

## Architecture Routing

If the diff touches layered code, read `.dhcoder/rules/architecture.md` before judging:

- `domain/` must stay free of Android framework dependencies
- `data/` is the only layer allowed to touch network/DB
- `feature/` ViewModels must not call Retrofit or DAOs directly

## Review Checklist

- Verify the change stays within the claimed scope.
- Anchor each finding to a concrete file, method, symbol, or code path.
- Support severe findings with evidence, not broad guesses.
- Use `android-verify` when compile, unit-test, or build evidence is needed.
- If no concrete bug is found, say that plainly instead of padding findings.

## Verification Escalation

Prefer the smallest check that proves the claim:

- `git diff --check`
- `./gradlew :app:compileDebugKotlin`
- targeted unit tests `./gradlew :app:testDebugUnitTest`
- manual verification notes only when runtime behavior cannot be proven from static reads

If evidence is missing, state the gap instead of upgrading severity.
