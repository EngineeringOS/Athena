---
title: Athena M28 - Governed Component Anatomy And Semantic Relationship Authoring Foundation
status: draft
created: 2026-07-21
updated: 2026-07-21
---

# PRD: Athena M28 - Governed Component Anatomy And Semantic Relationship Authoring Foundation

## 0. Document Purpose

M28 follows M27 by making the authored source shape match the engineering model, then closing the
authoring loop that M27 deliberately left as preview-only.

M24 proved semantic route facts. M25 proved governed symbol and presentation policy. M26 proved
semantic document projection. M27 proved professional sheet visual acceptance, compact linework,
semantic spatial intent, route quality, and transient semantic relationship preview.

M28 first fixes a language ergonomics problem: authored ports currently live as top-level
`port Device.port` declarations even though they are part of a device's component anatomy. M28 makes
nested device-owned ports the canonical authoring form:

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

The canonical semantic identity remains unchanged:

```text
port:SpareTerminalXT99.in1
```

After that language refactor, M28 proves that Athena can create a real engineering relationship
from the Graphical View without becoming CAD. Electrical connection is the first specialization of
the broader relationship-authoring contract:

```text
authored nested component anatomy
    -> canonical semantic subjects
    -> discover compatible semantic relationship candidates
    -> create SemanticRelationshipIntent
    -> specialize as ElectricalConnectionRelationship for M28
    -> preview relationship, route, diagnostics, and source impact
    -> accept through M8 mutation authority
    -> persist through canonical semantic persistence
    -> serialize to .athena source today
    -> recompile
    -> reproject as normal route facts and presentation facts
```

M28 is not just syntax cleanup, and it is not a freehand wire drawing milestone. The core thesis is:

```text
Athena source should read like engineering anatomy.
Athena does not let users draw wires.
Athena lets users author governed semantic relationships.
Electrical connection is the first proof, not the architectural ceiling.
The canvas previews and explains; mutation authority persists canonical engineering truth.
```

## 1. Vision

An engineer should author ports where they belong: inside the device that owns them. Then the same
engineer should be able to work from the professional M27 Graphical View, select semantic subjects
or visible endpoint occurrences, understand which relationships are valid, preview the resulting
electrical connection route, and accept the relationship as a source-backed engineering change.

The target authority chain is:

```text
.athena source with nested component anatomy
    -> compiler semantic model
    -> projection/document/symbol/spatial/routing facts
    -> Theia Graphical View selection and preview
    -> SemanticRelationshipIntent
    -> ElectricalConnectionRelationship specialization
    -> M8 mutation authority
    -> canonical semantic persistence
    -> .athena serialization today
    -> compiler revalidation
    -> normal projection refresh
```

The Graphical View may help the user choose and preview, but it must never become the source of
relationship truth.

M28 should make Athena feel like an engineering authoring system for the first time:

```text
from "I can inspect a professional semantic sheet"
to   "I can author compact component anatomy and safely author a semantic relationship from that sheet"
```

## 1.1 Why Now

Before M28, Athena has the pieces needed for safe graphical semantic relationship authoring:

- M8 provides the mutation authority and shared source/graph mutation vocabulary.
- M14/M15 provide component knowledge and guided semantic authoring foundations.
- M24 provides route facts and terminal-anchor routing.
- M25 provides presentation terminals, labels, and professional symbols.
- M26 provides document projection and sheet occurrence context.
- M27 provides semantic spatial intent, compact visual disclosure, and relationship preview.

If Athena delays authoring further, the product remains a high-quality viewer. If it implements
canvas wire drawing now, it breaks the EngineeringOS principle and recreates the CAD trap.

M28 should therefore first make component anatomy compact and straightforward, then make electrical
relationship authoring real through governed semantic mutation.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to verify that Athena can create a new semantic relationship from Graphical View
  without breaking `.athena` as today's source of truth.
- Maya needs to author a device and its ports in one compact block instead of scattering device
  anatomy across top-level declarations.
- Maya needs to select real terminals on a professional sheet, preview a valid electrical
  relationship, and accept a safe connection without typing source manually.
