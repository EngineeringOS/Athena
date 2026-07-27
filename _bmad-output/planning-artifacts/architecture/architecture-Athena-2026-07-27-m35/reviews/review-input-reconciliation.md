# M35 Architecture Spine Input Reconciliation Review

Date: 2026-07-27  
Target: `ARCHITECTURE-SPINE.md`  
Inputs: M35 `prd.md`, M35 `addendum.md`, M34 `ARCHITECTURE-SPINE.md`

## Verdict

**Changes required before freeze.** The M35 spine has the correct central architecture: physical
installation is derived, Cabinet is one projection, representation selection stays independent,
the renderer paints only compiled facts, SVG is geometry-only, ECS remains deferred, and constraint
evaluation does not become a solver. Mechanical BMAD lint passes with zero findings.

The remaining issues are authority and inheritance gaps, not a wrong overall direction. Six are
load-bearing: the M34 SVG contract is silently superseded without preserving its security rules,
`MountedOccurrence` has conflicting definitions, M34 drawing-grid placement is not reconciled with
M35 physical millimetres, package/resource ownership is incomplete, drawing bounds have no explicit
owner, and the professional visual checklist has weakened to screenshot existence.

## Findings

### HIGH-1 - M35 silently contradicts the inherited M34 SVG contract

**Evidence**

- M34 AD-3 allows selected SVG nodes to define anchors, labels, hotspots, and compatibility through
  a closed `data-athena-*` profile (`M34 ARCHITECTURE-SPINE.md:82`).
- M34 AD-4/AD-5 treat that annotated SVG frontend as a typed representation-contract source.
- M35 FR-17 and M35 AD-10 correctly narrow SVG to `id`/`data-athena-geometry-ref` hints and move all
  representation meaning into Athena (`prd.md:230`, `M35 ARCHITECTURE-SPINE.md:163`).
- The M35 inherited-invariants table does not identify which portions of M34 AD-3..AD-5 and AD-8
  are superseded. It also does not explicitly inherit M34 AD-12 safe parsing or AD-13 transform and
  anchor normalization (`M34 ARCHITECTURE-SPINE.md:162`, `:174`).

**Risk**

Two stories can both claim parent compliance while implementing incompatible SVG authority. A
cleanup story may also remove the old metadata profile and accidentally weaken parser hardening,
resource limits, transform normalization, or no-I/O guarantees.

**Required reconciliation**

Add an explicit M34 supersession/inheritance matrix. Supersede the metadata-authority portions of
M34 AD-3..AD-5 and the annotated-SVG output in AD-8. Preserve M34 AD-12, AD-13, and the applicable
snapshot security in AD-18. State whether the old required `data-athena-schema` root attribute is
retired; under FR-17 it cannot remain an authoritative metadata requirement.

### HIGH-2 - `MountedOccurrence` has two incompatible owners

**Evidence**

- The M35 PRD defines a Mounted Occurrence as "a compiled instance of an Element" (`prd.md:146`).
- M35 AD-4 instead makes it a physical occurrence keyed to one canonical semantic subject and one
  physical container (`M35 ARCHITECTURE-SPINE.md:100`).
- M35 AD-11 correctly keeps physical placement and representation selection parallel, joining a
  mounted occurrence to a separately constructed representation occurrence only during Cabinet
  composition (`M35 ARCHITECTURE-SPINE.md:174`).

**Risk**

If Epic 4 follows the glossary while Epic 5 follows AD-11, the physical model will either acquire an
Element dependency or the composition compiler will need two incompatible occurrence contracts.
That would violate the central "physical facts do not select visuals" boundary.

**Required reconciliation**

Keep the spine's direction and correct the input contract: a Mounted Occurrence is a physical
installation occurrence of a canonical engineering subject, not an Element instance. It must carry
no Element or representation-selection authority. Cabinet composition creates the composed graphic
occurrence by joining it with a resolved representation occurrence. FR-34's phrase "selected mounted
element" should be interpreted consistently.

### HIGH-3 - Existing drawing-grid layout intent is not reconciled with physical placement

**Evidence**

- M34 AD-25 fixes `layout <view> { place ... at (<column>, <row>) }` as drawing-grid projection intent
  and explicitly says it is never engineering geometry (`M34 ARCHITECTURE-SPINE.md:429`).
- M35 AD-5 introduces container-local integer millimetres as physical source/IR coordinates and
  separately maps them to drawing coordinates (`M35 ARCHITECTURE-SPINE.md:111`).
- M35 AD-15 promises only minimal public language additions but does not say whether M34 `layout`
  syntax remains drawing-only, is migrated, or is rejected for physical Cabinet placement.

**Risk**

An implementation can reinterpret existing drawing-grid values as millimetres, or keep two layout
authorities feeding the same Cabinet occurrence. Either path breaks M34 AD-25 and makes round-trip
editing ambiguous.

