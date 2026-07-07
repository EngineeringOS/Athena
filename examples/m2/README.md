# Athena M2 Proof Corpus

English | [Chinese (Simplified)](README.zh-CN.md)

`examples/m2/` publishes the minimal proof corpus for the first geometry-backed backend chain and the desktop operator proof seed.

## Purpose

- Keep one shared semantic seed for both supported M2 views.
- Publish only the artifacts needed to verify geometry-backed SVG output.
- Keep the corpus small enough to review during kernel and architecture work.
- Keep a connection-free desktop seed that lets the runtime prove command-backed connection creation.

## Contents

- `demo-cabinet.athena` - shared semantic seed
- `demo-cabinet.expectation.txt` - artifact map for deterministic proof checks
- `demo-cabinet.cabinet.svg` - expected backend SVG for the `cabinet` view
- `demo-cabinet.wiring.svg` - expected backend SVG for the `wiring` view
- `operator-proof.athena` - desktop workbench seed that starts without authored connections

## Boundary

This folder is not a general export area. It is a milestone proof corpus used by automated tests to verify that `Geometry IR` is the renderer-facing contract for the first backend path and that the desktop workbench can prove runtime-owned interaction on top of the same semantics.
