---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 2.3: Trace Routes And Hold The No-Optimization Boundary

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want incomplete Route trace to block compilation,
so that every visible Connection remains auditable to source and ports.

## Acceptance Criteria

1. Given a canonical Projection Snapshot with one or more visible Connections and otherwise valid
   placed Occurrences, Drawing Areas, and Anchors, Route compilation resolves the complete visible
   Connection set before publication. If any source or target Anchor is missing or ambiguous, it
   returns all deterministic endpoint diagnostics and publishes no Route or Lane subset. Each
   diagnostic names the exact Connection, source or target role, occurrence-port, owning Sheet when
   known, problem, correction, and Source Trace. `ProjectionSpatialCompiler` returns
   `RealityTransformationResult.Failure`; no `SpatialDocument`, quality facts, Presentation facts,
   fallback Occurrence endpoint, approximate Anchor, or renderer output is produced. Diagnostics
   remain equal under Connection and Anchor input permutations and sort by subject then problem.
   (FR-7.2, FR-7.3, FR-10.2, FR-10.3, FR-10.4, NFR-1, NFR-2)
2. Given a valid published `SpatialRoute`, its typed Route identity resolves exactly one owning
   Sheet and Projection Connection; its semantic Connection identity equals that Projection
   Connection; its source and target `SpatialAnchorId` values each name the exact same-Sheet
   Occurrence and semantic port and resolve exactly one published Anchor; and its Lane identity
   resolves exactly one reciprocal used Lane. The Route carries one required immutable
   `SpatialSourceTrace` whose canonical Projection identities cover the owning Sheet, Projection
   Connection, source Occurrence and port, and target Occurrence and port, and whose geometry
   sources cover the Connection origin plus both resolved Anchor derivations. Required identities
   come first in explicit endpoint order; inherited sources are deduplicated and canonicalized.
   Empty, missing, foreign, duplicated, contradictory, or reordered required trace components fail
   validation rather than being repaired downstream. (FR-7.1, FR-9.1, AD-22, AD-30)
3. Given repeated compilation and independent permutations of Connections, Sheets, Occurrences,
   Anchors, and trace contributors, complete Routes are equal, including Route/Connection/Sheet,
   endpoint Anchor, Lane, ordered points, and complete Source Trace. Route coverage equals the exact
   visible Projection Connection identity set, not only its count; no duplicate, extra, missing,
   cross-Sheet, or semantically mismatched Route can satisfy validation. Exact coverage is checked
   before quality so dropping a Route cannot improve any metric. (FR-6.1, FR-6.5, FR-7.1, FR-8.1,
   NFR-1, SM-C2)
4. Given review of the active product Route path, no first/last Occurrence fallback, nullable public
   Route trace, approximate endpoint, silent Route `mapNotNull`/filter loss, partial-success branch,
   compatibility adapter, or Presentation/Theia endpoint repair remains. Diagnostic-only collection
   helpers are allowed when they cannot discard a Connection or Route. `ProjectionSpatialCompiler`
   remains the sole Projection-to-Spatial orchestrator and stops before quality/document assembly
   on any Route issue. (FR-7.2, FR-10.4, AD-22, AD-27, NFR-3)
5. Given the rolling-shutter Golden Fixture, every one of the 9 visible Projection Connections has
   one literal expected Route trace covering the exact Connection, source/target occurrence-port
   Anchors, owning Sheet, reciprocal Lane, and canonical source geometry references. Tests compare
   the complete structured trace snapshot and complete Route equality under named input
   permutations. Nonempty trace, count-only coverage, `associateBy` overwrite, self-comparison,
   copied expected values, or a trace derived from the actual Route under test cannot satisfy this
   story. Story 2.2 exact points and 7 used Lanes remain unchanged. (FR-7.1, FR-9.4, SM-3, GATE-3)
