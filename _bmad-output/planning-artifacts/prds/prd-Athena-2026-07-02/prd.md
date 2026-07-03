---
title: Athena
status: final
created: 2026-07-02
updated: 2026-07-02
---

# PRD: Athena

## 0. Document Purpose

This PRD defines the product requirements for Athena as the implementation platform for the EngineeringOS thesis. It is written for founders, product decision-makers, developers, architecture owners, and downstream BMad workflows. The document is capability-first: Glossary terms are authoritative, features are grouped with globally numbered functional requirements, cross-cutting non-functional requirements are called out explicitly, and unresolved inferences are tagged inline as `[ASSUMPTION]`. This PRD builds on the product brief and technical addendum in `brief-Athena-2026-07-02/` and does not duplicate deep mechanism design that belongs in architecture.

## 1. Vision

Athena is an open semantic engineering platform that separates engineering intent from engineering representation. Its job is to make engineering meaning explicit, compilable, governed, and reusable above drawings, vendor project files, and tool-specific application models.

The product exists because the current engineering software stack places authority too low in the system. Drawings, pages, coordinates, XML dialects, and vendor-shaped files remain the practical source of truth even when teams need machine reasoning, rule execution, cross-tool synchronization, reusable engineering knowledge, and trustworthy AI assistance. Athena moves that authority into a semantic core made of `Engineering Language`, `Engineering Ontology`, `Engineering IR`, compiler logic, rules, and governed knowledge.

Athena should become the semantic layer beneath future engineering products and workflows rather than another isolated application beside them. The strategic opportunity is to own the missing coordination layer that lets many downstream views, tools, and integrations operate over one source of truth.

## 1.1 Why Now

Engineering software is entering a platform shift from representation-first systems to meaning-first systems. AI, automation, governed standards logic, and multi-tool workflows all become more valuable when they operate on explicit semantics rather than on drawings, coordinates, and vendor-specific file structures.

Athena exists to respond to that shift directly. If the source of truth remains trapped inside downstream applications, every later capability remains brittle. If the semantic layer moves upward into a compiler-centered platform, downstream views, integrations, and product surfaces can become coordinated consequences of one engineering model.

## 2. Target User

### 2.1 Jobs To Be Done

- Founders need a coherent product and platform thesis that explains why Athena is a new infrastructure layer rather than another engineering tool.
- Platform engineers need a stable semantic and compiler boundary they can build against without letting UI, exporter, or vendor-file assumptions leak into the core.
- Standards and domain maintainers need a governed path to turn engineering knowledge into reusable ontology, mappings, and rule artifacts.
- Engineering teams need a way to author intent once and derive synchronized outputs, validations, and integrations from it.
- Technical buyers need a path to improve traceability, interoperability, and automation without replacing every incumbent tool at once.

### 2.2 Non-Users (v1)

- Teams looking for a turnkey CAD replacement with no interest in a semantic source of truth
- Organizations that only want an AI wrapper over existing files without governed semantics
- Buyers expecting Athena v1 to replace EPLAN, KiCad, FreeCAD, or existing enterprise suites end to end

### 2.3 Key User Journeys

`[ASSUMPTION: These journeys are inferred from the current platform thesis and should be validated before UX work begins.]`

- **UJ-1. Aaron defines a semantic engineering project instead of starting from a drawing.**
  - **Persona + context:** Aaron is the platform founder shaping the first executable proof of the EngineeringOS thesis.
  - **Entry state:** Working in an authoring surface connected to Athena's core.
  - **Path:** Aaron authors engineering intent in `Engineering Language`; Athena lowers that intent into `Engineering IR`; the compiler validates and derives consequences; Athena emits a deterministic downstream output.
  - **Climax:** Aaron can explain the result at the language, IR, and compiler-pass boundary rather than pointing to hidden application behavior.
  - **Resolution:** The same project is ready for further rendering, export, and rule-driven evolution without redefining the source of truth.

