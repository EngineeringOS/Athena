---
stepsCompleted:
  - extract-m23-requirements
  - design-m23-epics
  - create-m23-stories
  - validate-m23-coverage
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/addendum.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-18-m23/review-rubric.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md
---

# Athena - M23 Epic Breakdown

## Overview

M23 closes the M22 truth gap by admitting governed layout hints into the real `.athena` language.
The milestone is complete only when ANTLR4, Tree-sitter, authored AST, compiler lowering, LSP
diagnostics, Graph Workbench source edits, and an openable sample project all agree on the same
system-scoped layout block syntax.

M23 is not a visual-polish milestone, not an EPLAN parity milestone, not advanced routing, not AI
layout, not repository/library ecosystem work, and not a raw pixel-coordinate layout language.

## Requirements Inventory

### Functional Requirements

FR1: Extend ANTLR4 and Tree-sitter grammars for system-scoped layout blocks.

FR2: Add authored AST support for `LayoutDeclaration` and layout statements.

FR3: Keep existing `.athena` source compatible across M0-M22 syntax, including package/import.

FR4: Lower layout declarations into layout constraint snapshots with subject/view/source identity.

FR5: Introduce a layout intent model between AST and constraints.

FR6: Support layout constraint priority in the model.

FR7: Feed admitted constraints into the existing layout optimization path.

FR8: LSP accepts and diagnoses layout blocks.

FR9: Graph Workbench applies approved layout hints as valid source edits.

FR10: Preserve active-source projection and accepted canvas behavior.

FR11: Provide an openable M23 sample project with real layout blocks.

FR12: Publish usage and regression evidence.

### NonFunctional Requirements

NFR1: ANTLR4 and Tree-sitter parser parity is mandatory for valid and invalid M23 layout fixtures.

NFR2: Generated parser types must stay internal; downstream code consumes Athena-owned AST.

NFR3: Syntax admission must not break device, port, connect, package, import, or prior samples.

NFR4: Layout facts remain deterministic and renderer-consumed only.

NFR5: Theia and the renderer must not infer or persist engineering meaning.

NFR6: Source edits must be reviewable, accepted `.athena` syntax with no hidden canvas state.

NFR7: Verification must include real sample project evidence, not only `.mjs` helper tests.

### Additional Requirements

- Architecture AD-1: Layout blocks are system-scoped first.
- Architecture AD-2: ANTLR4 and Tree-sitter parser parity is mandatory.
- Architecture AD-3: Authored AST owns the syntax handoff.
- Architecture AD-4: `LayoutDeclaration` lowers through layout intent before constraints.
- Architecture AD-5: Priority is model-owned; authored statements default to preference.
- Architecture AD-6: Graph Workbench is not syntax authority.
- Architecture AD-7: Compiler and LSP own meaning and diagnostics.
- Architecture AD-8: Existing Athena source compatibility remains binding.
- Architecture AD-9: Accepted Graph Workbench behavior carries forward.
- Architecture AD-10: M23 is language admission, not new layout depth.

### UX Design Requirements

UX-DR1: A reviewer can open `examples/m23/sample-project` in the normal Athena Theia IDE workflow.

UX-DR2: Valid layout-block files show no false editor, Problems, or Graphical View syntax errors.

UX-DR3: Invalid layout files produce useful diagnostics without crashing the editor or projection.

UX-DR4: Graph Workbench mutation preview shows the affected subject and accepted source syntax.

UX-DR5: Active-source Graphical View projection, same-tab outline navigation, grid-backed canvas,
transparent controls, and top-popover information behavior do not regress.

### FR Coverage Map

FR1: Epic 1 - parser parity and source fixtures.

FR2: Epic 2 - authored AST and language admission.

FR3: Epic 1 and Epic 5 - backward compatibility and boundary regression.

FR4: Epic 3 - compiler constraint lowering.

FR5: Epic 2 and Epic 3 - layout intent model and lowering path.

FR6: Epic 2 and Epic 3 - priority model and conflict diagnostics.

FR7: Epic 3 - admitted constraints feeding existing layout facts.

FR8: Epic 4 - LSP acceptance and diagnostics.

FR9: Epic 4 - Graph Workbench source edit closure.

