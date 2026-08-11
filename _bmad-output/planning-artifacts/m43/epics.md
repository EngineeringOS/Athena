---
milestone: M43
title: Presentation and Rendering Reality
status: final
created: 2026-08-05
updated: 2026-08-06
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-05-m43/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/M43-CONTRACT-PACK.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-05-m43/LEGACY-REPLACEMENT.md
---

# M43 Epics and Stories

## Replan Decision

M43 architecture and PRD changed materially. Existing story acceptance text described a fixed 4 x 4
grid, split DOM SVG/Canvas ownership, title-table paint, and raw `athena/projectionSession` payloads.
Those contracts are retired. Prior story records keep their historical `review` or `in-progress`
state under `.retired`; they are not reopened, reset to development, or counted as completed against
the new contract. Stories below are new-contract delivery units. Create each through
`bmad-create-story`, explicitly adopt already-valid code and tests, then implement only the remaining
contract delta through `bmad-dev-story`.

## Prior Work Adoption

| Retired story work | Disposition | New-contract owner |
| --- | --- | --- |
| 1-1 Sheet Companion grid/page parser | Adopt and extend; do not rewrite from zero | 1-1 |
| 1-2 occurrence anchors plus 2-1 placement mapping | Adopt and align to shared grid vectors | 1-2 |
| 2-2 grid-aligned Spatial compiler | Adopt and close missing-companion, bounds, overlap, and trace gaps | 1-3 |
| 3-1 raw Projection/Spatial LSP publication | Replace; raw presentation transport violates new authority | 0-2 and 2-1 |
| 3-1/3-2 split Canvas plus DOM SVG widget | Replace with one Konva adapter; keep only valid Theia host/opening behavior | 3-1 and 3-4 |
| 4-1 old rolling-shutter proof and screenshots | Retire as historical evidence; reproduce against canonical scene | 4-1 and 4-3 |

No repository-wide rollback is part of this replan. Existing implementation is evaluated by contract:
adopt when aligned, refactor when partially aligned, delete when it creates retired authority.

## Requirements Extract

FR-1..FR-3: require same-basename Sheet Companion; author `grid: C * R cell: N`; use A1-style
references and N x N micro anchors; persist authored anchors and locks.

FR-4..FR-6: compile deterministic logical Spatial Reality, apply authored anchors, and fail closed on
invalid references, bounds, overlap, or missing companion.

FR-7..FR-9: publish one closed `AthenaDiagramScene`; render clean page through one live adapter;
select and trace stable identities; refresh through READY/STALE/UNAVAILABLE publication states.

FR-10..FR-11: prove active M43 rolling shutter and close source-set/encoding/legacy hygiene.

FR-12: drag/lock occurrences and connect compatible Ports through revision-checked typed commands,
single-file WorkspaceEdit, validation, undo, and atomic recompile.

NFR-1..NFR-6: canonical determinism, authority safety, human-first diagnostics, one interaction
owner, measured responsiveness, and pre-1.0 deletion policy.

Architecture contract: `M43-CONTRACT-PACK.md` must be implemented before dependent stories. The
contract corpus is the only cross-language fixture authority. `LEGACY-REPLACEMENT.md` is a required
deletion ledger, not optional cleanup.

## Epic 0 - Presentation Contract Foundation

Goal: freeze scene, publication, command, trace, asset, grid, and conformance contracts before
independent backend/frontend work.

### Story 0-1 - Freeze Scene Publication And Command Contracts

As an implementation team, we can validate one closed scene/publication/command schema, so Kotlin,
LSP, TypeScript, adapters, and proof code cannot invent incompatible DTOs.

Acceptance:

- committed JSON Schemas, `svg-safe-1`, generated TypeScript types/validators, and Kotlin validation
  tests exist at contract-pack paths;
- scene topology, typed ID algebra, input revision, scene digest, canonical ordering, publication
  states, diagnostic envelope, and command/result envelopes match contract vectors;
- shared rolling-shutter scene, invalid scene, grid, command, trace, asset, SVG, and benchmark
  manifests exist;
- Ajv `8.20.0` is an exact direct frontend dependency; no schema fallback or compatibility reader.

### Story 0-2 - Retire Raw Projection Presentation Transport

As a maintainer, I can publish `AthenaScenePublication` through the new LSP methods, so no raw
Projection/Spatial presentation transport remains as a second authority.

