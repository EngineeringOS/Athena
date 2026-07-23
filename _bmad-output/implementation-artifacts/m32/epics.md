---
stepsCompleted:
  - m32-requirements-extraction
  - m32-epic-design
  - m32-story-generation
  - m32-final-validation
inputDocuments:
  - draft/layouts/003-presentation-language.md
  - draft/layouts/004-m32-draft.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-22-m32/ARCHITECTURE-SPINE.md
  - _bmad-output/implementation-artifacts/deferred-work.md
---

# Athena M32 - Epic Breakdown

## Overview

M32 establishes Athena's Engineering Package Platform foundation. It separates reusable engineering
knowledge, Presentation Profile policy, Representation Package descriptors, and Graphic Resources;
resolves them through governed local registries and compiler stages; integrates descriptor-backed
resources into M30 representation and presentation flows; and closes the M31 compatibility debts
that would otherwise pollute the new boundary.

## Requirements Inventory

### Functional Requirements

- **FR-1:** Define Engineering Package Descriptor v0 as a platform-owned, frontend-neutral contract.
- **FR-2:** Engineering Package descriptors carry package group/artifact/version identity, concept
  facts, product definitions, templates, parameters, defaults, validation, capabilities, lifecycle,
  docs/resource references, and provenance.
- **FR-3:** Engineering Package descriptors contain no representation primitives, Graphic Resource
  references, anchors, style, viewBox, or Presentation IR.
- **FR-4:** Engineering Package validation rejects invalid identity, version, concept binding,
  parameter schema, and forbidden representation fields.
- **FR-5:** Define Representation Package Descriptor v0 separately from Engineering Packages.
- **FR-6:** Representation Package descriptors carry package identity, standards/profile support,
  descriptor entries, resources, style tokens, variants, previews, and provenance.
- **FR-7:** Representation Descriptor v0 describes resource, bounds, anchors, label slots,
  hotspots, transforms, variants, style tokens, and validation rules.
- **FR-8:** Representation descriptors/resources contain no engineering truth, mutation rules,
  `.athena` syntax, or compiler behavior.
- **FR-9:** One Graphic Resource implementation is sufficient for the customer demo; future
  backends are modeled as deferred-compatible kinds.
- **FR-10:** Descriptor validation catches missing resources, duplicate anchors, missing labels,
  invalid bounds, unsupported variants, and unsafe fallback.
- **FR-11:** Provide deterministic local package registry for Athena-owned and project-local data.
- **FR-12:** Resolver discovers packages only from governed roots in deterministic priority order.
- **FR-13:** Resolver emits selected package, version, descriptor path, dependencies, validation,
  cache identity, and diagnostics.
- **FR-14:** Package resolution is reproducible for identical source, config, registry, and policy.
- **FR-15:** Missing, ambiguous, incompatible, or invalid packages fail closed with diagnostics.
- **FR-16:** Cache identity and invalidation prove package changes do not leave stale renderer data.
- **FR-17:** Define Presentation Profile v0 as independent appearance policy.
- **FR-18:** Presentation Profile carries profile identity, projection contexts, style profile,
  representation standard tags, package compatibility constraints, fallback policy, and provenance.
- **FR-19:** Define Binding Manifest v0 linking Engineering Packages to default/alternative
  Representation Packages and compatible Presentation Profiles without internal coupling.
- **FR-20:** Binding Manifest carries engineering package id/version, concept, default and
  alternative representation packages, profile tags, policy tags, and provenance.
- **FR-21:** Binding Resolver selects representation package, descriptor, variant, anchor mapping,
  label binding, and style profile.
- **FR-22:** Binding Resolver preserves semantic identity and never creates truth from package
  names, profiles, Graphic Resource ids, labels, or coordinates.
- **FR-23:** Same semantic subject resolves to at least two Presentation Profiles or representation
  variants without source change.
- **FR-24:** Binding diagnostics identify failed engineering package, presentation profile,
  representation package, manifest, policy, descriptor, resource, anchor, label slot, or renderer
  authority.
- **FR-25:** Resolved descriptors feed M30 Representation Occurrence creation without breaking
  Presentation IR or composition contracts.
- **FR-26:** Renderer consumes resolved handles and never parses semantic meaning from Graphic
  Resource internals, CSS classes, labels, or file names.
- **FR-27:** Normal component background and hitbox chrome remain transparent; interaction chrome
  remains transient.
- **FR-28:** Canvas viewBox/framing derives from presentation bounds and governed margins.
- **FR-29:** Terminal routing uses descriptor anchors mapped through Binding Resolver; center fallback
  is diagnostic, not success.
