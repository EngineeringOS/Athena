---
baseline_commit: 133972260704e62391d462a5b96e2a84e3596576
---

# Story 2.6: Publish The External-Domain Proof Corpus

Status: done

### Change Log

- 2026-07-07: Resolved post-review findings by fixing hosted-domain mismatch detection in the compiler and correcting dummy-domain authored-marker documentation.

## Story

As a founder or reviewer,
I want a minimal M3 proof corpus for hosted external domains,
so that the milestone has reusable evidence showing both proof domains participate through the same contracts.

## Acceptance Criteria

1. Given the electrical and dummy proof domains both participate through the stable SPI, when M3 proof fixtures are published, then Athena adds the minimum example and expectation corpus needed to exercise both domains through the hosted path, and the corpus stays limited to the proof scope rather than expanding into broad sample coverage.
2. Given M3 inherits the earlier rule that examples are architecture contract inputs, when the proof corpus is reviewed, then fixtures are structured clearly enough to support later verification and story execution, and they do not create a second parallel source of semantic truth outside the authored DSL.
3. Given the proof corpus must support later verification work in Epic 3, when the published fixtures are inspected, then they can be reused by the automated zero/one/multi-plugin verification matrix, and they make both the real proof domain and the synthetic proof domain visible to reviewers.
4. Given the external-domain proof corpus is implemented, when the standard Java `25` build and example regression checks are executed, then the workspace builds successfully and Epic 2 ends with reusable proof fixtures for both hosted domains, and the milestone has concrete evidence that real domain behavior can live outside kernel code.

## Tasks / Subtasks

- [x] Create the Story 2.6 artifact and move sprint tracking onto the active implementation state. (AC: 1, 2, 3, 4)
  - [x] Add the dedicated M3 story file under `_bmad-output/implementation-artifacts/m3/`.
  - [x] Move `2-6-publish-the-external-domain-proof-corpus` from `backlog` to the active execution state in the M3 sprint tracker.
- [x] Publish the minimum `examples/m3` proof corpus. (AC: 1, 2, 3)
  - [x] Add a small authored DSL corpus covering `electrical-only`, `dummy-only`, and `both` hosted plugin sets.
  - [x] Add sidecar expectation files that declare the hosted plugin-set intent and the minimum expected outcomes.
  - [x] Add English and Chinese folder READMEs and update the root `examples/README.md`.
- [x] Add automated regression checks that consume the published M3 proof corpus. (AC: 2, 3, 4)
  - [x] Add a dedicated compiler-side corpus test that reads `examples/m3/*.expectation.txt`.
  - [x] Execute each corpus example through an explicit hosted plugin set using the governed discovery seam rather than implicit environment state.
  - [x] Prove the dummy-only path stays semantically hosted even though it does not publish global default view definitions.
- [x] Verify the workspace sequentially on Java `25` and record the result. (AC: 4)
  - [x] Run the affected regression suite on Java `25`.
  - [x] Run the standard sequential Java `25` build.
  - [x] Record commands, changed files, and completion notes in this story record.

## Dev Notes

### Story Intent

- M3 needs a published proof corpus, not just ad hoc tests with inline source strings.
- The corpus should stay small enough to review but explicit enough to become the future Epic 3 matrix seed.
- The authored DSL remains the only semantic source. Sidecar files may describe expected hosted outcomes, not duplicate semantic facts with a second canonical model.

### Implementation Direction

- Keep `examples/m3/` narrow:
  - one electrical-only proof
  - one dummy-only proof
  - one combined hosted proof
- Use expectation files to declare hosted plugin intent, deterministic approved inventory, supported views, and rendering expectations.
- Drive verification through explicit hosted plugin sets in tests so the corpus remains reusable even if the default workspace inventory grows later.
- Do not publish dummy-owned fake layouts or view definitions just to make the dummy-only proof render. A blocked backend with valid hosted semantics is the correct proof for the current architecture.

### Previous Story Intelligence

- Story `2.4` refactored `domain-electrical` into the stable M3 proof shape and kept `cabinet` / `wiring` as the global view-definition pair.
- Story `2.5` added `domain-dummy` with explicit dummy ownership, synthetic validation, runtime-view participation, and declared render metadata without global view definitions.
- Epic `3` will automate the zero-plugin and mixed-plugin matrix, so Story `2.6` should publish fixtures and expectations in a reusable shape rather than hardcoding the evidence only inside test methods.

### References

- [Source: _bmad-output/planning-artifacts/epics-M3-2026-07-07.md#story-26-publish-the-external-domain-proof-corpus]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-07/ARCHITECTURE-SPINE.md]
- [Source: examples/README.md]
- [Source: kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M0ConformanceExamplesTest.kt]
- [Source: kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M2GeometryBackendExamplesTest.kt]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `Get-Content _bmad-output/planning-artifacts/epics-M3-2026-07-07.md`
- `Get-Content _bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `Get-Content examples/README.md`
- `Get-Content examples/m2/README.md`
- `Get-Content examples/m2/demo-cabinet.athena`
- `Get-Content examples/m2/operator-proof.athena`
- `codegraph explore "DummyRuntimeDomainPlugin ElectricalRuntimeDomainPlugin"`
- `Get-Content extensions/domain-dummy/src/main/kotlin/com/engineeringood/athena/domain/dummyruntime/DummyRuntimeDomainPlugin.kt`
- `Get-Content kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `Get-Content kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M2GeometryBackendExamplesTest.kt`

### Completion Notes List

- Published a new `examples/m3/` proof corpus with three authored fixtures covering the `electrical-only`, `dummy-only`, and combined hosted plugin states.
- Kept the proof corpus architecture-clean by storing semantic truth only in `.athena` sources and using sidecar expectation files only for deterministic hosted outcomes such as approved plugin sets, supported views, and rendering status.
- Added `M3ExternalDomainProofExamplesTest` in `:kernel:compiler` to exercise each published proof through an explicit governed plugin set built from the existing hosted discovery seam.
- Proved the dummy-only hosted path remains valid without inventing default global view definitions: the proof compiles semantically, publishes dummy render metadata, and blocks backend emission only because no supported geometry-backed view exists yet.
- Sequential Java `25` verification passed for the new corpus test and the full workspace `build`.

### File List

- `_bmad-output/implementation-artifacts/m3/2-6-publish-the-external-domain-proof-corpus.md`
- `_bmad-output/implementation-artifacts/m3/sprint-status.yaml`
- `examples/README.md`
- `examples/m3/README.md`
- `examples/m3/README.zh-CN.md`
- `examples/m3/electrical-proof.athena`
- `examples/m3/electrical-proof.expectation.txt`
- `examples/m3/dummy-proof.athena`
- `examples/m3/dummy-proof.expectation.txt`
- `examples/m3/dual-domain-proof.athena`
- `examples/m3/dual-domain-proof.expectation.txt`
- `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/M3ExternalDomainProofExamplesTest.kt`

### Change Log

- 2026-07-07: Created Story 2.6 and started implementation for the published external-domain proof corpus.
- 2026-07-07: Completed Story 2.6 implementation, published the M3 proof corpus, and passed sequential Java `25` verification.

### Verification Commands

- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.M3ExternalDomainProofExamplesTest`
- `java25; .\gradlew.bat --no-daemon --console=plain build`
