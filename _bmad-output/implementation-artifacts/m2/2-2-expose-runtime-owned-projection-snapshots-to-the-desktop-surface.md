---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 2.2: Expose Runtime-Owned Projection Snapshots To The Desktop Surface

Status: done

## Story

As an operator,
I want the desktop surface to inspect runtime-owned projection snapshots for the active view,
so that I can see and follow the same semantic object across different projections without making the UI the source of truth.

## Acceptance Criteria

1. Given an active project and runtime-owned projection session, when the desktop surface requests projection state, then it receives runtime-owned view projection snapshots for the active view, and the desktop surface does not privately derive layout or geometry from semantic state.
2. Given one semantic object appears in both `cabinet` and `wiring` projections, when the operator inspects or selects that object in the desktop surface, then the surface can show the same canonical semantic identity across both views, and differences in placement or emphasis do not change the object's semantic identity.
3. Given layout and geometry are downstream inspection layers only in M2, when the operator interacts with the desktop projection surface, then selection, pan, zoom, and view switching remain ephemeral UI/runtime state, and those interactions do not become direct semantic or geometry mutation paths.
4. Given runtime-owned projection exposure is implemented, when the standard Java `25` build and desktop checks are executed, then the workspace builds successfully and the desktop surface can inspect the active project through runtime-owned projection snapshots, and the implementation demonstrates multi-view inspection without moving authority into the UI.

## Tasks / Subtasks

- [x] Extend the desktop shell contract to carry runtime-owned projection session state. (AC: 1, 4)
  - [x] Add typed compose-shell models for supported projection views, active view identity, and active selection identity.
  - [x] Keep the shell contract viewer-facing only; it may describe runtime-owned projection state but may not become a second layout or geometry model.
  - [x] Keep all newly introduced core Kotlin classes documented with KDoc.
- [x] Expose runtime-owned projection snapshots through the desktop workbench session. (AC: 1, 4)
  - [x] Rebuild desktop shell state from `Athena Runtime` projection-session contracts instead of desktop-local projection assembly.
  - [x] Allow the workbench session to switch between the first supported proof pair, `cabinet` and `wiring`, through runtime-owned active-view switching.
  - [x] Preserve the existing runtime-backed connect-ports command panel and current desktop bootstrap flow.
- [x] Preserve canonical semantic identity during desktop-side projection inspection. (AC: 2, 3)
  - [x] Keep desktop selection anchored to canonical semantic ids rather than view-local render identity.
  - [x] Make the desktop shell show the same selected semantic id across view switching when the semantic object exists in both projections.
  - [x] Keep pan, zoom, selection, and view switching disposable UI/runtime state only; do not introduce geometry mutation or desktop-owned derivation.
- [x] Add deterministic desktop and compose-runtime regression tests for runtime-owned projection inspection. (AC: 1, 2, 3, 4)
  - [x] Add tests that the desktop session exposes supported view state and switches the active runtime-owned projection snapshot.
  - [x] Add tests that selected canonical semantic identity survives view switching for objects that appear in both projections.
  - [x] Keep existing desktop command-panel and runtime-backed bootstrap tests green.
- [x] Update module documentation for the desktop projection-inspection boundary. (AC: 4)
  - [x] Update `:apps:desktop-viewer` docs to describe runtime-owned projection consumption and active-view inspection.
  - [x] Update `:ui:compose-workbench` docs to describe projection-session metadata and the limit that pan/zoom/selection remain non-authoritative UI state.

## Dev Notes

### Story Intent

- Story `2.2` is the first desktop-side consumer of the runtime-owned projection session introduced in Story `2.1`.
- The success condition is desktop inspection over runtime-owned projection snapshots, not new semantic mutation paths or desktop-owned projection derivation.
- This story should prove that the same canonical semantic identity can stay visible while the operator changes active views.

### Architecture Guardrails

- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-2`: `:kernel:runtime` owns lifecycle and orchestration.
  - `AD-3`: runtime owns projection sessions, view switching, refresh, and inspectable projection snapshots.
  - `AD-4`: view definitions remain typed extension contracts rather than desktop hard-codes.
  - `AD-5`: canonical semantic identity must survive across semantic, layout, geometry, and desktop inspection layers.
  - `AD-6`: desktop and compose-workbench consume `Geometry IR`-backed runtime projection snapshots instead of deriving geometry privately.
  - `AD-7`: selection, pan, zoom, and view switching remain ephemeral UI/runtime state only.
  - `AD-8`: durable projection contracts stay in kernel modules; desktop and compose-workbench remain surfaces.
- Align to `manifesto/docs/architecture/09-layout-and-geometry.md`: layout and geometry remain downstream consequences of one semantic source, and desktop inspection must not collapse those layers back together.

### Technical Requirements

- Desktop workbench state must expose:
  - supported runtime-owned projection views
  - active runtime-owned view id
  - active projection snapshot availability
  - active semantic selection identity for inspection
- The first supported view pair remains:
  - `cabinet`
  - `wiring`
- View switching must flow through `Athena Runtime`; the desktop layer must not reinterpret or re-derive projections from semantic state.
- The desktop proof may keep selection, camera, and viewport state locally disposable, but that state must remain clearly non-authoritative.
- The existing GUI-backed `connect ports` path from M1 must remain available and compatible inside the same workbench session.

### Architecture Compliance

- `:kernel:runtime` remains the owner of projection-session truth; desktop reads and requests changes through runtime-owned contracts.
- `:ui:compose-workbench` may add viewer-facing projection-session DTOs and interaction intents, but it may not own semantic truth, layout truth, or geometry derivation.
- `:apps:desktop-viewer` may adapt runtime projection snapshots into shell state, but it may not privately derive layout or geometry from compiler semantic output.
- Any desktop-side selection model must anchor to canonical semantic ids and survive view changes only through that identity, never through view-local render ids.
- Do not add direct geometry editing, manual layout mutation, or generic multi-view authoring flows in this story.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
  - Compose Multiplatform already configured in the workspace
- Keep dependency changes minimal and local to existing desktop/compose modules unless the story makes a new dependency unavoidable.
- Reuse the current Kotlin test approach for both JVM and common tests.

### File Structure Requirements

- Likely update files:
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
  - `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
  - `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerRuntimeTest.kt`
  - `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellState.kt`
  - `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
  - `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
  - `apps/desktop-viewer/README.md`
  - `apps/desktop-viewer/README.zh-CN.md`
  - `ui/compose-workbench/README.md`
  - `ui/compose-workbench/README.zh-CN.md`
