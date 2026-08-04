---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 3.2: Enforce Geometry Validation

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want all invalid geometry reported before paint facts exist,
so that Theia cannot hide compiler defects.

## Acceptance Criteria

1. Given a complete typed `SpatialDocument`, one final validation boundary checks every Sheet
   independently and the document as a whole before Presentation receives any fact. Validation
   covers required per-Sheet facts, unique Sheet-qualified identities, exact root/child ownership,
   geometry containment, grouping membership, alignment, Anchor, Route, Lane, grid, Grid Reference,
   Source Trace, and current quality-snapshot structural integrity. Existing local value invariants
   that make blank, nonpositive, or self-contradictory typed values impossible remain constructor
   barriers; representable cross-fact failures are accumulated rather than thrown, overwritten, or
   reduced to one generic issue. (FR-10.1, AD-20, AD-26, AD-30, NFR-4)
2. Given canonical Projection input and final assembled Spatial facts, exact expected coverage is
   compared explicitly: one Spatial Sheet per Projection Sheet; one Occurrence per Sheet-qualified
   projected occurrence; one Region and Construct per projected grouping; one Anchor per referenced
   occurrence-port; one Route per visible Connection; one Grid Reference per Occurrence and
   Construct; one quality snapshot per Sheet; and only Lanes used by Routes. Missing, extra, or
   duplicate facts fail. Expected coverage comes from canonical Projection facts or an equivalent
   typed compiler-owned inventory, never from the output being validated. Repeated Projection
   identities across different Sheets remain valid because ownership is Sheet-qualified. (FR-9.4,
   FR-10.1, AD-25, AD-27, AD-30)
3. Given malformed but representable grouping, geometry, or grid relationships, validation reports
   every independent defect: duplicate identities; wrong owning Sheet; Occurrence/Region/Construct
   bounds outside the owning Drawing Area; Region/Construct members missing, duplicated, foreign,
   or not contained; alignment source/member disagreement; grid Drawing Area mismatch; Grid
   Reference wrong grid, missing/duplicate typed target, out-of-range row/column, or cell unequal to
   the target center's owning-Sheet mapping. Grid diagnostics use human-first rows `A/B/C/...`,
   columns `1/2/3/...`, and cells such as `A1` and `B3`. No global flattening, `associateBy`
   overwrite, first-grid fallback, or raw string map participates in validation. (FR-10.1,
   FR-10.3, AD-20, AD-25, NFR-2)
4. Given malformed but representable Anchor, Route, or Lane relationships, validation reports every
   independent defect: duplicate Anchor/Lane/Route identity; Anchor subject missing or foreign;
   Anchor point not on its declared Occurrence boundary; Route endpoint Anchor missing, ambiguous,
   reversed, or unequal to first/final Route point; diagonal or zero-length segment; point outside
   the Drawing Area; segment entering any non-endpoint Occurrence interior; Route missing its Lane;
   Lane naming a missing/duplicate/foreign Route; Route absent from or repeated across Lane
   membership; unused Lane; incomplete fact-specific Source Trace. Stage-level Projection-aware
   endpoint and canonical Route-trace validation remains intact. (FR-10.1, FR-10.3, AD-22, AD-23,
   AD-26)
5. Given one document containing multiple independent defects on multiple Sheets and any permutation
   of its unordered inputs, validation returns every deterministic `SpatialDiagnostic` exactly once,
   ordered by subject then problem with a stable correction tie-breaker. Every diagnostic has a
   concrete engineering subject, exact problem, actionable correction, and relevant immutable
   `SpatialSourceTrace`; no generic `RealityValidationIssue`, missing structured field, internal
   code, sentinel identity, exception-driven early exit, or formatted-message parsing satisfies
   this criterion. (FR-10.2, FR-10.3, NFR-1, NFR-2, AD-26, AD-30)
