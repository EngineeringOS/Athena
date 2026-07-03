---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - D:/Aaron/workspace/projects/2026/eos/Athena/_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-02/prd.md
  - D:/Aaron/workspace/projects/2026/eos/Athena/_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-02/ARCHITECTURE-SPINE.md
---

# Athena - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Athena, decomposing the requirements from the PRD and Architecture into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Authors can express engineering intent in `Engineering Language` without encoding layout coordinates, page mechanics, or target-specific file structures.

FR2: Athena can lower authored, imported, or AI-assisted inputs into `Engineering IR` through explicit transformation boundaries.

FR3: Athena preserves stable semantic identity for engineering objects across compilation, regeneration, and changes in view or output target.

FR4: The `Engineering Compiler` runs semantic compilation as a sequence of explicit passes with declared responsibilities.

FR5: Athena emits diagnostics with enough provenance for reviewers to trace findings back to authored intent, ontology concepts, rule sources, or governed mappings.

FR6: Athena produces downstream outputs as coordinated consequences of one semantic source rather than as separate authorities.

FR7: Athena accepts reviewed standards-derived or reference-derived knowledge through the `Knowledge Compiler` rather than directly through project compilation.

FR8: Athena can package accepted governed knowledge as reusable ontology additions, standards mappings, and rule artifacts.

FR9: Athena exposes published contracts for extension at language, rules, standards, renderer, importer, exporter, AI, and knowledge boundaries.

FR10: Athena can treat external tools and standards as sources, targets, or compatibility boundaries rather than as internal authorities.

FR11: Athena can connect to runtime and enterprise contexts while preserving the semantic core as the upstream authority.

FR12: Athena provides human-facing inspection surfaces that expose language, graph, diagnostics, and compiled outputs without relocating authority into UI state.

FR13: Athena can support multiple derived view types over the same semantic source, including layout- and projection-oriented outputs.

### NonFunctional Requirements

NFR1: Compiler conclusions that matter to user trust must be explainable through semantic objects, ontology concepts, rule artifacts, or standards mappings.

NFR2: Given the same semantic inputs and governed knowledge versions, compilation must produce the same results.

NFR3: Domain growth, renderers, importers, exporters, AI workflows, and knowledge packs must attach through explicit contracts rather than changes to the semantic center.

NFR4: External tool and standards boundaries must remain integrations around the core, not substitutes for the core.

NFR5: AI-assisted or standards-derived knowledge must enter operational use only through reviewable governance.

NFR6: UI and rendering layers must remain downstream and replaceable.

NFR7: Compiler execution must be deterministic for identical semantic inputs and governed knowledge versions.

NFR8: Diagnostics must remain inspectable and legible enough for human review.

### Additional Requirements

- M0 is the governing first proof slice: minimal Electrical/Runtime DSL -> AST -> semantic validation -> stable `Engineering IR` -> simple `SVG` renderer -> `5-10` conformance examples.
- M0 is JVM-first, local, and single-process on Kotlin/JVM with Java `25` LTS, Kotlin `2.4.0`, and Gradle `9.6.1`.
- The semantic core stays general; Electrical/Runtime is the first domain extension and must not become the permanent core vocabulary.
- `Engineering IR` is the first and only canonical semantic authority in M0; AST is syntax-level only; rule execution authority belongs only to `Engineering IR`.
- `SVG` is downstream only; a compiler-owned render-facing model sits between `Engineering IR` and rendering; semantic/layout/geometry separation is binding even though full durable `Layout IR` is deferred.
- M0 must prove a real plugin system, not just internal extension points.
- Plugin classes are typed, at minimum domain, renderer, and rule extension contracts.
- Plugins may extend the system only through core-owned contracts and may not privately redefine semantic meaning.
- Plugins depend on core contracts only, never on concrete sibling plugins.
- Plugin discovery is manifest-driven and local via JVM classpath with compatibility validation before use.
- The compiler owns pass ordering and phase boundaries; plugins attach only at declared extension points.
- `examples/` are conformance artifacts with stable expected validation outcomes, `Engineering IR` shape, and `SVG` output class.
- `AutomationML` is a standards and ontology reference concept for M0 boundary design only, not an implementation target.
- Remote plugin distribution, hot loading, sandboxing, marketplace mechanics, AI, `OPC UA`, cloud, enterprise, and multiplatform concerns are all deferred beyond M0.

