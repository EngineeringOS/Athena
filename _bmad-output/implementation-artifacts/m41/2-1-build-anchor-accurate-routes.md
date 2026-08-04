---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 2.1: Build Anchor-Accurate Routes

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want every referenced port represented by one stable boundary Anchor,
so that Route endpoints retain engineering identity exactly.

## Acceptance Criteria

1. Given Projection occurrence-port facts and placed Occurrence rectangles on one or more Sheets,
   Anchor compilation resolves every unique referenced occurrence-port to exactly one typed
   `SpatialAnchorPosition`. Identity contains owning Sheet, typed Occurrence, and semantic port and
   does not depend on Connection order, current side, or point. Repeated references reuse the same
   Anchor. Same semantic port identity on different Occurrences or Sheets remains distinct. Every
   Anchor carries owning `sheetId`, side, integer boundary point, and complete occurrence-port Source
   Trace. Repeated compilation and permutations of Connections, occurrence ports, Sheets, and
   geometry produce equal canonical results. (FR-5.1, FR-9.1, FR-9.3, NFR-1, NFR-4)
2. Preferred sides derive only from source/target endpoint roles and relative Connection orientation.
   For each occurrence-port, use its canonical first incident Connection by stable Projection
   Connection identity; compare exact doubled rectangle-center deltas from owner to peer; horizontal
   wins an absolute-axis tie, and a coincident-center tie uses source `RIGHT`, target `LEFT`. Ports
   assigned to one Occurrence side sort by stable semantic port identity and receive distinct
   non-corner integer boundary points at `floor((index + 1) * edgeLength / (count + 1))`. Insufficient
   edge capacity fails instead of collapsing points. No authored coordinate, electrical/solver type,
   or renderer rule enters this policy. (FR-5.2, FR-5.3, NFR-4, NFR-5)
3. A Connection endpoint missing typed Occurrence/port identity, an unknown Occurrence, missing or
   duplicate projected occurrence-port fact, missing or duplicate owning geometry, foreign Sheet
   ownership, duplicate Projection Connection identity, identical typed source/target endpoint, or
   insufficient distinct boundary capacity fails the Anchor stage. All deterministic
   issues are returned sorted by subject then problem with exact subject, problem, correction, and
   Source Trace. The result contains no partial Anchors; active `ProjectionSpatialCompiler` returns
   `RealityTransformationResult.Failure`, and no Route or Presentation document is published.
   Repeated references to one valid occurrence-port are reuse, not duplication. (FR-5.4, FR-10.2,
   FR-10.3, FR-10.4, NFR-2)
4. Given a valid same-Sheet Connection, `SpatialRouteCompiler` consumes the two resolved typed
   Anchors rather than deriving or approximating endpoints. Route first point equals the source
   Anchor point and final point equals the target Anchor point as the same integer `SpatialPoint`
   values. Presentation copies those points without rounding, snapping, endpoint inference, or
   repair. Current first/last-Occurrence fallback, generic per-Occurrence `:left`/`:right` Anchors,
   and silent `mapNotNull` Route loss are absent from the active path. Story 2.1 may retain only a
   minimal deterministic orthogonal middle path; obstacle avoidance and stable Lane policy remain
   Story 2.2, and complete Route trace validation remains Story 2.3. (FR-6.2, FR-7.2, AD-22, AD-23,
   AD-26, AD-27)
5. Given the rolling-shutter Golden Fixture, Anchor coverage equals the exact set of unique
   referenced occurrence-port identities and every visible Connection Route uses those exact
   Anchors. Tests compare literal identities, sides, points, Source Traces, and full canonical lists;
   no nonempty, minimum-count, self-comparison, or copied-constant assertion can satisfy this story.
   Projection remains coordinate-free and Spatial remains the only Anchor geometry authority.
   (FR-5.1, FR-5.2, FR-9.4, NFR-1, NFR-5)

## Tasks / Subtasks

