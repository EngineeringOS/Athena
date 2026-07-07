---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 1.1: Establish Explicit Layout And Geometry Model Modules

Status: done

## Story

As a platform engineer,
I want durable `Layout IR` and `Geometry IR` contracts in dedicated kernel modules,
so that projection state is owned by governed kernel boundaries instead of being hidden in UI or renderer code.

## Acceptance Criteria

1. Given the current brownfield module layout and the approved M2 architecture, when M2 projection contracts are introduced, then Athena adds `:kernel:layout-model` and `:kernel:geometry-model` as dedicated modules for view and geometry contracts, and projection contract types are no longer implied only through viewer-local or renderer-local structures.
2. Given the new projection modules exist, when core projection contracts are defined, then Athena provides explicit types for `ViewDefinition`, `Layout IR`, and `Geometry IR`, and those types are documented as downstream of `Engineering IR` rather than as new semantic authorities.
3. Given projection contracts must remain aligned with the canonical model, when layout and geometry contract types are authored, then they include stable semantic identity references as first-class fields, and projection-local identifiers do not replace canonical semantic identity.
4. Given the new modules and contracts are added, when the standard Java `25` build and test checks are executed, then the workspace builds successfully with the new kernel modules in place, and version and plugin declarations continue to be sourced through `gradle/libs.versions.toml`.

## Tasks / Subtasks

- [x] Register the new kernel modules in the Gradle workspace. (AC: 1, 4)
  - [x] Add `:kernel:layout-model` and `:kernel:geometry-model` to `settings.gradle.kts` without disturbing the existing grouped topology.
  - [x] Create `build.gradle.kts` files for both modules using the same lightweight Kotlin/JUnit setup already used by other kernel modules.
  - [x] Add English and Chinese module READMEs and marker-class smoke tests for both modules.
- [x] Publish the first durable projection contracts under kernel ownership. (AC: 1, 2, 3)
  - [x] Add a minimal `ViewDefinition` contract in `:kernel:layout-model`.
  - [x] Add a minimal `Layout IR` document and element shape in `:kernel:layout-model`.
  - [x] Add a minimal `Geometry IR` document and element shape in `:kernel:geometry-model`.
  - [x] Keep all core Kotlin classes documented with KDoc.
- [x] Preserve canonical identity as the projection anchor. (AC: 2, 3)
  - [x] Reuse `StableSemanticIdentity` from `:kernel:engineering-model` instead of inventing projection-owned semantic IDs.
  - [x] Allow projection-local IDs only as secondary structure helpers when they still resolve back to canonical semantic identity.
  - [x] Make the downstream-only boundary explicit in code comments and module READMEs.
- [x] Keep M2 Story 1.1 narrow and avoid premature integration churn. (AC: 1, 2, 4)
  - [x] Do not implement view contributions, layout derivation, geometry derivation, runtime projection sessions, or UI switching in this story.
  - [x] Do not replace the current direct `Engineering IR -> SvgRenderModel` path yet; later M2 stories own that migration.
  - [x] Do not move semantic authority into `:ui:compose-workbench`, `:kernel:svg-renderer`, or extension-private models.
- [x] Add deterministic proof tests and documentation for the new module boundary. (AC: 2, 3, 4)
  - [x] Add marker tests for both modules and focused unit tests for the first projection contract invariants.
  - [x] Keep root build and module tests green on Java 25.
  - [x] Update kernel group documentation so the new modules appear in the module map.

## Dev Notes

### Story Intent

- Story `1.1` is the M2 substrate entry point. Its job is to create the durable homes for projection contracts before any view-specific derivation logic, runtime session logic, or desktop behavior is added.
- The milestone risk here is architectural drift: if layout and geometry stay implicit inside renderer DTOs or Compose-facing scene models, M2 will violate the manifesto before the projection pipeline is even built.
- Story `1.2` owns contribution of the first supported `cabinet` and `wiring` view definitions from the electrical extension.
- Story `1.3` owns deriving `Layout IR` from canonical semantics.
- Story `1.4` owns deriving `Geometry IR` and preserving canonical identity across projection layers.
- Story `1.5` owns feeding the first backend from `Geometry IR` and publishing the M2 proof corpus.
- This story must therefore stop at explicit module and contract establishment. Do not absorb downstream derivation or desktop integration work here.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority. `Layout IR` and `Geometry IR` are downstream consequence layers only.
- Align to AD-1 and AD-8 by creating `:kernel:layout-model` and `:kernel:geometry-model` as durable kernel homes instead of hiding projection contracts in compiler, runtime, UI, or renderer code.
- Align to AD-2 by leaving derivation logic in `:kernel:compiler` for later stories. Story `1.1` defines contracts; it does not implement the two-stage projection pipeline yet.
- Align to AD-5 by carrying canonical semantic identity through projection contract fields from the start.
- Align to AD-6 by not letting `:kernel:svg-renderer` or `:ui:compose-workbench` become the long-term home of layout or geometry truth.
- Align to manifesto `09-layout-and-geometry.md`: layout intent and final geometry are distinct layers and neither may collapse back into semantic truth.