### UX Design Requirements

None included in this phase. UX work has not started yet.

### FR Coverage Map

FR1: Epic 1 - Author engineering intent without layout or target-specific encoding.
FR2: Epic 1 - Lower authored inputs into canonical `Engineering IR` through explicit transformations.
FR3: Epic 1 - Preserve stable semantic identity across compilation, regeneration, and output changes.
FR4: Epic 1 - Execute semantic compilation through explicit compiler passes.
FR5: Epic 1 - Emit diagnostics with traceable provenance back to intent, rules, and mappings.
FR6: Epic 1 - Produce downstream outputs as consequences of one semantic authority.
FR7: Epic 2 - Accept governed standards-derived and reference-derived knowledge through the `Knowledge Compiler`.
FR8: Epic 2 - Package governed knowledge as reusable ontology, mapping, and rule artifacts.
FR9: Epic 2 - Expose published extension contracts at declared system boundaries.
FR10: Epic 2 - Treat external tools and standards as boundaries rather than internal authorities.
FR11: Epic 2 - Connect runtime and enterprise contexts without displacing the semantic core.
FR12: Epic 1 - Provide inspection surfaces without relocating authority into UI state.
FR13: Epic 1 - Support multiple derived view types from the same semantic source.

## Epic List

### Epic 1: End-to-End Semantic Compilation Proof
Authors and reviewers can define engineering intent, compile it deterministically into canonical `Engineering IR`, inspect diagnostics and derived artifacts, and obtain a downstream `SVG` from one semantic source.
**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR12, FR13

### Epic 2: Governed Extension And External Boundary Proof
Platform builders can extend Athena through governed knowledge and typed plugin contracts while keeping external standards, tools, and runtime or enterprise contexts at the boundary rather than inside the semantic core.
**FRs covered:** FR7, FR8, FR9, FR10, FR11

## Epic 1: End-to-End Semantic Compilation Proof

Authors and reviewers can define engineering intent, compile it deterministically into canonical `Engineering IR`, inspect diagnostics and derived artifacts, and obtain a downstream `SVG` from one semantic source.

### Story 1.1: Establish The M0 JVM Compiler Workspace

**FRs implemented:** None directly; foundational implementation enabler for M0 stack constraints

As a platform builder,
I want Athena to start from a working Kotlin/JVM compiler workspace with baseline modules, build logic, and CLI entrypoint wiring,
So that the M0 proof can be implemented and verified on a deterministic foundation before semantic behavior is added.

**Acceptance Criteria:**

**Given** the approved M0 stack and module shape
**When** the initial workspace is created
**Then** the repository contains the baseline modules needed for `cli`, `language`, `semantics-core`, `ir`, `compiler`, `domain-electrical-runtime`, `renderer-svg`, and `examples`
**And** the workspace builds successfully on Java `25`, Kotlin `2.4.0`, and Gradle `9.6.1`

**Given** the baseline workspace
**When** a developer runs the standard build and test entrypoints
**Then** dependency resolution, compilation, and baseline test execution succeed deterministically
**And** the workspace exposes a minimal CLI entry path suitable for later compiler wiring

**Given** the greenfield starting point
**When** implementation proceeds to semantic stories
**Then** later stories can modify only the modules they need
**And** no later story needs to invent project structure ad hoc

### Story 1.2: Author And Parse The M0 Electrical/Runtime DSL

**FRs implemented:** FR1

As a platform builder,
I want Athena to accept the minimal M0 Electrical/Runtime DSL and parse it into a syntax-only AST,
So that authored engineering intent becomes deterministic compiler input for later semantic compilation passes.

**Acceptance Criteria:**

**Given** a valid M0 Electrical/Runtime source file using the approved keyword set for components, ports, and connections
**When** the compiler parses the file
**Then** it produces a syntax-only AST that preserves declarations, references, and source spans without assigning semantic meaning
**And** the parse result is deterministic for identical source input

