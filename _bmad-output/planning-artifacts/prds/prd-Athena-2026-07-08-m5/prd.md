---
title: Athena M5
status: draft
created: 2026-07-08
updated: 2026-07-08
---

# PRD: Athena M5

*Codename: Athena Repository And Package Graph Proof.*

## 0. Document Purpose

This PRD defines the M5 product requirements for Athena after the completed M4 milestone.

M5 exists to close the largest semantic gap intentionally left open by M4:

> Athena can already launch, open a repository, edit authored source through Athena LSP, and present a professional workbench. M5 must now make that repository mean something governed by freezing the package contract, dependency contract, and semantic package graph under the same canonical authority model.

This PRD is capability-first. It builds on the completed M4 PRD and architecture, the current workspace summary and M4 usage guide, the new roadmap note under `docs/roadmap/`, and the M5/M6 draft split captured under `draft/m5/001-draft.md` and `draft/m6/001-draft.md`. Implementation-shaped detail that is useful but too low-level for the PRD body is captured in [`addendum.md`](addendum.md).

## 1. Vision

M0 proved the authored DSL to canonical semantic model thesis. M1 proved runtime-owned workspace state and command history. M2 proved explicit layout and geometry as downstream projection layers. M3 proved hosted extensibility through stable plugin contracts. M4 proved the first serious Athena IDE shell on Theia with repository open/create, Athena LSP, and a professional workbench.

M5 must now prove the next layer that makes Athena operable as a real engineering platform instead of only an IDE proof:

- a governed repository/package contract
- deterministic dependency resolution
- inspectable lock state
- a semantic package graph owned by compiler and runtime rather than by ad hoc filesystem conventions

In other words, M4 proved that Athena can host engineering work. M5 must prove what that work is packaged as.

## 1.1 Why Now

The next technical risk is no longer product embodiment. M4 already proved that Athena can launch as a custom Theia-based IDE with serious semantic boundaries.

The next risk is semantic repository meaning.

Today Athena still uses a deliberately light repository shape:

- one repository root
- one preferred authored `.athena` source
- no final `athena.yaml`
- no final `athena.lock`
- no governed package identity or dependency graph

That is good M4 discipline, but it is not enough for the next platform layer. Until Athena freezes repository/package meaning, later milestones cannot safely build:

- semantic SCM over Git
- publish and review flows
- reusable engineering package ecosystems
- richer cross-package navigation and diagnostics

That is why M5 comes before M6 semantic SCM and before M7 graphical projection work.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need Athena repositories to have a stable governed contract instead of a light proof-only directory shape.
- Package authors need to declare package identity, dependencies, and import relationships in a way the compiler and runtime can own semantically.
- Language-tooling engineers need package-aware diagnostics and navigation to stand on a deterministic repository graph.
- Founders need proof that Athena can evolve from a single-repository IDE proof into a reusable engineering package platform.
- Future SCM and ecosystem milestones need package meaning frozen before semantic review or publish flows begin.

### 2.2 Non-Users (M5)

- Teams expecting M5 to deliver semantic SCM, intent commit flows, review flows, or publish flows above Git
- Teams expecting M5 to deliver graphical projection, diagram canvases, or GLSP-class graph infrastructure
- Teams expecting M5 to become a broad IDE-polish milestone
- Teams expecting M5 to replace the current semantic kernel/runtime authority with repository-manager logic in the frontend
- Teams expecting M5 to solve browser-first deployment or collaboration

### 2.3 Key User Journeys

- **UJ-1. Aaron opens an Athena repository and Athena understands its package structure instead of only finding one source file.**
  - **Persona + context:** Aaron is validating whether Athena repositories have become governed engineering package workspaces.
  - **Entry state:** Athena M4 already launches and can open a repository root.
  - **Path:** Aaron opens a repository containing governed Athena package metadata and dependencies. Athena resolves the repository contract, loads the package graph, and surfaces any package or dependency problems through the same JVM semantic path.
  - **Climax:** Aaron can explain the repository as a semantic package workspace rather than a loose directory with one authored file.
  - **Resolution:** Athena becomes credible as an engineering package platform, not only an editor proof.

