---
title: Athena M43 Presentation and Rendering Reality
status: final
created: 2026-08-05
updated: 2026-08-06
---

# PRD: Athena M43 Presentation and Rendering Reality

## 0. Document Purpose

This PRD defines the M43 product contract for turning validated engineering meaning into a usable
live document surface in Theia. It is milestone-local and feeds the M43 architecture spine,
epics, stories, tests, and closure evidence. M42 remains the authority for Engineering Knowledge
and Validation documents. M43 adds no compatibility layer for retired M0-M41 presentation paths.

## 1. Vision

M43 gives an engineer a professional, inspectable live engineering document in Theia. The user
sees a clean white page with a plot-frame-style numeric top border, alphabetic left border, symbols,
relationships, labels, terminals, and routed geometry. The document remains traceable to Athena
source and to the canonical occurrence identity that produced each visible item.

One immutable Presentation Reality contains every paint fact, stable identity, hit identity, and
source trace. One interactive adapter displays and edits that scene. Deterministic SVG and PNG proof
consume the same scene without becoming a second live authority. No renderer infers engineering
meaning, reconstructs geometry, or repairs invalid source.

M43 deliberately separates engineering authoring from presentation authoring. A project source file
owns Engineering Reality. A colocated `*.sheet.athena` companion owns sheet format, grid profile,
visibility intent, and authored occurrence placement constraints. Exact geometry remains
compiler-owned. Missing or ambiguous Sheet Companions produce an explicit absence state; no default
or legacy layout is guessed.

## 2. Target Users

### 2.1 Jobs To Be Done

- Engineers can open a real project and read its engineering document without reconstructing layout
  by hand.
- Engineers can move an important occurrence, lock it, recompile, and retain that authored intent.
- Engineers can connect two compatible Ports through a validated source command, without Canvas state
  becoming engineering truth.
- Reviewers can select a visible item and navigate to its project source and semantic subject.
- Integrators can consume one deterministic Presentation Reality without duplicating layout logic.
- Maintainers can reject malformed sheet input with plain, actionable diagnostics.

### 2.2 Non-Users In M43

- Users seeking automatic engineering solution generation or macro/pattern instantiation.
- Users seeking AI engineering decisions or AI-authored source mutation.
- Users seeking PDF, print, report, BOM, or other export workflows.
- Users seeking procurement, lifecycle, simulation, or standards-compliance automation.
- Users seeking full EPLAN database or file compatibility.

### 2.3 Key User Journeys

**UJ-1. Maya opens a professional live schematic.** Maya opens the controlled rolling-shutter
example in Theia. The product discovers the colocated project and sheet companion, compiles them with
the existing Engineering Reality, and shows one clean page with row letters, column numbers, symbols,
terminal references, labels, and relationship geometry. Selecting an occurrence shows its source
trace.

**UJ-2. Maya authors one stable move.** Maya drags `Q1` to cell `C3` and micro anchor `(2,1)`,
locks it, and drops. The product writes a validated Sheet Companion edit and recompiles. The compiler
may adjust derived routes and labels, but the authored anchor remains. Occurrence identity and source
trace remain unchanged.

**UJ-3. Nikhil fixes a sheet error.** Nikhil writes `cell: 6`, references an unknown row/cell, or
uses `micro(5,1)` while `cell: 4` is active.
The compiler rejects the sheet companion with a plain diagnostic naming the sheet, field, and
correction. The product does not render a guessed layout or silently fall back to a legacy profile.

**UJ-4. Maya connects two Ports.** Maya starts at an output Port and drops on a compatible input Port.
The product previews the gesture, submits stable Port identities and the current scene revision, and
writes engineering source only after validation. An incompatible direction produces a source-linked
diagnostic and leaves the accepted scene unchanged.

## 3. Glossary

- **Engineering Reality** - Canonical project entities, functions, ports, relationships, authored
  facts, and validated references. It owns engineering truth.
- **Projection Reality** - Domain-neutral views, sheets, regions, occurrences, and reading-order
  intent derived from Engineering Reality.
- **Spatial Reality** - Validated placement, anchors, bounds, routes, lanes, and geometry metrics.
- **Presentation Reality** - One immutable renderer-neutral scene containing page facts, semantic
  element metadata, logical geometry, stable hit identities, and source trace.
