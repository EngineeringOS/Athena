---
title: Athena M1
status: final
created: 2026-07-03
updated: 2026-07-04
---

# PRD: Athena M1

*Codename: Athena Runtime.*

## 0. Document Purpose

This PRD defines the M1 product requirements for Athena after the M0 compiler proof has been completed. It is written for founders, product owners, developers, architecture owners, and downstream BMad workflows. The document is capability-first: Glossary terms are authoritative, Features are grouped with globally numbered Functional Requirements, cross-cutting Non-Functional Requirements are explicit, and inferred points are tagged inline as `[ASSUMPTION]`. This M1 PRD builds on the final M0 PRD in `_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/` and on the M1 transition draft in `draft/m1/0001.md`. Architecture-heavy migration recommendations are preserved in `addendum.md` rather than overloaded into the main requirements narrative.

## 1. Vision

M0 proved that Athena can act as a semantic engineering compiler: authored DSL can be parsed into AST, lowered into `Engineering IR`, validated semantically, and rendered deterministically. M1 must prove a larger claim: Athena is not only a compiler pipeline but the semantic runtime upon which engineering applications can be built.

In M1, Athena becomes the long-lived runtime that hosts `Workspace` state, `Project` lifecycle, semantic services, commands, graph operations, validation, rendering, and plugins while preserving `Engineering IR` as the canonical semantic model. The compiler remains real, but it becomes one capability inside a larger runtime-centered platform.

If M1 succeeds, Athena stops being described as only a text-first engineering compiler and starts being described as the semantic runtime of EngineeringOS: a platform where DSL, GUI, AI, and future importers all converge on the same canonical model and where viewers, renderers, history, and downstream applications all consume that same semantic source of truth.

## 1.1 Why Now

M0 already answered the narrow proof question: can engineering be treated as a compiler problem? The answer is yes. The next risk is different. If Athena leaves the center of gravity inside `:compiler`, every later GUI, AI, viewer, editor, and integration surface will accumulate around a subsystem that was designed for input-to-output transformation rather than long-lived runtime coordination.

M1 exists to move that center of gravity upward before UI and integration work starts in earnest. The platform needs a runtime boundary before it grows more frontends, more outputs, more plugins, and more interactive behavior. That sequencing is load-bearing.

## 2. Target User

### 2.1 Jobs To Be Done

- Platform engineers need a runtime substrate that can host the compiler, plugins, graph operations, command history, and rendering without turning the compiler into a monolith.
- Application builders need one semantic runtime they can target from DSL, GUI, AI, and future API surfaces instead of building separate pipelines for each input mode.
- Domain and plugin authors need a first-class way to contribute semantics, commands, views, and knowledge without owning canonical semantics.
- Founders need Athena to demonstrate it can become the platform layer beneath future engineering applications rather than staying a narrow compiler proof.
- Reviewers need semantic changes to remain inspectable through stable identity, graph relationships, command history, and provenance-rich diagnostics.

### 2.2 Non-Users (M1)

- Teams expecting M1 to deliver a full ECAD editor
- Teams expecting Athena to replace incumbent engineering tools end to end in this phase
- Organizations primarily seeking cloud collaboration, enterprise administration, or workflow dashboards before runtime foundations exist

### 2.3 Key User Journeys

`[ASSUMPTION: These journeys are inferred from the M1 transition draft and should be confirmed before UX specification begins.]`

- **UJ-1. Aaron opens a Project through Athena Runtime and compiles it without the compiler owning lifecycle.**
  - **Persona + context:** Aaron is shaping Athena from a proven compiler into a durable platform.
  - **Entry state:** A local `Workspace` contains at least one `Project`.
  - **Path:** Aaron opens the `Workspace`; `Athena Runtime` loads services and plugins; Aaron runs compilation through the runtime; the runtime coordinates compiler and renderer services over the active `Project`.
  - **Climax:** The `Project` compiles and renders while lifecycle, plugin hosting, and execution context remain visibly runtime-owned rather than compiler-owned.
  - **Resolution:** Aaron can explain the system boundary as `CLI -> Runtime -> Compiler` instead of `CLI -> Compiler -> everything`.

