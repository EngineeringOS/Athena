# M31 Architecture Adversarial Seam Review

## Verdict

Pass after tightening four seams that could otherwise produce incompatible story implementations.

## Tested Divergence Pairs

### Revision Validation

One story could use an editor counter while another uses a semantic snapshot id. AD-3 now requires
both project semantic snapshot identity and exact target document URI, version, and UTF-8 SHA-256.

### Concept Ownership

One story could put electrical templates in generic runtime while another puts them in the renderer.
AD-6 now places the generic contract in authoring runtime and electrical instances in the electrical
domain extension or platform registry.

### Sheet Membership

One story could derive sheets from source files while another hard-codes frontend tabs. AD-7 and
AD-11 make Document Projection Policy the only sheet membership, count, order, and identity
authority.

### Source Edit Ownership

One story could serialize source in Theia while another uses LSP. AD-4 requires backend planning and
serialization; Theia may only apply returned edits as transport.

### Relationship Compatibility

One story could preserve `ConnectPortsIntent` while another uses generic semantic relationships.
AD-8 requires complete migration to `SemanticRelationshipIntent` and removal of the legacy model.

### Commit And Projection Failure

One story could roll back a valid semantic mutation on renderer failure while another silently
falls back. AD-6 and AD-10 require capability-gated discovery plus a distinct post-commit
`projection-failed` outcome with no silent fallback.

### Capability Ownership

One story could introduce an authoring-only registry while another extends M29 interaction
capabilities. AD-2 and AD-17 require typed authoring capabilities in the existing
SemanticCapabilityRegistry.

### Transaction Scope

One story could implement batch atomicity while another assumes one intent. AD-16 fixes M31 v0 at
exactly one mutable intent and requires an explicit unsupported diagnostic for other cardinalities.

### Hidden Projection Mutation

One story could move an occurrence directly while another reprojects from semantic truth. AD-18
forbids direct mutation of all document, representation, projection, layout, route, and geometry
outputs.
