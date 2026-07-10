---
baseline_commit: adb0ae5d1956b13d8aa3df8774e102dfc09ef380
---

# Story 1.4: Expose Typed Projection Queries And Governed Commands Through `ide/lsp`

Status: done

## Story

As an IDE engineer,
I want Athena to expose projection state through typed queries and an explicit governed command set,
so that graphical clients can inspect and request allowed actions without gaining ownership of semantic or projection truth.

## FR Traceability

- FR-1: stable projection boundary for graphical workbench delivery
- FR-2: keep graphical projection downstream of semantic authority
- FR-4: support graphical navigation and projection-oriented inspection
- FR-5: publish explicit read-only versus editable rules
- FR-6: deterministic graphical refresh from the same underlying semantic state
- FR-7: prepare for later interactive graphical work without locking final editing scope
- NFR-1: graphical surfaces remain downstream of canonical semantic, repository, package, and SCM meaning
- NFR-2: the same upstream semantic state and chosen view yield the same projection state
- NFR-3: projection boundary output remains inspectable
- NFR-5: later richer interaction must not collapse into unrestricted editing

## Acceptance Criteria

1. Given runtime-owned projection sessions exist, when the IDE requests graphical state, then `ide/lsp` exposes typed projection queries and deterministic projection updates over Athena-owned protocol seams, and the transport remains inspectable and stable enough for later richer graphical milestones.
2. Given graphical interaction needs to request state changes, when command surfaces are reviewed, then only an explicit allowlist of governed commands is exposed, and generic runtime control tunnels or graph-framework-owned authority are not introduced.

## Tasks / Subtasks

- [x] Add typed projection request and response contracts under `:ide:lsp`. (AC: 1)
  - [x] Define Athena-owned protocol DTOs for runtime projection-session inspection rather than reusing frontend, renderer, or graph-framework payloads.
  - [x] Expose at least the runtime-owned projection concerns already proven in Stories `1.1` to `1.3`, such as active/supported views, projection-session status, deterministic projection snapshot data, and inspectable unavailable diagnostics where applicable.
  - [x] Keep all new public/core Kotlin classes documented with KDoc.
- [x] Expose projection queries through additive `AthenaLanguageServer` request surfaces. (AC: 1)
  - [x] Reuse the existing `AthenaLspSessionHost` and runtime-backed active session instead of opening a second runtime or repository-session path for graphical clients.
  - [x] Preserve the existing Athena request surfaces for repository session, semantic inspection, semantic SCM state, and semantic history state.
  - [x] Keep projection transport Athena-owned and rooted at `ide/lsp`; do not bypass LSP with ad hoc HTTP, direct runtime calls, or graph-framework protocol ownership.
- [x] Introduce an explicit governed command allowlist for projection-facing interaction. (AC: 2)
  - [x] Publish only the narrow commands that are already justified by the current runtime-owned projection model, such as active-view switching and other inspect-first projection actions that remain downstream of canonical authority.
  - [x] Normalize those commands behind Athena-owned request/response contracts instead of exposing generic `AthenaCommandRuntimeService` execution or a raw runtime control tunnel.
  - [x] Do not expose arbitrary hosted plugin command contributions as an unbounded graphical command surface. If any plugin-backed action is surfaced, it must appear through an explicit Athena allowlist with deterministic typed payloads.
- [x] Keep runtime and protocol ownership boundaries explicit. (AC: 1, 2)
  - [x] Treat `:kernel:runtime` as the owner of active projection-session truth and `:ide:lsp` as the typed bridge for IDE consumers only.
  - [x] Keep Theia, desktop viewers, and future graph adapters as downstream consumers of typed projection payloads rather than owners of refresh, invalidation, or semantic mutation logic.
  - [x] Do not move Story `2.x` graph-adapter work, graphical panel lifecycle, or renderer translation into this story.
- [x] Cover the new LSP seam with focused tests and docs. (AC: 1, 2)
  - [x] Add or extend `:ide:lsp` tests for typed projection queries, deterministic payload shape, unavailable-state behavior, and explicit command allowlist behavior.
  - [x] Keep current runtime, repository-session, semantic SCM, and language-feature tests green.
  - [x] Update `ide/lsp/README.md` and `ide/lsp/README.zh-CN.md` to describe the new projection query seam and governed-command boundary.

### Review Findings