- **UJ-2. Maya edits a semantic object through a command-driven surface and undoes the change.**
  - **Persona + context:** Maya is building or reviewing an interactive engineering application on Athena.
  - **Entry state:** A `Project` is open in a viewer or editor-oriented surface.
  - **Path:** Maya selects a semantic object; an operation is issued as a `Command`; the runtime applies the mutation to the graph-backed model; affected validation and rendering update incrementally; Maya inspects the result and triggers undo.
  - **Climax:** The system restores the previous semantic state through command history instead of ad hoc UI rollback.
  - **Resolution:** Maya can trust that GUI-facing behavior still flows through the same canonical semantic runtime.

- **UJ-3. Priya builds a new surface that consumes Engineering IR without reimplementing semantics.**
  - **Persona + context:** Priya is an application or plugin author extending Athena.
  - **Entry state:** Core runtime services are available and at least one `Project` can be loaded.
  - **Path:** Priya attaches a viewer, plugin, or service to published runtime contracts; the surface consumes `Engineering IR`, graph queries, diagnostics, and render outputs; Priya does not fork semantic authority into the surface.
  - **Climax:** A new surface works over the same semantic runtime whether the source change began as DSL, GUI, or AI input.
  - **Resolution:** Athena proves that future applications can be built on top of the runtime rather than beside it.

## 3. Glossary

- **Athena Runtime** - The long-lived semantic runtime that owns `Workspace` lifecycle, execution context, service hosting, command execution, and coordination around canonical engineering semantics.
- **Workspace** - A runtime-owned container for one or more `Project` instances, active services, and execution context.
- **Project** - The runtime-owned semantic project boundary that can be opened, mutated, validated, rendered, and inspected.
- **Engineering Graph** - The graph-shaped representation of semantic engineering objects and relationships used for identity, traversal, lookup, and dependency-aware change handling.
- **Command Runtime** - The runtime layer that applies semantic mutations through explicit commands with history, undo, redo, replay, and serialization behavior.
- **Compose Runtime** - The editing and viewing runtime for viewport, selection, input handling, hit testing, camera, layers, and rendering coordination without domain-specific semantic ownership.
- **Incremental Semantic Pipeline** - The runtime-coordinated process that revalidates and rerenders only affected semantic scope after a change.
- **Service Registry** - The runtime-owned registration surface for compiler, renderer, plugin, knowledge, and related services.
- **Execution Context** - The runtime-owned context that binds the active `Workspace`, `Project`, plugins, services, and operation state for one run or interactive session.
- **Engineering IR** - The canonical semantic engineering model. It remains the source of semantic truth in M1.
- **Plugin** - An extension that adds domain semantics, views, commands, importers, exporters, AI skills, or related capabilities without owning canonical semantics.
- **Renderer Backend** - A downstream component that consumes canonical semantics or derived render models to produce a target output such as SVG or Compose-based viewing.

## 4. Features

### 4.1 Runtime Host And Workspace Lifecycle
**Description:** M1 must establish `Athena Runtime` as the new operational center of gravity. The runtime owns `Workspace` and `Project` lifecycle, binds the `Execution Context`, and coordinates services above the compiler rather than letting the compiler bootstrap everything directly. Realizes UJ-1.

**Functional Requirements:**

#### FR-1: Open And Manage Workspaces

An operator or surface can open, close, and manage a `Workspace` through `Athena Runtime`. Realizes UJ-1.

**Consequences (testable):**
- A `Workspace` can be created or opened without invoking compiler passes immediately.
- Runtime state distinguishes `Workspace` lifecycle from compiler execution lifecycle.
- Runtime-managed services can resolve the active `Workspace` and current `Project`.

#### FR-2: Bind Projects To A Runtime Execution Context

`Athena Runtime` can load a `Project` into an `Execution Context` that compiler, renderer, graph, and plugin services can share. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Compiler and renderer operations can execute against the active `Project` through runtime-owned context.
- A surface can query the active `Project` without directly constructing compiler internals.
- The context can carry runtime-owned service and plugin availability information.

#### FR-3: Host A Service Registry Above The Compiler

`Athena Runtime` exposes a `Service Registry` for compiler, renderer, plugin, and related platform services. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- Runtime services are discoverable and replaceable behind stable contracts.
- Compiler functionality is registered as a service rather than acting as the root owner of the system.
- Additional runtime capabilities can be introduced without collapsing back into compiler-owned orchestration.

