# M34 Brownfield Divergence Review

Date: 2026-07-24

Reviewed artifact: `ARCHITECTURE-SPINE.md`

Brownfield scope: Athena M30-M33 package, representation, binding, primitive, and Cabinet composition code.

## Verdict

**REJECT AS IMPLEMENTATION-READY.** The corrected M34 direction is sound, but the architecture spine still permits compliant implementers to create duplicate definition models, competing policy authorities, parallel occurrence types, and two different renderer input paths. The current Reuse/Extend/Replace/Delete calls are migration intentions, not executable architecture decisions.

The spine may be approved only after every overlapping M30-M33 contract has one exact disposition, one target owner, one allowed transition adapter, and one deletion gate.

## Evidence Summary

The brownfield does not contain one representation model. It already contains overlapping layers:

- `DrawingSymbolAnatomy` owns identity, version, package, tags, lifecycle, primitives, anchors, labels, hotspots, bounds, orientations, and provenance.
- `RepresentationDefinition` owns symbol identity, library, version, lifecycle, anatomy, labels, variants, and style tokens.
- package-model `RepresentationDescriptor` owns resource, bounds, anchors, labels, hotspots, transforms, variants, styles, and validation references.
- `RepresentationPackageDescriptor` owns descriptor/resource inventory, profiles, variants, styles, lifecycle, and provenance.
- `RepresentationPolicy`, `BindingManifest`, and `PresentationProfileDescriptor` all participate in representation selection.
- `BindingResolver` and `RepresentationBindingCompiler` are separate binding stages; the package runtime currently bridges them by synthesizing another definition and policy.
- `BindingResolution`, representation-model `RepresentationOccurrence`, presentation-model `PresentationOccurrence`, and `PresentationRepresentationFact` are distinct resolution/occurrence outputs.
- `GraphicPrimitive`, `PresentationPrimitive`, and `PresentationShapeCommand` are separate visual vocabularies; M33 converts between them on the Cabinet path.

M34 therefore starts from an already-divergent model graph. General statements such as "reuse/extend" cannot prevent a fourth model from appearing.

## Findings

### 1. Critical - The claimed single Symbol/Element IR has no declared canonical brownfield predecessor

The spine declares one `Symbol / Element IR` (`ARCHITECTURE-SPINE.md:17-34`) and says only that `M33IecSymbolDefinition` is replaced (`:192`). It does not dispose of either `DrawingSymbolAnatomy` or `RepresentationDefinition`, even though both already model reusable symbol definitions.

Evidence:

- `DrawingSymbolAnatomy.kt:149-164` already owns nearly every property assigned to M34 Symbol by AD-1.
- `RepresentationContracts.kt:224-233` independently owns symbol identity, lifecycle, anatomy, labels, variants, and styles.
- `M33IecSymbolSupport.kt:40-44` merely wraps descriptor/resource ids around `DrawingSymbolAnatomy`; replacing only this wrapper leaves both deeper models alive.

Two compliant implementations can therefore choose different canonical bases: one can extend `DrawingSymbolAnatomy`, while another can extend `RepresentationDefinition`. Both satisfy AD-11. The spine must name the sole canonical target type and explicitly mark the other model as adapter-only or deletable.

### 2. Critical - `RepresentationDescriptor` cannot be both reused and extended without recreating duplicate authority

The migration seed says `RepresentationDescriptor` is "Reuse/extend" (`ARCHITECTURE-SPINE.md:188`). Those are materially different decisions. The existing descriptor already owns bounds, anchors, label slots, hotspots, transforms, variants, styles, and validation references (`RepresentationDescriptorModels.kt:92-104`), all of which overlap the proposed Symbol/Element IR (`ARCHITECTURE-SPINE.md:60-68`).

An implementer may extend the descriptor into the source-level Element model; another may keep it as a lowered compiler artifact. Both comply, but only the second preserves one-way ownership. The required decision is: **reuse unchanged as a generated, immutable lowered artifact; do not add source identity, compatibility predicates, composition, or SVG metadata authority to it.** Every descriptor field must name its Element-IR source or derived rule.

