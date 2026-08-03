---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 6.1: Keep Theia And SVG Export Paint-Only

Status: review

## Story

As a platform maintainer,
I want Theia and SVG export to consume the same Presentation Document,
so that there is one visible drawing authority.

## Acceptance Criteria

1. Theia workbench rendering consumes Presentation Document facts for shapes, connectors, markers, labels, visibility, and paint order.
2. SVG export consumes the same Presentation Document facts for shapes, connectors, markers, labels, visibility, and paint order.
3. Theia does not snap endpoints, reroute connectors, infer topology, relabel, or interpret engineering domains.
4. SVG export does not snap endpoints, reroute connectors, infer topology, relabel, or interpret engineering domains.
5. Theia and SVG preserve connector route points exactly as emitted by Presentation Reality.
6. Theia and SVG use Presentation-owned visibility and paint order instead of local fallback order or renderer-owned filtering.
7. No Java2D, XML runtime authority, compatibility shim, milestone names, `V0`/`V1`, vague `Evidence`, `ProfessionalControlDrawing`, or renderer repair path is introduced.
8. Focused frontend/export tests, full regression, source-set hygiene audit, encoding audit, and `git diff --check` pass.

## Tasks / Subtasks

- [x] Establish failing paint-only tests first (AC: 1-8)
  - [x] Add or update SVG exporter tests proving export follows Presentation paint order and visibility.
  - [x] Add or update Theia/graph adapter tests proving route points are preserved exactly.
  - [x] Add tests or guards proving renderer code does not snap, reroute, infer topology, relabel, or apply domain logic.
- [x] Audit active renderer consumption path (AC: 1-7)
  - [x] Inspect `PresentationDocumentSvgExporter`.
  - [x] Inspect LSP Presentation payload/session path.
  - [x] Inspect graph-glsp projection adapter/source.
  - [x] Inspect Theia workbench model and edge layer.
  - [x] Record concrete findings in Dev Agent Record.
- [x] Refactor renderer path to paint facts only (AC: 1-7)
  - [x] Make SVG export honor Presentation paint plan order and visibility.
  - [x] Make Theia path honor Presentation paint facts without local repair.
  - [x] Delete stale fallback or repair logic touched by this story.
  - [x] Do not add migration shims.
- [x] Verify and update tracking (AC: 1-8)
  - [x] Run focused svg-renderer, ide/lsp, graph-glsp, and Theia frontend tests sequentially.
  - [x] Run broader `gradlew test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story proves only:

```text
Presentation Document -> SVG export
Presentation Document -> LSP payload -> graph-glsp -> Theia paint
```

It does not improve routing quality, create screenshots, change source syntax, change Spatial geometry, or claim professional drawing parity. Story 6.2 owns M39 product proof and screenshots.

### Current Code Intelligence

Use CodeGraph before editing. Active targets:

- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporter.kt`
- `kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporterTest.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`

### Architecture Requirements

- Presentation Reality owns paint facts only. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5]
- Renderer cannot repair truth. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7]
- Human names are product architecture. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-8]
- Athena is pre-1.0. Delete incompatible stale code, docs, examples, and tests directly. [Source: `AGENTS.md` - Pre-1.0 Architecture Rule]

### Previous Story Intelligence

Story 5.2 added `PresentationPaintPlan` and `PresentationPaintItem`.

Story 5.3 cleaned active Presentation naming:

- connector identity is now `routeId`;
- SVG metadata is `data-route-id`;
- trace names use Trace/Stats/Request/Path, not vague Evidence naming;
- no compatibility shim or renderer repair path was added.

Preserve this direct naming. Do not restore `routeIntentId` or stale `data-route-intent` into active Presentation surfaces.

### Testing Requirements

Use TDD. Write failing tests before production cleanup.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
cd integrations/graph-glsp; yarn test
cd ide/theia-frontend; yarn test
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-41 through FR-43]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5, AD-7, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E6 Story 6.1]
- [Source: `_bmad-output/implementation-artifacts/m39/5-2-build-labels-visibility-and-paint-order.md` - paint plan model]
- [Source: `_bmad-output/implementation-artifacts/m39/5-3-clean-presentation-naming-and-dead-paths.md` - active Presentation naming cleanup]
- [Source: `AGENTS.md` - Source-Set Hygiene Rule, Pre-1.0 Architecture Rule, Build Verification Rule, E2E Proof Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 5.3 entered review.
- Started development from sprint status after story creation.
- Confirmed renderer boundary: SVG exporter, LSP payloads, graph-glsp, and Theia model now consume `PresentationPaintPlan` as paint authority.
- Removed fallback publication paths: SVG export and LSP publication require `PresentationDocument.paintPlan`.
- Verified older active producers attach paint plans before publishing Presentation documents.
- Validation passed:
  - `.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test`
  - `.\gradlew.bat --no-daemon --console=plain :ide:lsp:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test`
  - `cd integrations/graph-glsp; yarn test`
  - `cd ide/theia-frontend; yarn test`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1`
  - `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`
  - `git diff --check` exited 0 with line-ending warnings only.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- SVG export now paints only `PresentationPaintPlan` items and respects Presentation-owned order and visibility.
- LSP publication now requires `paintPlan`; no null fallback or compatibility path remains in the active publication surface.
- Theia/graph-glsp path preserves Presentation route points and uses paint-plan visibility/order instead of local surface filtering.
- Active Presentation producers now attach real paint plans: `PresentationModelDeriver`, `AthenaCabinetProjectionCompiler`, and `AthenaProfessionalDrawingCompiler`.
- No Java2D, XML runtime authority, renderer snap/repair, milestone-named production class, or compatibility shim was introduced.

### File List

- _bmad-output/implementation-artifacts/m39/6-1-keep-theia-and-svg-export-paint-only.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporter.kt
- kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporterTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationModelDeriver.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCabinetProjectionCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaProfessionalDrawingCompiler.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationReferenceMarkerPayloadTest.kt
- integrations/graph-glsp/src/athena-glsp-projection-source.ts
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- integrations/graph-glsp/test/athena-graph-glsp-adapter.test.mjs
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs
- ide/theia-frontend/scripts/athena-m30-svg-bounds-regression.test.mjs

### Change Log

- 2026-08-02: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 5.3 output.
- 2026-08-02: Started development.
- 2026-08-02: Completed paint-only renderer path for SVG export, LSP payloads, graph-glsp, and Theia workbench model.
