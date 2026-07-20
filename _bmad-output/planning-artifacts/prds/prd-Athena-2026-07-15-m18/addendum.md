# M18 Addendum

This addendum sharpens the implementation direction for M18 without replacing the future
architecture spine.

## 1. Product Definition

Recommended milestone name:

```text
M18
Project Semantic Graph And Package Resolution
```

Do not reduce M18 to:

```text
import keyword milestone
syntax sugar milestone
package marketplace milestone
```

because M18 exists to prove compiler-owned package-aware authored semantics above the already
completed repository/package graph and parser foundations.

## 2. Mission Statement

Recommended statement:

> Transform Athena from a single-file semantic language into a composable engineering language
> where projects, components, and governed domain meaning can be imported, resolved, validated,
> and consumed through one compiler-owned semantic graph.

## 3. Current-State Observation

The repo already has two important good boundaries:

```text
M5
repository-root manifest + canonical lock + deterministic package graph
```

and:

```text
M17
ANTLR4 compiler parser + authored AST + Tree-sitter syntax UX split
```

The actual weakness is the gap between them:

```text
authored language does not yet consume governed package meaning directly
```

So M18 should connect two strong existing seams rather than inventing a third parallel package or
language model.

## 4. Core Architecture Position

The correct chain is:

```text
Governed Repository
        ->
Package Resolver
        ->
Project Semantic Graph

Authored Source Units
        ->
ANTLR4 Parser
        ->
Athena Authored AST
        ->
Compiler-Owned Import Resolver
        ->
Compiler-Owned Symbol Linking Against The Project Semantic Graph
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
highlight / folding / outline / syntax-oriented editor behavior
```

Avoid:

```text
import
    ->
filesystem traversal
    ->
paste AST
```

or:

```text
Tree-sitter
    ->
package resolution
```

Those paths would erode the architecture.

## 5. Recommended Scope Guard

The first M18 proof should be narrower than the original draft suggested.

Recommended scope:

- add the first supported package/import syntax slice
- bind imports to the governed package graph
- prove compiler-owned dependency and symbol linking
- expand LSP semantic navigation across boundaries

Do not quietly widen M18 into:

- a broad new authored declaration family
- a new authoring language centered on `machine`, `component`, or `use` unless separately planned
- package marketplace or remote transport work
- renderer, presentation, or AI milestone work

## 6. Recommended Package Vocabulary Rule

M18 should reuse Athena's actual repository/package nouns from M5 rather than introducing a new
parallel package vocabulary in milestone planning.

That means the milestone should stay grounded in existing concepts such as:

- governed repository contract
- `athena.yaml`
- `athena.lock`
- deterministic resolution input
- resolved package graph
- repository graph publication

The language feature should connect to those contracts instead of renaming them.

## 7. Semantic Namespace Position

Athena imports should be stronger than ordinary code imports.

The M18 milestone should position imported namespaces as:

```text
engineering capability
```

not merely:

```text
source include
```

That means imported package meaning should stay compatible with downstream governed consumers such
as:

- component knowledge
- ports
- rules
- presentation
- documentation
- AI context

M18 does not need to expand every one of those consumers. It does need to preserve the principle
that an Athena import can carry governed engineering meaning, not only syntax-level declarations.

## 8. Suggested First Proof Slice

The best narrow M18 proof is:

1. Parse the first supported package and import syntax through `ANTLR4`.
2. Adapt package/import intent into Athena-owned authored AST contracts.
3. Build a compiler-owned project semantic graph from governed package state.
4. Resolve imports only through that graph.
5. Link at least one real authored symbol across file or package boundaries.
6. Lower the linked result through the canonical compiler path.
7. Surface package-aware diagnostics and navigation through LSP.
8. Mirror package/import syntax in Tree-sitter for UX only.

## 9. Suggested Verification Direction

Recommended verification should include:

- compiler tests for successful import resolution and deterministic lowering
- compiler tests for failing import/package/symbol/cycle cases
- LSP tests for package-aware definition, references, and symbols
- Tree-sitter grammar tests for package/import syntax
- repository-backed examples under `examples/m18/`

## 10. Suggested Proof Corpus Shape

Recommended fixture layout:

```text
examples/m18/
  single-package/
  cross-package-import/
  invalid-import/
  unresolved-symbol/
  cyclic-package/
  vendor-package/
```

The exact folder names can vary, but the proof should show both successful and failing
package-aware authored semantics on governed repository state.

## 11. Explicit Exclusions

M18 should not include:

- remote registry resolution
- publish flows
- package marketplace behavior
- cloud registry work
- broad package-local manifest redesign
- frontend semantic-resolution ownership
- broad new authored-language redesign beyond the package-aware proof
- full dependency-management or build-tool replacement work

## 12. Carry-Forward Guardrails

### 12.1 From M5

- Keep repository/package meaning rooted in Athena-owned manifest, lock, and package graph
  contracts.
- Keep one-window / one-session authority boundaries intact unless a later milestone changes them.
- Do not bypass governed package semantics with raw path tricks in the language layer.

### 12.2 From M17

- Keep `ANTLR4` as the only compiler/LSP parser authority.
- Keep Tree-sitter as syntax UX only.
- Keep authored AST as the boundary between parser output and lowering.
- Keep milestone verification grounded in executable proof inputs, not narrative claims.

### 12.3 From Earlier Product Position

- DSL remains canonical serialization, not the mandatory mainstream UI.
- Package-aware language growth should strengthen Athena's platform layer, not pull semantic truth
  into product-shell code.

## 13. Extension Note

The review feedback used `.athena` examples to describe the target language shape.

For this PRD, the safer rule is:

- treat those examples as authored-language intent examples
- keep the milestone neutral about any source-extension rename unless a separate milestone promotes
  that change explicitly

## 14. Planning Consequence

If M18 is done correctly, later milestones become much cheaper for:

- package-aware authored constructs beyond first import support
- richer reuse declarations
- broader cross-package navigation and review
- package-aware guided authoring
- later ecosystem and publish flows

That is why M18 should be treated as a project semantic graph foundation milestone rather than as
a small syntax addition.
