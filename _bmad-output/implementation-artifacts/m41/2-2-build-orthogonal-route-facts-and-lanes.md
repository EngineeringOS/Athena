---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 2.2: Build Orthogonal Route Facts And Lanes

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want basic Routes to avoid non-endpoint Occurrence bodies,
so that the Control Drawing does not show impossible connections.

## Acceptance Criteria

1. Given one or more valid Projection Connections, exact endpoint Anchors, owning-Sheet Drawing
   Areas, and placed Occurrence rectangles, routing publishes exactly one canonical `SpatialRoute`
   per visible Connection or publishes no Routes. Every Route belongs to the one Sheet shared by its
   two Anchors, starts and ends at those exact integer Anchor points, contains at least one
   positive-length segment, stays inside the Drawing Area, and contains only horizontal or vertical
   segments. Consecutive duplicate points and zero-length segments are absent. Initial/final
   segments respect the Anchor boundary side so the path leaves and approaches endpoint owners from
   outside rather than traversing their interiors. (FR-6.1, FR-6.2, FR-6.3, AD-22)
2. Given a non-endpoint Occurrence body between two Anchors, every published segment avoids the
   positive-area interior of that body. Boundary travel and point contact are permitted; intersection
   with positive interior area is not. Endpoint-owner bodies are excluded from the FR-6.4 obstacle
   count, while the side-exit rule in AC 1 still prevents routing through them. Obstacle checks use
   exact integer segment/rectangle predicates, not vertex-only checks, pixels, renderer state, or a
   quality proxy. A hand-computable fixture proves a segment whose endpoints are both outside a body
   but whose interior passes through it is rejected and rerouted. (FR-6.4, NFR-4)
3. Given repeated compilation and permutations of Projection Connections, Anchors, Occurrence
   geometry, Sheets, and solver candidate inputs, complete canonical Route points, Route ordering,
   Lane identities, Lane membership, and Route-to-Lane assignments are equal. Route and Lane
   identities derive from owning Sheet plus stable Projection/Connection or channel identity, never
   request position or unordered collection iteration. The public basic Lane contract is typed,
   Sheet-owned, direction-safe, and lists every assigned Route once; no empty Lane is published and
   every Route references exactly one published Lane. Existing Grid Reference language remains
   vertical row letters plus horizontal column numbers (`A1`, `B3`); Lane identity is a routing
   channel fact and must not overload Grid Reference rows or cells. (FR-6.5, FR-8.1, FR-9.1, NFR-1)
4. Given obstacles and Drawing Area boundaries for which no legal basic orthogonal path exists, the
   route stage returns deterministic actionable diagnostics naming the Connection, obstruction,
   correction, and Source Trace. The stage publishes no Route or Lane subset, and
   `ProjectionSpatialCompiler` returns `RealityTransformationResult.Failure` with no
   `SpatialDocument`, quality facts, Presentation facts, or renderer output. It never returns a
   crossing path, an out-of-area detour, a degraded solver result, or a dropped Connection. (FR-6.6,
   FR-10.2, FR-10.3, FR-10.4, NFR-2)
5. Given a Connection whose source and target Anchors own different Sheets, routing fails before
   path search with one deterministic human-first diagnostic. It names the Connection and both
   Sheets, directs the engineer to keep the Connection on one Sheet or defer explicit continuation
   to M45, and preserves Connection/Anchor Source Trace. No cross-Sheet Route, synthetic
   continuation Anchor, fallback Sheet, or partial Lane is created. (FR-6.7, FR-8.2, AD-23)
6. Given the rolling-shutter Golden Fixture, Route coverage equals the exact visible Projection
   Connection set, all first/final points equal the exact Story 2.1 Anchors, all segments are
   positive, orthogonal, in-area, and obstacle-safe, and all used Lane facts and memberships equal a
   literal canonical expected snapshot. Tests compare complete ordered point lists and stable Lane
   assignments; nonempty, minimum-count, self-comparison, copied quality constants, or screenshot
   size cannot satisfy this story. M41 performs deterministic basic path selection only. It does not
   claim or implement bend/crossing minimization, bundle/trunk routing, professional Lane packing,
   multi-Sheet continuation, labels, or Presentation styling. (FR-6.1 through FR-6.7, FR-8.1,
   FR-8.2, FR-9.4, SM-3, SM-C4)

