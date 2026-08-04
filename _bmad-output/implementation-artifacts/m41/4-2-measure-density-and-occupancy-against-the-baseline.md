---
baseline_commit: f2245862b430c56aabdc4ef5bcdf97d587db3f81
---

# Story 4.2: Measure Density And Occupancy Against The Baseline

Status: done

<!-- Note: Created and validated through BMad create-story from milestone-local M41 artifacts. -->

## Story

As an engineer,
I want a generated baseline tied to compiler facts,
so that M41 closure and M44 work start from reproducible evidence.

## Acceptance Criteria

1. Given the dedicated M41 rolling-shutter source at
   `examples/m41/rolling-shutter/src/com/engineeringood/m41/rollingshutter/01-rolling-shutter-spatial.athena`,
   baseline generation compiles those exact UTF-8 source bytes through `AthenaCompiler` and the
   active `ProjectionSpatialCompiler` authority path. It writes one versioned, canonical UTF-8
   artifact at `_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties`.
   No production `src/main` proof type, alternate compiler, copied Golden oracle, Presentation fact,
   pixel sample, or renderer calculation may generate the artifact. (FR-13.1, AD-24, AD-27)
2. The artifact records schema version; fixture source path and SHA-256 digest of the exact bytes
   compiled; generation command and UTC ISO-8601 timestamp; Sheet identity, extent, and Drawing
   Area; typed subject counts; definitions for all nine M41 quality metrics; and exact per-Sheet
   metric values. Density and Occupancy are stored as integer numerator and denominator pairs, not
   rounded display decimals. Canonical key order and escaping make two generations from identical
   source, compiler facts, and injected timestamp byte-for-byte equal. (FR-13.1, NFR-1, NFR-4)
3. For the current Golden Fixture, structured baseline facts are exactly: one Sheet
   `schematic/sheet/S1`; extent `(0,0,1200,800)`; Drawing Area `(40,60,1120,640)`; 8 Occurrences,
   3 Regions, 7 Constructs, 10 Alignments, 16 Anchors, 9 Routes, 7 used Lanes, 15 Grid References,
   and 1 quality snapshot. Metric values are overlap `0`, containment failure `0`, Route/body
   intersection `0`, Route crossing `3`, twist `0`, used Lanes `7`, peak Routes per Lane `2`,
   Density `8 / 716800`, and Occupancy `25600 / 716800`. These values are generated from validated
   typed facts, not used as generator inputs. (FR-9.4, FR-12, FR-13.1)
4. A verification test strictly parses the committed artifact, independently reads and hashes the
   fixture source, recompiles it through the real authority path, projects the resulting typed
   `SpatialDocument` into the baseline schema, and compares every structured field and exact value.
   It rejects missing, duplicate, unknown, malformed, non-finite, out-of-order, or unsupported-
   version properties. It also rejects digest, bounds, count, metric definition, rational value,
   command, or timestamp tampering. The verifier never compares artifact values to themselves,
   reconstructs expected facts from prose, or rewrites the artifact during normal tests. (FR-13.2)
5. Baseline extraction preserves canonical Sheet/fact/metric order and proves multi-Sheet records
   remain independent. Density uses each Sheet's Occurrence count and Drawing Area area. Occupancy
   uses each Sheet's exact Occurrence rectangle union area and Drawing Area area. The test-only
   evidence adapter may independently derive integer rational components from validated Spatial
   facts and must cross-check their quotient against the compiler-published typed ratio; it must not
   become runtime metric authority or call a copied test oracle. (FR-12.7, FR-12.8, FR-13.2, AD-24)
6. Gradle exposes executable task `:kernel:compiler:generateM41SpatialQualityBaseline` on the test
   runtime classpath. Generation requires explicit
   `-Pm41BaselineTimestamp=<UTC ISO-8601 instant>`; the recorded command includes that same value,
   so rerunning the recorded command from repository root reproduces the committed bytes when inputs
   are unchanged. Invalid or absent timestamps fail with an actionable task error rather than using
   ambient wall-clock time. (FR-13.1, NFR-1)
