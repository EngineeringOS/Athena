# `:apps:cli`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:apps:cli` module is the shell-facing entry point for Athena's JVM proof. It provides a command-line surface over the runtime and compiler without owning language, semantic, or rendering rules itself.

## Responsibilities

- Start the process through `com.engineeringood.athena.cli.MainKt`.
- Expose the current CLI command surface for parse, command, history, plugin, and optional AI proposal flows.
- Call runtime-owned services and compiler-owned capabilities, then format plain-text output for terminal use.
- Prove that runtime, compiler, the Electrical domain extension, and the SVG renderer are wired into one runnable binary.

## Main Types

- `BootstrapCli`: small facade for argument dispatch and output formatting.
- `MainKt`: process entry point.
- `CliModuleMarker`: lightweight module marker used by tests and bootstrap help output.

## Dependencies

- `:kernel:runtime`
- `:kernel:compiler`
- `:kernel:engineering-model`
- `:kernel:language`
- `:extensions:domain-electrical`
- `:kernel:svg-renderer`

## Boundaries

This module does not define the DSL, canonical IR, semantic validation rules, plugin contracts, or SVG generation logic. It invokes those capabilities and renders a stable terminal-facing summary.

## Verification

From the repository root:

```bash
./gradlew :apps:cli:test
```

On Windows PowerShell in this repo, use:

```powershell
java25; .\gradlew.bat :apps:cli:test
```
