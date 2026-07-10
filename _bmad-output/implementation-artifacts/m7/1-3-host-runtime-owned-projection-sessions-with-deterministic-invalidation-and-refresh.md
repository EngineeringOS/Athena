---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 1.3: Host Runtime-Owned Projection Sessions With Deterministic Invalidation And Refresh

Status: ready-for-dev

## Story

As a platform engineer,
I want runtime-owned projection sessions to control active graphical state,
so that stale or conflicting projection state is rebuilt deterministically from canonical inputs instead of surviving privately in the frontend.

## FR Traceability

- FR-1: stable projection boundary for graphical workbench delivery
- FR-2: keep graphical projection downstream of semantic authority
- FR-6: deterministic graphical refresh from the same underlying semantic state
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same upstream semantic state and chosen view yield the same projection state
- NFR-3: projection output remains inspectable
- NFR-5: later richer interaction must not collapse into unrestricted editing

## Acceptance Criteria

1. Given an active repository session and one or more graphical views, when Athena opens graphical projection state, then runtime owns active `ProjectionSession` state beside the existing repository and semantic SCM context, and frontend workbench state remains disposable downstream state.
2. Given upstream semantic, repository, or semantic SCM state changes, when the active projection session refreshes, then stale projection state is invalidated and rebuilt deterministically from the current canonical inputs, and unapproved frontend-local state does not persist as authority.

## Tasks / Subtasks

- [ ] Turn the current ad hoc projection-session rebuild path into explicit runtime-owned session state. (AC: 1, 2)
  - [ ] Treat Stories `1.1` and `1.2` as hard prerequisites. If `:kernel:projection-model` or compiler-owned projection-model materialization is not landed, complete those first instead of extending the legacy geometry-only path as the M7 authority model.
  - [ ] Replace the current "rebuild on every request" projection-session behavior in `:kernel:runtime` with an explicit runtime-owned session lifecycle/state model rooted in `AthenaExecutionContext` and compatible with `RepositoryGraphSession`.
  - [ ] Keep all new public/core Kotlin runtime classes documented with KDoc.
- [ ] Bind projection-session ownership to authoritative runtime context instead of frontend or adapter state. (AC: 1)
  - [ ] Ensure one active projection session is owned beside the active execution context and can coexist with:
    - the active `RepositoryGraphSession`
    - runtime-owned semantic SCM state services
    - runtime-owned command/history/diff services
  - [ ] Preserve the current runtime rule that repository/package authority stays in `RepositoryGraphSession` and semantic review/history authority stays in the existing M6 runtime services rather than being duplicated inside the projection session.
  - [ ] Keep `activeViewId` and equivalent projection-session choices runtime-owned only; do not let Theia, desktop viewers, or future graph adapters become the durable owner.
- [ ] Add deterministic invalidation and refresh rules over current canonical inputs. (AC: 2)
  - [ ] Invalidate stale projection-session state on at least the concrete upstream changes already present in the repo:
    - successful semantic command mutation / undo / redo / replay
    - repository graph session activation or replacement
    - active project/workspace replacement
    - active view change
  - [ ] Rebuild invalidated session state from the current canonical runtime/compiler inputs rather than widget-local caches or protocol payload echoes.
  - [ ] If semantic SCM request/baseline context is associated with the active session, keep that invalidation explicit and typed; do not fabricate a new SCM meaning model.
  - [ ] Ensure unapproved frontend-local projection state is discarded or snapped back on refresh instead of being merged back as authority.
- [ ] Consume the new compiler-owned projection-model boundary while preserving current downstream compatibility. (AC: 1, 2)
  - [ ] Make runtime session state consume compiler-provided projection-model output from Story `1.2` as the M7 primary projection source.
  - [ ] Keep any legacy geometry/viewer adaptation as a downstream compatibility layer only, such as `AthenaRuntimeViewerProjection`; do not let that compatibility adapter remain the primary authority path.
  - [ ] Do not add LSP transport methods, Theia DTOs, graph-adapter payloads, or renderer-framework contracts in this story.
- [ ] Cover the runtime-owned session lifecycle with focused regression tests and documentation. (AC: 1, 2)
  - [ ] Add/extend runtime tests for:
    - explicit session ownership/lifecycle
    - deterministic refresh after command-backed semantic mutation
    - repository-session replacement invalidation
    - active-view switching over the same canonical state
    - unavailability behavior when canonical inputs are invalid
  - [ ] Keep existing command runtime, semantic SCM state, viewer compatibility, and desktop/runtime consumer tests green.
  - [ ] Update `kernel/runtime/README.md` and `kernel/runtime/README.zh-CN.md` to describe runtime-owned projection-session lifecycle, invalidation, and refresh boundaries.

## Dev Notes

### Story Intent

