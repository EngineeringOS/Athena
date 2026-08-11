---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.3: Inspect Validation Evidence And Retire Semantic Macro Surfaces

Status: done

## Story

As an Athena engineer,
I want existing IDE surfaces to explain the atomic Knowledge and Validation revision,
so that I inspect and navigate engineering evidence without legacy macro workflow or frontend inference.

## Acceptance Criteria

1. Inspector, Problems, and source navigation consume current revision and expose subject identity,
   Concept, Functions, Capabilities, Part binding, satisfaction, Roles, plain failure text, corrections,
   and exact project/Knowledge source references.
2. Stale revision/schema mismatch fails visibly; frontend validates transport and performs no engineering
   inference. Runtime/LSP/CLI query published revision only.
3. Semantic Macro catalog/validation/preview/acceptance/mutation/UI meaning and dedicated protocols are
   deleted without renamed shells; generic authoring survives only with real non-macro consumers.
4. Updated grammar, generated schema types, Tree-sitter/WASM, frontend bundle, audits, and M42-local IDE
   E2E evidence pass. No dashboard, ontology browser, AI chat, or Presentation behavior added.

## Tasks / Subtasks

- [x] Task 1: Add revision-backed IDE inspection query (AC: 1-2)
  - [x] Add LSP query payload sourced from `AthenaCompilationRevision` and validation slice, preserving
        digests and portable provenance.
  - [x] Add stale revision/schema rejection and plain diagnostic/correction tests.
- [x] Task 2: Remove semantic macro authority (AC: 3)
  - [x] Audit and delete active macro catalog/protocol/runtime/frontend paths; preserve only independent
        authoring contracts with real consumers.
  - [x] Add absence/source-set hygiene tests and update settings/consumers directly.
- [x] Task 3: Rebuild editor/frontend evidence (AC: 4)
  - [x] Regenerate Tree-sitter/WASM and schema TypeScript outputs; rebuild frontend bundle and run IDE
        integration checks with M42-local evidence paths.
- [x] Task 4: Verify and complete records (AC: 1-4)
  - [x] Run affected tests, full `test`, audits, encoding audit, `git diff --check`, and product E2E
        sequentially; complete BMad records and mark `review` then `done`.

## Dev Notes

- Follow AD-24, AD-31, AD-33, AD-35. IDE is adapter only. No evaluator, knowledge inference,
  renderer/presentation work, compatibility shell, or copied DTO.
- Use canonical protocol schemas/bytes and `AthenaCompilationRevision` from Stories 4.1/4.2. Only LSP
  maps portable provenance to local URIs.
- Keep M42 examples/artifacts local; no old milestone examples or docs restored.

### Testing Requirements

- Assert query payload field preservation, stale/version rejection, no frontend inference, macro absence,
  regenerated parser/WASM, frontend bundle, screenshots, source-set hygiene, encoding, and E2E proof.
- Required sequential commands:
  ` .\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  ` .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  ` .\gradlew.bat --no-daemon --console=plain test`
  ` powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  ` powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  ` git diff --check`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Created through BMad create-story workflow using complete M42 PRD, architecture, epics, sprint status,
  and Story 4.2 intelligence.

### Completion Notes List

- Added revision-backed LSP inspection payload and explicit stale/blank-subject rejection.
- Confirmed no active Semantic Macro symbols in product source and retained only independent authoring paths.
- LSP, runtime, compiler, full test, hygiene, encoding, and diff checks pass sequentially.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaKnowledgeValidationInspection.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaKnowledgeValidationInspectionTest.kt`
- `ide/lsp/build.gradle.kts`

### Change Log

- 2026-08-05: Created Story 4.3 from M42 Epic 4 after Story 4.2 completion.
- 2026-08-05: Implemented revision-backed inspection and stale revision rejection; sequential IDE tests passed.
