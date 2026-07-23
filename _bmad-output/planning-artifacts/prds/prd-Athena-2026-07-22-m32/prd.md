---
title: Athena M32 - Engineering Package Platform Foundation
status: draft
created: '2026-07-22'
updated: '2026-07-22'
---

# Athena M32 PRD - Engineering Package Platform Foundation

## Executive Summary

M32 turns Athena's M30 representation proof and M31 authoring workflow into an extensible
EngineeringOS package platform. The milestone introduces governed engineering catalogs,
presentation profiles, representation packages, local package resolution, binding resolution,
validation, and a customer-demo package set that proves engineering knowledge, presentation policy,
and graphic resources can evolve independently.

M32 is not a symbol-count milestone. It is the foundation that lets Athena consume professional
engineering and representation assets without making `.athena`, the semantic compiler, Graphic
Resources, or Theia become the authority for the wrong layer.

The M32 thesis:

```text
Engineering Language describes engineering truth.
Engineering Packages describe reusable product and concept knowledge.
Presentation Profiles describe how engineering should appear for IEC, ANSI, customer, print,
maintenance, training, compact, and theme contexts.
Representation Packages describe graphic resources through descriptors.
Binding Resolver connects semantic context, packages, and profiles to resolved representation.
Presentation IR and renderers consume resolved occurrences only.
```

## Product Thesis

M30 made Athena's renderer look credible for a small native symbol set. M31 proved governed
engineering model authoring. M32 must make that visual and engineering knowledge extensible:
vendors, standards, customers, and Athena-owned examples should be able to contribute package data
without modifying the semantic kernel or hand-coding renderer logic.

The correct data flow is:

```text
.athena semantic source
  -> Engineering Semantic Model
  -> Engineering Package Resolver
  -> Engineering Package Descriptor
  -> Presentation Profile and Binding Resolver
  -> Representation Package Resolver
  -> Representation Descriptor and resources
  -> Representation Occurrence
  -> Presentation IR
  -> Renderer
```

Graphic Resources are rendering resources. They are not semantic truth and they do not know what a
motor, PLC, terminal, signal, or relationship means.

## Users And Journeys

### Target Users

The primary user is a controls engineer or solution engineer preparing a customer demonstration
with credible product-like engineering objects and professional drawing representation. Secondary
users are Athena platform developers building reusable packages and future vendor/customer
integrators who need a stable package boundary.

### Jobs To Be Done

- Add reusable engineering product knowledge without editing compiler source.
- Add or swap professional representation assets without changing semantic source.
- Bind the same engineering concept to different representation standards or customer styles.
- Validate package descriptors before they reach the renderer.
- Demo a professional, non-toy drawing backed by package facts and governed authoring.
- Close known M31 compatibility paths before the package boundary hides stale authority leaks.

### User Journeys

**UJ-1 - Maya opens a packaged customer demo.** Maya opens the M32 sample project and sees a
professional rolling-shutter/control document using package-resolved engineering products and
representation descriptors. The drawing uses transparent normal chrome, derived bounds, stable
anchors, label slots, and no generic fallback boxes.

**UJ-2 - Maya switches presentation profile without changing engineering truth.** Maya changes the
active profile from the default IEC-style presentation to a compact customer profile. The same
semantic model resolves through a different presentation policy and representation package, then
recompiles into the same canonical engineering identities with different appearance.

**UJ-3 - A package author validates a descriptor.** A developer adds a local Engineering Package
and Representation Package. The resolver validates manifest identity, version, required anchors,
label slots, resource references, variants, and policy compatibility before the package can be used.

**UJ-4 - A maintainer audits old authoring surfaces.** A maintainer verifies M31-era compatibility
paths are either migrated to the governed transaction/relationship contracts or explicitly
versioned as legacy read-only APIs with evidence.

## Glossary

- **Engineering Package:** A reusable, versioned package of engineering product or concept facts.
  It may contain concept binding, parameter schema, defaults, validation rules, product metadata,
  datasheet references, lifecycle metadata, and relationship capability declarations. It contains
  no drawing primitives, Graphic Resource paths, anchors, styles, or renderer resources.
