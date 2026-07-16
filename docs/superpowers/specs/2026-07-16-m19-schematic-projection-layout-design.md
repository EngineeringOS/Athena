# M19 Design: Schematic Projection And Layout Workflow

## Purpose

Define a bounded M19 milestone that makes Athena more credible to end users without turning the
milestone into a full EPLAN clone, a complete IEC symbol library, or a public package repository
project.

M19 should prove a customer-facing engineering workflow while preserving Athena's core principle:
semantic meaning stays upstream, projection/view contracts translate meaning into engineering
views, and rendering is the final presentation layer rather than a source of truth.

## Recommended Milestone Shape

M19 should be a **schematic-first projection and layout workflow**.

The primary proof is:

1. Open a governed Athena project.
2. Select a schematic sheet view in the Theia IDE.
3. See a professional, sheet-based electrical schematic view derived from canonical semantics.
4. Select a component, terminal, conductor, or cross-reference on the rendered sheet.
5. See the same canonical subject reflected in inspector, source/navigation, diagnostics, and
   related references.

A cabinet/layout view may appear as a secondary read-only preview only if it reuses the same
projection and identity contracts cleanly. It must not become a second main milestone thread.

## Scope

In scope:

- one schematic sheet workflow that feels materially closer to QElectroTech/EPLAN references
- projection-owned sheet/page data such as frame, grid coordinates, title block, view family, and
  stable occurrence ids
- limited layout rules for a schematic sheet: lanes, anchors, labels, conductor segments, and
  cross-reference markers
- renderer improvements needed to draw the schematic sheet with professional density
- Theia IDE integration for source, canvas, outline, problems, inspector, and selection/reveal
  coherence
- a small fixture set based on existing Athena semantic/component knowledge, not a new broad
  catalog
- explicit backlog documentation for public repository/import ecosystem work

Out of scope:

- full EPLAN UI parity
- unrestricted graphical authoring
- full cabinet layout intelligence
- full IEC library ingestion
- full QElectroTech `.elmt` compatibility
- broad component/elements marketplace behavior
- public Maven/npm-style package repository
- remote registry, package publish, dependency upgrade, or trust infrastructure
- renderer-local semantic inference
- frontend-owned component, import, or symbol authority

## Why Schematic First

Schematic viewing is the best first customer-facing slice because it is more semantic than physical
layout and easier to bound.

Schematic needs:

- components and terminals
- conductors and labels
- sheet coordinates
- cross-page/reference markers
- title blocks and page identity
- selection and inspection

Cabinet layout needs more:

- footprints
- enclosure geometry
- DIN rail and duct placement
- physical sizing and clearances
- collision rules
- mounting constraints
- richer vendor part data

M19 should not pay that full physical-layout cost yet. It should keep cabinet as a future expansion
or minimal preview, not the core promise.

## Reference Use

### Screenshot References

The files under `draft/screenshort/` are visual and workflow references. They show the target
quality bar:

- sheet frames and coordinate grids
- dense symbols and labels
- conductor labeling
- cross-page references
- topology and cabinet view families
- title blocks and project/page identity

They are not architecture authorities.

### Structurizr Autolayout Reference

`reference/structurizr/structurizr-autolayout/` is useful as a pattern, not as an M19 dependency.

The useful pattern is:

1. export a semantic/view model into a layout-friendly intermediate form
2. run a layout engine or layout rule set
3. read back positions/routes/page size
4. apply those facts to the view model

For Athena, the M19 equivalent should stay Athena-owned:

```text
Semantic model
  -> projection model
  -> schematic view model
  -> layout facts
  -> renderer output
```

M19 should not require Graphviz as a product dependency unless a later technical design proves that
the operational and determinism tradeoffs are acceptable.

### QElectroTech Elements Reference

`draft/elements-lib/0001-qelectrotech-elements.md` is useful for understanding element anatomy:

- visual primitives
- terminals
- hotspots
- dynamic text
- element metadata
- internationalized names

M19 should not ingest the full QElectroTech element library. It may define a tiny Athena-native
symbol subset inspired by those concepts, enough for the schematic proof. Full library ingestion
belongs to a later component/symbol-library milestone.

## Architecture

### Ownership Rule

M19 must preserve this chain:

```text
Engineering semantics
  -> projection family and sheet contracts
  -> schematic view model
  -> layout facts
  -> renderer
  -> Theia interaction
```

No downstream layer may reconstruct engineering truth from coordinates, SVG elements, DOM nodes, or
frontend stores.

### Primary Components

1. **Semantic source**
   - existing compiler/runtime semantic state
   - existing M14 component knowledge where available
   - existing M18 package-aware project state where relevant

2. **Projection contract**
   - sheet identity
   - view family identity
   - subject occurrence ids
   - cross-reference ids
   - notation/symbol selection ids

3. **Schematic view model**
   - page frame and grid coordinates
   - component symbol occurrences
   - terminal anchors
   - conductor route intents or segment facts
   - labels and dynamic text fields
   - title block values