### 4.2 Engineering Graph
**Description:** M1 must introduce the `Engineering Graph` as the runtime's graph-shaped semantic operating model for identity, traversal, queries, and dependency-aware change handling. The graph is not a separate semantic authority from `Engineering IR`; it is the runtime-oriented graph surface over canonical semantics. Realizes UJ-2, UJ-3.

**Functional Requirements:**

#### FR-4: Represent Semantic Objects And Relationships As A Graph

The runtime can expose semantic objects and relationships through an `Engineering Graph` over the active `Project`. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- Core semantic objects such as systems, components, ports, connections, and properties can be addressed as graph elements.
- Relationships remain queryable without requiring a full recompilation mental model from the caller.
- The graph preserves stable identity across inspection and mutation flows.

#### FR-5: Support Query, Traversal, And Lookup

Consumers can perform query, traversal, reference lookup, and dependency inspection over the `Engineering Graph`. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- A surface or service can locate semantic neighbors and referenced objects through published graph APIs.
- Queries do not require direct access to parser-specific or renderer-specific structures.
- Dependency-aware operations can identify affected semantic scope after a change.

#### FR-6: Preserve Graph And IR Consistency

The runtime keeps `Engineering Graph` behavior consistent with canonical `Engineering IR` rather than letting graph state drift into a second authority. Realizes UJ-2.

**Consequences (testable):**
- Graph mutations or updates remain reconcilable to canonical semantic state.
- The same semantic identity is observable through both graph-oriented and compiler-oriented views.
- Graph services cannot silently invent semantic meaning outside canonical runtime rules.

### 4.3 Command Runtime And History
**Description:** Interactive mutation in M1 must happen through explicit commands rather than direct ad hoc edits to runtime state. The `Command Runtime` becomes the universal mutation path across CLI, GUI, AI, and future surfaces. Realizes UJ-2.

**Functional Requirements:**

#### FR-7: Execute Semantic Mutations As Commands

A caller can issue semantic mutations through explicit commands handled by the `Command Runtime`. Realizes UJ-2.

**Consequences (testable):**
- Create, rename, connect, disconnect, and comparable semantic mutations are represented as commands.
- Mutation requests flow through command execution rather than direct UI or caller-side object mutation.
- Command execution can be inspected and recorded by the runtime.

#### FR-8: Provide Undo, Redo, And Replay

The `Command Runtime` supports undo, redo, and replay over executed commands. Realizes UJ-2.

**Consequences (testable):**
- A successful command can be reversed through runtime-owned history.
- Redo can reapply a previously undone command without reauthoring it manually.
- Replay can reconstruct a sequence of semantic state transitions from recorded command history.

#### FR-9: Serialize Commands For History And Interoperability

Commands can be serialized in a stable enough form for history, replay, and future interoperability work. Realizes UJ-2.

**Consequences (testable):**
- History is not limited to in-memory callbacks.
- Serialized commands preserve enough information to explain what changed.
- Serialization does not require callers to introspect private UI state.

#### FR-20: Expose Semantic Diff And History Inspection

A reviewer or surface can inspect semantic diffs and command-history consequences for runtime-managed project changes. Realizes UJ-2.

**Consequences (testable):**
- The first GUI mutation path produces an inspectable before/after semantic diff for the affected scope.
- Diff inspection can be tied to command history and stable semantic identity rather than only to UI state.
- Reviewers can inspect what changed before or after undo and replay operations.

### 4.4 Multi-Frontend Semantic Input
**Description:** M1 must prove that Athena can accept semantic input from more than one frontend class while preserving one canonical runtime path. DSL remains important, but GUI- and AI-originated changes must converge on the same runtime and semantic model. Realizes UJ-1, UJ-2, UJ-3.

**Functional Requirements:**

#### FR-10: Keep DSL As A Runtime Frontend

The existing DSL path remains a supported frontend into the runtime-owned semantic pipeline. Realizes UJ-1.

**Consequences (testable):**
- DSL compilation can be triggered through `Athena Runtime`.
- The DSL path remains compatible with M0 compiler behavior where applicable.
- M1 runtime introduction does not require rewriting the proven M0 compiler path first.

#### FR-11: Accept GUI-Originated Semantic Changes Without A Parser Round Trip

