# M34 Architecture Spine Rubric Review

**Review date:** 2026-07-24  
**Artifact:** `ARCHITECTURE-SPINE.md`  
**Inputs cross-checked:** M34 `prd.md`, M34 `addendum.md`, M33 parent spine, and current brownfield module/type ownership  
**Verdict:** **NEEDS REVISION**

The direction is coherent, but the spine is not yet a reliable milestone-to-epics consistency
contract. It leaves the shared Symbol/Element IR and binding semantics open, adopts a migration rule
that does not make the migration decisions, drops an inherited Cabinet composition authority, and
seeds Graphic Primitive IR in a module that contradicts the current dependency graph. Independently
built epics can therefore comply with every written AD and still produce incompatible frontends,
policy resolution, safety profiles, and Cabinet composition paths.

## Rubric Result

| Good-spine criterion | Result | Basis |
| --- | --- | --- |
| Fixes the real divergence points one level down | **Fail** | Shared IR, binding selection, composition ownership, and security-profile semantics remain open. |
| AD rules are enforceable and prevent their stated divergence | **Fail** | AD-4, AD-6, AD-11, and AD-12 state outcomes or intentions without enough decision content to reject incompatible implementations. |
| No hidden open dimension | **Fail** | Compilation scope/registry authority, inherited composition ownership, source-toolchain placement, and compiler operational policy are neither decided nor deferred. |
| Brownfield fit | **Fail** | The structural seed relocates Graphic Primitive IR against the existing module graph and omits active M33 contracts from the migration seed. |
| Implementable one level down | **Fail** | Epics cannot derive one compatible frontend result contract, binding algorithm, or complete reuse/replace plan from the spine. |
| PRD/addendum coverage and consistency | **Partial** | The PRD and addendum are mutually consistent, but several resolved requirements do not land in the spine. |
| Parent-spine inheritance | **Fail** | Binding M33 composition and derived-bounds decisions are omitted from the inherited table and are not replaced by equivalent M34 ownership rules. |
| Mechanical spine form | **Pass** | `lint_spine.py` reports `ok: true` with no placeholders, duplicate AD ids, malformed ADs, or stack-version findings. |

## Blocking Findings

### HIGH-1 - The structural seed contradicts current Graphic Primitive IR ownership

**Evidence**

- The seed assigns Symbol/Element IR to `representation-model` but assigns Graphic Primitive IR to
  `presentation-model`: `ARCHITECTURE-SPINE.md:201` and `ARCHITECTURE-SPINE.md:205`.
- The actual renderer-neutral primitive contract is already owned by `representation-model`:
  `kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/GraphicPrimitiveModels.kt:115`
  and `GraphicPrimitiveModels.kt:242`.
- `presentation-model` already depends on `representation-model`:
  `kernel/presentation-model/build.gradle.kts:10`.
- `svg-renderer` consumes `representation-model` directly:
  `kernel/svg-renderer/build.gradle.kts:8`.
- AD-11 claims to prevent competing truths but only says to extend contracts "where they fit":
  `ARCHITECTURE-SPINE.md:126` and `ARCHITECTURE-SPINE.md:130`.

**Why this fails the spine test**

One epic can preserve `GraphicPrimitive` in `representation-model`; another can follow the seed and
create or move a primitive contract into `presentation-model`. Both are textually compliant. Moving
the contract also reverses the established dependency direction because `presentation-model`
already consumes representation contracts.

**Required disposition:** **Correct the structural seed.** Keep Graphic Primitive IR in
`representation-model`, state that `presentation-model` carries resolved presentation/composition
documents that consume it, and make that dependency direction binding.

### HIGH-2 - AD-11 defers the migration decision that it claims to adopt

**Evidence**

- AD-11 says "reuse/extend where they fit" and delegates each new type to name what it changes:
  `ARCHITECTURE-SPINE.md:126` and `ARCHITECTURE-SPINE.md:130`.
- The migration seed names only broad contracts and two M33 implementation types:
  `ARCHITECTURE-SPINE.md:184` through `ARCHITECTURE-SPINE.md:195`.
- It does not disposition active contracts including `DrawingSymbolAnatomy`
  (`kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolAnatomy.kt:149`),
  `DrawingSymbolPrimitiveCompiler`
  (`kernel/representation-model/src/main/kotlin/com/engineeringood/athena/representation/DrawingSymbolPrimitiveCompiler.kt:38`),
  `GraphicPrimitiveDocument` (`GraphicPrimitiveModels.kt:242`), or `PresentationDocument`
  (`kernel/presentation-model/src/main/kotlin/com/engineeringood/athena/presentation/PresentationDocument.kt:32`).
