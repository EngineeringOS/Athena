---
title: Athena M33 Epics And Stories
status: draft
created: '2026-07-23'
updated: '2026-07-23'
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-23-m33/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-23-m33/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-23-m33/ARCHITECTURE-SPINE.md
---

# Athena M33 Epics And Stories

## Extracted Functional Requirements

FR-1..FR-6: Engineering Drawing Symbol Anatomy v1 contract, anchors, slots, bounds, diagnostics.

FR-7..FR-11: Native IEC-style symbol package v1 with ten demo-ready symbols and package tests.

FR-12..FR-20: Graphic Primitive IR v0, SVG adapter, derived viewBox, no duplicate/off-screen
output, transient Workbench chrome, descriptor anchor routing.

FR-21..FR-26: Professional sheet composition facts for frame, title block, zones, lanes, terminal
strips, labels, references, bounds, and diagnostics.

FR-27..FR-31: Live Workbench graph consumes M32 package-resolved descriptors and preserves profile
as first-class policy.

FR-32..FR-37: Workbench product surface cleanup: Cabinet-only product focus, stable Cabinet
controls, Create Device decision, and contained debug controls.

FR-38..FR-43: M33 customer demo and quality gates with structured proof and Electron screenshots.

FR-44..FR-46: AC-to-evidence, polish/purge, and cleanup ledger gates.

## Extracted Non-Functional Requirements

NFR-1: `.athena` remains semantic truth and no visual syntax is added.

NFR-2: Standard and vendor visuals are package/profile/descriptor assets.

NFR-3: Graphic Primitive IR is renderer-neutral; SVG is an adapter.

NFR-4: Renderer is paint-only; upstream owns symbol/profile/package selection.

NFR-5: Workbench UX is separate from drawing engine authority.

NFR-6: Visual credibility is a release criterion.

NFR-7: Output proof is deterministic for same source, packages, profile, and viewport constraints.

NFR-8: Invalid anchors, labels, descriptors, packages, or bounds fail closed.

NFR-9: Standards are reference anchors, not compliance claims.

NFR-10: Gradle verification runs sequentially on Windows.

NFR-11: Repository text remains UTF-8.

## Architecture Requirements

- Follow architecture AD-1 through AD-10 from the M33 architecture spine.
- Keep drawing engine generic; IEC is first package.
- Keep Workbench UX separate from drawing authority.
- Require structured proof for every visual claim.
- End every story with AC-to-evidence and polish/purge.

## Epic 1: Drawing Engine Contracts

Establish the frontend-neutral model contracts that every later M33 story consumes.

### Story 1.1: Define Engineering Drawing Symbol Anatomy v1

As an Athena platform developer, I want a generic drawing symbol anatomy contract so IEC and future
domain packages share one symbol boundary.

Acceptance Criteria:
- Given a symbol definition, validation captures identity, package id, domain/profile tags,
  lifecycle, anchors, label slots, reference slots, hotspots, bounds, orientation, and provenance.
- Given forbidden semantic/source/DOM fields, validation rejects the symbol with structured
  diagnostics.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 1.2: Define Graphic Primitive IR v0

As a renderer developer, I want a renderer-neutral Graphic Primitive IR so representation output
does not directly become SVG.

Acceptance Criteria:
- IR supports line, polyline, arc, circle, rectangle, text, marker, dot, reference arrow, group,
  transform, bounds, and style token.
- IR validation rejects engineering truth, package resolver behavior, source mutation behavior,
  DOM selectors, and SVG-specific authority.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 1.3: Add Drawing Diagnostics And Proof Payloads

As a maintainer, I want drawing diagnostics and proof payloads so visual correctness is machine
checkable.

Acceptance Criteria:
- Diagnostics identify failed authority: symbol anatomy, primitive IR, sheet composition, route
  anchor, renderer adapter, Workbench chrome, or package binding.
- Proof payloads serialize without Theia/browser runtime.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 2: Native IEC-Style Symbol Package

Create the first professional representation package while keeping the underlying engine generic.