- Priya needs a demo that proves Athena is becoming an engineering authoring system, not only a
  semantic graph renderer.
- Winston needs the mutation path to preserve the authority chain: Theia proposes intent, runtime
  governs mutation, compiler validates, projection refreshes.

### 2.2 Non-Users

- Users expecting freehand wire drawing
- Users expecting graphical placement and wiring to persist as CAD geometry
- Users expecting automatic AI-generated wiring
- Users expecting complete EPLAN-style page macros or device insertion workflows
- Users expecting top-level `port Device.port` to remain the preferred authoring style forever
- Users expecting full IEC/company standards enforcement
- Users expecting cabinet, harness, 3D, or physical routing mutation
- Users expecting Theia to save `.athena` source directly

### 2.3 Key User Journeys

- **UJ-0. Maya authors compact component anatomy.**
  - **Context:** Maya creates or edits a device in `.athena` source.
  - **Path:** She writes ports inside the owning `device` block.
  - **Climax:** Athena parses, highlights, lowers, indexes, and navigates the nested ports as
    first-class port declarations.
  - **Resolution:** The semantic identity remains `port:Device.port`, but the source is easier to
    understand.

- **UJ-1. Maya authors a valid electrical relationship from the sheet.**
  - **Context:** Maya opens `examples/m28/sample-project` in Theia Graphical View.
  - **Path:** She activates relationship mode, selects or focuses an output terminal and a
    compatible input terminal.
  - **Climax:** Athena shows an `ElectricalConnectionRelationship` candidate as valid and previews
    the projected route.
  - **Resolution:** Maya accepts. Athena updates `.athena` through governed mutation, recompiles,
    and the new route appears as normal projection output.

- **UJ-2. Maya rejects an invalid relationship before persistence.**
  - **Context:** Maya selects two incompatible subjects, such as output-to-output or mismatched
    signal roles.
  - **Path:** Athena validates the proposed relationship before mutation.
  - **Climax:** The preview is blocked with a governed diagnostic.
  - **Resolution:** `.athena` source is unchanged, the graph remains clean, and the inspector
    explains the rejection.

- **UJ-3. Aaron reviews the persisted source change.**
  - **Context:** Aaron accepts a new graphical relationship.
  - **Path:** He checks source, Problems, Semantic SCM, and Graphical View.
  - **Climax:** The accepted mutation appears as a source-backed `connect A.p -> B.q` change for
    the electrical specialization.
  - **Resolution:** Source, graph, inspector, and semantic review all agree on canonical semantic
    identity.

- **UJ-4. Winston audits the boundary.**
  - **Context:** Winston reviews the implementation.
  - **Path:** He traces semantic subject selection, relationship intent, mutation, source
    serialization, recompile, and projection refresh.
  - **Climax:** He confirms Theia never owns hidden wire truth and does not infer relationship
    meaning from SVG geometry.
  - **Resolution:** M28 advances graphical authoring without becoming CAD.

## 3. Glossary

- **Semantic Relationship Authoring** - A governed workflow for creating a source-backed
  engineering relationship from selected semantic subjects.
- **Semantic Subject** - A canonical engineering entity or endpoint that may participate in one or
  more relationships. M28 subjects include ports and presentation terminals resolved to ports.
- **SemanticRelationshipIntent** - A domain-neutral mutation request describing candidate
  relationship authoring between canonical semantic subjects.
- **ElectricalConnectionRelationship** - The M28 specialization of `SemanticRelationshipIntent`
  that serializes to existing Athena `connect A.p -> B.q` syntax.
- **Component Anatomy** - The authored device-owned structure that includes device fields and
  nested port declarations.
- **Nested Port Declaration** - A `port name { ... }` block authored inside a `device` block. It is
  source syntax for a first-class port owned by that device, not a device property string.
- **Canonical Port Identity** - The downstream semantic identity for a port, still expressed as
  `port:Device.port` regardless of whether the port was authored nested or in legacy top-level form.
- **Relationship Mode** - A Graphical View interaction state in which selections are treated as
  candidate relationship authoring intent, not ordinary inspection selection.
- **Endpoint Candidate** - A selectable terminal or port occurrence resolved to canonical semantic
  identity and source location when available.
