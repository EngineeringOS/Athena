---
title: Athena M40 - Projection Reality
status: final
created: 2026-08-02
updated: 2026-08-02
---

# Athena M40 PRD

## Purpose

M40 establishes **Projection Reality** as the authoritative owner of engineering views. M39 fixed
the ownership boundaries of the Reality Graph; M40 now gives Projection its full substance:

```text
Engineering
    |
    v
Projection (views, sheets, occurrences, regions, reading order,
            projection rules, projection selection, projection constructs)
    |
    v
Spatial
    |
    v
Presentation
```

Composition is one capability inside Projection, not the milestone itself. The milestone objective:

> M40 establishes Projection Reality as the authoritative owner of engineering views.

## Product Thesis

Engineer writes meaning. Compiler projects facts. Theia paints facts.

Athena source remains the single source of truth. Projection is the view-specific engineering
document: what an engineer sees, in what order, grouped how - with no coordinates and no style.
Composition constructs (rails, rungs, terminal strips, groups, bundles) are one class of projection
fact, contributed by domain packages, not hardcoded in the kernel.

## What Changes After M39

M39 defined Projection as "view selection" and explicitly deferred composition vocabulary. M40
makes Projection a complete, authoritative reality:

- Projection model: view, sheet, occurrence, region, reading order, projection rules, projection
  selection;
- Projection constructs: domain-neutral `ProjectionConstruct` contract in the kernel, with concrete
  implementations contributed by domain packages (electrical first: rail, rung, branch, wire
  bundle, terminal strip, contact group, coil group) - exactly like M39 domain relation verbs
  (`power`/`control`/`earth`), declared by the active engineering domain package (M39 FR-3);
- Projection compiler: one thin typed Engineering -> Projection transformation;
- Spatial consumes Projection: the Spatial compiler derives placement, bounds, anchors, routes, and
  quality metrics from Projection facts; Projection never owns them;
- Density/collision/occupancy/label-pressure are **Spatial Quality metrics** - acceptance criteria,
  not an epic and not architecture.

M40 does not add a second user-facing DSL, a generic framework, routing optimization, or a label
engine.

## Scope

M40 must deliver:

- Projection model with view, sheet, occurrence, region, reading order, projection rules, and
  projection selection, all identity- and source-traceable;
- domain-neutral `ProjectionConstruct` contract in the kernel, with electrical package
  implementations (rail, rung, branch, wire bundle, terminal strip, contact group, coil group);
- one thin typed Engineering -> Projection transformation;
- Spatial consuming Projection: derived placement/bounds/anchor/route facts, with Projection
  holding no final coordinates;
- Spatial Quality metrics (density, collision, occupancy, label pressure) measured against the M39
  baseline (28 label collisions at the reference viewport) as acceptance evidence;
- one projection construct authority: the stale `:kernel:drawing-composition` Cabinet path is
  refactored into the new ownership model or retired (FR-10);
- a dedicated `examples/m40` project, an M40 verifier, screenshots under
  `_bmad-output/implementation-artifacts/m40/screenshots`, and an honest retrospective.

M40 must not deliver:

- Spatial Reality as a milestone (M41): placement and routing are consumed and extended, not the
  milestone deliverable;
- professional routing: lane optimization, bend minimization, crossing minimization, bundle/trunk
  routing, multi-sheet continuation (M45 per the roadmap);
- a label engine (M42/M44 territory per the roadmap);
- EPLAN/QET-level visual parity;
- AI auto-layout or semantic-assisted projection (M46 per the roadmap);
- Java2D or another renderer; XML runtime authority; SVG semantic authority;
- a second user-facing DSL or layout grammar (the M39 "grammar monster" lesson);
- electrical vocabulary hardcoded in the kernel; Projection stays domain-neutral;
- compatibility shims, milestone-named production classes, or stale `Evidence` /
  `ProfessionalDrawing` naming.

## Roadmap (One Reality Per Milestone)

| Milestone | Reality | Main Deliverable |
| --- | --- | --- |
| M39 | Reality Graph | Authority boundaries |
| **M40** | **Projection Reality** | Views, sheets, occurrences, projection constructs |
| M41 | Spatial Reality | Placement, routing, geometry |
| M42 | Presentation Reality | Styling, labels, visibility |
| M43 | Rendering Reality | SVG, Theia, PDF, Canvas |
| M44 | Projection Quality | Readability, density, metrics |
| M45 | Professional Routing | EPLAN-class routing |
| M46 | AI Projection | Semantic-assisted projection |

