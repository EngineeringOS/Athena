---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - step-03-create-stories
  - step-04-final-validation
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-07-m44/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-07-m44/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/m44/design.md
---

# Athena - M44 Epic Breakdown

## Overview

M44 delivers asset-first editable canvas quality: governed IEC/vendor library assets, one canonical
scene, clean Konva rendering, validated source round-trip operations, and milestone-local evidence.

## Requirements Inventory

### Functional Requirements

FR1: Resolve locked Engineering and Representation Library Packages through `athena.yaml`, `athena.lock`,
repository snapshots, and admitted digests.

FR2: Admit a Symbol only when one canonical SVG resource and one `symbol.yaml` descriptor using
`athena-symbol-v1` are present, digest-valid, and mutually consistent.

FR3: Support Symbol, Part, and coordinated Symbol/Part references while preserving Engineering Entity,
Function, Occurrence, and Relationship identity.

FR4: Publish traceable `AthenaDiagramScene` occurrences with semantic identity, occurrence identity,
library item reference, ports, labels, placement, style, and Source Trace.

FR5: Render admitted SVG geometry, not placeholders, and produce exact canonical SVG plus pinned PNG proof.

FR6: Keep the sheet clean: outer rulers/frame visible, internal grid hidden by default, no debug or bottom
table clutter.

FR7: Apply deterministic style cascade without altering engineering meaning.

FR8: Persist style edits only through same-basename `*.sheet.style.athena`; allow preview and discard.

FR9: Submit typed Edit Operations with target identity, Source Revision, and Source Trace.

FR10: Validate and atomically apply operations through server-side staged compile and source patch publish.

FR11: Preserve operation-level Undo/Redo.

FR12: Keep frontend free of engineering authority.

FR13: Isolate Konva behind a renderer backend contract.

FR14: Explain rejected publication and edit failures with plain subject/problem/correction diagnostics.

FR15: Produce reproducible M44 tests, screenshots, exports, operation transcripts, and performance evidence.

### NonFunctional Requirements

NFR1: Fixed full input tuple produces stable scene digest and exact SVG bytes; PNG proof uses pinned
Electron/Chromium raster tolerance.

NFR2: No operation partially applies source or publishes partial scene.

NFR3: Library bytes are digest-admitted and provenance/license inspectable.

NFR4: Pan/zoom and local interaction do not recompile semantic or spatial authority.

NFR5: Benchmark profile must meet p95 pan/zoom <=20 ms, p95 move-preview <=100 ms, and heap growth <=20 MB
over 60 seconds after warm-up.

NFR6: Symbols, ports, labels, and selection states stay distinguishable at fit-to-page scale.

NFR7: Diagnostics use human engineering language, not transport/internal codes as primary text.

NFR8: Production `src/main` contains no milestone demo/proof/sample, compatibility shim, `M44`, `V0`, or
`V1` production class names.

### Additional Requirements

- Architecture uses governed projection pipeline: source, Sheet, Style, lock, and Library Packages compile
  into one `AthenaDiagramScene`.
- `symbol.yaml` is the only M44 descriptor syntax; parser canonicalizes to JSON for digesting.
- Engineering source owns Port semantics; library descriptor owns keyed geometry and compatibility envelope.
- Source Revision compare-and-sets `inputRevision`, engineering source, Sheet, Style, lock, package digests,
  compiler/schema/profile, and source-root identity.
- Edit operation server transaction declares writable files, stages patches, validates compile, atomically
  publishes or rolls back, and emits publication correlation id.
- Renderer backend is Konva Canvas2D for M44; no second interactive renderer.
- Must slice lands first, but M44 closure requires the complete authoring transaction loop.
- Evidence must stay under `_bmad-output/implementation-artifacts/m44/`.

### UX Design Requirements

UX-DR1: Canvas displays clean outer row/column rulers and blank drawing area by default.

UX-DR2: Internal grid lines remain hidden unless future interaction mode enables them.

