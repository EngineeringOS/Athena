---
story: 3.2
epic: 3
title: Paint And Select Professional Connections In Konva
status: done
baseline_commit: 9ca7c80239d38d9e2a4880886e71e27e2923c41b
created: 2026-08-11
---

# Story 3.2: Paint And Select Professional Connections In Konva

Status: done

## Story

As an engineer,
I want thin selectable connections with invisible generous hit targets,
so that editing is precise without polluting printed output.

## Acceptance Criteria

1. Given accepted `SceneConnection` publication, when Konva paints at any zoom, then orthogonal/shared
   segments, Junctions, Crossing bridges, Interruption anchors, and selected annotations retain constant
   professional screen weight; hidden hit geometry never enters visible or exported paint.
2. Given one shared segment or explicit topology marker, when multiple connections reference it, then Konva
   paints it once from published identity and never infers Junction/Crossing/Interruption meaning from geometry.
3. Given a visible connection or invisible generous hit target, when the engineer selects it, then the adapter
   publishes semantic connection/net identity and source trace, paints only a restrained selection overlay,
   and does not change style target, annotation visibility, source, or engineering state.
4. Given desktop and narrow center editor sizes, when canvas fits, resizes, pans, and zooms, then top/left edge
   rulers stay aligned and the drawing remains unclipped, untwisted, non-overlapping, and centered in the full
   available editor panel.
5. Given default paint, when inspected visually or by adapter tests, then no oversized arrows, thick black
   connections, persistent Port rings, source paths, AST links, internal ids, debug labels, or unselected Port
   names appear.

## Tasks / Subtasks

- [x] Paint `SceneConnection` exactly (AC: 1, 2, 5)
  - [x] Replace the temporary single-polyline path with segment paint that deduplicates published shared segment
    identities and preserves deterministic connection/z-order.
  - [x] Paint explicit Junction, Crossing bridge, and Interruption markers from `SceneConnection.markers` only;
    keep line/marker weights constant through pan/zoom and keep arrows absent unless explicitly selected.
  - [x] Paint only published connection annotations; do not derive labels or expose ids/trace/debug strings.
- [x] Add invisible connection interaction geometry (AC: 1, 3, 5)
  - [x] Add one generous, transparent hit target per selectable connection path without changing visible paint,
    printed SVG, or scene authority.
  - [x] Extend `DiagramSelection` and selection overlay for `connection`, carrying `connectionId` and `traceId`.
  - [x] Route connection selection through `AthenaSemanticSelectionService` without changing style target or
    annotation visibility and without source mutation.
- [x] Preserve responsive engineering document behavior (AC: 4)
  - [x] Keep stage fit/resize/pan/zoom inside the full center panel and preserve narrow top/left ruler alignment.
  - [x] Verify connection hit/marker screen sizes do not scale with stage zoom and drawing bounds stay unclipped.
- [x] Add regression and contract proof (AC: 1-5)
  - [x] Extend frontend adapter tests for exact topology paint, shared-segment deduplication, invisible hit targets,
    selection identity/trace, no label flood, and no renderer topology inference.
  - [x] Extend layout/ruler tests for desktop/narrow canvas ownership and zoom-safe screen-space paint.
  - [x] Run frontend contract generation check and `yarn test`; run encoding and source-set hygiene audits.

## Dev Notes

### Authority Chain

```text
Athena source -> Connection IR -> Spatial plans/topology -> SceneConnection -> Konva paint/hit adapter
```

Konva is disposable. It consumes `SceneConnection` only and may own viewport, selection preview, and invisible
hit geometry. It cannot infer engineering topology, change source, cache Connection Reality, or publish paint
facts upstream.

### Current State And Required Change

- `konva-diagram-adapter.ts` now reads `scene.connections`, but flattens all segments into one line, paints no
  published topology markers/annotations, and keeps `routeGroup` non-listening. Connections cannot be selected.
- Existing occurrence/Port selection already carries semantic identity and trace through one callback. Extend
  that contract; do not build a second selection service.
- `AthenaPresentationWidget` already resolves selection trace through `AthenaSemanticSelectionService` and must
  keep `styleTarget` independent from canvas selection.
