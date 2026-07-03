# Athena

English | [Chinese (Simplified)](README.zh-CN.md)

Athena is the current JVM-first implementation workspace for the EngineeringOS semantic compiler thesis.

## Name

`engineeringood` means two things at once:

- `engineering good`: good for engineering
- `OOD`: object-oriented development

In EngineeringOS terms, that naming is intentional. It is another way to express the semantic target of the platform: engineering meaning should be explicit, structured, composable, and shaped into durable models rather than trapped inside drawings, files, or tool-specific representations.

At M0, the repository is proving a specific architectural claim:

- the DSL is the authored source of truth
- `Engineering IR` is the canonical semantic model
- the compiler owns pipeline orchestration
- plugins extend domain behavior without owning semantic authority
- rendering is a downstream backend, not the source of truth

## Current Scope

The current M0 proof includes:

- a minimal Electrical/Runtime DSL
- parsing into a syntax-only AST
- lowering into canonical `Engineering IR`
- core semantic validation plus plugin-provided domain semantics
- deterministic SVG rendering
- local plugin discovery on the JVM
- governed knowledge package loading and resolution
- external boundary descriptor validation
- published conformance examples under [`examples/`](examples/README.md)

This is an architecture proof, not a finished product surface or UX phase.

## Repository Map

| Path | Purpose |
| --- | --- |
| [`cli/`](cli/README.md) | Shell-facing entry point for the current compiler commands |
| [`language/`](language/README.md) | Syntax layer and parser for the M0 DSL |
| [`ir/`](ir/README.md) | Canonical `Engineering IR` model |
| [`semantics-core/`](semantics-core/README.md) | Generic semantic validation over canonical IR |
| [`renderer-svg/`](renderer-svg/README.md) | Thin render model and deterministic SVG emission |
| [`compiler/`](compiler/README.md) | Compiler facade, pipeline orchestration, plugins, knowledge, boundary descriptors |
| [`domain-electrical-runtime/`](domain-electrical-runtime/README.md) | First real domain plugin for M0 Electrical/Runtime semantics |
| [`examples/`](examples/README.md) | M0 conformance fixtures and published expectations |
| [`docs/compiler/`](docs/compiler) | Implementation boundary notes for the compiler workspace |
| [`manifesto/`](manifesto/README.md) | Product and platform vision behind the repository |

## Build Requirements

- Java 25 is mandatory
- Gradle wrapper: `9.6.1`
- Kotlin: `2.4.0`

The root build verifies Java 25 explicitly before full verification.

## Quick Start

Unix-like shells:

```bash
./gradlew test
./gradlew :cli:run --args="parse examples/m0/demo-cabinet.athena"
```

⚠️⚠️⚠️ `java25` is a local command,  will switch `JAVA_HOME` to `25`

Windows PowerShell in this repo:

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat :cli:run --args "parse examples/m0/demo-cabinet.athena"
```

## What To Read

If you want the implementation view:

1. [`compiler/README.md`](compiler/README.md)
2. [`docs/compiler/m0-pass-pipeline.md`](docs/compiler/m0-pass-pipeline.md)
3. [`docs/compiler/m0-plugin-contract-boundary.md`](docs/compiler/m0-plugin-contract-boundary.md)
4. [`docs/compiler/m0-domain-plugin-boundary.md`](docs/compiler/m0-domain-plugin-boundary.md)
5. [`examples/README.md`](examples/README.md)

If you want the platform thesis:

1. [`manifesto/README.md`](manifesto/README.md)
2. the architecture and technology chapters under `manifesto/docs/`

## Architecture Notes

- AST is syntax-owned and remains separate from semantic truth.
- `Engineering IR` is the canonical engineering model.
- Generic validation lives in `:semantics-core`; domain validation lives in typed plugins.
- Plugins are real and discoverable, but non-sovereign.
- Governed knowledge packages and external boundary descriptors are separate from authored project input.

## License

See [`LICENSE`](LICENSE).
