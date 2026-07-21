# Adversarial Seam Review - M29 Architecture Spine

Reviewed artifact: `_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m29/ARCHITECTURE-SPINE.md`

Verdict: **Not ready for independent downstream story execution.** The spine has the right architectural direction, but it does not yet pin enough shared contracts to prevent compliant teams from producing incompatible Interaction IR, lifecycle, transport, mutation, and cleanup implementations.

## Adversarial Construction: Two Compliant But Incompatible Story Teams

### Team A: Kernel-First Interaction Contract Team

Team A reads AD-1, AD-2, AD-3, AD-4, AD-5, AD-6, and AD-10 strictly. They implement:

- `kernel/interaction-model` Kotlin contracts as the canonical source of truth.
- `InteractionSubject` as:

```text
subjectId: StableSemanticIdentity
subjectKind: enum
occurrence: ProjectionOccurrenceContext(viewId, occurrenceId, sourceRevision)
capabilities: List<SemanticCapability>
metadata: InteractionMetadata
```

- `SemanticActionIntent` as a kernel-side producer-neutral request object that wraps reveal, preview, accept, reject, mutate, and create requests.
- `InteractionCommand` as a runtime-owned lifecycle aggregate with legal transitions and a `mutationId`.
- LSP payloads as serialized mirrors of the kernel model, with thin DTO mapping only.
- Relationship mutation as `SemanticRelationshipIntent` only, with `ConnectPortsIntent` reachable only through a compatibility adapter.
- Entity creation as `CreateComponentIntent` because the existing authoring model already has that contract.
- Runtime-owned preview state in Kotlin; Theia only renders preview facts returned from runtime.

This team obeys the spine: contracts live outside Theia, registry is semantic, compiler is derivation-only, accepted commands cross authoring runtime, runtime owns session state, and Theia is an adapter.

### Team B: Transport-First Adapter Integration Team

Team B reads the same ADs pragmatically through the capability map, especially lines assigning LSP payloads to `ide/lsp`, reveal to `ide/theia-frontend`, and relationship cleanup to `ide/theia-frontend`. They implement:

- Minimal `kernel/interaction-model` marker/value types, but the real request/response shapes live in LSP DTOs.
- `InteractionSubject` transport shape as:

```text
canonicalSubjectId: string
kind: string
surface: string
frontendAdapterMetadata: object
sourceRange?: object
diagnosticId?: string
```

- `SemanticActionIntent` as a field inside LSP command payloads, not a first-class kernel lifecycle object.
- Lifecycle as string statuses emitted by compiler/runtime/frontend components, without a single state machine owner.
- Relationship preview state kept in Theia because AD-7 says Theia may render affordances and request commands, and the existing frontend relationship authoring model already has mode and preview types.
- Accepted relationship mutation routed through `SemanticRelationshipIntent`, but older `connect-ports` command names retained as compatibility transport paths and logged in the cleanup ledger.
- Entity creation as a new `SemanticEntityCreationIntent` because AD-8 says "semantic entity creation" but the spine never names `CreateComponentIntent`.

This team also obeys the spine: Theia consumes transported payloads, does not infer canonical subjects from DOM/SVG, accepted mutation reaches backend authority, and retained `connect-ports` paths are ledgered.

### Resulting Incompatibility

Both teams can pass a local reading of the ADs, but their outputs cannot interoperate without rework:

- They disagree on whether kernel models or LSP DTOs are normative.
- They disagree on whether `SemanticActionIntent` is a top-level contract or a command field.
- They disagree on who owns lifecycle transitions and preview invalidation.
- They disagree on the identity key shape for projection occurrences.
- They disagree on whether entity creation maps to existing `CreateComponentIntent` or a new intent.
- They disagree on whether retained `connect-ports` is a compatibility adapter, a transport endpoint, or a ledger-only artifact.

## Findings

1. **Shared data shapes are named but not specified.**

   The spine lists Interaction IR concepts and conventions, but it does not define a minimal canonical schema for `InteractionSubject`, `InteractionAction`, `SemanticActionIntent`, `InteractionCommand`, `InteractionPreview`, `InteractionRevealTarget`, `InteractionDiagnostic`, `InteractionResult`, provenance, metadata, or transport envelopes. The addendum suggests vocabulary, but the spine has no companion contract and `companions: []`. A downstream team can comply by creating any transport-safe shape that includes the right words.

   Required fix: add a contract appendix or companion with required fields, optional fields, type ownership, enum values, versioning, and example serialized payloads for reveal, relationship mutation, and entity creation.

