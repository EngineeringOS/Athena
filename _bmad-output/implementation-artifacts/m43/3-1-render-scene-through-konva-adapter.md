---
baseline_commit: 9681345816d2659abf7a70b68151eacfef0944c2
---

# Story 3.1: Render Scene Through Konva Adapter

Status: done

## Story

As an engineer, I can see the active canonical scene on one clean page, so the document is readable,
traceable, and usable without split-layer drift.

## Acceptance Criteria

1. Frontend declares exact direct `konva@10.3.0` dependency. One imperative `KonvaDiagramAdapter` is
   the only Konva import boundary and consumes generated, schema-validated `AthenaScenePublication`
   data plus verified `AssetBundle` bytes.
2. Adapter renders one scene revision with white page, outer frame, numeric top coordinate labels,
   alphabetic left coordinate labels, occurrences, Ports, labels, routes, and admitted assets. No DOM
   SVG occurrence layer, raw Projection/Spatial reconstruction, interior grid, bottom table, banner,
   or unexplained whitespace exists.
3. Resize uses host CSS pixels and `ResizeObserver`; contain-fit, pan, zoom, hit regions, and logical
   scene coordinates are deterministic. Konva alone owns backing-store DPR; DPR never changes scene
   geometry, fit math, or hit coordinates.
4. Selection and keyboard focus return stable occurrence/Port IDs and source trace. Adapter owns one
   hit-testing path and never invents semantic meaning or edits source.
5. Invalid, stale, unavailable, malformed, or revision-mismatched publication is not painted as
   READY. Identical accepted publications produce stable node identity and no duplicate paint owner.
6. Frontend tests/build and focused contract tests pass; no Konva, DOM, CSS-pixel, or Theia types cross
   kernel/presentation contracts.

## Tasks / Subtasks

- [x] Establish one Konva adapter boundary and dependency (AC: 1, 6)
  - [x] Add exact direct `konva: 10.3.0` dependency and regenerate lock metadata without importing Konva elsewhere.
  - [x] Replace legacy split presentation paint path with `KonvaDiagramAdapter` plus a thin Theia host; retain React only for host lifecycle and controls.
  - [x] Keep generated `types.ts` and validators as the only frontend publication contract; reject malformed payload before adapter update.
- [x] Render canonical clean page (AC: 2, 3)
  - [x] Create one Stage, static page/frame/coordinate layer, scene content layer, and transient interaction layer.
  - [x] Render canonical scene order and stable IDs for decorations, routes, occurrences, Ports, labels, and assets; use bundle bytes only, never paths or network URLs.
  - [x] Implement CSS-pixel ResizeObserver contain-fit, pan/zoom, and Konva DPR handling without changing logical scene units.
  - [x] Ensure clean default output: no interior macro/micro grid, bottom table, duplicate SVG/Canvas geometry, or reserved blank chrome.
- [x] Implement selection, focus, and trace (AC: 4, 5)
  - [x] Use one delegated hit path with Port precedence over occurrence hit when overlapping, then stable z-order.
  - [x] Expose selected occurrence/Port IDs and trace origin through existing selection model; keyboard focus must not mutate scene or source.
  - [x] Clear/revoke asset resources on revision replacement and widget disposal; ignore stale publication updates.
- [x] Add conformance and product proof (AC: 1-6)
  - [x] Test adapter dependency/import boundary, stage/layer ownership, clean page, resize, contain-fit, pan/zoom, DPR invariance, selection, focus, assets, and stale revision refusal.
  - [x] Test active rolling-shutter scene renders nonblank canvas with expected frame/coordinates and stable occurrence/Port trace IDs at desktop and narrow host sizes.
  - [x] Run frontend tests/build, focused LSP/presentation tests, root Gradle tests, encoding audit, and source-set hygiene audit sequentially.

## Dev Notes

### Authority And Boundaries

- `AthenaDiagramScene` plus same-revision `AssetBundle` is sole paint input. Adapter never reads `.athena`,
  `.sheet.athena`, `ProjectionDocument`, `SpatialDocument`, package paths, or network resources.
- Semantic source owns Entity/Port identity, direction, flow, and engineering properties. IEC/part SVG
  owns visual geometry/center only. SVG metadata is inert in M43 and cannot create or alter Ports.
- `KonvaDiagramAdapter` is only Konva import boundary. React/Theia host supplies one DOM container and
  controls; no second scene tree or DOM SVG geometry layer.
- Editing is out of this story. Adapter may emit selection/focus state only; Story 3-2 owns move commands,
  Story 3-3 owns connect commands, and Story 3-4 owns atomic publication refresh.