A GUI-facing surface can create or modify semantic state through runtime and command contracts without requiring authored text parsing for every change. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- The first GUI proof is limited to one explicit command-backed semantic mutation path over the active `Project`.
- A GUI action can produce that semantic mutation through commands and runtime services.
- The resulting state remains available to validation and rendering without forcing a DSL reparse.
- GUI-facing operations still preserve canonical semantic ownership in runtime and IR layers and do not imply a full editor surface.

#### FR-12: Route AI-Assisted Changes Through The Same Semantic Runtime

AI-assisted input can propose command-shaped changes only through the same runtime, command, and validation boundaries used by other frontends. Realizes UJ-3. `[ASSUMPTION: AI interaction in M1 is a proving surface, not a broad autonomous workflow product.]`

**Consequences (testable):**
- AI-originated proposals can be validated and rendered through the same semantic path as DSL or GUI changes once accepted.
- AI proposals require explicit acceptance before they mutate canonical project state in M1.
- AI does not bypass command history, graph consistency, or validation rules.
- Reviewers can inspect accepted AI-originated changes through normal runtime, history, and diagnostic surfaces.

### 4.5 Compose Runtime And Viewer
**Description:** M1 needs a real runtime-facing visual proof that consumes canonical semantics interactively. The first Compose-based surface is a runtime and viewer, not a full ECAD editor. Realizes UJ-2, UJ-3.

**Functional Requirements:**

#### FR-13: Provide A Compose-Based Semantic Viewer

Athena provides a Compose-based viewer that can display the active `Project` through runtime-coordinated semantic and render services. Realizes UJ-2, UJ-3.

**Consequences (testable):**
- A viewer can render semantic project state without becoming the source of truth.
- The viewer consumes runtime and renderer services rather than reimplementing semantic logic.
- The Compose surface can be used as an M1 demonstration target for runtime-centered architecture.

#### FR-14: Support Viewport, Selection, Pan, And Zoom

The `Compose Runtime` supports baseline viewing interaction including viewport control, selection, pan, and zoom. Realizes UJ-2.

**Consequences (testable):**
- The viewer can maintain and update view state independently of semantic truth.
- Selection and navigation behavior do not mutate semantic state unless routed through explicit commands.
- Input handling remains runtime/editor infrastructure rather than domain-specific engineering logic.

#### FR-15: Keep Compose Runtime Domain-Neutral

The `Compose Runtime` remains domain-neutral infrastructure for interactive surfaces rather than becoming the owner of electrical semantics. Realizes UJ-3.

**Consequences (testable):**
- Compose runtime APIs describe view, input, and interaction infrastructure rather than electrical rules.
- Domain logic continues to live in canonical semantics and plugins.
- New domains can reuse the same viewer/runtime infrastructure.

### 4.6 Incremental Semantic Pipeline
**Description:** M1 must make semantic compilation interactive enough for runtime use by updating only affected scope after a change. The platform should move from whole-file compilation as the only path to change-aware validation and rendering. Realizes UJ-2.

**Functional Requirements:**

#### FR-16: Recompute Only Affected Semantic Scope After A Change

After a semantic mutation, the runtime can identify and recompute the affected semantic scope instead of rerunning every stage over the whole project by default. Realizes UJ-2.

**Consequences (testable):**
- The runtime can determine an affected semantic subset from the change.
- Validation and rendering can run on affected scope when the change allows it.
- Incremental behavior remains consistent with canonical semantic rules.

#### FR-17: Trigger Incremental Validation And Rendering

The runtime can trigger incremental validation and downstream rendering after a change. Realizes UJ-2.

**Consequences (testable):**
- A semantic change can update diagnostics without requiring a full manual recompilation ritual from the user.
- A semantic change can update viewer-facing output through runtime-coordinated rendering.
- Incremental updates remain inspectable rather than hidden behind UI-only caches.

### 4.7 Plugin Runtime v2
**Description:** M1 must turn plugins from a proven M0 mechanism into a first-class runtime growth model. Plugins can expand commands, views, importers, exporters, AI skills, and domain contributions without gaining semantic sovereignty. Realizes UJ-1, UJ-3.

**Functional Requirements:**

#### FR-18: Host First-Class Runtime Plugins