- [x] [Review][Patch] Bind the LSP projection transport to the real runtime-owned projection-session lifecycle before considering Story `1.4` complete [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt:116]
- [x] [Review][Patch] Projection failure diagnostics are collapsed into one synthetic `projection.unavailable` payload instead of exposing inspectable underlying failure detail [ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt:207]

## Dev Notes

### Story Intent

- Story `1.4` is the IDE transport story for the M7 projection boundary.
- The success condition is not "the graphical workbench is already live." It is "runtime-owned projection sessions are exposed through typed Athena LSP protocol seams with a narrow governed command allowlist."
- Story `1.4` must stop before Theia graphical panels, graph adapters, renderer mechanics, or broader interaction design.
- Epic `2.x` owns graphical panel delivery and graph-adapter placement.
- Epic `3.x` owns the first renderer proof and technology-path validation.

### Architecture Guardrails

- Align to inherited AD-3 by keeping `ide/lsp` as the only semantic and projection entry point for the IDE path. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to inherited AD-5 by keeping session authority in the LSP-embedded JVM runtime rather than frontend or adapter state. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to inherited AD-17 by preserving one runtime-owned `RepositoryGraphSession` per product window beside the new projection query seam. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to inherited AD-19 by preserving semantic SCM as its own VCS-neutral runtime layer and keeping projection transport additive beside it rather than absorbing it. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]
- Align to AD-27 by consuming the compiler/runtime projection-model path rather than reconstructing graphical state from raw engineering entities or frontend-local shapes. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-27]
- Align to AD-29 by treating layout, geometry, grouping, routing, and viewport state as downstream projection metadata and surfacing only inspectable runtime-owned session output. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-29]
- Align to AD-30 by exposing runtime-owned projection sessions through Athena-owned typed transport rooted at `ide/lsp` and by limiting that transport to typed queries, deterministic projection updates, and an explicit allowlist of governed commands. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-30]
- Align to AD-31 by keeping M7 inspect-first and routing any meaningful persisted change through governed commands rather than private frontend mutation or unreviewable tunnels. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#AD-31]
- Preserve inherited AD-23 by treating Theia-hosted or future graph-hosted surfaces as downstream bridges rather than semantic or projection cores. [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md#Inherited-Invariants]

### Technical Requirements

- Build directly on the current proven seams:
  - `AthenaLanguageServer`
  - `AthenaLspSessionHost`
  - `AthenaRepositoryGraphSessionProtocol`
  - `AthenaSemanticScmProtocol`
  - `AthenaExecutionContext`
  - `AthenaRuntimeProjectionSession`
  - `RepositoryGraphSession`
- Reuse the runtime-owned active session from Story `1.3` rather than re-materializing projection state inside `ide/lsp`.
- Keep the new protocol deterministic and inspectable:
  - the same runtime-owned projection session yields the same payload
  - supported and active view state is explicit
  - unavailable or invalid canonical inputs surface typed status and diagnostics instead of stale ready payloads
- Governed command support in this story must remain narrow:
  - no generic runtime execution endpoint
  - no untyped "send action" tunnel
  - no graph-framework command vocabulary as the public contract
  - no silent widening into full graphical editing
- If projection commands need to mutate canonical or persisted projection metadata, the LSP seam must route them through the existing runtime-owned command path rather than duplicating mutation logic inside `ide/lsp`.
- Add clean KDoc for all new public/core Kotlin types.

### Architecture Compliance

- The story is only successful if projection authority becomes easier to point to:
  - compiler and runtime own projection truth
  - `ide/lsp` exposes that truth through typed Athena protocol seams
  - graphical clients remain downstream consumers
  - allowed actions are explicit and governed
- Prevent these failure modes:
  - `ide/lsp` reconstructs projection state independently of the runtime-owned session
  - Theia, a graph adapter, or a renderer library defines the public projection payload contract
  - a new generic command or runtime tunnel bypasses the existing governed command/runtime path
  - plugin command execution becomes a raw public surface without Athena-owned allowlisting
  - Story `1.4` widens into graphical panel lifecycle, selection synchronization, or renderer implementation work

### Library / Framework Requirements

- Use the repo-approved stack already frozen in local planning artifacts:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Reuse the current `lsp4j`-based request pattern already present in `:ide:lsp`.
- No third-party dependency should be added just to expose projection requests or governed commands.
- Graph frameworks, GLSP protocol payloads, and Theia panel wiring remain later-story concerns.

### File Structure Requirements

- Expected update files:
  - `ide/lsp/README.md`
  - `ide/lsp/README.zh-CN.md`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionProtocol.kt`
  - `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt`
  - one new focused projection protocol file under `ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/`
  - one or more focused request tests under `ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/`
- Possible focused runtime touches if absolutely required:
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
  - `kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- Files whose current behavior and ownership must be preserved:
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt)
    - already exposes additive Athena-owned request surfaces over the runtime-backed active session
  - [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt)
    - already owns repository activation and runtime-backed session hosting for the IDE path
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt)
    - already owns canonical compilation state, active view choice, and runtime service access
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
    - already owns runtime-facing projection-session concepts and should remain the upstream session authority
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt)
    - remains the governed mutation path for canonical runtime state
  - [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt)
    - already exposes hosted command contributions, which must not become an unbounded public graphical command tunnel