**Required reconciliation**

State the source contract explicitly: M34 drawing-grid `layout` remains projection occurrence intent
and cannot be read as physical measurements. Define the M35 source of physical installation intent,
its lowering boundary, and any migration/deprecation rule. If both forms coexist, define precedence
as a compile-time conflict, not a fallback.

### HIGH-4 - Package descriptor, Athena resource declarations, exports, and lock evidence are not fully separated

**Evidence**

- FR-11 requires `athena.yaml` evidence for package id, version, dependencies, exports, and
  package-local resources (`prd.md:207`).
- M35 AD-7 assigns `athena.yaml` only repository/package identity, source roots, and dependency
  intent; AD-8 assigns resource declarations to compiled Athena AST; `athena.lock` records resolved
  hashes (`M35 ARCHITECTURE-SPINE.md:131`, `:142`).
- The package-admission sequence likewise sends only roots and dependencies from `athena.yaml`
  (`M35 ARCHITECTURE-SPINE.md:304`).
- FR-5 and the addendum still allow a resource to resolve relative to either its source file or its
  package root without choosing one normative base (`addendum.md:71`).

**Risk**

Stories can duplicate resource inventories in `athena.yaml` and `.athena`, disagree on export
authority, or resolve the same path differently. This recreates the parallel-authority problem M34
was intended to remove.

**Required reconciliation**

Define one field-level ownership table: `athena.yaml` owns package identity/version, roots,
dependencies, and export admission; representation `.athena` owns typed logical resource
declarations and their package-local path; `athena.lock` owns derived selected versions and content
hashes. Clarify what FR-11's "resource evidence" means without duplicating declaration authority.
Choose one resource path base, preferably the declaring source unit's package directory as shown by
the addendum, and require cross-package access through logical exported resource identity rather than
relative filesystem traversal.

### HIGH-5 - Physical IR, drawing bounds, labels, and viewport ownership are incomplete

**Evidence**

- FR-18 lists labels and drawing bounds in `PhysicalInstallationIR v0` (`prd.md:238`).
- FR-21 requires document bounds and viewport to derive from compiled physical composition, not
  renderer constants (`prd.md:248`).
- M35 AD-4 and the Physical Contract Seed omit labels and bounds (`M35 ARCHITECTURE-SPINE.md:100`,
  `:286`). AD-5 defines the physical-to-drawing transform but does not assign document-bounds,
  label-layout, or viewport authority.

**Risk**

One story can put drawing labels/bounds into the physical kernel while another recomputes or
hardcodes them in presentation/Theia. Either result violates NFR-3 or leaves FR-21 uncovered.

**Required reconciliation**

Resolve the input ambiguity rather than copying drawing concepts into the physical kernel. Physical
IR should own physical enclosure/space bounds and installation labels only if they are physical
facts. Cabinet composition should own derived drawing labels, drawing bounds, and the
physical-to-drawing transform. Presentation transport should carry the compiled viewport; Theia must
consume it unchanged. Record this as an enforceable rule and map FR-18/FR-21 to it.

### HIGH-6 - The professional visual gate has weakened to screenshot existence

**Evidence**

- FR-39 requires desktop and narrow screenshots evaluated against a structural Cabinet checklist,
  and FR-40 requires a visibly professional layout (`prd.md:315`).
- The addendum makes the hard gate concrete: visible enclosure, readable rail layout, dense aligned
  terminal strip, visible duct routing, readable labels, and no giant cards, shallow canvas, or
  floating toy boxes (`addendum.md:306`).
- M35 AD-16 requires fresh screenshots and nonblank-canvas proof but does not bind those structural
  criteria or require a review result (`M35 ARCHITECTURE-SPINE.md:228`).

**Risk**

The milestone can pass by merely producing screenshots of a nonblank but still toy-level Cabinet.
That repeats the exact M34 retrospective failure captured by CM-3 and SM-6.

**Required reconciliation**

Make the structural checklist an architecture-level acceptance boundary. Require machine evidence
for measurable items and recorded human review for readability/density. Screenshot presence and
nonblank pixels are necessary but not sufficient. Keep pixel-perfect matching explicitly excluded.

### MEDIUM-1 - M34 function-aware occurrence invariants are missing from inheritance

M34 AD-23 makes Engineering Function a child of one physical component, and AD-24 allows multiple
function-aware representation occurrences while preserving canonical physical identity (`M34
ARCHITECTURE-SPINE.md:407`, `:419`). M35 AD-4 permits one mounted occurrence per canonical semantic
subject, but AD-11 does not define how that physical occurrence joins to zero, one, or many
function-aware representation occurrences. Add the M34 decisions to inherited invariants and define
the join cardinality. A function occurrence must never become a separately mounted physical product.

### MEDIUM-2 - Resource admission weakens explicit M34/PRD path-security guarantees

