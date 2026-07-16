---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - step-03-create-stories
  - step-04-final-validation
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md
  - draft/layouts/001-disucss.md
---

# Athena - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Athena, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Project canonical semantics into a schematic sheet with page identity, frame, grid, title block, and stable occurrence ids.

FR2: Render schematic symbols, labels, terminals, and conductor paths with deterministic layout facts derived from the projection.

FR3: Produce deterministic sheet and layout facts from the projection contract, including page size, frame, zones, title block, revision metadata, and coordinate system.

FR4: Defer cabinet preview from M19 so the milestone stays focused on the first professional schematic sheet workflow.

FR5: Keep selection and reveal synchronized through canonical subject identity across source, inspector, Problems, and the sheet surface.

FR6: Publish a small customer-facing proof corpus with executable tests covering schematic rendering, selection/reveal, and deterministic sheet IR round-trip.

FR7: Keep M19 bounded away from public repository/import ecosystem work, full IEC breadth, full EPLAN parity, and frontend-owned semantic resolution.

### NonFunctional Requirements

NFR1: Identical governed input must produce deterministic sheet identity, sheet IR, layout facts, and occurrence ids.

NFR2: The proof corpus must remain small, executable, and runnable from local repository fixtures.

NFR3: M19 must stay inside the existing Athena IDE/runtime boundary with no new remote service, deployment topology, or desktop shell.

NFR4: Downstream UI and rendering surfaces must remain projection-only and not resolve semantic meaning locally.

### Additional Requirements

- Semantic authority stays upstream of projection, layout, and rendering.
- Sheet IR owns publication semantics such as page size, frame, coordinate zones, title block, revision metadata, sheet identity, and view composition.
- Layout facts must be deterministic and reproducible from governed input.
- Cabinet preview is deferred from M19.
- Final diagram protocol and layout stack selection is deferred to a dedicated tech-selector discussion.
- The existing Athena/Theia boundary remains intact; M19 does not introduce a new remote service tier.

### UX Design Requirements

No standalone UX handoff was supplied. The actionable UX requirements below are derived from the PRD and addendum.

UX-DR1: The sheet surface in Theia must read as a professional engineering sheet, with page frame, grid or coordinates, title block, labels, terminals, conductor routes, and cross-reference markers visible in the first proof.

UX-DR2: Selection, reveal, inspector, and Problems must visually track the same canonical subject and occurrence.

UX-DR3: Cabinet preview is not a required M19 UX surface.

### FR Coverage Map

FR1: Epic 1 - Ship the first professional schematic sheet
FR2: Epic 1 - Ship the first professional schematic sheet
FR3: Epic 1 - Ship the first professional schematic sheet
FR4: Epic 3 - Keep M19 focused on schematic credibility
FR5: Epic 2 - Make selection and reveal feel trustworthy
FR6: Epic 1 - Ship the first professional schematic sheet
FR7: Epic 3 - Keep M19 focused on schematic credibility

## Epic List

### Epic 1: Ship the first professional schematic sheet
Engineers can open a governed project and see a credible schematic sheet with stable identity, sheet IR, page structure, and deterministic layout.
**FRs covered:** FR1, FR2, FR3, FR6

### Epic 2: Make selection and reveal feel trustworthy
Engineers can click in the sheet, inspect in source and Problems, and round-trip back to the same canonical subject without the frontend inventing meaning.
**FRs covered:** FR5

### Epic 3: Keep M19 focused on schematic credibility
Reviewers and implementers can keep M19 schematic-first by deferring cabinet preview and other ecosystem work that would dilute the milestone.
**FRs covered:** FR4, FR7

## Epic 1: Ship the first professional schematic sheet

Engineers can open a governed project and see a credible schematic sheet with stable identity, sheet IR, page structure, and deterministic layout.

### Story 1.1: Establish sheet identity and page structure

As an engineer,
I want the sheet to carry stable page identity, frame, zones, and title block,
So that I can trust the sheet as a real engineering publication surface.