- Explicit non-goals:
  - no Theia frontend or backend widget work
  - no `integrations/graph-*` adapter implementation
  - no renderer or symbol-pack logic
  - no selection synchronization across textual and graphical views
  - no broad graphical editing or freeform canvas authoring

### Testing Requirements

- Minimum verification commands for story completion:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test"`
- Recommended focused compatibility regression:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :kernel:compiler:test"`
- Optional wider regression once focused tests are green:
  - `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- Required proof checks:
  - typed projection request returns deterministic runtime-owned session data
  - unavailable projection session state returns typed status and diagnostics rather than stale payloads
  - governed command requests reject unsupported or unallowlisted commands explicitly
  - existing repository-session, semantic inspection, semantic SCM, and semantic history request surfaces still work
  - no second transport path is introduced for graphical state
- Keep all Gradle verification sequential on Windows. Do not overlap build, test, or run commands in parallel shells.

### Current Code State To Preserve

- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt) already owns additive Athena `@JsonRequest` methods for semantic inspection, repository graph session, semantic SCM state, and semantic history state over the runtime-backed active session.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionProtocol.kt) already establishes the repo pattern for typed params/payloads and deterministic conversion helpers.
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt`](../../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt) already establishes the repo pattern for additive Athena-owned protocol DTOs over runtime-owned semantic state.
- [`ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionRequestTest.kt`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionRequestTest.kt) and [`ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt`](../../../ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt) already prove the request-testing pattern that Story `1.4` should extend.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt) currently caches canonical compilation state, stores the active projection view, and exposes runtime projection-session access rooted in the active project.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt) currently contains the runtime-owned session and snapshot concepts that Story `1.4` should publish rather than duplicate.
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`](../../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt) already separates inspectable hosted command contributions from actual execution and should remain governed by explicit Athena-owned command publication.

### Previous Milestone Intelligence

- Story `1.1` established the dedicated `projection-model` kernel boundary for renderer-neutral projection contracts.
- Story `1.2` established deterministic projection-model materialization from engineering, layout, and geometry inputs.
- Story `1.3` established runtime-owned projection sessions with deterministic invalidation and refresh over canonical inputs.
- M6 Story `2.4` already proved the correct additive pattern for exposing runtime-owned semantic state through `ide/lsp` without turning Theia into a second authority.
- M5 and M6 already proved repository-session, semantic inspection, semantic SCM, and package-history seams that this story must preserve while adding projection transport.
- The user has repeatedly enforced workspace rules that still bind here:
  - physical module structure must match intended architecture
  - root package is `com.engineeringood`
  - affected modules keep English and Chinese README coverage
  - Java `25` and sequential Windows Gradle execution are non-negotiable

### Git Intelligence Summary

- Recent milestone baseline:
  - `adb0ae5 Complete M4-M6 IDE, repository, and semantic SCM milestones`
  - `a9b3a12 Complete M3 hosted extensibility proof`
  - `1339722 feat(m3): establish kernel plugin api boundary`
  - `dfc0234 add reference`
  - `87b1342 Complete M2 projection proof and normalize milestone artifacts`
- Practical implication:
  - extend the current LSP bridge instead of creating a second IDE transport path
  - keep runtime projection authority explicit and typed
  - defer graph-framework and panel work to later M7 stories

### Latest Technical Information

- No extra web research is required for this story.
- The versions that matter are already frozen by the local M7 architecture and root build documentation:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Current `:ide:lsp` already depends on the runtime, repository, semantic SCM, compiler, language, and validation modules needed for this story's boundary work.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- `.codegraph/` exists in this repository and should continue to be used first when locating or understanding code areas.
- This story sits at the seam between:
  - `kernel/projection-model`
  - `kernel/runtime`
  - `ide/lsp`
  - existing repository and semantic SCM runtime services
- Naming should stay easy to understand and avoid framework leakage:
  - typed projection query payloads
  - governed projection command allowlist
  - runtime-owned projection sessions
  - Athena-owned protocol seams

### References

- [Source: _bmad-output/planning-artifacts/epics-M7-2026-07-09.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/prd.md]
- [Source: _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-09-m7/addendum.md]
- [Source: _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/ARCHITECTURE-SPINE.md]
- [Source: _bmad-output/implementation-artifacts/m7/sprint-status.yaml]
- [Source: _bmad-output/implementation-artifacts/m7/1-1-publish-canonical-projection-contracts-in-kernel-projection-model.md]
- [Source: _bmad-output/implementation-artifacts/m7/1-2-materialize-deterministic-projection-state-from-engineering-layout-and-geometry-inputs.md]
- [Source: _bmad-output/implementation-artifacts/m7/1-3-host-runtime-owned-projection-sessions-with-deterministic-invalidation-and-refresh.md]
- [Source: _bmad-output/implementation-artifacts/m6/2-4-expose-review-and-commit-semantics-through-runtime-lsp-and-existing-ide-seams.md]
- [Source: ide/lsp/README.md]
- [Source: ide/lsp/build.gradle.kts]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLspSessionHost.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionProtocol.kt]
- [Source: ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmProtocol.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaRepositoryGraphSessionRequestTest.kt]
- [Source: ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaSemanticScmStateRequestTest.kt]
- [Source: kernel/runtime/README.md]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeViewerProjection.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/RepositoryGraphSession.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticScmStateService.kt]
- [Source: kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaSemanticHistoryStateService.kt]

## Story Completion Status

- Status: done
- Completion note: Bound the typed projection LSP seam to a cached runtime-owned projection-session lifecycle with explicit invalidation on canonical state and active-view changes, preserved underlying projection failure diagnostics, and verified the full Java 25 regression path.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- M7 sprint status, epic breakdown, PRD, addendum, and architecture spine review
- prior M7 `1.3` story review for runtime-owned projection-session prerequisites
- M6 `2.4` story review for the additive runtime-to-LSP pattern
- CodeGraph exploration over `AthenaLanguageServer`, `AthenaLspSessionHost`, `AthenaExecutionContext`, and `AthenaRuntimeProjectionSession`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"` (red compile-failure confirmation)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"` (green focused verification)
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test :kernel:runtime:test :kernel:compiler:test"`
- `cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain test"`
- `powershell -ExecutionPolicy Bypass -File .\tools\encoding-audit.ps1`

