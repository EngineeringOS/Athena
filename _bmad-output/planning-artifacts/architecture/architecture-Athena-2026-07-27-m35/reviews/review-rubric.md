# M35 Architecture Spine Rubric Review

## Gate Verdict

**CHANGES REQUIRED.** The spine is mechanically clean and its compiler-owned physical-projection
paradigm is sound, but it is not ready to freeze. Six high-severity gaps still allow independently
implemented M35 stories to produce incompatible physical contracts, geometry evaluation, package
resolution, or module dependencies.

No critical finding invalidates the milestone direction. The required work is architectural
tightening, not redesign.

## Review Scope And Evidence

Reviewed against the BMAD good-spine checklist:

- target `ARCHITECTURE-SPINE.md`;
- M35 PRD and addendum;
- M35 architecture memlog;
- inherited M14 and M34 architecture decisions;
- M34 retrospective and M35 handoff;
- current Gradle module dependencies;
- current `PhysicalTraitDefinition`, `ResolvedPhysicalTraitDefinition`, package snapshot, binding,
  layout, geometry, representation, and compiler code located through CodeGraph;
- deterministic spine lint.

Mechanical result:

```text
lint_spine.py: PASS
findings: 0
```

## Good-Spine Checklist

| Check | Result | Judgment |
| --- | --- | --- |
| Fixes real divergence points for stories | **Fail** | Ownership is mostly clear, but contract-source precedence, physical geometry semantics, routing semantics, and migration from the active layout/geometry path remain open. |
| Every AD is enforceable and prevents its stated divergence | **Partial** | Most authority ADs are testable; AD-3, AD-4, AD-6, AD-11, AD-12, and AD-13 still permit materially different compliant implementations. |
| Deferred contains every decision unsafe to leave open | **Fail** | The PRD's resource-syntax and visual-checklist questions are neither decided nor carried into Deferred/Open Questions. |
| Named technology is reality-checked | **Pass** | The stack is explicitly ratified rather than newly selected, and the listed Kotlin, Gradle, ANTLR, LSP4J, Theia, TypeScript, Node, and tree-sitter lines match current repository configuration. |
| Brownfield fit | **Fail** | The new path does not settle how existing `LayoutIrDeriver`, `GeometryIrDeriver`, and Cabinet composition are replaced, reused, or retired; one dependency arrow contradicts current Gradle ownership. |
| PRD capability coverage | **Partial** | FR ranges are cited, but FR-18/21/24/28-31/39 lack sufficient cross-story invariants for bounds, constraint geometry, routing, and visual acceptance. |
| Inherited spine remains intact | **Fail** | Relevant M34 SVG hardening, coordinate normalization, document-bounds, and visual-acceptance ADs are not listed as inherited and are not fully restated. |
| Every feature-altitude dimension is decided/deferred/open | **Partial** | Local/offline execution is covered, but resource/security limits and operational behavior for untrusted package assets are not preserved explicitly. |

## High Findings

### H1 - Active brownfield layout and geometry paths have no migration decision

**Evidence:** The new pipeline and structural seed introduce `PhysicalInstallationIR` and a Cabinet
composition compiler, while current product code still derives Cabinet-facing output through
`kernel/compiler/.../LayoutIrDeriver.kt` and `GeometryIrDeriver.kt`. The M34 handoff explicitly names
both as the current weak path. AD-17 requires generic purge, but no AD says whether M35 Cabinet:

- replaces Layout IR and Geometry IR;
- lowers PhysicalInstallationIR through either existing IR;
- retains them for non-Cabinet projections only; or
- deletes their Cabinet producers after proof migration.

Two story teams can obey every current AD and still build parallel Cabinet pipelines.

**Required disposition:** **Discuss, then fix in the spine.** Add one migration/ownership AD naming
the single active M35 Cabinet path and the exact compatibility/deletion boundary for existing Layout
IR, Geometry IR, and old composition producers. This decision must also prevent a fourth parallel
spatial/rendering IR.

### H2 - Dependency direction contradicts Gradle and omits required owners

**Evidence:** In the dependency diagram, arrows otherwise run from a lower-level dependency to its
consumer (`engineering-model -> physical-model -> compiler`). On that convention,
`compiler -> presentation-model` is reversed. Current brownfield reality is the opposite:
`kernel/compiler/build.gradle.kts:21` declares a dependency on `kernel:presentation-model`.

The same diagram omits `routing-model` even though the capability map assigns physical routing to
it, and omits the interaction/provenance dependencies needed by AD-13. As written, stories cannot
tell whether those are value-only projections, module dependencies, or compiler-owned joins.

**Required disposition:** **Autofix after ownership is confirmed.** Reverse the compiler/presentation
edge and add the routing, interaction, and provenance edges or explicitly state that neutral copied
IDs/digests cross those boundaries without module dependencies.

### H3 - Physical contract source precedence is undefined

**Evidence:** AD-3 permits both existing `ResolvedPhysicalTraitDefinition` and explicit project
installation facts to feed one `PhysicalInstallationContract`. Current physical traits already own
size and mounting type. The spine does not define what happens when both sources provide different
footprint, mounting, orientation, clearance, or compatibility values.

One implementation can let project facts override package knowledge; another can prefer resolved
traits; a third can merge field-by-field. All comply with the current Rule, but they produce
different physical truth and diagnostics.

**Required disposition:** **Discuss, then fix in the spine.** Define one compiler-owned resolution
rule with provenance. Prefer explicit, typed override semantics or fail closed on conflicting
authorities; do not allow implicit field precedence or source-order precedence.

### H4 - Constraint evaluation is named but not geometrically defined