- **Compatibility Check** - Validation of relationship type, direction, signal type, endpoint role,
  component family, existing conflicts, package/source ownership, and mutation eligibility before
  persistence.
- **Relationship Preview** - A transient route and diagnostic preview generated from semantic
  identity, M27 spatial intent, route facts, and presentation context.
- **Mutation Authority** - The M8 runtime/LSP/source mutation boundary that owns accepted changes.
- **Canonical Semantic Persistence** - The authority path that persists an accepted semantic
  mutation. M28 implements this by serializing to `.athena`, because `.athena` is today's canonical
  source of truth.
- **Source Serialization** - The concrete M28 implementation detail that writes accepted semantic
  persistence back to `.athena` text.
- **Semantic Interaction Boundary** - The reserved M29 boundary for Interaction Intent,
  Interaction IR, selection, hover, preview, commands, and frontend adapters. M28 keeps interaction
  behavior narrow and does not make Theia the long-term owner of interaction semantics.
- **Post-Mutation Projection Refresh** - The normal compiler/runtime/presentation update after a
  source-backed mutation, proving the canvas is no longer showing preview state.

## 4. Features

### 4.1 Nested Component Anatomy Syntax

**Description:** M28 admits nested device-owned port declarations as the canonical compact source
form for component anatomy.

#### FR-1: Admit nested port declarations inside device blocks

Athena supports `port <name> { ... }` declarations inside a `device` block.

**Consequences:**

- ANTLR4 grammar accepts nested port declarations inside `device`.
- Tree-sitter grammar, corpus tests, node types, highlight queries, and generated parser artifacts
  accept and expose the nested form for syntax UX.
- The authored AST represents nested ports without turning them into device property assignments.
- Nested ports remain first-class syntax declarations with spans, fields, diagnostics, and
  navigation identity.

#### FR-2: Preserve canonical port identity across nested syntax

Nested `device D { port p { ... } }` lowers to the same canonical semantic identity as legacy
`port D.p { ... }`.

**Consequences:**

- Canonical identity remains `port:D.p`.
- Electrical relationship endpoints continue to use `D.p`.
- Existing projection, routing, presentation, document projection, and inspector code should not
  need to learn a second port identity scheme.
- Source provenance must point to the nested `port` block and nested port name span.

#### FR-3: Make nested syntax the canonical authoring style

M28 examples, generated source mutations, usage docs, and new tests use nested ports as the default
source style.

**Consequences:**

- `examples/m28/sample-project` uses nested ports.
- M28 graphical relationship acceptance serializes new source in the nested style when it needs to
  add or preserve component anatomy.
- Docs explain that top-level ports are legacy-compatible only if retained.
- Completion/diagnostic wording should guide authors toward nested ports.

#### FR-4: Define the legacy top-level port policy

M28 keeps `port Device.port { ... }` accepted as a temporary compatibility path while making nested
ports canonical for all new examples, source mutation output, and docs.

**Consequences:**

- Existing M0-M27 fixtures do not have to be rewritten in the same story that admits nested ports.
- M28 includes a cleanup/migration story to update milestone samples where practical.
- Duplicate nested and top-level declarations of the same `Device.port` must produce a governed
  duplicate-identity diagnostic.

### 4.2 M28 Sample Project

**Description:** M28 starts from a sample designed to prove one successful governed electrical
relationship and multiple rejected relationship attempts.

#### FR-5: Provide an openable M28 sample project

Athena provides `examples/m28/sample-project` with admitted `.athena` syntax and a professional
document/sheet projection suitable for semantic relationship authoring.

**Consequences:**

- The sample opens through the normal Athena Theia IDE path.
- The sample contains at least one intentionally unconnected compatible endpoint pair.
- The sample contains invalid endpoint pairs for rejection proof.
- The sample contains enough M24-M27 facts to preview route geometry before mutation.
- `.athena` files remain semantic source units, not sheet or page units.
- The sample uses nested device-owned port declarations.

#### FR-6: Preserve M27 visual acceptance baseline

M28 must keep the M27 compact sheet, transparent chrome, tight SVG viewBox, sheet selector, compact
route labels, and route quality guarantees while adding authoring.