**Acceptance Criteria:**

**Given** a governed project with canonical semantic input
**When** the schematic sheet is projected
**Then** the sheet exposes a stable identity and page structure
**And** the sheet carries frame, zones, and title block semantics
**And** the same governed input yields the same sheet identity

### Story 1.2: Project schematic content into sheet IR

As an engineer,
I want canonical semantics to become sheet IR before rendering,
So that the sheet remains a governed projection instead of a frontend reconstruction.

**Acceptance Criteria:**

**Given** a governed semantic model
**When** the sheet is projected
**Then** the sheet IR contains page size, coordinate system, revision metadata, and view composition
**And** the renderer consumes that sheet IR instead of inventing publication state
**And** the sheet IR round-trips the publication semantics needed for the view

### Story 1.3: Render credible schematic elements

As an engineer,
I want labels, terminals, conductor paths, and cross-reference markers rendered with deterministic placement,
So that the sheet reads like a professional engineering artifact.

**Acceptance Criteria:**

**Given** deterministic sheet IR and layout facts
**When** the sheet is rendered
**Then** schematic elements appear in stable positions
**And** labels, terminals, and conductor paths remain aligned with the projection model
**And** cross-reference markers remain tied to canonical subject identity

### Story 1.4: Prove sheet determinism with executable fixtures

As a reviewer,
I want a small executable proof corpus for the schematic sheet,
So that I can verify the milestone without relying on a manual demo.

**Acceptance Criteria:**

**Given** the local M19 proof corpus
**When** tests run
**Then** they verify schematic rendering, deterministic sheet IR, and stable output from the same input state
**And** the proof set stays small enough to remain maintainable
**And** a repeated run on the same fixture state produces the same observable sheet result

## Epic 2: Make selection and reveal feel trustworthy

Engineers can click in the sheet, inspect in source and Problems, and round-trip back to the same canonical subject without the frontend inventing meaning.

### Story 2.1: Round-trip selection through canonical identity

As an engineer,
I want clicking a rendered subject to update source and inspector state,
So that the sheet and the IDE stay synchronized.

**Acceptance Criteria:**

**Given** a rendered subject on the schematic sheet
**When** I select it
**Then** the matching canonical subject is revealed in source and inspector surfaces
**And** the same occurrence identity is preserved across the surfaces
**And** the selection path does not rely on frontend-local semantic inference

### Story 2.2: Reveal sheet subjects from source and Problems

As an engineer,
I want source and Problems selections to reveal the corresponding sheet object,
So that I can navigate from diagnostics back to the sheet without ambiguity.

**Acceptance Criteria:**

**Given** a source span or diagnostic tied to a canonical subject
**When** I trigger reveal
**Then** the matching sheet object is highlighted
**And** the same canonical subject identity is preserved
**And** no frontend-local semantic inference is needed

## Epic 3: Keep M19 focused on schematic credibility

Reviewers and implementers can keep M19 schematic-first by deferring cabinet preview and other ecosystem work that would dilute the milestone.

### Story 3.1: Make cabinet preview a deferred boundary

As a product reviewer,
I want cabinet preview explicitly out of M19,
So that the milestone does not split focus away from the schematic sheet.

**Acceptance Criteria:**

**Given** the M19 PRD and epics
**When** I inspect the scope
**Then** cabinet preview is clearly deferred
**And** no M19 story requires it to ship
**And** the deferred boundary is visible in the planning artifacts

### Story 3.2: Keep ecosystem expansion out of M19

As a product reviewer,
I want public repository/import ecosystem work and full IEC breadth excluded from M19,
So that the milestone stays centered on a credible sheet workflow.

**Acceptance Criteria:**

**Given** the M19 milestone scope
**When** I review the boundary
**Then** ecosystem expansion, full IEC breadth, and frontend-owned semantic resolution are excluded
**And** the milestone remains schematic-first
**And** the planning artifacts point those items to future work instead of implementation stories
