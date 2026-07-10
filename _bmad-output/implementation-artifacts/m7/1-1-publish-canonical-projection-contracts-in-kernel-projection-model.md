---
baseline_commit: adb0ae5c527b47d2d0e38cfcaf4d99242d49f2f5
---

# Story 1.1: Publish Canonical Projection Contracts In `kernel/projection-model`

Status: review

## Story

As a platform engineer,
I want Athena to define typed projection contracts in a dedicated kernel boundary,
so that graphical delivery consumes one renderer-neutral projection model instead of raw engineering entities or renderer-specific shapes.

## FR Traceability

- FR-1: stable projection boundary for graphical workbench delivery
- FR-2: keep graphical projection downstream of semantic authority
- FR-6: deterministic graphical refresh from the same underlying semantic state
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same upstream semantic state and chosen view yield the same projection state
- NFR-3: projection output remains inspectable
- NFR-7: renderer assets remain downstream of the engineering object graph

## Acceptance Criteria

1. Given the completed M2 layout and geometry layers and the M7 architecture spine, when M7 begins implementation, then Athena introduces a dedicated `kernel/projection-model` boundary for projection-facing view definitions, projection elements, stable references, and inspectable projection payloads, and those contracts remain renderer-neutral rather than tied to Theia, GLSP, or any specific canvas library.
2. Given projection contracts are reviewed against existing kernel ownership, when dependency direction is checked, then `projection-model` consumes engineering, layout, and geometry meaning without redefining semantic identity, and view definitions plus renderer assets remain downstream projection concerns rather than semantic roots.

## Tasks / Subtasks

- [x] Create the physical `:kernel:projection-model` module and register it consistently. (AC: 1, 2)
  - [x] Add `kernel/projection-model/` with `build.gradle.kts`, `README.md`, and `README.zh-CN.md`.
  - [x] Add `:kernel:projection-model` to [`settings.gradle.kts`](../../../settings.gradle.kts) as a normal sibling kernel module near `engineering-model`, `layout-model`, and `geometry-model`.
  - [x] Add a simple module marker plus focused marker test, following the existing kernel model-module pattern used by `:kernel:layout-model` and `:kernel:geometry-model`.
- [x] Publish the canonical projection-model contract surface in the new module. (AC: 1, 2)
  - [x] Define a deliberately narrow typed Kotlin contract surface for the first projection boundary, such as:
    - a root projection document/state type
    - one typed projection view descriptor or equivalent view-facing envelope
    - projection element / relationship / grouping identifiers or equivalent stable renderer-neutral projection ids
    - projection-facing references back to canonical semantic identity
    - optional renderer-asset references that remain explicitly downstream metadata
  - [x] Reuse existing upstream kernel types where appropriate instead of duplicating them:
    - `StableSemanticIdentity` from `:kernel:engineering-model`
    - `ViewDefinition` and other layout-owned concepts from `:kernel:layout-model`
    - `GeometryDocument` / `GeometryElement` style geometry contracts from `:kernel:geometry-model`
  - [x] Keep the Kotlin package root under `com.engineeringood.athena.projection`, matching the simple kernel noun style rather than `projectionmodel`, `graph`, `glsp`, `theia`, or renderer-specific packages.
  - [x] Add clean KDoc to all public/core Kotlin classes in the new module.
  - [x] Keep the contract surface intentionally narrow to the boundary publication story; do not add runtime orchestration, LSP transport, graph-framework protocol types, or renderer execution logic yet.
- [x] Keep the module dependency-light and architecture-safe. (AC: 1, 2)
  - [x] Prefer only the minimal project dependencies required by the typed contract surface:
    - `:kernel:engineering-model`
    - `:kernel:layout-model`
    - `:kernel:geometry-model`
  - [x] Do not make `:kernel:projection-model` depend on `:kernel:runtime`, `:kernel:compiler`, `:ide:lsp`, `:integrations:scm-git`, or frontend modules in Story `1.1`.
  - [x] Do not implement projection builders, runtime session refresh logic, typed query transport, graph adapters, renderer assets packs, or inspect-first command routing in this story.
- [x] Update workspace maps and module documentation so the repo tells the same story everywhere. (AC: 1, 2)
  - [x] Update [`kernel/README.md`](../../../kernel/README.md) and [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md) to include `:kernel:projection-model`.
  - [x] Update [`README.md`](../../../README.md) and [`README.zh-CN.md`](../../../README.zh-CN.md) so the module graph reflects the new M7 projection boundary.
  - [x] Update [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) so the active workspace shape includes the new projection boundary above layout and geometry.
  - [x] Keep documentation explicit that `:kernel:projection-model` is Athena-owned, renderer-neutral, and sits between canonical semantic truth and later graph delivery paths.
