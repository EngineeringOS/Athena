---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-20-m27/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-20-m27/ARCHITECTURE-SPINE.md
---

# Athena - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Athena M27, decomposing the
requirements from the PRD and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Provide an openable M27 sample project at `examples/m27/sample-project` using admitted Athena
syntax and exercising professional sheet frame, title block, zones, compact text, ordered linework,
professional symbols, terminal labels, and document projection integration.

FR2: Define professional visual acceptance references using QElectroTech-style professional sheets
as qualitative references, while rejecting pixel-perfect copying, QElectroTech import, and
drawing-first authority.

FR3: Render governed sheet frame facts, coordinate zones, margins, title block fields, and sheet
metadata through Presentation IR without making the canvas own page state.

FR4: Improve grid, density, line weights, symbol scale, terminal labels, route labels, and reference
markers as a coherent visual system with no accepted screenshot overlap.

FR5: Define Semantic Spatial Intent contracts for 2D electrical schematic projection, including
direction, terminal side, lane preference, grouping, separation, component avoidance, route ordering,
priority, confidence, and constraint source.

FR6: Keep routing backends below an Athena-owned adapter boundary. M27 may define a small adapter
seam, but external tools do not own semantic meaning, source mutation, terminal identity, document
occurrence identity, or persisted layout truth.

FR7: Avoid component crossings in accepted routes by consuming presentation bounds and terminal
anchors, and emit route quality metadata instead of silently presenting degraded routes as
satisfied.

FR8: Align routes into readable deterministic orthogonal lanes, preserving terminal-side entry where
policy exists.

FR9: Preserve compact route and reference text by keeping verbose semantic endpoint strings
available through hover, selection, inspector, or proof payloads, not always visible on the canvas.

FR10: Preview semantic endpoint connections by using semantic endpoint identity, signal
compatibility, direction, port metadata, spatial intent, and route geometry preview.

FR11: Defer auto-connection source mutation acceptance to M28 unless a story explicitly reuses an
existing governed mutation path without expanding M27 scope.

FR12: Polish Graphical View workbench chrome around controls, sheet selector, zoom, inspector
affordances, hover, selection, and information disclosure.

FR13: Preserve source, Problems, outline, graph, document projection, and inspector coherence.
Theia must not infer route or document meaning from DOM scans.

FR14: Add visual screenshot, DOM, or proof-payload acceptance coverage for sheet frame, title block,
zones, compact labels, ordered routes, route quality, component-crossing absence, center-fallback
absence, and verbose-label absence.

FR15: Publish M27 usage, comparison, and retrospective artifacts that explain M26 versus M27 and
state deferred work.

FR16: Purge stale code, docs, sample references, screenshots, and design claims before completion,
and record intentionally retained deferred artifacts with owner, reason, and milestone.

### NonFunctional Requirements

NFR1: The M27 route, layout, presentation, and visual proof outputs must be deterministic across
rebuilds for identical input.

NFR2: M27 must preserve the Athena authority chain:
`.athena source -> compiler semantic model -> semantic spatial intent -> layout/routing facts ->
representation/document projection facts -> Presentation IR -> Theia renderer`.

NFR3: M27 Semantic Spatial Compiler v0 is limited to 2D electrical schematic projection and must not
become a general spatial, CAD, 3D, cabinet, factory layout, or physical-routing system.

NFR4: M27 must not introduce new `.athena` syntax unless ANTLR4, Tree-sitter, compiler, LSP,
fixtures, tests, docs, and IDE behavior are upgraded together.

NFR5: Theia remains the only active frontend proof surface. Deprecated desktop-viewer, Compose, and
KMP frontend modules are out of scope.

NFR6: The renderer and Theia may paint, inspect, preview, select, and navigate facts only. They must
not infer engineering truth from SVG segments, DOM labels, canvas scans, or visual route breaks.

NFR7: Visual acceptance must use tolerant screenshot regression plus structured DOM/proof-payload
assertions, not pixel-perfect similarity to QElectroTech.

NFR8: M27 may break internal APIs when that improves long-term architecture, but it must preserve
authority boundaries and include a final stale artifact purge.

NFR9: `geometry-model` must remain minimal: bounds, anchors, route geometry facts, and presentation
primitives only. No curves, surfaces, solids, topology, or general constraint solver.

