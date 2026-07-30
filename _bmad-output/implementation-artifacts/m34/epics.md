---
stepsCompleted:
  - 1
  - 2
  - 3
  - 4
inputDocuments:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/prd.md
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-24-m34/addendum.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-24-m34/ARCHITECTURE-SPINE.md
title: Athena M34 Epics And Stories
status: ready-for-story-creation
created: '2026-07-24'
updated: '2026-07-24'
---

# Athena M34 - Epic Breakdown

## Overview

M34 delivers one canonical representation compiler with native Athena graphic bodies for simple
Symbols/Elements, Athena definitions that reference governed annotated SVG graphic resources for
complex Symbols/Elements, Athena Profiles/Bindings, deterministic Cabinet binding, and one
professional Cabinet customer-demo path. Every definition has one Athena metadata authority and
every epic closes a Cabinet-visible outcome with structured proof.

## Requirements Inventory

### Functional Requirements

- FR-1: Define atomic typed Symbol contracts.
- FR-2: Define reusable typed Element composition.
- FR-3: Require compatible metadata on every connectable anchor without declaring project ports.
- FR-4: Prevent representation definitions from owning project instances or classification.
- FR-5: Accept governed annotated SVG through Athena `graphic svg` references; no XML runtime authority.
- FR-6: Limit `data-athena-*` to typed reusable representation contracts on governed nodes.
- FR-7: Reject invalid SVG ids, annotations, typed values, and references.
- FR-8: Forbid unmarked geometry inference and all project/policy truth in SVG.
- FR-9: Add Symbol, Element, Profile, and Binding declarations to ordinary `*.athena`.
- FR-10: Support native typed Athena `graphic` bodies through the full language toolchain.
- FR-11: Enforce one Athena metadata authority and one graphic body form per definition.
- FR-12: Add ANTLR4, tree-sitter, SVG, LSP, lint, formatter/canonicalizer, outline, completion, and highlighting support.
- FR-13: Preserve project `.athena` instance/port/connection/layout SSOT.
- FR-14: Limit reusable definitions to representation contracts.
- FR-15: Prevent representation policy from mutating device semantics.
- FR-16: Fail closed on missing/incompatible Element anchors.
- FR-17: Validate direction, signal, role, and terminal compatibility.
- FR-18: Preserve one typed profile/manifest/rule/resolver/policy/compiler chain.
- FR-19: Compile Athena Binding declarations into typed `RepresentationBindingRule` inputs.
- FR-20: Make BindingResolver sole selector and RepresentationBindingCompiler sole occurrence builder.
- FR-21: Make one governed source the sole authority per definition and retire `policyTags` selection.
- FR-22: Extend canonical `RepresentationDefinition`; derive `RepresentationDescriptor`.
- FR-23: Enforce the M30-M33 Reuse/Extend/Replace/Delete migration map.
- FR-24: Parse annotated SVG with fail-closed XML hardening and exhaustive geometry/metadata grammars.
- FR-25: Reject active, external, unknown, and unsafe SVG content.
- FR-26: Enforce per-file and aggregate deterministic resource budgets.
- FR-27: Stage external assets through race-resistant repository-confined acquisition.
- FR-28: Normalize supported SVG coordinates and transforms at compile time.
- FR-29: Make transport incapable of carrying raw markup and prove no Cabinet markup sink executes.
- FR-30: Render active Cabinet through canonical compiled representation facts.
- FR-31: Remove XML manifests from active Cabinet authority.
- FR-32: Prove definition, policy, anchor, label, and provenance for every visible component.
- FR-33: Fail Cabinet smoke on fallback, unresolved, duplicated, off-screen, or hard-coded document facts.
- FR-34: Separate intrinsic asset viewBox from derived Cabinet document viewBox.
- FR-35: Deliver professional Cabinet enclosure, rails, elements, terminals, labels, routes, and frame.
- FR-36: Capture deterministic desktop/narrow Electron and structured visual evidence.
- FR-37: Remove all M32/M33 XML product/runtime paths and record deletion evidence.
- FR-38: Keep QET conversion offline and deferred until the Athena contract is stable.
- FR-39: Require every importer/AI to output canonical Athena source and, when needed, one referenced governed annotated SVG resource.
- FR-40: Reject imported connectable points until governed by a supported source frontend and compiled.
- FR-41: Require RED/GREEN, AC evidence, three-layer review, and final polish/purge in every story.
- FR-42: Require a Cabinet-visible outcome and structured proof from every epic.
- FR-43: Run Cabinet-only E2E and M34 retrospective/handoff.

