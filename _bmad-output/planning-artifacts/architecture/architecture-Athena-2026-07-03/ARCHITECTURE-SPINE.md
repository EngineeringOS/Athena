---
name: 'Athena M1'
type: architecture-spine
purpose: build-substrate
altitude: initiative
paradigm: 'single-process layered semantic runtime with command-sourced mutation and hosted compiler services'
scope: 'Athena M1 runtime platform'
status: draft
created: '2026-07-03'
updated: '2026-07-03'
binds:
  - 'FR-1'
  - 'FR-2'
  - 'FR-3'
  - 'FR-4'
  - 'FR-5'
  - 'FR-6'
  - 'FR-7'
  - 'FR-8'
  - 'FR-9'
  - 'FR-10'
  - 'FR-11'
  - 'FR-12'
  - 'FR-13'
  - 'FR-14'
  - 'FR-15'
  - 'FR-16'
  - 'FR-17'
  - 'FR-18'
  - 'FR-19'
sources:
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/addendum.md'
companions:
  - '_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md'
---

# Architecture Spine - Athena M1

## Design Paradigm

Athena M1 is a **single-process layered semantic runtime with command-sourced mutation and hosted compiler services**.

- **Single-process** keeps M1 local, deterministic, and evolution-friendly while the runtime boundary is still being proven.
- **Layered semantic runtime** moves the operational center of gravity to runtime-owned lifecycle and service coordination.
- **Command-sourced mutation** binds every semantic change to an explicit mutation path rather than direct caller-side object edits.
- **Hosted compiler services** keep parsing, lowering, validation, and rendering real, but no longer let the compiler own the whole platform.

## Inherited Invariants

| Inherited | From parent | Binds here |
| --- | --- | --- |
| AD-2 | `architecture-Athena-2026-07-02` | Core semantics stay general; domain wedges remain extensions rather than the permanent core vocabulary. |
| AD-3 | `architecture-Athena-2026-07-02` | `Engineering IR` remains the only canonical semantic authority. |
| AD-4 | `architecture-Athena-2026-07-02` | Rendering stays downstream of semantic truth. |
| AD-5 | `architecture-Athena-2026-07-02` | Plugins remain real, typed, and non-sovereign. |
| AD-6 | `architecture-Athena-2026-07-02` | Plugin discovery and compatibility remain explicit and governed. |
| AD-7 | `architecture-Athena-2026-07-02` | `examples/` remain conformance artifacts and architecture contract inputs. |

## Invariants & Rules

### AD-1 - M1 Is A JVM-First Single-Process Runtime Proof

- **Binds:** `all`
- **Prevents:** premature drift into distributed services, cloud-first decomposition, or multiplatform sprawl before the runtime boundary is proven
- **Rule:** M1 remains one Java 25 and Kotlin process. Runtime, graph, command, compiler, plugin, and viewer work optimize for local deterministic execution first. Distribution, multi-user concurrency, and cloud topology are deferred.

### AD-2 - Athena Runtime Owns Lifecycle And Service Orchestration

- **Binds:** `FR-1`, `FR-2`, `FR-3`, `FR-10`, `FR-11`, `FR-12`, `FR-13`, `FR-18`
- **Prevents:** `:compiler` remaining the operational center of gravity or each surface inventing its own bootstrapping path
- **Rule:** `Athena Runtime` is the sole owner of `Workspace`, `Project` activation, `Execution Context`, and `Service Registry`. Compiler, renderer, graph, command, and plugin capabilities are consumed through runtime-owned contracts and may not bootstrap themselves as top-level owners.

### AD-3 - Engineering IR Remains The Only Canonical Semantic Authority

- **Binds:** `FR-4`, `FR-5`, `FR-6`, `FR-10`, `FR-11`, `FR-12`, `FR-16`, `FR-17`, `FR-19`
- **Prevents:** graph state, command history, viewer state, or plugin-private models becoming durable semantic authorities
- **Rule:** `Engineering IR` remains the single canonical engineering model in M1. `Engineering Graph`, command records, diagnostics, and render/view state are derived or operational representations over canonical semantics and may not define competing durable meaning.

### AD-4 - All Semantic Mutation Flows Through The Command Runtime

- **Binds:** `FR-7`, `FR-8`, `FR-9`, `FR-11`, `FR-12`, `FR-14`, `FR-16`, `FR-17`
- **Prevents:** direct mutation of project semantics by GUI handlers, AI helpers, CLI utilities, or plugin internals
- **Rule:** Any semantic state change must enter Athena through an explicit command handled by the `Command Runtime`. Undo, redo, replay, and future transactions all derive from that path. Direct caller-side mutation of canonical project state is forbidden.