- **FR-30:** M27-M31 projection, interaction, authoring, mutation, and visual density do not
  regress.
- **FR-31:** Add `examples/m32/sample-project` using Athena-owned source and package assets.
- **FR-32:** Demo includes at least three product-like Engineering Packages and matching
  Representation Packages.
- **FR-33:** Demo proves Presentation Profile switch without semantic source change.
- **FR-34:** Demo structured proof covers Engineering Resolver, Binding Resolver, Representation
  Resolver, Presentation Profile selection, manifest selection, descriptor validation, anchors,
  labels, routes, bounds, and no fallback.
- **FR-35:** Screenshot evidence is secondary to structured proof.
- **FR-36:** Consolidate or version `AthenaAuthoringSessionRuntimeService` preview compatibility.
- **FR-37:** Align or retire non-Theia CLI/desktop/Compose relationship mutation surfaces.
- **FR-38:** Replace broad `port:` candidate affordance with registry-discovered compatibility
  evidence.
- **FR-39:** Decide and prove M26 display-title sheet-role fallback compatibility.
- **FR-40:** Decide and prove `_reference` occurrence fixture compatibility.
- **FR-41:** Every story ends with deep polish/purge review.
- **FR-42:** Stale artifacts are removed or recorded in the M32 cleanup ledger.

### Non-Functional Requirements

- **NFR-1:** `.athena` remains canonical semantic persistence.
- **NFR-2:** Package/profile/resolver contracts are frontend-independent and transport-safe.
- **NFR-3:** Semantic kernel does not own graphic resource internals.
- **NFR-4:** Theia remains an adapter and owns no package authority.
- **NFR-5:** Package resolution and binding are deterministic.
- **NFR-6:** Invalid package states fail closed with diagnostics.
- **NFR-7:** No QET, vendor tools, internet registries, or remote services at runtime.
- **NFR-8:** M27-M31 invariants do not regress.
- **NFR-9:** Story completion requires AC-to-evidence and polish/purge.
- **NFR-10:** Gradle verification runs sequentially on Windows.
- **NFR-11:** Repository text remains UTF-8; Chinese markdown retains UTF-8 BOM.
- **NFR-12:** Demo assets are Athena-owned or synthetic unless licensing is explicit.

### Additional Requirements

- Package model files should be data-driven and reviewable; exact JSON/YAML choice is an
  implementation decision based on existing tooling.
- QET `.elmt` may be documented as deferred offline importer research only.
- M31 deferred compatibility items are in scope and must be resolved before M32 closeout.

### UX Design Requirements

- Preserve industrial visual density: no visible normal wrappers, duplicate labels, oversized
  canvases, or center-fallback routing.
- Profile switching must be discoverable but not presented as semantic source editing.
- Package diagnostics should identify the authority that failed, not collapse to generic projection
  failure.

### FR Coverage Map

| FR | Epic | Coverage |
| --- | --- | --- |
| FR-1..FR-10 | Epic 1 | Descriptor contracts and validation |
| FR-11..FR-16 | Epic 2 | Registry, resolver, cache, diagnostics |
| FR-17..FR-24 | Epic 3 | Presentation Profile, Binding Manifest, and Binding Resolver |
| FR-25..FR-30 | Epic 4 | Representation, renderer, routing integration |
| FR-31..FR-35 | Epic 5 | Customer demo and structured proof |
| FR-36..FR-42 | Epic 6 | Compatibility cleanup, ledger, closeout |

## Epic List

### Epic 1: Package Descriptor Contracts

Platform developers can define and validate Engineering Package, Representation Package, and
Representation Descriptor v0 contracts without leaking graphics into semantics or engineering
truth into resources.

**FRs covered:** FR-1..FR-10.

### Epic 2: Local Registry, Resolver, And Cache

Athena can discover, validate, resolve, cache, and diagnose package data deterministically from
governed local roots.

**FRs covered:** FR-11..FR-16.

### Epic 3: Presentation Profile And Binding Resolver

Athena can connect engineering packages, presentation profiles, and representation packages through
explicit manifests and resolver-driven mapping while preserving semantic identity.

**FRs covered:** FR-17..FR-24.

### Epic 4: Descriptor-Backed Presentation Integration

Package-resolved descriptors feed M30 representation, composition, routing, Presentation IR, and
Theia rendering without renderer-owned package or semantic authority.

**FRs covered:** FR-25..FR-30.

### Epic 5: M32 Customer Demo And Product Proof

A customer reviewer can open the M32 sample and see professional package-backed engineering
representation with structured evidence and secondary screenshot proof.

**FRs covered:** FR-31..FR-35.

### Epic 6: Compatibility Cleanup And Milestone Closeout

