---
baseline_commit: bdc3227afbefd6834923c325aa556eaa8a2f6d77
---

# Story 1.3: Initialize The Compose Runtime Module Split And Version Catalog

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform engineer,
I want Athena to adopt the approved Compose module split and version-catalog approach,
so that Epic 1 has a clean runtime-facing surface foundation before viewer behavior is added.

## Acceptance Criteria

1. Given the approved M1 architecture and local Compose template reference, when the first Compose-based modules are introduced, then Athena creates a platform app entry module and a separate shared Compose runtime module shape aligned with the approved seed structure, and the split keeps platform bootstrapping separate from shared viewing infrastructure.
2. Given the first Compose module initialization work, when build configuration is added or updated, then shared plugin and library versions are managed through `gradle/libs.versions.toml`, and the new Compose-related modules consume catalog aliases instead of hard-coded per-module version strings.
3. Given the Compose runtime boundary in M1, when the first shared viewing infrastructure is defined, then its responsibilities are limited to app-shell bootstrapping plus runtime-facing viewport, selection, input, camera, hit-testing, and related infrastructure contracts, and it does not yet implement semantic viewer rendering or become the owner of domain-specific electrical semantics.
4. Given the first Compose module split has been added, when the standard Java `25` build and app bootstrap checks run, then the workspace builds successfully and the platform entrypoint can launch the initial Compose application shell, and the implementation remains an evolutionary addition above M0 rather than a repository rewrite.

## Tasks / Subtasks

- [x] Adopt Gradle version-catalog management for the approved Compose stack. (AC: 2, 4)
  - [x] Add `gradle/libs.versions.toml` and move shared plugin or dependency versions needed by the current repo and the new Compose modules into catalog aliases.
  - [x] Update root build configuration to consume plugin aliases or otherwise centralize version usage through the catalog without breaking the existing Java `25` and Kotlin `2.4.0` setup.
  - [x] Keep catalog adoption evolutionary; do not rewrite unrelated modules or introduce dependency churn outside what Story `1.3` requires.
- [x] Introduce the first Compose module split using the approved local template as structural reference. (AC: 1, 2, 4)
  - [x] Add a shared `:compose-runtime` module for domain-neutral Compose infrastructure.
  - [x] Add a desktop-primary platform entry module under `:apps:compose-viewer` or equivalent approved nested path that depends on the shared Compose runtime module.
  - [x] Align the split with the approved seed structure from the local template without copying unrelated template modules such as server or package names into Athena.
- [x] Keep the first Compose runtime scope infrastructure-only. (AC: 1, 3)
  - [x] Add a minimal application shell and shared runtime-facing Compose surface that can compile and launch without implementing semantic project rendering yet.
  - [x] Limit the new shared Compose runtime contracts to shell/bootstrap and future-facing viewing infrastructure concerns such as viewport, selection, input, camera, and hit-testing naming only.
  - [x] Do not implement actual semantic viewer rendering, domain-specific electrical behavior, docking choreography, or interaction features that belong to Stories `1.4` and `1.5`.
- [x] Add deterministic proof tests and bootstrap checks for the new Compose module boundary. (AC: 1, 2, 3, 4)
  - [x] Add focused module smoke tests for the new Compose modules and any small shell descriptors or markers introduced for bootstrap proof.
  - [x] Add a non-interactive or controlled bootstrap verification path proving the Compose desktop entrypoint can initialize the first application shell without hanging automated verification on Windows.
  - [x] Keep the standard Java `25` build and test path green after the Compose modules and version catalog are introduced.
- [x] Document the Story `1.3` boundary clearly enough to prevent overlap with Stories `1.4` and `1.5`. (AC: 3, 4)
  - [x] Add or update architecture-facing documentation under `docs/**` explaining that Story `1.3` owns module split and bootstrap only.
  - [x] State explicitly that semantic viewer rendering belongs to Story `1.4` and concrete interaction behavior belongs to Story `1.5`.

## Dev Notes

### Story Intent

