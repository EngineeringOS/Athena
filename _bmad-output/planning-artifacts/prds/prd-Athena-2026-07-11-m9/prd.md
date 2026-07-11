---
title: Athena M9
status: draft
created: 2026-07-11
updated: 2026-07-11
---

# PRD: Athena M9

*Codename: Athena Engineering Knowledge Runtime.*

## 0. Document Purpose

This PRD defines the M9 product requirements for Athena after the completed M8 milestone.

M9 exists to close the next platform gap intentionally left open by M8:

> Athena can now preserve one mutation authority across source and graph, but it still proves mostly structural semantic meaning. M9 must now prove that Athena can derive engineering context, evaluate capability and constraint, and publish impact directly from canonical semantic state.

This PRD is knowledge-first and kernel-first. It builds on the completed M8 PRD and M8 architecture boundary, the current roadmap under `docs/roadmap/`, the workspace summary, and the M9 drafts under `draft/m9/`. Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved authored DSL to canonical `Engineering IR`. M1 proved runtime-owned workspace state, commands, history, and semantic diff. M2 proved explicit downstream `Layout IR` and `Geometry IR`. M3 proved stable plugin-hosted extensibility. M4 proved the first serious Athena IDE shell. M5 proved governed repository and package meaning. M6 proved semantic SCM, semantic review, semantic commit intent, and package-aware semantic history. M7 proved graphical projection and the graph-first workbench. M8 proved one mutation authority across source and graph.

M9 must now prove the next layer that makes Athena more than a structurally correct engineering compiler:

- derived engineering context above canonical semantic structure
- engineering capability facts above that derived context
- narrow governed constraint evaluation over those facts
- impact understanding when engineering meaning changes
- semantic diagnostics and review consequences that express engineering insufficiency, not only structural invalidity

In other words, M8 proved how engineers can change truth without splitting mutation authority. M9 must prove whether Athena can understand a first slice of engineering truth deeply enough to evaluate sufficiency and impact.

## 1.1 Why Now

The next technical risk is no longer whether Athena can host authoring surfaces or mutation flows.

Today the workspace already has the needed upstream proof:

- M0 already proved canonical engineering meaning can be compiled and validated
- M1 already proved runtime-owned command execution and semantic consequence handling
- M6 already proved semantic review, commit, and history vocabulary above repository and engineering meaning
- M8 already proved source and graph can converge into one mutation-result model

That is exactly why M9 can now become the engineering-knowledge milestone:

- canonical engineering state is already upstream
- mutation authority is already upstream
- review and diagnostics paths already exist
- the next missing proof is engineering sufficiency, not another surface or transport layer

Starting M9 earlier would have risked mixing knowledge evaluation with unresolved projection, review, or mutation boundaries.

## 2. Target User

### 2.1 Jobs To Be Done

- Engineers need Athena to tell whether an authored design is only structurally valid or also engineering-sufficient within a first governed knowledge-pack rule slice.
- Reviewers need semantic review output to include engineering impact when a design change alters capability or constraint satisfaction.
- Platform engineers need one kernel-owned way to derive capability facts, evaluate constraints, and publish consequences without moving engineering knowledge into renderer logic or vendor adapters.
- Product and architecture owners need proof that Athena can become an engineering intelligence layer rather than only a semantic structure compiler.

### 2.2 Non-Users (M9)

- Teams expecting M9 to become a QElectroTech- or EPLAN-class editor-depth milestone
- Teams expecting M9 to widen into multi-domain knowledge packs from day one
- Teams expecting M9 to deliver AI copilot, auto-design, or automatic part selection
- Teams expecting M9 to become a full standards-compliance platform or vendor-catalog platform
- Teams expecting M9 to reopen the M7 renderer or M8 mutation foundations

### 2.3 Key User Journeys

- **UJ-1. Aaron checks whether a changed design is still engineering-sufficient.**
  - **Persona + context:** Aaron is validating whether Athena understands more than authored structure after a legitimate engineering change.
  - **Entry state:** An active Athena repository session exists and the changed source already compiles into canonical semantic state.
  - **Path:** Aaron changes an electrical value such as motor power or protection size. Athena derives engineering context, promotes capability facts, evaluates the first fixed knowledge-pack rule slice, and publishes resulting diagnostics through the existing semantic product path.
  - **Climax:** Aaron sees a clear engineering insufficiency such as undersized protection or incompatible relay sizing rather than only a generic semantic-valid/invalid answer.
  - **Resolution:** Athena proves that it can reason over a first engineering knowledge slice directly from canonical meaning.