6. Given the public Spatial routing contract and production source, M41 publishes traceable,
   deterministic basic Routes and used Lanes only. It adds no bend/crossing minimization, bundle,
   trunk, Lane packing, continuation, electrical-role, label, solver-tuning, or professional-routing
   field or claim. M42 owns labels, M43 rendering/export, M44 readability optimization, M45
   professional routing and multi-Sheet continuation, Story 3.1 final per-Sheet document roots,
   Story 3.2 complete independent-document validation, and Story 4.1 quality formulas. (FR-8.2,
   NFR-3, NFR-4, SM-C4)

## Tasks / Subtasks

- [x] Task 1: Prove complete Route trace and all-or-nothing endpoint failure with RED tests (AC: 1,
      2, 3)
  - [x] Extend `SpatialRouteCompilerTest.kt` with literal successful Route trace expectations and
        separate missing-source, missing-target, duplicate-source, and duplicate-target Anchor
        fixtures. Each failure must assert exact human-first diagnostic fields, Source Trace, and
        empty Routes/Lanes. Record the first failing RED assertion or compilation before production
        edits.
  - [x] Add a multi-Connection fixture where one Connection is valid and another has an unresolved
        endpoint. Prove every deterministic issue is returned canonically and the valid Connection
        is not published as a partial Route/Lane subset.
  - [x] Extend `ProjectionSpatialCompilerTest.kt` with route-stage failure proof returning
        `RealityTransformationResult.Failure` and no `SpatialDocument`; verify quality and
        Presentation cannot observe partial Route facts.
- [x] Task 2: Add the required Route Source Trace without duplicating endpoint authority (AC: 2,
      6)
  - [x] Evolve `SpatialRoutingModels.kt` so `SpatialRoute` requires non-null
        `sourceTrace: SpatialSourceTrace`. Keep `SpatialAnchorId` as the typed source/target
        occurrence-port identity authority; do not add raw occurrence/port mirrors, nullable trace,
        a second provenance type, compatibility constructor, or milestone/versioned type.
  - [x] Preserve immutable defensive copies and the existing Sheet-qualified structured identities.
        Lane trace remains its exact canonical `routeIds` membership; do not invent authored Lane
        provenance or routing syntax.
  - [x] Add Spatial-model contract tests proving trace is required, immutable, equality-bearing,
        and complete typed Anchor IDs still resolve Sheet/Occurrence/port without duplicated fields.
- [x] Task 3: Build one canonical successful Route trace and complete endpoint diagnostics (AC: 1,
      2, 3)
  - [x] Extract one cohesive compiler-side Route trace composition rule shared by
        `SpatialRouteCompiler` and `SpatialRouteValidator`; the current compiler file already mixes
        resolution, solve adaptation, diagnostics, and Lane derivation, so place distinct trace
        normalization in `SpatialRouteTraceSupport.kt` if that keeps responsibilities scannable.
  - [x] Compose required Projection identities in explicit order: owning Sheet, Projection
        Connection, source Occurrence, source port, target Occurrence, target port. Add Connection
        origin and both exact Anchor derivation sources; deduplicate and canonically order inherited
        contributors without allowing unrelated incident Connection IDs to replace required Route
        identity.
  - [x] Enrich missing/ambiguous endpoint diagnostics with owning Sheet and exact occurrence-port
        context. Reuse `diagnostic()` and `canonicalDiagnostics()` for trace union, duplicate issue
        merging, and subject/problem ordering; never drop a Connection while collecting issues.
- [x] Task 4: Validate exact trace, identity coverage, and fail-closed integration (AC: 1-4)
  - [x] Extend `SpatialRouteValidator` to recompute the expected Route trace from the exact
        Projection Connection and resolved Anchors, then compare complete canonical trace equality.
        Independently mutate/remove Sheet, Connection, each endpoint Occurrence/port, and required
        geometry origins so no broad helper-generated fixture can hide missing fields.
  - [x] Preserve Story 2.2 validator checks for exact Connection identity, Anchor endpoint equality,
        endpoint-owner and non-endpoint interiors, Drawing Area, positive orthogonal segments,
        reciprocal Lane membership, and canonical Lane channel. Story 3.2 owns moving these checks
        into complete independent-document validation.
  - [x] Update `SpatialToPresentationTransformation` to copy Route source Projection identities
        from the canonical Spatial Route trace rather than rebuilding a partial pair. Preserve exact
        points, typed endpoint IDs, complete shared Lane membership, and semantic visible labels.
