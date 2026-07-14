---
title: Athena M16
status: draft
created: 2026-07-14
updated: 2026-07-14
---

# PRD: Athena M16

*Codename: Athena Semantic Macro And Reuse Foundation.*

## 0. Document Purpose

This PRD defines the next milestone after M15.

M15 proved that Athena can let engineers create and modify governed components without writing raw DSL directly. The next unresolved gap is larger-scale engineering reuse.

M16 exists to prove that Athena can reuse parameterized engineering assemblies as governed **Semantic Macros** without letting graphics, copy-paste behavior, or package metadata become the source of truth.

This PRD builds directly on:

- M5 repository and package governance
- M8 unified mutation authority
- M14 component knowledge
- M15 guided semantic authoring
- M13 downstream presentation foundations

Implementation-shaped detail that is useful but too low-level for the main PRD is captured in [`addendum.md`](addendum.md).

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
M12 proved renderer trust and operator-surface hardening.
M13 proved a real presentation language foundation.
M14 proved a governed component-knowledge foundation.
M15 proved guided semantic authoring above that stack.

M16 must now prove the next strategic layer:

- engineers can reuse meaningful engineering assemblies rather than rebuilding them component by component
- reusable assemblies can be parameterized by engineering intent rather than by graphic block editing
- engineers can preview semantic consequences before accepting expansion
- accepted expansions still flow through the completed M8 mutation authority
- every expanded subject preserves reusable origin and parameter traceability

In plain terms:

- M15 proved engineers can author one governed component flow at a time
- M16 proves Athena can reuse whole governed engineering assemblies without losing semantic authority

## 1.1 Why Now

The current gap is no longer primarily:

- repository governance
- semantic SCM
- one mutation authority
- component knowledge
- guided component authoring
- downstream presentation foundations

The current gap is assembly-scale reuse.

Real engineering work is rarely authored only as isolated individual parts. Real projects reuse higher-level engineering structures such as:

- DOL starter
- PLC rack
- 24V distribution unit
- pump unit
- conveyor section

Without solving that gap, Athena risks falling back into the wrong reuse patterns:

- graphic block reuse
- copy-paste engineering
- package metadata treated as engineering truth
- frontend-specific template behavior
- generated structure with weak traceability

M16 is the correct milestone to solve this because:

- M5 already owns governed repository and package graph semantics
- M8 already owns the only accepted mutation path
- M14 already owns governed component and port knowledge
- M15 already proved guided authoring surfaces can stay downstream of semantic authority
- future update, diff, review, AI, and catalog flows need reusable assembly identity before they can scale cleanly

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need to insert common engineering assemblies by setting a few meaningful parameters instead of rebuilding the same structure one component at a time.
- Platform engineers need a reusable assembly contract above M5 package governance and above M8 mutation authority, not a second package manager.
- Domain-extension authors need a governed way to publish reusable assemblies built from component templates, connection templates, and parameter contracts.
- Product teams need a guided reuse flow that stays preview-first, inspectable, and traceable across source, graph, and review surfaces.

### 2.2 Non-Users (M16)

- Teams expecting M16 to become a full marketplace or cross-company package federation milestone
- Teams expecting M16 to become a graphic macro or symbol-block reuse milestone
- Teams expecting M16 to become unrestricted free-form drawing reuse
- Teams expecting M16 to become final automatic schematic generation
- Teams expecting M16 to become a full rule-engine or automation milestone
- Teams expecting M16 to introduce a second generic package system outside M5

## 3. Glossary

- **Semantic Macro** - A governed, parameterized reusable engineering assembly that expands into canonical semantic structure.
- **Macro Template** - The reusable definition behind one Semantic Macro, including parameter schema, component templates, connection templates, and downstream hints.
- **Macro Instantiation** - One configured attempt to use a Semantic Macro with explicit parameter values.
- **Expansion Preview** - The deterministic semantic consequences Athena derives before the engineer accepts the instantiation.
- **Accepted Expansion** - The canonical source-backed semantic structure persisted after preview approval through M8.
- **Origin Traceability** - The mapping that records which Semantic Macro and parameter set produced each expanded semantic subject.
- **Reuse Catalog** - The guided workbench surface that lists available Semantic Macros from the governed repository and package context.

