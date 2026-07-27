---
stepsCompleted:
  - 1
  - 2
  - 3
  - 4
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-27-m35/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-27-m35/ARCHITECTURE-SPINE.md
  - _bmad-output/implementation-artifacts/m34/m34-retrospective-and-m35-handoff.md
title: Athena M35 Epics And Stories
status: ready-for-story-creation
created: '2026-07-27'
updated: '2026-07-27'
---

# Athena M35 - Epic Breakdown

## Overview

M35 delivers one professional Cabinet physical-installation projection backed by deterministic,
typed Athena source, package-local resources, physical constraints, routing, occurrence trace, and a
paint-only renderer. Cabinet is the only visible product surface. M35 creates no Engineering
Component System, no second package authority, and no compatibility path for unreleased legacy
syntax or XML/runtime paths.

## Requirements Inventory

### Functional Requirements

- FR-1: Enforce lowercase package/path parity for governed Athena source and reject default-package source.
- FR-2: Publish machine-readable, source-spanned package/path diagnostics through LSP.
- FR-3: Migrate the M35 sample to package-hierarchical source layout.
- FR-4: Exclude generated snapshots and caches from package/path validation.
- FR-5: Resolve resource paths only from the declaring source unit inside its admitted package snapshot.
- FR-6: Separate logical resource identity, admitted content evidence, package snapshot identity, and validated lock-state fingerprint.
- FR-7: Prove one local standard package and one vendor/user package using Java-style hierarchy.
- FR-8: Reject absolute/traversal/symlink/junction/cross-package physical resource access; dependencies expose definitions, not raw resources.
- FR-9: Add typed package-local resource declarations with SVG as one resource kind.
- FR-10: Replace lock v1 with canonical `RepositoryLockV2`, fail-closed validate mode, and atomic explicit update mode.
- FR-11: Extend the single `athena.yaml`/`RepositoryManifest` contract for authored package/root/dependency intent without adding another manifest.
- FR-12: Keep package/namespace identity separate from engineering product identity.
- FR-13: Prove one complex SVG-backed vendor Element with Athena-owned metadata and geometry-only SVG hints.
- FR-14: Compile SVG-backed and native Athena graphic bodies through the same representation contracts.
- FR-15: Prove unmarked SVG geometry cannot become an anchor, label slot, role, or binding predicate.
- FR-16: Reject duplicate or conflicting Athena/SVG metadata.
- FR-17: Limit `data-athena-*` to optional geometry-reference hints and fail closed on missing referenced geometry.
- FR-18: Define `PhysicalInstallationIR v0` and derive Cabinet composition from it plus resolved representation occurrences.
- FR-19: Compile authored Cabinet intent into physical placement facts before rendering.
- FR-20: Make Enclosure the sole container; use typed surface/rail/terminal mount targets and deterministic terminal ordering.
- FR-21: Derive Cabinet document bounds and viewport from compiled composition bounds.
- FR-22: Keep renderer paint-only with no placement or semantic inference.
- FR-23: Separate physical intent, constraints, and renderer coordinates with typed size/orientation/mount/clearance/compatibility facts.
- FR-24: Validate orientation, depth, fit, containment, collision, clearance, and mounting compatibility without solving or optimization.
- FR-25: Keep product identity and physical truth out of visual Elements and geometry.
- FR-26: Define the closed, validated `PhysicalInstallationContractV0`.
- FR-27: Resolve physical contracts only from governed project facts or existing admitted exports; add no component/package kind.
- FR-28: Require stable connection aliases, same-source route references, authored channel rectangles, and deterministic no-search routing.
- FR-29: Bind endpoints to explicit anchors and use exact rational lane centres plus same-duct passable adjacency.
- FR-30: Emit structured route proof for counts, channel use, endpoints, segments, intersections, and failures.
- FR-31: Prove zero route/body intersections and zero endpoints outside bound anchors.
- FR-32: Trace every rendered Cabinet occurrence to authoritative Athena and package/source provenance.
- FR-33: Resolve Cabinet selection through typed interaction subjects, never DOM/SVG guessing.
- FR-34: Prove selected mounted occurrences identify device, binding, Element, and graphic-body source declarations.
- FR-35: Preserve the governed M31 action-to-preview-to-compile-to-rerender edit contract.
- FR-36: Forbid authoritative mutation of Presentation IR, Graphic Primitive IR, SVG, or renderer state.
- FR-37: Deliver one dedicated M35 sample that opens Cabinet by default.
- FR-38: Include enclosure, mounted controls, terminal group, channels, labels, complex vendor SVG, and package hierarchy in the sample.
- FR-39: Capture desktop and narrow Electron screenshots against the structural professional Cabinet checklist.
- FR-40: Prove zero diagnostics/fallback/XML/raw-markup authority, nonblank pixels, and visible professional Cabinet layout.
- FR-41: End every story with RED/GREEN evidence, AC review, and polish/purge of touched and adjacent paths.
- FR-42: Delete unreleased legacy XML/package/render and alias-free connection paths unless explicit migration evidence still requires them.

### NonFunctional Requirements

- NFR-1: Athena source remains the single metadata authority.
- NFR-2: Package/resource resolution is deterministic, reproducible, bounded, and safe to cache.
- NFR-3: Cabinet composition is compiler-owned and renderer-neutral.
- NFR-4: Public syntax remains small, type-safe, and usable by humans and AI authors.
- NFR-5: Tests and E2E evidence are falsifiable; screenshots and visuals cannot be mocked.
- NFR-6: Unreleased legacy paths are deleted instead of wrapped in compatibility adapters.
- NFR-7: Cabinet remains one projection of EngineeringOS physical reality and does not pollute semantic-kernel ownership.
- NFR-8: M35 introduces deterministic validation, not AI auto-layout or general constraint solving.