- **Sheet Companion** - A colocated `*.sheet.athena` source file containing presentation intent and
  authored placement constraints.
- **Layout Cell** - A page reference cell addressed by a row letter followed by a column number.
- **Micro Anchor** - One of `N x N` logical snap positions inside a Layout Cell when `cell: N` is
  active.
- **Diagram Edit Command** - A typed, revision-checked user intent submitted from the live scene for
  source validation and recompilation.
- **Occurrence** - A projection instance of an upstream engineering subject on a sheet.
- **Authored Placement Constraint** - A human-owned cell/micro anchor and optional lock state; never
  an exact renderer coordinate.
- **Pattern** - A future reusable engineering solution authority. M43 may display Pattern-produced
  subjects but does not define or instantiate Patterns.

## 4. Features

### 4.1 Sheet Companion Authoring

**Description:** Engineers can colocate a Sheet Companion beside project source. The source uses
human-first terms and one scalar grid setting. The compiler validates all authored presentation
intent before producing downstream realities.

#### FR-1: Discover Colocated Sheet Companion

Athena can discover `project.sheet.athena` beside `project.athena` and associate it with the same
workspace without merging their authority.

**Consequences (testable):**
- Missing companions produce an explicit `Sheet Companion unavailable` diagnostic and no
  Presentation Reality; no default or legacy loader is consulted.
- Multiple companions for one project are rejected as ambiguous.
- IDE tree grouping does not change compiler input identity.

#### FR-2: Author Page And Grid Intent

An engineer can author page format, orientation, `grid: C * R`, and one `cell: N` value in the Sheet
Companion.

**Consequences (testable):**
- Rows and columns are presentation references only.
- `N` must be a positive integer multiple of 4.
- Each Layout Cell has an implicit `N x N` Micro Anchor lattice; no second micro-grid declaration is
  accepted. `grid: 17 * 16 cell: 4` produces `68 x 64` logical subdivision units.
- A page profile can use a different cell count without changing the engineering source.

#### FR-3: Author Occurrence Placement Constraints

An engineer can place a known occurrence with direct syntax such as `"Supply" at A2`, add optional
`micro(x,y)` where `x` and `y` are in `1..N`, and lock that placement.

**Consequences (testable):**
- Unknown occurrence, row, column, or micro index produces a source-linked diagnostic.
- `lock` persists as authored intent; it does not freeze compiler-derived routes or labels.
- Exact x/y pixels, renderer paths, device scale, and route vertices are not valid Sheet Companion
  authority.

### 4.2 Deterministic Projection And Spatial Compilation

**Description:** The compiler combines Engineering Reality, Projection Reality, and Sheet Intent to
produce one deterministic Spatial Reality. Existing M41 engineering and spatial semantics remain
valid; M43 adds the Sheet Companion mapping and authored-placement application.

#### FR-4: Produce Deterministic Initial Layout

The compiler can place every eligible occurrence without authored placement using a deterministic
profile-specific strategy.

**Consequences (testable):**
- Identical inputs produce identical occurrence anchors, bounds, routes, and diagnostic ordering.
- Initial layout never creates engineering relationships or changes Engineering Reality.
- Unresolved or invalid engineering subjects are never displayed as validated facts.

#### FR-5: Apply Authored Anchors And Locks

The compiler can apply authored cell/micro anchors and lock state before deriving exact geometry.

**Consequences (testable):**
- Authored positions survive recompilation and unrelated source reordering.
- Locked occurrences are not moved by initial-layout passes.
- Derived geometry may change around a locked occurrence while the anchor remains stable.

#### FR-6: Validate Spatial Boundaries

The compiler can validate grid references, page bounds, occurrence overlap policy, anchor validity,
and required spatial coverage before publishing Presentation Reality.

**Consequences (testable):**
- Invalid sheet references and non-divisible `cell: N` fail closed with exact source provenance.
- Bounds and blocking overlap violations produce deterministic diagnostics.
- No partial or guessed Presentation Reality is published after a blocking failure.

### 4.3 Live Editable Theia Document Surface

**Description:** Theia displays and edits one canonical Presentation Reality through one interactive
adapter. Interaction, source tracing, resize behavior, and source mutation are product behavior, not
test-only proof.

#### FR-7: Render One Presentation Reality