- **UJ-2. Maya inspects why the system accepted or rejected an engineering change.**
  - **Persona + context:** Maya is a platform engineer or reviewer responsible for trust in compiler behavior.
  - **Entry state:** A semantic project has been authored or changed and compiled.
  - **Path:** Maya opens diagnostic and graph inspection surfaces; she traces identities, relationships, and rule findings; she maps a finding back to ontology concepts, rule packs, or standards mappings.
  - **Climax:** Maya can see exactly why a conclusion was reached and whether the issue belongs to authored intent, governed knowledge, or target-specific logic.
  - **Resolution:** She can approve the change, fix the source semantics, or escalate a governed knowledge update without resorting to drawing-level guesswork.

- **UJ-3. Priya publishes governed knowledge that future projects can reuse.**
  - **Persona + context:** Priya maintains domain knowledge, mappings, or standards-backed constraints.
  - **Entry state:** Priya has reviewed candidate ontology or rule content derived from standards or reference material.
  - **Path:** Priya submits reviewed updates through the governed knowledge path; Athena packages them as ontology additions, mappings, or rule proposals; future projects consume them through the compiler.
  - **Climax:** A later engineering project gains new validation or derivation behavior without changing the product's semantic center.
  - **Resolution:** The knowledge asset becomes reusable infrastructure rather than remaining trapped in one team or one project.

## 3. Glossary

- **Athena** - The implementation platform that operationalizes the EngineeringOS thesis through a semantic core and compiler-centered architecture.
- **Engineering Language** - The human-readable authoring surface used to express engineering intent.
- **Engineering Ontology** - The governed conceptual vocabulary for entities, relationships, constraints, and behavior.
- **Engineering IR** - The canonical computational representation of engineering meaning.
- **Engineering Compiler** - The execution system that validates, derives, diagnoses, and prepares outputs from `Engineering IR`.
- **Knowledge Compiler** - The separate governed path that converts reviewed standards-derived knowledge into ontology, mappings, and rule artifacts.
- **Governed Knowledge** - Reviewable ontology additions, standards mappings, rule packs, and related semantic assets accepted through governance.
- **Plugin** - An extension that adds domain capability, import/export boundaries, renderers, rules, AI workflows, or other edge behavior without redefining the core.
- **Studio** - The human-facing inspection and authoring shell that exposes the semantic core without becoming the source of truth.
- **Renderer** - A target-facing component that turns compiled semantics into a downstream representation.
- **Layout IR** - The representation of view and presentation intent downstream of `Engineering IR`.
- **Geometry IR** - The representation of exact renderable structure downstream of `Layout IR`.

## 4. Features

### 4.1 Semantic Authoring And Canonical Modeling
**Description:** Athena must let users author engineering intent in a human-readable form and lower that intent into a canonical semantic model without making drawings, pages, or vendor file structures authoritative. The product may support multiple authoring inputs over time, but they must converge on the same `Engineering IR`. Realizes UJ-1. `[ASSUMPTION: the first authoring surface is text-first and compiler-first rather than UI-first.]`

**Functional Requirements:**

#### FR-1: Author Engineering Intent In Engineering Language

An author can express engineering intent in `Engineering Language` without encoding layout coordinates, page mechanics, or target-specific file structures. Realizes UJ-1.

**Consequences (testable):**
- `Engineering Language` expresses engineering concepts and relationships using Glossary terms rather than drawing primitives.
- The authoring path remains valid even when no downstream visual editor is present.
- Authoring artifacts can be stored, diffed, and reviewed independently of generated outputs.

#### FR-2: Lower Multiple Inputs Into Engineering IR

Athena can lower authored, imported, or AI-assisted inputs into `Engineering IR` through explicit transformation boundaries. Realizes UJ-1.

**Consequences (testable):**
- Imported or AI-assisted content does not bypass `Engineering IR`.
- Lowering outputs enough explicit structure for validation, diffing, and target preparation.
- The system distinguishes authoring inputs from canonical representation in both code and user-facing explanations.

