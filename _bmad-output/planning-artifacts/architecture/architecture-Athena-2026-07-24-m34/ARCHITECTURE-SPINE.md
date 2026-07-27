---
name: Athena M34 Typed Symbol And Element System
type: architecture-spine
purpose: build-substrate
altitude: milestone-to-epics
paradigm: single-language-typed-representation-compiler
scope: symbol/element contracts, Athena language, governed annotated SVG, typed policy, Cabinet migration
status: draft
created: '2026-07-24'
updated: '2026-07-24'
---

# Architecture Spine - Athena M34 Typed Symbol And Element System

## Design Paradigm

M34 uses one metadata language and one representation IR:

```text
native *.athena representation source
  -> ANTLR4 / AST / type checker / lint
  -> graphic { ... } or graphic svg "./asset.svg"

referenced governed annotated SVG graphic resource
  -> hardened SVG parser / typed node-local data-athena profile / lint

both exclusive frontends
  -> RepresentationDefinition / GraphicPrimitive

project *.athena source
  -> Engineering Semantic Model / Profile / Binding IR

Semantic Model + typed representation facts
  -> Binding Resolver
  -> Representation Occurrence
  -> drawing-composition
  -> Graphic Primitive IR
  -> Renderer Adapter
```

Each reusable definition has one Athena metadata authority. Native Athena graphic bodies serve
simple visuals. Referenced governed annotated SVG graphic resources serve complex visuals. The
compiler rejects duplicate Athena identities and never merges fields between Athena and SVG. SVG is
untrusted compile-time input and may define only geometry plus node-local reusable representation
contracts through the closed `data-athena-*` profile. XML is not a manifest, transport, or runtime
authority. Project engineering remains an independent Athena source product.

Source form follows maintainability. Native Athena is used when concise typed primitives are easier
to read and review. `graphic svg "..."` is used when geometry is shape-heavy or naturally maintained
in a vector tool; only the specific SVG nodes that carry Athena representation contracts receive
governed `data-athena-*` attributes. SVG root metadata never duplicates Athena identity, kind,
version, lifecycle, profile, or binding.

## Inherited Invariants

| Inherited | Source | Binds M34 |
| --- | --- | --- |
| `.athena` remains project semantic truth | M32/M33 | Instances, ports, connections, and layout remain upstream. |
| Presentation Profile and Binding Resolver are policy boundaries | M32 | Selection policy stays separate from definitions. |
| Renderer is paint-only | M33 AD-2 | No SVG/DOM or semantic inference in frontend adapters. |
| Graphic Primitive IR is renderer-neutral | M33 | M34 lowers safe visuals into the existing primitive boundary. |
| Cabinet is the product surface | M33 AD-9 | M34 does not divide product effort across views. |
| Structured proof is required | M33 AD-10 | Visual claims require proof plus screenshots. |

## Invariants And Rules

### AD-1 - Symbol Is Atomic Visual Anatomy [ADOPTED]

- **Binds:** FR-1..FR-4
- **Prevents:** atomic graphics becoming duplicate semantic device definitions.
- **Rule:** Symbol owns identity, visual body, intrinsic bounds, anchors, slots, hotspots, lifecycle,
  and provenance. Every connectable anchor carries compatibility predicates. Symbol never owns
  project ports, connections, or device classification.

### AD-2 - Element Is Reusable Visual Composition [ADOPTED]

- **Binds:** FR-1..FR-4
- **Prevents:** vendor/user visuals bypassing composition and anchor governance.
- **Rule:** Element composes Symbols or governed visual groups and exports anchors, labels,
  hotspots, variants, and compatibility predicates. It never reclassifies a project device.

### AD-3 - Annotated SVG Is A Governed Graphic Resource [ADOPTED]

