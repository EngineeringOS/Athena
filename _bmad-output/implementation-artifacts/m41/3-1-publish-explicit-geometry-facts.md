---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 3.1: Publish Explicit Geometry Facts

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want inspectable typed facts with stable ownership and trace,
so that downstream tools cannot confuse a Construct, Occurrence, or route endpoint.

## Acceptance Criteria

1. Given a valid canonical Projection Snapshot, `ProjectionSpatialCompiler` publishes exactly one
   immutable `SpatialDocument` whose only root collection is canonical `SpatialSheet` values. Each
   `SpatialSheet` has one stable Sheet-qualified identity, exact owning `sheetId`, extent
   `(0,0,1200,800)`, Drawing Area `(40,60,1120,640)`, owning typed grid, Occurrences, Regions,
   Constructs, alignments, Anchors, used Lanes, Routes, typed Grid References, one typed quality
   snapshot, and required Sheet Source Trace. No document-wide flat fact list, defaulted empty root,
   compatibility constructor/getter, duplicate root authority, or partial Sheet is published.
   `ProjectionSpatialCompiler` remains the sole Projection-to-Spatial orchestrator. (FR-9.1,
   AD-20, AD-21, AD-27, NFR-3, NFR-4)
2. Given inspection of any published Spatial fact, every Sheet, Occurrence, Region, Construct,
   alignment, Anchor, Route, Lane, grid definition, Grid Reference, and quality snapshot has stable
   typed identity, exact owning Sheet, immutable equality-bearing content, and the trace required by
   PRD Section 11. Sheet, geometry, grouping, Anchor, Route, grid, Grid Reference, and quality facts
   carry required immutable `SpatialSourceTrace`; Lane trace remains its exact reciprocal typed
   `routeIds` membership and gains no invented authored provenance. A quality snapshot has one typed
   Sheet-qualified identity and a canonical trace of its contributing Spatial facts; Story 3.1 only
   establishes this shape and ownership, not final formulas. Required Route trace retains Story 2.3
   six-position endpoint order, including repeated identities. (FR-7.1, FR-8.1, FR-9.1, AD-20,
   AD-30)
3. Given a Construct identity, alignment source, Grid Reference, Route endpoint, or repeated
   Projection identity on different Sheets, model inspection preserves its exact typed role and
   owning Sheet. Construct identity uses `SpatialConstructId`, never `SpatialOccurrenceId`; Grid
   Reference subject remains typed `Occurrence` or `Construct`, never a raw map or untyped subject
   string; route endpoints remain typed occurrence-port Anchors. Same Projection occurrence labels
   on different Sheets remain distinct through Spatial and Presentation. No delimiter-built display
   string, list index, flattened lookup, or `associateBy` overwrite becomes identity authority.
   (FR-9.2, AD-20, AD-25, AD-30, NFR-4)
4. Given repeated compilation and independent permutations of unordered Projection Sheets,
   Occurrences, Connections, ports, and compiler-stage fact collections, complete
   `SpatialDocument` equality is unchanged. Sheets order by Projection authored Sheet order then
   stable identity; fact collections preserve explicitly authored Region/Construct/member order
   where meaningful and otherwise use documented stable typed identity ordering. Tests compare the
   complete nested typed document, including ownership, geometry, memberships, Route points, trace,
   Grid References, and quality snapshots; sorting the actual result for self-comparison or checking
   counts alone cannot satisfy this criterion. (FR-6.5, FR-9.3, NFR-1, AD-30)
5. Given the rolling-shutter Golden Fixture, literal independent expectations prove exactly one
   Spatial Sheet, 8 exact Occurrences, 3 exact Regions, 7 exact Constructs, the complete emitted
   Region/Construct alignment set, one Anchor for every unique referenced port, 9 Routes for the 9
   visible Connections, 15 typed Grid References (8 Occurrence plus 7 Construct), exactly 7 used
   Lanes, and one Sheet-owned quality snapshot. The oracle checks complete identity sets, ownership,
   nested membership, ordering, required traces, fixed Sheet/Drawing Area bounds, and complete
   document equality under named permutations while preserving all prior literal placement,
   grouping, grid, Anchor, Route point, Lane, and Route-trace oracles. (FR-9.4, SM-1, SM-3,
   GATE-3)
