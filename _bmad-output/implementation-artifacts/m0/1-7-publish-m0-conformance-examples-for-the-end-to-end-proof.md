---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.7: Publish M0 Conformance Examples For The End-to-End Proof

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to ship representative M0 example projects with stable expected outcomes,
so that the end-to-end semantic compilation proof can be verified repeatedly against known authored inputs, diagnostics, IR shape, and `SVG` output class.

## Acceptance Criteria

1. Given the completed M0 compiler path for parsing, lowering, validation, and rendering, when example projects under `examples/` are executed through the standard compilation entry path, then Athena produces the expected success or failure result for each example, and each example has stable expectations for diagnostics, `Engineering IR` shape, and `SVG` output class where applicable.
2. Given the M0 scope definition, when the example suite is assembled, then it contains at least `5` and at most `10` representative projects covering valid and invalid cases for declarations, ports, references, types, and connections, and the examples remain authored in the DSL rather than in downstream interchange or renderer-specific formats.
3. Given a regression in parser behavior, semantic lowering, validation, or rendering, when the conformance examples are re-run, then the regression is detectable through changed expected outcomes or fixtures, and the examples serve as a stable proof artifact for the M0 architectural decision.

## Tasks / Subtasks

- [x] Define the conformance-suite expectation format and automated test surface. (AC: 1, 2, 3)
  - [x] Keep authored examples as `.athena` DSL files under `examples/m0/`.
  - [x] Publish stable expectation files and artifacts alongside the examples rather than hiding expectations only inside test code.
- [x] Expand the `examples/m0/` suite to between `5` and `10` representative projects. (AC: 1, 2)
  - [x] Include valid and invalid coverage for declarations, ports, references, types, and connections.
  - [x] Preserve the existing demo and invalid semantic examples while adding the missing coverage cases.
- [x] Add automated compiler-path conformance tests that execute every example through the standard entry path. (AC: 1, 3)
  - [x] Assert the expected success or failure result for each example.
  - [x] Assert stable diagnostic rule IDs, IR shape expectations, and `SVG` behavior or artifact where applicable.
- [x] Publish any additional stable proof artifacts required by the valid examples. (AC: 1, 3)
  - [x] Reuse the existing published IR and SVG artifact pattern where it adds value for representative valid examples.
- [x] Document the M0 conformance suite, example inventory, and expectation conventions. (AC: 1, 2, 3)

## Dev Notes

### Story Intent

- Story `1.7` turns the current seed fixtures into the explicit M0 proof set.
- The proof target is not “more examples�?in isolation. The proof target is a stable, rerunnable conformance contract over the real compiler path.
- The examples must remain DSL-authored sources. Expected artifacts and expectation files may sit beside them, but the examples themselves must not move into downstream-only formats.

### Architecture Guardrails

- `examples/` is part of the architecture contract under AD-7, not disposable sample content.
- The standard compilation entry path remains `AthenaCompiler` end-to-end. Do not create a separate hidden harness that bypasses normal parsing, lowering, validation, or rendering.
- Keep semantic authority in `Engineering IR`; the conformance suite checks outcomes, it does not create alternate semantic truth.
- Preserve the Story `1.6` downstream rule: valid examples may emit `SVG`; invalid semantic examples must block rendering according to policy.

### Technical Requirements

- Add the conformance-suite automation under `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`.
- Keep the expectation format simple and local. A lightweight line-oriented file or similarly minimal format is preferred over adding a parser dependency.
- For parse-success examples, the conformance contract should at minimum be able to assert:
  - semantic success or semantic invalidity
  - component, port, and connection counts
  - diagnostic rule IDs when invalid
  - whether `SVG` is emitted or blocked
- For representative valid examples, publish exact expected `Engineering IR` and `SVG` artifacts where that adds proof value.
- Reuse existing helper patterns from current compiler tests where possible; do not duplicate a second compilation model.

### Architecture Compliance

- Align to AD-7 by treating example expectations as stable architecture inputs.
- Align to AD-3 and AD-4 by checking compiler and renderer outcomes through the same canonical pipeline rather than isolated helper logic.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Reuse the existing Kotlin test stack.
- Do not add YAML/JSON parser libraries just to load conformance expectations.