Acceptance:

- `athena/diagramScene`, `athena/diagramConnectOptions`, and `athena/applyDiagramCommand` use
  generated contracts;
- `athena/projectionSession`, duplicate Kotlin/TypeScript payloads, and frontend reconstruction
  path are deleted or rewritten under the legacy ledger;
- runtime publishes complete scene revision and asset bundle, not raw Spatial DTOs;
- tests prove old request/selector absence and new schema validation.

## Epic 1 - Sheet Companion And Grid Authority

Goal: establish human-first Sheet Companion syntax and exact A1/N x N lowering without changing
Engineering Reality authority.

### Story 1-1 - Parse Required Sheet Companion And Grid Intent

As an engineer, I can author `project.sheet.athena` with `grid: C * R cell: N`, so page coordinates
live beside project meaning and missing companions fail clearly.

Acceptance:

- same-basename companion discovery is exact; missing/ambiguous companion returns `UNAVAILABLE`;
- `cell: N` accepts only positive multiples of 4 and produces source spans;
- page/profile intent and grid dimensions are typed; project grammar remains unchanged;
- syntax `"Supply" at A2` is accepted without `place occurrence` ceremony;
- diagnostics name sheet, field, problem, and correction.

### Story 1-2 - Lower A1 Anchors And Deterministic Snap

As an engineer, I can place an occurrence at A1-style coordinates and N x N micro anchors, so drag
preview and committed placement agree exactly.

Acceptance:

- row letters, numeric columns, macro origin, default micro, boundary ownership, and placement-anchor
  formulas match contract vectors for `cell: 4` and `cell: 8`;
- `micro` outside `1..N`, unknown occurrence, out-of-grid address, and locked drag fail closed;
- Kotlin and TypeScript snap helpers pass identical vectors and tie-break rules;
- authored lock and placement provenance remain stable across source reorder.

### Story 1-3 - Compile Sheet Intent Into Spatial Reality

As a compiler, I can apply Sheet Intent to M41 Spatial compilation, so authored anchors influence
placement while Spatial remains the only geometry authority.

Acceptance:

- M41 exact anchor/route/validation/order invariants remain green;
- missing companion produces no Presentation Reality but does not corrupt non-presentation compilation;
- authored placement survives recompile and source reorder; initial placement is deterministic;
- bounds, overlap, route, label, and trace failures publish no partial Spatial document.

## Epic 2 - Canonical Scene Compilation And Assets

Goal: turn validated Spatial Reality into one complete, traceable, renderer-neutral scene and safe
revision-scoped assets.

### Story 2-1 - Compile Canonical AthenaDiagramScene

As a runtime, I can compile one closed scene from Spatial Reality, so every adapter receives the same
logical geometry, stable IDs, z-order, Ports, Routes, Labels, and source trace.

Acceptance:

- scene fields and topology match schema; no DOM/Konva/CSS/device-pixel type crosses kernel;
- Spatial geometry is copied/validated, not moved or rerouted by Presentation;
- IDs survive source reorder/process restart and digest/order vectors are byte-stable;
- clean frame and coordinate decorations are scene facts; no bottom table/interior grid by default.

### Story 2-2 - Publish Traceable Safe Assets And Render Oracle

As a renderer and reviewer, I can consume a revision-scoped asset bundle and deterministic SVG oracle,
so SVG, live paint, and proof use the same admitted bytes and metrics.

Acceptance:

- `svg-safe-1` parser/canonicalizer rejects prohibited constructs and enforces byte/decoded limits;
- asset/font bytes are digest-verified, revision-scoped, deduplicated, and never fetched from paths or
  network by adapters;
- trace roles and primary navigation match contract vectors;
- SVG serialization is canonical and exact against golden bytes; PNG remains Electron proof harness.

## Epic 3 - Live Editable Theia Surface

Goal: provide one clean, fast, traceable, editable document surface with one interaction owner.

### Story 3-1 - Render Scene Through Konva Adapter

As an engineer, I can see the active scene on a clean page, so the document is readable and usable
without split-layer drift.

Acceptance:

- exact direct `konva@10.3.0` adapter renders one scene revision with page frame, numeric top border,
  alphabetic left border, occurrences, Ports, labels, and routes;
- no DOM SVG occurrence layer, raw Spatial reconstruction, interior grid, bottom table, or unexplained
  whitespace remains;
