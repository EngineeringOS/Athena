---
baseline_commit: ad382d8a2d1841771c5b95e008f29f78a6f751cd
---

# Story 2.1: Host Runtime Projection Sessions And Supported View Switching

Status: done

## Story

As an operator,
I want `Athena Runtime` to expose supported projections for the active project and switch between `cabinet` and `wiring` views,
so that I can operate multiple synchronized projections through one runtime-owned session instead of ad hoc viewer state.

## Acceptance Criteria

1. Given an active project managed by `Athena Runtime`, when supported projections are requested, then the runtime exposes a projection session model for the active project, and that session owns the supported view list, active view identity, and projection snapshot access.
2. Given the first supported view pair is `cabinet` plus `wiring`, when the operator switches the active view, then the runtime can switch between those view definitions without mutating canonical semantic state, and the change is represented as runtime-owned projection state rather than semantic mutation.
3. Given view definitions are extension-contributed, when the runtime hosts them in a projection session, then it accepts only typed, non-sovereign view contributions, and unsupported or overreaching view definitions cannot silently become semantic authorities.
4. Given projection session hosting and view switching are implemented, when the standard Java `25` build and runtime checks are executed, then the workspace builds successfully and the active project can expose and switch between supported views through runtime-owned contracts, and the implementation keeps projection session ownership in `:kernel:runtime`.

## Tasks / Subtasks

- [x] Introduce an explicit runtime-owned projection session contract in `:kernel:runtime`. (AC: 1, 4)
  - [x] Add runtime types for projection session identity, supported view entries, active view identity, and active projection snapshot access.
  - [x] Keep canonical semantic ownership outside the session: the session is runtime state over compiler-owned projections, not a second semantic store.
  - [x] Keep all newly introduced core Kotlin classes documented with KDoc.
- [x] Host supported view definitions for the active project through the runtime-owned session. (AC: 1, 3)
  - [x] Reuse typed compiler/plugin view-definition contributions instead of inventing a separate runtime-only registry.
  - [x] Expose the first supported proof pair, `cabinet` and `wiring`, in deterministic order for the active project.
  - [x] Reject unsupported active-view requests explicitly through runtime-owned results or invariants.
- [x] Add runtime-owned active-view switching without semantic mutation. (AC: 2, 4)
  - [x] Store the active view inside runtime-owned projection state for the active project.
  - [x] Support switching between `cabinet` and `wiring` without changing canonical `Engineering IR`.
  - [x] Keep the existing single-view projection path compatible or adapt it to use the new session-owned active view.
- [x] Add deterministic runtime tests for projection sessions and supported view switching. (AC: 1, 2, 3, 4)
  - [x] Add tests that the active project exposes the supported view list and a default active view deterministically.
  - [x] Add tests that switching the active view changes runtime-owned projection state without mutating semantic state.
  - [x] Add tests that unsupported view ids are rejected and that typed hosted plugin view definitions remain the only accepted source.
- [x] Update runtime documentation for the first projection-session boundary. (AC: 4)
  - [x] Update `:kernel:runtime` docs to describe projection session ownership and supported view switching.
  - [x] Do not claim desktop-side session consumption, diff refresh behavior, or multi-view UI interaction beyond runtime hosting in this story.

## Dev Notes

### Story Intent

- Story `2.1` starts Epic `2` by moving multi-view ownership into `:kernel:runtime`.
- The success condition is runtime-owned projection session state, not yet the desktop multi-view UX. Desktop consumption is Story `2.2`.
- Story `1.5` already made `Geometry IR` the renderer-facing backend contract. Story `2.1` should reuse that compiler/runtime output rather than reopening backend derivation.

### Architecture Guardrails

- Align to `ARCHITECTURE-SPINE.md`:
  - `AD-2`: runtime owns lifecycle and orchestration.
  - `AD-3`: runtime owns projection sessions, active-view switching, projection snapshots, refresh coordination, and cache invalidation.
  - `AD-4`: view definitions remain typed contracts contributed by extensions.
  - `AD-5`: canonical semantic identity must remain stable across projection layers.
  - `AD-6`: renderer/viewer surfaces consume runtime-owned geometry-backed state, not semantic shortcuts.
  - `AD-7`: projection layers are inspectable but not authoritative mutation sources.
  - `AD-8`: projection contracts stay separated between compiler, runtime, backend, and UI modules.
- Align to `manifesto/docs/architecture/09-layout-and-geometry.md`: view changes stay downstream consequences over one semantic source and may not fork engineering meaning.

### Technical Requirements

- Add a runtime-owned projection session for the active project.
- The session must expose:
  - supported view definitions
  - active view id
  - projection snapshot access for the active view