NFR10: Gradle verification commands must run sequentially on Windows.

### Additional Requirements

- Architecture AD-1: M27 visual quality is derived from semantic spatial intent, layout facts,
  routing facts, representation facts, document projection facts, and Presentation IR.
- Architecture AD-2: Semantic Spatial Compiler v0 is 2D electrical schematic only.
- Architecture AD-3: Spatial intent owns preferences, priority, confidence, and source; it never
  persists raw canvas coordinates.
- Architecture AD-4: Layout and routing are separate spatial compilations.
- Architecture AD-5: Route geometry is normalized Athena fact output.
- Architecture AD-6: Accepted routes must avoid component bodies.
- Architecture AD-7: Routing backend SPI is a boundary, not a dependency decision.
- Architecture AD-8: Professional sheet surface is Presentation IR output.
- Architecture AD-9: Semantic connection preview is transient.
- Architecture AD-10: Route quality is a fact, not a visual guess.
- Architecture AD-11: Theia remains a fact consumer.
- Architecture AD-12: Internal refactor is allowed, authority drift is not.
- Architecture AD-13: Geometry model stays minimal.
- Architecture AD-14: No new source syntax by default.
- Architecture AD-15: Theia IDE is the only frontend scope.
- Architecture AD-16: Completion requires a cleanup gate.

### UX Design Requirements

UX-DR1: The M27 sheet surface must visibly include professional frame, coordinate zones, title
block, dense grid, stable margins, and printable engineering-page structure.

UX-DR2: Route labels, terminal labels, device tags, and reference markers must remain compact and
must not crowd accepted screenshots.

UX-DR3: Hover, selection, and inspector disclosure must reveal detailed semantic identity without
making verbose identifiers always visible on the sheet.

UX-DR4: The Graphical View toolbar, sheet selector, zoom controls, and inspector affordances must
feel dense and professional without crowding the engineering canvas.

UX-DR5: Route quality must be inspectable through route selection, proof payload, or governed
diagnostics without relying on raw DOM inspection.

UX-DR6: Semantic connection preview UI must make compatibility and projected route behavior visible
while staying transient and non-mutating in M27.

UX-DR7: Accepted visual evidence must support A3 landscape sheet proof at 1920x1080 and 2560x1440
desktop viewports.

### FR Coverage Map

FR1: Epic 1 - M27 sample project exercises the professional sheet proof.
FR2: Epic 1 - QElectroTech-style references guide qualitative visual acceptance without copying or import authority.
FR3: Epic 1 - Sheet frame, zones, margins, title block, and metadata render through Presentation IR.
FR4: Epic 1 - Grid, density, line weights, symbol scale, terminal labels, route labels, and reference markers form a coherent visual system.
FR5: Epic 2 - Semantic Spatial Intent contracts define 2D schematic direction, terminal side, lane, grouping, separation, avoidance, ordering, priority, confidence, and source.
FR6: Epic 2 - Routing backend SPI remains below Athena authority boundaries.
FR7: Epic 2 - Accepted routes avoid component bodies and emit route quality metadata when degraded.
FR8: Epic 2 - Routes align into deterministic orthogonal lanes while preserving terminal-side entry.
FR9: Epic 3 - Verbose semantic endpoint strings move to hover, selection, inspector, or proof payloads instead of crowding the canvas.
FR10: Epic 3 - Semantic endpoint connection preview uses endpoint identity, compatibility, metadata, spatial intent, and route geometry preview.
FR11: Epic 3 - Auto-connection source mutation acceptance is deferred to M28 unless an existing governed mutation path is reused without scope expansion.
FR12: Epic 3 - Graphical View controls, sheet selector, zoom, inspector affordances, hover, selection, and disclosure are polished.
FR13: Epic 3 - Source, Problems, outline, graph, document projection, and inspector coherence are preserved without DOM inference.
FR14: Epic 4 - Visual screenshot, DOM, or proof-payload acceptance coverage verifies M27 visual and routing quality.
FR15: Epic 4 - M27 usage, comparison, and retrospective artifacts explain M26 versus M27 and deferred work.
FR16: Epic 4 - Stale code, docs, sample references, screenshots, and design claims are purged or explicitly retained with owner, reason, and milestone.

## Epic List