6. Given any final Spatial or Presentation validation issue, `ProjectionSpatialCompiler` and the
   Spatial-to-Presentation boundary return `RealityTransformationResult.Failure` with the complete
   structured diagnostics and no output document. Multi-Sheet validation completes before any
   Sheet paint document is assembled; one invalid Sheet blocks every sibling. Authored and derived
   compilation paths surface the same failures through compiler diagnostics and publish no affected
   Presentation. Equal duplicate Presentation candidates may collapse once, but unequal candidates
   sharing one view/Sheet identity produce a diagnostic rather than an unexplained empty list.
   (FR-10.4, AD-26, AD-27, AD-29)
7. Given a valid single- or multi-Sheet Spatial document, Presentation transformation succeeds once
   per Sheet and copies each Occurrence rectangle, connector endpoint, and ordered Route point
   exactly. Sheet extent, Drawing Area, fixed title-block composition, and canvas containment are
   validated before paint assembly; Presentation and Theia do not snap, normalize, reroute, repair,
   or choose a first-Sheet fallback. Existing complete M41 Golden Spatial equality remains
   unchanged. (FR-11.1, AD-20, AD-29)
8. Given review of Story 3.2 production changes, authority remains Athena source -> Engineering ->
   Projection -> Spatial -> Presentation -> Theia. Story 3.2 does not correct Story 4.1 quality
   formulas, finalize metric names, reject non-finite quality values assigned to Story 4.1, generate
   the Story 4.2 baseline, add label or rendering behavior, optimize routing, invent multi-Sheet
   continuation, add authored coordinates, create a second validator authority, or preserve a
   compatibility path. Current quality validation is limited to identity, ownership, nonempty and
   unique measurement kinds, and trace structure needed before final formulas exist. (FR-12,
   NFR-3, NFR-5, SM-C3, SM-C4)

## Tasks / Subtasks

- [x] Task 1: Prove the complete validation contract with focused RED tests (AC: 1-5, 8)
  - [x] Add `SpatialValidationTest.kt` with one literal valid multi-Sheet document and independent
        mutations for every representable missing, duplicate, foreign, out-of-area, membership,
        containment, alignment, Anchor, Route, Lane, grid, Grid Reference, trace, and structural
        quality defect. Keep constructor-rejected local value defects covered by model tests rather
        than weakening typed invariants.
  - [x] Add one combined multi-defect fixture across two Sheets. Assert the complete literal list of
        `SpatialDiagnostic` values, including exact subject/problem/correction/trace, and record the
        first failing assertions before production edits.
  - [x] Independently reverse Sheets and every unordered fact/defect input. Assert complete ordered
        diagnostic equality; never sort the actual result in the test, compare counts alone, parse
        messages, or derive expected diagnostics from the validator output.
- [x] Task 2: Extract one typed complete-document validator (AC: 1, 3-5, 8)
  - [x] Create `SpatialValidation.kt` in `kernel/spatial-model` for the cohesive validation result,
        canonical diagnostic aggregation, and complete independent-document checks. Keep
        `SpatialDocument.kt` focused on document/Sheet roots and `SpatialReality` declaration; remove
        its flattened generic validator and local boundary helper after callers migrate.
  - [x] Return immutable typed `SpatialDiagnostic` values sorted by subject, problem, then
        correction. Deduplicate only equal diagnostics; unequal defects sharing a subject must all
        survive. Use the malformed fact's trace plus directly relevant owning/referenced fact traces
        in stable typed order.
  - [x] Preserve local model invariants and the Story 3.1 per-Sheet root. Do not add a flat document
        view, Projection dependency in `spatial-model`, default/compatibility constructor, second
        validation result, or milestone/version-named production type.
- [x] Task 3: Validate exact Sheet, grouping, and grid relationships (AC: 1-3, 5)
  - [x] Validate duplicate Sheet, Occurrence, Region, Construct, alignment, Anchor, Lane, Route, Grid
        Reference, and quality snapshot identities with full Sheet-qualified keys. Validate required
        facts per Sheet, never through document-wide flattened nonempty checks.
  - [x] Validate positive typed geometry remains inside the exact owning extent/Drawing Area;
        Region and Construct membership resolves once on the same Sheet and every member rectangle
        is contained; alignment sources resolve to the exact Region/Construct and alignment members
        agree with that source.
  - [x] Validate the root grid belongs to the Sheet and matches its Drawing Area. Validate exactly
        one typed Grid Reference for every Occurrence and Construct, no extras, correct root `gridId`,
        in-range row/column, and exact center-derived `rowLabel`/`columnNumber`/`cellReference` using
        the existing overflow-safe grid rules.
