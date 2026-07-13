---
title: 'Athena M13'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'canonical engineering truth with downstream presentation language built from primitives and composites'
scope: 'Athena M13 presentation language foundation'
status: draft
created: '2026-07-12'
updated: '2026-07-12'
binds:
  - 'FR-1'
  - 'FR-2'
  - 'FR-3'
  - 'FR-4'
  - 'FR-5'
  - 'FR-6'
  - 'FR-7'
  - 'FR-8'
  - 'FR-9'
  - 'FR-10'
sources:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m13/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m13/addendum.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/RENDERER-FOUNDATION.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-11-m11/ARCHITECTURE-SPINE.md'
  - 'draft/open/2026-07-09-Eplan-cross-compare-discuss.md'
  - 'draft/screenshort/README.md'
  - 'docs/roadmap/athena-milestone-roadmap.md'
companions:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-12-m13/prd.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-12-m12/RENDERER-FOUNDATION.md'
---

# Architecture Spine - Athena M13

## Design Paradigm

Athena M13 is a **canonical engineering truth with downstream presentation language built from primitives and composites** architecture.

- **canonical engineering truth** means `Engineering IR` still owns engineering meaning and identity.
- **downstream presentation language** means professional presentation is no longer reduced to generic graph nodes and edges.
- **primitives and composites** means Athena gains a layered downstream vocabulary that can grow toward serious operator presentation without letting renderer code become the truth model.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-27 | `architecture-Athena-2026-07-09-m7` | `kernel/projection-model` remains the renderer-neutral projection boundary. |
| AD-28 | `architecture-Athena-2026-07-09-m7` | Engineering identity remains in the object graph; renderer assets stay downstream. |
| AD-29 | `architecture-Athena-2026-07-09-m7` | Layout and geometry remain view-scoped metadata, not engineering truth. |
| AD-30 | `architecture-Athena-2026-07-09-m7` | Graphical workbench surfaces continue to consume runtime-owned state through Athena-owned transport. |
| AD-34 | `architecture-Athena-2026-07-10-m8` | One mutation authority above source and graph remains binding. |
| AD-38 | `architecture-Athena-2026-07-10-m8` | Unified semantic review facts remain shared across interaction origins. |
| AD-39 | `architecture-Athena-2026-07-10-m8` | Cross-surface anchoring continues to use canonical semantic identity. |
| AD-53 | `architecture-Athena-2026-07-11-m11` | Electrical workbench depth starts from canonical engineering entities, not symbols. |
| AD-54 | `architecture-Athena-2026-07-11-m11` | Explicit electrical projection families remain above one canonical subject. |
| AD-55 | `architecture-Athena-2026-07-11-m11` | Sheet identity remains projection-owned and separate from engineering identity. |
| AD-56 | `architecture-Athena-2026-07-11-m11` | Symbol and notation packs remain governed downstream contracts. |
| AD-57 | `architecture-Athena-2026-07-11-m11` | Repeated references and cross references anchor to canonical semantic identity. |
| AD-60 | `architecture-Athena-2026-07-11-m11` | Electrical workbench depth must preserve mutation, review, and knowledge coherence. |
| AD-62 | `architecture-Athena-2026-07-12-m12` | Electrical readability remains a downstream consequence of canonical state. |
| AD-63 | `architecture-Athena-2026-07-12-m12` | Routing ownership remains split across semantic, projection, and renderer layers. |
| AD-66 | `architecture-Athena-2026-07-12-m12` | Electrical navigation stays canonical-identity-first. |

## Invariants & Rules

### AD-67 - M13 Introduces Presentation IR As A New Dedicated Downstream Layer

- **Binds:** `FR-1`, `FR-2`, `FR-9`
- **Prevents:** render richness from being reconstructed ad hoc inside frontend widgets or backend-specific scene models
- **Rule:** M13 introduces `Presentation IR` as an Athena-owned downstream layer between projection output and renderer backends. This layer owns presentation vocabulary only. It is rebuildable from upstream projection artifacts and may not become a second semantic core.

### AD-68 - Primitive Presentation Atoms Are Downstream Assets, Not Engineering Entities

- **Binds:** `FR-3`, `FR-4`
- **Prevents:** minimal presentation definitions from becoming hidden engineering truth
- **Rule:** Primitive presentation definitions represent reusable downstream atoms such as contact marks, terminal marks, conductor segments, junctions, text slots, and related glyphs. Primitive definitions may expose anchors, slots, orientation rules, and token hooks, but they do not define engineering semantics.

