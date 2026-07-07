---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 1.5: Support Viewport, Selection, Pan, And Zoom Without Semantic Authority Leakage

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an operator,
I want the Compose viewer to support viewport control, selection, pan, and zoom as runtime-facing interaction infrastructure,
so that I can inspect and navigate the project interactively without turning viewer state into engineering truth.

## Acceptance Criteria

1. Given the active project is displayed in the Compose viewer, when I pan or zoom the view, then the camera and viewport state update for interactive inspection, and that state remains disposable UI infrastructure rather than canonical semantic state.
2. Given a rendered semantic object can be hit-tested in the viewer, when I select it, then selection updates independently from project semantics, and selection alone does not mutate canonical state unless a later explicit command path is invoked.
3. Given the viewer interaction slice is introduced, when public contracts are reviewed, then viewport, selection, input, camera, and hit-testing are described in domain-neutral runtime terms, and no electrical rules or plugin-private semantic ownership leak into Compose runtime APIs.
4. Given the interaction infrastructure is implemented, when the standard Java `25` build and viewer checks run, then the active-project viewer supports selection, pan, and zoom successfully, and Compose runtime remains viewing infrastructure rather than a domain-rich editor.

## Tasks / Subtasks

- [x] Add a domain-neutral viewer interaction model in `:compose-runtime`. (AC: 1, 3)
  - [x] Add documented Kotlin types for viewport, camera, interaction state, and selection that describe viewing infrastructure only.
  - [x] Keep interaction state session-local and disposable; it must not become semantic authority or an alternate engineering model.
  - [x] Ensure public names and KDoc stay domain-neutral and reusable above later frontend surfaces.
- [x] Add hit-testing and selection over the current semantic viewer scene. (AC: 2, 3)
  - [x] Derive selection targets from stable viewer scene identifiers rather than from mutable UI-local semantic copies.
  - [x] Add deterministic hit-test behavior for component surfaces and related viewer objects needed by the current scene.
  - [x] Keep selection as inspection state only; do not route command mutation through Story `1.5`.
- [x] Add pan and zoom behavior to the Compose semantic viewer stage. (AC: 1, 4)
  - [x] Apply camera transforms consistently to rendered component geometry and connection overlays.
  - [x] Support pointer-driven panning and zooming for desktop use while keeping the underlying semantic scene read-only.
  - [x] Preserve Story `1.4` active-project rendering proof while extending it with interaction infrastructure.
- [x] Prove the viewer interaction boundary with tests and app verification. (AC: 1, 2, 3, 4)
  - [x] Add focused tests for interaction-state math, hit-testing, and selection behavior.
  - [x] Add or update viewer verification so the desktop app proves interaction infrastructure is wired without requiring semantic mutation.
  - [x] Keep the standard Java `25` root build and test path green after the interaction slice lands.
- [x] Document the semantic boundary so Story `1.5` does not drift into Epic `2`. (AC: 2, 3, 4)
  - [x] Add or update architecture-facing documentation under `docs/**` that states viewport and selection are view infrastructure, not engineering truth.
  - [x] State explicitly that command-backed semantic mutation, diff/history, and plugin-owned rules remain Epic `2` scope.

## Dev Notes

### Story Intent

- Story `1.5` is the first interactive viewer slice for M1.
- It owns viewport control, selection, pan, zoom, and hit-testing as view infrastructure.
- It does not own command-backed semantic mutation, engineering graph edits, undo/redo, or plugin-private semantic behavior.
- The user-facing proof is an inspectable engineering workbench surface, not a toy demo and not yet a full editor.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority.
- Runtime remains the owner of `Workspace`, `Project`, `Execution Context`, and future command orchestration.
- Compose runtime stays a frontend adapter and may not become a sovereign semantic model.
- Viewer interaction state must be disposable infrastructure layered over the runtime-owned scene.
- Do not leak electrical semantics, wiring rules, or plugin ownership into generic viewer interaction contracts.
- Do not start Epic `2` mutation paths in this story.

### Technical Requirements