4. **Layout layer**
   - deterministic layout rules for the chosen proof fixture
   - enough auto-placement to avoid hand-authored renderer JSON
   - no claim of general EPLAN-quality automatic layout

5. **Renderer**
   - draws the schematic view model
   - exposes rendered occurrence ids for selection
   - does not create semantic identity

6. **Theia IDE workflow**
   - sheet/canvas view
   - source navigation
   - Problems/diagnostics
   - inspector
   - outline or project tree
   - selection and reveal coherence

## Data Flow

```text
Authored source / governed project
  -> compiler/runtime semantic state
  -> projection session request: schematic sheet
  -> projection-owned schematic view model
  -> deterministic layout pass
  -> renderable schematic page
  -> Theia canvas
  -> selection event by occurrence id
  -> canonical semantic id
  -> inspector/source/diagnostics/references update
```

The critical invariant is that selection on the rendered view resolves back to canonical semantic
identity through projection occurrence metadata, not through renderer object ids alone.

## Customer-Facing Workflow

The first M19 demo should be:

1. Start Athena IDE.
2. Open one governed proof project.
3. Open one `.athena` source file.
4. Open the schematic sheet view.
5. Inspect the title block, page grid, symbols, conductors, labels, and references.
6. Click a device or terminal on the schematic.
7. Inspector shows canonical subject identity, component knowledge, ports, and related diagnostics.
8. Source/navigation highlights or reveals the matching authored subject.
9. Click a conductor or cross-reference marker.
10. Related reference information appears without renderer-local semantic inference.

This is a viewer/inspection workflow. Editing may remain limited or absent in M19 unless it falls
through existing M15 guided authoring and M8 mutation authority without broad new UI work.

## Testing Strategy

Required proof surfaces:

1. **Projection model tests**
   - schematic view model derives from canonical semantic subjects
   - sheet, occurrence, label, and cross-reference ids are stable

2. **Layout tests**
   - deterministic layout output for the proof fixture
   - no renderer-local semantics required to place core subjects

3. **Renderer tests**
   - generated page includes frame, grid, symbols, terminals, conductors, labels, and title block
   - rendered occurrences carry selection ids

4. **LSP/runtime tests**
   - schematic projection request returns the same canonical identities used by source/navigation

5. **Theia frontend tests**
   - selection on rendered schematic updates inspector/source/reveal state
   - Problems/diagnostics remain compiler/LSP-owned

6. **Documentation and boundary audit**
   - M19 artifacts clearly defer full EPLAN parity, full IEC library ingestion, cabinet layout
     intelligence, and package ecosystem work

## Backlog Items From This Discussion

Public repository/import ecosystem:

- local package cache
- versioned package metadata
- private registry
- publish/consume flow
- dependency upgrade and lockfile diff
- public registry/search/trust

Symbol/component ecosystem:

- QElectroTech `.elmt` parser or importer
- IEC-oriented symbol packs
- large component and manufacturer libraries
- symbol editor
- symbol versioning and localization

Physical cabinet layout:

- footprints
- mounting constraints
- rail/duct layout
- collision and clearance rules
- physical layout optimization

## Risks And Controls

### Risk: M19 becomes an EPLAN clone attempt

Control:
Limit the milestone to one schematic viewing workflow and one proof fixture. Cabinet remains
secondary or deferred.

### Risk: Layout becomes a hidden semantic authority

Control:
Layout may place occurrences and routes, but it may not decide what a component, terminal,
conductor, or reference means.

### Risk: Element library work swallows the milestone

Control:
Use a tiny Athena-native symbol subset for M19. Treat QElectroTech and IEC references as design
inputs, not ingestion scope.

### Risk: Customer-facing UI work bypasses Athena architecture

Control:
All rendered selection and reveal flows resolve through projection occurrence ids back to canonical
semantic identity.

### Risk: The first demo still looks too naive

Control:
Spend visual effort on the professional sheet cues customers recognize: page frame, grid, title
block, labels, conductor routing, terminals, and cross references. Do not spend effort on broad UI
ornament.

## Success Criteria

M19 is successful when:

- Athena can show one credible schematic sheet in the Theia IDE.
- The schematic is generated from semantic/projection contracts, not from hand-authored renderer
  JSON.
- Selection and reveal connect rendered subjects to inspector, source, diagnostics, and references.
- The visual result clearly improves beyond the current naive canvas.
- The milestone leaves clean extension points for later cabinet layout, symbol libraries, and
  package ecosystem work.
- No public registry, full EPLAN clone, full IEC library, or renderer-owned semantic shortcut enters
  the implementation.

## Recommendation

Proceed with M19 as a **schematic-first projection and layout milestone**.

Use cabinet/layout, Structurizr autolayout, QElectroTech elements, and IEC libraries as references
and future backlog inputs. Do not make them the milestone center.