- [x] Task 4: Validate exact Anchor, Route, Lane, and trace relationships (AC: 1, 2, 4, 5)
  - [x] Validate each Anchor resolves one same-Sheet Occurrence-port and its point lies strictly on
        the declared boundary side. Validate each Route resolves one source and target Anchor, exact
        first/final points, same-Sheet endpoint owners, and complete required trace positions without
        globally deduplicating repeated identities.
  - [x] Validate every Route segment is positive and orthogonal, every Route point is inside its
        owning Drawing Area, and no segment intersects the positive-area interior of a non-endpoint
        Occurrence. Preserve endpoint-owner boundary/exit rules and do not add M45 optimization.
  - [x] Validate one reciprocal used Lane per Route: Lane membership references existing same-Sheet
        Routes exactly once; every Route appears in exactly one Lane list matching its `laneId`; no
        phantom, duplicate, multiply-owned, or unused Lane survives.
- [x] Task 5: Prove Projection-relative exact coverage after final assembly (AC: 2, 5, 8)
  - [x] Add compiler tests that remove, duplicate, add, and cross-own each final fact kind after
        otherwise-valid assembly. Expected Sheet, Occurrence, Region, Construct, Connection/Route,
        referenced-port/Anchor, Grid Reference, and quality coverage must come from canonical
        Projection input or one explicit typed compiler-owned inventory, not the Spatial output.
  - [x] Integrate coverage diagnostics with independent `SpatialValidation` diagnostics before the
        final `ProjectionSpatialCompiler` success boundary. Preserve current stage order and stop
        downstream stages when an upstream result cannot produce a complete document.
  - [x] Preserve repeated Projection identity across different Sheets, authored Region/Construct
        member order, canonical unordered fact ordering, Story 2.3 six-position Route traces, and
        exact M41 Golden coverage.
- [x] Task 6: Propagate complete failure before Presentation paint (AC: 5-7)
  - [x] Centralize `SpatialDiagnostic` to `RealityTransformationDiagnostic` conversion in the
        compiler transformation boundary; retain every structured field. Remove the private duplicate
        mapper after both Spatial compiler and Presentation callers use the shared path.
  - [x] Replace `transformSpatialSheetsToPresentation` bare-list/`emptyList()` failure signaling
        with a typed transformation result over the complete `SpatialDocument`. Validate all Sheets
        before creating any paint facts, return all diagnostics on failure, and return canonical
        per-Sheet documents only on total success.
  - [x] Update `AthenaCompilerCompilationSupport` and compiler result diagnostics so authored and
        derived Spatial/Presentation failures remain inspectable through normal compiler output and
        `diagnosticMessages()`. Reuse `RealityTransformationDiagnostic`; do not encode structured
        fields into a second string-only contract.
  - [x] Make unequal Presentation candidates sharing one view/Sheet identity a structured fail-
        closed outcome. Preserve equal duplicate collapse and ensure conflicts do not silently erase
        unrelated diagnostics or publications.
- [x] Task 7: Preserve exact coordinates and composition on valid input (AC: 6, 7)
  - [x] Update Presentation tests with valid in-Drawing-Area fixtures. Assert literal equality of
        every Spatial rectangle, Anchor endpoint, and ordered Route point against Presentation; no
        sorting or normalization in expected values.
  - [x] Add fixed-composition preflight tests for Sheet extent, Drawing Area, title-block bounds, and
        canvas containment, plus multi-Sheet proof that one invalid Sheet yields Failure and zero
        Presentation documents while all deterministic diagnostics survive.
  - [x] Keep `DedicatedM41ExampleTest.kt` complete Spatial Golden equality green and add only the
        minimum final-gate proof needed to show valid Golden facts reach Presentation unchanged.