## Tasks / Subtasks

- [x] Task 1: Prove route geometry and fail-closed behavior with RED tests (AC: 1, 2, 4, 5)
  - [x] Expand `SpatialRouteCompilerTest.kt` with hand-computable same-Sheet fixtures for direct,
        one-bend, and obstacle-detour paths. Assert literal ordered points, exact endpoint equality,
        positive orthogonal segments, Drawing Area containment, Anchor-side exit/entry, and strict
        body-interior avoidance.
  - [x] Add a segment-through-body fixture whose vertices remain outside the body, a boundary-contact
        fixture, an impossible Drawing Area barrier, and a cross-Sheet Connection. Assert exact
        diagnostics plus empty Routes/Lanes on every failure. Record first failing RED assertion or
        compilation before production edits.
  - [x] Add orchestrator RED tests proving any route-stage issue becomes complete
        `RealityTransformationResult.Failure` and prevents quality/Presentation output.
- [x] Task 2: Replace temporary Route/Lane primitives with cohesive typed Spatial contracts (AC: 1,
      3, 5)
  - [x] Evolve `SpatialRoutingModels.kt` with typed Route and Lane identities, owning `sheetId`,
        direction/orientation enum, canonical Route membership, and explicit positive orthogonal
        segment derivation from ordered integer points. Keep closely related routing values together;
        do not create one file per tiny type.
  - [x] Remove temporary raw `direction = "horizontal"`, `lane:<view>:main`, and unowned Lane facts.
        A used Lane must identify its Sheet and exact canonical Routes. A Route must identify its
        Sheet and one typed Lane. Add no compatibility constructor, raw-string mirror, `V0`/`V1`
        type, or milestone-named production type.
  - [x] Migrate compiler, Spatial quality, Presentation, and tests directly. Preserve Story 2.1 typed
        Anchor identity and exact integer endpoints. Story 2.3 will complete Route source trace;
        Story 3.1 will assemble the final per-Sheet document root.
- [x] Task 3: Reuse the existing routing geometry core behind a domain-neutral bounded solver (AC:
      1, 2, 4)
  - [x] Refactor the reusable orthogonal geometry from `AthenaRouteEngine` into one domain-neutral
        routing-model solver API that accepts stable request identity, exact integer endpoints,
        endpoint sides, Drawing Area, and canonical rectangular obstacles. Both
        `AthenaRouteEngine` and the Spatial adapter must use this one geometry core; do not duplicate
        Manhattan/path-search logic.
  - [x] Keep electrical roles, labels, authored `SourceProvenance`, route-quality policy, bundles,
        and solver tuning out of the domain-neutral solver and all public Spatial models. Existing
        governed routing callers may adapt their richer inputs/output around the shared core.
  - [x] Make candidate generation and selection total, bounded by Drawing Area, overflow-safe with
        `Long` intermediates, deterministic under input permutation, and complete for the finite
        axis-aligned rectangle candidate graph used by M41. A legal candidate edge may touch a body
        boundary but may not enter its positive-area interior. Return an explicit no-path result;
        never return the current intersecting default as degraded success.
- [x] Task 4: Compile one obstacle-safe Route per same-Sheet Connection (AC: 1, 2, 4, 5)
  - [x] Change `SpatialRouteCompiler` to consume Projection, exact Anchors, placed Occurrences, and
        explicit owning-Sheet Drawing Area inputs. Resolve the complete request set canonically,
        reject cross-Sheet endpoints before solving, and exclude only the two typed endpoint-owner
        Occurrences from non-endpoint obstacles.
  - [x] Normalize solver output to exact `SpatialPoint` lists. Remove consecutive duplicates,
        reject zero-length/non-orthogonal/out-of-area/interior-crossing segments, preserve exact
        first/final Anchor points, and collect all deterministic route issues before returning empty
        facts.
  - [x] Add a cohesive `SpatialRouteValidator` stage for count, identity, ownership, endpoint,
        segment, Drawing Area, obstacle, and Lane integrity. Story 3.2 may reuse it from complete
        document validation; do not build a second predicate set later.
