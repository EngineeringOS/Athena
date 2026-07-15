---
baseline_commit: 3c43ccd4d7057e437c2014294a8a7a325763dca5
---

# Story 4.1: Project Package-Aware Diagnostics Through LSP

Status: done

## Story

As an IDE user,
I want package/import/linking diagnostics shown through normal IDE diagnostic surfaces,
so that package-aware authoring errors are visible without custom frontend semantics.

## Acceptance Criteria

1. Given a compiler-owned project semantic graph snapshot with package-aware diagnostics, when LSP diagnostics are published for an opened or changed Athena document, then relevant diagnostics for that document are projected from snapshot records.
2. LSP diagnostics preserve stable Athena diagnostic codes, severity, message, and source/span provenance from `ProjectSemanticDiagnostic`.
3. LSP diagnostics use Theia/VS Code-like Problems/editor conventions: normal `textDocument/publishDiagnostics`, `Diagnostic.source`, `Diagnostic.code`, `Range`, and `relatedInformation` when present.
4. The implementation does not add frontend-local import resolution, Tree-sitter semantic diagnostics, canvas logic, desktop-viewer logic, raw filesystem import authority, or Kotlin Compose UI work.
5. Existing compiler semantic diagnostics and governed engineering knowledge diagnostics continue to publish unchanged through the current LSP Problems flow.
6. Verification runs sequentially through a focused LSP diagnostics test, `:kernel:compiler:test`, `:ide:lsp:test`, and encoding audit.

## Tasks / Subtasks

- [x] Add failing LSP coverage for project semantic diagnostics (AC: 1-5)
  - [x] Add or extend an `ide/lsp` diagnostics test that opens a governed repository document with an unresolved or unavailable package-aware import.
  - [x] Assert the published LSP diagnostic uses the compiler-owned package diagnostic code, severity, source, message, and range.
  - [x] Assert existing semantic and knowledge diagnostic publication still uses the normal Problems flow.
- [x] Project compiler-owned snapshot diagnostics into LSP diagnostics (AC: 1-4)
  - [x] Reuse `AthenaCompiler` project semantic graph APIs: `buildProjectSemanticGraph`, `resolveProjectSemanticImports`, `emitProjectSemanticDiagnostics`, `indexProjectSemanticDeclarations`, and `linkProjectSemanticReferences` as needed for diagnostics.
  - [x] Map `ProjectSemanticDiagnostic` to LSP `Diagnostic` without changing compiler diagnostic contracts.
  - [x] Resolve diagnostic source-unit provenance to the tracked LSP document URI without introducing frontend or raw-path import resolution.
  - [x] Preserve snapshot `graphId` internally in the LSP-side projection path for M18 traceability.
- [x] Preserve existing LSP behavior and authority boundaries (AC: 3-5)
  - [x] Keep `CompilerCompilationResult.toLspDiagnostics()` behavior for parse, semantic, and knowledge diagnostics.
  - [x] Keep Tree-sitter syntax UX out of semantic diagnostic publication.
  - [x] Keep canvas, desktop-viewer, and Kotlin Compose code untouched.
- [x] Run scoped verification sequentially (AC: 6)
  - [x] Run focused Story 4.1 LSP diagnostics test.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`.
  - [x] Run `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`.
  - [x] Run `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`.

## Dev Notes

- Current LSP diagnostic publication is in `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`.
  - `publishDiagnostics(...)` tracks the document through `AthenaLanguageFeatures.trackDocument(...)`.
  - `CompilerCompilationResult.toLspDiagnostics()` currently maps parse diagnostics, compiler semantic diagnostics, and governed knowledge diagnostics.
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt` owns tracked document state and currently stores `CompilerCompilationResult` plus document-local navigation index.
- Compiler-owned package diagnostic contracts already exist:
  - `ProjectSemanticDiagnostic` in `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/semantic/ProjectSemanticDiagnosticModels.kt`.
  - `ProjectSemanticDiagnosticProjector` emits import diagnostics such as `semantic.import.package.unavailable`, `semantic.import.namespace.unavailable`, and `semantic.import.namespace.ambiguous`.
  - `ProjectSemanticReferenceLinker` emits reference diagnostics such as `semantic.reference.unresolved`.
- Reuse the existing M18 semantic pass sequence. Do not create a second package diagnostics model in LSP.
- LSP projection should be transport-only: compiler snapshot in, `org.eclipse.lsp4j.Diagnostic` out.
- For Theia/VS Code-like behavior, use normal LSP fields:
  - `source`: prefer `Athena package semantic` or another Athena-owned semantic source string distinct from syntax and knowledge diagnostics.
  - `code`: `ProjectSemanticDiagnostic.code.value`.
  - `severity`: map `ERROR`, `WARNING`, and `INFO` to LSP severities.
  - `range`: derive from `ProjectSemanticDiagnostic.sourceSpan`.
  - `relatedInformation`: derive from `ProjectSemanticDiagnostic.relatedLocations` when a URI can be resolved for the related source unit.
