---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.1: Place Body And Anchors With One Transform

Status: done

## Story

As an engineer,
I want Element bodies and terminals placed together,
so that a visible terminal cannot drift away from its connection point.

## Acceptance Criteria

1. One current `GraphicOccurrence` path is evolved in place from `PresentationGraphicOccurrence`; no
   parallel trust/placement/occurrence model is added.
2. Given one `RepresentationDefinition` and one current placement, one transform places body
   geometry, bounds, Anchors, terminal marker geometry, hit geometry, and label slots exactly once.
3. Each placed Anchor is normalized once to the existing integer presentation/grid point.
4. Downstream consumers reuse that exact placed Anchor point without global offset, second rounding,
   renderer transform, body-center fallback, or endpoint reconstruction.
5. Missing, stale, duplicate, or conflicting placement/Anchor evidence blocks publication with
   source-spanned/plain diagnostics.
6. Obsolete occurrence types and duplicate placement transformations are deleted in the same change.
7. Focused transform, rotation, mirror, bounds, Anchor, negative, affected module, hygiene, encoding,
   and `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Lock one-transform occurrence behavior with tests (AC: 1-4)
  - [x] Add focused tests proving body bounds and every Anchor point use the same transform.
  - [x] Cover translation, rotation, mirror/negative scale if existing transform contract supports it.
  - [x] Assert placed Anchor point is integer-normalized once and reused by terminal marker/hit facts.
- [x] Add negative publication guards (AC: 5)
  - [x] Fail missing placement evidence before drawing publication.
  - [x] Fail missing Anchor, duplicate placed Anchor, stale occurrence/definition mismatch, and
        conflicting transform evidence.
  - [x] Diagnostics must name subject, problem, and source correction plainly.
- [x] Refactor current occurrence path in place (AC: 1, 2, 4, 6)
  - [x] Evolve `PresentationGraphicOccurrence` into the current `GraphicOccurrence` responsibility
        without creating another model.
  - [x] Move placed Anchors into occurrence facts with exact points and source/provenance evidence.
  - [x] Delete duplicate transform/offset/rounding logic when superseded by placed Anchor facts.
- [x] Keep renderer and language boundaries clean (AC: 4)
  - [x] Theia/LSP consume placed facts; no frontend endpoint repair or inference.
  - [x] No source syntax for coordinates, transforms, route bends, paint, labels, or renderer work.
- [x] Verify sequentially (AC: 7)
  - [x] Run focused placement/occurrence tests first.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run source-set hygiene, encoding, stale/fallback search, and `git diff --check`.

## Dev Notes

### Current Architecture Boundary

M38 owns exact attachment trust, not placement quality. M39 may improve placement later, but it must
consume the same placed occurrence and Anchor authority. Do not introduce new engineer-facing layout
syntax. Do not create `drawing.trust`, Java2D, second renderer, XML authority, compatibility shim, or
parallel occurrence contract.

### Previous Story Intelligence

Stories 1.1-1.3 established:

- `RepresentationDefinition` is the sole intrinsic geometry and Anchor authority.
- SVG/native geometry compiles into the same contract.
- Port-to-Anchor binding is explicit and carries binding ID, Port ID, Anchor ID, and provenance.
- Anchor geometry remains neutral; Port meaning stays in Athena source/binding evidence.

Story 2.1 must consume those facts and create placed Anchor facts once.

### Current Code Signals

CodeGraph found current placed/visible path:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrences.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CabinetPlacementCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CabinetPlacementPolicyCompiler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`

Use CodeGraph before editing named symbols. Worktree is dirty; do not revert unrelated files.

### Implementation Guardrails

- Existing `PresentationGraphicOccurrence` should become the single placed occurrence contract. Rename
  only if the rename stays human-readable and does not create churn without benefit.
- Placed Anchors must be typed facts, not re-derived from graphic body, terminal text, route endpoint,
  body center, or frontend coordinate math.
- Terminal marker/hit geometry belongs to the occurrence Anchor fact. Connectors only reference it in
  later stories.
- If an old active path cannot provide placed Anchors, block publication or delete active publication.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Forbidden search:

```powershell
rg -n "body-center|center fallback|endpoint repair|renderer repair|second transform|PresentationAnatomy|RepresentationBodyAuthority|routeFactSnapshot" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**"
```

Expected: no active fallback/repair authority. Negative tests may mention rejected behavior.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/1-3-bind-every-port-to-one-anchor.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 epics, PRD, addendum, architecture spine, CodeGraph placed occurrence exploration, and Story 1.3 completion record.
- 2026-07-31: Dev started from sprint order after Story 1.3 reached review.
- 2026-07-31: Added placed Anchor occurrence contract, compiler/LSP/Theia payload propagation, and focused validation tests.
- 2026-07-31: Verification passed sequentially: focused tests, affected Gradle modules, source-set hygiene, encoding audit, stale/fallback search review, and `git diff --check`.

### Completion Notes

- Story context created for BMad dev-story execution.
- `PresentationGraphicOccurrence` now carries `placedAnchors` with exact placed points and trace evidence; terminal bindings reuse those points.
- Professional drawing and cabinet projection compilers publish placed Anchors from one transformed geometry path.
- LSP payloads and Theia workbench consume placed Anchor facts directly; no frontend endpoint repair added.
- Validation blocks duplicate placed Anchors and terminal bindings that point outside placed Anchor facts.

### File List

- `_bmad-output/implementation-artifacts/m38/2-1-place-body-and-anchors-with-one-transform.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrences.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrenceAnchorContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalGraphicOccurrenceCompilerTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`

### Change Log

- 2026-07-31: Created Story 2.1 context and marked ready for dev.
- 2026-07-31: Implemented one-transform placed Anchor occurrence contract and marked ready for review.