### 3. Critical - Representation selection still has three policy authorities with no precedence or retirement rule

The spine refers to "Presentation Profile + Binding Policy" and reuses `BindingResolver` (`ARCHITECTURE-SPINE.md:25-31`, `:170-175`, `:189-190`) without naming which existing policy contract is authoritative.

Current code has:

- `RepresentationPolicy` selecting projection kind, subject kind, semantic role, occurrence role, symbol family, symbol, variant, fallback, and priority (`RepresentationContracts.kt:160-185`).
- `BindingManifest` selecting engineering package, concept, representation package, compatible profiles, and policy tags (`BindingManifestModels.kt:30-40`).
- `PresentationProfileDescriptor` selecting contexts, style profile, standard tags, compatibility, fallback, and policy facts (`PresentationProfileModels.kt:66-76`).

`BindingResolver` currently consumes `BindingManifest` and `PresentationProfileDescriptor`, not `RepresentationPolicy` (`BindingResolverModels.kt:26-34`). A compliant implementer can preserve both policy systems, merge them, or route around one. The spine must specify the exact selection stages, the input and output type of each stage, and the disposition of `RepresentationPolicy`.

The ambiguity is executable today: `PackageBackedRepresentationOccurrenceFactory.kt:64-100` takes `BindingResolver` evidence, synthesizes a new `RepresentationDefinition` and `RepresentationPolicy`, and invokes `RepresentationBindingCompiler`. M34's singular "Binding Resolver" can therefore mean either existing compiler, both in sequence, or a third replacement. The architecture must choose one canonical binding result and explicitly reuse, absorb, or delete the second stage and factory.

### 4. Critical - Anchor compatibility is named but has no exact owner or type

AD-3 and AD-6 require direction, signal, role, and terminal compatibility predicates (`ARCHITECTURE-SPINE.md:70-75`, `:91-96`), but the migration seed does not identify where those predicates live.

The current package `RepresentationAnchorDefinition` carries geometry and side, while M33 `DrawingSymbolAnchor` carries visual anchor role. The semantic project owns actual port direction and signal. Without a named `AnchorCompatibilityPredicate` contract and an exact validation boundary, implementers can add direction/signal fields to `RepresentationDescriptor`, `DrawingSymbolAnatomy`, `BindingManifest`, or a new Element model.

The spine must declare one predicate type owned by Element IR, define whether Symbol anchors may carry only visual roles or also reusable compatibility constraints, and define the one compiler stage that compares predicates with semantic port facts.

### 5. Critical - "Graphic Primitive IR" is not one existing boundary or module

The spine says Graphic Primitive IR is reused and is the sole active Cabinet renderer input (`ARCHITECTURE-SPINE.md:50`, `:191`, `:205`). The code has three visual vocabularies:

- `GraphicPrimitive` in `representation-model/GraphicPrimitiveModels.kt`, consumed directly by the SVG adapter.
- `PresentationPrimitive` in `representation-model/PresentationAnatomy.kt:43-86`, consumed by presentation/document paths.
- `PresentationShapeCommand` in `presentation-model/PresentationShapeModels.kt:9-56`, transported to the active Theia renderer through presentation packs.

M33 Cabinet explicitly converts `GraphicPrimitive` to `PresentationPrimitive` (`M33CabinetDrawingCompositionDeriver.kt:369-420`). That conversion is lossy: arcs become 12-segment polylines, transforms fail, and terminal/label anatomy is emptied. The spine also assigns Graphic Primitive IR to `presentation-model` (`ARCHITECTURE-SPINE.md:205`), which contradicts its current module location.

Two compliant implementers can target different primitive types and both claim to reuse Graphic Primitive IR. M34 must name the exact canonical class and module, the exact renderer entry point, and whether `PresentationPrimitive` is replaced, adapted one-way, or retained for a distinct purpose.