6. Given a valid multi-Sheet Spatial document reaches Presentation assembly, every Spatial Sheet
   produces one separate Presentation document with that Sheet's identity and fixed composition;
   Occurrences, connectors, endpoints, Lanes, traces, coordinates, and ordered Route points never
   cross Sheet boundaries or change. Canvas bounds contain the fixed Sheet composition. Current
   authored and derived Presentation assembly publishes each resulting view/Sheet once; it does not
   append a duplicate authored Presentation or choose a first-Sheet fallback. Any Spatial or
   Presentation validation failure remains fail closed; Story 3.2 owns surfacing the full authored
   failure diagnostics and independent-document validation. (FR-9.1, FR-11.1, AD-20, AD-27,
   AD-29)
7. Given review of Story 3.1 production changes, the authority chain remains Athena source ->
   Engineering -> Projection -> Spatial -> Presentation -> Theia. No authored coordinates,
   renderer repair, second layout/routing engine, raw metric map, compatibility shim, milestone- or
   version-named production type, proof/demo/sample production class, quality formula correction,
   label surface, rendering/export evolution, readability optimization, professional routing, or
   multi-Sheet continuation is added. Complete accumulated geometry validation remains Story 3.2;
   truthful quality formulas remain Story 4.1 and baseline comparison remains Story 4.2. (FR-8.2,
   FR-10, FR-12, NFR-3, NFR-4, SM-C3, SM-C4)

## Tasks / Subtasks

- [x] Task 1: Prove the typed per-Sheet root and immutable contract with RED model tests (AC: 1-3)
  - [x] Rewrite `SpatialDocumentTest.kt` against required `SpatialDocument(sheets = ...)` and a
        complete `SpatialSheet`. Add compile-time/reflective contract proof that flat document-wide
        collections and compatibility accessors do not remain.
  - [x] Add tests for required Sheet identity, `sheetId`, extent, Drawing Area, typed grid, child
        collections, quality snapshot identity, and exact Sheet/quality Source Trace. Mutate every
        input list after construction and prove document, Sheet, routing collections, and quality
        equality cannot change.
  - [x] Add role-separation tests proving `SpatialConstructId` cannot occupy Occurrence identity,
        typed Grid Reference subjects remain distinct, and repeated Projection IDs on different
        Sheets do not collide. Record first failing compilation/assertion before production edits.
- [x] Task 2: Replace the flat root with cohesive typed Sheet and quality models (AC: 1-3, 7)
  - [x] Update `SpatialDocument.kt` to expose only a required immutable canonical
        `List<SpatialSheet>`. Add `SpatialSheet` in this cohesive root file with stable typed identity,
        owning Sheet, extent, Drawing Area, one grid, typed child collections, one quality snapshot,
        and required Source Trace. Do not retain flat getters, defaults, secondary constructors, or
        a parallel root.
  - [x] Add `SpatialQualityModels.kt` for the small related quality contract: typed Sheet-qualified
        snapshot identity, owning Sheet, immutable measurements, and required trace. Move/evolve
        `SpatialQualityMeasurement` directly; do not create a map-backed metric authority or fix
        formulas assigned to Story 4.1.
  - [x] Make collection-bearing facts defensively immutable where Story 3.1 equality depends on
        them, especially `SpatialDocument.sheets`, Sheet child lists, quality measurements,
        `SpatialLane.routeIds`, and `SpatialRoute.points`. Preserve existing typed geometry,
        grouping, grid, Anchor, Route, and Lane identities rather than duplicating them.