- **Binds:** FR-5..FR-8, FR-13..FR-17
- **Prevents:** complex visuals requiring fragmented sidecars or arbitrary SVG/XML becoming authority.
- **Rule:** A complex definition is one Athena Symbol or Element declaration whose `graphic svg`
  member references one governed SVG resource. The SVG root declares only
  `data-athena-schema="representation/v1"`. Selected nodes use the exhaustive lowercase
  `data-athena-*` profile for anchors, explicit points, labels, hotspots, and compatibility
  predicates. Unmarked geometry has no meaning. Definition identity/version/kind/lifecycle, project
  facts, Profile, Binding, package policy, arbitrary metadata, and runtime behavior are forbidden in
  SVG. Authors annotate only contract-bearing nodes; ordinary geometry remains ordinary SVG.

### AD-4 - Athena Owns Type Interpretation [ADOPTED]

- **Binds:** FR-9..FR-12, FR-22, NFR-1
- **Prevents:** XML or frontend code becoming unvalidated metadata authority.
- **Rule:** Ordinary `*.athena` files contain project, native Symbol/Element, Profile, and Binding
  declarations. Complex Symbol/Element sources may use the governed annotated-SVG frontend. Each
  frontend has deterministic parse, semantic type checking, lint, canonical formatting, and
  compilation for human-, importer-, and AI-authored material. Their source-specific ASTs lower through
  one compiler-owned admission/validation pipeline into the same canonical definition; neither is a
  weaker runtime asset format and no SVG-specific runtime IR is allowed.

### AD-5 - Complex Visuals Use One Referenced Annotated SVG Body [ADOPTED]

- **Binds:** FR-5, FR-11, NFR-3
- **Prevents:** unreadable Athena geometry, mixed inline XML, and mandatory metadata/geometry sidecars.
- **Rule:** When native typed primitives are impractical, the Athena definition references exactly
  one governed annotated SVG graphic body. It compiles into `RepresentationDefinition` and Graphic
  Primitive exactly like a native body. No duplicate identity in SVG, field merge, or source
  precedence exists.

### AD-6 - Binding Fails Closed [ADOPTED]

- **Binds:** FR-13..FR-19
- **Prevents:** guessed terminal matching and silent device reclassification.
- **Rule:** Binding requires explicit policy selection and compatible anchors. Missing or mismatched
  direction, signal, role, terminal, profile, or element facts produce diagnostics and no occurrence.

### AD-7 - Cabinet Migrates Before Broader Product Work [ADOPTED]

- **Binds:** FR-30..FR-36
- **Prevents:** broad platform expansion on top of an unproven product path.
- **Rule:** Every M34 epic closes a Cabinet-visible outcome paired with structured proof. Cabinet is the only
  customer-facing surface.

### AD-8 - Importers Output Governed Representation Sources [ADOPTED]

- **Binds:** FR-37..FR-40
- **Prevents:** QET or vendor formats entering product runtime.
- **Rule:** Importers may read foreign formats but output canonical native Athena source or canonical
  annotated SVG that passes the same canonical model admission, type, lint, formatter/canonicalizer,
  and compiler gates as human authoring.

### AD-9 - Project And Representation Truth Are Disjoint [ADOPTED]

- **Binds:** FR-3..FR-4, FR-13..FR-17
- **Prevents:** the M33 `semanticType` duplication from returning in another format.
- **Rule:** Project source owns actual engineering instances and ports. Representation definitions
  own reusable visuals and compatibility predicates. Binding policy selects but never mutates either.

### AD-10 - One Source Owns Each Definition [ADOPTED]

- **Binds:** FR-20..FR-22
- **Prevents:** Athena, SVG, XML, Kotlin fixtures, and renderer code competing for one definition.
- **Rule:** Every reusable definition is authored by exactly one Athena declaration. A declaration
  chooses one native graphic body or one referenced annotated SVG graphic body. Duplicate identity
  across Athena declarations fails. XML manifests, SVG root identities, sidecars, Kotlin fixtures,
  and renderer code cannot contribute metadata or apply precedence/field merge.

### AD-11 - RepresentationDefinition Is Canonical [ADOPTED]