### Additional Requirements

- `kernel:physical-model` owns validated measurements, `PhysicalInstallationContractV0`, topology, IR, diagnostics, and pure constraint evaluation.
- Existing unchecked `PhysicalSize` is resolver input only; invalid values cannot enter resolved contracts or physical IR.
- Contract precedence is field-level project-over-trait; set fields replace atomically; ambiguity and missing fields fail before IR construction.
- Enclosure is the sole physical container; Duct and RouteChannel are never mount targets.
- Surface/Duct/TerminalGroup positions are enclosure-local, Rail is surface-local, RouteChannel is duct-interior-local, and occurrences are mount-target-local.
- All physical target transforms are rigid with determinant `+1`; vertical Rail frames must not mirror graphics.
- Surface, Duct, TerminalGroup, Rail, channel, occurrence, depth, collision, and clearance containment predicates are fail-closed.
- Physical placement and representation selection compile independently and join only by `InstallationOccurrenceKey`.
- `CabinetVisualTransform` uses the normative seven-stage transform and golden body/anchor tests for all rotations and Rail frames.
- Physical routing geometry belongs to `CabinetRoutingCompiler` in `drawing-composition`, not schematic `routing-model`.
- Lane coordinates use exact rational millimetres; adjacency is derived only from one positive same-Duct shared boundary.
- Every connection is aliased; alias-free grammar, AST, lowering, examples, and tests are migrated and deleted in one story.
- `installation cabinet` is system-scoped, `cabinet` is the only M35 kind, and `route` is installation-scoped.
- ANTLR4 remains semantic parser authority; Tree-sitter, formatter, LSP, examples, and `examples/m23/parser-parity-proof` move together.
- `RepositoryManifest` owns authored intent; resolved coordinates and `RepositoryLockV2` own derived resolution evidence.
- One repository loader feeds all consumers; duplicate YAML and regex source scanners are removed.
- Package admission is two-phase, immutable, AST-driven, no-follow, root-confined, and governed by one non-overridable `PackageAdmissionLimitsV1`.
- Bare resource ids are lexical to one source unit; dependencies expose compiled Symbol/Element definitions, never raw resources.
- `ValidatedLockStateDigest` hashes canonical lock facts, never lock bytes or package snapshot identity.
- SVG ids are geometry lookup keys; a referenced id must materialize exactly once after safe `use` expansion.
- SVG parser hardening, normalization, bounds separation, and raw-markup transport prohibition remain inherited M34 gates.
- `GraphicOccurrenceTraceTable` is normalized in `presentation-model`; primitives carry occurrence ids only.
- Selection reuses `InteractionSubjectResolver` and `SemanticCapabilityRegistry`; frontend prefix inference is deleted.
- Future edits reuse `SemanticActionIntent -> AuthoringIntent -> SemanticAuthoringTransaction -> AuthoringPreview` and source-edit evidence.
- M35 Cabinet has one active path: Physical IR + resolved representation -> Cabinet composition -> Graphic Primitive -> PresentationDocument.
- M34 Control Drawing remains regression-only; old Cabinet producers require zero-caller deletion proof.
- The dedicated sample uses one standard and one vendor/user package and opens only Cabinet.
- E2E requires fresh 1920x1080, 1280x900, and narrow Electron screenshots plus nonblank canvas-pixel and structured proof.
- Every story records RED/GREEN commands, AC-to-evidence mapping, three-layer review, and final polish/purge.
- Gradle verification on Windows runs strictly sequentially.

### UX Design Requirements

No separate M35 UX contract is included. FR-39, FR-40, AD-16, and the M34 handoff define the
Cabinet-only visual and interaction acceptance: professional physical containment, readable density,
contained routes, complete typed selection trace, no clipping/overflow/unintended overlap, no wide
shallow canvas, and no toy graph-card composition.

### FR Coverage Map

| Requirement | Epic | Coverage |
| --- | --- | --- |
| FR-1 | Epic 1 | Package/path parity |
| FR-2 | Epic 1 | Package diagnostics |
| FR-3 | Epic 1 | Hierarchical sample migration |
| FR-4 | Epic 1 | Generated-state exclusion |
| FR-5 | Epic 1 | Source-local resource resolution |
| FR-6 | Epic 1 | Resource/snapshot/lock identity separation |
| FR-7 | Epic 1 | Standard and vendor fixtures |
| FR-8 | Epic 1 | Resource confinement |
| FR-9 | Epic 1 | Typed resource declarations |
| FR-10 | Epic 1 | RepositoryLockV2 lifecycle |
| FR-11 | Epic 1 | Single manifest authority |
| FR-12 | Epic 1 | Package/product identity separation |
| FR-13 | Epic 1 | Complex SVG-backed vendor proof |
| FR-14 | Epic 1 | Shared representation contracts |
| FR-15 | Epic 1 | Unmarked geometry remains visual |
| FR-16 | Epic 1 | Metadata conflict rejection |
| FR-17 | Epic 1 | Geometry-hint-only SVG boundary |
| FR-18 | Epic 2 | PhysicalInstallationIR v0 |
| FR-19 | Epic 2 | Authored intent compilation |
| FR-20 | Epic 2 | Typed containment and mount targets |
| FR-21 | Epic 2 | Derived Cabinet bounds/viewport |
| FR-22 | Epic 2 | Paint-only renderer |
| FR-23 | Epic 2 | Intent/constraint/coordinate separation |
| FR-24 | Epic 2 | Deterministic physical validation |
| FR-25 | Epic 2 | Product knowledge excluded from visuals |
| FR-26 | Epic 2 | Closed physical contract |
| FR-27 | Epic 2 | Governed physical contract sources |
| FR-28 | Epics 2-3 | Aliased connection identity and deterministic physical routes |
| FR-29 | Epic 3 | Anchor/lane/adjacency contract |
| FR-30 | Epic 3 | Structured route proof |
| FR-31 | Epic 3 | Zero intersection/endpoint failures |
| FR-32 | Epic 3 | Occurrence-to-source trace |
| FR-33 | Epic 3 | Typed selection resolution |
| FR-34 | Epic 3 | Graphic-to-source proof |
| FR-35 | Epic 3 | Governed future edit path |
| FR-36 | Epic 3 | No graphic-state mutation authority |
| FR-37 | Epic 4 | Dedicated Cabinet-first sample |
| FR-38 | Epic 4 | Complete physical/vendor fixture |
| FR-39 | Epic 4 | Professional viewport screenshots |
| FR-40 | Epic 4 | Product smoke and authority proof |
| FR-41 | All epics | Mandatory RED/GREEN, AC review, and polish/purge |
| FR-42 | All epics | Mandatory unreleased legacy deletion |

