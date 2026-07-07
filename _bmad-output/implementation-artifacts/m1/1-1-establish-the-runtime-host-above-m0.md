---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 1.1: Establish The Runtime Host Above M0

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform engineer,
I want `Athena Runtime` to own workspace lifecycle, project activation, execution context, and service registry setup,
so that the platform can run above the proven M0 compiler path instead of letting the compiler remain the top-level owner.

## Acceptance Criteria

1. Given the existing M0 compiler modules and the approved M1 architecture, when the first runtime host is introduced, then Athena provides a runtime entry layer that owns `Workspace` open and close behavior independently from compiler pass execution, and opening a workspace does not require compilation to start immediately.
2. Given an opened `Workspace` with at least one available `Project`, when a `Project` is activated through `Athena Runtime`, then the runtime creates and exposes a shared `Execution Context` for the active project, and compiler and renderer capabilities can be resolved from that context without callers constructing compiler internals directly.
3. Given the runtime host is active, when platform capabilities are registered, then Athena exposes a runtime-owned `Service Registry` for compiler, renderer, plugin, and related services, and those services are discoverable through stable runtime contracts rather than compiler-owned bootstrap logic.
4. Given the first runtime host has been added above M0, when the standard build and runtime checks are executed, then the Java `25` workspace builds successfully with the runtime layer in place, and the implementation preserves evolutionary extraction by adding the runtime above M0 rather than rewriting proven compiler modules.

## Tasks / Subtasks

- [x] Add the first `:runtime` module and register it in the Gradle workspace. (AC: 1, 4)
  - [x] Add `:runtime` to `settings.gradle.kts` without disturbing the existing M0 module graph.
  - [x] Create `runtime/build.gradle.kts`, `runtime/src/main/kotlin`, and `runtime/src/test/kotlin` using the same lightweight marker-and-test convention already used by the existing modules.
  - [x] Add a `RuntimeModuleMarker` smoke test proving the module is wired into the Java 25 workspace.
- [x] Introduce a minimal in-memory runtime-owned lifecycle surface. (AC: 1, 2, 3)
  - [x] Add runtime-owned types for `Workspace`, `Project` handle, `ExecutionContext`, and `ServiceRegistry` under `com.engineeringood.athena.runtime`.
  - [x] Keep the first implementation path-backed and in-memory only; do not invent a persistence layer, manifest format, graph storage layer, or transaction system in this story.
  - [x] Ensure `openWorkspace` and `closeWorkspace` are independent from compiler pass execution.
  - [x] Ensure `activateProject` creates a shared execution context that can resolve compiler and renderer capability surfaces.
- [x] Add a stable runtime-owned service registry without over-designing it into a generic container. (AC: 2, 3)
  - [x] Expose compiler and renderer capabilities through explicit runtime-owned contracts or typed accessors.
  - [x] Keep plugin-related service registration minimal and forward-looking; this story should not implement Plugin Runtime v2.
  - [x] Do not reuse `compiler.plugin.AthenaCoreRuntime` as the runtime host; that type is the plugin compatibility version surface and must remain semantically distinct.
- [x] Adapt the current CLI bootstrap surface to acknowledge the new runtime boundary while preserving M0 behavior that belongs to later stories. (AC: 2, 3, 4)
  - [x] Update `:cli` dependencies and bootstrap wiring so the CLI can construct or report the runtime host above the compiler.
  - [x] Preserve the existing direct parse behavior as-is for now, or keep any new CLI runtime usage strictly to bootstrap and inspection; Story `1.2` owns routing the DSL path through runtime.
  - [x] If help output or marker reporting changes, update `BootstrapCliTest` accordingly without inventing a broader command surface.
- [x] Add deterministic proof tests and documentation for the M1 runtime boundary. (AC: 1, 2, 3, 4)
  - [x] Add runtime tests proving workspace open does not force compilation, project activation yields an execution context, and registered services are discoverable from runtime-owned context.
  - [x] Keep root build and CLI smoke checks green on Java 25.
  - [x] Add or update compiler-facing documentation for the M1 runtime host boundary and its non-goals in this story.

