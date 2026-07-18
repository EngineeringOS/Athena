---
title: Athena M23 - Governed Layout Hint Language Admission
status: draft
created: 2026-07-18
updated: 2026-07-18
---

# PRD: Athena M23 - Governed Layout Hint Language Admission

## 0. Document Purpose

M23 closes the most important correctness gap discovered after M22: M22 selected and previewed a
layout-hint block, but the real `.athena` language, compiler, LSP, and sample project do not accept
that block yet.

M23 exists to make governed layout hints first-class system-scoped authored syntax:

```athena
system MachineNo000 {
  device PLC1 {
    type Switch
  }

  layout schematic-sheet {
    place HMI1 near PLC1
    place XT1 below PLC1
    align HMI1 aligned-with PLC1 axis vertical
    group HMI1 grouped-with PLC1
  }
}
```

The milestone is not a new visual polish milestone. It is the language-to-layout contract closure
needed before Athena can honestly claim layout round-trip.

## 1. Vision

Athena should let an engineer approve a layout adjustment and see that adjustment become valid,
reviewable `.athena` intent. The IDE must not generate syntax that the editor immediately flags as
invalid. The compiler and projection stack must be able to consume the authored layout block and
turn it into governed layout constraints and facts.

The target pipeline is:

```text
.athena system-scoped layout block
  -> ANTLR parser and IDE Tree-sitter parser
  -> authored AST LayoutDeclaration
  -> layout intent model
  -> compiler layout-hint admission
  -> layout constraint snapshot
  -> layout optimization / projection facts
  -> Theia graph workbench
  -> approved adjustment emits the same accepted syntax
```

## 1.1 Why Now

M19 proved sheet publication. M20 made the sheet surface more acceptable. M21 introduced layout
intent and facts. M22 introduced layout constraints, optimization boundaries, and the selected
layout-hint block shape, but stopped short of real language admission.

Starting another layout intelligence or visual fidelity milestone before this correction would build
on a false product contract. M23 should make the source truth real first.

## 2. Target User

### 2.1 Jobs To Be Done

- Aaron needs to open a real sample project and see layout blocks inside `.athena` files without
  syntax errors.
- Maya needs to approve a placement/alignment/grouping adjustment and get valid source, not a
  preview-only snippet.
- Winston needs the layout block to lower through Athena-owned AST and compiler contracts, not
  frontend string conventions.
- Priya needs a customer-facing proof that Athena can round-trip engineering layout intent through
  source truth.

### 2.2 Non-Users

- Teams expecting full EPLAN layout parity
- Teams expecting advanced electrical routing intelligence
- Teams expecting full IEC/QElectroTech symbol libraries
- Teams expecting AI layout optimization
- Teams expecting free-form canvas drawing state

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a layout-hint sample without syntax errors.**
  - **Context:** Aaron opens `examples/m23/sample-project` in the Athena Theia IDE.
  - **Path:** He opens a `.athena` file containing `layout schematic-sheet { ... }`.
  - **Climax:** The editor, outline, Problems view, Graphical View, and LSP all accept the file.
  - **Resolution:** M23 proves the layout block is real source syntax, not documentation-only.

- **UJ-2. Maya approves a layout adjustment.**
  - **Context:** Maya selects a rendered subject and asks Athena to keep a placement, alignment, or
    grouping relationship.
  - **Path:** The graph workbench shows a mutation preview using the accepted layout block shape.
  - **Climax:** After approval, the `.athena` file receives valid layout syntax and reprojects.
  - **Resolution:** The same layout relationship survives close/reopen because source owns it.

- **UJ-3. Winston verifies architecture authority.**
  - **Context:** Winston reviews the parser, AST, compiler, layout model, and renderer boundary.
  - **Path:** He checks that layout hints lower to Athena constraints/facts and never become DOM or
    canvas truth.
  - **Climax:** The renderer consumes facts only; source and compiler own the intent.
  - **Resolution:** M23 preserves EngineeringOS authority while admitting new syntax.

## 3. Glossary

- **Layout Block** - A system-scoped authored `.athena` block that declares layout relationships for
  a view family such as `schematic-sheet`.
- **Layout Declaration** - The authored AST representation of a layout block.
- **Layout Hint Admission** - ANTLR4, Tree-sitter, authored AST, compiler, and LSP acceptance of the
  layout block as real source syntax.
- **Layout Intent Model** - A domain-neutral model between authored syntax and layout constraints
  that represents placement, alignment, grouping, relation, target, source, and priority intent.
- **Parser Parity** - The requirement that ANTLR4 and Tree-sitter accept the same M23 valid layout
  syntax and reject or recover the same invalid/incomplete fixture families where applicable.
- **Round-Trip Closure** - The graph workbench can generate, apply, reparse, and reproject a layout
  hint without creating syntax diagnostics or hidden canvas state.
