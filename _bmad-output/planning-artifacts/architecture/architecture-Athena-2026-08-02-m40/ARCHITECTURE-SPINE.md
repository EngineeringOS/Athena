---
name: 'Athena M40 - Projection Reality'
type: architecture-spine
purpose: build-substrate
altitude: feature
paradigm: Reality Graph pipeline; Projection is the authoritative owner of engineering views
scope: M40 Projection Reality milestone - view/sheet/occurrence/region/reading order/projection rules/selection, domain-neutral projection constructs, thin Engineering->Projection transformation, Spatial consumption, Spatial Quality acceptance metrics, proof
status: final
created: 2026-08-02
updated: 2026-08-02
binds:
  - FR-1..FR-22
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-02-m40/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md
companions: []
---

# Architecture Spine — Athena M40: Projection Reality

## Design Paradigm

M40 inherits the M39 paradigm: human source, concrete realities, paint-only renderer. M40 adds one
idea — **Projection is the authoritative owner of engineering views; composition is one projection
capability**:

```text
Athena Source
  -> Engineering Reality        (what the system is)
  -> Projection Reality         (views, sheets, occurrences, regions, reading order,
                                 projection rules/selection, projection constructs; no coordinates)
  -> Spatial Reality            (derived placement/bounds/anchors/routes + Spatial Quality metrics)
  -> Presentation Reality       (paint facts; no repair)
  -> Theia / SVG                (paint only)
```

Composition is a set of concrete, domain-contributed Projection constructs under a kernel-owned
`ProjectionConstruct` contract - not a framework and not a second DSL.

## Inherited Invariants

The M39 architecture spine's decisions bind here read-only, by their original IDs. A local M40
decision that contradicts one of these is a conflict to surface, not an override.

- **M39 AD-1:** Reality Graph is a pipeline, not a framework; no generic graph, universal `Fact`
  base class, or empty wrapper models for projection.
- **M39 AD-2:** Engineering Reality owns engineering truth; projection never changes connections,
  ports, or traces.
- **M39 AD-3:** Projection owns view selection; no coordinates, anchors, lanes, routes, strokes,
  labels, or paint order; a region is a logical document section, not a layout box.
- **M39 AD-4:** Spatial owns geometry result; routing is a Spatial subsystem; placement and
  quality facts are Spatial-owned.
- **M39 AD-5:** Presentation owns paint facts; projection is never painted directly.
- **M39 AD-6:** Transformations stay thin; no preserved/derived/discarded metadata in M40.
- **M39 AD-7:** Renderer cannot repair truth; Theia/SVG consume the same Presentation Document;
  no projection inference.
- **M39 AD-8:** Human names are product architecture; no milestone names, `V0`/`V1`, or vague
  `Evidence` naming in production.

Standing repo invariants also apply: no `intent` in normal source; `to` preferred over `->` (same
relation through one path); domain packages contribute vocabulary (M39 relation verbs, FR-3);
pre-1.0 cleanup is allowed and expected; Gradle verification on Windows runs strictly sequentially.

## Invariants & Rules

```mermaid
flowchart LR
    SRC[Athena Source] --> ENG[Engineering Reality]
    ENG --> PROJ[Projection Reality<br/>views, sheets, occurrences, constructs]
    PROJ --> SPAT[Spatial Reality<br/>derived placement + quality metrics]
    SPAT --> PRES[Presentation Reality<br/>paint facts]
    PRES --> THEIA[Theia / SVG export - paint only]
```

### AD-9 — Projection Is The Authoritative Owner Of Engineering Views

- **Binds:** FR-1 through FR-6; FR-11
- **Prevents:** projection being a passive view passthrough; views being assembled by Spatial,
  Presentation, or the renderer; projection changing engineering truth
- **Rule:** Projection Reality owns view, sheet, occurrence, region, reading order, projection
  rules, and projection selection. The projection compiler is the only creator of projection
  snapshots; downstream realities consume them immutably. Grouped endpoints remain one
  relationship through Projection (FR-11). A sheet exposes a grid reference system (rows,
  columns, cell references such as A1/B3) as Projection structure; grid facts carry no
  coordinates (FR-2).

### AD-10 — Composition Is One Projection Capability