## 4. Strategic Decision

M16 is a **reuse and instantiation milestone**, not a package-system milestone and not a graphic-macro milestone.

Why:

- M5 already owns repository contract, package graph, dependency resolution, `athena.yaml`, and `athena.lock`
- M16 introduces a reuse layer above that governance, not a replacement for it
- the source of reuse value is semantic engineering intent, not graphics and not package metadata

The architectural rule is:

```text
Reuse Catalog / Forms / Templates / AI / DSL / API
        ->
Semantic Macro Instantiation
        ->
Preview
        ->
M8 Mutation Authority
        ->
Engineering IR
        ->
M14 Component Knowledge
        ->
M13 Presentation / Workbench / Docs
```

M16 must therefore avoid:

```text
graphic block
    ->
engineering truth
```

or:

```text
package manager feature
    ->
semantic reuse
```

Those paths would break the architecture.

## 5. Product Position

Athena should learn from traditional ECAD that reusable engineering assemblies matter.

Athena must reject the older truth model where a macro is primarily a graphic block.

The correct Athena model is:

```text
semantic assembly = engineering truth
graphic representation = downstream projection
package metadata = governed distribution and versioning context
```

Consequences:

- one macro selection may expand into many canonical semantic objects
- presentation hints remain downstream of semantic authority
- package metadata can govern availability and versioning, but it does not become the engineering source of truth
- the first proof should be narrow, professional, and traceable rather than broad and marketplace-shaped

## 6. Features

### 6.1 Semantic Macro Contract

**Description:** Athena introduces a governed reusable assembly contract above component knowledge and below instantiation, expansion, and downstream presentation. This contract defines what a Semantic Macro is without collapsing it into package metadata or graphics.

#### FR-1: Define Semantic Macro As A Governed Reusable Assembly Contract

Athena can define a Semantic Macro as a stable reusable engineering assembly contract.

**Consequences (testable):**
- Semantic Macro identity is separate from package identity and separate from any graphic representation.
- A Semantic Macro can contain component templates, connection templates, parameter contracts, and downstream hints.
- A Semantic Macro does not treat SVG, manual layout, or copied graphics as engineering truth.

#### FR-2: Publish Reuse Contracts As Dedicated Platform Models

Athena can publish reusable assembly contracts through dedicated platform models instead of overloading M5 package models.

**Consequences (testable):**
- Architecture evaluates dedicated modules such as `kernel/reuse-model` and `kernel/template-model`.
- The contract surface covers macro identity, parameter schema, template contents, expansion result, and origin traceability.
- M16 does not redefine M5 package contracts in order to express semantic reuse.

### 6.2 Parameterized Instantiation Preview

**Description:** Athena introduces a governed instantiation flow where an engineer chooses one Semantic Macro, sets meaningful parameters, and reviews a deterministic expansion preview before anything mutates canonical state.

#### FR-3: Validate Macro Parameters Before Expansion

Athena can validate Semantic Macro parameters before producing an expansion preview.

**Consequences (testable):**
- The first proof can validate parameters such as `motorPower`, `controlVoltage`, `vendorFamily`, and `tagPrefix`.
- Invalid or incomplete parameter sets are blocked before canonical mutation.
- Defaulted values remain visible and inspectable rather than hidden in frontend state.

#### FR-4: Build A Deterministic Expansion Preview

Athena can produce a deterministic preview of expansion consequences before acceptance.

**Consequences (testable):**
- Preview shows the components, ports, connections, and presentation consequences that would be added or changed.
- Preview does not partially mutate canonical source or runtime-owned engineering state.
- The same Semantic Macro, parameter set, and repository state produce the same preview result.