- [x] Task 3: Assemble canonical complete Spatial Sheets in the sole orchestrator (AC: 1-4, 7)
  - [x] Extend compiler tests first for two canonical Sheets with colliding local labels and facts.
        Assert each root contains only facts whose exact typed `sheetId` matches it, one owning grid,
        one quality snapshot, exact extent/Drawing Area, and literal Sheet Source Trace.
  - [x] Refactor `ProjectionSpatialCompiler` only after RED proof: retain the current fail-closed
        stage order, partition every successful stage result by exact Sheet identity, reject no facts
        silently, measure only same-Sheet inputs, then assemble Sheets in Projection authored order
        followed by stable identity.
  - [x] Compose Sheet trace from the exact Projection Sheet identity and origin geometry reference.
        Compose each quality snapshot trace canonically from its owning Sheet and contributing
        Spatial facts without copying Story 4.1 formulas or creating a second provenance type.
- [x] Task 4: Canonicalize equality across every unordered input without losing authored order
      (AC: 3, 4)
  - [x] Add named independent permutation fixtures for Sheets, Occurrences, ports, Connections,
        Anchors, Routes, Lanes, grids, Grid References, trace contributors, and assembly inputs.
        Compare complete nested documents directly against a literal baseline.
  - [x] Define stable sort keys with typed components, not serialized display IDs. Preserve authored
        Sheet/Region/Construct/member order where it is semantic; canonicalize only unordered
        collections. Prove Sheet order tie-breaking by identity.
  - [x] Preserve Stories 1.1-2.3 exact geometry, Anchor endpoint role, repeated Route trace positions,
        actual endpoint Sheet resolution, all-or-nothing routing, and no-optimization boundary.
- [x] Task 5: Preserve Sheet boundaries through Presentation and remove duplicate publication
      (AC: 3, 6, 7)
  - [x] Refactor `SpatialToPresentationTransformation` to consume one complete `SpatialSheet` per
        Presentation document, or an equally explicit typed multi-Sheet result with no flattening.
        Use the Spatial Sheet identity, extent, Drawing Area, child facts, and traces; remove the
        hardcoded `spatial-presentation/sheet/01` and M39 composition identity.
  - [x] Update `AthenaCompilerCompilationSupport` to map every canonical Spatial Sheet exactly once
        through Presentation. Prevent `authoredPresentations + presentations` duplicate publication
        with a stable view/Sheet identity rule; do not choose `first`, `firstOrNull`, or global label
        lookup as multi-Sheet authority.
  - [x] Add tests proving same-label Occurrences on two Sheets create separate paint identities and
        documents, fixed Sheet bounds fit their canvases, coordinates/Route points remain byte-for-
        byte equal, and authored Presentation appears once. Leave full diagnostic propagation and
        canvas overflow validation to Story 3.2.
- [x] Task 6: Prove literal Golden typed coverage and trace (AC: 2, 4, 5)
  - [x] Extend `DedicatedM41ExampleTest.kt` with independent literal oracles for root Sheet identity,
        extent/Drawing Area, 8 Occurrences, 3 Regions, 7 Constructs, all alignments, every referenced
        occurrence-port Anchor, 9 Connection Routes, 15 typed Grid References, 7 used Lanes, and one
        quality snapshot. Assert exact identity sets and nested ownership, not only counts.
  - [x] Assert required trace components and order per fact kind without deriving expected values
        from the compiled subject. Preserve Story 2.3 literal six-position Route traces and exact
        Route points/Lane memberships unchanged.
  - [x] Compare complete Golden `SpatialDocument` equality under every named unordered-input
        permutation. Ban expected values copied from actual output, sorted self-comparison,
        `associateBy` overwrite, reflection-only type proof, or count-only coverage.
