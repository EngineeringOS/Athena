---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 1.2: Materialize Deterministic Projection State From Engineering, Layout, And Geometry Inputs

Status: review

## Story

As an engineer,
I want Athena to derive projection state deterministically from canonical inputs,
so that the same upstream semantic state always yields the same graphical projection state.

## FR Traceability

- FR-1: stable projection boundary for graphical workbench delivery
- FR-2: keep graphical projection downstream of semantic authority
- FR-6: deterministic graphical refresh from the same underlying semantic state
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same upstream semantic state and chosen view yield the same projection state
- NFR-3: projection output remains inspectable
- NFR-7: renderer assets remain downstream of the engineering object graph

## Acceptance Criteria

1. Given canonical engineering identities, relationships, layout metadata, and geometry metadata, when Athena builds a projection model, then it produces the same projection output for the same upstream inputs, and projection payloads remain inspectable for development and architecture debugging.
2. Given renderer-facing structure is reviewed, when model ownership is checked, then projection elements carry stable canonical references plus view-scoped projection identifiers, and renderer asset references stay separate from engineering identity.

## Tasks / Subtasks

- [x] Materialize projection-model documents in the compiler-owned derivation path rather than in runtime or UI code. (AC: 1, 2)
  - [x] Treat Story `1.1` as a hard prerequisite. If `:kernel:projection-model` and its canonical contracts are not present yet, complete Story `1.1` first instead of inventing temporary local types in compiler or runtime.
  - [x] Add one compiler-owned derivation/materialization seam, such as `ProjectionModelDeriver`, under `:kernel:compiler` to build projection-model documents from canonical engineering plus derived layout and geometry inputs.
  - [x] Keep projection-state materialization deterministic and compiler-owned; do not let runtime, `ide/lsp`, Theia, or any graph adapter derive projection-model payloads privately.
- [x] Define the first deterministic mapping from existing model layers into `projection-model`. (AC: 1, 2)
  - [x] Build projection documents in supported view order from the same canonical inputs already exposed by `AthenaCompiler`:
    - `EngineeringDocument`
    - `LayoutDocument`
    - `GeometryDocument`
    - supported `ViewDefinition`
  - [x] Ensure each emitted projection element or relationship preserves:
    - canonical semantic identity
    - view-scoped projection identity
    - inspectable references back to layout and/or geometry-owned structure where useful
  - [x] Keep renderer asset metadata explicitly downstream and optional; do not turn renderer mappings into semantic identity or hard-code electrical renderer logic into the kernel boundary in Story `1.2`.
- [x] Expose inspectable projection output through compiler-facing result models. (AC: 1, 2)
  - [x] Extend `CompilerCompilationSuccess` and any focused compiler APIs with the new projection-model output in a way that preserves current inspectable pipeline behavior.
  - [x] Keep current `layouts`, `geometries`, and `rendering` results intact for regression safety; projection-model is added as the new boundary above them, not as a destructive replacement in Story `1.2`.
  - [x] If a focused compiler helper is added for one-view derivation, keep it typed and deterministic rather than exposing raw maps or renderer-specific DTOs.
- [x] Preserve ownership boundaries for runtime, extensions, and later M7 stories. (AC: 1, 2)
  - [x] Do not move active `ProjectionSession` ownership into compiler; Story `1.3` owns runtime-hosted projection sessions, invalidation, and refresh.
  - [x] Do not add typed projection queries, governed command transport, or IDE protocol DTOs; Story `1.4` owns that boundary.
  - [x] Do not add graph-framework, GLSP, Theia, or canvas-library public types to projection-model or compiler output.
  - [x] Reuse existing extension-contributed `ViewDefinition` inputs and current approved-plugin ordering instead of creating a second view-registration path.
- [x] Cover the projection-model derivation with deterministic tests and documentation. (AC: 1, 2)
  - [x] Add focused `:kernel:projection-model` tests for any new contract invariants introduced by materialization.
  - [x] Extend compiler tests to prove repeated compilation yields identical projection-model output for the same source and supported views.
  - [x] Keep runtime projection-session tests green without migrating runtime session ownership to the new projection-model output yet.
  - [x] Update module documentation where the public derivation story changes, at minimum `kernel/compiler/README.md` and the new `kernel/projection-model/README.md`, with Chinese companions if those files are changed.

## Dev Notes

### Story Intent

- Story `1.2` is the first implementation of the M7 projection boundary as a real derived artifact, not just a contract shell.
- The success condition is not "the graphical workbench works already." It is "the compiler can materialize one deterministic, inspectable projection-model output from canonical engineering, layout, and geometry inputs."
- Story `1.2` must stop before runtime-owned projection sessions, IDE protocol transport, graph adapters, or renderer-proof work.
- Story `1.3` owns runtime-owned `ProjectionSession` state, invalidation, and refresh.
- Story `1.4` owns typed projection queries and governed commands through `ide/lsp`.
- Epic `2.x` owns graph adapter placement and the first real graphical workbench surface.
- Epic `3.x` owns the first relationship-forward renderer proof, electrical projection contributions, and technology-path validation.

