---
title: Athena M19 - First Professional Engineering Sheet Workflow
status: draft
created: 2026-07-16
updated: 2026-07-16
---

# PRD: Athena M19 - First Professional Engineering Sheet Workflow
*Working title - confirm.*

## 0. Document Purpose
This PRD defines the next Athena milestone after M18. It is written for product, architecture, and
downstream implementation planning. It keeps the milestone product-first: user value, view families,
and customer-visible workflow come before mechanism details. Technical layout and reference notes
that would make the PRD too dense live in `addendum.md`.

## 1. Vision
Athena M19 should make the product feel materially closer to a real engineering workspace for end
users. M18 proved that Athena can reason about governed packages and project semantic graphs; M19
must now prove that those semantics can become the first professional engineering sheet workflow in
the IDE.

The milestone is not "make the canvas prettier." It is "make the semantic system visible in a
professional engineering workflow." The user should be able to open a governed project, view a
schematic sheet, inspect canonical engineering subjects, and trust that what they see is projected
from Athena's semantic authority rather than reconstructed by the frontend.

The minimum credible sheet includes page size, frame, coordinate zones, title block, grid or
coordinate markings, subject labels, terminals, conductor paths, cross-reference markers, and sheet
metadata that round-trips back to the governed model.

M19 therefore sits between the existing semantic foundations and the eventual broader rendering or
library ecosystem. It should make Athena demonstrable to an end user without turning into a full
EPLAN clone or a public package repository project.

## 1.1 Why Now
The current gap is product visibility, not semantic proof. Athena already has enough foundation to
support project state, component knowledge, and package-aware semantics. What it still lacks is a
customer-facing workflow that makes those foundations feel real.

If M19 overreaches into full cabinet tooling, IEC library ingestion, or public repository work, the
milestone will become a long infrastructure track with weak user payoff. If it stays too small, the
IDE will remain naive and the project will still fail to look like a serious engineering tool. The
right move is a bounded schematic-first workflow that proves projection-to-view quality without
opening a new ecosystem program.

## 2. Target User

### 2.1 Jobs To Be Done
- Aaron needs to open a governed Athena project and immediately see a credible engineering view.
- Maya needs to inspect a schematic subject and trust that the visual selection matches canonical
  semantic identity.
- Priya needs to verify that Athena remains architecturally clean while becoming more visible to
  end users.
- A product reviewer needs a demoable IDE workflow that feels like an engineering workspace, not a
  toy canvas.

### 2.2 Non-Users (v1)
- Teams expecting a full EPLAN clone in one milestone
- Teams expecting a public package repository or Maven/npm-style ecosystem
- Teams expecting a full IEC symbol library or full cabinet-layout authoring surface
- Teams expecting frontend-owned semantic resolution

### 2.3 Key User Journeys
- **UJ-1. Aaron opens a governed project and sees a schematic sheet.**
  - **Persona + context:** Aaron is checking whether Athena can show real engineering meaning, not
    just syntax or hidden state.
  - **Entry state:** A governed Athena project is already available.
  - **Path:** Aaron opens the project in Theia, selects the schematic sheet view, scans the title
    block and page frame, then clicks a device or terminal.
  - **Climax:** The clicked subject resolves to a canonical semantic identity and the inspector/source
    selection stays in sync.
  - **Resolution:** Aaron can explain the view as a projection of governed semantics, not a front-end
    drawing.

- **UJ-2. Maya follows a sheet object back to source and diagnostics.**
  - **Persona + context:** Maya is using Athena as an engineering workspace and wants confidence
    that the rendered sheet and the source model agree.
  - **Entry state:** A schematic sheet is open in the IDE.
  - **Path:** Maya selects a conductor, label, or cross-reference marker, opens related references,
    and checks the Problems/inspector surfaces.
  - **Climax:** Maya can trace the subject back to source, diagnostics, or related references without
    the frontend inventing meaning.
  - **Resolution:** Maya keeps working in the same project without leaving the IDE.

