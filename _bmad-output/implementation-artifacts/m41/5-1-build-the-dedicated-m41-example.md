---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 5.1: Build The Dedicated M41 Example

Status: done

<!-- Note: Created through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want the M41 rolling-shutter source compiled through the real pipeline,
so that unit facts and product proof share one authority.

## Acceptance Criteria

1. Given the governed repository at `examples/m41/rolling-shutter`, when `AthenaCompiler` compiles
   `src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena`, the result retains
   the validated `SpatialDocument` produced inside the actual compiler pipeline by
   `ProjectionSpatialCompiler`. Dedicated M41 tests and baseline tooling consume that retained
   instance; they do not invoke a second Projection-to-Spatial transformation, build a synthetic
   Projection, or use a test-only compiler authority. (AD-27, AD-29, GATE-1, GATE-2)
2. The retained Golden Spatial document contains exactly one Sheet `schematic/sheet/S1`, extent
   `(0,0,1200,800)`, Drawing Area `(40,60,1120,640)`, 8 Occurrences, 3 Regions, 7 Constructs,
   10 Alignments, 16 referenced-port Anchors, 9 visible Projection Connections and exactly one
   Route per Connection, 7 used Lanes, 15 Grid References, and one quality snapshot. Route coverage
   is compared to actual Projection Connection identities rather than satisfied by a copied Route
   count alone. (FR-9.4, SM-1, SM-3)
3. Every referenced Projection occurrence-port resolves to exactly one same-Sheet Anchor. Every
   Route resolves one Connection, source/target occurrence-port, source/target Anchor, owning Sheet,
   used Lane, ordered positive orthogonal points, and Source Trace. Route first/last points equal
   Anchor points exactly; every Lane references existing Routes and no unreferenced Lane publishes.
   (FR-5, FR-6, FR-7, FR-8, AD-22)
4. Occurrence, Region, Construct, Alignment, Anchor, Route, Lane, Grid Reference, Sheet, and quality
   facts retain stable typed identity, owning-Sheet membership, canonical order, and required Source
   Trace. Grid Reference coverage is exactly one per Occurrence and Construct and uses row letters
   `A/B/C...`, column numbers `1/2/3...`, and human-readable cells such as `A1` and `B3`; no raw
   row/column map becomes authority. (FR-4, FR-9, AD-20, AD-25, AD-30)
5. The one quality snapshot equals compiler facts from the same validated Spatial document: overlap
   `0`, containment failure `0`, Route/body intersection `0`, Route crossing `3`, twist `0`, used
   Lanes `7`, peak Routes per Lane `2`, Density `8 / 716800`, and Occupancy `25600 / 716800`. No
   label metric, M40 reporter, Presentation-derived metric, or copied baseline value participates in
   compilation. (FR-12, AD-24)
6. The Presentation document used by the product pipeline is derived from the retained Spatial
   result. Occurrence geometry and Route points/endpoint identities remain exact; Presentation and
   Theia do not repair, recompute, or replace Spatial geometry. Story 5.2 owns runtime payload,
   pixel-bucket, Electron, viewport, and screenshot proof. (FR-11.1, AD-29)
7. Repeated compilation and permutations of unordered Projection collections produce equal retained
   Spatial facts. Missing/invalid Projection, Spatial, or Presentation transformation data publishes
   deterministic diagnostics and no partial retained Spatial document. (NFR-1, FR-10, AD-26)
8. Active production source contains only `ProjectionSpatialCompiler` as Projection-to-Spatial
   orchestration authority. `AuthoredProjectionSpatialBridge`, `ProjectionToSpatialTransformation`,
   row-only `SpatialPlacementCompiler`, `SpatialQualityMetricsReporter`, M40 example dependencies,
   compatibility shims, and proof/demo/sample/milestone types are absent from the active `src/main`
   path. Focused tests, `:kernel:compiler:test`, repository `test`, source-set hygiene audit,
   encoding audit, and `git diff --check` pass sequentially before review. (NFR-3, GATE-3, GATE-6)

## Tasks / Subtasks

