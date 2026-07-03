# `:cli`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:cli` module is the shell-facing entry point for Athena M0. It provides a minimal command-line wrapper around the compiler and exposes the current bootstrap command surface without owning language, semantic, or rendering rules itself.

## Responsibilities

- Start the process through `com.engineeringood.athena.cli.MainKt`.
- Expose the current `--help` and `parse <source-file>` commands.
- Call `AthenaCompiler` and format plain-text output for terminal use.
- Prove that the compiler, the sample Electrical/Runtime domain plugin, and the SVG renderer are wired into one runnable binary.

## Main Types

- `BootstrapCli`: small facade for argument dispatch and output formatting.
- `MainKt`: process entry point.
- `CliModuleMarker`: lightweight module marker used by tests and bootstrap help output.

## Dependencies

- `:compiler`
- `:language`
- `:domain-electrical-runtime`
- `:renderer-svg`

## Boundaries

This module does not define the DSL, canonical IR, semantic validation rules, plugin contracts, or SVG generation logic. It only invokes those capabilities and renders a stable terminal-facing summary.

## Verification

From the repository root:

```bash
./gradlew :cli:test
```

On Windows PowerShell in this repo, use:

```powershell
java25; .\gradlew.bat :cli:test
```
