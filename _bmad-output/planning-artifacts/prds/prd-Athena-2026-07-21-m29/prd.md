---
title: Athena M29 - Semantic Interaction Model And Authoring Workflow Foundation
status: draft
created: '2026-07-21'
updated: '2026-07-21'
---

# Athena M29 PRD

## Executive Summary

M29 introduces Athena's first governed **Semantic Interaction Model**. M27 made spatial projection
visually credible. M28 made semantic relationship mutation source-backed. M29 prevents those
features from becoming Theia-specific behavior by moving interaction meaning into an Interaction IR
that Theia consumes as an adapter. Internally, M29 reserves **Semantic Action Intent** as the deeper
primitive so future human, AI, workflow, and API producers can converge on the same action contract.

M29 proves the layer with three narrow consumers:

1. semantic reveal and navigation,
2. M28 semantic relationship mutation cleanup,
3. component insertion proof.

The milestone must not become a broad workflow designer or a full UI redesign. Its purpose is to
make interaction a semantic contract.

## Product Thesis

Athena does not let a frontend own interaction meaning.

```text
Semantic Model / Projection Facts
        ↓
Semantic Capability Registry / Interaction Subject Index
        ↓
Interaction IR
        ↓
Interaction Compiler v0
        ↓
Frontend Adapter
        ↓
Reveal / Preview / Command / Mutation Request
```

M29 should make Theia replaceable at the interaction boundary the same way earlier milestones made
rendering replaceable at the presentation boundary.

## Background

M28 deliberately deferred the full Semantic Interaction Compiler. It proved semantic relationship
authoring, but the relationship mode still lives close to Theia frontend behavior. M28 also retained
legacy `connect-ports` and guided connection paths with a cleanup target of M29.

M29 is the correct next step because Athena now has enough downstream behavior to justify an
interaction layer:

- M22/M23: governed layout and source-backed layout hints,
- M24: route facts and terminal anchors,
- M25: symbol and presentation policy,
- M26: document projection and sheet navigation,
- M27: semantic spatial projection and visual density,
- M28: semantic relationship authoring and source-backed mutation.

Without M29, future work will keep adding interaction behavior directly into Theia and will create
the same kind of frontend authority that Athena has avoided in layout, routing, rendering, and
document projection.

## Goals

- Define an Interaction IR that can represent selection, hover, focus, reveal, preview, command
  discovery, accept, reject, lifecycle, provenance, and mutation handoff.
- Build a Semantic Capability Registry / Interaction Subject Index from canonical semantic
  identities and projection facts.
- Introduce Semantic Action Intent as the frontend- and agent-neutral primitive below interaction
  gestures.
- Introduce an Interaction Runtime boundary for session state, pending commands, lifecycle, and
  provenance.
- Make Theia consume interaction facts and commands rather than infer meaning from DOM text, SVG
  geometry, widget ids, or local graph state.
- Migrate M28 semantic relationship mutation behind the Interaction IR.
- Prove semantic entity creation through Interaction IR and existing mutation authority, with
  component insertion as the first example.
- Preserve M27 visual density and M28 source-backed authoring guarantees.
- Record and clean up stale interaction paths.

## Non-Goals

- Full gesture framework.
- Full undo/redo engine replacement.
- AI interaction planner.
- Multi-user interaction state.
- Generic workflow designer.
- Full component library UX.
- Full source conflict resolution UI.
- Web, 3D, VR, or CLI frontend adapters beyond documented contract examples.
- Removing all legacy `connect-ports` compatibility unless an equivalent Interaction IR path is
  complete and verified.
- New Athena source syntax.

## Users And Stakeholders

- Athena maintainers who need stable interaction boundaries before adding more authoring workflows.
- Engineering users who need predictable selection, reveal, preview, and source-backed authoring.
- Future frontend implementers who should target Interaction IR instead of Theia internals.
- Future AI/agent workflows that need to discover safe semantic actions without screen scraping.

## Functional Requirements

### Epic 1 - Interaction IR Contract

**FR-1:** M29 shall introduce a platform-owned Interaction IR model independent of Theia.

**FR-2:** Interaction IR shall represent canonical subjects, subject roles, surfaces, affordances,
semantic action intents, commands, previews, reveal targets, command lifecycle, diagnostics, and
provenance.