- **Binds:** FR-1 through FR-12
- **Prevents:** composition growing into a separate pseudo-reality
- **Rule:** Projection constructs are projection facts with identity and trace, no different in
  kind from sheets or regions. M40 is the Projection Reality milestone; composition is its
  showcase capability, not a milestone boundary.

### AD-11 — ProjectionConstruct Is A Domain-Neutral Kernel Contract

- **Binds:** FR-7, FR-8, FR-11; NFR-7
- **Prevents:** electrical (or any domain) vocabulary hardcoded in the kernel
- **Rule:** The kernel owns the `ProjectionConstruct` contract only: identity, source trace,
  membership, validation shape. Domain packages provide implementations (`RailProjection`,
  `RungProjection`, `TerminalStripProjection`, `ContactGroupProjection`, `CoilGroupProjection`,
  `WireBundleProjection`, `BranchProjection` from the electrical package; mechanical, P&ID,
  building, and robot domains may follow). The kernel compiles without domain construct
  dependencies, exactly like M39 domain relation verbs (FR-3). Construct identity preserves grouped
  endpoint integrity (FR-11).

### AD-12 — Functional Regions Are Logical Sections, Not Layout Boxes

- **Binds:** FR-4
- **Prevents:** layout semantics leaking into authoring or Projection
- **Rule:** A functional region groups occurrences by identity and has no placement, size, or style
  facts at the Projection level, exactly as M39 AD-3 permits.

### AD-13 — Spatial Consumes Projection; Geometry Stays Spatial

- **Binds:** FR-16, FR-18
- **Prevents:** Projection storing final coordinates; a second placement owner; unverifiable
  density claims
- **Rule:** The Spatial compiler derives placement, bounds, anchors, and routes from Projection
  occurrences and constructs. Projection stores membership and reading order only. Spatial Quality
  metrics are measured by the Spatial Quality Analyzer against the M39 baseline.

### AD-14 — Density And Quality Metrics Are Acceptance Evidence, Not Architecture

- **Binds:** FR-18; Acceptance Criteria 5
- **Prevents:** metrics becoming a milestone epic or architectural surface
- **Rule:** Density, collision, occupancy, and label pressure are Spatial Quality measurements
  reported by the analyzer. M40 targets a milestone-owner-set number against the M39 baseline
  (28 label collisions); metrics never become a reality or a primary epic.

### AD-15 — Constructs Are Concrete Types With Validation

- **Binds:** FR-9, FR-12
- **Prevents:** a generic composition engine or universal construct base; silent construct errors
- **Rule:** Each construct is a concrete typed model under the `ProjectionConstruct` contract. No
  generic graph API, universal base type, or empty wrapper is introduced. Empty constructs,
  duplicate identities, missing source traces, and invalid nesting fail with named diagnostics
  before Spatial.

### AD-16 — M40 Performs No Routing Optimization

- **Binds:** FR-17; Non-Goals; roadmap M45
- **Prevents:** scope creep into professional routing
- **Rule:** M40 never performs lane optimization, bend minimization, crossing minimization,
  bundle/trunk routing, or multi-sheet continuation. Professional routing is M45 per the roadmap.

### AD-17 — M40 Proof Is Milestone-Local

- **Binds:** FR-19 through FR-22
- **Prevents:** M36-M39 examples becoming M40 proof authority; unverifiable closure
- **Rule:** M40 uses a dedicated `examples/m40` project, an M40 verifier, screenshots under
  `_bmad-output/implementation-artifacts/m40/screenshots`, and an honest retrospective citing only
  M40 paths.

### AD-18 — M40 Leaves One Projection Construct Authority

- **Binds:** FR-10; FR-7 through FR-12; source-set hygiene
- **Prevents:** a second construct authority surviving in production
- **Rule:** Before M40 claims Projection constructs, the existing `:kernel:drawing-composition`
  Cabinet path (`CabinetCompositionCompiler`, `CabinetRoutingCompiler`,
  `CabinetVisualTransformCompiler`, `CabinetCompositionEvidence`) is retired (decided
  2026-08-02, evidence in PRD FR-10). Sheet-frame and title-block facts are replaced by
  M40-owned facts before retirement so metric inputs and sheet chrome survive. Stale `Cabinet` /
  `Evidence` / `ProfessionalDrawing` naming does not survive in production.

### AD-19 — Projection Compiler Is Thin And Boundary-Validated