FR10: Epic 4 and Epic 5 - active-source projection and canvas behavior preservation.

FR11: Epic 1 and Epic 5 - openable sample project proof.

FR12: Epic 5 - usage, smoke, regression, and closeout evidence.

## Epic List

### Epic 1: Parser Parity And Source Fixtures
Developers can write system-scoped layout blocks in `.athena` and both parser stacks agree on the
same accepted and rejected syntax before any sample project claims success.
**FRs covered:** FR1, FR3, FR11

### Epic 2: Authored AST And Layout Intent Admission
Compiler-side code can receive layout blocks as Athena-owned authored syntax and convert them into
domain-neutral layout intent without exposing generated parser details.
**FRs covered:** FR2, FR5, FR6

### Epic 3: Compiler Constraint Lowering And Deterministic Facts
Admitted layout intent becomes governed constraints and deterministic layout facts with useful
diagnostics for unknown, duplicate, or contradictory hints.
**FRs covered:** FR4, FR6, FR7

### Epic 4: LSP And Graph Workbench Round-Trip Closure
Theia accepts valid layout blocks, reports invalid ones through Problems, and applies approved
layout hints through the accepted source syntax rather than frontend-owned strings.
**FRs covered:** FR8, FR9, FR10

### Epic 5: Sample Proof, Usage, And Boundary Guardrails
Reviewers can open the M23 sample project, verify parser/compiler/LSP/IDE behavior, and see clear
usage and boundary documentation that corrects the M22 overclaim.
**FRs covered:** FR3, FR10, FR11, FR12

## Epic 1: Parser Parity And Source Fixtures

Developers can write system-scoped layout blocks in `.athena` and both parser stacks agree on the
same accepted and rejected syntax before any sample project claims success.

### Story 1.1: Admit system-scoped layout blocks in ANTLR

As a language implementer,
I want ANTLR4 to parse system-scoped layout blocks,
So that compiler and LSP parsing can accept the M23 source contract.

**Acceptance Criteria:**

**Given** a `.athena` source file with `layout schematic-sheet { ... }` inside `system { ... }`
**When** the ANTLR parser runs
**Then** it accepts `place SUBJECT near TARGET`, `place SUBJECT below TARGET`, `align SUBJECT aligned-with TARGET axis horizontal|vertical`, and `group SUBJECT grouped-with TARGET`
**And** it rejects file-global layout blocks
**And** existing M0-M22 parser fixtures still pass
**And** package/import syntax remains unchanged

### Story 1.2: Add Tree-sitter layout-block parity

As an IDE user,
I want Tree-sitter to parse the same layout block shape as ANTLR,
So that the editor does not mark valid compiler syntax as broken.

**Acceptance Criteria:**

**Given** the valid and invalid M23 parser fixtures
**When** Tree-sitter tests run
**Then** valid system-scoped layout blocks parse without error nodes in the admitted syntax
**And** invalid `place`, `align`, `group`, axis, and file-global fixture families recover predictably
**And** Tree-sitter node names are documented for downstream highlighting and structural feedback

### Story 1.3: Add parser parity fixture corpus

As a reviewer,
I want paired ANTLR and Tree-sitter fixtures for layout syntax,
So that future syntax changes cannot silently drift between backend and IDE parser paths.

**Acceptance Criteria:**

**Given** the M23 fixture corpus
**When** parser parity tests run
**Then** valid fixtures cover the full admitted statement vocabulary
**And** invalid fixtures cover malformed `place`, invalid axis, missing target, and rejected file-global layout
**And** fixture names and expected outcomes are shared or cross-referenced between parser stacks

### Story 1.4: Preserve existing source compatibility

As an Athena engineer,
I want M23 grammar changes to preserve prior language behavior,
So that layout admission does not regress established source contracts.

**Acceptance Criteria:**

**Given** existing package/import/system/device/port/connect examples and tests
**When** parser and compiler verification runs
**Then** existing M0-M22 syntax still parses
**And** unsupported import forms still report the same deterministic diagnostics
**And** no prior example is rewritten only to satisfy M23

## Epic 2: Authored AST And Layout Intent Admission

Compiler-side code can receive layout blocks as Athena-owned authored syntax and convert them into
domain-neutral layout intent without exposing generated parser details.

