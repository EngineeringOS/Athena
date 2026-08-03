---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 5.2: Build Labels, Visibility, And Paint Order

Status: review

## Story

As an evaluator,
I want the final drawing to have explicit label, visibility, and paint order facts,
so that renderer behavior is deterministic.

## Acceptance Criteria

1. Presentation Document carries explicit paint facts for every visible shape, connector, marker, and label.
2. Every paint fact has target id, visibility, and paint order.
3. Every label has a target connector or occurrence and an explicit position.
4. `SpatialToPresentationTransformation` delegates paint planning to a focused Presentation authority instead of burying order logic inline.
5. Theia and SVG export remain paint-only; they consume Presentation facts and do not infer missing visibility or order.
6. Missing target, visibility, label position, or paint order fails closed with plain diagnostics before publication.
7. New names remain concrete and short. Do not add vague `Evidence`, `ProfessionalControlDrawing`, milestone names, `V0`/`V1`, generic graph framework, universal `Fact` base class, or compatibility shim.

## Tasks / Subtasks

- [x] Establish failing paint plan tests first (AC: 1-7)
  - [x] Add tests proving every occurrence, connector, marker, and connector label receives a paint fact.
  - [x] Add tests proving paint facts carry target id, visible flag, and order.
  - [x] Add tests proving labels carry explicit connector/occurrence target and position.
  - [x] Add tests proving invalid paint facts fail closed.
  - [x] Add naming guard coverage for new Presentation paint files and tests.
- [x] Add Presentation paint model (AC: 1-3, 6-7)
  - [x] Create short concrete model names for paint facts.
  - [x] Keep paint facts in `presentation-model`, not compiler-only structs.
  - [x] Reuse existing connector labels and markers where possible.
  - [x] Keep model validation plain and direct.
- [x] Implement Presentation paint planner (AC: 1-4, 6)
  - [x] Create a focused compiler class that derives paint facts from a `PresentationDocument`.
  - [x] Assign deterministic order: shapes first, connectors second, markers third, labels last.
  - [x] Fail when a visible item lacks target, order, or label position.
- [x] Wire Spatial to Presentation transformation (AC: 4-5)
  - [x] Remove inline paint-order derivation from `SpatialToPresentationTransformation`.
  - [x] Delegate final paint planning to the Presentation authority.
  - [x] Preserve connector endpoint trust and Story 5.1 connection paint behavior.
- [x] Verify and update tracking (AC: 1-7)
  - [x] Run focused compiler and presentation-model tests sequentially.
  - [x] Run spatial and projection model tests because transformation boundaries are affected.
  - [x] Run broader `gradlew test`.
  - [x] Run source-set hygiene audit, encoding audit, and `git diff --check`.
  - [x] Update this story File List, Completion Notes, Change Log, and sprint status.

## Dev Notes

### Scope Boundary

This story builds only:

```text
Presentation-owned paint facts for labels, visibility, and order
```

It does not improve routing, move endpoints, change source syntax, change Theia behavior, change SVG export behavior, or capture screenshots.

### Current Code Intelligence

Use CodeGraph before editing. Current direct targets:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt`
  - current document has occurrences, connectors, markers, and optional `drawingComposition`.
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt`
  - current connector labels have text, point, bounds, display, provenance, and snapshot id;
  - label target is implicit today and should become explicit.
- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDrawingComposition.kt`
  - current `PresentationDrawingStructureFact(kind = "paint-order")` is an older broad structure fact;
  - M39 should prefer a concrete paint-plan model instead of hiding visibility/order in generic structure facts.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
  - currently creates paint-order structure facts inline in `paintOrderFacts`;
  - Story 5.2 should delegate this to a Presentation paint authority.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt`
  - Story 5.1 authority for connector line, label, and marker ids. Preserve it.

Expected new/changed files are likely:

- `kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationPaintPlan.kt`
- `kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationPaintPlanTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompiler.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompilerTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`

### Architecture Requirements

- Presentation Reality owns visible shapes, connector paint facts, strokes, labels, markers, visibility, theme result, and paint order. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5]
- Theia and SVG export must not snap endpoints, infer topology, reroute, relabel, or apply domain rules. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-7]
- New M39 naming must be direct and human-readable. [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-8]
- Clean architecture wins over compatibility. Delete stale incompatible paths instead of adding shims. [Source: `AGENTS.md` - Pre-1.0 Architecture Rule]

### Implementation Guidance

- Prefer direct names: `PresentationPaintPlan`, `PresentationPaintItem`, `PresentationPaintCompiler`.
- Avoid `Evidence`, `Grammar`, `Professional`, `Intent`, `V0`, `V1`, or milestone tokens in new names.
- Do not delete `PresentationDrawingComposition` in this story unless needed for clean compile; Story 5.3 owns broader naming cleanup.
- Keep paint planning deterministic and boring:

```text
occurrence -> connector -> marker -> label
```

- A label target can be the connector id for connector labels. Do not invent label domain meaning.

### Previous Story Intelligence

Story 5.1 established:

- `ConnectionPaintCompiler` owns connector line, labels, and marker ids.
- `SpatialToPresentationTransformation` now delegates connector appearance to Presentation-owned code.
- Optional paint overrides affect Presentation facts only.

Story 5.2 should follow the same delegation pattern for visibility and paint order.

### Testing Requirements

Use TDD. Write failing tests before production code.

Focused checks:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
```

Regression and hygiene:

```powershell
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### References

- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md` - FR-34 through FR-40]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md` - AD-5, AD-7, AD-8]
- [Source: `_bmad-output/implementation-artifacts/m39/epics.md` - M39-E5 Story 5.2]
- [Source: `_bmad-output/implementation-artifacts/m39/5-1-build-dynamic-connection-appearance.md` - connection paint authority pattern]
- [Source: `AGENTS.md` - Source-Set Hygiene Rule, Pre-1.0 Architecture Rule, Build Verification Rule]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created after Story 5.1 entered review.
- Started development from sprint status after story creation.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added `PresentationPaintPlan` and `PresentationPaintItem` as concrete Presentation-owned paint facts.
- Added `PresentationPaintCompiler` with deterministic order: shape, connector, marker, label.
- Connector labels now carry explicit target ids; stale callers were updated directly.
- `SpatialToPresentationTransformation` now delegates paint planning to Presentation-owned code instead of inline paint-order structure facts.
- Verification passed: focused paint plan/compiler/transformation tests, compiler/presentation/spatial/projection module tests, full `gradlew test`, source-set hygiene audit, encoding audit, and `git diff --check`.

### File List

- _bmad-output/implementation-artifacts/m39/5-2-build-labels-visibility-and-paint-order.md
- _bmad-output/implementation-artifacts/m39/sprint-status.yaml
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationConnectorPayloadTest.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationConnectorCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompiler.kt
- kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/PresentationPaintCompilerTest.kt
- kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationOccurrenceModels.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationPaintPlan.kt
- kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationReality.kt
- kernel/presentation-model/src/test/kotlin/com/engineeringood/athena/presentation/PresentationPaintPlanTest.kt
- kernel/svg-renderer/src/test/kotlin/com/engineeringood/athena/renderer/svg/PresentationDocumentSvgExporterTest.kt

### Change Log

- 2026-08-01: Created story from corrected M39 PRD, architecture spine, sprint plan, and Story 5.1 output.
- 2026-08-01: Started development.
- 2026-08-01: Completed labels, visibility, and paint order facts and marked ready for review.
