# Athena M2 Proof Usage

## Purpose

This guide shows how to exercise the finished M2 proof surfaces:

- the geometry-backed backend proof
- the desktop multi-view operator proof

It assumes the workspace is already checked out locally and Java 25 is available through `java25`.

## Operating Rule

Run Gradle sequentially on this Windows workstation.

Use:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain <task>
```

## Proof Surface 1: Geometry-Backed Backend Chain

### Semantic Seed

Use:

- [`examples/m2/demo-cabinet.athena`](../../examples/m2/demo-cabinet.athena)

This one semantic source feeds both supported M2 views:

- `cabinet`
- `wiring`

### Expected Published Artifacts

- [`examples/m2/demo-cabinet.expectation.txt`](../../examples/m2/demo-cabinet.expectation.txt)
- [`examples/m2/demo-cabinet.cabinet.svg`](../../examples/m2/demo-cabinet.cabinet.svg)
- [`examples/m2/demo-cabinet.wiring.svg`](../../examples/m2/demo-cabinet.wiring.svg)

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :kernel:compiler:test
```

What this proves:

- `Engineering IR` lowers from the authored DSL
- `Layout IR` is derived for `cabinet` and `wiring`
- `Geometry IR` is derived from those layouts
- the SVG backend consumes geometry as geometry
- the published expected SVGs remain deterministic

## Proof Surface 2: Desktop Multi-View Operator Flow

### Desktop Seed

Use:

- [`examples/m2/operator-proof.athena`](../../examples/m2/operator-proof.athena)

This seed starts without authored connections. The proof is that the runtime command path creates:

- `connection:PLC1.out->M1.in`

while preserving canonical selection across:

- `cabinet`
- `wiring`

### Verification

```powershell
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:test
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:bootstrapSmoke
java25; .\gradlew.bat --no-daemon --console=plain :apps:desktop-viewer:operatorProofSmoke
```

What this proves:

- the desktop shell consumes runtime-owned projection sessions
- active-view switching stays runtime-owned
- canonical semantic identity stays stable across both views
- the supported `connect ports` mutation path refreshes projection state
- command history and semantic diff remain the primary explanation of change

### Interactive Launch

```powershell
java25; .\gradlew.bat :apps:desktop-viewer:run
```

The default desktop bootstrap opens the `operator-proof` seed automatically.

## Full Milestone Verification

If you want the normal close-out check for the whole workspace:

```powershell
java25; .\gradlew.bat --no-daemon --console=plain build
```

## Boundaries

M2 does prove:

- explicit `Layout IR`
- explicit `Geometry IR`
- synchronized multi-view projection
- runtime-owned projection sessions
- geometry-backed backend output
- desktop operator proof over one command-backed mutation path

M2 does not prove:

- arbitrary manual layout authoring
- arbitrary geometry editing
- browser-first Studio delivery
- cloud collaboration
- external CAD adapter breadth
