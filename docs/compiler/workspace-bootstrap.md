# Athena M0 Workspace Bootstrap

> This document remains the historical M0 bootstrap reference.
> Story `1.1` in M1 adds a runtime host above this workspace; see `docs/compiler/m1-runtime-host-boundary.md`.
> The module names in the bootstrap section below are the original M0 names, not the current grouped module topology.
> For the current physical and Gradle layout, use the root README and the group READMEs under `kernel/`, `extensions/`, `ui/`, and `apps/`.

## Purpose

Story `1.1` establishes the initial Kotlin/JVM workspace for Athena M0. This scaffold is intentionally thin: it proves the build, module seams, and CLI shell without implementing DSL parsing, semantic validation, `Engineering IR`, plugins, or rendering behavior.

## Pinned Build Stack

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`

Java `25` is a hard requirement for this repo.
Compilation uses the Kotlin/JVM toolchain, and root verification is pinned through the Gradle daemon JVM criteria.
See `docs/compiler/java-25-build-and-launch-notes.md` for the verified launcher-versus-daemon-versus-app runtime behavior.
On the current Windows workstation, still invoke `java25` before running the wrapper so build, test, and launch commands all follow the same Java `25` path.

## Naming Seed

- Gradle group: `com.engineeringood.athena`
- Kotlin package root: `com.engineeringood.athena`

Story `1.1` originally used an `org.engineeringos` placeholder. That was corrected so the bootstrap matches the actual package ownership from the start.

## Original M0 Module Shape

- `cli`
  - Command-line shell and future compiler entrypoint.
- `language`
  - Reserved for DSL syntax, parser, and AST work.
- `semantics-core`
  - Reserved for general semantic concepts and validation contracts.
- `ir`
  - Reserved for canonical `Engineering IR` types.
- `compiler`
  - Reserved for ordered compiler passes and orchestration.
- `domain-electrical-runtime`
  - Reserved for the first domain extension, kept outside the semantic core.
- `renderer-svg`
  - Reserved for the downstream SVG backend.
- `examples/`
  - Reserved for conformance artifacts and expected outputs.

## Current Bootstrap Rules

- The workspace is single-process and CLI-first.
- The semantic core stays general; no Electrical/Runtime semantics are hard-coded into the root build.
- Rendering remains downstream; there is no durable layout or geometry module in M0 bootstrap.
- The plugin system is not implemented in Story `1.1`, but the module boundaries preserve space for later typed plugin contracts.
- `manifesto/` remains a Git submodule and reference input only. It is not part of the Gradle project graph.
- Story `1.2` uses a standalone text DSL plus a hand-written Kotlin parser. See `docs/compiler/parser-front-end-decision.md`.

## Standard Commands

From the repo root:

```powershell
java25
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat :apps:cli:run --args="--help"
```

These commands are the expected bootstrap verification path for Story `1.1`.
