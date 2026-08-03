# M39 Retrospective

Date: 2026-08-02
Status: complete

## Outcome

M39 kept the finished Human DSL from Epic 1, then established Athena's first Reality Graph:

```text
Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia
```

M39 proved clean ownership, not drawing parity. The dedicated `examples/m39/reality-product-proof`
project compiles through all four realities with human-first `to` relations, and Theia renders a
paint-only Control Drawing surface backed by the schematic projection. The proof is honest: it
reports exact endpoints, zero route/body intersections, and 28 measured label collisions as debt.

All six M39 epics are delivered. Stories 1-1 through 6-3 are complete; the milestone is ready for
review.

## Human DSL (Epic 1) Completed

- Relation statements read as engineering language: `power Supply.L1 to Breaker.line`,
  `control StartButton.out to Drive.enable`, `earth EarthBar.PE to [Drive.PE, Motor.PE, Cabinet.PE]`.
- `to` is the preferred spelling; `->` is the same relation alias through one compiler path.
- Domain relation verbs `power`, `control`, and `earth` are declared by the active electrical
  domain package; the Athena kernel does not hardcode them as universal keywords.
- Normal M39 source has no connection `intent` blocks and no repeated compiler policy per relation.

## Reality Ownership Established

Each reality is a coherent domain with one authoritative fact set and one compiler owner:

| Reality | Owns | Authority |
|---|---|---|
| Engineering | what the engineered system is: systems, devices, ports, signals, connections, constraints | engineering compiler |
| Projection | view-specific engineering documents: view, sheet, occurrence, group, reading order — no coordinates or style | projection compiler |
| Spatial | geometry results: placement, bounds, anchors, lanes, routes, alignment, measured quality | spatial compiler |
| Presentation | paintable facts: shapes, connector visuals, strokes, labels, visibility, theme result, paint order | presentation compiler |

Projection models no longer own final coordinates, anchor positions, or route geometry. Spatial and
Presentation compilers own their facts, and the renderer never repairs truth.

## Transformation Chain

One thin typed interface (`RealityTransformation<InputReality, OutputReality>`) lowers Engineering
to Projection to Spatial to Presentation. Source trace and identity survive the chain without
transformation metadata and without renderer repair. Transformation metadata (preserved/derived/
discarded facts) stays deferred until repeated transformations prove the need.

## Spatial And Paint Ownership

- Spatial compiler derives placement and bounds for every occurrence and computes connector
  endpoints from placed anchor positions, keeping the M38 endpoint trust invariant.
- Spatial quality baseline records overlap, body intersection, crossing count, twist, lane use, and
  label pressure honestly. Routing is documented and tested as a Spatial subsystem.
- Presentation compiler emits one atomic Presentation Document with dynamic style, labels,
  visibility, and paint order, consumed identically by Theia and SVG export.
- Theia remains domain-neutral and paint-only: it does not snap endpoints, reroute connectors,
  infer topology, relabel, or interpret engineering domains.

## Verification Evidence

- `:kernel:compiler:test` and `:ide:lsp:test` passed, including the dedicated M39 product proof test.
- graph-glsp: 9/9 tests passed.
- Theia frontend: 232/232 tests passed, including both M39 product-proof contract tests.
- Theia backend: 12/12 tests passed.
- Full `gradlew test`: 152 tasks passed, including the wired source-set hygiene audit task.
- Standalone source-set hygiene audit, UTF-8 encoding audit, and `git diff --check` passed.
- IDE bundle rebuild (graph-glsp, frontend, backend, product, dev runtime) finished with 0 errors;
  LSP `installDist` passed.
- Electron E2E passed at 1920x1080, 1280x900, and 720x900.

## E2E Product Proof

- Workspace: `examples/m39/reality-product-proof`, active view `schematic`, product surface
  `control-drawing`.
- engineeringReality: source present, human relation syntax confirmed.
- projectionReality: schematic backing view confirmed.
- spatialReality: measured debt present, route proof present.
- presentationReality: paint layer visible (sheet frame, drawing area, title block, title field),
  paint plan required and present.
- paintOnlyRenderer: no endpoint snapping, no rerouting, no domain inference, no presentation repair.
- Routes: 8 terminal-anchored routes, 0 route/body intersections, 7 placed node boxes.

Screenshots (non-blank PNGs):

- `_bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-desktop-1920x1080.png`
- `_bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-desktop-1280x900.png`
- `_bmad-output/implementation-artifacts/m39/screenshots/m39-reality-product-proof-narrow.png`

## What Improved

- Facts have one owner: engineering truth, projection selection, spatial geometry, and presentation
  style no longer mix.
- Endpoints stay exact: visible connector first/last points equal placed anchor points.
- Routes and style are derived in the right reality; Theia and SVG export paint the same atomic
  Presentation Document.
- Human DSL is smaller and reads like engineering language; no `intent` boilerplate remains in
  normal source.
- Quality is measured, not assumed: overlap, crossing, twist, lane, and label-pressure facts exist
  as a Spatial baseline.
- A dedicated M39 example and product verifier make the milestone falsifiable on its own architecture.

## What Remains Visually Poor (Measured Debt)

- 28 label collisions at fit-to-screen zoom; no label engine exists yet.
- Routes are 2-point straight lines with no orthogonal bends, so the schematic does not yet read
  like a professional control drawing.
- Dense route and label composition at small viewports remains visually crowded.
- No professional composition: diagram constructs, functional regions, symbol grouping, and
  rail/rung/trunk/bundle organization are absent.
- No professional routing: lane optimization, bend/crossing minimization, bundle routing, and
  multi-sheet continuation are absent.

M39 does not claim professional drawing quality. These failures move to M40 (composition) and
M41+ (professional routing and label engine), per the M39 PRD handoff.

## Failure Learned From

Story 6.2 exposed a stale cross-milestone contract: the M39 rename of the shared Electron smoke
proof wording to product-neutral `governed product render proof` broke the M35 contract test, which
still asserted the old `governed Cabinet render proof`. The stale assertion was rewritten to the
current model rather than preserved. Lesson: when shared smoke language is product-neutralized,
older milestone contract tests must be updated in the same change.

A second process lesson: the M39 sprint status was updated before the product proof finished, so it
briefly disagreed with the story file about story 6-2's real state. Sprint status must be refreshed
as the final step of each story, after screenshots and checks complete.

## Handoff

M40 should build real composition quality on the Reality Graph: diagram constructs and engineering
reading order, functional regions, symbol grouping, contact/coil/terminal organization,
rail/rung/trunk/bundle composition, and better sheet density. M41+ should build professional
routing and a label engine toward EPLAN/QET-level readability. The measured Spatial baseline from
M39 is the starting truth for both.

M39 evidence currently lives in the working tree; the milestone commit is a follow-up once review
closes.