- [x] Task 8: Verify sequentially and complete BMad records (AC: 1-8)
  - [x] Run focused model/validator, Projection compiler, Presentation, authored compilation, and
        Golden tests; then `:kernel:spatial-model:test`, `:kernel:compiler:test`, repository `test`,
        source-set hygiene audit, encoding audit, and `git diff --check`, with Gradle commands strictly
        sequential.
  - [x] Confirm no generic/flat Spatial validation, exception-driven relational failure, dropped
        diagnostic, partial paint output, self-derived coverage oracle, formula change, renderer
        repair, later-milestone feature, or production source-set hygiene violation remains. Every
        acceptance consequence needs an actually passing behavioral test.
  - [x] Complete every Task checkbox, Debug Log, Completion Notes, File List, Change Log, review
        finding, and sprint status before moving Story 3.2 to review and adversarial acceptance.

### Review Findings

- [x] [Review][Patch] Complete multi-Sheet Presentation composition preflight and canonicalize its
      diagnostics before any paint assembly
      [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`]
- [x] [Review][Patch] Validate each independently valid Route segment against obstacles and derive
      required Route trace positions from typed endpoint identities even when Anchors do not resolve
      [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingValidation.kt`]
- [x] [Review][Patch] Reject foreign reciprocal Region membership instead of accepting an
      Occurrence additionally listed by the wrong Region
      [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGroupingValidation.kt`]
- [x] [Review][Patch] Combine all relevant traces and canonicalize duplicate diagnostic triples so
      fact permutations cannot change diagnostic trace or order
      [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`]
- [x] [Review][Patch] Compare final Spatial root grid, semantic payload, ordered Route endpoints, and
      canonical fact provenance against Projection-owned expectations
      [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageValidator.kt`]
- [x] [Review][Patch] Require each Lane to lie inside its Drawing Area and represent a channel
      actually used by every member Route
      [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingValidation.kt`]

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  `ProjectionSpatialCompiler` is the only Projection-to-Spatial orchestrator. Final validation runs
  after canonical complete per-Sheet assembly and before any Presentation document or paint plan.
- Apply AD-20, AD-22, AD-25, AD-26, AD-27, AD-29, and AD-30. `SpatialValidation.kt` is the one final
  independent-document validator. Existing Projection-aware layout/group/grid/Anchor/Route stage
  validators remain because `spatial-model` cannot infer missing Projection facts.
- `spatial-model` must not depend on `projection-model` or `compiler`. Projection-relative expected
  coverage stays compiler-owned and merges its diagnostics into the same final gate. Do not claim
  exact missing-fact proof by comparing an output collection to itself.
- Preserve Story 3.1 `SpatialDocument(sheets)` and `SpatialSheet` ownership/immutability contracts.
  Local constructors continue rejecting blank identities, nonpositive rectangles, and identity
  self-contradictions. Complete validation owns representable cross-fact/cardinality relationships.
- Final validation accumulates independent defects. Do not use `single`, `getValue`, `associateBy`,
  or an early return until multiplicity diagnostics are recorded and dependent checks can safely
  continue.

### Validation Contract

- Canonical diagnostic key is subject, problem, then correction. Subject text names exact fact and
  Sheet where needed. Correction tells an engineer which source/Projection fact or compiler-owned
  relationship must change. Diagnostics retain `SpatialSourceTrace` rather than parsing a display
  identity.
- Validate per Sheet first. Same local Projection identity on two Sheets is valid; same complete
  Sheet-qualified identity twice is not. Cross-Sheet lookup never satisfies local membership,
  coverage, endpoint, Lane, or Grid Reference checks.
- Required traces are fact-specific: Sheet/grid/grouping/Grid Reference/quality use their owning
  and contributing facts; Anchor uses Sheet + occurrence + port; Route retains Sheet + Connection +
  source occurrence + source port + target occurrence + target port in exact order, including
  repeats. Lane trace remains reciprocal typed `routeIds`, not invented provenance.