### Architecture Guardrails

- Align to AD-27 by materializing one renderer-neutral `ProjectionModel` boundary above layout and geometry rather than letting downstream graphical clients assemble incompatible shapes privately. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-27]
- Align to AD-28 by keeping engineering identity in the object graph while view definitions and renderer assets remain downstream projection concerns. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-28]
- Align to AD-29 by treating layout and geometry as view-scoped metadata inputs into projection-model materialization, not as engineering truth. Story `1.2` must materialize deterministic projection state; Story `1.3` will own refresh and invalidation behavior over that state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-29]
- Align to AD-30 by keeping Athena-owned transport and graph adapters out of this story. The output must be a compiler/runtime-consumable kernel model, not a protocol payload tied to Theia, GLSP, or any graph framework. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by not embedding inspect-first interaction, local widget state, or command routing inside projection-model materialization. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Align to AD-32 by ensuring the first projection-model output can support relationship-forward rendering over canonical identities without depending on notation-specific asset packs. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-32]
- Align to AD-33 by keeping domain-specific renderer mappings and richer projection contributions in `extensions/domain-*` rather than hard-coding them into kernel contracts in this story. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-33]
- Preserve the inherited M4/M5/M6 rules that runtime remains session authority, `ide/lsp` remains the sole IDE semantic/projection entry point, and frontend state remains disposable. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Materialize projection state from the compiler-owned canonical inputs already proven in the repo:
  - `EngineeringDocument`
  - `LayoutDocument`
  - `GeometryDocument`
  - supported `ViewDefinition`
- Reuse the existing deterministic supported-view order from `AthenaCompiler.supportedViewDefinitions()` instead of creating a second ordering mechanism.
- Reuse existing derived `layouts` and `geometries` from compiler passes rather than re-deriving layout or geometry inside projection-model materialization.
- Keep the public result inspectable and typed. The output should make it easy to answer:
  - which view this projection belongs to
  - which canonical semantic identities are represented
  - which projection-local identifiers are stable for that view
  - which layout or geometry structures a projection item came from
  - which downstream asset references, if any, are attached separately from semantic identity
- Keep renderer asset references optional and explicitly downstream metadata. Story `1.2` must not require notation packs, IEC symbols, or electrical renderer mappings to exist before the projection-model document is valid.
- Add clean KDoc to all new public/core Kotlin classes in compiler and `projection-model`.
- Prefer immutable Kotlin types consistent with the rest of the kernel model stack.

### Architecture Compliance

- The story is only successful if one canonical derivation path becomes obvious:
  - compiler derives `LayoutDocument`
  - compiler derives `GeometryDocument`
  - compiler materializes `ProjectionModel`
  - runtime consumes `ProjectionModel` later
- Prevent these failure modes:
  - runtime or UI privately derives projection-model payloads from `GeometryDocument`
  - `ProjectionModel` duplicates semantic identity instead of referencing it
  - renderer asset references become required semantic owners
  - the compiler re-queries plugin view definitions in a second unordered path
  - Story `1.2` widens into runtime-session refresh, IDE transport, or graph adapter behavior

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Extend the current sibling-module Gradle pattern only as needed:
  - `:kernel:compiler` should depend on `:kernel:projection-model` once Story `1.1` is landed
