---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.2: Introduce The Command Runtime For Semantic Mutations

Status: done

## Story

As an operator or platform builder,
I want semantic changes to be executed through an explicit `Command Runtime`,
so that all project mutations follow one inspectable, runtime-owned path instead of ad hoc caller-side object edits.

## Acceptance Criteria

1. Given an active `Workspace`, `Project`, and `Execution Context` managed by `Athena Runtime`, when a semantic mutation is requested, then the mutation must enter the system as an explicit command handled by the `Command Runtime`, and callers cannot directly mutate canonical project state outside that command path.
2. Given command-backed mutation support is introduced, when a command such as create, rename, connect, or disconnect is executed, then the runtime applies the semantic change through runtime-owned contracts over canonical state, and the command execution result is inspectable by the runtime for later history, diff, and replay behavior.
3. Given multiple surface types exist or will exist in M1, when CLI, GUI, plugin, or optional AI-originated mutation requests are reviewed, then they are all required to route through the same `Command Runtime` boundary, and no surface is allowed to bypass command, validation, or runtime ownership rules.
4. Given the first command runtime slice is in place, when standard Java `25` build and runtime checks run, then the workspace builds successfully and command-backed semantic mutation works over the active project, and the implementation preserves the invariant that commands are the only semantic mutation path in M1.

## Tasks / Subtasks

- [x] Add the first runtime-owned command contracts and inspectable execution result surface. (AC: 1, 2, 3)
  - [x] Add documented command request, command kind, and execution result types under `:runtime`.
  - [x] Keep execution results inspectable enough for later history, diff, undo, and replay work.
  - [x] Keep new command contracts runtime-owned rather than UI-owned, plugin-owned, or compiler-private.
- [x] Route canonical active-project state through the runtime instead of recompiling from source for every projection. (AC: 1, 3)
  - [x] Add a runtime-owned active-project compilation snapshot inside `AthenaExecutionContext`.
  - [x] Ensure graph and viewer projections consume the runtime-owned active state.
  - [x] Preserve the existing DSL compile path as the source bootstrap for the initial runtime state.
- [x] Implement the first semantic mutation through the command runtime. (AC: 1, 2, 4)
  - [x] Add the first concrete semantic command over canonical `Engineering IR` without DSL text editing.
  - [x] Recompute runtime-facing semantic and render state after a successful command.
  - [x] Reject unavailable or invalid mutation requests through explicit command results rather than hidden state changes.
- [x] Document and verify the command boundary. (AC: 1, 2, 3, 4)
  - [x] Add architecture-facing docs describing command runtime ownership and canonical-state mutation rules.
  - [x] Add focused runtime tests for service exposure, command execution, rejection behavior, and runtime-state refresh.
  - [x] Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

## Dev Notes

### Story Intent

- Story `2.2` is the first Epic `2` mutation slice.
- It must establish one real semantic mutation path owned by the runtime before undo, redo, replay, diff, or GUI wiring arrive.
- The proof is not a full editor; it is the minimal runtime mutation boundary that later surfaces must reuse.

### Architecture Guardrails

- `Athena Runtime` owns workspace lifecycle, active project state, execution context, and service orchestration.
- `Engineering IR` remains the only canonical semantic authority.
- Any semantic mutation must enter through an explicit runtime-owned command boundary.
- Graph, viewer, diagnostics, and later history remain derived or operational views over canonical semantics.
- Frontends are adapters only and may not bypass runtime command, validation, or orchestration rules.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin command and runtime classes documented with KDoc.
- Reuse canonical stable identities already defined by the lowering boundary.
- Avoid rename-heavy repository churn; evolve above the proven M0 and Epic `1` seams first.
- Do not introduce undo, redo, replay, serialization, diff, or plugin command hosting yet unless required as passive result metadata for future stories.

### Previous Story Intelligence

- Story `2.1` already established `AthenaExecutionContext` plus a runtime-owned engineering-graph projection and service-registry exposure.
- The graph is a projection only. Do not let command work create a second semantic authority beside canonical `Engineering IR`.
- Runtime graph and viewer consumers already exist, so mutation work must move them onto runtime-owned active state instead of reparsing source for every request.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Minimum verification commands for story completion:
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- On this Windows repo, Gradle verification must remain sequential. Do not run `build` and `test` concurrently.
- Follow red-green-refactor. No production command-runtime code should land before the first failing tests exist.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.2: Introduce The Command Runtime For Semantic Mutations`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-7`, `FR-11`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-2`, `AD-3`, `AD-4`, `AD-6`

## Story Completion Status

- Status: review
- Completion note: Runtime-owned command execution, active-state caching, command-boundary docs, and sequential verification are complete for Story `2.2`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from Epic `2.2` so implementation could proceed against an explicit artifact instead of backlog-only text.
- Added a runtime-owned command surface with explicit command kinds and inspectable execution results instead of caller-side semantic mutation.
- Moved `AthenaExecutionContext` onto a cached active compilation snapshot so command-backed state, graph projection, and viewer projection all read the same canonical runtime-owned state.
- Added `AthenaCompiler.recompute(...)` so runtime commands can revalidate and rerender canonical `Engineering IR` without reparsing authored DSL text.
- Added focused runtime tests for shared command service exposure, successful `CONNECT_PORTS` mutation, unavailable parse-state rejection, invalid-request rejection, and projection refresh.
- Added a CLI-facing `connect` adapter so one real shell surface now routes through the same runtime-owned command boundary for end-to-end verification.
- Verified sequentially with `.\\gradlew.bat --no-daemon --console=plain :runtime:test`, `.\\gradlew.bat --no-daemon --console=plain build`, `.\\gradlew.bat --no-daemon --console=plain test`, and `.\\gradlew.bat --no-daemon --console=plain :cli:test`.
- Verified a live shell E2E proof with `.\\gradlew.bat --no-daemon --console=plain :cli:run --args="connect <temp-project> PLC1.out M1.in"` and observed `connections before: 0`, `connections after: 1`, and `viewer connections: 1`.

### Completion Notes List

- Added `AthenaCommandRuntimeService`, `AthenaCommand`, `AthenaCommandKind`, and inspectable command execution result types under `:runtime`.
- Added runtime-owned active compilation caching in `AthenaExecutionContext` so graph and viewer projections reflect command-backed semantic state rather than reparsing source text each time.
- Added the first concrete semantic mutation command, `AthenaConnectPortsCommand`, which creates one canonical `EngineeringConnection` over existing port identities.
- Added compiler recomputation support for runtime-owned canonical documents and documented the command boundary in the repo docs.
- Added a CLI `connect` command that resolves authored port paths, calls the runtime-owned command service, and reports refreshed canonical and viewer state.
- Verified the full Story `2.2` proof path sequentially on Java `25`, including a live CLI E2E connect flow.

### File List

- `_bmad-output/implementation-artifacts/2-2-introduce-the-command-runtime-for-semantic-mutations.md`
- `_bmad-output/implementation-artifacts/sprint-status.yaml`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ConnectCliTest.kt`
- `docs/compiler/m1-command-runtime-boundary.md`
- `docs/compiler/m1-engineering-graph-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaEngineeringGraphService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`

### Change Log

- Created the Story `2.2` implementation artifact and moved it into active development.
- Added the first runtime-owned command execution path and active-state caching over canonical `Engineering IR`.
- Added command-boundary docs plus runtime tests proving service exposure, mutation success, rejection behavior, and projection refresh.
- Added a CLI `connect` surface and E2E-oriented CLI coverage for the same runtime-owned command path.