#### FR-3: Preserve Stable Semantic Identity

Athena preserves stable identity for engineering objects across compilation, regeneration, and changes in view or output target. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Identity is not tied to page order, coordinates, or exporter-specific handles.
- Layout or rendering changes do not create new engineering objects.
- Semantic references remain traceable across compiler passes and downstream outputs.

### 4.2 Compiler, Validation, And Diagnostics
**Description:** Athena's operational heart is the `Engineering Compiler`. It must turn canonical semantics into validated, inspectable, and reusable outputs through explicit passes rather than hidden application state. Realizes UJ-1, UJ-2.

**Functional Requirements:**

#### FR-4: Execute Explicit Compiler Passes

The `Engineering Compiler` runs semantic compilation as a sequence of explicit passes with declared responsibilities. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The compiler exposes distinct stages for normalization, validation, rule execution, derivation, and target preparation.
- Each pass consumes declared inputs and produces declared outputs.
- Compiler behavior can be inspected without reverse-engineering hidden UI or exporter logic.

#### FR-5: Produce Traceable Diagnostics

Athena emits diagnostics with enough provenance that a reviewer can trace findings back to authored intent, ontology concepts, rule sources, or governed mappings. Realizes UJ-2.

**Consequences (testable):**
- Diagnostics distinguish errors, warnings, and advisory findings.
- Diagnostic records retain references to the relevant semantic objects and contributing rule or knowledge artifacts.
- Reviewers can explain why a result occurred using system-visible evidence.

#### FR-6: Generate Coordinated Downstream Outputs

Athena produces multiple downstream outputs as coordinated consequences of one semantic source rather than as separate authorities. Realizes UJ-1.

**Consequences (testable):**
- At least one deterministic downstream output can be generated from the same compiled model in the initial milestone.
- Output generation does not require reinterpreting author-facing syntax from scratch.
- A change to the semantic source propagates through compilation rather than through manual reconciliation of disconnected artifacts.

**Feature-specific NFRs:**
- Compiler execution must be deterministic for identical semantic inputs and governed knowledge versions.
- Diagnostics must remain inspectable and legible enough for human review.

### 4.3 Governed Knowledge And Standards Mapping
**Description:** Athena must support a separate governed path for engineering knowledge so that standards-backed meaning, mappings, and rule logic can become reusable infrastructure instead of implicit team memory. Realizes UJ-3.

**Functional Requirements:**

#### FR-7: Accept Reviewed Knowledge Through A Separate Governance Path

Athena accepts reviewed standards-derived or reference-derived knowledge through the `Knowledge Compiler` rather than directly through project compilation. Realizes UJ-3.

**Consequences (testable):**
- Project compilation and knowledge compilation remain distinct workflows.
- Candidate knowledge can be reviewed before it affects operational compiler behavior.
- Knowledge updates can be versioned independently of specific projects.

#### FR-8: Publish Reusable Ontology, Mapping, And Rule Artifacts

Athena can package accepted governed knowledge as reusable ontology additions, standards mappings, and rule artifacts. Realizes UJ-3.

**Consequences (testable):**
- Accepted knowledge can be consumed by multiple projects.
- Published artifacts declare compatibility and provenance.
- Compiler behavior can depend on governed artifacts without hard-coding them into one application surface.

### 4.4 Plugin And Integration Layer
**Description:** Athena must remain small and durable at the center while growing through `Plugin` boundaries for domains, external tools, standards, runtime bridges, and target outputs. Realizes UJ-1, UJ-2, UJ-3.

**Functional Requirements:**

#### FR-9: Expose Stable Plugin Contracts

Athena exposes published contracts for extension at the language, rules, standards, renderer, importer, exporter, AI, and knowledge boundaries.

