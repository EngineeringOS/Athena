# PRD Quality Review - Athena M37

## Overall Verdict

M37 is decision-ready after the self-review corrections. Its thesis is coherent: M37 evolves M36
connectivity into authored Connection Intent, governed route-quality policy, Projection Policy, and
professional drawing grammar while preserving Athena source as the only engineering authority.
The original draft carried two high-risk ambiguities, a possible parallel connectivity contract and
continued endpoint-derived intent inference. Code review also found hardcoded proof success and
projection selection in the current professional drawing path. All three are now explicitly
prohibited.

## Decision-Readiness - strong

The PRD now resolves all phase-blocking choices: direct evolution of the M36 contract, intent
precedence, profile ownership of route-quality values, Athena-native planner scope, external evidence
sample scope, and fixed structural visual gates. Section 10 records that no product or architecture
question blocks epic creation.

## Substance Over Theater - strong

The milestone is grounded in an observed product failure and current production behavior. It does
not claim IEC, AML, ECLASS, ELK, EPLAN, or QET parity. External systems remain evidence, importer
inputs, planner inspiration, or visual references. The visible proof is one professional connection
drawing surface.

## Strategic Coherence - strong

Every feature serves one chain:

```text
Athena source
  -> Engineering Connectivity Contract and Connection Intent
  -> transient planning and typed Projection Policy
  -> validated RouteFacts and presentation classes
  -> Graphic Primitive IR
  -> paint-only renderer
```

The counter-metrics prevent optimization complexity, standards ownership, multi-surface spread, and
compatibility work from displacing that chain.

## Done-Ness Clarity - adequate

FR-32 and SM-3 now define zero-defect structural gates for endpoints, anchors, body intersections,
crossings, label collisions, and presentation classification. FR-34 requires screenshot-backed E2E
plus structural assertions. Invalid fixtures separately prove diagnostics. Story acceptance criteria
must preserve these as executable checks rather than replacing them with visual judgment.

## Scope Honesty - strong

The PRD explicitly excludes full ECS, product catalogs, AML/ECLASS resolvers, ELK integration,
remote resources, AI layout, multi-view polish, and renderer inference. M37 introduces only the
connectivity and projection facts needed for the focused proof.

## Downstream Usability - strong

FR IDs are contiguous, terms are defined, success metrics map to feature ranges, and resolved
decisions remove ambiguity before epic decomposition. The direct-refactor targets in `addendum.md`
connect the product contract to current code without turning the PRD into an implementation plan.

## Shape Fit - strong

This is a chain-top brownfield compiler milestone, so capability-first requirements, explicit
authority boundaries, source traces, diagnostics, and E2E proof are the correct shape. Broad personas
or multi-surface journeys would add ceremony without changing decisions.

## Resolved Findings

- **High - Parallel contract risk:** "Engineering Connectivity Contract" could have become a second
  model beside M36 `ConnectableEntityContract`. The PRD now requires a direct rename/evolution and
  forbids adapters, compatibility facades, and dual lowering.
- **High - Hidden intent authority:** the active professional drawing compiler currently derives
  intent through `ElectricalConnectionIntentClassifier`. FR-5 and FR-36 now require authored intent
  and removal of endpoint-derived inference.
- **High - Hidden route-policy authority:** the active drawing compiler injects fixed route
  constraints. The addendum now separates compiler hard rules from profile-authored values and
  requires traceable policy facts.
- **High - Fabricated proof risk:** the active drawing compiler hardcodes successful clearance,
  fallback-absence, and renderer-purity evidence and hardcodes its projection/view selection. FR-34,
  FR-36, and the addendum now require computed evidence and typed Projection Policy selection.
- **Medium - Unresolved policy ownership:** Route Quality Policy ownership is now compiler schema and
  hard rejects plus typed profile values selected by Projection Policy.
- **Medium - Scope expansion through ELK:** M37 now proves only the Athena-native planner; ELK is
  deferred.
- **Medium - Relative visual acceptance:** "more readable than M36" was not testable. FR-32 and SM-3
  now use fixed structural gates.
- **Medium - Evidence ecosystem sprawl:** the valid sample is limited to one IEC citation and one
  neutral external classification reference; AML/ECLASS resolvers remain deferred.
- **Low - Vague performance:** NFR-7 now gives a 10-second post-ready compile-to-presentation bound.

## Mechanical Notes

- Functional requirement IDs are contiguous from FR-1 through FR-36.
- Success metrics reference valid requirement ranges.
- No unresolved assumptions or phase-blocking open questions remain.
- The PRD and addendum consistently use Engineering Connectivity Contract, Connection Intent,
  External Evidence Mapping, Route Quality Policy, Drawing Standard Profile, Connection
  Presentation Class, and Projection Policy.
