# Reconciliation Review - M42 PRD

Sources reviewed:

- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/prd.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/addendum.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/.memlog.md`
- `_bmad-output/planning-artifacts/prds/prd-Athena-2026-08-04-m42/m42-engineering-knowledge-system-design.md`
- PRD input reconciliations and resolved reviewer findings in the same workspace

Target reviewed:

- `ARCHITECTURE-SPINE.md`

## Verdict

**CORRECTIONS REQUIRED before finalizing the spine.** Strategic direction, Reality boundaries,
Knowledge/Validation separation, exact-value strategy, failure-state classification, breaking
migration, M43 rendering deferral, cross-domain proof, and all three user journeys are structurally
supported. Seven load-bearing PRD constraints are missing or insufficiently bound, including one
direct deletion-scope contradiction.

## Findings

### High - Part, authored, and derived fact authority did not land

PRD FR-9 and the approved design require Concept suggestions, authored project facts, Part-owned
vendor facts, and derived facts to remain distinct; duplicate facts must agree; disagreement is
`INVALID`; Part assignment must never generate authored Functions or Ports. AD-22 places definitions
and project facts in the correct documents, but no AD fixes precedence, conflict behavior, or anatomy
generation. AD-29 mentions conflicting definitions, not conflicting facts. Independent stories
could therefore choose incompatible merge or precedence rules.

**Required spine correction:** bind fact ownership and conflict policy explicitly. Project source
owns authored instance facts, Part owns implementation facts, derived facts remain separate, Part
assignment generates no authored anatomy, and disagreement fails `INVALID` without precedence.

### High - Capability anti-ontology boundary is not an invariant

PRD FR-10 and the primary risk mitigation require Capability to mean only participation, provision,
or consumption. Classification must not trigger inference; authoring availability and Relationship
participation must use distinct contracts. The spine names Capability definitions and maps FR-10,
but no Rule prevents classification trees, hidden inference, or reuse of Capability as authoring or
relationship admission. This loses a central accepted correction intended to prevent ontology
expansion.

**Required spine correction:** add a Rule fixing Capability semantics and keeping classification,
`AuthoringActionAvailability`, and `RelationshipParticipationRule` separate. Relationship
definitions should remain owner of participant level, named roles, cardinality, direction, required
Capabilities, admitted Flows, and connectivity admission.

### High - Impact evidence has no owner or computation boundary

PRD FR-19, FR-28, SM-2, and UJ-1 require one input change to identify every affected Requirement,
formula result, Constraint result, explanation, and Correction Option. AD-31 defines portable
Provenance and ordered derivation steps, but does not require deterministic impact evidence or assign
its production to the compiler/Validation Document. AD-33 prohibits frontend evaluation, leaving a
gap where runtime, semantic-diff, or UI code could independently infer impact.

**Required spine correction:** make compiler evaluation the sole producer of deterministic impact
evidence and publish it through the Validation Document. Runtime, LSP, CLI, and Theia may query or
render it but must not reconstruct it by snapshot or UI-state inference.

### Medium - Standards and policy authority provenance is absent

PRD Section 14 and its unsafe-claims risk require each Constraint to identify authority as project
policy, Knowledge Package policy, vendor data, or exact external standard reference. A standards
claim additionally requires standard name, edition, clause, applicability, and source Provenance.
AD-31 supplies generic source and derivation provenance but does not bind these authority fields or
the rule that demonstration policy cannot masquerade as compliance.

**Required spine correction:** extend the definition/provenance contract with typed Constraint
authority and mandatory standard-reference evidence when a standards claim is made.

### Medium - Schema-version mismatch failure is not explicit

UJ-3, FR-21, NFR-4, and Section 13 require schema-version mismatch to fail explicitly rather than
produce partial inferred behavior. AD-32 requires a numeric `schemaVersion`; AD-33 requires Ajv shape
validation. Neither says consumers reject unsupported versions before use. A structurally valid
document with an unsupported version could pass unless each schema encodes a version constant.

**Required spine correction:** require explicit supported-version checking and rejection before
document consumption; no fallback reader, partial decode, or compatibility inference.

### Medium - Legacy deletion wording can include protected historical artifacts

PRD FR-6 and FR-29 require closed M0-M41 planning and implementation artifacts to remain unchanged.
AD-35 says to remove retired paths plus their docs/tests and all consumers without an explicit
active-product qualifier. That can be read as deleting historical BMad evidence, directly
contradicting the PRD and milestone-local rule.

**Required spine correction:** limit deletion to active production code, active tests, active docs,
and active examples. State that closed M0-M41 BMad artifacts are immutable history.

### Medium - Cross-domain constant audit omits terminals

NFR-6 forbids electrical-specific Capability, Relationship, Flow, unit, **terminal**, and rule
constants in kernel/shared contracts. AD-35's absence rule lists Capability, Relationship, Flow,
unit, and rule constants but omits terminal constants. AD-26 and AD-28 do not close this exact gap.

**Required spine correction:** add terminal/interface constants to the domain-leak prohibition and
closure audit, while allowing package-defined terminal/interface designations.

## Confirmed Alignment

- Engineering Knowledge System is cross-cutting, not another Reality, model, graph, or database.
- Engineering Reality remains sole owner of project subjects; no production
  `ValidatedEngineeringReality` type is introduced.
- Knowledge and Validation Documents are separate, immutable, open contracts.
- A failed well-formed blocking Constraint is `INCOMPLETE`; malformed, ambiguous, corrupt, or
  dimension-invalid evaluation is `INVALID` and publishes no satisfaction claim.
- Project and Knowledge Package sources share one ANTLR frontend and one default domain per file.
- Exact rational quantities, package-owned units/dimensions, bounded formulas, mathematical
  comparison operators, and interval bounds are preserved.
- Relationship and Flow remain independent; Function-to-Function cross-Entity participation and
  multi-participant connectivity remain first-class.
- Canonical JSON byte rules remain separate from JSON Schema shape validation.
- Runtime, LSP, CLI, Theia, Projection, and Spatial are consumers, not knowledge authorities.
- M42 is intentionally breaking, local-only, cross-domain, and proofed only through
  `examples/m42/controlled-conveyor` plus milestone-local evidence.
- Pattern, provider selection, AI chat, procurement/lifecycle, ontology expansion, distributed
  infrastructure, and Presentation/Rendering work remain deferred.

## Reconciliation Result

No PRD rewrite is needed. Resolve the seven spine gaps above, rerun architecture lint and reviewer
gate, then finalize.
