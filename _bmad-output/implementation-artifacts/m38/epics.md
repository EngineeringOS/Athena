---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/addendum.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-31-m38/review-rubric.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-31-m38/ARCHITECTURE-SPINE.md
---

# Athena M38 Epic Breakdown

## Outcome

M38 makes one promise: every visible engineering connection ends at the exact placed Element Anchor
bound to its Port, and that fact remains traceable to Athena source. Engineers author engineering
meaning. Compiler derives drawing facts. Theia paints and inspects them.

M38 does not add layout, route, paint, label, or renderer syntax. M39 owns placement quality. M40 owns
route quality.

## Requirements Inventory

### Functional Requirements

FR-1: Add no normal-source syntax for coordinates, transforms, bends, line paint, label placement,
renderer behavior, or compiler IR.

FR-2: Compile SVG-backed, native, and composite Elements into one intrinsic geometry contract.

FR-3: Keep local body, bounds, Anchors, hit geometry, label slots, and source/resource trace in that
contract.

FR-4: Keep Port meaning in Athena source and Anchor geometry in representation.

FR-5: Require one explicit, valid Port-to-Anchor binding.

FR-6: Allow neutral package-local SVG geometry references while rejecting SVG engineering metadata.

FR-7: Create one Graphic Occurrence from one Representation Definition and one placement.

FR-8: Apply the same transform once to body, bounds, Anchors, hit geometry, and label slots.

FR-9: Require downstream consumers to use placed coordinates without endpoint reconstruction.

FR-10: Block publication on missing, duplicate, stale, or conflicting placement/Anchor evidence.

FR-11: Resolve every visible Connection through Ports, bindings, Graphic Occurrences, Anchors, and
exact drawing points.

FR-12: Require route first and last points to equal placed Anchor points.

FR-13: Normalize existing route geometry without searching for a better route.

FR-14: Compile explicit line, junction, crossing, bus-tap, continuation, and label facts before paint.

FR-15: Use Presentation Document connectors as the only visible connection path in Theia and SVG
export; publish each document atomically.

FR-16: Trace each endpoint from Connection through Port, binding, Element, Anchor, placement, route,
and Athena source.

FR-17: Provide a dedicated M38 example with SVG-backed and native Elements and explicit bindings.

FR-18: Prove exact attachment, explicit topology, complete trace, and absence of fallback or repair.

FR-19: Capture fresh desktop and narrow Electron screenshots under M38 artifacts.

FR-20: Delete stale anatomy, compatibility, accepted RouteFact, duplicate lowering, fallback,
renderer repair, hardcoded sample policy, milestone production names, and stale tests/docs.

### Non-Functional Requirements

NFR-1: Athena source stays human-first, concrete, AI-friendly, and K.I.S.S.

NFR-2: Missing or ambiguous engineering and drawing facts fail before presentation.

NFR-3: Same source and compiler inputs produce identical placed Anchors and connector endpoints.

NFR-4: Theia and SVG export paint supplied facts without engineering inference.

NFR-5: Accepted drawings contain no loose, repaired, fallback, or untraceable endpoints.

NFR-6: Humans and AI can explain every visible endpoint from source evidence.

NFR-7: Wrong pre-public paths are deleted, not adapted.

NFR-8: Dedicated example refresh completes within 10 seconds after IDE readiness.

### Architecture Requirements

- Evolve existing product types in place. Do not create a parallel trust, geometry, occurrence,
  connector, or protocol model.
- `RepresentationDefinition` is sole intrinsic Element geometry and Anchor authority.
- `GraphicOccurrence` is sole placed body and Anchor authority.
- `PresentationConnector` is sole visible Connection authority.
- Compiler alone joins Port meaning, Anchor geometry, placement, and route candidates.
- `PresentationDocument.connectors` is the only visible connection collection.
- Shared junction, crossing, bus-tap, and continuation markers occur once at document level.
- Terminal marker and hit geometry belong to Graphic Occurrence Anchors.
- Route candidates remain compiler-internal and untrusted.
- Theia and SVG export consume the same Presentation Document.
- Complete Presentation Documents publish atomically; failure invalidates current drawing.
- No Java2D, second renderer, universal schema, speculative framework, compatibility layer, XML
  runtime authority, or remote resource runtime.