FR-8 names absolute paths, traversal, symlinks, Windows junctions, and unauthorized cross-package
reads (`prd.md:197`). M34 AD-18 defines reparse-point handling and the no-follow fallback in detail.
M35 AD-8 says "existing no-follow and root-containment checks" but does not explicitly preserve
junction/reparse rejection or define dependency-admitted cross-package reads. Inherit the applicable
M34 AD-18 guarantees and state that physical filesystem paths never cross package boundaries;
dependency access resolves an exported logical resource into that dependency's own admitted
snapshot.

### MEDIUM-3 - Package/path consistency has no normative mapping rule

FR-1 requires the declared package to match the package-relative filesystem path. AD-8 says the
check is AST-driven, but does not define the source-root-relative mapping, normalization/case rule,
or handling of files directly under a source root. Add the exact invariant so compiler, LSP, sample
migration, and package admission cannot implement different checks.

### MEDIUM-4 - Routing proof fields are not fixed by the spine

FR-30 requires route count, channel usage, endpoint bindings, body intersections, off-channel
segments, and unbound endpoints (`prd.md:283`). AD-12 fixes route ownership and blocking diagnostics,
while AD-16 fixes some zero thresholds, but neither owns the structured proof schema. Assign a typed,
deterministically ordered proof projection and include all six fields. Otherwise E2E and compiler
stories can emit incompatible evidence.

### MEDIUM-5 - PRD open questions disappear without decision or deferral

The PRD leaves resource syntax and the final structural visual checklist open (`prd.md:416`). The
spine assumes typed resource declarations and screenshots but contains no Open Questions section and
no Deferred row for either issue. Before freeze, either decide the minimal syntax and checklist or
carry each with a revisit condition. The current silence makes AD-15 and AD-16 look more complete
than they are.

### LOW-1 - Story verification requirements are only partially carried forward

Core acceptance requires RED/GREEN evidence and final polish/purge notes for every story. AD-17
fully captures polish/purge but not RED/GREEN evidence. This can be enforced in epics/story templates
rather than the architecture, but it must not disappear during BMAD story generation.

### LOW-2 - Required package fixtures are not explicit in the product-proof rule

FR-7 requires both a local standard-library package and a vendor/user package fixture. AD-16 requires
repository-declared local packages but does not require both categories. Add them to the proof seed or
capability map so one self-contained package cannot satisfy the milestone accidentally.

## Coverage Summary

| Input boundary | Status | Assessment |
| --- | --- | --- |
| Athena source SSOT | Landed | AD-1, AD-10, AD-14 preserve source authority. |
| Physical model is derived, not a second semantic world | Landed | AD-1..AD-6 are strong and enforceable. |
| ECS and new package kind deferred | Landed | AD-3 and Deferred are explicit. |
| Constraint evaluation, not solving/AI layout | Landed | AD-6 matches FR-24/NFR-8. |
| Representation selection independent from placement | Landed with terminology conflict | AD-11 is correct; PRD Mounted Occurrence definition conflicts. |
| Existing descriptor and lock reused | Partial | Format boundary lands; field-level descriptor/resource/export ownership does not. |
| SVG is geometry-only | Direction landed, inheritance unresolved | AD-10 matches M35 but silently contradicts parts of M34. |
| Safe SVG compiler and package snapshot | Partial | Output boundary lands; explicit inherited hardening/normalization is missing. |
| Cabinet composition and renderer ownership | Partial | Paint-only rule lands; labels, document bounds, and viewport owner are missing. |
| Physical routing | Partial | Ownership lands; mandatory proof schema does not. |
| Graphic/source trace and governed future editing | Landed | AD-13/AD-14 cover FR-32..FR-36 well. |
| One Cabinet surface | Landed | AD-16 and inherited-surface note are explicit. |
| Professional visual acceptance | Not fully landed | Screenshot capture remains, structural quality gate does not. |
| Language/compiler/editor lockstep | Landed with open syntax | AD-15 is sound; resource syntax remains unresolved. |
| Legacy purge | Landed | AD-17 matches FR-41/FR-42. |

## Freeze Conditions

The spine is ready to freeze after these conditions are met:

1. Record explicit M34 SVG decisions that are superseded and those that remain inherited.
2. Make Mounted Occurrence representation-independent in both architecture and requirements language.
3. Define the non-overlapping relationship between M34 drawing-grid layout and M35 physical intent.
4. Fix field-level authority for `athena.yaml`, resource declarations, exports, and `athena.lock`, plus one path-resolution base.
5. Assign physical bounds, drawing bounds, labels, and viewport to explicit compiler stages.
6. Restore the addendum's structural Cabinet checklist as a hard acceptance gate.
7. Preserve function-aware occurrence identity and package/SVG security guarantees from M34.
8. Close or explicitly defer the two PRD open questions.

No spine or product-code changes were made by this review.