- [x] Task 1: Prove the real compiler must retain its Spatial result with RED tests (AC: 1, 6, 7)
  - [x] Add a focused compiler contract test that compiles the actual M41 `.athena` source and fails
        because `CompilerCompilationSuccess` does not yet expose the pipeline-produced validated
        Spatial document.
  - [x] Test that retained Spatial exists only when Projection-to-Spatial and Spatial-to-Presentation
        complete successfully; diagnostics must not accompany a partial retained document.
  - [x] Preserve repository manifest/lock validation and exact raw source path/digest checks. Do not
        copy source into a synthetic test fixture.
- [x] Task 2: Retain one canonical Spatial result from the production pipeline (AC: 1, 6, 7)
  - [x] Extend `CompilerCompilationSuccess` with a small immutable typed collection for actual
        Spatial documents that successfully continue to Presentation.
  - [x] Refactor `AthenaCompilerCompilationSupport` so each authored/reality Projection is transformed
        through `ProjectionSpatialCompiler` once, the same validated `SpatialDocument` continues to
        Presentation, and the successful result is retained. Preserve complete diagnostics and no
        partial output on either transformation failure.
  - [x] Canonicalize/deduplicate retained documents deterministically without adding a second
        orchestration entry, compatibility field, or alternate Presentation path.
- [x] Task 3: Make all dedicated fixture evidence consume retained product facts (AC: 1-7)
  - [x] Update `compileDedicatedM41Example` to obtain Spatial facts from
        `CompilerCompilationSuccess`; delete its direct `ProjectionSpatialCompiler().transform(...)`
        call. Keep exact captured UTF-8 bytes as the compiler input and baseline digest input.
  - [x] Refactor `DedicatedM41ExampleTest` into readable cohesive assertions. Remove the awkward
        nested `run` indentation and avoid duplicate giant snapshots when the independent Golden
        oracle already proves the same literal facts.
  - [x] Assert exact Sheet/fact counts, referenced-port Anchor coverage, Connection-to-Route
        bijection, Route endpoints/points/trace, reciprocal used-Lane membership, Grid Reference
        coverage/cells, quality facts, and retained Spatial-to-Presentation coordinate preservation.
  - [x] Preserve `DedicatedM41SpatialGoldenAssertions` as an independent literal oracle. Product
        code and fixture compilation must never read its constants.
- [x] Task 4: Prove deterministic fixture authority and clean the active path (AC: 4, 7, 8)
  - [x] Keep unordered Projection/routing/grid permutation tests equal to the retained Golden
        Spatial document. A one-Sheet reversal is not multi-Sheet evidence; do not claim otherwise.
  - [x] Use CodeGraph callers/search plus behavioral tests to confirm no stale bridge/compiler/
        reporter or M40 example remains reachable from production compilation.
  - [x] Run source-set hygiene and confirm no `*Proof`, `*Demo`, `*Sample`, smoke-only,
        milestone-named, `V0`, or `V1` production type. Delete stale code instead of adapting it.
- [x] Task 5: Run all gates and complete BMad records (AC: 8)
  - [x] Run focused retained-pipeline and dedicated Golden tests, then `:kernel:compiler:test`, then
        repository `test`; never overlap Gradle invocations on Windows.
  - [x] Run `tools/source-set-hygiene-audit.ps1`, `tools/encoding-audit.ps1`, and
        `git diff --check`. If the example source changes, regenerate `athena.lock` and the Story 4.2
        baseline with its recorded timestamp, then verify exact artifact bytes.
  - [x] Complete Tasks, Debug Log, Completion Notes, File List, Change Log, review findings, and
        sprint status from actual command output before moving to review.

### Review Findings

- [x] [Review][Patch] Remove legacy null-Sheet Presentation when a view has Spatial-derived
      Presentation output. [`AthenaCompilerCompilationSupport.kt`:315]
- [x] [Review][Patch] Publish no retained Spatial collection when any authored/reality Spatial or
      Presentation transformation reports diagnostics. [`AthenaCompilerCompilationSupport.kt`:303]
- [x] [Review][Patch] Do not retain Spatial output whose canonical Presentation resolution fails.
      [`AthenaCompilerCompilationSupport.kt`:319]
- [x] [Review][Patch] Resolve retained Spatial documents by Sheet identity with deterministic
      conflict diagnostics and collision-safe order. [`AthenaCompilerCompilationSupport.kt`:311]
