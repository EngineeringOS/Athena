---
baseline_commit: 593d0a1dc86c0bac126fbde0501cc07051180346
---

# Story M36-E5.S1: Expose Typed Trace And Diagnostics

Status: done

## Story

As an engineer or AI agent,
I want to inspect every rendered Cabinet fact back to its source and compiler evidence,
so that I can understand and correct the governing engineering intent.

**Requirements:** FR-30, FR-31.

## Acceptance Criteria

1. Given compiled Cabinet occurrences and facts, occurrences, Ports, Anchors, Connections,
   PlacementFacts, and RouteFacts expose stable source, package, representation, and compiler
   trace.
2. Typed bridge, binding, connection, placement, route, crossing, and degraded-planner diagnostics
   are available through the IDE/LSP path.
3. Diagnostics identify relevant source spans and evidence rather than raw planner or SVG objects.
4. Renderer and workbench retain no independent engineering or layout authority.

## Tasks / Subtasks

- [x] Add failing tests for trace and diagnostics exposure (AC: 1-4)
  - [x] Cover source, package, representation, and compiler trace on rendered Cabinet facts.
  - [x] Cover bridge, binding, connection, placement, route, crossing, and degraded-planner
    diagnostics.
  - [x] Prove diagnostics carry source spans and evidence instead of raw planner or SVG objects.
- [x] Expose typed trace payloads through IDE/LSP contracts (AC: 1-4)
  - [x] Reuse normalized compiler facts instead of inventing a second trace model.
  - [x] Keep trace payloads source-first and compiler-owned.
  - [x] Preserve deterministic ordering and stable identifiers across payloads.
- [x] Wire diagnostics into the workbench and session protocols (AC: 2-4)
  - [x] Surface diagnostics through the existing LSP/presentation session boundary.
  - [x] Preserve renderer purity and keep workbench state non-authoritative.
  - [x] Keep source spans and compiler evidence attached to every diagnostic.
- [x] Run story evidence gate (AC: 1-4)
  - [x] Run sequential Gradle tests, encoding audit, and `git diff --check`.
  - [x] Record AC-to-evidence, file list, and completion notes.

## Dev Notes

- The current compiler path already emits normalized route facts, marker-bearing presentation
  payloads, and structured diagnostics. This story should expose those facts cleanly instead of
  inventing a parallel trace model.
- Likely seams: `PresentationModelDeriver`, `AthenaM35CabinetProjectionCompiler`,
  `AthenaProfessionalDrawingCompiler`, `ide/lsp` session protocols, and `presentation-model`.
- Preserve the rule that renderer and workbench consume normalized facts only. Trace is a compiler
  and transport concern, not a raw SVG or DOM concern.
- No XML compatibility paths. The project is unreleased, so stale incompatible trace or diagnostic
  paths may be removed rather than preserved.

### Project Structure Notes

- Likely touchpoints:
  - `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/`
  - `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaM35CabinetProjectionCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Keep text assets UTF-8.
- Do not add legacy XML compatibility paths.

### References

- `_bmad-output/implementation-artifacts/m36/epics.md` - Story 5.1, FR-30, FR-31.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-28-m36/prd.md` - FR-30, FR-31,
  NFR-1, NFR-3, NFR-4, NFR-8, NFR-9.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-28-m36/ARCHITECTURE-SPINE.md`
  - AD-9, AD-10.
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
  - route-fact and trace-bearing presentation document contract.
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationModelContractTest.kt`
  - presentation trace contract coverage.
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationRouteAttachmentContractTest.kt`
  - route attachment and quality evidence shape.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
  - typed diagnostics publishing contract.
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`
  - source-backed IDE/LSP support path.

## Dev Agent Record

### Agent Model Used

Codex (GPT-5)

### Debug Log References

- 2026-07-29: Fixed the `TerminalAnchorFact` LSP payload mapping to emit string ids only, then verified the full Gradle test suite plus encoding audit and `git diff --check`.

### Completion Notes List

- Verified the M35/M36 trace-bearing cabinet projection path exposes source provenance, projection ids, route fact trace, and structured diagnostics through the LSP presentation session.
- Corrected the `TerminalAnchorFact` transport mapper so `subjectId`, `occurrenceId`, `portId`, and `physicalTerminalId` are serialized as stable string values instead of wrapper types.
- Ran `.\gradlew.bat --no-daemon --console=plain test`, `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`, and `git diff --check`.

### File List

- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingCompositionPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationTracePayloads.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaDiagnosticsPublishingTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaM35DedicatedCabinetProjectionSmokeTest.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepresentationSourceLspSupportTest.kt`

## Change Log

- 2026-07-29: Created M36-E5.S1 story for typed trace and diagnostics exposure.
- 2026-07-29: Completed typed trace exposure and diagnostics transport verification for the M35/M36 cabinet projection path.