M32 closes M31 deferred compatibility items, removes stale authority paths, records any retained
legacy behavior, and finishes with regression proof.

**FRs covered:** FR-36..FR-42.

### Epic 7: Graph View Product UX And Package Authority Stabilization

Athena stabilizes Graph View as a coherent industrial authoring surface by separating projection
mode taxonomy, sheet navigation, governed create-entity controls, and live package-backed
representation authority.

**FRs covered:** FR-25..FR-35, FR-41, FR-42; M32 follow-up UX debt.

## Epic 1: Package Descriptor Contracts

Platform developers can define and validate Engineering Package, Representation Package, and
Representation Descriptor v0 contracts without leaking graphics into semantics or engineering
truth into resources.

### Story 1.1: Define Engineering Package Descriptor V0

As an Athena platform developer,
I want a frontend-neutral Engineering Package Descriptor contract,
So that reusable product and concept knowledge can be loaded without editing compiler source.

**Requirements:** FR-1..FR-4; NFR-1..NFR-3, NFR-6.

**Acceptance Criteria:**

**Given** an Engineering Package Descriptor
**When** it is parsed and validated
**Then** it carries stable package id, version, kind, concept identity, parameters, defaults,
validation references, relationship capabilities, lifecycle, docs/resource references, and
provenance.

**Given** a descriptor contains Graphic Resource references, anchors, style, viewBox, Presentation
IR, or renderer resource fields
**When** validation runs
**Then** it fails with structured diagnostics naming the forbidden representation field.

**Given** the story implementation is complete
**When** touched and adjacent package model, tests, docs, and fixtures are reviewed
**Then** stale artifacts are removed or ledgered and AC-to-evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 1.2: Define Representation Package Descriptor V0

As an Athena representation package author,
I want a separate Representation Package contract,
So that visual resources can be distributed without becoming engineering truth.

**Requirements:** FR-5, FR-6, FR-8, FR-9; NFR-2, NFR-3, NFR-7.

**Acceptance Criteria:**

**Given** a Representation Package Descriptor
**When** it is parsed and validated
**Then** it carries package id, version, supported profiles, descriptor entries, resource
references, style token references, variants, previews, and provenance.

**Given** a representation package declares engineering source mutation rules, domain truth, or
compiler behavior
**When** validation runs
**Then** it fails with diagnostics naming the semantic-leak authority violation.

**Given** one Graphic Resource backend is implemented first
**When** other future resource kinds appear
**Then** they are represented as explicit unsupported/deferred-compatible diagnostics rather than
silent renderer success.

**Given** the story implementation is complete
**When** package contracts, examples, tests, and documentation are deeply reviewed
**Then** stale artifacts are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 1.3: Define Representation Descriptor Validation

As an Athena renderer-integrator,
I want descriptor-level validation for resources, anchors, labels, bounds, variants, and hotspots,
So that invalid resources never produce trusted projection success.

**Requirements:** FR-7, FR-8, FR-10; NFR-5, NFR-6.

**Acceptance Criteria:**

**Given** a Representation Descriptor
**When** validation runs
**Then** it validates resource id/kind, bounds, anchors, label slots, hotspots, transforms,
variants, style tokens, and validation rules.

**Given** missing resources, duplicate anchors, missing required label slots, invalid bounds, or
unsupported variants
**When** validation runs
**Then** structured diagnostics are emitted before Representation Occurrence creation.

**Given** a descriptor-backed Graphic Resource
**When** tests inspect semantic authority
**Then** Graphic Resource ids, text labels, CSS classes, and file names do not define engineering truth.

**Given** the story implementation is complete
**When** validators, fixtures, edge cases, and docs are reviewed
**Then** stale descriptor experiments are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 2: Local Registry, Resolver, And Cache

Athena can discover, validate, resolve, cache, and diagnose package data deterministically from
governed local roots.

### Story 2.1: Add Governed Local Package Registry Roots

As an Athena project maintainer,
I want explicit local package registry roots,
So that package resolution is reproducible and does not scan arbitrary workspace folders.

**Requirements:** FR-11, FR-12; NFR-5, NFR-7.

**Acceptance Criteria:**

**Given** Athena-owned and project-local package roots
**When** registry discovery runs
**Then** roots are evaluated in deterministic priority order and arbitrary workspace folders are
ignored.

**Given** duplicate package ids are found across roots
**When** resolution runs
**Then** precedence is deterministic or ambiguity is diagnosed according to the registry policy.

**Given** the story implementation is complete
**When** registry paths, docs, samples, and tests are reviewed
**Then** stale ad hoc package path assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 2.2: Implement Package Resolver And Resolution Facts

