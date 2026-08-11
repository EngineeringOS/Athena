---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 4.2: Qualify 100000-Element Interactive Scale

Status: done

## Story

As a product maintainer,
I can run the normative large-scene fixture,
so future renderer decisions use measured evidence instead of guesses.

## Acceptance Criteria

1. The benchmark consumes the committed `contracts/presentation/v1/benchmark/scene-100k-manifest.json`
   and `interaction-transcript.json`, constructs exactly 100000 paint elements from the existing
   `AthenaDiagramScene` contract, places exactly 5 percent inside the `1600 x 1000` CSS-pixel
   viewport, records named hardware, Electron/Chromium, Node, adapter version, viewport, CSS DPR,
   warm-up frames, measured frames, and the complete operation/sample manifest under M43 artifacts.
2. Recorded run measures scene validation through first stable paint, incremental JS heap after
   settle, pan/zoom/drag input-to-paint p95, selection hit-to-visible-state p95, visible paint count,
   stable scene revision, hit identity, source trace, blank frames, and unhandled errors.
3. On the recorded reference hardware the run passes the committed gates: first stable paint
   `<=5 seconds`, incremental heap `<=512 MiB`, pan/zoom/drag p95 `<=50 ms`, selection p95
   `<=100 ms`, exactly 5000 visible paint elements, and zero identity/trace/error failures.
4. The benchmark exercises the same `KonvaDiagramAdapter` and scene/publication contract used by the
   product. Adapter work may use viewport culling, delegated events, static-layer caching,
   drag-layer isolation, batched redraw, and zoom-dependent detail, but may not add a second scene
   model, renderer, source authority, or compatibility path.
5. A failed gate blocks M43 closure and records a measured corrective action for culling, batching,
   cache, or adapter mapping. No PixiJS, WebGPU, WebGL, GLSP, or new rendering dependency is added
   by this story. Any future adapter decision requires the same-scene, same-command-contract proof.
6. Frontend contract tests, the scale harness, frontend build, affected Gradle/LSP tests, full Gradle
   test, encoding audit, and source-set hygiene audit pass sequentially. Benchmark evidence is
   reproducible from a deterministic command and is not a product export or user-facing feature.

## Tasks / Subtasks

- [x] Freeze benchmark fixture and evidence contract (AC: 1, 2, 3)
  - [x] Load and validate both committed benchmark manifests before execution; reject drift in count,
    visibility, viewport, DPR, warm-up, measured frames, operation order, or gate values.
  - [x] Add deterministic synthetic scene builder using `AthenaDiagramScene` and
    `AthenaScenePublication`; create exactly 100000 paint elements, stable IDs, one accepted input
    revision, and 5000 viewport-intersecting elements.
  - [x] Record machine/runtime/adapter metadata and per-sample timings, visible count, selected ID,
    trace ID, errors, and gate decisions at `_bmad-output/implementation-artifacts/m43/`.
- [x] Qualify the existing Konva adapter at scale (AC: 2, 3, 4)
  - [x] Keep one `KonvaDiagramAdapter` as paint and hit owner; add only bounded viewport culling,
    delegated/stable hit metadata, static or batched redraw, and transform-safe interaction support
    required by measured evidence.
  - [x] Preserve scene IDs, source trace, publication revision, READY mutation rules, asset bytes,
    and existing rolling-shutter behavior. No renderer-side engineering decisions.
  - [x] Add focused tests for visible-element culling, stable selection/trace, transform updates,
    and no second DOM SVG/Canvas owner.
- [x] Add deterministic product benchmark harness (AC: 1, 2, 4, 6)
  - [x] Run through Electron/Chromium against the built Theia frontend, using the same adapter code
    and scene/publication types. Keep harness outside production `src/main` and outside user export.
  - [x] Execute 60 warm-up frames then 300 measured operations over pan, zoom, select, and drag;
    capture first stable paint, heap, p95s, hit/trace/error counts, and visible count.
  - [x] Fail closed when workspace/product surface, scene revision, adapter, canvas, or benchmark
    contract is unavailable; never substitute a mock renderer or old presentation path.
- [x] Verify and record (AC: 3, 5, 6)
  - [x] Run focused frontend tests, frontend build, affected Gradle/LSP tests, root `test`, encoding
    audit, and source-set hygiene audit strictly sequentially.
  - [x] Write completion notes, debug log, file list, change log, benchmark evidence, and sprint state
    only after every gate has passing evidence. A failing run remains `review`/blocked for closure,
    with corrective action recorded instead of fabricated success.

## Dev Notes

### Authority And Scope

- `AthenaDiagramScene` remains the only scene model. Benchmark generation supplies a deterministic
  synthetic publication through the same schema; it does not introduce benchmark DTOs or geometry
  authority.
- `KonvaDiagramAdapter` remains the only live paint/hit owner. `reference/` projects are study-only;
  no PixiJS/WebGPU/GLSP source or dependency is imported.
- The benchmark measures adapter behavior, not an export format. Do not add PDF, PNG product export,
  SVG fallback, network asset loading, or compatibility readers.
- `READY` revision, occurrence IDs, Port IDs, and `traceId` values must remain stable during every
  gesture. Any mismatch is an immediate failure.

### Normative Fixture

- `contracts/presentation/v1/benchmark/scene-100k-manifest.json` is the authority for count, visible
  percentage, viewport, DPR, warm-up/measured frames, and budgets.
- `contracts/presentation/v1/benchmark/interaction-transcript.json` is the authority for operation
  sequence and stable-revision requirement.