- [x] Verify the boundary with focused and regression-safe tests. (AC: 1, 2)
  - [x] Add focused tests in `kernel/projection-model/src/test/kotlin/...` for the module marker and any small contract invariants introduced.
  - [x] Verify that the new module builds cleanly under Java 25 and that adjacent model/runtime boundaries still pass focused regression.
  - [x] Run Gradle verification sequentially on Windows with Java 25; do not run build or test tasks concurrently.

## Dev Notes

### Story Intent

- This is the contract-publication story for M7, not the workbench or renderer story.
- The point is to freeze the canonical `projection-model` nouns and ownership before runtime session refresh, LSP transport, graph adapters, workbench panels, or first-renderer behavior begin.
- The safest implementation is a small, typed, JVM-first kernel module that expresses projection-facing meaning without pulling in Theia, GLSP, or renderer-framework mechanics.
- Story `1.2` owns deterministic projection-state materialization from engineering, layout, and geometry inputs.
- Story `1.3` owns runtime-owned `ProjectionSession` state, invalidation, and deterministic refresh.
- Story `1.4` owns typed projection queries and governed commands through `ide/lsp`.
- Epic `2.x` owns graph adapter placement and the first real workbench surface.
- Epic `3.x` owns the first relationship-forward renderer proof, electrical projection contributions, and technology-path validation.

### Architecture Guardrails

- Align to AD-27 by creating a dedicated `projection-model` kernel boundary above layout and geometry rather than letting graphical clients read raw engineering entities directly. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-27]
- Align to AD-28 by keeping engineering identity in the object graph while view definitions and renderer assets remain downstream projection concerns. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-28]
- Align to AD-29 by keeping layout and geometry view-scoped metadata rather than engineering truth; Story `1.1` publishes the contracts only and must not widen into refresh behavior implementation. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-29]
- Align to AD-30 by keeping Athena-owned transport and graph adapters out of this story; the module must remain framework-neutral so later LSP and adapter work consumes it from above. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by not embedding inspect-first interaction or command semantics inside the module; those are later runtime/LSP concerns. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Align to AD-32 by ensuring the contract surface can support a relationship-forward renderer proof without depending on notation-specific asset packs. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-32]
- Align to AD-33 by keeping domain projection contributions in `extensions/domain-*`; Story `1.1` must not hard-code electrical view logic in the kernel boundary. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-33]
- Preserve the inherited M4/M5/M6 rule that `ide/lsp` remains the sole IDE semantic/projection entry point and runtime remains session authority. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- `:kernel:projection-model` should be a typed contract module, not a hidden service/orchestration module.
- Use Athena-native, renderer-neutral language only. Do not introduce public types or fields shaped around `GLSP`, `GModel`, `Sprotty`, `Theia`, `Monaco`, `Widget`, `DiagramServer`, or canvas-library DTOs in this story.
- Reuse existing kernel-owned types where they already express upstream truth:
  - `StableSemanticIdentity` from `:kernel:engineering-model`
  - `ViewDefinition` from `:kernel:layout-model`
  - `LayoutDocument` / `LayoutNode` / `LayoutRelationship` when projection contracts need to point back to layout-owned intent
  - `GeometryDocument` / `GeometryElement` when projection contracts need to point to downstream geometry-owned structure
- Do not clone `ViewDefinition` into `projection-model` unless there is a genuine boundary reason and the story explicitly preserves one canonical owner for the concept.
- Prefer immutable `data class`, `enum class`, `sealed interface`, and inline-id types consistent with the existing model modules.
- Add KDoc for all public/core Kotlin classes because the user explicitly requires clean KDoc on core Kotlin surfaces.
- Do not put filesystem IO, repository activation, runtime session switching, LSP methods, graph-adapter protocol code, frontend DTOs, or renderer execution code into `:kernel:projection-model`.

### Architecture Compliance

- The story is only successful if the M7 projection boundary becomes easier to point to:
  - one physical module
  - one canonical package namespace
  - one documented ownership story
