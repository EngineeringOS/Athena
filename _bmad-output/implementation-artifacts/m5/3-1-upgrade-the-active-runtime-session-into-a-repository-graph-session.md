---
baseline_commit: a9b3a12593a428305bb342f3594226aff1120de2
---

# Story 3.1: Upgrade The Active Runtime Session Into A `RepositoryGraphSession`

Status: done

## Story

As an engineer,
I want Athena to upgrade the active runtime session to carry repository manifest, lock, resolved package graph, and package diagnostics,
so that the opened repository behaves like a governed package workspace instead of a light M4 proof root.

## FR Traceability

- FR-7: upgrade the active runtime-backed repository session into a package graph session
- FR-8: keep repository open and create flows compatible with the governed M5 contract
- FR-9: surface package state in the existing Athena IDE path
- FR-10: narrow language-surface hardening stays downstream of runtime session authority
- FR-12: preserve later graphical projection without widening M5 beyond package operation
- NFR-1: repository/package meaning remains owned by compiler and runtime layers
- NFR-2: resolved session state remains deterministic from the same repository state
- NFR-3: manifest, lock, graph, and diagnostics remain inspectable for debugging

## Acceptance Criteria

1. Given a governed Athena repository is opened, when the session-upgrade path runs after governed repository open, then Athena creates one runtime-owned `RepositoryGraphSession` for the product window, and that session carries manifest state, lock state, resolved package graph state, and package diagnostics, and it upgrades the earlier contract-aware open seed into the authoritative runtime session model.
2. Given a new repository is opened in the same window, when the session changes, then the previous session is replaced according to the inherited one-window / one-session rule, and package-graph authority remains in the JVM runtime rather than moving into frontend state.

## Tasks / Subtasks

- [x] Add a runtime-owned `RepositoryGraphSession` model that carries canonical repository state. (AC: 1, 2)
  - [x] Introduce a typed runtime session result/model under `kernel/runtime` that holds repository root, canonical repository/package report state, and the active project/execution context needed by the existing runtime path.
  - [x] Keep repository manifest, lock, graph, and package diagnostics sourced from the compiler/runtime publication chain rather than restating them in Node or LSP-only models.
  - [x] Use `RepositoryGraphSession` consistently as the M5 noun instead of letting `workspace` remain the primary semantic state carrier.
- [x] Upgrade the runtime open flow from contract-aware seed state to authoritative repository graph session state. (AC: 1, 2)
  - [x] Extend `AthenaRuntime` and/or `AthenaWorkspace` so opening a governed repository can publish and own one active `RepositoryGraphSession`.
  - [x] Preserve existing execution-context behavior for active project operations while making the repository graph session the authoritative runtime-owned package state.
  - [x] Replace the previous session cleanly when a new repository is opened in the same window.
- [x] Keep the LSP host downstream of the runtime-owned repository graph session instead of duplicating session authority. (AC: 1)
  - [x] Update the LSP session host/open path so it consumes the runtime `RepositoryGraphSession` rather than reconstructing repository/package state independently.
  - [x] Preserve the current light transport snapshot shape where possible; this story should not widen into Story `3.2` protocol design.
  - [x] Keep Theia frontend/backend as orchestration and projection only; do not move session authority into Node.
- [x] Cover valid session activation and replacement behavior with focused tests. (AC: 1, 2)
  - [x] Prove one repository open creates a runtime-owned `RepositoryGraphSession` with canonical manifest, lock, graph, and diagnostics.
  - [x] Prove opening another repository in the same runtime window replaces the previous session according to the one-window / one-session rule.
  - [x] Prove LSP activation still succeeds by consuming runtime-owned session state rather than the older proof-only seed.
- [x] Run sequential Java 25 verification for runtime and LSP regressions. (AC: 1, 2)
  - [x] Run focused module tests first.
  - [x] Run the wider sequential Gradle regression command.

## Dev Notes

### Story Intent

- Epic 1 established the governed repository contract and open seed path.
- Epic 2 established deterministic resolution input, package graph, `athena.lock`, and canonical repository report publication.
- Story `3.1` now upgrades the active runtime-owned session so those canonical package semantics live in one explicit `RepositoryGraphSession` instead of being scattered across workspace seed data and LSP host summaries.

### Architecture Guardrails

- Align to AD-17: the active session becomes one runtime-owned `RepositoryGraphSession` per product window.
- Align to AD-18: keep M5 IDE work additive and package-operability-scoped.
- Preserve compiler/runtime authority from AD-13 through AD-16: the session consumes compiler-owned repository publication, not filesystem guesswork or frontend mirrors.
- Avoid turning Story `3.1` into Story `3.2`; protocol payload redesign belongs later.

### Technical Requirements

- Reuse Story `2.4` publication output rather than rebuilding repository/package meaning:
  - `AthenaCompiler.publishRepositoryGraphReport(repositoryRoot)`
  - `AthenaRepositoryReportService.publishRepositoryGraphReport(repositoryRoot)`
- Current runtime seams:
  - `AthenaRuntime.openWorkspace(rootPath)`
  - `AthenaWorkspace.activateProject(projectName, sourcePath)`
  - `AthenaExecutionContext` remains the active project execution surface