Theia can render a clean page frame, top numeric and left alphabetic coordinate border, occurrence
symbols, Ports, labels, routes, and relationship geometry from one published Presentation Reality.

**Consequences (testable):**
- One scene revision supplies all paint and hit identities.
- The surface is nonblank, uses available widget space, and remains correctly framed without twisting
  or unexplained whitespace at desktop and narrow viewports.
- Interior macro/micro grid lines, bottom title tables, and feature banners are absent from default
  paint. A future alignment overlay is viewport-only.
- No frontend code evaluates engineering rules or reconstructs frame, symbols, routes, or geometry
  from raw source.

#### FR-8: Select And Trace Occurrences

An engineer can select a visible occurrence and navigate to its canonical subject and project source
location.

**Consequences (testable):**
- Selection and keyboard focus resolve the scene's stable occurrence identity through one hit-testing
  owner.
- Selection never changes Engineering Reality.
- Missing trace metadata is a compiler/product error, not a frontend fallback.

#### FR-9: Refresh On Source Change

Theia can refresh the live document when project or Sheet Companion source changes.

**Consequences (testable):**
- One atomic revision is visible; mixed old/new element revisions are not observable.
- Invalid edits show diagnostics and retain the last valid revision only where the existing runtime
  contract explicitly permits it; no legacy fallback is introduced.
- Repeated identical edits produce identical visible ordering and trace IDs.

#### FR-12: Edit Through Validated Scene Commands

An engineer can drag an occurrence to a Sheet Anchor, persist lock intent, and connect compatible
Ports without making renderer state authoritative.

**Consequences (testable):**
- Every command carries the expected scene revision and stable typed subject identities; a stale
  command is rejected without source mutation.
- Move writes structured `*.sheet.athena` intent, participates in existing editor undo/redo, and
  becomes visible only after successful recompile.
- Port connection writes the owning project `*.athena` only after relationship direction and semantic
  validation; failure restores the accepted scene and shows an actionable diagnostic.
- Hover, selection, pan, zoom, and gesture preview remain transient UI state.

### 4.4 Product Evidence And Closure

#### FR-10: Prove Rolling-Shutter Surface

Athena can open an M43-local rolling-shutter project and show the approved professional schematic
surface with screenshot evidence.

**Consequences (testable):**
- Evidence lives under `_bmad-output/implementation-artifacts/m43`.
- Evidence includes active source, visible Theia surface, occurrence selection/trace, and desktop and
  narrow-viewport screenshots, move persistence, Port connection or rejection, and scene revision.
- The proof uses no M0-M42 demo or sample project.

#### FR-11: Preserve Active-Path Hygiene

M43 can close with no new production Proof, Demo, Sample, milestone-named, compatibility, or hidden
layout-authority classes.

**Consequences (testable):**
- Source-set hygiene audit passes.
- Encoding audit and repository tests pass.
- Retired presentation paths are deleted or directly migrated; no aliases or fallback readers remain.

## 5. Non-Goals

- Engineering Pattern or Macro language, variants, automatic solution generation, or provider choice.
- User-facing PDF/SVG/PNG export commands, print pagination, BOM, terminal reports, or manufacturing
  documents. Renderer-parity SVG and PNG proof remain in scope.
- AI chat, AI reasoning, AI-authored mutation, or automatic correction execution.
- New electrical ontology, vendor database, EPLAN import, or proprietary EPLAN compatibility.
- Simulation, routing optimization research, procurement, lifecycle, commissioning, or maintenance.
- A second scene model, layout authority, or simultaneous live interaction owner. Future renderer
  adapters may consume the same canonical scene only after measured need.

## 6. MVP Scope

### 6.1 In Scope

- Colocated `*.athena` and `*.sheet.athena` discovery and independent authority.
- Page profile, clean plot-frame-style rows/columns, and `grid: C * R cell: N` syntax.
- `N x N` micro anchor lattice per Layout Cell, where `N` is a positive multiple of 4.
- Authored occurrence anchors and lock state.
- Deterministic initial placement and M41 spatial integration.
- One renderer-neutral Presentation Reality with one live interaction owner, selection, trace, drag,
  lock, validated Port connection, and atomic refresh.