- The first supported view pair remains:
  - `cabinet`
  - `wiring`
- Use typed extension-contributed view definitions already provided by the electrical runtime plugin and compiler/plugin inventory.
- Keep view switching ephemeral runtime state only; it must not mutate source text, canonical `Engineering IR`, `Layout IR`, or `Geometry IR`.
- Preserve the current active project execution model and cached compilation model.

### Architecture Compliance

- `:kernel:runtime` owns session state; `:kernel:compiler` still owns derivation.
- Projection sessions may cache or select among already-derived compiler outputs, but they may not redefine view semantics or author geometry.
- Unsupported view ids must fail explicitly rather than silently falling back to a different semantic meaning.
- Do not move projection ownership into `apps:desktop-viewer` or `ui:compose-workbench` in this story.

### Library / Framework Requirements

- Use the existing repo-pinned stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Keep dependency changes minimal and local to runtime/compiler tests unless required by the story.
- Reuse the current Kotlin/JUnit test approach.

### File Structure Requirements

- Likely update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Likely add files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- Preserve current behavior in:
  - `kernel/compiler/**` derivation ownership
  - `apps/desktop-viewer/**` except for compatibility with unchanged runtime APIs
  - command history and semantic diff systems, which belong to later stories

### Testing Requirements

- Minimum verification commands:
  - `java25`
  - `.\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
- Required proof checks:
  - active project exposes supported `cabinet` and `wiring` views in deterministic order
  - runtime stores and reports a default active view deterministically
  - switching the active view does not change canonical connections/components
  - unsupported view ids are rejected explicitly
  - existing single-view projection and desktop smoke/test paths remain green
- Keep Gradle verification sequential on Windows.

### Previous Story Intelligence

- Story `1.5` already made backend output geometry-backed and preserved runtime viewer compatibility through the compiler render model.
- Runtime already hosts:
  - workspace lifecycle
  - active project execution context
  - command runtime
  - graph projection
  - plugin runtime services
- Runtime already exposes typed hosted plugin view-definition contributions via `AthenaPluginRuntimeServices.viewDefinitionContributions()`.
- Runtime currently has only one `projectViewerProjection()` path and no explicit multi-view session owner. Story `2.1` should add that owner rather than overloading ad hoc viewer state.

### Git Intelligence Summary

- `ad382d8 Complete M1 runtime workspace and regroup modules` remains the current baseline commit.
- The M2 sequence so far established explicit view definitions, layout derivation, geometry derivation, and a geometry-backed backend.
- Story `2.1` should now move the active-view and projection-session state into runtime while preserving the existing compiler/backend division.

### References

- `_bmad-output/planning-artifacts/epics-M2-2026-07-06.md`
  - `Story 2.1: Host Runtime Projection Sessions And Supported View Switching`
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
- `_bmad-output/implementation-artifacts/m2/1-5-feed-geometry-ir-to-the-first-backend-and-publish-the-m2-proof-corpus.md`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `extensions/domain-electrical/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`

## Story Completion Status

- Status: done
- Completion note: Runtime now owns supported-view projection sessions, active-view switching, and active projection snapshots for the active project while preserving compiler-owned canonical semantics and legacy viewer compatibility.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- `git rev-parse HEAD`
- `java25; .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test`
- `java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test`
- `java25; .\gradlew.bat --no-daemon --console=plain build`

### Completion Notes List

- Added `AthenaRuntimeProjectionSession` and related runtime-owned snapshot and switch-result contracts in `:kernel:runtime`.
- Hosted supported `cabinet` and `wiring` views from typed plugin/compiler view-definition contributions and enforced explicit rejection for unsupported view ids.
- Added runtime-owned active-view switching that preserves canonical compilation output and keeps view selection outside semantic state.
- Adapted legacy `projectViewerProjection()` to the active runtime projection session so existing desktop consumption stays compatible.
- Added deterministic runtime regression tests for supported views, default active view selection, switch behavior, semantic stability, and typed contribution alignment.
- Updated `:kernel:runtime` English and Chinese module READMEs to document projection-session ownership and supported-view switching.

## File List

- `_bmad-output/implementation-artifacts/m2/2-1-host-runtime-projection-sessions-and-supported-view-switching.md`
- `_bmad-output/implementation-artifacts/m2/sprint-status.yaml`
- `kernel/runtime/build.gradle.kts`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
- `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
- `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
- `kernel/runtime/README.md`
- `kernel/runtime/README.zh-CN.md`

## Change Log

- 2026-07-06: Created Story `2.1` with runtime-owned projection-session and supported-view-switching implementation guidance.
- 2026-07-07: Implemented runtime-owned projection sessions, supported-view switching, runtime regression tests, and module documentation updates.