### NonFunctional Requirements

- NFR-1: Athena compilers own reusable metadata interpretation across both governed frontends.
- NFR-2: Annotated SVG is untrusted compile-time representation source, never project or renderer authority.
- NFR-3: Each reusable definition remains one understandable authoritative artifact with no required sidecar.
- NFR-4: Binding and rendering are deterministic and fail closed.
- NFR-5: Downstream layers never infer engineering meaning from visual structure.
- NFR-6: Existing policy/representation/primitive boundaries are extended, not duplicated.
- NFR-7: Cabinet is the only customer-facing M34 view.
- NFR-8: Industrial visual density uses transparent normal chrome and hidden hitboxes.
- NFR-9: Repository text remains UTF-8 and evidence remains derived.
- NFR-10: Packages come only from repository-declared immutable snapshots.
- NFR-11: Caches are content-addressed, purgeable, and reproducible.
- NFR-12: Public Athena representation vocabulary remains small and excludes compiler/runtime terms.

### Additional Requirements

- `RepresentationDefinition` is the only canonical compiled reusable representation model.
- `RepresentationDescriptor` is generated; independent descriptor authoring is rejected.
- `RepresentationBindingRule` belongs to `kernel/package-model` and is the typed input to BindingResolver.
- Legacy `BindingManifest.policyTags` selection has an explicit active-caller deletion gate.
- Element V1 composes Symbols and local geometry only; nested Elements are rejected.
- Native typed Athena `graphic` bodies are used when concise textual geometry is clearer and easier to maintain.
- Shape-heavy or design-tool-authored visuals use `graphic svg "..."` from an Athena definition with
  `data-athena-*` only on contract-bearing SVG nodes; compiled source attributes never become runtime authority.
- A definition identity cannot merge, override, or duplicate fields between Athena and SVG.
- `kernel/drawing-composition` remains Cabinet sheet/frame/zone/layout authority.
- `GraphicPrimitive` is the sole active Cabinet visual vocabulary; `PresentationPrimitive` has no new producers.
- SVG V1 has normative element, `data-athena-*`, value, URL, path, text, transform, and budget rules.
- Package staging uses no-follow acquisition, stable file identity checks, and immutable private copies.
- M34 performs no network package fetch during compile, bind, render, or E2E.
- No starter template or new parallel module graph is introduced.
- Element remains a visual representation; Engineering Component definitions are deferred.
- Public Athena source does not expose descriptor, occurrence, renderer, transport, DOM, or IR declarations.

### UX Design Requirements

No separate M34 UX contract exists. FR-35, FR-36, and NFR-8 are the binding Cabinet visual and
interaction requirements.

### FR Coverage Map

| Requirement | Story Coverage |
| --- | --- |
| FR-1..FR-4 | 1.1, 1.3 |
| FR-5..FR-8 | 2.1, 2.4 |
| FR-9..FR-12 | 1.2, 1.3, 2.3 |
| FR-13..FR-15 | 1.1, 3.2 |
| FR-16..FR-17 | 3.2 |
| FR-18..FR-21 | 3.1 |
| FR-22..FR-23 | 1.1, 3.3, 3.4 |
| FR-24..FR-29 | 2.1, 2.2, 3.4 |
| FR-30..FR-34 | 3.3, 3.4 |
| FR-35..FR-36 | 4.1, 4.2, 4.3 |
| FR-37 | 3.3, 4.3 |
| FR-38..FR-40 | 2.4 |
| FR-41 | Every story |
| FR-42 | 1.3, 2.4, 3.4, 4.3 |
| FR-43 | 4.3 and epic-4 retrospective |

### NFR Coverage Map