- **Engineering Catalog:** A package family containing product definitions, concept definitions,
  templates, defaults, rules, parameter schemas, lifecycle metadata, and documentation. M32 models
  the package contract so future vendor or standards catalogs can scale by group, artifact, and
  version.
- **Presentation Profile:** A policy package or package-owned policy artifact that describes how
  engineering knowledge should appear in a context such as IEC, ANSI, customer standard, print,
  maintenance, training, compact view, or theme. It owns neither engineering truth nor graphic
  resources.
- **Representation Package:** A reusable, versioned package of view-layer descriptors and
  resources. It may contain Representation Descriptors, Graphic Resources, anchors, label slots,
  hotspots, variants, style tokens, previews, and validation metadata. It contains no engineering
  truth.
- **Representation Descriptor:** A platform-owned description of how a visual resource may be used:
  resource references, resource anchors, label slots, hotspots, bounds, variants, transforms, and
  style tokens. It is not `.athena` source and not a raw Graphic Resource.
- **Graphic Resource:** A renderer-consumable resource abstraction such as vector, bitmap, canvas,
  document, or future 3D media. It is a black box except for descriptor-declared resource anchors,
  slots, and bounds.
- **Binding Manifest:** A package-level bridge that declares allowed default and alternative
  representation packages for an engineering package without coupling their internals.
- **Binding Resolver:** The compiler/runtime stage that consumes Engineering Resolver output,
  Presentation Profile, Binding Manifest, Representation Resolver output, and projection context to
  produce resolved representation binding facts.
- **Binding Policy:** The rule set used by the Binding Resolver to select an engineering package,
  presentation profile, representation package,
  descriptor, variant, anchor mapping, and style profile for a semantic subject and projection
  context.
- **Package Resolver:** The runtime/compiler service that discovers, validates, versions, caches,
  and resolves local package descriptors.
- **Local Package Registry:** The M32 file-system registry for project-local and Athena-owned
  package data. It is not an internet marketplace.

## Goals

- Establish Engineering Package Descriptor v0.
- Establish Representation Package Descriptor v0 and Representation Descriptor v0.
- Establish Presentation Profile v0 as the policy layer between engineering knowledge and
  representation resources.
- Add a deterministic local Package Resolver and registry profile for Athena-owned packages.
- Add Binding Manifest, Binding Resolver, and Binding Policy v0 to map semantic concepts and
  profiles to representation choices.
- Keep `.athena` source semantic-only; do not add visual primitives, QET references, or Graphic
  Resource references to the language.
- Treat Graphic Resources as black-box renderer resources accessed through descriptors.
- Validate package identity, version, dependencies, resources, anchors, labels, variants, and policy
  compatibility.
- Integrate resolved package output with M30 Representation Occurrence, composition, and
  Presentation IR.
- Add one customer-demo package set that proves product-like engineering knowledge and swappable
  representation resources.
- Retire or version M31 deferred compatibility paths with evidence.
- End every story with AC-to-evidence mapping and polish/purge.

## Non-Goals

- No internet package registry, marketplace, authentication, publishing workflow, or remote update
  protocol.
- No QET `.elmt` runtime dependency and no QET data model authority.
- No direct `.elmt` to `.athena` conversion as a product feature.
- No new `.athena` syntax for selecting symbols, Graphic Resource files, coordinates, anchors, or package
  resources.
- No full IEC, ANSI, EPLAN, QET, AutoCAD Electrical, or vendor-library parity.
- No symbol editor, freehand CAD drawing, macro editor, or geometry database.
- No Semantic Agent Runtime; M32 keeps agent consumption deferred.
- No full Engineering Concept Library; M32 may include only sample packages required to prove the
  platform boundary.
- No PDF/print/revision release pipeline.
- No runtime dependency on real vendor software, network services, or trademarked external package
  feeds.

## Functional Requirements

### Feature 1 - Engineering Package Descriptor

**FR-1:** M32 shall define Engineering Package Descriptor v0 as a platform-owned, frontend-neutral
contract.