- Reuse the current Kotlin/JUnit test conventions from sibling kernel model and compiler modules.
- No third-party dependency should be added just to materialize deterministic projection-model output.
- GLSP stays a referenced later implementation-path option, not a dependency or public API in Story `1.2`. [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md#11-Open-Questions]

### File Structure Requirements

- Expected update files:
  - `kernel/compiler/build.gradle.kts`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
  - `kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
  - `kernel/compiler/README.md`
  - `kernel/compiler/README.zh-CN.md`
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/*.kt`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/*.kt`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
- Likely add files:
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/ProjectionModelDeriver.kt`
- Files whose current behavior and ownership must be preserved:
  - [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt)
    - currently owns deterministic supported-view discovery, layout derivation, geometry derivation, and backend preparation
  - [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt)
    - currently exposes `layouts`, `geometries`, and `rendering` on `CompilerCompilationSuccess`
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
    - runtime currently builds projection sessions from compiler-supported views and geometry-backed snapshots; Story `1.2` must not relocate session ownership or switch runtime to a new protocol yet
  - [`extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`](../../../extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt)
    - currently owns the first proof `ViewDefinition` pair and renderer-intent contributions; Story `1.2` must reuse those inputs without promoting them to kernel semantic authority
- Explicit non-goals:
  - no runtime `ProjectionSession` invalidation/refresh implementation
  - no LSP methods or transport DTOs
  - no graph adapter under `integrations/graph-*`
  - no Theia panels
  - no renderer-proof behavior
  - no domain-specific renderer mapping rollout

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"`
- Recommended focused regression after the compiler materialization path is green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Optional wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - repeated compilation for the same source yields identical projection-model output
  - projection-model output is emitted in supported-view order and remains inspectable
  - every projection element/relationship keeps canonical semantic identity separate from projection-local id
  - projection-model output does not require renderer asset mappings to be semantic-valid
  - current runtime projection-session tests stay green without moving session ownership out of runtime
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt) currently:
  - caches supported view definitions from approved plugins in deterministic order
  - derives layouts from `EngineeringDocument`
  - derives geometries from `LayoutDocument`
  - exposes those artifacts on `CompilerCompilationSuccess`
- [`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`](../../../kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt) currently makes `layouts`, `geometries`, and `rendering` part of the public inspectable compiler success shape.
- [`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`](../../../kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt) already encodes deterministic expected layouts, geometries, supported view ids, and render-contribution selection for the proof electrical example; extend this fixture style instead of inventing a separate deterministic corpus.
- [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt) already verifies runtime-owned supported views, active-view switching, and geometry-backed snapshots; Story `1.2` must keep these behaviors green and defer runtime projection-model adoption to Story `1.3`.
- [`kernel/compiler/README.md`](../../../kernel/compiler/README.md) currently documents compiler ownership through layout, geometry, and SVG backend preparation but does not yet describe projection-model materialization.

### Previous Story Intelligence

- Story `1.1` freezes the canonical `:kernel:projection-model` contracts and module ownership. Story `1.2` must consume that boundary, not redefine it locally.
- M2 already proved the correct derivation pattern:
  - `Engineering IR -> Layout IR`
  - `Layout IR -> Geometry IR`
  - runtime consumes those results later
- M7 now adds:
  - `Engineering + Layout + Geometry -> ProjectionModel`
  - runtime session ownership still later
- The user has repeatedly enforced four workspace rules that matter directly here:
  - physical module structure must match intended architecture
  - root package is `com.engineeringood`
  - every affected module keeps English and Chinese README coverage
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Recent milestone baseline:
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
- Practical implication:
  - follow the same grouped-module discipline used in M2, M3, M5, and M6
  - keep the new projection-model path small, typed, and documented
  - add the new compiler-facing derivation path without smuggling runtime, IDE, or integration behavior into the kernel boundary

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M7 architecture and root build documentation:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Node, Theia, and graph-framework choices remain relevant to later stories, but Story `1.2` should stay JVM-first and kernel/compiler-boundary focused.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- This story sits at the seam between:
  - `kernel/projection-model`
  - `kernel/compiler`
  - later `kernel/runtime`
- Naming should stay easy to understand and avoid renderer-framework leakage:
  - module: `projection-model`
  - package root: `com.engineeringood.athena.projection`
  - compiler helper: `ProjectionModelDeriver` or equivalently simple Athena-owned noun
- The story should make the future M7 path easier to explain:
  - projection-model is materialized once in compiler
  - runtime hosts it later
  - IDE transport exposes it later
  - graph adapters translate it later

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m7/1-1-publish-canonical-projection-contracts-in-kernel-projection-model.md]
- [Source: _bmad-output/implementation-artifacts/m2/1-3-derive-layout-ir-for-cabinet-and-wiring-views.md]
- [Source: _bmad-output/implementation-artifacts/m2/1-4-derive-geometry-ir-and-preserve-canonical-identity-across-projection-layers.md]
- [Source: _bmad-output/implementation-artifacts/m2/2-2-expose-runtime-owned-projection-snapshots-to-the-desktop-surface.md]
- [Source: kernel/compiler/build.gradle.kts]
- [Source: kernel/compiler/README.md]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt]
- [Source: kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt]
- [Source: kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt]
- [Source: extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt]

## Story Completion Status

- Status: review
- Completion note: Ultimate context engine analysis completed - comprehensive developer guide created for deterministic M7 projection-model materialization.

## Dev Agent Record

### Agent Model Used

Story context generation only.

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- prior M7 `1.1` story review for projection-boundary prerequisites
- M2 layout/geometry and runtime projection story review for derivation-pattern precedent
- CodeGraph exploration of compiler, runtime projection-session, and electrical plugin seams
- live compiler/runtime/model/test/README review
- recent commit history review

### Completion Notes List

- Identified Story `1.2` as the next backlog story in M7 after `1.1`.
- Locked deterministic projection-model materialization to the compiler-owned path over existing engineering, layout, and geometry inputs.
- Explicitly constrained Story `1.2` to preserve runtime session ownership for Story `1.3` and IDE transport ownership for Story `1.4`.
- Flagged the existing compiler fixture corpus and runtime projection-session tests that must remain the regression baseline.
- Captured the current electrical extension seam as a reusable input source, not a kernel-owned renderer-authority path.

### File List

- _bmad-output/implementation-artifacts/m7/1-2-materialize-deterministic-projection-state-from-engineering-layout-and-geometry-inputs.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