2. **`SemanticActionIntent` is the claimed primitive, but its boundary with Interaction IR and AuthoringIntent is undefined.**

   AD-1 says command-producing flows must carry or derive a `SemanticActionIntent`, while AD-5 says accepted relationship mutation remains `SemanticRelationshipIntent`. The spine never states whether `SemanticActionIntent` wraps `AuthoringIntent`, maps to `AuthoringIntent`, is a sibling to `AuthoringIntent`, or exists only before accepted mutation. This is exactly where human, AI, API, and workflow producers will fork.

   Required fix: define the relationship among `SemanticActionIntent`, `InteractionAction`, `InteractionCommand`, `AuthoringIntent`, `SemanticRelationshipIntent`, `CreateComponentIntent`, and transport commands. Include conversion points and fields that must survive each conversion.

3. **Action lifecycle ownership is ambiguous.**

   AD-6 says Interaction Runtime may track lifecycle and must clear stale transient state. The lifecycle convention lists states, but there is no legal state machine, transition owner, transition trigger, id correlation rule, retry rule, stale-command rule, or accepted-versus-committed distinction. A compiler-first team can centralize transitions in runtime while a frontend integration team can emit status strings from multiple layers and still claim compliance.

   Required fix: define lifecycle as a runtime-owned state machine with allowed transitions, required command/session ids, invalid transition diagnostics, and layer responsibilities for requested, discovered, validated, previewing, accepted, mutation-pending, committed, reprojected, blocked, stale, cancelled, and rejected.

4. **Preview and mutation ownership can split into incompatible implementations.**

   AD-4 says the compiler derives previews, AD-6 says runtime tracks preview state, AD-7 says Theia may show previews, and AD-5 says accepted commands cross mutation authority. The spine does not state whether previews are immutable compiler facts, runtime session facts, LSP payloads, frontend render state, or backend validation products. Existing relationship code already has frontend preview/mode structures and source-impact statements, so a team could keep preview assembly in Theia while another moves it into runtime.

   Required fix: specify preview ownership by phase: compiler derives preview candidates, runtime owns preview lifecycle/invalidation, mutation authority validates accepted mutation, frontend renders only returned preview payloads. Define which layer may generate `sourceImpact` and affected semantic identities.

5. **LSP payload boundaries are too vague to protect adapter independence.**

   AD-7 says LSP/runtime transport carries product-safe Interaction payloads, but "product-safe" is undefined. The spine does not define which kernel fields may cross LSP, what must be redacted or normalized, whether LSP DTOs are generated from kernel contracts, how versions are negotiated, or whether Theia may add adapter-only fields to payloads it later submits back. This leaves a direct path to transport-owned semantics.

   Required fix: define LSP envelope boundaries, DTO ownership, allowed adapter metadata, payload versioning, request/response names, and a rule that adapter metadata is never accepted as canonical input on command accept.

6. **Subject identity and projection occurrence context are under-specified.**

   AD-3 requires canonical semantic subject identity plus projection occurrence context, but the spine does not define occurrence identity, stability across projection refresh, source revision binding, sheet/view scoping, multi-occurrence behavior, or collision handling. `StableSemanticIdentity`-style ids alone are not enough for graph/source/sheet/diagnostic reveal coherence. Two teams can build different occurrence keys and both avoid frontend ids.

   Required fix: define `InteractionSubjectKey` and `InteractionOccurrenceKey` explicitly, including canonical id, subject kind, source context, projection view id, sheet/document id where applicable, occurrence id derivation, and staleness criteria.

7. **The Semantic Capability Registry has no single ownership or refresh contract.**

   The structural seed places compiler derivation in `kernel/compiler`, while the capability map allows the registry in `kernel/compiler` or `kernel/interaction-model` support. The spine says indexes refresh on projection refresh, but not who owns rebuilds, invalidation, action policy, cache identity, active `.athena` context binding, or diagnostics when the registry is stale. This invites multiple registries: compiler-owned, runtime-owned, and frontend-cached.

   Required fix: assign one owner for registry construction and one owner for runtime cache/session binding. Define refresh inputs, cache key, invalidation events, and stale registry diagnostics.

8. **Entity creation mutation ownership is not pinned to an existing authoring contract.**

   AD-8 says entity creation is model-first and AD-5 says accepted entity creation must flow through existing authoring/runtime/source-edit gates, but the spine never names `CreateComponentIntent` or any specific creation intent. Because the current authoring model already contains `CreateComponentIntent`, silence here allows a team to invent `SemanticEntityCreationIntent` and another to reuse `CreateComponentIntent`. Both can obey AD-8 while producing incompatible mutation APIs.

   Required fix: state whether component insertion must use existing `CreateComponentIntent`, a new creation intent, or a specific adapter mapping. Include required nested-port anatomy fields and the source edit authority that serializes them.

