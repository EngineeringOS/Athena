---
stepsCompleted:
  - draft-m20-prd
  - draft-m20-architecture
  - draft-m20-epics
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md
  - docs/usages/m19-proof-usage.md
---

# Athena - M20 Epic Breakdown

## Overview

M20 turns the M19 schematic-sheet proof into an engineering presentation fidelity milestone. The
goal is to make the existing semantic sheet feel professional, governed, and customer-acceptable
without changing semantic authority or reopening the deferred boundaries.

## Requirements Inventory

### Functional Requirements

FR1: Present a schematic sheet that reads as a professional engineering artifact.

FR2: Model sheet composition and representation families explicitly.

FR3: Introduce a governed sheet layout model between Presentation IR and rendering.

FR4: Encode engineering drawing rules for dense content.

FR5: Keep viewport choreography stable across open, fit, zoom, pan, and selection.

FR6: Preserve source, Problems, and sheet coherence while presentation is refined.

FR7: Publish a small executable proof corpus for layout acceptance and regression safety.

FR8: Keep M20 bounded away from cabinet preview, repository/import ecosystem work, full IEC breadth,
and any new protocol/layout-stack decision.

### NonFunctional Requirements

NFR1: Repeated runs on the same governed input must produce stable visible behavior.

NFR2: The sheet surface must remain readable at common window sizes and with dense content.

NFR3: M20 must stay inside the existing Athena IDE/Theia boundary.

NFR4: Layout refinement must not introduce frontend-owned semantic inference.

## Epic List

### Epic 1: Engineering Sheet Composition Model
Engineers can see Athena treat the sheet as a governed composition of frame, title block, zones,
views, representation families, and occurrences.
**FRs covered:** FR1, FR2

### Epic 2: Deterministic Sheet Layout Rules
Engineers can rely on a governed sheet layout model that turns Presentation IR into stable drawing
facts and engineering rules.
**FRs covered:** FR3, FR4

### Epic 3: Professional Renderer Experience
Engineers can open, fit, zoom, pan, and inspect the sheet without the frontend breaking selection,
reveal, or readability.
**FRs covered:** FR5, FR6

### Epic 4: Engineering Artifact Acceptance
Reviewers can verify the milestone with a small governed proof corpus and clear boundary checks.
**FRs covered:** FR7, FR8

## Epic 1: Engineering Sheet Composition Model

Engineers can see Athena treat the sheet as a governed composition of frame, title block, zones,
views, representation families, and occurrences.

### Story 1.1: Introduce the sheet composition contract

As an engineer,
I want the sheet to be represented as explicit composition data,
So that the frame, title block, zones, views, and occurrences feel governed rather than ad hoc.

**Acceptance Criteria:**

**Given** the M19 schematic proof
**When** the sheet is projected in M20
**Then** the sheet composition keeps frame, title block, zones, views, and occurrences distinct
**And** the model is clearly separate from renderer paint state

### Story 1.2: Encode representation families for schematic purpose

As an engineer,
I want one governed subject to be able to carry a representation family,
So that the same semantic object can be presented for the right sheet purpose.

**Acceptance Criteria:**

**Given** a governed engineering subject
**When** it is projected into a sheet
**Then** its representation family is explicit
**And** the family is driven by engineering purpose rather than raw pixel state

### Story 1.3: Bind the proof corpus to the sheet composition model

As a reviewer,
I want the proof fixtures to exercise the composition model,
So that the milestone proves governed sheet composition instead of only a visual screenshot.

**Acceptance Criteria:**

**Given** the local M20 proof corpus
**When** tests run
**Then** they assert sheet composition facts as well as rendered output
**And** the fixture set stays small and governed

## Epic 2: Deterministic Sheet Layout Rules

Engineers can rely on a governed sheet layout model that turns Presentation IR into stable drawing
facts and engineering rules.

### Story 2.1: Insert the sheet layout model between Presentation IR and rendering

As an architect,
I want a governed sheet layout model to sit between Presentation IR and rendering,
So that layout facts stay explicit and renderer-local guessing stays out of the system.

**Acceptance Criteria:**

**Given** a governed projection snapshot
**When** the sheet is laid out
**Then** layout facts are produced before rendering
**And** the renderer consumes those facts without inventing meaning

### Story 2.2: Encode engineering drawing rules for dense content

As an engineer,
I want spacing, routing, grouping, and label-avoidance rules to be governed,
So that dense schematic content remains readable and trustworthy.

**Acceptance Criteria:**

**Given** a dense schematic fixture
**When** layout is computed
**Then** placement and routing remain understandable
**And** obvious overlaps and clutter are reduced

### Story 2.3: Prove repeated runs produce the same layout facts

As a reviewer,
I want the same input to produce the same layout facts,
So that M20 proves deterministic presentation rather than one-off layout luck.

**Acceptance Criteria:**

**Given** the same governed input state
**When** layout runs repeatedly
**Then** the emitted layout facts remain stable
**And** visual drift is not introduced by repeated execution

## Epic 3: Professional Renderer Experience

Engineers can open, fit, zoom, pan, and inspect the sheet without the frontend breaking selection,
reveal, or readability.

### Story 3.1: Stabilize viewport choreography

As an engineer,
I want open, fit, zoom, and pan behavior to feel calm and predictable,
So that the sheet does not jump around while I inspect it.

**Acceptance Criteria:**

**Given** an opened schematic sheet
**When** I fit, zoom, or pan
**Then** the viewport behavior remains understandable and stable
**And** selected content stays findable

### Story 3.2: Keep source, Problems, and sheet coherence intact

As an engineer,
I want reveal and selection to keep working while presentation is improved,
So that the IDE workflow remains coherent.

**Acceptance Criteria:**

**Given** a source span or diagnostic tied to a canonical subject
**When** I trigger reveal
**Then** the same canonical subject is highlighted in the sheet
**And** the frontend still stays projection-only

### Story 3.3: Keep dense content readable at common window sizes

As an engineer,
I want the sheet to stay legible on the normal window sizes we use,
So that presentation fidelity does not collapse when content gets dense.

**Acceptance Criteria:**

**Given** the standard proof fixtures
**When** the sheet is viewed at common window sizes
**Then** labels, routes, and title information remain readable
**And** the sheet does not read as cluttered

## Epic 4: Engineering Artifact Acceptance

Reviewers can verify the milestone with a small governed proof corpus and clear boundary checks.

### Story 4.1: Define the customer-facing acceptance fixture

As a reviewer,
I want a small proof fixture that represents a real engineering sheet,
So that I can judge whether Athena looks professional without needing a manual demo.

**Acceptance Criteria:**

**Given** the acceptance fixture set
**When** I inspect the rendered result
**Then** it looks like a serious engineering artifact
**And** it stays grounded in governed source data

### Story 4.2: Add executable regression coverage for layout and interaction

As a reviewer,
I want the layout and interaction behavior to be testable,
So that regressions are caught without subjective review alone.

**Acceptance Criteria:**

**Given** the M20 proof corpus
**When** automated tests run
**Then** layout acceptability and selection coherence are covered
**And** repeated runs remain stable

### Story 4.3: Keep the deferred boundaries explicit

As a product reviewer,
I want M20 to stay visibly bounded,
So that cabinet preview, repository/import work, IEC breadth, and layout-stack selection remain
deferred.

**Acceptance Criteria:**

**Given** the M20 PRD and architecture
**When** I inspect scope
**Then** the deferred boundaries remain explicit
**And** no story requires a new stack decision