- [x] Task 7: Verify sequentially and complete BMad records (AC: 1-7)
  - [x] Run focused Spatial document/quality/routing model, orchestrator, Presentation, compiler
        publication, and Golden suites; then `:kernel:spatial-model:test`, `:kernel:compiler:test`,
        repository `test`, source-set hygiene audit, encoding audit, and `git diff --check`, strictly
        sequentially.
  - [x] Confirm no flat Spatial compatibility surface, cross-Sheet flattening, duplicate authored
        Presentation, formula change, routing change, later-milestone surface, or production
        source-set hygiene violation remains. Every cited acceptance consequence needs an actually
        passing behavioral test.
  - [x] Complete every Task checkbox, Debug Log, Completion Notes, File List, Change Log, review
        finding, and sprint status before moving Story 3.1 to review and adversarial acceptance.

### Review Findings

- [x] [Review][Patch] Sheet-qualify repeated Projection identities across Sheets instead of
      rejecting them [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt:117]
- [x] [Review][Patch] Bind every `SpatialSheet` child fact, grid, and quality snapshot to its root
      Sheet identity [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:51]
- [x] [Review][Patch] Remove sheetless legacy Presentation publication and prevent failed authored
      Spatial gates from leaking legacy output [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt:300]
- [x] [Review][Patch] Reject unequal Presentation documents sharing one view/Sheet identity instead
      of hiding conflicts with `distinctBy` [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt:315]
- [x] [Review][Patch] Add independent complete-document permutations for assembled Routes, Lanes,
      grids, Grid References, and Presentation assembly inputs [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt:226]
- [x] [Review][Defer] Accumulate and surface authored Spatial/Presentation failure diagnostics
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt:254] - deferred, Story 3.2
- [x] [Review][Defer] Validate required facts independently per Sheet instead of through flattened
      document collections [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:178] - deferred, Story 3.2
- [x] [Review][Defer] Validate reciprocal Lane membership and reject phantom Route memberships
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:207] - deferred, Story 3.2
- [x] [Review][Defer] Reject duplicate Spatial identities and validate Grid Reference ownership and
      targets [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:216] - deferred, Story 3.2
- [x] [Review][Defer] Validate arbitrary Sheet extents against fixed composition and canvas bounds
      [kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt:162] - deferred, Story 3.2
- [x] [Review][Defer] Reject diagonal and zero-length independent-document Route segments
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt:180] - deferred, Story 3.2
- [x] [Review][Defer] Reject non-finite quality measurements while correcting final quality formulas
      [kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt:47] - deferred, Story 4.1

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  Projection owns visible subjects, grouping, semantic order, ports, and Connections. Spatial owns
  exact geometry and per-Sheet quality shape. Presentation copies. Theia paints.
- Apply AD-20, AD-21, AD-24, AD-25, AD-27, AD-29, and AD-30. The fixed contract seed is
  `SpatialDocument(val sheets: List<SpatialSheet>)`; use it directly. `ProjectionSpatialCompiler`
  remains the only public Projection-to-Spatial orchestrator.
- Pre-1.0 cleanup applies. Delete the flat document authority and migrate every caller directly. No
  default `sheets`, deprecated accessor, adapter, extension flattening API, compatibility overload,
  raw parallel model, `V0`/`V1`, or milestone-named production type.
- Sheet roots are assembly, identity, ownership, and canonicalization work. Story 3.2 owns complete
  independent-document validation and accumulated diagnostics. Local constructor invariants needed
  to make invalid typed values impossible are valid Story 3.1 work.
- Story 4.1 owns exact quality formulas and removal of stale label metrics. Story 3.1 must establish
  typed per-Sheet quality ownership and trace while preserving current metric computation behavior
  except partitioning it by Sheet. Do not write assertions that bless broken formulas.

### Typed Fact And Ordering Contract

- `SpatialSheet` owns: stable identity, `sheetId`, extent, Drawing Area, one typed grid,
  Occurrences, Regions, Constructs, alignments, Anchors, Lanes, Routes, Grid References, one quality
  snapshot, and Source Trace. `SpatialDocument` owns Sheets only.
