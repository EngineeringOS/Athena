---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 4.1: Measure Quality Milestone Facts

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want quality metrics computed from Spatial geometry,
so that zero-defect claims are hand-verifiable and per-Sheet.

## Acceptance Criteria

1. Given one complete Sheet's typed Spatial facts, `SpatialQualityCompiler` publishes exactly one
   immutable, Sheet-owned quality snapshot with these typed fields and no open string-key metric
   map: Occurrence overlap count, Construct containment failure count, Route/body intersection
   count, Route crossing count, twist count, used Lane count, peak Routes per Lane, Density, and
   Occupancy. Counts are nonnegative integers; Density and Occupancy are finite nonnegative ratios.
   Metric order, equality, and output do not depend on input collection order. The snapshot keeps its
   stable Sheet-qualified identity and Source Trace to the same contributing facts that reach
   Presentation. (FR-12, AD-20, AD-24, AD-30)
2. Given distinct Occurrence rectangles on one Sheet, overlap count is the number of unordered pairs
   whose intersection width and height are both positive. Edge or corner contact is excluded. The
   same local Projection identity on another Sheet never participates because compilation is
   independently per Sheet. (FR-12.1)
3. Given Constructs and Occurrences on one Sheet, containment failure count is the number of
   Construct member Occurrences whose complete rectangle is not inside that Construct's envelope.
   Count failing member relationships, not Constructs, maximum-size guesses, Region bounds, or
   unowned Occurrences. Final Spatial validation remains responsible for missing, duplicate, or
   foreign member identities. (FR-12.2, AD-24, AD-26)
4. Given Route geometry, Route/body intersection count is the number of Route segments that
   intersect positive-area interior of at least one non-endpoint Occurrence. One segment contributes
   at most one even when it intersects several bodies. Segment/rectangle intersection is used, so a
   segment crossing a body with no vertex inside counts. Occurrences owning either endpoint Anchor
   are excluded, and boundary-only contact does not count. (FR-12.3)
5. Given two distinct Routes, crossing count is the number of distinct `(unordered Route pair,
   perpendicular intersection point)` tuples. Multiple segment-pair detections of the same tuple
   count once. Collinear overlap never counts. An intersection explicitly present as a vertex in
   both Routes is a shared engineering junction and is excluded. An intersection at endpoint points
   backed by the same endpoint `SpatialAnchorId` is also excluded. Coincident geometry alone does
   not invent an Anchor or connection relationship. (FR-12.4)
6. Given all Route segments on one Sheet, twist count increments once for each segment whose x and y
   both change. Every positive horizontal or vertical segment contributes zero; quality measurement
   neither repairs nor optimizes Route geometry. Used Lane count is the number of distinct existing
   Lanes assigned by at least one Route, and peak Routes per Lane is the largest actual Route count
   assigned to one existing Lane, or zero when no Route exists. Lane-list/Route reciprocity defects
   remain validation failures rather than alternate metric values. (FR-12.5, FR-12.6, AD-22,
   AD-24)
7. Given each Sheet's own Drawing Area, Density equals Occurrence count divided by positive
   Drawing Area area. Occupancy equals the geometric union area of that Sheet's Occurrence
   rectangles divided by Drawing Area area. Union area is overlap-safe and overflow-safe; Region and
   Construct overlays are excluded. Multi-Sheet inputs use independent numerators and denominators.
   Density and Occupancy are descriptive facts with no minimization target. (FR-12.7, FR-12.8,
   SM-C1)
8. Given quality construction, final Spatial validation, or compiler output, `NaN`, `Infinity`, and
   `-Infinity` never publish. Any invalid denominator or non-finite calculation fails closed before
   Presentation with a deterministic `SpatialDiagnostic` naming the quality snapshot, problem,
   correction, and relevant Source Trace; values are not clamped, coerced to zero, or partially
   emitted. This is Story 4.1's explicit metric-integrity clarification under FR-10/AD-26 because the
   M41 PRD does not otherwise specify numeric non-finite handling. (FR-10.1 through FR-10.4, AD-26)
9. Given M41 quality output and production source review, no label count, label pressure, label
   collision, route-count proxy, raw Lane-count proxy, `SpatialQualityMetricsReporter`, M40
   compatibility path, pixel-derived metric, authored coordinate, rendering repair, crossing
   minimization, readability target, or professional-routing claim exists. Exact coverage validates
   before quality claims, so missing Occurrences or Routes cannot lower a defect metric. Story 4.2
   owns generated baseline artifacts and their parser/verification. (FR-12.9, SM-C2 through SM-C4,
   AD-24, AD-27)