- [x] [Review][Patch] Expose retained Spatial documents through a defensively immutable typed
      collection. [`CompilerModels.kt`:162]
- [x] [Review][Patch] Make Presentation conflict diagnostics truthful and count distinct unequal
      candidates. [`AthenaCompilerCompilationSupport.kt`:1010]
- [x] [Review][Patch] Deduplicate public compiler diagnostic messages. [`CompilerModels.kt`:179]
- [x] [Review][Patch] Prove repeated full compilation, exact Presentation identity, multi-Sheet
      retained output, and no partial multi-view output. [`AthenaCompilerSpatialPipelineTest.kt`:12]
- [x] [Review][Patch] Reject duplicate Presentation occurrence/connector identities before map
      comparison. [`DedicatedM41ExampleTest.kt`:136]

## Dev Notes

### Current Product Gap

- `AthenaCompilerCompilationSupport.buildCompilationSuccess` already sends authored and selected
  reality Projections through `ProjectionSpatialCompiler`, then through Presentation. It retains
  Projection and Presentation results but discards the intervening validated `SpatialDocument`.
- `CompilerCompilationSuccess` currently has `authoredProjectionViews` and `presentations`, but no
  Spatial result. Therefore `DedicatedM41ExampleSupport.compileDedicatedM41Example` recompiles the
  retained Projection with a second direct `ProjectionSpatialCompiler` invocation. That second run
  is deterministic today, but it does not prove unit evidence and product Presentation consumed the
  same pipeline result.
- Fix the product contract at the compiler boundary. Do not preserve the second test path as a
  fallback and do not add a test-only Spatial cache.

### Architecture Guardrails

- Authority chain remains Athena source -> Engineering -> Projection -> Spatial -> Presentation ->
  Theia. Projection selects/groups; Spatial owns all geometry; Presentation preserves; Theia paints.
- `ProjectionSpatialCompiler` remains the sole Projection-to-Spatial entry. Stage order remains
  layout -> grouping geometry -> Anchors -> Routes/Lanes -> grid/quality -> assembly -> complete
  validation. No partial document crosses a failed stage.
- Retained Spatial output is evidence and downstream product input, not a second authoring source.
  It must remain immutable, typed, deterministic, implementation-neutral, and geometry-derived.
- No authored coordinates, solver fields, electrical routing roles, paint mechanics, protocol
  fields, label geometry, or renderer calculations may enter Athena source or public Spatial facts.
- M41 proves coherent exact facts and basic routing only. Do not claim professional drawing,
  readability, or routing parity.

### Golden Fixture Contract

- Active source:
  `examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena`.
  Current SHA-256 is
  `3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e`.
- Governed root has `athena.yaml` plus compiler-derived `athena.lock`; repository contract and lock
  validation already pass in `DedicatedM41ExampleTest`.
- Current literal Golden oracle proves one Sheet; 8 Occurrences; 3 Regions; 7 Constructs;
  10 Alignments; 16 Anchors; 9 Routes; 7 used Lanes; 15 Grid References; and exact quality values.
  Route coverage must still be derived against Projection Connection identities.
- Cell language follows engineering convention: vertical rows use letters, horizontal columns use
  numbers, and references are `A1`, `B3`, and so on. Compiler-owned `rowIndex`/`columnIndex` remain
  internal typed components; normal evidence names the human cell.
- Source or fixture edits are high blast radius: repository lock digest, trace identities, Golden
  oracle, baseline source digest, and baseline artifact may all change. Prefer leaving valid source
  untouched and fixing product retention/wiring.

### Files To Read And Preserve

- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - Current: public successful compilation contract; no Spatial result.
  - Change: add retained validated Spatial documents with a deterministic empty default.
  - Preserve: all existing semantic/layout/geometry/projection/presentation/rendering fields.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
  - Current: production orchestration invokes Spatial then Presentation but returns only Presentation
    documents/diagnostics from its private outcome.
  - Change: retain successful Spatial alongside corresponding Presentation; merge results canonically.
  - Preserve: authored and selected-reality paths, canonical Presentation resolution, diagnostics,
    derived legacy Presentation behavior outside M41, incremental compilation, and rendering.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`
  - Current: compiles exact captured UTF-8 source through `AthenaCompiler`, then runs Spatial again.
  - Change: consume retained compiler Spatial; keep source metadata and all Story 4.2 callers stable.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
  - Current: one 300+ line mixed test proves lock, Golden facts, traces, permutations, and a separately
    generated Presentation. It also has unnecessary nested indentation.
  - Change: assert retained product result and split helpers/assertion roles when this improves scan
    cost. Preserve every useful behavioral assertion.
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41SpatialGoldenAssertions.kt`
  remains the independent literal test oracle; do not weaken or couple it to product output.

