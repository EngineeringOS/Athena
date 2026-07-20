# M28 Addendum: Governed Component Anatomy And Semantic Relationship Authoring Boundary

## Source Discussion

This addendum preserves the M28 positioning decisions after M27:

```text
M28 should close the authoring loop, not start another visual-only milestone.
M28 should make ports part of authored component anatomy, not scattered top-level declarations.
M28 should author semantic relationships, with electrical connection as the first specialization.
```

M27 intentionally deferred accepted mutation. M28 should now make graphical relationship authoring
real while keeping `.athena` as today's source of truth.

The user also identified a source ergonomics problem:

```athena
device SpareTerminalXT99 {
  type Switch
  model "SPARE-XT"
}

port SpareTerminalXT99.in1 {
  direction in
  signal Digital
}
```

This is legal today, but it is less compact and less straightforward than:

```athena
device SpareTerminalXT99 {
  type Switch
  model "SPARE-XT"

  port in1 {
    direction in
    signal Digital
  }
}
```

M28 should admit the nested form and make it canonical.

## Component Anatomy Boundary

Nested ports are syntax for first-class semantic ports. They are not device property strings.

```text
device D {
  port p { ... }
}
```

must lower to the same canonical identity as:

```text
port D.p { ... }
```

Canonical identity remains:

```text
port:D.p
```

This preserves downstream projection, routing, presentation, document projection, inspector, and
relationship semantics.

## Semantic Relationship Authoring Boundary

M28 must not make "connection" the root architecture. The root authoring concept is:

```text
SemanticRelationshipIntent
```

Electrical connection is the first specialization:

```text
SemanticRelationshipIntent {
  relationshipType = ElectricalConnectionRelationship
  sourceSubject
  targetSubject
  projectionContext
  persistenceTarget
  provenance
}
```

Future specializations should fit without renaming the architecture:

```text
FlowRelationship
ContainmentRelationship
ControlRelationship
CommunicationRelationship
MountingRelationship
DependencyRelationship
```

M28 may still serialize an accepted electrical relationship as existing Athena syntax:

```athena
connect A.p -> B.q
```

That syntax is the electrical specialization's source representation, not the platform-wide
interaction model.

## Language Surface Blast Radius

The nested port refactor must move through all language surfaces together:

- ANTLR4 grammar
- ANTLR parse adapter
- Athena authored AST model
- compiler lowering
- semantic declaration indexer
- reference linker
- LSP navigation and diagnostics
- Tree-sitter grammar
- Tree-sitter corpus tests
- generated Tree-sitter parser/node types/WASM if applicable
- highlight queries
- parser parity fixtures
- source mutation tests
- examples and usage docs

Do not admit nested ports in only one parser.

## Legacy Top-Level Port Policy

M28 policy:

- Keep `port Device.port { ... }` accepted as temporary legacy-compatible syntax.
- Make nested ports the canonical syntax for new examples, docs, and source mutation output.
- Add diagnostics/docs that nudge authors toward nested syntax.
- Decide later whether M29 or a cleanup milestone removes top-level ports.

Reason:

- This avoids converting every historical fixture in the same story.
- It lets M28 focus on parser parity plus relationship authoring.
- It preserves current examples while improving the source style going forward.

If the team chooses hard removal later, that milestone must migrate all remaining fixtures, tests,
docs, and mutation assumptions in one controlled sweep.

## Core Boundary

Avoid:

```text
canvas drag wire -> SVG line -> inferred connection -> saved graph state
```

Use:

```text
compact nested component anatomy
    -> canonical semantic subjects
    -> relationship candidate discovery
    -> compatibility check
    -> transient route preview for electrical specialization
    -> governed SemanticRelationshipIntent
    -> M8 mutation authority
    -> canonical semantic persistence
    -> .athena source serialization today
    -> compiler revalidation
    -> projection refresh
```

## Relationship To M8

M8 already proved that graph-originated changes should converge on the runtime-owned mutation
authority. M28 should not invent a second authoring model.

The M28 command should extend the existing mutation intent vocabulary with a narrow generic
relationship intent and one electrical specialization:

```text
SemanticRelationshipIntent {
  relationshipType
  sourceSubject
  targetSubject
  projectionContext
  persistenceTarget
  provenance
}
```

