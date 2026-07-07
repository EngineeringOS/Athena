# `:kernel:geometry-model`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:geometry-model` module defines Athena's first explicit renderer-facing geometry contracts for M2. These types stay downstream of layout intent and canonical semantics, giving later viewers and backends a governed geometry boundary without rebuilding meaning from scratch.

## Responsibilities

- Define the first `Geometry IR` document and element contracts.
- Define minimal renderer-facing path and point contracts for the first projection proof.
- Keep canonical semantic identity attached to every geometry element.
- Provide small renderer-facing geometry kinds and bounds for the initial proof slice.
- Give M2 a durable kernel-owned geometry boundary instead of extending renderer-local DTOs into platform truth.

## Main Types

- `GeometryDocument`
- `GeometryElement`
- `GeometryElementId`
- `GeometryElementKind`
- `GeometryBounds`
- `GeometryPoint`

## Dependencies

- `:kernel:engineering-model`

## Boundaries

This module does not derive geometry from layout, does not emit SVG, does not own view switching, and does not redefine engineering semantics. It is the explicit geometry contract layer that later compiler, runtime, viewer, and backend stories use for precise placements and path geometry without rebuilding meaning from scratch.

## Verification

```bash
./gradlew :kernel:geometry-model:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:geometry-model:test
```
