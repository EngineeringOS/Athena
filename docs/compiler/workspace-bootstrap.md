# Athena M0 Workspace Bootstrap

## Purpose

Story `1.1` establishes the initial Kotlin/JVM workspace for Athena M0. This scaffold is intentionally thin: it proves the build, module seams, and CLI shell without implementing DSL parsing, semantic validation, `Engineering IR`, plugins, or rendering behavior.

## Pinned Build Stack

- Java `25`
- Kotlin `2.4.0`
- Gradle `9.6.1`

The workspace is pinned to Java `25` through the Kotlin/JVM toolchain. Java `25` is a hard requirement for this repo.
Activate a Java `25` shell before running Gradle. On the current workstation that means invoking `java25` first and then using the wrapper from the same shell.

## Naming Seed

- Gradle group: `com.engineeringood.athena`
- Kotlin package root: `com.engineeringood.athena`

Story `1.1` originally used an `org.engineeringos` placeholder. That was corrected so the bootstrap matches the actual package ownership from the start.

## Module Shape

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
.\gradlew.bat :cli:run --args="--help"
```

These commands are the expected bootstrap verification path for Story `1.1`.
