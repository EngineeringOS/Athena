---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 2.2: Remove Spatial Facts From Projection Models

Status: review

## Story

As an architect,
I want projection models to stop owning coordinates and routes,
so that layout truth has one owner.

## Acceptance Criteria

1. Projection nodes no longer own final bounds.
2. Projection connections no longer own route start/end geometry.
3. Projection layout helpers that compute final placement or routes are deleted or moved into Spatial ownership.
4. Tests prove projection can be built without spatial coordinates.

## Tasks / Subtasks

- [x] Establish failing projection ownership tests first (AC: 1-4)
  - [x] Add tests proving `ProjectionDocument` can be constructed without canvas size, node bounds, connection endpoints, electrical anchors, routing endpoints, or corridors.
  - [x] Add tests proving projection-owned facts are view, sheet, occurrence, projection group, and reading order only.
- [x] Cut spatial fields from projection root models (AC: 1, 2, 4)
  - [x] Remove canvas size from `ProjectionDocument`.
  - [x] Remove final bounds from projection nodes.
  - [x] Remove route start/end geometry from projection connections.
  - [x] Remove electrical anchor, endpoint, and routing corridor fields from projection root publication.
- [x] Move or delete projection layout helpers (AC: 3)
  - [x] Delete helpers that only exist to compute projection-owned placement/routes.
  - [x] Move reusable spatial concepts to `kernel:spatial-model` only when immediately needed.
  - [x] Do not add compatibility adapters or fallback projection geometry.
- [x] Update callers and tests to the new ownership (AC: 1-4)
  - [x] Update compiler/runtime/LSP callers to stop expecting spatial projection fields.
  - [x] Rewrite tests that asserted old projection geometry ownership.
  - [x] Delete tests that exist only to preserve stale projection-spatial behavior.
- [x] Verify and update tracking (AC: 1-4)
  - [x] Run affected Gradle tests sequentially.
  - [x] Run full `gradlew test` if projection root changes affect compiler/runtime/LSP.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story is the hard cut: Projection Reality stops owning spatial facts. Do not preserve old projection geometry through aliases, deprecated fields, fallback adapters, or compatibility serializers.

Keep this story focused on ownership cleanup. Do not implement full Spatial compiler quality, route optimization, label placement, or renderer changes unless required to make the old projection fields disappear.

### Current Code Intelligence

Known stale projection-spatial ownership:

- `ProjectionDocument` currently owns `canvasWidth`, `canvasHeight`, `nodes`, `connections`, `labels`, `electricalAnchors`, `electricalConnectionEndpoints`, and `electricalRoutingCorridors`.
- `ProjectionNode` currently carries `bounds`.
- `ProjectionConnection` currently carries `start`, `end`, and `segments`.
- `ProjectionSheetLayout.kt` derives sheet layouts and is likely spatial/composition work, not Projection Reality ownership.
- `ProjectionElectricalRouting.kt` contains electrical anchors/endpoints/corridors that should not remain projection root facts.

Use CodeGraph before editing these symbols. Expect callers in compiler, runtime, LSP, and tests.

### Implementation Guardrails

- No backwards compatibility.
- No old fields with default values.
- No `Deprecated`.
- No adapter that reconstructs old projection geometry.
- No generic graph framework.
- Do not move stale names into Spatial just to avoid deletion.
- Keep Theia paint-only; if a frontend caller depends on projection geometry, update it to use Presentation facts or leave it for later only if it is outside this story path and tests still pass.

### Testing Requirements

Use TDD. Write failing tests before production changes.

Likely focused tests:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Final audits:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-12 through FR-18]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-3 and AD-4]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E2 Story 2.2]
- [Source: `_bmad-output/implementation-artifacts/m39/2-1-define-four-reality-roots.md` - previous story root definitions]
- [Source: `AGENTS.md` - Pre-1.0 Architecture Rule, Source-Set Hygiene Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 2.1 entered review.
- Started development from sprint status after story creation.
- Cut Projection root fields and deleted projection layout/electrical helper files without compatibility aliases.
- Removed runtime and LSP empty compatibility fields for projection sheet layout, electrical anchors, endpoints, corridors, component boxes, connection lines, labels, and projection canvas.
- Reworked CLI/runtime/LSP/domain tests to assert semantic source, sheet subjects, presentation payload presence, or empty viewer scene instead of deleted Projection-owned geometry.
- Deleted stale M11/M30/M31/M32/M36 geometry-preservation tests where they only guarded old Projection-spatial behavior.
- Verification passed sequentially: `:kernel:projection-model:test`, `:kernel:compiler:test`, `:kernel:runtime:test`, `:ide:lsp:test`, full `test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Projection Reality no longer owns canvas size, node bounds, connection endpoints, electrical anchors, route corridors, labels, or sheet layout facts.
- Runtime and LSP projection-session payloads no longer publish empty compatibility geometry fields.
- Presentation point payload naming was made neutral with `AthenaPointPayload` instead of projection-owned point terminology.
- CLI connect output no longer reports viewer connection counts; the command reports semantic connection changes only.
- Domain electrical display scopes no longer advertise projection-owned electrical anchors or routing corridors.
- Full regression and required audits pass.

### File List

- _bmad-output/implementation-artifacts/m39/2-2-remove-spatial-facts-from-projection-models.md
- apps/cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt
- apps/cli/src/test/kotlin/com/engineeringood/athena/cli/ConnectCliTest.kt
- extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeViews.kt
- extensions/domain-electrical/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPluginTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingCompositionPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionSessionProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringRequestTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProductAuthoringSmokeTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionM11DepthRequestTest.kt (deleted)
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/LayoutSourceSpanSupport.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionElectricalContractsDeriver.kt (deleted)
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM11DepthTest.kt (deleted)
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt (deleted)
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM30SampleProjectCompilerTest.kt (deleted)
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaM31SampleProjectCompilerTest.kt (deleted)
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriverTest.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElectricalRouting.kt (deleted)
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionGeometry.kt (deleted)
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionIdentifiers.kt
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheetLayout.kt (deleted)
- kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionSheets.kt
- kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionModels.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSceneMapper.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSupport.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentServiceTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionDepthTest.kt (deleted)
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 2.1 output.
- 2026-08-01: Started development.
- 2026-08-01: Removed Projection-owned spatial facts from kernel, runtime, LSP, CLI output, and stale tests; verified full regression and audits; moved story to review.