## Epic List

### Epic 1: Use Trusted Standard And Vendor Representation Material

Engineers and AI authors can compile portable standard/vendor representation packages whose source,
resources, locks, and SVG geometry are deterministic, type-checked, confined, and governed by one
Athena metadata authority.

**FRs covered:** FR-1..FR-17, FR-41, FR-42

### Epic 2: Author And Validate Physical Cabinet Reality

Engineers can describe one Cabinet installation in compact Athena source and receive a deterministic,
validated physical model and composed Cabinet drawing whose containment, mounting, dimensions,
orientation, clearance, bounds, and visual transforms are compiler-owned.

**FRs covered:** FR-18..FR-28, FR-41, FR-42

### Epic 3: Route, Select, And Trace The Cabinet Safely

Engineers can route aliased connections through governed Cabinet channels, inspect falsifiable route
proof, select any mounted occurrence, and trace it back to authoritative semantic, installation,
binding, representation, and resource source without mutating graphic state.

**FRs covered:** FR-28..FR-36, FR-41, FR-42

### Epic 4: Demonstrate A Professional Cabinet Product

Customers can open one dedicated M35 project directly in the Cabinet surface and see a dense,
readable, physically contained, package-backed industrial drawing that passes structured authority,
geometry, routing, trace, desktop, narrow, and nonblank-canvas E2E gates.

**FRs covered:** FR-37..FR-42

## Epic 1: Use Trusted Standard And Vendor Representation Material

Engineers and AI authors can compile portable standard/vendor representation packages whose source,
resources, locks, and SVG geometry are deterministic, type-checked, confined, and governed by one
Athena metadata authority.

### Story 1.1: Enforce Governed Package Hierarchy

As a package author,
I want Athena package declarations and source paths validated by one repository contract,
So that packages remain predictable without turning filesystem namespaces into product identity.

**Acceptance Criteria:**

**Implements:** FR-1..FR-4, FR-11..FR-12, FR-41..FR-42.

**Given** an `athena.yaml` manifest with governed source roots and package intent
**When** repository validation runs
**Then** one extended `RepositoryManifest` and one loader provide typed roots to compiler, package runtime, and LSP
**And** duplicate YAML/line scanners are removed from the active path.

**Given** governed Athena source under a declared root
**When** its package namespace and normalized parent path differ by segment, case, default package, or NFC form
**Then** admission fails with a stable machine-readable source/path diagnostic
**And** LSP publishes the same diagnostic without considering generated snapshots or caches.

**Given** a valid namespace and path
**When** semantic/package identity is inspected
**Then** namespace remains transport identity only and cannot supply manufacturer, article, revision, rating, or lifecycle facts.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** touched/adjacent stale scanners, fixtures, docs, generated outputs, XML paths, and compatibility names are purged.

### Story 1.2: Admit And Lock Immutable Package Snapshots

As an engineering project owner,
I want package snapshots and lock evidence to be reproducible and fail closed,
So that the same project compiles from the same admitted material across machines and time.

**Acceptance Criteria:**

**Implements:** FR-6, FR-8, FR-10..FR-11, FR-41..FR-42.

**Given** repository-declared package roots and dependencies
**When** two-phase package admission runs
**Then** capture is no-follow, root-confined, immutable, AST-driven, race-checked, and governed by one `PackageAdmissionLimitsV1`
**And** absolute paths, traversal, links/reparse points, overlapping roots, duplicate physical files, and budget excess fail closed.

**Given** admitted package content
**When** snapshot identity is computed
**Then** canonical manifest intent, resolved coordinate, sorted source/resource hashes, and compiler/schema version determine the snapshot digest
**And** generated lock bytes and validated lock state do not participate in logical resource or snapshot identity.

**Given** validate mode
**When** `athena.lock` is missing, stale, or schema-incompatible
**Then** compilation fails with the specified stable lock diagnostic and performs no write.

**Given** explicit update mode
**When** `RepositoryLockV2` is materialized
**Then** canonical bytes are written through a same-directory temporary file and atomic replacement, temporary output is cleaned on failure, and successful output is revalidated
**And** lock v1, direct non-atomic writing, and the old lock-dependent snapshot algorithm are deleted without compatibility readers.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** touched/adjacent stale scanners, fixtures, docs, generated outputs, XML paths, and compatibility names are purged.