## Tasks / Subtasks

- [x] Task 1: Prove the final typed quality contract with RED model tests (AC: 1, 8, 9)
  - [x] Replace loose `SpatialQualityMeasurement(kind, value)` expectations with one typed metrics
        value owned by `SpatialQualitySnapshot`; assert all nine field names/types, equality,
        immutability, Sheet identity, Source Trace, and no raw map/string-key compatibility surface.
  - [x] Prove negative count/ratio and all non-finite ratio values cannot publish. Prove complete
        validation reports any representable metric-integrity defect with literal structured
        diagnostic fields and blocks Presentation.
  - [x] Update fixtures and Golden assertions to use explicit typed values. Add reflection/text
        assertions only for banned stale public names; behavioral values remain primary proof.
- [x] Task 2: Implement typed quality models and final metric-integrity validation (AC: 1, 8, 9)
  - [x] Refactor `SpatialQualityModels.kt` into cohesive identity, metrics, and snapshot contracts.
        Use nonnegative integer count fields and finite nonnegative Double ratios. Remove
        `SpatialQualityMeasurement`; do not keep deprecated constructors, adapters, raw maps, or
        aliases because Athena is pre-1.0.
  - [x] Update `SpatialValidation.qualityDiagnostics` for exact typed quality integrity and preserve
        deterministic subject/problem/correction/trace behavior. Do not duplicate formula
        computation in validation or Presentation.
  - [x] Migrate all production/test consumers once. Keep `SpatialQualitySnapshotId(sheetId)` and
        Sheet ownership semantics stable unless a proven typed identity defect requires a direct
        correction.
- [x] Task 3: Prove overlap and Construct containment formulas with RED tests (AC: 2, 3)
  - [x] Test separated, corner-touching, edge-touching, nested, and positive-overlap rectangle pairs;
        assert exact unordered-pair counts and input-permutation equality.
  - [x] Test multiple Constructs, repeated membership across distinct Constructs, contained members,
        and members extending past each envelope side. Assert failing member relationships, not
        Construct count or maximum rectangle size.
  - [x] Keep malformed missing/duplicate/foreign membership proof in `SpatialValidationTest`; quality
        tests use otherwise valid same-Sheet fact relationships.
- [x] Task 4: Prove segment/body and crossing geometry with RED tests (AC: 4, 5)
  - [x] Test a segment passing through a body with both vertices outside; body boundary travel;
        tangent/corner contact; endpoint-owner departure/re-entry; multiple bodies hit by one
        segment; and multiple defective segments. Assert each defective segment counts at most once.
  - [x] Test one perpendicular crossing, repeated segment-pair detection at the same point, multiple
        intersection points for one Route pair, different Route pairs at one point, collinear
        overlap, shared internal vertices, and matching shared endpoint Anchors. Assert exact
        canonical tuple counts and route/segment permutation equality.
  - [x] Use overflow-safe integer/Long comparison geometry. Do not add a second routing engine,
        floating tolerance, paint hit-testing, or mutate Route points.
- [x] Task 5: Prove twist and truthful Lane use with RED tests (AC: 6)
  - [x] Test horizontal, vertical, diagonal, reversed, and multi-segment Routes; assert one twist per
        non-orthogonal segment without optimization or repair.
  - [x] Test no Routes, several Routes on one Lane, and Routes distributed across Lanes; assert exact
        used-Lane and peak values from actual Route assignments to existing Lanes.
  - [x] Preserve final reciprocal Lane validation. Add no phantom Lane, global Lane denominator, or
        separate Lane metric authority.
- [x] Task 6: Prove Density and union-area Occupancy with RED tests (AC: 7, 8)
  - [x] Test empty, disjoint, touching, partially overlapping, nested, and multi-overlap Occurrence
        rectangles against literal Drawing Areas. Assert exact union area ratios and input-order
        independence; never sum rectangle areas as Occupancy.
  - [x] Test two Sheets with different Drawing Areas and occurrence compositions through
        `ProjectionSpatialCompiler`; assert each Sheet's literal Density/Occupancy and no
        cross-Sheet contamination. Region/Construct bounds changes must not change Occupancy.
  - [x] Use `Long` for area products/union accumulation before deterministic Double division. Do not
        hard-code the Golden `1120 x 640` Drawing Area or round values for display.
