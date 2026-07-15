---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-02-design-epics
  - step-03-create-stories
  - step-04-final-validation
inputDocuments:
  - ../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/prd.md
  - ../../planning-artifacts/prds/prd-Athena-2026-07-15-m18/addendum.md
  - ../../planning-artifacts/architecture/architecture-Athena-2026-07-15-m18/ARCHITECTURE-SPINE.md
---

# Athena - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Athena M18, decomposing the requirements from the M18 PRD, PRD addendum, architecture spine, and confirmed UI/canvas standards into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Parse governed package declarations, qualified names, and import declarations through the ANTLR compiler path and adapt them into Athena-owned authored AST contracts with inspectable source spans and syntax diagnostics.

FR2: Keep the first package-aware syntax slice narrow: package declaration plus package import plus symbol-target import, with alias support deferred unless needed for proof-fixture disambiguation and no broad export/visibility or unrelated statement expansion.

FR3: Build a compiler-owned project semantic graph from governed repository state, deterministic resolution input, resolved package graph, and source-unit availability without raw filesystem traversal, JVM classpath coincidence, or frontend heuristics as import authorities.

FR4: Surface typed package-aware diagnostics for missing packages, missing source units, missing symbols, invalid availability, ambiguous binding, and graph-invalid or cycle cases through compiler/LSP authority with stable codes and provenance.

FR5: Link authored symbols across source-unit or package boundaries through compiler-owned semantic namespaces, preserving stable declaration provenance and identity for diagnostics and navigation.

FR6: Resolve imported meaning as governed engineering capability, not syntax-only inclusion, preserving capability provenance such as component-knowledge availability for later compiler/runtime consumers.

FR7: Preserve canonical lowering and semantic determinism after linking: linked authored meaning still lowers through the canonical compiler path into Engineering IR without AST paste or hidden include expansion.

FR8: Expand LSP package-aware semantic behavior for diagnostics, definition, references, document symbols, and snapshot-derived workspace symbols where required, all projected from compiler-owned semantic graph snapshots.

FR9: Mirror package/import syntax on the Tree-sitter UX path for highlighting, folding, outline, and syntax recovery only; Tree-sitter must not resolve packages, link symbols, or emit semantic diagnostics.

FR10: Publish a repository-backed M18 proof corpus with executable valid and invalid package-aware authored behavior, including single-package success, cross-package success, invalid import, unresolved symbol, graph-invalid or cycle behavior, and vendor/governed package availability.

FR11: Keep later package-aware language growth explicit by documenting what M18 proves, what remains deferred, and preventing marketplace, remote registry, publish, broad visibility, and broad language redesign scope from entering M18.

### NonFunctional Requirements

NFR1: Engineering IR remains canonical engineering truth after package-aware linking.

NFR2: Import resolution binds only through Athena's governed repository/package graph, not raw filesystem paths, JVM classpath coincidence, or frontend-local heuristics.

NFR3: The same repository, manifest, lock, and source state produce the same package-resolution, import-resolution, linking, diagnostic, and lowering outcomes.

NFR4: Package-aware diagnostics and navigation preserve usable file/span provenance across source-unit and package boundaries.

NFR5: ANTLR remains the compiler/LSP parser authority and Tree-sitter remains syntax UX only.

NFR6: M18 moves Athena from single-file semantics toward project-scale semantic composition without expanding into ecosystem, marketplace, registry, publish, deployment, or multi-root breadth.

### Additional Requirements