- Grid mapping uses each root grid and Drawing Area. Human language is vertical rows `A/B/C/...`,
  horizontal columns `1/2/3/...`, cells `A1`, `B3`; keep typed fields and existing
  `spatialGridRowLabel`/overflow-safe center math.
- Geometry checks are integer and positive-area aware. Edge contact is not body-interior entry.
  Endpoint Anchors may touch their owner boundary, but Route segments may not re-enter endpoint-owner
  bodies after leaving. Do not measure crossings or optimize Routes here.
- Quality formulas and final metric dictionary are not available until Story 4.1. Story 3.2 checks
  only snapshot identity/Sheet ownership, measurement presence and unique kind, and required trace.
  Do not bless stale formula values in new expectations or move `label-pressure` cleanup early.

### Current Code Intelligence

- `SpatialDocument.kt` is 292 lines and mixes cohesive roots/declaration with a flattened validator
  returning generic `RealityValidationIssue`. It checks only global nonempty occurrence/Region
  facts, some Anchor/Route/Lane references, and grouping member existence. Move final validation to
  the architecture-seeded `SpatialValidation.kt`; do not leave two validators.
- `ProjectionSpatialCompiler.kt` already assembles canonical per-Sheet facts and invokes a final
  gate, but the current `SpatialReality.validate` loses subject/problem/correction/trace through the
  generic conversion. Preserve stage order and replace only the final complete-document gate plus
  explicit Projection-relative coverage.
- `SpatialRouteValidator.kt` already has strong Projection-aware checks for expected Connection
  coverage, endpoint roles, canonical trace, orthogonality, Drawing Area, obstacle entry, and Lane
  reciprocity. Keep it. Reuse/promote domain-neutral predicates where sensible; do not fork a second
  Projection-aware Route validator into `spatial-model`.
- `SpatialGridValidator.kt` validates compiler-stage inputs and owns overflow-safe center/grid
  rules, but it does not validate final published Grid Reference equality. Final validation must
  inspect root grid/reference/subject relationships without adding another grid authority.
- `SpatialToPresentationTransformation.kt` validates each Sheet separately, then the batch helper
  turns any Failure into `emptyList()` and discards diagnostics. Change the batch boundary to typed
  all-or-nothing validation before paint assembly.
- `AthenaCompilerCompilationSupport.kt` converts authored/reality Spatial failures to empty lists.
  `CompilerCompilationSuccess.authoredProjectionDiagnostics` is string-only and
  `diagnosticMessages()` currently omits it. Surface transformation failures with the existing
  structured `RealityTransformationDiagnostic` contract and include their messages in the public
  diagnostic accessor.
- `canonicalPresentations` currently returns `emptyList()` for unequal documents sharing one
  view/Sheet identity. Story 3.1 detects the conflict; Story 3.2 must make its failure actionable.

### Previous Story Intelligence

- Story 3.1 replaced the flat root with immutable typed `SpatialSheet` values, assembled every fact
  per exact Sheet, preserved canonical equality, and made Presentation one document per Sheet. Do
  not reintroduce flat getters, sheetless Presentation, or a first-Sheet fallback.
- Story 3.1 review fixed repeated Projection identity placement/publication, root child ownership,
  sheetless legacy Presentation leakage, unequal Presentation conflict suppression, and incomplete
  Golden permutations. Preserve those passing tests.
- Deferred here: full authored Spatial/Presentation diagnostic propagation; required facts per
  Sheet; reciprocal Lane membership; duplicate identities; Grid Reference ownership/targets;
  arbitrary extent/canvas checks; and diagonal/zero-length independent Routes.
- Non-finite quality measurement validation and truthful formulas remain Story 4.1. `SpatialPoint`
  now uses `Int`, so the older non-finite Route point note is obsolete and must not reintroduce a
  floating coordinate type.
