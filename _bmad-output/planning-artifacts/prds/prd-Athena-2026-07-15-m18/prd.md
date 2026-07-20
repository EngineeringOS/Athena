---
title: Athena M18
status: draft
created: 2026-07-15
updated: 2026-07-15
---

# PRD: Athena M18

*Codename: Athena Project Semantic Graph And Package Resolution.*

## 0. Document Purpose

This PRD defines the next milestone after the completed M17 proof.

M17 proved that Athena can evolve its authored language on a durable parser architecture:

- `ANTLR4` for compiler and LSP parsing
- `Tree-sitter` for IDE syntax UX
- an explicit authored AST boundary before lowering to `Engineering IR`

The next unresolved gap is no longer parser durability.

It is project-scale, package-aware authored language meaning.

Athena already proved governed repository/package meaning in M5 and durable language foundations in
M17, but the authored language still behaves too much like a single-file semantic universe. M18
exists to connect those two completed foundations so authored source can import, resolve, and link
across governed packages and source units through one compiler-owned project semantic graph without
weakening compiler authority.

This PRD builds directly on the current repository state, including:

- M5 governed repository contract, deterministic package graph, and canonical `athena.lock`
- M14 governed component-knowledge foundation
- M16 reuse pressure for cross-package authored semantics
- M17 ANTLR/authored-AST/Tree-sitter language foundation
- the current Athena IDE/LSP authority boundaries

Implementation-shaped detail that is useful but too low-level for the PRD body is captured in
[`addendum.md`](addendum.md).

## 1. Vision

M0 proved DSL to `Engineering IR`.
M1 proved runtime-owned workspace and mutation orchestration.
M2 proved explicit projection layers.
M3 proved hosted extensibility.
M4 proved the first serious Athena IDE shell.
M5 proved governed repository meaning and deterministic package graph resolution.
M6 proved semantic SCM.
M7 proved graphical projection and the first renderer path.
M8 proved one mutation authority across source and graph.
M9 proved executable engineering knowledge.
M10 proved AI-assisted reasoning above governed knowledge outputs.
M11 proved serious electrical workbench depth.
M12 proved renderer trust and operator-surface hardening.
M13 proved a real presentation language foundation.
M14 proved a governed component-knowledge foundation.
M15 proved guided semantic authoring above that stack.
M16 proved governed assembly-scale reuse through Semantic Macros.
M17 proved durable authored-language infrastructure.

M18 must now prove the next strategic layer:

- Athena can treat authored source as a package-aware semantic workspace instead of isolated files
- compiler-owned import resolution can bind authored language meaning to the governed package graph
- project-scale dependency and symbol resolution can remain deterministic and inspectable
- LSP semantic behavior can expand across package boundaries without moving authority into the
  frontend
- Tree-sitter can mirror package-aware syntax for UX without becoming semantic truth

In plain terms:

- M17 proved Athena can grow syntax safely
- M18 proves Athena can grow authored meaning beyond one file into a real project semantic graph
  while staying compiler-owned

## 1.1 Why Now

The current gap is no longer primarily:

- parser durability
- package-graph governance
- semantic SCM
- renderer ownership
- one mutation authority
- component knowledge
- guided authoring flow

The current gap is project-scale semantic workspace meaning.

Today Athena already has two strong foundations:

- one governed repository/package graph with canonical manifest and lock semantics
- one durable compiler parser and authored AST seam

But Athena still lacks the connection between them in the authored language itself.

Without solving that now, Athena risks drifting into the wrong future paths:

- `import` becomes only a filesystem shortcut
- package-aware meaning is reconstructed from IDE heuristics instead of compiler authority
- multiple files and packages still fail to become one deterministic dependency and symbol graph
- future authored constructs such as imports and package-aware declarations land as isolated syntax
  patches instead of governed language evolution
- package graph and authored language remain parallel foundations instead of one coherent model

M18 is the correct milestone to solve this because:

- M5 already froze the governed repository/package graph vocabulary
- M17 already froze the parser and authored-AST architecture
- M14 and M16 increase pressure for authored source to reference reusable governed meaning across
  boundaries
- the IDE and LSP path are already mature enough to expose package-aware diagnostics and
  navigation through the existing authority model

## 2. Target User

### 2.1 Jobs To Be Done

- Compiler engineers need authored imports and symbol linking to resolve through the governed
  package graph rather than through ad hoc filesystem logic.
- Package authors need Athena source to reference governed packages and source units explicitly and
  deterministically.
