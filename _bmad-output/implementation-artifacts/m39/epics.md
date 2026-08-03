---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-01-m39/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-01-m39/ARCHITECTURE-SPINE.md
---

# Athena M39 Epic Breakdown

## Outcome

M39 keeps the completed Human DSL work from Epic 1, then establishes the Reality Graph foundation:

```text
Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia
```

The goal is not professional drawing parity. The goal is clean ownership, simple source, and an end-to-end product path where each fact has one owner.

## Epic List

### M39-E1: Human-First Connection Language

Engineers author relationships with domain-provided words and `to`, with `->` accepted as the same relation alias. Normal source has no connection `intent` blocks.

**FRs covered:** FR-1 through FR-8.

### M39-E2: Reality Foundations

Define the four concrete realities and their purpose, owned facts, authority, identity, and validation without adding a generic graph framework.

**FRs covered:** FR-9 through FR-19.

### M39-E3: Reality Transformations

Implement one thin typed transformation path from Engineering to Projection to Spatial to Presentation.

**FRs covered:** FR-20 through FR-25.

### M39-E4: Spatial Compiler

Move placement, bounds, anchor positions, lanes, routes, and alignment into one Spatial compiler authority.

**FRs covered:** FR-26 through FR-33.

### M39-E5: Presentation Compiler

Move style, labels, visibility, theme result, paint order, and dynamic connection appearance into one Presentation compiler authority.

**FRs covered:** FR-34 through FR-40.

### M39-E6: Renderer Proof

Keep Theia and SVG export paint-only, rebuild the active surfaces, capture M39 screenshots, and close the milestone honestly.

**FRs covered:** FR-41 through FR-47.

## M39-E1: Human-First Connection Language

### Story 1.1: Replace Arrow Connections With `to`

As an engineer,
I want every relation to support `to`,
so that Athena reads like engineering language.

**Acceptance Criteria:**

Given a connection written with `to`
When Athena parses, highlights, formats, lowers, and compiles it
Then it produces a traceable engineering relationship
And tests cover parser, AST, formatter, syntax highlighting, compiler lowering, and LSP document symbols
And no renderer, layout, or route behavior changes in this story.

### Story 1.2: Add Domain Connection Verbs

As an engineer,
I want to write domain relations such as `power`, `control`, and `earth`,
so that common engineering relations are clear and short.

**Acceptance Criteria:**

Given `power A to B`, `control A to B`, and `earth A to [B, C]`
When Athena compiles them
Then the active electrical domain package resolves each relation word and its engineering contract
And Athena kernel does not hardcode those words as universal keywords
And grouped `earth` remains one relationship with ordered endpoints and one source trace
And an unknown relation reports the active domain and available relations plainly.

### Story 1.3: Remove `intent` From Normal M39 Source

As an engineer,
I want engineering rules and drawing defaults to come from their proper owners,
so that project source does not repeat compiler policy.

**Acceptance Criteria:**

Given the M39 dedicated example
When source is reviewed and compiled
Then no connection `intent` block or stale compatibility path appears
And relation contracts own compatibility, medium, separation, and physical restrictions
And drawing defaults own line appearance and label convention
And diagnostics remain plain when either contract is incomplete.

## M39-E2: Reality Foundations

### Story 2.1: Define Four Reality Roots

As a compiler maintainer,
I want concrete roots for Engineering, Projection, Spatial, and Presentation realities,
so that each stage has one clear owner.

**Acceptance Criteria:**

Given the existing model roots
When M39 reality roots are defined
Then Engineering Reality has a clear purpose and reuses or cleans the existing engineering root where possible
And Projection Reality has a clear purpose as a view-specific engineering document without coordinates or style
And Spatial Reality has a clean root for placement, bounds, anchor positions, alignment, lanes, and routes
And Presentation Reality has a clear root for paintable facts only
And no generic graph framework, universal `Fact` base class, or empty wrapper model is introduced.

### Story 2.2: Remove Spatial Facts From Projection Models

As an architect,
I want projection models to stop owning coordinates and routes,
so that layout truth has one owner.

**Acceptance Criteria:**

Given current projection models
When stale ownership is removed
Then projection nodes no longer own final bounds
And projection connections no longer own route start/end geometry
And projection layout helpers that compute final placement or routes are deleted or moved into Spatial ownership
And tests prove projection can be built without spatial coordinates.

### Story 2.3: Declare Reality Authority And Validation

As a maintainer,
I want each reality to state authority, identity, and required facts,
so that broken pipelines fail at the right boundary.

**Acceptance Criteria:**

Given each M39 reality root
When validation runs
Then each reality reports missing required facts in plain language
And each fact identity remains traceable to its source or prior reality
And new M39 naming avoids milestone names, `V0`/`V1`, vague `Evidence` terms, and long unclear names.

## M39-E3: Reality Transformations

### Story 3.1: Transform Engineering To Projection

As a compiler maintainer,
I want Engineering Reality to lower into Projection Reality through one typed transformation,
so that views do not corrupt engineering truth.

**Acceptance Criteria:**