- FR-23 requires a published `Reuse / Extend / Replace / Delete` map for M30-M33 types:
  `prd.md:170`.
- The addendum makes that rule exhaustive: "every M30-M33 representation type":
  `addendum.md:170` and `addendum.md:172`.

**Why this fails the spine test**

An Athena-frontend epic can extend `DrawingSymbolAnatomy` while an SVG-frontend epic introduces a
new `SymbolIr`; both can claim the old contract did or did not "fit." The conflict appears only at
integration, exactly the divergence AD-11 says it prevents.

**Required disposition:** **Tighten AD-11 and complete a named migration companion before epic
breakdown.** Every active representation, package, resolver, composition, transport, proof, loader,
and renderer contract needs one action, the named successor/adapter when applicable, and deletion
verification for retired authorities. The spine may reference that companion rather than embedding
the whole inventory.

### HIGH-3 - The single-IR promise has no canonical contract for two independent frontends

**Evidence**

- AD-2 permits composition of Symbols or undefined "governed visual groups":
  `ARCHITECTURE-SPINE.md:63` and `ARCHITECTURE-SPINE.md:67`.
- AD-4 requires both frontends to lower into "one typed IR" with the same diagnostics and proof, but
  names no schema or owner: `ARCHITECTURE-SPINE.md:77` and `ARCHITECTURE-SPINE.md:81`.
- AD-10 rejects duplicate identity without defining the registry/compilation scope in which
  identity is unique: `ARCHITECTURE-SPINE.md:119` and `ARCHITECTURE-SPINE.md:123`.
- FR-22 and the addendum require the same typed result, validation, diagnostics, provenance, and
  proof payload: `prd.md:167` and `addendum.md:98` through `addendum.md:110`.
- Existing `DrawingSymbolAnatomy` already supplies identity, version, package, tags, lifecycle,
  primitives, anchors, slots, and hotspots: `DrawingSymbolAnatomy.kt:149` through
  `DrawingSymbolAnatomy.kt:160`; AD-11 does not say which of those remain canonical.

**Why this fails the spine test**

The two frontend epics can independently choose incompatible identity/version coordinates,
composition-reference shape, anchor export and collision behavior, variant semantics, provenance
merge rules, and compilation-result envelopes while satisfying AD-1 through AD-4. "Same IR" is an
acceptance assertion, not an enforceable architecture rule, until that shared contract is named.

**Required disposition:** **Add an IR contract companion and bind AD-4 to it.** At minimum decide
the canonical owner and coordinates for identity/version/package, composition references and cycle
handling, exported-anchor collision rules, variants, lifecycle/provenance, compilation scope, and
the common result/diagnostic/proof envelope. Syntax details can remain with the syntax story.

### HIGH-4 - Binding policy has no complete deterministic selection rule

**Evidence**

- AD-6 requires explicit policy selection and lists facts that fail binding, but does not define
  matching cardinality, precedence, or ambiguity handling: `ARCHITECTURE-SPINE.md:91` through
  `ARCHITECTURE-SPINE.md:96`.
- FR-18 requires first-class typed selection from semantic facts and projection context, while NFR-4
  requires deterministic fail-closed behavior: `prd.md:155` and `prd.md:252`.
- The addendum defers only grammar and says the IR boundary remains the existing M32 resolver
  contract: `addendum.md:134` through `addendum.md:142`.
- The existing resolver gives manifest package order authority and selects the first compatible
  package: `kernel/package-runtime/src/main/kotlin/com/engineeringood/athena/packageruntime/BindingResolver.kt:144`
  through `BindingResolver.kt:152`; descriptor ambiguity separately fails closed at
  `BindingResolver.kt:166` through `BindingResolver.kt:185`.

**Why this fails the spine test**

One epic can retain manifest-order package selection and tag matching; another can implement
specificity ranking, source order, or version preference for new Element selectors. Each is
"explicit" and can fail on missing facts, but they select different Elements for the same input.
The spine also does not decide selector vocabulary, profile/variant interaction, package version
resolution, or whether zero/multiple matches share the existing diagnostic behavior.

**Required disposition:** **Amend AD-6 with the binding IR and resolution algorithm.** Name the
allowed input facts, exactly-one-result rule, candidate ordering or specificity rule, profile and
variant precedence, package/version selection, tie behavior, and stable diagnostics. Explicitly say
which existing `BindingResolver` semantics remain authoritative and which are replaced.