The M39 handoff items that were previously labeled "M40 composition" and "M41+ professional
routing" are re-bucketed by this roadmap: composition lands inside M40 Projection Reality;
professional routing moves to M45. `[DECIDED 2026-08-02: milestone owner accepted the roadmap
recommendations]`

## Functional Requirements

### Epic 1: Projection Model

**FR-1: Projection Is The Authoritative Owner Of Engineering Views.** Projection Reality owns the
view-specific engineering document: view, sheet, occurrence, region, reading order, projection
rules, and projection selection. It is created by the projection compiler and consumed downstream
as an immutable snapshot.

Consequences (testable):

- Every projection document has one view identity and one compiler authority.
- No projection fact is created or modified by Spatial, Presentation, or the renderer.

**FR-2: View And Sheet.** The projection compiler can represent views and sheets with identity,
membership, and source trace.

Consequences (testable):

- A sheet belongs to exactly one view and carries a stable sheet identity.
- Sheets group exactly their declared occurrences; an empty sheet fails with a plain diagnostic.
- A sheet exposes a grid reference system (rows, columns, cell references such as A1/B3) as
  Projection structure; grid facts carry no coordinates.
- A view with no sheets fails with a plain diagnostic.
- Every sheet contains at least one occurrence, directly or through a region or construct; a
  sheet with no occurrence anywhere fails with a plain diagnostic.

**FR-3: Occurrence Identity.** Every projected occurrence carries stable identity traceable to its
engineering subject.

Consequences (testable):

- An occurrence without an engineering source fails before Spatial.
- Duplicate occurrence identities are rejected with a named diagnostic.

**FR-4: Functional Region As Logical Section.** Engineers can group occurrences into functional
regions with one minimal declaration. A region is a logical document section, never a layout box.

Consequences (testable):

- A region groups exactly its declared occurrences by identity.
- A region has no placement, size, or style facts at the Projection level.
- An incomplete region (declared but empty, or referencing a missing occurrence) fails with a
  plain diagnostic.

**FR-5: Deterministic Reading Order.** Every projection document exposes a deterministic reading
order across regions and occurrences.

Consequences (testable):

- Reading order is a permutation of the declared sheets; within a sheet, occurrences follow
  declaration order inside regions, then region declaration order. A duplicate or unknown sheet
  entry fails with a plain diagnostic.
- The same source compiles to the same reading order on every run.

**FR-6: Projection Rules And Selection.** Projection owns the rules that select engineering facts
into a view and the selection result; selection is deterministic and domain-neutral.

Consequences (testable):

- Projection rules are represented as projection facts with identity, not hardcoded view logic.
- View declarations are the sole authoring surface for view selection in M40 source; no
  competing selection syntax coexists (the existing projection-policy surface for M40 source is
  retired or rewritten, not shimmed).
- The same source and rules produce the same selection on every run.

### Epic 2: Projection Constructs

**FR-7: Domain-Neutral ProjectionConstruct Contract.** The kernel owns a `ProjectionConstruct`
contract only. It defines identity, source trace, membership, and validation shape; it names no
electrical, mechanical, or process vocabulary.

Consequences (testable):

- No `Rail`, `Rung`, `ContactGroup`, or other domain construct name appears in kernel production
  source (`kernel/*/src/main`, audited after the FR-10 retirement lands in the same epic).
- A new domain package can add constructs without changing kernel code.

**FR-8: Domain Packages Provide Construct Implementations.** The electrical package contributes
concrete construct implementations: `RailProjection`, `RungProjection`, `BranchProjection`,
`WireBundleProjection`, `TerminalStripProjection`, `ContactGroupProjection`, `CoilGroupProjection`.
Mechanical, P&ID, building, and robot packages may follow the same pattern in later milestones.

Consequences (testable):

- The M40 example resolves electrical constructs through the electrical package, exactly like
  `power`/`control`/`earth` resolve through the active domain package (M39 FR-3).
- Kernel compiles without dependency on any domain construct implementation.

**FR-9: Construct Validation.** Incomplete or ambiguous constructs fail before Spatial with plain
diagnostics.

Consequences (testable):

- A group referencing a missing occurrence, a coil without its device, a strip referencing a
  missing terminal, or a rung without an assigned occurrence produces a named diagnostic
  identifying the construct and the missing subject.