| Requirement | Story Coverage |
| --- | --- |
| NFR-1 | 1.1, 1.2, 2.3, 2.4, 3.1, 3.2 |
| NFR-2 | 2.1, 2.4, 3.3, 3.4 |
| NFR-3 | 1.2, 1.3, 2.4 |
| NFR-4 | 1.3, 2.1, 3.1, 3.2, 3.3, 3.4, 4.2, 4.3 |
| NFR-5 | 1.1, 2.1, 3.2, 3.4 |
| NFR-6 | 1.1, 3.1, 3.3, 3.4 |
| NFR-7 | 1.3, 2.4, 3.4, 4.1, 4.2, 4.3 |
| NFR-8 | 1.3, 4.1, 4.2, 4.3 |
| NFR-9 | Every story; 4.3 final audit |
| NFR-10 | 2.2, 3.3, 4.3 |
| NFR-11 | 2.2, 4.3 |
| NFR-12 | 1.2, 2.3 |

## Epic List

### Epic 1: Author One Typed Element End To End

A library author or AI can write one Symbol and one composed Element in Athena, compile them into the
canonical model, and see the Element rendered in Cabinet.

**FRs covered:** FR-1..FR-4, FR-6, FR-9..FR-10, FR-13..FR-15, FR-22..FR-23,
FR-30, FR-41..FR-42.

### Epic 2: Author Complex Visual Assets Safely

A vendor, user, importer, or AI can author one Athena Element that references a complex governed
annotated SVG graphic resource, receive deterministic diagnostics, and see one safely compiled
vendor-style Element in Cabinet.

**FRs covered:** FR-5..FR-12, FR-24..FR-29, FR-38..FR-42.

### Epic 3: Resolve The Entire Cabinet Without XML Authority

An engineer can open the M34 sample and see every device selected and bound through typed Athena
Profile/Binding rules, with no XML, legacy tag selection, raw markup, or fallback authority.

**FRs covered:** FR-13..FR-23, FR-29..FR-34, FR-37, FR-41..FR-42.

### Epic 4: Demonstrate A Professional Cabinet Product Surface

A customer can open Athena and inspect one visually credible, dense, deterministic Cabinet drawing
at desktop and narrow sizes with complete structured evidence.

**FRs covered:** FR-30..FR-37, FR-41..FR-43.

## Epic 1: Author One Typed Element End To End

### Story 1.1: Establish The Canonical Representation Contract

As an Athena platform developer,
I want one canonical compiled representation model and explicit migration ownership,
So that later Symbol and Element features cannot create competing truths.

**Acceptance Criteria:**

**Implements:** FR-1, FR-3..FR-4, FR-13..FR-15, FR-22..FR-23, FR-41.

**Given** the existing M30-M33 representation contracts
**When** the M34 model is introduced
**Then** `RepresentationDefinition` owns Symbol/Element kind, intrinsic composition, Graphic Primitive
body, anchor compatibility, slots, lifecycle, version, and provenance
**And** `RepresentationDescriptor` is generated and cannot be independently authored.

**Given** a representation definition attempts to own a project device, port, connection, or classification
**When** validation runs
**Then** compilation fails with stable source-spanned diagnostics.

**Given** `DrawingSymbolAnatomy`, M33 symbol models, and legacy primitive paths
**When** migration tests run
**Then** each is classified Reuse/Extend/Replace/Delete with named callers and deletion gates.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 1.2: Compile A Typed Symbol With Native Geometry

As a Symbol library author,
I want to write a Symbol in ordinary Athena source with native typed geometry,
So that the compiler and IDE can validate it before it enters a package.

**Acceptance Criteria:**

**Implements:** FR-1, FR-3, FR-6, FR-9..FR-10, FR-12, FR-41.

**Given** a valid `symbol` declaration with a typed Athena `graphic` body
**When** ANTLR4 parsing, tree-sitter parity, AST lowering, semantic type checking, and lint run
**Then** one canonical `RepresentationDefinition` and Graphic Primitive body are produced.

**Given** missing identity/version/anchors, duplicate primitive ids, unbound `ref`, invalid
coordinates/styles/bounds, or unsupported geometry
**When** compilation runs
**Then** stable source-spanned diagnostics are emitted and no definition enters the package snapshot.