- Likely add files:
  - `ui/compose-workbench/src/commonTest/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellStateTest.kt`
- Update runtime files only if a small read-only projection-session inspection helper is required to keep desktop from deriving state privately.

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - desktop session exposes supported `cabinet` and `wiring` views from runtime-owned contracts
  - active-view switching changes the active desktop projection snapshot without semantic mutation
  - selected canonical semantic identity can remain inspectable across view switching when the object exists in both views
  - selection, pan, zoom, and view switching remain non-authoritative state
  - existing desktop bootstrap and connect-ports paths remain green
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `2.1` already introduced `AthenaRuntimeProjectionSession`, supported-view discovery, runtime-owned active-view switching, and geometry-backed active snapshots in `:kernel:runtime`.
- Desktop workbench state currently rebuilds from the legacy `projectViewerProjection()` path and keeps semantic viewer interaction state local to the render stage.
- `AthenaSemanticViewerInteractionState` already proves local selection, pan, zoom, and hit-testing behavior. Story `2.2` should reuse that behavior rather than inventing a second interaction system.
- The existing desktop workbench session already hosts the GUI-backed `connect ports` flow and must keep that mutation path intact.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline commit.
- Story `2.1` moved multi-view ownership into `:kernel:runtime`.
- Story `2.2` should move desktop consumption onto the runtime-owned projection session without reintroducing desktop-owned projection truth.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 2.2: Expose Runtime-Owned Projection Snapshots To The Desktop Surface`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-06/prd.md`
  - `FR-5`
  - `FR-6`
  - `FR-10`
  - `NFR-5`
  - `NFR-6`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-06/ARCHITECTURE-SPINE.md`
  - `AD-2`
  - `AD-3`
  - `AD-4`
  - `AD-5`
  - `AD-6`
  - `AD-7`
  - `AD-8`
- `manifesto/docs/architecture/09-layout-and-geometry.md`
- `_bmad-output/implementation-artifacts/m2/2-1-host-runtime-projection-sessions-and-supported-view-switching.md`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellState.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`

## Story Completion Status

- Status: done
- Completion note: The desktop workbench now consumes runtime-owned projection-session state, switches active views through `Athena Runtime`, and preserves canonical semantic inspection identity across supported view changes without moving projection authority into the UI.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `git rev-parse HEAD`
- `java25; .\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added shell-facing projection-session DTOs and new desktop intents for runtime-owned active-view switching and semantic selection inspection.
- Rebuilt the desktop workbench session from `Athena Runtime` projection-session state instead of the legacy single-view assembly path.
- Kept desktop selection anchored to canonical semantic ids and preserved that inspected identity across `cabinet` to `wiring` view switching.
- Updated the shared semantic viewer stage to accept external semantic selection while keeping pan, zoom, viewport, and hit-testing disposable UI state.
- Added desktop and compose-workbench regression tests for supported views, active-view switching, and canonical selection continuity.
- Updated the English and Chinese READMEs for `:apps:desktop-viewer` and `:ui:compose-workbench` to document the desktop projection-inspection boundary.

## File List

- `_bmad-output/implementation-artifacts/m2/2-2-expose-runtime-owned-projection-snapshots-to-the-desktop-surface.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `apps/desktop-viewer/README.md`
- `apps/desktop-viewer/README.zh-CN.md`
- `apps/desktop-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/desktop-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `ui/compose-workbench/README.md`
- `ui/compose-workbench/README.zh-CN.md`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellState.kt`
- `ui/compose-workbench/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaSemanticViewerStage.kt`
- `ui/compose-workbench/src/commonTest/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellStateTest.kt`

## Change Log

- 2026-07-07: Created Story `2.2` with desktop projection-inspection implementation guidance.
- 2026-07-07: Implemented runtime-owned desktop projection inspection, active-view switching, canonical selection continuity, tests, and module documentation updates.