- [x] Task 5: Prove Golden trace and no-optimization boundary (AC: 3, 5, 6)
  - [x] Extend `DedicatedM41ExampleTest.kt` with a literal Connection-to-Route trace oracle for all
        9 visible Connections. Keep literal ordered Route points and the 7-Lane membership oracle;
        compare complete Routes under named Connection, Sheet, Occurrence, Anchor, and trace-source
        permutations.
  - [x] Add contract/audit proof that public Spatial Route/Lane facts contain no planner, optimizer,
        bundle, trunk, continuation, electrical role, bend score, crossing score, label, or solver
        tuning surface. Do not mechanically ban diagnostic-only `mapNotNull`; prove no Route can be
        silently discarded through behavior.
  - [x] Preserve Stories 2.1-2.2 exact Anchors, obstacle-safe point lists, endpoint side guards,
        overflow-safe no-path behavior, blocker diagnostics, and semantic Connection labels.
- [x] Task 6: Verify sequentially and complete BMad records (AC: 1-6)
  - [x] Run focused Spatial routing model, Route compiler/validator, orchestrator, Presentation, and
        Golden tests; then `:kernel:spatial-model:test`, `:kernel:compiler:test`, repository `test`,
        source-set hygiene audit, encoding audit, and `git diff --check`, strictly sequentially.
  - [x] Confirm no routing solver or quality formula changed, no production source-set hygiene
        violation exists, and every cited acceptance consequence has an actually passing behavioral
        test.
  - [x] Complete every Task checkbox, Debug Log, Completion Notes, File List, Change Log, review
        finding, and sprint status before moving Story 2.3 to review and adversarial acceptance.

### Review Findings

- [x] [Review][Patch] Reject Routes whose typed Anchors contradict Projection endpoint roles
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt:114`]
- [x] [Review][Patch] Preserve full six-position required Route trace when endpoint identities repeat
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteTraceSupport.kt:13`]
- [x] [Review][Patch] Reject unique foreign-Sheet Anchors during role-specific endpoint resolution
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt:252`]
- [x] [Review][Patch] Add orchestrator proof that one invalid Route blocks a valid sibling and all
  downstream Spatial facts
  [`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt:175`]
- [x] [Review][Defer] Reject duplicate Lane identities before Spatial-to-Presentation lookup
  [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt:56`]
  - deferred to Story 3.2 complete independent-document validation; compiler-stage Lane validation
    remains fail closed.

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  Projection owns Connection and occurrence-port meaning without coordinates. Spatial owns exact
  Anchors, Routes, Lanes, and source trace. Presentation copies; renderer paints.
- Apply AD-20, AD-22, AD-23, AD-26, AD-27, AD-28, AD-29, and AD-30.
  `ProjectionSpatialCompiler` remains the only public Projection-to-Spatial orchestrator and already
  stops on Route diagnostics before quality or document assembly.
- Story 2.3 is provenance and completeness work, not a routing algorithm story. Do not edit
  `OrthogonalRouteSolver` or `AthenaRouteEngine` unless a failing Story 2.3 acceptance test proves
  the shared geometry core violates trace publication; no current requirement does.
- Fail closed. Endpoint resolution is a complete canonical pass across every visible Connection.
  Any issue returns empty Route/Lane facts and prevents quality, `SpatialDocument`, Presentation,
  and renderer output.
- Pre-1.0 cleanup applies. Migrate all Route constructors and consumers directly to required trace.
  No default trace, compatibility overload, fallback branch, raw-string mirror, or stale parallel
  path.