- An empty construct (zero members), a duplicate construct identity, a construct without source
  trace, and invalid construct nesting (e.g., a rung containing a rail) each fail with a named
  diagnostic before Spatial.

**FR-10: One Projection Construct Authority.** M40 leaves exactly one projection construct
authority. Disposition (decided 2026-08-02): **retire** the existing
`:kernel:drawing-composition` module and its production callers. The module
(`CabinetCompositionCompiler`, `CabinetRoutingCompiler`, `CabinetVisualTransformCompiler`,
`DrawingSheetCompositionCompiler`, `CabinetCompositionEvidence` naming) is wired into the build
(`settings.gradle.kts`) and bypasses the M39 reality chain: `AthenaProfessionalDrawingCompiler`
(called from `AthenaCompilerCompilationSupport.kt`) emits `authority = "drawing-composition"`
from Engineering directly; `AthenaCabinetProjectionCompiler` has no production callers. The
module, both compilers, and the LSP `AthenaDrawingCompositionPayload` surface are deleted before
M40 claims Projection constructs. Dependent tests and M34-M38 examples using the
`professional-connection-drawing` policy are rewritten to the current model or deleted per the
Pre-1.0 rule.

Consequences (testable):

- No production code outside the M40 model composes regions, rails, rungs, strips, or groups.
- The `deriveProfessionalControlDrawing` branch and the `professional-connection-drawing`
  projection-policy target surface in `AthenaCompilerCompilationSupport.kt` are removed, not
  left as silent no-ops.
- The module dependency in `kernel/compiler/build.gradle.kts` is removed with the module.
- Sheet-frame and title-block facts currently provided by the retiring module
  (`DrawingSheetFrameFact`, `DrawingSheetTitleBlockFact`) are replaced by M40 Projection/
  Presentation-owned facts before retirement, so metric inputs and sheet chrome survive.
- The Cabinet render-path deletion gate and M36/M37 cabinet tests are rewritten to the current
  model or deleted; no doc, test, or example references the retired path after cleanup.
- The M40 retrospective records the retirement explicitly.
- Stale `Cabinet` / `Evidence` / `ProfessionalDrawing` naming does not remain in production.
- Source-set hygiene and encoding audits pass at closure.

**FR-11: Grouped Endpoint Integrity.** Projection constructs must not change engineering truth:
grouping, regions, rails, and rungs reorganize *views* of the same engineering relationships
without altering connections, ports, or traces.

Consequences (testable):

- Engineering connections and their source traces are identical with and without projection
  declarations.
- A grouped endpoint (e.g., `earth A to [B, C]`) remains one traceable relationship through
  Projection, exactly as in M39.

**FR-12: Concrete Types, Not A Generic Framework.** Projection constructs are concrete typed
models under the `ProjectionConstruct` contract; no generic graph/fact framework is introduced.

Consequences (testable):

- No universal `Fact` base class, empty wrapper model, or generic graph API is introduced.

### Epic 3: Projection Compiler

**FR-13: Engineering To Projection Transformation.** Engineering Reality lowers to Projection
Reality through one thin typed transformation (`RealityTransformation<InputReality,
OutputReality>`), carrying views, sheets, occurrences, regions, reading order, and constructs.

Consequences (testable):

- The transformation accepts Engineering Reality and emits Projection Reality only.
- Source trace and engineering identity survive the transformation.

**FR-14: Deterministic Projection Compilation.** The same source and domain packages produce the
same projection snapshot on every run.

Consequences (testable):

- Projection compilation is deterministic and reproducible.

**FR-15: Projection Boundary Validation.** Projection outputs are validated before downstream
consumption; no coordinate, anchor, lane, route, stroke, label, or paint-order fact exists in a
Projection snapshot.

Consequences (testable):

- A projection snapshot containing spatial or presentation facts fails validation with a named
  diagnostic.

### Epic 4: Spatial Consumes Projection

**FR-16: Spatial Derives Placement From Projection.** The Spatial compiler derives placement,
bounds, anchors, and route facts from Projection occurrences and constructs (existing M39 spatial
machinery consumed and extended). Projection stores membership and reading order only.

Consequences (testable):

- Spatial output is traceable to projection construct and occurrence identities.
- Projection models store no final coordinates.
- Spatial placement maps each occurrence and construct to a sheet grid cell reference (e.g.,
  A1/B3) derived from its placement; grid references are Spatial-owned facts available to later
  export surfaces.