### 6.3 Accepted Expansion Through M8

**Description:** Athena proves that accepted Semantic Macro expansion still flows through the completed M8 mutation authority instead of creating a new authoring path.

#### FR-5: Route Expansion Acceptance Through The Sole Mutation Authority

Athena can convert an approved Semantic Macro preview into governed mutation through M8.

**Consequences (testable):**
- Acceptance becomes a governed semantic mutation request rather than direct graph, source, or package edits.
- No reuse flow bypasses M8.
- Rejected previews leave no partial canonical state behind.

#### FR-6: Persist Accepted Expansion Into Canonical Engineering State

Athena can persist accepted Semantic Macro expansion into canonical source-backed engineering state.

**Consequences (testable):**
- Accepted expansion updates source, graph, semantic inspection, and review state coherently.
- Derived runtime state refreshes coherently after acceptance.
- Expansion results remain machine-readable semantic structure rather than opaque generated blobs.

### 6.4 Guided Reuse Catalog And Workbench Flow

**Description:** Athena adds a guided reuse surface that lets engineers select assemblies, edit meaningful parameters, and accept previewed semantic consequences without turning the workbench into a free-form graphics editor.

#### FR-7: Expose A Governed Reuse Catalog From Active Repository Context

Athena can expose available Semantic Macros through a governed reuse catalog.

**Consequences (testable):**
- The workbench can list available Semantic Macros from active repository and package context.
- Catalog entries derive from governed packs and repository state rather than hardcoded frontend menus.
- The first proof can expose a narrow electrical slice such as `DOL Starter`, `PLC Rack`, and `24V Distribution Unit`.

#### FR-8: Support Parameter Editing And Review-First Acceptance In The Workbench

Athena can support guided Semantic Macro parameter editing and review-first acceptance in the workbench.

**Consequences (testable):**
- A user can choose a Semantic Macro, edit meaningful parameters, review the expansion preview, and accept or cancel it.
- The guided reuse surface stays downstream of semantic authority rather than becoming a free-form drawing path.
- The first proof does not require the user to understand internal template or mutation mechanics.

### 6.5 Origin And Traceability

**Description:** Athena preserves explicit reusable origin on all expanded subjects so later review, diff, replacement, and AI reasoning can remain grounded.

#### FR-9: Preserve Macro Origin On Expanded Semantic Subjects

Athena can preserve reusable origin identity on expanded semantic subjects.

**Consequences (testable):**
- Every expanded component, connection, and related semantic subject can be tied to the originating Semantic Macro and parameter set.
- Origin traceability survives source, graph, inspector, and semantic SCM surfaces.
- Traceability data remains inspectable and machine-readable.

#### FR-10: Expose Expansion Membership And Origin Inspection

Athena can show which structure belongs to a Semantic Macro expansion and how it was configured.

**Consequences (testable):**
- Engineers can inspect which Semantic Macro created a structure and which parameter values were used.
- Engineers can identify which semantic subjects belong to one accepted expansion.
- M16 does not need to ship full update, replace, or rebind workflows to prove traceability.

### 6.6 Governance Boundary And Narrow Proof Slice

**Description:** Athena proves semantic reuse on a narrow electrical slice while preserving the existing M5 package boundary and refusing to widen into federation, marketplace, or graphics ownership.

#### FR-11: Reuse Existing M5 Governance Instead Of Inventing A New Package System

Athena can resolve Semantic Macros through the existing governed package and repository foundation.

**Consequences (testable):**
- Active Semantic Macros are constrained by the existing repository graph and `athena.lock` flow.
- M16 introduces no second lockfile and no ad hoc generic package resolver.
- Package versioning and dependency governance remain M5 concerns.

#### FR-12: Prove One Narrow Electrical Reuse Slice End To End

Athena can prove semantic reuse on one narrow electrical assembly slice without widening the milestone prematurely.

