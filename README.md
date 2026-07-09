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
- hosted plugins enter through governed source plus approval boundaries
- extensions add domain behavior without becoming semantic authorities
- rendering is a downstream backend, not the source of truth

The implemented scope currently includes:

- a minimal Electrical/Runtime DSL
- parsing into a syntax-only AST
- lowering into canonical `Engineering IR`
- core semantic validation plus extension-provided domain semantics
- deterministic SVG rendering
- stable hosted plugin API and hosted plugin approval boundaries
- governed knowledge package loading and resolution
- external boundary descriptor validation
- runtime-hosted graph, command, history, diff, and optional AI proposal flows
- real `domain-electrical` and synthetic `domain-dummy` proof domains
- a desktop Compose viewer proof and published examples under [`examples/`](examples/README.md)
- a desktop-first Athena Theia IDE proof with runtime-backed repository sessions, Athena LSP authoring support, and professional workbench panels
- a governed repository/package graph proof with canonical `athena.yaml`, canonical `athena.lock`, runtime-owned `RepositoryGraphSession`, and package-aware IDE feedback
- a VCS-neutral semantic SCM kernel boundary for baseline, diff, consequence, review, commit-intent, and history contracts above `:kernel:repository-model`
- a deterministic semantic diff layer that compares current and baseline repository/package/engineering meaning through the governed JVM path
- compiler-derived validation and repository-contract consequence publication over the same runtime-owned semantic comparison path
- deterministic semantic review-summary generation that keeps affected packages, authored semantic intent, derived consequences, validation impact, and degraded-input warnings inspectable and traceable
- additive hosted-plugin semantic review enrichment that lets approved plugins append domain labels, hints, and summaries without rewriting core semantic SCM facts
- deterministic semantic commit-intent generation that keeps adapter-ready commit preparation semantic-first, inspectable, and free of Git-specific staging nouns
- package-aware semantic history and release-relevance contracts anchored to stable package identity and version meaning
- runtime/LSP/Theia semantic SCM projection that exposes baseline-driven review, commit-preparation, package evolution, and release-relevance state through the existing Athena workbench without moving semantic authority into the frontend

This is not the final UX phase yet.

## Module Topology

Athena now groups implementation assets by architectural role. The current JVM/Gradle module graph lives under `kernel/`, `extensions/`, `ui/`, and `apps/`, while the primary M4 IDE product path now exists as a runnable Theia workspace under `ide/`.

| Group | Module / Seed | Directory | Purpose |
| --- | --- | --- | --- |
| `ide` | `node: theia-product` | [`ide/theia-product/`](ide/theia-product/README.md) | Product composition, packaging, and curated Athena Theia capability set |
| `ide` | `node: theia-frontend` | [`ide/theia-frontend/`](ide/theia-frontend/README.md) | Theia frontend contributions, workbench composition, panels, and commands |
| `ide` | `node: theia-backend` | [`ide/theia-backend/`](ide/theia-backend/README.md) | Theia backend contributions, startup, path handling, and process orchestration |
| `ide` | `gradle: :ide:lsp` | [`ide/lsp/`](ide/lsp/README.md) | Athena LSP host and JVM semantic-service boundary for the IDE path |
| `integrations` | `:integrations:scm-git` | [`integrations/scm-git/`](integrations/scm-git/README.md) | First vendor substrate adapter for semantic baseline loading |
| `kernel` | `:kernel:language` | [`kernel/language/`](kernel/language/README.md) | Syntax layer and parser for authored DSL text |
| `kernel` | `:kernel:repository-model` | [`kernel/repository-model/`](kernel/repository-model/README.md) | Canonical M5 repository/package contract boundary for manifest, lock, package identity, and package graph reports |
| `kernel` | `:kernel:semantic-scm` | [`kernel/semantic-scm/`](kernel/semantic-scm/README.md) | Canonical M6 semantic SCM contract boundary for baselines, diffs, consequences, typed reviews, commit intent, and package-aware history |
| `kernel` | `:kernel:engineering-model` | [`kernel/engineering-model/`](kernel/engineering-model/README.md) | Canonical engineering model after lowering |
| `kernel` | `:kernel:layout-model` | [`kernel/layout-model/`](kernel/layout-model/README.md) | Explicit layout projection contracts downstream of canonical semantics |
| `kernel` | `:kernel:geometry-model` | [`kernel/geometry-model/`](kernel/geometry-model/README.md) | Explicit geometry projection contracts downstream of layout intent |
| `kernel` | `:kernel:validation` | [`kernel/validation/`](kernel/validation/README.md) | Generic semantic validation over the canonical model |
| `kernel` | `:kernel:plugins:plugin-api` | [`kernel/plugins/plugin-api/`](kernel/plugins/plugin-api/README.md) | Stable hosted plugin SPI, including additive semantic review enrichment contracts |
| `kernel` | `:kernel:plugins:plugin-host` | [`kernel/plugins/plugin-host/`](kernel/plugins/plugin-host/README.md) | Plugin source, approval, inventory, and hosted lifecycle boundary |
| `kernel` | `:kernel:compiler` | [`kernel/compiler/`](kernel/compiler/README.md) | Compiler facade, lowering, pass orchestration, knowledge, boundary loading, and hosted contribution coordination |
| `kernel` | `:kernel:runtime` | [`kernel/runtime/`](kernel/runtime/README.md) | Workspace lifecycle, execution context, graph, command, history, semantic baseline/diff/review/commit, hosted review enrichment, plugin, and AI proposal hosting |
| `kernel` | `:kernel:svg-renderer` | [`kernel/svg-renderer/`](kernel/svg-renderer/README.md) | Deterministic SVG projection from semantic state |
| `extensions` | `:extensions:domain-electrical` | [`extensions/domain-electrical/`](extensions/domain-electrical/README.md) | First real Electrical domain extension |
| `extensions` | `:extensions:domain-dummy` | [`extensions/domain-dummy/`](extensions/domain-dummy/README.md) | Synthetic proof domain for hosted SPI generality |
| `ui` | `:ui:compose-workbench` | [`ui/compose-workbench/`](ui/compose-workbench/README.md) | Shared Compose workbench and viewer interaction infrastructure |
| `apps` | `:apps:cli` | [`apps/cli/`](apps/cli/README.md) | Shell-facing entry point |
| `apps` | `:apps:desktop-viewer` | [`apps/desktop-viewer/`](apps/desktop-viewer/README.md) | Desktop Compose application entry point |

