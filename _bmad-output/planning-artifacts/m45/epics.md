---
stepsCompleted: [1, 2, 3]
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-08-08-m45/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-08-08-m45/ARCHITECTURE-SPINE.md
  - AGENTS.md
---

# Athena - M45 Epic Breakdown

## Requirements Inventory

### Functional Requirements

FR1: Discover direct project-local packages and resolve them through the existing `ResolvedPackageGraph`.
FR2: Admit package-local SVG resources with safe profile, digest, license, and provenance.
FR3: Author native Symbol, Element, Part, Macro, Variant, and Placeholder definitions.
FR4: Pair native metadata with SVG geometry, center, anchors, compatibility contracts, and lineage.
FR5: Resolve source-owned FunctionPartBinding compatibility against admitted Parts.
FR6: Bind Functions through stable FunctionRepresentationBinding identities.
FR7: Resolve composite Elements with public ports and no hidden engineering mutation.
FR8: Browse, preview, inspect, insert, and configure real package-backed occurrences through server operations.
FR9: Publish complete rolling-shutter page with real geometry, ports, routes, labels, rulers, clean frame, and no bottom table.
FR10: Resolve Macro Variants and typed Placeholders deterministically while preserving semantic identity.
FR11: Publish package provenance plus compiler-owned usage trace to every visible occurrence.
FR12: Resolve exact package and semantic capability/interface dependencies before binding or scene publication.

### NonFunctional Requirements

NFR1: Canonical package-item and `athena-lock-v3` digests are deterministic SHA-256 outputs.
NFR2: Only `PACKAGE_READY` items/packages enter runtime snapshot, lock, binding, Source Revision, or Scene.
NFR3: Unsafe SVG, path escape, network/external reference, malformed metadata, and resource-limit violations fail closed.
NFR4: External catalog formats and `reference/` trees are never compiler/runtime inputs.
NFR5: Package metadata is represented once; provenance and usage trace are inspectable and stable.
NFR6: All package and authoring mutations use full Source Revision CAS and recoverable server transactions.
NFR7: Theia is a typed client; Canonical Scene is renderer-neutral and Konva remains disposable.
NFR8: Evidence is deterministic/reproducible and stored under `_bmad-output/implementation-artifacts/m45/`.
NFR9: Production source contains no proof/demo/sample/milestone-named classes or compatibility shims.

### Additional Requirements

- Root `athena.yaml` remains authored dependency intent; direct package children feed existing `ResolvedPackageGraph`.
- `athena-lock-v3` replaces Lock V2 and pseudo item ids; Source Revision derives package details only from expected lock.
- `PackageItemMetadata` is authored; `AdmittedPackageItem` owns normalized payload, digest, state, diagnostics, and resolved refs.
- `PortCompatibilityContract` maps SymbolAnchor -> ElementPort -> FunctionSlot; EngineeringPort remains source authority.
- `FunctionRepresentationBinding` has stable persisted identity; `FunctionPartBinding` is source-owned and projection-independent.
- Package provenance ends at PackageItem; usage trace begins at binding and ends at scene occurrence.
- Macro geometry is relative representation reuse; Variant preserves interface fingerprint; Placeholder targets non-interface representation fields only.
- Package write transactions persist durable before/after bytes before PREPARED and recover deterministically.
- Existing `representationPackageRoots`, `LocalPackageRegistry`, duplicate package descriptors, `symbol.yaml`, BindingManifest, and Lock V2 paths are deleted, not adapted.
- Repository visual Golden Rule wins: narrow flush rulers, one-pixel square frame, blank white canvas, no visible grid, no bottom title/table block.
- Spatial location uses `SemanticPlacementIntent`, `LogicalLayoutCoordinate`, then compiler-derived
  `PhysicalGeometryCoordinate`; Snap remains independent and SVG anchors never own port semantics.

### UX Requirements

UX1: Package browser shows package/item kind, admission state, center, ports, compatibility, Parts, variants, provenance, and license.
UX2: Preview uses admitted package snapshot only and cannot mutate source or scene.
UX3: Insert/configure actions submit typed intent and refresh after server publication.
UX4: Incomplete package work is visible through AdmissionReport with exact correction diagnostics, never as a runtime asset.
UX5: Rolling-shutter canvas follows canonical reference visual grammar and existing ruler/frame rules.

## FR Coverage Map

| Requirement | Stories |
| --- | --- |
| FR1, FR12 | 1.1, 1.2, 1.3 |
| FR2, FR4 | 1.4, 3.1 |
| FR3 | 2.1, 2.2, 2.3, 2.4 |
| FR5, FR6 | 2.5, 2.6 |
| FR7, FR10 | 4.1, 4.2, 4.3 |
| FR8 | 5.1, 5.2 |
| FR9 | 5.3, 5.4 |
| FR11 | 3.2 |
| NFR1-NFR6 | 1.2, 1.3, 1.4, 2.5, 4.3, 6.1 |
| NFR7-NFR9 | 5.1, 5.4, 6.1, 6.2 |