7. M40 contributes no numeric comparison row because its fixture, viewport context, units/area
   denominator, and Presentation/screenshot method are not identical to M41 Spatial measurement.
   The artifact may record only explicit non-comparability metadata and reason. Any future M40 row
   must prove exact equality of fixture digest, viewport, units, and method before values are
   admitted. No label count, label pressure, label collision, M40 reporter/adapter, A3 proxy,
   optimization target, M44 readability claim, or M45 routing claim appears in schema or artifact.
   (FR-12.9, FR-13.3, AD-24, AD-27)
8. Focused baseline tests, existing dedicated M41 Golden tests, `:kernel:compiler:test`, repository
   `test`, source-set hygiene audit, encoding audit, and `git diff --check` all pass sequentially.
   Story records contain every executed command and result before status advances to review. (GATE-4,
   GATE-6)

## Tasks / Subtasks

- [x] Task 1: Prove strict baseline schema and canonical codec with RED tests (AC: 2, 4, 7)
  - [x] Define one test-source typed baseline model and closed schema version `1`: metadata,
        ordered per-Sheet bounds/counts/metrics, and explicit M40 comparison status. Keep schema small,
        inspectable, deterministic, and implementation-neutral.
  - [x] Test UTF-8 `.properties` parsing for missing, duplicate, unknown, malformed, out-of-order,
        unsupported-version, invalid timestamp, invalid SHA-256, negative count, zero/negative
        denominator, and non-finite value cases. Use `java.util.Properties` loading with duplicate
        detection; do not add JSON/YAML dependencies or parse arbitrary text with fragile splitting.
  - [x] Test deterministic sorted serialization, required escaping, parse/encode round-trip, and
        byte identity across two encodes with the same fixed `Instant`.
- [x] Task 2: Build real-fixture compilation support without another authority (AC: 1, 3, 4)
  - [x] Extract cohesive test support from `DedicatedM41ExampleTest.kt` that locates the repository,
        reads the exact active `.athena` source as UTF-8 bytes, compiles its decoded text through
        `AthenaCompiler`, and returns the validated `SpatialDocument` plus source metadata.
  - [x] Preserve every existing dedicated M41 Golden and permutation assertion. Reuse compilation
        plumbing only; `DedicatedM41SpatialGoldenAssertions` remains an independent literal test
        oracle and is never read by baseline generation.
  - [x] Compute SHA-256 over the exact source byte array passed into UTF-8 compilation. Record a
        repository-relative forward-slash path; perform no newline, BOM, whitespace, or path
        normalization before hashing.
- [x] Task 3: Project validated Spatial facts into exact evidence records (AC: 2, 3, 5)
  - [x] Extract canonical Sheet identity, extent, Drawing Area, and counts for Occurrences, Regions,
        Constructs, Alignments, Anchors, Routes, used Lanes, Grid References, and quality snapshots.
        Reject missing or multiple quality snapshots instead of choosing one silently.
  - [x] Record definitions and integer values for the seven count metrics. Record Density numerator
        as Occurrence count and denominator as Drawing Area area. Independently compute exact
        rectangle-union Occupancy numerator from typed Occurrence bounds and use the same per-Sheet
        denominator; cross-check both rational quotients against compiler-published ratios.
  - [x] Add a synthetic multi-Sheet test with different bounds, counts, and overlaps. Assert stable
        Sheet ordering, independent denominators, exact union area, and no cross-Sheet contamination.
- [x] Task 4: Implement generator and explicit Gradle entry point (AC: 1, 2, 6, 7)
  - [x] Add test-source `M41SpatialQualityBaselineGenerator` accepting source path, artifact path,
        and injected timestamp. It compiles real source, builds one typed record, canonically encodes
        it, and writes only the milestone-local artifact.
  - [x] Add `generateM41SpatialQualityBaseline` as `JavaExec` in
        `kernel/compiler/build.gradle.kts`, depending on test classes and using test runtime
        classpath. Require/validate `m41BaselineTimestamp`; pass repository-root-resolved source and
        output paths without machine-specific paths entering the artifact.
  - [x] Record full executable command including the injected timestamp. Publish explicit M40
        non-comparability metadata only; reject numeric M40 entries and all label metric keys.