- The implementation must introduce a compiler-owned immutable project semantic graph snapshot with `graphId`, `rootPackageId`, ordered packages, source units, namespaces, declarations, bindings, and diagnostics.
- LSP navigation indexes, lowering inputs, compiler explanations, and tests must derive from the same project semantic graph snapshot rather than separate rescans.
- Canonical identity builders must exist for package keys, source unit ids, declaration ids, namespace ids, binding ids, and graph ids.
- Source unit ids must be package-key plus normalized source-root-relative path.
- Declaration ids must be source-unit id plus declaration kind plus qualified authored name within the M18 proof slice.
- Binding ids must be source-unit id plus reference span plus resolved declaration id.
- Graph ids must be deterministic hashes or stable renderings of the resolved package graph plus ordered source-unit content identities.
- Package-aware LSP behavior must hold or request a compiler-owned semantic graph snapshot version and internally preserve the snapshot `graphId`.
- M18 closeout must include at least one semantic namespace capability proof that carries a governed package capability marker, such as component-knowledge availability, through compiler explanation or diagnostics.
- `examples/m18/` must contain governed repository-backed fixtures and be covered by compiler, LSP, and Tree-sitter tests or equivalent mirrored test data.
- M18 must run in the existing local Gradle/JVM repository and workbench/LSP environment with no new remote service, cloud registry, publish transport, deployment topology, or multi-root workspace authority.
- Stack constraints remain Java toolchain 25, Gradle wrapper 9.6.1, Kotlin 2.4.0, ANTLR 4.13.2, LSP4J 0.23.1, Tree-sitter CLI >=0.26.1, and web-tree-sitter ^0.26.0.

### UX Design Requirements

UX-DR1: IDE-facing package-aware diagnostics, navigation, document symbols, and any workspace symbol affordance must follow Theia/VS Code-like interaction conventions: familiar Problems, go-to-definition, references, outline, and workspace navigation behavior rather than custom semantic-resolution UI.

UX-DR2: Canvas or graphical workbench interactions touched by package-aware navigation or reveal must follow EPLAN-style engineering canvas expectations: precise engineering-object selection, deterministic reveal, inspectable connections, and non-decorative operational density.

UX-DR3: Any enriched UX options added during M18 must remain downstream of compiler/LSP authority and subordinate to the Theia/VS Code IDE standard and EPLAN-style canvas standard.

UX-DR4: Tree-sitter package/import syntax support must improve syntax UX only: highlighting, folding, outline, and recovery must not imply semantic success when compiler package/import resolution fails.

### FR Coverage Map

FR1: Epic 1 - compiler package/import parsing and authored AST contracts.

FR2: Epic 1 - narrow syntax slice and deferred language breadth.

FR3: Epic 2 - compiler-owned project semantic graph from governed repository state.

FR4: Epic 2 - typed package/import/linking diagnostics from compiler authority.

FR5: Epic 3 - cross-source-unit and cross-package symbol linking.

FR6: Epic 3 - imported namespaces as governed engineering capability.

FR7: Epic 3 - deterministic canonical lowering after linking.

FR8: Epic 4 - LSP package-aware diagnostics, definition, references, symbols, and reveal behavior.

FR9: Epic 1 - Tree-sitter package/import syntax mirroring only.

FR10: Epic 4 - repository-backed executable proof corpus accumulated across all epics.

FR11: Epic 4 - explicit future-growth boundaries and non-goal enforcement.

## Epic List

### Epic 1: Package-Aware Authored Syntax

Authors can write the first governed `package` and `import` syntax slice, and Athena recognizes it consistently through compiler parsing, authored AST, and syntax-only Tree-sitter UX.

**FRs covered:** FR1, FR2, FR9

### Epic 2: Governed Project Semantic Workspace

Compiler users can open a governed repository and get one deterministic project semantic graph built from `athena.yaml`, `athena.lock`, resolved packages, source units, namespaces, declarations, bindings, and diagnostics.

**FRs covered:** FR3, FR4

### Epic 3: Cross-Package Linking And Capability Semantics

Package authors can link symbols across source-unit/package boundaries, preserve stable semantic namespace identity, prove imported engineering capability provenance, and lower linked results into canonical Engineering IR.

**FRs covered:** FR5, FR6, FR7

### Epic 4: Package-Aware IDE Experience And Closeout Evidence

IDE users can use package-aware diagnostics, definition, references, symbols, and reveal behavior through compiler/LSP authority using Theia/VS Code-like IDE conventions and EPLAN-style canvas expectations. Maintainers can close M18 with executable repository-backed proof evidence accumulated across all epics.

**FRs covered:** FR8, FR10, FR11

