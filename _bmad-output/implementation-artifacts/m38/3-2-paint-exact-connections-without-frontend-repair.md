---
baseline_commit: 1b2e8b7a6854bc186c82f2e796cf8adf9d5efc62
---

# Story 3.2: Paint Exact Connections Without Frontend Repair

Status: done

## Story

As an engineer,
I want Theia to paint only compiler-supplied facts,
so that the IDE cannot hide broken engineering truth.

## Acceptance Criteria

1. Theia paints supplied body, terminal, route, marker, line, label, and text facts from the current
   `PresentationDocument` payload.
2. Connector endpoints equal supplied placed Anchor points in rendered route and hit-test geometry.
3. Text shaping and paint remain normal Theia renderer work and never feed endpoint or topology
   truth.
4. Empty-route acceptance, `(0,0)` defaults, fallback terminals, graph-edge reconstruction, crossing
   detection, route-label recovery, endpoint snapping, and style guessing are deleted.
5. Invalid protocol input produces a clear failed drawing state rather than repaired geometry.
6. Focused frontend model, edge-layer, hit-test, invalid-payload, rendering tests, frontend build,
   hygiene, encoding, forbidden-path scan, and `git diff --check` pass.

## Tasks / Subtasks

- [x] Audit Theia connection model path (AC: 1, 4)
  - [x] Confirm `athena-graph-presentation-model.ts` resolves connectors only from payload
        `presentation.connectors`.
  - [x] Delete or fail any graph-edge reconstruction, route-label recovery, endpoint snapping,
        style guessing, empty route acceptance, and `(0,0)` endpoint default.
  - [x] Keep text rendering as paint only.
- [x] Enforce exact endpoint paint and hit-test geometry (AC: 1, 2, 5)
  - [x] Validate connector route first/last points against source/target endpoint payload.
  - [x] Surface invalid payload as failed drawing state or rejected connector result.
  - [x] Ensure workbench edge/hit geometry consumes the validated route only.
- [x] Paint compiler markers and labels only (AC: 1, 3, 4)
  - [x] Ensure connection markers are painted from `connectionMarkers`, not line intersections.
  - [x] Ensure connector labels are painted from typed connector labels, not recovered from route
        tokens.
  - [x] Ensure line appearance comes from typed connector line.
- [x] Add focused frontend tests (AC: 2, 4, 5, 6)
  - [x] Add/extend model tests for endpoint equality and invalid payload rejection.
  - [x] Add/extend render model tests for markers/labels/line facts from payload.
  - [x] Add forbidden assertions for frontend repair terms.
- [x] Verify sequentially (AC: 6)
  - [x] Run Theia frontend test/build commands.
  - [x] Run affected LSP/kernel tests only if contracts changed.
  - [x] Run hygiene, encoding, forbidden-path scan, and `git diff --check`.

## Dev Notes

### Architecture Boundary

Story 3.2 is Theia-side enforcement. It must not improve route quality or invent layout. It must make
broken compiler facts visible as failure, not repaired paint.

Theia may shape text, choose SVG/React paint primitives, and handle pointer events. It must not
derive engineering endpoint, terminal, topology, label, crossing, or line-class facts.

No Java2D. No second renderer. No frontend topology logic. No compatibility fallback.

### Previous Story Intelligence

Story 3.1 removed public `PresentationDocument.routeFactSnapshot`, deleted LSP route snapshot
payloads, added direct Presentation Document SVG export, and removed SVG crossing inference. Story
3.2 must close the IDE side: Theia cannot recover missing facts after transport.

### Code Signals

Inspect:

- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`
- `ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`
- `integrations/graph-glsp/src/athena-glsp-projection-source.ts`
- `integrations/graph-glsp/src/athena-glsp-projection-adapter.ts`

Expected active smells:

- optional endpoint defaults;
- empty connector route tolerated;
- frontend recomputes source/target from graph node bounds;
- line appearance guessed from class/id strings when typed line missing;
- labels recovered from token maps;
- crossing/marker inferred from polyline intersections.

### Testing Requirements

Run frontend from `ide/theia-frontend` if touched:

```powershell
yarn test
yarn build
```

Run GLSP build from `integrations/graph-glsp` if touched:

```powershell
yarn build
```

Run repository gates:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

Forbidden-path scan:

```powershell
rg -n "empty-route|0,0|fallback terminal|graph-edge reconstruction|crossing detection|route-label recovery|endpoint snapping|style guessing|repair endpoint|infer.*crossing|infer.*label|infer.*style" ide integrations -g "*.ts" -g "*.tsx" -g "*.mjs" -g "!**/build/**" -g "!**/lib/**"
```

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-31: Story created from M38 sprint order after Story 3.1 reached review.
- 2026-07-31: Dev started from sprint order.
- 2026-07-31: Removed presentation connector repair in Theia resolver and blocked raw graph edge
  fallback when a Presentation Document is present.

### Completion Notes

- Story context created for BMad dev-story execution.
- Theia now validates presentation connector route endpoints against supplied source/target endpoint
  points before building workbench edges.
- Malformed presentation connector payload produces a failed drawing state with
  `presentation.connector.invalid`, not repaired geometry.
- Raw graph edges are no longer used when a `PresentationDocument` exists; visible connections come
  from `presentation.connectors`.
- Tests now use typed connector labels and explicit connection markers instead of route token label
  recovery or intersection-derived crossings.
- Verification passed: Theia `yarn test` including build, frontend forbidden repair scan,
  source-set hygiene, encoding audit, and `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m38/3-2-paint-exact-connections-without-frontend-repair.md`
- `_bmad-output/implementation-artifacts/m38/sprint-status.yaml`
- `ide/theia-frontend/src/browser/athena-graph-presentation-model.ts`
- `ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`
- `ide/theia-frontend/scripts/athena-graph-workbench-model.test.mjs`

### Change Log

- 2026-07-31: Created Story 3.2 context and marked ready for dev.
- 2026-07-31: Started Story 3.2 implementation.
- 2026-07-31: Implemented paint-only Theia connector validation and marked ready for review.