- **UJ-3. Priya checks whether the milestone stayed bounded.**
  - **Persona + context:** Priya is validating that M19 improved end-user credibility without
    quietly becoming a platform or registry project.
  - **Entry state:** The schematic workflow and proof corpus are in place.
  - **Path:** Priya reviews the scope boundary, the proof fixture set, and the planned backlog
    exclusions.
  - **Climax:** Priya confirms the milestone is schematic-first, not a full EPLAN or package
    ecosystem program.
  - **Resolution:** Priya approves M19 as a controlled customer-facing step.

## 3. Glossary
- **Canonical Subject** - The semantic engineering object that remains authoritative across source,
  projection, and rendered view.
- **Projection Contract** - The Athena-owned contract that turns canonical semantics into a view
  family, sheet identity, and occurrence identity.
- **Schematic Sheet** - The customer-facing engineering sheet view used as the primary M19 proof.
- **Sheet IR** - Athena-owned publication semantics for page size, frame, coordinate zones, title
  block, revision metadata, and view composition.
- **Cabinet Preview** - A limited secondary physical-layout view, only if it can reuse the same
  projection and identity contracts cleanly.
- **Layout Facts** - Deterministic placement and routing facts used to place rendered objects on a
  schematic sheet.
- **Occurrence** - A rendered instance of a canonical subject, carrying a stable occurrence id.
- **View Family** - A named projection family such as schematic or cabinet preview.
- **Theia Workspace** - The current IDE shell and surrounding interaction surface.

## 4. Features

### 4.1 Schematic Sheet Workflow
**Description:** Athena can show a credible schematic sheet in the Theia IDE. The sheet should look
like engineering work, not a generic graph: page frame, grid, title block, subject labels,
terminals, conductors, and cross-reference markers all appear as part of one coherent view.
Realizes UJ-1 and UJ-2.

**Functional Requirements:**

#### FR-1: Project canonical semantics into a schematic sheet
Athena can project canonical subjects into a schematic sheet view with page identity, frame, grid,
title block, and stable occurrence ids.
**Consequences (testable):**
- The same governed project state produces the same schematic sheet identity and subject occurrences.
- A rendered occurrence can be traced back to a canonical subject id.
- The sheet contains engineering cues, not generic diagram chrome.
- Page size, coordinate zones, and sheet metadata remain part of the governed publication model.

#### FR-2: Render schematic symbols, labels, and connections
Athena can render schematic occurrences, labels, terminals, and conductor paths with deterministic
layout facts derived from the projection.
**Consequences (testable):**
- The renderer does not invent meaning from pixels or DOM state.
- Labels and conductor paths remain aligned with the projection model.
- The schematic is legible enough to serve as a customer-facing workflow.

### 4.2 Projection And Sheet Contracts
**Description:** Athena can translate canonical semantics into sheet IR and layout facts that place
a schematic sheet in a repeatable way. The sheet IR is a first-class customer-facing contract, not a
renderer detail: it owns publication semantics such as page size, frame, zones, title block, sheet
identity, revision metadata, and coordinate system; layout may help arrange subjects and routes, but
it may not become a second semantic authority. Realizes UJ-1 and UJ-3.

**Functional Requirements:**

#### FR-3: Produce deterministic sheet and layout facts
Athena can compute stable sheet and layout facts for the schematic sheet from the projection
contract.
**Consequences (testable):**
- The same input state yields the same subject placement and route intent.
- Layout remains repeatable across runs.
- Changing the semantic model changes the sheet through projection facts, not renderer guesswork.
- Sheet identity, page metadata, and route intent remain deterministic for the same governed input.
- Page size, frame, zones, title block, and revision metadata round-trip through sheet IR.

#### FR-4: Defer cabinet preview from M19
Athena keeps cabinet preview out of the M19 MVP so the milestone remains focused on the first
professional schematic sheet workflow.
**Consequences (testable):**
- No M19 story is required to ship a cabinet preview.
- Any later cabinet preview must reuse the same semantic, projection, sheet, and identity contracts.
- The M19 proof corpus does not need cabinet preview outputs.