- Fresh Story 3.1 review verification passed spatial-model 19 tests, compiler 472 tests, repository
  `test` with 148 tasks, source-set hygiene, encoding audit, and `git diff --check`.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). Stories
  1.1-3.1 are cumulative intentional uncommitted changes. Preserve them; do not reconstruct files
  from `HEAD` or treat their additions as unrelated dirt.
- Recent commits `8fd6e34` and `e93623d` contain M41 planning/design only. No dependency, framework,
  schema, external API, or version change is required. Stack remains Kotlin 2.4.0 and Gradle 9.6.1.
- No web research is needed: Story 3.2 introduces no technology or external contract.

### Project Structure Notes

- New production/test files:
  `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt` and
  `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTest.kt`.
- Primary production updates:
  `SpatialDocument.kt`, `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`,
  `ProjectionSpatialCompiler.kt`, `SpatialToPresentationTransformation.kt`,
  `AthenaCompilerCompilationSupport.kt`, and likely `CompilerModels.kt` for structured surfaced
  transformation diagnostics.
- Primary tests:
  `SpatialDocumentTest.kt`, `SpatialRoutingModelsTest.kt`, `ProjectionSpatialCompilerTest.kt`,
  `SpatialToPresentationTransformationTest.kt`, `ViewAndSheetAuthorityCompilationTest.kt`,
  `AthenaCompilerTest.kt`, and `DedicatedM41ExampleTest.kt`.
- Existing Presentation fixtures place some Occurrences at `(0,0)` while declaring Drawing Area
  `(40,60,1120,640)`. Once full validation is real, update those success fixtures to valid literal
  in-area coordinates and update Anchors/Routes consistently; do not weaken validation to retain an
  invalid test fixture.
- Keep Kotlin files role-based. `SpatialValidation.kt` may hold cohesive validator helpers, but split
  distinct roles if it grows past roughly 200-300 lines. Do not create one file per tiny check or a
  new mixed-responsibility dump.

### Testing Requirements

- Strict red-green-refactor per task. Record actual failing test names/assertions before production
  edits. Run focused tests after each minimal change and full suites only after focused green.
- Use literal valid Sheet fixtures with exact geometry/traces. Each defect test changes one
  relationship. Combined accumulator test changes several independent relationships and asserts all
  expected diagnostics; no count-only or `any { message.contains(...) }` acceptance proof.
- Prove missing coverage against Projection input, not output-derived sets. Prove duplicates with
  `groupBy` full typed identity. Prove same Projection identity on different Sheets remains valid.
- Prove all-or-nothing at three levels: final `ProjectionSpatialCompiler`, complete multi-Sheet
  Presentation boundary, and normal authored compiler output/diagnostics.
- Valid-path coordinate tests compare complete ordered values directly: Spatial rectangle to
  Presentation bounds and Spatial Route point list to connector point list. No renderer, sorting,
  snapping, or normalization participates.
- Gradle commands run strictly sequentially on Windows. After source/docs run encoding audit; after
  production changes run source-set hygiene audit.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-3.2-Enforce-Geometry-Validation`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-10-Validate-The-Complete-Spatial-Document`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-11-Preserve-Spatial-Authority-Through-Presentation-And-Theia`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#Subject-By-Fact-Contract`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-26-ADOPTED---Complete-Spatial-Validation-Is-The-Presentation-Gate`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-29-ADOPTED---Presentation-And-Product-Proof-Preserve-Runtime-Geometry`]
