# M34 Brownfield Divergence Follow-up Review

Date: 2026-07-24

Reviewed artifact: `ARCHITECTURE-SPINE.md`

Brownfield scope: actual M30-M33 representation definition, package descriptor, binding, occurrence construction, Cabinet primitive, transport, rendering, and deletion paths.

Method: CodeGraph exploration of the named contracts and their active callers, followed by line-level comparison with the revised architecture spine.

## Verdict

**REVISE - NOT IMPLEMENTATION-READY.** The revision now chooses the correct top-level owners: `RepresentationDefinition` is canonical, `RepresentationDescriptor` is derived, `BindingResolver` selects, `RepresentationBindingCompiler` constructs occurrences, and `GraphicPrimitive` is the intended active Cabinet vocabulary. However, those decisions still do not define a unique migration through the actual M30-M33 types and production call paths. Two compliant implementers can still produce a second visual body, preserve the reverse descriptor-to-definition adapter, create incompatible resolver/compiler handoffs, or leave the active PresentationPrimitive fallback pipeline in production.

Approval requires exact target fields, named transition adapters, named production callers, and executable deletion gates for the remaining findings below.

## Confirmed Corrections Since Initial Review

The revised spine resolves these prior architecture-level ambiguities:

- `RepresentationDefinition` is explicitly the canonical reusable definition (`ARCHITECTURE-SPINE.md:127-135`).
- `RepresentationDescriptor` is explicitly generated and non-authorable (`ARCHITECTURE-SPINE.md:132-135`, `:270`).
- `BindingResolver` and `RepresentationBindingCompiler` have distinct stated responsibilities, with `RepresentationPolicy` derived from resolver output (`ARCHITECTURE-SPINE.md:170-178`, `:273-275`).
- `GraphicPrimitive` is named as the sole active M34 Cabinet visual vocabulary, while `PresentationPrimitive` is compatibility-only (`ARCHITECTURE-SPINE.md:180-188`, `:276-277`).
- package discovery is repository-confined, immutable, offline, and deterministic (`ARCHITECTURE-SPINE.md:190-197`, `:207-213`).

These are necessary ownership decisions, but the following critical/high gaps remain against the current code.

## Remaining Findings

### 1. Critical - The active Cabinet cutover is still unspecified, so GraphicPrimitive-only compliance can coexist with the legacy production renderer path

The spine requires active M34 Cabinet compilation and rendering to consume `GraphicPrimitive` only (`ARCHITECTURE-SPINE.md:180-188`) and shows `Graphic Primitive IR -> Renderer` (`:230-235`). It does not disposition the actual active Cabinet orchestration or transport fields.

Current production evidence:

- `PresentationModelDeriver.kt:131-137` first derives M33 facts, then falls back to `M32PackageBackedPresentationFactDeriver`, then falls back again to the generic presentation composer.
- `PresentationModelDeriver.kt:138-155` independently derives M33 drawing composition and publishes both `representationFacts` and `drawingComposition` on `PresentationDocument`.
- `M33CabinetPresentationFactDeriver.kt:156-182` creates renderer-facing `PresentationAnatomy`; `:231-269` lowers `GraphicPrimitive` into `PresentationPrimitive`.
- `M33CabinetDrawingCompositionDeriver.kt:369-420` contains a second Graphic-to-Presentation lowerer. It approximates arcs as 12-segment polylines and rejects transformed primitives.
- `PresentationDocument.kt:32-60` exposes `PresentationRepresentationFact` containing both `SymbolAnatomy` and `PresentationAnatomy`.
- `AthenaPresentationSessionProtocol.kt:250-340` serializes `PresentationAnatomy` and `PresentationPrimitive` to the active presentation payload.
- CodeGraph reports `GraphicPrimitiveSvgAdapter.kt:49` is used by `M33IecDrawingSvgCatalogService` and renderer tests, not by `AthenaPresentationSessionProtocol`; therefore the existing GraphicPrimitive SVG adapter is not itself proof of the active Cabinet cutover.

One implementer can route new M34 definitions through `GraphicPrimitiveSvgAdapter` while retaining the M33/M32/generic `PresentationModelDeriver` branches for the product. Another can replace the LSP payload and remove all PresentationPrimitive branches. Both can claim adherence to AD-17, but they ship different ownership and fallback behavior.

Required correction:

- Add exact Replace/Delete dispositions for `PresentationModelDeriver`'s M33, M32, and generic representation branches; `M33CabinetPresentationFactDeriver`; `M33CabinetDrawingCompositionDeriver`; and the `PresentationDocument.representationFacts` / `drawingComposition` fields.
- Name the one production M34 Cabinet compiler output, the one transport payload, and the one renderer entry point that carry `GraphicPrimitive`.
- Require a production reachability test proving zero calls to both `toPresentationPrimitives` lowerers and zero M32/generic fallback invocations for an M34 Cabinet repository.
- Require the Electron proof to assert the rendered primitive authority and transport type, not merely visual output or a separately generated SVG catalog.

