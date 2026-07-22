---
name: Athena M31 Governed Engineering Model Authoring
type: architecture-spine
purpose: build-substrate
altitude: milestone-to-epics
paradigm: semantic-intent-driven-authoring
scope: graphical-first engineering model authoring, capability discovery, semantic transaction, revision-bound preview, multi-sheet projection, persistence, and product proof
status: final
created: '2026-07-21'
updated: '2026-07-21'
binds:
  - M31 PRD FR-1..FR-51
  - M31 PRD NFR-1..NFR-13
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-21-m31/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m29/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
  - kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt
  - kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringPreviewModels.kt
  - kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt
  - ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringSourceEditProtocol.kt
  - ide/theia-frontend/src/browser/athena-graph-workbench-model.ts
companions:
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/AUTHORING-CONTRACT.md
---

# Architecture Spine - Athena M31 Governed Engineering Model Authoring

## Design Paradigm

M31 uses semantic-intent-driven authoring:

```text
Human / API / Workflow / Future Agent
  -> Semantic Capability Registry
  -> Interaction IR / Semantic Action Intent
  -> Semantic Authoring Transaction
  -> Authoring Runtime and revision-bound preview/validation
  -> Mutation Authority
  -> Canonical semantic persistence
  -> Engineering Semantic Model
  -> Document Projection / Representation Binding
  -> Composition / Spatial / Routing
  -> Presentation IR / Frontend Adapter
```

The user starts graphically, while authority remains semantic and source-backed.

## Inherited Invariants

| Inherited | Source | Binds M31 |
| --- | --- | --- |
| Mutation Authority owns accepted source change | M8 architecture | Authoring accept and persistence |
| Spatial intent precedes layout and routing | M27 architecture | Composition Target and reprojection |
| Interaction meaning is platform-owned | M29 AD-1..AD-15 | Action discovery, lifecycle, provenance, reveal |
| Representation policy and binding precede paint | M30 AD-1..AD-14 | Entity projection, symbols, anchors, composition |

## Invariants And Rules

### AD-1 - Graphical-First Does Not Transfer Authority [ADOPTED]

- **Binds:** FR-1..FR-6, NFR-1, NFR-2
- **Prevents:** canvas state becoming engineering truth.
- **Rule:** Graphical View may initiate Authoring Intent and display preview. Canonical identity,
  source edits, engineering meaning, persistence, and final projection remain backend-owned.

### AD-2 - Authoring Extends Existing Capability And Interaction IR [ADOPTED]

- **Binds:** FR-1..FR-4, FR-13, FR-31, NFR-5
- **Prevents:** a second Theia-only command model.
- **Rule:** Domain providers add typed Authoring Capability entries to M29's existing
  SemanticCapabilityRegistry. All M31 actions are discovered through that registry and represented
  through Interaction IR and Semantic Action Intent before transaction creation. No second
  capability registry is allowed.

### AD-3 - Every Mutation Has A Revision-Bound Preview [ADOPTED]

- **Binds:** FR-4..FR-6, FR-10..FR-12, FR-14, FR-18, FR-32, NFR-4
- **Prevents:** stale or opaque source changes.
- **Rule:** A mutable action cannot be accepted without a preview validated against the exact active
  Revision Guard: semantic snapshot id plus target source URI, LSP document version, and SHA-256 of
  UTF-8 document content. Any mismatch blocks acceptance without source change.

### AD-4 - Mutation Authority Is The Only Write Path [ADOPTED]

- **Binds:** FR-7..FR-18, FR-33..FR-35, NFR-1, NFR-2
- **Prevents:** frontend-generated or competing persistence paths.
- **Rule:** Backend authoring protocol plans and serializes validated source edits, then submits the
  semantic mutation through existing Mutation Authority. Theia may apply a returned editor edit as
  transport but never computes insertion spans or serializes `.athena` mutation text.

### AD-5 - Proposed Source Validates Before Persistence [ADOPTED]

- **Binds:** FR-8..FR-10, FR-15..FR-18, FR-33, FR-34
- **Prevents:** accepted authoring leaving canonical source syntactically or semantically invalid.
- **Rule:** The complete proposed source must parse and pass semantic validation before persistence.
  `STOP_DOWNSTREAM` blocks the mutation and remains a named diagnostic state.

### AD-6 - Semantic Concept And Representation Definition Stay Separate [ADOPTED]

- **Binds:** FR-7..FR-9, FR-25, FR-26
- **Prevents:** palette symbols or visual assets becoming semantic type authority.
- **Rule:** A generic template contract may live in authoring runtime, while electrical template
  instances belong to the electrical domain extension or its platform registry. Representation
  Policy and Binding Compiler independently select Representation Occurrences after compilation.
  M31 action discovery exposes only concepts with valid semantic and active-projection
  representation capabilities.

