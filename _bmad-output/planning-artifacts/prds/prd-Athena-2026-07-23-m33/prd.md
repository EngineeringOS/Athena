---
title: Athena M33 - Professional Engineering Drawing Engine Foundation
status: draft
created: '2026-07-23'
updated: '2026-07-23'
---

# Athena M33 PRD - Professional Engineering Drawing Engine Foundation

## Executive Summary

M33 turns the M30-M32 representation and package foundations into a customer-visible professional
engineering drawing engine. The current Athena graph surface can be semantically correct and still
look toy-like because the drawing layer does not yet express a serious engineering drawing
language: symbol anatomy, sheet composition, linework, text placement, anchors, references,
terminal strips, and visual-density rules.

M33 is not another hidden architecture milestone and not a broad package catalog milestone. It is
the first milestone where the canvas itself must become credible to a controls engineer.

M33 is also the last intentionally ECAD-shaped milestone in Athena's first-generation platform.
Its customer demo uses an IEC-style electrical/control sheet because that is the fastest way to
close the visible credibility gap, but the underlying contracts must be generic enough for future
hydraulic, pneumatic, P&ID, robotics, SCADA, cabinet, network, and mechanical drawing packages.

The M33 thesis:

```text
.athena owns engineering truth.
Engineering packages own reusable product and concept knowledge.
Presentation Profiles choose visual policy.
Representation packages own drawing assets and symbol anatomy.
Graphic Primitive IR is the renderer-neutral drawing scene.
Concrete renderers, starting with SVG, only paint.
Workbench UX owns toolbar, sheet navigation, and authoring controls.
```

M33 does not claim full IEC compliance. It uses IEC 60617, IEC 61082, IEC 81346, and IEC 81714 as
reference anchors for the first electrical symbol package, document preparation, reference
designation, and graphical symbol design. Acceptance is based on deterministic proof plus
screenshot evidence, not taste-only claims.

## Product Thesis

The product problem is no longer "can Athena render a graph?" The problem is:

```text
Can Athena render a professional engineering sheet that a customer does not dismiss as a toy?
```

M32 created the package boundary:

```text
Engineering Package
  -> Presentation Profile
  -> Binding Manifest
  -> Representation Package
  -> Presentation IR
```

M33 must finish the drawing engine that consumes that boundary:

```text
Resolved representation descriptor
  -> engineering drawing symbol anatomy
  -> sheet composition plan
  -> Graphic Primitive IR
  -> SVG renderer adapter
  -> derived bounds and routes
  -> professional Workbench view
```

The M33 demo has one customer-facing product surface: Cabinet. Workbench UX cleanup is required for
customer credibility, but it remains separate from drawing engine authority. Documentation and
Schematic/Wiring remain internal compatibility projections only; they receive no M33 toolbar,
visual-polish, or screenshot acceptance scope.

## Problem

Current UI still reads as toy for five structural reasons:

1. **Drawing symbol language is too weak.** Generic rectangles and labels cannot express professional
   terminals, contacts, coils, lamps, breakers, motors, cross-references, and continuation markers.
2. **Graphic primitives are incomplete.** The visible layer lacks disciplined strokes, joins,
   dots, arcs, text slots, line crossings, reference markers, and deterministic bounds.
3. **Sheet composition is immature.** Professional engineering documents need title blocks,
   zones, rails, terminal strips, lane discipline, reference locations, and compact label placement.
4. **Workbench chrome is not product-clean.** Debug modes, overlapping controls, unstable
   dropdowns, and visible wrappers damage focus. These are Workbench UX problems, not renderer
   semantics.
5. **Proof is not strict enough.** Screenshots alone can hide stale DOM paths, hard-coded viewBox,
   duplicate labels, off-screen elements, and fallback symbol boxes.

## Goals

- Define Engineering Drawing Symbol Anatomy v1 as an Athena-owned representation contract backed
  by packages.
- Create a small professional IEC-style symbol package as the first implementation.
- Introduce Graphic Primitive IR v0 so representation output is renderer-neutral before SVG.
- Extend SVG renderer support so symbols are drawn from Graphic Primitive IR, not ad hoc JSX/SVG
  fragments.
- Build professional sheet composition v1: frame, title block, zones, rails, terminal strips,
  reference markers, dense label bands, and route discipline.
- Make Workbench UX focus exclusively on the Cabinet product surface.
- Prove package-backed symbols are actually used by the live graph, not just present in files.
- Remove or ledger toy fallback rendering paths, visible normal wrapper borders, duplicate labels,
  hard-coded viewBox values, center-fallback routes, and dead UI controls.