**Consequences (testable):**
- Extension points exist without redefining the Glossary terms that belong to the core.
- Plugins declare what they extend and what compatibility they require.
- The system can reject or isolate incompatible plugin behavior rather than silently merging it into the core.

#### FR-10: Integrate External Tool And Standards Boundaries

Athena can treat external tools and standards as sources, targets, or compatibility boundaries rather than as internal authorities. `[ASSUMPTION: first-class boundaries will include QElectroTech, KiCad, FreeCAD, AutomationML, and OPC UA over time.]`

**Consequences (testable):**
- External integrations consume or emit derived forms rather than redefining `Engineering IR`.
- The platform can explain what is owned internally versus mapped at the boundary.
- Downstream compatibility does not require cloning the internal architecture of the external system.

#### FR-11: Support Runtime And Enterprise Bridges Without Re-Centering Authority

Athena can connect to runtime and enterprise contexts while preserving the semantic core as the upstream authority.

**Consequences (testable):**
- Runtime-facing and enterprise-facing integrations are downstream of compilation.
- Hosted or organizational features distribute, govern, or operate on semantic artifacts rather than inventing new hidden sources of truth.
- Integration paths can be added incrementally without collapsing the core into a monolith.

### 4.5 Inspection And Product Surfaces
**Description:** Athena requires human-facing surfaces for authoring, inspection, diagnostics, and projection, but those surfaces must remain downstream of the semantic core. Realizes UJ-2. `[ASSUMPTION: Studio follows after the initial semantic pipeline rather than preceding it.]`

**Functional Requirements:**

#### FR-12: Expose Semantic Inspection Surfaces

Athena provides human-facing inspection surfaces that expose language, graph, diagnostics, and compiled outputs without relocating authority into UI state. Realizes UJ-2.

**Consequences (testable):**
- A user can inspect semantic objects, relationships, diagnostics, and outputs through a surface such as `Studio` or equivalent tooling.
- Behavior observable in the surface can be explained at the `Engineering Language`, `Engineering IR`, or compiler boundary.
- The inspection surface does not become the only place where the product can function.

#### FR-13: Support Multiple Downstream View Types

Athena can support multiple derived view types over the same semantic source, including layout- and projection-oriented outputs.

**Consequences (testable):**
- The system distinguishes semantic truth from view intent and exact renderable geometry.
- More than one view or target can coexist over the same semantic project without redefining engineering identity.
- New views can be added without rewriting the core compiler contract.

## 5. Non-Goals (Explicit)

- Athena is not building a drawing-first CAD application as its center of gravity.
- Athena is not replacing incumbent tools head-on before the semantic core proves itself.
- Athena is not letting AI generate opaque production artifacts outside governed semantic boundaries.
- Athena is not hard-coding one engineering discipline's vocabulary as the permanent definition of engineering.
- Athena is not discovering its architecture through cloud or enterprise requirements first.

## 6. MVP Scope

### 6.1 In Scope

- A minimal `Engineering Language` authoring path
- Lowering from authored intent into `Engineering IR`
- Stable identity and explicit relationship representation
- Explicit compiler passes for validation and deterministic derivation
- At least one canonical rule path
- At least one deterministic downstream output
- Enough diagnostics to explain compiler outcomes

### 6.2 Out of Scope for MVP

- Full CAD-like visual authoring workflows
- Full replacement of EPLAN, KiCad, FreeCAD, or equivalent downstream tools
- Broad domain coverage beyond the initial proving wedge `[NOTE FOR PM: first wedge still unresolved]`
- Mature cloud collaboration and enterprise governance surfaces `[NOTE FOR PM: defer until core contracts stabilize]`
- Unbounded AI-generated engineering workflows beyond semantic assistance

## 7. Cross-Cutting NFRs