### 6. High - Replacing `M33CabinetPackageSet` does not assign its mixed responsibilities

The migration table says the `M33CabinetPackageSet` XML loader is replaced (`ARCHITECTURE-SPINE.md:193`), but that class is not only an XML loader. It currently:

- matches model and duplicated semantic type (`M33CabinetPackageSet.kt:101-106`);
- maps semantic ports to symbol anchors (`:154-171`);
- converts symbol anatomy into `RepresentationDescriptor` (`:174-198`);
- synthesizes `EngineeringPackageDescriptor` (`:222-250`);
- synthesizes `BindingManifest` (`:252-265`);
- validates package/profile/representation cross-links and parses assets (`:279-383`).

Removing only XML parsing leaves multiple local DTOs and synthetic authorities active. The spine must assign each responsibility to a specific M34 compiler/runtime type and list the exact classes and methods to delete after migration.

### 7. Critical - The active Cabinet cutover path is absent from the migration table

`PresentationModelDeriver.kt:131-155` currently executes four authorities in one request: M33 package-backed facts, M32 package fallback, generic M25 representation fallback, and M33 drawing composition. In addition, both `M33CabinetPresentationFactDeriver.kt:231-286` and `M33CabinetDrawingCompositionDeriver.kt:384-420` independently lower `GraphicPrimitive` into `PresentationPrimitive`.

None of these active-path symbols appears in the migration seed. Replacing the M33 XML loader can leave every fallback and both lowerers intact, while another compliant implementation can bypass `PresentationModelDeriver` and feed Graphic IR directly to the SVG adapter. Both can claim compliance with lines 191-195 and produce incompatible products.

The spine must give exact dispositions to `PresentationModelDeriver`, `M33CabinetPresentationFactDeriver`, `M33CabinetDrawingCompositionDeriver`, `M32PackageBackedPresentationFactDeriver`, `document.toPresentationRepresentationFacts`, and the `PresentationDocument` representation/composition fields. The Cabinet cutover gate must prove one production branch, one lowerer, and zero legacy fallback invocations.

### 8. High - "Delete or fixture-only" and "Delete or hidden adapter" are not decisions

The XML and legacy-path rows use alternatives (`ARCHITECTURE-SPINE.md:194-195`). A compliant implementation may retain production readers as "fixtures" or leave runtime fallback reachable as a "hidden adapter."

This is especially unsafe because `M33CabinetPackageSet.isDeclared` activates when **any** one XML asset exists (`M33CabinetPackageSet.kt:267-277`), while `load` requires all four (`:279-284`). The architecture must require that production Cabinet has no call path to `isDeclared/load`, move fixtures under test resources, and define a test proving XML deletion or production unreachability. A transition adapter must have a named caller, input/output types, telemetry or proof, and deletion story.

### 9. High - Duplicate identity/value types are not covered by the migration seed

The brownfield has same-concept value types in different modules, including `RepresentationVariantId` in both package-model and representation-model, plus parallel descriptor/symbol/resource/label/anchor ids. M33 performs manual string conversions between them.

M34's "one typed IR" can still introduce `SymbolId`, `ElementId`, `AnchorId`, and `VariantId` beside all existing ids unless the architecture states which identities are canonical in source IR, package inventory, descriptor output, and occurrence output. The migration table must include every duplicated id family and allow only explicit one-way boundary converters.

### 10. High - Element selection versus descriptor selection is algorithmically undefined

The architecture flow says `Semantic Model + Element IR + Policy -> Binding Resolver -> Representation Descriptor / Occurrence` (`ARCHITECTURE-SPINE.md:172-176`). Existing `BindingResolver` does not accept Element IR. It selects a `RepresentationPackageDescriptorEntry` by `BindingPolicyTag`, then finds a pre-supplied `RepresentationDescriptor` (`BindingResolver.kt:18-45`, `:155-187`).

