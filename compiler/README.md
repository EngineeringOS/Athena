# `:compiler`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:compiler` module is Athena's orchestration core. It exposes the public compiler facade, owns compiler pipeline reporting, publishes plugin contracts and discovery logic, coordinates domain semantics, resolves governed knowledge packages, validates external boundary descriptors, and drives downstream rendering.

## Responsibilities

- Expose `AthenaCompiler` with `parse`, `lower`, and `compile` entry points.
- Keep the declared pass order stable: `PARSE -> LOWER -> VALIDATE -> DOWNSTREAM_DERIVATION`.
- Lower syntax-owned source into canonical `Engineering IR`.
- Run generic semantic validation and domain-plugin validation.
- Publish core-owned plugin contracts, manifests, validation, discovery, and approved inventory models.
- Load and resolve governed knowledge packages.
- Load and validate external boundary descriptors.
- Derive the SVG render model and call the SVG renderer.

## Main Areas

- `AthenaCompiler`: facade and pipeline orchestration.
- `CompilerModels.kt`: public compiler result models.
- `EngineeringIrLowerer`: syntax-to-IR lowering.
- `plugin/*`: plugin contracts, manifests, validation, discovery, and domain coordination.
- `knowledge/*`: governed knowledge package models, loading, and resolution.
- `boundary/*`: external boundary descriptor models, loading, and resolution.

## Dependencies

- `:language`
- `:semantics-core`
- `:ir`
- `:renderer-svg`

Test-only dependency:

- `:domain-electrical-runtime`

## Boundaries

This module does not own the DSL grammar itself, the canonical IR schema, or the concrete Electrical/Runtime domain rules. It orchestrates those pieces while preserving the architecture rule that the DSL is the authored source, `Engineering IR` is the canonical model, and renderers are downstream backends.

## Verification

```bash
./gradlew :compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :compiler:test
```
