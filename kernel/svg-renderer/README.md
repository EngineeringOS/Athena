# `:kernel:svg-renderer`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:svg-renderer` module owns the thin render-facing model and deterministic SVG emission for the M0 proof. It renders downstream artifacts from semantic truth but does not create or reinterpret that truth.

## Responsibilities

- Define the render-facing DTOs in `SvgRenderModel.kt`.
- Emit simple stable SVG strings in `SvgRenderer.kt`.
- Stay downstream of canonical `Engineering IR`.
- Keep rendering logic small, deterministic, and free of semantic recovery.

## Main Types

- `SvgRenderModel`
- `SvgRenderBox`
- `SvgRenderConnection`
- `SvgRenderer`

## Dependencies

- `:kernel:validation`
- `:kernel:engineering-model`

## Boundaries

This module does not parse source text, validate engineering semantics, own plugin contracts, or infer layout semantics from invalid input. The compiler must hand it a thin render model that is already safe to render.

## Verification

```bash
./gradlew :kernel:svg-renderer:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:svg-renderer:test
```
