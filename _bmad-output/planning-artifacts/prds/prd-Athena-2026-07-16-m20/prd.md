---
title: Athena M20 - Engineering Presentation Fidelity Foundation
status: draft
created: 2026-07-16
updated: 2026-07-16
---

# PRD: Athena M20 - Engineering Presentation Fidelity Foundation

## 0. Document Purpose
M20 follows M19 by proving that Athena can present governed semantic engineering meaning as a
professional sheet artifact that engineers will accept. This is not a semantic milestone and not a
generic UI polish milestone. It is the first serious validation of the human-facing presentation
layer.

## 1. Vision
M19 proved Athena can generate and synchronize a schematic sheet from governed semantics. M20 must
prove the result is presentation-grade: readable, professionally composed, and recognizable as an
engineering document instead of a graph surface with decorations.

The milestone should improve visible presentation fidelity, engineering drawing rules, sheet
composition, viewport choreography, and interaction steadiness while keeping semantic authority
upstream and avoiding any new protocol or layout-stack decision.

## 1.1 Why Now
Athena has reached a dangerous point: technology credibility exists, but user perception can still
fail. If the sheet feels naive, customers will not care that the semantics are correct.

M20 is the right time to validate the last layer in the chain:

```text
Semantic Engineering Model
    -> Projection
    -> Presentation IR
    -> Sheet Layout Model
    -> Renderer
    -> Human Engineering Experience
```

The work must stay focused on presentation acceptability, not on reworking the semantic model.

## 2. Target User

### 2.1 Jobs To Be Done
- Aaron needs to open the schematic sheet and immediately trust the visual result.
- Maya needs to move through source, Problems, and the sheet without the surface fighting back.
- Priya needs to confirm Athena now looks like a serious engineering workspace, not only a semantic
  proof.

### 2.2 Non-Users (v1)
- Teams expecting a new semantic model
- Teams expecting cabinet preview authoring
- Teams expecting repository/import ecosystem work
- Teams expecting a final layout-stack decision in M20

### 2.3 Key User Journeys
- **UJ-1. Aaron opens the schematic sheet and judges whether it is professional.**
  - **Persona + context:** Aaron is looking at the first customer-facing engineering sheet.
  - **Entry state:** A governed project is open in Theia.
  - **Path:** Aaron opens the schematic sheet, scans the title block, reads the labels, and checks
    for collisions, awkward density, or visual noise.
  - **Climax:** The sheet reads like a real engineering artifact rather than a rough diagram.
  - **Resolution:** Aaron keeps working instead of mentally downgrading the tool.

- **UJ-2. Maya moves between source, Problems, and the sheet without losing orientation.**
  - **Persona + context:** Maya is using Athena as a working IDE.
  - **Entry state:** A schematic sheet and its source are open.
  - **Path:** Maya selects a subject, follows reveal, zooms, and returns to source or Problems.
  - **Climax:** The viewport and selection behavior feel predictable and steady.
  - **Resolution:** Maya trusts the workflow enough to keep iterating.

## 3. Glossary
- **Presentation Fidelity** - The degree to which the rendered sheet reads as a professional
  engineering artifact.
- **Engineering Drawing Rules** - Placement, grouping, spacing, and routing conventions that make
  the sheet read correctly to an engineer.
- **Sheet Layout Model** - A governed contract between Presentation IR and the renderer that carries
  engineering layout rules without moving semantic authority downstream.
- **Representation Family** - A view-oriented grouping of how one governed engineering subject is
  presented for a given sheet purpose.
- **Viewport Choreography** - The open, fit, zoom, pan, and selection motion of the sheet surface.
- **Sheet Surface** - The Theia-facing sheet view that users inspect directly.
- **Canonical Subject** - The semantic engineering object that stays authoritative across surfaces.
- **Proof Corpus** - Small executable fixtures that demonstrate presentation acceptability and
  regression safety.

## 4. Features

### 4.1 Engineering Sheet Composition Model
**Description:** Athena should present the sheet as a governed composition of frame, title block,
views, zones, representations, and occurrences. The sheet must feel intentionally assembled for
engineering work, not simply rendered from graph nodes.

#### FR-1: Make the first view read as a professional engineering sheet
Athena can present a schematic sheet whose frame, title block, labels, routes, and
cross-reference markers are visually coherent and readable.
**Consequences (testable):**
- The sheet avoids obvious collisions and cramped placement.
- The title block and frame read cleanly at first open.
- The surface feels intentional, not accidental.

