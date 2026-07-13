---
title: Athena M13
status: draft
created: 2026-07-12
updated: 2026-07-12
---

# PRD: Athena M13

*Codename: Athena Presentation Language Foundation.*

## 0. Document Purpose

This PRD defines the next milestone after M12.

M12 hardens the first electrical renderer and workbench surface.

M13 exists because renderer hardening alone does not solve the deeper product gap:

> Athena can now carry electrical projection meaning and improve the first operator surface, but it still lacks a true downstream presentation language that can express IEC-like primitives and governed composite elements without collapsing back into generic node-edge rendering.

M13 is therefore not a UI-polish extension of M12.

M13 is a **foundation milestone** that introduces the missing downstream model needed for professional engineering presentation growth:

- an Athena-owned **Presentation IR**
- a governed **primitive presentation vocabulary**
- **composite presentation definitions** built from those primitives
- domain-specific **presentation packs**, with electrical as the first serious pack
- a renderer-backend path that can consume this richer vocabulary without taking semantic ownership

This milestone builds on the completed semantic, projection, mutation, knowledge, AI, and electrical workbench foundations from M0 through M12.

## 1. Vision

M0 proved DSL to `Engineering IR`.
M1 proved runtime-owned workspace and mutation orchestration.
M2 proved explicit projection layers.
M3 proved hosted extensibility.
M4 proved the first serious IDE shell.
M5 proved governed repository meaning.
M6 proved semantic SCM.
M7 proved graphical projection and the first renderer path.
M8 proved one mutation authority across source and graph.
M9 proved executable engineering knowledge.
M10 proved AI-assisted reasoning above governed knowledge outputs.
M11 proved electrical projection families, sheets, notation packs, and repeated references.
M12 proves renderer correctness and operator-surface hardening.

M13 must prove the next strategic layer:

- Athena can express presentation through a real downstream language instead of generic graph nodes and edges
- the renderer can consume governed primitives and composites without becoming semantic authority
- one canonical engineering subject can still appear through multiple view families and presentation occurrences without identity drift
- future domain packs such as electrical, SCADA, documentation, or mechanical presentation can share one neutral presentation boundary

In plain terms:

- M12 proves the first electrical canvas can stop looking toy
- M13 proves Athena has the right **presentation-language foundation** to become a serious engineering tool

## 1.1 Why Now

The current gap is no longer primarily:

- repository governance
- semantic SCM
- graph mutation
- knowledge diagnostics
- AI reasoning
- first electrical projection depth

The current gap is:

- renderer input is still too shallow
- notation contracts are still too broad
- presentation identity is still too generic
- composite presentation structure does not yet exist as a first-class downstream contract

Without addressing that gap, later rendering work risks turning into:

- CSS-heavy approximation
- frontend-specific hardcoding
- accidental renderer-owned meaning
- product lock-in to one temporary graph technology

M13 is the correct milestone to solve this because M11 and M12 have already made the projection and renderer gap visible enough to design it cleanly.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need the system to present subjects through governed domain presentation instead of generic cards and labels.
- Electrical engineers need IEC-like primitives and composite symbols that read like professional operator surfaces.
- Platform engineers need a downstream presentation model that can support multiple domains without polluting `Engineering IR`.
- Renderer engineers need a stable contract that can feed multiple backends such as SVG proof surfaces and future high-performance canvases.
- Product and architecture owners need a path from the current graph-first proof to a professional operator surface without rewriting the semantic center.

### 2.2 Non-Users (M13)

- Teams expecting M13 to become a freeform CAD drawing milestone
- Teams expecting M13 to become standards-compliance knowledge expansion
- Teams expecting M13 to become broad multi-domain parity in one step
- Teams expecting M13 to move engineering truth into primitives, composites, or renderer-local geometry
- Teams expecting M13 to introduce semantic macro authoring as part of the presentation layer

## 3. Strategic Decision

M13 is a **new milestone**, not an M12 patch.

Why:

- M12 improves how the current electrical surface behaves and reads
- M13 changes what the renderer is fundamentally allowed to consume

That is a boundary change in the downstream architecture, not a polish story.

## 4. Features

### 4.1 Presentation IR

**Description:** Athena introduces a dedicated downstream `Presentation IR` between projection output and renderer backends.

#### FR-1: Introduce A Dedicated Presentation IR Boundary

Athena can derive a renderer-neutral presentation model from projection contracts.

**Consequences (testable):**
- `Engineering IR` remains canonical semantic truth.
- `Projection Model` remains the renderer-neutral view contract.
- `Presentation IR` carries presentation archetypes, presentation variants, conductor or connector segments, leader-line contracts, text slots, junctions, and sheet furniture.
- No renderer backend is required to infer professional presentation directly from generic node-edge payloads.

#### FR-2: Keep Presentation IR Downstream Of Semantic And Projection Authority

Athena can add presentation richness without moving truth into the renderer.

**Consequences (testable):**
- Presentation instances resolve through canonical semantic identity and projection occurrence identity.
- `Presentation IR` does not own engineering validation, mutation truth, or source of truth for connection semantics.
- `Presentation IR` remains rebuildable from upstream artifacts.

### 4.2 Primitive Presentation Vocabulary

**Description:** Athena introduces a governed library of small presentation primitives, with electrical IEC-like primitives as the first serious pack.

#### FR-3: Publish A Minimal Primitive Presentation Library

Athena can define a small but extensible set of primitive presentation atoms.

**Consequences (testable):**
- Primitive definitions include stable ids, slots, anchor points, and visual-token references.
- Primitive definitions remain downstream assets, not semantic authorities.
- The first electrical primitive pack is sufficient to represent core subjects such as contact marks, coils, terminals, breaker marks, motor marks, conductor furniture, and related reference markers.

