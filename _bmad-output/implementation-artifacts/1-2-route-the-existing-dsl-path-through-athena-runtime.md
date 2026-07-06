---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 1.2: Route The Existing DSL Path Through Athena Runtime

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform engineer,
I want the existing DSL compilation path to be invoked through `Athena Runtime` and its shared execution context,
so that the proven M0 compiler behavior becomes a runtime capability instead of remaining the system owner.

## Acceptance Criteria

1. Given an active `Workspace` and `Project` managed by `Athena Runtime`, when a caller requests DSL compilation through the runtime, then the runtime invokes the existing compiler path through runtime-owned contracts, and the caller does not need to bootstrap compiler internals directly.
2. Given the M0 DSL input path and fixtures already exist, when they are executed through `Athena Runtime`, then the compilation results remain behaviorally consistent with the proven M0 path where applicable, and introducing the runtime does not require a rewrite of the existing DSL compiler flow.
3. Given runtime-owned project activation and execution context, when DSL compilation runs, then compiler services resolve the active project and required runtime services from the shared execution context, and the runtime remains the visible orchestration boundary above compilation.
4. Given the runtime-routed DSL path is in place, when standard build and regression checks run, then the Java `25` workspace builds successfully and the DSL path remains deterministic, and the implementation demonstrates `frontend -> runtime -> compiler` instead of `frontend -> compiler -> everything`.

## Tasks / Subtasks

- [x] Add a minimal runtime-owned DSL execution surface above the existing compiler service. (AC: 1, 2, 3)
  - [x] Extend `:runtime` with explicit runtime-owned DSL operations that use the active project and execution context instead of requiring callers to pass compiler internals around.
  - [x] Keep the runtime API explicit and typed; do not introduce a generic command bus, graph layer, or speculative SDK surface in this story.
  - [x] Preserve `AthenaCompiler` as the owner of parse, lower, validate, and downstream derivation orchestration; Story `1.2` wraps that capability through runtime and does not relocate pass ownership.
- [x] Route the current CLI DSL entry path through `Athena Runtime` while preserving the user-facing shell shape. (AC: 1, 2, 4)
  - [x] Update `:cli` bootstrap wiring so `parse <source-file>` activates runtime-owned workspace and project context before invoking the DSL path.
  - [x] Keep the process entrypoint thin and avoid inventing a broad new CLI command surface in this story.
  - [x] Preserve the current CLI parse output format unless a deliberate runtime-owned improvement is required and covered by tests.
- [x] Add deterministic runtime and CLI proof tests for the runtime-routed DSL path. (AC: 1, 2, 3, 4)
  - [x] Add runtime tests proving an active project can invoke the existing compiler path through runtime-owned context.
  - [x] Add or update CLI tests proving `parse <source-file>` now travels through runtime while preserving behavioral output for the M0 example fixtures.
  - [x] Add at least one failure-path test proving runtime-routed DSL execution still surfaces syntax or file-resolution failures deterministically.
- [x] Document the runtime-routed frontend boundary without rewriting M0 history. (AC: 3, 4)
  - [x] Update the M1 runtime boundary docs under `docs/compiler/**` to explain that DSL is now a frontend adapter to runtime-owned orchestration.
  - [x] Keep Story `1.3` ownership intact: no Compose runtime initialization, no version catalog work, and no UI shell implementation in this story.

## Dev Notes

### Story Intent

- Story `1.2` is the first behavior slice above the runtime host established in Story `1.1`.
- The job here is not to redesign compilation. The job is to prove that the existing DSL path can be invoked through runtime-owned lifecycle, execution context, and service lookup.
- Story `1.1` already created the runtime host and service registry.
- Story `1.3` owns Compose module initialization and version-catalog adoption.
- Story `1.2` must therefore stop at runtime-routed DSL orchestration and proof tests.

### Architecture Guardrails

- M1 remains one Java `25` and Kotlin process. No distributed services, cloud runtime, or multiplatform expansion here.
- `Athena Runtime` remains the sole owner of `Workspace`, `Project` activation, `Execution Context`, and `Service Registry`.
- `Engineering IR` remains the only canonical semantic authority.
- Preserve evolutionary extraction: route through the owner added in Story `1.1`; do not rewrite parser, lowerer, semantic validation, SVG rendering, or plugin compatibility surfaces.
- Commands, graph, diff/history, incremental recomputation, and Compose viewer work are not part of Story `1.2`.

### Technical Requirements