- Platform engineers need project-scale dependency and symbol resolution before Athena can behave
  like a real engineering programming-language foundation.
- IDE engineers need cross-file and cross-package navigation, symbols, and diagnostics to stay on
  the compiler/LSP path.
- Platform architects need package-aware language growth to remain consistent with M5 repository
  authority and M17 parser authority.
- Future language-feature authors need a package-aware semantic workspace foundation before later
  authored constructs expand further.

### 2.2 Non-Users (M18)

- Teams expecting M18 to deliver a broad new authored-language redesign beyond the first package-aware proof
- Teams expecting M18 to deliver remote registry transport, package marketplace behavior, or
  publish workflows
- Teams expecting M18 to move semantic package meaning into frontend-local state
- Teams expecting M18 to turn Tree-sitter into a second semantic engine
- Teams expecting M18 to become a renderer, presentation, AI-generation, or ecosystem-expansion
  milestone

### 2.3 Key User Journeys

- **UJ-1. Aaron opens a governed Athena repository and authored imports resolve through the same package graph Athena already governs.**
  - **Persona + context:** Aaron is checking whether Athena now behaves like a package-aware engineering language instead of a single-file DSL proof.
  - **Entry state:** A governed repository already exists with valid `athena.yaml`, `athena.lock`, and package dependencies.
  - **Path:** Aaron opens the repository, edits or inspects authored source with package and import declarations, and Athena resolves imports through the compiler-owned package graph.
  - **Climax:** Aaron can explain the authored program as one semantic workspace spanning multiple source units, governed packages, and deterministic symbol resolution.
  - **Resolution:** Athena becomes credible as a package-aware engineering language rather than only a per-file language proof.

- **UJ-2. Maya authors an import or symbol reference and receives package-aware compiler diagnostics and navigation immediately.**
  - **Persona + context:** Maya is using Athena as a serious engineering workspace where referenced meaning may live outside the current file.
  - **Entry state:** Athena is running through the existing LSP and workbench path.
  - **Path:** Maya adds or changes a package or import declaration, then sees either successful linking or typed diagnostics for missing packages, missing symbols, invalid references, or graph cycles.
  - **Climax:** Maya can jump to definitions and inspect references across source-unit or package boundaries.
  - **Resolution:** Package-aware authored source becomes operable in the Athena IDE without creating a second semantic authority.

- **UJ-3. Priya verifies that import resolution remains downstream of compiler-owned semantic authority.**
  - **Persona + context:** Priya is checking that package-aware language growth does not erode Athena's architecture.
  - **Entry state:** M18 proof surfaces are present in compiler, LSP, frontend, and examples.
  - **Path:** Priya traces package parsing, import resolution, symbol linking, diagnostics, and navigation through the compiler/LSP path.
  - **Climax:** Priya confirms that Tree-sitter mirrors syntax only and that package-aware meaning still belongs to compiler/runtime authority.
  - **Resolution:** Priya accepts M18 as the correct base for later package-aware authored-language growth.

## 3. Glossary

- **Import Declaration** - The authored language construct that names governed external source or package meaning for use in the current source unit.
- **Package Declaration** - The authored language construct that positions a source unit inside a governed semantic namespace.
- **Source Unit** - One authored `.athena` file participating in a package-aware compilation workspace.
- **Governed Package Graph** - The deterministic package/dependency graph already owned by Athena manifest, lock, compiler, and runtime authority.
- **Project Semantic Graph** - The compiler-owned combination of governed package dependencies, source-unit availability, and symbol linking that allows one repository to behave as one semantic workspace.
- **Semantic Namespace** - The governed imported meaning made available to the current source unit through package-aware resolution, including declarations and downstream admitted engineering knowledge carried by the imported package.
- **Symbol Linking** - The compiler-owned process of binding authored references to governed declarations across source-unit or package boundaries.
- **Package-Aware Semantic Workspace** - Athena's authored-language operating model where semantic meaning can span more than one file while remaining governed and deterministic.
- **Package-Aware Diagnostics** - Typed diagnostics produced when imports, package availability, symbol resolution, or related linking rules fail.

## 4. Strategic Decision

M18 is a **project semantic graph and package resolution milestone**, not merely an import-keyword milestone.

Why:

- Athena already proved the package graph in M5
- Athena already proved the parser architecture in M17
- the real missing capability is compiler-owned linking between authored language and governed
  package meaning
- later package-aware authored constructs should land on a proven project semantic graph instead
  of on syntax-only patches

