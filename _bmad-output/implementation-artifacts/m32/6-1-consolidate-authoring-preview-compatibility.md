---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 6.1
epic: 6
title: Consolidate Authoring Preview Compatibility
---

# Story 6.1: Consolidate Authoring Preview Compatibility

## Status

Review

## Story

As an Athena authoring-runtime maintainer,
I want old preview-session compatibility consolidated or explicitly versioned,
so that M32 does not carry hidden M31 transaction authority drift.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Cleanup ledger: `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- Previous story: `_bmad-output/implementation-artifacts/m32/5-3-polish-professional-demo-layout-and-density.md`
- Epic 5 retrospective: `_bmad-output/implementation-artifacts/m32/epic-5-retro-2026-07-22.md`

## Acceptance Criteria

1. Given `AthenaAuthoringSessionRuntimeService` preview/session compatibility callers, when
   CodeGraph caller review and runtime tests run, then each caller is migrated into governed
   Semantic Authoring Transaction runtime or versioned as a read-only legacy preview API with
   explicit docs.
2. Given migrated or retained compatibility paths, when regression tests run, then M31 transaction
   behavior remains authoritative and no hidden mutable source path is left.
3. Given the story implementation is complete, when authoring runtime, callers, tests, docs, cleanup
   ledger, and sprint artifacts are reviewed, then stale preview compatibility artifacts are removed
   or ledgered and AC evidence is recorded.
4. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Run CodeGraph caller review for `AthenaAuthoringSessionRuntimeService` and record the
  compatibility surface. (AC: 1)
- [x] Add RED tests proving retained preview/session compatibility is read-only/versioned and does
  not expose mutable source authority. (AC: 1,2)
- [x] Implement migration or explicit read-only legacy versioning at the authoring runtime boundary.
  (AC: 1,2)
- [x] Update docs and cleanup ledger/action status for the retained or removed compatibility path.
  (AC: 3)
- [x] Run focused tests sequentially, then full regression sequentially; do not run Gradle
  concurrently on Windows. (AC: 1..3)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 3,4)

## Dev Notes

- Use CodeGraph before grep/read on `AthenaAuthoringSessionRuntimeService`.
- Preserve M31 governed Semantic Authoring Transaction authority. The preview/session API may
  remain only if it is explicitly read-only/versioned and cannot commit source mutations.
- Do not touch unrelated M32 package sample proof unless compatibility cleanup requires a test
  update.
- If a stale compatibility item is retained, update `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target, and verification.
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Testing Requirements

- Follow TDD: write failing runtime compatibility tests before implementation.
- Focused command should target the runtime/authoring module touched by implementation.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- CODEGRAPH: `codegraph node AthenaAuthoringSessionRuntimeService` and `codegraph callers AthenaAuthoringSessionRuntimeService` reviewed the runtime service and found no direct class callers; `codegraph explore` showed active use through LSP protocol files and existing runtime tests.
- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test` failed before implementation because `compatibilityContract` was unresolved.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test` passed after adding `AthenaAuthoringPreviewCompatibilityContract`.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` exited 0 with `BUILD SUCCESSFUL`.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` exited 0 with `Encoding audit passed.`
- PURGE: `git status --short` showed the runtime service/test modifications plus M32 artifacts; `.tools` was not present.

### Completion Notes List

- Added an explicit `legacy-preview-readonly-v1` compatibility contract to
  `AthenaAuthoringSessionRuntimeService`.
- Verified the retained preview/session API declares no mutable source authority and governed
  acceptance still requires governed authorities.
- Existing M31 tests continue to prove plain preview decisions do not mutate canonical state and
  governed acceptance executes through `SemanticAuthoringTransactionRuntime`.
- Updated docs and cleanup ledger with the explicit retained-read-only compatibility decision.
- AC evidence:
  - AC1: CodeGraph service/caller review plus `AthenaAuthoringSessionRuntimeServiceTest` records
    the versioned read-only contract.
  - AC2: existing and new runtime tests verify no hidden mutable source path; full regression
    passed.
  - AC3: docs and cleanup ledger updated; stale implicit compatibility was closed as explicit
    read-only legacy API.
  - AC4: encoding audit and purge/status gate completed.

### File List

- `_bmad-output/implementation-artifacts/m32/6-1-consolidate-authoring-preview-compatibility.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
- `docs/usages/engineering-package-platform.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeServiceTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 6 authoring preview compatibility cleanup.
- 2026-07-22: Versioned preview/session compatibility as read-only legacy API and marked story ready for review after focused and full verification.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent authoring runtime code, callers, tests, docs, cleanup ledger, and
  sprint artifacts.
- Remove stale preview compatibility artifacts or explicitly ledger retained read-only legacy API
  behavior with owner and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