- Story `1.3` is the build and module-boundary slice that prepares Compose work for Epic 1.
- It is intentionally not the first real viewer behavior slice.
- Story `1.4` owns displaying the active project in a Compose semantic viewer.
- Story `1.5` owns concrete viewport, selection, pan, and zoom behavior.
- Story `1.3` must therefore stop at version-catalog adoption, module split, and launchable shell/bootstrap proof.

### Architecture Guardrails

- M1 remains one Java `25` and Kotlin process. Compose is still desktop-primary in this story; web or WASM enrichment remains later scope.
- `Athena Runtime` remains the owner of workspace, project activation, execution context, and service orchestration.
- `Engineering IR` remains the only canonical semantic authority.
- The new Compose modules are infrastructure consumers above the runtime boundary, not owners of semantics.
- Preserve evolutionary extraction above M0: add modules and build structure without destabilizing the proven runtime and compiler slices from Stories `1.1` and `1.2`.
- Do not start command runtime, graph, diff/history, incremental recomputation, or semantic viewer rendering work in Story `1.3`.

### Technical Requirements

- Use the approved local KMP template as a structural reference, not as a literal copy:
  - template root: `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop`
  - version catalog reference: `gradle/libs.versions.toml`
  - module-shape reference:
    - `:app:desktopApp` as the platform entry
    - `:app:shared` as the shared Compose module
    - `:core` as the platform-neutral dependency module
- Map that template shape into Athena intentionally:
  - `:apps:compose-viewer` should act as the desktop entry module
  - `:compose-runtime` should act as the shared Compose infrastructure module
  - existing Athena modules such as `:runtime`, `:compiler`, and `:renderer-svg` remain the platform-neutral substrate; do not add a redundant Athena `:core` module unless the implementation proves a real need
- Prefer Kotlin Multiplatform with `jvm()` only for the shared Compose runtime module in this story, matching the template's current JVM-first posture.
- Keep all new core Kotlin classes documented with KDoc.
- Keep package and group root under `com.engineeringood.athena`.

### Architecture Compliance

- Align to AD-1 by keeping the Compose bootstrap JVM-first and local.
- Align to AD-2 by ensuring the new app shell consumes runtime-owned services instead of owning lifecycle itself.
- Align to AD-6 by preparing one future frontend surface that will later consume the runtime contract.
- Align to AD-7 by keeping the shared Compose runtime domain-neutral and infrastructure-only in this story.
- Align to AD-10 by introducing `gradle/libs.versions.toml` for shared plugin and dependency versions.
- Do not implement AD-4, AD-5, AD-8, or AD-9 surfaces in Story `1.3`.

### Library / Framework Requirements

- Use the repo-approved local stack:
  - Java `25`
  - Kotlin `2.4.0`
  - Gradle `9.6.1`
- Use the Compose and catalog versions from the approved local template as the starting reference unless the repo already proves a different pinned decision:
  - Compose Multiplatform `1.11.1`
  - Kotlin Compose plugin `2.4.0`
  - Material 3 `1.11.0-alpha07`
  - AndroidX lifecycle Compose `2.11.0-beta01`
  - Kotlin coroutines Swing `1.11.0`
- Do not add web, Android, server, Ktor, or unrelated template modules in Story `1.3`.
- Do not introduce UI frameworks outside Compose Multiplatform.

### File Structure Requirements

- Expected new files and directories:
  - `gradle/libs.versions.toml`
  - `compose-runtime/build.gradle.kts`
  - `compose-runtime/src/commonMain/**`
  - `compose-runtime/src/commonTest/**`
  - `apps/compose-viewer/build.gradle.kts`
  - `apps/compose-viewer/src/main/**`
  - optional small shell/bootstrap source files needed to prove launchability
- Expected update files:
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `gradle.properties` if small template-aligned Gradle settings are needed and justified
  - documentation under `docs/**`
- Files whose current behavior must be preserved unless a direct need is proven:
  - `runtime/**`
    - Story `1.3` may depend on runtime ownership concepts, but must not weaken or rename the existing runtime boundary.
  - `cli/**`
    - CLI runtime routing from Story `1.2` must stay green; Compose introduction must not regress the current shell path.
  - `compiler/**`
    - Compiler ownership remains unchanged in this story.