### AD-5 - Engineering Graph Is A Runtime Projection With Shared Identity

- **Binds:** `FR-4`, `FR-5`, `FR-6`, `FR-16`, `FR-17`
- **Prevents:** graph APIs drifting from canonical semantics or inventing graph-only identities and relationship truth
- **Rule:** `Engineering Graph` is a runtime-owned projection over canonical project semantics. Graph nodes and edges reuse canonical stable identities, and dependency/traversal services may expose or accelerate runtime operations but may not redefine semantic ownership.

### AD-6 - Frontends Are Adapters To One Runtime Contract

- **Binds:** `FR-10`, `FR-11`, `FR-12`
- **Prevents:** separate DSL, GUI, and AI pipelines each choosing incompatible mutation, validation, and rendering paths
- **Rule:** DSL, GUI, and AI are frontend adapters only. All frontend-originated work must converge on the same runtime-owned project activation, command, validation, and rendering path. A frontend may differ in input method, but not in semantic authority or mutation protocol.

### AD-7 - Compose Runtime Is Domain-Neutral Viewing Infrastructure

- **Binds:** `FR-13`, `FR-14`, `FR-15`
- **Prevents:** the first interactive viewer from absorbing electrical semantics or becoming a second application-specific core
- **Rule:** The first interactive runtime surface uses Compose-based infrastructure for viewport, selection, camera, input, and hit-testing only. Domain semantics stay in canonical semantics and plugins. Compose runtime may consume runtime services, never replace them.

### AD-8 - Incremental Work Is Dependency-Scoped And Runtime-Triggered

- **Binds:** `FR-5`, `FR-16`, `FR-17`
- **Prevents:** full-project recompilation remaining the only runtime behavior or UI-local caches hiding semantic inconsistency
- **Rule:** After a command changes semantic state, the runtime must identify affected semantic scope first and then re-run only the necessary graph, validation, and rendering work for that scope. Incremental results must remain explainable against canonical identities and deterministic rules.

### AD-9 - Plugin Runtime v2 Extends The Runtime Without Owning It

- **Binds:** `FR-3`, `FR-18`, `FR-19`
- **Prevents:** Plugin Runtime v2 becoming a generic escape hatch for lifecycle, orchestration, or semantic redefinition
- **Rule:** Runtime-hosted plugins may contribute commands, views, importers, exporters, AI skills, and domain semantics only through runtime-owned typed contracts. Plugins may not own `Workspace` lifecycle, redefine `Engineering IR`, or bypass command and validation boundaries.

### AD-10 - Build Dependency Management Uses Gradle Version Catalog TOML

- **Binds:** `FR-1`, `FR-3`, `FR-13`, `FR-18`
- **Prevents:** dependency/version drift across runtime, Compose, and future module expansion or ad hoc plugin/version declarations scattered across build files
- **Rule:** Athena M1 manages shared library, plugin, and tool versions through `gradle/libs.versions.toml`. New runtime, graph, command, Compose, and app modules consume versions through catalog aliases instead of hard-coded per-module version strings.

```mermaid
flowchart LR
  cli[CLI]
  dsl[DSL frontend]
  gui[GUI frontend]
  ai[AI frontend]

  runtime[Athena Runtime]
  registry[Service Registry]
  workspace[Workspace and Project lifecycle]
  command[Command Runtime]
  graph[Engineering Graph]
  compiler[Compiler services]
  plugins[Plugin Runtime v2]
  compose[Compose Runtime]
  svg[SVG Renderer]
  ir[Engineering IR]

  cli --> runtime
  dsl --> runtime
  gui --> runtime
  ai --> runtime

  runtime --> registry
  runtime --> workspace
  runtime --> command
  runtime --> graph
  runtime --> compiler
  runtime --> plugins
  runtime --> compose

  command --> ir
  compiler --> ir
  graph --> ir
  plugins --> ir
  compose --> ir
  svg --> ir

  compiler --> svg
  plugins --> command
  plugins --> compose
```

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Naming (entities, files, interfaces, events) | Runtime-owned entrypoints use role nouns such as `Runtime`, `Workspace`, `Project`, `Registry`, `Command`, and `Graph`. Compiler-owned types keep compile vocabulary such as `Parse`, `Lower`, and `Validate`. Viewer infrastructure stays domain-neutral in naming. |
| Data & formats (ids, dates, error shapes, envelopes) | Stable semantic identity is shared across IR, graph, diagnostics, commands, and render models. Command records carry explicit command identity, target project context, and deterministic payload shape. Diagnostics keep provenance plus semantic subject identity. |
| State & cross-cutting (mutation, errors, logging, config, auth) | Runtime owns active state. Commands are the only semantic mutation path. Viewer state is disposable and never canonical. Incremental caches are derived and rebuildable. M1 remains local and has no auth or distributed configuration surface. |
| Build and dependency management | Shared versions live in `gradle/libs.versions.toml`. Compose and future M1 modules use catalog aliases for plugins and libraries rather than repeating raw versions in individual build files. |

