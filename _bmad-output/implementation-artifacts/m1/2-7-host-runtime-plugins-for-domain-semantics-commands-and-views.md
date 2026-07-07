---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 2.7: Host Runtime Plugins For Domain Semantics, Commands, And Views

Status: done

## Story

As a platform builder,
I want `Athena Runtime` to host first-class plugins for domain semantics, commands, and views,
so that M1 proves the runtime can grow through typed extensions without rewriting the runtime core.

## Acceptance Criteria

1. Given the M1 runtime host, command path, and viewer path already exist, when runtime plugin hosting is introduced, then Athena can load and activate plugins for at least domain semantics, commands, and views through runtime-owned typed contracts, and those plugin types operate as extensions of the runtime rather than as separate top-level owners.
2. Given a runtime-hosted domain semantics plugin is active, when the runtime processes project semantics, then the plugin can contribute domain-specific behavior through declared extension contracts, and canonical `Engineering IR` remains the runtime-owned semantic authority.
3. Given a runtime-hosted command or view plugin is active, when the plugin contributes runtime behavior, then the plugin attaches through explicit runtime contracts for commands or views, and new capability can be added without rewriting the runtime core or bypassing the command and viewer boundaries already proven in M1.
4. Given the first runtime plugin slice is implemented, when standard Java `25` build and runtime checks run, then the workspace builds successfully and runtime-hosted domain, command, and view plugins function over the active project, and the implementation proves the minimum M1 plugin slice without requiring importers, exporters, AI skills, or broader plugin breadth yet.

## Tasks / Subtasks

- [x] Add a real runtime-owned plugin host behind `AthenaPluginRuntimeServices`. (AC: 1, 2, 3)
  - [x] Replace the placeholder runtime plugin services contract with an inspectable hosted plugin service.
  - [x] Reuse the existing compiler plugin discovery and approval path instead of creating a second discovery system.
  - [x] Expose one runtime-owned approved inventory and activation report grouped by contribution kind.
- [x] Route compiler domain semantics through the runtime-hosted approved inventory. (AC: 1, 2)
  - [x] Update `AthenaCompiler` construction so runtime and compiler share the same approved plugin inventory.
  - [x] Keep `AthenaDomainPlugin` as the existing lowering and validation contract for domain semantics.
  - [x] Preserve canonical `Engineering IR` ownership and compiler pass ordering.
- [x] Introduce runtime plugin contribution contracts for commands and views. (AC: 1, 3)
  - [x] Add optional runtime-facing contributor interfaces without creating a second compiler plugin type.
  - [x] Keep plugin command contributions routed through `AthenaCommandRuntimeService`.
  - [x] Keep plugin view contributions limited to runtime-owned derived shell data.
- [x] Prove the first runtime plugin slice through the electrical plugin, CLI, and Compose workbench. (AC: 2, 3, 4)
  - [x] Extend `ElectricalRuntimeDomainPlugin` with one runtime command contribution and one runtime view contribution.
  - [x] Add CLI inspection and execution coverage for the first hosted runtime plugin slice.
  - [x] Surface runtime plugin view contribution output through existing Compose inspector, diagnostics, or console seams.
- [x] Document and verify the runtime plugin hosting boundary. (AC: 1, 2, 3, 4)
  - [x] Add focused runtime, compiler, CLI, and Compose tests using red-green-refactor.
  - [x] Add one architecture-facing note for runtime plugin hosting.
  - [x] Run Gradle verification strictly sequentially on Java `25`.

### Review Findings

- [x] [Review][Patch] Restore file-scoped parse command semantics [cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt:99]
- [x] [Review][Patch] Add an explicit runtime-owned domain semantics contribution contract [runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt:31]
- [x] [Review][Patch] Stateful follow-up CLI commands cannot work across normal one-shot invocations [cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt:205]
- [x] [Review][Patch] Connect command fabricates semantic ids when port-path resolution fails [cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt:135]

## Dev Notes

### Story Intent

- Story `2.7` proves the minimum runtime plugin slice required by `FR-18`.
- The implementation should extend the existing M0 plugin model instead of creating a parallel plugin architecture.
- The first slice is limited to domain semantics, commands, and views only.

### Architecture Guardrails

- `Athena Runtime` owns plugin hosting, activation reporting, and contribution access.
- `Engineering IR` remains the only canonical semantic authority.
- Plugins remain real, typed, and non-sovereign.
- Runtime command contributions must flow through the existing command runtime.
- Runtime view contributions may enrich existing shell state, but they must not become a second UI-owned semantic source of truth.

### Technical Requirements