UX-DR3: Real SVG symbols, labels, ports, routes, and selection overlays are legible and non-overlapping at
fit-to-page and supported zoom levels.

UX-DR4: Style edits can be previewed, discarded, or solidified without changing engineering meaning.

UX-DR5: Rejected edit and publication failures show exact subject/problem/correction in product UI.

### FR Coverage Map

FR1: Epic 1 - locked Library Packages resolve deterministically.
FR2: Epic 1 - `symbol.yaml` plus SVG asset admission blocks bad packages.
FR3: Epic 1 - Symbol/Part references preserve engineering identity.
FR4: Epic 1 - canonical traceable scene occurrences are published.
FR5: Epic 1 - real SVG geometry renders and exports.
FR6: Epic 1 - sheet frame/rulers stay clean.
FR7: Epic 2 - style cascade resolves without changing engineering meaning.
FR8: Epic 2 - Style Companion preview/discard/solidify flow persists style.
FR9: Epic 3 - typed edit operations carry identity, revision, and trace.
FR10: Epic 3 - operations validate and apply atomically.
FR11: Epic 3 - Undo/Redo works at operation granularity.
FR12: Epic 3 - frontend has no engineering authority.
FR13: Epic 4 - Konva backend is bounded and replaceable.
FR14: Epic 1, Epic 3 - rejected package/publication/edit diagnostics are actionable.
FR15: Epic 4 - reproducible screenshots, exports, transcripts, performance, and hygiene evidence exist.

## Epic List

### Epic 1: Readable Governed Engineering Sheet

Engineers can open the active M44 example and see real IEC/vendor library symbols, traceable ports,
labels, routes, and clean sheet rulers from admitted package assets.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR14

### Epic 2: Controllable Presentation Without Semantic Drift

Engineers can preview, discard, or persist presentation styling while engineering meaning, port facts,
relationship facts, and identity stay unchanged.

**FRs covered:** FR7, FR8

### Epic 3: Safe Canvas Edits Round-Trip To Source

Engineers can perform validated canvas operations that patch the correct source files atomically, reject
invalid intent, and preserve operation-level history.

**FRs covered:** FR9, FR10, FR11, FR12, FR14

### Epic 4: Product Evidence And Closure

Reviewers can trust M44 because the Theia renderer, export paths, performance profile, operation
transcripts, screenshots, and source-set hygiene prove the active example end to end.

**FRs covered:** FR13, FR15

## Final Validation

- FR1-FR15 covered by at least one epic and story.
- UX-DR1 through UX-DR5 covered by Stories 1.3, 2.1, 2.2, 3.2, 4.1, and 4.2.
- No story depends on a future story in the same epic.
- Foundational order is explicit: 1.1, 1.2, 1.3, then operation foundation before broader edit breadth.
- Architecture decisions AD-1 through AD-13 are represented in story acceptance criteria.
- Evidence and hygiene requirements point only to `_bmad-output/implementation-artifacts/m44/`.

## Epic 1: Readable Governed Engineering Sheet

Engineers can open the active M44 example and see real IEC/vendor library symbols, traceable ports,
labels, routes, and clean sheet rulers from admitted package assets.

### Story 1.1: Admit Locked Symbol Library Assets

As a library maintainer,
I want Athena to admit `symbol.yaml` plus SVG library items through the package lock,
So that projects consume governed IEC/vendor symbols instead of copied reference files.

**Acceptance Criteria:**

**Given** a governed M44 example depends on a Representation Package with one SVG and one `symbol.yaml`
using `athena-symbol-v1`
**When** the project resolves packages and compiles
**Then** the Symbol item is admitted with package identity, item identity, version, digest, provenance,
and license trace
**And** missing SVG, missing descriptor, unknown unnamespaced field, digest mismatch, or
reference-directory dependency rejects publication with subject/problem/correction diagnostics.

### Story 1.2: Compile Traceable Function Symbol Occurrences