**FR-2:** Engineering Package Descriptor v0 shall carry package id, group/artifact/version identity,
package kind, concept identity, product definitions, concept definitions, templates, parameter
schema, defaults, validation rule references, relationship capability declarations, lifecycle
metadata, documentation/resource references, and provenance.

**FR-3:** Engineering Package descriptors shall not contain representation primitives, Graphic
Resource references, anchor geometry, label coordinates, renderer style, viewBox, or Presentation
IR.

**FR-4:** Engineering Package validation shall reject missing identity, invalid version,
unsupported concept binding, invalid parameter schema, and forbidden representation fields with
structured diagnostics.

### Feature 2 - Representation Package And Descriptor

**FR-5:** M32 shall define Representation Package Descriptor v0 as a platform-owned,
frontend-neutral contract separate from Engineering Package Descriptor.

**FR-6:** Representation Package Descriptor v0 shall carry package id, version, supported standards
or profiles, descriptor entries, resource references, style token references, variants, previews,
and provenance.

**FR-7:** Representation Descriptor v0 shall describe resource id, resource kind, resource bounds,
anchors, label slots, hotspots, supported transforms, variants, style tokens, and validation rules.

**FR-8:** Representation descriptors and graphic resources shall not contain engineering type truth,
relationship truth, source mutation rules, `.athena` syntax, or domain-specific compiler behavior.

**FR-9:** M32 shall support one Graphic Resource implementation sufficient for the customer demo
and shall model future renderer backends as explicit deferred-compatible resource kinds without
making any concrete resource format a semantic concept.

**FR-10:** Descriptor validation shall catch missing resources, duplicate anchors, missing required
label slots, invalid bounds, unsupported variants, and unsafe fallback conditions before rendering.

### Feature 3 - Local Package Registry And Resolver

**FR-11:** M32 shall provide a deterministic local package registry for Athena-owned and
project-local package data.

**FR-12:** The Package Resolver shall discover packages from governed registry roots in deterministic
priority order and shall not search arbitrary workspace folders.

**FR-13:** The resolver shall return structured package resolution facts: selected package,
version, descriptor path, dependency list, validation status, cache identity, and diagnostics.

**FR-14:** Resolution shall be reproducible for identical source, project configuration, package
registry content, and policy input.

**FR-15:** Missing, ambiguous, incompatible, or invalid packages shall produce structured
diagnostics and shall not fall back to generic renderer boxes as success.

**FR-16:** M32 shall include cache identity and invalidation rules sufficient to prove package
changes re-resolve without stale renderer artifacts.

### Feature 4 - Presentation Profile, Binding Manifest, And Binding Resolver

**FR-17:** M32 shall define Presentation Profile v0 as an independent policy artifact for IEC,
ANSI, customer, print, maintenance, training, compact, and theme-oriented appearance choices.

**FR-18:** Presentation Profile v0 shall carry profile id, version, intended projection contexts,
style profile, representation standard tags, package compatibility constraints, fallback policy,
and provenance.

**FR-19:** M32 shall define Binding Manifest v0 to link an Engineering Package to default and
alternative Representation Packages and compatible Presentation Profiles without coupling package
internals.

**FR-20:** Binding Manifest v0 shall carry engineering package id/version range, concept identity,
default representation package, alternative representation packages, compatible profile tags,
policy tags, and provenance.

**FR-21:** Binding Resolver v0 shall select representation package, descriptor, variant, anchor
mapping, label binding, and style profile from semantic subject, projection context, engineering
package facts, Binding Manifest, Presentation Profile, active profile, and package validation facts.

**FR-22:** Binding Resolver shall preserve semantic identity and shall not create new engineering
truth from presentation profile identity, representation package identity, Graphic Resource ids,
labels, or coordinates.

**FR-23:** The same semantic subject shall be able to resolve to at least two Presentation Profiles
or representation variants without changing `.athena` source.

**FR-24:** Binding diagnostics shall identify the failed authority: engineering package,
presentation profile, representation package, manifest, binding policy, descriptor, resource,
anchor, label slot, or renderer.

### Feature 5 - Representation Platform Integration