### Story 1.3: Compile Source-Local Typed Resources

As a representation package author,
I want to declare a package-local resource beside its Athena definition,
So that standard and vendor visuals resolve without fragile workspace-relative paths.

**Acceptance Criteria:**

**Implements:** FR-5, FR-7..FR-9, FR-41..FR-42.

**Given** `resource <id> { kind svg path "./asset.svg" }` and `graphic svg resource <id>` in one source unit
**When** ANTLR4 parsing, type checking, and resource resolution run
**Then** the bare id resolves only to a declaration in that source unit and path resolution starts at that unit's admitted directory
**And** duplicate same-unit ids, missing declarations, unsupported kinds, and missing files fail with source-spanned diagnostics.

**Given** equal resource ids in different source units
**When** package admission runs
**Then** they remain independent `PackageResourceKey`s and do not shadow
**And** moving a declaration to another source unit deterministically changes its key.

**Given** a dependency package
**When** a consumer references its exported Symbol or Element
**Then** the dependency's internal resource resolves inside its own snapshot
**And** direct cross-source/cross-package raw resource keys or physical paths are impossible.

**Given** the public resource syntax
**When** formatter, LSP diagnostics/completion/tokens, Tree-sitter parsing/highlighting, and parser parity run
**Then** every frontend accepts/rejects the same corpus and repeated formatting is idempotent.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** touched/adjacent stale scanners, fixtures, docs, generated outputs, XML paths, and compatibility names are purged.

### Story 1.4: Compile Complex SVG Geometry Without Metadata Authority

As a vendor asset author,
I want a complex SVG body to provide geometry while Athena source provides all meaning,
So that realistic assets remain maintainable without creating a second language or authority.

**Acceptance Criteria:**

**Implements:** FR-13..FR-17, FR-41..FR-42.

**Given** a complex package-local SVG referenced by an Athena Element
**When** safe compilation runs
**Then** namespace-aware no-I/O parsing, closed-subset validation, fixed limits, transform normalization, and `GraphicPrimitive` lowering apply
**And** raw markup never crosses compiler/LSP/Electron transport.

**Given** Athena geometry references and SVG XML ids
**When** safe acyclic `use` expansion completes
**Then** each referenced id materializes exactly once and returns normalized geometry plus accumulated transform
**And** zero or multiple materializations fail as missing or ambiguous while unreferenced geometry may repeat.

**Given** SVG attributes other than optional matching `data-athena-geometry-ref`
**When** metadata validation runs
**Then** SVG cannot declare identity, lifecycle, anchors, labels, roles, direction, signal, compatibility, profile, binding, physical size, or project ports
**And** unmarked geometry remains visual-only.

**Given** a native Athena body and an SVG-backed body
**When** both compile
**Then** both produce the same canonical representation contracts and downstream Cabinet path
**And** duplicate/conflicting metadata and missing geometry references fail closed.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** legacy SVG semantic attributes, XML authority, stale fixtures, generated output, and compatibility adapters are deleted.

### Story 1.5: Prove Portable Standard And Vendor Packages End To End

As an engineering library consumer,
I want one standard package and one vendor/user package to compile reproducibly,
So that I can trust package-backed visual material before physical Cabinet authoring depends on it.

**Acceptance Criteria:**

**Implements:** FR-3, FR-7, FR-10, FR-13..FR-17, FR-41..FR-42.

**Given** package-hierarchical standard and vendor/user fixtures
**When** the repository is compiled from a clean workspace
**Then** both packages are discovered only through `athena.yaml`, their exported definitions resolve, their internal resources are admitted, and zero path rewriting is used.

**Given** two clean compilations of identical source, package material, and validated lock state
**When** structured evidence is compared
**Then** resolved coordinates, resource hashes, snapshot digests, definition ids, and ordered proof are byte-stable.

**Given** a changed source/resource/lock fact or malicious path/SVG fixture
**When** compilation runs
**Then** the relevant identity changes or admission fails with the expected stable diagnostic
**And** no renderer/runtime filesystem fallback occurs.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** touched/adjacent duplicate fixtures, old flat paths, generated evidence, XML logic, and compatibility names are purged.

## Epic 2: Author And Validate Physical Cabinet Reality

Engineers can describe one Cabinet installation in compact Athena source and receive a deterministic,
validated physical model and composed Cabinet drawing whose containment, mounting, dimensions,
orientation, clearance, bounds, and visual transforms are compiler-owned.

### Story 2.1: Give Every Engineering Connection Stable Authored Identity

As an engineer or AI author,
I want every connection to have a stable source alias,
So that physical installation intent and future editing can reference one connection without endpoint or group guesses.

**Acceptance Criteria:**

**Implements:** FR-28, FR-35, FR-41..FR-42.

**Given** grouped and ungrouped connection source
**When** M35 grammar and AST compile it
**Then** every connection requires a source-unit-unique alias and `EngineeringConnectionId` is `(SourceUnitId, connectionAlias)`
**And** group names remain organizational and never identify or merge connections.

**Given** alias-free current grammar, AST, endpoint-plus-ordinal identity, examples, and fixtures
**When** the migration story completes
**Then** all repository sources/tests are migrated to required aliases and the old forms/identity are deleted
**And** there is no compatibility parser or adapter.