## Dev Notes

### Story Intent

- Story `1.1` is the first M1 substrate slice. Its job is to move lifecycle and service ownership above the proven M0 compiler without prematurely rerouting the DSL path, adding Compose, or implementing the command runtime.
- The success condition is architectural, not feature-rich: Athena must have a real `Athena Runtime` owner for `Workspace`, `Project` activation, `Execution Context`, and `Service Registry`.
- Story `1.2` owns rerouting the existing DSL path through runtime.
- Story `1.3` owns the Compose runtime and viewer module split plus version-catalog adoption.
- This story must therefore stop at runtime ownership and bootstrap proof. Do not collapse `1.1`, `1.2`, and `1.3` into one broad refactor.

### Architecture Guardrails

- M1 remains one Java 25 and Kotlin process. No distributed services, no cloud runtime, no multiplatform expansion, and no Compose module work in this story.
- `Athena Runtime` becomes the sole owner of `Workspace`, `Project` activation, `Execution Context`, and `Service Registry`.
- `Engineering IR` remains the only canonical semantic authority. This story must not invent project state models that compete with canonical semantics.
- The runtime layer sits above the proven M0 modules. Preserve evolutionary extraction: add a new owner above `:compiler` rather than rewriting parser, lowerer, validation, or SVG behavior.
- Commands, graph, diff/history, and incremental recomputation are not part of Story `1.1`.
- Compose UI, welcome screens, docking behavior, and the viewer shell are not part of Story `1.1`, but the runtime state names and boundaries must support the later UX shell states cleanly.

### Technical Requirements

- Prefer a minimal, explicit runtime design:
  - `AthenaRuntime`
  - `AthenaWorkspace`
  - `AthenaProjectRef` or equivalent path-backed project handle
  - `AthenaExecutionContext`
  - `AthenaServiceRegistry`
- Keep the first `Workspace` and `Project` implementation path-backed and local. A minimal proof is enough as long as:
  - a workspace can open without compiling
  - a project can become active
  - an execution context exists
  - compiler and renderer capabilities are resolved through runtime-owned context
- Do not invent a new persistent project descriptor format in this story. Use the thinnest path-backed handle that supports future activation work.
- The service registry should be typed and explicit, not a speculative DI container. Over-general registry infrastructure is story creep here.
- The runtime host may compose existing compiler and renderer services, but it must not steal compile-pass ownership out of `AthenaCompiler` yet.
- Keep all new core Kotlin classes documented with KDoc.

### Architecture Compliance

- Align to AD-1 by keeping the runtime host local, deterministic, and JVM-first.
- Align to AD-2 by moving lifecycle and service orchestration ownership into the new `:runtime` module.
- Align to AD-3 by keeping runtime state operational and derived rather than semantically sovereign.
- Align to AD-6 by preparing one runtime-owned contract that later DSL, GUI, and AI surfaces can target, even though only bootstrap proof is required in this story.
- Align to the architecture consistency conventions:
  - runtime-owned entrypoints should use role nouns such as `Runtime`, `Workspace`, `Project`, `Registry`, and `ExecutionContext`
  - active state belongs to runtime
  - viewer state and future command state remain outside this story
- Do not implement AD-4, AD-5, AD-7, or AD-8 surfaces early here. Those belong to later stories.

### Library / Framework Requirements

- Use the repo-pinned local stack already approved for Athena:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not introduce `gradle/libs.versions.toml` in this story; version-catalog adoption belongs to Story `1.3`.
- Do not add DI frameworks, serialization frameworks, persistence libraries, Compose dependencies, or networking libraries for Story `1.1`.
- Reuse the existing Kotlin/JUnit test setup and the marker-test pattern used by the current modules.

### File Structure Requirements

- Expected new files and directories:
  - `runtime/build.gradle.kts`
  - `runtime/src/main/kotlin/com/engineeringood/athena/runtime/**`
  - `runtime/src/test/kotlin/com/engineeringood/athena/runtime/**`
