---
title: Athena M41 - Spatial Reality Recovery
status: final
created: 2026-08-03
updated: 2026-08-03
---

# PRD: Athena M41 Spatial Reality Recovery

## 0. Document Purpose

This PRD is the decision and acceptance contract for M41. It feeds architecture, epic/story
creation, implementation, review, and milestone closure. Glossary terms are normative. Functional
Requirement consequences are atomic and keep their IDs downstream without weakening. Technical
mechanisms and rejected alternatives live in `addendum.md`.

M41 was reopened after the first pass produced deterministic placeholder fields and nonblank
screenshots while the visible drawing remained collapsed. The failed pass is evidence, not a
compatibility target.

## 1. Vision

Athena engineers write engineering meaning. Athena compiler derives geometry. Renderer paints the
derived result without repair.

M41 makes Spatial Reality trustworthy enough to carry the exact geometry between Projection
Reality and Presentation Reality. A fixed engineering example must become a coherent,
two-dimensional control drawing with complete subject coverage, exact port endpoints,
obstacle-safe basic routes, truthful per-sheet measurements, and source trace back to Projection.

M41 does not promise QElectroTech or EPLAN parity. It promises correct Spatial facts and a useful
composition foundation that M42-M45 can style, render/export, improve for readability, and optimize
professionally.

## 2. Target User And Job

Primary user: an engineer authoring human-first Athena source and inspecting the Control Drawing in
Athena Theia.

Job to be done: express engineering meaning once and receive a deterministic drawing whose
placement, grouping, route endpoints, obstacle relationships, grid references, and source trace can
be trusted without authored coordinates or renderer inference.

This internal compiler capability does not need a separate user-journey specification. Product
proof in Section 10 covers the real engineer-visible workflow.

## 3. Product Thesis And Authority

```text
Athena source
  -> Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia paint
```

Spatial Reality is the only authority for placement, bounds, anchors, lanes, routes, grid
references, and geometry quality facts. Projection owns which engineering subjects appear and how
they are grouped. Presentation preserves Spatial coordinates. Theia paints Presentation facts.

No source or Projection coordinate, second layout authority, fallback endpoint, silent route drop,
or renderer repair is permitted.

## 4. Glossary

- **Projection Snapshot:** immutable Projection input for one compilation, including Sheets,
  Occurrences, Regions, Constructs, Connections, ports, authored order, reading order, and grids.
- **Sheet:** one projected drawing surface. A Spatial Sheet owns all geometry and quality facts for
  that surface.
- **Drawing Area:** rectangular Sheet area available for Occurrences and Routes. Title-block and UI
  areas are excluded.
- **Occurrence:** one visible projected instance of an engineering subject. Each Occurrence has
  exactly one rectangle on exactly one Sheet.
- **Region:** ordered projected grouping of Occurrences. Each non-empty Region has exactly one
  padded union bound on its owning Sheet.
- **Construct:** ordered projected engineering grouping with a kind and member Occurrences. Each
  Construct has exactly one padded union envelope on its owning Sheet.
- **Anchor:** exact Spatial point for one referenced port on one Occurrence boundary. One port
  resolves to exactly one Anchor within a Projection Snapshot.
- **Connection:** visible projected engineering relation between one source occurrence-port and one
  target occurrence-port.
- **Route:** ordered nonzero orthogonal segments for exactly one Connection, beginning and ending at
  its exact Anchors.
- **Lane:** stable routing channel identity used by at least one Route on one Sheet. Route-to-Lane
  references and Lane-to-Route membership are consistent.
- **Grid Reference:** typed mapping from one Occurrence center or Construct envelope center to one
  cell in the owning Sheet grid.
- **Source Trace:** stable Projection identities plus resolvable source location/geometry references
  sufficient to name the originating Sheet, subject, Connection, occurrence-port, and Anchor where
  applicable.
- **Spatial Diagnostic:** plain failure containing exact subject, problem, correction, and Source
  Trace.
- **Blocking Metric:** metric whose nonzero value prevents M41 closure: Occurrence overlap,
  Construct containment failure, Route/body intersection, or twist.
- **Golden Fixture:** `examples/m41/rolling-shutter`, the fixed M41 source used for exact compiler and
  product proof.

## 5. Brownfield Delta