**Given** an alias reference in governed source
**When** semantic linking runs
**Then** it resolves exactly one connection alias in the same SourceUnitId
**And** missing, duplicate, group-name, or cross-source references fail with stable source-spanned diagnostics.

**Given** aliased syntax in normal, invalid, and incomplete files
**When** ANTLR4, semantic lowering, formatter, LSP, Tree-sitter, highlighting, and parser parity run
**Then** every frontend and generated artifact reflects the same required alias contract.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** alias-free source, parser branches, generated grammar artifacts, fixtures, ids, and compatibility names are purged.

### Story 2.2: Author Type-Safe Cabinet Installation Source

As an engineer or AI author,
I want a compact typed Cabinet installation syntax inside my system,
So that physical intent is readable, compilable, lintable, and independent from renderer details.

**Acceptance Criteria:**

**Implements:** FR-18..FR-20, FR-23, FR-41..FR-42.

**Given** the normative M35 source surface
**When** a system declares `installation cabinet`, enclosure, surface, rail, duct, channel, terminal group, mount, and route members
**Then** ANTLR4 produces typed source models with spans and `cabinet` is the only accepted installation kind
**And** IR, occurrence, descriptor, snapshot, renderer, pixel, DOM, and transport terms are not public syntax.

**Given** valid, invalid, and incomplete installation fixtures
**When** ANTLR4, Tree-sitter, formatter, LSP diagnostics/completion/tokens, and parser parity run
**Then** all frontends agree on acceptance, recovery, highlighting, and canonical formatting
**And** the existing `examples/m23/parser-parity-proof` corpus is extended rather than duplicated.

**Given** physical intent is parsed
**When** source ownership is inspected
**Then** project Athena source owns authored engineering/installation intent and source spans
**And** no renderer, SVG, package manifest, or generated IR becomes source authority.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** touched/adjacent stale grammar branches, generated parsers, fixtures, docs, and compatibility names are purged.

### Story 2.3: Resolve Validated Physical Installation Contracts

As an engineer,
I want installation constraints resolved into one validated contract,
So that Cabinet fit decisions never depend on visual geometry or unchecked product data.

**Acceptance Criteria:**

**Implements:** FR-23, FR-25..FR-27, FR-41..FR-42.

**Given** project physical facts and existing resolved physical traits
**When** `PhysicalInstallationContractResolver` runs
**Then** project scalar fields override trait fields independently, set fields replace atomically, same-precedence duplicates fail, and every resolved field carries provenance.

**Given** `PhysicalInstallationContractV0`
**When** validation succeeds
**Then** it contains positive width/height/depth, mounting type, non-empty allowed orientations, non-negative top/right/bottom/left clearance, and non-empty compatible container kinds in canonical digest order.

**Given** existing unchecked `PhysicalSize` trait input
**When** any dimension is invalid
**Then** source-spanned diagnostics are emitted before validated M35 values are constructed
**And** no unchecked `PhysicalSize` enters the resolved contract or physical IR.

**Given** a visual Element, SVG body, namespace, or package metadata attempts to supply product or physical truth
**When** contract resolution runs
**Then** that input is rejected or ignored by type boundary
**And** no Engineering Component System, manufacturer/article model, or new package kind is introduced.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** duplicate physical DTOs, inferred geometry facts, stale docs, fixtures, and compatibility code are purged.

### Story 2.4: Compile Typed Physical Cabinet Topology

As an engineer,
I want Cabinet containment and mounting intent compiled into one typed physical model,
So that enclosure, rails, ducts, terminals, and mounted equipment have explicit engineering structure.

**Acceptance Criteria:**

**Implements:** FR-18..FR-20, FR-23, FR-41..FR-42.

**Given** valid installation source and resolved physical contracts
**When** physical lowering runs
**Then** `PhysicalInstallationIR v0` contains typed ids, source provenance, InstallationSpace, Enclosure, MountingSurface, Rail, Duct, RouteChannel, TerminalGroup, MountedOccurrence, route topology, and route intent
**And** it contains no final drawing coordinates, representation selection, anchor coordinates, or route segments.

**Given** v0 topology
**When** containment is validated
**Then** Enclosure is the sole container; surfaces/ducts/terminal groups belong to it; rails belong to surfaces; channels belong to one duct; and occurrences target only surface, rail, or terminal group
**And** generic parent ids, cycles, duplicates, orphans, illegal mount targets, and implicit channels fail closed.

**Given** terminals mounted to a TerminalGroup
**When** IR is canonicalized
**Then** terminals use ordinary MountedOccurrence records and the group orders keys by along-axis position, cross-axis position, then key.

**Given** horizontal and vertical physical infrastructure
**When** frames are derived
**Then** parent-local coordinates and rigid determinant-`+1` Rail bases match AD-5
**And** source never stores pixels or drawing-grid coordinates.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** competing Cabinet topology models, stale DTOs, fixtures, docs, and compatibility code are purged.

### Story 2.5: Evaluate Physical Fit Collision And Clearance

As an engineer,
I want invalid Cabinet placement rejected deterministically,
So that the drawing represents physically coherent installation intent rather than attractive guesses.

**Acceptance Criteria:**

**Implements:** FR-23..FR-27, FR-41..FR-42.

**Given** surfaces, ducts, terminal groups, rails, channels, and occurrences
**When** Physical Constraint Evaluation v0 runs
**Then** every parent object fits its typed parent, every Rail interval fits its surface, every channel rectangle fits its duct interior, and every occurrence fits its enclosure and target-specific rule.

**Given** a mounted occurrence
**When** orientation, depth, mounting, and compatibility are checked
**Then** selected orientation belongs to the allowed set, depth fits authored enclosure depth, mounting type matches the target, and enclosure kind is compatible.