- **Constraint Lowering** - Conversion from authored layout hints to layout constraints consumed by
  the layout engine.

## 4. Features

### 4.1 Real `.athena` Layout Block Admission

**Description:** M23 admits the selected M22 layout block into the real language path.

#### FR-1: Extend ANTLR and Tree-sitter grammars for system-scoped layout blocks

Athena supports `layout schematic-sheet { ... }` inside `system { ... }`.

**Consequences:**

- ANTLR4 accepts the layout block for compiler and LSP parsing.
- IDE Tree-sitter accepts the layout block for editor parsing, highlighting, and structural
  feedback.
- ANTLR4 and Tree-sitter stay synchronized on the accepted M23 layout syntax.
- `place SUBJECT near TARGET` parses.
- `place SUBJECT below TARGET` parses.
- `align SUBJECT aligned-with TARGET axis horizontal|vertical` parses.
- `group SUBJECT grouped-with TARGET` parses.
- The grammar does not accept arbitrary canvas coordinates as the primary authored language.
- File-global layout blocks remain deferred.

#### FR-2: Add authored AST support for layout declarations

Athena adds an Athena-owned `LayoutDeclaration` syntax node rather than exposing generated parser
types downstream.

**Consequences:**

- Layout declarations carry source spans.
- Layout statements carry subject, relation, target, and optional axis.
- Existing declaration consumers fail exhaustively until they handle the new variant.
- Invalid layout syntax produces normal syntax diagnostics, not crashes.

#### FR-3: Keep existing `.athena` source compatible

Existing M0-M22 `.athena` files continue to parse and compile.

**Consequences:**

- `device`, `port`, and `connect` behavior remains unchanged.
- M18 package/import behavior remains unchanged.
- Existing samples and tests remain valid.

### 4.2 Compiler And Layout Constraint Lowering

**Description:** M23 turns layout declarations into governed layout constraints.

#### FR-4: Lower layout declarations into layout constraint snapshots

The compiler can convert authored layout statements into `near`, `below`, `aligned-with`, and
`grouped-with` constraints.

**Consequences:**

- Constraints carry canonical subject, view family, source file, and source span identity where
  available.
- Unknown subjects produce diagnostics instead of silent ignored hints.
- Duplicate or contradictory hints are reported predictably.
- Layout hints do not create engineering devices, ports, or connections.

#### FR-5: Introduce a layout intent model between AST and constraints

Athena introduces a domain-neutral layout intent model between syntax and constraints.

**Consequences:**

- The authored AST remains syntax-only.
- The graph workbench can produce layout intent objects instead of hand-built source strings.
- The compiler maps `LayoutDeclaration` syntax into layout intent before constraint lowering.
- Domain plugins and future AI/layout systems can reason over relation intent rather than raw
  coordinates.

#### FR-6: Support layout constraint priority in the model

Athena layout intent and constraint models carry priority so future company rules, user hints, and
solver proposals can be resolved predictably.

**Consequences:**

- M23 authored layout statements default to a non-hard preference priority unless explicitly promoted
  by compiler/runtime policy.
- The model can represent `hard`, `soft`, and `preference` priorities.
- M23 does not need to expose `prefer` or `require` source keywords unless the implementation remains
  mechanically small and does not expand the language scope.
- Conflict diagnostics can name priority when two admitted hints disagree.

#### FR-7: Feed admitted constraints into the existing layout optimization path

Admitted layout constraints influence existing layout facts through the M21/M22 layout model and
engine boundaries.

**Consequences:**

- Renderer remains paint-only.
- Layout facts remain deterministic.
- ELK or other adapters, if used later, still receive Athena-normalized constraints.

### 4.3 IDE And LSP Round-Trip Closure

**Description:** Theia must generate only syntax that the real language accepts.

#### FR-8: LSP accepts and diagnoses layout blocks

The Athena LSP accepts valid layout blocks and reports invalid layout blocks through normal Problems
diagnostics.

**Consequences:**

- Valid layout sample files show no false syntax errors.
- Invalid relation, missing target, or invalid axis cases receive useful diagnostics.
- Outline and source navigation remain coherent for surrounding `.athena` declarations.

#### FR-9: Graph workbench applies approved layout hints as valid source edits

The graph workbench mutation preview and accepted source edit use the admitted layout block syntax.

**Consequences:**

- Preview text and persisted text are the same accepted syntax.
- The graph workbench produces layout intent and uses an Athena serializer/source-edit path rather
  than manually owning layout syntax strings.
- Rejected previews do not mutate source or canvas state.
- Accepted previews reparse without syntax errors.
- Close/reopen reproduces the governed layout relationship from source.

#### FR-10: Preserve active-source projection and accepted canvas behavior

