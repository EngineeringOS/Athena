# Athena M11 Proof Usage

## Purpose

This guide shows how to exercise the finished M11 proof surfaces:

- richer electrical projection families above one canonical source
- governed sheet, notation-pack, and repeated-reference contracts
- dense electrical repository delivery through runtime and `ide/lsp`
- downstream graph and workbench consumption without frontend-owned semantics

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node plus Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m11/milestone-summary-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m11/milestone-summary-2026-07-12.md)
- [`_bmad-output/implementation-artifacts/m11/m11-retrospective-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m11/m11-retrospective-2026-07-12.md)
- [`examples/m11/README.md`](../../examples/m11/README.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd integrations/graph-glsp <task>
yarn --cwd ide/theia-frontend <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## What M11 Proves

M11 proves that Athena can deepen the first electrical ECAD workbench without moving engineering meaning into sheets, symbols, graph adapters, or frontend state.

The central M11 claim is:

- one canonical engineering source can feed `cabinet`, `wiring`, `schematic`, and `documentation` families
- sheets, notation packs, and repeated references can remain governed downstream contracts
- dense electrical projection state can flow through runtime and `ide/lsp`
- graph adapters and workbench panels can consume richer projection data without becoming semantic authorities
- M8 mutation and M9 knowledge review paths still remain coherent under the denser electrical case

## Published Fixture

### Main Fixture

- [`examples/m11/dense-electrical-proof/`](../../examples/m11/dense-electrical-proof/)
- [`examples/m11/dense-electrical-proof/src/assembly-line.athena`](../../examples/m11/dense-electrical-proof/src/assembly-line.athena)

The dense proof repository intentionally includes:

- 16 components
- 48 ports
- 29 connections
- four delivered electrical view families
- documentation sheets with repeated-reference pressure
- M9-style motor knowledge inputs on `M1`

## Proof Surface 1: Projection Family, Sheet, And Notation Contracts

### Main Modules

- [`kernel/layout-model/`](../../kernel/layout-model/README.md)
- [`kernel/projection-model/`](../../kernel/projection-model/README.md)
- [`extensions/domain-electrical/`](../../extensions/domain-electrical/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test :extensions:domain-electrical:test"
```

What this proves:

- electrical family vocabulary is explicit and typed
- sheets remain projection-owned and separate from engineering identity
- notation packs remain inspectable downstream presentation contracts
- repeated-reference and cross-reference vocabulary exists before runtime and renderer delivery

## Proof Surface 2: Dense Compiler And Runtime Delivery

### Main Modules

- [`kernel/compiler/`](../../kernel/compiler/README.md)
- [`kernel/runtime/`](../../kernel/runtime/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM11DepthTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionDepthTest"
```

What this proves:

- the dense repository compiles through the normal compiler path
- documentation output produces stable sheets and cross references
- runtime-owned projection sessions carry family ids, notation packs, and repeated-reference payloads
- M9 knowledge diagnostics still remain available on the dense proof

## Proof Surface 3: LSP Delivery And Mutation Coherence

### Main Modules

- [`ide/lsp/`](../../ide/lsp/README.md)
- [`kernel/runtime/`](../../kernel/runtime/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest"
```

What this proves:

- the dense repository is delivered through the existing Athena LSP boundary
- documentation view can expose cross references over canonical semantic ids
- accepted source mutation still preserves knowledge diagnostics, impact consequences, and repeated-reference coherence

## Proof Surface 4: Graph Adapter And Workbench Consumption

### Main Modules

- `integrations/graph-glsp`
- [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`](../../ide/theia-frontend/src/browser/athena-graph-workbench-model.ts)
- [`ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`](../../ide/theia-frontend/src/browser/athena-semantic-selection-model.ts)

### Verification

```powershell
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
```

What this proves:

- graph adapters consume typed projection payloads instead of rebuilding meaning
- workbench selection still resolves through canonical semantic ids
- cross-reference counts and repeated occurrences can remain inspectable under dense view state
- fit-to-view and dense navigation behavior hold under the wider documentation scene

## Interactive Use

1. Start the Athena desktop shell.
2. Open `examples/m11/dense-electrical-proof`.
3. Open `src/assembly-line.athena`.
4. Open `Graphical View`.
5. Switch between `cabinet`, `wiring`, `schematic`, and `documentation`.
6. In `documentation`, inspect `M1` and confirm:
   - two documentation occurrences are visible
   - cross-reference state points to both documentation sheets
   - canonical semantic selection stays anchored on `component:M1`
7. Change `power "7.5kw"` on `M1` to `power "9kw"` and inspect mutation or review output.

What this proves:

- one authored source can drive all four view families
- repeated references stay downstream of one canonical subject
- dense electrical mutation still reuses the existing semantic review and knowledge path

## Full Verified Path

The following commands were confirmed during M11 closeout:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:projection-model:test"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM11DepthTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionDepthTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionM11DepthRequestTest"
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
```

## Current Boundaries

M11 does prove:

- serious electrical multi-view delivery above canonical semantics
- governed sheet, notation, and cross-reference contracts
- dense proof delivery through compiler, runtime, LSP, graph adapter, and workbench
- mutation and knowledge coherence under the denser electrical case

M11 does not yet prove:

- unrestricted graphical authoring
- full EPLAN-class symbol or catalog depth
- renderer-correct electrical readability on the level needed to make dense scenes feel like a trusted electrical CAD surface
- operator-grade cross-reference navigation flows such as coil-to-contact, source-to-destination, or related-diagnostic jumps
- larger readability benchmarks beyond the published dense proof repository
- desktop E2E automation of the full human-facing proof path
- huge-scene performance beyond the published dense proof
- final renderer architecture for future web or WASM product depth

Those concerns are intentionally carried forward into M12 instead of being treated as failed M11 scope.
