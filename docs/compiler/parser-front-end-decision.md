# Athena M0 Parser Front-End Decision

## Decision

Athena M0 uses a **standalone textual DSL** as the canonical authored source input.

The front-end stack for Story `1.2` is:

- standalone text DSL
- hand-written Kotlin tokenizer
- hand-written recursive-descent parser
- syntax-only AST in Kotlin data classes / sealed hierarchies
- explicit source spans and syntax diagnostics with file, line, and column provenance

## Why

This keeps the M0 thesis clean:

- the DSL is the authored source of truth
- the AST is syntax only
- `Engineering IR` arrives later as the canonical semantic model

M0 does not need a heavy grammar toolchain yet. The first language cut is intentionally small and constrained, so a hand-written Kotlin parser is the simplest way to keep the compiler front end readable, deterministic, and easy to debug.

## Explicit Non-Choices

### Not Kotlin `@DslMarker` as the production language

Kotlin `@DslMarker` is useful for embedded Kotlin builders, but that would make Kotlin itself the practical authoring language. For Athena M0, the canonical source must be an independent engineering language, not a Kotlin-hosted builder.

If an internal Kotlin builder becomes useful later for tests or fixtures, it must remain secondary and must not replace the text DSL.

### Not `Tree-sitter` as the compiler parser

`Tree-sitter` is valuable for editor tooling such as syntax highlighting, incremental parsing, and IDE support. It is not the canonical compiler parser for M0.

If editor tooling is added later, `Tree-sitter` belongs at the authoring-tool boundary, not at the semantic compiler boundary.

### Not ANTLR by default

ANTLR is a real option, but not the default. Story `1.2` should start with the smallest Kotlin-native parser implementation.

ANTLR should be introduced only if the language shape clearly outgrows a hand-written parser and the tradeoff is explicit.

### Not LLVM or MLIR

Athena M0 is not building a machine-code compiler backend. Its compiler is a semantic engineering compiler, so LLVM/MLIR are the wrong abstraction layer for the current front-end problem.

## Implementation Rules

- Keep AST types in `language/`.
- Keep the AST free of semantic validation logic, rule execution, and `Engineering IR` concepts.
- Keep the DSL free of layout, geometry, and renderer-specific concerns.
- Parse failure must stop later phases.
- Examples under `examples/` should be real text DSL files that can become conformance artifacts later.

## Revisit Conditions

Revisit this decision only if one of these becomes true:

- the M0 grammar becomes materially more complex than a small recursive-descent parser can support cleanly
- multiple front-end syntaxes must target the same AST layer immediately
- editor tooling becomes a first-class deliverable and needs a dedicated incremental parser

Until one of those conditions is true, keep the front end simple.