Given Engineering Reality with devices, ports, and connections
When Engineering to Projection runs
Then it uses the common typed transformation interface
And emits Projection Reality only
And keeps source trace, engineering identity, connection identity, and hierarchy
And diagnostics name the missing engineering or projection fact plainly.

### Story 3.2: Transform Projection To Spatial

As a compiler maintainer,
I want Projection Reality to lower into Spatial Reality through one typed transformation,
so that coordinates and routes do not leak backward.

**Acceptance Criteria:**

Given Projection Reality
When Projection to Spatial runs
Then it uses the common typed transformation interface
And emits Spatial Reality only
And derives placement, bounds, anchor positions, alignment, lanes, routes, and quality measurements
And failed placement or missing anchor facts stop before Presentation Reality.

### Story 3.3: Transform Spatial To Presentation

As a compiler maintainer,
I want Spatial Reality to lower into Presentation Reality through one typed transformation,
so that the renderer cannot become a compiler.

**Acceptance Criteria:**

Given Spatial Reality
When Spatial to Presentation runs
Then it uses the common typed transformation interface
And emits Presentation Reality only
And derives shape paint facts, connector paint facts, labels, style, visibility, and paint order
And rejects incomplete visual facts before publication.

## M39-E4: Spatial Compiler

### Story 4.1: Build Spatial Placement Authority

As a compiler maintainer,
I want placement and bounds to be derived in Spatial Reality,
so that projection remains coordinate-free.

**Acceptance Criteria:**

Given Projection Reality occurrences
When Spatial compiler runs
Then it derives placements and bounds for each occurrence
And no projection model stores final coordinates
And source trace remains attached.

### Story 4.2: Build Anchor And Route Authority

As an evaluator,
I want connector endpoints to be computed from placed anchors,
so that visible lines attach to real terminals.

**Acceptance Criteria:**

Given placed occurrences with anchors
When routes are derived
Then connector first and last points equal placed anchor points
And route facts reference source connection, occurrence, port, and anchor identities
And missing anchors fail before Presentation Reality
And route start/end geometry is not owned by Projection Reality.

### Story 4.3: Capture Spatial Quality Baseline

As a milestone owner,
I want honest geometry measurements,
so that M40 and M41 start from truth.

**Acceptance Criteria:**

Given the M39 example
When Spatial compiler finishes
Then overlap, body intersection, crossing count, twist, lane use, and label pressure are measured
And failures are recorded as baseline debt unless they break M39 trust invariants
And M39 does not claim professional routing
And routing is documented and tested as a subsystem of Spatial Reality.

## M39-E5: Presentation Compiler

### Story 5.1: Build Dynamic Connection Appearance

As an engineer,
I want each visible connection to have clear style facts,
so that line weight, style, color, marker, and label are controlled in the right layer.

**Acceptance Criteria:**

Given a relation contract, drawing defaults, and optional source properties
When Presentation compiler runs
Then it derives connector style, weight, color, marker, label, and label position
And optional properties such as `style`, `label`, and `position` override presentation defaults only
And engineering truth is unchanged.

### Story 5.2: Build Labels, Visibility, And Paint Order

As an evaluator,
I want the final drawing to have explicit label, visibility, and paint order facts,
so that renderer behavior is deterministic.

**Acceptance Criteria:**

Given Spatial Reality
When Presentation compiler emits a Presentation Document
Then every shape, connector, marker, and label has visibility and paint order
And labels have explicit targets and positions
And incomplete paint facts fail closed with plain diagnostics.

### Story 5.3: Clean Presentation Naming And Dead Paths

As a maintainer,
I want presentation code names to be direct,
so that the model is readable by humans and LLMs.

**Acceptance Criteria:**

Given M39 presentation paths
When cleanup runs
Then new M39 product concepts avoid vague `Evidence` naming
And stale compatibility classes, methods, docs, examples, and tests violating M39 are deleted
And source-set hygiene audit passes.

## M39-E6: Renderer Proof

### Story 6.1: Keep Theia And SVG Export Paint-Only

As a platform maintainer,
I want Theia and SVG export to consume the same Presentation Document,
so that there is one visible drawing authority.

**Acceptance Criteria:**

Given a M39 Presentation Document
When Theia and SVG export render it
Then both consume the same shape, connector, marker, label, visibility, and paint order facts
And neither snaps endpoints, repairs routes, infers topology, relabels, or interprets engineering domains.

### Story 6.2: Build Dedicated M39 Product Proof

As a milestone owner,
I want a dedicated M39 example and E2E proof,
so that M39 is judged on its own architecture.

**Acceptance Criteria:**

Given the M39 example
When kernel, LSP, frontend, and product surfaces are rebuilt
Then the example compiles through Engineering, Projection, Spatial, and Presentation realities
And screenshots are captured under `_bmad-output/implementation-artifacts/m39/screenshots`
And proof cites only M39 paths.

### Story 6.3: Close M39 With Honest Retrospective

As a milestone owner,
I want closure to separate foundation from visual parity,
so that M40 starts from the real remaining work.

**Acceptance Criteria:**

Given all M39 stories are complete
When retrospective is written
Then it records Human DSL, four Reality ownership, transformation chain, Spatial/Paint ownership, and screenshots
And it lists remaining visual debt without claiming professional drawing quality.