### AD-69 - Composite Presentation Assembles Primitive Atoms Without Redefining Canonical Subjects

- **Binds:** `FR-5`, `FR-6`
- **Prevents:** richer presentation elements from becoming parallel semantic entities
- **Rule:** Composite presentation definitions may combine primitive atoms into richer downstream subject presentations. Composite parts, ports, text slots, and local layout contracts remain downstream assemblies bound to one canonical subject or governed occurrence identity.

### AD-70 - Semantic Macro Or Engineering Assembly Is Not Part Of Presentation IR

- **Binds:** `FR-7`, `FR-8`
- **Prevents:** reusable engineering composition from being silently collapsed into renderer vocabulary
- **Rule:** M13 does not place semantic macro or engineering assembly inside `Presentation IR`. If Athena later requires reusable engineering composition, that layer must live above projection and then project down into `Presentation IR`.

### AD-71 - Presentation Vocabulary Is Layered Above Notation But Below Renderer Backends

- **Binds:** `FR-1`, `FR-4`, `FR-9`
- **Prevents:** notation contracts from being too shallow to drive serious presentation, or renderer backends from inventing their own vocabulary
- **Rule:** View family, sheet, notation pack, electrical anchors, endpoint mappings, and routing corridors remain projection-level contracts. `Presentation IR` refines those contracts into primitives, composites, conductor segments, label leaders, and sheet furniture consumable by one or more renderer backends.

### AD-72 - One Canonical Subject May Have Multiple Presentation Occurrences Across Primitive And Composite Forms

- **Binds:** `FR-2`, `FR-6`, `FR-8`
- **Prevents:** occurrence multiplication from causing semantic drift
- **Rule:** The same canonical engineering subject may appear through multiple primitive, composite, or repeated-reference presentation occurrences. All reveal, selection, diagnostics, review, mutation, and AI-context flows must resolve through canonical semantic identity first and presentation occurrence identity second.

### AD-73 - Renderer Backends Stay Replaceable

- **Binds:** `FR-9`
- **Prevents:** lock-in to one temporary SVG- or frontend-specific renderer implementation
- **Rule:** M13 must preserve at least one proof backend and one future-main-backend path above the same `Presentation IR`. Backend-specific scene trees, draw commands, batching, or GPU concerns remain backend-owned and may not leak back into semantic or projection layers.

### AD-74 - Presentation Packs Are Extension-Compatible Assets

- **Binds:** `FR-10`
- **Prevents:** downstream presentation vocabulary from being permanently trapped inside one frontend bundle or one milestone proof repository
- **Rule:** M13 primitive and composite presentation packs must be modeled as hosted downstream assets or contracts that can later be packaged, versioned, and extended through Athena's plugin or extension model.

## Layer Responsibilities

### Engineering IR

Owns:

- canonical components, ports, connections, properties
- engineering identity
- semantic validation

Does not own:

- presentation primitives
- composites
- renderer furniture

### Projection Model

Owns:

- view families
- placements
- sheets
- notation-pack selection
- repeated references
- anchors
- routing-corridor guidance

Does not own:

- primitive draw instructions
- composite layout internals
- backend-specific scene trees

### Presentation IR

Owns:

- presentation archetype binding
- primitive and composite occurrence structure
- conductor segment model
- label leaders and text slots
- junction and reference markers
- sheet frame and furniture contracts
- render-token references

Does not own:

- semantic truth
- mutation truth
- knowledge truth
- semantic macro or engineering assembly truth

### Renderer Backend

Owns:

- final draw tree
- viewport behavior
- batching
- exact path generation
- SVG, canvas, or GPU details

Does not own:

- canonical engineering meaning
- projection identity
- presentation taxonomy

## Recommended Proof Shape

The first M13 proof should stay narrow:

- one `Presentation IR` contract
- one primitive electrical presentation pack
- one composite electrical presentation pack
- one end-to-end proof repository
- one proof backend
- full traceability from rendered occurrence back to canonical semantic identity

This is enough to prove the architecture without exploding into final parity work.

## Final Statement

M13 should prove:

> presentation itself can become a governed downstream language without breaking Athena's semantic-first architecture.

That is the foundation required before deeper parity work, denser workflow breadth, or richer renderer backends can scale safely.