### Technical Requirements

- Prefer small, explicit model types over an oversized speculative hierarchy. A good first slice is:
  - `ViewDefinition`
  - `LayoutDocument`
  - `LayoutNode` or equivalent minimal layout item shape
  - `GeometryDocument`
  - `GeometryElement` or equivalent minimal geometry item shape
- Package roots should remain under `com.engineeringood.athena` and stay direct:
  - `com.engineeringood.athena.layout`
  - `com.engineeringood.athena.geometry`
- Reuse `StableSemanticIdentity` from `:kernel:engineering-model` for semantic anchoring.
- Projection-local IDs may exist for renderer or tree structure, but they must never replace canonical semantic identity as the primary reference.
- Keep both new modules dependency-light. `:kernel:layout-model` and `:kernel:geometry-model` should depend only on the minimum shared kernel model they actually need.
- Add KDoc for all core Kotlin classes introduced in these two new modules.

### Architecture Compliance

- AD-1: Projection contracts are explicit kernel-owned model types, not renderer-local DTOs.
- AD-2: No derivation logic moves into runtime, viewer, or renderer code in this story.
- AD-5: Every durable projection contract carries canonical semantic identity references first.
- AD-6: This story prepares the later renderer-facing boundary by introducing a geometry contract instead of extending `SvgRenderModel` into the canonical projection model.
- AD-8: Module growth stays grouped under `kernel/` and follows the current grouped workspace topology.

### Library / Framework Requirements

- Use the repo-approved local stack already pinned in the workspace:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - shared plugin and dependency versions from `gradle/libs.versions.toml`
- Do not introduce new external libraries for Story `1.1`.
- Reuse the existing Kotlin/JUnit test setup and marker-test convention already used by other kernel modules.

### File Structure Requirements

- Expected new files and directories:
  - `kernel/layout-model/build.gradle.kts`
  - `kernel/layout-model/README.md`
  - `kernel/layout-model/README.zh-CN.md`
  - `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/**`
  - `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/**`
  - `kernel/geometry-model/build.gradle.kts`
  - `kernel/geometry-model/README.md`
  - `kernel/geometry-model/README.zh-CN.md`
  - `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/**`
  - `kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/**`
- Expected update files:
  - `settings.gradle.kts`
  - `kernel/README.md`
  - `kernel/README.zh-CN.md`
- Files whose current behavior must be preserved unless a direct need is proven:
  - `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
    - This is still the canonical semantic model. Do not duplicate semantic entities into layout or geometry contracts.
  - `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
    - It currently derives `SvgRenderModel` directly from `EngineeringDocument`. Do not replace or broaden that implementation in Story `1.1`.
  - `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`
    - This remains the current downstream renderer-facing model until later M2 stories migrate to `Geometry IR`.
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
    - It currently exposes a viewer-safe scene derived from compiler rendering. Do not refactor runtime viewer projection to geometry snapshots yet.
