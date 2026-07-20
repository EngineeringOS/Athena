# M17 Addendum

This addendum sharpens the implementation direction for M17 without replacing the future architecture spine.

## 1. Product Definition

Recommended milestone name:

```text
M17
Parsing, AST, And Editor Language Foundation
```

Do not reduce M17 to:

```text
syntax sugar milestone
import feature milestone
parser-library swap
```

because M17 exists to harden Athena's long-term language architecture.

## 2. Mission Statement

Recommended statement:

> Prove that Athena can grow its authored language on a durable architecture by using ANTLR4 for compiler parsing, Tree-sitter for IDE syntax UX, and an explicit authored AST boundary before lowering to Engineering IR.

## 3. Current-State Observation

The repo already has an important good boundary:

```text
:kernel:language
    ->
syntax-only AST with spans and diagnostics
```

The actual weakness is:

```text
handwritten parser durability
future grammar growth cost
missing IDE-native syntax parser
```

So M17 should harden an existing good seam rather than replace it with a worse one.

## 4. Core Architecture Position

The correct chain is:

```text
Authored Source
        ->
ANTLR4 Lexer/Parser
        ->
Athena Authored AST
        ->
Lowering
        ->
Engineering IR
```

and separately:

```text
Authored Source
        ->
Tree-sitter CST
        ->
highlight / folding / outline / selection ranges
```

Avoid:

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

Those paths would erode the architecture.

## 5. Recommended Module Direction

M17 architecture should evaluate a layout such as:

```text
kernel/language
    public authored AST
    parse facade
    diagnostics model

kernel/language
    src/main/antlr
    compiler-parser grammar and generated sources

ide/
    tree-sitter-athena or equivalent owned grammar package
```

The exact generated-source placement can vary, but the public architectural rule should stay:

```text
downstream code depends on Athena AST contracts
not generated parser internals
```

## 6. Suggested Delivery Components

### 6.1 Compiler Parser Migration

Recommended first delivery:

- introduce ANTLR grammar for the current supported syntax subset
- adapt ANTLR parse output into today's authored AST model or its direct successor
- preserve spans and syntax diagnostics
- keep lowering operating on authored AST only

### 6.2 Tree-sitter Grammar And Queries

Recommended second delivery:

- publish a Tree-sitter grammar for the same narrow syntax subset
- add highlight queries
- add folding and outline-oriented queries if practical
- prove invalid source still yields a usable syntax tree

### 6.3 IDE Integration

Recommended third delivery:

- attach Tree-sitter to Theia syntax UX only
- keep LSP diagnostics and semantic meaning on the compiler parser path
- avoid turning Tree-sitter into a second semantic feature engine

## 7. Suggested Proof Slice

The best narrow M17 proof is:

1. Parse today's current Athena syntax through ANTLR4.
2. Lower it through authored AST into the same canonical `Engineering IR` shape as before.
3. Open the same source in Theia and prove Tree-sitter-backed syntax UX works on valid and incomplete files.
4. Emit compiler diagnostics on invalid source through the compiler/LSP path.
5. Demonstrate that editor parsing and compiler parsing are complementary, not competing truths.

## 8. Verification Direction

Recommended verification should include:

- language parser tests for ANTLR-backed parser parity
- compiler tests proving lowering continuity from authored AST to `Engineering IR`
- IDE/frontend tests for Tree-sitter-backed syntax UX
- at least one valid-source and one invalid/incomplete-source proof input

## 9. Explicit Exclusions

M17 should not include:

- final import resolution semantics
- full package-aware authored semantics
- full macro language redesign
- full compiler incremental semantic rebuild redesign
- any change that makes editor parsing the source of semantic truth

## 10. Carry-Forward Value

If M17 is done correctly, later milestones become much cheaper for:

- imports
- package-aware source features
- macro-use syntax
- richer authoring flows
- AI-authored patch interpretation
- graph-to-source round-trip hardening

That is why M17 should be treated as a strategic foundation milestone rather than a cosmetic parser refactor.

## 11. Deferred Carry-Forward From Earlier Milestones

These items should be carried into M17 as guardrails, not widened into separate feature tracks.

### 11.1 From M14

- Keep compiler-owned semantic context as the integration seam. Parser replacement must not move semantic truth into IDE or parser internals.
- Keep direct DSL positioned as expert serialization. M17 should strengthen language infrastructure without making Athena product strategy DSL-first.
- Prefer real governed repository fixtures when milestone proof claims repository-level behavior.

### 11.2 From M15

- Keep all future authoring surfaces above the same intent and preview contract. M17 must not create a new edit path that bypasses M8-era architectural rules.
- Keep renderer and presentation work downstream of canonical identity and component knowledge. Tree-sitter integration must stay syntax-UX-only.
- Do not widen M17 into unsupported Theia layout experiments such as reparenting stock outline widgets. If outline is part of the proof, keep it on a supported path.

### 11.3 From M16

- Keep `ANTLR4` as compiler/LSP parser and `Tree-sitter` as IDE parser. Do not blur the split during implementation.
- Any proof that claims governed behavior should include a checked-in repository-backed fixture plus one UI/E2E verification path where practical.
- Preserve source spans and provenance strongly enough that downstream traceability and inspection do not regress after parser migration.
- Keep milestone verification disciplined on this Windows repo: sequential Gradle runs only, plus encoding audit after touched docs.