`Athena Runtime` can host first-class plugins for semantic rules, commands, views, importers, exporters, knowledge-related services, and comparable runtime capabilities. Realizes UJ-1, UJ-3.

**Consequences (testable):**
- M1 must prove runtime-hosted plugins for at least domain semantics, commands, and views.
- Importers, exporters, AI skills, and knowledge-related services must fit the same hosting model when introduced, but they are not required as the first implementation slice.
- Plugins can attach to runtime-owned contracts beyond the M0 compiler-only scope.
- Runtime and plugin hosting remain explicit and inspectable.
- New capabilities can be added through plugins without rewriting the runtime core.

#### FR-19: Keep Plugins Non-Sovereign

Plugins cannot replace canonical semantic ownership, project lifecycle ownership, or runtime orchestration ownership.

**Consequences (testable):**
- Plugins can extend behavior but not redefine `Engineering IR` as a plugin-private model.
- Runtime contracts can reject incompatible or overreaching plugins.
- Platform owners can explain which invariants stay core-owned even when plugins are active.

## 5. Non-Goals (Explicit)

- M1 is not a full ECAD editor.
- M1 is not delivering wire routing, auto layout, symbol-library completeness, PLC logic tooling, BOM generation, report generation, DXF, or DWG output.
- M1 is not cloud collaboration, multi-user concurrency, or enterprise operations first.
- M1 is not a rewrite of successful M0 layers for the sake of repository aesthetics.
- M1 is not moving canonical semantics into Compose UI state, command history state, or plugin-private models.

## 6. MVP Scope

### 6.1 In Scope

M1 delivery is sequenced. The runtime proof must land as a narrow, believable slice first; broader GUI, AI, and plugin expansion only count if they reuse that same runtime-centered path rather than introducing parallel one-off implementations.

- `Athena Runtime` as a new runtime-owned host above the compiler
- Runtime-owned `Workspace`, `Project`, `Execution Context`, and `Service Registry`
- First `Engineering Graph` APIs for semantic objects and relationships
- `Command Runtime` with execute, undo, redo, and replay behavior
- Semantic diff and history inspection for the first mutation path
- Compose-based semantic viewer with selection, pan, and zoom
- One command-backed GUI semantic mutation path over an active `Project`
- Incremental validation and rendering over affected scope
- Expanded runtime plugin hosting for at least domain semantics, commands, and views
- End-to-end demos proving runtime-centered architecture rather than compiler-only architecture
- Optional AI proposal slice only if it reuses the same runtime-centered path without delaying the foundation proof

### 6.2 Out Of Scope For M1

- Full editor feature breadth such as routing, symbol authoring, and production drafting
- Broad downstream output expansion beyond what is needed to prove runtime-centered architecture
- Collaboration, cloud tenancy, enterprise admin, and workflow governance surfaces
- Major domain breadth expansion unrelated to the runtime proof
- A large-scale repository rewrite before runtime boundaries are proven `[NOTE FOR PM: prefer evolutionary extraction above M0 rather than renaming-driven reorganization]`

### 6.3 M1 Proof Demonstrations

M1 foundation is complete only when Athena can demonstrate the runtime thesis through a sequenced proof set:

Required foundation proofs:

- `DSL -> Engineering IR -> Compose Viewer`
- `GUI -> one command-backed semantic mutation -> Engineering IR -> SVG`
- `Engineering IR -> Diff/History -> Undo/Replay`

Optional extension proof on the same runtime path:

- `AI proposal -> accepted command -> Engineering IR -> Validation -> SVG`

## 7. Cross-Cutting NFRs

- **NFR-1 Canonical Semantics:** `Engineering IR` remains the canonical semantic model even as runtime, graph, command, and viewer layers are introduced.
- **NFR-2 Determinism:** Given the same semantic state and plugin/knowledge versions, runtime-coordinated validation and rendering remain deterministic.
- **NFR-3 Inspectability:** Runtime, graph, command, and plugin behavior must remain inspectable enough for a reviewer to explain why semantic state changed.
- **NFR-4 Evolutionary Migration:** M1 should evolve above the proven M0 layers rather than forcing a destabilizing rewrite of working compiler boundaries.
- **NFR-5 Frontend Neutrality:** DSL, GUI, and AI surfaces must converge on one runtime-owned semantic path.
- **NFR-6 Interaction Readiness:** Runtime and incremental update behavior must support interactive use rather than only batch compilation.