### Exact Route Trace Contract

- `SpatialRouteId` already carries owning Sheet plus Projection Connection identity;
  `connectionId` carries semantic Connection identity; `sourceAnchorId` and `targetAnchorId` already
  carry complete Sheet-qualified Occurrence and semantic port identity. Do not duplicate these
  fields under weaker names.
- Add one required `SpatialSourceTrace` to `SpatialRoute`. Required Projection identity order is
  owning Sheet, Projection Connection, source Occurrence, source port, target Occurrence, target
  port. Route typed Anchor fields name the two derived Anchors exactly.
- Required geometry sources include `ProjectionConnection.originGeometryElementId` and the sources
  required to derive both exact Anchors. Preserve source/target role order for required identities;
  deduplicate remaining identities and sort inherited geometry IDs by value.
- Anchor traces may include other incident Connections because Anchor side/position can depend on
  all incidents. Do not blindly concatenate their `projectionIds` into the required Route prefix.
  Use one trace builder that deliberately separates required Route identity from inherited sources.
- `SpatialSourceTrace` already rejects empty/blank content, takes immutable copies, and participates
  in structural equality. Reuse it. Do not create `RouteProvenance`, nullable fields, or transport
  DTOs in Spatial.
- `SpatialLane.routeIds` is the Lane's Route-membership trace for this story. Story 3.1 owns final
  Source Trace completion for every per-Sheet fact.

### Current Code Intelligence

- `SpatialRoutingModels.kt` has a 158-line cohesive routing contract. `SpatialRoute` currently owns
  Route/Sheet/semantic Connection IDs, exact source/target Anchor IDs, Lane, points, and segments,
  but no Source Trace. Add the field here; do not split tiny routing values across files.
- `SpatialRouteCompiler.kt` is about 390 lines. It resolves owning Sheet and exact Anchors, gathers
  all resolution diagnostics, solves canonically, builds Routes/Lanes, and validates them. Its
  successful Route constructor omits trace. Missing/ambiguous endpoint diagnostics currently omit
  owning Sheet in some branches.
- `SpatialRouteValidator.kt` already checks exact Projection/semantic Connection identity, Anchor
  resolution, endpoint points/sides, all Occurrence interiors, Drawing Area, canonical Lane channel,
  and reciprocal membership. Extend rather than duplicate these predicates.
- `SpatialAnchorCompiler.kt` establishes the local normalization pattern: required Projection IDs
  first; additional inherited IDs distinct and sorted; geometry IDs distinct and sorted by value.
- `SpatialToPresentationTransformation.kt` currently rebuilds connector source IDs from Route ID
  plus semantic Connection ID. It must consume canonical Route trace while preserving coordinates
  and paint-only authority.
- The active Route path's `mapNotNull` gathers diagnostics; it does not filter Routes. FR-7.2 bans
  silent Route loss, not safe diagnostic collection. Keep or refactor based on clarity and totality,
  never token matching.
- `SpatialQualityCompiler` does not consume trace and remains Story 4.1 scope. Do not change current
  metric formulas here.

### Previous Story Intelligence

- Story 2.2 established typed Sheet-owned Routes/Lanes, one bounded deterministic solver, exact
  endpoints, positive orthogonal segments, obstacle avoidance, 7 used Golden Lanes for 9 Routes,
  and all-or-nothing routing.
- Its adversarial review fixed endpoint-owner re-entry, governed zero-stub fallback, all-pairs graph
  growth, clearance overflow, Sheet-incomplete Anchor resolution, missing/duplicate visible geometry,
  exact Connection identity coverage, complete shared Lane membership, Lane channel validation,
  blocker diagnostics, missing direct/one-bend/permutation proof, and serialized-ID labels. Preserve
  all fixes.
- Complete independent-document routing validation remains recorded in
  `_bmad-output/implementation-artifacts/m41/deferred-work.md` for Story 3.2. Do not pull it into 2.3.