- **Binds:** FR-18..FR-23, NFR-6
- **Prevents:** `DrawingSymbolAnatomy`, `RepresentationDefinition`, `RepresentationDescriptor`, and
  a new Element IR becoming competing reusable-definition authorities.
- **Rule:** Extend `RepresentationDefinition` as the canonical compiled Symbol/Element contract. It
  owns kind, Graphic Primitive body, intrinsic composition, anchors, compatibility, slots, lifecycle,
  and provenance. `RepresentationDescriptor` is generated from it for package indexing/resolution and
  cannot be authored. `DrawingSymbolAnatomy` is a compatibility input until migrated and deleted.

### AD-12 - SVG Compiles Through A Closed Safe Subset [ADOPTED]

- **Binds:** FR-24..FR-29
- **Prevents:** script execution, external-resource access, parser abuse, and raw DOM authority.
- **Rule:** Parser initialization fails if namespace awareness and required hardening features cannot
  be set. DOCTYPE, entity expansion, external entities, XInclude, external DTD/schema, scripts, event
  attributes, CSS resources, and external URLs are disabled, with a no-I/O resolver rejecting every
  external request. Exhaustive geometry and node-specific `data-athena-*` grammars, immutable package
  snapshots, aggregate resource limits, and reference validation run before canonical lowering. Raw
  XML/markup and source attributes cannot appear in transport payload types or cross the compiler
  boundary.

### AD-13 - Anchor Coordinates Normalize At Compile Time [ADOPTED]

- **Binds:** FR-7, FR-28
- **Prevents:** renderer-specific transform interpretation and displaced terminals.
- **Rule:** Supported transforms, viewBox, groups, `defs`, and `use` resolve into deterministic visual
  root coordinates. Unsupported, cyclic, ambiguous, or hidden-only targets fail closed.

### AD-14 - Asset Bounds And Document Bounds Are Different [ADOPTED]

- **Binds:** FR-33..FR-34
- **Prevents:** the hard-coded document viewBox defect from being confused with valid asset coordinates.
- **Rule:** A Symbol/Element may own intrinsic bounds and viewBox. Cabinet document bounds derive from
  compiled composition occurrences and routing facts.

### AD-15 - Visual Credibility Is An Acceptance Boundary [ADOPTED]

- **Binds:** FR-35..FR-36, NFR-7..NFR-8
- **Prevents:** architecture proof being reported as customer-demo quality.
- **Rule:** M34 completion requires structured evidence plus desktop and narrow screenshot review for
  enclosure, rails, proportions, terminals, labels, routes, transparency, and drawing density at
  1920x1080 and 1280x900 from a fresh Electron user-data directory. Structured proof reports zero
  fallback, clipping, text overflow, unintended overlap, visible normal-state hitboxes/borders, and
  route endpoints outside their anchors.

### AD-16 - Selection And Occurrence Construction Have Separate Owners [ADOPTED]

- **Binds:** FR-18..FR-20
- **Prevents:** `BindingResolver` and `RepresentationBindingCompiler` independently selecting visuals.
- **Rule:** PresentationProfile constrains context/style/compatibility. BindingManifest joins an
  engineering concept/package to allowed representation packages. `RepresentationBindingRule`, owned
  by `kernel/package-model` and compiled from Athena `binding`, carries typed semantic predicates,
  exact target definition/version, variant, priority, and provenance. BindingResolver alone consumes
  these inputs, selects one generated descriptor/variant, and emits `ResolvedRepresentationSelection`.
  RepresentationPolicy is derived from that result. RepresentationBindingCompiler alone validates
  the canonical definition and constructs RepresentationOccurrence; it never searches or selects. Package versions are exact
  in the immutable snapshot. Candidates must satisfy profile, manifest, projection, lifecycle, and all
  selectors; highest explicit priority wins; an equal highest-priority tie fails as ambiguous. A
  requested variant must exist, otherwise exactly one default variant is required.

### AD-17 - GraphicPrimitive Is The Active Cabinet Visual Vocabulary [ADOPTED]