Two compliant implementations can either extend `BindingResolver` to understand Element composition or compile Elements into descriptors before calling the unchanged resolver. These produce different ownership and caching behavior. The spine must choose one. The safer boundary is: compile and register Element definitions first; resolver selects a compiled definition/descriptor by stable id; resolver never interprets SVG or Element composition.

### 11. High - Occurrence ownership ignores Element composition

The migration seed does not mention any occurrence contract. Representation-model `RepresentationOccurrence` stores `symbolId`, variant, label bindings, terminal bindings, references, and composition memberships (`RepresentationContracts.kt:266-277`). Package-runtime `BindingResolution` is another resolved-selection result (`BindingResolverModels.kt:64-73`). Presentation-model separately publishes `PresentationOccurrence` and `PresentationRepresentationFact` (`PresentationOccurrenceModels.kt:27-42`, `PresentationDocument.kt:48-60`). M34 introduces Elements composed from Symbols, but does not define which layer is canonical or whether an occurrence refers to an Element, a Symbol, both, or a nested occurrence graph.

One implementer can add `elementId` to `RepresentationOccurrence`; another can create `ElementOccurrence`; another can flatten to symbol occurrences. All satisfy the diagram. M34 must retain one public occurrence contract and define the flattening boundary, stable occurrence identity, anchor export mapping, and whether nested symbol occurrences are compiler-private.

### 12. High - Lifecycle, provenance, profile, variant, and style ownership remains multiplied

AD-1 gives Symbol lifecycle and provenance; AD-2 gives Element variants (`ARCHITECTURE-SPINE.md:60-68`). Existing ownership is already spread across:

- `DrawingSymbolAnatomy`: package/profile tags, lifecycle, orientations, provenance;
- `RepresentationDefinition`: lifecycle, variants, style tokens;
- `RepresentationPackageDescriptor`: profiles, variants, style references, lifecycle, provenance;
- `PresentationProfileDescriptor`: style profile, standards, provenance;
- `RepresentationDescriptor`: variants and style references.

No field-level authority table says which values are authored, inherited, constrained, or derived. Compliant implementers can copy them at every lowering stage and create drift. The spine must define source authority and derivation for each field family.

### 13. High - Dual-front-end conflict detection is scoped too narrowly

AD-10 rejects dual metadata authority for "a definition" (`ARCHITECTURE-SPINE.md:119-124`) but does not define the global identity key or discovery scope. A standalone annotated SVG and an Athena-first definition can declare the same logical symbol in separate folders/packages without being recognized as one definition.

The architecture must define canonical identity as package coordinates plus definition kind plus definition id plus version, resolve imports before duplicate checks, and reject collisions across the entire resolved package graph. File-local duplicate detection is insufficient.

### 14. High - Existing generic vector-resource paths can bypass the safe SVG frontend

AD-12 protects the new safe SVG frontend (`ARCHITECTURE-SPINE.md:133-138`), but existing package contracts still allow `GraphicResourceKind.VECTOR_DOCUMENT` with an arbitrary path (`RepresentationPackageModels.kt:71-87`). Existing render payloads also carry a resource handle.

Unless the architecture changes this boundary, an implementer can preserve a package path that reaches a renderer without safe compilation. The spine must require vector source paths to resolve only inside the compiler, and package/runtime/render layers to carry a compiled artifact id or Graphic Primitive document handle. Raw SVG path, XML DOM, or SVG DOM must be absent from renderer-facing payloads.

### 15. Medium - Structural ownership of package artifacts versus element artifacts is incomplete

The structural seed separates `packages/` and `elements/` (`ARCHITECTURE-SPINE.md:208-211`) but does not define whether an Element is contained by a Representation Package, referenced by it, or independently versioned. It also does not define whether Presentation Profiles and Binding Policies remain package assets or become Athena representation source declarations.

Two implementations can create incompatible artifact graphs while respecting the directory sketch. The architecture must define artifact coordinates, import direction, package inventory ownership, version resolution, and the compiler output for every source artifact.

