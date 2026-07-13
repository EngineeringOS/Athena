# Athena M12 Proof Usage

Updated: 2026-07-13

## Purpose

This guide shows how to exercise the finished M12 proof surfaces:

- renderer-correct electrical connection presentation above canonical semantics
- stable endpoint anchoring and canonical selection coherence
- reliable fit, pan, zoom, and resize behavior on a larger benchmark scene
- compact graph-side controls and dense Athena-owned support panels
- IDE-theme-relative cabinet and wiring surfaces instead of hardcoded graph skin colors
- first repeated-reference reveal and related-subject navigation in the workbench

It assumes the workspace is already checked out locally, `java25` is available on this workstation, and Node plus Yarn are already usable for the Theia workspace.

## Companion Records

- [`_bmad-output/implementation-artifacts/m12/README.md`](../../_bmad-output/implementation-artifacts/m12/README.md)
- [`examples/m12/README.md`](../../examples/m12/README.md)

## Operating Rule

Run JVM and Node verification sequentially on this Windows workstation.

Use:

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain <task>"
yarn --cwd integrations/graph-glsp <task>
yarn --cwd ide/theia-frontend <task>
yarn --cwd ide <task>
```

Do not overlap Gradle and Yarn builds, tests, or desktop runs in parallel shells.

## What M12 Proves

M12 proves that Athena can harden the first electrical renderer and workbench without moving engineering meaning into the canvas, renderer heuristics, or frontend-owned state.

The central M12 claim is:

- electrical connections can render with clearer conductor intent while staying downstream of governed anchors and routing corridors
- endpoint selection can still resolve through canonical ports and connections rather than line-center guesswork
- larger scenes can keep reliable fit and resize behavior through runtime-owned projection state
- graph-side controls can stay compact, canvas-first, and less distracting without becoming shell chrome
- cabinet and wiring views can follow the IDE theme instead of taking hardcoded product colors as truth
- repeated-reference reveal and related-subject navigation can remain canonical-selection driven
- denser workbench styling can become more IDE-like without becoming a second product shell

## Published Benchmark Tiers

### Baseline Tier

- [`examples/m11/dense-electrical-proof/src/assembly-line.athena`](../../examples/m11/dense-electrical-proof/src/assembly-line.athena)
- 16 components
- 48 ports
- 29 connections

### M12 Renderer Benchmark Tier

- [`examples/m12/renderer-benchmark-proof/src/expansion-line.athena`](../../examples/m12/renderer-benchmark-proof/src/expansion-line.athena)
- 24 components
- 74 ports
- 45 connections

## Proof Surface 1: Endpoint And Selection Coherence

### Main Modules

- [`ide/theia-frontend/src/browser/athena-semantic-selection-model.ts`](../../ide/theia-frontend/src/browser/athena-semantic-selection-model.ts)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-edge-layer.tsx)
- [`ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx`](../../ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx)

### Verification

```powershell
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
```

What this proves:

- endpoint aliases stay traceable to canonical ports and connections
- selecting one rendered terminal can still reveal the same semantic subject in inspection and SCM surfaces
- repeated-reference and related-subject reveal do not invent a renderer-owned identity graph

## Proof Surface 2: Viewport Reliability

### Main Modules

- [`ide/theia-frontend/src/browser/athena-graph-workbench-model.ts`](../../ide/theia-frontend/src/browser/athena-graph-workbench-model.ts)
- [`ide/theia-frontend/src/browser/style/index.css`](../../ide/theia-frontend/src/browser/style/index.css)

### Verification

```powershell
yarn --cwd ide/theia-frontend test
yarn --cwd ide build
yarn --cwd ide start:smoke
```

What this proves:

- fit-to-viewport uses actual normalized scene bounds
- manual viewport sessions preserve focus when the workbench resizes
- graph-surface controls stay compact and subordinate to the canvas
- theme-relative cabinet and wiring surfaces remain consistent with the active IDE shell
- the desktop shell still starts cleanly after the M12 frontend hardening

## Proof Surface 3: Larger Renderer Benchmark Fixture

### Main Modules

- [`examples/m12/renderer-benchmark-proof/`](../../examples/m12/renderer-benchmark-proof/)
- [`kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt`](../../kernel/compiler/src/test/kotlin/com/engineeringood/athena/compiler/AthenaCompilerM12RendererBenchmarkTest.kt)

### Verification

```powershell
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM12RendererBenchmarkTest"
```

What this proves:

- M12 ships a governed electrical fixture larger than the M11 dense baseline
- the larger fixture still compiles through the normal compiler and projection path
- documentation view still publishes repeated references over canonical semantic ids

## Interactive Use

1. Start the Athena desktop shell.
2. Open `examples/m12/renderer-benchmark-proof`.
3. Open `src/expansion-line.athena`.
4. Open `Graphical View`.
5. Use fit-to-viewport and confirm the whole electrical scene is immediately visible.
6. Select one connection terminal and confirm the selection resolves to a canonical port, not only the connection line.
7. Select `component:M1` or `component:M5`, open the information panel, and use `Show repeated references` to move into `documentation`.
8. In `documentation`, use the `Related` section to reveal owner, port, or connection subjects and confirm inspection and SCM panels follow the same canonical selection.

## Full Verified Path

The following commands were confirmed during the M12 closeout pass:

```powershell
yarn --cwd integrations/graph-glsp test
yarn --cwd ide/theia-frontend test
yarn --cwd ide/theia-product build
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :extensions:domain-electrical:test --tests com.engineeringood.athena.domain.electricalruntime.ElectricalRuntimeDomainPluginTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerTest"
cmd /c "call java25 && .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test --tests com.engineeringood.athena.compiler.AthenaCompilerM12RendererBenchmarkTest"
```

## Current Boundaries

M12 does prove:

- clearer conductor-style connection rendering over governed electrical anchors
- canonical endpoint selection and related-subject reveal
- larger benchmark validation beyond the M11 dense baseline
- resize-safe viewport behavior and compact graph-side controls
- IDE-theme-relative cabinet and wiring surfaces for the current desktop shell
- repeated-reference reveal through the current governed view-switch path

M12 does not yet prove:

- full IEC-grade notation depth or final symbol-pack breadth
- true sheet-specific jump commands beyond the current view-family reveal path
- final render-IR, token-system, or emotion-system architecture for future product skins
- unrestricted graphical authoring or full ECAD parity
- huge-scene performance beyond the published M12 benchmark tier
