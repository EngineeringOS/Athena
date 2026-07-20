---
title: Athena M17
status: draft
created: 2026-07-14
updated: 2026-07-14
---

# PRD: Athena M17

*Codename: Athena Parsing, AST, And Editor Language Foundation.*

## 0. Document Purpose

This PRD defines the next milestone after M16.

M16 proved that Athena can reuse governed engineering assemblies as Semantic Macros through preview-first expansion while keeping M8 as the sole mutation authority.

The next unresolved gap is not reuse breadth. It is language infrastructure durability.

Athena's current handwritten syntax layer and direct lowering path are good enough for the current M0-M16 language subset, but they are not the right long-term foundation for the syntax growth Athena clearly needs next, including future constructs such as imports, package-oriented language features, template use forms, richer declarations, and stronger editor experience.

M17 exists to prove that Athena can evolve its authored language on a durable architecture:

- `ANTLR4` for compiler and LSP parsing
- `Tree-sitter` for IDE syntax experience
- an explicit authored AST boundary between parser output and `Engineering IR`

This PRD builds directly on the current repository state, including:

- the existing `:kernel:language` syntax model and parser seam
- the current lowering path from authored syntax into canonical `Engineering IR`
- M4 Theia workbench foundations
- M5 package and repository governance
- M8 unified mutation authority
- M16 proof that higher-level semantics will continue to demand richer authored syntax over time

This PRD is informed by the parser-architecture discussion captured in `draft/dsl/001-discuss.md`.

Implementation-shaped direction that is useful but too low-level for the main PRD is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved DSL to `Engineering IR`.
M1 proved runtime-owned workspace and mutation orchestration.
M2 proved explicit projection layers.
M3 proved hosted extensibility.
M4 proved the first serious IDE shell.
M5 proved governed repository meaning and package graph resolution.
M6 proved semantic SCM.
M7 proved graphical projection and the first renderer path.
M8 proved one mutation authority across source and graph.
M9 proved executable engineering knowledge.
M10 proved AI-assisted reasoning above governed knowledge outputs.
M11 proved serious electrical multi-view workbench depth.
M12 proved renderer trust and operator-surface hardening.
M13 proved a real presentation language foundation.
M14 proved a governed component-knowledge foundation.
M15 proved guided semantic authoring above that stack.
M16 proved governed assembly-scale reuse through Semantic Macros.

M17 must now prove the next strategic layer:

- Athena can keep growing its authored language without turning the parser into a long-term bottleneck
- compiler truth and editor parsing can diverge by responsibility without diverging by architecture
- parser implementation can change without changing lowering, runtime, or semantic ownership
- future authored constructs such as imports and package-aware declarations have a durable place to land
- IDE responsiveness can improve without weakening compiler authority

In plain terms:

- M16 proved Athena can scale engineering meaning upward through reuse
- M17 proves Athena can scale authored language depth without losing compiler or IDE discipline

## 1.1 Why Now

The current gap is no longer primarily:

- repository governance
- semantic SCM
- renderer ownership
- one mutation authority
- component knowledge
- guided authoring flow
- semantic reuse contract

The current gap is language durability.

Athena's current syntax model already has one good property:

- `:kernel:language` owns a syntax-only AST with spans and diagnostics

But the current parser path still carries strategic risk:

- the parser implementation is handwritten
- the compiler depends on the current syntax shape remaining small and stable
- future grammar growth will make maintenance harder
- Theia editor features still need a parser path optimized for syntax UX rather than compiler truth

Without solving this now, Athena risks drifting into one of the wrong future paths:

- editor parser becomes accidental compiler truth
- compiler parser becomes responsible for editor UX concerns
- parse implementation details leak into lowering and runtime boundaries
- future syntax such as imports or template-use forms lands as ad hoc parser patches
- direct parse-tree-driven lowering makes later syntax evolution expensive

M17 is the correct milestone to solve this because:

- the current syntax surface is still narrow enough to migrate cleanly
- M5 package governance already creates pressure for future import-like syntax
- M15 and M16 increased the need for richer authored language features over time
- M4 Theia foundations are mature enough to benefit from a true editor parsing layer
- the architecture decision is now known: `ANTLR4` for compiler, `Tree-sitter` for IDE

## 2. Target User

### 2.1 Jobs To Be Done

- Compiler engineers need a parser stack that can support future Athena syntax growth without making the compiler brittle.
- IDE engineers need incremental, error-tolerant syntax parsing for highlighting, folding, outline, and similar editor features.
- Platform architects need parser implementation choices that preserve clear ownership boundaries between syntax, semantics, and canonical engineering meaning.
- Future language-feature authors need a stable authored AST seam where imports, package-aware declarations, macro use forms, and richer statements can land safely.

### 2.2 Non-Users (M17)

- Teams expecting M17 to become a full language redesign milestone
- Teams expecting M17 to deliver the full final import/package feature set
- Teams expecting M17 to replace semantic compiler truth with an editor parser
- Teams expecting M17 to become a broad IDE polish milestone unrelated to language architecture
- Teams expecting M17 to introduce a second semantic authority beside `Engineering IR`

## 3. Glossary

- **Compiler Parser** - The authoritative parser used by the compiler and LSP semantic path.
- **Editor Parser** - The syntax parser used for editor-facing UX such as highlighting and folding.
- **Authored AST** - Athena's syntax-owned internal language model that sits between parser output and lowering.
- **Parse Tree / CST** - The parser-implementation tree shape produced by a parser such as ANTLR or Tree-sitter.
- **Lowering** - The deterministic transformation from authored AST into canonical `Engineering IR`.
- **Parser Parity** - The requirement that parser replacement preserves accepted authored meaning and diagnostics for supported syntax.

## 4. Strategic Decision

M17 is a **language architecture milestone**, not a broad syntax-feature milestone.

Why:

- Athena already has a syntax-owned AST seam worth preserving
- the current parser implementation is the weak point, not the existence of a syntax model
- IDE syntax experience and compiler semantics need different parser responsibilities
- later milestones will need imports, package-aware authored constructs, macro-use syntax, and richer declarations

The architectural rule is:

```text
Authored Source
        ->
Compiler Parser (ANTLR4)
        ->
Authored AST
        ->
Lowering
        ->
Engineering IR
```

and separately:

```text
Authored Source
        ->
Editor Parser (Tree-sitter)
        ->
Syntax UX
```

M17 must therefore avoid:

```text
Tree-sitter CST
    ->
Engineering IR
```

or:

```text
ANTLR parse tree
    ->
Engineering IR
```

Those paths would break the architecture.

## 5. Product Position

Athena should treat authored language growth as a compiler-and-IDE architecture problem, not as a parser-library swap.

The correct Athena model is:

```text
Authored language truth for compilation = compiler parser + authored AST
Editor syntax UX = tree-based client parsing
Canonical engineering truth = Engineering IR
```

Consequences:

- the compiler parser can evolve without becoming the editor parser
- the editor parser can be fast and error tolerant without becoming semantic truth
- lowering remains stable even if parser implementation changes
- future syntax growth lands on a deliberate language architecture rather than a sequence of patches

## 6. Features

### 6.1 Language Architecture Boundary

**Description:** Athena hardens the existing syntax layer into an explicit long-term authored AST boundary and formally separates compiler parsing from editor parsing.

#### FR-1: Preserve One Explicit Authored AST Boundary

Athena can preserve one syntax-owned authored AST boundary between parser output and `Engineering IR`.

**Consequences (testable):**
- Lowering consumes authored AST contracts rather than generated parser-tree types.
- Parser implementation details do not leak into runtime, IDE, or `Engineering IR` models.
- The current `:kernel:language` boundary remains syntax-owned and free of semantic truth.

#### FR-2: Publish A Dual-Parser Responsibility Model

Athena can publish a clear split between compiler parser responsibility and editor parser responsibility.