### HIGH-5 - M34 drops the inherited owner of Cabinet composition and document bounds

**Evidence**

- M33 AD-4 assigns frame, zones, lanes, label bands, terminal-strip grouping, route channels,
  references, and bounds facts to sheet composition: M33 `ARCHITECTURE-SPINE.md:78` through
  M33 `ARCHITECTURE-SPINE.md:84`.
- M33 AD-8 derives framing from primitives, composition, routed anchors, labels, and governed
  margins: M33 `ARCHITECTURE-SPINE.md:108` through M33 `ARCHITECTURE-SPINE.md:113`.
- Neither decision appears in M34's inherited table: `ARCHITECTURE-SPINE.md:43` through
  `ARCHITECTURE-SPINE.md:52`.
- M34 AD-14 says document bounds derive from composition occurrences and routing facts, but does not
  own or produce those facts: `ARCHITECTURE-SPINE.md:147` through `ARCHITECTURE-SPINE.md:152`.
- The existing brownfield owner is concrete: `DrawingSheetStructureRequest` contains rails, lanes,
  terminal strips, label bands, and route channels at
  `kernel/drawing-composition/src/main/kotlin/com/engineeringood/athena/drawing/composition/DrawingSheetStructureModels.kt:79`
  through `DrawingSheetStructureModels.kt:87`, and `DrawingSheetStructurePlan` emits them at
  `DrawingSheetStructureModels.kt:151` through `DrawingSheetStructureModels.kt:159`.
- FR-35 still requires enclosure, rails, route channels, and drawing frame: `prd.md:210`.

**Why this fails the spine test**

An Element/composition epic can absorb rails, route channels, or document bounds into Element IR,
while a Cabinet epic can continue using `drawing-composition`. Both satisfy AD-14 because it names
inputs but not their authority. This silently weakens a binding parent decision and risks making
reusable assets own project/document layout.

**Required disposition:** **Restore M33 AD-4 and AD-8 as inherited invariants.** Keep
`drawing-composition` as the owner of Cabinet sheet facts, include the existing module in the
structural seed and migration map, and state how its output joins Element occurrences before
document bounds are derived.

### HIGH-6 - The safe SVG profile is not one enforceable, versioned policy

**Evidence**

- AD-12 names an allowlist, resource limits, confinement, and reference validation but defines no
  policy identity, authority for configured values, or accounting model:
  `ARCHITECTURE-SPINE.md:133` through `ARCHITECTURE-SPINE.md:138`.
- FR-26 requires bounds for six independent dimensions and FR-27 explicitly requires symlink-escape
  rejection: `prd.md:181` through `prd.md:185`.
- The addendum lists the initial allowed primitive subset but reduces all limits to "configured
  complexity limits": `addendum.md:144` through `addendum.md:158`.
- AD-13 covers transform/reference correctness, not how recursive `defs`/`use`, external file size,
  or transform expansion consumes a shared budget: `ARCHITECTURE-SPINE.md:140` through
  `ARCHITECTURE-SPINE.md:145`.

**Why this fails the spine test**

Compiler, package, and test epics can choose different defaults, configuration sources, path roots,
symlink canonicalization order, and recursive-reference accounting while all claiming an allowlist
and limits. A permissive runtime configuration could then invalidate the security guarantee without
violating AD-12.

**Required disposition:** **Bind AD-12 to a versioned safe-profile contract.** Define the policy
owner, allowed constructs/style features, fixed versus configurable limits and who may configure
them, canonical repository-root resolution before access, symlink handling, recursive expansion
budgets, and stable rejection diagnostics. Numeric values may live in a companion, but authority and
accounting must be architectural.

## Additional Findings

### MEDIUM-1 - Resolved source forms and existing language-toolchain placement did not land

**Evidence:** The PRD fixes `.athena.element`, permits `symbol` in that source form, and fixes
annotated `.svg` as the other carrier (`prd.md:308` through `prd.md:315`); FR-12 requires ANTLR4,
tree-sitter, and LSP support (`prd.md:135`). The spine's structural seed names only a generic
`compiler` and omits `kernel/language`, tree-sitter, and LSP (`ARCHITECTURE-SPINE.md:197` through
`ARCHITECTURE-SPINE.md:211`), although the brownfield already has `:kernel:language`
(`settings.gradle.kts:42`), ANTLR (`kernel/language/build.gradle.kts:6`), LSP dependency on that
module (`ide/lsp/build.gradle.kts:16`), and a tree-sitter package
(`ide/tree-sitter-athena/package.json:3`).

