---
name: android-verify
description: Use when verifying Android changes in this repository, especially for Gradle compile, unit-test, or resource/build checks. Covers picking the narrowest verification proof for AndroidBase.
---

# Android Verify

Use this skill for Android verification work in this repository.

## Scope

- Repo-wide Android build and verification behavior
- Especially useful for `app/` changes
- Covers Gradle compile, resource validation, and unit-test selection

## Goals

- Pick the narrowest verification that proves the change
- Separate real failures from pre-existing / unrelated warnings
- Reuse the known-good command patterns for this repo

## Read First

1. Read `references/task-map.md`.
2. If the task is a code review, combine this skill with `android-review`.
3. If the task is bug or performance regression diagnosis, combine this skill with `diagnose`.

## Core Rules

- Always inspect the diff first so the verification scope matches the change.
- This repo has **no product flavors**, so `debug` task names are unambiguous.
- Run `git diff --check` for touched files before or alongside heavier verification.
- Treat unrelated warnings and deprecations as background noise unless they fail the build or point to touched files.

## Workflow

1. Inspect changed files and classify the change:
   - Kotlin logic
   - Compose UI
   - tests only
   - build logic / Version Catalog
2. Choose the smallest proof:
   - compile for Kotlin / Compose code
   - unit tests for changed tested logic
   - broader assemble only when compile is insufficient
3. Run `git diff --check` on the touched files.
4. Run the exact Gradle task from `references/task-map.md`.
5. Report:
   - command run
   - whether it passed
   - whether warnings were pre-existing noise or new actionable failures

## Reporting Guidance

Keep verification reporting crisp:

- `Verified`: exact commands that passed
- `Blocked`: exact command and blocker
- `Noise`: pre-existing warnings, only if relevant
- `Risk`: what still needs manual or visual validation

## Guardrails

- Do not claim success from static inspection alone when a command can verify the change.
- Do not escalate immediately; first try inside the sandbox, then escalate only if access restrictions actually block the command.
- Do not turn repo-wide deprecation spam into review findings unless it is caused by the current diff.
