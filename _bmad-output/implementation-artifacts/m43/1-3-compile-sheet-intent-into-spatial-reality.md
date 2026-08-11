---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 1.3: Compile Sheet Intent Into Spatial Reality

Status: done

## Story

As a compiler, I can apply Sheet Intent to Spatial compilation,
so authored anchors influence placement while Spatial remains the only geometry authority.

## Acceptance Criteria

1. Sheet Companion intent compiles through existing M41 Spatial pipeline; exact anchor, route, validation, ordering, and trace invariants remain green.
2. Missing or invalid companion produces no Presentation Reality but does not corrupt non-presentation compilation.
3. Authored placement and lock survive recompile and source reorder; unplaced initial placement remains deterministic.
4. Bounds, overlap, route, label, and trace failures publish no partial Spatial document.
5. Focused compiler/runtime/LSP/frontend tests, full Gradle, encoding, hygiene pass.

## Tasks / Subtasks

- [x] Integrate Sheet Companion intent with Spatial compilation (AC: 1, 2, 3)
  - [x] Ensure exact companion locator outcomes flow to fail-closed publication diagnostics.
  - [x] Preserve authored anchors, locks, and source provenance through compile/recompile.
  - [x] Keep Spatial as sole geometry authority; no frontend geometry derivation.
- [x] Close Spatial validation/proof gaps (AC: 1, 4)
  - [x] Verify bounds, overlap, route, label, trace failure atomicity.
  - [x] Verify deterministic initial layout and source reorder stability.
- [x] Add product-facing regression tests (AC: 2, 3, 5)
  - [x] Cover missing companion, invalid companion, valid rolling-shutter companion, and source reorder.
  - [x] Verify no partial Spatial publication on failure.
- [x] Verify and record (AC: 5)
  - [x] Run focused tests, full Gradle, frontend, encoding, hygiene.
  - [x] Complete story records and set status `review` only after evidence passes.

## Dev Notes

- Adopt M41 Spatial compiler and tests. Change only contract gaps revealed by M43 shared vectors.
- Spatial documents remain internal compiler outputs until Story 2-1 converts them to canonical `AthenaDiagramScene`.
- No raw Projection/Spatial LSP payload may be restored.

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Added compiler-level publication regressions for valid, missing, malformed, and reordered Sheet Companion inputs.
- Fixed invalid-companion diagnostics to carry a valid geometry source identity; missing and ambiguous companion outcomes now fail closed before Spatial publication.

### Completion Notes List

- Sheet Companion intent is applied before Spatial compilation and remains compiler-owned.
- Any companion discovery, parse, mapping, geometry, route, or final validation failure publishes an empty `CompilerSpatialDocuments` result while preserving engineering projections.
- Source declaration reorder produces equal canonical Spatial Reality for the rolling-shutter proof.
- No frontend geometry derivation or legacy Projection/Spatial transport was restored.

### File List

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/RollingShutterLiveDocumentProofTest.kt`

### Change Log

- 2026-08-06: Integrated fail-closed Sheet Companion discovery/parse diagnostics and added atomic Spatial publication and reorder regression proof.
- 2026-08-06: Revalidated final compiler, LSP, product, and hygiene gates; status set to `done`.