### Epic 1: Professional Sheet Users Can Trust
Users can open the M27 sample and see a professional engineering sheet surface: frame, zones, title
block, grid, margins, compact text, and QElectroTech-inspired visual quality without copying QET or
making drawing artifacts authoritative.
**FRs covered:** FR1, FR2, FR3, FR4.

### Epic 2: Semantic Linework Reads Like Engineering
Users get deterministic, ordered, orthogonal routes that avoid component bodies, preserve terminal-side
intent, expose route quality, and stay behind Athena-owned routing boundaries.
**FRs covered:** FR5, FR6, FR7, FR8.

### Epic 3: Theia Disclosure Without Canvas Crowding
Users can inspect semantic detail through hover, selection, inspector, Problems/source/outline
coherence, and transient connection preview without verbose labels always crowding the sheet.
**FRs covered:** FR9, FR10, FR11, FR12, FR13.

### Epic 4: Acceptance Evidence And Cleanup Gate
The team can trust M27 because visual/DOM/proof assertions exist, docs and retrospective are accurate,
and stale code, docs, samples, screenshots, and design claims are purged or explicitly retained before
completion.
**FRs covered:** FR14, FR15, FR16.

## Epic 1: Professional Sheet Users Can Trust

Users can open the M27 sample and see a professional engineering sheet surface: frame, zones, title
block, grid, margins, compact text, and QElectroTech-inspired visual quality without copying QET or
making drawing artifacts authoritative.

### Story 1.1: Openable M27 Sheet Proof

As an engineer,
I want to open the M27 sample project and see a valid professional sheet,
So that I can verify the milestone is rendering a real engineering page instead of a graph demo.

**Acceptance Criteria:**

**Given** `examples/m27/sample-project` exists with admitted Athena syntax
**When** the project is opened through the Theia IDE graphical view
**Then** the sample renders through the current accepted Theia graphical projection path without errors
**And** the semantic content needed by later M27 sheet-frame, title-block, compact-label, terminal-label, and ordered-linework stories is present.

**Given** the M27 sample project is rebuilt or reopened
**When** the same source is compiled and projected again
**Then** the same sheet identity, page structure, and proof payload identity are produced deterministically
**And** the canvas does not persist or infer hidden page truth.

### Story 1.2: Professional Sheet Frame And Metadata Facts

As an engineer,
I want sheet frame, zones, margins, title block fields, and sheet metadata to come from governed facts,
So that the rendered page looks professional while keeping `.athena` semantic source authoritative.

**Acceptance Criteria:**

**Given** a document projection snapshot for the M27 sample
**When** Presentation IR is generated
**Then** sheet frame, coordinate zones, margins, title block fields, and sheet metadata are present as explicit presentation facts
**And** Theia renders those facts without owning page state.

**Given** the same source and projection inputs
**When** Presentation IR is regenerated
**Then** sheet frame and metadata facts are stable across runs
**And** no raw canvas coordinates are introduced as source truth.

### Story 1.3: Professional Density, Scale, And Compact Text

As an engineer,
I want grid density, line weights, symbol scale, terminal labels, route labels, and reference markers to feel coherent,
So that the sheet is readable at engineering-document density without visual crowding.

**Acceptance Criteria:**

**Given** the M27 sheet renders at the accepted desktop proof viewports
**When** the visual proof is inspected
**Then** grid density, line weights, symbol scale, terminal labels, route labels, and reference markers are visually consistent
**And** accepted screenshots show no label overlap or text spilling outside its intended parent.

**Given** a route or reference has a verbose semantic identity
**When** the sheet is rendered by default
**Then** the always-visible text remains compact
**And** verbose endpoint strings are not painted as default route titles.

## Epic 2: Semantic Linework Reads Like Engineering

Users get deterministic, ordered, orthogonal routes that avoid component bodies, preserve terminal-side
intent, expose route quality, and stay behind Athena-owned routing boundaries.

### Story 2.1: Semantic Spatial Intent Contract

As an Athena engineer,
I want a Semantic Spatial Intent contract for 2D electrical schematic projection,
So that layout and routing behavior is driven by engineering meaning instead of renderer heuristics.

**Acceptance Criteria:**

**Given** the M27 projection pipeline consumes the semantic model
**When** spatial intent is derived
**Then** the contract represents direction, terminal side, lane preference, grouping, separation, component avoidance, route ordering, priority, confidence, and constraint source
**And** the contract is explicitly limited to 2D electrical schematic projection.