**Consequences:**

- M28 does not reopen the M27 visual boundary unless a regression blocks authoring.
- Relationship-mode overlays are transient and visually restrained.
- Normal sheet state remains dense, clean, and free of helper wrapper borders.

### 4.3 Semantic Subject Selection And Intent Creation

**Description:** The user selects real semantic subjects from Graphical View and Athena constructs
a relationship intent from canonical identities.

#### FR-7: Select semantic subject candidates

The Graphical View exposes ports and presentation terminals as selectable relationship candidates.

**Consequences:**

- Candidates resolve to canonical semantic ids, such as `port:ControllerPLC1.do1`.
- Selection uses projection occurrence ids only as downstream context.
- Theia must not use SVG coordinates or DOM text as semantic identity.
- Candidates show enough hover/selection affordance for dense industrial UI without adding
  permanent visual noise.

#### FR-8: Create a governed semantic relationship intent

When the user selects two compatible subjects in relationship mode, Athena creates a
`SemanticRelationshipIntent` with `ElectricalConnectionRelationship` as the M28 relationship type.

**Consequences:**

- The intent contains relationship type, source subject id, target subject id, active projection
  context, and user-facing provenance.
- The intent is inspectable before acceptance.
- The intent is not persisted in Theia state as hidden engineering truth.
- Canceling or changing selection discards the transient intent.
- Future relationship types, such as flow, containment, control, and communication, must fit the
  same contract without renaming the architecture around wires.

### 4.4 Compatibility And Mutation Eligibility

**Description:** Athena validates whether a relationship can be authored before any persistence.

#### FR-9: Validate relationship compatibility

Athena validates relationship type, direction, signal type, endpoint role, component family,
existing conflicts, and semantic ownership for the selected subjects.

**Consequences:**

- Output-to-input compatible electrical endpoints may proceed to preview.
- Output-to-output, input-to-input, signal-mismatch, or unsupported subject pairs are rejected.
- Rejection produces governed diagnostics and inspector detail.
- The UI does not draw an accepted-looking route for invalid relationship pairs.

#### FR-10: Validate persistence eligibility

Athena validates whether the relationship can be serialized into source safely.

**Consequences:**

- The mutation path identifies the owning `.athena` source target for the new `connect` statement.
- Ambiguous source ownership blocks acceptance with an explicit diagnostic.
- Cross-package or cross-source mutation is allowed only if existing M8 authority can serialize it
  deterministically; otherwise it is rejected for M28.
- Dirty/invalid source state blocks acceptance unless the existing mutation authority explicitly
  supports safe application.

### 4.5 Relationship Preview

**Description:** Before mutation, Athena previews compatibility, route geometry, route quality, and
source impact.

#### FR-11: Show transient relationship preview

Compatible electrical relationship pairs produce a transient route preview using M27 semantic
spatial intent and routing contracts.

**Consequences:**

- Preview routes are visually distinct from committed routes.
- Preview routes are not included in persisted route facts.
- Preview routes can show satisfied or degraded route quality.
- Preview disappears on cancel, source reload, projection refresh, or accepted mutation.

#### FR-12: Explain persistence impact before acceptance

The preview explains the source change that would be made by today's `.athena` serialization.

**Consequences:**

- The user can see the proposed `connect A.p -> B.q` form or equivalent semantic summary.
- The preview identifies the target source file when known.
- The preview exposes expected semantic review impact.
- The preview does not require the user to inspect raw JSON or DOM.

### 4.6 Governed Acceptance Through M8 Mutation Authority

**Description:** Accepting a relationship routes through the existing governed mutation authority.

#### FR-13: Accept relationship through mutation authority

When the user accepts a valid preview, Theia sends the semantic relationship intent to the M8
mutation path rather than editing source directly.

**Consequences:**

- Theia does not write `.athena` text.
- The accepted command produces the existing mutation-result vocabulary.
- Source mutation, validation feedback, semantic consequences, and review output remain aligned
  with M8.
- Failed acceptance leaves source unchanged and clears or marks preview state as rejected.

