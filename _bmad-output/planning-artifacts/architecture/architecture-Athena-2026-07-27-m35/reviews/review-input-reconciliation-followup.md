# M35 Input Reconciliation Follow-up Review

Date: 2026-07-27  
Scope: Re-review only the findings in `review-input-reconciliation.md`  
Inputs: Updated M35 `prd.md`, M35 `addendum.md`, and M35 `ARCHITECTURE-SPINE.md`

## Verdict

**Not ready to freeze: 8 of 13 previous findings are closed; 5 remain partially open.** The updated
documents resolve the main physical/composition architecture, visual gate, routing proof, story
evidence, and package-authority separation. The remaining five findings collapse into three
load-bearing blockers: parent SVG decisions are not explicitly superseded, two residual statements
still bind a physical Mounted Occurrence to an Element, and the package/resource path contract still
permits incompatible implementations.

Mechanical BMAD spine lint passes with zero findings. This follow-up introduces no new review scope.

## Finding Closure

| Previous finding | Status | Follow-up assessment |
| --- | --- | --- |
| HIGH-1 - M34 SVG contract conflict | **PARTIAL - BLOCKER** | M34 AD-12/AD-13 hardening and normalization are now inherited (`ARCHITECTURE-SPINE.md:62`) and AD-10 preserves them while deleting legacy role metadata (`:193`). However, the spine still does not explicitly supersede the metadata-authority portions of M34 AD-3..AD-5/AD-8 or retire M34's required `data-athena-schema` root contract. Parent and child ADs therefore remain formally contradictory. |
| HIGH-2 - MountedOccurrence has two owners | **PARTIAL - BLOCKER** | The PRD glossary is corrected and the spine keeps physical and representation occurrences independent (`prd.md:148`, `ARCHITECTURE-SPINE.md:207`). Two conflicting remnants remain: FR-34 says "selected mounted element" (`prd.md:305`), and the addendum lists `bound element id` as a `PhysicalInstallationIR` output fact (`addendum.md:200`). The latter directly reintroduces a representation dependency into physical output. |
| HIGH-3 - Drawing-grid versus physical placement | **CLOSED** | AD-18 makes M35 Cabinet consume only the physical composition path, explicitly excludes M34 `LayoutIntent`/`SchematicPlacementFact`, and retains M34 Control Drawing on its existing regression-tested schematic path (`ARCHITECTURE-SPINE.md:308`). No precedence fallback remains. |
| HIGH-4 - Descriptor/resource/export/lock authority | **PARTIAL - BLOCKER** | Field ownership is now clear: `athena.yaml` owns package coordinate/roots/dependencies, Athena owns resources/exports, and the lock owns derived evidence (`prd.md:211`, `ARCHITECTURE-SPINE.md:149`). Snapshot identity also no longer depends on generated lock bytes. The resource path base is still unresolved: FR-5 permits either the source file or package root (`prd.md:191`), while the addendum says only "owning package context" and AD-9 does not define the resolution formula. |
| HIGH-5 - Bounds, labels, and viewport ownership | **CLOSED** | FR-18 now keeps physical extents in physical IR and derives labels/drawing bounds during composition. AD-16 assigns physical extents, composition bounds/label placement, and `PresentationDocument` viewport to distinct owners (`ARCHITECTURE-SPINE.md:279`). |
| HIGH-6 - Professional visual gate weakened | **CLOSED** | FR-39 and the addendum now carry the structural checklist. AD-16 fixes exact visible content, defect-zero conditions, desktop/narrow evidence, and states that screenshot existence alone is insufficient (`ARCHITECTURE-SPINE.md:279`). |
| MEDIUM-1 - Function-aware occurrence inheritance | **CLOSED** | M34 AD-23/AD-24 are now inherited (`ARCHITECTURE-SPINE.md:65`). AD-11 gives Cabinet v0 one physical and one resolved representation occurrence per installation key; schematic functions cannot become separate mounted products. |
| MEDIUM-2 - Package/resource path security | **PARTIAL - BLOCKER** | AD-8 now preserves hard limits and rejects absolute paths, traversal, symlinks, junctions/reparse points, external access, overlapping roots, and duplicate physical-file admission (`ARCHITECTURE-SPINE.md:161`). The cross-package rule remains inconsistent: FR-8 permits dependency-admitted cross-package reads (`prd.md:200`), while AD-8 admits only package-local resources and does not define logical exported-resource access through a dependency snapshot. |
| MEDIUM-3 - Package/path normative mapping | **PARTIAL - BLOCKER** | AD-9 now fixes portable normalization and rejects case-fold/NFC collisions (`ARCHITECTURE-SPINE.md:178`), but no rule states the exact mapping `source-root-relative parent directory == declared namespace segments`, including case/default-package behavior. FR-1 and examples express intent but do not prevent compiler/LSP divergence. |
| MEDIUM-4 - Routing proof schema | **CLOSED** | AD-12 fixes the Cabinet route fact and the consistency table now requires route count, channel usage, endpoint bindings, ordered segments, body intersections, off-channel segments, and unbound endpoints (`ARCHITECTURE-SPINE.md:429`). |
| MEDIUM-5 - Open planning questions dropped | **CLOSED** | The PRD and spine now contain resolved planning decisions/questions for the exact resource syntax and structural visual approval contract (`prd.md:427`, `ARCHITECTURE-SPINE.md:508`). |
| LOW-1 - RED/GREEN story evidence missing | **CLOSED** | AD-17 now requires RED/GREEN command evidence and AC-to-evidence review before the polish/purge gate (`ARCHITECTURE-SPINE.md:298`). |
| LOW-2 - Standard and vendor fixtures missing | **CLOSED** | AD-16 now requires one standard package and one vendor/user package in the dedicated M35 sample (`ARCHITECTURE-SPINE.md:289`). |

## Remaining Blockers

### 1. Explicitly supersede the old M34 SVG metadata authority

Add one unambiguous supersession statement or table:

- supersede the metadata-authority parts of M34 AD-3, AD-4, AD-5, and importer output in AD-8;
- retire the old required `data-athena-schema` root metadata contract;
- retain M34 AD-12/AD-13 hardening, closed geometry subset, limits, expansion checks, and coordinate
  normalization.

AD-10 has the correct runtime rule, but without explicit supersession the parent spine still contains
simultaneously adopted contradictory decisions.

### 2. Remove the last Element ownership from physical occurrence language

- Change PRD FR-34 from "selected mounted element" to "selected mounted occurrence" or "selected
  mounted subject".
- In the addendum's physical output facts, replace `bound element id` with `canonical semantic
  subject` and `InstallationOccurrenceKey`.
- Keep the resolved Element/representation id only in Cabinet composition output and trace.

This is required to keep `physical-model` independent from `representation-model` as AD-2 and AD-11
already require.

### 3. Freeze one package/resource path contract

The documents must state all three rules directly:

1. A declared resource path resolves from one base only. Recommended: the directory of the declaring
   governed Athena source unit inside its admitted package snapshot.
2. A physical path never crosses package boundaries. Dependency resources are reached only through
   an exported logical definition/resource key resolved inside the dependency's own admitted
   snapshot; otherwise change FR-8 to reject all direct cross-package resource reads.
3. For each governed source root, the normalized source-relative parent directory must equal the
   declared Athena namespace segments exactly. Define case behavior and whether a default package is
   forbidden.

Until these are fixed, compiler, LSP, package admission, and fixtures can each implement a different
but textually compliant rule.

No PRD, addendum, architecture spine, or product-code changes were made by this follow-up review.