**Consequences (testable):**
- `ANTLR4` is the compiler/LSP parser path.
- `Tree-sitter` is the IDE syntax UX path.
- No milestone artifact claims that Tree-sitter replaces compiler truth.

### 6.2 ANTLR4 Compiler Parser

**Description:** Athena introduces `ANTLR4` as the durable compiler-facing parser implementation for the supported language subset while preserving source spans, diagnostics, and lowering behavior.

#### FR-3: Parse Supported Athena Syntax Through ANTLR4

Athena can parse the current supported Athena authored syntax through `ANTLR4`.

**Consequences (testable):**
- The current `system`, `device`, `port`, `connect`, qualified-name, string, and property forms are accepted through the ANTLR path.
- Syntax diagnostics preserve enough provenance for compiler and LSP use.
- Supported authored inputs continue to compile through the compiler path after parser migration.

#### FR-4: Preserve Lowering Through Authored AST Instead Of Parse Trees

Athena can route the ANTLR parser output through authored AST before lowering.

**Consequences (testable):**
- No canonical lowering path consumes generated ANTLR parse-tree types directly.
- Existing lowering logic continues to operate on authored AST contracts.
- Parser replacement does not require semantic or runtime layers to depend on generated parser internals.

### 6.3 Tree-sitter IDE Syntax Path

**Description:** Athena introduces `Tree-sitter` as the IDE-facing syntax parser for responsive editor behavior without assigning it semantic ownership.

#### FR-5: Publish A Tree-sitter Grammar For Athena Source

Athena can publish a Tree-sitter grammar for the supported authored language subset.

**Consequences (testable):**
- Tree-sitter can recognize the current core authored forms used in proof files.
- Incomplete or malformed source still yields a usable syntax tree for editor features.
- Tree-sitter grammar remains scoped to syntax, not semantic validation.

#### FR-6: Use Tree-sitter For Syntax UX Rather Than Semantic Truth

Athena can use Tree-sitter for syntax-oriented editor experience only.

**Consequences (testable):**
- Highlighting, folding, outline, selection ranges, or bracket matching can use Tree-sitter outputs.
- Semantic diagnostics, resolution, and compiler meaning remain on the compiler/LSP path.
- No IDE feature claims Tree-sitter output as canonical engineering meaning.

### 6.4 Compiler And LSP Continuity

**Description:** Athena proves that parser hardening does not weaken compiler semantics or LSP-facing diagnostics.

#### FR-7: Keep Compiler And LSP Semantics On The Compiler Parser Path

Athena can keep semantic diagnostics and compilation behavior on the compiler parser path.

**Consequences (testable):**
- LSP syntax/semantic diagnostics still derive from the compiler-owned path.
- Compiler outputs remain grounded in the same lowering and semantic validation flow.
- Editor syntax UX improvements do not create a second semantic path.

#### FR-8: Preserve Useful Failure Behavior On Invalid Source

Athena can preserve useful parse feedback for incomplete or invalid source while editor syntax UX remains responsive.

**Consequences (testable):**
- The compiler parser emits inspectable diagnostics on malformed source.
- Tree-sitter still supports syntax UX on incomplete files.
- M17 does not require invalid source to become semantically meaningful in order to keep the editor usable.

### 6.5 Future Syntax Extensibility

**Description:** Athena proves that the new language architecture can carry future syntax growth cleanly without forcing M17 to deliver the entire future language.

#### FR-9: Define A Durable Landing Zone For Future Syntax

Athena can define a durable parser and AST architecture for future constructs such as imports and package-aware authored forms.

**Consequences (testable):**
- Milestone artifacts explicitly position imports and related syntax as future consumers of the M17 foundation.
- The authored AST boundary remains extensible without collapsing parser and lowering responsibilities.
- M17 architecture does not assume the current syntax surface is the final Athena language.

#### FR-10: Publish A Verification Corpus For Parser Parity And IDE Behavior

Athena can publish a narrow verification path that proves compiler parser parity and editor parser usefulness.