As an engineering author,
I want one Entity to publish multiple Function/Symbol occurrences with stable traces,
So that EPLAN-like function separation survives without making symbols own engineering identity.

**Acceptance Criteria:**

**Given** the active M44 rolling-shutter example has one Entity with at least two Functions and compatible
library Symbol references
**When** Athena compiles the Canonical Scene
**Then** each occurrence has stable `semanticId`, `occurrenceId`, `traceId`, Function-before-Entity source
trace, resolved ports, labels, and package item reference
**And** a Symbol/Part replacement preserves Entity, Function, Occurrence, and Relationship identities.

### Story 1.3: Render Clean Real-Symbol Sheet

As a design reviewer,
I want Theia to show real admitted symbol geometry on a clean sheet,
So that the canvas looks like an engineering document rather than debug rectangles.

**Acceptance Criteria:**

**Given** the active M44 example compiles with admitted assets and traceable occurrences
**When** the Theia canvas opens the sheet
**Then** every engineering occurrence renders admitted SVG geometry, labels, ports, routes, and selection
trace without placeholder rectangles
**And** outer row/column rulers and sheet frame are visible, internal grid is hidden by default, and no
debug/bottom-table clutter appears.

## Epic 2: Controllable Presentation Without Semantic Drift

Engineers can preview, discard, or persist presentation styling while engineering meaning, port facts,
relationship facts, and identity stay unchanged.

### Story 2.1: Resolve Sheet Style Companion

As an engineering author,
I want same-basename Style Companions to control presentation only,
So that style can change without corrupting engineering truth.

**Acceptance Criteria:**

**Given** `rolling-shutter.sheet.athena` has zero or one
`rolling-shutter.sheet.style.athena`
**When** Athena compiles the scene
**Then** missing style companion uses defaults, one companion participates in Source Revision and scene
trace, and multiple companions produce an actionable diagnostic
**And** style changes never alter Entity, Function, Part, Port, Relationship, or Symbol identity.

### Story 2.2: Preview, Discard, And Solidify Style Edits

As a design reviewer,
I want to preview route and symbol style edits before writing them,
So that presentation can be adjusted deliberately.

**Acceptance Criteria:**

**Given** a user changes line width, dash, color, label typography, route marker, or port display
**When** the user previews, discards, or solidifies the edit
**Then** preview affects only session overlay, discard writes no files, and solidify writes only
`*.sheet.style.athena`
**And** recompilation preserves deterministic resolved styles and source trace.

### Story 2.3: Render Aligned Editor Rulers

As a design reviewer,
I want row and column coordinates rendered as aligned editor chrome,
So that the engineering sheet is clean, readable, and never distorted by canvas scaling.

**Acceptance Criteria:**

**Given** a READY Canonical Scene publishes row and column counts
**When** the Engineering Document renders at desktop or narrow editor sizes
**Then** one plain HTML horizontal flex ruler shows `1..columns`, one vertical flex ruler shows
`A..rows`, one corner cell joins them, and every bordered ruler cell aligns exactly with the drawing area
**And** Konva paints only the drawing area and engineering content, internal grid lines remain hidden,
the style toolbar occupies its own layout row, and no scene-space ruler label or divider is painted.

## Epic 3: Safe Canvas Edits Round-Trip To Source

Engineers can perform validated canvas operations that patch the correct source files atomically, reject
invalid intent, and preserve operation-level history.

### Story 3.1: Classify Edit Operations And Journal Transactions

As a maintainer,
I want every canvas operation classified by authority and journaled after acceptance,
So that presentation, representation, and engineering edits cannot leak authority into each other.

**Acceptance Criteria:**

**Given** Move, Align, Distribute, Snap, Set Style, Change Symbol, Reconnect, Bind Part, Undo, or Redo is
submitted
**When** the operation reaches the server
**Then** it declares `PresentationEditOperation`, `RepresentationEditOperation`, or
`EngineeringEditOperation`, plus target identity, Source Trace, Source Revision, and writable file set
**And** accepted operations append one Operation Journal entry with operation type, authority class,
accepted patch, publication correlation id, and resulting Source Revision.