## Epic 1: Package-Aware Authored Syntax

Authors can write the first governed `package` and `import` syntax slice, and Athena recognizes it consistently through compiler parsing, authored AST, and syntax-only Tree-sitter UX.

### Story 1.1: Parse Package Declarations Into Authored AST

As a package author,
I want Athena source to declare its governed package namespace,
So that a source unit can participate in package-aware compilation.

**Acceptance Criteria:**

**Given** authored source with a supported `package` declaration
**When** the compiler parses it through ANTLR
**Then** the authored AST contains Athena-owned package declaration data
**And** source spans and syntax diagnostics remain inspectable.

### Story 1.2: Parse Import Declarations Into Authored AST

As a package author,
I want Athena source to import governed package or symbol meaning,
So that later semantic graph resolution can bind those imports.

**Acceptance Criteria:**

**Given** authored source with supported package and symbol-target imports
**When** the compiler parses it
**Then** the authored AST records import intent without exposing generated parser types
**And** unsupported alias, export, visibility, and unrelated declaration syntax is rejected or remains outside the supported grammar.

### Story 1.3: Preserve Narrow Syntax Scope

As a compiler engineer,
I want M18 syntax support limited to the approved package/import slice,
So that package-aware semantics do not become a broad language redesign.

**Acceptance Criteria:**

**Given** valid M18 package/import syntax
**When** parser tests run
**Then** the syntax is accepted.
**And** given unrelated new declaration families or broad visibility syntax, parser tests show they are not accepted as part of M18.

### Story 1.4: Mirror Package And Import Syntax In Tree-sitter

As an IDE user,
I want package/import syntax to highlight and structure correctly,
So that authoring feels native without giving Tree-sitter semantic authority.

**Acceptance Criteria:**

**Given** authored source with package/import declarations
**When** Tree-sitter parses it
**Then** highlighting, outline/folding structure, and syntax recovery recognize the supported syntax
**And** Tree-sitter does not resolve packages, link symbols, or emit semantic diagnostics.

### Story 1.5: Add Syntax Proof Fixtures

As a maintainer,
I want executable examples for valid and invalid M18 syntax,
So that syntax behavior is proven before semantic graph work builds on it.

**Acceptance Criteria:**

**Given** `examples/m18/` syntax fixtures
**When** compiler parser and Tree-sitter tests run
**Then** valid package/import examples pass
**And** invalid or out-of-scope syntax examples fail with expected syntax behavior.

## Epic 2: Governed Project Semantic Workspace

Compiler users can open a governed repository and get one deterministic project semantic graph built from `athena.yaml`, `athena.lock`, resolved packages, source units, namespaces, declarations, bindings, and diagnostics.

### Story 2.1: Add Canonical Identity Builders

As an IDE and compiler engineer,
I want canonical identity builders for package-aware semantic subjects,
So that diagnostics, navigation, linking, and lowering join the same objects.

**Acceptance Criteria:**

**Given** package graph and source-unit inputs
**When** identity builders run
**Then** package keys, source unit ids, declaration ids, namespace ids, binding ids, and graph ids are deterministic
**And** graph ids change only when resolved package graph or ordered source-unit content identities change.

### Story 2.2: Define Project Semantic Graph Snapshot Contracts

As a compiler engineer,
I want a compiler-owned project semantic graph snapshot contract,
So that import resolution, linking, LSP, lowering, and tests share one semantic workspace shape.

**Acceptance Criteria:**

**Given** the M18 architecture spine
**When** graph model contracts are implemented
**Then** the snapshot exposes `graphId`, `rootPackageId`, ordered packages, source units, namespaces, declarations, bindings, and diagnostics
**And** no LSP or lowering path defines a separate graph payload for the same meaning.

### Story 2.3: Build Semantic Graph From Governed Repository State

As a package author,
I want Athena to build semantic workspace meaning from governed repository state,
So that imports resolve through `athena.yaml`, `athena.lock`, and the resolved package graph.

**Acceptance Criteria:**