**Given** the source is human- or AI-authored
**When** formatter and lint run twice
**Then** output and diagnostics are deterministic and idempotent.

**Given** the public M34 grammar
**When** its declarations and keywords are audited
**Then** it exposes only `symbol`, `element`, `profile`, `binding`, and their minimum nested
domain-facing constructs, with no descriptor/occurrence/renderer/transport/DOM/IR declarations.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 1.3: Compose And Display One Typed Element

As a Cabinet library author,
I want to compose Symbols into one typed Element and display it in Cabinet,
So that Athena proves a reusable visual component end to end.

**Acceptance Criteria:**

**Implements:** FR-2..FR-3, FR-9, FR-14, FR-30, FR-41..FR-42.

**Given** valid child Symbols, explicit child ids/transforms/z-order, and exported anchors
**When** an `element` declaration compiles
**Then** deterministic intrinsic composition is stored in `RepresentationDefinition`.

**Given** nested Elements, duplicate child ids/z-order, cycles, or unexported connectable anchors
**When** lint and compilation run
**Then** compilation fails closed with stable diagnostics.

**Given** the Epic 1 Cabinet fixture
**When** it opens through the existing occurrence, drawing-composition, Graphic Primitive, and renderer path
**Then** the compiled Element is visible with transparent normal chrome and structured source/anchor proof.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

## Epic 2: Author Complex Visual Assets Safely

### Story 2.1: Compile A Referenced Governed SVG Graphic Body

As a vendor asset author,
I want one Athena Element to reference a complex SVG parsed through a closed safe geometry and node
annotation profile,
So that professional visuals stay maintainable without executing code or bypassing Athena governance.

**Acceptance Criteria:**

**Implements:** FR-5..FR-8, FR-11, FR-24..FR-26, FR-28, FR-41.

**Given** one Athena Symbol or Element declaration with `graphic svg "./asset.svg"`, an SVG root with
required `data-athena-schema="representation/v1"`, normative M34 SVG elements, values, transforms,
text, path, internal-use rules, and node-specific `data-athena-*` schema
**When** the declaration and referenced SVG compile together
**Then** they lower to exactly one canonical `RepresentationDefinition` and governed Graphic
Primitive body including ellipse and normalized path segments, with no duplicate identity/version/kind
in SVG.

**Given** DTD/entities, XInclude, script/events, style/CSS URLs, external/data/file URLs, image/filter/mask,
SVG metadata/namespaces, unknown geometry or `data-athena-*` fields/values, wrong-node annotations,
missing/duplicate definition or SVG ids, unresolved marked references, cyclic use, or malformed path data
**When** parsing runs
**Then** the no-I/O parser fails closed and no raw markup leaves the compiler.

**Given** valid geometry, ids, and references without explicit governed annotations
**When** compilation runs
**Then** no anchor, port, direction, signal, identity, label, hotspot, or binding fact is inferred.

**Given** Athena-owned identity/version/kind and selected-node anchor/label/hotspot annotations
**When** semantic validation runs
**Then** exact source-spanned typed contracts are produced, while project devices, actual ports,
connections, classification, Profile, Binding, and package policy remain structurally forbidden.

**Given** the same `(library, identity, version)` appears in more than one Athena declaration, the
same declaration contains both native `graphic` and `graphic svg`, an SVG root duplicates
identity/version/kind, or the SVG schema is unknown
**When** package admission runs
**Then** the entire definition batch fails with complete source spans and no format precedence or partial output.

**Given** per-file or package aggregate limits are exceeded
**When** compilation runs
**Then** deterministic diagnostics report measured and allowed bytes/files/elements/segments/use/primitives/work units.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 2.2: Stage External Assets Into An Immutable Package Snapshot

As an Athena package consumer,
I want external geometry acquired into an immutable repository-confined snapshot,
So that packages cannot escape roots or change during compilation.

**Acceptance Criteria:**

**Implements:** FR-11, FR-26..FR-27, FR-41.

**Given** repository-declared package roots and valid regular files
**When** staging runs
**Then** no-follow acquisition, component checks, pre/post file identity checks, hashing, and private snapshot copy succeed.

