---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.4: Deliver One GUI Command-Backed Port Connection Mutation Path

Status: done

## Story

As an operator,
I want one GUI action in the Compose surface to connect two existing compatible ports through the `Command Runtime`,
so that Athena proves an interactive runtime path from UI input to canonical project state without requiring a DSL parse round trip.

## Acceptance Criteria

1. Given an active project is displayed in the Compose viewer, when the operator chooses two existing compatible ports and performs the first GUI connection action, then the UI issues an explicit `connect ports` semantic command through the runtime, and the change does not rely on editing authored DSL text for that interaction path.
2. Given the first GUI mutation path is executed, when the command is processed by `Athena Runtime`, then canonical project state is updated through the `Command Runtime` over `Engineering IR` and related runtime projections to create the new connection between those ports, and the UI does not mutate semantic objects directly through viewer-local state.
3. Given the first GUI port-connection mutation changes project semantics successfully, when the operator inspects the runtime-managed project after execution, then the updated connection state is available to graph, validation, rendering, and history services through the same canonical runtime path, and the interaction proves `GUI -> command -> canonical semantics` rather than `GUI -> local model -> sync later`.
4. Given the first GUI mutation proof is implemented, when standard Java `25` build and app checks run, then the application can execute the GUI `connect ports` mutation successfully over the active project, and the implementation preserves the M1 scope guardrail that this is one explicit mutation proof, not a full editor surface.

## Tasks / Subtasks

- [x] Add a runtime-backed GUI workbench session for the Compose viewer. (AC: 1, 2, 3)
  - [x] Keep the active `Athena Runtime`, `Workspace`, and `Execution Context` in an app-layer session instead of flattening them into a static snapshot.
  - [x] Rebuild shell state from runtime-owned viewer, graph, and history projections after each accepted GUI command.
  - [x] Keep authored DSL text read-only in the shell for this story.
- [x] Introduce one explicit shell command panel for `connect ports`. (AC: 1, 4)
  - [x] Add typed shell intent/state contracts for selecting source and target ports and dispatching the first GUI mutation.
  - [x] Present only runtime-derived compatible port choices for the first proof path rather than implying a full graphical port editor already exists.
  - [x] Keep the Compose runtime surface domain-light and action-oriented instead of moving semantic ownership into UI code.
- [x] Route the GUI action through the existing command runtime and refresh projections. (AC: 1, 2, 3, 4)
  - [x] Dispatch `AthenaConnectPortsCommand` from the app-layer GUI session through `AthenaCommandRuntimeService`.
  - [x] Refresh viewer, graph, inspector, console, and history-facing shell state from canonical runtime state after the command result.
  - [x] Surface command outcomes in the shell without mutating semantic objects directly in viewer-local state.
- [x] Document and verify the first GUI mutation boundary. (AC: 1, 2, 3, 4)
  - [x] Add focused tests for the GUI session and shell intent flow.
  - [x] Add or update desktop Compose viewer runtime tests to prove the post-command state is visible through runtime projections.
  - [x] Add architecture-facing documentation for the GUI mutation boundary and keep verification sequential on Windows with Java `25`.

## Dev Notes

### Story Intent

- Story `2.4` proves one interactive GUI mutation path and nothing more.
- The goal is `GUI -> runtime command -> canonical semantics -> refreshed projections`, not a general-purpose editor shell.
- The existing canvas remains a viewer-first surface; this story may use a shell action panel for port selection instead of inventing a richer graphical port editor prematurely.

### Architecture Guardrails

- `Athena Runtime` remains the owner of workspace lifecycle, active project state, graph, command history, and viewer projections.
- `Engineering IR` remains the only canonical semantic authority.
- The Compose shell may hold disposable selection and action state only; it may not mutate semantic objects directly.
- GUI-originated changes must reuse the existing `AthenaCommandRuntimeService` path and resulting history behavior from Stories `2.2` and `2.3`.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin classes documented with KDoc.
- Prefer a typed app-layer session/controller plus typed Compose shell intents over unstructured UI callbacks.
- Do not edit authored DSL source text as part of the GUI connection flow in this story.

### Previous Story Intelligence

- Story `1.4` introduced the runtime-driven Compose viewer bootstrap, but the app still materializes a static snapshot into the shell.
- Story `1.5` added disposable viewer interaction state for selection, pan, and zoom, but that state is still viewer-local and does not address semantic mutation.
- Story `2.1` exposes runtime-owned graph projection data that can be reused to derive port choices for a GUI command path.
- Story `2.2` already provides `AthenaConnectPortsCommand` and command execution over canonical runtime state.
- Story `2.3` already records command history, so successful GUI mutations must appear in that same runtime-owned history.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Minimum verification commands for story completion:
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
- Follow red-green-refactor. No production GUI mutation code should land before the first failing tests exist.
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.4: Deliver One GUI Command-Backed Port Connection Mutation Path`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-7`, `FR-11`, `FR-13`, `FR-20`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-3`, `AD-4`, `AD-6`

## Story Completion Status

- Status: review
- Completion note: Runtime-backed GUI port selection, typed shell intents, command-panel wiring, canonical command dispatch, and sequential verification are complete for Story `2.4`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story created from Epic `2.4` so the first GUI mutation proof could be implemented against an explicit implementation artifact.
- Added `AthenaComposeViewerWorkbenchSession` to keep one runtime-managed project session active for the desktop workbench and rebuild shell state from runtime-owned projections.
- Added typed Compose shell command-panel state and shell intents for selecting source and target ports and dispatching the first GUI mutation.
- Wired the desktop app entrypoint to the runtime-backed workbench session instead of a one-time static shell snapshot.
- Added focused app-module tests for GUI source selection, compatibility filtering, runtime command dispatch, history visibility, and read-only DSL persistence.
- Documented the GUI command boundary and verified sequentially with `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`, `.\\gradlew.bat --no-daemon --console=plain :runtime:test`, `.\\gradlew.bat --no-daemon --console=plain build`, `.\\gradlew.bat --no-daemon --console=plain test`, and `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`.

### Completion Notes List

- Added a runtime-backed desktop workbench session that keeps GUI action state separate from canonical semantics while reusing one active runtime-owned project session.
- Added a typed `Connect Ports` command panel to the Compose shell and routed its intents back into the app-layer session.
- Rebuilt shell state from runtime-owned viewer, graph, and history projections after GUI command execution so the new connection appears in scene, inspector, and console state without editing DSL text.
- Added focused GUI session tests covering compatibility filtering and runtime-backed connection execution, plus architecture notes for the GUI command boundary.

### File List

- `_bmad-output/implementation-artifacts/m1/2-4-deliver-one-gui-command-backed-port-connection-mutation-path.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellState.kt`
- `docs/compiler/m1-gui-command-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`

### Change Log

- Created the Story `2.4` implementation artifact and moved it into active development.
- Added the first runtime-backed GUI port-connection workbench session, typed shell command-panel intents, and canonical command refresh path for the Compose desktop surface.
- Documented and verified the Story `2.4` GUI command boundary on Java `25` with sequential Windows-safe checks.
