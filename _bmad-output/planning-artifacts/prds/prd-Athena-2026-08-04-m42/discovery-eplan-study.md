# EPLAN Study Discovery Evidence

## Source

- `draft/20260803-confuse/dir-where-we-go-and-learn-from-eplan-study.md`
- Extraction purpose: preserve product-direction evidence for M42 PRD discovery.

## Product Insights

- Athena should be the semantic intelligence layer above EPLAN, CAD, CAE, and MES, not an EPLAN clone, AI CAD editor, or drawing tool.
- Existing compiler, Engineering IR, projection, plugin, and semantic-first architecture remains valid. M41 provides the downstream representation foundation; M42 must begin the upstream Engineering Knowledge Reality.
- EPLAN's durable value comes from its engineering object model, master data, rules, industrial knowledge, and manufacturing outputs, not diagram drawing.
- Engineering IR limited to components, properties, connections, and geometry would make Athena a modeling framework rather than an Engineering OS.
- M42 should establish a small, inspectable `Engineering Knowledge IR v1` centered on engineering entities, capabilities, constraints, and typed engineering relationships.
- Spatial Reality remains downstream. It may project engineering meaning but must never become its source or authority.
- Pattern selection, engineering reasoning, and universal flow models are plausible later layers. They depend on a sound knowledge model and do not belong in M42.
- Strategic framing to preserve: Athena has built the lower half of EngineeringOS; M42 begins the upper half. M41 completes the "physics of representation," while M42 starts the "physics of engineering knowledge."

## User Expectations

- Keep M41 intact and treat it as prerequisite infrastructure, not discarded work.
- Rebalance the roadmap after M41 from drawing polish toward engineering intelligence.
- Model what an engineering object can do and what governs it, not merely what device it is or how it is drawn.
- Let one engineering concept support multiple implementations without making a vendor or device identity kernel truth.
- Keep authoring natural and engineering-centered while compiler-owned layers handle projection and presentation.
- Prove the model with one small, concrete, testable electrical slice before generalizing across engineering domains.
- EPLAN's current interaction model is that an engineer selects objects and the system manages them. Athena's long-term target is that an engineer states intent and the system generates and validates a solution.
- Measure long-term progress by **Engineering Intelligence Density**: how many engineering decisions Athena can make, not diagrams generated or IDE features delivered. The source's `human 20% / Athena 80%` split is a directional aspiration, not an M42 target.

## EPLAN-Aligned Terminology

### Core Terms

- **Engineering Entity:** A durable engineering concept or instance with engineering meaning independent of its drawing representation.
- **Capability:** Engineering behavior an entity provides. Example: `Controlled Power Switching Capability` can be implemented by a contactor, solid-state relay, or vendor-specific variant.
- **Constraint:** A condition governing an entity, capability, relationship, or valid implementation, including engineering values and units where applicable.
- **Engineering Relationship:** A typed semantic relationship between engineering entities, richer than a generic connection.
- **Engineering Knowledge Reality:** The upstream layer that represents engineering concepts, capabilities, constraints, and relationships before existing Engineering Reality and projection layers consume them.
- **Engineering Knowledge IR v1:** M42's small, deterministic compiled representation of that knowledge.

### Relationship Vocabulary

- `provides energy to`
- `controls`
- `protects`
- `communicates with`
- `mechanically drives`

### Deferred Concepts

- **Pattern:** Intent plus constraints produces a generated solution and validation.
- **Reasoning:** Intent, constraints, pattern selection, solution, then validation.
- **Universal engineering flows:** Energy, signal, material, force, and information.

These terms inform future direction but do not expand M42 scope.

## Non-Goals

- No ontology engine.
- No knowledge graph database.
- No AI reasoning or engineering agent.
- No pattern language, pattern engine, or rule engine.
- No universal engineering model or cross-domain ontology.
- No broad capability taxonomy beyond what the chosen proof slice needs.
- No new rendering engine or transfer of semantic authority to Spatial or Presentation Reality.
- No priority shift toward IDE polish, visualization, additional DSL surface, presentation styling, export, readability, or advanced routing.
- No vendor-specific model as kernel truth.

## Risks

- **Device-first modeling:** Entities become catalog-device records and capabilities remain incidental metadata.
- **Thin IR:** Engineering Knowledge IR remains limited to component, property, connection, and geometry concepts.
- **Clone drift:** M42 moves Athena toward "VS Code for Engineering" or an open-source EPLAN clone.
- **Premature breadth:** Ontology, AI, pattern, rule, or universal-domain machinery obscures the small M42 foundation.
- **Authority inversion:** Drawing, layout, renderer, or projection concerns leak upward and define engineering knowledge.
- **Vendor coupling:** Manufacturer identity becomes required to express a capability.
- **Unfalsifiable abstraction:** Universal concepts are introduced without one concrete end-to-end engineering example and passing checks.
- **Roadmap regression:** Downstream visual polish displaces the knowledge-layer correction identified by the study.

## Falsifiable PRD Requirements

1. Athena source can express an engineering entity, capability, constraint, and typed engineering relationship as durable engineering meaning.
2. One engineering entity can provide more than one capability.
3. At least two distinct implementations can satisfy the same capability without vendor identity becoming kernel truth.
4. M42 produces a small `Engineering Knowledge IR v1`, rather than a universal ontology or reasoning system.
5. Typed relationships distinguish `provides energy to`, `controls`, `protects`, `communicates with`, and `mechanically drives` from a generic connection where those meanings apply.
6. Constraints can express both fixed engineering conditions, such as voltage or power, and relational conditions, such as breaker rating relative to motor current and a safety factor.
7. M41's existing Engineering Reality and projection foundation remains intact; Spatial Reality stays downstream and cannot define engineering intelligence.
8. One small electrical slice demonstrates the move from device objects to engineering concepts without requiring a broad cross-domain model.
9. M42 fails its strategic purpose if its primary output is IDE polish, drawing features, additional presentation behavior, or more surface syntax without richer engineering meaning.
10. M42 fails its scope boundary if it introduces an ontology engine, knowledge graph database, AI reasoning, pattern language, or universal engineering model.