- Prevent these failure modes:
  - `projection-model` types are added under `com.engineeringood.athena.runtime.*`
  - graph-framework nouns leak into the public projection contract surface
  - `ViewDefinition` is duplicated casually so the codebase has two practical owners for one concept
  - runtime-owned `AthenaRuntimeProjectionSession` is moved into the new module in Story `1.1`
  - renderer asset references become mandatory semantic identity instead of optional downstream metadata
  - Story `1.1` widens into projection building, transport, adapter, or workbench implementation

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Follow the existing sibling-module Gradle pattern:
  - `plugins { alias(libs.plugins.kotlinJvm) }`
- Reuse the existing Kotlin/JUnit test conventions from sibling kernel model modules.
- No third-party dependency should be added just to publish these typed projection contracts.
- GLSP stays a later implementation-path option, not a dependency or public API in Story `1.1`. [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md#FR-8]

### File Structure Requirements

- Expected new files:
  - `kernel/projection-model/build.gradle.kts`
  - `kernel/projection-model/README.md`
  - `kernel/projection-model/README.zh-CN.md`
  - `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/ProjectionModelMarker.kt`
  - one or more contract files under `kernel/projection-model/src/main/kotlin/com/engineeringood/athena/projection/`
  - `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/ProjectionModelMarkerTest.kt`
  - focused contract tests under `kernel/projection-model/src/test/kotlin/com/engineeringood/athena/projection/`
- Expected update files:
  - [`settings.gradle.kts`](../../../settings.gradle.kts)
  - [`kernel/README.md`](../../../kernel/README.md)
  - [`kernel/README.zh-CN.md`](../../../kernel/README.zh-CN.md)
  - [`README.md`](../../../README.md)
  - [`README.zh-CN.md`](../../../README.zh-CN.md)
  - [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md)
- Files whose current behavior and ownership must be preserved:
  - [`kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`](../../../kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt)
    - `ViewDefinition` is currently the canonical layout-owned typed definition of one supported projection context and should not be duplicated casually.
  - [`kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`](../../../kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt)
    - `GeometryDocument` and `GeometryElement` remain the explicit geometry boundary downstream of layout intent.
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
    - runtime currently owns the active projection session entrypoints and must remain the session authority.
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
    - runtime currently owns supported-view switching and geometry-backed projection snapshots; Story `1.1` must not relocate that orchestration into the new module.
  - [`ide/lsp/README.md`](../../../ide/lsp/README.md)
    - `ide/lsp` remains the future transport owner and should not be bypassed by this story.
- Explicit non-goals:
  - no runtime projection refresh implementation
  - no `ProjectionSession` implementation
  - no LSP methods
  - no graph adapter under `integrations/graph-*`
  - no Theia panels
  - no renderer proof
  - no notation-specific asset-pack implementation

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"`
- Recommended focused regression after the module exists:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:geometry-model:test"`
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Optional wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - `:kernel:projection-model` is a real physical module, not only a plan artifact
  - the module package root is `com.engineeringood.athena.projection`
  - the public contract surface stays renderer-neutral and typed
  - `ViewDefinition` remains a clear layout-owned concept unless a deliberate, documented refactor is made
  - runtime remains the owner of `AthenaRuntimeProjectionSession`
  - docs consistently describe projection-model as the M7 boundary above layout and geometry and below later transport/adapters
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`settings.gradle.kts`](../../../settings.gradle.kts) currently includes `:kernel:semantic-scm`, `:kernel:engineering-model`, `:kernel:layout-model`, and `:kernel:geometry-model`, but does not yet include `:kernel:projection-model`.
- [`kernel/repository-model/build.gradle.kts`](../../../kernel/repository-model/build.gradle.kts) shows the lightest current model-module Gradle pattern.
- [`kernel/layout-model/build.gradle.kts`](../../../kernel/layout-model/build.gradle.kts) and [`kernel/geometry-model/build.gradle.kts`](../../../kernel/geometry-model/build.gradle.kts) show the current projection-layer dependency pattern: each stays small and depends only on `:kernel:engineering-model`.
- [`kernel/runtime/build.gradle.kts`](../../../kernel/runtime/build.gradle.kts) currently depends on compiler, repository-model, semantic-scm, engineering-model, layout-model, geometry-model, and renderer modules; `projection-model` should not create reverse cycles into runtime.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt) currently exposes `projectProjectionSession()` and `switchActiveProjectionView()` as runtime-owned entry points.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently builds supported views from compiler-owned `ViewDefinition`s and turns `GeometryDocument` into viewer scenes. Story `1.1` must not move this behavior yet; it only prepares the canonical boundary that later stories can consume.
- [`kernel/README.md`](../../../kernel/README.md), [`README.md`](../../../README.md), and [`docs/usages/athena-workspace-summary.md`](../../../docs/usages/athena-workspace-summary.md) currently document the kernel and workspace without `:kernel:projection-model`.