**Given** a governed repository with manifest and lock state
**When** the compiler builds the project semantic graph
**Then** admitted packages and source units come from governed repository resolution
**And** raw-path import attempts, JVM classpath-style resolution attempts, and frontend-style import hints do not resolve and instead produce typed diagnostics.

### Story 2.4: Resolve Imports Against The Semantic Graph

As a package author,
I want imports to resolve against the project semantic graph,
So that imported package/source-unit availability is deterministic and inspectable.

**Acceptance Criteria:**

**Given** valid imports admitted by the governed package graph
**When** semantic graph resolution runs
**Then** imported namespaces are available in the snapshot
**And** resolution output can explain package dependencies, source-unit availability, and namespace availability.

### Story 2.5: Emit Typed Package-Aware Diagnostics

As an IDE user,
I want import and graph failures reported as typed Athena diagnostics,
So that I can understand and fix package-aware authoring errors.

**Acceptance Criteria:**

**Given** missing package, missing source unit, invalid availability, ambiguous binding, raw-path/classpath/frontend-style resolution attempts, or graph-invalid/cycle cases
**When** project semantic graph construction runs
**Then** stable Athena diagnostic codes and source/span provenance are produced
**And** diagnostic codes remain stable across compiler, CLI/test, and LSP projection surfaces.
**And** graph-invalid and cycle diagnostics are represented before IDE closeout rather than only in final proof-corpus tests.

## Epic 3: Cross-Package Linking And Capability Semantics

Package authors can link symbols across source-unit/package boundaries, preserve stable semantic namespace identity, prove imported engineering capability provenance, and lower linked results into canonical Engineering IR.

### Story 3.1: Index Declarations Into Semantic Namespaces

As a compiler engineer,
I want authored declarations indexed into semantic namespaces,
So that imports can expose declaration availability across source units and packages.

**Acceptance Criteria:**

**Given** parsed source units in admitted packages
**When** the project semantic graph indexes declarations
**Then** namespaces contain deterministic declaration ids, package ids, source unit ids, qualified authored names, and source spans
**And** duplicate or ambiguous declaration availability is represented deterministically.

### Story 3.2: Link References Across Source Units

As a package author,
I want authored references to bind across source units in the same governed workspace,
So that multi-file authored source behaves as one semantic workspace.

**Acceptance Criteria:**

**Given** one source unit importing or referencing a declaration from another admitted source unit
**When** symbol linking runs
**Then** the reference resolves to the expected declaration id
**And** definition/reference provenance includes both source and target locations.

### Story 3.3: Link References Across Governed Packages

As a package author,
I want authored references to bind across governed package boundaries,
So that reusable package meaning can be consumed deterministically.

**Acceptance Criteria:**

**Given** a governed package dependency with an imported declaration
**When** symbol linking runs
**Then** the reference resolves through the admitted package namespace
**And** unresolved or unavailable cross-package references produce typed diagnostics rather than fallback lookup.

### Story 3.4: Preserve Governed Capability Provenance

As a platform architect,
I want imported namespaces to preserve governed capability provenance,
So that imports represent engineering capability and not just code inclusion.

**Acceptance Criteria:**

**Given** an imported package with an admitted capability marker such as component-knowledge availability
**When** the semantic namespace is built
**Then** the namespace records that capability provenance
**And** compiler explanation or diagnostics can show the capability marker without requiring all downstream consumers to execute it.

### Story 3.5: Lower Linked Meaning Into Engineering IR

As a compiler user,
I want linked package-aware authored meaning to lower through the canonical compiler path,
So that Engineering IR remains the canonical truth after linking.

**Acceptance Criteria:**

**Given** linked authored references across source-unit or package boundaries
**When** lowering runs
**Then** Engineering IR is produced through the existing canonical lowering path
**And** imports are not implemented as AST paste, source include, or hidden expansion.

### Story 3.6: Add Linking And Lowering Proof Fixtures

As a maintainer,
I want executable fixtures for successful and failing package-aware linking,
So that cross-package semantics are proven before IDE integration.

**Acceptance Criteria:**