9. **Legacy `connect-ports` cleanup has a loophole big enough to preserve the old path indefinitely.**

   The convention says remove, migrate, or ledger each retained `connect-ports` path with owner, reason, and target milestone. AD-5 says `ConnectPortsIntent` is legacy compatibility only. There is no inventory requirement, blocking acceptance criterion, maximum retention scope, compatibility adapter owner, test migration rule, or target condition for deletion. Existing code still includes `ConnectPortsIntent` compatibility and frontend relationship authoring preview/mode types, so "ledgered" can become a compliant excuse for leaving the old interaction path alive.

   Required fix: require an explicit `connect-ports` inventory, classify each path as removed/migrated/compatibility adapter, forbid new story work from calling legacy paths directly, and require tests proving relationship authoring reaches `SemanticRelationshipIntent` through Interaction IR rather than old frontend commands.

10. **Diagnostics are required, but the diagnostic contract is not stable enough for cross-layer assertions.**

   The spine requires structured diagnostics with stable codes for several cases, but does not define namespaces, severity, subject attachment, lifecycle attachment, localization/display separation, source/range attachment, retryability, or whether diagnostics originate in compiler, runtime, mutation authority, or adapter. Story teams can emit different codes for the same failure and still satisfy "structured diagnostics."

   Required fix: define a diagnostic envelope and minimum stable code set for unresolved subject, unsupported action, invalid command state, stale command, mutation-ineligible command, missing reveal target, stale registry, and legacy path rejection.

11. **Reveal/navigation boundaries are insufficient for multi-surface coherence.**

   AD-7 prevents frontend inference, but the spine does not define reveal request/response semantics: one-to-many targets, missing target degradation, source range authority, current sheet behavior, cross-sheet occurrence resolution, Problems linkage, or whether reveal changes selection/focus state. This can produce incompatible reveal behavior while all teams use canonical subject ids.

   Required fix: define `InteractionRevealRequest` and `InteractionRevealResult` payloads, target precedence, partial success behavior, diagnostics, and whether reveal is a command lifecycle participant or a stateless action.

12. **Structured proof is required, but the proof payload inventory is missing.**

   AD-10 correctly prioritizes structured assertions, but the spine does not define the proof payloads that model/compiler/runtime/LSP/frontend tests must emit or consume. Without canonical fixtures, Team A can assert Kotlin state machine objects while Team B asserts LSP JSON and frontend proof logs. Both can pass structured tests without proving the same seam.

   Required fix: add a structured assertion inventory with canonical sample payloads for subject registry, action discovery, reveal, relationship preview, relationship accept, entity creation preview, entity creation accept, stale clearing, and retained legacy path reporting.

## Boundary-Specific Assessment

### Shared Data Shapes

Risk: high. The document names the concepts but leaves the schemas open. The highest-risk missing shapes are `SemanticActionIntent`, `InteractionCommand`, `InteractionSubjectKey`, `InteractionOccurrenceKey`, `InteractionPreview`, LSP envelope, and diagnostic envelope.

### Action Lifecycle Ownership

Risk: high. Lifecycle is vocabulary, not governance. The spine needs a runtime-owned state machine and correlation/id rules before story teams split work.

### Mutation Ownership

Risk: high. Relationship mutation is better pinned than entity creation because `SemanticRelationshipIntent` is named. Entity creation is still vulnerable because no existing creation contract is named. Preview/source-impact generation is also unowned.

### LSP Payload Boundary

Risk: high. "Product-safe Interaction payloads" does not prevent LSP DTOs from becoming the real contract. The spine needs an explicit kernel-to-LSP mapping rule and adapter metadata rejection rule.

### Legacy Connect-Ports Cleanup

Risk: high. The cleanup language is too permissive. Retention ledger without inventory, expiry, ownership, and tests is not cleanup; it is documentation of drift.

## Minimum Spine Tightening Needed Before Story Split

1. Add a companion contract for Interaction IR schemas and canonical JSON/Kotlin examples.
2. Define the `SemanticActionIntent` to `InteractionCommand` to `AuthoringIntent` mapping.
3. Make lifecycle a runtime-owned state machine with legal transitions and stale handling.
4. Assign preview/source-impact generation and invalidation ownership.
5. Define LSP payload envelope, DTO ownership, versioning, and adapter metadata rules.
6. Pin component insertion to an explicit creation mutation contract.
7. Require a `connect-ports` inventory and direct-call ban for new M29 stories.
8. Add structured proof payload fixtures shared by model, compiler, runtime, LSP, frontend, and product smoke tests.
