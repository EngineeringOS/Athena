# Athena M4 Proof Usage

## Purpose

This guide shows how to exercise the finished M4 proof surfaces:

- the branded Athena Theia desktop shell
- the runtime-backed single-repository session path
- the Athena LSP boundary for diagnostics and core authoring support
- the professional multi-panel workbench with read-only semantic inspection

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

## What M4 Proves

M4 proves that Athena is no longer only a semantic kernel and runtime proof. It is now also a serious desktop-first IDE product proof.

The central M4 claim is:

- Athena can launch as its own Theia-based engineering product shell
- one Athena window can host one runtime-backed Engineering Repository session
- authored `.athena` files reach meaning only through Athena LSP
- diagnostics, completion, symbols, definition, references, and semantic inspection stay downstream of the JVM semantic stack
- the workbench can host professional navigation, Problems, Output, and inspection panels without becoming a second semantic authority

## Proof Surface 1: Desktop Product Shell

### Main Packages

- [`ide/theia-product/`](../../ide/theia-product/README.md)
- [`ide/theia-frontend/`](../../ide/theia-frontend/README.md)
- [`ide/theia-backend/`](../../ide/theia-backend/README.md)

### Verification

```powershell
Set-Location ide
yarn build
yarn start:smoke
```

What this proves:

- the Athena desktop shell builds from the real `ide/` product path
- the Electron wrapper resolves Java 25 automatically on Windows
- the product reaches a real window-ready state instead of only spawning a process
- the shell is Athena-owned rather than a generic upstream Theia sample

## Proof Surface 2: Repository Open And Create Path

### Main Boundaries

- [`ide/lsp/`](../../ide/lsp/README.md)
- [`ide/theia-backend/`](../../ide/theia-backend/README.md)
- [`ide/theia-frontend/`](../../ide/theia-frontend/README.md)
- [`examples/m4/open-repository-proof/`](../../examples/m4/open-repository-proof/)

### Verification

Build the JVM host and the Theia workspace:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"
Set-Location ide
yarn build
```

Repository-open proof against the published fixture:

```powershell
cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root examples\m4\open-repository-proof"
```

Repository-create-and-activate proof:

```powershell
$parent = Join-Path $env:TEMP ("athena-m4-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $parent | Out-Null
$bootstrap = @'
const { AthenaRepositoryBootstrapper } = require("./ide/theia-backend/lib/node/athena-repository-bootstrapper");
(async () => {
  const result = await new AthenaRepositoryBootstrapper().createRepository(process.argv[2], "Factory Line Alpha");
  console.log(JSON.stringify(result));
})().catch(error => {
  console.error(error.stack);
  process.exit(1);
});
'@ | node - $parent
$info = $bootstrap | ConvertFrom-Json
cmd /c "call java25 && echo shutdown | ide\lsp\build\install\athena-lsp-host\bin\athena-lsp-host.bat --repository-root `"$($info.repositoryRootPath)`""
Remove-Item -Recurse -Force $parent
```

What this proves:

- Athena opens exactly one repository into exactly one runtime-backed session
- repository creation reuses the same activation path instead of inventing a frontend shortcut
- the temporary M4 bootstrap remains intentionally light:
  - `src/<project>.athena`
  - no final `athena.yaml`
  - no final `athena.lock`

## Proof Surface 3: Athena LSP Authoring Boundary

### Main Modules

- `:ide:lsp`
- [`ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts`](../../ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:build"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain build"
```

What this proves:

- `initialize` activates the repository inside the JVM host
- `textDocument/didOpen` and `textDocument/didChange` drive tracked in-memory Athena document state
- diagnostics are published from Athena-owned parsing and semantic validation
- completion, document symbols, definition, and references are served from the same JVM-owned tracked state
- repeated edits stay monotonic and reject stale rollback versions

## Proof Surface 4: Professional Workbench And Semantic Inspection

### Main Surfaces

- Athena Home
- Repository Navigator
- Problems
- Output
- Semantic Inspection

### Interactive Use

```powershell
Set-Location ide
yarn start
```

Then, inside the running Athena shell:

1. Open the repository fixture at `examples/m4/open-repository-proof`.
2. Open `src/factory-line.athena`.
3. Use the Athena-owned entry points under the `View -> Athena` submenu or the Athena Home quick actions.
4. Reveal `Repository Navigator`, `Problems`, `Output`, and `Semantic Inspection`.

What this proves:

- the workbench is IDE-shaped rather than a bare editor window
- Athena-owned panels attach additively through explicit product-owned extension points
- semantic inspection is read-only and derived from the same LSP-owned tracked state as diagnostics and navigation
- frontend layout stays downstream of `frontend -> LSP -> runtime/compiler`

## Current Boundaries

M4 does prove:

- desktop-first Athena Theia product embodiment
- curated product-owned workbench capability set
- runtime-backed single-session repository ownership
- Athena LSP as the sole semantic entry point for the IDE path
- diagnostics, navigation, and semantic inspection inside the workbench
- additive professional workbench composition

M4 does not yet prove:

- syntax highlighting or semantic tokens for `.athena`
- hover, rename, formatting, or richer multi-file tooling
- multi-root repository support
- final `athena.yaml`, `athena.lock`, or package graph rules
- semantic SCM
- graphical projection or graph editing under Theia