### Story 2.1: Scaffold IEC-Style Representation Package v1

As a package author, I want an Athena-owned IEC-style package scaffold so demo symbols resolve
through M32 package rules.

Acceptance Criteria:
- Package contains descriptor metadata, profile compatibility, lifecycle metadata, and validation
  tests.
- Package is Athena-owned and contains no copied QET/EPLAN/vendor assets.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 2.2: Implement Core Power And Control Symbols

As a controls engineer, I want professional power/control symbols so the demo no longer renders
generic boxes.

Acceptance Criteria:
- Package includes power supply marker, protective breaker/fuse, switch/contact, relay coil,
  lamp/indicator, motor/load, and connection dot.
- Every symbol proves anchors, label slots, primitive output, bounds, and profile compatibility.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 2.3: Implement Terminal And Reference Symbols

As a controls engineer, I want terminals and references so the sheet looks like an engineering
document, not a plain graph.

Acceptance Criteria:
- Package includes terminal, terminal strip segment, folio/continuation reference, and reference
  label slots.
- Route anchor tests reject center fallback for demo symbols.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 3: Graphic Primitive Rendering Pipeline

Compile package-backed symbol descriptors into Graphic Primitive IR and render through SVG.

### Story 3.1: Compile Symbol Anatomy To Graphic Primitive IR

As a rendering pipeline developer, I want descriptor-backed symbols compiled to Graphic Primitive
IR so rendering stays backend-neutral.

Acceptance Criteria:
- Compiler maps symbol primitives, anchors, label slots, style tokens, and bounds into Graphic
  Primitive IR.
- Compiler does not inspect raw SVG DOM, CSS classes, file names, or label text for meaning.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 3.2: Implement SVG Adapter For Graphic Primitive IR

As a Workbench user, I want Graphic Primitive IR rendered as SVG so current Graph View displays
professional symbols.

Acceptance Criteria:
- SVG adapter renders required primitives with stroke, fill, text baseline, transform, and marker
  fidelity.
- Normal output contains no visible hitbox/background wrapper borders.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 3.3: Enforce Derived ViewBox And No-Fallback Rendering

As a reviewer, I want renderer proof to fail on toy regressions.

Acceptance Criteria:
- `viewBox` derives from resolved primitive/sheet bounds plus governed margins.
- Proof rejects duplicate labels, off-screen duplicate elements, center fallback routes, and
  generic fallback boxes in the demo.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 4: Professional Sheet Composition

Create professional engineering document structure around the symbols.

### Story 4.1: Add Sheet Frame, Title Block, And Zones

As a controls engineer, I want a real drawing sheet frame so the canvas reads as documentation.

Acceptance Criteria:
- Composition emits frame, title block, zones/grid references, and governed margins.
- Bounds proof includes frame/title block without hard-coded oversized canvas constants.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 4.2: Add Lanes, Rails, Terminal Strips, And Label Bands

As a controls engineer, I want disciplined schematic structure so the drawing is dense and
scanable.

Acceptance Criteria:
- Composition emits supply rails, control lanes, terminal-strip grouping, compact label bands, and
  route channels.
- Diagnostics detect collisions, excessive whitespace, label overflow, and out-of-sheet content.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 4.3: Add Reference And Continuation Placement

As a reviewer, I want reference markers and continuation placement so the sheet follows
professional drawing patterns.

Acceptance Criteria:
- Composition emits reference target facts and continuation placement facts.
- Demo includes at least one reference/continuation marker with structured proof.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 5: Workbench Product Surface Cleanup

Make the customer-facing Workbench stop exposing debug/projection chaos.

### Story 5.1: Define Professional Schematic As Primary Surface [Superseded Decision]

As a customer demo user, I want one clear primary graph surface so I am not confused by internal
projection ids.

Acceptance Criteria:
- Normal toolbar exposes Professional Schematic as primary surface.
- Raw ids such as `cabinet`, `documentation`, and `schematic` do not appear as confusing peer
  product buttons.