- **Binds:** FR-24..FR-36
- **Prevents:** `GraphicPrimitive`, `PresentationPrimitive`, direct SVG, and box renderers becoming
  interchangeable active authorities.
- **Rule:** Active M34 Cabinet compilation and rendering consume `GraphicPrimitive` only. M34 extends
  that sealed vocabulary with governed ellipse and normalized path segments needed by safe SVG.
  `PresentationPrimitive`, direct SVG paths, and box renderers admit no new M34 producers and remain
  compatibility-only until their callers are migrated and deletion tests pass. LSP/Electron transport
  types carry typed primitives only and are structurally incapable of carrying raw markup; instrumented
  E2E asserts no raw SVG/HTML parsing sink is invoked for Cabinet content.

### AD-18 - Package Discovery Produces An Immutable Snapshot [ADOPTED]

- **Binds:** FR-18..FR-29, NFR-10..NFR-11
- **Prevents:** renderer filesystem reads, identity races, path escapes, and stale package caches.
- **Rule:** Repository `athena.yaml` declares package source roots. Secure ingestion rejects archive
  traversal, symlinks, junctions/reparse points, and duplicate `(group, artifact, version, definitionId)`
  identities. It uses handle-relative `SecureDirectoryStream` traversal when available; otherwise
  `NOFOLLOW_LINKS` open plus pre/post file-key, size, mtime, and root-containment verification. Missing
  stable identity or unavailable no-follow guarantees reject the package. Only verified bytes enter a
  newly created private immutable snapshot. All compile/hash reads use the staged copy. Compilation caches key on source bytes,
  compiler/schema version, and dependency-lock digest. Runtime and renderer consume the snapshot only.

### AD-19 - Intrinsic And Project Composition Have Separate Owners [ADOPTED]

- **Binds:** FR-2..FR-4, FR-30..FR-36
- **Prevents:** Element child layout and Cabinet occurrence placement becoming competing spatial truth.
- **Rule:** Symbol is atomic. Element V1 composes Symbols and governed local geometry only; nested
  Elements are rejected. Each child has a unique id, explicit translate/rotate/scale transform, and
  unique integer z-order. Exported anchors map explicitly to one child anchor or geometry id. Cycles,
  duplicate ids/z-orders, and unexported connectable anchors fail. Element owns intrinsic layout;
  existing `kernel/drawing-composition` plus the spatial compiler own Cabinet occurrence position,
  rails, lanes, alignment, routing, frame, title block, and document bounds. Generated IR, caches, and
  screenshots are reproducible outputs and never authority.

### AD-20 - M34 Package Compilation Is Offline And Deterministic [ADOPTED]

- **Binds:** NFR-4, NFR-10..NFR-11
- **Prevents:** environment-dependent package resolution and hidden network/runtime dependencies.
- **Rule:** M34 resolves only repository-declared local package snapshots and performs no network
  fetch during compile, bind, render, or E2E. The Windows Electron product and Gradle verification
  consume the same compiled snapshot contract. Remote registries and trust distribution are deferred.

### AD-21 - Element Is Not An Engineering Component [ADOPTED]

- **Binds:** FR-2..FR-4, FR-13..FR-17, NFR-5
- **Prevents:** vendor product knowledge, project truth, and view-specific drawing composition
  collapsing into one Element definition.
- **Rule:** Element owns one reusable visual composition and compatibility predicates only. Existing
  project semantic declarations remain engineering truth. A future Engineering Component System may
  own reusable parameters, functions, constraints, behaviors, and a set of view-specific
  representations, then participate in binding without changing the Symbol/Element contract.

### AD-22 - Public Athena Vocabulary Has A Budget [ADOPTED]

- **Binds:** FR-9..FR-12, NFR-12
- **Prevents:** implementation architecture leaking into the language until Athena becomes XML-like.
- **Rule:** M34 adds only `symbol`, `element`, `profile`, `binding`, and their minimum nested
  domain-facing constructs. `RepresentationDefinition`, descriptor, occurrence, renderer, transport,
  DOM, and IR remain compiler/runtime names and cannot become top-level source declarations.
  `GraphicPrimitive` remains the internal M34 type name; renaming it creates no user value and is
  deferred until a broader renderer-neutral vocabulary requires migration.

