---
title: Athena M14
status: draft
created: 2026-07-13
updated: 2026-07-13
---

# PRD: Athena M14

*Codename: Athena Component Knowledge Foundation.*

## 0. Document Purpose

This PRD defines the next milestone after M13.

M13 proved that Athena can carry engineering meaning into a governed downstream presentation language without letting renderer or frontend systems become semantic authorities.

But that success exposes the next real product and kernel gap:

> Athena can already store engineering structure, execute a narrow slice of engineering knowledge, and render downstream views, but it still cannot resolve what an authored engineering component actually is in a rich, governed, vendor-neutral way.

M14 therefore exists to introduce a deterministic **component knowledge foundation** above canonical `Engineering IR` and below later knowledge-runtime, projection, presentation, validation, and AI flows.

M14 is also the milestone that freezes one permanent product rule:

> DSL is Athena's canonical serialization.  
> DSL is not Athena's default human interface.

## 1. Vision

M0 proved DSL to `Engineering IR`.
M1 proved runtime-owned workspace and mutation orchestration.
M2 proved explicit projection layers.
M3 proved hosted extensibility.
M4 proved the first serious IDE shell.
M5 proved governed repository meaning and package graph resolution.
M6 proved semantic SCM.
M7 proved graphical projection and the first renderer path.
M8 proved one mutation authority across source and graph.
M9 proved executable engineering knowledge.
M10 proved AI-assisted reasoning above governed knowledge outputs.
M11 proved serious electrical multi-view workbench depth.
M12 proved renderer correctness and operator-surface hardening.
M13 proved a real presentation language foundation.

M14 must prove the next strategic layer:

- Athena can resolve authored component references into governed engineering concepts
- semantic ports stop being label strings and become first-class typed knowledge
- minimal physical traits become reusable downstream knowledge instead of ad hoc renderer or layout assumptions
- vendor parts become implementations of engineering concepts, not semantic truth
- downstream M9, M13, and later UX surfaces can consume resolved component knowledge without breaking kernel authority

In plain terms:

- M9 proved Athena can execute some engineering knowledge
- M14 proves Athena can understand what a component is before later knowledge or presentation consumes it

## 1.1 Why Now

The current gap is no longer primarily:

- repository governance
- semantic SCM
- graph mutation
- first knowledge-runtime proof
- AI reasoning
- renderer hardening
- presentation-language foundation

The current gap is:

- component identity is still too shallow
- ports are still too close to labels rather than typed engineering connection concepts
- physical meaning is not yet first-class reusable kernel knowledge
- vendor implementation mapping is not yet separated cleanly from engineering concept identity
- future authoring surfaces still lack a rich semantic substrate for palettes, forms, and AI-guided creation

Without solving that gap, later product work risks turning into:

- renderer-specific heuristics
- vendor-specific hardcoding
- AI-assisted guesswork over thin semantics
- high user learning cost because the only precise surface would be direct DSL authoring

M14 is the correct milestone to solve this because:

- M5 already gives Athena deterministic package governance
- M8 already gives Athena one mutation authority
- M9 already gives Athena a narrow knowledge-runtime above canonical state
- M13 already gives Athena a downstream presentation language

M14 becomes the missing substrate between them.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need Athena to understand components as engineering concepts, not just as authored objects with properties.
- Platform engineers need a vendor-neutral semantic layer that later validation, projection, presentation, and AI can all reuse.
- Domain-extension authors need a governed way to publish component definitions, semantic ports, and vendor implementation mappings.
- Product owners need future graphical, form-based, template-based, and AI-assisted authoring to avoid forcing mainstream users into direct DSL authoring.

### 2.2 Non-Users (M14)

- Teams expecting M14 to become the mainstream graphical authoring milestone
- Teams expecting M14 to become broad catalog parity with EPLAN
- Teams expecting M14 to become a behavior or simulation milestone
- Teams expecting M14 to become a broad standards-platform or marketplace milestone
- Teams expecting M14 to introduce a second mutation authority outside M8

## 3. Strategic Decision

M14 is a **new milestone**, not an M9 patch and not an M13 extension.

Why:

- M9 introduced executable knowledge-runtime layers above canonical state
- M13 introduced presentation-language layers below projection
- M14 introduces **component knowledge resolution** between canonical authored structure and those later consumers

This is a new kernel boundary, not a small feature addition.

## 4. Product Position

Athena must keep semantic truth machine-readable and inspectable.

But Athena must not require mainstream engineers to author raw DSL directly as the default product experience.