- Current LSP seams:
  - `AthenaRepositoryResolver.resolve(repositoryRoot)` still provides the deterministic authored-source seed
  - `AthenaLspSessionHost.activateRepository(repositoryRoot)` currently creates only the older session-ready summary
- Story `3.1` should introduce:
  - one runtime-owned `RepositoryGraphSession` model
  - one authoritative runtime accessor for the active repository graph session
  - session replacement behavior when a new repository root is opened
- Keep this story out of scope for:
  - new Node/TypeScript repository models
  - Athena-namespaced LSP package-state methods and payload design
  - frontend visualization or package feedback panels
  - semantic SCM or publish/review workflows

### Architecture Compliance

- Prevent these failure modes:
  - repository/package session meaning split between runtime, LSP snapshot state, and Theia frontend state
  - runtime session state omits canonical lock/graph/diagnostic information already proven in Epic 2
  - `AthenaWorkspace` remains the hidden semantic authority instead of the explicit `RepositoryGraphSession`
  - Story `3.1` silently expands into Story `3.2` transport design or Story `3.3` IDE feedback work
  - one-window / one-session replacement semantics regress when another repository opens

### Library / Framework Requirements

- Use the frozen local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- No new dependencies are justified.

### File Structure Requirements

- Expected update files:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
- Likely new files:
  - one or more runtime session model files under `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/`
  - matching runtime tests under `kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/`
  - possibly updated LSP tests under `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Files whose current behavior must be preserved:
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryResolver.kt`
    - still owns only the deterministic authored-source seed selection, not canonical package authority
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRepositoryReportService.kt`
    - remains a runtime consumer of compiler publication output
  - existing runtime command/projection flows
    - should keep using `AthenaExecutionContext` once a session is active

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"`
- Preferred wider regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`
- Required proof checks:
  - runtime owns one active `RepositoryGraphSession`
  - the session carries canonical manifest, lock, graph, and diagnostics from Epic 2 authority
  - opening a second repository replaces the first session
  - LSP activation continues by consuming the runtime-owned session
- Keep all Gradle verification sequential on Windows. Do not run Gradle commands concurrently with each other.

### Current Code State To Preserve

- `AthenaRuntime` currently owns `activeWorkspace` and `activeExecutionContext`, but not yet an explicit repository graph session.
- `AthenaWorkspace` currently exposes repository report accessors and project activation, but repository graph semantics are not yet materialized into one session model.
- `AthenaLspSessionHost` still says it is not the full `RepositoryGraphSession` yet; Story `3.1` is the point where that changes.
- `AthenaLanguageServer` currently stores a transport snapshot plus `AthenaLspSessionHostReady`; it should stay downstream of runtime authority after this story.

### Previous Story Intelligence

- Story `2.4` already created the canonical publication seam the runtime session should consume.
- Story `2.4` intentionally stopped short of `RepositoryGraphSession`; this story should build directly on that seam instead of bypassing it.
- The user's standing constraints still apply:
  - package root stays `com.engineeringood`
  - Java `25` is mandatory
  - physical workspace structure must match architecture
  - Gradle verification must stay sequential on Windows

### Git Intelligence Summary

- Recent M5 work progressed in a narrow sequence: contract -> input -> graph -> lock -> published report.
- Practical implication:
  - Story `3.1` should be the next narrow runtime authority step, not an IDE surface expansion.

### Latest Technical Information

- No web research is required for Story `3.1`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- Story `3.1` should leave clean room for:
  - Story `3.2` LSP package-state exposure
  - Story `3.3` IDE package feedback
  - Story `3.4` narrow editor hardening
  - later M6 semantic SCM flows

### References

- [Source: _bmad-output/planning-artifacts/epics-M5-2026-07-08.md#story-31-upgrade-the-active-runtime-session-into-a-repositorygraphsession]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-08-m5/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-08-m5/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m5/2-4-publish-package-diagnostics-and-graph-reports-for-runtime-ide-and-m6-foundations.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M5 epics, PRD, addendum, and architecture spine review for `RepositoryGraphSession`
- Story `2.4` completion notes and repository publication seam review
- CodeGraph review of `AthenaRuntime`, `AthenaWorkspace`, `AthenaExecutionContext`, and `AthenaLspSessionHost`
- runtime and LSP regression test review

### Completion Notes List

- Added runtime-owned `RepositoryGraphSession` as the explicit M5 repository/package authority carrying the canonical publication output plus active execution context.
- Updated `AthenaRuntime` and `AthenaWorkspace` so one governed repository activation creates and owns exactly one active repository graph session, and opening another repository replaces it.
- Updated `AthenaLspSessionHost` to consume the runtime-owned repository graph session while keeping the existing transport summary narrow.
- Added focused runtime and LSP tests for session activation and replacement behavior.
- Verified sequentially with `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"` and `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"`.

### File List

- _bmad-output/implementation-artifacts/m5/3-1-upgrade-the-active-runtime-session-into-a-repository-graph-session.md
- _bmad-output/implementation-artifacts/m5/sprint-status.yaml
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRepositoryGraphSessionTest.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHostTest.kt

## Change Log

- 2026-07-09: Created Story `3.1` context and moved it to ready-for-dev.
- 2026-07-09: Implemented runtime-owned `RepositoryGraphSession` activation and replacement, updated the LSP host to consume it, and moved the story to review after sequential Java 25 verification.