## Epic List

1. **Package Authority and Resolution** - One native package graph, item phases, canonical lock, and fail-closed admission.
2. **Symbol, Element, Part Contracts** - Complete reusable definitions, compatibility ports, bindings, and source-owned Part facts.
3. **Native Package Assets and Provenance** - Curate package-local SVG, enforce safe admission, and expose lineage/AdmissionReport.
4. **Macro Composition, Variants, Placeholders** - Deterministic representation reuse without engineering inference.
5. **Library UX and Rolling-Shutter Authoring** - Browse, preview, insert, bind, and publish complete page.
6. **M45 Closure** - Full verification, hygiene, visual evidence, retrospective, and milestone close.

## Epic 1: Package Authority and Resolution

Goal: establish one compiler-owned package graph and lock authority.

### Story 1.1: Discover Direct Local Package Catalog
As a compiler maintainer, I want direct `packages/<package-name>` catalog discovery, so package identity and roots are deterministic.
**Acceptance Criteria:**
- Given a project with direct package children, when discovery runs, then each valid `package.yaml` becomes a candidate in existing `ResolvedPackageGraph`.
- Given nested roots, mismatched directory/package ids, or reference-tree paths, when discovery runs, then admission fails with exact correction diagnostics.
- Given an undeclared package insertion, when transaction runs, then typed mutation updates `athena.yaml` before lock materialization.
- `representationPackageRoots` and `LocalPackageRegistry` are absent from production resolver paths.

### Story 1.2: Implement PackageItem Authored/Admitted Contracts
As a package author, I want one native PackageItem contract, so all item kinds share identity without duplicate authorities.
**Acceptance Criteria:**
- Authored metadata cannot set digest, admission state, diagnostics, or resolved references.
- Compiler emits `AdmittedPackageItem` with closed kinds and normalized payload.
- Canonical `athena-package-item-c14n-v1` digest vectors are byte deterministic.
- Old duplicate package descriptors and `symbol.yaml` admission are removed, not adapted.

### Story 1.3: Materialize athena-lock-v3 and Source Revision Inputs
As a compiler maintainer, I want one canonical lock and revision model, so stale package edits fail safely.
**Acceptance Criteria:**
- `athena-lock-v3` records package, item-version, resource, dependency, semantic-provider, and profile digests.
- Lock output is sorted/canonical and opening fails on drift.
- Source Revision derives package detail only from expected lock; pseudo ids and independent ledgers are gone.
- Missing, ambiguous, cyclic, or incompatible dependency resolution fails closed.

### Story 1.4: Publish Admission States and AdmissionReport
As an engineer, I want exact diagnostics for incomplete packages, so I can correct them without exposing invalid runtime assets.
**Acceptance Criteria:**
- Package aggregation is strict: any INVALID => INVALID; else any INCOMPLETE => INCOMPLETE; else READY.
- READY only enters runtime snapshot/lock/binding/scene.
- INCOMPLETE is readable through immutable AdmissionReport outside runtime roots and has draft revision guards.
- INVALID has no runtime or browser preview path.

## Epic 2: Symbol, Element, Part Contracts

Goal: establish complete package facts and explicit source bindings.

### Story 2.1: Admit Symbol Geometry and Port Compatibility
As a package author, I want SVG-backed Symbol metadata, so geometry and anchor compatibility are complete.
**Acceptance Criteria:** SVG resource, bounds, center/origin, transforms, labels, stable anchors, and `PortCompatibilityContract` admit only when complete; EngineeringPort semantics remain source-owned; interface mismatch fails closed.

### Story 2.2: Admit Composite Elements and Function Slots
As an engineer, I want Elements to compose Symbols and expose stable ports, so composite devices remain inspectable.
**Acceptance Criteria:** composition order, child identities, Function slots, SymbolAnchor -> ElementPort -> FunctionSlot mappings, public ports, compatible Parts, variants/placeholders all resolve; Element never equals Entity and cannot create Relationships.

### Story 2.3: Admit Source-Owned Part Facts
As an engineer, I want physical/procurement Parts, so implementation selection is validated without owning geometry or solution intent.
**Acceptance Criteria:** manufacturer/article, typed technical values, function templates, capability compatibility, compatible Elements, accessories, provenance/license admit; Part cannot create Relationship or Pattern.

### Story 2.4: Resolve FunctionPartBinding
As an engineer, I want a stable source-owned Part binding, so every projection sees same implementation fact.
**Acceptance Criteria:** `FunctionPartBinding(functionId, implementationRole)` identity is stable; one active Part per role; selection is EngineeringEditOperation; invalid compatibility leaves source/scene unchanged.

### Story 2.5: Persist FunctionRepresentationBinding
As an engineer, I want explicit Function representation bindings, so one Function can use multiple projections.
**Acceptance Criteria:** stable binding identity/cardinality; Element/Variant/Placeholder changes preserve Function/Entity/Relationship/Port identity; scene occurrence identity remains separate; binding digest enters Source Revision.

