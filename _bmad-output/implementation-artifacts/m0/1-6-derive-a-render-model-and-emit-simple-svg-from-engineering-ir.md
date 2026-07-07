---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.6: Derive A Render Model And Emit Simple SVG From Engineering IR

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a reviewer,
I want Athena to derive a renderer-facing model from canonical `Engineering IR` and emit a simple `SVG`,
so that I can inspect a downstream visual consequence of the semantic source without turning rendering into an independent authority.

## Acceptance Criteria

1. Given semantically valid `Engineering IR` from an M0 compilation run, when the rendering backend executes, then the compiler derives a renderer-facing model from `Engineering IR` before `SVG` emission, and the renderer-facing model contains only the information needed for rendering, not new semantic authority.
2. Given the same valid `Engineering IR` input, when the `SVG` renderer executes multiple times, then it emits a stable `SVG` output class consistent with the renderer-facing model, and the renderer does not infer, repair, or invent missing semantic meaning.
3. Given semantic invalidity or missing renderer prerequisites according to pipeline policy, when `SVG` emission is requested, then the compiler either blocks rendering or emits only the outputs explicitly allowed by policy, and any rendering-related diagnostic remains traceable back to upstream semantic or pipeline state.

## Tasks / Subtasks

- [x] Define the thin renderer-facing model and rendering result contracts without expanding `Engineering IR` into layout or geometry authority. (AC: 1, 2, 3)
  - [x] Keep semantic truth in `ir/`; place only downstream render-facing structures where the renderer can consume them.
  - [x] Include enough structure for deterministic `SVG` emission and renderer blocking diagnostics.
- [x] Implement compiler-owned derivation from semantically valid `Engineering IR` into the thin renderer-facing model. (AC: 1, 2, 3)
  - [x] Reuse the Story `1.5` pass pipeline and replace the placeholder downstream-derivation stage with real model derivation and `SVG` emission.
  - [x] Keep derivation deterministic and independent from AST reinterpretation.
- [x] Implement a simple `SVG` renderer over the renderer-facing model in `renderer-svg/`. (AC: 1, 2)
  - [x] Emit stable, minimal `SVG` for the current M0 electrical/runtime proof without recovering missing semantics.
  - [x] Keep renderer logic presentation-only; it may format and place already-derived objects but may not fix semantic defects.
- [x] Extend compiler-facing results and tests so valid runs surface derived render output and invalid runs surface explicit render blocking. (AC: 2, 3)
  - [x] Cover deterministic repeated rendering for the same valid input.
  - [x] Cover blocked rendering for semantic-invalid input under the current continuation policy.
- [x] Document the M0 render boundary, renderer-facing model scope, and SVG proof contract. (AC: 1, 2, 3)

## Dev Notes

### Story Intent

- Story `1.6` is the first downstream proof that canonical semantic compilation can produce a view artifact without moving authority into the renderer.
- The proof target is not a sophisticated drawing system. It is a thin, deterministic render-facing model plus simple `SVG` output that a reviewer can trace back to `Engineering IR`.
- Layout/geometry separation remains binding even though M0 still defers a durable `Layout IR`. This story may use a thin transient render-facing model, not a new canonical substrate.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority. The render-facing model is downstream and disposable.
- The compiler owns derivation from semantic truth into render-facing view data. The renderer owns only format-specific `SVG` emission.
- The renderer must not infer, repair, or invent missing semantic meaning. If semantic validity or prerequisites are insufficient, rendering must block or report only explicitly allowed downstream state.
- Preserve the architecture rule from `manifesto/docs/architecture/09-layout-and-geometry.md`: semantic meaning, layout/view intent, and exact render output remain conceptually distinct even if M0 uses a thin combined render-facing model for the first proof.
- Keep this JVM-first, local, and deterministic. No UI, browser, plugin discovery, or external rendering engines belong in Story `1.6`.

### Technical Requirements

- Expected implementation split:
  - `compiler/` owns orchestration and derivation from `Engineering IR` to the render-facing model
  - `renderer-svg/` owns the render-facing model contract and `SVG` emission surface, unless a thinner shared contract is clearly more coherent there
- Extend the Story `1.5` pipeline so the `DOWNSTREAM_DERIVATION` pass becomes a real derived-output stage rather than a placeholder status.
- If a new compile result field is needed, preserve the existing parse/lower/validate surfaces and extend `compile()` coherently rather than inventing a separate rendering API first.
- The render-facing model should be intentionally small and deterministic for M0. A valid first slice may include:
  - system label
  - renderable component boxes or symbols
  - stable labels
  - deterministic connector anchor points
  - renderable connection lines or paths
  - overall canvas dimensions
- Do not add layout, page, or geometry fields to `EngineeringDocument`.
- Rendering-related diagnostics or block reasons must stay traceable to upstream semantic or pipeline state, even if they are represented as compiler-facing render outcome metadata.
- A simple emitted `SVG` string or document contract is sufficient for M0. Avoid introducing third-party SVG libraries unless the current workspace truly requires one.

### Architecture Compliance

- Align to AD-3 by deriving rendering only from canonical `Engineering IR` and pipeline state.
- Align to AD-4 by keeping semantic truth upstream and `SVG` downstream of a compiler-owned render-facing model.
- Align to AD-7 by treating expected `SVG` output class as part of the conformance proof.
- Preserve the deferred decision that a full durable `Layout IR` is not required yet; the separation rule still governs the implementation.

### Library / Framework Requirements

- Stay on Java `25`, Kotlin `2.4.0`, Gradle `9.6.1`.
- Preserve the package root `com.engineeringood.athena`.
- Prefer standard Kotlin data classes and string builders for the first `SVG` proof.
- Reuse the existing Kotlin test setup; do not add a second test framework or a browser snapshot tool.

