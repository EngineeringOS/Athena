# M42 Architecture Adversarial Divergence Review

## Verdict

**PASS AFTER AUTOFIX.** Constructed independent compiler, runtime, LSP, authoring, plugin, routing,
and frontend units that obey every AD. Two remaining incompatibilities were found and fixed before
this report: transport-shape authority and mixed-revision publication.

## Attacks

### Schema-First Frontend Versus Kotlin-First Runtime

One unit could treat committed JSON Schema as authoritative while another reflected Kotlin DTOs into
a different shape. AD-32 now makes committed schemas authoritative and forbids reflection-derived
schemas and parallel transport DTOs.

### Atomic Runtime Versus Independent Document Refresh

One adapter could publish Knowledge before Validation while another keyed queries to the prior
Engineering snapshot. AD-33 now requires one atomic immutable revision containing all snapshots and
digests; readers cannot observe mixed revisions.

### Authoring Versus Knowledge Ownership

Independent authoring and evaluator units converge: `authoring-model` owns its separate availability
contract without importing definitions; compiler/runtime maps evaluated facts into it. Neither can
reinterpret Capability as classification.

### Plugin Versus Package Definitions

Plugin API loses Component/Connection/Part knowledge contributors and may consume only declared
non-knowledge extension contracts. Package-local Athena source remains sole definition authority; a
plugin cannot create a second definition path.

### Relationship Projection Versus Spatial Routing

Projection receives one resolved multi-participant Relationship group. Spatial alone derives route
legs and geometry. Neither layer can split or create engineering Relationships.

### Canonical Codec Versus Product Consumers

Schema array annotations, key order, escaping, Unicode policy, version rejection, SHA-256 input, and
digest encoding are fixed. Runtime, CLI, LSP, and Theia cannot choose incompatible serialization or
hashing while remaining compliant.

### Project, Part, And Derived Facts

Fact sources remain distinct and must agree after typed normalization. Conflicts are `INVALID`; no
unit may choose source precedence or let Part assignment generate authored anatomy.

## Remaining Deferred Choices

Grammar layout sugar, registry lifecycle, optimization, Pattern/AI, M43 rendering, and distributed
operations can vary without causing M42 story incompatibility because their revisit conditions and
authority exclusions are explicit.
