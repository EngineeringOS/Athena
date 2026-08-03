# M40 PRD Addendum

## Why Projection Reality, Not "Composition"

PM feedback (2026-08-02) refocused the milestone: composition is only one capability inside
Projection. Projection owns view, sheet, occurrence, region, reading order, projection rules,
projection selection, and projection constructs. Naming the milestone "Projection Reality" keeps
every milestone aligned to one architectural authority (see roadmap below) and prevents composition
from growing into a separate pseudo-reality.

## The Composition Reference

`draft/screenshort/equipement_d'un_volet_roulant.png` is a QElectroTech gallery drawing of a
rolling-shutter control (1050x720). The draft folder README identifies the gallery images as
renderer-layer references. M40 uses this drawing as the visual composition target for proof: rails,
rungs, grouped contactor logic, terminal strips, and readable density - not a pixel-parity target.

## Options Considered

### Authored vs derived projection constructs

- **Option A (recommended):** durable engineering intent is authored (region, strip, group,
  rail/rung membership); geometry and placement are derived by Spatial. Source stays small and
  human-first.
- **Option B:** all constructs are derived from existing devices/relations. Cheapest source, but
  regions and reading order are engineering decisions that belong in source.
- **Option C:** a full diagram grammar. Rejected: this is the M39 "grammar monster" lesson; it
  would put layout mechanics in authoring syntax.

### Kernel-neutral vs kernel-owned construct vocabulary

- **Option A (recommended, per PM feedback):** the kernel owns the `ProjectionConstruct` contract
  only; domain packages contribute `RailProjection`, `RungProjection`, `TerminalStripProjection`,
   and so on - the same pattern as M39 domain relation verbs (`power`/`control`/`earth`) declared
   by the active engineering domain package (M39 FR-3). Mechanical, P&ID, building, and robot
   domains may contribute their own constructs later.
- **Option B:** kernel hardcodes electrical constructs. Rejected: violates M39 AD-8/AD-2 domain
  neutrality and repeats the old "kernel knows electrical" smell.

### Density as architecture vs acceptance evidence

- **Option A (recommended, per PM feedback):** density/collision/occupancy/label pressure are
  Spatial Quality metrics measured by a Spatial Quality Analyzer and reported as acceptance
  evidence, not a milestone epic.
- **Option B:** density as a primary M40 epic. Rejected: metrics are not architecture; they
  describe quality of the projection/spatial output.

### Roadmap (one reality per milestone)

Accepted from PM feedback (pending milestone-owner sign-off):

| Milestone | Reality | Main Deliverable |
| --- | --- | --- |
| M39 | Reality Graph | Authority boundaries |
| M40 | Projection Reality | Views, sheets, occurrences, projection constructs |
| M41 | Spatial Reality | Placement, routing, geometry |
| M42 | Presentation Reality | Styling, labels, visibility |
| M43 | Rendering Reality | SVG, Theia, PDF, Canvas |
| M44 | Projection Quality | Readability, density, metrics |
| M45 | Professional Routing | EPLAN-class routing |
| M46 | AI Projection | Semantic-assisted projection |

## Diagram Grammar Handoff Context

The M39 addendum deferred rail, rung, branch, terminal strip, bus, functional region, contact
group, coil group, and wire bundle to the milestone after reality ownership is clean. M40
introduces them as domain-contributed Projection constructs, not as a new grammar.

## Decisions To Confirm Before Finalize

1. Projection declaration syntax (PRD Syntax Target proposal).
2. Authored vs derived split per construct.
3. M40 density target number.
4. Example subject (rolling-shutter recommended).
5. Epic breakdown for sprint planning.
6. Disposition of the existing `:kernel:drawing-composition` Cabinet path (FR-10): the module is
   wired into `settings.gradle.kts`, called from `AthenaCabinetProjectionCompiler` and
   `AthenaProfessionalDrawingCompiler`, and still carries `CabinetCompositionEvidence` naming.
7. Roadmap sign-off (M41-M46 one-reality-per-milestone sequence).

## Decisions Confirmed (2026-08-02)

Milestone owner accepted all recommendations. The PRD is final. Full rationale lives in the PRD
"Decisions (confirmed 2026-08-02)" section and this addendum's options analysis:

1. **Authored vs derived: Option A.** Durable intent authored; geometry/paint derived. B and C
   rejected (B: regions/reading order are engineering decisions; C: the M39 "grammar monster"
   lesson).
2. **Syntax: committed.** `view` blocks + domain-resolved construct words, all seven construct
   forms specified in the Syntax Target, mirroring M39 relation verbs.
3. **Density target:** label collisions <= 28 (M39 baseline) at desktop 1920x1080 fit-to-screen;
   route/body intersections = 0; other metrics reported as measured. No label engine in M40.
4. **Example subject:** rolling-shutter control system matching the QElectroTech reference.
5. **`:kernel:drawing-composition`: retire.** `AthenaProfessionalDrawingCompiler` emits
   `authority = "drawing-composition"` from `AthenaCompilerCompilationSupport.kt`, bypassing the
   M39 reality chain; `AthenaCabinetProjectionCompiler` has no production callers. Delete module,
   compilers, LSP payload surface; replace sheet-frame/title-block facts with M40-owned facts;
   rewrite/delete dependent tests and M34-M38 examples.
6. **Epics:** 1 Projection Model, 2 Projection Constructs, 3 Projection Compiler, 4 Spatial
   Consumes Projection, 5 Proof.
7. **Roadmap:** M41 Spatial, M42 Presentation, M43 Rendering, M44 Projection Quality, M45
   Professional Routing, M46 AI Projection.

## Recorded Requirement: Excel Position Export (2026-08-02)

Milestone-owner input: component/element position on the overall canvas must be exportable to
Excel as a grid reference (A1/B3 style) plus center coordinates, like a component position list.

Decision: deferred, split across milestones to keep ownership clean:

- M40 provides the sheet grid reference system as Projection structure (FR-2) and derives
  grid-cell mappings of placement in Spatial (FR-16).
- M41 owns placement and grid-reference facts.
- M43 owns the Excel/position-list export surface.

No Excel export is built in M40; export columns are fixed at M41/M43 planning, not guessed now.