### 16. Medium - Compatibility adapters have no containment or removal contract

AD-11 allows adapters, and the migration seed mentions a compatibility adapter for `M33IecSymbolDefinition` (`ARCHITECTURE-SPINE.md:126-131`, `:192`). It does not prohibit adapters from becoming new authorities, nor define where they live, which direction they convert, or when they are removed.

Every adapter must be one-way from a named legacy type into the canonical M34 type, live outside canonical model packages, be forbidden from authoring or mutating either side, have characterization tests, and have a deletion condition tied to the final migrated caller.

## Required Architecture Correction

Replace the migration seed with an exhaustive decision table containing at least these columns:

| Existing symbol | Exact action | Canonical target | Allowed adapter | Production callers after M34 | Deletion gate |
| --- | --- | --- | --- | --- | --- |
| `DrawingSymbolAnatomy` | Choose one: reuse, adapt, or delete | Named M34 type | Named one-way adapter or none | Named callers | Named test/story |
| `RepresentationDefinition` | Choose one | Named M34 type | Named adapter or none | Named callers | Named test/story |
| package `RepresentationDescriptor` | Reuse unchanged as lowered output or replace | Named type | Named lowerer | Named callers | Named test/story |
| `RepresentationPolicy` | Reuse or retire | Named policy stage | Named adapter or none | Named callers | Named test/story |
| `RepresentationBindingCompiler` | Absorb, retain as named stage, or delete | Named binding result | Named adapter or none | Named callers | Named test/story |
| `PackageBackedRepresentationOccurrenceFactory` | Replace or delete | Named lowerer/binder | None on final path | Named callers or none | Named test/story |
| `BindingManifest` | Reuse or retire | Named policy stage | Named adapter or none | Named callers | Named test/story |
| `PresentationProfileDescriptor` | Reuse unchanged or replace | Named type | Named adapter or none | Named callers | Named test/story |
| `RepresentationOccurrence` | Extend or replace, never parallel | Named occurrence type | Named adapter or none | Named callers | Named test/story |
| `BindingResolution`, `PresentationOccurrence`, `PresentationRepresentationFact` | Name each as intermediate, canonical, transport-only, or deletable | Named occurrence pipeline | Named one-way adapters | Named callers | Contract tests |
| `GraphicPrimitive` | Reuse or replace | Exact module and class | Named adapter or none | Exact renderer entry point | Named test/story |
| `PresentationPrimitive` | Reuse, adapt, or delete | Exact target | Named one-way adapter or none | Named callers | Named test/story |
| `PresentationShapeCommand` and presentation packs | Retain as renderer DTO or delete | Exact target | Named one-way adapter or none | Exact frontend renderer | Cabinet transport proof |
| `M33CabinetPackageSet` and local DTOs | Split and delete | Named replacement per responsibility | None on final path | None | Cabinet E2E proof |
| M32/M33 Cabinet fact/composition derivers | Replace and delete or name transition-only scope | One M34 Cabinet compiler | Named temporary adapter only | None on final path | Production reachability test |
| M32/M33 XML readers/assets | Delete from production | Compiled Athena artifacts | Test-only fixture loader if needed | None | Production reachability test |

Also add a field-authority table for identity, version, lifecycle, provenance, profiles, variants, styles, bounds, anchors, labels, hotspots, resources, and compatibility predicates. Each field must have exactly one authored owner and explicit derived copies.

## Approval Gate

M34 becomes implementation-ready only when:

1. one canonical Symbol/Element definition type is named;
2. one policy pipeline and one occurrence contract are named;
3. one primitive type and one active Cabinet renderer input are named;
4. every M30-M33 overlapping type has an exact Reuse, Extend, Replace, or Delete disposition, never an alternative;
5. every transition adapter is one-way, named, tested, and scheduled for deletion;
6. production XML and raw-SVG bypass paths have explicit unreachability tests.
7. the active Cabinet path has one binding stage, one occurrence pipeline, and one visual lowering path.