| Capability | Failed M41 behavior | Required M41 behavior | Required proof |
| --- | --- | --- | --- |
| Placement | `x = index * 140`, `y = 0` | Three ordered Region columns with meaningful X and Y ranges | Golden facts, shuffled-input equality, product pixels |
| Region geometry | No typed Spatial Region | One padded union bound per Region | Mixed-size union fixture and exact coverage |
| Construct geometry | Maximum member size pseudo-envelope | Padded union envelope of actual member rectangles | Offset mixed-size member fixture |
| Grid ownership | First grid and fixed global size | Owning-Sheet grid and independent per-Sheet mapping | Multi-Sheet fixture |
| Anchors | Partial endpoint assumptions | Exactly one stable boundary Anchor per referenced port | Missing/duplicate/shuffled port fixtures |
| Routing | Fallback first/last occurrence and silent `mapNotNull` loss | One obstacle-safe Route per Connection or complete failure | Missing-anchor and blocking-obstacle fixtures |
| Spatial model | Raw maps and overloaded occurrence IDs | Typed facts with stable identity, Sheet ownership, Source Trace | Equality and validation fixtures |
| Quality | Misnamed route-count label metrics and incomplete geometry math | Metric dictionary in Section 8 computed from Spatial facts | Hand-computable geometry fixtures |
| Product proof | PNG size and hardcoded booleans | Runtime counts, geometry ranges, pixel distribution, coordinate preservation | Desktop and narrow Electron screenshots |

## 6. Feature 1: Deterministic Spatial Composition

### FR-1: Compile Sheet-Owned Occurrence Placement

Spatial Reality shall produce exactly one positive-size Occurrence rectangle on exactly one owning
Sheet for every projected Occurrence.

Consequences:

- **FR-1.1:** Every M41 Sheet extent is exactly `1200 x 800`; Drawing Area is exactly
  `(x=40, y=60, width=1120, height=640)`; no Occurrence enters the title-block band beginning at
  `y=740`.
- **FR-1.2:** Every Occurrence rectangle is fully inside its Drawing Area and no distinct pair has
  positive shared area. Edge contact alone is not overlap.
- **FR-1.3:** Recompiling the same Projection Snapshot, including any permutation of unordered
  input collections, yields equal canonical placement facts.
- **FR-1.4:** Each placement reason names the Region, Construct/reading-order constraint, and stable
  fallback order actually used, without exposing solver implementation fields.
- **FR-1.5:** Projection and Athena source contain no placement coordinates or layout mechanics.

### FR-2: Compose Ordered Regions In Two Dimensions

Spatial Reality shall use authored Region order as a visible left-to-right composition constraint.

Consequences:

- **FR-2.1:** Golden Fixture contains exactly three Regions with disjoint ordered X ranges and a
  32-unit gutter between adjacent Region columns.
- **FR-2.2:** Occurrences within a Region use Construct membership, Connection topology, authored
  order/reading order when present, then stable identity.
- **FR-2.3:** Vertically adjacent Occurrences maintain at least 48 units of separation.
- **FR-2.4:** Golden Fixture occupied Occurrence range spans at least 55% of Drawing Area width and
  at least 45% of Drawing Area height.
- **FR-2.5:** Unassigned Occurrences use one explicit final Region and stable order; none are
  discarded.

### FR-3: Derive Region Bounds, Construct Envelopes, And Alignment

Spatial Reality shall derive grouping geometry from placed Occurrence rectangles.

Consequences:

- **FR-3.1:** Every projected Region has exactly one typed Region fact whose members equal the
  projected membership and whose bound is the member-rectangle union expanded by 24 units on every
  side.
- **FR-3.2:** Every projected Construct has exactly one typed Construct fact whose ordered members
  equal the projected membership and whose envelope is the member-rectangle union expanded by 24
  units on every side.
- **FR-3.3:** Every Construct envelope fully contains every member rectangle. Maximum member width
  or height alone cannot satisfy this consequence.
- **FR-3.4:** Alignment facts reference existing Occurrences on the same Sheet and reflect the
  Region/Construct constraints used in placement.
- **FR-3.5:** Empty, duplicate, unknown, or cross-Sheet Region/Construct membership fails before
  Presentation.
- **FR-3.6:** Every Region bound and Construct envelope is positive and fully inside its Drawing
  Area; placement leaves enough edge clearance for required padding.

### FR-4: Derive Owning-Sheet Grid References

Spatial Reality shall derive one Grid Reference for every Occurrence and Construct from the center
of its rectangle/envelope and the owning Sheet grid.

Consequences:

- **FR-4.1:** Cell mapping uses Drawing Area-relative coordinates and owning-Sheet row/column
  counts; another Sheet grid cannot affect the result.
- **FR-4.2:** Occurrence and Construct Grid Reference coverage is exact, typed, and one-to-one.
- **FR-4.3:** Missing grids, nonpositive dimensions, out-of-area centers, or unsupported row counts
  fail with a Spatial Diagnostic.
- **FR-4.4:** Multi-Sheet fixtures prove independent grids and denominators.

## 7. Feature 2: Exact Basic Routing

### FR-5: Resolve Stable Port Anchors

Spatial Reality shall resolve every Connection endpoint port to exactly one Anchor on its
Occurrence boundary.

Consequences:

- **FR-5.1:** Anchor identity includes owning Sheet, Occurrence, and port identity and remains
  stable across recompilation and shuffled port input.
- **FR-5.2:** Stable port order distributes multiple ports along the selected body edge without
  collapsing distinct ports to one point.
- **FR-5.3:** Direction and Connection orientation choose a preferred edge without changing
  engineering meaning.
- **FR-5.4:** A missing or duplicate endpoint port fails the complete Spatial compilation with a
  Spatial Diagnostic; no approximate Anchor is created.

### FR-6: Produce One Obstacle-Safe Orthogonal Route Per Connection

Every visible Connection shall produce exactly one Route on the endpoint Sheet or the complete
Spatial compilation shall fail.

Consequences:

- **FR-6.1:** Route count equals visible Connection count exactly; no route is silently omitted.
- **FR-6.2:** Route first point equals source Anchor point and final point equals target Anchor point
  exactly, without rounding, snapping, or renderer inference.
- **FR-6.3:** Every Route contains at least one positive-length segment and every segment is
  horizontal or vertical.
- **FR-6.4:** No Route segment intersects the positive-area interior of a non-endpoint Occurrence.
  Endpoint-owner bodies and boundary contact at the Anchor are excluded.
- **FR-6.5:** The same Projection Snapshot produces equal canonical Routes across repeated runs and
  unordered-input permutations.
- **FR-6.6:** If no obstacle-safe basic Route exists, Spatial compilation fails; it does not emit a
  degraded or partial document.
- **FR-6.7:** A Connection whose endpoints own different Sheets fails with a Spatial Diagnostic;
  M41 does not invent multi-Sheet continuation.

### FR-7: Publish Complete Route Trace

Every Route shall carry non-null source and target occurrence-port-anchor identities, Connection
identity, owning Sheet, Lane identity, and Source Trace.

Consequences:

- **FR-7.1:** Each Route trace resolves to exactly one Connection and its exact two endpoint
  Anchors.
- **FR-7.2:** No first/last Occurrence fallback, nullable endpoint trace, or silent route filtering
  exists in the active product path.
- **FR-7.3:** A trace failure names Connection, missing/ambiguous endpoint, correction, and source
  location and blocks the complete Spatial document.

### FR-8: Publish Stable Basic Lanes Without Optimization Claims

Every Route shall reference exactly one stable Lane on its owning Sheet; each used Lane shall list
its Routes consistently.

Consequences:

- **FR-8.1:** Lane assignment is deterministic and every Lane is used by at least one Route.
- **FR-8.2:** M41 does not claim or perform bend/crossing minimization, bundle/trunk routing,
  professional lane optimization, or multi-Sheet continuation. These remain M45.

## 8. Feature 3: Typed Spatial Contract And Validation

### FR-9: Publish Typed Per-Sheet Spatial Facts

Spatial Reality shall publish typed facts for Sheets, Occurrences, Regions, Constructs, Anchors,
Routes, Lanes, Grid References, and quality snapshots.

Consequences:

- **FR-9.1:** Every fact has a stable identity, owning Sheet, and the Source Trace fields required
  by its row in Section 11.
- **FR-9.2:** Construct identity is never stored in an Occurrence identity field; Grid References
  are not raw string maps.
- **FR-9.3:** Canonical fact ordering makes document equality independent of unordered input order.
- **FR-9.4:** Golden Fixture exact coverage is 8 Occurrences, 3 Regions, 7 Constructs, one Anchor per
  referenced port, one Route per visible Connection, one Grid Reference per Occurrence/Construct,
  and no unreferenced Lane.

### FR-10: Validate The Complete Spatial Document

The compiler shall validate the complete typed Spatial document before Presentation receives any
facts.

Consequences:

- **FR-10.1:** Validation covers exact subject/Connection/Anchor/Lane/Grid Reference coverage,
  unique identities, Sheet ownership, positive in-area geometry, membership, containment, Route
  endpoints, orthogonality, nonzero segments, obstacle safety, complete trace, and metric integrity.
- **FR-10.2:** Validation returns all deterministic issues sorted by subject then problem.
- **FR-10.3:** Every issue is a Spatial Diagnostic with subject, problem, correction, and Source
  Trace.
- **FR-10.4:** Any issue blocks Presentation transformation; no partial Spatial document or
  compatibility shim is emitted.

### FR-11: Preserve Spatial Authority Through Presentation And Theia

Presentation and Theia shall consume Spatial coordinates without deriving, snapping, rerouting,
repairing, or replacing geometry.

Consequences:

- **FR-11.1:** Presentation coordinates and ordered Route points equal Spatial coordinates and
  ordered Route points exactly.
- **FR-11.2:** Renderer proof values come from runtime payloads and rendered pixels, not hardcoded
  booleans or script-text inspection.

## 9. Feature 4: Truthful Per-Sheet Quality

### FR-12: Compute Geometry Metrics From Spatial Facts

Every Spatial Sheet shall publish the following metric dictionary from the same validated geometry
that reaches Presentation.

Consequences:

- **FR-12.1 Occurrence overlap count:** number of distinct Occurrence rectangle pairs with positive
  shared area; edge contact is excluded.
- **FR-12.2 Construct containment failure count:** number of Construct members not fully contained
  by their Construct envelope.
- **FR-12.3 Route/body intersection count:** number of Route segments intersecting a non-endpoint
  Occurrence interior; endpoint-owner bodies and Anchor boundary contact are excluded.
- **FR-12.4 Route crossing count:** number of distinct `(unordered Route pair, perpendicular
  intersection point)` tuples, excluding shared engineering junctions and shared endpoint Anchors.
  Collinear shared-Lane travel is not a crossing.
- **FR-12.5 Twist count:** number of non-horizontal and non-vertical Route segments.
- **FR-12.6 Lane use:** number of Lanes carrying at least one Route and peak Routes assigned to one
  Lane.
- **FR-12.7 Density:** Occurrence count divided by Drawing Area area, computed independently per
  Sheet.
- **FR-12.8 Occupancy:** union area of Occurrence rectangles divided by Drawing Area area, computed
  independently per Sheet. Region and Construct overlays do not inflate it.
- **FR-12.9:** M41 publishes no label count, label pressure, or label collision metric because M42
  owns label geometry.

### FR-13: Publish A Reproducible Golden Baseline

M41 shall publish exact Golden Fixture facts and metric values generated from compiler output.

Consequences:

- **FR-13.1:** Baseline records source digest, Sheet/Drawing Area bounds, subject counts, metric
  definitions, exact per-Sheet values, generation command, and timestamp.
- **FR-13.2:** Tests compute and compare structured compiler facts; prose constants and
  self-comparisons cannot satisfy the baseline.
- **FR-13.3:** Where an M40 measurement is genuinely comparable, the table names identical units,
  fixture, viewport, and method; incomparable label metrics are omitted.

## 10. Success Metrics And Closure Gates

### Primary Metrics

- **SM-1 Spatial coverage:** exact Golden Fixture counts in FR-9.4 with no missing, duplicate, or
  cross-Sheet fact. Validates FR-1 through FR-10.
- **SM-2 Spatial coherence:** three ordered Region columns, occupied width ratio at least 0.55,
  occupied height ratio at least 0.45, and all Blocking Metrics equal zero. Validates FR-1 through
  FR-3 and FR-12.
- **SM-3 Routing completeness:** Route count equals visible Connection count; all endpoints exact;
  all segments nonzero/orthogonal; Route/body intersection and twist counts equal zero. Validates
  FR-5 through FR-8.
- **SM-4 Product reality:** desktop and narrow Control Drawing screenshots show non-background body
  and Route pixels across at least three horizontal and three vertical Drawing Area buckets, with
  runtime counts/metrics and Presentation-coordinate equality. Validates FR-9 through FR-13.

### Counter-Metrics

- **SM-C1:** Do not minimize Density or Occupancy as quality scores; both describe composition and
  have no M41 optimization target.
- **SM-C2:** Do not trade missing subjects or Routes for lower overlap/intersection counts; exact
  coverage gates are evaluated first.