- Expected update files:
  - `settings.gradle.kts`
  - `cli/build.gradle.kts`
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
  - `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
  - documentation under `docs/compiler/**`
- Files whose current behavior must be preserved unless a direct need is proven:
  - `build.gradle.kts`
    - Keep the existing root Java 25 enforcement and shared subproject Kotlin/JUnit conventions intact.
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/Main.kt`
    - Keep the process entrypoint thin.
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
    - It currently owns parse/lower/validate/downstream orchestration. Story `1.1` must not relocate compile-pass logic out of this class yet.
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaCoreRuntime.kt`
    - This is the compiler plugin compatibility version surface. Do not rename, repurpose, or overload it as the new runtime host.
  - `docs/compiler/workspace-bootstrap.md`
    - This is still the M0 bootstrap reference. Either update it carefully with an M1 bridge note or add a parallel M1 runtime-boundary doc instead of rewriting history confusingly.
- Module dependency expectations for this story:
  - `:runtime` should depend only on the minimal existing modules needed to expose runtime-owned compiler and renderer capability resolution.
  - `:cli` may depend on `:runtime` after this story.
  - Do not add Compose app modules, graph modules, or command modules here.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
  - `.\\gradlew.bat -q :cli:run --args="--help"`
- Required runtime proof tests:
  - opening a workspace does not trigger compilation implicitly
  - activating a project creates an execution context
  - compiler and renderer capabilities are reachable through runtime-owned context
  - service registry lookup remains deterministic and typed
  - CLI bootstrap help or runtime smoke reporting remains green after the runtime layer is added
- Keep Gradle verification sequential on Windows. Do not run `build` and `test` in parallel in this repo.

### Current Code State To Preserve

- `settings.gradle.kts` currently includes only the M0 modules: `:cli`, `:language`, `:semantics-core`, `:ir`, `:compiler`, `:domain-electrical-runtime`, and `:renderer-svg`.
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt` currently:
  - constructs `AthenaCompiler` directly
  - reports module markers in help output
  - provides `parse <source-file>` directly against the compiler
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt` currently asserts M0 help wording and module list. Update deliberately if runtime becomes visible there.
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` already owns deterministic M0 compile orchestration, plugin discovery report construction, and current boundary or knowledge resolution hookups. Story `1.1` must wrap this capability, not absorb or replace it.
- The repository does not yet contain:
  - a `:runtime` module
  - a version catalog
  - Compose viewer modules
  - graph or command modules

### Previous Story Intelligence

- The original M0 bootstrap story remains relevant as prior-work intelligence even though M1 restarts epic numbering.
- Carry forward these established repo rules from `_bmad-output/implementation-artifacts/m0/1-1-establish-the-m0-jvm-compiler-workspace.md`:
  - Java `25` is non-negotiable and should be activated through `java25` on this workstation before Gradle verification.
  - The package and group root must remain `com.engineeringood.athena`.
  - Module marker classes plus small marker tests are the established smoke-test pattern for new modules.
  - `manifesto/` remains a Git submodule and reference input only, not a Gradle module.
  - Sequential wrapper verification on Windows is the expected proof path.
- Preserve the existing repo style of thin module markers, focused unit tests, and architecture notes under `docs/compiler/**`.

### Git Intelligence Summary

- Recent commits do not yet provide a stable runtime implementation pattern:
  - `bdc3227 init in 2026-07-03`
  - `dd9dcbe init in 2026-07-03`
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical guidance should therefore come from the current working tree and the approved M1 planning artifacts, not from commit-history conventions.

### Latest Technical Information

- No external version upgrade decision is required for Story `1.1`.
- Treat the repo-approved local stack as authoritative for this implementation story:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Compose Multiplatform and version-catalog adoption are explicitly deferred to Story `1.3`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- UX now exists for M1, but Story `1.1` is still substrate-first:
  - The runtime should support later shell states such as `Welcome / Start`, `Workspace Shell`, and `Workspace active, no project selected`.
  - It should not implement those UI surfaces yet.
- The UX flow `Workspace Under Control` is relevant as a behavioral target:
  - opening Athena should later separate workspace opening from compile execution
  - compile orchestration should later remain visibly runtime-owned
- This story should create the runtime ownership boundary that later UX and Compose work can consume.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 1: Activate And Inspect A Runtime-Managed Project`
  - `Story 1.1: Establish The Runtime Host Above M0`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - Sections `1`, `1.1`, `2.1`, `4.1`, and `10`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-1`, `AD-2`, `AD-3`, `AD-6`, `AD-10`
  - `Consistency Conventions`
  - `Structural Seed`
  - `Capability -> Architecture Map`
- `_bmad-output/planning-artifacts/ux-designs/ux-Athena-2026-07-04/EXPERIENCE.md`
  - `Information Architecture`
  - `Component Patterns`
  - `State Patterns`
  - `Flow 1 - Workspace Under Control`
- `_bmad-output/implementation-artifacts/m0/1-1-establish-the-m0-jvm-compiler-workspace.md`
  - prior bootstrap rules and Java 25 / package-root constraints
- `settings.gradle.kts`
- `build.gradle.kts`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaCoreRuntime.kt`
- `docs/compiler/workspace-bootstrap.md`

## Story Completion Status

- Status: review
- Completion note: Story 1.1 implementation completed with a new `:runtime` module, runtime-owned workspace and execution-context lifecycle, CLI runtime-host bootstrap reporting, and Java 25 verification green.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Create-story context assembled from current M1 PRD, architecture spine, UX spine, sprint status, current repo bootstrap files, and M0 bootstrap precedent.
- Red-path verification: `java25; .\gradlew.bat --no-daemon --console=plain :runtime:test :cli:test`
- Early Java verification exposed that the shell launcher JVM and the actual Gradle build JVM were not the same thing; the later repo fix pinned the Gradle daemon to Java `25`, replacing the temporary manual `JAVA_HOME` workaround used during the first investigation.
- Regression proof: added a failing test showing a closed `AthenaWorkspace` could still activate a project, then fixed runtime lifecycle invalidation and reran verification green.
- Green-path verification:
  - `.\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\gradlew.bat --no-daemon --console=plain :cli:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `.\gradlew.bat --no-daemon --console=plain :cli:run --args="--help"`

### Completion Notes List

- Epic 1 is the active M1 entry path.
- Story `1.1` is intentionally narrower than Stories `1.2` and `1.3`; do not absorb DSL rerouting or Compose initialization into this implementation.
- No existing M1 story file precedes this one in Epic 1.
- Added `:runtime` above M0 without changing compiler pass ownership in `AthenaCompiler`.
- Introduced `AthenaRuntime`, `AthenaWorkspace`, `AthenaProjectRef`, `AthenaExecutionContext`, and `AthenaServiceRegistry` as a path-backed in-memory runtime boundary.
- `closeWorkspace()` now invalidates the previously opened workspace so closed runtime state cannot be reactivated through stale references.
- Kept plugin service support minimal through the forward-looking `AthenaPluginRuntimeServices` contract instead of reusing `compiler.plugin.AthenaCoreRuntime`.
- Updated CLI bootstrap help to report the M1 runtime host while preserving direct `parse <source-file>` compiler routing for Story `1.2`.
- Added `docs/compiler/m1-runtime-host-boundary.md` and preserved `docs/compiler/workspace-bootstrap.md` as the historical M0 reference with a bridge note.

### File List

- `_bmad-output/implementation-artifacts/m1/1-1-establish-the-runtime-host-above-m0.md`
- `settings.gradle.kts`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `runtime/build.gradle.kts`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaPluginRuntimeServices.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaProjectRef.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaWorkspace.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/RuntimeModuleMarker.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/RuntimeModuleMarkerTest.kt`
- `docs/compiler/m1-runtime-host-boundary.md`
- `docs/compiler/workspace-bootstrap.md`
