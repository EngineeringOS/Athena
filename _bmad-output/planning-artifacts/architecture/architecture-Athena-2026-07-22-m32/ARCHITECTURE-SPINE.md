---
name: Athena M32 Engineering Package Platform Foundation
type: architecture-spine
purpose: build-substrate
altitude: milestone-to-epics
paradigm: package-resolved-engineering-platform
scope: package descriptors, local registry, resolver, binding manifest, binding policy, representation descriptors, renderer integration, demo proof, and M31 compatibility cleanup
status: draft
created: '2026-07-22'
updated: '2026-07-22'
binds:
  - M32 PRD FR-1..FR-42
  - M32 PRD NFR-1..NFR-12
sources:
  - draft/layouts/003-presentation-language.md
  - draft/layouts/004-m32-draft.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-22-m32/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m30/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-21-m31/ARCHITECTURE-SPINE.md
---

# Architecture Spine - Athena M32 Engineering Package Platform Foundation

## Design Paradigm

M32 uses package-resolved engineering platform:

```text
.athena semantic source
  -> Engineering Semantic Model
  -> Engineering Resolver
  -> Engineering Package Descriptor
  -> Presentation Profile
  -> Binding Manifest / Binding Resolver
  -> Representation Resolver
  -> Representation Package Descriptor
  -> Representation Descriptor / Graphic Resource handle
  -> Representation Occurrence
  -> Composition / Spatial / Routing
  -> Presentation IR
  -> Renderer Adapter
```

The engineering source stays semantic. Engineering packages carry reusable knowledge, Presentation
Profiles carry appearance policy, Representation Packages carry descriptor-backed resources, and
renderers draw already-resolved presentation outputs.

## Inherited Invariants

| Inherited | Source | Binds M32 |
| --- | --- | --- |
| Mutation Authority owns accepted source change | M8/M31 architecture | Package selection cannot become a hidden source mutation path. |
| Spatial intent precedes layout and routing | M27 architecture | Descriptor anchors feed routing; routes remain projection facts. |
| Interaction meaning is platform-owned | M29 architecture | Package-driven actions still pass through semantic capability evidence. |
| Representation policy and binding precede paint | M30 architecture | Package descriptors extend binding; renderer remains paint-only. |
| Semantic authoring transaction governs product edits | M31 architecture | Package cleanup does not reintroduce legacy source/edit authorities. |
| Polish and purge is a story gate | M31 AD-15 | M32 stories must remove or ledger stale artifacts before done. |

## Invariants And Rules

### AD-1 - Packages Are Extension Metadata, Not Semantic Source [ADOPTED]

- **Binds:** FR-1..FR-4, NFR-1, NFR-3
- **Prevents:** `.athena` carrying visual/resource package syntax or package files becoming
  canonical engineering truth.
- **Rule:** `.athena` remains canonical semantic persistence. Package descriptors can enrich
  compilation, validation, binding, and projection, but they do not replace or mutate semantic
  source truth.

### AD-2 - Package Authorities Are Separated By Kind [ADOPTED]

- **Binds:** FR-1..FR-10, FR-17, FR-18, NFR-2, NFR-3
- **Prevents:** product data and graphics becoming one CAD macro object.
- **Rule:** Engineering Packages own product/concept facts and contain no graphic resources.
  Presentation Profiles own appearance policy and contain no engineering truth or graphic
  resources. Representation Packages own view/resource contracts and contain no engineering truth
  or source mutation rules.

### AD-3 - Representation Descriptor Is The Resource Contract [ADOPTED]

- **Binds:** FR-5..FR-10, FR-25..FR-29
- **Prevents:** raw Graphic Resources carrying hidden platform meaning.
- **Rule:** Renderable resources are addressed through Representation Descriptors containing
  bounds, anchors, label slots, hotspots, variants, transforms, style tokens, and resource handles.
  Graphic Resource ids, text, CSS classes, and file names are never semantic authority.

### AD-4 - Presentation Profile Is Independent Policy [ADOPTED]

- **Binds:** FR-17, FR-18, FR-21, FR-23
- **Prevents:** standards, customer styles, print modes, or themes being hard-coded into
  engineering packages or representation packages.
- **Rule:** Presentation Profiles describe appearance policy for IEC, ANSI, customer, print,
  maintenance, training, compact, and theme contexts. They own neither product facts nor Graphic
  Resources.

### AD-5 - Binding Manifest Is The Compatibility Bridge [ADOPTED]

- **Binds:** FR-19, FR-20, FR-21
- **Prevents:** direct coupling between engineering package internals and representation package
  internals.
- **Rule:** A Binding Manifest may name compatible representation defaults, alternatives, profile
  tags, version ranges, and provenance for an Engineering Package. It may not embed representation
  geometry or semantic compiler behavior.

