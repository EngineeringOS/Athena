# M31 Architecture Reality Check

## Verdict

Pass after brownfield alignment. The architecture names current modules and turns known gaps into
explicit M31 work instead of describing them as already solved.

## Evidence

- `kernel/authoring-model` already owns `AuthoringIntent`, component-specific intents,
  `SemanticRelationshipIntent`, and simple preview decisions.
- `kernel/runtime` already owns the authoring preview session but does not yet execute canonical
  mutation commits.
- `kernel/interaction-model` already carries optional source revision data used for stale registry
  diagnostics.
- `kernel/interaction-model` already owns `SemanticCapabilityRegistry`; M31 extends it with typed
  authoring capability rather than creating a new registry.
- No first-class Semantic Authoring Transaction exists yet; AD-16 defines this as M31 work and
  limits v0 to one mutable intent.
- `ide/lsp` already generates governed component and relationship source edits.
- Theia currently applies backend-returned source edits through its editor bridge.
- Graph layout currently contains frontend `.athena` serialization and source-edit construction;
  FR-43 and AD-4 require migration or removal.
- `ConnectPortsIntent` remains as a legacy adapter; FR-44 and AD-8 require migration and removal.
- `DocumentProjectionPolicy` already owns deterministic sheet roles and occurrence identity;
  M31 adds a customer-demo profile with exactly control and field/device roles.

## Version Check

M31 introduces no new external framework, library, service, or deployment topology. No web version
decision is required.
