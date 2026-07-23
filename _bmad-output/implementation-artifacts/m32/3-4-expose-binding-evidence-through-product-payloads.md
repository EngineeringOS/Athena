---
status: review
baseline_commit: 8d12ba03c05b373b98fbc513e6c7f3f6fce962d6
story_id: 3.4
epic: 3
title: Expose Binding Evidence Through Product Payloads
---

# Story 3.4: Expose Binding Evidence Through Product Payloads

## Status

Review

## Story

As an Athena workbench and test consumer,
I want binding evidence in transport-safe payloads,
so that UI and proof tooling can explain package and profile choices without inferring them.

## Required Context

- PRD: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md`
- Addendum: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md`
- Architecture: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md`
- Epics: `_bmad-output/implementation-artifacts/m32/epics.md`
- Sprint: `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- Previous story: `_bmad-output/implementation-artifacts/m32/3-3-implement-binding-resolver-selection.md`

## Acceptance Criteria

1. Given package binding succeeds, when product-safe payloads are produced, then they include
   semantic subject id, engineering package id/version, Presentation Profile id, representation
   package id/version, descriptor id, variant, anchor map summary, label binding summary, resolver
   stage, and diagnostics.
2. Given package binding fails, when product-safe payloads are produced, then authority diagnostics
   are preserved without accepting renderer fallback or hiding the failed package/profile/manifest
   boundary.
3. Given Theia or future frontend adapters receive binding evidence, when they render or inspect a
   subject, then they can consume returned facts and do not need to infer package choice from file
   names, Graphic Resource internals, DOM, or CSS.
4. Given the story implementation is complete, when transport payloads, runtime adapters, smoke
   hooks, and docs are reviewed, then stale inferred-binding code is removed or ledgered and AC
   evidence is recorded.
5. Mandatory Polish/Purge Gate complete.

## Tasks/Subtasks

- [x] Add RED tests for successful Binding Resolution evidence payload mapping. (AC: 1,3)
- [x] Add RED tests for failed Binding Resolution evidence preserving authority diagnostics and
  fallback rejection. (AC: 2,3)
- [x] Implement transport-safe Binding Evidence payload models and mapper in package runtime
  boundary. (AC: 1..3)
- [x] Document product payload fields and Theia adapter boundary. (AC: 1..4)
- [x] Run focused package-runtime tests sequentially, then full regression sequentially; do not run
  Gradle concurrently on Windows. (AC: 1..4)
- [x] Complete mandatory polish/purge review and record final evidence. (AC: 4,5)

## Dev Notes

- Story 3.4 should map existing `BindingResolutionResult` facts into product-safe payloads. It
  should not rerun selection logic or create a new resolver.
- Payloads must be plain values suitable for LSP/runtime/frontend transport: strings, lists,
  maps/summaries, booleans, and diagnostic records.
- Do not make Theia or renderer code infer package choice from resource ids, file names, DOM nodes,
  CSS classes, or visible labels.
- This story may remain in `kernel/package-runtime` if no existing product payload transport exists
  yet. Later LSP integration can consume the same payload contract.
- Preferred implementation location:
  `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime`.
- Suggested files:
  - `BindingEvidencePayloads.kt`
  - `BindingEvidencePayloadMapper.kt`
  - `BindingEvidencePayloadTest.kt`
- Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` after text edits.

## Project Structure Notes

- Keep runtime evidence payloads separate from `kernel/package-model`; package-model remains
  descriptor/manifest contract-only.
- If docs mention Theia, state adapter-only consumption rather than implementation ownership.

## Testing Requirements

- Follow TDD: write failing payload tests before production code.
- Focused command: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`.
- Full regression command after story completion:
  `.\gradlew.bat --no-daemon --console=plain check`.
- Encoding audit command after docs/text edits:
  `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- RED: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  failed in `:kernel:package-runtime:compileTestKotlin` with unresolved
  `BindingEvidencePayloadMapper`.
- GREEN: `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test`
  passed after adding transport-safe Binding Evidence payload DTOs and mapper.
- REFACTOR VERIFY: focused `:kernel:package-runtime:test` passed again after documentation update.
- REGRESSION: `.\gradlew.bat --no-daemon --console=plain check` passed sequentially.
- ENCODING: `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  passed after text edits.
- PURGE: `git status --short` showed M32 artifacts and no staged/tracked `.tools` addition.

### Completion Notes List

- Added product-safe Binding Evidence payloads containing semantic subject id, engineering package
  id/version, Presentation Profile id, representation package id/version, descriptor id, variant,
  anchor map summary, label binding summary, resolver stage, diagnostics, and fallback state.
- Added mapper from `BindingResolutionRequest` plus `BindingResolutionResult`, preserving failure
  diagnostics without rerunning selection logic.
- Documented that frontend adapters consume binding evidence and must not infer package choices
  from Graphic Resource internals, file names, visible labels, DOM, or CSS.
- AC evidence:
  - AC1: `BindingEvidencePayloadTest.binding evidence payload maps successful resolution into
    transport safe facts`.
  - AC2: `BindingEvidencePayloadTest.binding evidence payload preserves failed authority
    diagnostics without fallback success`.
  - AC3: payload contract is plain string/list/boolean DTO data and docs define adapter-only
    consumption.
  - AC4: docs updated; purge review found no stale inferred-binding code touched by this story and
    no cleanup-ledger entry was required.
  - AC5: focused runtime test, full `check`, encoding audit, and workspace purge review recorded.

### File List

- `_bmad-output/implementation-artifacts/m32/3-4-expose-binding-evidence-through-product-payloads.md`
- `_bmad-output/implementation-artifacts/m32/sprint-status.yaml`
- `docs/usages/engineering-package-platform.md`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloadMapper.kt`
- `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloads.kt`
- `kernel/package-runtime/src/test/kotlin/com/engineeringood/athena/packageruntime/BindingEvidencePayloadTest.kt`

## Change Log

- 2026-07-22: Story created for M32 Epic 3 after Binding Resolver selection.
- 2026-07-22: Implemented Binding Evidence payload DTOs, mapper, docs, and tests.

## Mandatory Final Polish/Purge Gate

- Review touched and adjacent package runtime, LSP/runtime payload docs, tests, fixtures, and sprint
  artifacts.
- Remove dead/stale inferred-binding experiments or misleading frontend authority claims.
- Ledger any retained stale item in `_bmad-output/implementation-artifacts/m32/cleanup-ledger.md`
  with owner, reason, target milestone, and verification.
- Record AC-to-evidence mapping before moving the story beyond `review`.