- Every story removes the stale path it replaces and leaves the repository buildable.
- Gradle verification runs sequentially on Windows.

## FR Coverage

FR-1: M38-E1 through M38-E4 - preserve the human-first source boundary.

FR-2 through FR-6: M38-E1 - trustworthy Element geometry and explicit Port binding.

FR-7 through FR-14: M38-E2 - exact placed attachment and strict visible connectors.

FR-15 through FR-16: M38-E3 - one Theia presentation path and complete inspection trace.

FR-17 through FR-20: M38-E4 - dedicated proof, purge, screenshots, and milestone gate.

## Epic List

### M38-E1: Use Elements With Trustworthy Terminals

Package authors can use native or SVG-backed Elements whose visible geometry and terminal Anchors
compile through one small contract, while engineers keep Port meaning in Athena source.

**FRs covered:** FR-1 through FR-6, FR-20.

### M38-E2: See Every Connection Attached Exactly

Engineers see each Connection end at the exact placed Anchor selected by its Port binding, with no
fallback, second transform, or renderer repair.

**FRs covered:** FR-1, FR-7 through FR-14, FR-20.

### M38-E3: Inspect One Truthful Drawing In Theia

Engineers and AI inspect one atomic Presentation Document in Theia and trace every endpoint to its
Athena source without frontend inference.

**FRs covered:** FR-1, FR-10, FR-14 through FR-16, FR-20.

### M38-E4: Prove And Freeze The Trustworthy Path

Evaluators open a dedicated M38 example, verify exact connection structure and source trace, inspect
fresh screenshots, and find no competing legacy authority.

**FRs covered:** FR-1, FR-17 through FR-20.

## M38-E1: Use Elements With Trustworthy Terminals

### Story 1.1: Make Representation Definition The Only Geometry Contract

As a package author,
I want one intrinsic Element contract,
so that every Element has one understandable geometry and Anchor authority.

**Requirements:** FR-1 through FR-4, FR-20; AD-1 through AD-3, AD-8.

**Acceptance Criteria:**

**Given** current representation contracts and all production consumers
**When** intrinsic Element authority is refactored
**Then** `RepresentationDefinition` directly owns local body geometry, bounds, geometric Anchors, hit
geometry, label slots, and source/resource trace
**And** SVG-backed, native, and composite definitions expose that same contract shape
**And** Port, signal, direction, compatibility, component, and connection meaning do not exist in
Anchor geometry
**And** `PresentationAnatomy`, body-authority switches, compatibility shells, fallback behavior, and
parallel intrinsic geometry contracts are deleted from production code
**And** every retained production consumer uses `RepresentationDefinition` directly with no adapter,
alias, deprecated type, default fallback, or milestone-named replacement
**And** focused representation tests, affected module tests, source-set hygiene, and encoding audits
pass sequentially.

### Story 1.2: Compile Native And SVG Geometry Through One Contract

As a package author,
I want simple native geometry and complex package-local SVG geometry compiled identically,
so that geometry choice does not change engineering authority.

**Requirements:** FR-2, FR-3, FR-6, FR-20; AD-2, AD-8.

**Acceptance Criteria:**

**Given** one native Element and one package-local SVG-backed Element
**When** representation source and resources compile
**Then** both produce complete `RepresentationDefinition` values with local bounds, body geometry,
Anchors, hit geometry, label slots, and trace
**And** SVG admits only safe geometry plus neutral `data-athena-ref` identities
**And** scripts, handlers, external resources, path escape, unsafe entities, duplicate references,
unknown `data-athena-*`, and engineering metadata fail closed
**And** no SVG DOM, raw markup, XML authority, or resource-specific geometry model crosses the
representation boundary
**And** package-local resolution remains the only active resource scheme
**And** focused native, SVG, security, package-resource, and negative tests plus hygiene and encoding
audits pass sequentially.