- [x] Task 7: Integrate exact per-Sheet measurement at the sole compiler boundary (AC: 1-9)
  - [x] Change `SpatialQualityCompiler` to consume one Sheet's Drawing Area, Occurrences, Constructs,
        Lanes, and Routes and return the complete typed metrics contract. Keep formula helpers
        cohesive and split by role if the file crosses the repository organization heuristic.
  - [x] Update `ProjectionSpatialCompiler` after each Sheet's exact facts are selected. Preserve
        stage order, canonical Sheet ordering, quality Source Trace, complete final validation, and
        the single all-or-nothing success boundary.
  - [x] Update authored, Projection, Presentation, fixture, and dedicated M41 Golden tests. Remove
        stale `label-pressure`, `route-count`, `lane-count`, old baseline-label test names, and every
        raw measurement lookup rather than translating them.
- [x] Task 8: Verify sequentially and complete BMad records (AC: 1-9)
  - [x] Run focused model, validator, quality compiler, Projection compiler, authored compilation,
        Presentation, and Golden tests; then `:kernel:spatial-model:test`,
        `:kernel:compiler:test`, repository `test`, source-set hygiene audit, encoding audit, and
        `git diff --check`, with Gradle commands strictly sequential.
  - [x] Confirm exact formulas, typed schema, per-Sheet isolation, deterministic permutations,
        complete Source Trace, finite output, banned-metric removal, no coverage tradeoff, and no
        later-milestone behavior. Every acceptance consequence needs an actually passing test.
  - [x] Complete every Task checkbox, Debug Log, Completion Notes, File List, Change Log, review
        finding, and sprint status before moving Story 4.1 to review and adversarial acceptance.

### Review Findings

- [x] [Review][Patch] Finite forged quality metrics pass Presentation preflight
      [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt:46`]
- [x] [Review][Patch] Extreme representable rectangles can throw during final quality validation
      [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityGeometry.kt:47`]