**Given** absolute/traversal paths, archive traversal, symlinks, junctions/reparse points, changed identity,
duplicate package/definition identity, or unsupported secure-open guarantees
**When** staging runs
**Then** the package is rejected before parse or cache publication.

**Given** the same source bytes, compiler/schema version, and dependency-lock digest
**When** compilation repeats offline
**Then** snapshot/cache identity and compiled output are reproducible without renderer filesystem reads.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 2.3: Complete IDE Support For Governed Representation Sources

As a human or AI representation author,
I want full IDE/compiler feedback for native Athena representation declarations and referenced governed SVG resources,
So that generated engineering materials can be corrected before compilation.

**Acceptance Criteria:**

**Implements:** FR-9..FR-12, FR-41.

**Given** valid or invalid native Athena representation source or referenced annotated SVG resource
**When** ANTLR4 and tree-sitter parse it
**Then** Athena syntax remains parser-parity checked and the SVG frontend recognizes only the governed
geometry and `data-athena-*` profile.

**Given** an open native Athena representation file or referenced annotated SVG resource
**When** LSP features run
**Then** outline, completion, highlighting, go-to identity, formatter, and stable source-spanned lint diagnostics
cover Symbols, Elements, children, anchors, labels, profiles, bindings, selectors, variants, governed
SVG annotations, and geometry references.

**Given** SVG completion and validation rules
**When** IDE and compiler parity is audited
**Then** both derive from the compiler-owned versioned schema; no XSD or frontend schema is a second authority.

**Given** parser/tree-sitter/LSP fixtures and an AI-generated corpus
**When** parity and determinism tests run
**Then** accepted/rejected cases and canonical formatting match across repeated clean builds.

**Given** compiler/runtime implementation type names
**When** completion, highlighting, outline, and grammar fixtures run
**Then** those names are not offered or accepted as Athena source declarations.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 2.4: Prove The Importer And AI Authoring Boundary

As an importer or AI agent,
I want every generated asset to become one canonical governed representation source,
So that foreign or generated materials use the same compiler contract as native work.

**Acceptance Criteria:**

**Implements:** FR-5..FR-8, FR-38..FR-42.

**Given** a QET `.elmt`, vendor export, or AI-generated complex-visual fixture
**When** the M34 import boundary is exercised
**Then** output is canonical Athena representation source and, for complex geometry, one referenced
governed annotated SVG resource, never a foreign runtime schema or duplicate metadata sidecar.

**Given** the QET analysis fixture
**When** product dependencies are inspected
**Then** no QET runtime, full converter, or foreign schema dependency has been introduced in M34.

**Given** an imported connectable point without an explicit native-Athena or `data-athena-*` anchor contract
**When** generated source compiles
**Then** it is rejected until role/direction/signal compatibility is authored and validated.

**Given** one complex vendor-style generated Element
**When** it passes formatter/canonicalizer, lint, compiler, safe geometry/metadata, and package snapshot validation
**Then** it is visible in the Epic 2 Cabinet proof with no foreign/runtime metadata dependency.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

## Epic 3: Resolve The Entire Cabinet Without XML Authority

### Story 3.1: Compile Deterministic Profile And Binding Rules

As a Cabinet package author,
I want typed Profile and Binding declarations with deterministic resolution,
So that one governed Element is selected without hidden tag or file-order behavior.

**Acceptance Criteria:**

**Implements:** FR-18..FR-21, FR-41.

**Given** valid Athena `profile` and `binding` declarations
**When** compilation runs
**Then** typed profile, manifest admission, and `RepresentationBindingRule` values are produced in `package-model`.

**Given** exact snapshot versions and candidate rules
**When** BindingResolver evaluates profile/manifest/projection/lifecycle/selectors/priority/variant
**Then** exactly one `ResolvedRepresentationSelection` is emitted or stable missing/ambiguous diagnostics result.

**Given** legacy `BindingManifest.policyTags`
**When** the active M34 path runs
**Then** no active selector reads them; fixture adapters translate to explicit rules before resolution and deletion-gate tests track remaining callers.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 3.2: Bind Project Ports To Element Anchors