- [x] Task 5: Generate and independently verify committed artifact (AC: 1-7)
  - [x] Run generator with one explicit UTC instant and commit its canonical output under M41.
        Rerun same command and prove `git diff` remains empty for that artifact.
  - [x] Verification reads committed bytes, requires canonical re-encoding equality, recompiles
        fixture independently, and compares typed actual record field-by-field while preserving the
        artifact timestamp for byte comparison.
  - [x] Add tamper tests for source bytes, digest, each bounds/count/metric category, rational
        numerator/denominator, command, timestamp, and forbidden M40/label keys. Failure messages
        must name exact field and correction.
- [x] Task 6: Run all gates sequentially and complete BMad records (AC: 8)
  - [x] Run focused codec/generator/verifier and dedicated M41 tests, then
        `:kernel:compiler:test`, repository `test`, source-set hygiene audit, encoding audit, and
        `git diff --check`. Never run two Gradle invocations concurrently on Windows.
  - [x] Confirm no `src/main` proof/demo/sample/milestone class, dependency, compatibility shim,
        stale M40 authority, label metric, Presentation-derived value, or generated machine path.
  - [x] Complete Tasks, Debug Log, Completion Notes, File List, Change Log, review findings, and
        sprint status before moving to review and adversarial acceptance.

### Review Findings

- [x] [Review][Patch] Pin expected committed generation timestamp so coordinated timestamp and
      command tampering fails verification
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineVerifier.kt:9]
- [x] [Review][Patch] Prove multi-Sheet evidence with distinct Drawing Area denominators
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineTest.kt:63]
- [x] [Review][Patch] Cover valid bounds, metric-definition, rational, command, and timestamp
      tampering field by field
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineTest.kt:173]
- [x] [Review][Patch] Bound `sheet.count` and report missing core properties without uncontrolled
      allocation or leaked lookup exceptions
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineSupport.kt:46]
- [x] [Review][Patch] Compile captured UTF-8 source bytes in memory so digest and compiler input are
      identical
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt:44]
- [x] [Review][Patch] Reject absolute, traversal, and noncanonical fixture provenance paths
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineModels.kt:18]
- [x] [Review][Patch] Derive used Lanes from actual Route assignments and reject off-Drawing-Area
      occupancy evidence
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineProjector.kt:39]
- [x] [Review][Patch] Write generated evidence through a repository-contained atomic temporary file
      and reject symlink or junction redirection
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineGenerator.kt:23]
- [x] [Review][Patch] Record exact gate commands and results in story evidence
      [_bmad-output/implementation-artifacts/m41/4-2-measure-density-and-occupancy-against-the-baseline.md:279]
- [x] [Review][Defer] Assert projected Presentation Occurrence identities/count as part of product
      E2E evidence
      [kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt:343] - deferred, pre-existing; Story 5.2 owns visible product-surface proof.

## Dev Notes

### Architecture Guardrails

- Authority remains Athena source -> Engineering -> Projection -> Spatial -> Presentation -> Theia.
  Baseline consumes the final validated `SpatialDocument`; it does not change compiler authority or
  become an input to compilation.
- Apply AD-24, AD-27, and AD-30. Geometry quality remains exact and per Sheet. Baseline tooling and
  artifact generation live in test source plus milestone artifacts, never production `src/main`.
- Generation and verification are separate responsibilities sharing only schema/codec/projection
  support. Generator receives compiler facts; verifier independently compiles current source and
  compares against parsed committed facts.
- Athena is pre-1.0. Do not restore `SpatialQualityMetricsReporter`, an M40 adapter, old string-key
  runtime metrics, or a compatibility serializer. No external baseline consumer exists.

### Baseline Schema Decisions