- [x] [Review][Patch] An empty Spatial document incorrectly validates as complete
      [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt:12`]

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  `ProjectionSpatialCompiler` remains sole Projection-to-Spatial orchestrator and complete success
  boundary. Quality reads compiler-owned Spatial facts; Presentation/Theia never recompute it.
- Apply AD-20, AD-24, AD-26, AD-27, and AD-30. One quality snapshot belongs to each `SpatialSheet`.
  Formula results come from same Sheet's facts after stage validation and are checked again as part
  of complete final Spatial validation before Presentation.
- Replace loose `kind/value` measurements with smallest coherent typed model. Counts are `Int`;
  ratios are `Double`. No serialization or external compatibility contract currently consumes the
  loose list, so delete it cleanly rather than preserve a migration branch.
- Coverage precedes claims. `ProjectionSpatialCoverageValidator` and `SpatialValidation` keep exact
  facts/relationships authoritative. Quality must not hide a missing fact or turn invalid input into
  a smaller score.

### Formula Contract

- Rectangle bounds are half-open for area: `[x, right) x [y, bottom)`. Positive overlap requires
  `max(left) < min(right)` and `max(top) < min(bottom)`.
- Construct failures count `(Construct, member Occurrence)` relationships where the resolved member
  rectangle is not fully inside that Construct envelope. Validated duplicate/missing membership is
  not reinterpreted by metric code.
- Body intersections count Route segments satisfying an existential body predicate, once per
  segment. Exclude endpoint-owner Occurrence IDs before geometry. Open-interior intersection excludes
  edge/corner-only contact.
- Crossing key contains canonical unordered Route IDs and exact integer intersection point. Only
  perpendicular horizontal/vertical segment intersections count. Deduplicate keys in a Set, then
  count. Shared vertex in both Routes and matching shared endpoint Anchor at that point are excluded.
- Used Lane and peak occupancy derive from actual Routes whose `laneId` resolves an existing Lane.
  Reciprocal validity remains validator-owned; metric code does not count phantom membership.
- Density denominator is `drawingArea.width.toLong() * drawingArea.height.toLong()`. Occupancy uses
  rectangle union area, preferably deterministic x-sweep plus merged y intervals with Long products.
  No pixel sampling, rasterization, rounding, or Golden dimension constant.

### Current Code Intelligence

- `SpatialQualityModels.kt` currently exposes `List<SpatialQualityMeasurement>` where each item is a
  free `kind: String` and `value: Double`; positive infinity passes its invariant. Story 4.1 owns
  final typed schema and non-finite policy.
- `SpatialQualityCompiler.kt` currently takes only Occurrences, Lanes, and Routes. It emits stale
  `overlap-count`, vertex-only `body-intersection-count`, flattened segment `crossing-count`,
  `twist-count`, `lane-use-count`, forbidden `label-pressure`, and proxy `route-count`/`lane-count`.
  It has no Construct, Drawing Area, peak Lane, Density, Occupancy, route-pair deduplication, or
  endpoint exclusion.
- `ProjectionSpatialCompiler.kt` already selects exact per-Sheet Occurrences, Constructs, Lanes, and
  Routes and owns `drawingArea`. Extend this call directly. Do not add another orchestrator or a
  document-wide quality pass.
- `SpatialValidation.qualityDiagnostics` only checks nonempty measurements and duplicate string
  kinds. Replace these obsolete checks with typed metric-integrity checks while preserving complete
  diagnostic aggregation.
- Direct stale tests are `SpatialQualityCompilerTest.kt`, `M41GeometryQualityTest.kt`, and
  `AuthoredProjectionSpatialQualityTest.kt`. `SpatialTestFixtures.kt`,
  `DedicatedM41SpatialGoldenAssertions.kt`, Presentation tests, Projection compiler tests, and
  spatial-model tests also construct or inspect the old list and must migrate.

### Previous Story Intelligence

- Story 3.2 made final validation complete, deterministic, structured, and all-or-nothing before
  Presentation. Keep its independent defect accumulation and canonical diagnostic ordering.
- Story 3.2 review fixed Route obstacle checks, exact reciprocal grouping, deterministic merged
  traces, Projection-owned payload/provenance, truthful Lane channels, and complete Presentation
  preflight. Quality formulas may reuse domain-neutral geometry predicates only when behavior stays
  one authority; do not weaken those validations.
- `ProjectionSpatialCompiler` quality Source Trace already merges Sheet, grid, geometry, grouping,
  Anchor, Route, and Grid Reference traces deterministically. Preserve exact Golden trace behavior.
  Lanes have typed reciprocal Route membership rather than a separate Source Trace.
- Story 3.2 explicitly deferred final formula names and non-finite quality policy here. Its final
  gates passed `:kernel:spatial-model:test`, `:kernel:compiler:test`, repository `test` (148 tasks),
  source-set hygiene, encoding audit, and `git diff --check`.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). Stories
  1.1-3.2 are cumulative intentional uncommitted changes. Preserve them and never restore affected
  files from `HEAD`.
- Recent commits `8fd6e34` and `e93623d` contain M41 planning/design. No library, dependency,
  framework, schema, external API, or version research is required. Stack remains existing Kotlin
  and Gradle project configuration.

### Project Structure Notes

- Primary production updates:
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt`,
  `SpatialValidation.kt`,
  `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`, and
  `ProjectionSpatialCompiler.kt`.
- Primary tests:
  `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialGridModelsTest.kt`,
  `SpatialValidationTest.kt`,
  `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`,
  `AuthoredProjectionSpatialQualityTest.kt`, `M41GeometryQualityTest.kt`,
  `ProjectionSpatialCompilerTest.kt`, `DedicatedM41SpatialGoldenAssertions.kt`, and
  `SpatialTestFixtures.kt`.
- Keep role-based Kotlin organization. One cohesive calculator may remain one file. Split union-area
  or intersection helpers into `*Support.kt` only when they form a distinct reusable role or the
  calculator becomes a mixed 200-300+ line dump. Never create milestone-named production types.

### Testing Requirements

- Strict red-green-refactor per Task. Record actual failing test names/assertions before production
  edits. Run focused tests after each minimal behavior, then module/repository gates sequentially.
- Expected metric values must be literal, independently calculated values. Never compute expected
  values through production helpers, assert only presence/nonnegativity, compare output to itself,
  or hide missing formulas behind `contains` checks.
- Include input-permutation tests for Occurrences, Constructs, Lanes, Routes, Route point order only
  where semantic reversal remains valid, and multi-Sheet order. Result fields and snapshots must
  remain equal without sorting actual output in tests.
- Story 4.2 baseline generation is out of scope. Keep dedicated Golden compiler assertions green,
  but do not author baseline artifact, parser, digest, command, or timestamp here.
- Gradle commands run strictly sequentially on Windows. After source/docs run encoding audit; after
  production changes run source-set hygiene audit.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-4.1-Measure-Quality-Milestone-Facts`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-12-Compute-Geometry-Metrics-From-Spatial-Facts`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#Success-Metrics-And-Closure-Gates`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-24-ADOPTED---Quality-Is-Exact-Per-Sheet-And-Derived-From-Validated-Geometry`]
- [Source: `_bmad-output/implementation-artifacts/m41/deferred-work.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/3-2-enforce-geometry-validation.md`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, PRD/addenda, architecture,
  recovery design/audit, deferred work, completed Story 3.2, current CodeGraph source/call paths,
  full relevant UPDATE files and tests, git history/worktree state, and independent PRD/architecture
  analyses.
- PRD ambiguity resolved explicitly: Route/body metric counts defective Route segments, so one
  segment contributes at most once even if it intersects multiple non-endpoint bodies.
- PRD has no explicit finite-number wording. Story clarifies fail-closed non-finite handling from
  FR-10 metric integrity and AD-26 without presenting it as quoted PRD text.
- RED: `ProjectionSpatialCompilerTest.final compiler gate rejects finite quality metrics that do not
  match final Spatial facts` first failed at the expected `assertIs<Failure>` because the forged
  finite metric still passed final validation. GREEN followed after compiler-owned recomputation was
  added at the final all-or-nothing boundary.
- Sequential verification passed: focused spatial model/validation tests; focused quality,
  Projection, authored, Presentation, and M41 Golden compiler tests; full
  `:kernel:spatial-model:test`; full `:kernel:compiler:test`; repository `test` with 148 actionable
  tasks; source-set hygiene audit; encoding audit; and `git diff --check`.
- Review RED/GREEN: finite forged metrics first reached Presentation, extreme rectangle union first
  threw `ArithmeticException`, and `SpatialDocument(emptyList())` first constructed successfully.
  Shared exact-quality validation, structured overflow handling, and a non-empty Sheet-root invariant
  made all three focused tests green.
- Adversarial review ran Blind Hunter and Edge Case Hunter layers. Acceptance Auditor timed out after
  the available review thread was reused; two additional candidates were dismissed because Lane
  provenance is already carried by Route facts and AC 1 permits any finite nonnegative descriptive
  ratio while exact recomputation owns formula truth.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced open string-key measurements with one immutable, Sheet-owned typed quality snapshot and
  migrated production, fixtures, Presentation preflight, and Golden assertions without compatibility
  adapters.
- Implemented exact overlap, Construct containment, Route/body intersection, Route crossing, twist,
  Lane-use, Density, and rectangle-union Occupancy formulas with deterministic per-Sheet behavior and
  overflow-safe geometry.
- Added finite-ratio validation plus final compiler recomputation, so representable invalid ratios or
  forged finite metrics fail closed with structured Source Trace before Presentation.
- Verified literal M41 Golden values: overlap 0, containment 0, body intersection 0, crossing 3,
  twist 0, used Lanes 7, peak Routes/Lane 2, Density `8 / 716800`, and Occupancy `25600 / 716800`.
- Resolved all three adversarial findings: Presentation now rejects forged finite metrics, overflow
  during malformed final geometry produces a structured quality diagnostic, and empty Spatial roots
  cannot construct. Updated Presentation fixtures to publish exact metrics from their own facts.

### File List

- `_bmad-output/implementation-artifacts/m41/4-1-measure-quality-milestone-facts.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityGeometry.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialQualityModelsTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM40ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41SpatialGoldenAssertions.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`

### Change Log

- 2026-08-04: Recreated Story 4.1 through BMad create-story with exact typed metric schema,
  formulas, deterministic geometry rules, finite-number policy, test plan, and milestone boundaries;
  moved Epic 4 to in-progress and Story 4.1 to ready-for-dev.
- 2026-08-04: Implemented and verified exact typed per-Sheet quality metrics, finite-value and final
  recomputation validation, migrated consumers, deterministic geometry tests, and M41 Golden values;
  moved Story 4.1 to review.
- 2026-08-04: Resolved three adversarial review findings with test-first Presentation recomputation,
  overflow diagnostics, and non-empty Spatial roots; reran module, repository, hygiene, encoding, and
  diff gates; marked Story 4.1 done.