- Build directly on the Story `1.4` runtime viewer proof and scene model.
- Keep all new core Kotlin classes documented with KDoc.
- Keep package and group root under `com.engineeringood.athena`.
- Prefer small immutable view-state types plus explicit update helpers over hidden mutable semantic copies.
- Selection must reference stable scene identifiers only.
- Hit-testing and camera transforms must remain consistent with the layout/geometry separation defined in the manifesto:
  - semantic structure is not layout authority
  - layout/view state is not semantic truth
  - rendered geometry is derived and disposable

### Architecture Compliance

- Align to AD-1 by keeping the interaction proof JVM-first and desktop-primary.
- Align to AD-2 by keeping runtime ownership above the viewer.
- Align to AD-3 by treating all interaction state as derived from canonical `Engineering IR`.
- Align to AD-4 by not introducing semantic mutation in Story `1.5`.
- Align to AD-6 by keeping the Compose surface an adapter to one runtime contract.
- Align to AD-7 by keeping Compose runtime domain-neutral viewing infrastructure.
- Align to AD-10 by preserving the version-catalog-based module setup.

### Library / Framework Requirements

- Use the repo-approved local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Compose Multiplatform `1.11.1`
- Reuse the existing `compose-runtime` and `apps:compose-viewer` modules.
- Prefer official Compose pointer/transform primitives already available in the approved stack; do not introduce unnecessary UI framework churn.
- Do not add web, Android, server, AI, or plugin scope in this story.

### File Structure Requirements

- Expected update files:
  - `compose-runtime/src/commonMain/**`
  - `compose-runtime/src/commonTest/**`
  - `apps/compose-viewer/src/main/**`
  - `apps/compose-viewer/src/test/**`
  - `docs/compiler/**`
- Expected optional update files if justified by implementation evidence:
  - `runtime/src/main/**`
  - `runtime/src/test/**`
- Files whose current behavior must be preserved unless direct story need is proven:
  - `compiler/**`
    - compiler ownership remains unchanged
  - `cli/**`
    - CLI runtime routing from Story `1.2` must stay green
  - `runtime/**`
    - runtime continues to own semantic compilation and project activation

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- Required proof expectations:
  - the active-project viewer supports pan, zoom, and selection
  - interaction state does not mutate canonical semantic state
  - the root Java `25` build and test path remain green
- Keep Gradle verification sequential on Windows.

### Current Code State To Preserve

- Story `1.4` already established:
  - runtime-owned active-project bootstrap
  - runtime-facing viewer projection
  - shared semantic viewer scene and display-only stage
  - deterministic desktop smoke proof
- Current shared viewer seam lives at:
  - `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
  - `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerScene.kt`
  - `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
- Current desktop entrypoint lives at:
  - `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`

### Previous Story Intelligence

- Story `1.1` established runtime-owned lifecycle and service orchestration.
- Story `1.2` proved `frontend -> runtime -> compiler`.
- Story `1.3` established the Compose module split and Java `25` build posture.
- Story `1.4` proved runtime-owned semantic viewer display without interaction.
- The implementation-readiness review warned that Stories `1.3` and `1.5` can overlap if interaction infrastructure is pulled too early.
- Carry forward these constraints:
  - Java `25` is non-negotiable
  - package and group root remain `com.engineeringood.athena`
  - Compose runtime must not own semantic authority
  - command-backed mutation belongs to Epic `2`

### Git Intelligence Summary

- The current repo state already contains the completed display-only viewer proof from Story `1.4`.
- The next safe extension point is to add interaction infrastructure around the existing scene and shell rather than reopening compiler or runtime architecture.
- Practical guidance should come from the current working tree, the M1 planning artifacts, and the completed Epic `1` stories.

### Latest Technical Information

- Use the approved Compose Multiplatform stack already adopted in the repo.
- Treat official Compose gesture support as implementation guidance only; preserve Athena architecture boundaries over API convenience.

### Project Structure Notes

