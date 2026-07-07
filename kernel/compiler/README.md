# `:kernel:compiler`

English | [Chinese (Simplified)](README.zh-CN.md)

The `:kernel:compiler` module is Athena's orchestration core. It exposes the public compiler facade, owns compiler pipeline reporting, publishes plugin contracts and discovery logic, coordinates domain semantics, resolves governed knowledge packages, validates external boundary descriptors, derives explicit `Layout IR`, derives explicit `Geometry IR`, and drives the first geometry-backed downstream backend path.

## Responsibilities

- Expose `AthenaCompiler` with `parse`, `lower`, and `compile` entry points.
- Keep the declared pass order stable: `PARSE -> LOWER -> VALIDATE -> DOWNSTREAM_DERIVATION`.
- Lower syntax-owned source into canonical `Engineering IR`.
- Run generic semantic validation and domain-plugin validation.
- Derive supported `Layout IR` documents from canonical `Engineering IR` plus typed `ViewDefinition` contributions.
- Derive supported `Geometry IR` documents from explicit `Layout IR`.
- Publish core-owned plugin contracts, manifests, validation, discovery, and approved inventory models.
- Load and resolve governed knowledge packages.
- Load and validate external boundary descriptors.
- Derive the runtime viewer model from selected `Geometry IR`.
- Feed selected `Geometry IR` directly into the SVG backend.

## Main Areas

- `AthenaCompiler`: facade and pipeline orchestration.
- `LayoutIrDeriver`: deterministic `Engineering IR -> Layout IR` derivation for supported views.
- `GeometryIrDeriver`: deterministic `Layout IR -> Geometry IR` derivation for supported views.
- `CompilerModels.kt`: public compiler result models.
- `EngineeringIrLowerer`: syntax-to-IR lowering.
- `plugin/*`: plugin contracts, manifests, validation, discovery, and domain coordination.
- `knowledge/*`: governed knowledge package models, loading, and resolution.
- `boundary/*`: external boundary descriptor models, loading, and resolution.

## Incremental Refresh Boundary

Story `2.3` adds the first narrow incremental recompute proof for M2:

- Scope is limited to the runtime-owned `connect ports` mutation path.
- Validation, layout, geometry, and downstream rendering each report whether scoped reuse stayed valid or fell back.
- `LayoutIrDeriver` and `GeometryIrDeriver` may reuse unchanged projection objects when the refreshed documents remain structurally stable.
- The compiler stays honest: if a safe scoped merge is not available, the pass reports `FULL_FALLBACK` instead of pretending the refresh stayed incremental.
- The canonical semantic source of truth does not move. Runtime mutates `Engineering IR`; compiler recomputes downstream artifacts from that canonical state.

## Dependencies

- `:kernel:language`
- `:kernel:validation`
- `:kernel:engineering-model`
- `:kernel:layout-model`
- `:kernel:geometry-model`
- `:kernel:svg-renderer`

Test-only dependency:

- `:extensions:domain-electrical`

## Boundaries

This module does not own the DSL grammar itself, the canonical IR schema, or the concrete Electrical/Runtime domain rules. It orchestrates those pieces while preserving the architecture rule that the DSL is the authored source, `Engineering IR` is the canonical model, `Layout IR` is the first explicit downstream projection layer, `Geometry IR` is the renderer-facing downstream layer, and renderers are downstream backends fed from geometry rather than semantic shortcuts.

## Verification

```bash
./gradlew :kernel:compiler:test
```

Windows PowerShell:

```powershell
java25; .\gradlew.bat :kernel:compiler:test
```