### Geometry And Interaction Contract

- Fit/pan/zoom calculations use host CSS pixels and integer logical `SceneUnit` geometry. DPR only affects
  Konva backing store/cache; never multiply Stage dimensions or transform by DPR.
- Default paint is clean printable page: white background, outer frame, numeric top labels, alphabetic
  left labels, engineering content. Interior grid and bottom tables remain absent.
- Use `ResizeObserver`; disconnect on disposal. Keep one Stage, one viewport transform, one hit-testing
  owner. Use delegated events and stable scene IDs, not node index or array position.
- Selection precedence: Port hit before occurrence; otherwise topmost canonical z-order. Trace comes from
  scene `traceId` and `traces`, never inferred from rendered text.

### Asset Rules

- Bundle entry bytes are verified canonical bytes from `PresentationAssetCompiler`. Convert bytes to
  browser-safe object URLs or decoded image/font resources in the adapter, keyed by accepted revision and
  asset ID; revoke on replacement/disposal. Never reopen package paths or fetch URLs.
- SVG assets are visual only. Do not parse SVG metadata, infer shape, or map Ports in TypeScript.

### Existing Code To Replace Or Reuse

- Replace `ide/theia-frontend/src/browser/athena-presentation-widget.tsx` split Canvas/DOM SVG paint with
  the host plus adapter; preserve Theia opening, revision state, diagnostics, and source trace behavior.
- Reuse `ide/theia-frontend/src/browser/diagram/generated/types.ts`, `validators.ts`, grid helpers, and
  current LSP bridge. Do not create duplicate DTOs or a second renderer.
- Follow `ARCHITECTURE-SPINE.md` AD-9, AD-11, AD-12, AD-14 and `M43-CONTRACT-PACK.md` sections 7, 9, 10.
- Exact dependency target: `konva@10.3.0`; `ajv@8.20.0` remains direct and schema validation stays upstream
  of adapter paint. `reference/konva` is read-only evidence, never production import.

### Testing Requirements

- Tests stay in frontend `scripts/*.test.mjs` or frontend test source; no production proof/demo class.
- Assert no `<svg>` occurrence paint and one Konva import boundary by source inspection/build checks.
- Assert canvas pixel nonblank after READY, frame/coordinate labels visible, no interior grid/table,
  resize stability, DPR invariance, selection/focus IDs, asset bytes, stale update rejection, disposal,
  and repeated publication identity stability.
- Run Gradle commands sequentially on Windows. Product E2E screenshot proof belongs to Story 4-1 after
  move/connect commands exist; this story supplies adapter-level proof only.

### References

- `_bmad-output/planning-artifacts/m43/epics.md`#Story-3-1---Render-Scene-Through-Konva-Adapter
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md`#AD-9, #AD-11, #AD-12, #AD-14
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md`#7, #9, #10
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md`
- `_bmad-output/implementation-artifacts/m43/2-2-publish-traceable-safe-assets-and-render-oracle.md`

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Replaced placeholder publication text with one Konva adapter boundary after frontend build initially lacked `konva`.
- Added exact dependency, generated-contract validation, asset-byte object URLs, resize/fit/DPR handling, delegated selection, and stale revision disposal.
- Final evidence: frontend `yarn test` passed (14/14); root `.:test` passed; encoding and source-set hygiene audits passed.

### Completion Notes List

- Ultimate BMad context created from complete M43 sprint, Epic 3, PRD, architecture spine, contract pack, and Story 2-2 evidence.
- `KonvaDiagramAdapter` owns one Stage, three layers, one transform, one hit path, and stable scene IDs.
- Theia widget now supplies only lifecycle/host state; no DOM SVG or raw Spatial/Projection reconstruction remains.
- Semantic source remains authority for Entity/Port meaning; assets remain visual-only and arrive as verified bundle bytes.

### File List

- `ide/theia-frontend/package.json`
- `ide/yarn.lock`
- `ide/theia-frontend/src/browser/diagram/konva-diagram-adapter.ts`
- `ide/theia-frontend/src/browser/athena-presentation-widget.tsx`
- `ide/theia-frontend/src/browser/style/index.css`
- `ide/theia-frontend/scripts/athena-konva-adapter.test.mjs`

### Change Log

- 2026-08-06: Created through BMad story flow; status `ready-for-dev`.
- 2026-08-06: Implemented one Konva scene adapter, clean page paint, asset-byte loading, interaction
  selection, and conformance proof; final product and scale gates passed; status set to `done`.
