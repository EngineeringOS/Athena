# M41 Spatial Reality Recovery Design

Date: 2026-08-03
Status: approved

## Problem

The first M41 pass proved field presence and deterministic placeholder behavior, not Spatial
Reality. Every occurrence was placed in one row at `y = 0`, construct bounds were guessed from the
largest member, routes could silently disappear, quality names did not match their calculations,
and E2E accepted any PNG larger than 1024 bytes. The resulting drawing collapsed into an unreadable
strip while all focused tests passed.

M41 must be reopened. Its PRD, epics, stories, implementation, tests, proof, and closure records
must be replaced in numeric story order.

## Product Exit Bar

M41 delivers a coherent Spatial drawing, not QElectroTech parity:

- regions and constructs visibly influence a deterministic 2D composition;
- occurrences do not overlap and remain inside their owning sheet;
- routes start and end at exact port anchors and avoid non-endpoint bodies;
- Spatial facts carry stable identity, sheet ownership, and source trace;
- geometry metrics use specified formulas and hand-verifiable fixtures;
- desktop and narrow product proof shows useful content distributed in two dimensions;
- Presentation and Theia consume geometry without snapping, rerouting, inference, or repair.

Presentation styling and label layout stay M42. Rendering/export stays M43. Readability
optimization stays M44. Professional routing optimization stays M45. M41 still requires correct
basic routing: exact endpoints, orthogonal nonzero segments, and zero non-endpoint body
intersections.

## Architecture

The authority chain remains:

```text
Athena source
  -> Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia paint
```

`ProjectionSpatialCompiler` becomes the single Spatial orchestration entry. It performs these
steps:

1. Validate and canonicalize the Projection snapshot.
2. Convert sheet, region, construct, occurrence, connection, and reading-order facts into layout
   intent and constraints.
3. Use the current rule-based layout engine through a generic Projection adapter.
4. Normalize solver output into typed Spatial facts.
5. Derive port anchors from placed occurrence geometry and stable port order.
6. Use `AthenaRouteEngine` for exact-anchor, obstacle-aware orthogonal routes and lanes.
7. Derive per-sheet grid references and truthful quality measurements.
8. Validate the complete Spatial document before Presentation receives it.

The reusable engines remain internal deterministic solvers. Spatial owns their normalized output.
No solver type, electrical policy field, or layout algorithm leaks into Athena authoring syntax.

The reset removes competing placeholder behavior:

- one-row `SpatialPlacementCompiler` behavior;
- first/last endpoint routing fallback;
- silent `mapNotNull` route loss;
- maximum-member pseudo-envelopes;
- raw metric-name/value placeholders;
- experimental ELK wrapper with no external engine;
- milestone-named or stale milestone comments in active product paths.

## Spatial Contract

Spatial facts are scoped per sheet. Raw maps and overloaded occurrence IDs are not sufficient.

### Sheet

Each Spatial sheet records stable sheet identity, compiler-owned extent, drawing-area bounds, grid
definition, occurrence geometry, region geometry, construct geometry, routes, lanes, grid
references, quality snapshot, and source trace.

Sheet extent is compiler policy, not authored geometry. The active M41 policy uses a 1200 by 800
sheet and a content drawing area at `(40, 60)` sized 1120 by 640. The bottom title-block band begins
at `y = 740`. Projection supplies grid row/column counts and sheet order.

### Occurrence

Each projected occurrence has exactly one placement rectangle on exactly one sheet. It records:

- occurrence and semantic subject identity;
- sheet identity;
- top-left position and positive size;
- placement explanation referencing region, construct constraints, reading order, and stable
  fallback order actually used;
- source projection identities;
- stable port anchors;
- center-derived grid cell.

### Region

Each projected region has exactly one Spatial region fact. Region order follows authored order.
Its bounds are the padded union of member occurrence rectangles. Empty, duplicate, unknown, or
cross-sheet membership fails validation.

### Construct

Each projected construct has exactly one Spatial construct fact. It records construct kind,
ordered members, source trace, and a padded union envelope derived from actual member rectangles.
Construct geometry never pretends to be occurrence geometry. Its grid cell derives from envelope
center.

### Anchors And Routes

Every connection endpoint port resolves to exactly one anchor on its occurrence. Stable port order
distributes anchors along the relevant body edge. Direction and connection orientation select the
preferred edge without changing engineering meaning.

Every visible Projection connection produces exactly one Spatial route. Route facts record:

- route and connection identity;
- source/target occurrence, port, and anchor identity;
- owning sheet and lane identity;
- ordered orthogonal nonzero segments;
- exact source and target anchor points;
- source trace and route-quality result.

Missing or ambiguous endpoints fail. No first/last occurrence fallback exists. A route that cannot
avoid non-endpoint bodies fails the M41 gate instead of being silently degraded.

## Placement Policy

Placement is deterministic and visibly two-dimensional:

1. Sheets sort by authored order and stable sheet identity.
2. Region bands sort left-to-right by authored region order.
3. Occurrences within a region sort using construct membership, connection topology, authored
   sheet/construct order when applicable, then stable identity.
4. Region columns use a 32-unit gutter. Occurrences use at least 48 units of vertical separation;
   region and construct envelopes use 24 units of padding.
5. The solver verifies that every rectangle fits the drawing area without overlap.
6. Unassigned occurrences use a final explicit region and stable order; they are never discarded.