- Establish visual quality gates: structured DOM/proof checks, screenshot evidence, and reference
  comparison notes.

## Non-Goals

- No full IEC 60617 compliance claim.
- No EPLAN, QElectroTech, AutoCAD Electrical, or vendor-library parity.
- No full QET `.elmt` importer.
- No symbol editor.
- No package marketplace or package authoring UI.
- No new `.athena` syntax for symbols, resources, coordinates, visual profiles, or renderer
  primitives.
- No semantic-kernel ownership of graphic primitives or vendor visual assets.
- No broad multi-view product polish beyond what is needed to keep the primary view clean.
- No 3D, PDF release package, revision workflow, or print engine beyond a screen-rendered sheet
  frame proof.

## Functional Requirements

### Feature 1 - Engineering Drawing Symbol Anatomy v1

**FR-1:** M33 shall define Engineering Drawing Symbol Anatomy v1 as a frontend-neutral
representation contract.

**FR-2:** Symbol Anatomy v1 shall model symbol identity, package identity, domain/profile tags,
version, lifecycle state, primitives, anchor roles, terminal roles, label slots, reference slots,
hotspots, bounds, orientation support, and provenance.

**FR-3:** Symbol Anatomy v1 shall not contain engineering truth, source mutation behavior,
renderer-specific DOM assumptions, or `.athena` syntax.

**FR-4:** Each symbol shall declare required and optional anchors. Missing required anchors shall
produce structured diagnostics and block successful demo proof.

**FR-5:** Each symbol shall declare label slots for device tag, terminal number, rating/model,
cross-reference, location, and status where applicable.

**FR-6:** Symbol bounds shall be derived from declared primitives and slots, then verified against
descriptor bounds. Hard-coded sample canvas bounds shall not be accepted as proof.

### Feature 2 - Native IEC-Style Symbol Package v1

**FR-7:** M33 shall provide an Athena-owned IEC-style representation package as the first
implementation of Engineering Drawing Symbol Anatomy v1.

**FR-8:** The v1 symbol set shall include at least: power supply marker, protective breaker/fuse,
terminal, switch/contact, relay coil, lamp/indicator, motor/load, terminal strip segment,
connection dot, and folio/continuation reference.

**FR-9:** Symbols shall be authored as data-driven representation descriptors and renderer
primitive definitions, not Theia hard-coded JSX fragments.

**FR-10:** The M33 demo shall use no generic fallback rectangle for devices that have matching
symbol descriptors.

**FR-11:** Symbol package tests shall prove symbol identity, anchors, label slots, primitive count,
bounds, lifecycle metadata, and profile compatibility.

### Feature 3 - Graphic Primitive IR v0 And SVG Renderer Adapter

**FR-12:** M33 shall introduce Graphic Primitive IR v0 as the renderer-neutral scene layer between
Representation Descriptors and concrete renderers.

**FR-13:** Graphic Primitive IR v0 shall support line, polyline, arc, circle, rectangle where part
of symbol anatomy, text, marker, connection dot, reference arrow, group, transform, bounds, and
style token.

**FR-14:** Graphic Primitive IR v0 shall not contain engineering truth, package resolution
behavior, source mutation behavior, DOM selectors, or SVG-specific ids as authority.

**FR-15:** M33 shall implement an SVG renderer adapter that consumes Graphic Primitive IR v0.

**FR-16:** Graphic primitive rendering shall support stroke width, line cap, line join, dash
pattern, fill/transparent fill, text anchor, dominant baseline, rotation, and stable transforms.

**FR-17:** SVG renderer output shall derive SVG `viewBox` from actual resolved presentation bounds
plus governed margins.

**FR-18:** SVG renderer output shall not duplicate off-screen symbols, duplicate terminal labels,
or draw visible hitbox/background borders in normal state.

**FR-19:** Hover, selection, focus, and drag borders shall be transient Workbench interaction
chrome and must not appear in normal screenshot proof.

**FR-20:** Route terminals shall attach to descriptor-declared anchors. Center fallback shall be a
diagnostic, not accepted output for the customer demo.

### Feature 4 - Professional Sheet Composition v1

**FR-21:** M33 shall produce a professional sheet composition plan for the customer demo.

**FR-22:** Sheet composition v1 shall include frame, title block, zones/grid references, supply
rails, control lanes, terminal strip grouping, label bands, and continuation/reference placement.

**FR-23:** Sheet composition shall consume representation bounds and anchors, not source file
count, DOM size, or hard-coded sample viewBox constants.

**FR-24:** Composition output shall expose structured facts for lane membership, terminal strip
membership, zone, label band, route channel, reference target, and projected bounds.