**Given** an invalid M0 source file with malformed syntax
**When** the compiler parses the file
**Then** it emits syntax diagnostics with file, line, and column provenance
**And** semantic validation and rendering passes do not execute on that failed parse result

**Given** a representative valid example under `examples/`
**When** it is parsed in the standard compiler entry path
**Then** the AST shape is stable enough to support snapshot or fixture-based verification
**And** the example remains free of layout or renderer-specific authoring concerns

### Story 1.3: Lower Parsed Intent Into Canonical Engineering IR

**FRs implemented:** FR2, FR3

As a platform builder,
I want Athena to lower a valid syntax-only AST into canonical `Engineering IR` with stable semantic identities,
So that every later compiler pass works from one authoritative semantic model rather than from syntax trees or renderer-specific structures.

**Acceptance Criteria:**

**Given** a valid parsed M0 AST describing electrical or runtime declarations, ports, and connections
**When** the semantic lowering pass executes
**Then** it produces `Engineering IR` objects for the declared engineering elements and their relationships
**And** the resulting IR is independent of any layout, geometry, or renderer-specific fields

**Given** two compilations of semantically unchanged authored input
**When** the lowering pass produces `Engineering IR`
**Then** each engineering object retains a stable semantic identity across runs
**And** downstream passes can reference those identities without using AST node positions as authority

**Given** authored input that parses successfully but contains unresolved semantic references reserved for later validation
**When** lowering completes
**Then** the compiler still emits a structurally well-formed `Engineering IR` with traceable source provenance
**And** semantic errors remain the responsibility of later validation passes rather than the lowering phase

### Story 1.4: Validate Engineering IR And Emit Provenance-Rich Diagnostics

**FRs implemented:** FR4, FR5

As a reviewer,
I want Athena to validate canonical `Engineering IR` for references, types, ports, and connections,
So that semantic defects are detected in one authoritative model and can be traced back to their authored origin.

**Acceptance Criteria:**

**Given** a valid `Engineering IR` produced from M0 authored input
**When** the semantic validation pass executes
**Then** it verifies references, declared types, port compatibility, and allowed connections against the active domain rules
**And** it marks the IR as semantically valid for downstream compilation

**Given** an `Engineering IR` containing unresolved references, incompatible types, invalid ports, or illegal connections
**When** the semantic validation pass executes
**Then** it emits diagnostics that identify the failing semantic object and rule category
**And** each diagnostic includes enough provenance to trace the issue back to source locations, IR identities, and the responsible validation rule

**Given** a compilation run with one or more semantic diagnostics
**When** validation completes
**Then** the compiler does not report semantic success for that input
**And** downstream rendering or derived output steps run only according to the declared pipeline policy for invalid semantic state

### Story 1.5: Execute The M0 Compiler As Declared Deterministic Passes

**FRs implemented:** FR4

As a platform builder,
I want Athena to run M0 compilation through an explicit ordered pass pipeline,
So that parsing, lowering, validation, and downstream derivation happen predictably through declared responsibilities and phase boundaries.

**Acceptance Criteria:**

**Given** the standard M0 compiler entry path
**When** a compilation request is executed
**Then** the compiler runs parsing, lowering, validation, and downstream derivation through an explicitly declared pass sequence
**And** each pass has a documented responsibility, declared input state, and declared output state

**Given** identical semantic inputs and governed knowledge versions
**When** the same pass pipeline is executed multiple times
**Then** the compiler produces the same pass outcomes, diagnostics, and success or failure state
**And** no pass may rely on hidden mutable state or implicit ordering outside the declared pipeline

**Given** a pass failure or a pipeline gate condition
**When** the compiler evaluates whether to continue
**Then** it applies deterministic continuation or stop rules for subsequent passes
**And** the pipeline behavior is inspectable enough for reviewers to understand why later passes did or did not run

### Story 1.6: Derive A Render Model And Emit Simple SVG From Engineering IR

**FRs implemented:** FR6, FR12, FR13