- Naming guardrail:
  - `runtime` and `compose-runtime` are distinct modules with distinct responsibilities. Do not overload them or rename one into the other.

### Testing Requirements

- Minimum verification commands for story completion:
  - `java25`
  - `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
  - one controlled app-shell bootstrap proof command that does not block indefinitely on Windows, such as a dedicated smoke task or a one-shot launch mode
- Required proof expectations:
  - version catalog adoption does not break the existing repo build
  - the new Compose modules compile and test cleanly
  - the desktop entrypoint can initialize the first shell boundary deterministically
- Keep Gradle verification sequential on Windows. Do not run `build` and `test` in parallel in this repo.

### Current Code State To Preserve

- `settings.gradle.kts` currently includes only the existing JVM modules:
  - `:cli`
  - `:runtime`
  - `:language`
  - `:semantics-core`
  - `:ir`
  - `:compiler`
  - `:domain-electrical-runtime`
  - `:renderer-svg`
- `build.gradle.kts` currently:
  - uses `plugins { base; kotlin("jvm") version "2.4.0" apply false }`
  - enforces Java `25` at the root through `verifyJava25`
  - applies Kotlin/JVM and JUnit conventions to subprojects
  - does not yet use a version catalog
- `gradle.properties` currently contains only:
  - `org.gradle.warning.mode=all`
  - `kotlin.code.style=official`
- Stories `1.1` and `1.2` already proved:
  - runtime-owned workspace and execution context boundaries
  - runtime-routed CLI parse behavior
- `docs/compiler/m1-runtime-host-boundary.md` currently documents the runtime and DSL frontend boundary but not Compose bootstrap yet.

### Previous Story Intelligence

- Story `1.1` exposed the shell-versus-build JVM mismatch early; the final repo fix now pins the Gradle daemon to Java `25`, so ordinary wrapper verification no longer depends on manual `JAVA_HOME` edits.
- Story `1.2` proved `frontend -> runtime -> compiler` through the CLI and kept the runtime boundary explicit and typed.
- Carry forward these constraints:
  - Java `25` is non-negotiable.
  - package and group root remain `com.engineeringood.athena`
  - sequential wrapper verification on Windows remains the expected proof path
  - keep module additions evolutionary rather than rename-heavy
- The implementation readiness review already identified the main Story `1.3` risk:
  - treat Story `1.3` as module and contract setup only
  - reserve actual interaction behavior for Story `1.5`

### Git Intelligence Summary

- Recent commits do not yet provide a stable Compose or version-catalog implementation pattern in Athena:
  - `bdc3227 init in 2026-07-03`
  - `dd9dcbe init in 2026-07-03`
  - `4f77469 Add Story 2.1 plugin contract design spec`
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- Practical guidance should therefore come from the current working tree, the approved local Compose template, and the M1 planning artifacts.

### Latest Technical Information

- Treat the approved local template as the authoritative version reference for Story `1.3`.
- Compose Multiplatform and related plugin versions should be introduced through `gradle/libs.versions.toml` rather than per-module hard-coded strings.
- Desktop remains the primary launch target in this story; web or WASM remains later enrichment.

### Project Structure Notes

- No `project-context.md` file exists in the repository.
- UX exists for M1, but Story `1.3` is still infrastructure-first:
  - the shell should feel like a professional engineering workbench later
  - this story should only provide a launchable shell boundary and shared Compose infrastructure
  - it should not yet implement semantic rendering, diagnostics panes, workspace tree behavior, or dock choreography
- The UX flow `Workspace Under Control` is relevant as a later behavioral target, but Story `1.3` should only prepare the shell substrate that later stories will populate.

### References

- `_bmad-output/planning-artifacts/epics.md`
  - `Epic 1: Activate And Inspect A Runtime-Managed Project`
  - `Story 1.3: Initialize The Compose Runtime Module Split And Version Catalog`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md`
  - Sections `4.5`, `6.1`, `8`, `9`, and `10`
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md`
  - `AD-1`, `AD-2`, `AD-6`, `AD-7`, `AD-10`
  - `Structural Seed`
  - `Consistency Conventions`
- `_bmad-output/planning-artifacts/ux-designs/ux-Athena-2026-07-04/EXPERIENCE.md`
  - `Foundation`
  - `Information Architecture`
  - `State Patterns`
- `_bmad-output/planning-artifacts/implementation-readiness-report-2026-07-04.md`
  - note on Story `1.3` vs Story `1.5` overlap
- `_bmad-output/implementation-artifacts/1-1-establish-the-runtime-host-above-m0.md`
- `_bmad-output/implementation-artifacts/1-2-route-the-existing-dsl-path-through-athena-runtime.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `docs/compiler/m1-runtime-host-boundary.md`
- local template references:
  - `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/settings.gradle.kts`
  - `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/build.gradle.kts`
  - `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/gradle/libs.versions.toml`
  - `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/app/desktopApp/build.gradle.kts`
  - `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop/app/shared/build.gradle.kts`