## Source And Dependency Rules

```text
Athena parser / AST
  +--> Engineering Semantic Model for project source
  +--> RepresentationDefinition for native representation source
  +--> PresentationProfile + BindingManifest

safe annotated SVG compiler
  -> graphic body facts for the referencing Athena definition

Semantic Model + PresentationProfile + BindingManifest + generated descriptors
  -> BindingResolver
  -> ResolvedRepresentationSelection

Semantic Model + RepresentationDefinition + ResolvedRepresentationSelection
  -> derived RepresentationPolicy
  -> RepresentationBindingCompiler
  -> Representation Occurrence
  -> Graphic Primitive IR
  -> Renderer
```

- No source frontend imports a renderer; duplicate Athena definition identity rejects before admission.
- RepresentationDefinition does not import the semantic model.
- Binding Resolver selects; RepresentationBindingCompiler constructs. Neither copies semantic authority.
- `kernel/drawing-composition` remains the sheet/frame/zone/layout composition authority.
- Renderer consumes only resolved presentation/primitive facts.

## Safe SVG Lowering Profile V1

The SVG root requires only `data-athena-schema="representation/v1"` plus a valid SVG namespace and
viewBox. Definition kind, identity, version, and lifecycle are owned by the referencing Athena
declaration.
Compiler-owned schema rules produce complete attribute source spans and drive IDE support; XSD or a
frontend-maintained schema cannot become a second validation authority.

| SVG Input | Graphic Primitive Result |
| --- | --- |
| `line` | `GraphicPrimitive.Line` |
| `polyline`, `polygon` | `GraphicPrimitive.Polyline` with explicit closed flag/segment |
| `rect` | `GraphicPrimitive.Rectangle` |
| `circle` | `GraphicPrimitive.Circle` |
| `ellipse` | M34 extension `GraphicPrimitive.Ellipse` |
| `path` | M34 extension `GraphicPrimitive.Path` with normalized move/line/cubic/quadratic/arc/close segments |
| `text` | `GraphicPrimitive.Text` with governed text/style attributes |
| `g` and supported transforms | `GraphicPrimitive.Group` / `Transformed` after root-coordinate validation; selected nodes may carry only governed `data-athena-*` contracts |

Every supported element has the normative geometry and `data-athena-*` attribute/value grammar in the
M34 addendum; unknown elements, attributes, metadata fields, namespaces, child nodes, CSS declarations,
and values reject. Numbers are finite
unitless decimals; ids match `[A-Za-z_][A-Za-z0-9_.:-]*`; paint is restricted to `none`,
`currentColor`, hex RGB, or bounded integer `rgb()`; transforms are translate/rotate/scale only;
path commands are normalized `M/L/H/V/C/S/Q/T/A/Z`; text is a plain UTF-8 node with governed
position/font-size/anchor/baseline. URL-valued attributes are rejected
except internal `#id` on the supported `use` path. `style`, CSS `url(...)`, `image`, filter, mask,
`foreignObject`, animation, scripting, external URL, and unsupported paint/resource features fail
closed in V1. Internal `defs`/`use` are allowed only when acyclic, fully resolvable, and expanded
within limits.

Initial hard limits are compiler configuration with non-increasable product caps: 5 MiB per source,
20,000 XML elements per SVG, depth 64, 100,000 normalized path segments per SVG, transform depth 64,
10,000 expanded `use` nodes per SVG, 50 MiB aggregate SVG bytes per package snapshot, and 1,000,000
aggregate normalized path segments per package snapshot. Package caps also allow at most 512 SVG
files, 200,000 XML elements, 100,000 expanded `use` nodes, 1,000,000 emitted primitives, and 5,000,000
deterministic work units (parsed element + normalized segment + expanded use + emitted primitive).
Limit diagnostics include measured and allowed values.

