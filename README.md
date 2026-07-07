# Athena

English | [Chinese (Simplified)](README.zh-CN.md)

Athena is the JVM-first implementation workspace for the EngineeringOS semantic compiler thesis.

## Name

`engineeringood` carries two meanings at once:

- `engineering good`: good for engineering
- `OOD`: object-oriented development

In EngineeringOS terms, that is the same semantic target expressed another way: engineering meaning should be explicit, structured, composable, and preserved in durable models instead of being trapped inside drawings, files, or tool-specific formats.

## Current Scope

The current proof is still architecture-first. It demonstrates that:

- the DSL is the authored source of truth
- `Engineering IR` is the canonical semantic model
- the compiler owns pass orchestration
- extensions add domain behavior without becoming semantic authorities
- rendering is a downstream backend, not the source of truth

The implemented scope currently includes:

- a minimal Electrical/Runtime DSL
- parsing into a syntax-only AST
- lowering into canonical `Engineering IR`
- core semantic validation plus extension-provided domain semantics
- deterministic SVG rendering
- JVM plugin discovery and hosting
- governed knowledge package loading and resolution
- external boundary descriptor validation
- runtime-hosted graph, command, history, diff, and optional AI proposal flows
- a desktop Compose viewer proof and published examples under [`examples/`](examples/README.md)

This is not the final UX phase yet.

## Module Topology

Athena now names and stores Gradle modules by architectural role. The physical workspace layout matches the grouped module graph under `kernel/`, `extensions/`, `ui/`, and `apps/`.

| Group | Gradle Module | Directory | Purpose |
| --- | --- | --- | --- |
| `kernel` | `:kernel:language` | [`kernel/language/`](kernel/language/README.md) | Syntax layer and parser for authored DSL text |
| `kernel` | `:kernel:engineering-model` | [`kernel/engineering-model/`](kernel/engineering-model/README.md) | Canonical engineering model after lowering |
| `kernel` | `:kernel:validation` | [`kernel/validation/`](kernel/validation/README.md) | Generic semantic validation over the canonical model |
| `kernel` | `:kernel:compiler` | [`kernel/compiler/`](kernel/compiler/README.md) | Compiler facade, lowering, orchestration, plugin contracts, knowledge, and boundary loading |
| `kernel` | `:kernel:runtime` | [`kernel/runtime/`](kernel/runtime/README.md) | Workspace lifecycle, execution context, graph, command, history, plugin, and AI proposal hosting |
| `kernel` | `:kernel:svg-renderer` | [`kernel/svg-renderer/`](kernel/svg-renderer/README.md) | Deterministic SVG projection from semantic state |
| `extensions` | `:extensions:domain-electrical` | [`extensions/domain-electrical/`](extensions/domain-electrical/README.md) | First real Electrical domain extension |
| `ui` | `:ui:compose-workbench` | [`ui/compose-workbench/`](ui/compose-workbench/README.md) | Shared Compose workbench and viewer interaction infrastructure |
| `apps` | `:apps:cli` | [`apps/cli/`](apps/cli/README.md) | Shell-facing entry point |
| `apps` | `:apps:desktop-viewer` | [`apps/desktop-viewer/`](apps/desktop-viewer/README.md) | Desktop Compose application entry point |

Group overviews:

- [`kernel/`](kernel/README.md)
- [`extensions/`](extensions/README.md)
- [`ui/`](ui/README.md)
- [`apps/`](apps/README.md)

## Documentation Scope

The current taxonomy is authoritative in:

- the root and module READMEs
- group READMEs
- [`DEV.md`](DEV.md)
- current boundary notes under [`docs/compiler/`](docs/compiler)

Some compiler docs are intentionally marked as historical references and may preserve story-era labels for context.
Historical BMAD artifacts under `_bmad-output/` are retained as records and may still use older story labels or earlier module naming.

## Build Requirements

- Java 25 is mandatory
- Gradle wrapper: `9.6.1`
- Kotlin: `2.4.0`

On Windows, run Gradle build, test, and launch tasks sequentially. Do not overlap them in parallel shells.

## Quick Start

Unix-like shells:

```bash
./gradlew test
./gradlew :apps:cli:run --args="parse examples/m0/demo-cabinet.athena"
```

Windows PowerShell in this repo:

```powershell
java25; .\gradlew.bat test
java25; .\gradlew.bat :apps:cli:run --args "parse examples/m0/demo-cabinet.athena"
```

`java25` is the local workstation helper that switches `JAVA_HOME` to Java 25 before running Gradle.

## What To Read

If you want the implementation view:

1. [`kernel/compiler/README.md`](kernel/compiler/README.md)
2. [`kernel/runtime/README.md`](kernel/runtime/README.md)
3. [`docs/compiler/m0-pass-pipeline.md`](docs/compiler/m0-pass-pipeline.md)
4. [`docs/compiler/m1-runtime-host-boundary.md`](docs/compiler/m1-runtime-host-boundary.md)
5. [`examples/README.md`](examples/README.md)

If you want the platform thesis:

1. [`manifesto/README.md`](manifesto/README.md)
2. the architecture and technology chapters under `manifesto/docs/`

## Architecture Notes

- AST is syntax-owned and remains separate from semantic truth.
- `Engineering IR` is the canonical engineering model.
- Generic validation lives in `:kernel:validation`; domain validation lives in extensions.
- Plugins are real and discoverable, but non-sovereign.
- Governed knowledge packages and external boundary descriptors are separate from authored project input.

## License

See [`LICENSE`](LICENSE).


## Reference

1. [Deepwiki](https://deepwiki.com/EngineeringOS/Athena)
2. [GitHub Page](https://engineeringos.github.io/manifesto)
3. [Github](https://github.com/EngineeringOS/manifesto)
4. [Athena POC](https://github.com/EngineeringOS/Athena)