- **UJ-2. Maya authors package dependencies and sees package-aware feedback immediately.**
  - **Persona + context:** Maya is using Athena as a serious programmable environment across more than one package boundary.
  - **Entry state:** An Athena repository already contains governed package metadata.
  - **Path:** Maya edits package declarations or imports, then receives package-aware diagnostics, lock or resolution feedback, and repository-state updates through Athena LSP and the workbench.
  - **Climax:** Maya can see that package and dependency meaning is semantic and governed, not implicit filesystem convention.
  - **Resolution:** Athena becomes ready for later semantic SCM and ecosystem work.

- **UJ-3. Priya verifies that repository and package meaning still stays downstream of canonical semantic authority.**
  - **Persona + context:** Priya is checking that repository/package work does not quietly move authority into UI or ad hoc tooling layers.
  - **Entry state:** Athena M5 is running in the Theia shell.
  - **Path:** Priya opens a repository, inspects package manifests, dependency diagnostics, and lock state behavior, and traces them through runtime/compiler ownership.
  - **Climax:** Priya confirms that repository/package semantics live in the JVM stack and that the workbench is still a downstream consumer.
  - **Resolution:** Priya approves M5 as the correct base for M6 semantic SCM.

## 3. Glossary

- **Engineering Repository** - The root directory Athena opens as the primary unit of local engineering work. In M5 it becomes a governed repository/package workspace instead of only a light M4 proof root.
- **Athena Package** - A governed semantic package unit declared inside an Engineering Repository with identity, dependencies, and authored source governed by Athena contracts.
- **`athena.yaml`** - The repository/package manifest contract introduced in M5.
- **`athena.lock`** - The deterministic resolved dependency and package graph lock contract introduced in M5.
- **Semantic Package Graph** - The canonical package/dependency graph owned by compiler and runtime meaning, not by editor-local or frontend-local state.
- **Repository Session** - The active runtime-backed session opened by Athena for one Engineering Repository in one product window.
- **Dependency Resolver** - The compiler/runtime-owned capability that resolves package references, dependency declarations, and lock state into governed semantic package meaning.
- **Package-Aware Diagnostics** - Diagnostics derived from manifest, dependency, import, or package-graph problems through Athena-owned semantic analysis.

## 4. Features

### 4.1 Repository And Package Contract

**Description:** Athena must freeze the first real repository/package contract so repositories stop being only light M4 proof roots. Realizes UJ-1, UJ-3.

#### FR-1: Define A Stable Athena Repository Manifest

Athena can define a stable M5 repository/package manifest contract. Realizes UJ-1.

**Consequences (testable):**
- Athena introduces a real `athena.yaml` contract with stable meaning for repository/package identity and dependency declarations.
- The manifest is explicit enough to be validated, reasoned over, and evolved by later milestones.
- The M4 temporary bootstrap shape is superseded by a governed contract rather than stretched indefinitely.

#### FR-2: Define A Deterministic Lock Contract

Athena can define a stable M5 lock contract. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena introduces a real `athena.lock` contract that captures deterministic repository/package resolution state.
- Lock state is inspectable and reproducible.
- The lock contract is owned by Athena semantic rules rather than by generic package-manager assumptions.

#### FR-3: Define Package Identity And Layout Rules

Athena can define stable package identity and local layout rules. Realizes UJ-1.

**Consequences (testable):**
- Athena can distinguish repository root meaning from package meaning.
- Package identity and authored source placement are governed instead of being left to implicit convention.
- The compiler/runtime can validate repository/package structure explicitly.

### 4.2 Semantic Package Resolution

**Description:** Athena must resolve package meaning semantically rather than treating imports and dependencies as passive files. Realizes UJ-1, UJ-2, UJ-3.

#### FR-4: Resolve Local And Declared Dependencies Deterministically

Athena can resolve package dependencies deterministically. Realizes UJ-1, UJ-2.

**Consequences (testable):**
- Athena can resolve the declared repository/package graph through governed rules.
- Resolution order and resolved state are deterministic.
- The same repository state produces the same semantic package graph and lock result.

#### FR-5: Surface Package And Dependency Diagnostics