**FR-25:** M32 resolved descriptors shall feed M30 Representation Occurrence creation without
breaking existing M30 Presentation IR and composition contracts.

**FR-26:** Renderer integration shall consume resolved descriptor/resource handles and shall not
parse semantic meaning from Graphic Resource internals, DOM, CSS classes, raw text labels, or file
names.

**FR-27:** Normal component background and hitbox chrome shall remain transparent; selection,
hover, preview, and DnD chrome shall remain transient.

**FR-28:** Canvas viewBox and framing shall remain derived from resolved presentation bounds and
governed margins, not package resource dimensions alone or hard-coded sample constants.

**FR-29:** Terminal routing shall use descriptor-declared anchors mapped through Binding Resolver;
center fallback shall remain a structured diagnostic rather than accepted routing.

**FR-30:** Existing M27-M31 projection, interaction, authoring, and mutation behavior shall not
regress when package-resolved representations are active.

### Feature 6 - Customer Demo Package Set

**FR-31:** M32 shall include `examples/m32/sample-project` with Athena-owned source and package
assets only.

**FR-32:** The M32 demo shall include at least three product-like Engineering Packages and matching
Representation Packages sufficient for a professional rolling-shutter/control customer workflow.

**FR-33:** The demo shall prove engineering package identity, Presentation Profile identity, and
representation package identity are distinct by switching at least one active Presentation Profile
without semantic source change.

**FR-34:** The demo shall include structured proof for Engineering Resolver output, Binding Resolver
output, Representation Resolver output, Presentation Profile selection, manifest selection,
descriptor validation, anchor mapping, label binding, route anchoring, viewBox bounds, and no
fallback rendering.

**FR-35:** Screenshot evidence shall support human visual review, but structured package and
projection proof remains the acceptance authority.

### Feature 7 - Compatibility Cleanup And Closeout

**FR-36:** M32 shall consolidate `AthenaAuthoringSessionRuntimeService` preview-session
compatibility into the governed Semantic Authoring Transaction runtime or explicitly version it as
a read-only legacy preview API.

**FR-37:** M32 shall align non-Theia CLI, desktop, and Compose relationship mutation surfaces with
`SemanticRelationshipIntent` or explicitly retire them.

**FR-38:** M32 shall replace Graphical View's broad `port:` candidate affordance with
registry-discovered compatible and rejected target evidence before preview.

**FR-39:** M32 shall keep or remove the legacy M26 display-title sheet-role fallback through an
explicit compatibility decision and proof that M31/M32 payloads do not depend on it.

**FR-40:** M32 shall keep, rename, or remove `_reference` occurrence legacy fixtures through an
explicit compatibility decision and proof that normal compiler/runtime/LSP payloads do not emit
duplicate visual reference components.

**FR-41:** Every story shall end with a deep polish-and-purge review of touched and adjacent code,
tests, samples, documentation, compatibility paths, and design claims.

**FR-42:** Stale artifacts exposed by a story shall be removed or recorded in the M32 cleanup ledger
with owner, reason, target milestone, and verification.

## Non-Functional Requirements

**NFR-1:** `.athena` remains canonical semantic persistence; package data is extension metadata,
not source truth.

**NFR-2:** Engineering Packages, Presentation Profiles, Representation Packages, descriptors,
manifests, resolvers, and policies shall be frontend-independent and transport-safe.

**NFR-3:** The semantic kernel shall not own graphic resource internals. Kernel contracts may define
package model shapes; package loading and resources live outside semantic truth.

**NFR-4:** Theia remains an adapter and shall not resolve packages, infer package binding, parse
resource semantics, or construct authoritative package decisions.

**NFR-5:** Package resolution and binding shall be deterministic.

**NFR-6:** Invalid package states shall fail closed with structured diagnostics.

**NFR-7:** M32 shall not introduce QET, vendor tools, internet registries, or remote services as
runtime dependencies.

**NFR-8:** M27-M31 layout, representation, interaction, mutation, authoring transaction, and visual
density invariants shall not regress.

**NFR-9:** Story completion requires acceptance-criterion-to-evidence mapping and the mandatory
polish-and-purge gate.