## M30-M33 Migration Seed

| Existing Contract | M34 Action | Boundary |
| --- | --- | --- |
| `RepresentationDefinition` | Extend; canonical | Add Symbol/Element kind, Graphic Primitive body, composition, compatibility, and source provenance. |
| `DrawingSymbolAnatomy` / `M33IecSymbolDefinition` | Replace | Migrate `M33IecSymbolSupport` producers into Athena source; delete after zero non-test callers. |
| `RepresentationDescriptor` | Reuse as derived projection | Generate only from canonical definition; `PackageBackedRepresentationOccurrenceFactory` consumes it only through resolved selection. |
| `PresentationProfileDescriptor` | Reuse | Context/style/compatibility constraint authority only. |
| `BindingManifest` | Reuse/extend | Package/concept admission only; no direct descriptor selection. |
| `RepresentationBindingRule` | Add in `package-model` | Canonical typed input compiled from Athena `binding` declarations. |
| `BindingManifest.policyTags` | Delete selection use | Active resolver reads explicit rules only; fixture adapter translates tags before resolution, then field is deleted after zero active callers. |
| `BindingResolver` | Extend; sole selector | Emit one `ResolvedRepresentationSelection` or diagnostics. |
| `RepresentationPolicy` | Reclassify as derived | Created only from resolver result, never separate source authority. |
| `RepresentationBindingCompiler` | Reuse; sole occurrence builder | Validate resolved selection and create occurrence; no searching. |
| `GraphicPrimitive` | Extend; canonical visual vocabulary | Add ellipse/path and remain sole active Cabinet renderer input. |
| `PresentationPrimitive` | Replace | Migrate `M33CabinetPresentationFactDeriver`, `M33CabinetDrawingCompositionDeriver`, and `AthenaPresentationSessionProtocol`; then delete. |
| `M33CabinetPackageSet` XML loader | Delete | Migrate active compiler/product callers to compiled snapshot loader; no compatibility adapter is required. |
| M32/M33 XML assets | Delete | Remove product/runtime and fixture authority; retain only deletion evidence. |
| Direct SVG/box renderer paths | Replace | `GraphicPrimitiveSvgAdapter` is the only Cabinet SVG emitter; E2E instruments and rejects every direct markup/box fallback sink before deletion. |

## Structural Seed

```text
kernel/
  language/                # shared Athena AST declarations and source spans
  representation-model/   # RepresentationDefinition, derived policy, occurrences, GraphicPrimitive
  package-model/           # package/profile/manifest/descriptor/binding-rule/resolved-selection contracts
  package-runtime/         # immutable snapshot loader, sole BindingResolver, compatibility adapters
  compiler/                # native Athena declarations + safe SVG graphic compiler, descriptor projection
  drawing-composition/     # existing Cabinet sheet/frame/zone/layout authority
  svg-renderer/            # paint-only GraphicPrimitive adapter

examples/m34/sample-project/
  packages/                # ordinary *.athena definitions, policies, and referenced governed SVG resources
  src/                     # project engineering truth
```

## Deferred

| Deferred | Revisit Condition |
| --- | --- |
| Full QET/annotated-SVG converter | Athena representation grammar and safe SVG profile are stable and Cabinet proof passes. |
| Engineering Component System | M34 proves visual Elements; a later milestone can model reusable product parameters, functions, constraints, behaviors, and multi-view representation sets. |
| GraphicPrimitive naming review | A second renderer-neutral output or non-graphic drawing domain proves the current name is materially limiting. |
| Symbol editor UI | Round-trip source and diagnostics are stable. |
| Vendor marketplace | Package trust, signing, version conflict, and sandboxing are funded. |
| Additional product views | Cabinet meets M34 visual and E2E acceptance. |

## Corrective Architecture Decisions - Professional Control Drawing