### Story 1.3: Bind Every Port To One Anchor

As an engineering author,
I want each Port explicitly bound to one Element Anchor,
so that connection meaning and connection geometry cannot drift apart.

**Requirements:** FR-4, FR-5, FR-10; AD-3, AD-7.

**Acceptance Criteria:**

**Given** a selected Element and its engineering Ports
**When** representation binding compiles
**Then** each required Port resolves exactly one binding and one geometry-only Anchor
**And** binding identity and source span survive into compiler input
**And** missing Port, missing Anchor, duplicate binding, duplicate Anchor, incompatible binding, and
unknown geometry reference block compilation with plain corrective diagnostics
**And** compiler never binds by matching names, SVG nodes, body centers, nearest points, or route
coordinates
**And** no new normal-source keyword for coordinates, transforms, route, paint, or renderer mechanics
is introduced
**And** focused binding, diagnostic, parser-regression, and determinism tests plus hygiene and
encoding audits pass sequentially.

## M38-E2: See Every Connection Attached Exactly

### Story 2.1: Place Body And Anchors With One Transform

As an engineer,
I want Element bodies and terminals placed together,
so that a visible terminal cannot drift away from its connection point.

**Requirements:** FR-7 through FR-10; AD-4.

**Acceptance Criteria:**

**Given** one `RepresentationDefinition` and one current placement
**When** compiler creates a `GraphicOccurrence`
**Then** one transform places body geometry, bounds, Anchors, terminal marker geometry, hit geometry,
and label slots exactly once
**And** each placed Anchor is normalized once to the existing integer presentation/grid point
**And** all downstream consumers reuse that exact point without global offset, second rounding,
renderer transform, body-center fallback, or endpoint reconstruction
**And** missing, stale, duplicate, or conflicting placement evidence blocks publication with source-
spanned diagnostics
**And** obsolete occurrence types and duplicate placement transformations are deleted in the same
change
**And** focused transform, rotation, mirror, bounds, Anchor, and negative tests plus affected module
tests and audits pass sequentially.

### Story 2.2: Lower Route Candidates Into Exact Connectors

As an engineer,
I want every visible Connection lowered from exact endpoint evidence,
so that no line starts or ends at a random computed point.

**Requirements:** FR-11 through FR-13, FR-20; AD-5.

**Acceptance Criteria:**

**Given** an engineering Connection, two Ports, two bindings, two placed Anchors, and an untrusted
route candidate
**When** compiler lowers the visible Connection
**Then** one strict `PresentationConnector` carries required Connection, Port, binding, occurrence,
Anchor, endpoint point, route, line-class, and source evidence
**And** first and last route points equal the two placed Anchor points exactly
**And** normalization only replaces endpoints, removes zero-length points, and merges redundant
orthogonal segments
**And** fewer than two points, non-orthogonal geometry, ambiguity, missing evidence, or incompatible
endpoints block presentation
**And** accepted `RouteFact` authority, fallback quality states, nullable endpoint IDs, `(0,0)` and
body-center defaults, and duplicate connector lowerers are deleted
**And** focused lowering, endpoint equality, normalization, invalid-route, and trace tests plus audits
pass sequentially.

### Story 2.3: Publish Complete Connection Facts Atomically

As an engineer,
I want joins, crossings, line appearance, and labels published with their Connections,
so that drawing meaning never depends on visual guessing or stale partial state.

**Requirements:** FR-10, FR-14, FR-15; AD-6, AD-9.

**Acceptance Criteria:**

