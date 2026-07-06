# M0 Render Boundary

## Purpose

Story `1.6` turns the declared downstream stage from Story `1.5` into a real render proof:

- the compiler derives a thin render-facing model from canonical `Engineering IR`
- `kernel/svg-renderer/` emits simple deterministic `SVG` from that model
- invalid semantic state blocks rendering rather than pushing semantic recovery into the renderer

This keeps semantic truth upstream while still proving that Athena can produce a visible downstream artifact.

## Boundary Split

Current responsibilities are:

- `kernel/engineering-model/` owns canonical semantic truth
- `kernel/compiler/` owns derivation from `Engineering IR` into a thin render-facing model and integrates that work into the declared pass pipeline
- `kernel/svg-renderer/` owns target-specific `SVG` emission from the already-derived model

The renderer does not inspect AST, repair unresolved semantics, or re-run validation logic.

## Thin Render-Facing Model

The first M0 render-facing model is intentionally small:

- `SvgRenderModel`
- `SvgRenderBox`
- `SvgRenderConnection`

The model contains only what the renderer needs:

- system label
- canvas size
- component boxes and labels
- connection line endpoints

It is downstream view data, not a second canonical semantic representation.

## Deterministic Derivation

The current compiler derivation rules are intentionally simple and stable:

- component order follows canonical `Engineering IR` order
- each component becomes one labeled box
- ports determine left or right connection anchors from validated `direction`
- connections become line segments between derived anchors
- canvas size is derived from the resulting boxes

These rules are enough for the current M0 proof without introducing a durable `Layout IR`.

## Blocking Rules

Rendering currently follows the Story `1.5` continuation policy:

- if semantic validation returns `CONTINUE`, the compiler derives the render model and emits `SVG`
- if semantic validation returns `STOP_DOWNSTREAM`, the compiler returns `CompilerRenderingBlocked`
- if parsing fails, rendering never runs because later passes are skipped

This keeps renderer failures traceable to upstream pipeline state instead of hiding them in output-specific behavior.

## Proof Artifact

The current published proof artifact is:

- `examples/m0/demo-cabinet.svg`

That artifact is checked by compiler tests alongside the existing `Engineering IR` artifact so the M0 proof now has a stable downstream visual consequence.