- Artifact path is
  `_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties`; encoding is
  UTF-8 without BOM; schema version is `1`.
- Use canonical sorted property keys and one deterministic escaping implementation. `Properties.store`
  alone is unsuitable because it injects a wall-clock comment and does not guarantee canonical key
  order. Parse with `java.util.Properties` or equivalent standard structured support, intercepting
  duplicate writes, then validate exact allowed keys and canonical re-encoding.
- Timestamp is an injected `java.time.Instant` rendered with `Instant.toString()`. It is evidence
  metadata, not compiler truth. Require UTC `Z`; reject local offsets even when semantically equal.
- Source digest is lowercase `sha256:<64 hex>` over the raw bytes of the one source file passed to
  `AthenaCompiler`. Artifact stores repository-relative path only. If compilation later gains more
  input files, schema must explicitly enumerate and hash each input rather than silently widening
  this digest.
- Ratios store unsimplified integer numerator and denominator from facts (`8/716800` and
  `25600/716800` today). This preserves hand-verifiable geometry. Do not serialize rounded decimal
  display values as authority.
- Exact property names belong to codec tests. Recommended groups are `schema.*`, `fixture.*`,
  `generation.*`, `sheet.<index>.*`, and `comparison.m40.*`. Metric definitions and values must be
  closed/typed by the record, not accepted as arbitrary map entries.

### Current Code Intelligence

- `DedicatedM41ExampleTest.kt` already locates repository root, reads the active fixture, compiles
  through `AthenaCompiler`, and proves active Projection-to-Spatial transformation. Extract that
  plumbing rather than add another compiler path.
- `DedicatedM41SpatialGoldenAssertions.kt` already proves complete typed Golden Sheet equality and
  literal expected metrics. Keep it independent; generator must not import values, constants, or
  assertion helpers from this oracle.
- Current exact compiler result: one Sheet, 8 Occurrences, 3 Regions, 7 Constructs, 10 Alignments,
  16 Anchors, 9 Routes, 7 Lanes, 15 Grid References, and one quality snapshot. Source SHA-256 was
  observed as `3af530db2f1390c5b873ed1f5134766293bf390301535adfcb94dfd3cb1a773e`
  during story analysis; this is diagnostic context only and must be recomputed by implementation.
- `kernel/compiler/build.gradle.kts` has Kotlin/JUnit test infrastructure but no active JSON/YAML
  codec dependency and no M41 baseline task. Use JDK `MessageDigest`, `Properties`, and `java.time`;
  add no dependency for this closed artifact.

### Previous Story Intelligence

- Story 4.1 replaced loose metrics with one immutable typed quality snapshot and exact per-Sheet
  formulas. Final validation recomputes compiler-owned quality before Presentation and blocks forged
  finite values. Baseline verification must compare against that validated output, not reintroduce
  loose metric names into production.
- Story 4.1 Golden values are overlap 0, containment 0, body intersection 0, crossing 3, twist 0,
  used Lanes 7, peak Routes/Lane 2, Density `8 / 716800`, and Occupancy `25600 / 716800`.
- Story 4.1 review fixed finite metric forgery, overflow diagnostics, and empty Spatial roots. Keep
  those gates green; baseline extraction may assume a nonempty validated document but must still
  reject malformed evidence records.
- Story 4.1 passed focused tests, full spatial/compiler tests, repository `test` (148 tasks), source
  hygiene, encoding, and diff gates. Continue sequential verification.

### M40 Comparison Boundary

- M40 uses `examples/m40/rolling-shutter-control`, Presentation/A3-area measurement, desktop
  `1920x1080` framing, and label-era evidence. M41 uses different source, Spatial Drawing Area,
  typed geometry union, and no label metrics. None of the four equivalence predicates match.
- Therefore publish zero numeric M40 rows. A boolean/status plus plain reason may document why no
  row exists, but must not smuggle M40 values into the M41 schema. Future comparability needs an
  explicit validator proving fixture digest, viewport, units, and method all equal.

### File Structure Requirements

