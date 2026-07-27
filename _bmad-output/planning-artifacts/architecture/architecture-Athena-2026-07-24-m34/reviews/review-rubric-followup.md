# M34 Architecture Spine Follow-up Review

**Review date:** 2026-07-24  
**Reviewed artifact:** `ARCHITECTURE-SPINE.md`  
**Baseline:** `reviews/review-rubric.md`  
**Verdict:** **NEEDS REVISION - canonical visual ownership is substantially resolved, but package/runtime boundaries and the module graph are not yet implementation-ready.**

No critical findings remain. Five high findings remain. The revision now fixes one canonical
`RepresentationDefinition`, one active `GraphicPrimitive` vocabulary, one selector, and one
occurrence builder. It also closes the prior high-severity SVG policy and compiler-operational gaps.
The remaining defects are convergence defects: two compliant epics can still define incompatible
Element composition, binding selection, cross-module result contracts, Cabinet composition paths,
or migration boundaries.

## Good-Spine Checklist

| Criterion | Result | Follow-up judgment |
| --- | --- | --- |
| Real divergence points are fixed | **Fail** | Ownership names are improved, but composition semantics, resolver ordering, local version choice, and cross-module result placement remain open. |
| Every AD is enforceable and prevents its stated divergence | **Fail** | AD-6 and AD-16 identify the selector but do not define its deterministic decision rule; AD-19 names a generic owner rather than the brownfield boundary. |
| No hidden open dimension | **Fail** | `ResolvedRepresentationSelection`, derived `RepresentationPolicy`, snapshot build/load ownership, and local package-version conflict handling are neither placed nor deferred safely. |
| Brownfield fit | **Partial** | Graphic Primitive and `RepresentationDefinition` ownership now fit the repository, but drawing composition and presentation integration disappear from the seed and migration map. |
| Implementable one level down | **Fail** | Frontend, package/runtime, binding, and Cabinet epics still need to make shared architectural choices not fixed by the spine. |
| Parent invariants preserved | **Fail** | M33 AD-4 and AD-8 remain absent, and AD-19 does not explicitly retain their existing `drawing-composition` owner. |
| Named technology/current reality | **Pass** | The spine binds existing Athena/Gradle/Electron/module contracts rather than introducing an unverified external stack. |
| Mechanical form | **Pass** | `lint_spine.py` reports `ok: true` with zero findings. |

## Remaining High Findings

### HIGH-1 - Canonical definition ownership is fixed, but canonical Element composition is still undefined

**Evidence**

- AD-11 now correctly makes `RepresentationDefinition` the one canonical Symbol/Element contract
  and makes `RepresentationDescriptor` generated only: `ARCHITECTURE-SPINE.md:127` through
  `ARCHITECTURE-SPINE.md:135`.
- AD-2 still permits undefined "governed visual groups" and does not define the composition
  reference boundary: `ARCHITECTURE-SPINE.md:64` through `ARCHITECTURE-SPINE.md:69`.
- AD-11 says the definition owns "intrinsic composition" but does not fix reference identity,
  graph/cycle behavior, transform composition, exported-anchor identity/collision rules, or variant
  interaction: `ARCHITECTURE-SPINE.md:132` through `ARCHITECTURE-SPINE.md:135`.
- AD-18 defines snapshot duplicate identity as
  `(group, artifact, version, definitionId)`, while AD-11 does not bind that key to the canonical
  definition's identity fields: `ARCHITECTURE-SPINE.md:190` through `ARCHITECTURE-SPINE.md:197`.

**Why this remains blocking**

The Athena frontend can model an Element as a package-qualified DAG with explicitly exported
anchors, while the SVG frontend can model nested groups with local ids and flattening. Both emit
`RepresentationDefinition`, obey the named owner, and still disagree on valid references, cycles,
collisions, and variants. Canonical ownership alone does not make the shared contract convergent.

**Required disposition:** Bind AD-11 to a minimal composition contract or companion that fixes the
package-qualified reference key, DAG/cycle rule, transform order, exported-anchor namespace and
collision behavior, variant application point, and common diagnostic/proof envelope. Exact DTO
shape can remain code-owned after those semantics are fixed.

### HIGH-2 - Binding has one owner but still lacks one deterministic selection procedure

**Evidence**

- AD-6 only says selection is explicit and missing/mismatched facts fail:
  `ARCHITECTURE-SPINE.md:92` through `ARCHITECTURE-SPINE.md:97`.
- AD-16 makes `BindingResolver` the sole selector, but does not define selector vocabulary,
  candidate ordering, specificity, profile/variant precedence, or tie behavior:
  `ARCHITECTURE-SPINE.md:170` through `ARCHITECTURE-SPINE.md:178`.