- [x] Task 1: Prove typed occurrence-port and Anchor behavior with RED tests (AC: 1, 2, 4, 5)
  - [x] Add `SpatialAnchorCompilerTest.kt` with hand-computable rectangles, literal typed Anchor
        identities, sides, integer points, and Source Traces. Include multiple ports on every side,
        a shared port used by multiple Connections, equal-axis and coincident-center ties, and same
        semantic port identity on different Occurrences/Sheets.
  - [x] Shuffle Connections, projected occurrence ports, Sheets, and occurrence geometry; compile
        repeatedly and compare complete results. Prove point movement or side change cannot change
        Anchor identity.
  - [x] Update route tests so exact typed Anchors are input and literal first/final Route points equal
        them. Add a failing Presentation test that exposes current `Double.roundToInt()` endpoint
        conversion. Record first failing RED assertion or compilation before production edits.
- [x] Task 2: Publish coordinate-free Projection occurrence-port contracts (AC: 1, 3, 5)
  - [x] Add a small typed Projection occurrence-port fact containing typed Occurrence identity,
        semantic port identity, and origin geometry identity. Replace four nullable endpoint strings
        on `ProjectionConnection` with nullable typed source/target endpoint values so invalid input
        remains representable for Spatial diagnostics; do not retain compatibility fields.
  - [x] Add canonical `ProjectionDocument.occurrencePorts` facts. Update Projection identity rules and
        tests without adding coordinates, sides, Anchor positions, route points, or paint fields.
        `ProjectionReality` may validate typed fact shape, but it must leave null endpoint and
        missing/duplicate occurrence-port resolution representable for exact Anchor-stage
        `SpatialDiagnostic` coverage instead of replacing it with a generic Projection failure.
  - [x] Update `EngineeringToProjectionTransformation`: Engineering ports become occurrence-port
        facts owned by component Occurrences, not separate display nodes; every Connection carries
        its exact typed endpoints. Update `AuthoredProjectionViewCompiler` to publish occurrence-port
        facts for selected Occurrences and typed endpoints for selected Connections. Never infer
        owner identity by parsing a port display string when typed Engineering ownership exists.
- [x] Task 3: Replace primitive Anchor contracts with typed integer Spatial models (AC: 1, 2, 4)
  - [x] Create `SpatialRoutingModels.kt` for closely related `SpatialPoint`, boundary-side,
        occurrence-port subject, `SpatialAnchorId`, `SpatialAnchorPosition`, Lane, and Route contracts.
        Remove these mixed responsibilities from `SpatialDocument.kt`; do not split every tiny value
        into its own file.
  - [x] Use public integer drawing units. Anchor identity includes typed Sheet/Occurrence/port and is
        independent of side/point. Anchor facts validate Sheet ownership, side, boundary point, and
        nonempty Source Trace. Route endpoint fields use typed Anchor identities.
  - [x] Migrate active compiler, Spatial-model, quality, Presentation, and test callers directly.
        Delete `Double` Anchor/Route point contracts and rounding; add no adapter or fallback.
- [x] Task 4: Compile stable boundary Anchors and fail closed (AC: 1-3)
  - [x] Add cohesive internal `SpatialAnchorCompiler` and separate validator. Build endpoint requests
        from typed Projection Connections, resolve each occurrence-port against exactly one projected
        occurrence-port fact and one same-Sheet geometry fact, and deduplicate legitimate repeated
        references by typed identity.
  - [x] Implement the exact side and integer edge-distribution policy from AC 2 with overflow-safe
        `Long` arithmetic. Canonically order Anchors by authored Sheet order, typed Occurrence
        identity, then semantic port identity. Return immutable facts and diagnostics.
  - [x] Add separate RED/GREEN cases for null endpoint, unknown Occurrence, zero/multiple projected
        port facts, zero/multiple geometry facts, foreign ownership, duplicate Connection identity
        with differently oriented peers, identical source/target occurrence-port, insufficient edge
        capacity, and a multi-defect permutation fixture. Assert all exact diagnostics and empty
        Anchor facts.