- Scene contains 100000 paint elements and exactly 5000 elements intersecting the measured viewport.
  Use deterministic integer bounds and IDs. Do not modify committed manifests to make a run pass.
- Evidence must include operation samples, not only aggregate numbers, so a reviewer can distinguish
  first paint, transform, selection, drag, and error failures.

### Scale Controls Allowed

- Viewport culling may decide which already-compiled scene elements receive Konva nodes for the
  current viewport. It must not delete scene elements, mutate source, change IDs, or change hit/trace
  semantics when an element becomes visible.
- Static page/frame layers may remain cached. Pan/zoom may update stage transform without rebuilding
  unchanged nodes; redraw batching and drag-layer isolation are allowed behind the adapter.
- Hit testing must remain delegated to Konva and return the same `elementId`, semantic ID, occurrence
  or Port kind, and `traceId` as the non-benchmark rolling-shutter surface.

### Failure Policy

- Any threshold failure blocks M43 closure. Record exact metric, environment, operation sample, and
  first corrective action. Do not lower budgets, reduce element count, reduce visible percentage,
  skip validation, or claim support beyond evidence.
- A Pixi/WebGPU decision is explicitly out of scope. It may be considered only in a later milestone
  after this same-scene Konva run fails despite measured culling/batching/cache corrections.

### Project Structure

- Benchmark harness and product launcher belong in `ide/theia-product/scripts/`.
- Shared synthetic fixture/adapter test helpers belong in frontend tests or a cohesive benchmark
  support module; no `*Demo`, `*Sample`, `*Proof`, or milestone-named production class.
- Evidence belongs under `_bmad-output/implementation-artifacts/m43/`; contract fixtures remain under
  `contracts/presentation/v1/benchmark/`.

### Required Sequential Commands

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-frontend contracts:check
yarn workspace @engineeringood/athena-theia-frontend test
yarn workspace @engineeringood/athena-theia-product build
yarn verify:m43-scale
Set-Location ..
.\gradlew.bat --no-daemon --console=plain :ide:lsp:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
```

Gradle commands run one at a time on Windows. Product benchmark runs only after frontend/product
bundles are rebuilt.

## References

- `_bmad-output/planning-artifacts/m43/epics.md` Epic 4, Story 4-2
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md` NFR-5 and SM-8
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md` AD-14, AD-16, AD-17, AD-18
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md` section 10 and benchmark corpus
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/UPSTREAM-ADOPTION.md`
- `_bmad-output/implementation-artifacts/m43/3-1-render-scene-through-konva-adapter.md`
- `_bmad-output/implementation-artifacts/m43/3-4-refresh-publication-states-atomically.md`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `contracts/presentation/v1/benchmark/scene-100k-manifest.json`
- `contracts/presentation/v1/benchmark/interaction-transcript.json`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

1. First scale run failed gate `interactionP95` at `51.1ms` after page-fit expansion. Samples showed
   repeated culling work scanning all `100000` occurrences.
2. Added deterministic `SceneSpatialIndex` broad-phase buckets behind the existing
   `KonvaDiagramAdapter`; no second renderer or scene authority added.
3. Rebuilt frontend/product and reran the normative harness. Final run passed all gates:
   first stable paint `331.6ms`, heap `30.95MiB`, interaction p95 `22.1ms`, selection p95 `26.9ms`,
   exactly `100000` scene elements and `5000` visible, stable revision, zero identity/trace/errors.
4. Initial command from `ide` failed because `verify:m43-scale` is owned by
   `ide/theia-product`; reran from that package and recorded passing evidence.

### Implementation Plan

1. Lock committed benchmark manifests and deterministic publication builder.
2. Keep `KonvaDiagramAdapter` as sole paint/hit owner; use bounded culling and batched redraw.
3. Add product-scale regression coverage and center-panel layout regression coverage.
4. Rebuild frontend/product, run scale and product proof, then run sequential Gradle/audit gates.

### Completion Notes List

- Center editor panel now has explicit flex/height ownership; canvas host consumes the full available
  main editor area and uses white surrounding canvas space.
- Default page fit preserves aspect ratio while removing the artificial `0.94` shrink margin.
- Added deterministic `SceneSpatialIndex` broad-phase culling. Source order, scene IDs, trace IDs,
  READY mutation rules, and Konva hit ownership remain unchanged.
- Added regression tests for full-panel layout, page fit, spatial bucket boundary behavior, source
  order, and identity-safe culling.
- Normative M43 scale evidence written to `m43-scale-benchmark.json`; desktop/narrow product proof
  refreshed after rebuild.
- Verification passed sequentially: frontend contracts/build/tests (`27/27`), product build,
  `yarn verify:m43-scale`, `yarn verify:m43-proof`, `:ide:lsp:test`, root `test`, encoding audit,
  and source-set hygiene audit.

### File List

- `_bmad-output/implementation-artifacts/m43/4-2-qualify-100000-element-interactive-scale.md`
- `_bmad-output/implementation-artifacts/m43/m43-scale-benchmark.json`
- `_bmad-output/implementation-artifacts/m43/m43-product-proof.json`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m43/m43-rolling-shutter-mobile-720x900.png`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/diagram/scene-spatial-index.ts`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-product-layout.test.mjs`
- `ide/theia-frontend/scripts/athena-presentation-layout.test.mjs`
- `ide/theia-frontend/scripts/athena-spatial-index.test.mjs`

### Change Log

- 2026-08-06: Created through BMad create-story workflow; status `ready-for-dev`.
- 2026-08-06: Qualified normative 100000-element Konva scale path, fixed center-panel sizing,
  added spatial culling and regression evidence; status `done`.