- Current fresh verification after Story 2.2 passed 81 routing-model tests, 15 spatial-model tests,
  461 compiler tests, repository `test` with 148 tasks, source-set hygiene, encoding audit, and
  `git diff --check`.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). M41 work is
  intentionally uncommitted across completed Stories 1.1-2.2; preserve the current worktree and do
  not reconstruct files from `HEAD`.
- Recent relevant commit `8fd6e34` planned M41 recovery. No new dependency or framework is required;
  repository versions remain Kotlin 2.4.0 and Gradle 9.6.1.
- Story 2.3 should be a narrow Spatial-model/compiler/Presentation/test delta. Avoid unrelated
  cleanup in quality, solver, example infrastructure, or later-story contracts.

### Project Structure Notes

- Primary updates:
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`,
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`,
  `SpatialRouteValidator.kt`, and `SpatialToPresentationTransformation.kt`.
- A small new `SpatialRouteTraceSupport.kt` is appropriate only if it gives compiler and validator
  one trace-composition authority and reduces the current compiler's mixed responsibilities.
- Expected test updates:
  `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`,
  compiler `SpatialRouteCompilerTest.kt`, `ProjectionSpatialCompilerTest.kt`,
  `SpatialToPresentationTransformationTest.kt`, `DedicatedM41ExampleTest.kt`, and shared fixtures
  that construct Routes.
- Mechanical constructor migrations may reach quality/Presentation tests. Update them to complete
  typed trace fixtures; do not weaken assertions or introduce a default helper that generates the
  expected value from the actual subject under test.
- No UX artifact applies. No web research is needed because Story 2.3 introduces no external API,
  library, or version decision.

### Testing Requirements

- TDD per task: focused RED, smallest GREEN, refactor, rerun focused. Record actual first failure.
- Trace tests use literal expected identity/geometry lists. Test each required component independently;
  nonempty or superset checks cannot prove exact trace.
- Multi-defect tests verify all deterministic diagnostics and canonical order. Missing/ambiguous
  endpoints must never allow valid sibling Connections to publish.
- Permutation tests mutate every named independent input, including trace contributor order; compare
  complete structured Routes and diagnostics, not derived counts.
- Golden trace expectations remain independent literals. Keep exact Story 2.2 point and Lane
  snapshots unchanged.
