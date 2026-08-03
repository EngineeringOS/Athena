---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.2: Lower Route Candidates Into Exact Connectors

Status: done

## Story

As an engineer,
I want every visible Connection lowered from exact endpoint evidence,
so that no line starts or ends at a random computed point.

## Acceptance Criteria

1. One strict `PresentationConnector` carries required Connection, Port, binding, occurrence, Anchor,
   endpoint point, route, line-class, and source evidence.
2. Route candidate first and last points are replaced with exact placed Anchor points and must equal
   them after normalization.
3. Route normalization removes zero-length points and merges redundant orthogonal segments only; it
   does not search for a better path.
4. Fewer than two points, non-orthogonal geometry, ambiguity, missing evidence, or incompatible
   endpoints block presentation with plain diagnostics.
5. Accepted `RouteFact` authority, fallback quality states, nullable endpoint IDs, `(0,0)` and
   body-center defaults, and duplicate connector lowerers are deleted from active visible publication.
6. Focused lowering, endpoint equality, normalization, invalid-route, trace, affected module,
   hygiene, encoding, stale/fallback search, and `git diff --check` gates pass sequentially.

## Tasks / Subtasks

- [x] Lock strict connector contract with tests (AC: 1, 2)
  - [x] Add presentation-model tests proving connector endpoints require Connection, Port, binding,
        occurrence, Anchor, exact point, line class, route, and trace evidence.
  - [x] Assert connector route first and last point equal source and target endpoint points exactly.
  - [x] Reject nullable or blank endpoint evidence and ambiguous endpoint identities.
- [x] Add route candidate normalization tests (AC: 2, 3, 4)
  - [x] Cover endpoint replacement from placed Anchors.
  - [x] Cover zero-length point removal and redundant orthogonal segment merge.
  - [x] Reject fewer than two points after normalization and non-orthogonal interior geometry.
- [x] Refactor compiler lowering path in place (AC: 1-5)
  - [x] Convert current route output from accepted visible `RouteFact` authority into untrusted
        candidate input for compiler lowering.
  - [x] Lower visible output through one strict `PresentationConnector` path.
  - [x] Remove duplicate connector lowering and visible publication paths that bypass placed Anchors.
- [x] Keep boundaries clean (AC: 5)
  - [x] Do not add Athena source syntax for route bends, coordinates, line style, labels, or renderer
        behavior.
  - [x] Do not add Theia endpoint repair, endpoint snapping, graph-edge reconstruction, or Java2D.
  - [x] Keep SVG geometry-only and Athena source SSOT.
- [x] Verify sequentially (AC: 6)
  - [x] Run focused presentation and compiler tests first.
  - [x] Run affected Gradle modules one command at a time.
  - [x] Run source-set hygiene, encoding audit, stale/fallback search, and `git diff --check`.

## Dev Notes

### Current Architecture Boundary

M38 owns attachment truth only. M39 may improve placement. M40 may improve routing. Story 2.2 must not
search for a professional route. It only makes the current route candidate safe for visible paint by
attaching endpoints to placed Anchors and rejecting invalid geometry.

No normal-source drawing language. No `drawing.trust` package. No Java2D. No XML authority. No
compatibility shim. No stale fallback.

### Previous Story Intelligence

Story 2.1 established:

- `PresentationGraphicOccurrence` is the current placed occurrence contract.
- `placedAnchors` carry exact placed Anchor points plus trace evidence.
- Terminal bindings reuse placed Anchor points.
- LSP and Theia receive placed Anchors; frontend does not repair endpoints.

Story 2.2 must consume `placedAnchors` and make visible connectors use them directly.

### Current Code Signals

CodeGraph route path for this story:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrences.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/EndpointAttachmentValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`

Current `RouteFactSnapshot` / `RouteFact` may still exist during this story only as untrusted
candidate input. Public visible output must be strict `PresentationDocument.connectors`.

### Implementation Guardrails

- Evolve existing product types in place. Do not create a parallel connector model.
- Required connector evidence must be typed and non-null.
- The compiler is the only owner of Port, binding, occurrence, Anchor, and route candidate joins.
- Route first/last points must be exact placed Anchor points after normalization.
- Interior points must stay orthogonal. If not, fail before publication.
- Line class must be resolved by compiler facts, not guessed by Theia.
- Diagnostics name subject, problem, and correction plainly.

### Testing Requirements

Run Gradle sequentially only:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Forbidden search:

```powershell
rg -n "body-center|center fallback|endpoint repair|renderer repair|endpoint snapping|graph-edge reconstruction|accepted RouteFact|RouteQualityState.FALLBACK|nullable endpoint|second connector|toPresentationConnector" kernel extensions ide -g "*.kt" -g "*.ts" -g "*.tsx" -g "!**/build/**"
```

Expected: no active fallback/repair/duplicate connector authority. Negative tests may mention
rejected behavior.

### References

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md`
- `_bmad-output/implementation-artifacts/m38/epics.md`
- `_bmad-output/implementation-artifacts/m38/2-1-place-body-and-anchors-with-one-transform.md`
- `AGENTS.md`

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 epics, PRD, addendum, architecture spine, Story 2.1 completion record, and CodeGraph route/connector exploration.
- 2026-07-31: Dev started from sprint order after Story 2.1 reached review.
- 2026-07-31: Added strict `PresentationConnector` endpoint model and route candidate lowerer.
- 2026-07-31: Fixed endpoint joins to compare route-qualified anchors against placed local Anchor bindings.
- 2026-07-31: Removed visible connector publication from raw `RouteFact` conversion; `connectorsForRendering()` now returns compiled connectors only.
- 2026-07-31: Aligned cabinet route classification to authored physical route channel evidence when explicit connection intent is absent.
- 2026-07-31: Replaced route quality fallback state with degraded quality and removed active forbidden fallback scan hits.

### Completion Notes

- Strict connector endpoint evidence is now required in presentation model, LSP payloads, GLSP payloads, and Theia model payloads.
- `PresentationConnectorCompiler` lowers route candidates into exact endpoint connectors using placed occurrence terminal bindings and line-class evidence.
- Route first/last points are normalized to exact placed Anchor points; short or non-orthogonal lowered routes fail before publication.
- Old projection deriver connector lowering now starts/ends at endpoint Anchor positions instead of raw projection line points.
- No Athena syntax, renderer repair, Java2D, XML authority, or SVG semantic authority was added.
- Verification passed sequentially on 2026-07-31.

### File List

- `_bmad-output/implementation-artifacts/m38/2-2-lower-route-candidates-into-exact-connectors.md`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrences.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationConnectorContractTest.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationGraphicOccurrenceAnchorContractTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM34CabinetRenderPathDeletionGateTest.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteConstraintsAndFacts.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnostics.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/RouteQualityDiagnosticsTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-source.d.ts`
- `integrations/graph-glsp/lib/athena-glsp-projection-adapter.js`

### Change Log

- 2026-07-31: Created Story 2.2 context and marked ready for dev.
- 2026-07-31: Implemented strict endpoint connector lowering and moved story to review.

### Verification

- `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:routing-model:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :kernel:package-runtime:test` passed.
- `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- Active forbidden scan passed with no production hits.
- Full forbidden scan has only negative test-name hits for `center fallback`.
- `git diff --check` passed; it reported line-ending warnings only.