As an engineer,
I want authored device ports validated against selected Element anchors,
So that Cabinet connections remain semantically correct without duplicate port truth.

**Acceptance Criteria:**

**Implements:** FR-13..FR-17, FR-20, FR-41.

**Given** one resolved Element and authored project ports
**When** RepresentationBindingCompiler constructs an occurrence
**Then** every terminal binding validates role, direction, signal, terminal, labels, and source identity.

**Given** missing, ambiguous, or incompatible anchors or labels
**When** binding runs
**Then** no occurrence or renderer fallback is produced and stable diagnostics identify both project and representation provenance.

**Given** a representation rule attempts to add/reclassify a device or port
**When** semantic authority validation runs
**Then** compilation fails before mutation, occurrence creation, or rendering.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 3.3: Migrate The M34 Cabinet Package From XML To Athena

As an engineer opening the sample,
I want all visible Cabinet components resolved from compiled governed package sources,
So that XML manifests and Kotlin sample facts are no longer product authority.

**Acceptance Criteria:**

**Implements:** FR-21..FR-23, FR-30..FR-32, FR-34, FR-37, FR-41.

**Given** the M34 sample project
**When** package discovery and compilation run
**Then** every visible component identifies one native-Athena or annotated-SVG definition, generated descriptor, binding rule,
resolved variant, anchor/label bindings, exact version, and source provenance.

**Given** M32/M33 XML manifests and sample-specific Kotlin definitions
**When** active Cabinet authority is inspected
**Then** zero product-path reads remain; any fixture-only adapter is isolated, ledgered, and cannot ship in the M34 sample.

**Given** intrinsic component bounds and project layout facts
**When** Cabinet composition runs
**Then** Element intrinsic transforms remain separate from `drawing-composition` occurrence placement and derived document bounds.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 3.4: Make Graphic Primitive The Only Cabinet Render Path

As an Athena product maintainer,
I want one typed visual transport and renderer input,
So that raw SVG, PresentationPrimitive, and fallback boxes cannot silently return.

**Acceptance Criteria:**

**Implements:** FR-22..FR-23, FR-29..FR-34, FR-41..FR-42.

**Given** all M34 Cabinet occurrences and drawing-composition facts
**When** presentation payloads are emitted
**Then** only typed Graphic Primitive values reach LSP/Electron transport and `GraphicPrimitiveSvgAdapter`.

**Given** `PresentationPrimitive`, direct SVG markup, legacy box rendering, or raw markup transport
**When** active-caller and instrumented Electron tests run
**Then** no active M34 producer/sink exists and named deletion gates pass or remain explicitly ledgered.

**Given** the complete Epic 3 sample
**When** Cabinet opens
**Then** every component and route is visible with no XML/raw-markup/fallback authority and structured
proof includes derived document viewBox and zero hard-coded document bounds.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

## Epic 4: Demonstrate A Professional Cabinet Product Surface

### Story 4.1: Deliver The Governed Cabinet Element Set

As a Cabinet engineer,
I want a compact professional set of typed reusable components,
So that the demo represents recognizable industrial equipment instead of generic boxes.

**Acceptance Criteria:**

**Implements:** FR-30, FR-32, FR-35, FR-41..FR-42.

**Given** the M34 Athena package contains concise native definitions and complex annotated-SVG definitions
**When** the governed set compiles through both source frontends
**Then** it provides governed enclosure, DIN rail, protective device, switch/control, relay/contactor,
terminal block, power supply, actuator/load, label, and route-channel Symbols/Elements required by the sample.

**Given** each connectable Element
**When** contract validation runs
**Then** required anchors, role/direction/signal compatibility, labels, bounds, version, lifecycle, and provenance are complete.

**Given** the typed set is rendered
**When** visual structure is inspected
**Then** component proportions, terminals, line weights, label hierarchy, and transparent normal chrome
match the approved industrial Cabinet criteria without standards-compliance claims.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 4.2: Compose The Professional Cabinet Drawing

As a customer reviewing Athena,
I want the complete sample arranged as a credible Cabinet drawing,
So that the first impression is a professional engineering product.

**Acceptance Criteria:**