**FR-3:** Interaction subjects shall carry canonical semantic identity plus projection occurrence
context when available.

**FR-4:** Interaction IR shall distinguish durable semantic identity from transient frontend state.

**FR-5:** Interaction IR shall model at least these action families:

- select,
- hover,
- focus,
- reveal,
- preview,
- accept,
- reject,
- mutate.

**FR-6:** Interaction IR shall include diagnostics for unresolved subjects, unsupported actions,
invalid command state, and mutation-ineligible commands.

**FR-7:** Interaction IR shall include a lifecycle model with at least requested, discovered,
validated, previewing, accepted, rejected, mutation-pending, committed, reprojected, blocked, stale,
and cancelled states.

**FR-8:** Interaction IR shall include Interaction Provenance fields for actor, origin surface,
reason, timestamp where available, and confidence where available.

### Epic 2 - Interaction Compiler v0

**FR-9:** M29 shall build a Semantic Capability Registry / Interaction Subject Index from compiled
semantic model and projection facts.

**FR-10:** The subject index shall include components, ports, connections, routes, document sheet
occurrences, reference markers, diagnostics, and source ranges where available.

**FR-11:** The Interaction Compiler v0 shall discover available actions for a selected subject from
facts and capability policy, not from frontend widget structure.

**FR-12:** The compiler shall preserve source/projection/inspector/problem reveal coherence for the
same canonical subject.

**FR-13:** The compiler shall expose product-safe payloads through LSP/runtime transport.

**FR-14:** The compiler shall not mutate source or projection state directly.

**FR-15:** Interaction indexes shall be rebuilt or refreshed on projection refresh and bound to the
current active `.athena` source context.

**FR-16:** Interaction IR shall preserve standard, symbol, and presentation metadata when present,
but shall not create, infer, or own standards meaning.

### Epic 3 - Semantic Reveal And Navigation Consumer

**FR-17:** Selecting a component, port, connection, route, reference marker, or diagnostic shall
resolve to an Interaction subject.

**FR-18:** The user shall be able to reveal the subject in source, graph, inspector, and Problems
where that target exists.

**FR-19:** Reveal operations shall use governed source ranges and projection occurrence facts.

**FR-20:** Missing reveal targets shall degrade with explicit diagnostics rather than guessing.

**FR-21:** Reveal state shall remain stable when switching document sheets or projection modes.

### Epic 4 - M28 Semantic Relationship Mutation Cleanup

**FR-22:** M28 semantic relationship mutation shall become an Interaction command, not a Theia-only
mode.

**FR-23:** Relationship subject selection shall continue to use projection facts and canonical port
identity only.

**FR-24:** Existing `SemanticRelationshipIntent` shall remain the mutation contract for accepted
relationship mutation.

**FR-25:** Legacy `connect-ports` frontend paths shall be removed, migrated, or recorded in a
retention ledger with owner, reason, and target milestone.

**FR-26:** Invalid relationship accepts shall remain blocked at the backend mutation/source-edit
gate.

**FR-27:** Relationship preview shall remain transient and shall clear on cancel, source reload,
projection refresh, or accepted mutation.

### Epic 5 - Semantic Entity Creation Proof

**FR-28:** M29 shall provide one governed semantic entity creation proof through Interaction IR,
with component insertion as the first example.

**FR-29:** The proof shall start from an Interaction action, such as palette action, context action,
or system-level action discovery.

**FR-30:** The insertion flow shall produce an inspectable preview with source impact and affected
semantic identities.

**FR-31:** Acceptance shall flow through existing runtime/mutation authority and return a governed
source edit.

**FR-32:** Generated component anatomy shall use nested ports.

**FR-33:** Recompile/reproject shall show the inserted component in source, semantic inspection,
projection facts, and graph view.

**FR-34:** Component insertion rejection shall leave source and projection state unchanged.

**FR-35:** Component insertion shall be modeled as creation of a semantic entity first; symbol
placement is a projection consequence, not the mutation source of truth.

### Epic 6 - Product Smoke, Retrospective, And Cleanup

**FR-36:** M29 shall include `examples/m29/sample-project`.