- Module dependency expectations for this story:
  - `:kernel:layout-model` should depend on `:kernel:engineering-model`.
  - `:kernel:geometry-model` should depend on `:kernel:engineering-model` and only on `:kernel:layout-model` if a contract field truly requires it.
  - Do not add these modules as broad dependencies across runtime, UI, or extensions yet unless needed strictly for compilation of the new contracts.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:geometry-model:test`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:compiler:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof tests:
  - both module markers report the expected Gradle module names
  - layout model tests prove canonical semantic identity is required or preserved on core projection shapes
  - geometry model tests prove geometry contracts remain downstream and semantically anchored
  - workspace build remains green with the two new kernel modules registered
- Keep Gradle verification sequential on Windows. Do not run `build` and `test` concurrently in this repo.

### Current Code State To Preserve

- `settings.gradle.kts` currently includes:
  - `:apps:cli`
  - `:apps:desktop-viewer`
  - `:ui:compose-workbench`
  - `:kernel:runtime`
  - `:kernel:language`
  - `:kernel:engineering-model`
  - `:kernel:validation`
  - `:kernel:compiler`
  - `:extensions:domain-electrical`
  - `:kernel:svg-renderer`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt` currently defines:
  - `StableSemanticIdentity`
  - `EngineeringDocument`
  - canonical semantic entities such as `EngineeringSystem`, `EngineeringComponent`, `EngineeringPort`, and `EngineeringConnection`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt` currently has no layout or geometry projection result model; do not invent broad compiler result rewrites in this story.
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt` currently implements the direct deterministic `Engineering IR -> SvgRenderModel` proof path.
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt` currently maps compiler rendering output into a viewer-safe scene model using `semanticId` on boxes and connections.
- `gradle/libs.versions.toml` already owns version and plugin pins; keep new module build files aligned to that catalog.

### Previous Story Intelligence

- M1 is the immediately preceding completed milestone and provides the relevant carry-forward rules:
  - Java `25` is non-negotiable and should be activated through `java25` before Gradle verification on this workstation.
  - The package and group root must remain `com.engineeringood.athena`.
  - New modules should carry marker classes, focused unit tests, and English plus Chinese READMEs.
  - Grouped physical module layout under `kernel`, `extensions`, `ui`, and `apps` is already an explicit repo rule and should be preserved.
  - Runtime-above-compiler ownership from M1 stays intact; M2 must add projection contracts below that orchestration layer, not invert it.
- Preserve the current repo style of thin module build scripts, KDoc on core Kotlin classes, and deterministic local verification on Java 25.

### Git Intelligence Summary

- Recent commits confirm the current baseline is the completed M1 grouped-module workspace:
  - `ad382d8 Complete M1 runtime workspace and regroup modules`
  - `bdc3227 init in 2026-07-03`
  - `dd9dcbe init in 2026-07-03`
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
- Practical implementation guidance should therefore come from the current working tree and the finalized M2 planning artifacts, not from older pre-grouping assumptions.

### Latest Technical Information

- No external version upgrade decision is required for Story `1.1`.
- Treat the repo-pinned local stack as authoritative for this story:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Compose Multiplatform `1.11.1` remains part of the workspace but is not used directly in this story

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- M2 implementation artifacts are intentionally isolated under `_bmad-output/implementation-artifacts/m2/` because M1 and M2 both use `Epic 1` / `Epic 2` numbering.
- This story should update module maps and module-local READMEs so the workspace stays understandable after the new kernel modules land.
- UX is intentionally out of scope for this story. Do not add view switching, inspector panels, or shell-level behavior while defining the new contracts.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Epic 1: Produce Canonical Multi-View Projections`
  - `Story 1.1: Establish Explicit Layout And Geometry Model Modules`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - Sections `1`, `1.1`, `4.1`, `4.2`, `8`, and `9`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-1` through `AD-8`
  - `Consistency Conventions`
  - `Structural Seed`
  - `Capability -> Architecture Map`
- `manifesto/docs/architecture/03-ir.md`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `docs/usages/athena-workspace-summary.md`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `kernel/README.md`
- `kernel/engineering-model/README.md`
- `kernel/engineering-model/src/main/kotlin/com/engineeringood/athena/ir/EngineeringModel.kt`
- `kernel/compiler/README.md`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `kernel/svg-renderer/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRenderModel.kt`

## Story Completion Status

- Status: done
- Completion note: Added explicit `:kernel:layout-model` and `:kernel:geometry-model` modules with documented projection contracts, semantic identity anchors, README coverage, and green Java 25 verification without disturbing the existing M1 render path.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Red phase:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:geometry-model:test`
  - Failed as expected on unresolved projection contract types in the new tests.
- Green phase:
  - Added `:kernel:layout-model` and `:kernel:geometry-model` to `settings.gradle.kts`.
  - Implemented minimal projection contracts and marker classes under `com.engineeringood.athena.layout` and `com.engineeringood.athena.geometry`.
  - Added English and Chinese READMEs for both new modules.
  - Updated `kernel/README.md` and `kernel/README.zh-CN.md` to include the new kernel modules.
- Verification:
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:layout-model:test :kernel:geometry-model:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain :kernel:engineering-model:test :kernel:compiler:test`
  - `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `:kernel:layout-model` as the durable home for `ViewDefinition`, `LayoutDocument`, `LayoutNode`, and `LayoutNodeId`.
- Added `:kernel:geometry-model` as the durable home for `GeometryDocument`, `GeometryElement`, `GeometryElementId`, `GeometryElementKind`, and `GeometryBounds`.
- Kept `StableSemanticIdentity` as the primary semantic anchor across the first explicit projection contracts.
- Preserved the existing direct `Engineering IR -> SvgRenderModel` pipeline and did not refactor runtime viewer projection or renderer ownership in this story.
- Added marker tests and projection contract tests proving semantic identity is preserved on the new model shapes.
- Kept verification sequential on Windows and confirmed the full Java 25 build remains green.

## File List

- `_bmad-output/implementation-artifacts/m2/1-1-establish-explicit-layout-and-geometry-model-modules.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `settings.gradle.kts`
- `kernel/README.md`
- `kernel/README.zh-CN.md`
- `kernel/layout-model/build.gradle.kts`
- `kernel/layout-model/README.md`
- `kernel/layout-model/README.zh-CN.md`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModelMarker.kt`
- `kernel/layout-model/src/main/kotlin/com/engineeringood/athena/layout/LayoutModel.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelMarkerTest.kt`
- `kernel/layout-model/src/test/kotlin/com/engineeringood/athena/layout/LayoutModelTest.kt`
- `kernel/geometry-model/build.gradle.kts`
- `kernel/geometry-model/README.md`
- `kernel/geometry-model/README.zh-CN.md`
- `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModelMarker.kt`
- `kernel/geometry-model/src/main/kotlin/com/engineeringood/athena/geometry/GeometryModel.kt`
- `kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/GeometryModelMarkerTest.kt`
- `kernel/geometry-model/src/test/kotlin/com/engineeringood/athena/geometry/GeometryModelTest.kt`

## Change Log

- 2026-07-06: Added the first explicit kernel-owned layout and geometry model modules for M2, with KDoc, README coverage, marker tests, and identity-preserving projection contract tests.