**FR-25:** Composition shall prioritize visual density and scanability for industrial control
drawings: compact labels, no card layout, no decorative panels, no generic graph-node spacing.

**FR-26:** Composition diagnostics shall report collisions, missing anchors, out-of-sheet content,
excessive whitespace, and label overflow.

### Feature 5 - Package-Backed Live Graph Integration

**FR-27:** The live Workbench graph surface shall consume M32 package-resolved descriptors for
M33 symbols.

**FR-28:** The Workbench shall expose structured evidence that each rendered demo component came
from engineering package, presentation profile, binding manifest, representation package, and
symbol descriptor resolution.

**FR-29:** Presentation Profile shall remain first-class policy input. Profile switching may exist
only if it is stable and does not clutter the normal graph toolbar. If unstable, it shall be moved
behind debug/inspection affordance and ledgered.

**FR-30:** Package facts present in `examples/m33/sample-project` shall affect visible rendering or
be removed from the demo.

**FR-31:** Renderer and Theia code shall not infer package or engineering meaning from file names,
CSS classes, raw SVG ids, DOM text, or coordinate positions.

### Feature 6 - Workbench Product Surface Cleanup

**FR-32:** M33 shall define one customer-facing Workbench surface: Cabinet.

**FR-33:** Internal projection ids such as `documentation`, `schematic`, and `wiring` shall not
appear as peer product buttons in the normal toolbar.

**FR-34:** Documentation and Schematic/Wiring may remain only as hidden compatibility or inspection
projections. M33 shall not spend customer-facing UX or rendering-polish effort on them.

**FR-35:** Create Device shall either perform a governed semantic entity creation transaction with
visible result or be removed/disabled with explicit explanation. Dead placeholder buttons are not
allowed in the customer demo.

**FR-36:** Cabinet navigation, layout controls, and active state shall remain stable across graph
refreshes. They shall not depend on switching to Documentation or Schematic/Wiring.

**FR-37:** Debug/proof controls shall be grouped under inspection affordances and must not fill the
normal canvas toolbar with line-like buttons.

### Feature 7 - Customer Demo And Quality Gates

**FR-38:** M33 shall include `examples/m33/sample-project` using Athena-owned assets only.

**FR-39:** The Cabinet demo shall render a professional rolling-shutter control cabinet with
package-backed devices, protective equipment, relay/control equipment, terminal strip, motor/load
interfaces, status indication, rails, labels, and governed references.

**FR-40:** The demo shall include structured proof for package resolution, symbol anatomy, Graphic
Primitive IR output, sheet composition, route anchoring, text placement, bounds, viewBox, and no
fallback rendering.

**FR-41:** The demo shall include screenshot proof across at least desktop IDE viewport and a
narrower viewport where toolbar and sheet controls remain usable.

**FR-42:** M33 shall define a visual review checklist comparing the demo qualitatively against
professional electrotechnical drawing references without copying proprietary assets.

**FR-43:** Full smoke shall fail if the rendered demo contains visible normal wrapper borders,
duplicate labels, off-screen duplicate elements, hard-coded oversized viewBox, dead Create Device
panel, or hidden sheet navigation.

### Feature 8 - Polish And Purge Gate

**FR-44:** Every M33 story shall end with AC-to-evidence mapping.

**FR-45:** Every M33 story shall run a polish/purge review of touched and adjacent code, tests,
sample assets, package descriptors, docs, screenshots, compatibility paths, and stale UI controls.

**FR-46:** Stale or deferred artifacts shall be removed or recorded in the M33 cleanup ledger with
owner, reason, target milestone, and verification status.

## Non-Functional Requirements

**NFR-1:** `.athena` remains semantic truth. M33 shall not add visual syntax.

**NFR-2:** Standard and vendor visual elements are managed through packages, descriptors, and
profiles, not kernel code or semantic source.

**NFR-3:** Graphic Primitive IR is renderer-neutral. SVG is the first concrete renderer adapter,
not the drawing authority.

**NFR-4:** The renderer is paint-only. Symbol/profile/package selection remains upstream.

**NFR-5:** Workbench UX is separate from drawing engine authority. Theia shall not own renderer
authority, package authority, semantic meaning, or source mutation planning.

**NFR-6:** Visual credibility is a release criterion. Architecture-only success is not enough.

**NFR-7:** Determinism is required: same source, packages, profile, and viewport constraints shall
produce the same proof facts.

**NFR-8:** Invalid anchors, labels, descriptors, packages, or sheet bounds fail closed with
diagnostics.

**NFR-9:** M33 shall use standards as reference anchors, not unverifiable compliance claims.