## Epic 3: Native Package Assets and Provenance

Goal: curate real geometry into governed packages without external format runtime paths.

### Story 3.1: Admit Safe Package-Local SVG
As a package author, I want copied SVG resources admitted safely, so real symbols render without active/external content.
**Acceptance Criteria:** `svg-safe-1` rejects scripts, event attributes, DTD/entities, foreignObject, CSS/external/data URLs, traversal, symlinks, malformed UTF-8, oversized/nested resources; digest/license/provenance recorded; runtime reads only package-local SVG.

### Story 3.2: Curate IEC, Vendor, Community Native Packages
As an engineer, I want local IEC/vendor/community packages, so the golden project uses real reusable symbols.
**Acceptance Criteria:** three direct packages exist under M45 example; at least ten native Symbol/Element definitions and five Parts admit READY; no external reference path is read by compiler/runtime; package licenses/provenance inspectable.

### Story 3.3: Publish Package Provenance and Usage Trace
As an engineer, I want to trace every visible asset to its source, so library usage is reviewable.
**Acceptance Criteria:** immutable package provenance ends at PackageItem; usage trace links binding to scene occurrence; composed read view is stable; machine-local absolute paths excluded from digests/exports.

## Epic 4: Macro Composition, Variants, Placeholders

Goal: provide EPLAN-informed representation reuse without hidden engineering intelligence.

### Story 4.1: Resolve Representation Macros
As an engineer, I want reusable Symbol/Element/Page Macros, so repeated representation composition is fast.
**Acceptance Criteria:** Macro uses relative geometry and declared child/binding references; insertion supplies Sheet, Function ids, transform, operation id; server persists deterministic ids; no Relationship/Part/Pattern inference.

### Story 4.2: Resolve Variants with Interface Preservation
As an engineer, I want named representation variants, so visual alternatives remain safe.
**Acceptance Criteria:** Variant resolution deterministic; same normalized Function-slot/public-port fingerprint required; interface change requires new Element item/version; semantic identity preserved.

### Story 4.3: Resolve Typed Placeholders
As an engineer, I want typed representation placeholders, so package configuration is explicit.
**Acceptance Criteria:** only declared label/style/non-interface fields target substitution; missing/type-invalid values fail closed; previous accepted scene remains; Placeholder cannot select Part, infer Function, create Relationship, or apply rules.

## Epic 5: Library UX and Rolling-Shutter Authoring

Goal: complete the human authoring loop and publish reference-quality page.

### Story 5.1: Build Theia Package Browser and Inspector
As an engineer, I want package search/preview/inspection, so I can choose assets by engineering metadata.
**Acceptance Criteria:** browser reads READY snapshot and AdmissionReport through typed server API; shows kind, state, center, ports, compatibility, Parts, variants, provenance/license; preview cannot mutate source/scene.

### Story 5.2: Insert and Bind Real Element Occurrences
As an engineer, I want to insert package-backed Elements, so source and scene stay synchronized.
**Acceptance Criteria:** typed intent operation uses full Source Revision; server stages `athena.yaml`/source/Sheet/binding/lock as declared; validates, recovers, journals, publishes; frontend never patches source or scene; persisted placement uses semantic intent and/or logical Sheet coordinates, never raw canvas X/Y; compiler derives physical geometry and Snap remains separate.

### Story 5.3: Publish Complete Rolling-Shutter Page
As an engineer, I want a complete IEC-style rolling-shutter page, so Athena proves real engineering authoring.
**Acceptance Criteria:** page contains supply, breaker, contactors, overload, motor, PLC, sensors, terminals, coils, auxiliary contacts, lamps; ten+ package-backed occurrences; visible ports/routes/labels; A-H/1-17 rulers; one-pixel frame; blank white interior; no bottom table; visually similar reference.

### Story 5.4: Prove Edit, Reopen, Export Stability
As an engineer, I want edit/reopen/export evidence, so authored package use is durable.
**Acceptance Criteria:** valid insert/bind/variant/placeholder and rejected invalid operation leave deterministic source/scene; restart/reopen preserves identities and traces; repeated SVG digests equal; pinned PNG evidence stored under M45 artifacts; desktop/narrow screenshots pass visual comparison.

## Epic 6: M45 Closure

Goal: verify, clean, and close milestone.

### Story 6.1: Run Full M45 Verification and Hygiene
As a maintainer, I want sequential product verification, so M45 closure is evidence-backed.
**Acceptance Criteria:** affected Gradle modules, frontend contracts/tests, LSP, product E2E, encoding audit, source-set hygiene audit, and stale-path scans pass sequentially; no external-format runtime path remains.

### Story 6.2: Publish M45 Closure and Retrospective
As a team, we want closure and retrospective records, so next milestone starts from verified truth.
**Acceptance Criteria:** implementation artifacts include screenshots, exports, lock, lineage, operation transcripts, test logs; closure maps all stories/FRs/NFRs; retrospective records lessons and follow-up; sprint status marks all M45 stories/epics done only after proof.