**Implements:** FR-30, FR-33..FR-36, FR-41.

**Given** the governed compiled Element set and project layout facts
**When** spatial and `drawing-composition` compilers run
**Then** enclosure, rails, devices, terminals, labels, route channels, frame, and title block use
derived bounds and stable industrial spacing.

**Given** desktop and narrow available viewports
**When** Cabinet fits and centers
**Then** no visible item is clipped/off-screen, no text overflows, no unintended component overlap exists,
no route endpoint leaves its anchor, and no normal-state hitbox/border/background is visible.

**Given** hover, selection, or DnD is active
**When** interaction state changes
**Then** dotted interaction borders appear only for that state and disappear on return to normal.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 4.3: Prove And Hand Off The M34 Cabinet Product

As an Athena product owner,
I want repeatable customer-demo evidence and a truthful retrospective,
So that M34 completion is based on observed behavior rather than architecture claims.

**Acceptance Criteria:**

**Implements:** FR-30..FR-37, FR-41..FR-43.

**Given** a clean M34 sample and temporary Electron user-data directory
**When** E2E runs at 1920x1080 and 1280x900
**Then** Cabinet is the deterministic active view, LSP reports zero diagnostics, canvas pixels are
nonblank, screenshots are fresh, and structured visual/authority proof passes.

**Given** visual and authority proof
**When** assertions run
**Then** fallback, clipping, overflow, unintended overlap, normal hitbox/border visibility, out-of-anchor
routes, XML authority, raw markup sinks, hard-coded document viewBox, and ungoverned assets are all zero.

**Given** all M34 stories and migration gates
**When** retrospective and handoff run
**Then** SSOT recovery, AI/type-safe authoring, SVG safety, XML/policyTags retirement, Cabinet credibility,
open ledger items, and readiness for the next milestone are recorded without unsupported claims.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** the entire M34 source, tests, fixtures, docs, screenshots, generated outputs, process/session state,
and workspace are deeply reviewed; stale/duplicate artifacts are removed; all open actions are closed or
truthfully owned; and RED/GREEN, AC-to-evidence, three-layer review, and encoding audit are recorded.

## Epic 5: Close The Professional Control Drawing Gap

Epic 5 is the approved corrective continuation of Story 4.3. It retains the typed Symbol/Element,
package, binding, safe SVG, and Graphic Primitive IR work, but replaces Cabinet-only product
acceptance with one professional rolling-shutter control drawing.

### Story 5.1: Freeze The Professional Drawing Product Contract

As an Athena product owner,
I want one executable visual contract and one focused workbench surface,
So that implementation cannot substitute another Cabinet strip or architecture proof.

**Acceptance Criteria:**

**Implements:** FR-44, FR-49..FR-50, NFR-13.

**Given** the approved screenshot and QET reference project
**When** target-contract tests inspect them
**Then** the contract records 17 columns, 8 rows, required sheet regions, required device/function
families, terminal-label families, title metadata, and visual tolerance without importing QET runtime.

**Given** the M34 workbench
**When** the sample opens
**Then** one professional control-drawing surface is primary and Cabinet/Documentation selector chaos
does not appear on that surface.

**Given** sample semantic source
**When** authority tests run
**Then** enclosure, rails, route channels, frame, zones, and title labels are not authored as devices,
and no active layout role derives from identifier prefixes.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, target evidence, docs, generated outputs, and workspace state are deeply
reviewed; stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer
review are recorded.

### Story 5.2: Model Functions And Deterministic Drawing Placement

As an engineering author,
I want one physical device to expose typed functional units and each occurrence to have governed
drawing placement,
So that coils, contacts, and terminals are semantically correct and spatially deterministic.

**Acceptance Criteria:**

**Implements:** FR-45..FR-46, FR-48..FR-49, NFR-14.

**Given** a device with existing authored ports
**When** function declarations compile
**Then** stable Engineering Functions reference those ports, preserve physical component identity and
terminal numbers, and reject missing, duplicate, cross-device, or multiply-owned port references.