**FR-37:** The sample shall prove reveal/navigation, relationship mutation through Interaction IR,
and semantic entity creation proof.

**FR-38:** Theia product smoke shall open the M29 sample and verify structured interaction proof
payloads.

**FR-39:** M29 shall include structured assertions at model, compiler/runtime, LSP, frontend, and
product-smoke seams.

**FR-40:** M29 shall finish with usage docs, retrospective, and cleanup ledger.

## Non-Functional Requirements

**NFR-1:** Interaction semantics must be deterministic for identical source and projection inputs.

**NFR-2:** Theia must remain an adapter and may not persist semantic interaction truth.

**NFR-3:** No DOM text, SVG geometry, CSS class, or widget id may become semantic authority.

**NFR-4:** Interaction payloads must be transport-safe and stable enough for future non-Theia
adapters.

**NFR-5:** Existing M27 visual density guarantees must not regress.

**NFR-6:** Existing M28 source-backed mutation guarantees must not regress.

**NFR-7:** Verification commands must run sequentially on Windows when they invoke Gradle.

**NFR-8:** M29 must produce a cleanup ledger for every removed or retained stale interaction path.

**NFR-9:** M29 must not introduce IEC/QElectroTech/EPLAN-style symbol-library expansion or visual
equivalence work.

**NFR-10:** M29 must preserve the option for future AI, workflow, and API producers to use the same
Semantic Action Intent contract as human frontend adapters.

## Success Metrics

**SM-1:** Same canonical subject can reveal source, graph, inspector, and Problems without DOM text
parsing.

**SM-2:** Semantic relationship mutation uses Interaction IR command discovery and still serializes
through `SemanticRelationshipIntent`.

**SM-3:** Semantic entity creation creates nested-port component source and appears after
recompile/reproject.

**SM-4:** Invalid reveal targets and invalid mutation accepts produce structured diagnostics instead
of guessed behavior.

**SM-5:** M29 product smoke opens the sample and emits structured proof for reveal, relationship,
and component insertion.

**SM-6:** Legacy interaction paths are either removed or documented with owner, reason, and target
milestone.

**SM-7:** M27/M28 product smoke checks continue to pass or their equivalent shared contracts are
explicitly migrated.

## Acceptance Criteria

- `examples/m29/sample-project` exists and opens in Theia.
- Interaction IR contracts exist outside Theia frontend code.
- Interaction subject index can resolve at least component, port, connection, route, sheet
  occurrence, reference marker, diagnostic, and source range subjects.
- Theia graph/source/inspector paths consume Interaction IR for reveal/navigation.
- M28 semantic relationship mutation is routed through Interaction IR.
- Semantic entity creation proof accepts one governed component insertion and rejects one insertion
  without source mutation.
- Generated inserted component uses nested ports.
- Product smoke verifies reveal, relationship, component insertion, and no DOM text authority.
- Usage doc, retrospective, cleanup ledger, and structured assertion inventory are created.
- M29 PRD/addendum explicitly defer IEC/QElectroTech/EPLAN visual/library expansion to a later
  standards/presentation milestone.

## Open Questions

1. Should the first component insertion proof start from palette action, graph context action, or
   system-level command palette action?  
   **Recommendation:** graph/system context action first; palette polish can follow.

2. Should Interaction IR live in a new `kernel/interaction-model` module or inside the existing
   authoring/runtime model first?  
   **Recommendation:** new `kernel/interaction-model` module to keep the boundary visible.

3. Should M29 remove legacy `connect-ports` frontend tests or retain them behind compatibility
   naming?  
   **Recommendation:** migrate relationship authoring tests to Interaction IR and keep only the
   minimum compatibility assertions.

4. Should undo/redo be modeled in M29?  
   **Recommendation:** include lifecycle vocabulary, undoable marker, and mutation id link only; do
   not implement full undo/redo.

5. Should M29 product smoke drive actual UI clicks or structured frontend/LSP proof payloads?  
   **Recommendation:** structured proof first, then UI click smoke only where stable enough.

6. Where should Interaction Runtime live?  
   **Recommendation:** introduce a runtime boundary alongside `kernel/interaction-model`; exact
   module split may be `kernel/runtime` integration first or a later `interaction-runtime` module if
   the codebase warrants it.
