---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 1.4: Render The Active Project In A Compose Semantic Viewer

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an operator,
I want the active project to be displayed in a Compose-based semantic viewer driven by runtime-coordinated services,
so that I can inspect canonical project state through a real runtime-facing surface without moving semantic authority into the UI.

## Acceptance Criteria

1. Given an active `Workspace` and `Project` managed by `Athena Runtime`, when the Compose viewer is opened, then it resolves the active project through runtime-owned services, and it does not construct compiler or semantic state privately inside the UI layer.
2. Given a project that can already compile through the runtime DSL path, when the viewer requests display data, then Athena renders the project into a viewer-consumable representation through runtime-coordinated semantic and render services, and the viewer displays the active project without becoming the source of truth.
3. Given canonical semantic state changes through approved runtime paths, when the viewer refreshes its displayed content, then the displayed result remains consistent with canonical `Engineering IR` and downstream rendering rules, and no viewer-local state is allowed to redefine engineering meaning.
4. Given the first semantic viewer proof is in place, when the standard Java `25` build and app checks run, then the Compose application can launch and display the active project successfully, and the implementation demonstrates the M1 proof path `DSL -> Engineering IR -> Compose Viewer`.

## Tasks / Subtasks

- [x] Add a runtime-owned viewer bootstrap path for the active project. (AC: 1, 4)
  - [x] Add a deterministic desktop bootstrap that opens a runtime-owned workspace and activates a known project fixture without constructing compiler state inside the Compose UI.
  - [x] Keep the active-project bootstrap owned by runtime-facing classes or descriptors rather than by Composable-local state.
  - [x] Preserve the non-interactive smoke path from Story `1.3`.
- [x] Introduce a viewer-consumable semantic scene model above the runtime boundary. (AC: 1, 2, 3)
  - [x] Add small documented Kotlin types that represent the first Compose viewer scene from runtime-coordinated semantic and render outputs.
  - [x] Build that scene from runtime compilation results rather than by reparsing or duplicating semantic logic in the UI layer.
  - [x] Keep the scene read-only and inspectable; it must remain a derived view over canonical state.
- [x] Render the active project in the shared Compose shell. (AC: 2, 4)
  - [x] Update the shared Compose shell to show the active project identity and a simple semantic viewer surface derived from the runtime scene model.
  - [x] Render enough project structure to prove a real semantic viewer, not only a static placeholder.
  - [x] Keep Story `1.4` focused on display only; do not add pan, zoom, selection mechanics, or semantic mutation behavior that belong to Story `1.5` and Epic `2`.
- [x] Add deterministic tests and app proof for the semantic viewer path. (AC: 1, 2, 3, 4)
  - [x] Add focused tests for the runtime-owned viewer bootstrap and scene derivation behavior.
  - [x] Add or update desktop smoke verification so the app proves that a real active project is displayed, not just that the shell opens.
  - [x] Keep the standard Java `25` root build and test path green after the viewer rendering slice is added.
- [x] Document the `1.4` boundary so it stays distinct from `1.5`. (AC: 3, 4)
  - [x] Add or update architecture-facing documentation under `docs/**` to state that Story `1.4` owns semantic display proof only.
  - [x] State explicitly that viewport, selection, pan, zoom, and hit-testing behavior remain Story `1.5` scope.

### Review Findings

- [x] [Review][Patch] `bootstrapSmoke` exits before the Compose viewer path is created, so the smoke task does not prove the active-project viewer renders. [apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt:10]
- [x] [Review][Patch] The desktop viewer app crosses the runtime boundary by switching on compiler and rendering result types inside the UI bootstrap instead of consuming one runtime-facing contract. [apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt:79]
- [x] [Review][Patch] The default desktop bootstrap depends on the current working directory remaining under the repository tree, so launches outside the repo fail before the viewer opens. [apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt:126]
- [x] [Review][Patch] The semantic viewer stage mixes Compose `dp` placement for component boxes with raw canvas pixel coordinates for connections, which misaligns lines on scaled displays. [compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt:88]

## Dev Notes

### Story Intent

- Story `1.4` is the first real viewer behavior slice after the Compose bootstrap work in Story `1.3`.
- It must prove `DSL -> Engineering IR -> Compose Viewer`.
- It owns rendering the active project in Compose through runtime-coordinated services.
- It does not own viewport navigation, selection state, pan, zoom, or semantic mutation behavior.

### Architecture Guardrails