**FR-17: Composition Is Not Routing.** M40 does not perform lane optimization, bend minimization,
crossing minimization, or multi-sheet continuation.

Consequences (testable):

- No M40 code path claims or performs optimized routing; the M45 handoff list is unchanged.

**FR-18: Spatial Quality Metrics As Acceptance Evidence.** Density, collision, occupancy, and label
pressure are measured by the Spatial Quality Analyzer for composed sheets and reported against the
M39 baseline (28 label collisions at the reference viewport). These are acceptance measurements,
not a milestone epic.

Consequences (testable):

- Metric numbers are computed deterministically from Presentation Document label and route
  bounds (no pixels); screenshots confirm only visual state at the reference viewport. Metric
  definitions: density = occurrences per sheet area; occupancy = used/available sheet-region
  ratio; label pressure = label-label overlaps plus label-route intersections.
  Sheet-area facts come from the M40 replacement for the retired
  `DrawingSheetFrameFact`/`DrawingSheetTitleBlockFact` inputs.
- The M40 proof measures the same M40 example source with and without projection constructs at
  desktop 1920x1080 fit-to-screen zoom, plus the M39 baseline (28 label collisions) as
  cross-reference only; the primary comparison is M40-flat vs M40-composed.
- Label suppression is forbidden as a metric strategy: the full label set emitted by the
  Presentation Document is measured and the label count is reported.
- The M40 target (decided 2026-08-02): label collisions <= M39 baseline (28) at desktop
  1920x1080 fit-to-screen; route/body intersections = 0. Density, occupancy, and label pressure
  are reported as measured numbers. No label engine is added in M40 (deferred to M42/M44), so
  the target is honest non-regression plus measurement, not professional parity.

### Epic 5: Proof

**FR-19: Dedicated M40 Example.** A dedicated M40 example exists under `examples/m40`, exercises
Projection end to end, and does not reuse M36-M39 examples as proof authority.

Consequences (testable):

- The example compiles through all four realities with projection constructs present.
- The example declares at least one of each electrical construct (rail, rung, branch, wire
  bundle, terminal strip, contact group, coil group) and at least one functional region; the
  verifier asserts each appears in the projection snapshot.
- The example uses human-first relation syntax (`to` preferred) and no `intent` blocks.
- Proof artifacts cite only M40 paths.

**FR-20: M40 E2E Evidence.** Kernel, LSP, frontend, and product surfaces are rebuilt before
screenshots; screenshots are stored under `_bmad-output/implementation-artifacts/m40/screenshots`.

Consequences (testable):

- Screenshots exist for the example at the reference viewports and are non-blank.
- The verifier reports projection constructs, Spatial Quality metrics, and paint-only assertions.
- The LSP surface recompiles and validates M40 source without regression; M40 adds no new LSP
  protocol surface.

**FR-21: Paint-Only Renderer Boundary.** Theia and SVG export consume the same Presentation
Document and perform no projection, composition, placement, grouping, or routing inference.

Consequences (testable):

- The M40 verifier asserts the renderer does not create views, regions, rails, rungs, or groups.
- No renderer code path snaps endpoints, reroutes, relabels, or interprets engineering domains.

**FR-22: Honest M40 Closure.** The M40 retrospective records what improved, what remains visually
poor, and which failures move to later milestones.

Consequences (testable):

- The retrospective compares M40 output against the M39 baseline and the QElectroTech composition
  reference without claiming EPLAN/QET parity.
- Routing, label-engine, and parity items are explicitly deferred per the roadmap.

## Non-Functional Requirements

**NFR-1:** K.I.S.S. is binding: human-first names, direct concepts, small source language, no
compiler-shaped authoring.

**NFR-2:** Athena source must be LLM-friendly: regular forms, explicit names, low ambiguity, and low
boilerplate.

**NFR-3:** Clean architecture wins over compatibility. Athena is pre-public; old designs have no
compatibility rights.

**NFR-4:** No stale source, docs, examples, tests, or compatibility branches may remain if they
violate the current M40 model - including the stale Cabinet composition path.

**NFR-5:** XML is out of the active runtime path unless explicitly used as temporary import/input.

**NFR-6:** SVG may provide package-local geometry and stable geometry references. SVG must not own
engineering facts.

**NFR-7:** The kernel is domain-neutral: no electrical, mechanical, process, or building vocabulary
in kernel production source. Domain packages contribute vocabulary through contracts, exactly like
the M39 domain relation verbs (`power`/`control`/`earth`) declared by the active engineering
domain package.