### AD-7 - Composition Target Is Intent, Not Geometry [ADOPTED]

- **Binds:** FR-19..FR-24, NFR-1, NFR-7
- **Prevents:** source-level coordinate truth and CAD database drift.
- **Rule:** Preview may request a stable sheet, zone, lane, or alignment context. Existing document
  projection policy resolves final membership from semantic role and projection context after each
  compile; preview context is not independent persistence. Coordinates, routes, and bounds remain
  downstream projection facts.

### AD-8 - Canonical Relationships Remain Flat [ADOPTED]

- **Binds:** FR-13..FR-18
- **Prevents:** grouped source syntax creating a second relationship ontology.
- **Rule:** Flat and grouped `connect` declarations lower to equivalent canonical relationships.
  Group identity is source provenance and organization only. M31 migrates all consumers from
  legacy `ConnectPortsIntent` to `SemanticRelationshipIntent`, then removes the legacy contract.

### AD-9 - Dependency Impact Precedes Removal [ADOPTED]

- **Binds:** FR-11, FR-12, FR-18
- **Prevents:** destructive mutation silently deleting engineering relationships.
- **Rule:** Entity removal preview enumerates dependent relationships and occurrences. M31 blocks
  non-empty dependency removal; automatic cascade is deferred.

### AD-10 - Lifecycle Outcomes Are Not Collapsed [ADOPTED]

- **Binds:** FR-31..FR-35, NFR-8
- **Prevents:** misleading generic errors such as unexplained projection unavailability.
- **Rule:** Requested through reprojected states, stale/blocked/cancelled outcomes, compile stop,
  and projection failure after commit are represented and transported distinctly.

### AD-11 - Stable Identity Crosses Sheets And Reopen [ADOPTED]

- **Binds:** FR-19..FR-24, FR-36, NFR-3
- **Prevents:** frontend-local sheet or occurrence ids breaking reveal and persistence.
- **Rule:** Sheet count, role, order, and identity derive from Document Projection Policy, never
  source file count, widget lifecycle, or DOM identity. The M31 customer-demo policy exposes exactly
  control and field/device roles. Projection Occurrence identity derives deterministically from
  canonical semantic and document inputs.

### AD-12 - Renderer Remains Paint-Only [ADOPTED]

- **Binds:** FR-25..FR-30, NFR-2, NFR-7
- **Prevents:** authoring integration reintroducing generic boxes, wrappers, center routes, or fixed
  canvases.
- **Rule:** Renderer consumes resolved Presentation IR and transient interaction state only. It does
  not infer semantic meaning, choose symbols, route relationships, or persist placement.

### AD-13 - Structured Proof Is Acceptance Authority [ADOPTED]

- **Binds:** FR-37..FR-40, NFR-3, NFR-6
- **Prevents:** screenshot-only acceptance and unverified product claims.
- **Rule:** Model, compiler, mutation, runtime, transport, frontend, and Electron smoke proofs carry
  structured assertions. Screenshots supplement human review only.

### AD-14 - Refactor Replaces Paths Completely [ADOPTED]

- **Binds:** NFR-11, NFR-12
- **Prevents:** repeated milestones accumulating hidden compatibility paths.
- **Rule:** Aggressive refactor is allowed when it restores one authority path. Replaced code,
  tests, docs, samples, and design claims are removed or explicitly ledgered.

### AD-15 - Polish And Purge Is A Story Gate [ADOPTED]

- **Binds:** FR-41, FR-42, NFR-12
- **Prevents:** dead or stale artifacts surviving story completion.
- **Rule:** Every story's final acceptance criterion audits touched and adjacent artifacts. A story
  cannot move to done until stale items are removed or recorded with owner, reason, target
  milestone, and verification.

### AD-16 - Semantic Authoring Transaction Is The Mutation Envelope [ADOPTED]

- **Binds:** FR-45..FR-47, FR-50, NFR-3..NFR-5
- **Prevents:** preview, validation, provenance, decision, and mutation state diverging across human,
  API, workflow, and future agent producers.
- **Rule:** Every mutable action creates one Semantic Authoring Transaction carrying capability
  evidence, intent, Revision Guard, preview, validation, decision, lifecycle, mutation handoff,
  result, diagnostics, and provenance. M31 v0 permits exactly one mutable intent; multi-intent
  transactions return an unsupported diagnostic.

### AD-17 - Capability Discovery Precedes Transaction Creation [ADOPTED]

- **Binds:** FR-48..FR-50
- **Prevents:** frontend or transaction runtime hard-coding which actions a subject supports.
- **Rule:** SemanticCapabilityRegistry derives authoring eligibility from subject or creation
  context, actor policy, domain capability, concept template, projection context, and active
  representation capability. Only discovered eligible actions may create transactions.