**Action:** Add the two resolved source forms to the paradigm/source rules and seed changes into the
existing language, tree-sitter, and LSP boundaries. Keep exact grammar productions deferred to the
syntax story as the addendum permits.

### MEDIUM-2 - The inherited-invariant table is incomplete and one attribution is misleading

**Evidence:** The M34 table cites only selected parent decisions (`ARCHITECTURE-SPINE.md:43` through
`ARCHITECTURE-SPINE.md:52`) and omits at least M33 AD-3, AD-4, AD-6, and AD-8. It attributes
"Renderer is paint-only" solely to M33 AD-2 at `ARCHITECTURE-SPINE.md:49`, while M33 itself carries
the resolved-handle/paint-only constraint from M32 AD-10 at M33 `ARCHITECTURE-SPINE.md:51`.

**Action:** List every parent AD that remains binding by its stable id, including transitive source
where needed. If M34 replaces a parent decision, state the conflict and resolve it rather than
silently omitting the parent.

### MEDIUM-3 - Compiler operational behavior is a hidden dimension rather than an explicit deferment

**Evidence:** The Deferred table covers product features and marketplace trust only
(`ARCHITECTURE-SPINE.md:214` through `ARCHITECTURE-SPINE.md:222`). It is silent on compilation
unit/registry scope, deterministic cache keys, incremental versus full rebuild authority, limit
configuration by environment, and invalidation when referenced SVG/package content changes. AD-10's
duplicate-identity rejection (`ARCHITECTURE-SPINE.md:123`) and AD-12's resource limits
(`ARCHITECTURE-SPINE.md:137`) require at least a defined compilation scope and configuration
authority to be enforceable.

**Action:** Decide the compilation/registry scope and configuration authority now; explicitly defer
cache and incremental strategy with a revisit condition if they are not cross-epic invariants for
M34.

## PRD And Addendum Reconciliation

No direct contradiction was found between the M34 PRD and its addendum. The addendum consistently
elaborates the PRD's source-authority, typed-compilation, binding, SVG-safety, coordinate, and
migration requirements. The contradictions are between that combined requirement set and what the
spine either omits or leaves non-enforceable:

| Input decision | Spine status | Required reconciliation |
| --- | --- | --- |
| `.athena.element` plus annotated `.svg` are the two authoritative source forms (`prd.md:308`, `addendum.md:19`) | Missing from ADs and structural seed | Carry the resolved extensions and existing language tooling into source/dependency rules. |
| Both forms produce the same typed result, diagnostics, provenance, and proof (`prd.md:167`, `addendum.md:110`) | Asserted by AD-4 without a named contract | Add the shared IR/result companion and compilation scope. |
| Existing M32 Binding Resolver remains the IR boundary (`addendum.md:140`) | "Reuse/extend" and new selection semantics remain open | State retained semantics and explicit M34 changes. |
| Every M30-M33 representation type receives one migration action (`prd.md:170`, `addendum.md:172`) | Seed is illustrative rather than exhaustive | Publish and bind the complete named migration map. |
| Safe subset, bounded complexity, and symlink-safe confinement (`prd.md:175`, `addendum.md:144`) | Security mechanisms named without one policy contract | Add a versioned profile with authority and accounting rules. |
| Cabinet composition and derived bounds remain required (`prd.md:194`, `prd.md:207`, `prd.md:210`) | Parent composition owner omitted | Re-inherit M33 AD-4/AD-8 and retain the brownfield module boundary. |

## Adversarial Convergence Check

The spine currently fails the defining test: two independently built, fully compliant epics can
still diverge in at least these ways:

1. The Athena frontend emits an extension of `DrawingSymbolAnatomy`; the SVG frontend emits a new
   Symbol/Element result with different identity and proof payloads.
2. Binding policy A preserves manifest-order selection; policy B ranks selectors by specificity or
   package version. Both are explicit and fail closed on missing facts.
3. Cabinet composition A remains in `drawing-composition`; composition B moves rails, channels, and
   bounds into Element IR because M34 no longer names the inherited owner.
4. SVG compiler A accounts expanded `use` trees against one global budget; compiler B applies limits
   per referenced node/file. Both enforce "configured complexity limits" but accept different
   adversarial inputs.
5. Renderer integration A consumes the existing representation-owned Graphic Primitive IR;
   integration B follows the structural seed and creates a presentation-owned equivalent.

Until those choices are fixed or explicitly deferred behind a single owner and revisit condition,
the spine should not be used as the contract for M34 epic decomposition.