- Prefer a minimal explicit runtime API for DSL-oriented work, for example runtime-owned methods on `AthenaRuntime` or `AthenaExecutionContext` that call the existing compiler service using the active project's source path.
- The active project handle from Story `1.1` remains path-backed and local. Do not invent a persistent project descriptor or manifest format.
- `AthenaCompiler` must remain the owner of parse/lower/validate/downstream pass orchestration.
- Runtime-owned DSL invocation must resolve the compiler through `AthenaServiceRegistry` or `AthenaExecutionContext`, not by callers constructing compiler internals directly.
- Keep all new core Kotlin classes and methods documented with KDoc.

### Architecture Compliance

- Align to AD-1 by keeping the runtime-routed DSL path local, deterministic, and JVM-first.
- Align to AD-2 by ensuring runtime remains the visible owner of lifecycle and service orchestration above compilation.
- Align to AD-3 by treating runtime state as operational and the compiler outputs as canonical or derived semantic artifacts.
- Align to AD-6 by proving the DSL is now a frontend adapter to one runtime contract.
- Do not implement AD-4, AD-5, AD-7, AD-8, AD-9, or AD-10 early in this story.

### Library / Framework Requirements

- Use the repo-pinned local stack already approved for Athena:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Do not introduce `gradle/libs.versions.toml` in this story; version-catalog adoption belongs to Story `1.3`.
- Do not add DI frameworks, serialization frameworks, persistence libraries, Compose dependencies, graph modules, or command modules for Story `1.2`.
- Reuse the existing Kotlin/JUnit test setup.

### File Structure Requirements