The architectural rule is:

```text
Governed Repository
        ->
Package Resolver / Dependency Graph
        ->
Project Semantic Graph

Authored Source Units
        ->
Compiler Parser (ANTLR4)
        ->
Authored AST
        ->
Compiler-Owned Import Resolution And Symbol Linking Against The Project Semantic Graph
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

M18 must therefore avoid:

```text
import
    ->
filesystem lookup
    ->
paste AST
```

or:

```text
Tree-sitter
    ->
package resolution
```

Those paths would break Athena's architecture.

## 5. Product Position

Athena should treat authored imports as a semantic contract between the authored language and the
governed package graph, not as a lightweight source-include convenience.

The correct Athena model is:

```text
Authored language truth for package-aware compilation
  =
compiler parser + authored AST + compiler-owned package resolution + project-scale symbol linking

Editor syntax UX
  =
Tree-sitter-backed syntax structure only

Canonical engineering truth
  =
Engineering IR
```

Consequences:

- package-aware meaning stays compiler-owned
- imported meaning behaves like engineering capability, not only like code inclusion
- authored language can grow across source-unit and package boundaries without frontend semantic
  drift
- later language growth lands on a real project semantic graph foundation
- M18 prepares later package-aware authored behavior without widening into package transport or
  ecosystem breadth

## 6. Features

### 6.1 Import And Package Syntax Foundation

**Description:** Athena extends the M17 language foundation so authored source can express a first
supported package-aware syntax slice without broadening into a general language redesign. Realizes
UJ-1, UJ-2, UJ-3.

#### FR-1: Parse Governed Package And Import Declarations Through The Compiler Parser And Authored AST

Athena can parse a first supported `package` and `import` declaration slice through the `ANTLR4`
compiler path and adapt it into Athena-owned authored AST contracts. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- package declarations, qualified names, and import declarations are accepted through the compiler
  parser on the supported M18 proof slice.
- Authored AST contracts carry package and import intent as Athena-owned syntax contracts rather
  than generated parser types.
- Source spans and syntax diagnostics for package-aware syntax remain inspectable through the same
  syntax boundary proven in M17.

#### FR-2: Keep The First Package-Aware Syntax Proof Narrow And Deliberate

Athena can introduce package-aware syntax without quietly widening into a broad new authored
statement family. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The first proof can support the minimum package and import syntax required for package-aware
  authored semantics.
- Alias support may be included if it materially improves the proof.
- Export/visibility systems, broad new declaration families, or unrelated statement-language
  expansion are not required for M18 success unless explicitly promoted into scope.

### 6.2 Project Semantic Graph And Package Resolution

**Description:** Athena binds authored import semantics to the governed repository/package graph it
already owns instead of inventing a parallel package model. Realizes UJ-1, UJ-2, UJ-3.

#### FR-3: Build A Compiler-Owned Project Semantic Graph From Governed Repository State

Athena can build a compiler-owned project semantic graph from manifest-declared, lock-backed
package state and authored source availability. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Import resolution reuses Athena's governed repository contract, deterministic resolution input,
  resolved package graph, and source-unit availability rather than bypassing them.
- The compiler can explain package dependencies and symbol availability as one inspectable semantic
  graph rather than as disconnected steps.
- Raw filesystem traversal, JVM classpath coincidence, or frontend heuristics do not become
  practical import authorities.
- The same repository state produces the same package-resolution and import-resolution result.

#### FR-4: Surface Typed Package-Aware Import Diagnostics

Athena can surface typed diagnostics when import or package resolution fails. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Missing package, missing symbol, invalid availability, or other import-resolution failures appear
  as Athena-owned diagnostics.
- Circular package dependency failures can be represented as typed package-aware diagnostics.
- Diagnostics remain traceable to compiler/LSP authority rather than frontend-local engines.
- Failure quality stays inspectable enough for package-aware authoring and review workflows.

### 6.3 Cross-File And Cross-Package Symbol Linking

**Description:** M18 proves that Athena can link authored meaning across source units and packages
without losing deterministic semantic ownership. Realizes UJ-1, UJ-2, UJ-3.

#### FR-5: Link Authored Symbols Across Source Units And Packages

Athena can bind authored references to governed declarations across source-unit and package
boundaries. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- One source unit can reference declarations made available through imports from another source unit
  or governed package.
- Imported namespaces behave as semantic namespaces, not just file handles.
- Linked declarations retain stable provenance and identity strong enough for diagnostics and IDE
  reveal/navigation.
- Package-aware symbol availability is compiler-owned rather than reconstructed in the frontend.

#### FR-6: Resolve Imported Meaning As Engineering Capability, Not Only As Syntax

Athena can treat imported semantic namespaces as governed engineering capability made available to
later compiler/runtime consumers. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Imported meaning can feed later compiler/runtime layers through existing governed seams rather
  than through ad hoc side-loading.
- Where a package already carries admitted engineering meaning such as component knowledge, ports,
  rules, presentation, documentation, or AI context, the import path does not sever that governed
  linkage.
- M18 does not need to reinvent every downstream consumer, but it must position imports as
  stronger than code-only inclusion.

#### FR-7: Preserve Canonical Lowering And Semantic Determinism After Linking

Athena can keep package-aware linking downstream of compiler-owned semantic truth and upstream of
canonical lowering. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Linked authored meaning still lowers through the canonical compiler path into `Engineering IR`.
- Import resolution does not devolve into AST pasting or hidden include-style expansion.
- The same governed repository state yields deterministic linking and lowering outcomes.

### 6.4 LSP And IDE Package-Aware Operation

**Description:** The IDE becomes package-aware through the existing compiler/LSP authority path
without introducing a second semantic implementation. Realizes UJ-2, UJ-3.

#### FR-8: Expand LSP Semantic Behavior Across Package Boundaries

Athena can expose package-aware semantic diagnostics and navigation through the existing LSP path.
Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Diagnostics can report import, cycle, and symbol-linking failures across source-unit or package
  boundaries.
- Diagnostic surfaces may expose relevant alternatives or nearby available symbols where practical,
  but such affordances remain downstream of compiler authority.
- Go-to-definition and references can cross file or package boundaries where authored semantics now
  allow it.
- Document or workspace symbol behavior can reflect the package-aware authored workspace rather than
  a single-file view.

#### FR-9: Mirror Package-Aware Syntax On The Tree-sitter UX Path Only

Athena can mirror package-aware syntax on the Tree-sitter path for syntax UX without giving
Tree-sitter semantic authority. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Tree-sitter can recognize supported package-aware syntax well enough for highlighting, folding,
  outline, or related syntax-oriented UX.
- Tree-sitter does not become responsible for package resolution, symbol linking, or semantic
  diagnostics.
- The M17 parser responsibility split remains intact after M18.

### 6.5 Proof Corpus And Growth Safety

**Description:** M18 must close on executable evidence and clear future boundaries rather than only
on syntax implementation. Realizes UJ-1, UJ-2, UJ-3.

#### FR-10: Publish A Repository-Backed Project-Scale Import And Linking Proof Corpus

Athena can publish a repository-backed proof corpus for valid and invalid package-aware authored
behavior. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- The proof corpus includes successful and failing import/linking examples rooted in governed
  repository state.
- The corpus covers at least one single-package baseline, one cross-package success path, one
  import-not-found path, and one cycle or graph-invalid path.
- M18 closeout depends on executable package-aware proof inputs rather than on prose-only claims.

#### FR-11: Keep Later Package-Aware Growth Explicit Instead Of Accidental

Athena can make later package-aware authored-language growth cheaper without forcing M18 to absorb
all later package semantics now. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Milestone artifacts clearly state what M18 proves and what remains deferred.
- M18 does not quietly absorb package marketplace behavior, full export/visibility systems, or
  broad authored-language redesign.
- The completed proof leaves later package-aware language milestones on a safer foundation than
  pre-M18 Athena had.

## 7. Non-Goals (Explicit)

- remote registry transport or package marketplace behavior
- semantic SCM redesign or publish workflow redesign
- broad new authored declaration families unrelated to the first package-aware proof
- turning Tree-sitter into a semantic engine
- frontend-owned package resolution or diagnostics
- renderer or presentation milestone work
- AI-generation or agent workflow expansion as the milestone center
- full dependency-management or build-tool replacement work
- vendor library marketplace or cloud registry work
- multi-root repository sessions
- multi-package repository authoring or package-local manifest design beyond the already proven M5
  boundaries

## 8. MVP Scope

### 8.1 In Scope

- first supported package-aware syntax slice on the `ANTLR4` compiler path
- authored AST support for package and import intent
- compiler-owned project semantic graph and import resolution against the governed package graph
- typed import/package diagnostics
- cross-file or cross-package symbol linking on the supported proof slice
- semantic namespace proof strong enough to show imported meaning behaves as engineering capability
  rather than only as code inclusion
- package-aware LSP diagnostics, definition, references, and symbol behavior
- Tree-sitter syntax support for package-aware forms
- repository-backed M18 proof corpus

### 8.2 Out Of Scope For MVP

- remote registry or publish transport
- broad export/visibility system unless the first proof strictly requires it
- broad new statement or declaration families beyond the package-aware proof
- frontend semantic resolution engines
- multi-root or multi-package repository-authoring redesign
- general build-system or package-manager replacement work

## 9. Success Metrics

**Primary**

- **SM-1:** The supported M18 package-aware syntax slice parses through the `ANTLR4` compiler path, adapts into authored AST, and participates in successful project-scale package-aware compilation. Validates FR-1, FR-3, FR-5, FR-7.
- **SM-2:** A compiler-owned project semantic graph exists strongly enough to explain package dependencies, source-unit availability, and symbol linking on the supported proof slice. Validates FR-3, FR-5.
- **SM-3:** Invalid import, symbol-linking, or package-cycle cases surface typed Athena diagnostics with usable provenance through the compiler/LSP path. Validates FR-4, FR-8.
- **SM-4:** Athena IDE semantic navigation can cross supported file or package boundaries without moving authority out of the compiler/LSP path. Validates FR-5, FR-8.

**Secondary**

- **SM-5:** Tree-sitter mirrors supported package-aware syntax for syntax UX while remaining outside semantic authority. Validates FR-9.
- **SM-6:** One repository-backed proof corpus demonstrates deterministic success and failure behavior for project-scale package-aware authored semantics. Validates FR-10.
- **SM-7:** Imported semantic namespaces remain strong enough to carry admitted engineering meaning into later compiler/runtime consumers without breaking governance boundaries. Validates FR-6.
- **SM-8:** Milestone artifacts leave later package-aware authored-language growth on an explicitly safer and narrower foundation than before M18. Validates FR-11.

**Counter-metrics (do not optimize)**

- **SM-C1:** Do not optimize for syntax breadth over project semantic graph correctness.
- **SM-C2:** Do not optimize for frontend cleverness over compiler authority.
- **SM-C3:** Do not optimize for package ecosystem breadth over the first governed language proof.

## 10. Cross-Cutting NFRs

- **NFR-1 Authority Preservation:** `Engineering IR` remains canonical engineering truth after package-aware linking.
- **NFR-2 Repository Boundary Discipline:** Import resolution binds only through Athena's governed repository/package graph, not through raw filesystem or frontend-local heuristics.
- **NFR-3 Determinism:** The same repository, manifest, lock, and source state produce the same import-resolution and linking results.
- **NFR-4 Provenance Preservation:** Package-aware diagnostics and navigation preserve usable file/span provenance across boundaries.
- **NFR-5 Boundary Split Preservation:** `ANTLR4` remains the compiler/LSP parser and Tree-sitter remains syntax UX only.
- **NFR-6 Project-Scale Growth Safety:** M18 must move Athena from single-file semantics toward project-scale semantic composition without expanding into ecosystem or marketplace breadth.

## 11. Open Questions

1. What is the minimum first supported package-aware syntax slice Athena should freeze in M18: package declaration plus package-only import, package declaration plus symbol-target import, or package declaration plus alias support?
2. Does the first M18 proof require any explicit export/visibility rule, or is package-aware availability sufficient without adding a broader surface yet?
3. What is the narrowest symbol-linking proof that still demonstrates a real compiler-owned project semantic graph: declaration-level linking, source-unit-level linking, or another governed unit?
4. Should package-aware workspace symbols be part of the first M18 proof, or should M18 limit LSP expansion to diagnostics, definition, references, and later add richer symbol search?
5. How explicit should M18 be about semantic namespace scope: is imported declaration availability enough, or should the milestone also require one proof that imported packages preserve admitted downstream engineering meaning such as component knowledge?

## 12. Final Statement

M17 proved:

> Athena can scale authored language depth through `ANTLR4` compiler parsing, `Tree-sitter` IDE
> parsing, and a preserved authored AST boundary without weakening canonical semantic authority.

M18 must now prove:

> Athena can scale authored meaning across governed packages and multiple source units through
> compiler-owned package resolution, project-scale symbol linking, and semantic namespaces without
> weakening repository authority, compiler authority, or canonical lowering.

That is the next milestone where Athena stops behaving like a single-file language proof and starts
behaving like a project-scale package-aware engineering language.