The correct long-term model is:

```text
Graph / Forms / Templates / AI / DSL / API
        ->
Semantic Command And Mutation Layer
        ->
Engineering IR
        ->
Component Knowledge Resolution
        ->
Downstream Knowledge / Projection / Presentation / Renderer
```

Consequences:

- DSL remains canonical serialization
- direct DSL authoring remains available for power users, automation, vendors, and AI agents
- future graphical and guided authoring surfaces remain valid because they still converge through one mutation authority
- M14 does not itself implement the mainstream graphical authoring experience, but it must enable it

## 5. Features

### 5.1 Component Knowledge Resolution

**Description:** Athena introduces a new deterministic layer that resolves authored component references into governed engineering concepts and their downstream reusable semantic shape.

#### FR-1: Resolve Authored Component References Into Governed Engineering Concepts

Athena can resolve authored component references into stable engineering concept identities.

**Consequences (testable):**
- One authored component reference resolves to one vendor-neutral engineering concept.
- Engineering concept identity remains separate from vendor implementation identity.
- Resolution outputs preserve canonical authored semantic subject identity.

#### FR-2: Publish Component Knowledge As New Kernel Contracts

Athena can define component knowledge through dedicated kernel models rather than compiler-local helper data.

**Consequences (testable):**
- `kernel/component-model` exists as the home for engineering concept contracts.
- `kernel/connection-model` exists as the home for semantic port contracts.
- `kernel/physical-model` exists as the home for minimal physical-trait contracts.
- `kernel/part-model` exists as the home for vendor implementation mappings.

### 5.2 Semantic Port Knowledge

**Description:** Athena upgrades ports from labels and ad hoc properties into first-class semantic connection concepts.

#### FR-3: Define Typed Semantic Port Contracts

Athena can define semantic ports with stable role and signal meaning.

**Consequences (testable):**
- Ports may define stable role, direction, signal family, and optional protocol-bearing metadata.
- Semantic ports remain kernel contracts and do not depend on renderer geometry or frontend state.
- The first M14 proof can publish ports such as `L+`, `M`, `PE`, and `MPI` as typed component knowledge.

#### FR-4: Keep Rich Compatibility Rules Outside The Port Contract

Athena can define semantic connection concepts without collapsing M14 into a full rule engine.

**Consequences (testable):**
- M14 semantic port contracts define type and role, not full downstream compatibility evaluation.
- Richer compatibility or sufficiency evaluation remains downstream M9 knowledge logic.

### 5.3 Minimal Physical Traits

**Description:** Athena introduces minimal physical component traits needed by later layout, projection, and validation consumers.

#### FR-5: Publish Minimal Physical-Trait Contracts

Athena can define minimal physical traits as reusable component knowledge.

**Consequences (testable):**
- Physical traits may include width, height, depth, mounting type, and basic installation environment markers.
- Physical traits remain vendor-neutral when appropriate and may be specialized per vendor implementation when needed.
- Physical traits do not replace layout, geometry, or renderer-scene ownership.

### 5.4 Vendor Part Mapping

**Description:** Athena separates engineering concept identity from vendor implementation identity.

#### FR-6: Model Vendor Parts As Implementations Of Engineering Concepts

Athena can map one engineering concept to one or more vendor implementations.

**Consequences (testable):**
- Vendor part ids are not treated as the semantic type system.
- One concept may resolve to multiple valid implementations.
- The first M14 proof can publish at least one Siemens implementation mapping for each targeted proof component family.

### 5.5 Deterministic Pack Loading

**Description:** Athena resolves component knowledge through governed extension packs constrained by the repository package graph.

#### FR-7: Load Knowledge Packs Deterministically Through Existing Package Governance

Athena can resolve active component-knowledge packs through the existing M5 package graph and hosted plugin seams.

**Consequences (testable):**
- Active component-knowledge packs are constrained by the repository's resolved package graph.
- Package versions remain governed through the existing `athena.lock` flow.
- M14 does not introduce ad hoc runtime network fetches or a second canonical lockfile.

#### FR-8: Surface Conflicting Definitions Explicitly

Athena can fail clearly when multiple packs try to define the same thing incompatibly.

**Consequences (testable):**
- Conflicting engineering concept ids or vendor part ids surface as compiler-owned diagnostics.
- Load order or filesystem order does not silently decide semantic truth.

### 5.6 Downstream Integration

**Description:** Athena makes resolved component knowledge available to existing and future downstream layers without changing write authority.