- [x] Task 5: Make Routes consume exact Anchors through the sole orchestrator (AC: 3, 4)
  - [x] Invoke `SpatialAnchorCompiler` in `ProjectionSpatialCompiler` after placement/grouping
        geometry and before routing. Any Anchor diagnostic returns complete transformation failure.
  - [x] Refactor `SpatialRouteCompiler` to accept resolved Anchors. Remove internal generic left/right
        Anchor generation, first/last Occurrence endpoint fallback, nullable endpoint selection, and
        `mapNotNull` loss. Preserve Connection meaning and exact source/target endpoint roles.
  - [x] Keep routing changes narrow: exact endpoints and a deterministic orthogonal middle path are
        sufficient here. Do not implement obstacle search, final Lane allocation, professional
        optimization, multi-Sheet continuation, or document-wide Route trace validation early.
        Preserve the current single deterministic `lane:<view>:main` fact for valid Connections as
        an explicitly temporary basic Lane contract; add no new Lane policy or quality claim before
        Story 2.2.
  - [x] Update `SpatialToPresentationTransformation` so integer Spatial Route points copy directly to
        Presentation. Presentation endpoint facts use exact semantic port and Anchor identities
        available from Spatial; no generated fake endpoint identity may replace them.
- [x] Task 6: Prove active-path and Golden Fixture coverage, then verify (AC: 1-5)
  - [x] Update orchestrator, Engineering-to-Projection, authored-view, Spatial-model, Presentation,
        and Golden Fixture tests with literal exact contracts. Golden Fixture expected Anchors must
        equal unique typed Connection endpoint occurrence-ports exactly; assert all Route endpoints
        resolve to those Anchors.
  - [x] Preserve Stories 1.1-1.3 byte-equality for Occurrence, Region, Construct, alignment, grid,
        and Grid Reference facts under their focused suites. Update only fixtures that relied on
        retired fallback endpoints, port display nodes, primitive strings, or `Double` geometry.
  - [x] Run focused Anchor/Route/orchestrator/transformation/Golden tests, Projection-model tests,
        Spatial-model tests, full compiler tests, repository tests, source-set hygiene audit,
        encoding audit, and `git diff --check` strictly sequentially. Complete all story records
        before moving to review.

### Review Findings