**NFR-8:** Visual claims must be honest. M40 may claim authoritative Projection ownership and
measured Spatial Quality, not professional drawing parity.

**NFR-9:** Diagnostics must name the exact subject, problem, and correction in plain engineering
language, per the Human-First Language Rule; "plain/named diagnostic" consequences in this PRD
mean a pinned message template with subject, problem, and correction.

## User-Facing Syntax Target

*Committed M40 syntax (decided 2026-08-02).*

```athena
view schematic {
  sheet S1
  region "Power Distribution" { occurrences [Supply, Breaker] }
  region "Control Logic" { occurrences [StartButton, Drive] }
  reading-order [S1]
}

# construct words resolve through the electrical package, like connection verbs:
power-rail L1
rung L1 [Supply.L1, Breaker.line]
terminal-strip X1 [Drive.L, Drive.N, Drive.enable, Drive.PE]
```

The proposal keeps one intent per line, adds no `intent` blocks, and stays readable to LLMs and
humans. Construct words are domain-provided; the kernel names no rail, rung, or strip. Placement,
geometry, and paint remain derived. All seven construct forms are committed:

```athena
power-rail L1
rung L1 [Supply.L1, Breaker.line]
branch [Supply.L1, Drive.L]
wire-bundle B1 [L1, N]
terminal-strip X1 [Drive.L, Drive.N, Drive.enable, Drive.PE]
contact-group KM1 [13, 14, 21, 22]
coil-group KM1 [A1, A2]
```

`[DECIDED 2026-08-02: syntax committed; authored/derived split = Option A (durable intent
authored, geometry and paint derived)]`

## Acceptance Criteria

1. Projection Reality is the authoritative owner of engineering views: view, sheet, occurrence,
   region, reading order, projection rules, and projection selection exist with identity and
   trace, and no projection fact owns coordinates or style.
2. Composition is one projection capability: `ProjectionConstruct` is a domain-neutral kernel
   contract, and the M40 example resolves electrical constructs through the electrical package
   without kernel vocabulary.
3. Engineering -> Projection runs through one thin typed transformation, deterministically and
   with source trace preserved.
4. Spatial consumes Projection: placement, bounds, anchors, and routes are derived in Spatial;
   Projection holds no final coordinates.
5. Spatial Quality metrics (density, collision, occupancy, label pressure) are measured and
   reported against the M39 baseline (28 label collisions) at desktop 1920x1080 fit-to-screen
   zoom; M40 target (decided 2026-08-02): label collisions <= 28 non-regression, route/body
   intersections = 0, with exact measured values published.
6. Exactly one projection construct authority exists: the stale `:kernel:drawing-composition`
   Cabinet path is refactored or retired, and hygiene audits pass.
7. The dedicated M40 example compiles through all four realities, uses human-first `to` syntax and
   no `intent`, and cites only M40 paths.
8. M40 screenshots exist under `_bmad-output/implementation-artifacts/m40/screenshots` after
   rebuilding kernel, LSP, frontend, and product surfaces.
9. The M40 retrospective honestly records improvement, remaining visual debt, and deferrals
   without claiming professional drawing parity.
10. Source-set hygiene audit, UTF-8 encoding audit, and `git diff --check` pass.
11. Counter-metric: composition must not make authoring worse - count authored declaration lines
    (view, sheet, region, and construct declarations) per authored connection relation in the
    `examples/m40` source; the ratio must not exceed the M39 example ratio, and projection-compile
    time stays within 2x the M39 example compile time on the same machine, measured as the median
    of three runs (decided 2026-08-02).

## Decisions (confirmed 2026-08-02)

Milestone owner accepted all recommendations on 2026-08-02. The PRD is final.

1. **Authored vs derived split per construct: Option A.** Durable engineering intent is authored -
   regions, terminal strips, contact/coil groups, and rail/rung membership; geometry, placement,
   and paint stay derived. Rejected: all-derived (regions and reading order are engineering
   decisions that belong in source) and full diagram grammar (the M39 "grammar monster" lesson).
2. **Projection declaration syntax: committed.** `view` blocks declare sheets, regions, and
   reading order; construct words (`power-rail`, `rung`, `branch`, `wire-bundle`,
   `terminal-strip`, `contact-group`, `coil-group`) resolve through the electrical package
   exactly like M39 domain relation verbs (FR-3). All seven declaration forms are specified in
   the Syntax Target. No second DSL, no layout grammar.