- Planned new test support:
  `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`,
  `M41SpatialQualityBaselineSupport.kt`, `M41SpatialQualityBaselineGenerator.kt`, and
  `M41SpatialQualityBaselineTest.kt`.
- Planned updates: `kernel/compiler/build.gradle.kts`, `DedicatedM41ExampleTest.kt`, this story, and
  milestone-local `sprint-status.yaml`. No production source update is expected.
- Keep tiny related baseline data classes and codec helpers together. Split generator entry point
  from schema/codec because orchestration and serialization are distinct roles. Avoid one file per
  tiny DTO and avoid a mixed 300+ line support dump.

### Testing Requirements

- Strict red-green-refactor per Task. Record actual initial failing test and assertion before adding
  implementation. Behavioral tests own correctness; text scans only enforce forbidden names/keys.
- Golden expected values remain literal and independent. Baseline generator receives no expected
  count or metric constants. Verification actuals come from a fresh compiler run, never parsed
  artifact values or canonical bytes generated from those same parsed values alone.
- Mutation/tamper tests must show failures, not merely changed hashes. Include fixed timestamp byte
  reproduction and a test proving normal `test` never overwrites the artifact.
- Gradle commands run strictly sequentially. After all source/docs changes run encoding audit and
  source-set hygiene audit even though implementation should remain in test source.

### Git Intelligence

- Baseline remains `f2245862b430c56aabdc4ef5bcdf97d587db3f81` (`push before m41`). Stories
  1.1-4.1 are cumulative intentional uncommitted changes. Preserve them; never restore affected
  files from `HEAD`.
- Recent commits `8fd6e34` and `e93623d` contain M41 planning/design. No dependency, external API,
  framework, or version research is needed for JDK `Properties`, SHA-256, and `Instant`.

### References