- [x] [Review][Patch] Spatial validation drops Sheet identity when resolving an Anchor's owning
      Occurrence. [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt:110`]
- [x] [Review][Patch] Serialized Anchor identity can collide when structured identity parts contain
      delimiters. [`kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt:38`]

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  Projection owns selected occurrence-port and Connection endpoint identities but no coordinates.
  Spatial alone owns Anchor identity, side, point, and Route geometry.
- Apply AD-20, AD-22, AD-23, AD-26, AD-27, AD-28, AD-29, and AD-30.
  `ProjectionSpatialCompiler` remains the only public Spatial orchestrator. `SpatialAnchorCompiler`
  and `SpatialRouteCompiler` are internal stages, not alternative pipelines.
- Public Spatial geometry uses integer drawing units. Use exact doubled centers and `Long`
  intermediates for orientation/distribution math. Do not expose `AthenaRouteEngine`, electrical
  roles, solver facts, `Double`, pixels, renderer state, or authored geometry in public contracts.
- Failures are complete. An invalid Anchor stage publishes no Anchors, Routes, Lanes, quality facts,
  `SpatialDocument`, or Presentation. Diagnostics use human-first subject/problem/correction/trace.
- Projection validation must not consume the invalid endpoint fixtures governed by FR-5.4. Those
  fixtures reach `SpatialAnchorValidator`, which owns exact missing/duplicate port diagnostics.
- Pre-1.0 rule applies: delete primitive/fallback behavior and migrate callers. No string-field shim,
  generated fake endpoint, optional compatibility constructor, or old-path test remains.

### Exact Anchor Policy

For one endpoint owner rectangle `r` and peer rectangle `p`, compare exact doubled centers:

```text
dx2 = (2 * p.x + p.width) - (2 * r.x + r.width)
dy2 = (2 * p.y + p.height) - (2 * r.y + r.height)
```

- Choose horizontal when `abs(dx2) >= abs(dy2)` and either delta is nonzero. Positive `dx2` selects
  `RIGHT`; negative selects `LEFT`. Otherwise positive `dy2` selects `BOTTOM`, negative selects
  `TOP`.
- If both deltas are zero, a source endpoint selects `RIGHT` and a target endpoint selects `LEFT`.
  An identical typed source/target occurrence-port is invalid and fails before this tie rule.
- When a port participates in multiple Connections, sort incidents by stable Projection Connection
  identity and use the first to choose its one stable side. This preserves one Anchor per port.
  Duplicate Projection Connection identities fail before selection so input order cannot break the
  canonical-first rule.
- Within each `(Occurrence, side)` group, sort by semantic port identity. For zero-based `index` and
  group `count`, use `offset = floor((index + 1) * edgeLength / (count + 1))`. Apply offset to `y`
  on left/right and to `x` on top/bottom; fixed coordinate is the exact rectangle boundary.
- Require `edgeLength >= count + 1`, making offsets distinct and non-corner. If not, emit one exact
  capacity diagnostic for the Occurrence side and publish no Anchors. Never clamp or merge ports.

### Typed Contract Direction

- Recommended Projection cluster in `ProjectionElements.kt` or one cohesive related file:
  `ProjectionOccurrencePort`, typed occurrence-port identity, and `ProjectionConnectionEndpoint`.
  `ProjectionDocument.occurrencePorts` is canonical and coordinate-free.
- Recommended Spatial cluster in `SpatialRoutingModels.kt`: `SpatialPoint(Int, Int)`,
  `SpatialBoundarySide`, typed occurrence-port subject/identity, `SpatialAnchorPosition`, existing
  Lane contract, and Route contract. Anchor identity derives from owning Sheet plus typed
  occurrence-port; point is output, not identity.
- Anchor Source Trace includes owning Sheet, Occurrence, port, and canonical incident Connection
  Projection identities plus the corresponding occurrence, port, and Connection geometry origins.
  Preserve complete distinct canonical lists.
- Repeated Connection use of one projected occurrence-port must reuse one Anchor. Duplicate means
  more than one projected occurrence-port fact for the same typed occurrence-port key, not multiple
  valid Connection references to that key.

### Current Code Intelligence

- `ProjectionConnection` currently exposes four nullable raw strings and `ProjectionDocument` has no
  projected port collection. This cannot prove missing/duplicate port resolution or typed identity.
- `EngineeringToProjectionTransformation` currently emits Engineering ports as display nodes and
  leaves every Connection endpoint null. Active `deriveRealityPresentation` uses this transformation,
  so `SpatialRouteCompiler` currently succeeds only through its prohibited first/last fallback.
- `AuthoredProjectionViewCompiler` populates raw endpoint strings but uses `mapNotNull` plus
  `singleOrNull`, which can silently omit selected engineering Connections. Do not preserve silent
  loss while adding projected port facts.
- `SpatialRouteCompiler` currently creates generic left/right `Double` Anchors per node, falls back
  to first/last Projection nodes, and silently drops unresolved routes with `mapNotNull`.
- `SpatialDocument.kt` mixes root, Anchor, Lane, `SpatialPoint(Double, Double)`, and Route models.
  Split routing responsibility as required by architecture and Kotlin organization rules.
- `SpatialToPresentationTransformation` currently rounds every Spatial Route point and generates
  fake source/target port and Anchor identities from Route IDs. Replace this only as needed for
  exact endpoint preservation; Story 3.1 owns complete per-Sheet document assembly.

### Previous Story Intelligence

- Stories 1.1-1.3 are `done`. They established Sheet-qualified typed IDs, integer `SpatialRect`,
  immutable canonical lists, exact Source Trace, separate compiler/validator stages, human-first
  diagnostics, and strict fail-closed orchestration. Match those patterns.
- Story 1.3 review found that duplicate facts must retain secondary defects, repeated diagnostics
  must merge traces canonically, model invariants must reject contradictory facts, and acceptance
  tests must mutate every named independent input. Apply these lessons from the first RED fixture.
- `SpatialGridCompiler` uses overflow-safe exact center math and stage-local validation. Reuse its
  doubled-center helpers only if ownership remains clear; do not create a generic geometry utility
  without meaningful shared complexity.
- Full verification after Story 1.3 passed 433 compiler tests and repository `test` with 148 tasks,
  plus source-set hygiene, encoding, and diff checks. Preserve that baseline.

### Git Intelligence

- Current baseline includes M41 recovery planning (`8fd6e34`) and a large pre-M41 snapshot
  (`f224586`). Stories 1.1-1.3 remain in the current dirty worktree rather than isolated commits.
- Read current disk state before every edit. Do not use commit-only diffs to infer Story 2.1 scope,
  do not revert unrelated worktree changes, and record every actual file touched in this story.
- No dependency or library change is required. Kotlin standard-library integer/collection APIs and
  existing test infrastructure are sufficient; web research found no version-sensitive need.

### Expected Files

- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- Create: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorValidator.kt`
- Create: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompilerTest.kt`
- Create: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- Create/update Spatial routing model tests.
- Update: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt`
- Update: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
- Update: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- Update: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- Update: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- Update relevant Projection/Spatial/compiler/Presentation/Golden fixtures only where typed ports,
  integer points, exact Anchors, or retired fallback behavior require migration.

### Testing Requirements

- Follow RED-GREEN-REFACTOR per task. Record each first failing compilation/assertion before editing
  production. Literal expected values and exact diagnostic contracts are required.
- Test same-edge distribution with dimensions that make every expected offset hand-computable.
  Include capacity boundary (`edgeLength == count + 1`) and one insufficient case.
- Prove active `ProjectionSpatialCompiler` failure has no `SpatialDocument`; helper-only failure does
  not satisfy AC 3. Prove Presentation endpoint points/identities come from Spatial facts.
- Run Gradle commands strictly sequentially on Windows:

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests "*SpatialAnchorCompilerTest*" --tests "*SpatialRouteCompilerTest*" --tests "*ProjectionSpatialCompilerTest*" --tests "*EngineeringToProjectionTransformationTest*" --tests "*DedicatedM41ExampleTest*"
.\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:spatial-model:test
.\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
.\gradlew.bat --no-daemon --console=plain test
powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1
powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1
git diff --check
```

### Scope Boundaries

- Story 2.2 owns obstacle-safe routing, positive orthogonal segment validation, final deterministic
  Lane facts beyond the temporary one-Lane contract, impossible-route diagnostics, and cross-Sheet
  continuation failure.
- Story 2.3 owns complete Route Source Trace, no-fallback audit, and final Connection coverage.
- Story 3.1 owns complete typed per-Sheet `SpatialDocument` assembly. Story 3.2 owns document-wide
  validation of every identity, coverage, geometry, and trace invariant.
- Epic 4 owns corrected quality math and baseline. M42 owns labels, M43 render/export consumption,
  M44 readability, and M45 professional routing/optimization/multi-Sheet continuation.
- Do not add Athena syntax, authored coordinates/sides, a second routing engine, compatibility
  adapters, milestone production types, or proof/demo/sample classes in `src/main`.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md` Epic 2, Story 2.1]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md` Sections 3, 6-7, 9, 11-13]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md` Confirmed Decisions 6-10]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md` AD-20, AD-22, AD-23, AD-26-AD-30]
- [Source: `docs/superpowers/specs/2026-08-03-m41-spatial-reality-recovery-design.md` Data Flow, Spatial Contract, Anchors And Routes, Verification Design]
- [Source: `_bmad-output/implementation-artifacts/m41/m41-delivery-audit-2026-08-03.md` Findings 6, 9-11, 14-15]
- [Source: `_bmad-output/implementation-artifacts/m41/1-3-derive-grid-reference-facts.md`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`]
- [Source: `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`]
- [Source: `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt`]
- [Source: `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, all five PRD shards, all five
  architecture shards, approved design, failed-delivery audit, Story 1.3 records, current source and
  tests through CodeGraph, and git state.
- RED/GREEN cycles compiled typed Projection occurrence-port/endpoints, integer Spatial routing
  contracts, exact Anchor policy, fail-closed diagnostics, exact Route endpoints, and direct
  Presentation copying through focused tests before each production stage was accepted.
- Golden extraction RED intentionally failed `DedicatedM41ExampleTest` at line 55 and printed all 16
  canonical Anchors. GREEN replaced extraction with one literal canonical snapshot covering Anchor
  identity, owning Sheet, occurrence-port subject, side, point, Projection trace, and geometry trace;
  exact 9-Connection Route and Presentation endpoint coverage passed.
- Full compiler RED exposed two stale expected Projection fixtures without typed endpoints/ports and
  one falsely named empty fixture that still contained an invalid Connection. Expected fixtures now
  represent current typed contracts; truly empty input asserts the earlier Projection Reality gate.
- Verification passed sequentially on 2026-08-03: focused Anchor/Route/orchestrator/transformation/
  Golden suite, Projection-model suite, Spatial-model suite, 446-test compiler suite, repository
  `test` with 148 tasks, source-set hygiene audit, encoding audit, and `git diff --check`.
- Adversarial review: Blind Hunter completed; Edge Case Hunter and Acceptance Auditor timed out after
  bounded review. Four findings were triaged: two fixed, cross-Sheet routing dismissed because Story
  2.2 owns it, and canonical-first incident selection dismissed because AC 2 explicitly requires it.
- Review RED: focused Spatial-model tests produced three expected failures for wrong-Sheet typed
  Anchor acceptance, colliding delimiter-bearing serialized IDs, and the old literal serialization.
  Review GREEN: full typed occurrence comparison and labeled UTF-8 form encoding passed focused and
  full sequential verification.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added coordinate-free typed Projection occurrence-port facts and typed Connection endpoints;
  removed raw nullable endpoint string fields and port display-node authority.
- Added typed integer Spatial Anchor/Route contracts plus stable Sheet/Occurrence/port Anchor
  identity independent of side and point.
- Added separate Anchor compiler/validator with canonical incident selection, exact doubled-center
  orientation, semantic edge distribution, complete trace, capacity checks, and fail-closed sorted
  diagnostics without partial facts.
- Routes now consume exact resolved Anchors; generic left/right Anchors, first/last Occurrence
  fallback, silent Route loss, `Double` endpoint geometry, and Presentation rounding are absent.
- Rolling-shutter Golden Fixture proves literal canonical 16-Anchor facts and exact 9-Connection
  Route/Presentation endpoint identity and point preservation.
- Review hardened Spatial validation to resolve owning Occurrences by complete Sheet-qualified typed
  identity and made serialized Anchor identities collision-free for delimiter-bearing fields.

### File List

- `_bmad-output/implementation-artifacts/m41/2-1-build-anchor-accurate-routes.md`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionViewCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformation.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionPlacementPlanner.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialAnchorValidator.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompiler.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformation.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionSpatialQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AuthoredProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ConnectionPaintCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/EngineeringToProjectionTransformationTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41GeometryQualityTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/ProjectionSpatialLayoutTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialAnchorCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialQualityCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialRouteCompilerTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialTestFixtures.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/SpatialToPresentationTransformationTest.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionDocument.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionElements.kt`
- `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionReality.kt`
- `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelContractTest.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialDocument.kt`
- `kernel/spatial-model/src/main/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModels.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialDocumentTest.kt`
- `kernel/spatial-model/src/test/kotlin/com/engineeringood/athena/spatial/SpatialRoutingModelsTest.kt`

### Change Log

- 2026-08-03: Created through BMad create-story from milestone-local M41 artifacts; marked
  ready-for-dev.
- 2026-08-03: Implemented typed occurrence-port Anchors, exact integer Route endpoints, fail-closed
  validation, literal Golden Fixture proof, and full sequential verification; moved to review.
- 2026-08-03: Fixed both reachable adversarial-review findings through RED/GREEN tests, reran full
  sequential verification, and marked Story 2.1 done.