As a reviewer,
I want Athena to derive a renderer-facing model from canonical `Engineering IR` and emit a simple `SVG`,
So that I can inspect a downstream visual consequence of the semantic source without turning rendering into an independent authority.

**Acceptance Criteria:**

**Given** semantically valid `Engineering IR` from an M0 compilation run
**When** the rendering backend executes
**Then** the compiler derives a renderer-facing model from `Engineering IR` before `SVG` emission
**And** the renderer-facing model contains only the information needed for rendering, not new semantic authority

**Given** the same valid `Engineering IR` input
**When** the `SVG` renderer executes multiple times
**Then** it emits a stable `SVG` output class consistent with the renderer-facing model
**And** the renderer does not infer, repair, or invent missing semantic meaning

**Given** semantic invalidity or missing renderer prerequisites according to pipeline policy
**When** `SVG` emission is requested
**Then** the compiler either blocks rendering or emits only the outputs explicitly allowed by policy
**And** any rendering-related diagnostic remains traceable back to upstream semantic or pipeline state

### Story 1.7: Publish M0 Conformance Examples For The End-to-End Proof

**FRs implemented:** FR6

As a platform builder,
I want Athena to ship representative M0 example projects with stable expected outcomes,
So that the end-to-end semantic compilation proof can be verified repeatedly against known authored inputs, diagnostics, IR shape, and `SVG` output class.

**Acceptance Criteria:**

**Given** the completed M0 compiler path for parsing, lowering, validation, and rendering
**When** example projects under `examples/` are executed through the standard compilation entry path
**Then** Athena produces the expected success or failure result for each example
**And** each example has stable expectations for diagnostics, `Engineering IR` shape, and `SVG` output class where applicable

**Given** the M0 scope definition
**When** the example suite is assembled
**Then** it contains at least `5` and at most `10` representative projects covering valid and invalid cases for declarations, ports, references, types, and connections
**And** the examples remain authored in the DSL rather than in downstream interchange or renderer-specific formats

**Given** a regression in parser behavior, semantic lowering, validation, or rendering
**When** the conformance examples are re-run
**Then** the regression is detectable through changed expected outcomes or fixtures
**And** the examples serve as a stable proof artifact for the M0 architectural decision

## Epic 2: Governed Extension And External Boundary Proof

Platform builders can extend Athena through governed knowledge and typed plugin contracts while keeping external standards, tools, and runtime or enterprise contexts at the boundary rather than inside the semantic core.

### Story 2.1: Define Core-Owned Typed Plugin Contracts

**FRs implemented:** FR9

As a platform builder,
I want Athena to publish core-owned typed contracts and manifest requirements for plugins,
So that domain, rule, and renderer extensions can attach to the compiler without redefining semantic authority.

**Acceptance Criteria:**

**Given** the M0 core compiler architecture
**When** extension points are defined
**Then** Athena exposes typed contracts for at least domain, rule, and renderer plugins
**And** those contracts define what a plugin may contribute without allowing it to replace `Engineering IR` as the semantic authority

**Given** a plugin implementation targeting Athena
**When** it declares itself for local use
**Then** it includes a manifest with plugin identity, version, declared plugin type, compatible core version range, and required extension points
**And** the manifest format is owned by the core rather than by individual plugins

**Given** multiple plugin implementations in the system
**When** their dependencies are evaluated
**Then** each plugin depends only on published core contracts and shared core types
**And** no plugin requires direct linkage to concrete sibling plugins to function

### Story 2.2: Discover Local Plugins And Validate Compatibility Before Use

**FRs implemented:** FR9, FR10

As a platform builder,
I want Athena to discover local plugins from manifests and validate compatibility before activation,
So that the M0 compiler can load real extensions safely and deterministically without hidden runtime coupling.

**Acceptance Criteria:**

**Given** a local Athena installation with one or more plugin artifacts on the JVM classpath
**When** the compiler initializes plugin discovery
**Then** it locates plugin manifests through the core-defined discovery mechanism
**And** it builds a deterministic inventory of candidate plugins before any compiler pass uses them