- Expected update files:
  - `runtime/src/main/kotlin/com/engineeringood/athena/runtime/**`
  - `runtime/src/test/kotlin/com/engineeringood/athena/runtime/**`
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
  - `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
  - documentation under `docs/compiler/**`
- Files whose current behavior must be preserved unless a direct need is proven:
  - `build.gradle.kts`
    - Keep the existing root Java `25` enforcement and shared subproject Kotlin/JUnit conventions intact.
  - `cli/src/main/kotlin/com/engineeringood/athena/cli/Main.kt`
    - Keep the process entrypoint thin.
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
    - It still owns deterministic M0 compiler pass orchestration. Story `1.2` must wrap, not absorb, that behavior.
  - `compiler/src/main/kotlin/com/engineeringood/athena/compiler/plugin/AthenaCoreRuntime.kt`
    - This remains the plugin compatibility version surface and must not become the M1 runtime host.
  - `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
    - It already owns workspace lifecycle and active execution context state from Story `1.1`; extend carefully rather than bypassing it.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\\gradlew.bat --no-daemon --console=plain :runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :cli:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
  - `.\\gradlew.bat --no-daemon --console=plain :cli:run --args="parse examples/m0/demo-cabinet.athena"`
- Required proof tests:
  - runtime can invoke the active project's DSL path through runtime-owned context
  - M0 example fixture output remains behaviorally consistent when invoked through runtime
  - syntax failure or missing-file behavior remains deterministic through the runtime-routed path
- Keep Gradle verification sequential on Windows. Do not run `build` and `test` in parallel in this repo.

### Current Code State To Preserve

- Story `1.1` already added `:runtime` and the core runtime types:
  - `AthenaRuntime`
  - `AthenaWorkspace`
  - `AthenaProjectRef`
  - `AthenaExecutionContext`
  - `AthenaServiceRegistry`
- `AthenaServiceRegistry` currently exposes typed compiler and renderer accessors and a minimal forward-looking plugin runtime accessor.
- `AthenaWorkspace.activateProject(...)` currently creates the shared execution context and updates runtime-owned active state.
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt` currently:
  - reports the M1 runtime host in help text
  - still invokes `compiler.parse(path)` directly for `parse <source-file>`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt` currently asserts:
  - successful parse output for `examples/m0/demo-cabinet.athena`
  - deterministic syntax failure output for malformed source
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt` currently exposes:
  - `parse(path)`
  - `lower(path)`
  - `compile(path)`
- `docs/compiler/m1-runtime-host-boundary.md` currently documents Story `1.1` and should be extended rather than replaced.

### Previous Story Intelligence

- Carry forward these implemented constraints from Story `1.1`:
  - Java `25` is non-negotiable.
  - Package and group root remain `com.engineeringood.athena`.
  - Keep the runtime boundary explicit and typed; avoid speculative generic containers or SDKs.
  - Keep core runtime Kotlin types documented with KDoc.
  - Keep sequential wrapper verification on Windows.
- Story `1.1` found one environment-specific pitfall:
  - early verification showed a shell-versus-daemon JVM mismatch on this workstation; the final repo posture now pins the Gradle daemon to Java `25`, so normal wrapper verification no longer depends on manual `JAVA_HOME` edits.
- Story `1.1` also proved one lifecycle edge case worth preserving:
  - closing a workspace must invalidate stale workspace references rather than allowing reactivation after shutdown.

### Git Intelligence Summary

- Recent commits do not provide a stable M1 runtime-routing implementation pattern:
  - `bdc3227 init in 2026-07-03`
  - `dd9dcbe init in 2026-07-03`
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical guidance should therefore come from the current working tree, Story `1.1` implementation, and the approved M1 planning artifacts.

### Latest Technical Information

- No external version upgrade decision is required for Story `1.2`.
- Treat the repo-approved local stack as authoritative for this implementation story:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Compose Multiplatform and version-catalog adoption are explicitly deferred to Story `1.3`.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- UX now exists for M1, but Story `1.2` remains substrate-first:
  - the shell should later show compile as runtime-owned behavior
  - this story should provide the orchestration boundary the shell will consume later
  - it should not implement UI shells, dock panels, or Compose state yet
- UX `Flow 1 - Workspace Under Control` is the behavioral target:
  - compile should later read as `workspace/project -> runtime -> compiler`
  - the frontend should no longer appear to own compiler orchestration directly

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 1: Activate And Inspect A Runtime-Managed Project`
  - `Story 1.2: Route The Existing DSL Path Through Athena Runtime`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - Sections `1`, `4.1`, `4.4`, `8`, `9`, and `10`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-1`, `AD-2`, `AD-3`, `AD-6`
  - `Consistency Conventions`
  - `Capability -> Architecture Map`
- `_bmad-output/planning-artifacts/ux-designs/ux-Athena-2026-07-04/EXPERIENCE.md`
  - `Flow 1 - Workspace Under Control`
  - `State Patterns`
- `_bmad-output/implementation-artifacts/1-1-establish-the-runtime-host-above-m0.md`
  - completed runtime host constraints and environment notes
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntime.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaServiceRegistry.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/AthenaCompiler.kt`
- `docs/compiler/m1-runtime-host-boundary.md`

## Story Completion Status

- Status: review
- Completion note: Story 1.2 implementation completed with runtime-owned DSL entrypoints, CLI parse delegation through `Athena Runtime`, runtime and CLI proof tests, and Java 25 verification green.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context assembled from Epic 1, the M1 PRD, architecture spine, UX spine, Story `1.1` implementation notes, and current runtime/CLI/compiler code behavior.
- Red-path verification: `java25; .\gradlew.bat --no-daemon --console=plain :runtime:test :cli:test`
- Runtime proof debugging:
  - initial `compileActiveProject()` success assertion was too broad for the default plugin activation state
  - runtime proof was narrowed to successful `parseActiveProject()` plus deterministic `compileActiveProject()` parse-failure coverage
  - runtime test path resolution was updated to locate the repo root explicitly instead of assuming the `:runtime` module working directory
- Green-path verification:
  - `.\gradlew.bat --no-daemon --console=plain :runtime:test :cli:test`
  - `.\gradlew.bat --no-daemon --console=plain build`
  - `.\gradlew.bat --no-daemon --console=plain test`
  - `.\gradlew.bat --no-daemon --console=plain :cli:run --args="parse examples/m0/demo-cabinet.athena"`

### Completion Notes List

- Story `1.2` must prove `frontend -> runtime -> compiler` without starting Compose or command-runtime work early.
- The runtime host from Story `1.1` is now the required owner boundary for DSL path invocation.
- The current CLI `parse` path is the most visible frontend adapter and should likely become the first runtime-routed proof.
- Added `parseActiveProject()`, `lowerActiveProject()`, and `compileActiveProject()` on `AthenaExecutionContext` so the active project can invoke the existing compiler service through runtime-owned context.
- Routed `BootstrapCli.parse` through runtime-owned workspace and project activation while preserving the existing parse output shape.
- Added runtime tests for successful active-project parse, deterministic compile parse-failure, and CLI proof that `parse <source-file>` leaves runtime-owned workspace and execution context active.
- Extended `docs/compiler/m1-runtime-host-boundary.md` to document DSL as a runtime frontend adapter and to update the verification path.

### File List

- `_bmad-output/implementation-artifacts/1-2-route-the-existing-dsl-path-through-athena-runtime.md`
- `runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaExecutionContext.kt`
- `runtime/src/test/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeTest.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/ParseCliTest.kt`
- `docs/compiler/m1-runtime-host-boundary.md`

## Change Log

- 2026-07-04: Implemented Story 1.2 by routing the CLI DSL parse path through runtime-owned workspace and execution context, adding explicit runtime DSL entrypoints, extending proof tests, and updating the M1 runtime boundary documentation.