### Previous Milestone Intelligence

- M2 created `:kernel:layout-model` and `:kernel:geometry-model` as explicit downstream boundaries, which is exactly the layer this story must build on rather than collapse back into runtime or renderer DTOs.
- M4 and M6 locked the IDE rule that `ide/lsp` is the sole semantic-service bridge for the product path; Story `1.1` must keep transport out of the new module.
- The M7 renderer note explicitly says Athena semantic authority is an engineering object graph, not a flattened device-symbol model, and renderer packs are downstream asset bundles rather than domain truth. [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md#2.1-Renderer-Framing-From-The-EPLAN-Cross-Compare-Note]
- The user has repeatedly enforced four workspace rules that matter directly here:
  - physical module structure must match intended architecture
  - root package is `com.engineeringood`
  - every module keeps English and Chinese README coverage
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
  - keep new kernel boundaries small, explicit, and documented
  - do not smuggle M7 projection semantics into runtime, IDE, or integration packages before the dedicated boundary exists

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M7 architecture and root build documentation:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Node `22+` and Eclipse Theia `1.73.1` remain relevant to later stories, but Story `1.1` should not depend on Node/Theia-side code.
- GLSP remains a referenced later implementation path, not a `1.1` dependency or public API commitment.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- The new module should sit alongside the existing kernel semantic/model boundaries:
  - `kernel/engineering-model`
  - `kernel/layout-model`
  - `kernel/geometry-model`
  - `kernel/semantic-scm`
- The naming should stay easy to understand:
  - module: `projection-model`
  - Gradle path: `:kernel:projection-model`
  - Kotlin package root: `com.engineeringood.athena.projection`
- The story should reduce future renaming churn by freezing the projection boundary nouns now, but it should not widen into transport, adapter, or renderer semantics before the later stories that own them.

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/sprint-status.yaml]
- [Source: settings.gradle.kts]
- [Source: README.md]
- [Source: README.zh-CN.md]
- [Source: kernel/README.md]
- [Source: kernel/README.zh-CN.md]
- [Source: ide/lsp/README.md]
- [Source: kernel/layout-model/README.md]
- [Source: kernel/geometry-model/README.md]
- [Source: docs/usages/athena-workspace-summary.md]
- [Source: kernel/repository-model/build.gradle.kts]
- [Source: kernel/layout-model/build.gradle.kts]
- [Source: kernel/geometry-model/build.gradle.kts]
- [Source: kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt]
- [Source: kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt]
- [Source: kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt]
- [Source: kernel/runtime/build.gradle.kts]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelMarkerTest.kt]
- [Source: kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/GeometryModelMarkerTest.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt]
- [Source: _bmad-output/implementation-artifacts/m6/1-1-publish-canonical-semantic-scm-contracts-in-kernel-semantic-scm.md]

## Story Completion Status

- Status: review
- Completion note: Ultimate context engine analysis completed - comprehensive developer guide created for the M7 projection-boundary entry story.

## Dev Agent Record

### Agent Model Used

Story context generation only.

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- prior M6 `1.1` story structure review for analogous boundary-publication patterns
- CodeGraph exploration of runtime projection-session symbols, layout/geometry model markers, and current Gradle/module topology
- live build-file, README, runtime, layout, geometry, and workspace-summary review
- recent commit history review

### Completion Notes List

- Identified Story `1.1` as the first backlog story in M7 and the Epic 1 entry point.
- Locked the new boundary to a physical `:kernel:projection-model` module with renderer-neutral typed contracts only.
- Flagged `ViewDefinition` duplication risk and explicitly constrained Story `1.1` to preserve one clear owner for layout-owned view concepts.
- Flagged runtime/IDE cycle risk and explicitly constrained Story `1.1` to stay dependency-light and behavior-free.
- Captured the current runtime projection-session files whose ownership must remain intact while the new boundary is introduced.
- Advanced the milestone tracker to `epic-1: in-progress` and `story 1.1: ready-for-dev`.

### File List

- _bmad-output/implementation-artifacts/m7/1-1-publish-canonical-projection-contracts-in-kernel-projection-model.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