M23 does not regress M20-M22 graph workbench behavior.

**Consequences:**

- Active `.athena` source still drives Graphical View.
- Grid remains the coordinate surface.
- Floating controls remain transparent.
- `Cabinet Main` remains in the top information popover.
- Whitespace click closes the popover.

### 4.4 Customer-Facing Proof And Guardrails

**Description:** M23 must be testable through a real project, not only unit tests.

#### FR-11: Provide an openable M23 sample project with real layout blocks

Athena provides `examples/m23/sample-project` with real `.athena` files that include accepted layout
blocks.

**Consequences:**

- The sample opens through the normal Athena Theia IDE workflow.
- The sample includes valid layout-block files and invalid-diagnostic fixtures where appropriate.
- Reviewers do not need to inspect `.mjs` files to understand the proof.

#### FR-12: Publish usage and regression evidence

Athena records how to run and verify M23.

**Consequences:**

- Usage docs identify the sample project and expected IDE behavior.
- Compiler, language, LSP, frontend, and smoke tests cover layout-block admission.
- Boundary tests prevent M23 from claiming advanced layout, routing, library, or EPLAN parity.

## 5. Non-Goals

- Full EPLAN parity
- Advanced electrical routing intelligence
- Standards-specific label generation
- Physical wire, harness, cable tray, cabinet, or 3D routing
- Full IEC/QElectroTech library ingestion
- Public repository/import ecosystem
- AI layout optimization
- Final ELK/layout-stack selection
- Free-form canvas drawing persistence
- Raw pixel-coordinate layout language as the primary authored form

## 6. MVP Scope

### 6.1 In Scope

- ANTLR grammar support for the selected layout block
- IDE Tree-sitter grammar support for the selected layout block
- Authored AST `LayoutDeclaration` and layout statement model
- Compiler admission and constraint lowering
- Layout intent model between AST and constraints
- Layout constraint priority model
- LSP acceptance and diagnostics
- Graph workbench source edit using accepted syntax
- `examples/m23/sample-project` with real layout-block `.athena` files
- Regression coverage proving valid layout blocks parse, compile, project, and survive IDE workflow
- Documentation correcting the M22 preview-only boundary

### 6.2 Out Of Scope

- New layout algorithm depth beyond consuming the admitted constraints
- Advanced route/label hint persistence
- New visual EPLAN parity work
- Broader component library or repository milestones
- AI/ELK final technology selection

## 7. Success Metrics

**Primary**

- **SM-1:** A real `.athena` file containing `layout schematic-sheet { ... }` parses and compiles
  without syntax diagnostics.
- **SM-2:** The M23 sample project opens in Theia and the layout-block file is accepted by editor,
  LSP, Problems, and Graphical View.
- **SM-3:** ANTLR4 and Tree-sitter accept the same valid layout fixtures and reject or recover the
  same invalid/incomplete fixture families where applicable.
- **SM-4:** Authored layout hints lower through layout intent into Athena layout constraints and
  influence deterministic layout facts.
- **SM-5:** An approved graph workbench layout preview applies valid source syntax and reprojects.
- **SM-6:** Active-source projection, outline behavior, and accepted canvas behavior remain intact.

**Counter-metrics**

- **SM-C1:** Do not claim full EPLAN parity.
- **SM-C2:** Do not persist hidden canvas state.
- **SM-C3:** Do not make layout syntax a raw coordinate language.
- **SM-C4:** Do not make ELK or any adapter the syntax, architecture, or persistence authority.

## 8. Assumptions Index

- The selected M22 layout-block shape remains the correct starting point, but M23 owns the decision
  that the block is system-scoped first.
- M23 builds on M17's dual-parser architecture: ANTLR4 is the compiler/LSP parser and Tree-sitter is
  the IDE syntax UX parser.
- Layout blocks can be admitted without breaking existing package/import/system grammar in both
  ANTLR4 and Tree-sitter.
- Layout hints can lower through a layout intent model into existing M21/M22 layout constraint and
  fact contracts.
- Theia source edit plumbing from M22 can be reused once the language accepts the emitted syntax.
- M23 should create a new sample project rather than retroactively making M22 appear more complete
  than it was.

## 9. Open Questions

- Should layout hint subjects use raw authored names first, or canonical semantic ids when available?
- Should invalid unknown-subject layout hints be syntax diagnostics, semantic diagnostics, or both?
- Should M23 expose layout declarations in Outline, or only keep them accepted and diagnostic-aware?
- Should route/label hints get syntax reservation now, or remain fully deferred?
- Should explicit `prefer` / `require` source keywords be admitted in M23, or should priority remain
  model-only with default authored preference priority?
- What is the exact Tree-sitter node shape for `layout`, `place`, `align`, and `group`, and how
  should it map to existing editor features?