As an Athena compiler/runtime consumer,
I want package resolution facts,
So that downstream binding can prove exactly which packages and descriptors were used.

**Requirements:** FR-13..FR-15; NFR-5, NFR-6.

**Acceptance Criteria:**

**Given** valid Engineering and Representation packages
**When** resolver runs
**Then** it returns package id, version, descriptor path, dependency list, validation status,
diagnostics, and selected registry root.

**Given** missing, ambiguous, incompatible, or invalid packages
**When** resolver runs
**Then** it returns structured diagnostics and no generic renderer fallback is reported as success.

**Given** identical source, registry content, config, and policy
**When** resolver runs repeatedly
**Then** resolution facts are deterministic.

**Given** the story implementation is complete
**When** resolver logic, diagnostics, tests, and docs are reviewed
**Then** stale direct package lookup paths are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 2.3: Add Package Cache Identity And Invalidation

As an Athena runtime maintainer,
I want package cache identity tied to descriptor, resource, registry, and policy content,
So that package changes cannot leave stale presentation artifacts.

**Requirements:** FR-13, FR-14, FR-16; NFR-5.

**Acceptance Criteria:**

**Given** a resolved package
**When** cache identity is computed
**Then** it includes package id, version, descriptor content identity, resource identity, registry
root, binding policy identity, and active profile.

**Given** descriptor, resource, policy, or profile input changes
**When** projection reruns
**Then** cache identity changes and stale package artifacts are not reused.

**Given** the story implementation is complete
**When** cache behavior, invalidation tests, and adjacent runtime docs are reviewed
**Then** stale cache assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 3: Presentation Profile And Binding Resolver

Athena can connect engineering packages, presentation profiles, and representation packages through
explicit manifests and resolver-driven mapping while preserving semantic identity.

### Story 3.1: Define Presentation Profile V0

As an Athena package platform developer,
I want Presentation Profile to be independent of engineering and representation packages,
So that standards, customer styles, and output contexts can change appearance without changing
engineering truth.

**Requirements:** FR-17, FR-18, FR-23; NFR-2, NFR-3, NFR-5.

**Acceptance Criteria:**

**Given** a Presentation Profile
**When** it is parsed and validated
**Then** it carries profile id, version, intended projection contexts, style profile,
representation standard tags, package compatibility constraints, fallback policy, and provenance.

**Given** a profile contains engineering truth, product parameters, Graphic Resource internals, or
source mutation behavior
**When** validation runs
**Then** it fails with diagnostics naming the authority-boundary violation.

**Given** IEC, ANSI, customer, compact, print, maintenance, training, or theme policies are modeled
**When** package validation runs
**Then** they are profile facts rather than Engineering Package or Representation Package internals.

**Given** the story implementation is complete
**When** profile contracts, fixtures, tests, and docs are reviewed
**Then** stale policy-in-package assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 3.2: Define Binding Manifest V0

As an Athena package integrator,
I want a manifest that links engineering packages, presentation profiles, and representation
package choices,
So that package internals stay separate while compatible profiles remain discoverable.

**Requirements:** FR-19, FR-20; NFR-2, NFR-3.

**Acceptance Criteria:**

**Given** a Binding Manifest
**When** it is validated
**Then** it carries engineering package id/version range, concept identity, default representation
package, alternative representation packages, compatible Presentation Profile tags, policy tags,
and provenance.

**Given** a manifest embeds representation geometry, Graphic Resource internals, semantic compiler
rules, or source mutation behavior
**When** validation runs
**Then** it fails with an authority-boundary diagnostic.

**Given** the story implementation is complete
**When** manifest contracts, examples, and docs are reviewed
**Then** stale coupling assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 3.3: Implement Binding Resolver Selection

As an Athena projection compiler,
I want Binding Resolver to select descriptors from semantic context, package facts, and
Presentation Profile,
So that representation choice is governed, explainable, and swappable.

**Requirements:** FR-21..FR-24; NFR-5, NFR-6.

**Acceptance Criteria:**

**Given** a semantic subject, projection context, engineering package, manifest, Presentation
Profile, active profile, and valid representation packages
**When** Binding Resolver runs
**Then** it selects representation package, descriptor, variant, anchor mapping, label binding, and
style profile.

**Given** the active Presentation Profile changes
**When** Binding Resolver reruns
**Then** the same semantic subject can resolve to a different appearance without changing `.athena`
source.

**Given** Binding Resolver cannot resolve descriptor, anchor, label, package, manifest, or profile
**When** binding runs
**Then** diagnostics name the failed authority and no fallback box is accepted as success.

**Given** the story implementation is complete
**When** resolver logic, tests, docs, and package fixtures are reviewed
**Then** stale hard-coded representation choices are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 3.4: Expose Binding Evidence Through Product Payloads