**NFR-10:** Gradle verification commands shall run strictly sequentially on Windows.

**NFR-11:** Repository text shall remain UTF-8, and `*.zh-CN.md` shall retain UTF-8 with BOM.

**NFR-12:** Package examples shall avoid runtime dependency on real vendor proprietary assets. Any
vendor-like names in samples shall be synthetic or clearly Athena-owned unless licensing is
explicitly documented.

## M32 Core Acceptance Scope

The customer-facing milestone is complete only when all six outcomes work together:

1. Resolve one Engineering Package from a local registry.
2. Resolve one compatible Presentation Profile, Representation Package, and descriptor through a
   Binding Manifest and Binding Resolver.
3. Render at least three product-like packaged engineering objects in a professional M32 sample.
4. Switch at least one Presentation Profile without changing `.athena` semantic source.
5. Prove descriptor anchors, label slots, route anchoring, derived bounds, and no fallback rendering
   structurally.
6. Close M31 deferred compatibility items by migration, removal, or explicit versioned legacy
   ownership.

## Success Metrics

- **SM-1:** Package resolver proof shows deterministic discovery and validation of Engineering and
  Representation Packages from governed local registry roots.
- **SM-2:** Binding proof shows one semantic subject resolving through two Presentation Profiles
  without source change.
- **SM-3:** Descriptor proof validates resource references, anchors, label slots, bounds, variants,
  and style tokens before rendering.
- **SM-4:** M32 sample opens in Athena IDE with professional packaged objects and no visible normal
  wrappers, duplicate labels, off-screen duplicates, center-fallback routes, or hard-coded oversized
  viewBox.
- **SM-5:** Renderer proof shows Graphic Resources are consumed as descriptor-backed resources and
  do not provide semantic authority.
- **SM-6:** M31 deferred compatibility items are migrated, removed, or versioned with owner and
  verification evidence.
- **SM-7:** M27-M31 regression smokes and package-specific tests pass sequentially, and encoding
  audit passes after repository text changes.
- **SM-8:** Every completed story includes AC-to-evidence mapping and polish/purge outcome.

### Counter-Metrics

- **SM-C1:** Do not optimize for symbol quantity; M32 succeeds through package boundaries and one
  credible customer sample.
- **SM-C2:** Do not make package selection a `.athena` visual syntax feature.
- **SM-C3:** Do not make Graphic Resource ids, labels, or geometry carry engineering meaning.
- **SM-C4:** Do not hide compatibility debt behind the new resolver layer.

## Acceptance Criteria

- Dedicated M32 PRD, addendum, architecture, epics, sprint, story, retrospective, and cleanup
  artifacts follow the established BMAD milestone structure.
- Engineering Package Descriptor, Presentation Profile, Representation Package Descriptor,
  Representation Descriptor, Binding Manifest, Binding Resolver, and Binding Policy v0 contracts
  exist with validation tests.
- Local registry and resolver deterministically discover and validate Athena-owned package data.
- M30 representation and composition output consume package-resolved descriptor facts.
- The M32 sample proves engineering package identity, Presentation Profile identity, and
  representation package identity are separate and swappable.
- Product smoke and structured proof cover package resolution, binding, descriptor validation,
  renderer integration, visual-density guards, and compatibility cleanup.
- M31 deferred compatibility items are closed or explicitly versioned with evidence.
- No `.athena` visual syntax, QET runtime dependency, or frontend package authority is introduced.
- Encoding audit passes after repository text changes.
- Cleanup ledger and retrospective are complete and accurate.

## Open Questions

No phase-blocking question is left open for planning. During implementation, package file format
may start with JSON or YAML based on existing repository parser/tooling fit, but the contract must
remain data-driven and frontend-independent.

## Assumptions Index

- **A-1:** M32 examples use Athena-owned or synthetic product-like package assets rather than
  proprietary vendor assets.
- **A-2:** The local registry is repository/project-local for M32; user-global registry behavior is
  deferred until package lifecycle and publishing are designed.
- **A-3:** A single concrete Graphic Resource implementation is enough for M32 customer acceptance;
  richer backends are modeled for compatibility but deferred.