- **UJ-2. Maya reviews the impact of an engineering change.**
  - **Persona + context:** Maya needs to understand what else becomes affected when one engineering value changes.
  - **Entry state:** A valid baseline and changed repository state are available in the current M6 and M8 review path.
  - **Path:** Maya changes or reviews a changed engineering value. Athena recomputes derived engineering context and capability facts, identifies affected rule evaluations, and publishes affected semantic subjects into the review surface.
  - **Climax:** Maya can see that one change propagates into breaker, relay, or cable consequences instead of reading only the raw edited property.
  - **Resolution:** Athena proves that review can express engineering impact, not just textual or structural delta.

- **UJ-3. Priya audits the first engineering knowledge boundary.**
  - **Persona + context:** Priya is checking whether M9 adds engineering reasoning without collapsing into uncontrolled rule sprawl.
  - **Entry state:** The first knowledge runtime is implemented for a narrow electrical slice.
  - **Path:** Priya inspects the derived facts, the constrained rule pack, the emitted diagnostics, and the explicit non-goals.
  - **Climax:** Priya can explain what M9 knows, what it does not know, and why the milestone remains small enough to validate the architecture honestly.
  - **Resolution:** Athena gains a credible first knowledge-runtime proof without pretending to be a full engineering expert system.

## 3. Glossary

- **Derived Engineering Context** - deterministic engineering context computed from canonical semantic state, such as full-load current, starting current, or thermal load, before Athena promotes selected meanings into capability facts.
- **Engineering Capability Fact** - a derived, inspectable engineering judgement computed above derived engineering context, such as required protection current, cable demand, or relay sizing demand.
- **Constraint Evaluation** - the governed runtime check that compares derived capability facts against a narrow rule slice and produces engineering sufficiency results.
- **Impact Consequence** - the explicit record that a changed engineering fact affects one or more downstream semantic subjects or constraint evaluations.
- **Knowledge Runtime** - the kernel-owned runtime slice that derives engineering context, capability facts, evaluates constraints, and publishes resulting diagnostics and review consequences.
- **Knowledge Pack** - the governed extension unit that contributes a narrow set of derived-context formulas, capability-fact semantics, and rule slices for a specific engineering scope.
- **Rule Pack** - the fixed, narrowly scoped rule slice activated from a knowledge pack for the M9 proof.
- **Engineering Sufficiency** - the question of whether a design is adequate for a governed engineering rule slice, beyond merely being structurally or syntactically valid.
- **Semantic Diagnostic** - the typed output Athena publishes when engineering sufficiency fails or degrades, including severity, explanation, and affected semantic identities.

## 4. Features

### 4.1 Derived Engineering Context And Capability Facts

**Description:** Athena must derive a first narrow set of electrical engineering context and then promote selected meanings into capability facts instead of treating engineering values only as passive properties. Realizes UJ-1, UJ-3.

#### FR-1: Derive Inspectable Engineering Context From Canonical Semantic State

Athena can compute a first narrow set of derived engineering context from canonical `Engineering IR`. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena derives at least one governed set of electrical intermediate context values such as full-load current, starting current, or thermal load.
- Derived engineering context remains traceable back to canonical semantic subjects and authored input values.
- Derived engineering context is published as Athena-owned kernel output rather than renderer-local annotations or vendor-specific metadata.

#### FR-2: Derive Capability Facts From Derived Context Through Explicit Domain Semantics

Athena can keep the first capability derivation slice narrow, explicit, and domain-scoped. Realizes UJ-3.

**Consequences (testable):**
- Athena promotes at least one governed set of electrical capability facts from derived engineering context, such as required protection current, cable demand, or relay sizing demand.
- M9 limits capability derivation to a first electrical proof slice rather than a generic multi-domain fact platform.
- Capability-fact semantics are explicit enough for architecture review and test coverage.
- M9 does not require vendor catalog modeling to derive the first capability facts.

### 4.2 Narrow Constraint Evaluation

**Description:** Athena must evaluate a first fixed knowledge-pack rule slice over derived engineering context and capability facts so engineering insufficiency becomes a semantic outcome instead of tribal interpretation. Realizes UJ-1, UJ-3.

#### FR-3: Evaluate A First Governed Knowledge Pack Rule Slice

Athena can evaluate a first narrow electrical knowledge-pack rule slice over derived engineering context and capability facts. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Athena evaluates at least one first-pass sufficiency family such as protection sizing, cable sufficiency, or relay compatibility.
- The first M9 proof runs through a fixed governed knowledge pack rather than a generic end-user rule-authoring system.
- Constraint evaluation produces deterministic accepted, warning, or error results over the same canonical state.
- Rule evaluation stays kernel-owned rather than encoded in IDE widgets, graph adapters, or downstream review formatting.