The following decisions were approved on 2026-07-26. They supersede AD-7 and every Cabinet-only
completion statement for final M34 acceptance while retaining the valid typed representation,
package snapshot, binding, safe SVG, and Graphic Primitive IR boundaries.

### AD-22 - One Professional Control Drawing Is The M34 Product Surface [ADOPTED]

- **Rule:** Final M34 acceptance uses one focused rolling-shutter control drawing based on
  `draft/screenshort/equipement_d'un_volet_roulant.png`.
- **Rule:** Cabinet, Documentation, and legacy Schematic selectors do not compete on the M34 product
  surface. The workbench exposes the single accepted drawing.
- **Prevents:** three partial views and architecture-only evidence being presented as product quality.

### AD-23 - Engineering Functions Partition One Physical Component [ADOPTED]

- **Rule:** `EngineeringFunction` is a generic, stable semantic child of one
  `EngineeringComponent`. It has a typed role and references existing component-owned ports.
- **Rule:** Device identity remains physical and canonical. Functions such as coil, main contact
  group, NO contact, and NC contact do not become duplicate devices.
- **Rule:** Terminal identity remains an authored semantic-port fact. Function definitions cannot
  invent or reclassify terminals.
- **Rule:** The reusable Engineering Component System remains deferred; M34 admits only project
  functional partitioning needed by the accepted drawing.
- **Prevents:** QET Master/Slave semantics, duplicate KM1/KM2 devices, and view-owned engineering truth.

### AD-24 - Function-Aware Occurrences Preserve Physical Subject Identity [ADOPTED]

- **Rule:** Representation selection may target an Engineering Function role. Each resulting
  occurrence identifies both its function and canonical physical component.
- **Rule:** Existing repeated-subject, interaction-index, and cross-reference infrastructure is
  extended rather than replaced.
- **Rule:** A device port binds only to an anchor of the occurrence for the function that references
  that port.
- **Prevents:** one-occurrence-per-device limits and ambiguous terminal-to-symbol routing.

### AD-25 - Explicit Placement Is Projection Intent, Never Engineering Geometry [ADOPTED]

- **Rule:** `layout <view> { place <subject-or-function> at (<column>, <row>) ... }` lowers to typed
  hard placement constraints in drawing-grid coordinates. Orientation is occurrence intent.
- **Rule:** intrinsic Symbol/Element geometry remains package-local; frame, zones, title block,
  routes, and document bounds remain composition facts.
- **Rule:** hard placement and relative constraints are validated together and fail on conflict.
- **Prevents:** list-index layout, pixel coordinates in Engineering IR, and renderer inference.

### AD-26 - Native Drawing Syntax Stays Small [ADOPTED]

- **Rule:** Native `graphic` adds only line, polyline, arc, circle, rectangle, and dynamic label-slot
  forms that lower to existing Graphic Primitive IR contracts.
- **Rule:** no public general path/CAD constraint language is added. Complex geometry uses one
  package-local governed SVG graphic body.
- **Prevents:** an SVG clone in Athena and line-only IEC symbols.

### AD-27 - QET Is Evidence, Not Authority [ADOPTED]

- **Reinterpret:** terminal position/orientation as representation anchors; occurrence x/y as layout
  intent; conductor endpoints as semantic-port bindings; repeated device graphics as function-aware
  occurrences; border/inset as composition facts.
- **Reject:** QET XML runtime, `.qet`/`.elmt` product loading, page ownership, Master/Slave classes,
  scene terminal ids, and label-string linking.
- **Prevents:** importing ECAD architecture into EngineeringOS.

### Corrected Pipeline

```text
Engineering Component
  -> Engineering Functions -> existing semantic ports
  -> projection occurrence intent + drawing-grid placement
  -> profile/binding resolver
  -> Element -> Symbol -> native primitive or governed SVG body
  -> Graphic Primitive IR + routing/composition facts
  -> Presentation transport
  -> renderer-only workbench surface
```
| Standards compliance claim | Audited IEC/ANSI/DIN/GB content and validation work exists. |