- M1 remains one Java `25` and Kotlin process. Desktop remains the primary proof surface in this story.
- `Athena Runtime` remains the owner of `Workspace`, `Project` activation, `Execution Context`, and service orchestration.
- `Engineering IR` remains the only canonical semantic authority.
- The Compose viewer must consume runtime-owned outputs and may not privately bootstrap compiler or semantic state.
- Preserve evolutionary extraction above M0 and above the Story `1.3` Compose bootstrap work.
- Do not start command runtime, graph mutation, diff/history UI, or `1.5` interaction mechanics in this story.

### Technical Requirements

- Use the existing Story `1.3` module split:
  - `:compose-runtime` is the shared Compose surface module.
  - `:apps:compose-viewer` is the desktop entry module.
- Use runtime-owned execution context methods already established in Story `1.2`:
  - `parseActiveProject()`
  - `lowerActiveProject()`
  - `compileActiveProject()`
- Prefer `compileActiveProject()` as the single viewer-facing source for the first semantic viewer proof so the UI consumes the canonical runtime compilation result rather than reconstructing semantic state.
- Keep all new core Kotlin classes documented with KDoc.
- Keep package and group root under `com.engineeringood.athena`.
- Use the existing published M0 fixtures under `examples/m0/` as the deterministic viewer proof inputs unless implementation evidence proves a different fixture is required.

### Architecture Compliance

- Align to AD-1 by keeping the viewer proof JVM-first and local.
- Align to AD-2 by resolving the active project through `AthenaRuntime` and `AthenaExecutionContext`.
- Align to AD-3 by treating the Compose scene as a derived view, not semantic truth.
- Align to AD-4 by not introducing semantic mutation paths in this story.
- Align to AD-6 by keeping the Compose surface as a frontend adapter to one runtime contract.
- Align to AD-7 by keeping Compose domain-neutral viewer infrastructure that consumes runtime outputs.
- Align to AD-10 by reusing the version-catalog-driven Compose setup from Story `1.3`.

### Library / Framework Requirements

- Use the repo-approved local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Compose Multiplatform `1.11.1`
- Reuse the existing `compose-runtime` and `apps:compose-viewer` modules from Story `1.3`.
- Do not add web, Android, server, or unrelated framework scope in this story.

### File Structure Requirements

- Expected update files:
  - `compose-runtime/src/commonMain/**`
  - `compose-runtime/src/commonTest/**`
  - `apps/compose-viewer/src/main/**`
  - `apps/compose-viewer/src/test/**`
  - `docs/compiler/**`
- Expected optional update files if justified by the implementation:
  - `runtime/src/main/**`
  - `runtime/src/test/**`
- Files whose current behavior must be preserved unless direct story need is proven:
  - `cli/**`
    - CLI runtime routing from Story `1.2` must stay green.
  - `compiler/**`
    - Compiler ownership remains unchanged; use its runtime-facing results rather than moving logic into the UI.
  - `compose-runtime/build.gradle.kts`
    - Preserve the Story `1.3` bootstrap and test wiring.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- Required proof expectations:
  - the desktop app bootstrap shows a real active-project viewer proof
  - the shared Compose shell renders derived runtime-owned project information
  - the root Java `25` build and test path remain green
- Keep Gradle verification sequential on Windows. The repo is currently pinned to safe sequential in-process Kotlin compilation in `gradle.properties`.

### Current Code State To Preserve

- Story `1.3` already established:
  - `gradle/libs.versions.toml`
  - `:compose-runtime`
  - `:apps:compose-viewer`
  - `AthenaComposeShellDescriptor`
  - `AthenaComposeShell`
  - deterministic desktop `bootstrapSmoke`
- Runtime already exposes:
  - `AthenaRuntime.openWorkspace(...)`
  - active project activation through `AthenaWorkspace`
  - `AthenaExecutionContext.compileActiveProject()`