Theia sends intent. M8 authority decides whether and how canonical semantic persistence changes
today's `.athena` source.

## Canonical Semantic Persistence Boundary

Use this wording:

```text
canonical semantic persistence
```

Do not frame M28 architecture as "source write-through." `.athena` serialization is today's
implementation because `.athena` remains Athena's current source of truth. The architecture should
leave room for a future semantic database, Git semantic object, or collaborative graph store without
renaming the authoring contract.

## Relationship To M27

M27 preview remains the upstream interaction proof:

- semantic subject identity
- compatibility preview
- route preview for electrical relationships
- route quality
- compact visual disclosure

M28 changes the final step:

```text
preview only -> preview + governed accept mutation
```

M28 must keep M27 visual guardrails:

- tight SVG scene bounds
- no A3 publication-size viewBox leak
- no visible sheet/frame wrapper borders in normal state
- verbose route labels deferred by default
- selector persists across projection mode switches

## Relationship To M18/M23 Parser Policy

M18 and M23 established that language admission must happen across compiler parser and Tree-sitter
syntax UX together. M28 nested ports are a deliberate language admission, so the implementation must
update both ANTLR4 and Tree-sitter, plus parser parity fixtures and LSP behavior.

Tree-sitter remains syntax UX only. It does not become semantic authority for component anatomy.

## Mutation Target Rule

The first implementation should be conservative:

- If both subjects have a deterministic owning source/system, insert one `connect` statement there.
- If ownership is ambiguous, reject mutation with a diagnostic.
- If subjects span source units and no deterministic policy exists, reject mutation for M28.
- If source is dirty or invalid and M8 cannot safely apply the edit, reject mutation for M28.

This keeps M28 from becoming a general source transformation engine.

## Source Serialization Rule

M28 source mutation should preserve existing source style when only adding a `connect` statement. If
M28 creates or rewrites component anatomy, it should prefer nested ports.

The first accepted relationship flow should not require adding a missing port. The sample should
already contain unconnected nested ports so M28 can focus on relationship insertion.

## UI Interaction Rule

Relationship mode should be explicit. A user should not accidentally create relationships through
ordinary selection.

Acceptable initial patterns:

- toolbar toggle: "Relate" or domain-specific "Connect"
- context menu: "Start relationship from this subject"
- keyboard-modified subject selection

Recommended first proof: toolbar toggle plus endpoint click sequence because it is easiest to test
and explain. The UI may use the domain label "Connect" for electrical authoring, but the underlying
contract remains semantic relationship authoring.

## Compatibility Rule

The M28 electrical compatibility model should stay minimal:

- relationship type
- direction
- signal type
- endpoint role
- duplicate/existing relationship conflict
- component/family support
- source persistence eligibility

Do not expand M28 into full standards intelligence.

## Acceptance Rule

A relationship is not accepted when the preview line appears.

A relationship is accepted only when:

1. mutation authority returns accepted
2. source text contains the new governed relationship serialization
3. compiler validates
4. projection refresh contains the new route fact for the electrical specialization
5. Theia renders the committed route from refreshed facts

## Semantic Interaction Boundary

M28 may implement enough Theia interaction to prove relationship authoring. It must not make Theia
the durable owner of interaction semantics.

Reserve M29 for:

```text
Semantic Model
    -> Interaction Intent
    -> Semantic Interaction Compiler
    -> Interaction IR
    -> frontend adapters: Theia, Web, 3D, VR, CLI, AI agent
```

M28 should leave this boundary documented and unblocked.

## Testing Rule

M28 must have both:

- source/runtime/LSP mutation tests
- Theia product-path smoke

The smoke must prove:

- one valid electrical relationship accepted
- two invalid relationships rejected
- source changed only for the accepted relationship
- projection refreshed after accepted mutation
- preview state did not leak after refresh

## Deferred After M28

M28 should defer:

- component insertion workflows
- macro insertion
- AI-suggested wiring
- standards-complete relationship rules
- graphical source conflict resolution
- full Semantic Interaction Compiler and Interaction IR
- multi-user edit merging
- cabinet/harness/3D authoring
- freehand route editing