### File Structure Requirements

- Expected primary touch points:
  - `renderer-svg/src/main/kotlin/com/engineeringood/athena/renderer/svg/**`
  - `renderer-svg/src/test/kotlin/com/engineeringood/athena/renderer/svg/**`
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/**`
  - `compiler/src/test/kotlin/com/engineeringood/athena/compiler/**`
  - `docs/compiler/**`
  - `examples/m0/**` for stable proof artifacts if needed
- `ir/` should not gain renderer-facing state.
- `language/` and `semantics-core/` should remain unchanged unless a direct typed seam is missing.

### Testing Requirements

- Minimum verification should include sequential Java `25` runs:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :renderer-svg:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Add tests that prove:
  - valid compilation derives a renderer-facing model before `SVG` emission
  - repeated valid rendering is deterministic
  - invalid semantic state blocks rendering under current policy
  - emitted `SVG` stays stable for at least one published conformance example
- Keep Windows verification sequential only; do not run Gradle verification commands in parallel in this repository.

### Previous Story Intelligence

- Story `1.5` is complete and established:
  - explicit pass schedule and inspectable pipeline report
  - `DOWNSTREAM_DERIVATION` as a declared stage
  - deterministic continuation gating from semantic validation
- Story `1.6` should replace the placeholder downstream summary with real derived render results, not add a second downstream pipeline outside `compile()`.
- The current `renderer-svg/` module is marker-only, so Story `1.6` can add the first real renderer contract without legacy cleanup.

### Git Intelligence Summary

- Current committed history is still minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Implementation guidance should come from the working tree and completed story artifacts.

### Project Structure Notes

- No UX artifact exists for this phase. Keep the work compiler-first and proof-oriented.
- M0 still defers full durable `Layout IR` and `Geometry IR`; do not accidentally smuggle those concerns back into `Engineering IR`.
- A thin render-facing model is acceptable now because the architecture spine explicitly allows it before a first-class durable layout artifact becomes necessary.

### References

- `_bmad-output/planning-artifacts/epics.md` - Epic 1, Story `1.6` acceptance criteria and FR mapping.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - FR-6, FR-12, FR-13, downstream output, and replaceable surface constraints.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-3, AD-4, AD-7, renderer boundary, and deferred layout note.
- `_bmad-output/specs/spec-athena/SPEC.md` - CAP-3, CAP-4, constraints, and success signal.
- `_bmad-output/implementation-artifacts/m0/1-5-execute-the-m0-compiler-as-declared-deterministic-passes.md` - current pass pipeline and downstream placeholder boundary.
- `manifesto/docs/architecture/01-compiler.md` - renderer as downstream compiler product.
- `manifesto/docs/architecture/03-ir.md` - semantic authority and explicit derived forms.
- `manifesto/docs/architecture/09-layout-and-geometry.md` - semantic/layout/geometry separation boundary.
- `draft/0002.md` - M0 proof target that the IR renders to a simple `SVG`.

## Story Completion Status

- Status: done
- Completion note: Thin render-model derivation, simple SVG emission, render blocking, and proof artifacts are implemented and verified.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Sprint status identified `1-6-derive-a-render-model-and-emit-simple-svg-from-engineering-ir` as the next backlog story after Story `1.5`.
- No `project-context.md` file was present in the repository.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- Architecture and manifesto review consistently bound `SVG` to a downstream backend and kept `Engineering IR` / layout / geometry separation binding for M0.
- Current code inspection confirmed `renderer-svg/` is still marker-only and Story `1.5` leaves a placeholder downstream derivation stage ready to be replaced.
- Red-phase tests were added first for renderer-model contracts, deterministic SVG emission, blocked invalid rendering, and published SVG conformance output.
- Fresh verification evidence:
  - `java25; .\gradlew.bat --no-daemon --console=plain :compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain :renderer-svg:test` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain build` -> `BUILD SUCCESSFUL`
  - `java25; .\gradlew.bat --no-daemon --console=plain test` -> `BUILD SUCCESSFUL`

### Completion Notes List

- Created the concrete Story `1.6` developer guide for render-facing model derivation, simple `SVG` emission, and downstream authority boundaries.
- Added the first real downstream rendering contracts: `CompilerRenderingResult`, `CompilerRenderingSuccess`, `CompilerRenderingBlocked`, `SvgRenderModel`, `SvgRenderBox`, `SvgRenderConnection`, and `SvgRenderer`.
- Replaced the Story `1.5` downstream placeholder with real compiler-owned render-model derivation and `SVG` emission while preserving semantic gate blocking.
- Added deterministic renderer and compiler regression coverage for valid rendering, blocked invalid rendering, repeated output stability, and the published `examples/m0/demo-cabinet.svg` conformance artifact.
- Documented the render boundary in `docs/compiler/m0-render-boundary.md` and updated the pass-pipeline document now that downstream derivation emits `SVG`.

### File List

- `_bmad-output/implementation-artifacts/m0/1-6-derive-a-render-model-and-emit-simple-svg-from-engineering-ir.md`
- `_bmad-output/implementation-artifacts/m0/sprint-status.yaml`
- `compiler/build.gradle.kts`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerRenderingModel.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `docs/compiler/m0-pass-pipeline.md`
- `docs/compiler/m0-render-boundary.md`
- `docs/superpowers/plans/2026-07-02-svg-render-proof.md`
- `examples/m0/demo-cabinet.svg`
- `renderer-svg/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
- `renderer-svg/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderer.kt`
- `renderer-svg/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`

### Change Log

- 2026-07-02: Implemented Story `1.6` thin render-model derivation, simple SVG emission, render blocking, deterministic tests, published SVG proof artifact, and render-boundary documentation.
