---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.5: Recompute Affected Scope And Refresh Validation And Rendering

Status: done

## Story

As an operator or reviewer,
I want Athena to recompute only the affected semantic scope after a command-backed change and refresh downstream validation and rendering,
so that interactive runtime behavior stays fast, inspectable, and consistent with canonical semantics without falling back to whole-project recompilation by default.

## Acceptance Criteria

1. Given a semantic mutation has been executed through the `Command Runtime`, when the runtime evaluates post-change work, then it identifies the affected semantic scope from the changed canonical identities and relationships, and the affected-scope decision is derived through runtime-owned dependency logic rather than UI-local heuristics.
2. Given affected scope has been identified after a change, when downstream processing is triggered, then Athena reruns validation and rendering only for the required semantic subset when the change allows it, and the incremental behavior remains consistent with canonical semantic rules and stable identities.
3. Given a GUI mutation changes the active project, when the runtime completes incremental follow-up work, then diagnostics and viewer-facing rendered output refresh through runtime-coordinated services, and the result remains inspectable rather than hidden behind disposable UI-only caches.
4. Given incremental recomputation support is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and affected-scope validation and rendering updates work over the active project, and the implementation demonstrates interactive readiness without requiring whole-project recompilation as the default path.

## Tasks / Subtasks

- [x] Add runtime-visible affected-scope and incremental-update reporting contracts. (AC: 1, 2, 3)
  - [x] Introduce documented compiler/runtime report types for changed scope, validation scope, render scope, and scoped-vs-fallback mode.
  - [x] Keep the report inspectable from runtime-managed compilation state after command-backed recompute.
  - [x] Derive affected scope from canonical semantic identities and runtime-owned graph relationships rather than GUI-local hints.
- [x] Evolve the recompute path to support scoped validation and render refresh for the current M1 command slice. (AC: 1, 2, 4)
  - [x] Extend compiler recompute to accept incremental scope inputs instead of always behaving like a whole-document refresh.
  - [x] Add scoped semantic validation for the current connection-focused mutation path while preserving deterministic fallback behavior.
  - [x] Add scoped render refresh for the current connection-focused mutation path while preserving canonical render consistency.
- [x] Refresh runtime and GUI-facing surfaces from incremental recompute state. (AC: 2, 3, 4)
  - [x] Ensure command execution, undo, redo, and replay preserve or rebuild incremental reports over runtime-managed canonical state.
  - [x] Surface refreshed diagnostics and incremental scope details through runtime-managed compilation and desktop workbench state.
  - [x] Keep viewer output and diagnostics refresh runtime-owned and inspectable.
- [x] Document and verify the incremental recompute boundary. (AC: 1, 2, 3, 4)
  - [x] Add focused tests for affected-scope derivation and scoped validation/render refresh.
  - [x] Add one GUI-facing proof that diagnostics and rendered state refresh after a runtime-backed mutation.
  - [x] Add architecture-facing documentation and keep all Gradle verification sequential on Windows with Java `25`.

## Dev Notes

### Story Intent

- Story `2.5` should introduce a real incremental seam, not a fake runtime wrapper around whole-project recomputation.
- The narrow proof target is the existing M1 `CONNECT_PORTS` command path plus related undo, redo, and replay behavior.
- The goal is inspectable incremental recompute metadata and refreshed diagnostics/render state, not a generalized future incremental engine.

### Architecture Guardrails

- `Engineering IR` remains the only canonical semantic authority.
- `Athena Runtime` owns the affected-scope decision and post-command recompute orchestration.
- The GUI must consume refreshed diagnostics and render state from runtime-managed compilation results rather than recalculating them privately.
- Incremental scope decisions must be deterministic and explainable from canonical identities and runtime graph relationships.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin contracts documented with KDoc.
- Prefer a narrow scoped implementation for the current connection-mutation path plus deterministic fallback where broader coverage is not yet supported.
- Avoid introducing a second semantic pipeline or bypassing existing compiler/runtime ownership.

### Previous Story Intelligence

- `AthenaExecutionContext.replaceActiveProjectDocument(...)` currently skips parse/lower but still relies on compiler recompute for semantic validation and rendering.
- `AthenaCompiler.recompute(...)` currently rebuilds semantic validation and render output globally; no affected-scope contract exists yet.
- Story `2.4` added a runtime-backed desktop workbench session and GUI command path, but diagnostics are still minimal and incremental scope is not surfaced.
- Story `2.3` already records command history; undo, redo, and replay must remain aligned with any new incremental report state.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Minimum verification commands for story completion:
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- Follow red-green-refactor. No production incremental recompute code should land before the first failing tests exist.
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.5: Recompute Affected Scope And Refresh Validation And Rendering`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-16`, `FR-17`, `FR-20`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-3`, `AD-4`, `AD-6`

## Story Completion Status

- Status: review

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Targeted red tests were added first in `runtime` and `apps:compose-viewer` for scoped recompute reporting and GUI refresh visibility.
- Runtime now derives affected semantic scope from changed stable identities and reuses that scope for command execution, undo, redo, and replay.
- Compiler recompute now accepts affected scope plus previous rendering, emits an inspectable incremental update report, and attempts scoped validation and connection-line render refresh before deterministic full fallback.
- Runtime now republishes diagnostics and incremental recompute metadata through runtime-owned surfaces so the Compose workbench does not depend on compiler internals.
- Added the architecture note `docs/compiler/m1-incremental-recompute-boundary.md` and linked it from the runtime-host boundary.
- Sequential Java 25 verification passed with targeted tests, repo-wide `build`, repo-wide `test`, and `:apps:compose-viewer:bootstrapSmoke`.

### Completion Notes List

- Implemented runtime-visible affected scope planning and incremental update reporting for the M1 `CONNECT_PORTS` mutation slice.
- Scoped validation and scoped render refresh now run during recompute when the previous render model can be safely reused.
- Compose workbench diagnostics and console output now expose runtime-owned incremental refresh metadata after GUI-backed semantic mutation.
- Verification remained strictly sequential on Windows and used `java25` for every Gradle command.

### File List

- `_bmad-output/implementation-artifacts/2-5-recompute-affected-scope-and-refresh-validation-and-rendering.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModels.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/SvgRenderModelDeriver.kt`
- `docs/compiler/m1-incremental-recompute-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/EngineeringIrValidator.kt`

### Change Log

- Added runtime-derived affected scope planning, compiler incremental recompute reporting, scoped validation, and scoped render refresh for the current M1 command slice.
- Surfaced runtime-owned diagnostics and incremental recompute metadata into the Compose workbench after GUI-backed command execution.
- Documented the new incremental recompute boundary and verified the implementation sequentially on Java 25.
