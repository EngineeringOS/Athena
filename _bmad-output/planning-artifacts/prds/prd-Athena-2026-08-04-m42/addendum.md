# M42 Engineering Knowledge System - PRD Addendum

Date: 2026-08-04
Status: Companion input to M42 PRD

## Role

This addendum preserves architectural depth, migration evidence, and rejected-alternative rationale
that informs the M42 PRD but does not belong in capability requirements. The approved discussion
design remains the detailed source:

- `m42-engineering-knowledge-system-design.md`

## Architecture Boundary

M42 establishes the Engineering Knowledge System as a cross-cutting authority. It is not another
Reality domain, graph, database, source model, or mutation path.

```text
governed Athena knowledge packages
            |
            v
Engineering Knowledge System
  definitions + evaluation
            |
            +---- resolves knowledge-backed meaning ----+
            |                                            |
Athena source -> Semantic Graph -> Engineering Reality <-+
                                      |
                                      +---- knowledge evaluation
                                      |
                                      v
                           Validated Engineering Reality
                                      |
                                      v
                              Projection Reality
                                      |
                                      v
                               Spatial Reality
                                      |
                                      v
                              existing renderer
```

Engineering Reality remains canonical owner of project Entities, Functions, Ports, Relationships,
and authored/resolved facts. `Validated Engineering Reality` means that canonical model paired with
immutable `READY` or `INCOMPLETE` validation evidence. It is not a copied entity graph.

Knowledge definitions and project validation use separate open contracts:

- `EngineeringKnowledgeDocument` contains compiled governed definitions from the resolved package
  set;
- `EngineeringValidationDocument` contains project-specific evaluation, canonical subject
  references, state, judgements, corrections, and provenance.

## Model Boundaries

### Identity

```text
EngineeringConcept
    vendor-neutral governed definition

EngineeringEntity
    authored project instance

PartImplementation
    optional vendor realization bound to an Entity

PhysicalAsset
    installed instance; deferred beyond M42

ProjectionOccurrence
    downstream appearance; never engineering authority
```

`EngineeringFunction` is owned by an Entity for identity and containment. Relationships remain
project-level and may bind Function-to-Function across Entities, exact Ports for connectivity, or
Entities when their governed definition admits that participant level.

Capability means what a subject can participate in, provide, or consume. It never means what the
subject is. Concept identity and explicit classification keys carry kind information.

Relationship and Flow stay distinct. A Relationship provides typed semantic roles such as
provider/consumer, controller/controlled, protector/protected, and driver/driven. Flow describes
what passes through a relationship, such as electrical power, control information, mechanical
power, material, or another package-defined meaning. Not every Relationship is drawable.

## Knowledge Versus Validation

Knowledge compilation resolves package-local definitions, dependencies, qualified identities,
typed schemas, formulas, constraints, and provenance. It does not carry project satisfaction state.

Project evaluation applies compiled knowledge to canonical Engineering Reality. It produces:

- requirements, provisions, and explicit satisfaction evidence;
- typed constraint judgements;
- plain engineering explanations;
- deterministic, non-executing correction options;
- project-source and knowledge-source provenance;
- `READY`, `INCOMPLETE`, or `INVALID` state.

Evaluation never edits source, chooses providers, creates Entities, or guesses through ambiguity.

## Source And Formula Direction

Canonical domain knowledge is package-local Athena source. Manifests own package identity and
dependencies, not engineering facts. Kotlin owns compiler mechanics only.

One source file declares one default domain, such as `domain electrical`, `domain automation`, or
`domain mechanical`. Unqualified lookup filters by that domain and must still resolve exactly one
imported declaration. Package order never breaks ambiguity.

Typed values include exact quantities with units and dimensions, integers, booleans, text, symbols,
and references. Comparisons use familiar ASCII operators `>`, `>=`, `<`, `<=`, `=`, and `!=`.
Intervals use mathematical bounds such as `[10, 20)`, `[10, 20]`, and `(10, 20)`. Square bounds are
inclusive; round bounds are exclusive.

The formula subset is pure and deterministic: `+`, `-`, `*`, `/`, `min`, `max`, `round-up`,
parentheses, and typed references. M42 adds no scripts, loops, arbitrary functions, network calls,
or hidden Kotlin engineering calculations.

## Brownfield Migration Evidence

CodeGraph inspection on 2026-08-04 confirms current responsibilities remain fragmented:

- `EngineeringKnowledgeState` has runtime, impact-calculation, semantic-diff, mutation-review, and
  semantic-SCM consumers;
- `EngineeringFunction` exists in `engineering-model` but has a narrow lowering surface and no
  direct covering test reported by CodeGraph;
- `PartImplementationDefinition` lives in a separate `part-model` with compiler, plugin, and runtime
  consumers;
- Semantic Macro runtime contracts combine catalog, validation, preview, acceptance, mutation, and
  origin inspection around Component/Connection templates;
- domain code still depends on `EngineeringComponent`.

M42 therefore requires direct migration, not parallel models or adapters. Each replacement removes
its superseded production code, tests, active docs, and examples in the same delivery story.

Generic provenance, governed preview, explicit acceptance, and review mechanisms survive only when
they have consumers independent of retired Semantic Macro meaning. They receive independent product
names and contracts; legacy macro APIs do not survive as shells.

## Source Conflict Resolutions

The EPLAN study sources contain broader future direction. M42 resolves that material as follows:

- Pattern language, variants, intent-to-solution generation, and automatic provider selection are
  deferred. They depend on M42 knowledge and validation contracts.
- Engineering Knowledge is a cross-cutting System, not an upstream or downstream Reality domain.
- M42 proves electrical depth plus real automation and mechanical participation; it does not claim
  a universal engineering ontology.
- Manufacturing outputs, BOMs, reports, simulation, asset lifecycle, procurement, and catalogs are
  future consumers, not M42 deliverables.
- M43 owns Presentation and Rendering Reality. M42 adds no rendering improvement.

## Delivery Dependencies

1. **Kernel Authority:** EngineeringValue, Entity, Function, Port, Relationship, Flow reference,
   and direct Component/Connection migration. No knowledge grammar yet.
2. **Knowledge Contract:** package-local definitions for Concept, Part, Capability, Relationship,
   Flow, formula, and Constraint; canonical `EngineeringKnowledgeDocument`.
3. **Evaluation:** Requirement, Satisfaction, judgement, correction, provenance, states, canonical
   `EngineeringValidationDocument`, and product-surface integration.
4. **Product Proof And Closure:** `examples/m42/controlled-conveyor`, cross-domain evidence, legacy
   deletion audit, and final product E2E.

Exact epics and stories are downstream BMad outputs, expected to remain approximately 8-10 stories
in numeric dependency order.

## Inputs

- `draft/20260803-confuse/FULL-DISCUSS.md`
- `draft/20260803-confuse/dir-where-we-go-and-learn-from-eplan-study.md`
- `discovery-full-discuss.md`
- `discovery-eplan-study.md`
- current repository CodeGraph evidence
- user review titled `M42 Review - Verdict`