3. **M40 density target: non-regression.** Label collisions <= M39 baseline (28) at desktop
   1920x1080 fit-to-screen; route/body intersections = 0; density/occupancy/label pressure
   reported as measured numbers.
4. **Example subject: rolling-shutter control.** Matching the QElectroTech reference
   (`draft/screenshort/equipement_d'un_volet_roulant.png`) as the visual composition target.
5. **Disposition of `:kernel:drawing-composition`: retire.** Evidence: the module is wired into
   `settings.gradle.kts` and `kernel/compiler/build.gradle.kts`; `AthenaProfessionalDrawingCompiler`
   is called from `AthenaCompilerCompilationSupport.kt` and emits
   `authority = "drawing-composition"`, bypassing the M39 reality chain;
   `AthenaCabinetProjectionCompiler` has no production callers. Retire all three production
   surfaces and the LSP `AthenaDrawingCompositionPayload`; replace sheet-frame/title-block facts
   with M40-owned facts; rewrite or delete dependent tests and M34-M38 examples per the Pre-1.0
   rule.
6. **Epic breakdown: adopted.** Epic 1 Projection Model (FR-1..FR-6), Epic 2 Projection
   Constructs (FR-7..FR-12), Epic 3 Projection Compiler (FR-13..FR-15), Epic 4 Spatial Consumes
   Projection (FR-16..FR-18), Epic 5 Proof (FR-19..FR-22). Stories follow numeric order.
7. **Roadmap: adopted.** One reality per milestone: M41 Spatial Reality, M42 Presentation
   Reality, M43 Rendering Reality, M44 Projection Quality, M45 Professional Routing, M46 AI
   Projection.
8. **Projection rules surface (FR-6): view declarations are the sole M40 authoring surface.**
   The existing projection-policy selection surface is retired or rewritten, not shimmed.
9. **M39 `layout`/`place` migration: placement is derived in M40.** Existing `place`
   declarations are dropped from M40 example source; Spatial derives placement. M39 examples keep
   their syntax but are no longer proof authority; the retrospective records the migration.

## Handoff From M39

Keep:

- the four reality ownership boundaries and the typed transformation chain;
- exact endpoint equality and source trace through the chain;
- paint-only Theia/SVG boundary and atomic Presentation Document;
- the measured Spatial quality baseline (8 routes, 0 route/body intersections, 28 label
  collisions);
- the human DSL (`to` preferred, `->` alias, domain relation verbs, no `intent`);
- domain-neutral kernel via domain-contributed vocabulary (M39 relation verbs, FR-3);
- pre-1.0 cleanup authority and milestone hygiene rules.

Change:

- Projection becomes the full authoritative owner of engineering views (not just view selection);
- `ProjectionConstruct` contract added to the kernel, with domain-package implementations;
- Spatial consumes and validates against the full Projection snapshot;
- resolve the stale `:kernel:drawing-composition` authority (refactor or retire);
- adopt the QElectroTech rolling-shutter reference as the visual composition target for proof.

## Handoff To M41+

Per the adopted roadmap (decided 2026-08-02):

- M41 (Spatial Reality): placement, routing, geometry as the milestone deliverable.
- M42 (Presentation Reality): styling, labels, visibility; label engine contract lands in M42
  and label readability metrics in M44.
- M43 (Rendering Reality): SVG, Theia, PDF, Canvas.
- M44 (Projection Quality): readability, density, metrics.
- M45 (Professional Routing): EPLAN-class routing.
- M46 (AI Projection): semantic-assisted projection.

The M40 Spatial Quality baseline is the starting truth for M41 and M44.

## Recorded Requirement: Excel Position Export (deferred)

Recorded 2026-08-02 from milestone-owner input: the product must export each component/element's
position on the overall canvas as a grid reference (e.g., A1/B3) plus center coordinates, in a
spreadsheet/Excel form similar to a component position list.

Deferral decision:

- **M40**: sheet grid reference system only (FR-2); grid-cell mapping of placements is derived in
  Spatial (FR-16). No Excel export is built in M40.
- **M41 (Spatial Reality)**: owns the placement and grid-reference facts that feed the export.
- **M43 (Rendering/Export)**: owns the Excel/position-list export surface, consuming the same
  Spatial facts.

The exact export columns (component tag, grid reference, center x/y, sheet) are fixed at M41/M43
planning, not guessed now.