**Evidence:** AD-4 through AD-6 specify integer millimetres, containers, fit, collision, clearance,
orientation, and compatibility, but omit the conventions shared by evaluator, composition, proof,
and E2E:

- coordinate origin and axis direction;
- admitted orientation values and footprint rotation;
- rectangle boundary convention;
- whether edge contact counts as collision;
- how per-side clearance expands a footprint;
- whether clearance zones may overlap each other, ducts, rails, or enclosure boundaries;
- deterministic handling of equal positions and ordering.

These are not per-story details. Different choices create incompatible IR facts and acceptance
proof.

**Required disposition:** **Fix.** Add a compact physical-geometry convention AD. Keep the data seed
small, but make the evaluator semantics falsifiable.

### H5 - Routing ownership exists, but route semantics do not

**Evidence:** AD-12 correctly separates engineering endpoints, physical channel topology, and bound
terminal anchors. It does not define the V0 route contract needed to prove `off-channel segment`,
`body intersection`, or endpoint binding consistently. The capability map additionally introduces
`routing-model` without defining what it owns relative to `physical-model`.

Two compliant stories can choose centerline routes versus channel-area routes, different endpoint
transition rules, arbitrary polylines versus orthogonal segments, and different intersection
tolerances.

**Required disposition:** **Fix.** Define the V0 route vocabulary and containment/intersection
conventions, and assign route topology, solved route facts, and proof metrics to exact modules.
General path optimization can remain Deferred.

### H6 - Relevant inherited safety, bounds, and visual gates were dropped

**Evidence:** M35 consumes complex untrusted SVG assets and changes Cabinet bounds and visual proof,
but its inherited table omits:

- M34 AD-12: closed, hardened SVG subset and aggregate resource limits;
- M34 AD-13: compile-time coordinate normalization;
- M34 AD-14: intrinsic asset bounds versus document bounds;
- M34 AD-15: concrete visual acceptance boundary.

M35 AD-10 says "safe SVG lowering" but does not preserve parser hardening, external-I/O rejection,
DOM/reference limits, or transform normalization. AD-16 requires screenshots but does not settle the
PRD's mandatory structural checklist. This weakens inherited behavior precisely where M35 expands
resource admission.

**Required disposition:** **Fix.** Add these parent AD IDs to Inherited Invariants and state any M35
extensions, including package/file/byte/DOM/reference budgets and document-bounds ownership. Do not
silently weaken them.

## Medium Findings

### M1 - PRD open questions disappeared instead of being resolved or deferred

The PRD still asks for the minimum typed `resource` syntax and the mandatory visual checklist. AD-15
only says the syntax is minimal; AD-16 only says screenshots are required. The spine has no Open
Questions section and its Deferred table contains neither item.

**Disposition:** Resolve both before epic/story generation. If either is intentionally delegated,
put it in Deferred with a named owning story and a revisit/acceptance condition.

### M2 - GraphicOccurrenceTrace ownership can create an accidental module dependency

AD-13 requires a trace containing semantic, physical, binding, representation, package snapshot, and
source-declaration evidence. The structural seed places the trace projection in
`presentation-model`, but the dependency diagram does not define whether that model imports
package-runtime/repository/compiler types. Current `presentation-model` is a lower model consumed by
the compiler and does not depend on those orchestration modules.

**Disposition:** Define a neutral trace-value contract: stable IDs, hashes, and source spans owned by
an appropriate lower model, assembled by the compiler. Forbid presentation-model from importing
compiler or runtime orchestration types.

### M3 - Descriptor and lock extension ownership is not fully pinned

AD-7 and AD-9 establish the existing `athena.yaml`/`athena.lock` authority and identity material, but
do not name the canonical schema owner, lock materializer, atomic write rule, or behavior for stale
locks after resource changes. This is important because current code already has a compiler-owned
repository lock materializer while package-runtime stages resources.

**Disposition:** Tighten the existing AD: repository-model owns descriptor/lock value contracts;
compiler owns canonical lock materialization and stale-lock diagnostics; package-runtime supplies
verified resource evidence but never writes or interprets lock authority independently.

## Low Finding

### L1 - Stack ranges are repository-compatible but not reproducibility evidence

`TypeScript 5.9.x`, `Node >=22`, `tree-sitter CLI >=0.26.1`, and `web-tree-sitter 0.26.x` accurately
describe repository constraints, but they are not exact toolchain pins. This is acceptable for a
ratified stack table only if the checked-in lockfiles remain the reproducibility authority.

**Disposition:** Clarify that package-manager lockfiles pin the executable dependency graph used by
CI/E2E; otherwise record exact verified versions in proof tooling.

## What Passes

- The compiler-owned physical projection paradigm is the correct M35 abstraction.
- Semantic, physical, representation, composition, and paint-only renderer authorities are
  directionally separated.
- M35 avoids creating an Engineering Component System or a second package kind.
- Athena remains metadata SSOT; SVG remains geometry-only input.
- Binding selection and occurrence construction remain separate.
- Trace cannot become a graphic mutation authority.
- Offline deterministic execution, single Cabinet surface, sequential Windows Gradle verification,
  and per-story polish/purge are explicit.
- Deferred ECS, solver/Auto Layout AI, registry/trust, extra views, importer, and graphic editing are
  appropriate and have usable revisit conditions.

## Gate Summary

```text
Critical: 0
High:     6
Medium:   3
Low:      1
Verdict:  CHANGES REQUIRED
```

The spine should be updated before BMAD epic/story generation. The direction does not need to
change; the missing work is to make the existing direction singular, enforceable, and compatible
with Athena's current module graph.
