---
baseline_commit: ae76b71c58bb036f1367e96608aaee7eac213dac
---

# Story 1.1: Establish The M0 JVM Compiler Workspace

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a platform builder,
I want Athena to start from a working Kotlin/JVM compiler workspace with baseline modules, build logic, and CLI entrypoint wiring,
so that the M0 proof can be implemented and verified on a deterministic foundation before semantic behavior is added.

## Acceptance Criteria

1. Given the approved M0 stack and module shape, when the initial workspace is created, then the repository contains the baseline modules needed for `cli`, `language`, `semantics-core`, `ir`, `compiler`, `domain-electrical-runtime`, `renderer-svg`, and `examples`, and the workspace builds successfully on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`.
2. Given the baseline workspace, when a developer runs the standard build and test entrypoints, then dependency resolution, compilation, and baseline test execution succeed deterministically, and the workspace exposes a minimal CLI entry path suitable for later compiler wiring.
3. Given the greenfield starting point, when implementation proceeds to semantic stories, then later stories can modify only the modules they need, and no later story needs to invent project structure ad hoc.

## Tasks / Subtasks

- [x] Create the root Gradle bootstrap files: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, and `gradle/wrapper/*`, pinning the workspace to Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`. (AC: 1, 2)
- [x] Register the code modules `cli`, `language`, `semantics-core`, `ir`, `compiler`, `domain-electrical-runtime`, and `renderer-svg` in `settings.gradle.kts`, and create `examples/` as a tracked root directory for later conformance assets. (AC: 1, 3)
- [x] Scaffold each code module with minimal `src/main/kotlin` and `src/test/kotlin` roots plus one placeholder type or marker per module so the workspace proves package, dependency, and compilation flow without prematurely implementing parser, IR, validation, or rendering behavior. (AC: 1, 2, 3)
- [x] Wire `:cli` as the only executable entry path for M0 using a placeholder command or help output that can later host the compiler pipeline without reshaping the workspace. (AC: 2, 3)
- [x] Add deterministic smoke verification for the bootstrap workspace, including root build, root test, and CLI invocation coverage. (AC: 2)
- [x] Document the scaffold decisions where the next stories need them, especially module intent, shared build conventions, and the fact that `manifesto/` remains a Git submodule input to architecture thinking rather than a build participant. (AC: 3)

### Review Findings

- [ ] [Review][Patch] Replace machine-specific Java bootstrap paths with portable Java 25 toolchain configuration [gradle.properties:1]
- [ ] [Review][Patch] Restore the pre-existing ignore rules and add Gradle ignores additively [.gitignore:1]
- [ ] [Review][Patch] Use an official Gradle distribution URL and a sane wrapper timeout [gradle/wrapper/gradle-wrapper.properties:3]
- [ ] [Review][Patch] Add an in-repo automated smoke check that exercises the CLI entrypoint [cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt:7]

## Dev Notes

### Story Intent

- This story is pure substrate work. It establishes the JVM compiler workspace and module boundaries for M0, but it does not implement the DSL, AST, semantic validation, Engineering IR logic, plugin loading, or SVG rendering behavior yet.
- The repo is currently documentation-first and greenfield from a code perspective. There are no existing Gradle files, wrapper files, Kotlin source roots, or compiler modules in the workspace.
- No prior story exists in Epic 1, so there are no implementation learnings to inherit yet.

### Architecture Guardrails

- Keep M0 as one local Kotlin/JVM process with a CLI shell. Do not introduce services, daemons, HTTP APIs, cloud plumbing, Compose UI, WASM, or multiplatform structure in this story.
- Preserve the structural seed from the architecture spine exactly: `cli`, `language`, `semantics-core`, `domain-electrical-runtime`, `ir`, `compiler`, `renderer-svg`, and `examples/`.
- `Engineering IR` remains the only canonical semantic authority later in M0. This bootstrap story must not let AST, renderer, or future layout concerns leak into the initial workspace structure.
- Rendering stays downstream. Do not create a durable `layout-ir` or `geometry-ir` module in 1.1. The separation rule is important, but the durable artifacts are deferred.
- Real plugin behavior is required later in M0, but this story should only preserve module seams for that future work. Do not fake plugin discovery, manifest processing, or compatibility loading here.
- `AutomationML`, `OPC UA`, AI, cloud, enterprise, and product shell concerns are explicitly out of scope for this story.

### Technical Requirements

- Use Gradle Kotlin DSL, not Groovy build scripts.
- Configure Java `25` via the Gradle JVM toolchain so builds do not depend on ad hoc local compiler selection.
- Keep the root build minimal. A shared root configuration plus subproject conventions is preferred over introducing extra build-system complexity such as service infrastructure or premature custom plugin ecosystems in Story 1.1.
- Keep dependencies intentionally small. This is compiler substrate work, so avoid framework drift into Spring, databases, React, Electron, Kubernetes, or similar application-stack concerns.
- Use one consistent test approach across modules. A minimal JVM unit-test setup is enough as long as `build` and `test` run deterministically from the wrapper.
- The `examples/` directory is an architectural proof asset, not a semantic authority and not necessarily a Gradle subproject in this story.

### Architecture Compliance

- Align the workspace to AD-1 by making the CLI module the runtime shell and keeping everything single-process.
- Align to AD-2 by keeping `semantics-core` general and treating `domain-electrical-runtime` as the first domain-specific extension module, even if it only contains placeholders in this story.
- Align to AD-3 by keeping syntax, semantics, and rendering in separate modules from day one.
- Align to AD-4 by reserving downstream renderer boundaries without introducing render-owned semantics.
- Align to AD-5 and AD-6 by preserving clean extension boundaries for future typed plugin contracts without implementing the plugin system early.
- Align to AD-7 by creating `examples/` now so later stories can attach conformance artifacts without revisiting repository shape.

### Library / Framework Requirements

- Pin the wrapper and build scripts to Gradle `9.6.1`.
- Pin Kotlin to `2.4.0`.
- Pin the Java toolchain to `25`.
- Prefer the smallest repository set needed for Kotlin/Gradle resolution.
- Any additional library introduced in this story must directly support bootstrap compilation or testing. If it does not, it is likely story creep.

### File Structure Requirements

- Create these root paths in Story 1.1:
  - `settings.gradle.kts`
  - `build.gradle.kts`
  - `gradle.properties`
  - `gradlew`
  - `gradlew.bat`
  - `gradle/wrapper/gradle-wrapper.properties`
  - `gradle/wrapper/gradle-wrapper.jar`
  - `cli/`
  - `language/`
  - `semantics-core/`
  - `ir/`
  - `compiler/`
  - `domain-electrical-runtime/`
  - `renderer-svg/`
  - `examples/`
- Each code module should have at least:
  - `build.gradle.kts`
  - `src/main/kotlin/`
  - `src/test/kotlin/`
- Use one consistent root package/group choice across all modules and define it centrally. No stable namespace is documented yet, so the dev agent must choose one value once and apply it consistently rather than inventing per-module variants.
- `manifesto/` stays a Git submodule and must not be absorbed into the Gradle module graph.

### Testing Requirements

- Minimum verification commands for story completion:
  - `./gradlew.bat build`
  - `./gradlew.bat test`
  - `./gradlew.bat :cli:run --args="--help"` or an equivalent placeholder invocation
- The CLI path only needs to prove bootstrap wiring. It should not parse engineering DSL or execute compiler passes yet.
- At least one smoke test should prove that the CLI or shared bootstrap code is callable and that the test engine is wired correctly.
- Re-running the same wrapper commands in a clean workspace should produce the same success outcome without hidden manual setup steps.

### Current Repo State And Preservation Rules

- The repository currently contains planning artifacts, manifesto materials, and a `manifesto` Git submodule, but no implementation workspace yet.
- Preserve existing documentation files and repo metadata. Bootstrap work should be additive.
- If build onboarding instructions are added, keep them consistent with the existing `DEV.md` note that `manifesto/` is a submodule.

### Git Intelligence Summary

- Recent commit history is minimal:
  - `ae76b71 Add manifesto submodule`
  - `1e0719f Initial commit`
- There is no prior application or compiler code pattern to follow, so the architecture spine and planning artifacts are the primary authority for structure.

### Latest Technical Information

- Official release material confirms the pinned stack is currently real and available: Java `25` is an LTS JDK line, Kotlin `2.4.0` is the current stable release line, and Gradle `9.6.1` is available as the current wrapper target verified on `2026-07-02`.
- Use the wrapper and toolchain configuration to enforce those versions in-repo rather than relying on a developer's globally installed Gradle.

### Project Structure Notes

- There is no UX document for this phase, and UX work is intentionally deferred. Do not create frontend modules, Studio shells, or UI scaffolding here.
- Keep the bootstrap biased toward compiler evolution:
  - `language` will later host DSL syntax, parser, and AST.
  - `semantics-core` will later host general semantic concepts and validation contracts.
  - `ir` will later host canonical Engineering IR types.
  - `compiler` will later own pass orchestration.
  - `domain-electrical-runtime` will later carry the first domain extension.
  - `renderer-svg` will later host the SVG backend.
  - `examples/` will later host conformance projects and expected outputs.

### References

- `_bmad-output/planning-artifacts/epics.md` - Story 1.1, Epic 1 context, FR coverage map.
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md` - Sections 4.1, 4.2, 6, 8, 9, and 10.
- `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md` - AD-1 through AD-7, Stack, Structural Seed, Deferred.
- `_bmad-output/specs/spec-athena/SPEC.md` and `_bmad-output/specs/spec-athena/glossary.md`.
- `manifesto/docs/architecture/01-compiler.md`.
- `manifesto/docs/architecture/05-plugin.md`.
- `manifesto/docs/architecture/09-layout-and-geometry.md`.
- `manifesto/docs/technologies/01-kotlin.md`.
- `draft/0001.md`.
- `draft/0002.md`.
- `DEV.md` and `.gitmodules`.

## Story Completion Status

- Status: done
- Completion note: Implemented the Java 25 / Kotlin 2.4.0 / Gradle 9.6.1 workspace bootstrap, added placeholder module scaffolds and CLI help wiring, and verified `build`, `test`, and `:cli:run --args="--help"` via the wrapper.

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Baseline commit captured: `ae76b71c58bb036f1367e96608aaee7eac213dac`
- Red phase verified with `./gradlew.bat test` failing on missing module marker types.
- Red phase verified with `./gradlew.bat :cli:test --tests com.engineeringood.athena.cli.BootstrapCliTest` failing on missing `BootstrapCli`.
- Verification evidence:
  - `./gradlew.bat -version`
  - `./gradlew.bat --console=plain build`
  - `./gradlew.bat --console=plain test`
  - `./gradlew.bat -q :cli:run --args="--help"`

### Completion Notes List

- Story created from sprint status auto-discovery for the first backlog item in Epic 1.
- No UX artifact was found under `_bmad-output/planning-artifacts/`.
- No prior story file existed for Epic 1.
- No `project-context.md` file was present in the repository.
- Added the root Gradle Kotlin DSL build, module registration, and Java 25 enforcement through Gradle properties plus the Windows wrapper launcher path.
- Added placeholder marker classes and tests across `cli`, `language`, `semantics-core`, `ir`, `compiler`, `domain-electrical-runtime`, and `renderer-svg`.
- Added the placeholder CLI entrypoint and help output for `:cli:run --args="--help"`.
- Added bootstrap documentation in `docs/compiler/workspace-bootstrap.md` and updated `DEV.md`.
- Added Gradle build output ignores to `.gitignore`.
- Corrected the Gradle group and Kotlin package root from the placeholder `org.engineeringos` value to `com.engineeringood`.

### File List

- `.gitignore`
- `DEV.md`
- `_bmad-output/implementation-artifacts/m0/1-1-establish-the-m0-jvm-compiler-workspace.md`
- `_bmad-output/implementation-artifacts/m0/sprint-status.yaml`
- `build.gradle.kts`
- `cli/build.gradle.kts`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/BootstrapCli.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/CliModuleMarker.kt`
- `cli/src/main/kotlin/com/engineeringood/athena/cli/Main.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/BootstrapCliTest.kt`
- `cli/src/test/kotlin/com/engineeringood/athena/cli/CliModuleMarkerTest.kt`
- `compiler/build.gradle.kts`
- `compiler/src/main/kotlin/com/engineeringood/athena/compiler/CompilerModuleMarker.kt`
- `compiler/src/test/kotlin/com/engineeringood/athena/compiler/CompilerModuleMarkerTest.kt`
- `docs/compiler/workspace-bootstrap.md`
- `domain-electrical-runtime/build.gradle.kts`
- `domain-electrical-runtime/src/main/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainMarker.kt`
- `domain-electrical-runtime/src/test/kotlin/com/engineeringood/athena/domain/electricalruntime/ElectricalRuntimeDomainMarkerTest.kt`
- `examples/README.md`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew`
- `gradlew.bat`
- `ir/build.gradle.kts`
- `ir/src/main/kotlin/com/engineeringood/athena/ir/EngineeringIrModuleMarker.kt`
- `ir/src/test/kotlin/com/engineeringood/athena/ir/EngineeringIrModuleMarkerTest.kt`
- `language/build.gradle.kts`
- `language/src/main/kotlin/com/engineeringood/athena/language/LanguageModuleMarker.kt`
- `language/src/test/kotlin/com/engineeringood/athena/language/LanguageModuleMarkerTest.kt`
- `renderer-svg/build.gradle.kts`
- `renderer-svg/src/main/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarker.kt`
- `renderer-svg/src/test/kotlin/com/engineeringood/athena/renderer/svg/SvgRendererModuleMarkerTest.kt`
- `semantics-core/build.gradle.kts`
- `semantics-core/src/main/kotlin/com/engineeringood/athena/semantics/core/SemanticsCoreModuleMarker.kt`
- `semantics-core/src/test/kotlin/com/engineeringood/athena/semantics/core/SemanticsCoreModuleMarkerTest.kt`
- `settings.gradle.kts`

## Change Log

- 2026-07-02: Bootstrapped the Athena M0 Gradle workspace, added placeholder module scaffolds and tests, wired the CLI help entrypoint, and documented the bootstrap conventions.
- 2026-07-02: Corrected the package and group root from `org.engineeringos` to `com.engineeringood` and rebuilt the workspace on Java 25.
