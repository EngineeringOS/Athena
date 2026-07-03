---
title: Athena PRD Addendum
status: final
created: 2026-07-02
updated: 2026-07-02
---

# Addendum: Technical And Strategic Context

## Source Inputs

- Product brief and addendum from `brief-Athena-2026-07-02`
- Manifesto doctrine, architecture, roadmap, business strategy, and technology chapters
- Draft incubation notes in `draft/0001.md` and `draft/0002.md`

## Why This Lives Outside The PRD

This addendum preserves architectural and mechanism depth that informs implementation and later architecture work but would overload the PRD's main narrative. It should feed `bmad-architecture`, `bmad-ux` where relevant, and later epics/stories without turning requirements into implementation lock-in.

## Architecture Carry-Forward

- The semantic core is `Engineering Language` + `Engineering Ontology` + `Engineering IR`.
- The operational center is the `Engineering Compiler`, not the UI shell.
- `Knowledge Compiler` remains separate from project compilation.
- `Engineering IR`, `Layout IR`, and `Geometry IR` remain separate layers.
- Graph-oriented semantics and stable identity are core invariants.
- Plugins are the growth mechanism at the edges; governance remains at the center.

## Technology Carry-Forward

- Kotlin is the preferred implementation substrate for DSL hosting, compiler logic, ontology, plugin contracts, and shared surfaces.
- Compose Multiplatform and WASM are default delivery directions, not the product's differentiator.
- OCCT, QElectroTech, KiCad, and FreeCAD are integration boundaries.
- AutomationML and OPC UA are standards/runtime boundaries.
- LLVM is the primary architectural inspiration; MLIR and SysML v2 remain reference material rather than fixed dependencies.
- LSP is the expected route into serious editor ecosystems.

## Deferred Depth For Architecture

- Exact compiler pass breakdown and intermediate forms beyond the PRD level
- Storage model and persistence decisions for graph-shaped semantics
- Plugin packaging, compatibility, and runtime loading design
- Knowledge-governance workflow mechanics and review tooling
- UI abstraction and semantic view-model design
- First commercial packaging and rollout sequence above the open core