**Given** spatial intent is serialized into proof or diagnostic output
**When** it is inspected
**Then** it contains no persisted raw canvas coordinates
**And** it preserves the authority chain from `.athena` source through compiler-owned facts.

### Story 2.2: Routing Backend Boundary And Normalized Route Facts

As an Athena engineer,
I want routing backends hidden behind an Athena-owned adapter boundary,
So that external routing tools can assist later without owning semantic meaning or persisted layout truth.

**Acceptance Criteria:**

**Given** route generation is invoked for the M27 sample
**When** a routing backend or internal router produces candidate geometry
**Then** Athena normalizes the result into owned route geometry facts
**And** semantic meaning, terminal identity, document occurrence identity, source mutation, and persisted layout truth remain outside the backend.

**Given** an external backend is absent
**When** M27 routing tests and sample proof run
**Then** the built-in route path still produces deterministic normalized route facts
**And** the adapter boundary is covered by a minimal contract test or fixture.

### Story 2.3: Component-Avoiding Orthogonal Routes

As an engineer,
I want schematic routes to avoid component bodies and enter at terminal anchors,
So that linework reads like engineering wiring rather than generic graph edges.

**Acceptance Criteria:**

**Given** presentation bounds and terminal anchors are available
**When** route facts are generated
**Then** accepted routes do not cross component bodies
**And** route endpoints attach to terminal anchors instead of center fallback points.

**Given** a route cannot fully satisfy component avoidance
**When** route quality is emitted
**Then** the route is marked degraded with a reason
**And** the renderer does not silently present the degraded route as satisfied.

### Story 2.4: Deterministic Lanes And Route Quality Facts

As an engineer,
I want routes aligned into readable deterministic lanes with visible quality status,
So that schematic linework remains ordered and reviewable across rebuilds.

**Acceptance Criteria:**

**Given** multiple related routes exist in the M27 sample
**When** route facts are generated
**Then** routes use deterministic orthogonal lanes with stable ordering
**And** terminal-side entry is preserved where presentation policy defines it.

**Given** a route is selected or inspected through proof output
**When** route quality is queried
**Then** the quality metadata reports satisfied, degraded, crossing, or fallback status
**And** route quality is derived from facts, not DOM scanning or rendered SVG guessing.

## Epic 3: Theia Disclosure Without Canvas Crowding

Users can inspect semantic detail through hover, selection, inspector, Problems/source/outline
coherence, and transient connection preview without verbose labels always crowding the sheet.

### Story 3.1: Compact Route Labels With Hover And Selection Detail

As an engineer,
I want route labels to stay compact while detailed semantic identities remain available on demand,
So that the canvas stays readable without losing engineering traceability.

**Acceptance Criteria:**

**Given** a route connects verbose semantic endpoints
**When** the sheet is rendered
**Then** the visible route label uses compact display text
**And** the full semantic endpoint identity is available through hover, selection, inspector, or proof payload.

**Given** a route is selected
**When** the inspector updates
**Then** it shows canonical source identity, endpoint identity, route quality, and document projection context
**And** Theia obtains this information from facts rather than scanning DOM labels.

### Story 3.2: Dense Professional Graphical View Controls

As an engineer,
I want Graphical View controls to feel dense, predictable, and professional,
So that sheet navigation and inspection do not distract from the engineering canvas.

**Acceptance Criteria:**

**Given** the Graphical View is open on the M27 sample
**When** the user interacts with sheet selector, zoom controls, inspector affordances, hover, and selection
**Then** controls are compact, aligned, and do not overlap the canvas or each other
**And** the active frontend remains the Theia IDE only.

**Given** the viewport is 1920x1080 or 2560x1440
**When** the M27 proof sheet is displayed
**Then** controls and sheet content remain visually coherent
**And** text does not overflow buttons, panels, cards, or labels.

### Story 3.3: Semantic Connection Preview Foundation

As an engineer,
I want to preview a possible semantic connection between endpoints,
So that I can evaluate compatibility and projected route behavior before any source mutation exists.

**Acceptance Criteria:**

**Given** two semantic endpoints are selected for connection preview
**When** compatibility is evaluated
**Then** the preview uses endpoint identity, signal compatibility, direction, port metadata, spatial intent, and route geometry preview
**And** incompatible endpoints produce a governed explanation rather than a hidden canvas failure.