## 8. Constraints And Guardrails

### 8.1 Architectural Guardrails

- `Athena Runtime` becomes the operational center of gravity, but `Engineering IR` remains the semantic center of gravity.
- The compiler is narrowed toward parsing, lowering, and semantic compilation rather than continuing to absorb every platform concern.
- Graph, command, viewer, plugin, and runtime services remain layered around canonical semantics rather than replacing them.

### 8.2 Migration Guardrails

- M0 modules remain the starting point for M1 evolution.
- The first runtime milestone should add a runtime layer above M0 before large repository restructuring.
- Extraction of plugin, knowledge, workspace, and related concerns out of compiler internals should happen as boundary clarification, not as cosmetic rebranding.

### 8.3 Surface Guardrails

- Compose work in M1 is runtime and viewer infrastructure, not a domain-rich editor product.
- GUI and AI surfaces do not get authority to mutate semantics outside commands and runtime contracts.
- New surfaces should consume runtime services rather than bypassing them.

## 9. Integration And Dependencies

- M1 builds on the existing Java 25 and Kotlin-based workspace.
- The M0 compiler path remains a dependency and proving baseline for M1.
- Compose-based viewing is the preferred first interactive proof surface for the M1 runtime proof.
- Existing plugin, knowledge, and renderer work should be reused and lifted into clearer runtime ownership rather than duplicated.
- Shared library and plugin versions for M1 module expansion should be managed through `gradle/libs.versions.toml` rather than scattered per-module version declarations.

## 10. Success Metrics

**Primary**
- **SM-1:** Athena demonstrates `DSL -> Engineering IR -> Compose Viewer` through runtime-owned orchestration. Validates FR-1, FR-2, FR-10, FR-13.
- **SM-2:** Athena demonstrates one command-backed GUI mutation path that updates canonical project state and produces `GUI -> Engineering IR -> SVG` without requiring a DSL reparse for that interaction path. Validates FR-7, FR-11, FR-13.
- **SM-3:** Athena demonstrates `Engineering IR -> Diff/History -> Undo/Replay`, and undo restores the prior semantic identity set and corresponding diagnostics for the first GUI mutation path. Validates FR-8, FR-9, FR-20.
- **SM-4:** Athena demonstrates incremental validation and rendering over affected scope for the first GUI mutation path rather than relying on whole-project recompilation by default. Validates FR-16, FR-17.

**Secondary**
- **SM-5:** If the optional AI slice is included in M1, Athena demonstrates `AI proposal -> accepted command -> Engineering IR -> Validation -> SVG` through the same runtime-owned semantic path. Validates FR-12, FR-17, FR-18.
- **SM-6:** A reviewer can explain runtime-centered system behavior as `frontend -> runtime -> compiler/render services` instead of `frontend -> compiler -> everything`. Validates FR-3, NFR-3, NFR-4.
- **SM-7:** The same semantic object identity remains visible through graph, command, validation, and viewer flows. Validates FR-4, FR-5, FR-6.

**Counter-metrics (do not optimize)**
- **SM-C1:** Do not optimize for editor breadth before runtime ownership is proven. Counterbalances SM-1 and SM-2.
- **SM-C2:** Do not optimize for repository reorganization size if it increases migration risk without improving architectural boundaries. Counterbalances SM-5.
- **SM-C3:** Do not optimize for plugin surface breadth if plugins begin to dictate canonical semantics or runtime authority. Counterbalances SM-5.

## 11. Open Questions

1. What is the minimum public `Athena Runtime` API surface required in M1 to support DSL, GUI, and AI frontends without overcommitting the SDK?
2. Which single command-backed GUI semantic mutation should be the first proof slice in M1?
3. What persistence and storage approach should the first `Engineering Graph` use before M2-level scalability concerns enter the design?
4. What exact repository extraction order best reduces `:compiler` scope without destabilizing the proven M0 path?

## 12. Assumptions Index

- Section 2.3 - The named M1 journeys are inferred from the transition draft and should be confirmed before UX work begins.
- Section 4.4 - AI interaction in M1 is a proving surface, not a broad autonomous workflow product.
- Section 6.2 - Evolutionary extraction is preferred over a renaming-driven repository rewrite.