#### FR-9: Feed Resolved Component Knowledge Into M9 And M13 Downstream Consumers

Athena can expose resolved component knowledge to later knowledge-runtime and presentation flows.

**Consequences (testable):**
- M9 can consume resolved component knowledge as later input to derived-context or capability-fact logic.
- Projection and presentation flows can consume resolved component identity and minimal physical traits.
- M14 does not replace `Engineering IR`, M9, or `Presentation IR`.

#### FR-10: Preserve One Mutation Authority And Multi-Surface Product Direction

Athena can introduce component knowledge without changing the existing mutation and authoring authority model.

**Consequences (testable):**
- M8 remains the only authoritative write path.
- M14 introduces no second mutation subsystem.
- M14 explicitly keeps future graph, form, template, API, AI, and DSL authoring aligned through the same semantic write authority.

## 6. Non-Goals

M14 is not:

- a mainstream graphical authoring milestone
- a complete EPLAN replacement milestone
- a behavior-model or simulation milestone
- a digital-twin execution milestone
- a broad standards or compliance platform
- a full vendor-catalog ingestion platform
- a procurement or part-selection optimization milestone
- a rule-authoring or marketplace milestone
- a reason to force direct DSL authoring on mainstream users

## 7. Constraints

- Canonical authored engineering meaning remains in `Engineering IR`.
- The only write authority remains the M8 semantic command and mutation path.
- M14 output is read-only resolved component knowledge.
- Existing repository and package governance remains authoritative for pack reproducibility.
- Existing runtime and `ide/lsp` seams remain the delivery path.
- The first M14 proof stays narrow: electrical only, Siemens-first.

## 8. MVP Scope

### 8.1 Domain

Electrical only.

### 8.2 Proof Component Slice

P0:

- PLC CPU
- Digital I/O
- Analog I/O

P1:

- Contactor
- Relay
- Motor

P2:

- 24V power supply
- Breaker
- Cable

Deferred:

- HMI
- VFD / drive
- sensor families
- broad catalog parity

### 8.3 Vendor Coverage

First proof vendor:

- Siemens only

## 9. End-To-End Proof Scenario

Input:

```eos
system Machine001 {

    device CPU {
        component Siemens.S7300.CPU313C
    }

}
```

Athena derives:

### Semantic Resolution

```text
engineering concept: electrical.plc.cpu
```

### Semantic Ports

```text
L+
M
PE
MPI
```

### Physical Traits

```text
DIN rail mounting
governed width / height / depth
```

### Vendor Mapping

```text
Siemens S7-300 CPU313C
```

### Downstream Consumption

```text
projection may consume component family and physical traits
presentation may consume component family identity
later knowledge runtime may consume semantic port and component concept identity
```

## 10. Deliverables

### Kernel

- `kernel/component-model`
- `kernel/connection-model`
- `kernel/physical-model`
- `kernel/part-model`

### Compiler

- deterministic knowledge-pack resolution phase
- authored component reference resolution phase
- explicit conflict diagnostics for pack collisions

### Runtime

- resolved component knowledge access service
- session-scoped resolved-knowledge cache as derivative state only

### Extensions

- first electrical component-definition pack
- first Siemens vendor-implementation pack

### Documentation

- Component Knowledge Specification
- Component Definition Format
- Vendor Implementation Mapping Format
- Extension Authoring Guide

## 11. Success Metrics

### SM-1

Athena can resolve at least one authored Siemens component reference into one vendor-neutral engineering concept plus one vendor implementation record.

### SM-2

Athena can publish typed semantic ports for the first proof component slice without relying on renderer or frontend heuristics.

### SM-3

Athena can publish minimal physical traits for the first proof component slice without turning M14 into a geometry or layout milestone.

### SM-4

Athena can expose resolved component knowledge to later knowledge-runtime and presentation consumers without creating a second write authority.

### SM-5

Athena can report unresolved or conflicting component definitions deterministically and inspectably.

## 12. Final Statement

M9 proved:

> Athena can execute a governed slice of engineering knowledge.

M13 proved:

> Athena can express downstream presentation as a governed language.

M14 must prove:

> Athena can resolve what an engineering component actually is.

This milestone is the transition from:

```text
semantic engineering platform with narrow knowledge proof
```

to:

```text
component-aware engineering operating substrate
```

And product-wise it freezes one permanent rule:

> Engineers should not be required to learn the DSL.  
> DSL is Athena's canonical serialization.  
> Graph, forms, templates, AI, API, and DSL must all converge through the same semantic mutation authority.