- Keep all file references in artifacts relative to this implementation folder or repo root.
- Do not touch `apps/desktop-viewer`, Kotlin Compose UI, frontend semantic resolution, Tree-sitter semantic behavior, or canvas code for this story.

### Previous Story Intelligence

- Story 3.6 added repository-backed M18 linking/lowering fixtures under `examples/m18/linking-lowering-proof/` and proved the semantic pass order through compiler tests.
- Story 3.5 added `ProjectSemanticLinkedLowerer`; no lowering work is required for LSP diagnostics except preserving the shared snapshot authority boundary.
- Stories 3.1-3.4 established declaration indexing, reference linking, cross-package binding, and capability provenance. Reuse those passes instead of duplicating symbol/import logic in LSP.
- Recent verification pattern: focused test first, then `:kernel:compiler:test`, then `:ide:lsp:test`, then encoding audit. Gradle commands must run sequentially.

### Project Structure Notes

- Expected implementation modules:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Compiler contracts should remain in `kernel/compiler`; only add compiler code if the LSP projection cannot reuse an existing public compiler API cleanly.
- Test fixtures may reuse or mirror `examples/m18/linking-lowering-proof/` behavior, but LSP tests should stay under `ide/lsp`.

### References

- [Source: `epics.md` - Epic 4, Story 4.1]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md` - FR-8, SM-3, SM-4]
- [Source: `../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md` - Sections 8-11]
- [Source: `../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md` - AD-7, AD-8, AD-12, AD-14]
- [Source: `3-6-add-linking-and-lowering-proof-fixtures.md`]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.publish*package*` failed before LSP projected project semantic snapshot diagnostics.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.publish*package*` failed until the test fixture materialized a canonical lock before LSP initialization.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest.publish*package*` passed after implementation and fixture correction.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest` passed after code-review follow-up fixes.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed after code-review follow-up fixes.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after code-review follow-up fixes.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after code-review follow-up fixes.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest` failed on the current-source-unit regression before the LSP snapshot used package/path identity for multi-source project snapshots.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaDiagnosticsPublishingTest` passed after the current-source-unit and package-root URI mapping fixes.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed after the final Story 4.1 review follow-up.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed after the final Story 4.1 review follow-up.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed after the final Story 4.1 review follow-up.

### Completion Notes List

- Ultimate context engine analysis completed - LSP diagnostics scope prepared for compiler-owned project semantic snapshot projection.
- Added package-aware project semantic snapshot tracking to the LSP document state with internal `graphId` preservation.
- Published compiler-owned `ProjectSemanticDiagnostic` records through normal LSP diagnostics for package-aware documents.
- Added LSP coverage for an unresolved package-aware import using a canonical governed repository fixture.
- Kept legacy non-package documents on the existing diagnostics path and left Tree-sitter, canvas, desktop-viewer, and Kotlin Compose untouched.
- Resolved code review findings by building project semantic snapshots from governed source units with dirty-buffer overlay, deriving package identity from authored package declarations, carrying source-unit URI mappings for cross-source related information, skipping project semantic diagnostics on parse failures, and publishing spanless project diagnostics as document-level diagnostics.
- Resolved the final review follow-up by preserving the current source-unit id when sibling source units are indexed, avoiding content-based dirty-buffer matching, and resolving related source-unit URIs through each package's actual source root.

### File List

- `_bmad-output/implementation-artifacts/m18/4-1-project-package-aware-diagnostics-through-lsp.md`
- `_bmad-output/implementation-artifacts/m18/sprint-status.yaml`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`

## Change Log

- 2026-07-15: Created Story 4.1 from Epic 4, PRD FR-8, architecture AD-7/AD-8/AD-14, and Story 3.6 implementation intelligence.
- 2026-07-15: Implemented compiler-owned project semantic diagnostic projection through LSP and verified focused/full regression checks.
- 2026-07-15: Addressed Story 4.1 code-review follow-ups for multi-source project snapshots, current source-unit identity, and cross-source URI mapping.

## Senior Developer Review (AI)

Review Date: 2026-07-15

Outcome: Approve

Findings: All resolved.

Notes:

- Blind Hunter, Acceptance Auditor, and Edge Case Hunter findings were received and addressed.
- Fixed current-buffer-only project snapshot behavior by including governed sibling source units and dirty-buffer overlay in LSP project semantic diagnostics.
- Fixed package identity selection so package-aware diagnostics derive from authored package declarations when the governed graph admits the package, with non-admitted packages diagnosed by the compiler snapshot.
- Fixed cross-source related diagnostic information by carrying a source-unit-to-URI map into the LSP projector.
- Fixed parse-failure duplication by skipping project semantic diagnostics unless the main compiler result is a compilation success.
- Fixed spanless project diagnostics by publishing them at document level instead of dropping them.
- Fixed current source-unit tracking for multi-source snapshots by matching package/path identity and covering the regression where a sibling source unit previously suppressed current-document project diagnostics.
- `git diff --cached --check` passed.