### AD-6 - Binding Resolver Owns Selection And Mapping [ADOPTED]

- **Binds:** FR-21..FR-24
- **Prevents:** Theia, renderer code, package names, profiles, or Graphic Resource data deciding
  which representation a semantic subject uses.
- **Rule:** Binding Resolver consumes Engineering Resolver output, Presentation Profile, Binding
  Manifest, Representation Resolver output, semantic subject, and projection context to select
  descriptor, variant, anchor mapping, label binding, and style profile.

### AD-7 - Three Resolver Responsibilities Stay Separate [ADOPTED]

- **Binds:** FR-11..FR-24, NFR-5, NFR-6
- **Prevents:** one resolver becoming a monolithic package compiler that invalidates too much or
  owns conflicting truth.
- **Rule:** Engineering Resolver selects/validates engineering packages. Binding Resolver selects
  compatible profile and representation binding facts. Representation Resolver selects/validates
  Representation Packages, descriptors, and Graphic Resource handles.

### AD-8 - Local Registry Discovery Is Governed And Deterministic [ADOPTED]

- **Binds:** FR-11..FR-16, NFR-5, NFR-6
- **Prevents:** arbitrary workspace scans, hidden package precedence, and non-reproducible demo
  output.
- **Rule:** M32 resolver searches only explicit registry roots in deterministic priority order,
  returns resolution facts, and fails closed for missing, ambiguous, invalid, or incompatible
  packages.

### AD-9 - Package Cache Identity Is Content And Policy Sensitive [ADOPTED]

- **Binds:** FR-13, FR-14, FR-16
- **Prevents:** stale package resources surviving descriptor or policy changes.
- **Rule:** Cache identity includes package id, version, descriptor content digest, resource digest
  or declared resource identity, registry root, binding policy identity, and active profile.

### AD-10 - Renderer Consumes Resolved Handles Only [ADOPTED]

- **Binds:** FR-25..FR-30, NFR-4, NFR-8
- **Prevents:** renderer becoming package resolver, symbol selector, or semantic parser.
- **Rule:** Renderer consumes Presentation IR plus resolved descriptor/resource handles and
  transient interaction state. It never resolves packages, infers engineering type, chooses
  variants, or reads package manifests.

### AD-11 - Routing Uses Descriptor Anchors Through Policy [ADOPTED]

- **Binds:** FR-21, FR-24, FR-29
- **Prevents:** center fallback returning under package-resolved representation.
- **Rule:** Terminal routes attach to policy-mapped descriptor anchors. Missing anchor, slot,
  descriptor, or package facts produce structured diagnostics and block success claims.

### AD-12 - Package Validation Runs Before Projection Success [ADOPTED]

- **Binds:** FR-4, FR-10, FR-15, FR-24, FR-34
- **Prevents:** invalid descriptors producing visually plausible but ungoverned drawings.
- **Rule:** Package validation precedes representation occurrence creation. Projection success
  requires valid engineering package, manifest/policy, representation package, descriptor, resource
  availability, anchors, and label slots.

### AD-13 - M32 Demo Uses Athena-Owned Package Assets [ADOPTED]

- **Binds:** FR-31..FR-35, NFR-7, NFR-12
- **Prevents:** customer demo depending on proprietary vendor assets or external tools.
- **Rule:** `examples/m32/sample-project` uses Athena-owned or synthetic product-like packages. Any
  real-vendor naming or imported asset requires explicit licensing notes and is not assumed.

### AD-14 - QET Is Research/Input, Not Runtime [ADOPTED]

- **Binds:** Non-goals, NFR-7
- **Prevents:** Athena coupling to QET `.elmt` semantics before its own descriptor boundary is
  stable.
- **Rule:** QET `.elmt` work is deferred offline importer research that targets Athena
  Representation Descriptors after validation. No runtime QET dependency and no direct `.elmt` to
  `.athena` product path are introduced in M32.

### AD-15 - Compatibility Debt Must Be Closed At The Owning Boundary [ADOPTED]

- **Binds:** FR-36..FR-40
- **Prevents:** new package platform hiding old M31 authority leaks.
- **Rule:** M31 deferred items are migrated, removed, or explicitly versioned in the boundary that
  owns them: authoring runtime preview API, non-Theia relationship surfaces, interaction capability
  candidates, sheet-role compatibility, and `_reference` fixtures.

### AD-16 - Structured Proof Is Package Acceptance Authority [ADOPTED]

- **Binds:** FR-34, FR-35, FR-41, FR-42, NFR-9
- **Prevents:** screenshot-only claims about package correctness or professional rendering.
- **Rule:** Package resolver, binding, descriptor validation, occurrence, routing, bounds, visual
  density, and compatibility cleanup are proven by structured evidence. Screenshots remain
  secondary human-review evidence.

