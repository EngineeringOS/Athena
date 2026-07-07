# `:kernel:svg-renderer`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:svg-renderer` module owns deterministic SVG emission for the current proof backend. In M2 it consumes explicit `Geometry IR` directly and emits stable SVG without recreating semantic or layout meaning.

## Responsibilities

- Define the thin runtime/viewer DTOs in `SvgRenderModel.kt`.
- Emit simple stable SVG strings in `SvgRenderer.kt`.
- Consume explicit `Geometry IR` directly for the first backend proof.
- Keep rendering logic small, deterministic, and free of semantic recovery.

## Main Types

- `SvgRenderModel`
- `SvgRenderBox`
- `SvgRenderConnection`
- `SvgRenderer`

## Dependencies

- `:kernel:engineering-model`
- `:kernel:geometry-model`

## Boundaries

This module does not parse source text, validate engineering semantics, own plugin contracts, or infer layout semantics from invalid input. The compiler must hand it explicit `Geometry IR` for backend emission or a thin viewer model already derived from geometry. The current SVG proof renders the `BOX` and `PATH` subset needed by the first backend chain and does not invent missing semantics.

## Verification

```bash
./gradlew :kernel:svg-renderer:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:svg-renderer:test
```