- Sheet extent is `(0,0,1200,800)` and Drawing Area is `(40,60,1120,640)`. Reuse
  `ProjectionSpatialLayout` constants; do not duplicate independent numeric authority across
  compilers. Title-block boundary remains `y=740`.
- Existing `SpatialOccurrenceId`, `SpatialRegionId`, `SpatialConstructId`,
  `SpatialAlignmentSource`, `SpatialAnchorId`, `SpatialRouteId`, `SpatialLaneId`, and typed
  `SpatialGridReferenceSubject` are the identity vocabulary. Reuse them.
- Sheet sort key is Projection Sheet `order`, then `sheetId`. Nested collections use semantic
  authored order where Projection owns it; otherwise use stable typed component keys. Do not use
  generated display strings or Kotlin hash iteration order.
- Grid language remains human-first: vertical rows `A/B/C/...`, horizontal columns `1/2/3/...`,
  cells `A1`, `B3`, and so on. `rowLabel`, `columnNumber`, and `cellReference` stay typed fields;
  never replace them with raw maps or compiler-internal coordinate language.
- PRD Section 11 defines Lane trace as exact Route membership. Do not add `SpatialSourceTrace` to a
  Lane merely to make every row syntactically identical; its canonical typed reciprocal `routeIds`
  is the required trace.

### Current Code Intelligence

- `SpatialDocument.kt` currently exposes ten defaulted document-wide lists and runs legacy flat
  validation. It has no Sheet identity, extent, Drawing Area, per-Sheet grid/quality, or Sheet trace.
  Replace the root now; migrate validation traversal minimally so existing fail-closed checks still
  work until Story 3.2 replaces them comprehensively.
- `SpatialQualityMeasurement` currently lives at the bottom of `SpatialDocument.kt` with only
  `kind` and `value`. Move it to `SpatialQualityModels.kt` beside the new typed snapshot. Keep small
  related values together per Kotlin organization rules.
- Existing geometry/grouping/grid/routing models are already Sheet-qualified and mostly typed.
  `SpatialSourceTrace`, grouping member lists, and placement reasons already take immutable copies;
  Route points and Lane membership currently need equivalent protection.
- `ProjectionSpatialCompiler.kt` already produces all stage facts but assembles one flat document
  and calls quality once for all Sheets. Keep its validation/layout/geometry/grid/Anchor/Route
  failure boundaries unchanged, then assemble complete Sheet roots.
- `SpatialToPresentationTransformation.kt` currently consumes flat lists, derives a global canvas,
  omits Sheet identity from Occurrence paint IDs, and hardcodes
  `spatial-presentation/sheet/01` plus M39 title metadata. Migrate it to explicit one-Sheet input or
  output cardinality so no cross-Sheet flattening remains.
- `AthenaCompilerCompilationSupport.kt` currently transforms one flat Spatial document at
  `deriveAuthoredPresentation`/`deriveRealityPresentation` and returns
  `authoredPresentations + presentations`, which can duplicate authored output. Migrate the direct
  callers and deduplicate by stable view/Sheet identity without hiding a conflicting document.

### Previous Story Intelligence

- Story 2.3 established required immutable equality-bearing Route Source Trace, complete endpoint
  diagnostics, exact Projection endpoint-role validation, actual endpoint-Sheet resolution,
  fail-closed sibling behavior, 9 Golden Routes, and 7 used Lanes. Preserve all of it.
- Its review fixed swapped endpoint acceptance, lost repeated required trace identity, foreign-Sheet
  Anchor selection, and missing orchestrator sibling proof. Multi-Sheet assembly must not reintroduce
  any of these through flattening or ID-only lookup.
- Complete duplicate Lane and Route/Lane independent-document checks remain recorded in
  `_bmad-output/implementation-artifacts/m41/deferred-work.md` for Story 3.2.