**Given** two occurrence footprints and four-sided clearances
**When** collision/clearance checks run
**Then** positive-area footprint intersections fail, edge contact alone is allowed, either inflated rectangle intersecting the other footprint fails, and required inflated bounds fit their target/enclosure.

**Given** Rail placement
**When** fit is checked
**Then** target-local normal coordinate is zero, along-axis footprint plus leading/trailing clearance fits `[0, length]`, and normal extent is checked against enclosure bounds.

**Given** invalid physical input
**When** evaluation completes
**Then** stable source-spanned diagnostics include subject, measured value, and expected constraint
**And** no optimization, automatic placement, general solver, or AI layout executes.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** renderer-side fit checks, duplicated evaluators, stale tests, and compatibility code are purged.

### Story 2.6: Join Mounted Occurrences To One Visual Transform

As an engineer,
I want each valid physical occurrence joined to exactly one selected representation,
So that physical truth and visual policy meet without either becoming the other's authority.

**Acceptance Criteria:**

**Implements:** FR-18..FR-23, FR-25, FR-41..FR-42.

**Given** physical lowering and Cabinet representation binding for one semantic subject
**When** composition joins inputs
**Then** both sides use the same `InstallationOccurrenceKey` and exactly one physical plus one representation occurrence exists
**And** missing/duplicate sides fail before Graphic Primitive output.

**Given** intrinsic representation bounds and a physical footprint
**When** `CabinetVisualTransform` is built
**Then** the exact seven-stage normalization/scale/centre/rotate/place/target/enclosure-to-drawing order is used once
**And** body, anchors, labels, hotspots, bounds, routes, and trace share the same transform id.

**Given** all four occurrence rotations and horizontal/vertical Rail targets
**When** golden body/anchor tests run
**Then** expected coordinates match exactly and no target frame mirrors geometry.

**Given** visual geometry or physical constraints
**When** ownership is audited
**Then** geometry never supplies footprint/mounting/clearance and physical rules never select an Element.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** duplicate transform math, fallback joins, stale fixtures, and compatibility paths are purged.

### Story 2.7: Compose One Paint-Only Cabinet Document

As an engineer,
I want the Cabinet view composed from validated physical and representation facts,
So that the renderer paints one bounded physical projection instead of arranging a toy graph.

**Acceptance Criteria:**

**Implements:** FR-19, FR-21..FR-22, FR-41..FR-42.

**Given** valid Physical IR and joined representation occurrences
**When** `CabinetCompositionCompiler` runs
**Then** it emits deterministic Graphic Primitives for enclosure, mounting surfaces, rails, ducts, terminal groups, mounted bodies, labels, and frame
**And** Cabinet does not consume schematic LayoutIntent/placement, legacy GeometryDocument, PresentationPrimitive, or direct SVG/box producers.

**Given** composed Cabinet facts
**When** document bounds and viewport are calculated
**Then** bounds derive from required physical/composed content plus governed padding and labels
**And** no renderer hardcoded width, shallow canvas, semantic source-order placement, clipping, or off-canvas required content remains.

**Given** the presentation payload reaches Theia
**When** the Cabinet surface paints
**Then** the frontend performs no physical inference, representation selection, filesystem access, or engineering classification.

**Given** the retained M34 Control Drawing path
**When** Cabinet migration tests run
**Then** Control Drawing regression remains green but gains no physical-Cabinet authority
**And** old Cabinet-specific producers are deleted after zero-caller proof.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** duplicate Cabinet paths, stale render helpers, generated output, XML logic, and compatibility names are purged.

## Epic 3: Route, Select, And Trace The Cabinet Safely

Engineers can route aliased connections through governed Cabinet channels, inspect falsifiable route
proof, select any mounted occurrence, and trace it back to authoritative semantic, installation,
binding, representation, and resource source without mutating graphic state.

### Story 3.1: Compile Route Channels With Exact Lanes And Adjacency

As an engineer,
I want route channels compiled into deterministic topology and lane facts,
So that every conductor path is reproducible before route geometry is drawn.

**Acceptance Criteria:**

**Implements:** FR-28..FR-29, FR-41..FR-42.

**Given** an authored RouteChannel rectangle, orientation, margin `M`, lane count `N`, and cross span `S`
**When** topology lowering runs
**Then** it requires `N > 0` and `U = S - 2M > 0`
**And** lane `i` centre is the reduced exact rational `M + ((2i + 1) * U) / (2N)` with no pre-drawing rounding.

**Given** connections whose route intents traverse one channel
**When** lanes are allocated
**Then** stable connection-id order receives lane indices up to capacity, horizontal order is top-to-bottom, and vertical order is left-to-right
**And** overflow fails instead of searching or overlapping.

**Given** two consecutive authored channels
**When** `RouteChannelTopology` derives adjacency
**Then** exactly one adjacency exists only for same-Duct, interior-disjoint rectangles sharing one axis-aligned segment that remains positive after margin trimming
**And** cross-Duct, corner-only, overlapping, gapped, or multiply intersecting transitions fail.

**Given** odd/non-divisible spans, full capacity, and invalid topology fixtures
**When** golden tests and structured proof run
**Then** rational lane centres, ordering, passable-boundary midpoint, and diagnostics are deterministic.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** inferred channels, floating-point lane variants, alternate topology models, stale fixtures, and compatibility paths are purged.

### Story 3.2: Compile Deterministic Physical Routes And Proof