As an Athena workbench and test consumer,
I want binding evidence in transport-safe payloads,
So that UI and proof tooling can explain package and profile choices without inferring them.

**Requirements:** FR-13, FR-21, FR-24, FR-34; NFR-2, NFR-4.

**Acceptance Criteria:**

**Given** package binding succeeds
**When** LSP/runtime payloads are produced
**Then** they include semantic subject id, engineering package id/version, Presentation Profile id,
representation package id/version, descriptor id, variant, anchor map summary, label binding
summary, resolver stage, and diagnostics.

**Given** Theia receives binding evidence
**When** it renders or inspects a subject
**Then** it displays/uses returned facts and does not infer package choice from file names, Graphic
Resource internals, DOM, or CSS.

**Given** the story implementation is complete
**When** transport payloads, frontend adapters, smoke hooks, and docs are reviewed
**Then** stale inferred-binding code is removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 4: Descriptor-Backed Presentation Integration

Package-resolved descriptors feed M30 representation, composition, routing, Presentation IR, and
Theia rendering without renderer-owned package or semantic authority.

### Story 4.1: Feed Descriptors Into Representation Occurrences

As an Athena representation compiler,
I want resolved descriptors to create M30-compatible Representation Occurrences,
So that package-backed symbols use the existing presentation pipeline.

**Requirements:** FR-25, FR-30; NFR-8.

**Acceptance Criteria:**

**Given** valid package resolution and binding facts
**When** Representation Occurrence creation runs
**Then** occurrences reference descriptor/resource handles, variant, labels, anchors, bounds, and
style profile while preserving semantic subject identity.

**Given** existing M30 native definitions
**When** package-backed definitions are enabled
**Then** existing Presentation IR and composition contracts continue to work or fail with structured
migration diagnostics.

**Given** the story implementation is complete
**When** representation model, compiler, fixtures, and docs are reviewed
**Then** stale native-only assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 4.2: Render Graphic Resources As Descriptor-Backed Resources

As an Athena frontend adapter,
I want Graphic Resources rendered through descriptor handles,
So that the renderer draws professional resources without owning semantic meaning.

**Requirements:** FR-26..FR-28; NFR-4, NFR-8.

**Acceptance Criteria:**

**Given** a descriptor-backed Graphic Resource
**When** Theia renders it
**Then** it uses resolved resource handles, descriptor bounds, anchors, labels, and transient
interaction state only.

**Given** normal component rendering
**When** DOM and screenshot proof are inspected
**Then** hitboxes and backgrounds are transparent, interaction chrome is transient, labels are not
duplicated, and viewBox derives from presentation bounds and governed margins.

**Given** the story implementation is complete
**When** renderer code, CSS, DOM tests, screenshots, and docs are reviewed
**Then** stale wrappers, hard-coded canvases, and resource-semantic assumptions are removed or ledgered.
**And** the AC evidence and mandatory Polish/Purge Gate are complete.

### Story 4.3: Route Through Descriptor Anchors

As an Athena spatial/routing compiler,
I want terminal routes attached to descriptor anchors through binding policy,
So that package-backed drawings do not fall back to component centers.

**Requirements:** FR-24, FR-29, FR-30; NFR-6, NFR-8.

**Acceptance Criteria:**

**Given** a relationship between semantic terminals with mapped descriptor anchors
**When** routing runs
**Then** endpoints use the resolved terminal anchors and route facts name their descriptor anchor
evidence.

**Given** an anchor, descriptor, or label slot is missing
**When** routing or projection runs
**Then** the failure is diagnostic and no center-fallback route is accepted as success.

**Given** the story implementation is complete
**When** spatial, routing, representation, proof, and docs are reviewed
**Then** stale center-fallback assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 5: M32 Customer Demo And Product Proof

A customer reviewer can open the M32 sample and see professional package-backed engineering
representation with structured evidence and secondary screenshot proof.

### Story 5.1: Create M32 Sample Packages And Project

As a customer-demo owner,
I want a package-backed M32 sample project,
So that Athena demonstrates professional product-like representation without proprietary runtime
dependencies.

**Requirements:** FR-31..FR-33; NFR-7, NFR-12.

**Acceptance Criteria:**

**Given** `examples/m32/sample-project`
**When** its source, package descriptors, manifests, resources, and README are inspected
**Then** it uses Athena-owned semantic source and package assets only
**And** it includes at least three product-like engineering packages with matching representation
packages.

**Given** the sample compiles
**When** package profile changes
**Then** at least one semantic subject resolves to a different Presentation Profile without
changing `.athena` source.