#### FR-14: Serialize accepted electrical relationship into `.athena`

The mutation authority serializes the accepted `ElectricalConnectionRelationship` into admitted
Athena syntax.

**Consequences:**

- The default output is an existing `connect A.p -> B.q` statement unless an architectural review
  proves the syntax cannot represent the accepted case.
- Any source mutation that creates or reshapes component anatomy must prefer nested port syntax.
- M28 does not introduce new relationship syntax by default.
- If new syntax becomes necessary, ANTLR4, Tree-sitter, compiler, LSP, fixtures, docs, and IDE
  behavior must be updated together.
- The source edit is deterministic and preserves formatting conventions used by current examples.

#### FR-15: Recompile and reproject after mutation

After accepted mutation, Athena recompiles and refreshes projection normally.

**Consequences:**

- The new electrical connection appears as committed semantic route facts, not preview state.
- Source, Problems, Semantic SCM, inspector, and Graphical View agree.
- The accepted route uses M24-M27 route facts, terminal anchors, compact labels, and route quality.
- No hidden graph-local relationship remains after refresh.

### 4.7 Diagnostics, Review, And Coherence

**Description:** M28 makes accepted and rejected relationship authoring inspectable and testable.

#### FR-16: Surface compatibility diagnostics

Rejected relationship attempts produce clear diagnostics in the preview surface and inspector.

**Consequences:**

- Diagnostics identify the rejected subjects and reason.
- Diagnostics distinguish semantic incompatibility from persistence-eligibility failure.
- Problems view is used only when the diagnostic is source-backed or governed as project feedback.
- The canvas does not infer diagnostic meaning from drawn route failures.

#### FR-17: Preserve cross-surface semantic coherence

Source, graph, inspector, document sheet selector, Problems, and Semantic SCM remain coherent before
and after mutation.

**Consequences:**

- Selecting the new route reveals canonical source and semantic identities.
- Semantic SCM shows the accepted relationship change.
- The active document sheet remains stable or switches only through governed projection navigation.
- Dirty source and invalid source states are visible to the user.

### 4.8 Acceptance Evidence And Cleanup

**Description:** M28 must be proven through product-path tests and leave no stale M27/M28 claims.

#### FR-18: Add product-path authoring smoke

Athena adds an M28 smoke test that opens the sample, authors an electrical relationship through the
Graphical View, accepts it, and verifies source-backed projection refresh.

**Consequences:**

- Smoke verifies subject selection, compatibility preview, acceptance, source mutation, recompile,
  projection refresh, and committed route rendering.
- Smoke verifies nested port syntax is the authored style in the M28 sample and post-mutation source.
- Smoke verifies invalid relationship attempts do not mutate source.
- Smoke captures screenshot or proof evidence for the accepted route.
- Smoke must not rely on raw DOM text as semantic authority.

#### FR-19: Add structured mutation assertions

Tests assert the mutation result and post-mutation projection at runtime/LSP/frontend seams.

**Consequences:**

- Runtime/LSP tests cover `SemanticRelationshipIntent` and its electrical specialization.
- Language, compiler, and Tree-sitter tests cover nested ports.
- Frontend tests cover relationship mode state and preview cleanup.
- Tests prove accepted relationship goes through mutation authority.
- Tests prove rejected previews leave source unchanged.

#### FR-20: Publish M28 usage, retrospective, and cleanup ledger

M28 publishes usage documentation, retrospective notes, and stale artifact cleanup records.

**Consequences:**

- Docs explain how M28 differs from M27 preview-only behavior.
- Docs explain nested component anatomy syntax and canonical port identity preservation.
- Docs explain semantic relationship authoring and electrical connection as the first specialization.
- Docs state what remains deferred to M29 or later.
- Stale M27 visual failure artifacts are either updated, archived with reason, or removed.
- The final cleanup gate checks stale docs, screenshots, sample references, and design claims.

## 5. Non-Functional Requirements

### NFR-1: Preserve Athena authority chain

M28 must preserve:

```text
.athena source -> compiler semantic model -> runtime/mutation authority -> projection facts -> Presentation IR -> Theia
```

### NFR-2: Theia remains downstream