### Testing Requirements

- Strict red-green-refactor. First failure must show absent retained pipeline Spatial, not a typo or
  synthetic fixture error.
- Use actual `.athena` bytes and `AthenaCompiler`; no mock compiler, copied Projection, or direct
  helper transformation may satisfy primary acceptance.
- Assert structured identities and cardinalities. Nonblank lists, `>= 0`, file existence, snapshots
  generated from actual values, or artifact self-comparisons are insufficient.
- Test failure behavior: a Spatial or Presentation failure yields diagnostics and no retained partial
  Spatial result. Never weaken compilation to make the Golden Fixture pass.
- Keep Story 4.2 baseline tests green because they share `DedicatedM41ExampleSupport`.
- Gradle verification is strictly sequential. After all text/source edits run encoding and
  source-set hygiene audits.

### Previous Story Intelligence

- Story 4.2 created a canonical compiler-backed baseline and hardened its evidence adapter after
  adversarial review. `compileDedicatedM41Example` now compiles the exact captured UTF-8 bytes; keep
  that invariant while replacing its second Spatial transformation.
- Story 4.2 pinned committed timestamp verification, bounded strict parsing, used Route-derived Lane
  counts, enforced Drawing Area containment, and proved distinct multi-Sheet denominators. Do not
  bypass those checks or feed baseline values back into product compilation.
- Story 4.2 full compiler suite passed with 514 tests; repository `test` passed with 148 actionable
  tasks. Treat these as regression baselines, not current completion evidence.
- Deferred Presentation Occurrence identity/count product E2E belongs to Story 5.2. Story 5.1 may
  assert product contract preservation but must not claim Electron/pixel/screenshot completion.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). All M41
  implementation changes are cumulative and intentionally uncommitted; preserve them.
- Recent commits `8fd6e34` and `e93623d` contain M41 planning and recovery design. No later
  implementation commit exists to copy or revert.
- No external library, framework, API, or version change is needed. JDK/Kotlin/JUnit and existing
  compiler contracts are sufficient; web research adds no implementation requirement.

### Project Structure Notes

- Expected production updates are limited to `CompilerModels.kt` and
  `AthenaCompilerCompilationSupport.kt`. No new production type/file is expected unless a cohesive
  private outcome cannot remain readable in the existing orchestration file.
- Test updates belong under `kernel/compiler/src/test/.../compiler`. A new focused test/support file
  is acceptable when it separates retained-pipeline contract assertions from the large Golden
  oracle; do not create one file per tiny helper.