- Story `1.3` is the runtime-ownership story for M7.
- The success condition is not "the IDE already consumes graphical sessions." It is "runtime owns one explicit projection-session state machine and refreshes it deterministically from canonical inputs."
- Story `1.3` must stop before IDE transport, graph adapters, or frontend graphical panels.
- Story `1.4` owns typed projection queries and governed commands through `ide/lsp`.
- Epic `2.x` owns graph adapter placement and the first real graphical workbench surface.
- Epic `3.x` owns the first relationship-forward renderer proof, electrical projection contributions, and technology-path validation.

### Architecture Guardrails

- Align to inherited AD-17 by keeping one runtime-owned authoritative repository session per product window and hosting projection-session state beside it rather than as a detached viewer cache. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to inherited AD-19 by preserving semantic SCM as a dedicated VCS-neutral runtime/core above repository meaning rather than baking a second review/history model into projection-session state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to AD-27 by consuming one canonical `ProjectionModel` boundary rather than rebuilding runtime projection state from raw engineering entities or frontend-local shapes. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-27]
- Align to AD-29 by treating layout, geometry, grouping, routing, and viewport information as projection metadata and invalidating stale session state when upstream semantic/repository/semantic-SCM state changes. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-29]
- Align to AD-30 by keeping the active graphical view fed from runtime-owned projection sessions and deferring transport concerns to later `ide/lsp` work. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by ensuring any unapproved frontend interaction remains transient only and is discarded or snapped back on refresh. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Preserve inherited AD-23 by keeping frontend/backend surfaces downstream bridges, not session authorities. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Runtime session ownership must build on the current proven runtime seams rather than replacing them:
  - `AthenaRuntime`
  - `AthenaWorkspace`
  - `AthenaExecutionContext`
  - `RepositoryGraphSession`
  - `AthenaSemanticScmStateService`
  - `AthenaCommandRuntimeService`
- Reuse the current cached canonical compilation behavior in `AthenaExecutionContext.compileActiveProject()` and `replaceActiveProjectDocument(...)` rather than inventing a second semantic cache.
- Promote the session source hierarchy to the M7 model:
  - compiler/materialized `ProjectionModel` is primary
  - legacy geometry/viewer scene adaptation is downstream compatibility only
- Keep refresh deterministic and inspectable:
  - the same canonical semantic state plus the same chosen view must yield the same runtime session state
  - invalidation triggers must be explicit and tied to authoritative upstream changes
  - unavailable canonical inputs must surface unavailable session state, not stale ready state
- Do not make semantic SCM state a hidden dependency of ordinary projection rendering. Session state may sit beside SCM context, but it must not silently rewrite projection meaning from baseline review/history results.
- Add clean KDoc for all new public/core runtime types.

### Architecture Compliance

- The story is only successful if session authority becomes easier to point to:
  - compiler materializes canonical projection-model output
  - runtime hosts active projection-session state
  - runtime invalidates and refreshes it deterministically
  - frontend state remains disposable
- Prevent these failure modes:
  - `projectProjectionSession()` still reconstructs full authority ad hoc on each request with no explicit invalidation model
  - runtime session state is stored in desktop/Theia/frontend modules
  - repository/package state or semantic SCM state is duplicated inside projection-session payloads as a second authority model
  - runtime continues treating the legacy viewer scene as the primary M7 projection source after Story `1.2`
  - Story `1.3` widens into LSP transport, adapter payloads, or renderer implementation work

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Extend the current sibling-module Gradle pattern only as needed:
  - `:kernel:runtime` should depend on `:kernel:projection-model` once Stories `1.1` and `1.2` land
- Reuse the current Kotlin/JUnit test conventions from runtime, compiler, and semantic SCM modules.
- No third-party dependency should be added just to manage session lifecycle or refresh invalidation.
- Node/Theia/graph-framework concerns remain later-story dependencies only.

### File Structure Requirements

- Expected update files:
  - `kernel/runtime/build.gradle.kts`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
  - `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt`
  - `kernel/runtime/README.md`
  - `kernel/runtime/README.zh-CN.md`