**Consequences (testable):**
- The first proof focuses on a curated set such as `DOL Starter`, `PLC Rack`, and `24V Distribution Unit`.
- Milestone success depends on one reliable end-to-end reuse flow, not on broad macro breadth.
- Marketplace federation, broad vendor coverage, and unrestricted macro classes remain out of scope.

## 7. Non-Goals (Explicit)

- symbol editor
- SVG editor
- graphic macro editor
- free-form block library
- package marketplace
- multi-company package federation
- final auto-routing
- final automatic schematic generation
- unrestricted rule-engine expansion
- full AI engineering agent
- second mutation authority outside M8

## 8. MVP Scope

### 8.1 In Scope

- Semantic Macro contract and parameter schema
- deterministic parameter validation and preview-first instantiation
- M8-backed acceptance and canonical expansion
- origin traceability for expanded semantic subjects
- guided reuse catalog and parameter-editing flow in the workbench
- one narrow electrical proof slice

### 8.2 Out Of Scope For MVP

- broad catalog federation or marketplace distribution
- graphic block import or arbitrary symbol reuse
- final automatic routing or final schematic generation
- full update, replace, or rebind lifecycle over prior expansions
- broad vendor coverage beyond the first proof slice
- a new generic package-management layer beyond M5

## 9. Success Metrics

**Primary**

- **SM-1:** An engineer can choose `DOL Starter` from the reuse catalog, set meaningful parameters, receive a deterministic expansion preview, and accept it without writing raw DSL manually. Validates FR-3, FR-4, FR-5, FR-8.
- **SM-2:** Accepted expansion updates canonical source, graph, semantic inspection, and review state coherently. Validates FR-5, FR-6.
- **SM-3:** Every expanded semantic subject in the proof scenario preserves inspectable origin traceability to Semantic Macro identity and parameter values. Validates FR-9, FR-10.

**Secondary**

- **SM-4:** Available Semantic Macros are resolved through the existing M5 repository and package governance with no second lockfile or package authority. Validates FR-7, FR-11.
- **SM-5:** The milestone demonstrates at least one serious assembly-scale reuse flow beyond one-by-one component placement. Validates FR-7, FR-12.

**Counter-metrics (do not optimize)**

- **SM-C1:** Do not optimize for graphic fidelity over semantic truth.
- **SM-C2:** Do not optimize for marketplace breadth over one reliable end-to-end reuse proof.
- **SM-C3:** Do not optimize for package-management novelty over reusable engineering meaning.

## 10. Cross-Cutting NFRs

- **NFR-1 Authority Preservation:** M8 remains the only accepted mutation authority.
- **NFR-2 Determinism:** The same Semantic Macro, parameters, and repository state produce the same preview and accepted expansion result.
- **NFR-3 Inspectability:** Parameter values, preview consequences, and origin traceability remain inspectable across product surfaces.
- **NFR-4 Traceability:** Accepted expansion preserves enough origin data for later review, diff, replacement, and AI reasoning work.
- **NFR-5 Workbench Continuity:** The existing product shell remains downstream of runtime-owned semantic authority.

## 11. Open Questions

1. Should the first runtime split land as `kernel/reuse-model` plus `kernel/template-model`, or should the orchestration boundary be named differently while preserving the same ownership?
2. How should expansion preview encode additions, derived identities, and downstream presentation consequences without leaking frontend assumptions into M8?
3. How much naming and tagging policy belongs inside Semantic Macro parameterization versus later repository or domain conventions?
4. How should presentation hints attach to Semantic Macro definitions without becoming graphic truth?
5. What future update or replace model should build on M16 traceability once the first expansion proof exists?

## 12. Final Statement

M15 proved:

> engineers can create governed engineering intent without directly writing raw DSL for every change.

M16 must prove:

> Athena can instantiate reusable engineering assemblies as parameterized Semantic Macros through governed preview-first expansion while keeping canonical engineering meaning stronger than package metadata, graphics, or widget state.

That is the first milestone where Athena proves that engineering reuse can scale above single-component authoring without surrendering semantic authority.