As an engineer,
I want aliased connections routed through their authored channel sequences and bound anchors,
So that Cabinet conductors are physically contained, inspectable, and fail closed.

**Acceptance Criteria:**

**Implements:** FR-28..FR-31, FR-41..FR-42.

**Given** engineering endpoints, ordered channel ids, channel topology, bound representation anchors, and Cabinet transforms
**When** `CabinetRoutingCompiler` runs
**Then** it emits one `CabinetRouteFact` per stable connection with endpoint bindings, ordered channels, orthogonal segments, source provenance, and intersection proof
**And** existing schematic `routing-model` owns none of these facts.

**Given** channel transitions and endpoint stubs
**When** segment geometry is built
**Then** transitions use the exact passable midpoint projected to assigned lanes, bends are horizontal then vertical, ties use lower X then lower Y, and stubs use the nearest point on first/last lane.

**Given** missing/incompatible anchors, invalid adjacency/capacity, off-channel segments, or non-endpoint body intersections
**When** routing completes
**Then** composition fails with stable diagnostics and no alternate-route search or renderer heuristic runs.

**Given** valid Cabinet routes
**When** structured route proof is emitted
**Then** it reports route count, channel use, endpoint bindings, ordered segments, intersections, off-channel segments, and unbound endpoints
**And** the acceptance fixture proves zero route/body intersections and zero endpoints outside their anchors.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** generic midpoint routes, schematic physical-route leakage, stale route helpers, fixtures, and compatibility paths are purged.

### Story 3.3: Trace Every Graphic Occurrence To Governed Source

As an engineer,
I want every selectable Cabinet occurrence to reveal its authoritative source chain,
So that I and future agents can explain what is shown without reading DOM or labels.

**Acceptance Criteria:**

**Implements:** FR-32, FR-34, FR-41..FR-42.

**Given** composed Cabinet bodies, labels, hotspots, routes, and decorative primitives
**When** `GraphicOccurrenceTraceTable` is built
**Then** each selectable primitive carries one `GraphicOccurrenceId`, decorative primitives are explicitly nonselectable, and table completeness is proven
**And** `GraphicOccurrenceId` remains distinct from `GraphicPrimitiveId`.

**Given** one mounted occurrence
**When** its trace entry is inspected
**Then** ordered typed ids, source spans, and digests identify semantic subject, installation declaration, mounted occurrence, binding rule, representation definition, resource snapshot, and owning declarations
**And** no source bytes, filesystem handles, snapshot objects, DOM ids, or label guesses are transported.

**Given** duplicate, missing, synthetic, or mismatched trace subjects
**When** presentation validation runs
**Then** composition fails with stable diagnostics and no selectable primitive is emitted without complete trace.

**Given** LSP transport
**When** trace payloads are serialized twice
**Then** table version, ordering, ids, spans, and digests are byte-stable.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** duplicate per-primitive traces, DOM/source guesses, stale transport DTOs, and compatibility names are purged.

### Story 3.4: Resolve Selection And Preserve Governed Editing

As an engineer,
I want Cabinet selection to resolve through typed capabilities and governed source mutation contracts,
So that graphic interaction cannot corrupt Athena's source-of-truth model.

**Acceptance Criteria:**

**Implements:** FR-33, FR-35..FR-36, FR-41..FR-42.

**Given** a selected `GraphicOccurrenceId`
**When** the frontend asks LSP for its subject
**Then** typed trace maps through existing `InteractionSubjectResolver` backed by `SemanticCapabilityRegistry`
**And** semantic-id prefix inference, DOM ids, labels, and SVG nodes are not fallback authority.

**Given** future move, representation change, or engineering replacement intent
**When** the governed flow is evaluated
**Then** it follows `SemanticActionIntent -> capability/command translation -> AuthoringIntent -> SemanticAuthoringTransaction -> AuthoringPreview + AuthoringSourceEditEvidence -> compile/lint -> accept/reject -> rerender`
**And** each action targets the authoritative installation, binding/profile, or engineering declaration.

**Given** a UI, renderer, or agent attempts direct mutation
**When** boundary tests run
**Then** SVG DOM, Graphic Primitive IR, Presentation IR, representation occurrences, and renderer state cannot be authoritative mutation targets.

**Given** the Cabinet view is selected and source reveal is invoked
**When** interaction proof runs
**Then** the exact governed source span opens and the resolved capability/subject evidence matches the trace table.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** prefix heuristics, direct graphic mutation hooks, stale selection DTOs, and compatibility paths are purged.

## Epic 4: Demonstrate A Professional Cabinet Product

Customers can open one dedicated M35 project directly in the Cabinet surface and see a dense,
readable, physically contained, package-backed industrial drawing that passes structured authority,
geometry, routing, trace, desktop, narrow, and nonblank-canvas E2E gates.

### Story 4.1: Assemble The Dedicated M35 Cabinet Project

As a customer evaluator,
I want one self-contained M35 Cabinet project with realistic standard and vendor material,
So that I can inspect the complete product path without switching views or assembling fixtures.

**Acceptance Criteria:**

**Implements:** FR-37..FR-38, FR-41..FR-42.

**Given** `examples/m35/physical-installation-cabinet`
**When** repository/package validation runs
**Then** project source, standard package, vendor/user package, resources, manifest, and lock follow the approved hierarchy and authority contracts
**And** the project compiles offline with zero diagnostics.

**Given** the governed sample source
**When** Physical IR and Cabinet composition compile
**Then** the sample includes enclosure, mounting surface, horizontal and vertical Rail proof, ducts/channels, terminal group, mounted controls, labels, aliased connections, routes, native symbols, and one complex SVG-backed vendor Element.