- Rulers are DOM edge chrome outside Konva. Never move ruler paint into the canvas.

### Professional Paint Rules

- Follow `AGENTS.md` Engineering Document Visual Golden Rule and
  `draft/screenshort/equipement_d'un_volet_roulant.png`.
- Visible connection width is approximately one screen pixel with butt/miter defaults. Selection may use a
  subtle screen-space highlight but must not replace the canonical black line.
- Hit width may be generous but must be transparent. Do not use visible Port rings or scene hit radii as paint.
- Junctions are tiny; Crossing bridge ownership and Interruption pairs come from scene markers. Shared segments
  paint once by published segment identity.
- Annotation text comes only from `SceneConnection.annotations`; default empty list means no connection text.

### Technical Stack

- Konva `10.3.0`, existing single adapter import boundary.
- React/Theia widget remains declarative chrome; imperative geometry stays in `KonvaDiagramAdapter`.
- TypeScript generated contracts are compiler-owned; update generator/schema only if contract mismatch is found.

### Expected Files / Areas

- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/athena-semantic-selection-model.ts` only if current selection shape requires it
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-editor-rulers.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`

### Previous Story Intelligence

- Story 3.1 replaced `SceneRoute` with schema-v2 `SceneConnection`; no compatibility aliases exist.
- Segments retain path order. Shared segment identity is stable. Marker ownership and selected annotations are
  explicit. Konva must not recalculate them.
- LSP/publication JSON and generated TypeScript expose `connections`, `segments`, `markers`, and `annotations`.
- Full Gradle, frontend, encoding, and hygiene verification passed at Story 3.1 closure.

### Verification

```powershell
Set-Location ide/theia-frontend
yarn contracts:check
yarn test
Set-Location ../..
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

## References

- [Source: `_bmad-output/planning-artifacts/m46/epics.md`, Epic 3 / Story 3.2]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-10-m46/prd.md`, FR-12, NFR-3]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-10-m46/ARCHITECTURE-SPINE.md`, AD-11 through AD-15]
- [Source: `_bmad-output/implementation-artifacts/m46/3-1-publish-sceneconnection-and-deterministic-svg.md`]
- [Source: `AGENTS.md`, Engineering Document Visual Golden Rule]

## Dev Agent Record

### Agent Model Used

Codex

### Debug Log References

- RED: `yarn test` built successfully, 52 prior tests passed, and four new Story 3.2 contract tests failed on
  missing exact segment paint, connection hit/selection, viewport intersection, and widget selection handling.
- GREEN: first implementation build exposed one TypeScript narrowing error at marker insertion; narrowed the
  marker factory result from `Konva.Node` to `Konva.Shape`, then all 56 frontend tests passed.
- Verification: `yarn contracts:check`, full sequential Gradle `test`, encoding audit, and source-set hygiene
  audit all passed.

### Completion Notes List

- Replaced flattened connection polylines with ordered segment paint and deterministic shared-segment,
  marker, and annotation identity deduplication.
- Painted only published Junction, owned Crossing bridge, Interruption, and annotation facts with constant
  screen-space line/marker/text weight; no topology or labels are inferred from geometry.
- Added transparent 12px connection hit paths, semantic `connectionId`/`traceId` selection, and restrained
  paint-only overlays while keeping style target, source, and engineering state unchanged.
- Added segment/bounds viewport intersection so long connections crossing a visible viewport remain painted.
- Removed the obsolete `contentLayer` compatibility alias; Konva remains one disposable paint/hit adapter.

### File List

- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`
- `_bmad-output/implementation-artifacts/m46/3-2-paint-and-select-professional-connections-in-konva.md`
- `_bmad-output/implementation-artifacts/m46/sprint-status.yaml`

### Change Log

- 2026-08-11: Created Story 3.2 from active M46 PRD, architecture spine, epics, current Konva adapter,
  frontend selection flow, and completed Story 3.1 intelligence; status `ready-for-dev`.
- 2026-08-11: Implemented exact professional SceneConnection paint, invisible semantic hit selection,
  responsive viewport handling, and regression contracts; all verification passed; status `review`.
