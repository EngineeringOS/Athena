---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.9: Route Optional AI Proposals Through Accepted Commands

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a reviewer or operator,
I want AI-assisted changes to enter Athena only as accepted command-shaped proposals through the existing runtime path,
so that the optional AI proof reuses the same canonical semantic, validation, history, and rendering boundaries as every other frontend.

## Acceptance Criteria

1. Given the M1 runtime, command, validation, and rendering path already exists, when an AI-assisted proposal is introduced, then Athena represents the proposal as a command-shaped candidate rather than as a direct semantic mutation, and the proposal remains outside canonical project state until explicit acceptance.
2. Given an AI-originated proposal is reviewed and accepted, when the operator applies it, then the proposal enters the same `Command Runtime` path used by GUI and other mutation sources, and the resulting semantic change is subject to the same history, diff, validation, and rendering behavior as other commands.
3. Given an AI-originated proposal is rejected or fails validation, when the runtime processes that outcome, then canonical project state remains unchanged unless an accepted command is actually executed, and AI is not allowed to bypass graph consistency, command history, or validation rules.
4. Given the optional AI proof slice is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and accepted AI proposals can flow through the existing command-backed runtime path, and the implementation remains optional for M1 completion and does not expand into a broader autonomous workflow product.

## Tasks / Subtasks

- [x] Add a runtime-owned AI proposal service over the existing command boundary. (AC: 1, 2, 3)
  - [x] Represent AI-assisted work as explicit command-shaped proposal records rather than direct semantic mutations.
  - [x] Keep proposal state outside canonical project state until acceptance.
  - [x] Preserve deterministic runtime-owned proposal inspection for review, acceptance, and rejection.
- [x] Route accepted AI proposals through the existing command/history path. (AC: 2, 3)
  - [x] Reuse `AthenaCommandRuntimeService` rather than creating an AI-specific mutation path.
  - [x] Keep accepted AI-originated changes inspectable through normal history and diff surfaces.
  - [x] Preserve canonical validation, incremental recompute, and viewer refresh behavior after acceptance.
- [x] Prove proposal rejection and unchanged-state behavior. (AC: 1, 3, 4)
  - [x] Add runtime tests for pending, accepted, rejected, and validation-failed proposals.
  - [x] Add one thin CLI proof surface for propose/review/accept/reject without expanding into a full AI product subsystem.
  - [x] Ensure proposal rejection or failed acceptance leaves canonical state unchanged.
- [x] Document and verify the optional M1 AI adapter boundary. (AC: 4)
  - [x] Add or update one compiler/runtime boundary note describing the optional AI proposal adapter and its non-goals.
  - [x] Run sequential Java `25` verification for affected modules and the full regression path.

## Dev Notes

### Story Intent

- Story `2.9` is an optional M1 proof slice, not a broad AI product.
- The implementation must show that AI-originated intent can only enter Athena as a reviewed command-shaped proposal.
- The accepted path must converge on the same runtime, command, validation, history, diff, and rendering flow already proven by Stories `2.2` through `2.8`.

### Architecture Guardrails

- `Athena Runtime` remains the only owner of workspace lifecycle, project activation, execution context, and service orchestration.
- `Engineering IR` remains the only canonical semantic authority.
- All semantic mutation must continue to flow through the `Command Runtime`; AI may not mutate canonical state directly.
- DSL, GUI, plugin, and AI frontends are adapters to one runtime contract, not separate mutation systems.
- The AI proof must remain optional, local, deterministic, and narrow. No model serving, autonomy loop, cloud dependency, or agent-product expansion belongs in this story.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin runtime/proposal classes documented with KDoc.
- Reuse `AthenaCommandRuntimeService`, `AthenaExecutionContext`, `AthenaServiceRegistry`, command history, diff inspection, and existing CLI session persistence patterns.
- Keep the proposal shape command-oriented; do not introduce a second semantic model, AI-private history, or AI-specific mutation backend.
- Avoid new third-party AI SDKs, remote inference dependencies, or asynchronous job infrastructure in this story.

### Files Likely In Scope

- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/AthenaCliSessionStore.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/CommandHistoryCliTest.kt`
- `docs/compiler/`

### Current Code Notes

- `AthenaCommandRuntimeService` already owns the only mutation path, command history, undo/redo/replay, deterministic history serialization, and latest diff inspection.
- `AthenaExecutionContext` already caches canonical compilation state, command-history state, and latest diff inspection; it is the right place for adjacent runtime-owned proposal state if state must survive within an active project session.
- `AthenaCliSessionStore` already persists history/diff state across one-shot CLI invocations by replaying commands; the optional AI proposal proof should extend that pattern instead of inventing a second sidecar mechanism.
- `BootstrapCli` already exposes thin adapters for connect/history/diff/plugin commands and is a suitable narrow proof surface for review and acceptance behavior.

### Previous Story Intelligence

- Stories `2.2` through `2.6` already proved that semantic mutation, history, diff inspection, and incremental recompute are runtime-owned and inspectable.
- Story `2.7` added plugin-hosted command contributions that still route through the command runtime.
- Story `2.8` tightened non-sovereign enforcement and made core-owned invariants explicit; Story `2.9` must preserve those boundaries for AI-originated work.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Follow red-green-refactor. Add failing tests first for proposal creation, acceptance, rejection, and unchanged-state behavior.
- Cover both runtime-owned proposal behavior and at least one CLI-level persisted review/accept/reject flow.
- Verify that accepted AI proposals show up in normal command history/diff surfaces and rejected proposals do not mutate canonical state.
- Minimum verification commands for story completion:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### Project Structure Notes

- Keep the optional AI proof in runtime/CLI seams already present in M1.
- Do not start the broader UX/editor/agent backlog here.
- If a domain-specific connect-proposal proof is needed, keep it obviously demo-scoped and tied to the existing electrical fixture instead of implying generic AI capability breadth.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.9: Route Optional AI Proposals Through Accepted Commands`
  - `Story 2.8: Enforce Non-Sovereign Plugin Boundaries`
  - `Story 2.2: Introduce The Command Runtime For Semantic Mutations`
  - `Story 2.3: Record Command History With Undo, Redo, Replay, And Serialization`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-12: Route AI-Assisted Changes Through The Same Semantic Runtime`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-2`, `AD-3`, `AD-4`, `AD-6`

## Story Completion Status

- Status: review
- Story context created from Epic 2, the latest architecture spine, `FR-12`, and the current command/history/plugin boundary already proven in M1.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-06: created the `2.9` BMAD story artifact from the current Epic 2 backlog entry and aligned it to the existing command-runtime boundary.
- 2026-07-06: implemented the runtime-owned AI proposal queue, CLI/session proof surface, and acceptance path through the existing command runtime.
- 2026-07-06: verified sequential Java `25` runs for `:runtime:test`, `:cli:test`, `:apps:compose-viewer:test`, `test`, and `build`.

### Completion Notes List

- Captured the optional AI slice as a command-shaped review/acceptance adapter rather than a new semantic subsystem.
- Anchored the story to the existing command runtime, history, diff, and CLI session seams to avoid duplication.
- Added `AthenaAiProposalRuntimeService` and explicit `AthenaCommandOrigin` tracking so accepted AI proposals remain visible through normal history and serialization surfaces.
- Extended CLI one-shot persistence to carry pending AI proposals and accepted-command origin metadata without breaking older session files.
- Added `docs/compiler/m1-ai-proposal-boundary.md` to document the optional AI adapter boundary and non-goals.

### File List

- `_bmad-output/implementation-artifacts/m1/2-9-route-optional-ai-proposals-through-accepted-commands.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistory.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaCommandRuntimeService.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaAiProposalRuntimeServiceTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaCommandHistoryTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/AthenaCliSessionStore.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/AthenaAiProposalCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `docs/compiler/m1-ai-proposal-boundary.md`

### Change Log

- Created the BMAD story artifact for `2.9` and moved sprint tracking to `in-progress`.
- Implemented a runtime-owned AI proposal queue that keeps proposals outside canonical state until explicit acceptance.
- Routed accepted AI proposals through the existing command runtime and preserved origin metadata across history serialization and CLI session restore.
- Added thin CLI proof commands for proposal queueing, listing, acceptance, and rejection.
- Documented the optional AI adapter boundary and completed sequential Java `25` verification through full `test` and `build`.