**Given** the Athena IDE opens the sample workspace
**When** the product initializes
**Then** Cabinet is active and the only visible product surface
**And** Documentation, Schematic, Wiring, debug projections, fallback cards, and placeholder authoring controls do not appear.

**Given** package, physical, binding, composition, route, and trace proof
**When** fixture assertions run
**Then** every required occurrence and connection is sourced from governed declarations and no hardcoded presentation mock supplies acceptance facts.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** old M34 Cabinet samples/producers, flat paths, generated snapshots, XML logic, and duplicate fixtures are purged when no longer required for regression.

### Story 4.2: Reach Professional Cabinet Visual Quality

As a customer evaluator,
I want the Cabinet drawing to look like a credible industrial installation,
So that Athena no longer presents its engineering model through a toy-level graph surface.

**Acceptance Criteria:**

**Implements:** FR-21..FR-22, FR-38..FR-40, FR-41..FR-42.

**Given** the approved QET image as visual/domain reference only
**When** the M35 Cabinet is composed and painted
**Then** enclosure containment, mounting surface/Rails, ducts/channels, terminal density, mounted controls, route containment, labels, and drawing frame are visibly coherent
**And** no QET schema, runtime code, copied logic, or imported semantic authority enters Athena.

**Given** desktop and narrow workbench sizes
**When** the Cabinet document is fitted
**Then** required content remains visible and readable with professional density, stable aspect ratio, and intentional spacing
**And** there is no clipping, text overflow, unintended overlap, giant graph card, floating toy box, or wide shallow canvas.

**Given** labels, strokes, terminals, anchors, routes, and complex vendor geometry
**When** renderer output is inspected
**Then** all are painted from Graphic Primitive/Presentation facts with stable dimensions and no frontend inference or hardcoded screenshot background.

**Given** a visual defect requires upstream facts
**When** it is corrected
**Then** the fix is made in source, physical model, representation, composition, or renderer ownership as appropriate
**And** no mock, CSS concealment, or fabricated proof is accepted.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** obsolete graph chrome, fallback visuals, stale CSS, generated output, and compatibility paths are purged.

### Story 4.3: Run Falsifiable Cabinet Product E2E

As a product owner,
I want fresh automated proof of the real IDE and Cabinet renderer,
So that milestone completion is based on observable behavior rather than claims or stale screenshots.

**Acceptance Criteria:**

**Implements:** FR-39..FR-40, FR-41..FR-42.

**Given** a clean rebuilt IDE product and the dedicated M35 sample
**When** Electron E2E runs at 1920x1080, 1280x900, and one narrow viewport
**Then** each run opens the real Cabinet surface, captures a fresh screenshot, and proves nonblank canvas pixels
**And** screenshots visibly satisfy every structural professional Cabinet checklist item.

**Given** compiler/LSP/product structured evidence
**When** the smoke gate evaluates it
**Then** it proves zero diagnostics, fallback components, XML authority, raw SVG/HTML transport, unbound anchors, off-channel routes, required body intersections, missing trace, clipping, overflow, and off-canvas required content.

**Given** a blank, mocked, stale, hidden, or hardcoded result
**When** the E2E verifier runs
**Then** the gate fails even if screenshot files exist
**And** package/resource/physical/route/trace proof must match the rendered sample identities.

**Given** all Gradle and Node verification required by M35
**When** the acceptance sequence runs on Windows
**Then** Gradle commands run strictly sequentially and every process is awaited to completion
**And** commands, timestamps, screenshots, proof payloads, and results are recorded in the story evidence.

**Given** all previous acceptance criteria are green
**When** the mandatory story gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, and three-layer review are recorded
**And** stale screenshots, generated snapshots, temporary workspaces, dead smoke scripts, XML paths, and compatibility names are purged.

### Story 4.4: Freeze M35 And Hand Off The Next EngineeringOS Layer

As the Athena product team,
I want M35 acceptance, cleanup, and lessons recorded after the real product gate passes,
So that M36 starts from proven semantic, representation, physical, routing, and trace boundaries.

**Acceptance Criteria:**

**Implements:** FR-37..FR-42 and all M35 success/counter-metrics.

**Given** all M35 stories and E2E evidence
**When** final AC-to-evidence review runs
**Then** every FR, NFR, architecture AD, visual checklist item, and success/counter-metric maps to current reproducible evidence
**And** no story is marked done from status text alone.

**Given** the unreleased repository
**When** final zero-caller, dependency, fixture, and authority audits run
**Then** obsolete XML/package/render paths, alias-free connections, duplicate scanners, old Cabinet producers, fallback visuals, stale generated evidence, and compatibility adapters are deleted
**And** retained M34 Control Drawing regression ownership is explicit and green.

**Given** full sequential verification and fresh Cabinet E2E pass
**When** the M35 retrospective is written
**Then** it records what worked, what failed, evidence links, unresolved risks, and only genuinely deferred work with owner/reason/revisit condition
**And** it recommends the next milestone from EngineeringOS value rather than more broad rendering polish.

**Given** any blocking acceptance gap
**When** retrospective/freeze is attempted
**Then** M35 remains in progress and the gap becomes an owned story or corrective action
**And** the milestone is not declared complete until fresh verification passes.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge gate runs
**Then** RED/GREEN commands, AC-to-evidence mapping, three-layer review, encoding audit, and repository status review are recorded
**And** only authoritative source, tests, required examples, final screenshots/proof, and explicit deferred records remain.