#### FR-2: Model sheet composition and representation families explicitly
Athena can project governed semantics into a sheet composition model before rendering.
**Consequences (testable):**
- Sheet, frame, title block, zones, views, and occurrences remain distinct concepts.
- Representation families can be grouped by engineering purpose instead of raw pixel state.
- The renderer consumes composed sheet facts, not ad hoc frontend guesses.

### 4.2 Deterministic Sheet Layout Model
**Description:** Athena should apply engineering drawing rules through a sheet layout model so that
presentation is deterministic and readable. The layout model is not the final stack decision; it is
the contract that makes the renderer predictable.

#### FR-3: Introduce a governed sheet layout model
Athena can carry layout facts and engineering drawing rules between Presentation IR and rendering.
**Consequences (testable):**
- Placement, grouping, spacing, and routing rules are explicit rather than implicit.
- The same governed input yields the same layout facts.
- The layout model does not become a second semantic authority.

#### FR-4: Encode engineering drawing rules for dense content
Athena can keep dense schematic content readable through deterministic layout rules.
**Consequences (testable):**
- Labels and routes remain understandable on dense fixtures.
- Obvious overlaps or clutter are reduced.
- Engineering relationships remain visible instead of buried by the layout.

### 4.3 Professional Renderer Experience
**Description:** Athena's Theia surface should feel steady and trustworthy while using the new
presentation rules. Source, Problems, and sheet navigation still need to agree, but the sheet itself
should now feel more like a professional engineering artifact.

#### FR-5: Keep viewport choreography stable
Athena can preserve readable layout and selection visibility as users open, fit, zoom, pan, and
navigate the sheet.
**Consequences (testable):**
- Open, fit, and zoom actions do not create jarring jumps.
- Selection remains visible and understandable after viewport changes.
- Layout does not visibly drift between repeated runs on the same input.

#### FR-6: Keep source, Problems, and sheet interactions aligned during presentation refinement
Athena can preserve canonical reveal and selection behavior while the sheet presentation is improved.
**Consequences (testable):**
- Source and Problems still navigate to the same canonical subject.
- Presentation polish does not break cross-surface identity.
- No frontend-local semantic inference is introduced.

### 4.4 Proof Corpus And Regression Safety
**Description:** Athena can prove the presentation-fidelity change with a small local fixture set and
executable tests.

#### FR-7: Publish a small presentation-acceptance proof corpus
Athena can demonstrate professional layout, viewport choreography, and selection coherence through a
small fixture set and executable tests.
**Consequences (testable):**
- The proof set stays small and local.
- The proof corpus covers readable layout, not just semantic identity.
- Repeated runs produce stable visual and interaction results.

### 4.5 Scope Boundaries
**Description:** Athena keeps M20 focused on presentation fidelity, not semantic expansion or stack
selection.

#### FR-8: Keep M20 bounded
Athena keeps cabinet preview, repository/import ecosystem work, full IEC breadth, and any new
protocol/layout-stack decision out of M20.
**Consequences (testable):**
- No M20 story requires cabinet preview authoring.
- No M20 story adds repository/import ecosystem behavior.
- No M20 story chooses the final layout stack.

## 5. Non-Goals
- New semantic capability
- Cabinet preview authoring
- Public repository/import ecosystem work
- Full IEC library breadth
- New protocol/layout-stack decision
- Frontend-owned semantic resolution
- Renderer-side semantic invention

## 6. MVP Scope

### 6.1 In Scope
- Theia sheet presentation fidelity and interaction steadiness
- Viewport choreography improvements
- Engineering drawing rules and sheet composition model
- Small executable proof corpus for visual acceptance

### 6.2 Out of Scope for MVP
- New semantic model or syntax work
- Cabinet preview
- Repository/import ecosystem expansion
- Full IEC breadth
- Desktop viewer scope
- Layout-stack selection

## 7. Success Metrics

**Primary**
- **SM-1:** A user can open the sheet and judge it as professionally presented rather than naive.
- **SM-2:** Selection, zoom, and reveal remain understandable and stable during repeated use.
- **SM-3:** The proof corpus catches visible layout regressions without requiring a manual demo.

**Counter-metrics**
- **SM-C1:** Do not optimize for a new semantic capability.
- **SM-C2:** Do not optimize for cabinet preview.
- **SM-C3:** Do not optimize for stack selection in M20.

## 8. Assumptions Index
- The semantic and reveal plumbing from M19 remains intact.
- The work stays inside the existing Athena/Theia frontend boundary.