## Stack

| Name | Version |
| --- | --- |
| Java | 25 LTS |
| Kotlin | 2.4.0 |
| Gradle | 9.6.1 |
| Compose Multiplatform | 1.11.1 |

## Structural Seed

```mermaid
flowchart TD
  source[Frontend input]
  runtime[Athena Runtime]
  project[Project state]
  command[Command Runtime]
  graph[Engineering Graph]
  compiler[Compiler services]
  validation[Validation]
  render[Renderer backends]
  viewer[Compose viewer]

  source --> runtime
  runtime --> project
  runtime --> command
  command --> project
  project --> graph
  project --> compiler
  compiler --> validation
  validation --> render
  render --> viewer
  graph --> validation
```

```text
Athena/
  gradle/libs.versions.toml    # shared plugin and dependency catalog
  cli/                        # shell entrypoint and bootstrap surface
  language/                   # DSL syntax and parsing
  ir/                         # canonical Engineering IR model
  semantics-core/             # generic semantic validation over canonical IR
  compiler/                   # parse, lower, validate, and downstream derivation services
  domain-electrical-runtime/  # first real domain plugin
  renderer-svg/               # first renderer backend
  runtime/                    # workspace, project, execution context, service registry
  graph/                      # runtime graph projection, query, traversal, dependency scope
  command/                    # command execution, history, undo, redo, replay
  compose-runtime/            # domain-neutral viewer runtime infrastructure
  apps/compose-viewer/        # first interactive viewer surface
  examples/                   # conformance inputs and expected outputs
```

Compose initialization reference for `compose-runtime/` and `apps/compose-viewer/`:

- local template path: `D:/Aaron/workspace/projects/2026/desktop/temp/edge-desktop/edge-desktop`
- version catalog reference: `gradle/libs.versions.toml`
- module-shape reference: `app:desktopApp` as the platform entry, `app:shared` as the shared Compose module, and `core` as the platform-neutral dependency module

## Capability -> Architecture Map

| Capability / Area | Lives in | Governed by |
| --- | --- | --- |
| Runtime-owned workspace and project lifecycle | `runtime` | AD-1, AD-2 |
| Runtime service hosting and execution context | `runtime` | AD-1, AD-2, AD-9 |
| Canonical engineering semantics | `ir`, `compiler`, `semantics-core` | AD-3 |
| Graph query and dependency scope | `graph`, `runtime` | AD-3, AD-5, AD-8 |
| Semantic mutation, history, undo/redo | `command`, `runtime` | AD-4 |
| DSL as a runtime frontend | `language`, `compiler`, `runtime` | AD-2, AD-6 |
| GUI and AI semantic entry paths | `compose-runtime`, `apps/compose-viewer`, `runtime` | AD-4, AD-6, AD-7 |
| Incremental validation and rendering | `runtime`, `graph`, `compiler`, `renderer-svg` | AD-3, AD-5, AD-8 |
| Runtime-hosted plugin growth | `runtime`, `compiler`, `domain-electrical-runtime` | AD-2, AD-9 |
| Compose-based interactive viewing | `compose-runtime`, `apps/compose-viewer` | AD-7 |

## Deferred

- Exact public SDK packaging and external API shape for runtime consumers are deferred until the first runtime boundary is implemented.
- Exact storage and persistence design for `Project` state and `Engineering Graph` are deferred until the first command and incremental flows exist in code.
- Repository macro-layout beyond the seed module split is deferred; the architecture binds ownership boundaries, not a rename-heavy reorganization.
- Collaboration, cloud topology, service decomposition, and multi-user concurrency are deferred beyond M1.
- Broader renderer backends, rich editor tools, routing, symbol systems, and production drafting are deferred beyond the M1 runtime proof.