- Story 1.2 deferred Sheet-boundary Presentation assembly, repeated labels per Sheet, fixed canvas
  containment, and duplicate authored Presentation publication to this story. These are required,
  not optional cleanup.
- Fresh Story 2.3 verification passed 17 spatial-model tests, 469 compiler tests, 81 routing-model
  tests, repository `test` with 148 tasks, source-set hygiene, encoding audit, and
  `git diff --check`.

### Git Intelligence

- Baseline is `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). Completed M41 Stories
  1.1-2.3 are intentionally uncommitted in the active worktree. Preserve all current changes and do
  not reconstruct any file from `HEAD`.
- Recent relevant commits `8fd6e34` and `e93623d` contain M41 planning/design only. No dependency,
  framework, or external API change is required. Repository versions remain Kotlin 2.4.0 and Gradle
  9.6.1.

### Project Structure Notes

- Primary production updates:
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`, new
  `SpatialQualityModels.kt`, `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`,
  `SpatialQualityCompiler.kt`, `SpatialToPresentationTransformation.kt`, and
  `AthenaCompilerCompilationSupport.kt`.
- Expected focused tests: spatial-model `SpatialDocumentTest.kt` and constructor migrations;
  compiler `ProjectionSpatialCompilerTest.kt`, `SpatialQualityCompilerTest.kt`,
  `SpatialToPresentationTransformationTest.kt`, `DedicatedM41ExampleTest.kt`,
  `AuthoredProjectionSpatialQualityTest.kt`, `AthenaCompilerTest.kt`, and
  `ViewAndSheetAuthorityCompilationTest.kt`, plus `SpatialTestFixtures.kt`.
- Compiler-internal stage outputs may remain flat until final Sheet assembly. Do not invent
  duplicate per-stage Sheet wrappers unless tests prove a real ownership hole.
- No UX artifact applies. No web research is needed because Story 3.1 adds no dependency, external
  API, or version decision.

### Testing Requirements

- TDD per task: focused RED, smallest GREEN, refactor, rerun focused. Record actual first failure.
- Multi-Sheet fixtures must deliberately reuse local Occurrence labels and include separate grids,
  traces, and paint identities. Assert exact Sheet-local membership, never `groupBy`/`associateBy`
  behavior alone.
- Trace tests use literal expected Projection and geometry identity lists per fact kind. Nonempty,
  contains-only, count-only, or output-derived expectations do not prove exact trace.
- Permutation tests independently reverse each unordered input source while preserving authored
  order fields. Compare complete `SpatialDocument`; do not normalize both expected and actual with
  the implementation helper under test.
- Golden tests retain all prior literal coordinate, grouping, grid, Anchor, Route, Lane, and trace
  assertions. Add exact nested typed root coverage instead of replacing deeper proof with counts.
- Gradle commands run strictly sequentially on Windows. After source/docs run encoding audit; after
  production changes run source-set hygiene audit.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-3.1-Publish-Explicit-Geometry-Facts`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-9-Publish-Typed-Per-Sheet-Spatial-Facts`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#Subject-By-Fact-Contract`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-20-ADOPTED---Spatial-Owns-One-Typed-Per-Sheet-Geometry-Contract`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-30-ADOPTED---Normalization-Owns-Stable-Identity-Trace-And-Ordering`]