### File Structure Requirements

- Expected primary touch points:
  - `examples/m0/**`
  - `examples/README.md`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `docs/compiler/**`
- Avoid production-code changes unless the test surface truly needs a tiny compiler helper or public contract addition.

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- The conformance suite should fail loudly if:
  - an example count falls outside `5-10`
  - a required expectation file is missing
  - diagnostics drift
  - a published IR or `SVG` artifact changes unexpectedly

### Previous Story Intelligence

- Story `1.6` now provides the first stable `SVG` artifact and render blocking behavior.
- Story `1.5` already gives an inspectable pass pipeline; Story `1.7` should use that real path, not bypass it.
- Current seed fixtures are:
  - `demo-cabinet.athena`
  - `demo-cabinet.engineering-ir.txt`
  - `demo-cabinet.svg`
  - `invalid-semantic-cabinet.athena`

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story `1.7` acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-7 and the examples contract.
- `_bmad-output/specs/spec-athena/SPEC.md` - conformance-suite success signal and `5-10` example requirement.
- `_bmad-output/implementation-artifacts/m0/1-6-derive-a-render-model-and-emit-simple-svg-from-engineering-ir.md` - current render proof boundary and published SVG artifact.
- `examples/README.md` - current examples inventory.

## Story Completion Status

- Status: done
- Completion note: Published the M0 conformance example suite, automated its end-to-end compiler checks, closed the Epic 1 review findings, and verified the final proof on Java 25.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status identified `1-7-publish-m0-conformance-examples-for-the-end-to-end-proof` as the next backlog story in Epic 1.
- Epic 1 close-out included a full review pass over the implemented workspace, compiler, renderer, examples, and planning artifacts.
- Fresh verification evidence after closing Story `1.7` and the Epic 1 review findings:
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain build` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain test` -> `BUILD SUCCESSFUL`

### Completion Notes List

- Created the concrete Story `1.7` developer guide for the M0 conformance example suite and end-to-end proof artifacts.
- Published sidecar expectation files for the representative M0 examples and kept the authored sources as `.athena` DSL files under `examples/m0/`.
- Added `M0ConformanceExamplesTest` to run every published example through the standard compiler path and assert semantic outcome, counts, diagnostics, and `SVG` behavior.
- Expanded the suite to six representative examples covering valid flow plus declaration, reference, type, direction, and duplicate-identity failures.
- Reused the existing `demo-cabinet` published IR and `SVG` artifacts as exact proof fixtures for the valid path.
- Documented the conformance suite contract in `docs/compiler/m0-conformance-suite.md` and updated `examples/README.md`.
- Closed the Epic 1 review findings by requiring one expectation file per `.athena` source and by removing workstation-specific Java path assumptions from the repository bootstrap docs and Gradle properties.

### File List

- `_bmad-output/implementation-artifacts/m0/1-7-publish-m0-conformance-examples-for-the-end-to-end-proof.md`
- `_bmad-output/implementation-artifacts/m0/sprint-status.yaml`
- `DEV.md`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/M0ConformanceExamplesTest.kt`
- `docs/compiler/m0-conformance-suite.md`
- `docs/compiler/workspace-bootstrap.md`
- `examples/m0/demo-cabinet.expectation.txt`
- `examples/m0/dual-drive-cabinet.athena`
- `examples/m0/dual-drive-cabinet.expectation.txt`
- `examples/m0/duplicate-identity-cabinet.athena`
- `examples/m0/duplicate-identity-cabinet.expectation.txt`
- `examples/m0/invalid-direction-cabinet.athena`
- `examples/m0/invalid-direction-cabinet.expectation.txt`
- `examples/m0/invalid-semantic-cabinet.expectation.txt`
- `examples/m0/quoted-properties-cabinet.athena`
- `examples/m0/quoted-properties-cabinet.expectation.txt`
- `examples/README.md`
- `gradle.properties`

## Change Log

- 2026-07-02: Published the M0 conformance example suite, automated end-to-end expectation checks, and documented the proof contract.
- 2026-07-02: Closed Epic 1 review findings by requiring full example/expectation inventory coverage and removing workstation-specific Java path configuration from the repository.
