# Athena M7 Proof Usage

## Purpose

This guide shows how to exercise the finished M7 proof surfaces:

- the translation-only graph adapter boundary
- the graph-first Athena graphical workbench
- extension-owned electrical view definitions and renderer mappings
- the recorded M7 graphical technology decision

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node/Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/milestone-summary-2026-07-10.md)
- [`_bmad-output/implementation-artifacts/m7/epic-1-retro-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/epic-1-retro-2026-07-10.md)
- [`_bmad-output/implementation-artifacts/m7/epic-2-retro-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/epic-2-retro-2026-07-10.md)
- [`_bmad-output/implementation-artifacts/m7/epic-3-retro-2026-07-10.md`](../../_bmad-output/implementation-artifacts/m7/epic-3-retro-2026-07-10.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd integrations/graph-glsp <task>
yarn --cwd ide <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## What M7 Proves

M7 proves that Athena can host a real graphical engineering projection without moving semantic truth into canvas state.

The central M7 claim is:

- runtime and `ide/lsp` remain the only projection authorities
- graph-framework vocabulary stays behind the translation-only adapter boundary
- the Athena workbench can host a graph-first split engineering surface
- electrical view definitions and graphical surface mappings enter through extensions
- the M7 technology direction is explicit enough for later milestone planning

## Published Fixture

### Main Fixture

- [`examples/m4/open-repository-proof/`](../../examples/m4/open-repository-proof/)
- [`examples/m4/open-repository-proof/src/factory-line.athena`](../../examples/m4/open-repository-proof/src/factory-line.athena)

M7 intentionally reuses the governed repository fixture published earlier instead of inventing a special graphical-only repository shape.

The proof is now graphical because the same repository can be opened into:

- code view
- semantic inspection
- graphical projection
- extension-owned cabinet and wiring renderer mappings

## Proof Surface 1: Translation-Only Graph Adapter

### Main Modules

- [`integrations/graph-glsp/`](../../integrations/graph-glsp/README.md)
- [`ide/theia-frontend/src/browser/athena-graph-adapter-service.ts`](../../ide/theia-frontend/src/browser/athena-graph-adapter-service.ts)

### Verification

```powershell
yarn --cwd integrations/graph-glsp test
```

What this proves:

- Athena projection payloads are translated into GLSP-shaped graph data through an explicit adapter seam
- graph vocabulary remains outside `kernel/` and `ide/lsp`
- the adapter stays translation-only instead of becoming a semantic or transport authority

## Proof Surface 2: Extension-Owned Electrical Projection Mappings

### Main Modules

- [`extensions/domain-electrical/`](../../extensions/domain-electrical/README.md)
- [`kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt`](../../kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaRuntimeProjectionSession.kt)
- [`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt`](../../ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionProtocol.kt)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test :kernel:runtime:test :ide:lsp:test"
```

What this proves:

- `cabinet` and `wiring` stay extension-owned view definitions
- graphical surface mappings are published as downstream metadata, not kernel-owned domain logic
- active render contributions flow through runtime and `ide/lsp` without creating a second projection authority

## Proof Surface 3: Graph-First Athena Workbench

### Main Modules

- [`ide/theia-frontend/`](../../ide/theia-frontend/README.md)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`](../../ide/theia-frontend/src/browser/athena-graph-workbench-model.ts)

### Verification

```powershell
yarn --cwd ide/theia-frontend test
java25; yarn --cwd ide build
java25; yarn --cwd ide start:smoke
```

### Interactive Use

```powershell
java25; yarn --cwd ide start
```

Then, inside the running Athena shell:

1. Open the repository fixture at `examples/m4/open-repository-proof`.
2. Open `src/factory-line.athena`.
3. Reveal `Graphical View`.
4. Confirm the graph opens beside the source editor.
5. Switch between `Cabinet` and `Wiring`.
6. Pan, zoom, and fit the graph viewport.
7. Toggle the floating info panel and inspect selection plus session metadata.

What this proves:

- the workbench is graph-first rather than a small preview card
- source and graph stay visible together in the current product shell
- navigation is infinite-canvas style and inspect-first
- cabinet and wiring surface profiles differ through extension-owned mappings

## Proof Surface 4: Technology Decision Record

### Main Record

- [`_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md`](../../_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-09-m7/GRAPHICAL-TECHNOLOGY-DECISION.md)

What this proves:

- M7 did not end in an unresolved framework debate
- the chosen path is evaluated against Athena constraints:
  - JVM-first authority
  - Theia product-shell fit
  - translation-only adapters
  - inspectability
  - deterministic refresh
- fuller GLSP-class runtime integration is kept as a follow-on option instead of a hidden M7 dependency

## Current Boundaries

M7 does prove:

- real graphical projection inside the Athena workbench
- graph-first split workbench posture
- runtime/LSP-owned projection authority
- extension-owned view definitions and downstream renderer mappings
- explicit technology direction for the first graphical stack

M7 does not yet prove:

- unrestricted graphical editing
- governed bidirectional code/graph mutation
- final notation-pack editing
- full GLSP runtime adoption as the live editor core
- final UX skin system or QElectroTech/EPLAN-class domain depth