- **SM-C3:** Do not add authored coordinates, renderer repair, or a second layout engine to improve
  screenshots.
- **SM-C4:** Do not claim professional routing/readability parity from zero Blocking Metrics.

### Delivery And Product Proof Gates

- **GATE-1:** Replacement PRD, architecture, epics, and sprint plan pass BMad validation and
  implementation readiness with no unresolved critical/high finding.
- **GATE-2:** Every story is recreated using `bmad-create-story` and implemented using
  `bmad-dev-story` in numeric order from 1.1 through 5.3.
- **GATE-3:** Every acceptance consequence cited by a story has an actually passing behavioral test;
  story tasks, Debug Log, Completion Notes, File List, Change Log, review, and sprint status agree.
- **GATE-4:** Kernel/compiler, LSP, frontend, and Electron product surfaces are rebuilt before final
  E2E. Gradle verification commands run sequentially on Windows.
- **GATE-5:** Desktop and narrow screenshots are fresh under
  `_bmad-output/implementation-artifacts/m41/screenshots`, inspected by a human, and satisfy SM-4.
- **GATE-6:** Source-set hygiene, encoding audit, repository tests/build, frontend tests/build,
  product E2E, screenshot pixel/layout checks, and `git diff --check` pass from fresh commands.
- **GATE-7:** Story 5.3 and retrospective begin only after Stories 1.1-5.2 and Epics 1-4 are `done`.
  Retrospective records exact evidence and does not claim QElectroTech/EPLAN parity.

Failure of any gate blocks M41 closure.

## 11. Subject-By-Fact Contract

| Subject | One per projected subject | Rectangle/bound | Sheet owner | Stable identity | Source Trace | Grid Reference | Quality input |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Sheet | Yes | extent + Drawing Area | self | Yes | Yes | grid definition | denominator/aggregation |
| Occurrence | Yes | rectangle | Yes | Yes | Yes | center cell | occupancy/overlap/obstacle |
| Region | Yes | padded member union | Yes | Yes | Yes | No | containment context only |
| Construct | Yes | padded member union envelope | Yes | Yes | Yes | center cell | containment |
| Anchor | One per referenced port | point on Occurrence edge | Yes | Yes | occurrence-port trace | No | Route endpoint |
| Route | One per Connection | ordered segments | Yes | Yes | Connection + endpoints | No | intersection/crossing/twist |
| Lane | One per used channel | routing channel fact | Yes | Yes | Route membership | No | lane use |
| Quality snapshot | One per Sheet | metric values | Yes | Yes | contributing Spatial facts | No | result |

## 12. Non-Functional Requirements

- **NFR-1 Determinism:** same Projection Snapshot produces byte-equivalent canonical Spatial facts
  across repeated runs and unordered-input permutations.
- **NFR-2 Human-first diagnostics:** normal failure text names exact engineering subject, problem,
  and correction; internal diagnostic codes do not become authoring syntax or primary UI text.
- **NFR-3 Clean pre-1.0 architecture:** no compatibility shim, stale alternative pipeline,
  milestone-named production type, `V0`/`V1` type, or proof/demo/sample class in `src/main`.
- **NFR-4 Small public contract:** Spatial facts are typed, deterministic, inspectable, documented,
  implementation-neutral, and do not expose one solver or electrical policy as kernel truth.
- **NFR-5 Source authority:** Athena source remains human-first and geometry-free; XML is not runtime
  authority; SVG may own stable geometry references but not engineering facts.

## 13. Non-Goals And Roadmap Boundary

- Authored coordinates, layout hints, paint mechanics, or routing mechanics in Athena source.
- QElectroTech or EPLAN visual parity.
- M42: Presentation styling, label layout, label metrics, visibility, terminal labels, grid chrome.
- M43: SVG/Theia/PDF/Canvas rendering and Excel position export surface.
- M44: readability optimization and quality target tuning beyond M41 correctness gates.
- M45: professional lane/bend/crossing optimization, bundles/trunks, and multi-Sheet continuation.
- M46: AI-assisted or semantic-assisted placement.
- Compatibility with the failed M41 pass or preservation of its tests/proof.

## 14. Open Questions

None. Any new issue that changes a consequence or gate requires BMad course correction before the
affected story proceeds.

## 15. Assumptions Index

None. The milestone owner approved the recovery design, exit bar, fixture, scope boundary, and BMad
story workflow in the active 2026-08-03 recovery session.