- `examples/m41/rolling-shutter` is the only M41 example. Do not read or copy M40 example behavior.
- No production proof/demo/sample/milestone type. No compatibility adapter. No stale authority.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-5.1-Build-The-Dedicated-M41-Example`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-9-Publish-Explicit-Per-Sheet-Facts`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#Delivery-And-Product-Proof-Gates`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md#Confirmed-Decisions`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-27-ADOPTED---ProjectionSpatialCompiler-Is-The-Single-Spatial-Orchestration-Entry`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-29-ADOPTED---Product-Proof-Reads-Runtime-Facts-And-Drawing-Area-Pixels`]
- [Source: `_bmad-output/implementation-artifacts/m41/4-2-measure-density-and-occupancy-against-the-baseline.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`]
- [Source: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`]
- [Source: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Implementation Plan

- Prove actual M41 compilation lacks retained Spatial output before changing production contracts.
- Retain each validated Spatial result only when its corresponding Presentation transformation
  succeeds; canonicalize outputs in `CompilerCompilationSuccess`.
- Remove the dedicated helper's second primary Spatial transformation, then prove exact Golden and
  Presentation coverage from the retained product result.
- Run focused, compiler, repository, hygiene, encoding, and diff gates sequentially before review.

### Debug Log References

- Story context created from full milestone-local sprint status, epics, PRD/addendum/reviews,
  architecture spine/reviews, recovery audit/change/deferred work, completed Story 4.2, active
  fixture/source/lock, current compiler/test symbols through CodeGraph, and git history/worktree.
- Create-story decision: retain the production pipeline's validated Spatial result and remove the
  dedicated helper's second transformation. This is required for one authority; fixture source is
  already valid and should not be rewritten speculatively.
- RED: `AthenaCompilerSpatialPipelineTest` failed to compile because
  `CompilerCompilationSuccess.spatialDocuments` did not exist.
- GREEN: focused retained-pipeline, dedicated Golden, baseline, and missing-grid compiler tests
  passed after retaining only Spatial documents that completed Presentation transformation.
- Regression: `:kernel:compiler:test` passed 516 tests with zero failures, errors, or skips.
- Repository: `test --no-daemon --console=plain` passed with 148 actionable tasks.
- Audits: source-set hygiene, encoding, and `git diff --check` passed. CodeGraph plus repository
  search found stale authority names only inside a negative guard test, never active production.
- Fixture bytes remained unchanged: source SHA-256
  `3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e`; baseline SHA-256
  `c53d2f82dd244dbf89500f61c78d753a0624da8bc4761a4905508f0d1e4b7ab5`.
- Review RED: five focused assertions failed for legacy Presentation publication after authored
  failure, successful-view Presentation publication after a peer Spatial failure, unstructured
  Presentation conflict details, per-Sheet false Spatial conflicts, and overlapping identity sets.
- Review GREEN: focused tests passed after fail-closing Spatial-owned Presentation publication,
  allowing truthful structured diagnostics without fabricated geometry traces, and resolving
  canonical Spatial authority by whole ordered Sheet identity sets.
- Final review gates: `:kernel:compiler:test` passed 520 tests with zero failures, errors, or skips;
  repository `test` passed with 148 actionable tasks; source-set hygiene, encoding audit, and
  `git diff --check` passed sequentially.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- `CompilerCompilationSuccess` now retains deterministic validated Spatial documents and structured
  transformation diagnostics from actual authored/reality pipeline runs.
- Spatial retention occurs only after matching Spatial-to-Presentation success; missing-grid and
  invalid authored-view tests prove diagnostics publish without partial Spatial output.
- Dedicated M41 fixture and baseline tooling now consume retained product Spatial facts. No second
  primary Projection-to-Spatial transformation remains in fixture support.
- Dedicated proof uses actual compiler Presentation and verifies exact geometry, endpoint identity,
  Route/Lane reciprocity, Grid cell language, traces, quality, and permutation stability.
- Removed duplicate Anchor/Route literal snapshots from the mixed test; independent Golden oracle
  remains unchanged and authoritative only in tests.
- Review patches prevent any Presentation owned by a failed Spatial batch from publishing without
  retained Spatial authority, preserve structured Presentation conflict facts without inventing a
  Source Trace, and reject same or overlapping ordered Sheet identity conflicts deterministically.

### File List

- `_bmad-output/implementation-artifacts/m41/5-1-build-the-dedicated-m41-example.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompilerCompilationSupport.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/RealityTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerSpatialPipelineTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ViewAndSheetAuthorityCompilationTest.kt`

### Change Log

- 2026-08-04: Created Story 5.1 through BMad create-story with retained product-pipeline Spatial
  authority, exact Golden Fixture coverage, source-set cleanup boundaries, and sequential gate plan;
  moved Story 5.1 to ready-for-dev.
- 2026-08-04: Retained validated Spatial output in the real compiler pipeline, removed fixture
  recompilation, switched dedicated proof to actual Presentation output, strengthened failure and
  permutation coverage, and passed all focused/full/audit gates; moved Story 5.1 to review.
- 2026-08-04: Resolved all review findings, including fail-closed Presentation publication,
  structured conflict diagnostics, and whole-document ordered Sheet authority; passed 520 compiler
  tests plus repository and audit gates; moved Story 5.1 to done.