**Given** validated Graphic Occurrences and strict Presentation Connectors from one compilation
**When** compiler builds a Presentation Document
**Then** line appearance and labels are typed presentation facts
**And** junction, no-connect crossing, bus-tap, and continuation markers each have one document-level
identity, point, participants, appearance, and trace
**And** connectors reference shared marker identities and never infer topology from intersection
**And** terminal marker and hit geometry remain owned by Graphic Occurrence Anchors and are not
painted again by connectors
**And** occurrence, connector, marker, label, and trace facts publish as one validated atomic document
or no current drawing
**And** partial merge, stale connector retention, generic token maps for required facts, and duplicate
marker ownership are deleted
**And** focused document, marker, atomic-publication, and failure tests plus audits pass sequentially.

## M38-E3: Inspect One Truthful Drawing In Theia

### Story 3.1: Carry One Presentation Document Through Every Visible Path

As an engineer,
I want Theia and SVG export to receive the same compiled drawing,
so that each visible surface shows the same engineering facts.

**Requirements:** FR-15, FR-20; AD-6, AD-8, AD-9.

**Acceptance Criteria:**

**Given** one validated Presentation Document
**When** compiler, LSP, Theia protocol, and SVG export transport it
**Then** `PresentationDocument.connectors` is the only visible connection collection
**And** required occurrence, endpoint, route, marker, label, appearance, and trace fields remain typed
and complete across transport
**And** SVG export serializes that same document and performs no independent connection lowering
**And** public `routeFactSnapshot`, graph-edge fallback, raw route-candidate transport, partial
publication, and duplicate presentation models are deleted
**And** missing required facts fail before transport rather than becoming nullable frontend state
**And** focused compiler, serialization, LSP, SVG-export, and contract tests plus audits pass
sequentially.

### Story 3.2: Paint Exact Connections Without Frontend Repair

As an engineer,
I want Theia to paint only compiler-supplied facts,
so that the IDE cannot hide broken engineering truth.

**Requirements:** FR-14, FR-15, NFR-4, NFR-5; AD-6.

**Acceptance Criteria:**

**Given** a complete Presentation Document reaches Theia
**When** the graph workbench renders occurrences and connectors
**Then** it paints supplied body, terminal, route, marker, line, label, and text facts
**And** connector endpoints equal supplied placed Anchor points in rendered and hit-test geometry
**And** text shaping and paint remain normal Theia renderer work and never feed endpoint or topology
truth
**And** empty-route acceptance, `(0,0)` defaults, fallback terminals, graph-edge reconstruction,
crossing detection, route-label recovery, endpoint snapping, and style guessing are deleted
**And** invalid protocol input produces a clear failed drawing state rather than repaired geometry
**And** focused frontend model, edge-layer, hit-test, invalid-payload, and rendering tests plus frontend
build and audits pass.

### Story 3.3: Inspect Every Endpoint Back To Source

As an engineer or AI agent,
I want to inspect a visible endpoint and reach its Athena declaration,
so that every connection can be explained and corrected from source.

**Requirements:** FR-10, FR-16; AD-7.

**Acceptance Criteria:**

**Given** a visible connector endpoint in Theia
**When** inspection requests its trace
**Then** trace identifies Connection, Port, binding, Element, Anchor, Graphic Occurrence, placed point,
route endpoint, package resource, and Athena source span
**And** identities agree with the current atomic Presentation Document snapshot
**And** missing or conflicting trace blocks publication with a plain diagnostic naming subject,
problem, and source correction
**And** internal diagnostic codes remain protocol detail rather than user-facing jargon
**And** no DOM, SVG semantic metadata, graph inference, or renderer state creates trace
**And** focused compiler, LSP, selection, inspection, stale-snapshot, and negative tests plus audits
pass sequentially.

## M38-E4: Prove And Freeze The Trustworthy Path

### Story 4.1: Author The Dedicated M38 Example

As an evaluator,
I want one dedicated source-first M38 project,
so that exact attachment is tested with realistic geometry instead of mocked presentation payloads.

**Requirements:** FR-17, FR-20.

**Acceptance Criteria:**