- Run Gradle commands strictly sequentially on Windows. After code/docs, run encoding audit; after
  production changes, run source-set hygiene audit.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-2.3-Trace-Routes-And-Hold-The-No-Optimization-Boundary`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-7-Publish-Complete-Route-Trace`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-8-Publish-Stable-Basic-Lanes-Without-Optimization-Claims`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-10-Validate-The-Complete-Spatial-Document`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-22-ADOPTED---Routing-Starts-From-Exact-Typed-Port-Anchors-And-Fails-Closed`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-23-ADOPTED---M41-Basic-Routing-Does-Not-Claim-Professional-Optimization`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-30-ADOPTED---Normalization-Owns-Stable-Identity-Trace-And-Ordering`]
- [Source: `_bmad-output/implementation-artifacts/m41/2-2-build-orthogonal-route-facts-and-lanes.md`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, all PRD and architecture
  shards, approved Story 2.2 records, current CodeGraph, git state, and three independent analyses.
- RED: `SpatialRoute` constructor migrations first failed compilation on unresolved required
  `sourceTrace`; route endpoint tests then failed on missing owning-Sheet/port trace details.
- RED: validator mutation tests proved incomplete and reordered Route trace was accepted before
  exact trace recomputation; Presentation tests proved connectors rebuilt only a partial source list.
- RED: Golden fixture lacked literal trace columns for all 9 Routes; complete trace oracle failed
  until canonical trace publication and permutation preservation were implemented.
- GREEN: required Route trace, shared canonical builder, endpoint diagnostics, exact validation,
  Presentation provenance, multi-defect fail-closed behavior, and literal Golden trace all passed.
- 2026-08-03 verification: focused Spatial routing-model and compiler/orchestrator/Presentation/
  Golden suites passed; `:kernel:spatial-model:test` passed 17 tests; `:kernel:compiler:test` passed
  466 tests; repository `test` passed 148 tasks. Routing-model XML remained 81 passing tests.
- 2026-08-03 audits: source-set hygiene and encoding audits passed; `git diff --check` passed with
  pre-existing line-ending warnings only.
- 2026-08-04 adversarial review: Acceptance Auditor and Edge Case Hunter identified four actionable
  gaps; Blind Hunter timed out and was recorded as a failed review layer. RED rerun produced four
  failing tests for swapped endpoint roles, repeated required trace identity, and foreign-Sheet
  endpoint diagnostics; orchestrator valid-sibling failure proof passed immediately.
- 2026-08-04 review GREEN: all four patches passed focused suites; `:kernel:spatial-model:test`
  passed 17 tests; `:kernel:compiler:test` passed 469 tests; repository `test` passed 148 tasks;
  routing-model remained 81 tests. Source-set hygiene, encoding, and `git diff --check` passed.

### Implementation Plan

- Specify exact successful Route trace, complete endpoint failure, trace mutation, permutation, and
  Golden behavior with focused RED tests.
- Add one required canonical Route Source Trace and share its composition rule across compilation,
  validation, and Presentation provenance.
- Preserve Story 2.2 path/Lane behavior and prove the active path stays fail closed without entering
  M45 optimization scope.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added required immutable equality-bearing `SpatialRoute.sourceTrace` without duplicating typed
  Sheet/Connection/Anchor endpoint authority or adding compatibility paths.
- Added one canonical Route trace builder shared by compilation and validation: required identities
  retain Sheet/Connection/source/target order; inherited Projection and geometry sources normalize
  deterministically.
- Endpoint resolution now returns complete human-first source/target diagnostics and fails closed
  across every visible Connection. Multi-defect proof returns both sorted endpoint issues while
  publishing zero Routes/Lanes despite a valid sibling Connection.
- Validator now rejects missing, reordered, extra, duplicated, and foreign Projection or geometry
  trace components through exact recomputation; exact Connection, Anchor, Lane, and path checks stay
  intact.
- Presentation copies canonical Route Projection provenance. Golden fixture asserts literal trace
  for all 9 Routes, retains exact point oracles and 7 Lanes, and is equal under named permutations.
- No routing solver, Lane algorithm, quality formula, professional-routing surface, or later-
  milestone behavior changed. All focused, module, repository, hygiene, encoding, and diff gates
  passed sequentially.
- Resolved all four review patches: validator independently binds typed Anchors to Projection source/
  target roles; six-position trace preserves repeated identities; endpoint resolution accepts only
  the Anchor on each endpoint Occurrence's actual Projection Sheet; orchestrator proof blocks a
  valid sibling and every downstream Spatial fact when one cross-Sheet Route fails.
- Deferred duplicate independent `SpatialDocument` Lane identity validation to Story 3.2 in the
  milestone-local deferred-work record. Explicit cross-Sheet failure still names M45 continuation.

### File List

- `_bmad-output/implementation-artifacts/m41/2-3-trace-routes-and-hold-the-no-optimization-boundary.md`
- `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteTraceSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`

### Change Log

- 2026-08-03: Created and validated through BMad create-story from milestone-local M41 artifacts;
  marked ready-for-dev.
- 2026-08-03: Added canonical complete Route Source Trace, fail-closed endpoint diagnostics, exact
  trace validation, Presentation provenance preservation, literal Golden proof, and sequential
  regression/audit evidence; marked review.
- 2026-08-04: Resolved four adversarial review patches, preserved explicit cross-Sheet diagnostics,
  passed 469 compiler and repository-wide regression gates, recorded one Story 3.2 defer, and marked
  done.