#### FR-4: Keep Primitive Presentation Style-Driven And Pack-Driven

Athena can separate primitive identity from final visual skin and notation style.

**Consequences (testable):**
- Primitive identity does not hardcode one final look.
- Token packs or notation packs can vary border weight, line class, fill, text treatment, and family-specific appearance.
- The same primitive may present differently across schematic, cabinet, documentation, SCADA, or later product styles.

### 4.3 Composite Presentation System

**Description:** Athena introduces composite presentation definitions that assemble multiple primitives into one governed downstream representation.

#### FR-5: Support Composite Presentation Definitions Above Primitive Atoms

Athena can describe one presentation occurrence as a composite made of multiple primitives.

**Consequences (testable):**
- Composite definitions can map canonical ports or terminals to explicit presentation anchors.
- Composite definitions can expose text slots, terminal slots, part ids, and internal layout rules.
- Composite definitions do not become canonical engineering models.

#### FR-6: Allow One Canonical Subject To Project Into Multiple Composite Variants

Athena can bind one canonical engineering subject to different composite presentation variants by view family or presentation pack.

**Consequences (testable):**
- A subject may appear differently in schematic, cabinet, documentation, or later domains without identity drift.
- Composite choice remains governed by downstream contracts, not renderer-local heuristics.

### 4.4 Composition Boundary

**Description:** Athena explicitly separates presentation composition from future semantic composition.

#### FR-7: Keep Presentation Composition Separate From Semantic Macro Composition

Athena can model composite presentation without pretending that engineering macros are renderer assets.

**Consequences (testable):**
- M13 does not define engineering macros as part of `Presentation IR`.
- If later needed, semantic macros or engineering assemblies will belong above projection and then project down into `Presentation IR`.
- Presentation composition stays inspectable and downstream.

#### FR-8: Preserve Semantic Traceability Across Composite Occurrences

Athena can preserve reveal, diagnostics, review, mutation, and AI-context coherence across primitive and composite occurrences.

**Consequences (testable):**
- Multiple presentation occurrences still resolve through canonical semantic identity first.
- Composite parts do not fork semantic review, diagnostics, or mutation authority.
- Presentation-level selection can still reveal canonical upstream subjects deterministically.

### 4.5 Renderer Backend Abstraction

**Description:** Athena introduces a stable renderer-backend seam above the new `Presentation IR`.

#### FR-9: Support Multiple Renderer Backends Over One Presentation IR

Athena can feed more than one renderer backend from the same presentation contract.

**Consequences (testable):**
- SVG remains available as a proof and debug backend.
- A future high-performance backend can consume the same presentation model without rewriting semantic or projection logic.
- Backend choice does not redefine primitives, composites, or sheet contracts.

### 4.6 Extension Ecosystem

**Description:** Athena turns presentation libraries into extension-compatible downstream assets.

#### FR-10: Host Presentation Packs Through Extension Contracts

Athena can load or register primitive and composite presentation packs through extension-friendly contracts.

**Consequences (testable):**
- Primitive and composite presentation packs are not hardcoded into one frontend bundle only.
- Different vendors, industries, or house styles can contribute additional packs later.
- The first M13 proof can ship with a narrow built-in electrical presentation pack while preserving the future ecosystem path.

## 5. Non-Goals

M13 is not:

- a full EPLAN clone
- unrestricted freehand CAD
- a broad standards or rule-engine milestone
- a broad AI milestone
- a final dense-scene performance milestone for every renderer backend
- a commitment to one permanent visual skin
- a reason to move canonical engineering semantics into presentation definitions
- a semantic macro or engineering assembly authoring milestone

## 6. Constraints

- Canonical engineering meaning remains in `Engineering IR`.
- Projection remains the renderer-neutral view contract.
- Presentation primitives and composites remain downstream and inspectable.
- Mutation, review, knowledge, and AI surfaces must continue to resolve through canonical semantic identity.
- The new system must be compatible with future Theia-hosted and web-capable renderers.
- The first serious presentation pack is electrical, but the top-level contract name remains domain-neutral.

## 7. Success Metrics

### SM-1

Athena publishes at least one first `Presentation IR` contract that is visibly richer than the current node-edge payload.

### SM-2

Athena ships a first primitive electrical presentation pack sufficient to represent the core M11 and M12 proof subjects without generic-card fallback as the primary electrical posture.

### SM-3

Athena can define at least one composite presentation and trace it back to canonical semantic identity.

### SM-4

Athena preserves cross-reference, selection, reveal, diagnostics, mutation, and review coherence across primitive and composite occurrences.

### SM-5

Athena can route the same `Presentation IR` into at least one proof backend while keeping the backend replaceable.

## 8. Risks

- designing presentation vocabulary too narrowly around one current screenshot
- designing presentation vocabulary too abstractly to drive a professional renderer
- confusing semantic macro or engineering assembly with presentation composition
- tying the architecture to one temporary frontend library
- splitting responsibilities unclearly between projection, `Presentation IR`, and renderer backend

## 9. Milestone Position

Recommended sequence:

- **M12** - renderer correctness and operator hardening over the current stack
- **M13** - presentation language foundation, primitive packs, composite packs, and backend abstraction
- **later milestone** - semantic macro or engineering assembly model if the product truly needs reusable engineering composition above projection
- **later milestone** - renderer backend evolution, denser parity work, workflow depth, and broader operator capability

## 10. Final Statement

M13 proves:

> presentation itself can become a governed downstream language.

That is the missing foundation between "Athena can render an electrical graph" and "Athena can become a serious engineering operator surface."