**Given** `examples/m38/professional-control-drawing`
**When** its source and package resources compile
**Then** package hierarchy matches Athena package declarations
**And** the project contains one complex package-local SVG-backed Element, one simple native Element,
explicit Port-to-Anchor bindings, power/control/PE Connections, labels, one explicit junction, and one
explicit no-connect crossing
**And** all engineering metadata remains in `.athena` source while SVG carries geometry and neutral
references only
**And** the project reuses no active M36/M37 example, copied QET runtime format, XML authority, mock
Presentation Document, or hardcoded sample policy
**And** valid source compiles without blocking diagnostics
**And** dedicated example tests plus package hierarchy, hygiene, and encoding audits pass.

### Story 4.2: Purge Every Competing Drawing Authority

As a platform maintainer,
I want one current drawing path and no stale alternatives,
so that future work cannot silently restore detached connections.

**Requirements:** FR-20, NFR-7; AD-8.

**Acceptance Criteria:**

**Given** all retained production source and active visible producers
**When** the M38 authority audit runs
**Then** every producer, including Cabinet or any retained export path, publishes strict Presentation
Connectors or is removed from active publication
**And** no legacy anatomy, body-authority switch, compatibility shell, accepted RouteFact authority,
duplicate lowering, fallback endpoint, renderer repair, hardcoded sample policy, or stale M38-adjacent
test/doc remains
**And** no production `src/main` type is named `Proof`, `Demo`, `Sample`, by milestone, or with `V0` or
`V1`
**And** no alias, adapter, deprecation, migration default, or dead module exists solely to preserve
pre-M38 behavior
**And** source-set hygiene and repository searches enforce these prohibitions
**And** all affected module tests, source-set hygiene, encoding audit, and `git diff --check` pass.

### Story 4.3: Prove Exact Connections End To End

As a milestone evaluator,
I want structural and visual evidence from a fresh M38 build,
so that drawing trust is demonstrated rather than claimed.

**Requirements:** FR-18, FR-19, NFR-3, NFR-5, NFR-6, NFR-8.

**Acceptance Criteria:**

**Given** freshly rebuilt kernel, LSP, Theia frontend, and dedicated M38 project
**When** structural and Electron E2E verification runs
**Then** every route endpoint exactly equals its placed Anchor point
**And** proof reports zero detached endpoints, fallback endpoints, renderer repairs, inferred topology,
missing line facts, and incomplete traces
**And** mutation tests fail when any required Port, binding, Anchor, placement, endpoint, marker,
appearance, or trace fact is removed or corrupted
**And** endpoint inspection reaches the exact Athena source span
**And** post-ready refresh completes within 10 seconds on the supported workstation
**And** 1920x1080, 1280x900, and narrow Electron screenshots are stored under
`_bmad-output/implementation-artifacts/m38/screenshots`
**And** sequential root, affected runtime, LSP, frontend, IDE, Electron, hygiene, encoding, and diff
checks pass.

### Story 4.4: Close M38 And Hand Off Placement And Routing

As a milestone owner,
I want verified M38 records and narrow future handoffs,
so that M39 and M40 improve quality without weakening attachment truth.

**Requirements:** FR-18 through FR-20; CM-1 through CM-4.

**Acceptance Criteria:**

**Given** all M38 stories and required verification are complete
**When** milestone closure is recorded
**Then** sprint status, screenshot index, usage handoff, and retrospective cite only fresh evidence
**And** architecture audit confirms one `RepresentationDefinition`, one `GraphicOccurrence`, one
strict visible `PresentationConnector`, one atomic Presentation Document, and paint-only Theia/SVG
consumers
**And** M39 handoff may replace only current placement production while preserving occurrence
transform and attachment authority
**And** M40 handoff may replace only route-candidate production while preserving endpoint validation
and strict connector lowering
**And** no claim is made for professional placement or routing quality in M38
**And** no story, epic, command, artifact, or screenshot is marked complete without fresh evidence.
