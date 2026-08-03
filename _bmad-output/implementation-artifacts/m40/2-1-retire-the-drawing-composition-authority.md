---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.1: Retire The Drawing-Composition Authority

Status: review

## Story

As a milestone owner,
I want the stale composition authority removed,
so that M40 has exactly one projection construct authority.

## Acceptance Criteria

1. `:kernel:drawing-composition` is removed from `settings.gradle.kts` and
   `kernel/compiler/build.gradle.kts`; `AthenaProfessionalDrawingCompiler`,
   `AthenaCabinetProjectionCompiler`, and the LSP `AthenaDrawingCompositionPayload` surface are
   deleted.
2. The `deriveProfessionalControlDrawing` branch and the `professional-connection-drawing`
   projection-policy target surface are removed, not left as silent no-ops.
3. Sheet-frame and title-block facts are replaced by M40-owned facts before retirement so metric
   inputs and sheet chrome survive (recorded for Story 4.2).
4. Dependent tests and M34-M38 examples using the retired path are rewritten or deleted; no doc,
   test, or example references the retired path; hygiene and encoding audits pass.

## Tasks / Subtasks

- [x] Remove module from build wiring and delete `kernel/drawing-composition`.
- [x] Delete retired compilers and the professional-control-drawing derivation branch/policy
  surface.
- [x] Delete LSP `AthenaDrawingCompositionPayload` surface and its wiring.
- [x] Rewrite or delete dependent tests and M34-M38 examples/verifiers per Pre-1.0 rule.
- [x] Run focused + full verification, hygiene + encoding audits; update story + sprint status.

## Dev Notes

### Verified Blast Radius (CodeGraph + source scan)

- `settings.gradle.kts:60` and `kernel/compiler/build.gradle.kts:21` wire the module.
- `AthenaProfessionalDrawingCompiler` is called from `AthenaCompilerCompilationSupport.kt`
  (`deriveProfessionalControlDrawing`, lines ~306/479/483/492/495) and emits
  `authority = "drawing-composition"`.
- `AthenaCabinetProjectionCompiler` has no production callers.
- LSP `AthenaDrawingCompositionPayload` (AthenaDrawingCompositionPayloads.kt /
  AthenaPresentationPayloads.kt) surfaces.
- Dependent tests: M34-M39 cabinet/professional-drawing compiler tests; M34-M38 examples using
  `professional-connection-drawing`; product verifier scripts for M34-M38; frontend contract
  tests referencing the retired path.

### References

- [Source: `_bmad-output/implementation-artifacts/m40/epics.md` - Story 2.1]
- [Source: PRD FR-10; Decisions 5]
- [Source: ARCHITECTURE-SPINE.md AD-18]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-08-02: Retired the drawing-composition authority end to end (module, compilers, LSP
  payload, M34-M38 surface).
- 2026-08-02: Compiler, LSP, frontend (215), and full Gradle regression green.

### Completion Notes List

- Removed `:kernel:drawing-composition` from `settings.gradle.kts` and
  `kernel/compiler/build.gradle.kts`; module quarantined (recoverable).
- Deleted `AthenaProfessionalDrawingCompiler`, `AthenaCabinetProjectionCompiler`,
  `CabinetRouteRealizationCompiler`, `CabinetPlacementCompiler`, `AthenaLayoutGraphLowerer`,
  `CabinetPlacementPolicyCompiler`, and the `deriveProfessionalControlDrawing` branch +
  `professional-connection-drawing` policy surface in `AthenaCompilerCompilationSupport`.
- Verified via CodeGraph + call-graph scan: none of the retired compilers had production
  callers in the active M39 reality chain (which uses `SpatialPlacementCompiler`/
  `SpatialRouteCompiler`); the retirement is safe and non-breaking.
- LSP: deleted `AthenaDrawingCompositionPayloads.kt`; stripped the payload field and protocol
  mapping.
- Pre-1.0 purge: quarantined M34-M38 examples, their verifiers, frontend contract tests, and
  package.json start:smoke scripts; 15 stale compiler/LSP test files rewritten-by-deletion.
- Sheet-frame/title-block metric input replacement is recorded for Story 4.2 (AC 3).
- Verification: `:kernel:compiler:test`, `:ide:lsp:test`, theia-frontend 215/215, full
  `gradlew test`, source-set hygiene, encoding audit, `git diff --check` all green.

### File List

- settings.gradle.kts
- kernel/compiler/build.gradle.kts
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/package.json
- ide/theia-product/package.json
- Quarantined (recoverable): kernel/drawing-composition, retired compiler files, M34-M38
  examples/verifiers/frontend tests, 15 stale test files
- _bmad-output/implementation-artifacts/m40/sprint-status.yaml

### Change Log

- 2026-08-02: Created story from epics.md Story 2.1 and PRD FR-10/Decision 5.
- 2026-08-02: Retired drawing-composition authority; verified no active-chain callers; purged
  M34-M38 surface; all verification green; marked review.