**Given** the story implementation is complete
**When** sample assets, names, licensing notes, docs, and adjacent examples are reviewed
**Then** stale demo assets are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 5.2: Add Structured Package Product Smoke

As an Athena maintainer,
I want structured product smoke for package resolution and rendering,
So that M32 completion does not depend on visual guessing.

**Requirements:** FR-34, FR-35; NFR-5, NFR-8, NFR-10.

**Acceptance Criteria:**

**Given** the exact M32 sample
**When** product smoke runs
**Then** it verifies engineering package resolution, representation package resolution, manifest
selection, descriptor validation, anchor mapping, label binding, route anchoring, derived bounds,
profile switching, and no fallback rendering.

**Given** visual evidence is captured
**When** screenshot proof is inspected
**Then** it is human-review evidence only and is backed by structured assertions for every semantic
and package claim.

**Given** the story implementation is complete
**When** smoke scripts, proof payloads, screenshots, and docs are reviewed
**Then** stale proof paths are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 5.3: Polish Professional Demo Layout And Density

As a customer reviewer,
I want the M32 sample to look like a serious industrial engineering document,
So that Athena no longer reads as a toy renderer.

**Requirements:** FR-27, FR-28, FR-30, FR-35; NFR-8.

**Acceptance Criteria:**

**Given** the M32 sample opens in the IDE
**When** the primary sheet renders
**Then** component backgrounds and hitboxes are transparent, no duplicated visible text appears,
viewBox tightly fits resolved content plus governed margins, and control panels do not hide sheet
navigation.

**Given** package-backed elements render
**When** visual review compares them with the professional target direction from
`draft/layouts/003-presentation-language.md`
**Then** primitive IEC-like and complex product-like elements show descriptor-driven anchors,
labels, and compact composition rather than generic rectangles.

**Given** the story implementation is complete
**When** UI, CSS, layout, screenshots, and docs are deeply reviewed
**Then** stale toy-layout artifacts are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 6: Compatibility Cleanup And Milestone Closeout

M32 closes M31 deferred compatibility items, removes stale authority paths, records any retained
legacy behavior, and finishes with regression proof.

### Story 6.1: Consolidate Authoring Preview Compatibility

As an Athena authoring-runtime maintainer,
I want old preview-session compatibility consolidated or explicitly versioned,
So that M32 does not carry hidden M31 transaction authority drift.

**Requirements:** FR-36, FR-41, FR-42; NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** `AthenaAuthoringSessionRuntimeService` preview/session compatibility callers
**When** CodeGraph caller review and runtime tests run
**Then** each caller is migrated into governed Semantic Authoring Transaction runtime or versioned
as a read-only legacy preview API with explicit docs.

**Given** migrated or retained compatibility paths
**When** regression tests run
**Then** M31 transaction behavior remains authoritative and no hidden mutable source path is left.

**Given** the story implementation is complete
**When** authoring runtime, callers, tests, docs, and cleanup ledger are reviewed
**Then** stale preview compatibility artifacts are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 6.2: Align Non-Theia Relationship Mutation Surfaces

As an Athena multi-surface maintainer,
I want CLI, desktop, and Compose relationship mutation surfaces aligned or retired,
So that `SemanticRelationshipIntent` remains the single relationship authoring contract.

**Requirements:** FR-37, FR-41, FR-42; NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** non-Theia surfaces using low-level `AthenaConnectPortsCommand`
**When** migration audit runs
**Then** each surface uses `SemanticRelationshipIntent` or is explicitly retired with tests and
documentation.

**Given** relationship mutation commands are searched
**When** fixed-string and CodeGraph caller checks run
**Then** no unowned mutable relationship path bypasses M28/M31 authority.

**Given** the story implementation is complete
**When** CLI, desktop, Compose, runtime, tests, and docs are reviewed
**Then** stale command paths are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 6.3: Replace Broad Port Candidate Affordance

As an Athena graphical author,
I want relationship candidates derived from registry evidence,
So that Graphical View does not highlight targets by `port:` prefix or node kind.

**Requirements:** FR-38, FR-41, FR-42; NFR-4, NFR-8.

**Acceptance Criteria:**

**Given** a selected source terminal
**When** Graphical View requests candidates
**Then** candidate and rejected target evidence comes from semantic capability/compatibility payloads
before preview.

**Given** frontend relationship UX is inspected
**When** scans and tests run
**Then** no `semanticId.startsWith('port:')` or equivalent broad node-kind gate remains as the
candidate authority.

**Given** the story implementation is complete
**When** interaction model, LSP payloads, frontend UX, tests, and docs are reviewed
**Then** stale broad-affordance code is removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 6.4: Resolve Projection Compatibility Fallbacks And Close M32