### 4.3 IDE Coherence And Inspection
**Description:** Athena's Theia surfaces should make the schematic workflow feel trustworthy. Source,
Problems, inspector, outline, and reveal must stay synchronized with the selected rendered subject.
Realizes UJ-2 and UJ-3.

**Functional Requirements:**

#### FR-5: Keep selection and reveal synchronized
Athena can keep rendered selection, source navigation, inspection, and diagnostics synchronized
through canonical subject identity.
**Consequences (testable):**
- Clicking a rendered subject updates inspector/source state.
- Source or inspector selection can reveal the matching rendered subject.
- The frontend does not resolve imports, symbols, or component meaning on its own.
- Source, Problems, and the sheet surface all reflect the same canonical subject and occurrence ids.

### 4.4 Proof Corpus And Boundary Safety
**Description:** Athena can prove the schematic workflow with a small fixture set and keep the
milestone bounded. Realizes UJ-3.

**Functional Requirements:**

#### FR-6: Publish a small customer-facing proof corpus
Athena can prove the schematic workflow with a small governed fixture set and executable tests.
**Consequences (testable):**
- The proof corpus covers schematic rendering, selection/reveal, and deterministic layout.
- The proof corpus covers sheet IR round-trip behavior for page size, frame, zones, title block, and revision metadata.
- The milestone can be demonstrated from local repository fixtures.
- The proof set is small enough to stay maintainable.

#### FR-7: Keep M19 bounded
Athena can keep public repository/import ecosystem work, broad IEC library ingestion, and full EPLAN
parity out of M19.
**Consequences (testable):**
- M19 artifacts explicitly defer registry, publish, and marketplace work.
- M19 artifacts explicitly defer full cabinet layout intelligence and full symbol-library breadth.
- M19 artifacts explicitly defer cabinet preview and cabinet authoring.
- Reviewers can tell the milestone is schematic-first, not ecosystem-first.

## 5. Non-Goals (Explicit)
- Full EPLAN UI parity
- Full IEC symbol library ingestion
- Public Maven/npm-style package repository
- Remote registry or publish flow
- Broad authored-language redesign
- Frontend-owned semantic resolution
- Cabinet preview in M19
- Full cabinet authoring or physical-layout optimization
- Renderer-local semantic inference

## 6. MVP Scope

### 6.1 In Scope
- One schematic-first engineering sheet workflow in the Theia IDE
- Projection-owned page and occurrence identity
- Deterministic sheet and layout facts for schematic rendering
- Source, Problems, inspector, and reveal coherence
- A small governed proof corpus

### 6.2 Out of Scope for MVP
- Public registry or package marketplace work
- Broad IEC or QElectroTech library ingestion
- Full EPLAN-style cabinet layout and authoring
- Cabinet preview
- Broad element editor or symbol-editor work
- General-purpose auto-layout platform work

## 7. Success Metrics

**Primary**
- **SM-1:** A user can open a governed project and see one credible schematic sheet with page frame,
  title block, coordinate zones, and sheet metadata intact. Validates FR-1, FR-2, FR-3.
- **SM-2:** Selection on the rendered sheet round-trips to canonical subject identity and updates
  source/inspector/reveal surfaces. Validates FR-5.
- **SM-3:** The proof corpus demonstrates deterministic schematic output and stable sheet IR from the
  same project state. Validates FR-3, FR-6.
- **SM-4:** Users perceive the IDE as materially more serious because it presents a governed
  engineering sheet workflow instead of a generic canvas. Validates FR-1, FR-2, FR-5.

**Counter-metrics (do not optimize)**
- **SM-C1:** Do not optimize for full EPLAN parity.
- **SM-C2:** Do not optimize for public registry breadth.
- **SM-C3:** Do not optimize for library breadth over schematic credibility.

## 8. Assumptions Index
- None.
