# Engineering Package Platform

M32 introduces a platform package boundary for reusable engineering knowledge.

```text
.athena semantic source
  -> Engineering Resolver
  -> Engineering Package Descriptor
  -> Presentation Profile
  -> Binding Manifest / Binding Resolver
  -> Representation Resolver
  -> Representation Package Descriptor
  -> Representation Descriptor / Graphic Resource handle
  -> Presentation IR
```

## Engineering Package Descriptor

An Engineering Package Descriptor owns product and concept knowledge:

- package id, group id, artifact id, and version
- package kind
- concept definitions
- product definitions
- templates and defaults
- parameter schema
- validation rule references
- relationship capability references
- lifecycle metadata
- documentation references
- provenance

It does not own presentation or rendering facts. The descriptor must reject representation
primitives, Graphic Resource references, anchors, style, viewBox, Presentation IR, and source
mutation rules.

## Representation Package Descriptor

A Representation Package Descriptor owns view/resource contracts:

- package id, group id, artifact id, and version
- supported Presentation Profile ids and tags
- representation descriptor entries
- Graphic Resource references
- style token references
- variants
- previews
- lifecycle metadata
- provenance

It does not own engineering truth. The descriptor must reject source mutation rules, product or
domain facts, semantic compiler behavior, and `.athena` syntax authority.

Graphic Resource is a generic renderer resource handle, not an architecture synonym for SVG. M32 v0
supports one vector-document backend first. Other resource kinds are represented by explicit
unsupported/deferred diagnostics so they cannot silently become trusted renderer success.

## Representation Descriptor

A Representation Descriptor is the validated contract between a Representation Package and later
Representation Occurrence creation. It describes how a declared Graphic Resource can be used:

- resource id and resource kind binding
- positive bounds
- anchors
- label slots
- hotspots
- transforms
- variants
- style token references
- validation rule references

It still does not own engineering truth. Graphic Resource ids, visible text, CSS classes, and file
names are not semantic authority. Descriptor validation must fail closed before projection success
when required resources, anchors, labels, bounds, or variants are missing or invalid.

## Governed Local Registry Roots

Package discovery starts from explicit local registry roots. Athena must not scan arbitrary
workspace folders, reference mirrors, internet registries, or tool caches.

M32 v0 recognizes two governed root kinds:

- project-local package roots
- Athena-owned package roots

Project-local roots have higher precedence than Athena-owned defaults. Duplicate package ids either
fail with an ambiguity diagnostic or select the highest-priority governed root when the registry
policy explicitly allows precedence.

This follows the deterministic root normalization style already used by the brownfield compiler
knowledge package resolver, but M32 package registry behavior lives in the package runtime boundary,
not in compiler-only knowledge package infrastructure.

## Package Resolution Facts

Package resolution consumes governed roots and typed descriptor candidates, then emits facts for
downstream binding and proof tooling:

- package id
- package kind
- version
- descriptor path
- dependency list
- validation status
- diagnostics
- selected registry root

Missing, ambiguous, incompatible, or invalid packages fail closed with structured diagnostics.
Renderer fallback is not a package resolution success state.

Resolution facts are not cache identity. Cache identity must include descriptor/resource/policy
content and active profile in a later story.

## Package Cache Identity

Package cache identity is a deterministic content and policy key. It includes:

- package id
- package kind
- version
- descriptor path
- descriptor content identity
- resource content identities
- selected registry root
- binding policy identity
- active profile

Any descriptor, resource, policy, profile, package version, or selected-root change must produce a
different cache identity so stale package artifacts are not reused. M32 v0 computes this identity
only; it does not introduce a persistent cache store or renderer cache.

## Presentation Profile Descriptor

Presentation Profile is independent appearance policy. It can describe standards, customers, output
contexts, style profiles, compatibility constraints, fallback policy, and provenance without moving
those facts into Engineering Packages or Representation Packages.

Examples of profile-owned policy facts:

- IEC or ANSI standard tags
- customer-specific presentation policy
- compact, print, maintenance, or training output mode
- theme/style profile

It must not contain engineering truth, product parameters, Graphic Resource internals, or source
mutation behavior.

Athena also has an older M30 runtime `PresentationPolicyProfile` in
`kernel:presentation-policy-model`. That model selects current representation families for the M30
composer. M32 `PresentationProfileDescriptor` is the package-platform descriptor contract that later
Binding Resolver stories consume. The two should bridge intentionally rather than being treated as
the same object.

## Binding Manifest

Binding Manifest is the compatibility bridge between an Engineering Package concept, compatible
Presentation Profile tags, and default or alternative Representation Packages.

It may carry:

- engineering package id and version range
- concept id
- default representation package id
- alternative representation package ids
- compatible profile tags
- policy tags
- provenance

It must not carry representation geometry, Graphic Resource internals, compiler behavior, or source
mutation behavior. Binding Resolver selection happens later from manifest facts, profile facts,
package resolution facts, and semantic context.