**Given** `examples/m18/` linking fixtures
**When** compiler tests run
**Then** single-package, cross-source-unit, cross-package, unresolved symbol, and invalid availability cases produce expected bindings or diagnostics
**And** successful linked cases lower deterministically.

## Epic 4: Package-Aware IDE Experience And Closeout Evidence

IDE users can use package-aware diagnostics, definition, references, symbols, and reveal behavior through compiler/LSP authority using Theia/VS Code-like IDE conventions and EPLAN-style canvas expectations. Maintainers can close M18 with executable repository-backed proof evidence accumulated across all epics.

### Story 4.1: Project Package-Aware Diagnostics Through LSP

As an IDE user,
I want package/import/linking diagnostics shown through normal IDE diagnostic surfaces,
So that package-aware authoring errors are visible without custom frontend semantics.

**Acceptance Criteria:**

**Given** a project semantic graph snapshot with package-aware diagnostics
**When** LSP diagnostics are requested or published
**Then** diagnostics are projected from compiler-owned snapshot records
**And** the UX follows Theia/VS Code-like Problems/editor diagnostic conventions.

### Story 4.2: Add Package-Aware Definition And References

As an IDE user,
I want go-to-definition and references to cross source-unit and package boundaries,
So that I can navigate package-aware authored meaning from the IDE.

**Acceptance Criteria:**

**Given** a linked reference in a semantic graph snapshot
**When** definition or references are requested through LSP
**Then** results use snapshot declaration/binding/provenance records
**And** no frontend-local import or symbol resolution is used.

### Story 4.3: Add Snapshot-Derived Symbol Behavior

As an IDE user,
I want document symbols, and explicitly promoted workspace symbols if included, to reflect package-aware authored meaning,
So that source units and imported declarations are discoverable in familiar IDE flows.

**Acceptance Criteria:**

**Given** a project semantic graph snapshot
**When** document symbols are requested, or workspace symbols are explicitly promoted into the M18 implementation slice
**Then** symbol results derive from snapshot declarations and namespaces
**And** behavior follows Theia/VS Code-like outline/workspace navigation expectations.

### Story 4.4: Use Existing Canvas Reveal From Package-Aware Navigation

As an engineering workbench user,
I want package-aware navigation to use existing reveal-capable canvas surfaces when applicable,
So that source navigation and graphical inspection stay aligned.

**Acceptance Criteria:**

**Given** package-aware semantic provenance for an engineering subject
**When** IDE navigation reaches an existing workbench reveal surface
**Then** the canvas reveal is deterministic and uses canonical subject identity
**And** canvas behavior follows EPLAN-style expectations for precise engineering-object selection and inspectable connections.
**And** M18 does not introduce a new canvas system, renderer path, or graphical projection authority.

### Story 4.5: Complete Repository-Backed M18 Proof Corpus

As a maintainer,
I want one executable M18 proof corpus accumulated across all epics,
So that M18 closeout depends on evidence rather than prose.

**Acceptance Criteria:**

**Given** `examples/m18/`
**When** M18 verification runs
**Then** the corpus includes single-package success, cross-package success, invalid import, unresolved symbol, graph-invalid or cycle behavior, and vendor/governed package availability accumulated from earlier epic proof work
**And** compiler, LSP, and Tree-sitter tests cover the relevant fixture behavior.
**And** vendor/governed package availability means a locally admitted package through the governed repository graph, not a remote registry or marketplace dependency.

### Story 4.6: Document M18 Scope Boundaries And Deferred Growth

As a platform architect,
I want M18 artifacts to state what is proven and what is deferred,
So that later package-aware language growth does not accidentally expand M18 scope.

**Acceptance Criteria:**

**Given** M18 closeout documentation or milestone notes
**When** reviewers inspect the milestone
**Then** remote registry, marketplace, publish flows, full export/visibility, broad language redesign, frontend semantic resolution, and multi-root behavior are explicitly deferred
**And** future package-aware growth points back to the project semantic graph foundation.
**And** validation fails if milestone proof fixtures or documentation imply registry, marketplace, publish, multi-root, or frontend-owned semantic-resolution behavior entered M18 scope.