For the rolling-shutter fixture, the three authored regions must occupy distinct left-to-right
columns, and their members must span meaningful horizontal and vertical ranges. A single-row result
cannot satisfy the geometry or E2E gates.

## Quality Definitions

All metrics are computed per sheet from Spatial geometry. Construct and region overlays do not
inflate occurrence occupancy.

- **Occurrence overlap count:** number of distinct occurrence rectangle pairs with positive shared
  area. Edge contact is not overlap.
- **Construct containment failures:** number of construct members not fully contained by their
  construct envelope.
- **Route/body intersection count:** number of route segments intersecting a non-endpoint
  occurrence interior. Endpoint-owner bodies and anchor boundary contact are excluded.
- **Route crossing count:** intersections between segments from distinct routes, excluding shared
  engineering junctions and shared endpoint anchors.
- **Twist count:** number of non-orthogonal route segments. M41 requires zero.
- **Lane use:** number of lanes carrying at least one route plus peak routes assigned to one lane.
- **Density:** occurrence count divided by drawing-area units.
- **Occupancy:** union area of occurrence rectangles divided by drawing-area area.

M41 does not claim label collision or label-pressure metrics. M42 owns label geometry; M44 may set
later readability targets. The M41 proof publishes exact values for its golden fixture and blocks
closure on overlap, containment, body-intersection, or twist failure.

## Validation And Diagnostics

Complete Spatial validation runs before Presentation. It verifies:

- exact occurrence, region, construct, connection, anchor, lane, and grid-reference coverage;
- unique stable identities and valid sheet ownership;
- positive finite geometry inside the drawing area;
- valid region/construct membership and containment;
- exact route endpoints, orthogonal nonzero segments, and obstacle avoidance;
- complete occurrence, port, anchor, connection, and source trace;
- valid metric values derived from the same Spatial sheet.

Every failure reports the exact subject, problem, correction, and source trace. Example:

```text
Connection 'control:StartButton.contact13->Contactor.coilA1' has no target anchor for port
'Contactor.coilA1'. Ensure the projected occurrence exposes that port before compiling Spatial
Reality. Source: 01-rolling-shutter-spatial.athena:74.
```

No compatibility shim, fallback endpoint, partial document, or renderer repair is allowed.

## Verification Design

### Unit And Contract Tests

- exact golden facts for the M41 rolling-shutter fixture;
- deterministic equality after repeated compilation and shuffled input order;
- multi-sheet ownership and independent grids;
- exact one-to-one coverage for every subject and connection;
- construct union-envelope and region union-bounds examples;
- exact grid cells from geometry centers;
- route endpoint, orthogonality, obstacle, lane, crossing, and trace checks;
- hand-computable metrics covering overlap, edge contact, segment/body intersection, shared
  junctions, crossing, union occupancy, and density;
- one negative test per validator branch, including complete diagnostic content;
- Presentation coordinate preservation and paint-only authority checks.

### Product E2E

The product verifier rebuilds kernel, LSP, frontend, and Electron surfaces before proof. It asserts
runtime geometry, not script text:

- active M41 source and Control Drawing surface;
- expected occurrence, region, construct, anchor, route, and lane counts;
- exact route count equals compiled connection count;
- zero overlap, containment failure, route/body intersection, and twist;
- meaningful occupied horizontal and vertical ranges;
- rendered body and route pixels inside the drawing area at desktop and narrow viewports;
- no top-strip collapse, blank canvas, internal diagnostic IDs as primary labels, or overlapping UI
  chrome;
- renderer coordinates equal Presentation coordinates.

Fresh screenshots replace the failed images under
`_bmad-output/implementation-artifacts/m41/screenshots`. A human review of both screenshots is a
closure gate, not retrospective commentary.

## Story Recovery

The PRD, addendum, memlog, epics, sprint status, and every story are corrected before production
work. Each story is recreated with `bmad-create-story` and implemented with `bmad-dev-story` in
numeric order.

1. Story 1.1: deterministic explainable 2D occurrence placement.
2. Story 1.2: occurrence bounds, alignment, region bounds, and construct envelopes.
3. Story 1.3: per-sheet occurrence and construct grid facts.
4. Story 2.1: exact per-port anchors and route endpoint equality.
5. Story 2.2: obstacle-aware orthogonal segments and stable lanes.
6. Story 2.3: complete route trace and fail-closed no-fallback boundary.
7. Story 3.1: typed geometry identities, ownership, and source trace.
8. Story 3.2: complete Spatial validation and actionable diagnostics.
9. Story 4.1: truthful geometry quality metrics.
10. Story 4.2: per-sheet density, occupancy, and published golden baseline.
11. Story 5.1: dedicated M41 example with exact four-reality proof.
12. Story 5.2: rebuilt product E2E, pixel/layout assertions, and fresh screenshots.
13. Story 5.3: honest retrospective after every prior story is accepted.

For every story: discover, mark in-progress, red-green-refactor per task, run focused tests, run the
required broader suite sequentially, complete all story records, mark review, perform acceptance
review, then mark done. The retrospective cannot become complete while any story or epic remains
open.

## Non-Goals

- Authored coordinates or layout hints.
- QElectroTech or EPLAN visual parity.
- Professional crossing/bend minimization, bundles, trunks, or multi-sheet continuation.
- Presentation styling or a label layout engine.
- Excel, SVG, PDF, or Canvas export work.
- AI-assisted placement.
- Compatibility with the failed first M41 implementation.