### Completion Notes List

- Added `AthenaProjectionProtocol.kt` with KDoc-covered projection session, snapshot, diagnostic, and governed-command DTOs rooted in Athena-owned LSP contracts.
- Extended `AthenaLanguageServer` with additive `athena/projectionSession` and `athena/projectionCommand` request surfaces over the existing runtime-backed active session.
- Published an explicit allowlist limited to `switch-active-view`, rejecting unallowlisted command ids instead of exposing a generic runtime or plugin-command tunnel.
- Added `AthenaProjectionRequestTest` covering ready projection state, governed command application, unallowlisted command rejection, and unavailable projection state.
- Bound `AthenaExecutionContext.projectProjectionSession()` to a cached runtime-owned session lifecycle with explicit invalidation after canonical document updates and active-view switches.
- Preserved underlying unavailable projection diagnostics by carrying typed runtime diagnostic detail, including stable codes, severity, and provenance where the upstream failure model provides it.
- Updated `ide/lsp` English and Chinese READMEs to document the new projection boundary and governed command seam.
- Verified the change with focused LSP tests, recommended runtime/compiler regression, full Gradle `test`, and the repository encoding audit.

### File List

- _bmad-output/implementation-artifacts/m7/1-4-expose-typed-projection-queries-and-governed-commands-through-ide-lsp.md
- _bmad-output/implementation-artifacts/m7/sprint-status.yaml
- ide/lsp/README.md
- ide/lsp/README.zh-CN.md
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt
- ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt
- ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt
- kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt
- kernel/runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSessionTest.kt

### Change Log

- 2026-07-10: Added typed projection-session and governed projection-command LSP seams, focused projection request tests, updated module docs, and verified full regression on Java 25.
- 2026-07-10: Closed review patches by binding the LSP seam to the runtime-owned projection-session cache/invalidation lifecycle and by preserving inspectable unavailable diagnostics from runtime through the Athena projection payload.