### AD-17 - Polish And Purge Is A Story Gate [ADOPTED]

- **Binds:** FR-41, FR-42, NFR-9
- **Prevents:** dead package experiments, stale docs, duplicate fixtures, or abandoned compatibility
  shims accumulating during M32.
- **Rule:** Every story's final acceptance criterion audits touched and adjacent artifacts. A story
  cannot move to done until stale items are removed or recorded with owner, reason, target
  milestone, and verification.

## Dependency Direction

```text
Project / package registry configuration
  -> Package model contracts
  -> Engineering Resolver and validator
  -> Presentation Profile
  -> Binding manifest / Binding Resolver
  -> Representation Resolver and validator
  -> Representation model integration
  -> Composition / spatial / routing
  -> Presentation IR
  -> Theia renderer adapter
```

No downstream layer may select upstream package truth. No renderer resource may define engineering
truth.

## Consistency Conventions

| Concern | Convention |
| --- | --- |
| Package id | Reverse-domain-like stable id plus semantic package kind; no path-derived identity. |
| Version | Semver-compatible string for v0; resolver treats invalid version as diagnostic. |
| Registry roots | Explicit local roots only, ordered by project config and Athena-owned defaults. |
| Descriptor files | Data-driven package assets; file format may be JSON or YAML but contracts stay frontend-neutral. |
| Presentation profile | Independent policy artifact; no engineering truth and no Graphic Resource internals. |
| Resource authority | Descriptor-declared anchors, label slots, and bounds are trusted after validation; raw resource internals are not semantic. |
| Binding diagnostics | Include authority, package id/version, Presentation Profile, subject id, and failed contract. |
| Cache identity | Include descriptor/resource/policy content identity and registry root. |
| Evidence | Each story maps acceptance criteria to fresh tests, proof payloads, scans, or screenshots. |
| Cleanup | Remove stale artifacts or add a complete cleanup-ledger entry before done. |

## Structural Seed

```text
kernel/
  package-model/           # package ids, catalogs, profiles, descriptors, manifests, diagnostics
  representation-model/    # existing M30 contracts extended with descriptor-backed occurrences
  interaction-model/       # capability evidence consumed by M32 candidate cleanup
  authoring-model/         # M31 transaction compatibility cleanup contracts

runtime/
  package-runtime/         # engineering/binding/representation resolvers, validation, cache identity
  authoring-runtime/       # preview-session compatibility consolidation

compiler/
  binding-policy/          # semantic + engineering package + profile + representation -> occurrence facts

ide/
  lsp/                     # package/resolution proof payload transport
  theia-frontend/          # adapter-only rendering, profile switch, candidate evidence UI

examples/
  m32/sample-project/
  packages/m32/            # Athena-owned engineering and representation packages

_bmad-output/
  implementation-artifacts/m32/
```

Exact modules and file splits follow existing repository structure after inspection. The dependency
direction and authority rules above are binding.

## Capability To Architecture Map

| Capability | Lives in | Governed by |
| --- | --- | --- |
| Engineering package/catalog contract | `kernel/package-model` seed | AD-1, AD-2 |
| Presentation Profile contract | package model / binding policy seed | AD-2, AD-4 |
| Representation descriptor contract | `kernel/package-model` or existing representation model extension | AD-2, AD-3 |
| Local registry and resolvers | runtime package service | AD-7, AD-8, AD-9 |
| Binding manifest and Binding Resolver | compiler/runtime binding layer | AD-5, AD-6 |
| Descriptor-backed rendering | representation model + Presentation IR + Theia adapter | AD-3, AD-10 |
| Anchored routing | binding resolver + spatial/routing layers | AD-11 |
| Demo package set | examples and package assets | AD-12, AD-13 |
| M31 cleanup | owning runtime/frontend/projection boundaries | AD-15 |
| Product proof and cleanup | tests, smoke, sprint artifacts, cleanup ledger | AD-16, AD-17 |

## Deferred

| Deferred | Reason |
| --- | --- |
| Internet package registry and publishing | Requires trust, security, signing, auth, lifecycle, and update policy. |
| Full Engineering Concept Library | M32 proves package mechanics; M33 can expand reusable semantic templates. |
| Standards/domain compliance engine | M32 carries policy tags and validation hooks only. |
| Semantic Agent Runtime | Agents should consume stable package and authoring contracts after M32. |
| Full QET importer | Descriptor language must stabilize before importer fidelity work. |
| Symbol editor | Data-driven descriptor validation should exist before editor UX. |
| Additional Graphic Resource backends | M32 models resource kind compatibility but implements one concrete resource backend first. |
| User-global package cache | M32 starts with governed local registry roots. |