#### FR-4: Publish Engineering Sufficiency As Semantic Diagnostics

Athena can surface engineering insufficiency through typed semantic diagnostics. Realizes UJ-1.

**Consequences (testable):**
- Failed or degraded constraint evaluations produce typed diagnostics with severity, explanation, and affected semantic identities.
- The same canonical state yields the same diagnostic outcome regardless of whether the initiating change came from source or graph.
- Engineering sufficiency diagnostics remain distinguishable from syntax, structural semantic, or renderer feedback.

### 4.3 Impact Consequences And Review Integration

**Description:** Athena must explain what else becomes affected when a governed engineering value changes. Realizes UJ-2, UJ-3.

#### FR-5: Compute Affected Engineering Subjects For A Changed Capability Fact

Athena can compute a first narrow impact consequence set when a relevant engineering value changes. Realizes UJ-2.

**Consequences (testable):**
- When a governed input changes, Athena can identify at least one affected downstream semantic subject or rule evaluation set.
- Impact consequences remain anchored on canonical semantic identities.
- Impact computation stays deterministic and inspectable for the same before/after semantic state.

#### FR-6: Feed Engineering Impact Into Existing Review Surfaces

Athena can route engineering impact into the existing semantic review path. Realizes UJ-2.

**Consequences (testable):**
- Accepted changes can publish engineering impact facts into the current runtime, LSP, and review surfaces without inventing a second review vocabulary.
- Review output can distinguish direct edited subjects from downstream affected subjects.
- M9 reuses the existing M6 and M8 semantic review path instead of creating a separate knowledge-review subsystem.

### 4.4 Existing IDE Surfaces As Product Delivery

**Description:** M9 must prove engineering knowledge through the current product path instead of widening into a new IDE or renderer milestone. Realizes UJ-1, UJ-2, UJ-3.

#### FR-7: Reuse Existing Semantic Delivery Surfaces

Athena can surface the first knowledge-runtime proof through existing semantic product surfaces. Realizes UJ-1, UJ-2, UJ-3.

**Consequences (testable):**
- The first M9 proof is delivered through existing runtime, LSP, Problems, semantic inspection, or semantic SCM surfaces.
- M9 does not require a new graphical editor mode, symbol palette, or sheet-management surface to prove the knowledge-runtime architecture.
- Supporting IDE work stays additive and limited to displaying governed knowledge outputs clearly.

#### FR-8: Preserve Renderer And Workbench Independence

Athena can keep the knowledge-runtime proof independent from renderer and workbench depth. Realizes UJ-3.

**Consequences (testable):**
- QElectroTech-style renderer and editor concerns remain later than M9.
- M9 knowledge outputs can be consumed by existing text, graph, and review surfaces without changing their authority boundaries.
- Engineering knowledge remains upstream of graphical notation, not dependent on it.

## 5. Non-Goals (Explicit)

- M9 does not become a full electrical standards platform.
- M9 does not become a vendor-catalog, part-selection, or procurement optimization system.
- M9 does not become an AI copilot, auto-design, or auto-remediation milestone.
- M9 does not become a QElectroTech- or EPLAN-class workbench-depth milestone.
- M9 does not reopen M7 renderer delivery or M8 mutation authority.
- M9 does not attempt multi-domain engineering knowledge packs in the first proof.

## 6. MVP Scope

### 6.1 In Scope

- one narrow electrical derived-context slice
- one narrow electrical capability-fact slice
- one fixed governed electrical knowledge pack and rule slice
- deterministic engineering sufficiency diagnostics
- deterministic impact consequence output for changed governed values
- reuse of current runtime, LSP, Problems, and review surfaces for delivery

### 6.2 Out Of Scope For MVP

- broad notation or symbol-library expansion
- multi-sheet ECAD editor depth
- automatic correction or redesign suggestions
- full standards and vendor-compliance coverage
- generic rule-authoring platform or knowledge-pack authoring ecosystem for all future domains

## 7. Success Metrics

**Primary**
- **SM-1:** Athena can derive a first governed set of electrical engineering context from canonical semantic state. Validates FR-1.
- **SM-2:** Athena can derive a first governed set of electrical capability facts from derived engineering context. Validates FR-2.
- **SM-3:** Athena can evaluate a first fixed electrical knowledge-pack rule slice and emit deterministic engineering sufficiency diagnostics. Validates FR-3, FR-4.
- **SM-4:** Athena can identify downstream affected engineering subjects when a governed engineering value changes. Validates FR-5.
- **SM-5:** Athena can route engineering impact into the current semantic review path without inventing a second review vocabulary. Validates FR-6.
- **SM-6:** Athena can publish the first knowledge-runtime proof through existing semantic delivery surfaces without requiring a new editor or renderer milestone. Validates FR-7, FR-8.