- Current desktop entrypoint lives at:
  - `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
- Current shared shell is still a minimal bootstrap placeholder:
  - `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`

### Previous Story Intelligence

- Story `1.1` established runtime-owned lifecycle and service orchestration above M0.
- Story `1.2` proved `frontend -> runtime -> compiler`.
- Story `1.3` proved the Compose module split, catalog adoption, desktop bootstrap path, and Windows-safe root verification.
- Carry forward these constraints:
  - Java `25` is non-negotiable.
  - package and group root remain `com.engineeringood.athena`
  - the Compose shell must stay distinct from runtime ownership
  - Story `1.5` still owns viewport, selection, pan, zoom, and hit-testing behavior

### Git Intelligence Summary

- Current implementation context is still anchored in the same working-tree evolution as Story `1.3`.
- The most relevant established pattern is the recent Compose bootstrap extraction rather than a long commit history of viewer behavior.
- Practical guidance should come from the current working tree, Story `1.3`, and the approved M1 planning artifacts.

### Latest Technical Information

- Treat the approved local Compose template as the structural reference for app/shared module posture only.
- For Story `1.4`, the actual viewer behavior must be derived from Athena runtime outputs rather than copied from the template's placeholder app logic.

### Project Structure Notes

- UX now exists for M1 and should shape this story lightly:
  - `Workspace Under Control` is the main viewer proof reference.
  - Story `1.4` should make the shell feel like a real engineering workbench surface, but only at the semantic display level.
  - Do not start docking choreography, advanced editor behavior, or full workbench parity in this story.
- The viewer proof should remain inspectable and calm rather than flashy or toy-like.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 1: Activate And Inspect A Runtime-Managed Project`
  - `Story 1.4: Render The Active Project In A Compose Semantic Viewer`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - Sections `4.5`, `6.1`, `8`, `9`, and `10`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-1`, `AD-2`, `AD-3`, `AD-6`, `AD-7`, `AD-10`
  - `Structural Seed`
  - `Capability -> Architecture Map`
- `_bmad-output/planning-artifacts/ux-designs/ux-Athena-2026-07-04/EXPERIENCE.md`
  - `Foundation`
  - `Information Architecture`
  - `State Patterns`
  - `Flow 1 - Workspace Under Control`
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-04.md`
- `_bmad-output/implementation-artifacts/m1/1-1-establish-the-runtime-host-above-m0.md`
- `_bmad-output/implementation-artifacts/m1/1-2-route-the-existing-dsl-path-through-athena-runtime.md`
- `_bmad-output/implementation-artifacts/m1/1-3-initialize-the-compose-runtime-module-split-and-version-catalog.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`

## Story Completion Status

- Status: done
- Completion note: Runtime-owned semantic viewer proof is implemented, review patches are applied, and Java `25` verification is green for `:compose-runtime:test`, `:apps:compose-viewer:test`, `:apps:compose-viewer:bootstrapSmoke`, root `build`, and root `test`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context assembled from Epic `1`, the M1 PRD, architecture spine, UX spine, the implementation-readiness report, and the completed Stories `1.1` through `1.3`.
- Current repo seams verified against `AthenaRuntime`, `AthenaExecutionContext`, `AthenaCompiler`, `AthenaComposeShell`, and the desktop viewer entrypoint.
- Red phase verified with failing tests for missing semantic viewer scene and runtime bootstrap types before implementation.
- Green verification completed with `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`, `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`, `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`, `.\\gradlew.bat --no-daemon --console=plain build`, and `.\\gradlew.bat --no-daemon --console=plain test` on Java `25`.

### Completion Notes List

- Added `AthenaComposeViewerBootstrap` and `AthenaComposeViewerProjectSnapshot` so the desktop app opens `examples/m0/demo-cabinet.athena` through `AthenaRuntime` rather than constructing compiler state inside the UI.
- Added shared `AthenaSemanticViewerScene` types and a display-only `AthenaSemanticViewerStage` derived from canonical runtime compilation results.
- Updated `AthenaComposeShell` and the desktop entrypoint to render active-project identity, component boxes, and connection lines from the derived runtime scene.
- Added focused tests for scene derivation and desktop bootstrap behavior, and documented the `1.4` display-only boundary separately from Story `1.5`.
- Applied code-review fixes that moved viewer projection branching behind the runtime module, rendered the smoke path off-screen through Compose, added explicit repo-root bootstrap overrides, and aligned stage coordinates through `LocalDensity`.
- Story `1.5` keeps viewport, selection, pan, zoom, and hit-testing ownership.

### File List

- `_bmad-output/implementation-artifacts/m1/1-4-render-the-active-project-in-a-compose-semantic-viewer.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerScene.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
- `compose-runtime/src/commonTest/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerSceneTest.kt`
- `apps/compose-viewer/build.gradle.kts`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerProjectSnapshot.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerSmokeVerifier.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerRuntimeTest.kt`
- `docs/compiler/m1-compose-bootstrap-boundary.md`
- `docs/compiler/m1-compose-viewer-boundary.md`
- `runtime/build.gradle.kts`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt`

### Change Log

- Added a runtime-owned desktop bootstrap that compiles the active project fixture and derives a viewer scene from the canonical runtime compilation result.
- Added shared semantic viewer scene and stage types so Compose renders component boxes and connection lines without owning semantic truth.
- Updated the desktop app and shared shell to display the active project proof path while preserving the Story `1.3` smoke mode.
- Added tests and documentation that keep Story `1.4` constrained to semantic display proof ahead of Story `1.5` interaction work.
