# M30 Demo Usage

## Open The Sample

Use the M30 sample workspace:

```powershell
yarn --cwd ide start:m30
```

Workspace:

```text
examples/m30/sample-project
```

Primary source:

```text
examples/m30/sample-project/src/01-rolling-shutter-control-source.athena
```

## Run Product Proof

Build the product if needed, then run the product smoke:

```powershell
yarn --cwd ide build
yarn --cwd ide start:smoke:m30
```

The smoke opens the M30 sample, verifies structured representation proof, and captures:

```text
_bmad-output/implementation-artifacts/m30/screenshots/m30-graph-workbench-smoke.png
```

## What To Inspect

Use the screenshot as a human visual regression guard only. The authoritative proof is the structured payload printed as:

```text
ATHENA_M30_REPRESENTATION_PROOF=
ATHENA_GRAPH_WORKBENCH_PROOF=
```

The proof checks representation library ids, binding counts, anchor usage, composition bounds, route anchors, transparent chrome, sheet selector persistence, and nested Outline paths.

## Visual Reference Boundary

M30 uses QElectroTech and EPLAN only as qualitative visual reference material for professional density, linework, and engineering drawing expectations.

M30 makes no QET/EPLAN parity claim. It does not load QET assets during product execution, does not import `.elmt` files at runtime, and does not encode visual primitives in `.athena` semantic source.