- Keep the implementation JVM-first, local, and deterministic on Java `25`.
- Preserve package root `com.engineeringood.athena`.
- Keep new core Kotlin contracts documented with KDoc.
- Reuse the existing `AthenaPluginDiscovery` and `AthenaApprovedPluginInventory` model where possible.
- Do not introduce importer, exporter, AI-skill, or marketplace breadth in this story.

### Previous Story Intelligence

- M0 already proved real typed plugins and approved plugin discovery in the compiler substrate.
- Story `1.1` introduced a runtime-owned `AthenaServiceRegistry` and a forward-looking plugin service seam.
- Stories `2.2` through `2.6` already proved runtime-owned command execution, history, diff inspection, and Compose shell enrichment paths.
- The Compose workbench already has existing seams for inspector groups, diagnostics entries, and console output; story `2.7` should reuse those seams for runtime plugin view contributions.
- Sequential Gradle verification on Windows is mandatory in this repo. Do not run `build` and `test` concurrently.

### Testing Requirements

- Follow red-green-refactor. No production runtime plugin hosting code should land before the first failing tests exist.
- Minimum verification commands for story completion:
  - `java25; .\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :compiler:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `java25; .\\gradlew.bat --no-daemon --console=plain build`
  - `java25; .\\gradlew.bat --no-daemon --console=plain test`
- Keep verification strictly sequential on Windows and preserve the root Java `25` build path.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Story 2.7: Host Runtime Plugins For Domain Semantics, Commands, And Views`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - `FR-18`, `FR-19`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-2`, `AD-3`, `AD-4`, `AD-9`
- `manifesto/docs/architecture/05-plugin.md`
- `manifesto/docs/technologies/09-automationml.md`

## Story Completion Status

- Status: done

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- 2026-07-06: normalized the `2.7` planning trail back to BMAD-only artifacts and removed the parallel `docs/superpowers` spec copy.
- Red tests were added first for runtime-hosted plugin inventory sharing, contributed plugin command execution, shared compiler plugin reports, Compose plugin view surfacing, and CLI plugin inspection/execution.
- Runtime now owns one hosted plugin discovery report and shares that approved inventory with `AthenaCompiler` by default through `AthenaServiceRegistry`.
- `ElectricalRuntimeDomainPlugin` now proves the first hosted runtime slice with one contributed command and one contributed view.
- CLI now exposes `plugins` and `plugin-command <source-file> <contribution-id>` as thin adapters over runtime-hosted plugin services.
- Added the architecture note `docs/compiler/m1-plugin-runtime-hosting-boundary.md` and linked it from the runtime-host boundary overview.
- Sequential Java 25 verification passed for `:runtime:test`, `:compiler:test`, `:cli:test`, `:apps:compose-viewer:test`, full `build`, full `test`, and `:apps:compose-viewer:bootstrapSmoke`.
- Review patch pass restored file-scoped `parse`, added a persisted CLI session sidecar for one-shot history commands, surfaced direct authored-port lookup failures, and made domain semantics explicit in the runtime-hosted contract.

### Completion Notes List

- Implemented a real runtime-owned hosted plugin service instead of the previous placeholder plugin seam.
- Kept runtime and compiler on one approved plugin inventory by allowing `AthenaCompiler` to consume a hosted discovery report.
- Added runtime command and runtime view contributor interfaces without changing the existing compiler typed plugin contract model.
- Added an explicit runtime-owned domain semantics contribution surface and wired the compiler through that hosted contract without introducing a compiler-to-runtime dependency.
- Extended `ElectricalRuntimeDomainPlugin` with one deterministic contributed command and one derived view contribution.
- Surfaced hosted plugin inspection and contributed command execution through CLI, persisted one-shot command history sessions for follow-up commands, and merged contributed view data into the existing Compose inspector and diagnostics surfaces.

### File List

- `_bmad-output/implementation-artifacts/m1/2-7-host-runtime-plugins-for-domain-semantics-commands-and-views.md`
- `_bmad-output/implementation-artifacts/m1/sprint-status.yaml`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSession.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerWorkbenchSessionTest.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/AthenaCliSessionStore.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/CommandHistoryCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ConnectCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/PluginRuntimeCliTest.kt`
- `compiler/build.gradle.kts`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaDomainSemanticsCoordinator.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerTest.kt`
- `docs/compiler/m1-plugin-runtime-hosting-boundary.md`
- `docs/compiler/m1-runtime-host-boundary.md`
- `domain-electrical-runtime/build.gradle.kts`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainPlugin.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServicesTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`

### Change Log

- Created the BMAD story artifact for `2.7` and aligned its tasks with the approved runtime plugin hosting design.
- Added a real hosted runtime plugin service, shared compiler/plugin inventory reuse, electrical runtime command and view contributions, CLI plugin inspection/execution, Compose plugin view surfacing, and the M1 plugin runtime hosting boundary note.