- Deterministic SVG oracle, environment-pinned PNG proof, and recorded 100,000-element scale fixture.
- M43-local rolling-shutter product proof, screenshots, tests, and hygiene closure.

### 6.2 Out Of Scope For MVP

- Pattern system and macro migration: later knowledge milestone.
- PDF/export: later presentation milestone.
- AI assistance: later reasoning milestone.
- Full EPLAN/QElectroTech file import: later integration work.

## 7. Cross-Cutting Requirements

### NFR-1: Determinism

Identical Engineering Reality, Sheet Companion, profile, and compiler version produce byte-stable
published documents, stable occurrence IDs, stable geometry ordering, and stable diagnostics.

### NFR-2: Authority Safety

Renderer, scene adapter, SVG/PNG proof, Theia state, and layout cache never create or repair
engineering truth.
Missing, ambiguous, invalid, or out-of-bounds intent fails closed.

### NFR-3: Human-First Diagnostics

Every blocking Sheet or Spatial diagnostic names the sheet, field, occurrence, problem, and correction
direction, with navigable source provenance.

### NFR-4: Interaction Consistency

Paint, hit testing, selection, focus, source trace, and commands expose one stable identity set and one
scene revision. No parallel live interaction layer may diverge.

### NFR-5: Product Responsiveness

The controlled rolling-shutter proof remains interactive during pan, zoom, selection, drag, Port
connection, and source refresh on supported Theia desktop and narrow viewports. A named-hardware
100,000-element fixture records load time, settled memory, visible-node count, input latency, and
frame-time percentiles. Athena publishes no large-project performance claim beyond recorded evidence.

### NFR-6: Pre-1.0 Breaking Policy

M43 removes superseded active presentation contracts. No aliases, adapters, compatibility flags, or
fallback layout readers are added.

## 8. Success Metrics

### Primary

- **SM-1:** Rolling-shutter proof shows a nonblank, clean framed page with numeric top border,
  alphabetic left border, symbols, labels, terminals, routes, traceable occurrences, and no interior
  grid or bottom table. Validates FR-7, FR-10.
- **SM-2:** Moving and locking one occurrence survives recompile without identity drift. Validates
  FR-3, FR-5, FR-8, FR-9, FR-12.
- **SM-3:** Invalid `cell: 6`, unknown `B99`, and `micro(5,1)` under `cell: 4` fail closed with source-linked
  diagnostics. Validates FR-2, FR-3, FR-6.
- **SM-4:** Repeated identical compilation produces identical Presentation Reality and screenshots
  at the same viewport. Validates FR-4, FR-7, NFR-1.

### Secondary

- **SM-5:** Interactive paint, deterministic SVG, and PNG proof resolve the same scene revision,
  occurrence IDs, geometry, and source trace. Validates FR-7, FR-8, NFR-4.
- **SM-6:** M43 active path passes source-set hygiene, encoding, repository tests, and product E2E.
  Validates FR-11, NFR-6.
- **SM-7:** One valid Port connection persists through engineering source and recompile; one invalid
  direction leaves source unchanged with exact diagnostics. Validates FR-12, NFR-2, NFR-4.
- **SM-8:** A 100,000-element scene benchmark passes on recorded reference hardware: first stable
  paint <=5 seconds, incremental heap <=512 MiB, pan/zoom/drag p95 <=50 ms, selection p95 <=100 ms,
  and zero hit/trace/error failures. Validates NFR-5; failure blocks M43 closure and triggers adapter
  correction before a future Pixi decision.

### Counter-Metrics

- **SM-C1:** Number of new layout keywords stays minimal; more syntax is not success.
- **SM-C2:** Number of renderer-side engineering decisions remains zero.
- **SM-C3:** Number of compatibility shims and legacy fallback readers remains zero.
- **SM-C4:** PDF/export and Pattern code added to M43 remains zero.

## 9. Open Questions

1. Exact physical unit mapping from logical `cell: N` to each page format remains an implementation
   detail of the selected sheet profile; it must not change the authored coordinate contract.
2. Accepted performance budgets for product claims beyond the M43 controlled proof require measured
   customer-scale workloads; M43 records baseline evidence first.

## 10. Assumptions Index

- No phase-blocking assumptions remain. Missing companions fail closed, `cell: N` owns subdivision
  count, and one canonical scene owns live paint and hit identity.