**Given** a control-drawing layout
**When** function/component occurrences use typed `at` grid placement and orientation
**Then** ANTLR4, Athena AST, tree-sitter, LSP, semantic lowering, and layout constraint compilation
agree deterministically and diagnose conflicts with source spans.

**Given** KM1/KM2 coil, main-contact, NO-contact, and NC-contact functions
**When** projection and binding run
**Then** multiple occurrences share one canonical physical device identity while each binds only its
declared ports and publishes inspectable cross-reference evidence.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 5.3: Compile The Rolling-Shutter IEC Package And Semantic Sample

As an engineer opening the M34 example,
I want recognizable IEC-style symbols bound to a truthful rolling-shutter semantic model,
So that every visible occurrence is compiled from governed Athena material.

**Acceptance Criteria:**

**Implements:** FR-44..FR-49.

**Given** native Athena graphic declarations
**When** line, polyline, arc, circle, rectangle, and dynamic label-slot forms compile
**Then** they lower through the existing canonical Graphic Primitive IR with deterministic type,
bounds, style, anchor, and label diagnostics.

**Given** the dedicated M34 sample
**When** repository and semantic compilation run
**Then** it contains truthful source, breaker, fuse-disconnector, transformer, reversing contactors,
coils, NO/NC contacts, push buttons, limit switches, lamps, terminals, motor, earth, and connections
with preserved terminal identities and no fake drawing-structure devices.

**Given** profile and function-aware binding rules
**When** package resolution runs
**Then** every required occurrence resolves exactly one package-local Element/Symbol and no name,
file order, QET path, fallback box, or Kotlin fixture selects representation.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 5.4: Compose And Route The Professional Control Drawing

As a customer reviewing Athena,
I want the complete rolling-shutter circuit composed as a professional engineering sheet,
So that Athena demonstrates a credible product rather than a row of symbols.

**Acceptance Criteria:**

**Implements:** FR-44, FR-46, FR-48..FR-50.

**Given** function-aware occurrences and explicit grid placement
**When** composition runs
**Then** the 17x8 frame, coordinate bands, title block, power region, control region, device labels,
terminal labels, and all occurrences use derived typed facts with stable document bounds.

**Given** semantic connections and bound terminal anchors
**When** routing runs
**Then** orthogonal conductors terminate on the correct anchors, preserve junction dots and separate
crossing semantics, avoid symbol/label interiors, and contain no renderer-generated engineering inference.

**Given** the focused Theia surface at desktop and narrow viewport sizes
**When** fit/center and interaction run
**Then** the sheet is readable, correctly framed, nonblank, unclipped, and free of normal-state boxes,
toolbar clutter, unintended overlaps, and off-anchor routes.

**Given** all previous acceptance criteria are green
**When** the mandatory final polish/purge task runs
**Then** source, tests, fixtures, docs, generated outputs, and workspace state are deeply reviewed;
stale/duplicate artifacts are removed; and RED/GREEN, AC-to-evidence, and three-layer review are recorded.

### Story 5.5: Prove Polish And Hand Off M34

As an Athena product owner,
I want fresh real-product evidence against the approved target,
So that M34 closes only when the customer-visible result is truthful.

**Acceptance Criteria:**

**Implements:** FR-44..FR-50, NFR-13..NFR-14.

**Given** a clean build and temporary Electron user-data directory
**When** the dedicated M34 sample opens at 1920x1080 and 1280x900
**Then** LSP reports zero diagnostics, the focused control drawing is active, canvas pixels are
nonblank, screenshots are fresh, and structured authority/layout/routing proofs pass.

**Given** actual and approved target screenshots
**When** visual acceptance runs
**Then** sheet aspect, frame/grid/title structure, major circuit regions, required symbol families,
terminal/device labels, route topology, density, and line-weight hierarchy satisfy the recorded
tolerance; failures remain open and are not converted into mocks or weaker assertions.

**Given** all M34 code and artifacts
**When** final polish/purge and retrospective run
**Then** fake sample devices, stale Cabinet-only paths, duplicate authority, generated snapshots,
orphan tests/docs, open actions, and process leaks are removed or truthfully blocked; sequential full
verification, encoding audit, AC-to-evidence, and E2E evidence are recorded before Story 4.3 and M34 close.