### Story 3.2: Execute Presentation Operations

As an engineering author,
I want Move, Align, Distribute, Snap, and Set Style to round-trip through validated source,
So that presentation edits survive recompilation without touching Engineering Reality.

**Acceptance Criteria:**

**Given** a selected occurrence or occurrence group has a current Source Revision and Source Trace
**When** the user completes Move, Align, Distribute, Snap, or Set Style
**Then** the frontend submits one typed operation, server compare-and-sets full Source Revision, writes
only `*.sheet.athena` or `*.sheet.style.athena`, recompiles, and publishes one new scene with a correlation id
**And** reopening the project shows the accepted placement.

### Story 3.3: Execute Representation And Engineering Operations

As an engineering author,
I want Change Symbol, Reconnect, and Bind Part to go through their correct authority paths,
So that non-graphics engineering intent is validated before source changes.

**Acceptance Criteria:**

**Given** a user changes a Symbol, reconnects a valid Port, or binds/replaces a Part
**When** the operation is validated
**Then** Change Symbol writes representation binding and preserves Engineering Entity, Function,
Relationship, and Port identity
**And** Reconnect and Bind Part write engineering source only after Relationship, Capability, and Flow
validation pass.

### Story 3.4: Reject Invalid Edit Without Mutation

As an engineering author,
I want invalid canvas edits to fail closed,
So that the last accepted design stays trustworthy.

**Acceptance Criteria:**

**Given** an operation has stale Source Revision, missing target, invalid port direction, incompatible
flow/domain, or staged compile failure
**When** the server validates the operation
**Then** no source file changes, no partial scene publishes, and the diagnostic names exact
subject/problem/correction
**And** frontend tests prove engineering validation is not duplicated in UI logic.

### Story 3.5: Complete Operation Journal Undo Redo And Transcripts

As a design reviewer,
I want every accepted operation to have an auditable journal and transcript,
So that editing breadth does not bypass transaction safety.

**Acceptance Criteria:**

**Given** any M44 operation is implemented
**When** the proof suite runs
**Then** each operation transcript records target identity, preconditions, writable files, accepted patch,
staged compile result, publication correlation id, identity preservation, reopen result, and rejection path
**And** Undo/Redo use the Operation Journal, failed Redo emits stale/conflict diagnostic, and failed Redo
records no new operation.

## Epic 4: Product Evidence And Closure

Reviewers can trust M44 because the Theia renderer, export paths, performance profile, operation
transcripts, screenshots, and source-set hygiene prove the active example end to end.

### Story 4.1: Produce Shared Scene Export Evidence

As an integrator,
I want canvas, SVG, and PNG evidence to consume the same Canonical Scene,
So that export does not become a second rendering truth.

**Acceptance Criteria:**

**Given** the active M44 example has fixed source, Sheet, Style, lock, package bytes, compiler/schema/profile,
and source-root inputs
**When** export proof runs twice
**Then** scene digest and SVG bytes are exact identical, PNG captures pass recorded pixel tolerance, and
evidence records OS, Electron/Chromium version, viewport, DPR, fonts, and color profile
**And** artifacts are stored under `_bmad-output/implementation-artifacts/m44/`.

### Story 4.2: Close M44 With Performance And Hygiene Evidence

As a maintainer,
I want M44 closure backed by performance, screenshots, tests, and hygiene audits,
So that the milestone does not ship stale demo or compatibility paths.

**Acceptance Criteria:**

**Given** all M44 stories are implemented and source is rebuilt
**When** closure proof runs on the checked benchmark profile
**Then** p95 pan/zoom frame time is <=20 ms, p95 drag preview is <=100 ms, heap growth is <=20 MB over
60 seconds, and screenshots show clean real-symbol canvas
**And** encoding audit, source-set hygiene audit, relevant Gradle tests, frontend contract/build tests, and
Theia proof pass sequentially with evidence paths recorded.