As the Athena project owner,
I want legacy projection fallbacks and milestone artifacts closed with evidence,
So that M33 starts from accurate package and projection boundaries.

**Requirements:** FR-39..FR-42; NFR-9..NFR-11.

**Acceptance Criteria:**

**Given** M26 display-title sheet-role fallback
**When** compatibility review runs
**Then** it is removed or explicitly versioned outside M31/M32 typed payload authority with tests
proving current samples do not depend on display-title parsing.

**Given** `_reference` occurrence fixtures
**When** compatibility review runs
**Then** they are removed, renamed, or documented as legacy defensive tests with proof that normal
compiler/runtime/LSP payloads do not emit duplicate visual reference components.

**Given** all M32 stories have evidence
**When** final regression runs sequentially
**Then** relevant package tests, M27-M32 smoke/proof checks, duplicate sprint-key check, and encoding
audit pass or any remaining failure is ledgered with owner and target milestone.

**Given** the milestone closes
**When** retrospectives and sprint status are updated
**Then** every story has AC-to-evidence and polish/purge results, all stale artifacts are removed or
ledgered, and no `.tools` path is staged or committed.
**And** the mandatory Polish/Purge Gate is complete.

## Epic 7: Graph View Product UX And Package Authority Stabilization

Athena stabilizes Graph View as a coherent industrial authoring surface by separating projection
mode taxonomy, sheet navigation, governed create-entity controls, and live package-backed
representation authority.

### Story 7.1: Define Graph View Taxonomy And Toolbar Contract

As an Athena product owner,
I want Graph View modes and controls named by user-facing engineering concepts,
So that the workbench no longer exposes mixed architecture vocabulary such as `cabinet`,
`documentation`, and `schematic` as peer concepts.

**Requirements:** FR-25..FR-30, FR-41, FR-42; NFR-4, NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** Graph View exposes view-mode controls
**When** the taxonomy contract is reviewed
**Then** the contract separates projection families, document sheet navigation, presentation
profile choice, and authoring actions as different UI concepts.

**Given** current labels include `cabinet`, `documentation`, `schematic`, and `Document projection
sheet view`
**When** the story completes
**Then** user-facing labels and test fixtures use product language such as View, Sheet, Profile,
and Create, while internal ids remain transport-safe.

**Given** the story implementation is complete
**When** touched docs, frontend model tests, smoke proof assertions, and cleanup ledger are reviewed
**Then** stale terminology is removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 7.2: Stabilize Sheet Navigation Across Graph View Modes

As a controls engineer,
I want sheet navigation to stay predictable while switching Graph View modes,
So that the sheet dropdown does not appear and disappear unpredictably.

**Requirements:** FR-28, FR-30, FR-34, FR-41, FR-42; NFR-4, NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** a runtime payload contains multiple governed sheets
**When** the active projection mode changes
**Then** sheet navigation remains visible and uses the current runtime sheet selector or the last
valid selector only as explicit compatibility fallback.

**Given** a runtime payload contains only one sheet or no governed sheet facts
**When** Graph View renders
**Then** the UI intentionally hides or disables sheet navigation with structured proof, not by
accidental projection state.

**Given** Electron smoke switches available modes
**When** the M32 sample is opened
**Then** E2E proof records sheet selector visibility, option count, selected sheet, and no
blink/disappear regression.

**Given** the story implementation is complete
**When** model logic, frontend CSS/DOM, product smoke, docs, and stale compatibility gates are
reviewed
**Then** stale M31-only selector assumptions are removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 7.3: Make Create Entity Control Understandable And Usable

As a controls engineer,
I want the add/create control to open a clear, frontmost creation panel,
So that governed semantic entity creation is usable instead of hidden behind architecture wording
or overlapping graph chrome.

**Requirements:** FR-30, FR-34, FR-35, FR-41, FR-42; NFR-4, NFR-8, NFR-9.

**Acceptance Criteria:**

**Given** the Graph View add button is visible
**When** a user hovers or inspects the control
**Then** the label explains the action in product language, not only `Create governed entity`.

**Given** the add button is clicked
**When** the create panel opens
**Then** it is frontmost, within the viewport, not covered by canvas/sheet chrome, keyboard
reachable, closeable, and visually stable at desktop and compact widths.

**Given** no Athena source editor is active
**When** the create panel opens
**Then** browsing concept templates remains possible, while preview/accept clearly require a
governed source context.

**Given** Electron smoke runs
**When** the M32 sample opens
**Then** structured proof verifies panel geometry, frontmost hit target, controls, close behavior,
and screenshot evidence.