- [Source: `_bmad-output/implementation-artifacts/m41/epics.md#Story-4.2-Measure-Density-And-Occupancy-Against-The-Baseline`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/prd.md#FR-13-Generate-And-Verify-The-M41-Baseline`]
- [Source: `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-03-m41/addendum.md#M40-Comparison-Rule`]
- [Source: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-03-m41/ARCHITECTURE-SPINE.md#AD-24-ADOPTED---Quality-Is-Exact-Per-Sheet-And-Derived-From-Validated-Geometry`]
- [Source: `_bmad-output/implementation-artifacts/m41/4-1-measure-quality-milestone-facts.md`]
- [Source: `_bmad-output/implementation-artifacts/m40/4-2-measure-spatial-quality-against-the-m40-target.md`]
- [Source: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`]
- [Source: `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41SpatialGoldenAssertions.kt`]
- [Source: `kernel/compiler/build.gradle.kts`]

## Dev Agent Record

### Agent Model Used

Codex GPT-5

### Implementation Plan

- Prove a closed canonical evidence schema and strict parser before implementation.
- Reuse the real M41 fixture compiler path, then project validated Spatial facts into exact per-Sheet
  records with an independent union-area calculation.
- Keep generator and verifier in test source, expose one explicit timestamped Gradle task, generate
  milestone-local evidence, and verify every field through a fresh compiler run.

### Debug Log References

- Story context created from full milestone-local M41 sprint, epics, PRD/addendum, architecture,
  recovery design/audit, deferred work, completed Story 4.1, current test/compiler patterns, git
  history/worktree state, and independent code plus contract analyses.
- Create-story ambiguity decisions: strict canonical `.properties` schema version 1; raw source-byte
  SHA-256; injected UTC `Instant`; exact rational fields; explicit M40 non-comparability; no
  production source additions.
- RED: baseline tests first failed compilation because schema/codec types did not exist; later RED
  cycles failed on missing fixture support, projector, generator, verifier, and finally the absent
  committed artifact. Each cycle became green before the next Task began.
- Generator without `-Pm41BaselineTimestamp` failed as required with `Missing
  -Pm41BaselineTimestamp=<UTC ISO-8601 instant>; ambient time is not reproducible.`
- Generated baseline twice with timestamp `2026-08-04T01:20:00Z`; both files had SHA-256
  `C53D2F82DD244DBF89500F61C78D753A0624DA8BC4761A4905508F0D1E4B7AB5`.
- Sequential verification passed: 13 focused baseline tests plus dedicated M41 Golden test,
  `:kernel:compiler:test` with 508 tests, repository `test` with 148 actionable tasks, source-set
  hygiene audit, encoding audit, and `git diff --check`.
- Adversarial review RED: focused baseline compilation failed on absent atomic writer and verifier
  timestamp contract. GREEN: 19 focused baseline tests passed after implementing accepted findings.
- `.\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline --no-daemon --console=plain`
  failed as required with `Missing -Pm41BaselineTimestamp=<UTC ISO-8601 instant>; ambient time is
  not reproducible.`
- `.\gradlew.bat :kernel:compiler:generateM41SpatialQualityBaseline
  -Pm41BaselineTimestamp=2026-08-04T01:20:00Z --no-daemon --console=plain` passed twice; each output
  SHA-256 was `C53D2F82DD244DBF89500F61C78D753A0624DA8BC4761A4905508F0D1E4B7AB5`.
- `.\gradlew.bat :kernel:compiler:test --tests
  "com.engineeringood.athena.compiler.M41SpatialQualityBaselineTest" --tests
  "com.engineeringood.athena.compiler.DedicatedM41ExampleTest" --no-daemon --console=plain` passed.
- `.\gradlew.bat :kernel:compiler:test --no-daemon --console=plain` passed with 514 tests.
- `.\gradlew.bat test --no-daemon --console=plain` passed with 148 actionable tasks.
- `powershell -ExecutionPolicy Bypass -File .\tools\source-set-hygiene-audit.ps1` passed.
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1` passed.
- `git diff --check` passed; only existing line-ending warnings were reported.

### Completion Notes List

- Ultimate context engine analysis completed - comprehensive developer guide created.
- Added strict versioned canonical UTF-8 baseline codec with duplicate/unknown/malformed/tamper
  rejection and closed definitions for all nine M41 quality metrics.
- Reused real M41 fixture compilation, hashed exact source bytes, and projected typed Spatial facts
  into exact counts plus unsimplified Density/Occupancy rationals with independent union geometry.
- Added explicit timestamp-required Gradle generation, committed reproducible milestone-local
  evidence, and fresh-compiler field-by-field verification. M40 numeric rows and label metrics remain
  excluded because measurement contexts differ.
- Closed adversarial findings: pinned committed timestamp verification, distinct multi-Sheet
  denominators, complete structured tamper coverage, bounded strict parsing, immutable captured-byte
  compilation, canonical provenance paths, Route-derived used Lanes, contained Occupancy, and atomic
  repository-contained artifact replacement.

### File List

- `_bmad-output/implementation-artifacts/m41/4-2-measure-density-and-occupancy-against-the-baseline.md`
- `_bmad-output/implementation-artifacts/m41/m41-spatial-quality-baseline.properties`
- `_bmad-output/implementation-artifacts/m41/sprint-status.yaml`
- `kernel/compiler/build.gradle.kts`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/DedicatedM41ExampleTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineGenerator.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineModels.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineProjector.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineSupport.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineTest.kt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M41SpatialQualityBaselineVerifier.kt`

### Change Log

- 2026-08-04: Recreated Story 4.2 through BMad create-story with strict versioned baseline schema,
  exact compiler-backed facts, deterministic generation, independent verification, explicit M40
  non-comparability, test plan, and milestone-local boundaries; moved Story 4.2 to ready-for-dev.
- 2026-08-04: Implemented and verified canonical compiler-backed baseline generation, exact rational
  geometry evidence, strict parsing/tamper detection, and fresh compiler verification; moved Story
  4.2 to review.
- 2026-08-04: Resolved all nine accepted adversarial review patch groups, passed focused/compiler/
  repository/audit gates, recorded exact evidence, and moved Story 4.2 to done.