**Given** a discovered plugin manifest
**When** Athena evaluates it for activation
**Then** the core validates plugin identity, declared type, compatible core version range, and required extension points
**And** incompatible or malformed plugins are rejected with inspectable diagnostics before activation

**Given** a valid set of compatible plugins
**When** the compiler starts a compilation run
**Then** only the approved plugins are attached at declared extension points owned by the core
**And** plugin activation order does not override the compiler's pass ordering or semantic authority

### Story 2.3: Deliver Electrical And Runtime Semantics Through A Real Domain Plugin

**FRs implemented:** FR9

As a platform builder,
I want the first Electrical/Runtime domain semantics to be delivered through a real Athena domain plugin,
So that M0 proves the semantic core stays general while the first domain extension remains replaceable and governed by core contracts.

**Acceptance Criteria:**

**Given** the core Athena compiler and plugin contracts
**When** the M0 Electrical/Runtime domain is implemented
**Then** its domain vocabulary, lowering contributions, and semantic validation contributions are provided through a domain plugin
**And** the core compiler remains free of hard-coded Electrical/Runtime-specific semantic meaning beyond shared contracts

**Given** the M0 proof slice for declarations, ports, references, types, and connections
**When** the Electrical/Runtime domain plugin is active
**Then** the compiler can parse, lower, and validate those domain concepts through the declared extension points
**And** removing the plugin disables those domain semantics without breaking the core compiler architecture

**Given** future domain growth beyond the first M0 scope
**When** new domain concepts are considered
**Then** the architecture permits them to arrive through the same contract-governed extension mechanism
**And** the Electrical/Runtime plugin does not become the permanent core vocabulary of Athena

### Story 2.4: Define Governed Knowledge Artifact Packages

**FRs implemented:** FR7, FR8

As a platform builder,
I want Athena to define versioned package and manifest formats for governed ontology, mapping, and rule artifacts,
So that reviewed knowledge can be published as reusable compiler inputs with explicit provenance and compatibility.

**Acceptance Criteria:**

**Given** reviewed ontology, mapping, or rule content approved for operational use
**When** that content is packaged for Athena
**Then** the result includes a typed artifact package and manifest declaring artifact kind, version, provenance, and compatible core or contract range
**And** the package format remains distinct from project-authored engineering input

**Given** a malformed or incomplete governed knowledge package
**When** Athena validates the package
**Then** it rejects the package before operational use
**And** it emits diagnostics describing the packaging or manifest defect

### Story 2.5: Resolve Governed Knowledge Artifacts Into Compilation Context

**FRs implemented:** FR7, FR8

As a platform builder,
I want Athena to resolve approved governed knowledge artifacts into the effective compilation context,
So that compiler behavior can use reviewed knowledge and trace conclusions back to exact artifact versions.

**Acceptance Criteria:**

**Given** one or more compatible governed knowledge packages
**When** a compilation run begins
**Then** Athena resolves the active artifacts into the effective compilation context
**And** the active artifact identities, versions, and provenance remain inspectable

**Given** diagnostics or derived outcomes influenced by governed knowledge
**When** those results are reported
**Then** they can reference the responsible governed artifact versions
**And** incompatible knowledge packages are rejected before they change compiler behavior

### Story 2.6: Define External Boundary Contract Descriptors

**FRs implemented:** FR10, FR11

As a platform builder,
I want Athena to define machine-readable boundary contract descriptors for external tools, standards, and runtime or enterprise contexts,
So that future integrations can connect to the semantic core without becoming alternate semantic authorities.

**Acceptance Criteria:**

**Given** a candidate external boundary such as a standards interchange, runtime connector, or enterprise bridge
**When** Athena defines the boundary descriptor
**Then** the descriptor declares the boundary direction, owned semantic authority, expected exchanged forms, and compatibility assumptions
**And** those contracts keep `Engineering IR` as the upstream semantic authority

**Given** a boundary descriptor for a standards concept such as `AutomationML`
**When** it is validated in M0
**Then** Athena can represent it as a reference or compatibility boundary without requiring a production importer, exporter, or live connector
**And** validation fixtures prove that the descriptor does not relocate authority out of the semantic core