- **Binds:** FR-13, FR-14, FR-15
- **Prevents:** projection compilation drifting from the thin typed transformation; spatial or
  presentation facts leaking into Projection snapshots
- **Rule:** Engineering Reality lowers to Projection Reality through one
  `RealityTransformation<InputReality, OutputReality>`; compilation is deterministic; projection
  snapshots containing coordinate, anchor, lane, route, stroke, label, or paint-order facts fail
  validation with a named diagnostic.

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Naming | Human-first construct names via domain packages (`RailProjection`, `RungProjection`, `TerminalStripProjection`, ...); no milestone names, `V0`/`V1`, or `Evidence` in production (inherited AD-8) |
| Data & formats | Construct/view/sheet/occurrence identity + source trace follow the M39 pattern; reality snapshots immutable; grouped endpoints remain one relationship through Projection |
| State & cross-cutting | Reality mutation only through typed transformations (inherited AD-6); renderer performs no repair (inherited AD-7); Gradle verification stays strictly sequential on Windows |
| Errors | Incomplete regions, strips, groups, or rungs fail before Spatial with plain diagnostics naming the construct and the missing subject |
| Boundary validation | Projection snapshots carrying spatial or presentation facts fail with named diagnostics (FR-15, AD-19) |

## Stack

Seed - verified current in the repository at authoring; the code owns these once it exists. No new
stack is introduced by M40.

| Name | Version |
| --- | --- |
| Kotlin | 2.4.0 (repo toolchain) |
| Gradle | 9.6.1 |
| Node.js | >=22 |
| Yarn | 1.22 |
| TypeScript | 5.9 |
| Theia | 1.73.1 |
| Electron | 39.8.7 |
| graph-glsp | current repo integration |

## Structural Seed

```text
kernel/
  projection-model/        # view, sheet, occurrence, region, reading order, ProjectionConstruct contract
  spatial-model/           # derived placement/bounds/anchors/routes + Spatial Quality metrics
  presentation-model/      # paint facts + paint order
  compiler/                # typed transformations + projection boundary validation
extensions/domain-electrical/   # RailProjection, RungProjection, TerminalStripProjection, ... (domain contributions)
examples/m40/              # dedicated rolling-shutter control example (PRD Decision 4)
ide/                       # unchanged paint-only Theia/SVG surfaces
```

## Capability → Architecture Map

| Capability / Area | Lives in | Governed by |
| --- | --- | --- |
| Projection model (FR-1..FR-6) | kernel/projection-model + compiler | AD-9, AD-12 |
| Projection constructs (FR-7..FR-12) | kernel/projection-model (contract) + extensions/domain-electrical (implementations) + compiler | AD-10, AD-11, AD-15, AD-18 |
| Projection compiler (FR-13..FR-15) | kernel/compiler | AD-9, AD-19 |
| Spatial consumption (FR-16..FR-18) | kernel/spatial-model + compiler | AD-13, AD-14, AD-16 |
| E2E proof + honest closure (FR-19..FR-22) | examples/m40 + verifier + artifacts | AD-17 |

## Deferred

- Spatial Reality as a milestone (placement, routing, geometry as the deliverable) - M41 per the
  roadmap; M40 only consumes Spatial for proof.
- Presentation Reality (styling, labels, visibility) - M42.
- Rendering Reality (SVG, Theia, PDF, Canvas) - M43.
- Projection Quality milestone (readability, density, metrics) - M44; M40 measures a baseline only.
- Professional routing (lane/bend/crossing/bundle/multi-sheet) - M45, enforced by AD-16.
- AI projection (semantic-assisted projection) - M46.
- Transformation metadata (preserved/derived/discarded) - still deferred from M39 AD-6.
- Projection declaration syntax and authored-vs-derived split - decided 2026-08-02 (Option A,
  committed syntax); not deferred.
- M40 density target vs the M39 baseline - decided 2026-08-02 (non-regression, <= 28 collisions).
- Disposition of `:kernel:drawing-composition` - decided 2026-08-02 (retire, AD-18).
- Example subject - decided 2026-08-02 (rolling-shutter control).
- Deployment/operations envelope - unchanged from M39 (desktop Theia product); not owned at this
  altitude.
- Excel position export (component center positions as grid references plus coordinates) -
  recorded requirement 2026-08-02; Spatial-owned facts land in M41, export surface in M43; M40
  provides only the sheet grid reference system.