- UX for M1 already frames the viewer as a professional engineering workbench surface with source tree, render pane, console, and inspector patterns.
- Story `1.5` only delivers the interaction infrastructure inside the render pane surface.
- Do not start docking systems, inspector editing, token systems, skinning, or broader Maya workbench scope here.
- Keep the interaction behavior calm, readable, and precise rather than flashy.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 1: Activate And Inspect A Runtime-Managed Project`
  - `Story 1.5: Support Viewport, Selection, Pan, And Zoom Without Semantic Authority Leakage`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - Sections `4.5`, `6.1`, `8.3`, and `10`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-1`, `AD-2`, `AD-3`, `AD-4`, `AD-6`, `AD-7`, and `AD-10`
  - `Structural Seed`
  - `Capability -> Architecture Map`
- `_bmad-output/planning-artifacts/ux-designs/ux-Athena-2026-07-04/EXPERIENCE.md`
  - `Foundation`
  - `State Patterns`
  - `Interaction Primitives`
  - `Flow 1 - Workspace Under Control`
  - `Flow 2 - Inspect And Connect Ports`
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-04.md`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m1/1-3-initialize-the-compose-runtime-module-split-and-version-catalog.md`
- `_bmad-output/implementation-artifacts/m1/1-4-render-the-active-project-in-a-compose-semantic-viewer.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerScene.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`

## Story Completion Status

- Status: review
- Completion note: Disposable viewer interaction infrastructure is implemented and verified for `:compose-runtime:test`, `:apps:compose-viewer:test`, `:apps:compose-viewer:bootstrapSmoke`, root `build`, and root `test` on Java `25`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context assembled from Epic `1`, the M1 PRD, the architecture spine, the UX experience spine, the implementation-readiness report, the layout/geometry manifesto, and the completed Stories `1.1` through `1.4`.
- Current repo seams verified against the runtime viewer projection, the shared Compose shell, the semantic viewer scene, the semantic viewer stage, and the desktop viewer entrypoint.
- Red phase started with a new `AthenaSemanticViewerInteractionStateTest` suite that failed before the new viewer interaction contracts existed.
- Green verification completed with `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`, `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`, `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`, `.\\gradlew.bat --no-daemon --console=plain build`, and `.\\gradlew.bat --no-daemon --console=plain test`.
- Initial root verification exposed that `verifyJava25` depends on the Gradle daemon JVM rather than only on the Kotlin toolchain; the final fix pinned the daemon through `gradle/gradle-daemon-jvm.properties` plus the Foojay resolver plugin so normal wrapper `build` and `test` now execute on Java `25` without manual `JAVA_HOME` editing.
- Desktop launch verification reproduced the original Java `19` failure for `:apps:compose-viewer:run`, then proved the final fix by launching a responsive `Athena` window from a Java `19` shell while the app JVM itself ran on the Java `25` toolchain.

### Completion Notes List

- Added `AthenaSemanticViewerInteractionState`, `AthenaSemanticViewerViewport`, `AthenaSemanticViewerCamera`, and `AthenaSemanticViewerSelection` so camera, viewport, hit-testing, and selection remain disposable viewer-state contracts.
- Added deterministic hit-testing over component boxes and connection lines plus pure tests that prove pan, zoom anchoring, selection, and deselection behavior.
- Updated `AthenaSemanticViewerStage` to bind click-selection, drag-pan, zoom controls, selection highlighting, and viewport readouts over the existing runtime-owned scene.
- Added a dedicated compiler-facing boundary note that keeps Story `1.5` interaction infrastructure separate from Epic `2` semantic mutation work.
- Pinned the desktop Compose launcher to the Java `25` toolchain and added stable Windows runtime JVM arguments so `:apps:compose-viewer:run` opens the first desktop window instead of failing under Java `19`.

### File List

- `_bmad-output/implementation-artifacts/m1/1-5-support-viewport-selection-pan-and-zoom-without-semantic-authority-leakage.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `apps/compose-viewer/build.gradle.kts`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerInteractionState.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
- `compose-runtime/src/commonTest/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerInteractionStateTest.kt`
- `docs/compiler/m1-compose-interaction-boundary.md`

### Change Log

- Implemented the Story `1.5` interaction slice with domain-neutral viewer-state contracts, hit-testing, click-selection, drag-pan, zoom controls, and boundary documentation.
- Stabilized the desktop viewer launch path by forcing the Compose app to use the Java `25` toolchain plus Windows-safe runtime JVM arguments for native access and software rendering.