Athena can surface package-aware diagnostics through the same semantic boundary used for language tooling. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Invalid manifest content, missing dependencies, bad package references, or graph inconsistencies appear as Athena-owned diagnostics.
- Diagnostics remain traceable to compiler/runtime-owned package analysis.
- The Theia client does not invent an independent repository/package diagnostic engine.

#### FR-6: Preserve Canonical Semantic Authority During Resolution

Athena can keep package resolution downstream of canonical semantic authority. Realizes UJ-3.

**Consequences (testable):**
- Repository/package meaning is owned by compiler and runtime layers.
- Frontend and backend orchestration may request or display repository/package state, but they do not become package-graph authorities.
- Later SCM and publish logic can build on the same governed package meaning instead of redefining it.

### 4.3 Runtime-Backed Repository Graph Session

**Description:** The M4 repository session must evolve from a light proof root into a real governed repository/package graph session. Realizes UJ-1, UJ-3.

#### FR-7: Upgrade The Active Repository Session To A Package Graph Session

Athena can operate one active runtime-backed repository session over a governed package graph. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Opening a repository activates a runtime-owned repository graph session rather than only a one-file proof path.
- The session can expose repository/package state to Athena workbench features without splitting authority.
- The single-window / single-session M4 rule may remain, while the semantic richness of the session increases.

#### FR-8: Keep Repository Open And Create Flows Compatible With The New Contract

Athena can adapt existing repository open/create flows to the governed M5 contract. Realizes UJ-1.

**Consequences (testable):**
- The M4 open and create flows continue to work, but now target the governed repository/package contract.
- Repository creation no longer emits only the light M4 seed once the M5 contract is in force.
- Contract errors are visible early instead of becoming later runtime surprises.

### 4.4 IDE Support For Package-Aware Operation

**Description:** M5 can add narrow IDE usability work only when it directly supports governed repository/package operation. Realizes UJ-2.

#### FR-9: Surface Package State In The Existing Athena IDE Path

Athena can surface repository/package state in the current Theia workbench without rewriting the M4 shell. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- The existing Athena IDE shell can expose package-aware repository state, diagnostics, and resolution feedback.
- Package-related views and commands attach through the same additive product boundaries proven in M4.
- The workbench remains downstream of Athena LSP and runtime authority.

#### FR-10: Add Narrow Language-Surface Hardening That Directly Supports M5

Athena can add limited language-surface hardening where it directly improves package-aware operation. Realizes UJ-2.

**Consequences (testable):**
- Athena may add minimal syntax highlighting, semantic token groundwork, or other narrow editor improvements that make package-aware work operable.
- Such work remains supporting scope rather than replacing the package-graph milestone core.
- The language surface remains governed by the same semantic authority boundary proven in M4.

### 4.5 Growth Safety For M6 And M7

**Description:** M5 must prepare M6 semantic SCM and M7 graphical projection without widening into either one. Realizes UJ-1, UJ-3.

#### FR-11: Prepare M6 Semantic SCM Without Implementing It

Athena can prepare stable repository/package meaning for M6 semantic SCM. Realizes UJ-3.

**Consequences (testable):**
- Repository/package identity and dependency meaning are stable enough for later semantic diff and review flows.
- M6 can build on governed package semantics instead of inventing them.
- M5 does not attempt to implement review, commit, or publish flows itself.

#### FR-12: Preserve A Later Graphical Projection Path Without Expanding Into It

Athena can preserve a later graphical projection path while keeping M5 narrow. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- The M5 repository/package work does not block later graphical projection or GLSP-class research.
- Repository/package semantics remain stable inputs for later projection work.
- M5 does not turn into a visual-workbench milestone.

## 5. Non-Goals (Explicit)

- M5 does not deliver semantic SCM, intent commit, review, or publish workflows above Git.
- M5 does not deliver the graphical projection milestone, diagram canvases, or graph editing.
- M5 does not become a broad IDE-polish milestone.
- M5 does not replace the M4 Theia shell architecture.
- M5 does not move repository/package authority into frontend-only state.
- M5 does not make browser-first or collaboration-first delivery the milestone center.

## 6. MVP Scope

### 6.1 In Scope

- stable `athena.yaml`
- stable `athena.lock`
- package identity and repository/package layout rules
- deterministic package/dependency resolution
- semantic package graph
- package-aware diagnostics through Athena semantic boundaries
- runtime-backed repository graph session
- adapting repository open/create flows to the governed contract
- narrow IDE operability improvements directly supporting package-aware work