**Consequences (testable):**
- The current authored proof corpus parses through the ANTLR path.
- Tree-sitter proof inputs cover both valid and incomplete source examples.
- M17 closeout depends on one reliable parser-proof path, not on broad new syntax breadth.

## 7. Non-Goals (Explicit)

- final import/package semantics
- full macro language syntax redesign
- full expression language
- type system milestone
- dependency-resolution redesign
- semantic validation redesign
- replacing `Engineering IR` as canonical truth
- using Tree-sitter as compiler truth
- broad Theia visual redesign unrelated to parsing

## 8. MVP Scope

### 8.1 In Scope

- explicit authored AST boundary confirmation and preservation
- `ANTLR4` compiler parser for the currently supported Athena syntax subset
- parser-to-authored-AST adaptation path
- `Tree-sitter` grammar for the same narrow syntax subset
- first IDE syntax UX integration path through Tree-sitter
- parser parity and invalid-source proof corpus

### 8.2 Out Of Scope For MVP

- full import resolution semantics
- full new language-feature family beyond the narrow proof
- full compiler incremental parsing redesign
- semantic model redesign beyond parser/AST boundaries
- full IDE semantic-feature rewrite

## 9. Success Metrics

**Primary**

- **SM-1:** The current supported authored syntax compiles through the new ANTLR-backed compiler path while preserving lowering behavior into canonical `Engineering IR`. Validates FR-1, FR-3, FR-4.
- **SM-2:** Tree-sitter can power at least one serious IDE syntax path such as highlighting, folding, or outline on the supported syntax subset. Validates FR-5, FR-6.
- **SM-3:** Invalid or incomplete source still yields useful compiler diagnostics and usable editor syntax behavior without collapsing ownership boundaries. Validates FR-6, FR-7, FR-8.

**Secondary**

- **SM-4:** Milestone artifacts make future syntax growth such as imports architecturally straightforward rather than parser-fragile. Validates FR-9.
- **SM-5:** One repository-backed verification corpus proves parser parity and editor behavior end to end. Validates FR-10.

**Counter-metrics (do not optimize)**

- **SM-C1:** Do not optimize for broad syntax breadth over architecture durability.
- **SM-C2:** Do not optimize for editor cleverness over compiler authority.
- **SM-C3:** Do not optimize for parser-library novelty over long-term maintainability.

## 10. Cross-Cutting NFRs

- **NFR-1 Authority Preservation:** `Engineering IR` remains canonical engineering truth.
- **NFR-2 Parser Boundary Discipline:** Generated parser-tree types do not become public semantic contracts.
- **NFR-3 Provenance Preservation:** Source spans and diagnostics remain inspectable across parser migration.
- **NFR-4 IDE Responsiveness:** Editor syntax parsing remains error tolerant and low latency.
- **NFR-5 Extensibility:** The M17 architecture must make future syntax additions cheaper, not harder.

## 11. Open Questions

1. Should generated `ANTLR4` artifacts live directly under `:kernel:language`, or should Athena introduce a helper module while keeping `:kernel:language` as the public syntax boundary?
2. Should M17 include one narrow parse-only future-syntax seed such as `import`, or should it keep the proof entirely on parser replacement and Tree-sitter integration?
3. What is the cleanest Theia integration path for Tree-sitter in Athena: `web-tree-sitter`, `@theia/monaco-tree-sitter`, or a thin Athena-owned adapter?
4. How much editor outline behavior should use Tree-sitter directly versus LSP document symbols in the first proof slice?
5. Should the authored AST naming stay aligned with today's `SourceFileAst` and declaration model, or should M17 rename nodes while preserving the same architectural seam?

## 12. Final Statement

M16 proved:

> Athena can scale engineering meaning upward through governed, traceable reuse.

M17 must prove:

> Athena can scale authored language depth through `ANTLR4` compiler parsing, `Tree-sitter` IDE parsing, and a preserved authored AST boundary without weakening canonical semantic authority.

That is the next milestone where Athena turns language growth from a future risk into a governed architecture.