- [Source: `_bmad-output/implementation-artifacts/m41/deferred-work.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/2-3-trace-routes-and-hold-the-no-optimization-boundary.md`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, PRD, addendum, rubric,
  validation, architecture and reviews, recovery artifacts, deferred work, completed Story 2.3,
  current CodeGraph/callers, git state, and three independent create-story analyses.
- RED: nested-root model tests first failed compilation on missing `SpatialSheet`,
  `SpatialQualitySnapshotId`, and `SpatialQualitySnapshot` contracts.
- RED: routing immutability assertions proved mutable `SpatialLane.routeIds` and
  `SpatialRoute.points`; defensive copies made both tests green.
- RED: two-Sheet compiler fixture exposed Projection semantic identity reuse; fixture was corrected
  to distinct typed identities sharing only the display label.
- RED: multi-Sheet authored publication fixture exposed View-level grid grammar; one authored View
  grid now feeds both Sheet-owned Spatial grids without duplicate declarations.
- RED: Presentation assembly test first failed on missing all-or-nothing assembly, then proved one
  invalid Sheet cannot leave a published sibling document.
- Sequential verification: Spatial model 19 tests, compiler 472 tests, repository `test` 148 tasks,
  source-set hygiene, encoding audit, and `git diff --check` all passed.
- Review RED/GREEN: foreign-Sheet child ownership, sheetless authored Presentation leakage,
  conflicting Presentation identities, repeated Projection identity placement/publication,
  Connection-owned Anchor selection, canonical assembly, and quality snapshot metadata each failed
  focused tests before their minimal production fixes and passed afterward.
- Post-review verification: `:kernel:compiler:test` passed after all regression fixes, repository
  `test` passed with 148 tasks, and source-set hygiene, encoding, and `git diff --check` passed.

### Implementation Plan

- Prove required nested typed root, immutable equality, per-Sheet ownership, canonical permutations,
  Presentation boundaries, and Golden exact coverage with focused RED tests.
- Replace flat publication directly, assemble complete canonical Sheets in the sole orchestrator,
  migrate Presentation/callers without compatibility paths, then pass every sequential gate.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Replaced flat Spatial root with immutable canonical `SpatialSheet` roots and typed per-Sheet
  quality snapshots; no compatibility constructor, getter, or parallel authority remains.
- Partitioned every compiler stage by exact Sheet ownership, preserved authored ordering where
  semantic, and proved complete equality under named independent unordered-input permutations.
- Preserved Sheet boundaries through Presentation, encoded Sheet identity in paint occurrences,
  removed duplicate authored publication, and made multi-Sheet Presentation assembly fail closed.
- Added independent rolling-shutter Golden oracles for exact typed geometry, grouping, alignment,
  Grid Reference, Anchor, Route, Lane, trace, quality ownership, and fixed composition facts.
- Preserved prior placement, Anchor, Route endpoint, Route trace, Lane membership, and no-
  optimization behavior; Story 3.2 validation and Story 4.1 formulas remain deferred as assigned.
- Resolved review finding [HIGH]: repeated Projection identities are Sheet-qualified through
  Spatial and Presentation, with Connection ownership selecting the correct Anchor geometry.
- Resolved review finding [HIGH]: every `SpatialSheet` child, grid, and quality snapshot must match
  its root Sheet identity.
- Resolved review finding [HIGH]: authored Projection publication no longer emits sheetless legacy
  Presentation output or leaks it past a failed Spatial gate.
- Resolved review finding [HIGH]: equal Presentation duplicates collapse canonically while unequal
  documents sharing one view/Sheet identity fail closed.
- Resolved review finding [MEDIUM]: complete independent typed Golden oracles and named assembly
  permutations cover Routes, Lanes, grids, Grid References, and Presentation inputs.

### File List

- `_bmad-output/implementation-artifacts/m41/3-1-publish-explicit-geometry-facts.md`
- `_bmad-output/implementation-artifacts/m41/deferred-work.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialQualityModels.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM40ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41SpatialGoldenAssertions.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt`

### Change Log

- 2026-08-04: Created and validated through BMad create-story from milestone-local M41 artifacts;
  marked ready-for-dev.
- 2026-08-04: Implemented immutable typed per-Sheet Spatial publication, canonical multi-Sheet
  assembly, fail-closed Presentation publication, exact Golden coverage, and sequential gates;
  marked review.
- 2026-08-04: Addressed code review findings - 5 items resolved; 7 later-story items recorded in
  milestone-local deferred work; reran full sequential gates; marked done.