### 6.2 Out Of Scope For MVP

- semantic SCM over Git, which is deferred to M6
- semantic review and publish flows, which are deferred to M6
- graphical projection, diagram editing, or GLSP-class product delivery, which are deferred to M7
- broad UX/system polish or final emotion-system work
- browser-first or collaboration-first productization

## 7. Success Metrics

**Primary**

- **SM-1:** Athena repositories can declare governed package metadata and dependencies through a stable manifest contract.
- **SM-2:** Athena can resolve repository/package graphs deterministically and materialize inspectable lock state.
- **SM-3:** Package and dependency errors surface through the same JVM semantic path used by Athena language tooling.
- **SM-4:** The existing Athena IDE shell can operate on the governed repository/package graph without architectural rewrite.

**Secondary**

- **SM-5:** Narrow IDE hardening such as basic highlighting or package-aware feedback improves operability without displacing the package-graph milestone core.
- **SM-6:** M5 creates an obvious foundation for M6 semantic SCM and M7 graphical projection work.

**Counter-metrics**

- **SM-C1:** Do not optimize for broad IDE polish over freezing the repository/package contract.
- **SM-C2:** Do not optimize for SCM-like flows before package meaning is stable.
- **SM-C3:** Do not optimize for graphical experimentation over package-graph correctness.

## 8. Cross-Cutting NFRs

- **NFR-1 Semantic Authority Preservation:** Repository/package meaning must remain owned by compiler and runtime layers.
- **NFR-2 Determinism:** Resolution and lock results must be reproducible from the same repository state.
- **NFR-3 Inspectability:** Manifest, lock, package graph, and package diagnostics must remain inspectable for development and architecture debugging.
- **NFR-4 IDE Continuity:** M5 must extend the current M4 IDE shell rather than forcing a shell rewrite.
- **NFR-5 Growth Safety:** M5 must prepare M6 semantic SCM and M7 graphical projection without widening into either one.

## 9. Constraints And Guardrails

### 9.1 Product Guardrails

- Theia remains the active IDE product shell inherited from M4.
- M5 must stay package-graph-first rather than IDE-polish-first.
- Supporting IDE improvements are allowed only when they directly improve governed repository/package operation.

### 9.2 Architectural Guardrails

- Compiler and runtime remain the semantic authorities.
- `ide/lsp` remains the sole semantic entry point for the IDE path.
- Package-graph work must not bypass the existing M0 to M4 semantic stack.
- Workbench additions must stay additive through Athena-owned product boundaries.

### 9.3 Roadmap Guardrails

- M5 owns repository manifest, lockfile, dependency resolver, and semantic package graph concerns.
- M6 owns semantic SCM concerns over Git and review/publish-oriented history flows.
- M7 owns real graphical projection and visual-workbench concerns.

## 10. Platform And Delivery

- **Primary platform:** desktop-first Athena Theia product inherited from M4
- **Primary delivery target:** local developer-run product shell plus deterministic JVM and workspace verification
- **Primary repository authority:** runtime-backed repository graph session
- **Primary language-service foundation:** Athena LSP
- **Current semantic backend foundation:** the existing Athena kernel/compiler/runtime stack from M0 to M4

## 11. Open Questions

1. Should `athena.yaml` exist only at repository root in the first M5 cut, or should package-local manifest boundaries exist immediately?
2. What is the minimum stable package identity model for M5: name only, name plus version, or a stronger namespace contract?
3. How much resolution state belongs in `athena.lock` in the first M5 cut: only local graph locking, or also future-ready external package coordinates? `[ASSUMPTION: M5 should start with the narrowest useful deterministic lock scope.]`
4. Which narrow IDE improvements are necessary to keep M5 operable without turning it into an editor-polish milestone?

## 12. Assumptions Index

- M5 should freeze the package contract before M6 semantic SCM begins.
- M5 should keep the existing single-window / single-session rule unless package-graph work itself forces a broader session model.
- M5 may include narrow IDE operability improvements, but only as supporting backlog under the package-graph milestone rather than as the milestone core.