- AC-to-evidence and polish/purge notes are recorded before review.

Historical note: Story 5.1 was completed before the approved Cabinet-only sprint correction. Its
single-surface adapter work remains useful, but its choice of Professional Schematic is superseded.

### Story 5.2: Make Cabinet The Only Product Surface

As a customer demo user, I want one focused Cabinet surface so Athena can deliver one professional
layout instead of three incomplete views.

Acceptance Criteria:
- The normal Workbench exposes only Cabinet as the product surface and activates backend `cabinet`.
- Documentation and Schematic/Wiring remain hidden compatibility paths and are absent from product
  controls and M33 visual acceptance.
- Cabinet navigation and active state remain stable across refresh without cross-view caching.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 5.3: Resolve Create Device Demo Behavior

As a Cabinet demo user, I want Create Device to either work in Cabinet or not distract me.

Acceptance Criteria:
- Create Device performs governed semantic entity creation with visible graph refresh, or is
  removed/disabled with explicit rationale.
- No dead placeholder panel remains in the customer demo.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 5.4: Contain Debug And Proof Controls

As a Cabinet demo user, I want debug controls hidden from normal canvas flow.

Acceptance Criteria:
- Debug/proof controls live under inspection affordances.
- Normal toolbar no longer fills with line-like buttons or internal proof toggles.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 6: M33 Customer Demo And Proof

Build the demo project and make claims testable.

### Story 6.1: Create M33 Sample Project And Package Set

As a solution engineer, I want an M33 Cabinet sample project so the milestone has a concrete demo
target.

Acceptance Criteria:
- `examples/m33/sample-project` contains Athena-owned source, packages, profiles, and assets.
- Cabinet demo covers power supply, protective device, switch/control input, relay/actuator,
  terminal strip, motor/load interface, lamp/status, rails, labels, and reference marker.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 6.2: Add Structured Drawing Product Smoke

As a maintainer, I want a structured product smoke so demo correctness is not screenshot-only.

Acceptance Criteria:
- Cabinet smoke proves package resolution, symbol anatomy, Graphic Primitive IR, composition,
  anchors, text placement, bounds, viewBox, and no fallback rendering.
- Smoke fails on visible wrapper borders, duplicate labels, off-screen duplicates, dead Create
  Device, and hidden sheet navigation.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 6.3: Add Electron Screenshot Evidence

As a reviewer, I want screenshot evidence across real IDE viewports.

Acceptance Criteria:
- Electron proof activates Cabinet and captures desktop and narrower viewport screenshots.
- Screenshots support qualitative professional review and are paired with structured proof.
- AC-to-evidence and polish/purge notes are recorded before review.

## Epic 7: Closeout, Review, And Next Phase

Close M33 without stale rendering and UX debt.

### Story 7.1: Publish Visual Review Checklist

As a reviewer, I want a visual checklist so M33 does not rely on vague taste.

Acceptance Criteria:
- Checklist compares the M33 Cabinet layout qualitatively against professional cabinet and
  engineering drawing references without copying proprietary assets.
- Checklist states M33 uses standards as reference anchors, not compliance claims.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 7.2: Purge Toy Rendering And Workbench Debt

As a maintainer, I want stale renderer and Workbench paths removed or ledgered.

Acceptance Criteria:
- Cleanup removes or ledgers Cabinet generic fallback boxes, stale product modes, dead authoring
  panels, duplicate-label paths, and hard-coded viewBox paths.
- Ledger entries include owner, reason, target milestone, and verification.
- AC-to-evidence and polish/purge notes are recorded before review.

### Story 7.3: Run M33 Retrospective And M34 Handoff

As a platform owner, I want M33 lessons recorded and M34 direction prepared.

Acceptance Criteria:
- Retrospective records whether M33 closed the first-generation rendering gap.
- Handoff recommends M34 Engineering Knowledge Runtime or Standards Platform unless M33 evidence
  shows renderer blockers remain.
- AC-to-evidence and polish/purge notes are recorded before review.
