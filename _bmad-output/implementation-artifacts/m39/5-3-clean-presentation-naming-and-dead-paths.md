---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.3: Clean Presentation Naming And Dead Paths

Status: review

## Story

As a maintainer,
I want presentation code names to be direct,
so that the model is readable by humans and LLMs.

## Acceptance Criteria

1. New M39 presentation path uses direct product names: connection paint, paint plan, paint item, label, visibility, order.
2. Stale or vague M39 presentation names such as `Evidence`, `ProfessionalControlDrawing`, milestone names, `V0`, `V1`, `Compatibility`, `Intent`, and empty wrapper names are removed from active M39 Presentation path.
3. Any stale compatibility classes, methods, docs, examples, and tests that violate M39 Presentation ownership are deleted or refactored directly.
4. The active M39 Presentation path remains: Spatial Reality -> Presentation compiler -> Presentation Document -> Theia/SVG paint-only.
5. Existing non-M39 historical artifacts are not used as active M39 proof or architecture authority.
6. Source-set hygiene audit, encoding audit, and full tests pass.

## Tasks / Subtasks

- [x] Establish failing naming and stale-path tests first (AC: 1-6)
  - [x] Add tests or audits proving new M39 Presentation classes avoid banned terms.
  - [x] Add tests proving active M39 Presentation path has no renderer repair or compatibility route.
  - [x] Add tests proving M39 stories/examples do not depend on M36/M37/M38 examples.
- [x] Audit active Presentation path (AC: 1-5)
  - [x] Inspect `SpatialToPresentationTransformation`, `ConnectionPaintCompiler`, `PresentationPaintCompiler`, and Presentation model files.
  - [x] Inspect Theia/SVG/LSP payload path only for active M39 Presentation ownership violations.
  - [x] Record findings in Dev Agent Record.
- [x] Refactor or delete stale active Presentation names (AC: 1-5)
  - [x] Rename or delete stale M39-active names directly.
  - [x] Remove unused or misleading M39 Presentation compatibility logic.
  - [x] Do not add migration shims.
- [x] Verify and update tracking (AC: 1-6)
  - [x] Run focused compiler, presentation-model, svg-renderer, and ide/lsp tests sequentially.
  - [x] Run broader `gradlew test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story cleans the active M39 Presentation path only.

It does not attempt a repository-wide purge of every historical `Evidence` word if it belongs to old non-M39 artifacts. It must remove or refactor any stale term that is part of active M39 Presentation ownership.

### Current Code Intelligence

Use CodeGraph before editing. Active M39 Presentation files include:

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationPaintPlan.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporter.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt`
- `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt`

### Architecture Requirements

- Presentation Reality owns paint facts only. It cannot change engineering truth, projection grouping, or spatial geometry. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5]
- Renderer cannot repair truth. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7]
- Human names are product architecture. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-8]
- Athena is pre-1.0. Delete stale incompatible code, docs, examples, and tests directly. [Source: `AGENTS.md` - Pre-1.0 Architecture Rule]

### Previous Story Intelligence

Story 5.1 added `ConnectionPaintCompiler`.
Story 5.2 added `PresentationPaintPlan`, `PresentationPaintItem`, and `PresentationPaintCompiler`.

These are the intended direct names. Preserve them unless a clearer shorter name is needed.

### Testing Requirements

Use TDD. Write failing tests before production/code cleanup.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:svg-renderer:test
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-39, NFR-1 through NFR-4]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5, AD-7, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E5 Story 5.3]
- [Source: `_bmad-output/implementation-artifacts/m39/5-1-build-dynamic-connection-appearance.md` - connection paint naming]
- [Source: `_bmad-output/implementation-artifacts/m39/5-2-build-labels-visibility-and-paint-order.md` - paint plan naming]
- [Source: `AGENTS.md` - Source-Set Hygiene Rule, Pre-1.0 Architecture Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 5.2 entered review.
- Started development from sprint status after story creation.
- Added `PresentationNamingHygieneTest` red first to ban stale M39 Presentation names and renderer repair paths.
- Audited active Presentation path across presentation-model, compiler, svg-renderer, ide/lsp, graph-glsp, and Theia frontend.
- Final stale-name scan found only routing-domain `routeIntentId` internals outside active Presentation paint path.
- Verified sequentially: `:kernel:presentation-model:test`, `:kernel:compiler:test`, `:kernel:svg-renderer:test`, `:ide:lsp:test`, `integrations/graph-glsp yarn test`, `ide/theia-frontend yarn test`, full `gradlew test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Active Presentation connector identity now uses `routeId`; SVG export uses `data-route-id`.
- Package trace naming now uses `PresentationPackageTrace` / `packageTrace`, not vague Evidence naming.
- Presentation trace/edit/support names now use direct Trace/Stats/Request/Path naming.
- Presentation structure naming now uses `structure`, not `structureIntent`.
- No compatibility shim, migration path, or renderer repair logic was added.

### File List

- _bmad-output/implementation-artifacts/m39/5-3-clean-presentation-naming-and-dead-paths.md
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDrawingComposition.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/GraphicOccurrenceTraceTable.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/CabinetSelectionAndEditBoundary.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationResolvedSubject.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationNamingHygieneTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt
- kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporter.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationTracePayloads.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaDrawingCompositionPayloads.kt
- integrations/graph-glsp/src/athena-glsp-projection-source.ts
- integrations/graph-glsp/src/athena-glsp-projection-adapter.ts
- ide/theia-frontend/src/browser/athena-graph-presentation-model.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx
- ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
- ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 5.2 output.
- 2026-08-01: Started development.
- 2026-08-02: Cleaned active M39 Presentation naming and stale path terms; verified focused, frontend, full regression, hygiene, encoding, and diff checks.