## Story Completion Status

- Status: review
- Completion note: Compose bootstrap, version-catalog adoption, and Java `25` verification are complete for Story `1.3`.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Story context assembled from the corrected Epic 1 story list, the M1 PRD, architecture spine, UX spine, implementation-readiness notes, the approved local Compose template, and the completed Story `1.1` and `1.2` implementation records.
- Verified the shared KMP and desktop entry structure against `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop`.
- Restored full root verification by filtering root lifecycle aggregation to leaf subprojects, pinning Windows verification to sequential in-process Kotlin compilation, and restoring `:compiler` test access to `:domain-electrical-runtime`.
- Verified with:
  - `.\\gradlew.bat --no-daemon --console=plain clean`
  - `.\\gradlew.bat --no-daemon --console=plain :compose-runtime:test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:test`
  - `.\\gradlew.bat --no-daemon --console=plain build`
  - `.\\gradlew.bat --no-daemon --console=plain test`
  - `.\\gradlew.bat --no-daemon --console=plain :apps:compose-viewer:bootstrapSmoke`

### Completion Notes List

- Added `gradle/libs.versions.toml` and moved new Compose/Kotlin plugin wiring to catalog aliases.
- Added the shared `:compose-runtime` KMP module and the desktop `:apps:compose-viewer` entry module using `com.engineeringood.athena`.
- Added the minimal shared shell surface, desktop bootstrap entrypoint, focused tests, and deterministic bootstrap smoke mode.
- Documented the Story `1.3` boundary so rendering stays in Story `1.4` and interaction behavior stays in Story `1.5`.
- Kept root verification stable on this Windows Java `25` workstation by enforcing sequential in-process Kotlin compilation in `gradle.properties`.

### File List

- `_bmad-output/implementation-artifacts/1-3-initialize-the-compose-runtime-module-split-and-version-catalog.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`
- `language/build.gradle.kts`
- `semantics-core/build.gradle.kts`
- `ir/build.gradle.kts`
- `compiler/build.gradle.kts`
- `domain-electrical-runtime/build.gradle.kts`
- `renderer-svg/build.gradle.kts`
- `compose-runtime/build.gradle.kts`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/ComposeRuntimeModuleMarker.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShellDescriptor.kt`
- `compose-runtime/src/commonMain/kotlin/com/engineeringood/athena/composeruntime/AthenaComposeShell.kt`
- `compose-runtime/src/commonTest/kotlin/com/engineeringood/athena/composeruntime/ComposeRuntimeModuleMarkerTest.kt`
- `apps/compose-viewer/build.gradle.kts`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/ComposeViewerAppModuleMarker.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/AthenaComposeViewerBootstrap.kt`
- `apps/compose-viewer/src/main/kotlin/com/engineeringood/athena/apps/composeviewer/Main.kt`
- `apps/compose-viewer/src/test/kotlin/com/engineeringood/athena/apps/composeviewer/ComposeViewerAppModuleMarkerTest.kt`
- `docs/compiler/m1-runtime-host-boundary.md`
- `docs/compiler/m1-compose-bootstrap-boundary.md`

### Change Log

- Added version-catalog-backed Compose build wiring and the first nested app module path.
- Added the shared Compose shell bootstrap layer and desktop viewer bootstrap entrypoint.
- Added deterministic bootstrap proof tests and Windows-safe root verification settings.