**NFR-10:** Gradle verification commands shall run sequentially on Windows.

**NFR-11:** Repository text shall remain UTF-8, and Chinese docs touched in future shall preserve
UTF-8 with BOM.

## Core Acceptance Scope

M33 is complete only when all outcomes work together:

1. At least ten IEC-style symbol definitions render through package-backed descriptors and Graphic
   Primitive IR.
2. The M33 sample uses those symbols in the live Workbench graph surface with no generic fallback
   boxes.
3. The sheet has professional frame, title block, zones, rails, terminal strip, references, and
   compact labels.
4. Routes attach to descriptor anchors, not component centers.
5. `viewBox` and canvas framing derive from actual resolved bounds.
6. Normal state has transparent hitboxes and no wrapper borders.
7. Create Device and Cabinet navigation/layout controls are either usable or removed from the
   customer demo surface.
8. Workbench controls are stable and separated from drawing engine authority.
9. Structured proof and Electron screenshot proof both pass.

## Success Metrics

**SM-1:** A controls engineer can open the M33 sample and see a professional Cabinet layout, not
generic graph boxes.

**SM-2:** Symbol anatomy tests prove at least ten IEC-style symbols with anchors, slots, bounds,
and Graphic Primitive IR output.

**SM-3:** Product proof shows every demo component resolved through package/profile/manifest/
descriptor evidence.

**SM-4:** Renderer proof shows no hard-coded oversized viewBox, off-screen duplicate elements,
duplicate labels, visible normal wrapper borders, or center-fallback routes.

**SM-5:** Sheet composition proof shows frame, title block, zones, rails, terminal strips, route
channels, labels, and references.

**SM-6:** Workbench toolbar is stable, focused only on Cabinet, and does not expose Documentation,
Schematic/Wiring, debug, or projection chaos in normal mode.

**SM-7:** Create Device is either working with visible semantic result or removed/disabled from the
demo with explicit product rationale.

**SM-8:** Sequential verification and encoding audit pass after changes.

### Counter-Metrics

**SM-C1:** Do not optimize for symbol quantity. Ten excellent symbols beat one hundred weak ones.

**SM-C2:** Do not hide toy rendering behind package architecture.

**SM-C3:** Do not copy QET/EPLAN assets or claim parity.

**SM-C4:** Do not allow screenshot-only proof.

**SM-C5:** Do not let fallback rendering count as successful professional rendering.

## Acceptance Criteria

- Dedicated M33 PRD, addendum, architecture, epics, sprint, story, retrospective, and cleanup
  artifacts follow established BMAD milestone structure.
- Engineering Drawing Symbol Anatomy v1 contract exists and is testable without Theia.
- Native IEC-style symbol package v1 exists with at least ten demo-ready symbols.
- Graphic Primitive IR v0 supports required linework, text, anchors, dots, arcs, and reference
  markers, and SVG adapter consumes it.
- Professional composition v1 produces the M33 Cabinet demo.
- Live Cabinet surface consumes package-backed symbols and hides Documentation/Schematic/Wiring
  product controls.
- M33 Electron smoke opens the sample and proves layout, controls, symbols, routes, bounds, and
  normal chrome.
- Full smoke fails on toy regressions.
- Cleanup ledger records removed/deferred renderer, graph-view, and package/UI debts.

## Open Questions

1. Should M33 include a minimal package browser?
   - **Recommendation:** No. Keep package browser deferred unless needed for proof inspection.
2. Which view is the M33 customer-facing product surface?
   - **Resolved:** Cabinet only. Documentation and Schematic/Wiring remain hidden compatibility.
3. Should M33 implement QET importer prototype?
   - **Recommendation:** No. Use QET only as visual reference and future importer research.
4. Should Create Device be kept in the demo?
   - **Recommendation:** Keep only if it creates a governed semantic entity and refreshes the
     graph visibly. Placeholder UI must be removed or disabled.
5. Is M33 the last ECAD-specific milestone?
   - **Resolved recommendation:** Yes. M33 uses IEC-style electrical drawing as first proof, but
     contracts must be generic engineering drawing contracts. M34 should move upward into
     Engineering Knowledge Runtime or Standards Platform.

## Assumptions Index

- **A-1:** M33 demo uses Athena-owned synthetic IEC-style assets.
- **A-2:** SVG remains the concrete render backend for M33, but renderer primitives and descriptors
  stay backend-neutral enough for future Canvas/PDF/Skia/WebGPU.
- **A-3:** M33 focuses on electrical/control schematic credibility first; other domains benefit
  later from the same representation discipline.
