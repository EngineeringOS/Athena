---
title: Athena M1 PRD Addendum
status: final
created: 2026-07-03
updated: 2026-07-04
---

# Addendum: M1 Transition Depth

## Source Inputs

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/addendum.md`
- `draft/m1/0001.md`

## Why This Lives Outside The PRD

The main PRD should define what M1 must prove as a product and platform milestone. This addendum preserves the deeper architecture and migration thinking from the M1 draft so it can inform architecture, epics, and implementation planning without hard-freezing low-level structure inside product requirements.

## M1 Thesis Shift

- M0 proved a compiler thesis.
- M1 should prove a runtime thesis.
- The key shift is not "make the compiler bigger."
- The key shift is "move the operational center of gravity above the compiler."

In shorthand:

```text
M0
frontend -> compiler -> outputs

M1
frontend -> runtime -> compiler/render/graph/command services -> outputs
```

## Architectural Recommendation

The highest-value architectural move in M1 is to introduce `Athena Runtime` as a real layer before broad GUI or application work expands. The runtime should initially own:

- `Workspace`
- `Project`
- `Execution Context`
- `Service Registry`
- plugin hosting coordination
- runtime-level orchestration of compiler and renderer services

This allows the compiler to become one runtime capability rather than remaining the owner of every later platform concern.

## Evolution Rather Than Rewrite

The M1 draft strongly argues for evolutionary extraction rather than a wholesale rewrite. That guidance is worth preserving:

- keep the proven M0 modules
- add the new runtime layer above them first
- extract responsibilities outward from `:compiler` only when the new owner is clear
- avoid rename-heavy or directory-driven churn before runtime boundaries are demonstrated

## Suggested Milestone Progression

### Stage 1

Add a new runtime layer while keeping the current M0 module layout substantially intact.

Initial runtime concerns:

- `Workspace`
- `Project`
- lifecycle
- `Execution Context`

### Stage 2

Reduce `:compiler` back toward compiler concerns by extracting runtime-owned hosting concerns such as:

- plugin hosting
- knowledge hosting
- workspace ownership
- outer execution orchestration

### Stage 3

Add the first real `Engineering Graph` layer with:

- nodes
- edges
- references
- traversal
- identity
- lookup

### Stage 4

Add the `Command Runtime` as the universal mutation path with:

- command execution
- history
- undo
- redo
- replay
- transaction-friendly semantics

### Stage 5

Add a domain-neutral viewer/editor runtime layer for:

- viewport
- selection
- hit testing
- camera
- coordinate handling
- layers

### Stage 6

Expand renderer backends and keep them as consumers of canonical semantics rather than hidden semantic owners.

## Demo-Centered Milestone Framing

The M1 draft proposed four load-bearing demonstrations. After review, the recommended sequencing is:

Required foundation proofs:

1. `DSL -> IR -> Compose Viewer`
2. `GUI -> one command-backed semantic mutation -> IR -> SVG`
3. `IR -> Diff/History -> Undo`

Optional extension proof on the same runtime path:

4. `AI proposal -> accepted command -> IR -> Validation -> SVG`

These are useful because they test the runtime claim directly rather than only testing internal module extraction. The key refinement is that AI should not broaden M1 into a second proving wedge before the runtime-centered foundation path is solid.

## Suggested Ownership Boundaries

| Module area | Owns |
| --- | --- |
| `language` | syntax |
| `compiler` | authored source to canonical semantic compilation |
| `ir` | canonical semantic model |
| `runtime` | lifecycle and execution coordination |
| `graph` | semantic relationships and traversal |
| `command` | mutation, history, undo/redo |
| `renderer` | visualization backends |
| `domains` | domain-specific semantics |
| `apps` | user experience surfaces |

This ownership split is strategically useful because it prevents future surfaces from taking over mutation or semantic authority.

## Repository Evolution Direction

The draft suggests a future repository shape where compiler, runtime, graph, command, editor-runtime, renderer backends, domains, and apps become clearer peers. The important part is the boundary logic, not the exact folder names. Architecture work should preserve that distinction:

- authored source handling is not runtime lifecycle ownership
- runtime lifecycle ownership is not renderer ownership
- renderer ownership is not domain ownership
- application UX is not semantic authority

## Handoff Notes For Architecture

The next architecture pass should resolve:

- the first concrete `Athena Runtime` API surface
- how `Engineering Graph` projects from canonical semantic state
- the minimum command contract shape
- how incremental recomputation boundaries are derived
- what Compose viewer/runtime boundary looks like without leaking domain semantics
- the extraction order that safely shrinks `:compiler`
- the minimum runtime-hosted plugin slice for M1, with at least domain semantics, commands, and views proven first

## Compose Seed And Build Management Reference

The current preferred local reference for Compose Multiplatform initialization is:

- `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop`

The M1 implementation direction should preserve two decisions:

- shared plugin and library versions are managed through `gradle/libs.versions.toml`
- the first Compose seed should keep a clean split between platform app entrypoint and shared Compose runtime/view code

That reference should guide the first M1 viewer/runtime initialization work rather than letting Athena introduce a second dependency-management style or a tangled app/runtime module shape.