### 2. Critical - Extending RepresentationDefinition can still create a second canonical visual/anatomy body

The revised decision says `RepresentationDefinition` owns the Graphic Primitive body, intrinsic composition, anchors, compatibility, slots, lifecycle, and provenance (`ARCHITECTURE-SPINE.md:127-135`) and the migration row says to "Add" those concerns (`:268`). It does not say what happens to the existing `anatomy` field.

Current ownership conflicts inside the selected canonical type:

- `RepresentationContracts.kt:224-233` defines `RepresentationDefinition.anatomy: PresentationAnatomy` alongside label slots, variants, and style tokens.
- `PresentationAnatomy.kt:110-117` independently owns bounds, hotspot, `List<PresentationPrimitive>`, terminals, and label anchors.
- `PresentationAnatomy.kt:43-86` defines the legacy visual body vocabulary.
- `GraphicPrimitiveModels.kt:115-230` defines the different Graphic vocabulary that AD-17 makes canonical.

The word "Add" permits at least two compliant implementations: add a `GraphicPrimitiveDocument` field beside `anatomy`, or mutate `PresentationAnatomy` to hold Graphic primitives. The first leaves duplicate bounds, primitive, anchor, and label authority; the second changes a transport-oriented compatibility type into the canonical body while AD-17 simultaneously schedules it for deletion. Neither migration is ruled out by the spine.

Required correction:

- Define the exact final `RepresentationDefinition` field shape and state whether the existing `anatomy` field is replaced, renamed, or temporarily adapted.
- Make `GraphicPrimitiveDocument` or a named M34 body type the sole stored visual body; derived Presentation compatibility data must not be stored back on the definition.
- Name one one-way legacy adapter and prohibit reverse conversion or dual population.
- Add a compile-time or contract-test gate proving a final definition cannot contain both a Graphic body and authoritative `PresentationAnatomy`.

### 3. High - Descriptor ownership is stated, but neither the field projection nor the existing reverse-authority bridge is dispositioned

AD-11 correctly makes `RepresentationDescriptor` generated and non-authorable (`ARCHITECTURE-SPINE.md:132-135`, `:270`). The architecture does not define the generator, its complete mapping, or removal of the brownfield path that currently does the opposite.

Current evidence:

- `RepresentationDescriptorModels.kt:92-104` carries descriptor/resource identity, bounds, anchors, label requirements, hotspots, transforms, variants, style references, validation-rule references, and forbidden-authority claims.
- The proposed `RepresentationDefinition` ownership list does not state how descriptor/resource ids, resource kind, transform entries, validation references, or forbidden claims are derived.
- `PackageBackedRepresentationOccurrenceFactory.kt:64-100` consumes descriptor evidence, synthesizes a `RepresentationDefinition` and a new `RepresentationPolicy`, then invokes `RepresentationBindingCompiler`.
- `PackageBackedRepresentationOccurrenceFactory.kt:141-183` is an explicit reverse `RepresentationDescriptor.toRepresentationDefinition` conversion. It fabricates a rectangle body and zero-coordinate hotspot/label anchors while dropping descriptor hotspots, transforms, style references, validation rules, forbidden claims, and label `required` semantics.

If M34 adds a forward definition-to-descriptor generator but leaves this factory reachable for M32 compatibility, the system has a cycle: canonical definition -> generated descriptor -> synthesized definition. Two models with the same logical identity can then differ structurally while both pass through supported code.

Required correction:

- Name the sole definition-to-descriptor projector and provide a field-level source/derivation table for every field in `RepresentationDescriptor`.
- State whether generated descriptors are transient snapshot records or serialized artifacts, and identify the validation boundary that rejects independently authored descriptors.
- Mark `PackageBackedRepresentationOccurrenceFactory.toRepresentationDefinition` as Delete, with `M32PackageBackedPresentationFactDeriver` and `M32ProductSmokeProof` either migrated to canonical definitions or isolated to a non-production test source set.
- Add a one-way projection test proving descriptor identity and all projected fields derive from one immutable canonical definition, with no descriptor-to-definition call path in M34 production.

### 4. High - ResolvedRepresentationSelection is named but not specified enough to enforce the BindingResolver/BindingCompiler ownership split

The revised spine says `BindingResolver` emits `ResolvedRepresentationSelection`, `RepresentationPolicy` is derived from it, and `RepresentationBindingCompiler` validates the canonical definition and constructs the occurrence (`ARCHITECTURE-SPINE.md:170-178`, `:226-233`, `:273-275`). `ResolvedRepresentationSelection` does not exist in the current code, and the document does not define its fields or its relationship to the existing `BindingResolution`.