### Story 2.1: Add authored `LayoutDeclaration` AST

As a compiler engineer,
I want a first-class authored AST node for layout declarations,
So that generated parser types stay internal to the parser adapter.

**Acceptance Criteria:**

**Given** an admitted layout block parse tree
**When** parser adaptation builds authored syntax
**Then** it emits `LayoutDeclaration` with view family, statements, and source span
**And** statements represent place-near, place-below, align-with-axis, and group-with
**And** declaration consumers handle the new variant explicitly

### Story 2.2: Map layout AST into layout intent

As an architect,
I want layout syntax to lower into domain-neutral intent before constraints,
So that syntax does not leak directly into solver behavior.

**Acceptance Criteria:**

**Given** a `LayoutDeclaration`
**When** compiler admission runs
**Then** it produces layout intent objects with subject, relation, target, optional axis, source span, and priority
**And** authored statements default to preference priority
**And** AST remains syntax-only with no semantic subject resolution

### Story 2.3: Add priority model compatibility

As a layout-model maintainer,
I want M23 authored priority to coexist with existing layout priorities,
So that future conflict handling has a stable model without breaking M21/M22 layout contracts.

**Acceptance Criteria:**

**Given** existing layout priority types
**When** M23 adds authored layout priority support
**Then** preference/default priority is represented without silently changing existing semantics
**And** hard/soft/preference mapping or a separate authored constraint priority type is documented
**And** tests cover deterministic priority ordering

### Story 2.4: Add layout source serialization contract

As a Graph Workbench developer,
I want a serializer for layout intent source text,
So that frontend code does not hand-build final `.athena` syntax.

**Acceptance Criteria:**

**Given** a layout intent object for placement, alignment, or grouping
**When** the serializer runs
**Then** it emits accepted M23 system-scoped layout block syntax
**And** formatting is stable across repeated runs
**And** serializer tests cover all admitted statements

## Epic 3: Compiler Constraint Lowering And Deterministic Facts

Admitted layout intent becomes governed constraints and deterministic layout facts with useful
diagnostics for unknown, duplicate, or contradictory hints.

### Story 3.1: Bind layout hint subjects through compiler semantics

As a compiler user,
I want layout hint subjects and targets resolved by the compiler,
So that unknown layout references are reported instead of ignored.

**Acceptance Criteria:**

**Given** layout intent references authored subject names
**When** semantic binding runs
**Then** known subjects bind to canonical identities where available
**And** unknown subjects and targets produce semantic diagnostics with source spans
**And** Tree-sitter or Theia code does not perform semantic binding

### Story 3.2: Lower layout intent into constraint snapshots

As a layout engineer,
I want admitted hints to become governed layout constraints,
So that the existing layout engine can consume source-owned relationships.

**Acceptance Criteria:**

**Given** bound layout intent for a system
**When** constraint lowering runs
**Then** `near`, `below`, `aligned-with`, and `grouped-with` constraints are emitted
**And** constraints carry view family, subject, target, priority, source span, and snapshot identity where available
**And** constraints are sorted deterministically

### Story 3.3: Diagnose duplicate and contradictory hints

As an engineer,
I want layout hint conflicts reported predictably,
So that source intent stays reviewable and does not produce mysterious layouts.

**Acceptance Criteria:**

**Given** duplicate or contradictory hints in a layout block
**When** compiler diagnostics run
**Then** duplicates are either de-duplicated with evidence or reported consistently
**And** contradictory hints include relation, subject, target, and priority in the diagnostic
**And** diagnostics do not crash layout fact generation

### Story 3.4: Feed admitted constraints into deterministic layout facts

As a reviewer,
I want admitted layout hints to influence layout facts,
So that M23 proves source-owned layout intent reaches the renderer contract.

**Acceptance Criteria:**

**Given** a sample project with admitted layout hints
**When** projection/layout generation runs
**Then** the emitted layout facts reflect the admitted constraints where the current engine supports them
**And** repeated runs on the same input produce identical facts
**And** renderer code remains paint-only

## Epic 4: LSP And Graph Workbench Round-Trip Closure

Theia accepts valid layout blocks, reports invalid ones through Problems, and applies approved
layout hints through the accepted source syntax rather than frontend-owned strings.