- [Source: `_bmad-output/implementation-artifacts/m41/deferred-work.md`]
- [Source: `_bmad-output/implementation-artifacts/m41/3-1-publish-explicit-geometry-facts.md`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteValidator.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, PRD, addendum, rubric,
  validation, architecture and reviews, completed Story 3.1, deferred work, current CodeGraph/call
  paths, full relevant UPDATE files, git history/worktree state, and three independent create-story
  analyses.
- Focused RED coverage proved final Grid Reference trace aggregation reordered an equal authored
  trace; `combinedTrace` now preserves equal traces exactly and orders unequal whole traces without
  removing repeated Route trace positions.
- Cross-fact root RED failed with `IllegalArgumentException` before validation. `SpatialSheet` now
  keeps local typed invariants while complete ownership and Drawing Area relationships accumulate at
  the one final validator boundary.
- Duplicate-identity RED matrices exposed dependent alignment ambiguity diagnostics; literal
  expectations were corrected without weakening production validation.
- Two-Sheet permutation RED initially retained dependent facts after removing their geometry roots;
  fixture now removes the complete dependent set and asserts the full four-diagnostic literal result
  in forward and independently reversed order.
- Structured Presentation conflict RED replaced message-only failure with complete
  subject/problem/correction/source-trace fields.
- Verification ran strictly sequentially: focused validator and transformation tests,
  `:kernel:spatial-model:test` (33 tests), `:kernel:compiler:test` (482 tests), repository `test`
  (148 tasks), source-set hygiene, encoding audit, and `git diff --check`.
- Adversarial review RED proved five omitted independent behaviors: an invalid Route segment
  suppressed a separate obstacle crossing; unresolved Anchors suppressed typed Route trace checks;
  foreign reciprocal Region membership passed; duplicate fact traces depended on input order; and
  reciprocal Lane identity could claim a channel unused by Route geometry.
- Projection coverage RED proved final root grid, semantic Occurrence/Connection payload, ordered
  Route endpoints, and canonical Source Trace could diverge from Projection authority while
  retaining matching fact identity. Focused GREEN then passed 19 Spatial validator tests and the
  complete `ProjectionSpatialCompilerTest` class.
- Review refactor split the 612-line coverage implementation into orchestration, Projection-owned
  inventory, authority validation, and shared support files. Final sequential verification passed
  `:kernel:spatial-model:test`, `:kernel:compiler:test`, and repository `test` with 148 tasks.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added one immutable typed complete-document validation result with deterministic exact diagnostics
  for per-Sheet identity, ownership, containment, grouping, alignment, grid, Grid Reference,
  Anchor, Route, Lane, trace, and structural quality relationships.
- Removed exception-driven root/child relationship validation while preserving blank, positive-size,
  typed-identity, and self-consistency constructor barriers.
- Added Projection-owned exact final coverage for Sheet, Occurrence, Region, Construct, Anchor,
  Route, Grid Reference, and quality-through-Sheet-root cardinality without deriving expectations
  from Spatial output.
- Made Spatial-to-Presentation validation all-or-nothing across all Sheets and preserved literal
  Occurrence rectangles, Anchor endpoints, ordered Route points, fixed composition, and complete
  structured diagnostics before paint assembly.
- Unequal Presentation candidates now fail closed with structured engineering fields and relevant
  immutable candidate provenance; equal duplicates still collapse once.
- Split validation by cohesive responsibility into core, grouping, grid, routing, trace, and support
  files; extracted exhaustive test fixtures from literal acceptance scenarios.
- Kept Story 4.1 formulas, final metric names, and non-finite quality policy untouched.
- Closed every adversarial review patch: independent Route validation, exact reciprocal Region
  membership, canonical duplicate-diagnostic traces, Projection-owned final payload/provenance,
  truthful Lane channel use, and complete pre-paint Presentation composition preflight.

### File List

- `_bmad-output/implementation-artifacts/m41/3-2-enforce-geometry-validation.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialValidationSupport.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGroupingValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialGridValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingValidation.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialTraceValidation.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialValidationTestFixtures.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageInventory.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialAuthorityValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCoverageSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialGeometryCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt`

### Change Log

- 2026-08-04: Implemented complete typed Spatial validation, Projection-relative coverage,
  all-or-nothing Presentation propagation, exact-coordinate preservation, structured Presentation
  conflicts, exhaustive deterministic tests, and role-based validator organization. Status moved to
  review after all sequential gates passed.
- 2026-08-04: Resolved adversarial review findings with test-first independent Route, reciprocal
  Region, deterministic trace, Projection authority, and Lane channel checks; split coverage roles;
  reran all sequential gates; moved Story 3.2 and Epic 3 to done.