### AD-18 - Downstream Projection Artifacts Are Immutable Authoring Outputs [ADOPTED]

- **Binds:** FR-51, NFR-2, NFR-6, NFR-7, NFR-13
- **Prevents:** hidden document editing and geometry becoming a competing truth model.
- **Rule:** Authoring never directly mutates Presentation IR, Representation Occurrence, Projection
  Occurrence, sheet output, layout facts, route facts, or rendered geometry. Accepted semantic
  mutation triggers deterministic downstream re-derivation.

## Dependency Direction

```text
Human Theia Adapter / API / Workflow / Future Agent
  -> Semantic Capability Registry / Interaction transport
  -> Interaction IR / Semantic Authoring Transaction / Authoring Runtime
  -> Mutation Authority / compiler
  -> Engineering Semantic Model / projection facts
  -> Representation binding / composition / spatial intent
  -> Presentation IR
  -> Theia Adapter
```

No downstream layer may become an authority for an upstream layer.

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Canonical ids | Use semantic ids and stable projection occurrence ids; adapter ids are metadata only. |
| Revision guard | Every mutable preview and accept carries semantic snapshot id, source URI, document version, and UTF-8 SHA-256. |
| Diagnostics | Stable code, authority, lifecycle stage, subject id, message, and source range where available. |
| Lifecycle | Use M29 lifecycle vocabulary plus `recompiled`, `reprojected`, and `projection-failed`. |
| Transaction | One mutable intent per M31 transaction; multi-intent input is explicitly unsupported. |
| Capability | Extend M29 SemanticCapabilityRegistry; never add a parallel authoring registry. |
| Composition | Resolve membership through document projection policy; never persist preview state or arbitrary coordinates. |
| Evidence | Each story maps every acceptance criterion to fresh command or product evidence. |
| Cleanup | Remove stale artifacts or add a complete cleanup-ledger entry before done. |

## Structural Seed

```text
kernel/
  interaction-model/       # existing capability registry, Semantic Action Intent, interaction contracts
  authoring-model/         # transaction, generic authoring intents, previews, decisions, diagnostics
  runtime/                 # transaction session, preview, validation, lifecycle, mutation handoff
  compiler/                # semantic validation, source edit planning, recompile integration
  representation-model/   # existing M30 policy, definitions, occurrences, composition intent

ide/
  lsp/                     # transport-safe authoring preview/result payloads
  theia-frontend/          # graphical action, preview, inspector, reveal adapters

extensions/
  domain-electrical/       # electrical Engineering Concept Template instances

examples/
  m31/sample-project/      # two-sheet customer authoring proof

_bmad-output/
  implementation-artifacts/m31/
```

Exact file and module splits follow existing code after inspection. Dependency direction and
authority ownership do not change.

## Capability To Architecture Map

| Capability | Lives in | Governed by |
| --- | --- | --- |
| Action discovery and lifecycle | Interaction model/runtime | AD-1, AD-2, AD-10, AD-17 |
| Semantic authoring transaction | Authoring model/runtime | AD-3, AD-16 |
| Revision-bound preview | Authoring runtime/compiler | AD-3, AD-5, AD-16 |
| Entity and relationship mutation | Mutation Authority integration | AD-4, AD-8, AD-9 |
| Semantic concept creation | Compiler/runtime template support | AD-6 |
| Sheet and composition resolution | Document projection/composition | AD-7, AD-11 |
| Professional occurrence and routing | M30 representation and M27 spatial layers | AD-6, AD-12 |
| Product proof | LSP, Theia adapter, Electron smoke | AD-13 |
| Story cleanup and authority migration | All touched areas and M31 ledger | AD-4, AD-8, AD-14, AD-15 |
| Projection re-derivation | Compiler, document, representation, spatial, presentation | AD-18 |

## Deferred

| Deferred | Reason |
| --- | --- |
| Full undo/redo | M31 proves preview, rejection, and mutation lifecycle; history orchestration is separate. |
| Multi-intent and batch transactions | M31 v0 proves one mutable intent per transaction. |
| Freeform placement and coordinate persistence | Violates composition-intent boundary and is unnecessary for the vertical slice. |
| Complete component library browser | M31 reuses the M30 demo concept and symbol set. |
| Automatic cascade delete | Dependency impact must be proven before adding destructive convenience. |
| QET importer and standards expansion | M31 focuses on authoring integration. |
| Semantic Agent Runtime | Must consume the proven M31 authoring contract in a later milestone. |
| PDF, print, and revision packages | Downstream document release workflow remains separate. |
| Deployment and infrastructure changes | M31 is an in-product authoring milestone and introduces no new service topology. |