### Story 4.1: Publish layout-block syntax and semantic diagnostics through LSP

As an IDE user,
I want valid layout blocks accepted and invalid blocks diagnosed in Problems,
So that source truth and editor feedback agree.

**Acceptance Criteria:**

**Given** valid and invalid M23 layout sources
**When** the Athena LSP analyzes them
**Then** valid layout blocks produce no false syntax errors
**And** invalid relation, missing target, invalid axis, unknown subject, and unknown target cases produce useful diagnostics
**And** diagnostics carry source ranges for reveal

### Story 4.2: Apply Graph Workbench layout hints through serializer/source edit

As an engineer,
I want an approved layout adjustment to become valid `.athena` text,
So that layout round-trip survives close and reopen.

**Acceptance Criteria:**

**Given** a selected subject in Graph Workbench
**When** a placement, alignment, or grouping adjustment is approved
**Then** Graph Workbench sends layout intent to the serializer/source-edit path
**And** the resulting source text reparses without syntax errors
**And** rejected previews do not mutate source or canvas state

### Story 4.3: Reparse and reproject after approved layout edits

As an IDE user,
I want accepted layout edits to immediately reparse and reproject,
So that the graph reflects source-owned layout intent.

**Acceptance Criteria:**

**Given** an approved M23 layout source edit
**When** the source document updates
**Then** LSP diagnostics refresh
**And** Graphical View reprojects from the active source
**And** close/reopen reproduces the same admitted layout relationship

### Story 4.4: Preserve active-source and canvas behavior

As a reviewer,
I want M23 language admission to preserve M20-M22 IDE behavior,
So that parser work does not regress the visible product surface.

**Acceptance Criteria:**

**Given** the M23 sample project is open in Theia
**When** switching between `.athena` files and opening Graphical View
**Then** the active source file drives the projection
**And** outline navigation keeps the same editor tab
**And** grid, transparent controls, top information popover, and whitespace dismissal behavior remain unchanged

## Epic 5: Sample Proof, Usage, And Boundary Guardrails

Reviewers can open the M23 sample project, verify parser/compiler/LSP/IDE behavior, and see clear
usage and boundary documentation that corrects the M22 overclaim.

### Story 5.1: Create the openable M23 sample project

As a reviewer,
I want a real M23 sample project with layout blocks in `.athena` source,
So that I can prove the milestone through the IDE instead of reading scripts.

**Acceptance Criteria:**

**Given** M23 parser/compiler admission is in place
**When** I open `examples/m23/sample-project`
**Then** `src/01-layout-hints.athena` contains a real system-scoped layout block
**And** the file opens without false syntax diagnostics
**And** the Graphical View projects the active layout-hint source

### Story 5.2: Add M23 end-to-end smoke proof

As a product reviewer,
I want automated smoke coverage for the M23 sample,
So that future parser or IDE regressions are caught before manual testing.

**Acceptance Criteria:**

**Given** the M23 sample project
**When** smoke tests run
**Then** compiler, LSP, and Graphical View paths accept the sample
**And** test output identifies the exact sample file under test
**And** failures are actionable without inspecting unrelated `.mjs` implementation details

### Story 5.3: Publish M23 usage guide

As Aaron,
I want usage documentation for M23,
So that I know which project to open, what syntax is supported, and what behavior is expected.

**Acceptance Criteria:**

**Given** M23 implementation is complete
**When** usage docs are published
**Then** they name `examples/m23/sample-project` and `src/01-layout-hints.athena`
**And** they show the admitted syntax and expected IDE behavior
**And** they clearly state M22 was preview-only and M23 is real language admission

### Story 5.4: Add M23 boundary and stale-doc regression checks

As an Athena maintainer,
I want M23 docs and tests to reject overstated claims,
So that future retrospectives do not promise unsupported layout features.

**Acceptance Criteria:**

**Given** M23 artifacts and docs
**When** boundary checks run
**Then** they confirm no claims of EPLAN parity, advanced routing, AI layout, public repository/import ecosystem, broad IEC ingestion, or hidden canvas-state persistence
**And** stale M22 preview-only wording remains corrected
**And** M23 retrospective records achievement, usage, and deferred work honestly