- CSS-pixel fit, ResizeObserver, pan/zoom, hit regions, asset bundle, and DPR behavior match contract;
- selection and keyboard focus return stable occurrence/Port IDs and source trace.

### Story 3-2 - Apply Move And Lock Commands

As an engineer, I can drag an occurrence to a snapped Sheet Anchor and lock it, so authored layout
survives recompilation and undo.

Acceptance:

- drag preview uses contract snap and emits revision-checked `MoveOccurrence` only on drop;
- runtime validates and returns one WorkspaceEdit against `.sheet.athena`; frontend never edits text;
- stale, locked, out-of-bounds, and overlap failures leave source and READY scene unchanged;
- accepted edit publishes correlated revision and editor undo/redo recompiles deterministically.

### Story 3-3 - Connect Ports Through Validated Commands

As an engineer, I can connect compatible Ports through an explicit compiler-advertised option, so
engineering relationships are authored without renderer inference.

Acceptance:

- options query is revision-bound and exposes relationship definition, roles, direction, and source
  target; ambiguous options require selection;
- `ConnectPorts` validates direction/semantics and writes one project source WorkspaceEdit;
- invalid direction or stale revision returns exact diagnostics and no source/scene mutation;
- accepted relationship appears after atomic recompile with stable route/trace identity.

### Story 3-4 - Refresh Publication States Atomically

As an engineer, I can edit project or Sheet source and see one READY/STALE/UNAVAILABLE state, so old
and new scene elements never mix.

Acceptance:

- valid edit replaces complete scene and asset bundle atomically;
- invalid source shows attempted revision and diagnostics, optionally displays identified last-valid
  scene as non-editable STALE;
- missing companion/schema/adapter failure is UNAVAILABLE with no guessed scene;
- repeated identical edits yield identical scene digest, order, trace, and screenshots.

## Epic 4 - Product Proof, Scale, And Closure

Goal: prove the active rolling-shutter product surface, qualify large-scene behavior, and remove all
superseded M43 presentation paths.

### Story 4-1 - Prove Rolling-Shutter Editable Document

As a reviewer, I can open the M43 rolling-shutter project and inspect, select, move, trace, and
connect on a clean live page, so milestone value is demonstrated on the real product surface.

Acceptance:

- M43-local same-basename project and Sheet Companion open explicitly in Theia;
- page has clean frame, top numbers, left letters, symbols, labels, Ports, routes, and no interior
  grid/table; scene revision and source trace are visible through product behavior;
- selection, move/lock, one valid Port command, one invalid-direction rejection, and stale edit evidence
  pass;
- desktop and narrow screenshots plus scene/SVG/PNG evidence live under M43 artifacts only.

### Story 4-2 - Qualify 100000-Element Interactive Scale

As a product maintainer, I can run the normative large-scene fixture, so future renderer decisions
use evidence rather than guesses.

Acceptance:

- exactly 100000 paint elements, 5 percent visible, named hardware, viewport/DPR, warm-up, gesture,
  and sample manifest are recorded;
- first paint <=5 seconds, incremental heap <=512 MiB, pan/zoom/drag p95 <=50 ms, selection p95 <=100
  ms, and zero hit/trace/error failures;
- failure blocks closure and leaves a measured action for culling/batching/adapter correction;
- no Pixi or WebGPU dependency is added without same-scene adapter proof.

### Story 4-3 - Close M43 With Replacement And Regression Evidence

As a maintainer, I can verify M43 closure from deterministic commands, so stale layout and compatibility
authority cannot survive in active code.

Acceptance:

- `LEGACY-REPLACEMENT.md` absence gate passes; old request, DTOs, widget selectors, proof selectors,
  and duplicate dependencies are gone;
- source-set hygiene, encoding audit, schema/conformance tests, affected Gradle tests, frontend build,
  and product E2E pass sequentially;
- story records, sprint status, closure notes, contract corpus, screenshots, benchmark, and source
  trace evidence are complete;
- no M0-M42 example, proof, fallback, alias, or compatibility reader is used.

## Delivery Order

Stories execute strictly in numeric order:

```text
0-1 -> 0-2 -> 1-1 -> 1-2 -> 1-3 -> 2-1 -> 2-2 ->
3-1 -> 3-2 -> 3-3 -> 3-4 -> 4-1 -> 4-2 -> 4-3
```