**Secondary**
- **SM-7:** M9 prepares later richer engineering authoring and diagnostics without reopening the M8 mutation core.
- **SM-8:** M9 proves that engineering sufficiency can remain kernel-owned and renderer-independent.

**Counter-metrics**
- **SM-C1:** Do not optimize for broad rule count over correctness and inspectability of the first rule slice.
- **SM-C2:** Do not optimize for flashy new IDE surfaces if the same knowledge proof can be delivered through the existing semantic path.
- **SM-C3:** Do not optimize for vendor-specific richness if it weakens canonical and domain-scoped knowledge modeling.

## 8. Cross-Cutting NFRs

- **NFR-1 Kernel Authority Preservation:** Engineering capability, constraint, and impact evaluation must remain upstream of renderer, IDE, and vendor adapters.
- **NFR-2 Determinism:** The same canonical state yields the same derived engineering context, capability facts, constraint results, and impact consequences.
- **NFR-3 Inspectability:** Derived engineering context, capability facts, rule evaluations, diagnostics, and impact consequences remain inspectable for development and architecture review.
- **NFR-4 Narrowness:** The first M9 proof must stay small enough to validate the architecture honestly rather than hiding risk inside rule sprawl.
- **NFR-5 Governed Packaging:** The first M9 proof must ship as a fixed governed knowledge pack rather than a general rule-authoring platform.
- **NFR-6 Delivery Reuse:** M9 must prefer existing semantic delivery surfaces over opening a new product-shell or renderer frontier.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- The current Athena workbench remains the product host for M9.
- Supporting IDE work is allowed only where it directly improves the delivery of governed knowledge outputs.
- M9 should prove engineering understanding, not visual polish or editor breadth.

### 9.2 Architectural Guardrails

- `Engineering IR` remains the canonical source for knowledge derivation.
- Derived engineering context remains an explicit runtime-visible layer above canonical engineering state and below capability facts.
- Runtime remains the owner of derived context, capability-fact, constraint, and impact evaluation orchestration.
- The first M9 proof uses a fixed governed knowledge pack rather than open rule authoring.
- `ide/lsp` remains the sole IDE semantic entry point.
- M9 must reuse the current M6 and M8 review and diagnostic path wherever possible.
- Renderer and graph frameworks remain downstream consumers, not engineering-knowledge authorities.

### 9.3 Roadmap Guardrails

- M9 owns the first engineering knowledge runtime proof.
- M9 owns a fixed knowledge-pack proof, not a knowledge-pack ecosystem.
- M9 does not reopen M5 through M8 milestone centers.
- QElectroTech-style workbench and notation depth remain later than M9.
- AI engineering agents remain later than M9.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4 through M8
- **Primary delivery target:** local developer-run product shell plus deterministic JVM, IDE, and knowledge verification
- **Primary runtime authority:** runtime-backed canonical semantic state with derived knowledge evaluation
- **Primary text/language-service foundation:** Athena LSP
- **Primary product-delivery surfaces:** diagnostics, semantic inspection, semantic SCM or review output, and existing graph-aware workbench context

## 11. Open Questions

1. What is the narrowest fixed electrical knowledge-pack rule slice that still proves real engineering value without widening into standards-platform scope?
2. Which governed values should M9 treat as the first authoritative capability inputs: motor power, breaker current, cable current, relay current, or a smaller subset?
3. Should impact consequences publish only affected semantic identities in M9, or also short categorized reason labels?
4. How should M9 distinguish structural semantic validity from engineering sufficiency in the current user-facing diagnostics language?
5. What is the right public name and version boundary for the first fixed M9 knowledge pack?

## 12. Assumptions Index

- M9 should stay kernel-first and should not become a renderer or workbench-depth milestone.
- M9 should prove one narrow electrical capability-and-constraint slice before any broader engineering knowledge ambition.
- M9 should represent engineering meaning as `Engineering IR -> Derived Engineering Context -> Capability Fact -> Constraint Result -> Impact Consequence -> Diagnostic`.
- Existing runtime, LSP, Problems, and review surfaces are sufficient for the first M9 proof.
- QElectroTech-style screenshots and source references are later renderer and workbench inputs, not M9 core requirements.
- M9 should use a fixed governed knowledge pack and explicitly defer rule authoring and broader pack ecosystems.
- M9 should prepare later source-apply, authoring-depth, and AI-assisted milestones without reopening the kernel, mutation, or projection foundations already frozen by M0 through M8.