Group overviews:

- [`ide/`](ide/README.md)
- [`integrations/`](integrations/README.md)
- [`kernel/`](kernel/README.md)
- [`extensions/`](extensions/README.md)
- [`ui/`](ui/README.md)
- [`apps/`](apps/README.md)

During M5, `apps/cli`, `apps/desktop-viewer`, and `ui/compose-workbench` remain valid proof and verification surfaces, but they are no longer the intended home of the primary IDE product path.

## Documentation Scope

The current taxonomy is authoritative in:

- the root and module READMEs
- group READMEs
- [`DEV.md`](DEV.md)
- current boundary notes under [`docs/compiler/`](docs/compiler)
- the active milestone roadmap note under [`docs/roadmap/`](docs/roadmap/athena-milestone-roadmap.md)

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
Set-Location ide
yarn build
yarn start:smoke
```

`java25` is the local workstation helper that switches `JAVA_HOME` to Java 25 before running Gradle.

For the M6 Athena IDE shell, the Electron wrapper now resolves Java 25 automatically on Windows. Use `yarn start:smoke` under `ide/` as the deterministic desktop-start proof, then `yarn start` for the live interactive window. The current workbench now includes the additive `Semantic SCM` panel for baseline-driven review, commit preparation, package evolution, and release relevance. For focused milestone usage, read [`docs/usages/m5-proof-usage.md`](docs/usages/m5-proof-usage.md) and [`docs/usages/m6-proof-usage.md`](docs/usages/m6-proof-usage.md).

## What To Read

If you want the implementation view:

1. [`kernel/compiler/README.md`](kernel/compiler/README.md)
2. [`kernel/runtime/README.md`](kernel/runtime/README.md)
3. [`docs/usages/athena-workspace-summary.md`](docs/usages/athena-workspace-summary.md)
4. [`docs/usages/m6-proof-usage.md`](docs/usages/m6-proof-usage.md)
5. [`docs/usages/m5-proof-usage.md`](docs/usages/m5-proof-usage.md)
6. [`docs/usages/m4-proof-usage.md`](docs/usages/m4-proof-usage.md)
7. [`docs/usages/m3-proof-usage.md`](docs/usages/m3-proof-usage.md)
8. [`docs/compiler/m0-pass-pipeline.md`](docs/compiler/m0-pass-pipeline.md)
9. [`docs/compiler/m1-runtime-host-boundary.md`](docs/compiler/m1-runtime-host-boundary.md)
10. [`examples/README.md`](examples/README.md)
11. [`docs/roadmap/athena-milestone-roadmap.md`](docs/roadmap/athena-milestone-roadmap.md)

If you want the platform thesis:

1. [`manifesto/README.md`](manifesto/README.md)
2. the architecture and technology chapters under `manifesto/docs/`

## Architecture Notes

- AST is syntax-owned and remains separate from semantic truth.
- `:kernel:repository-model` is the VCS-neutral repository/package contract boundary for M5 and later SCM work must consume it from above.
- `:kernel:semantic-scm` is the VCS-neutral baseline/diff/review layer above `:kernel:repository-model`, and runtime consumes it through the JVM-owned repository session path.
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
5. [Zread](https://zread.ai/EngineeringOS/Athena)