The current handoff is materially incomplete:

- `BindingResolverModels.kt:64-74` returns `BindingResolution` with semantic subject, package/profile/descriptor/variant ids, anchor and label maps, and style profile.
- `RepresentationBindingCompiler.kt:3-16` requires a different `RepresentationBindingRequest`: canonical/projection identities, subject and projection kinds, semantic and occurrence roles, a full `RepresentationPolicy`, a full `RepresentationDefinition`, labels, terminal ports, priority, references, and composition membership.
- `PackageBackedRepresentationOccurrenceFactory.kt:64-100` fills that gap by inventing definition, policy, role, priority, and composition data outside both named owners.

Without an exact handoff contract, one implementer can extend `BindingResolution`, another can create a parallel `ResolvedRepresentationSelection`, and a third can preserve the factory as the policy/definition authority. All satisfy the prose that the resolver "selects" and compiler "constructs," but selection-derived data and defaults remain owned by different stages.

Required correction:

- Declare whether `ResolvedRepresentationSelection` replaces or renames `BindingResolution`; parallel public selection results are forbidden.
- Specify its exact required fields, including canonical definition identity/handle, generated descriptor identity, selected package/profile/variant/style, anchor and label mappings, projection context, resolver diagnostics/provenance, and every value from which policy fields are deterministically derived.
- Define one named mapper from selection plus semantic/projection facts to `RepresentationBindingRequest`; defaults for role, priority, fallback, and composition membership must be specified or rejected, never invented by an adapter.
- Delete or replace `PackageBackedRepresentationOccurrenceFactory` after the mapper is active, and add a contract test proving `RepresentationBindingCompiler` performs no package/descriptor search while no other production component synthesizes an independent policy.

### 5. High - Deletion gates remain descriptive rather than executable

The migration table still uses gates that two implementers can interpret differently:

- `DrawingSymbolAnatomy` / `M33IecSymbolDefinition`: "delete after zero active callers" (`ARCHITECTURE-SPINE.md:269`).
- `PresentationPrimitive`: "delete after LSP/composition callers migrate" (`:277`).
- `M33CabinetPackageSet`: retain a fixture adapter "only if tested" (`:278`).
- M32/M33 XML assets: "Delete or fixture-only" (`:279`).
- direct SVG/box paths: "delete after E2E" (`:280`).

The caller sets are not theoretical:

- CodeGraph reports `DrawingSymbolAnatomy.kt:149` has production callers in M33 package runtime, drawing-composition models, and Cabinet derivation, in addition to tests.
- `M33IecSymbolSupport.kt:40-44` wraps `DrawingSymbolAnatomy` in `M33IecSymbolDefinition`, and `M33CabinetPackageSet.kt:56-68`, `:81-119` carries that wrapper through resolution and compilation.
- `M33CabinetPackageSet.kt:267-283` activates the XML path when any one of four files exists, then loads all four files.
- `PresentationPrimitive` remains in both M33 lowerers and `AthenaPresentationSessionProtocol.kt:286-340`.

"Zero active callers" does not define whether compatibility, sample, smoke, or runtime callers count. "Fixture-only" does not require a test-only source set or prevent production packaging. "After E2E" does not name an assertion that proves unreachability. The unresolved "Delete or fixture-only" alternative is itself not an architecture decision.

Required correction:

- Replace every alternative with one exact action and list the complete allowed post-M34 caller set.
- Require fixture adapters and XML assets to live under test resources/source sets that are absent from production runtime and distribution artifacts.
- Name the static dependency/build checks that fail on production imports of `DrawingSymbolAnatomy`, `M33IecSymbolDefinition`, `PresentationPrimitive`, `M33CabinetPackageSet.load/isDeclared`, and legacy renderers.
- Name the M34 Cabinet integration/Electron assertions that prove no legacy resolver, XML loader, fallback deriver, PresentationPrimitive lowerer, or direct SVG/box authority was reached.

## Approval Gate

The brownfield divergence review can pass when all five conditions are represented as executable architecture constraints:

1. `RepresentationDefinition` has one exact final body/anatomy shape and cannot carry both Graphic and Presentation visual authority.
2. `RepresentationDescriptor` has one named forward projector, a complete field map, and no production reverse conversion.
3. `ResolvedRepresentationSelection` has one exact schema and one deterministic handoff into `RepresentationBindingCompiler`; the existing parallel bridge is retired.
4. active Cabinet compilation, transport, and rendering have one named GraphicPrimitive-only path with no M33/M32/generic fallback branches.
5. every Replace/Delete row names production callers, allowed test-only adapters, static packaging checks, and runtime/E2E unreachability assertions.