- `BindingManifest` is limited to package/concept admission, while no other authored contract owns
  semantic-fact-to-descriptor/variant selection rules: `ARCHITECTURE-SPINE.md:174` through
  `ARCHITECTURE-SPINE.md:177` and `ARCHITECTURE-SPINE.md:272`.
- Snapshot ingestion rejects duplicate exact coordinates but permits distinct versions of the same
  package/definition: `ARCHITECTURE-SPINE.md:194` through `ARCHITECTURE-SPINE.md:197`.
- Local package resolution is active M34 behavior, yet version conflict is deferred with the vendor
  marketplace: `ARCHITECTURE-SPINE.md:207` through `ARCHITECTURE-SPINE.md:213` and
  `ARCHITECTURE-SPINE.md:304`.

**Why this remains blocking**

Two resolvers can both be sole selectors and fail closed, yet one can preserve manifest package
order while another ranks compatibility predicates or chooses the highest satisfying local version.
They can also resolve multiple compatible variants differently. Deferring marketplace conflict does
not settle multiple local versions in one immutable snapshot.

**Required disposition:** Amend AD-16 with the authored selector-fact authority and an exactly-one
resolution algorithm: admissible facts, package/version candidate order, compatibility/specificity
rule, profile and variant precedence, zero/multiple-match behavior, and stable diagnostics. Split
local deterministic version selection from deferred remote marketplace conflict.

### HIGH-3 - The cross-module selection and snapshot contracts have no canonical module owner

**Evidence**

- The dependency flow introduces `ResolvedRepresentationSelection` and then derives
  `RepresentationPolicy`: `ARCHITECTURE-SPINE.md:226` through `ARCHITECTURE-SPINE.md:233`.
- AD-16 says `package-runtime` behavior emits the selection while
  `RepresentationBindingCompiler` consumes the derived policy, but neither the selection contract
  nor the derivation owner is placed: `ARCHITECTURE-SPINE.md:170` through
  `ARCHITECTURE-SPINE.md:178`.
- The structural seed gives `representation-model` definitions, occurrences, and primitives;
  `package-model` profile/policy contracts; and `package-runtime` the selector, but does not assign
  `ResolvedRepresentationSelection` or derived `RepresentationPolicy`:
  `ARCHITECTURE-SPINE.md:282` through `ARCHITECTURE-SPINE.md:290`.
- AD-18 says secure ingestion stages an immutable snapshot, the seed says `package-runtime` has a
  snapshot loader, and `compiler` owns the source frontends/descriptor projection; no rule identifies
  the snapshot schema owner, builder, or allowed dependency direction:
  `ARCHITECTURE-SPINE.md:190` through `ARCHITECTURE-SPINE.md:197` and
  `ARCHITECTURE-SPINE.md:286` through `ARCHITECTURE-SPINE.md:290`.

**Why this remains blocking**

One epic can place `ResolvedRepresentationSelection` in `package-runtime`; another can place it in
`representation-model` so the occurrence builder can consume it without a reverse dependency.
Likewise, either `compiler` or `package-runtime` can become the snapshot builder. These choices can
create a `representation-model` to `package-runtime` cycle or a compiler/runtime cycle while obeying
all written rules.

**Required disposition:** Add a binding module ownership table and arrows. Place the immutable
selection, derived-policy, and snapshot contracts in dependency-neutral model modules; name the
single snapshot builder, loader, selector, policy derivation owner, and occurrence builder; prohibit
reverse imports explicitly.

### HIGH-4 - Cabinet composition ownership remains generic and contradicts the brownfield module graph

**Evidence**

- AD-19 correctly separates intrinsic Element transforms/bounds from project occurrence layout,
  routing, and Cabinet document bounds: `ARCHITECTURE-SPINE.md:199` through
  `ARCHITECTURE-SPINE.md:205`.
- It assigns project facts only to generic "Project layout/spatial compilation" and does not name
  the inherited `drawing-composition` authority: `ARCHITECTURE-SPINE.md:203` through
  `ARCHITECTURE-SPINE.md:205`.
- M33 AD-4 and AD-8 are still absent from the inherited table:
  `ARCHITECTURE-SPINE.md:43` through `ARCHITECTURE-SPINE.md:52`.
- `drawing-composition` and `presentation-model` are absent from both the migration seed and the
  structural seed: `ARCHITECTURE-SPINE.md:264` through `ARCHITECTURE-SPINE.md:296`.
- AD-14 still derives document bounds from composition occurrences and routing facts without
  identifying their producing boundary: `ARCHITECTURE-SPINE.md:153` through
  `ARCHITECTURE-SPINE.md:158`.

**Why this remains blocking**