Theia may select, preview, inspect, request mutation, and render. It must not own source mutation,
relationship truth, route truth, or hidden graph-local engineering state.

### NFR-3: No freehand wire geometry

M28 must not introduce canvas-drawn wire geometry as persisted truth. Route geometry remains
projection output.

### NFR-4: One deliberate source syntax admission

M28 introduces one deliberate language admission: nested device-owned port syntax. It uses the
existing `connect A.p -> B.q` syntax for the electrical relationship specialization unless a
reviewed blocker proves otherwise.

### NFR-5: Deterministic mutation and projection

The same relationship intent under the same source state must produce the same source edit,
mutation result, semantic review, and projection refresh.

### NFR-6: Product-path proof is required

M28 cannot be accepted with unit tests only. It needs product-path Theia smoke evidence for at least
one accepted electrical relationship and at least two rejected relationship attempts.

### NFR-7: Dirty/invalid source safety

Relationship acceptance must not silently mutate dirty or invalid source states unless the existing
mutation authority explicitly supports safe deterministic application.

### NFR-8: Internal refactor is allowed, authority drift is not

Athena is not live, so M28 may refactor internal APIs aggressively when that improves the long-term
architecture. The cleanup gate must remove stale paths and docs after the new path is verified.

### NFR-9: Windows verification discipline

Gradle verification commands must run sequentially on Windows.

### NFR-10: Visual density preservation

Relationship mode must not reintroduce M27 visual regressions: hard-coded large SVG viewBox,
visible wrapper borders, crowded route labels, or stale sheet selector behavior.

### NFR-11: Parser parity preservation

Nested port syntax must be admitted through ANTLR4 and Tree-sitter together, with parser parity,
highlighting, LSP navigation, diagnostics, and source span coverage.

### NFR-12: Semantic interaction boundary reservation

M28 may implement Theia relationship-mode behavior, but it must name and reserve the future
Semantic Interaction Compiler boundary for M29. Theia interaction code must not become the durable
architecture for selection, hover, preview, commands, undo, redo, or AI/CLI interaction.

## 6. Non-Goals

M28 does not include:

- freehand wire drawing
- CAD geometry persistence
- automatic AI wiring
- full graphical component insertion workflow
- drag/drop symbol placement expansion beyond what existing projection paths support
- standards-complete IEC/company rule packs
- QElectroTech `.elmt` import
- PDF export or print workflow
- cabinet, harness, 3D, or physical routing
- production ELK/libavoid/yFiles backend adoption
- full multi-user collaboration or revision workflow
- new `.athena` relationship syntax unless all language surfaces are upgraded together
- changing canonical port identity from `port:Device.port`
- treating nested ports as generic device properties
- implementing the full Semantic Interaction Compiler or Interaction IR

## 7. Success Metrics

- **SM-1:** The M28 sample opens in Theia Graphical View without projection errors.
- **SM-2:** Nested `device { port ... }` syntax parses through ANTLR4 and Tree-sitter.
- **SM-3:** Nested ports lower to canonical `port:Device.port` identities with correct source
  provenance.
- **SM-4:** The M28 sample uses nested ports as canonical authoring style.
- **SM-5:** A user can select compatible semantic subjects and see a governed relationship preview.
- **SM-6:** Accepting the preview creates a source-backed `connect` statement through mutation
  authority.
- **SM-7:** Recompile/reprojection shows the new committed route as normal route facts.
- **SM-8:** At least two invalid relationship pairs are rejected before source mutation.
- **SM-9:** Rejected attempts leave `.athena` source unchanged.
- **SM-10:** Source, Problems, Semantic SCM, inspector, and Graphical View agree after accepted
  mutation.
- **SM-11:** The product-path smoke verifies accepted and rejected relationship flows.
- **SM-12:** M27 visual guarantees remain protected: tight viewBox, transparent chrome, compact
  labels, stable sheet selector, no route/body intersections in accepted proof.
- **SM-13:** Final cleanup removes or records stale M27/M28 docs, screenshots, and implementation
  paths.
- **SM-14:** The M28 architecture uses `SemanticRelationshipIntent` as the generic authoring
  contract and treats `ElectricalConnectionRelationship` as the first specialization.