- **NFR-1 Traceability:** Every compiler conclusion that matters to user trust must be explainable through semantic objects, ontology concepts, rule artifacts, or standards mappings.
- **NFR-2 Determinism:** Given the same semantic inputs and governed knowledge versions, compilation must produce the same results.
- **NFR-3 Extensibility:** Domain growth, renderers, importers, exporters, AI workflows, and knowledge packs must attach through explicit contracts rather than changes to the semantic center.
- **NFR-4 Interoperability:** External tool and standards boundaries must remain integrations around the core, not substitutes for the core.
- **NFR-5 Governance:** AI-assisted or standards-derived knowledge must enter operational use only through reviewable governance.
- **NFR-6 Replaceable Surfaces:** UI and rendering layers must remain downstream and replaceable.

## 8. Constraints And Guardrails

### 8.1 Architectural Guardrails

- `Engineering Language`, `Engineering Ontology`, `Engineering IR`, compiler logic, rules, and governed knowledge remain under Athena control.
- `Engineering IR`, `Layout IR`, and `Geometry IR` remain distinct representations.
- `Studio`, cloud, and enterprise surfaces remain downstream of the semantic core.

### 8.2 Reuse Guardrails

- Athena reuses mature adjacent systems where they already solve the right problem.
- Geometry is integrated rather than reimplemented.
- External ecosystems are treated as boundaries, not as internal center-of-gravity dependencies.

### 8.3 AI Guardrails

- AI assists authoring, review, extraction, and transformation over semantics.
- AI does not become the hidden source of truth.
- AI outputs affecting governed knowledge require a review path.

## 9. Integration And Dependencies

- Editor interoperability should route through standard tooling such as `LSP`.
- Downstream visual and application surfaces should remain compatible with a Kotlin-centered core and a replaceable UI boundary.
- Mechanical, electrical, PCB, runtime, and standards integrations should remain explicit boundary work rather than core substitution.
- `[ASSUMPTION: Initial milestone dependencies prioritize compiler-first infrastructure over cloud or enterprise operations.]`

## 10. Success Metrics

**Primary**
- **SM-1:** Athena can carry at least one end-to-end semantic project from `Engineering Language` through `Engineering IR`, validation, and deterministic downstream output. Validates FR-1, FR-2, FR-4, FR-6.
- **SM-2:** Reviewers can explain compiler findings through visible provenance rather than hidden application state. Validates FR-5, FR-7, FR-8, NFR-1.

**Secondary**
- **SM-3:** The product can add at least one boundary integration or extension without changing the core semantic contracts. Validates FR-9, FR-10, FR-11, NFR-3.
- **SM-4:** The same semantic project can support more than one derived inspection or output view without redefining engineering identity. Validates FR-3, FR-12, FR-13.

**Counter-metrics (do not optimize)**
- **SM-C1:** Do not optimize for breadth of UI surface before semantic-core credibility is proven. Counterbalances SM-1.
- **SM-C2:** Do not optimize for AI novelty at the cost of governed semantics and traceability. Counterbalances SM-2.
- **SM-C3:** Do not optimize for number of integrations if those integrations begin to dictate the internal model. Counterbalances SM-3.

## 11. Open Questions

1. Which engineering domain should be the first proving wedge for MVP?
2. Which initial downstream output or target best demonstrates semantic leverage after the first deterministic compiler path works?
3. What level of domain specificity belongs in the first `Engineering Ontology` without hard-coding one discipline as the permanent center?
4. When should `Studio` become a formal product surface rather than a lightweight inspection shell?
5. What is the first commercial surface to introduce above the open semantic core?

## 12. Assumptions Index

- Section 4.1 - The first authoring surface is text-first and compiler-first rather than UI-first.
- Section 2.3 - The named journeys are inferred from the current platform thesis and should be validated before UX work begins.
- Section 4.4 - First-class external boundaries will include QElectroTech, KiCad, FreeCAD, AutomationML, and OPC UA over time.
- Section 4.5 - `Studio` follows after the initial semantic pipeline rather than preceding it.
- Section 9 - Initial milestone dependencies prioritize compiler-first infrastructure over cloud or enterprise operations.