- [x] Task 5: Publish deterministic basic Lane facts without M45 claims (AC: 3, 6)
  - [x] Define one documented basic Lane assignment rule from canonical solved channel facts. Lane
        identities must remain equal across request/obstacle permutations and must not use raw
        request index as identity. Canonically sort Lane facts and each Lane's Route membership.
  - [x] Prove every published Lane is used, every Route appears in exactly one Lane membership list,
        the Route references that same Lane, different Sheets cannot share a Lane, and no Lane
        references a missing Route.
  - [x] Do not minimize bend count, crossing count, Lane count, or packing. Do not add bundles,
        trunks, continuation, label clearance, electrical role, or professional-routing claims.
- [x] Task 6: Integrate, prove Golden facts, and verify sequentially (AC: 1-6)
  - [x] Update `ProjectionSpatialCompiler` to pass exact occurrence geometry and per-Sheet Drawing
        Areas into routing. Route/Lane failure remains before quality and complete document
        publication.
  - [x] Replace the Golden Fixture's temporary Route/Lane expectations with literal exact canonical
        Route point lists, typed Sheet ownership, Lane identities, and memberships for all visible
        Connections. Add complete permutation equality for Routes and Lanes.
  - [x] Preserve Stories 1.1-2.1 placement, grouping, grid, Anchor, Projection, Presentation endpoint,
        and Golden Anchor suites. Run focused routing-model/Route/orchestrator/Golden tests,
        `:kernel:routing-model:test`, `:kernel:spatial-model:test`, `:kernel:compiler:test`, repository
        `test`, source-set hygiene audit, encoding audit, and `git diff --check` strictly
        sequentially. Complete story records before review.

### Review Findings