- **SM-15:** M28 documents the reserved M29 Semantic Interaction Compiler boundary without
  implementing it prematurely.

## 8. Risks And Mitigations

### Risk 1: Canvas wire drawing sneaks in

**Risk:** The implementation stores route geometry or relationship state in Theia as hidden truth.

**Mitigation:** Require every accepted relationship to round-trip through mutation authority,
source serialization, compiler validation, and projection refresh before it is considered committed.

### Risk 2: Language refactor breaks parser parity

**Risk:** ANTLR4 accepts nested ports but Tree-sitter, highlighting, LSP, or examples drift.

**Mitigation:** Treat nested port admission as a full language-surface story: ANTLR4, AST adapter,
Tree-sitter grammar/corpus/generated artifacts, highlights, compiler lowering, LSP navigation,
fixtures, docs, and product smoke move together.

### Risk 3: Source serialization becomes too broad

**Risk:** M28 expands into a general source-editing engine.

**Mitigation:** Limit source mutation to one governed electrical relationship insertion path and
reject ambiguous ownership.

### Risk 4: Visual mode pollutes dense canvas

**Risk:** Relationship mode adds permanent overlays, large hitboxes, or labels that undo M27
density.

**Mitigation:** Keep overlays transient, inspectable, and visually restrained; keep M27 smoke
guards.

### Risk 5: Compatibility rules become standards-complete too early

**Risk:** Direction/signal validation expands into full IEC/company rule enforcement.

**Mitigation:** M28 validates the minimal compatibility needed for safe electrical relationship
authoring and defers full standards intelligence.

### Risk 6: Dirty-source behavior corrupts files

**Risk:** Graph acceptance applies edits into stale or invalid source buffers.

**Mitigation:** Block acceptance unless M8 mutation authority can prove deterministic application
against the active source state.

### Risk 7: ECAD vocabulary hardens into platform architecture

**Risk:** `ConnectPortsIntent` becomes the root authoring model and makes future flow,
containment, communication, and control relationships feel like exceptions.

**Mitigation:** M28 architecture uses `SemanticRelationshipIntent` as the generic boundary and
keeps electrical connection as a specialization.

## 9. Open Questions

1. Should `device` blocks allow properties and nested ports in any order, or require properties
   before ports for readability?
2. Should duplicate nested and top-level declarations of the same `Device.port` be a hard error or
   duplicate-identity diagnostic?
3. Should M28 mutation target the current active source file only, or may it insert into another
   deterministic owner file when the selected subjects come from different source units?
4. Should the first relationship mode be toolbar-driven, context-menu-driven, or keyboard-modified
   subject selection?
5. What are the exact two invalid electrical relationship cases required for acceptance proof?
6. Should accepted mutation immediately open/reveal the inserted `connect` statement in source?
7. Should source formatting preserve current local section order, or append new `connect`
   statements to the end of the owning `system` block?

## 10. Recommended MVP Cut

M28 MVP should implement:

```text
one sample project
one nested component anatomy syntax admission
one parser parity and highlighting proof
one relationship mode
one valid compatible electrical endpoint pair
two invalid electrical relationship pairs
one source-backed connect insertion
one post-mutation projection refresh
one product smoke proof
one cleanup gate
```

Do not add component insertion, AI authoring, standards packs, or any additional syntax until this
loop is boringly reliable.

## 11. Expected Next Artifacts

After PRD approval:

1. Create `ARCHITECTURE-SPINE.md` for M28.
2. Create M28 epics and stories under `_bmad-output/implementation-artifacts/m28`.
3. Create `examples/m28/sample-project`.
4. Implement the authoring path story by story.
5. Run final stale artifact purge before closing M28.

## 12. M29 Direction Reserved

M29 should likely become the Semantic Interaction Model / Interaction IR milestone:

```text
Semantic Model
    -> Interaction Intent
    -> Semantic Interaction Compiler
    -> Interaction IR
    -> frontend adapters: Theia, Web, 3D, VR, CLI, AI agent
```

M28 must leave this boundary visible and unblocked, but must not expand into the full interaction
compiler.