**Given** the story implementation is complete
**When** frontend state, CSS, authoring protocol boundaries, E2E proof, and docs are reviewed
**Then** stale overlapping panel behavior is removed or ledgered and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 7.4: Make Package-Backed Representation The Live Graph Authority

As an Athena platform owner,
I want the live Graph View to render from package/profile/binding resolution facts,
So that M32 proves packages are actually used instead of only existing as side proof assets.

**Requirements:** FR-21..FR-29, FR-31..FR-35, FR-41, FR-42; NFR-2..NFR-8.

**Acceptance Criteria:**

**Given** the M32 sample resolves Engineering Packages, Presentation Profiles, Binding Manifests,
Representation Packages, and descriptors
**When** Graph View renders
**Then** rendered representation ids/resources come from package-backed descriptors and no
`athena-industrial-control-v0` native fallback is accepted as M32 success.

**Given** package-backed descriptor anchors exist
**When** routes render
**Then** route endpoints prove descriptor anchor authority through Binding Resolver evidence and
center fallback is diagnostic, not success.

**Given** structured product smoke runs
**When** it inspects Graph View proof
**Then** it asserts engineering package id, presentation profile id, binding manifest id,
representation package id, descriptor id, resource handle, anchor mapping, label binding, and no
native fallback for the M32 sample.

**Given** package facts are unavailable or invalid
**When** Graph View renders
**Then** failure is explicit diagnostic state instead of silently using generic/native success.

**Given** the story implementation is complete
**When** package runtime, graph adapter, presentation payloads, frontend proof, docs, and stale
native fallback paths are reviewed
**Then** retained compatibility is ledgered with owner and AC evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.

### Story 7.5: Final Graph View Product Polish And Purge

As a customer-demo owner,
I want Graph View to read as one coherent industrial product surface,
So that the M32 customer demo is not undermined by chaotic controls, stale labels, or hidden
fallbacks.

**Requirements:** FR-27, FR-28, FR-30, FR-34, FR-35, FR-41, FR-42; NFR-8..NFR-12.

**Acceptance Criteria:**

**Given** Stories 7.1 through 7.4 are complete
**When** the M32 IDE sample opens
**Then** the Graph View toolbar has stable grouped controls, no confusing architecture labels, no
visible normal wrapper chrome, transparent hitboxes/backgrounds, and no duplicated labels.

**Given** Electron E2E runs
**When** it captures structured proof and screenshot
**Then** it verifies view taxonomy, stable sheet navigation, usable create panel, package-backed
representation authority, tight viewBox, route anchors, and no fallback rendering.

**Given** final cleanup runs
**When** source, generated Theia bundles, docs, examples, proof scripts, screenshots, and sprint
artifacts are reviewed
**Then** stale experiments are removed or ledgered, `.tools` is not staged, and AC-to-evidence is
recorded before review.
**And** the mandatory Polish/Purge Gate is complete.

### Story 7.6: Correct Cabinet-First Graph Authoring UX

As a controls engineer,
I want Cabinet to remain the stable primary Graph View and Create Device to complete directly from
that surface,
So that the M32 customer demo behaves like an engineering authoring product instead of exposing
incomplete proof controls.

**Requirements:** FR-27, FR-30, FR-34, FR-35, FR-41, FR-42; NFR-4, NFR-8..NFR-11.

**Acceptance Criteria:**

**Given** a new M32 IDE session
**When** Graph View opens
**Then** `cabinet` is the visible and active primary customer projection
**And** unfinished projection modes remain explicit compatibility surfaces rather than hidden
active state.

**Given** Documentation is activated through a compatibility or programmatic path
**When** sheet and cross-reference navigation is available
**Then** it renders in a contextual navigation region and does not add elongated controls to the
global tool group.

**Given** Graph View is active and no Athena source editor has been opened
**When** the user selects a concept, enters a tag/model, previews, and accepts Create Device
**Then** LSP and Mutation Authority derive the canonical source context, validate the Revision
Guard, persist the backend-authored edit, reproject the new device, and keep Theia free of source
serialization authority.

**Given** Electron E2E runs against a temporary M32 workspace copy
**When** the create transaction completes and the IDE reopens
**Then** structured proof verifies preview eligibility, acceptance, source persistence, projected
semantic identity, and reopen persistence without first opening an Athena editor.

**Given** the story implementation is complete
**When** touched and adjacent source, generated bundles, tests, docs, samples, screenshots, status,
and cleanup ledger are deeply reviewed
**Then** stale documentation-first and shell-only proof assumptions are removed or ledgered,
`.tools` is excluded, and AC-to-evidence is recorded.
**And** the mandatory Polish/Purge Gate is complete.