An epic can continue the established `drawing-composition` facts, while another can implement the
same authority in `layout-model`, `spatial-model`, or `compiler` under AD-19's generic wording. Both
keep project and intrinsic truth separate, but they create duplicate Cabinet composition paths and
incompatible document-bounds inputs.

**Required disposition:** Restore M33 AD-4 and AD-8 explicitly, name `drawing-composition` as the
owner of frame/zones/lanes/rails/channels/references/bounds facts, place it and
`presentation-model` in the module graph, and define their direction into Cabinet document and
Graphic Primitive assembly.

### HIGH-5 - The migration seed is still not the exhaustive migration contract required by AD-11

**Evidence**

- The revised seed now dispositions the major definition, descriptor, resolver, policy, occurrence,
  primitive, loader, and renderer concepts: `ARCHITECTURE-SPINE.md:264` through
  `ARCHITECTURE-SPINE.md:280`.
- It still omits previously identified active contracts such as `DrawingSymbolPrimitiveCompiler`,
  `GraphicPrimitiveDocument`, `PresentationDocument`/presentation composition, drawing-composition
  contracts, transport/proof payloads, and their adapters/deletion gates.
- `BindingManifest` is marked `Reuse/extend` while its authority is narrowed to package/concept
  admission and direct descriptor selection is forbidden: `ARCHITECTURE-SPINE.md:272`. That is a
  semantic migration, but the retained fields/callers and replacement authority are not named.
- The table contains no reference to an exhaustive companion or ledger that completes the required
  type-by-type disposition: `ARCHITECTURE-SPINE.md:264` through `ARCHITECTURE-SPINE.md:280`.

**Why this remains blocking**

The frontend and Cabinet epics can independently retain, adapt, or replace the omitted compiler,
document, composition, and proof contracts. The `BindingManifest` change can also leave stale
policy-tag selection active in one path while the new resolver obtains policy elsewhere.

**Required disposition:** Bind AD-11 to a complete migration companion before epic decomposition.
For every active representation/package/composition/transport/proof/renderer type, record one of
Reuse, Extend, Reclassify, Replace, or Delete; identify the successor/adapter, owning epic, active
call-site exit condition, and deletion test.

## Prior Rubric Disposition

| Prior finding | Follow-up status | Basis |
| --- | --- | --- |
| HIGH-1 Graphic Primitive structural contradiction | **Closed** | AD-17 and the seed now keep `GraphicPrimitive` canonical in `representation-model` (`ARCHITECTURE-SPINE.md:180`, `ARCHITECTURE-SPINE.md:286`). |
| HIGH-2 migration decision deferred | **Still High** | The table is improved but remains non-exhaustive and unbound to a complete companion. |
| HIGH-3 no canonical shared IR | **Partially closed; still High** | `RepresentationDefinition` is canonical, but Element composition/reference/export semantics remain open. |
| HIGH-4 binding selection undefined | **Still High** | Owner separation is fixed; deterministic selector and local version/variant rules are not. |
| HIGH-5 Cabinet composition owner dropped | **Still High** | AD-19 separates truth but does not retain or place `drawing-composition`. |
| HIGH-6 SVG safety profile unenforceable | **Closed at High** | AD-12, AD-18, and Safe SVG Lowering Profile V1 now fix prohibited capabilities, path confinement, immutable input, product caps, and diagnostics (`ARCHITECTURE-SPINE.md:137`, `ARCHITECTURE-SPINE.md:190`, `ARCHITECTURE-SPINE.md:243`). |
| MEDIUM-1 source/tooling placement omitted | **Still Medium** | `.athena.element`, language, tree-sitter, and LSP placement remain absent from the seed. |
| MEDIUM-2 inherited table incomplete | **Still Medium, contributes to HIGH-4** | Parent AD ids remain selectively omitted. |
| MEDIUM-3 compiler operational envelope absent | **Closed** | AD-18 and AD-20 establish snapshot identity, deterministic cache inputs, local-only operation, and no network runtime authority. |

## Implementation-Readiness Confirmation

- **Canonical ownership:** **Partially ready.** `RepresentationDefinition`, generated
  `RepresentationDescriptor`, `GraphicPrimitive`, `BindingResolver`, and
  `RepresentationBindingCompiler` now have single conceptual owners. Element composition semantics
  and several bridge contracts do not.
- **Package/runtime boundaries:** **Not ready.** Selection authorship and ordering, local version
  handling, snapshot build/load ownership, selection-result placement, and policy derivation remain
  ambiguous.
- **Module graph:** **Not ready.** The seed avoids the former Graphic Primitive contradiction but
  omits dependency-critical selection/snapshot contracts and the existing composition/presentation
  path. It does not yet rule out model/runtime or compiler/runtime cycles.

The spine should not drive independent M34 epics until the five high findings above are resolved.