## Binding Resolver

Binding Resolver is the governed selection and mapping stage. It consumes a semantic subject,
projection context, Engineering Package descriptor, Binding Manifest, active Presentation Profile,
Representation Package descriptors, and validated Representation Descriptors.

It selects:

- representation package
- representation descriptor
- variant
- semantic-port to descriptor-anchor mapping
- descriptor label-slot binding
- style profile

The resolver preserves the upstream semantic subject id. It does not create engineering truth from
package names, Presentation Profile ids, descriptor ids, Graphic Resource ids, text labels, bounds,
or coordinates.

When selection fails, diagnostics name the failed authority: Engineering Package, Presentation
Profile, Binding Manifest, Representation Package, descriptor, anchor, label slot, or binding
policy. A fallback box is not accepted as success.

## Binding Evidence Payload

Binding evidence is the product-safe transport view of a Binding Resolver result. It is designed
for LSP, workbench, proof tooling, and future frontend adapters.

The payload exposes:

- semantic subject id
- engineering package id and version
- Presentation Profile id
- representation package id and version
- descriptor id
- variant
- anchor map summary
- label binding summary
- resolver stage
- authority diagnostics
- renderer fallback acceptance flag

Frontend adapters consume these returned facts. They must not infer binding from file names,
Graphic Resource internals, visible labels, DOM structure, or CSS classes. If diagnostics are
present, the payload preserves the failed authority instead of collapsing the problem into generic
rendering failure.

## Descriptor-Backed Representation Occurrence

Descriptor-backed occurrence creation bridges M32 package evidence into the existing M30
Representation Occurrence pipeline. It consumes Binding Evidence plus a validated Representation
Descriptor and produces the same `RepresentationOccurrence` contract used by native M30
definitions.

The bridge carries:

- canonical semantic subject id from binding evidence
- projection occurrence id from the projection context
- descriptor id as representation symbol identity
- representation package id as library identity
- variant
- descriptor-backed label slots
- descriptor-backed terminal anchor bindings
- representation diagnostics

The descriptor id and resource id remain representation identities only. They do not rewrite the
canonical semantic subject id and they do not become engineering truth.

If binding evidence or descriptor facts are incomplete, occurrence creation fails closed with
representation diagnostics such as missing symbol, missing anchor, or missing label slot. It does
not create a generic fallback occurrence.

## Descriptor-Backed Graphic Resource Render Payload

Descriptor-backed render payloads are the adapter-safe view of a Graphic Resource. They expose
only resolved paint facts:

- semantic subject id for selection correlation
- resource handle and resource kind
- descriptor bounds
- descriptor anchor summary
- descriptor label summary
- transient interaction state
- normal background and hitbox visibility flags
- derived viewBox with governed margins
- duplicate-label proof
- resource-semantic-inference guard

Normal component backgrounds and hitboxes remain invisible. Selection, hover, preview, and DnD
chrome are transient interaction state, not permanent component borders.

Renderers and frontend adapters consume these payloads as paint instructions. They do not resolve
packages, parse Graphic Resource internals, infer semantic type from CSS classes, or treat labels
and file names as engineering truth.

## Descriptor-Anchor Route Evidence

Descriptor-anchor route evidence connects semantic terminal relationships to descriptor-declared
anchors. It consumes Binding Evidence anchor summaries and Representation Descriptor anchors, then
emits source and target endpoint evidence such as:

```text
descriptor.drive#power
descriptor.breaker#line
```

The route evidence carries endpoint coordinates and sides from validated descriptor anchors. It
does not inspect raw Graphic Resource geometry, file names, CSS classes, or labels to discover
terminals.

If a source descriptor, target descriptor, source anchor, or target anchor is missing, the mapper
emits diagnostics and rejects center fallback. A center route is not accepted as package-backed
routing success.

## M32 Sample Package Project

The customer-demo sample lives at `examples/m32/sample-project`. It is Athena-owned synthetic data,
not a vendor package feed and not a QET-derived runtime dependency.

The sample separates:

- `.athena` semantic source in `src/01-package-platform-demo.athena`
- Engineering Package descriptors in `packages/engineering`
- Presentation Profile descriptors in `packages/profiles`
- Binding Manifests in `packages/manifests`
- Representation Package descriptors in `packages/representation`
- vector Graphic Resources in `packages/resources`

`ShutterMotorM32` proves profile switching for M32: the same semantic source resolves through the
`m32-iec` and `m32-compact` Presentation Profiles with different representation package and
descriptor ids. This is package/binding runtime evidence; the `.athena` source is not edited to
select a visual resource.

## M32 Structured Product Smoke

`M32ProductSmokeProofRunner` is the package-runtime smoke proof for the M32 sample. It composes the
same contracts used by the earlier package stories:

- `M32SamplePackageSet`
- `BindingResolver`
- `BindingEvidencePayloadMapper`
- `RepresentationDescriptorValidator`
- `PackageBackedRepresentationOccurrenceFactory`
- `DescriptorBackedGraphicResourceRenderPayloadMapper`
- `DescriptorAnchorRouteEvidenceMapper`

The proof records Engineering Package resolution, Representation Package resolution, manifest
selection, descriptor validation, anchor mapping, label binding, occurrence creation, derived
bounds, route anchoring, profile switching, and no accepted fallback.

Screenshot evidence is intentionally secondary. A screenshot can help a human review visual
quality, but it cannot satisfy package, binding, descriptor, route, or fallback claims without the
structured proof passing.

## M32 Professional-Density Proof

`M32DemoLayoutDensityProofRunner` validates the sample's industrial-density constraints from
descriptor-backed render payloads and sample layout facts:

- normal component background and hitbox chrome are transparent
- duplicate visible labels are rejected
- anchors and labels come from descriptors and binding evidence
- generic rectangle fallback is not accepted
- viewBox is derived from content bounds plus a governed margin
- the sheet navigation band remains visible in the structured adapter proof

The sample layout fixture is `examples/m32/sample-project/presentation/layout-density-proof.json`.
It is demo presentation data, not `.athena` visual syntax and not semantic kernel truth.

## Authoring Preview Compatibility Closeout

M32 keeps `AthenaAuthoringSessionRuntimeService` as an explicitly versioned
`legacy-preview-readonly-v1` compatibility API. The compatibility contract states:

- preview/session methods are retained for submit, state, snapshot, restore, and decision review
- the API has no mutable source authority
- accepted governed previews still require active governed authorities
- canonical source mutation remains owned by Semantic Authoring Transaction authority

This closes the M31 preview-session compatibility item without hiding a mutable source path behind
the package platform work.

## Relationship Mutation Compatibility Closeout

M32 keeps `AthenaConnectPortsCommand` as an explicitly versioned
`legacy-connect-ports-runtime-command-v1` compatibility command for non-Theia runtime surfaces:

- CLI
- desktop Compose viewer
- domain-electrical runtime workbench

The compatibility contract states:

- retained callers use the runtime-owned command path only
- the command has no mutable source-authoring authority
- product and Theia authoring surfaces continue to use `SemanticRelationshipIntent`
- the retained command is not a second product authoring contract

The current audit shows the active Theia/LSP product path already routes governed relationship
authoring through `SemanticRelationshipIntent`; remaining `AthenaConnectPortsCommand` hits are
runtime compatibility surfaces, tests, and historical docs. The closeout proof lives in
`AthenaCommandRuntimeTest` and the runtime-published `compatibilityContract()`.

## Relationship Candidate Affordance Closeout

M32 removes the Graphical View relationship candidate authority that previously treated any
`port:` label as a connect target. Candidate highlighting now asks
`relationshipCandidateEvidence()` before preview. The frontend evidence helper consumes the
existing LSP semantic inspection payload:

- source and target must both appear in semantic inspection port facts
- signal-family facts must match
- direction facts must be compatible
- rejected targets receive structured evidence reasons before preview

The remaining `startsWith('port:')` hit in the Graphical View source is limited to
`resolveCreatedEntitySemanticId`, where the create-entity reveal path prefers a created component
identity over an affected child port. It is not relationship candidate authority.

## Projection Compatibility Closeout

M32 removes the M26 GLSP display-title sheet-role fallback. Sheet role is now transported only from
typed projection payload authority:

- `sheet.role`
- normalized `policyEvidence.sheetViewRole`

`displayName` remains visible sheet text only. It is not parsed to derive `power_distribution`,
`control_logic`, `field_wiring`, or any other governed sheet role. The graph-glsp adapter test
`does not derive sheet role from displayName when typed policy evidence is absent` locks this
boundary.

M32 retains `_reference` occurrence strings only in legacy defensive tests and historical fixtures
that exercise cross-reference handling. Normal compiler, runtime, and LSP payloads prove that
duplicate off-sheet visual reference components are not emitted:

- `AthenaM30SampleProjectCompilerTest` checks documentation nodes and cross-sheet links do not use
  `_reference` occurrence ids.
- `AthenaRuntimeProjectionSessionTest` checks runtime documentation projection components do not
  carry `_reference` duplicates.
- `AthenaProjectionRequestTest` checks LSP projection payload components do not carry `_reference`
  duplicates.

Future projection work should remove or rename the legacy defensive fixtures only when an
equivalent cross-reference fixture exists that does not look like a normal production occurrence.

## Brownfield Boundary

Athena already has M0 governed knowledge package infrastructure under
`kernel/compiler/src/main/kotlin/com/engineeringood/athena/compiler/knowledge`. M32 package model
contracts may reuse its naming and diagnostic style, but Engineering Package v0 is a platform
contract and should not be hidden inside a compiler-only artifact model. Resolver stories decide
how this platform contract bridges into compiler/runtime package loading.
