# Athena M5 Proof Usage

## Purpose

This guide shows how to exercise the finished M5 proof surfaces:

- the governed Athena repository contract
- deterministic package-graph and canonical lock behavior
- the runtime-owned `RepositoryGraphSession`
- the package-aware Athena IDE path

It assumes the workspace is already checked out locally and that the current workstation has Java 25 available through `java25`.

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
Set-Location ide
yarn <task>
```

Do not overlap Gradle and Yarn desktop runs in parallel shells.

## What M5 Proves

M5 proves that Athena no longer treats repository meaning as a light IDE convenience.

The central M5 claim is:

- Athena repositories have a canonical authored manifest in `athena.yaml`
- reproducible package resolution is recorded in canonical derived `athena.lock`
- package identity and dependency meaning stay compiler- and runtime-owned
- one product window owns one runtime-backed `RepositoryGraphSession`
- the IDE consumes package graph state only through Athena LSP

## Published Fixture

### Main Fixture

- [`examples/m5/repository-graph-proof/`](../../examples/m5/README.md)
- [`examples/m5/repository-graph-proof/athena.yaml`](../../examples/m5/repository-graph-proof/athena.yaml)
- [`examples/m5/repository-graph-proof/athena.lock`](../../examples/m5/repository-graph-proof/athena.lock)
- [`examples/m5/repository-graph-proof/src/root.athena`](../../examples/m5/repository-graph-proof/src/root.athena)

This fixture proves one governed repository root with one primary package and one canonical lock rendered in stable order.

## Proof Surface 1: Governed Repository Contract

### Main Boundaries

- [`kernel/repository-model/`](../../kernel/repository-model/README.md)
- [`ide/theia-backend/src/node/athena-repository-bootstrapper.ts`](../../ide/theia-backend/src/node/athena-repository-bootstrapper.ts)
- [`ide/lsp/`](../../ide/lsp/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:repository-model:test"
```

What this proves:

- Athena now treats `athena.yaml` as authored intent and `athena.lock` as derived state
- repository-root contract validation is explicit
- governed repository create/open flows reuse the same repository nouns instead of M4 single-file heuristics

## Proof Surface 2: Deterministic Package Graph And Lock

### Main Modules

- `:kernel:repository-model`
- `:kernel:compiler`

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"
```

What this proves:

- manifest dependency declarations become deterministic resolution inputs
- local-first resolution yields one canonical package graph from the same repository state
- Athena materializes and validates canonical `athena.lock`
- malformed or stale lock content stays visible through explicit package diagnostics

The published `examples/m5` fixture stays intentionally minimal. Wider local-first dependency resolution remains proven primarily through the compiler test corpus.

## Proof Surface 3: Runtime-Owned Repository Graph Session

### Main Modules

- `:kernel:runtime`
- `:ide:lsp`

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"
cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root examples\m5\repository-graph-proof"
```

What this proves:

- the active repository session is now a runtime-owned `RepositoryGraphSession`
- the session carries manifest, lock, resolved graph, and package diagnostics
- Athena LSP exposes package-state payloads from the runtime-owned session instead of from frontend-owned package models

## Proof Surface 4: Package-Aware Athena IDE

### Main Surfaces

- Athena Home
- Repository Navigator
- Repository Graph
- Problems
- Output
- Semantic Inspection

### Interactive Use

```powershell
Set-Location ide
yarn build
yarn start:smoke
yarn start
```

Then, inside the running Athena shell:

1. Open the repository fixture at `examples/m5/repository-graph-proof`.
2. Open `src/root.athena`.
3. Reveal `Repository Graph`, `Problems`, `Output`, and `Semantic Inspection` from the Athena workbench entry points.

What this proves:

- the existing Athena IDE now operates on governed repository/package meaning
- package diagnostics and repository graph feedback remain downstream of `frontend -> LSP -> runtime/compiler`
- `.athena` files now get narrow M5 editor hardening without creating a second parser in TypeScript

## Current Boundaries

M5 does prove:

- canonical repository-root manifest and lock contracts
- deterministic local-first package graph resolution
- runtime-owned repository graph session authority
- package diagnostics and repository graph feedback in the Athena IDE
- narrow `.athena` syntax highlighting and editor hardening subordinate to package-aware operation

M5 does not yet prove:

- semantic SCM, semantic review, or publish workflows
- remote registry, Git transport, or multi-vendor package transport
- multi-root repository sessions
- broad IDE authoring parity such as hover, rename, formatting, or full semantic-token support
- graphical projection or graph editing under Theia
