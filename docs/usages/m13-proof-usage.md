# Athena M13 Proof Usage

Updated: 2026-07-13

## Purpose

This guide shows how to exercise the finished M13 proof surfaces:

- neutral `Presentation IR` above semantic and projection authority
- primitive and composite electrical presentation packs
- runtime and `ide/lsp` delivery of presentation payloads
- graph-adapter and Theia workbench consumption of `diagram.presentation`
- backend independence above one proof rendering path

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node plus Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m13/README.md`](../../_bmad-output/implementation-artifacts/m13/README.md)
- [`_bmad-output/implementation-artifacts/m13/milestone-summary-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m13/milestone-summary-2026-07-12.md)
- [`_bmad-output/implementation-artifacts/m13/m13-retrospective-2026-07-12.md`](../../_bmad-output/implementation-artifacts/m13/m13-retrospective-2026-07-12.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd integrations/graph-glsp <task>
yarn --cwd ide/theia-frontend <task>
yarn --cwd ide/theia-product <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## What M13 Proves

M13 proves that Athena can make presentation a governed downstream language instead of a frontend reconstruction habit.

The central M13 claim is:

- `Engineering IR` remains canonical engineering truth
- `Projection Model` remains the renderer-neutral view contract
- `Presentation IR` becomes the downstream presentation language
- primitive and composite packs remain extension-compatible presentation assets
- runtime, LSP, graph adapters, and workbench code consume Athena-owned presentation payloads instead of inventing local presentation truth

## Published Fixture

### Main Proof Fixture

- [`examples/m0/demo-cabinet.athena`](../../examples/m0/demo-cabinet.athena)

The first M13 proof intentionally reuses the narrow canonical fixture instead of introducing a second semantic corpus just to test presentation.

## Proof Surface 1: Presentation IR Contract

### Main Modules

- [`kernel/presentation-model/`](../../kernel/presentation-model/README.md)
- [`kernel/plugins/plugin-api/`](../../kernel/plugins/plugin-api/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test"
```

What this proves:

- Athena now has a domain-neutral `PresentationDocument`
- primitive packs, composite packs, occurrences, and connectors are typed kernel contracts
- semantic macro and backend-specific draw trees stay outside the presentation boundary

## Proof Surface 2: Compiler Derivation And Electrical Packs

### Main Modules

- [`kernel/compiler/`](../../kernel/compiler/README.md)
- [`extensions/domain-electrical/`](../../extensions/domain-electrical/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test --tests com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPluginTest"
```

What this proves:

- compiler derivation rebuilds `Presentation IR` from projection-owned contracts
- electrical primitive and composite packs are published through extension-owned contracts
- one canonical subject can produce family-specific presentation without identity drift

## Proof Surface 3: Runtime And LSP Delivery

### Main Modules

- [`kernel/runtime/`](../../kernel/runtime/README.md)
- [`ide/lsp/`](../../ide/lsp/README.md)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"
```

What this proves:

- runtime sessions publish presentation snapshots as Athena-owned state
- LSP transports flattened presentation payloads without moving authority into the client
- supported views still stay projection-owned while richer presentation stays downstream

## Proof Surface 4: Graph Adapter And Workbench Consumption

### Main Modules

- `integrations/graph-glsp`
- [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`](../../ide/theia-frontend/src/browser/athena-graph-workbench-model.ts)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-presentation-node.tsx)

### Verification

```powershell
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
```

What this proves:

- GLSP translation normalizes and carries `diagram.presentation`
- Theia graph workbench prefers presentation occurrences and symbol commands over generic graph fallback
- the desktop product still builds with the new presentation path integrated

## Interactive Use

1. Start the Athena desktop shell.
2. Open `examples/m4/open-repository-proof`.
3. Open `src/factory-line.athena`.
4. Open `Graphical View`.
5. Switch between `cabinet`, `wiring`, and `schematic`.
6. Confirm the graph surface is using presentation-owned device parts and connectors rather than generic cards and generic lines as the primary rendering source.

What this proves:

- the desktop path is consuming Athena-owned presentation payloads
- presentation stays downstream of the same canonical repository and semantic ids

## Full Verified Path

The following commands were confirmed during the M13 closeout pass:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:presentation-model:test"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:runtime:test --tests com.engineeringood.athena.runtime.AthenaRuntimeProjectionSessionTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test --tests com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPluginTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :ide:lsp:test --tests com.engineeringood.athena.ide.lsp.AthenaProjectionRequestTest"
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
```

## Current Boundaries

M13 does prove:

- neutral presentation language foundation above projection
- extension-compatible primitive and composite electrical presentation packs
- canonical traceability across presentation occurrences
- backend independence above one proof rendering path
- runtime, transport, and workbench consumption of presentation payloads

M13 does not yet prove:

- semantic macro or engineering assembly authoring
- broad multi-domain presentation parity
- final renderer-performance architecture
- final product skin or emotion-system depth
- unrestricted graphical authoring over the presentation layer
