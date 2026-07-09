# Athena M6 Proof Usage

## Purpose

This guide shows how to exercise the finished M6 proof surfaces:

- the VCS-neutral semantic SCM kernel
- vendor-neutral baseline loading through the Git substrate adapter
- deterministic semantic diff, review summary, and commit intent generation
- runtime-owned semantic SCM and semantic history projection
- the Athena IDE `Semantic SCM` panel for review, commit, package evolution, and release relevance

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

## What M6 Proves

M6 proves that Athena can understand repository change semantically instead of delegating change meaning to Git-first or frontend-first models.

The central M6 claim is:

- baseline, diff, consequence, review, commit, and package-history meaning live in Athena semantic contracts
- baseline loading is vendor-neutral at the kernel boundary, even when Git is the first practical substrate
- runtime owns the active semantic SCM and semantic history projection state
- the IDE consumes review, commit, and package-history output only through Athena LSP
- later graphical work can consume the same history payloads without redefining semantic truth

## Published Fixture

M6 reuses the governed repository fixture introduced in M5 as the active repository input:

- [`examples/m5/repository-graph-proof/`](../../examples/m5/README.md)
- [`examples/m5/repository-graph-proof/athena.yaml`](../../examples/m5/repository-graph-proof/athena.yaml)
- [`examples/m5/repository-graph-proof/athena.lock`](../../examples/m5/repository-graph-proof/athena.lock)
- [`examples/m5/repository-graph-proof/src/root.athena`](../../examples/m5/repository-graph-proof/src/root.athena)

Semantic baseline and history windows are primarily proven through the test corpus in M6 because the same active repository can be compared against different baseline repositories and package states.

## Proof Surface 1: Semantic SCM Kernel

### Main Boundaries

- [`kernel/semantic-scm/`](../../kernel/semantic-scm/README.md)
- [`integrations/scm-git/`](../../integrations/scm-git/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:semantic-scm:test :integrations:scm-git:test"
```

What this proves:

- Athena owns a dedicated VCS-neutral semantic SCM contract boundary above `:kernel:repository-model`
- baseline loading is performed behind a substrate adapter seam rather than through Git-shaped public nouns
- the same semantic inputs yield deterministic diff, review, commit, and history output

## Proof Surface 2: Runtime-Owned Semantic SCM And History State

### Main Modules

- `:kernel:runtime`
- `:ide:lsp`

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test :ide:lsp:test"
```

What this proves:

- runtime owns semantic baseline, diff, review, commit, and package-history projection state
- Athena LSP exposes additive request surfaces for:
  - semantic review and commit state
  - package evolution and release relevance
- unresolved baselines remain inspectable through typed diagnostics instead of opaque UI-only text

## Proof Surface 3: Athena IDE Semantic SCM Panel

### Main Surfaces

- Athena Home
- Repository Navigator
- Repository Graph
- Problems
- Output
- Semantic Inspection
- Semantic SCM

### Build Verification

```powershell
Set-Location ide
yarn build
yarn start:smoke
```

### Interactive Use

```powershell
Set-Location ide
yarn start
```

Then, inside the running Athena shell:

1. Open the repository fixture at `examples/m5/repository-graph-proof`.
2. Open `src/root.athena`.
3. Reveal `Semantic SCM` from the Athena workbench entry points.
4. Inspect:
   - baseline-driven review entries
   - commit-preparation entries
   - package evolution
   - release relevance
   - contract-break risk
   - validation movement

What this proves:

- the existing Athena workbench can inspect semantic SCM output without a shell rewrite
- review, commit, and package-history state stay downstream of `frontend -> LSP -> runtime/compiler`
- the M6 package-history output is ready for later visual consumption without widening M6 into GLSP or canvas work

## Current Boundaries

M6 does prove:

- VCS-neutral semantic SCM contracts
- vendor-neutral baseline loading with Git as the first substrate
- deterministic semantic diff, consequence, review, commit, and history output
- runtime-owned review/commit/package-history projection
- package evolution and release relevance in the current Athena IDE path

M6 does not yet prove:

- graphical semantic-history review
- remote registry or publish transport workflows
- multi-substrate baseline loading beyond the first Git-backed adapter
- automated interactive IDE E2E coverage for the semantic SCM panel