- Possible focused add files if needed:
  - one dedicated runtime session-state/lifecycle file under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - one focused runtime session invalidation test file under `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
- Files whose current behavior and ownership must be preserved:
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
    - currently owns cached canonical compilation, active projection view id, and command-triggered recompute
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt)
    - currently routes command mutation, undo/redo/replay, diff inspection, and projection consequences through runtime-owned canonical state
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt)
    - remains the authoritative repository/package session and must not be reduced to projection metadata
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt)
    - remains the typed M6 semantic SCM projection service and must not be replaced by projection-session logic
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt)
    - remains a compatibility-facing adapter and should not become the session authority model
- Explicit non-goals:
  - no `ide/lsp` methods or transport contracts
  - no Theia/frontend panel lifecycle
  - no graph-adapter layer under `integrations/graph-*`
  - no renderer-proof logic
  - no semantic SCM feature expansion unrelated to projection-session adjacency

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test"`
- Recommended focused compatibility regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test"`
- Optional wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - repeated session requests with unchanged canonical inputs yield the same runtime-owned session state
  - successful semantic command mutation invalidates and refreshes session state deterministically
  - repository-session replacement or workspace/project replacement invalidates stale session state
  - unsupported/invalid canonical inputs do not keep a stale ready projection session alive
  - semantic SCM state inspection remains deterministic and separate from projection-session authority
  - legacy viewer consumers still work through runtime-owned downstream adaptation
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt) currently owns workspace lifecycle, active workspace, active repository graph session, and active execution context.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt) currently supports both `activateProject(...)` and `activateRepositoryGraphSession(...)`; Story `1.3` must preserve both flows while making runtime-owned projection sessions repository-aware when repository state is active.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt) currently caches `CompilerCompilationResult`, stores `activeProjectionViewId`, and exposes `projectProjectionSession()` as a rebuild-on-demand path.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently rebuilds supported views and geometry-backed snapshots directly from compiler output each time; Story `1.3` should convert this into explicit runtime-owned session state with deterministic invalidation/refresh.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt) currently invalidates canonical state only indirectly by replacing the active project document and recomputing compiler outputs; Story `1.3` should hook projection-session invalidation to this path rather than inventing a parallel mutation flow.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt) currently provides deterministic baseline/review/commit state over `RepositoryGraphSession` and should remain untouched except where explicit typed session adjacency is required.
- [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt) and [`kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`](../../../kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt) already prove active-view switching and command-triggered canonical refresh; extend those tests rather than building a separate proof harness.

### Previous Milestone Intelligence

- Story `1.2` freezes the requirement that projection-model output is materialized once in compiler and later consumed by runtime.
- M2 Story `2.1` already proved runtime-owned supported-view hosting and active-view switching; M7 `1.3` should upgrade that into an explicit runtime-owned session lifecycle rather than restart from scratch.
- M2 Story `2.3` already proved command-triggered scoped refresh metadata after `connect ports`; M7 `1.3` should reuse that recompute/invalidation basis instead of inventing a second refresh engine.
- M6 already proved deterministic runtime-owned semantic SCM and history state beside repository sessions; M7 `1.3` should sit beside those services, not absorb them.
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
  - keep runtime session ownership explicit and typed
  - do not smuggle IDE transport or graph-framework behavior into runtime before Story `1.4`

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M7 architecture and root build documentation:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Node, Theia, and graph-framework choices remain relevant to later stories, but Story `1.3` should stay JVM-first and runtime-boundary focused.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- This story sits at the seam between:
  - `kernel/projection-model`
  - `kernel/runtime`
  - existing `kernel/repository-model` and `kernel/semantic-scm` consumers
- Naming should stay easy to understand and avoid transport/framework leakage:
  - runtime-owned `ProjectionSession`
  - explicit session invalidation/refresh state
  - repository graph session remains a separate authoritative runtime contract
- The story should make the future M7 path easier to explain:
  - compiler materializes projection-model
  - runtime hosts/invalidates/refreshes projection sessions
  - LSP exposes them later
  - frontend/adapters consume them later

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m7/1-2-materialize-deterministic-projection-state-from-engineering-layout-and-geometry-inputs.md]
- [Source: _bmad-output/implementation-artifacts/m2/2-1-host-runtime-projection-sessions-and-supported-view-switching.md]
- [Source: _bmad-output/implementation-artifacts/m2/2-3-refresh-projection-state-after-one-supported-semantic-mutation-path.md]
- [Source: kernel/runtime/build.gradle.kts]
- [Source: kernel/runtime/README.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateService.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjectionTest.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt]
- [Source: kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateServiceTest.kt]

## Story Completion Status

- Status: ready-for-dev
- Completion note: Ultimate context engine analysis completed - comprehensive developer guide created for runtime-owned M7 projection-session lifecycle, invalidation, and refresh.

## Dev Agent Record

### Agent Model Used

Story context generation only.

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- prior M7 `1.2` story review for projection-model prerequisites
- M2 runtime-session and scoped-refresh story review for runtime precedent
- CodeGraph exploration of runtime host/session lifecycle, repository-session ownership, and projection refresh paths
- live runtime service, workspace, command, semantic SCM, test, and README review
- recent commit history review

### Completion Notes List

- Identified Story `1.3` as the next backlog story in M7 after `1.2`.
- Locked M7 runtime session authority to `:kernel:runtime` beside repository-session and semantic SCM state rather than frontend caches.
- Explicitly constrained Story `1.3` to consume compiler-owned projection-model output from Story `1.2` and defer transport to Story `1.4`.
- Flagged the current ad hoc `projectProjectionSession()` rebuild path as the primary ownership gap this story must fix.
- Captured command/history/recompute and repository-session activation as the concrete existing invalidation triggers the implementation must reuse.

### File List

- _bmad-output/implementation-artifacts/m7/1-3-host-runtime-owned-projection-sessions-with-deterministic-invalidation-and-refresh.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