- [x] [Review][Patch] Prevent Routes from re-entering endpoint-owner interiors after valid side exit
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt:142`]
- [x] [Review][Patch] Remove governed routing's `stubLength = 0` fallback that can violate Anchor side
  [`kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt:224`]
- [x] [Review][Patch] Replace all-pairs solver neighbor construction with bounded visibility adjacency
  [`kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/OrthogonalRouteSolver.kt:119`]
- [x] [Review][Patch] Return deterministic `NoPath` when obstacle clearance expansion exceeds `Int`
  bounds [`kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/OrthogonalRouteSolver.kt:85`]
- [x] [Review][Patch] Resolve Anchors through Connection owning Sheet and complete typed identity
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt:240`]
- [x] [Review][Patch] Reject missing or duplicate visible Occurrence obstacle geometry
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt:24`]
- [x] [Review][Patch] Validate exact Projection Connection-to-Route identity coverage, not count only
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt:30`]
- [x] [Review][Patch] Preserve complete canonical Lane membership in Presentation connectors
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt:96`]
- [x] [Review][Patch] Validate every Lane channel describes actual assigned Route geometry
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt:126`]
- [x] [Review][Patch] Name canonical blocking Occurrences or Drawing Area in no-path diagnostics
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt:180`]
- [x] [Review][Patch] Add literal direct/one-bend and Golden named-input permutation proof
  [`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt:33`]
- [x] [Review][Patch] Stop exposing serialized typed Route identity as default visible label
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt:45`]
- [x] [Review][Defer] Move complete independent-document routing invariants into `SpatialReality.validate`
  [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:98`] - deferred to
  Story 3.2, which owns final complete-document geometry validation.

## Dev Notes

### Architecture Guardrails

- Authority stays Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  Projection owns Connection and occurrence-port meaning without coordinates. Spatial owns Drawing
  Area, Anchors, Routes, obstacles, and Lanes. Presentation and renderer copy/paint only.
- Apply AD-20, AD-22, AD-23, AD-26, AD-27, AD-28, AD-29, and AD-30.
  `ProjectionSpatialCompiler` remains sole public Spatial orchestrator. Shared routing-model geometry
  is internal mechanism, not another Spatial authority.
- `AthenaRouteEngine` already owns deterministic orthogonal helpers and Lane allocation, but its
  current public input is coupled to `ElectricalConnectionRoleFact`, `TerminalAnchorFact`, labels,
  route quality, and authored provenance. Extract/reuse its geometry core; do not manufacture fake
  electrical facts or leak these contracts into Spatial.
- Current engine `laneAround` can return an intersecting default as degraded success, checks body
  boundaries as intersections, lacks Drawing Area maximum bounds, and considers only aggregate
  outside corridors. Story 2.2 must replace those correctness gaps in the shared geometry core.
- Fail closed. Any request, solver, normalization, or Lane issue publishes no Route/Lane list and
  blocks complete `SpatialDocument`. No `mapNotNull`, first/last fallback, approximate endpoint,
  partial success, or renderer repair.
- Pre-1.0 cleanup applies. Delete temporary Story 2.1 Lane behavior and migrate callers directly.
  No compatibility shim or stale parallel path.

### Exact Route Geometry

- Public coordinates remain `Int`. Use `Long` for sums, differences, Manhattan length, and candidate
  arithmetic before checked conversion to `Int`.
- Drawing Area includes its boundary. Every Route point lies within the closed Drawing Area. Each
  segment lies within it because it is axis-aligned between in-area endpoints.
- For rectangle `(x, y, width, height)`, positive interior is `(x, x + width) x (y, y + height)`.
  A horizontal segment enters interior only when its fixed `y` is strictly inside and its open
  traveled X interval overlaps the rectangle's open X interval by positive length. Apply the dual
  rule to vertical segments. Edge travel and corner/point contact are legal.
- Obstacles are all same-Sheet Occurrence rectangles except the exact source and target Occurrence
  identities named by the endpoint Anchors. Never exclude by semantic subject alone because one
  subject may have multiple Occurrences.
- First segment must move outward from source side (`LEFT` decreases X, `RIGHT` increases X, `TOP`
  decreases Y, `BOTTOM` increases Y); final segment approaches target from its outside direction.
  Direct Routes on a shared axis still require these side conditions.
- Candidate graph must include exact Anchors plus Drawing Area/obstacle boundary axes needed to
  route along legal edges. Canonical node/edge order and deterministic tie-breaks are contract;
  shortest/best-looking path is not. Compact consecutive collinear segments only when exact
  endpoints and legality remain unchanged.

### Basic Lane Contract

- Lane is internal routing channel fact, not authored syntax and not Grid Reference. Preserve the
  established human coordinate notation: vertical grid rows use `A`, `B`, `C`...; horizontal grid
  columns use `1`, `2`, `3`...; cells read `A1`, `B3`. Do not rename these to routing Lanes.
- Use typed Sheet-qualified Lane identity and typed Route membership. Identity must encode structured
  components collision-safely, following Story 2.1's labeled UTF-8 form encoding pattern rather
  than delimiter-concatenated display text.
- Assignment may select a deterministic channel exposed by the solved path, but it must describe
  actual geometry and remain stable under input permutation. It cannot be a global fake `main` Lane
  or a raw request index.
- M41 Lane facts prove deterministic basic routing only. Zero crossings/bends are not Lane promises;
  M45 owns professional routing optimization, bundles/trunks, packing, and continuation.

### Current Code Intelligence

- `SpatialRouteCompiler.kt` currently accepts only Projection plus Anchors, creates one raw
  `lane:<view>:main`, emits a midpoint Manhattan path, has no Drawing Area or obstacle input, and
  does not canonicalize Connection input before Route publication.
- `SpatialRoutingModels.kt` currently stores raw Lane ID/direction and raw Route ID/Lane ID. Route
  has typed endpoint Anchor IDs and integer points but no Sheet ownership, typed Lane identity,
  membership, segment contract, or Source Trace.
- `ProjectionSpatialCompiler.kt` already has placed Occurrences and fixed per-Sheet Drawing Area
  available before routing. It currently calls routing with only Projection and Anchors, then passes
  returned facts to quality and `SpatialDocument`.
- `SpatialReality.validate` currently checks only Lane ID existence, Anchor existence, and endpoint
  point equality for Routes. Story 2.2 stage validation must own full route correctness now; Story
  3.2 will make complete document validation the final gate.
- `AthenaRouteEngine.kt` contains reusable orthogonal segment construction, side stubs, obstacle
  checks, detours, and deterministic request sorting. Its current `SchematicComponentBounds.intersects`
  treats boundary contact as intersection and solve output may be degraded rather than fail closed.
- Existing Golden Fixture has 9 visible Connections and 16 exact Anchors from Story 2.1. Story 2.2
  must prove all 9 exact ordered Paths and only their used Lanes, not merely count them.

### Previous Story Intelligence

- Story 2.1 established coordinate-free typed Projection endpoints/ports, Sheet-qualified typed
  Anchor identities, exact integer boundary points, canonical Source Trace, and fail-closed
  Anchor/orchestrator behavior. Keep these contracts unchanged.
- Story 2.1 review fixed two subtle identity bugs: always compare complete Sheet-qualified
  `SpatialOccurrenceId`, and serialize structured IDs with labeled UTF-8 form encoding. Apply both
  lessons to obstacle owner matching and new Route/Lane identities from first RED tests.
- Story 1.3 review established that duplicate facts may carry secondary defects, repeated
  diagnostics merge traces canonically, model invariants reject contradictory facts, and each named
  independent input needs an actual mutation test.
- Latest sequential verification after Story 2.1 passed focused routing/Anchor/Golden tests,
  Projection-model, Spatial-model, 446 compiler tests, repository `test` (148 tasks), source-set
  hygiene, encoding audit, and `git diff --check`.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). M41 work is
  intentionally uncommitted across prior completed stories; do not revert or overwrite it.
- Recent relevant docs commit `8fd6e34` planned M41 Spatial recovery. Current work has no
  routing-model production diff yet, so Story 2.2 can refactor the shared geometry core without
  reconciling an earlier M41 routing-model edit.
- Story 2.1 changed Projection, compiler, Spatial-model, Presentation, and Golden tests. Read and
  preserve current worktree state, not baseline source.

### Project Structure Notes

- Primary updates:
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`,
  `ProjectionSpatialCompiler.kt`,
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`,
  `SpatialDocument.kt`, and
  `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`.
- Expected focused tests:
  `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`,
  `ProjectionSpatialCompilerTest.kt`, `DedicatedM41ExampleTest.kt`, Spatial-model routing tests, and
  routing-model solver/engine tests. Add `SpatialRouteValidator.kt` and its test only if separation
  keeps compiler flow cohesive.
- Keep domain-neutral reusable path geometry in `kernel/routing-model`; keep Projection/Spatial
  adaptation and diagnostics in `kernel/compiler`; keep only implementation-neutral immutable
  Route/Lane facts in `kernel/spatial-model`.
- No new library or framework is required. Repository versions remain Kotlin 2.4.0 and Gradle 9.6.1.
  No web research changes implementation guidance.

### Testing Requirements

- TDD per task: focused RED, smallest GREEN, refactor, rerun focused. Record actual first failure.
- Exact behavior tests: literal point lists, segment predicates, diagnostics, Lane identity and
  membership, complete result equality under every named permutation, same semantic subjects on
  different Sheets/Occurrences, boundary contact, and impossible routing.
- Shared-core tests must prove both Spatial adapter and existing `AthenaRouteEngine` use the same
  bounded orthogonal geometry behavior; no duplicate private solver is acceptable.
- Run Gradle commands strictly sequentially on Windows. After text/code work, run encoding audit;
  after production changes, run source-set hygiene audit. Never run Gradle verification in parallel.
- Story cannot move to review/done until every AC has passing tests and Tasks, Debug Log,
  Completion Notes, File List, Change Log, review findings, and sprint status agree.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-2.2-Build-Orthogonal-Route-Facts-And-Lanes`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-6-Produce-One-Obstacle-Safe-Orthogonal-Route-Per-Connection`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-8-Publish-Stable-Basic-Lanes-Without-Optimization-Claims`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md#Architecture-Mechanism`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-22-ADOPTED---Routing-Starts-From-Exact-Typed-Port-Anchors-And-Fails-Closed`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-23-ADOPTED---M41-Basic-Routing-Does-Not-Claim-Professional-Optimization`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-28-ADOPTED---Existing-Solvers-Stay-Internal-And-Domain-Neutral`]
- [Source: `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md#Anchors-And-Routes`]
- [Source: `_bmad-output/implementation-artifacts/m41/2-1-build-anchor-accurate-routes.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`]
- [Source: `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, all five PRD shards, all five
  architecture shards, approved recovery design, Story 2.1 records, current code through CodeGraph,
  and git state.
- RED: route compiler fixtures could not compile against the temporary two-input API because no
  Sheet/Drawing Area or Occurrence obstacle inputs existed, and Route/Lane facts lacked typed Sheet
  ownership and segment contracts.
- GREEN: introduced typed routing facts and the shared bounded solver, then migrated compiler,
  Presentation, quality, and fixtures directly without compatibility paths.
- Migration regression: compiler Presentation tests expected raw `route:main-feed`/`lane:main`
  values; updated exact expectations to Sheet-qualified structured identities after confirming
  actual typed output.
- Golden RED: `DedicatedM41ExampleTest` expected an empty oracle and failed with the complete
  canonical 9-Route/8-Lane result; replaced it with a literal structured snapshot and reran green.
- Review RED: adversarial Route fixtures exposed endpoint-owner re-entry, governed zero-stub fallback,
  Sheet-incomplete Anchor resolution, incomplete Route/Lane validation, generic no-path diagnostics,
  incomplete shared-Lane paint membership, and missing direct/one-bend/permutation proof.
- Review GREEN: added endpoint collision guards, adjacent visibility edges, overflow-safe `NoPath`,
  Sheet-complete identity checks, fail-closed geometry coverage, exact Connection/Route/Lane
  validation, blocker names, complete shared membership, semantic labels, and literal fixtures.

### Implementation Plan

- Specify exact Route geometry, obstacle, no-path, cross-Sheet, identity, Lane-integrity, and
  permutation behavior with focused RED tests.
- Extract one domain-neutral bounded orthogonal solver and adapt both governed routing and Spatial
  routing to it.
- Publish typed Sheet-owned Routes/Lanes through the fail-closed Spatial orchestrator, migrate
  downstream paint/quality consumers, and verify the complete Golden Fixture snapshot.
- Run all focused/module/repository, hygiene, encoding, and diff gates sequentially before review.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added one bounded, deterministic `OrthogonalRouteSolver` shared by `AthenaRouteEngine` and M41
  Spatial routing. Solver respects exact endpoint sides, Drawing Area bounds, positive obstacle
  interiors, boundary contact, overflow-safe candidate arithmetic, and explicit no-path results.
- Replaced temporary raw Route/Lane primitives with Sheet-qualified structured identities,
  positive orthogonal segments, typed Lane orientation/channel ownership, and reciprocal canonical
  membership. No compatibility shim or M45 optimization surface added.
- `SpatialRouteCompiler` now resolves all Connections canonically, rejects cross-Sheet endpoints
  before search, guards endpoint-owner interiors after exact side exit, validates complete output,
  and fails closed with human-first diagnostics naming canonical obstructions and source trace.
- `ProjectionSpatialCompiler`, quality facts, Presentation paint, and fixtures now consume the typed
  contracts directly. Golden Fixture asserts exact ordered points and all 7 used Lane memberships
  for 9 visible Connections, including complete membership for the shared vertical Lane at X=600.
- Resolved all 12 adversarial review patches. Complete independent-document routing validation stays
  explicitly deferred to Story 3.2 in `deferred-work.md`, matching that story's ownership boundary.
- Sequential verification passed: focused Route/orchestrator/Golden/Presentation suites;
  `:kernel:routing-model:test` (81 tests); `:kernel:spatial-model:test` (15 tests);
  `:kernel:compiler:test` (461 tests); repository `test` (148 tasks); source-set hygiene; encoding
  audit; and clean `git diff --check`.

### File List

- `_bmad-output/implementation-artifacts/m41/2-2-build-orthogonal-route-facts-and-lanes.md`
- `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/AthenaRouteEngine.kt`
- `kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/OrthogonalRouteSolver.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineLaneAndAvoidanceTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/AthenaRouteEngineSideStubTest.kt`
- `kernel/routing-model/src/test/kotlin/com/engineeringood/athena/routing/OrthogonalRouteSolverTest.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`

### Change Log

- 2026-08-03: Created through BMad create-story from milestone-local M41 artifacts; marked
  ready-for-dev.
- 2026-08-03: Implemented shared bounded orthogonal routing, typed Sheet-owned Route/Lane facts,
  obstacle-safe fail-closed compilation, complete Golden snapshot, and sequential verification;
  marked ready for adversarial review.
- 2026-08-03: Addressed all 12 adversarial review patches, reran full repository and audit gates,
  and marked ready for final review closure.
- 2026-08-03: Code review closed with no unresolved patch or decision findings; marked done.
