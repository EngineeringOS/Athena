# Athena M1 Compose Bootstrap Boundary

## Purpose

Story `1.3` introduces the first Compose Multiplatform application shape above the runtime-owned M0 foundation. Its job is deliberately narrow: adopt version-catalog-based Compose build wiring, establish the shared-versus-platform module split, and prove that Athena can launch a deterministic desktop shell on Java `25`.

This story does not start semantic viewer rendering, domain-specific visualization, or concrete interaction behavior.

Story `1.4` builds directly on this bootstrap by rendering the active runtime-managed project inside the shared shell. See `docs/compiler/m1-compose-viewer-boundary.md`.

## Module Split

- `:ui:compose-workbench`
  - Shared Kotlin Multiplatform module with `jvm()` only in Story `1.3`.
  - Owns the minimal Compose shell surface and bootstrap descriptors.
  - Remains domain-neutral and infrastructure-only.
- `:apps:desktop-viewer`
  - Desktop-primary entry module.
  - Owns the JVM `main` entrypoint and controlled smoke-launch path.
  - Depends on `:ui:compose-workbench` instead of duplicating shared shell code.

This split follows the approved local KMP template structurally while preserving Athena naming, package ownership, and repository boundaries.

## Build Boundary

- Shared plugin and library versions are centralized in `gradle/libs.versions.toml`.
- Root and module builds consume catalog aliases instead of embedding new Compose version strings per module.
- Java `25` remains mandatory for both Gradle runtime verification and Kotlin toolchains.
- The verified launcher details are recorded in `docs/compiler/java-25-build-and-launch-notes.md`.

## Bootstrap Surface

Story `1.3` adds only the minimum launchable shell proof:

- `AthenaComposeShellDescriptor`
  - Shared shell metadata for the first desktop window.
- `AthenaComposeShell`
  - Minimal shared Compose shell UI.
- `AthenaComposeViewerBootstrap`
  - Desktop bootstrap metadata and deterministic smoke message.
- `MainKt`
  - JVM entrypoint for either interactive desktop launch or non-interactive smoke execution.

The shared Compose runtime may name future infrastructure concerns such as viewport, selection, input, camera, and hit-testing, but Story `1.3` does not implement those behaviors yet.

## Explicit Non-Goals

- No semantic project rendering. That belongs to Story `1.4`.
- No pan, zoom, selection mechanics, hit-testing behavior, or other concrete interaction logic. That belongs to Story `1.5`.
- No docking system, diagnostics layout, workspace tree behavior, welcome workflow, or full professional workbench UX yet.
- No ownership change to `AthenaRuntime`, compiler passes, or `Engineering IR`.

## Verification Path

From the repo root:

```powershell
.\gradlew.bat --no-daemon --console=plain :ui:compose-workbench:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
.\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
.\gradlew.bat --no-daemon --console=plain build
.\gradlew.bat --no-daemon --console=plain test
```

These checks prove the Compose bootstrap layer is added evolutionarily above M0 and remains bounded to module split plus shell initialization.
For the root Java `25` enforcement and Compose Desktop launcher details, see `docs/compiler/java-25-build-and-launch-notes.md`.