**Given** a connection preview is displayed
**When** the user cancels, changes selection, or reloads the project
**Then** no `.athena` source is modified
**And** no hidden connection truth is persisted in Theia state.

### Story 3.4: Source, Problems, Outline, Graph, And Inspector Coherence

As an engineer,
I want source, Problems, outline, graph, document projection, and inspector views to agree,
So that visual inspection stays tied to semantic truth.

**Acceptance Criteria:**

**Given** the M27 sample contains routes, symbols, labels, and document projection facts
**When** a subject is selected in the graph or inspector
**Then** the matching source identity, outline item, Problems diagnostic, route quality, and document projection context remain coherent
**And** no view infers route or document meaning from DOM scans.

**Given** a route quality diagnostic exists
**When** Problems or inspector disclosure is opened
**Then** the diagnostic includes governed provenance and actionable reason text
**And** the canvas remains a consumer of that diagnostic rather than its source.

## Epic 4: Acceptance Evidence And Cleanup Gate

The team can trust M27 because visual/DOM/proof assertions exist, docs and retrospective are accurate,
and stale code, docs, samples, screenshots, and design claims are purged or explicitly retained before
completion.

### Story 4.1: M27 Visual Acceptance Harness

As an Athena maintainer,
I want visual acceptance coverage for the M27 sheet proof,
So that professional sheet quality can be protected from regression.

**Acceptance Criteria:**

**Given** the M27 sample project is served in the Theia proof environment
**When** acceptance capture runs at 1920x1080 and 2560x1440
**Then** screenshots or equivalent visual artifacts verify the A3 landscape sheet surface, frame, zones, title block, compact labels, and ordered routes
**And** visual comparison is tolerant regression evidence, not pixel-perfect QElectroTech matching.

**Given** visual evidence is generated
**When** it is reviewed
**Then** the artifacts clearly distinguish QElectroTech-inspired quality references from Athena-authored facts
**And** no QET import or copied drawing authority is introduced.

### Story 4.2: Structured Proof Assertions For Routing And Labels

As an Athena maintainer,
I want structured DOM or proof-payload assertions for M27 routing and label quality,
So that visual acceptance does not rely only on screenshots.

**Acceptance Criteria:**

**Given** the M27 proof payload is generated
**When** assertions run
**Then** they verify sheet frame, title block, zones, compact labels, ordered routes, route quality, component-crossing absence, center-fallback absence, and verbose-label absence
**And** the assertions are deterministic across rebuilds.

**Given** a route has degraded quality
**When** proof assertions inspect it
**Then** the degraded state and reason are visible in structured output
**And** the test does not require SVG geometry scraping to infer engineering meaning.

### Story 4.3: M27 Usage, Comparison, And Retrospective Artifacts

As an Athena maintainer,
I want accurate M27 usage, comparison, and retrospective documentation,
So that future milestones understand what M27 proved and what it intentionally deferred.

**Acceptance Criteria:**

**Given** M27 implementation is complete
**When** documentation is published
**Then** it explains how to open the M27 sample, what changed from M26, how QElectroTech references were used, and which work is deferred to M28 or later
**And** it states that M27 does not introduce new `.athena` syntax, PDF export, QET import, full IEC/company standards, cabinet routing, or auto-connection source mutation acceptance.

**Given** retrospective notes are written
**When** they are reviewed
**Then** they accurately record implementation tradeoffs, test evidence, known limitations, and cleanup decisions
**And** they do not claim unsupported syntax, UI behavior, or architecture capabilities.

### Story 4.4: Stale Artifact Purge And Retention Ledger

As an Athena maintainer,
I want stale code, docs, sample references, screenshots, and design claims purged or explicitly retained,
So that the project remains clean and accurate after M27.

**Acceptance Criteria:**

**Given** all M27 stories are implemented
**When** the cleanup gate runs
**Then** stale M27 code, docs, screenshots, sample references, and design claims are removed or corrected
**And** intentionally retained deferred artifacts are recorded with owner, reason, and target milestone.

**Given** cleanup is complete
**When** final verification runs
**Then** encoding audit, relevant tests, and milestone documentation checks pass
**And** deprecated desktop-viewer, Compose, and KMP frontend modules remain untouched unless a specific cleanup item documents a safe removal.
