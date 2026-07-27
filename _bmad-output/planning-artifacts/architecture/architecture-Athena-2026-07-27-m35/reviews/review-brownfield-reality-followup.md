# M35 Architecture Spine Brownfield Reality Follow-up

**Target:** `ARCHITECTURE-SPINE.md`  
**Baseline:** `reviews/review-brownfield-reality.md`  
**Review type:** follow-up brownfield reality check  
**Date:** 2026-07-27  
**Verdict:** **FREEZE BLOCKED - the major architecture corrections landed, but four divergence holes remain.**

The updated spine fixes the original lock/snapshot cycle at the architecture level, keeps physical
routing out of the schematic `routing-model`, defines trace as typed references and digests, and
makes Cabinet the only M35 composition path. Those are substantive corrections.

The remaining blockers are narrower. They are still freeze blockers because the current repository
does not contain the proposed M35 contracts, and the spine does not yet tell story authors where
existing contracts end, how invalid physical traits become valid physical values, or which typed
repository contract exclusively owns package roots and the new lock schema.

## Freeze Blockers

### 1. [HIGH] The required `EXISTING / EXTEND / NEW IN M35` contract ledger is still absent

The spine marks all M35 decisions `[ADOPTED]` and lists target modules and contracts, but it does not
separate current brownfield APIs from M35 additions. A repository-wide search finds no current
definitions for `PhysicalInstallationIR`, `PhysicalInstallationContractResolver`,
`ResolvedPhysicalInstallationContract`, `InstallationOccurrenceKey`, `CabinetCompositionCompiler`,
`CabinetRoutingCompiler`, `GraphicOccurrenceTraceTable`, `PackageResourceKey`, or
`AdmittedPackageSnapshot`.

Current module reality also differs from the target dependency diagram:

- `kernel:physical-model` currently depends only on `engineering-model`
  (`kernel/physical-model/build.gradle.kts:5-7`).
- `kernel:drawing-composition` currently depends on engineering, projection, and representation,
  but not `physical-model` (`kernel/drawing-composition/build.gradle.kts:5-9`).
- `kernel:presentation-model` currently depends on engineering, document projection, layout,
  schematic routing, and representation, but not physical, package, or interaction models
  (`kernel/presentation-model/build.gradle.kts:5-11`).
- `kernel:interaction-model` currently depends only on engineering
  (`kernel/interaction-model/build.gradle.kts:5-7`).

These are valid M35 changes, not defects in the current code. The freeze problem is that the spine's
Structural Seed and Capability Map do not label them as new work. Add one compact ledger naming each
contract/module edge as `EXISTING`, `EXTEND`, or `NEW IN M35`. `[ADOPTED]` must continue to mean an
accepted decision, not an implemented API.

### 2. [HIGH] The physical measurement boundary still does not close the unchecked `PhysicalSize`

AD-5 requires validated integer millimetres, positive dimensions, non-negative positions and
clearances, and diagnostics before invalid value construction (`ARCHITECTURE-SPINE.md:121-133`).
AD-3 also consumes existing resolved physical traits.

The current trait boundary remains:

```kotlin
data class PhysicalSize(
    val widthMillimeters: Int,
    val heightMillimeters: Int,
    val depthMillimeters: Int,
)
```

It has no invariant checks (`kernel/physical-model/src/main/kotlin/com/engineeringood/athena/physical/PhysicalTraitModels.kt:11-15`), and it is already used by electrical-domain physical knowledge.
The spine does not choose whether M35 strengthens this existing public value, replaces it with new
validated physical value types, or validates and converts it at the trait-to-installation boundary.

Bind one path before freeze. The least disruptive brownfield choice is: keep `PhysicalSize` as legacy
trait input, validate every field in `PhysicalInstallationContractResolver`, and construct new
validated M35 measurement types only after diagnostics pass. No unchecked `PhysicalSize` may enter
`ResolvedPhysicalInstallationContract` or `PhysicalInstallationIR`.

### 3. [HIGH] Package roots and the new lock schema still lack one named typed owner

AD-7 through AD-9 correctly require one `athena.yaml` contract, AST-driven resource admission, and a
snapshot digest that never hashes generated lock bytes (`ARCHITECTURE-SPINE.md:149-193`). This fixes
the old circular design. It does not yet identify the exact typed contract that owns
`representationPackageRoots`, resource-root intent, and the lock's new digest/hash/schema fields.

Current repository reality has three independent root readers:

- `RepositoryManifest` has only `primaryPackage` and `dependencies`
  (`kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt:72-77`).
- `AthenaRepositoryContractLoader` parses `representationPackageRoots` separately and stores them on
  its validation result (`AthenaRepositoryContractLoader.kt:100-105`, `139`, `241-262`).
- `RepresentationPackageSnapshotStager` parses the same YAML block again
  (`RepresentationPackageSnapshotStager.kt:39`, `222-236`).
- LSP code also recognizes that manifest block independently
  (`ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageFeatures.kt:1811`).

The current `RepositoryLock` is schema version 1 and contains only the primary package and resolved
packages (`RepositoryContracts.kt:84-90`); it has no snapshot digest, resource hash, or compiler
schema fields. The current stager also still puts `dependencyLockDigest` into snapshot identity
(`RepresentationPackageSnapshotStager.kt:129-133`, `202-219`).

Before freeze, state that `RepositoryManifest` is extended to own typed package roots, one repository
loader/parser supplies them to every consumer, and all other line scanners are deleted. Also bind the
replacement lock contract and deterministic serializer as a new schema version. Because Athena is
unreleased, M35 may replace v1 without a compatibility reader; the active path must not retain both
schemas or both snapshot identity algorithms.

### 4. [HIGH] AD-14 and AD-13 still name non-existent brownfield aliases as if they were contracts

AD-14 writes the mutation path as:

```text
ActionIntent -> AuthoringTransaction -> SourceMutationPreview
```

(`ARCHITECTURE-SPINE.md:253-263`). The current contracts are `SemanticActionIntent`
(`kernel/interaction-model/.../InteractionModels.kt:138-145`), `SemanticAuthoringTransaction`
(`kernel/authoring-model/.../AuthoringTransactionModels.kt:184-197`), `AuthoringPreview`, and
`AuthoringSourceEditEvidence` (`AuthoringPreviewModels.kt:55-93`). There is no generic
`ActionIntent`, `AuthoringTransaction`, or `SourceMutationPreview` contract.

AD-13 also says an existing "interaction subject index" resolves selection
(`ARCHITECTURE-SPINE.md:251`). The repository actually has `SemanticCapabilityRegistry` plus
`InteractionSubjectResolver` (`InteractionSubjectResolver.kt:13-26`), not an
`InteractionSubjectIndex`.

Replace the aliases with exact existing contract names, or label the flow explicitly as conceptual.
Also bind removal of frontend semantic-id prefix inference when the typed trace table becomes active;
otherwise the old heuristic can survive beside the new trace authority.

## Original Finding Reconciliation

| Original | Follow-up status | Evidence |
| --- | --- | --- |
| 1. Lock/snapshot digest cycle | **Resolved in design** | AD-9 excludes generated lock bytes and makes lock output/evidence. Current cyclic stager is explicit replacement work. |
| 2. Existing versus target contracts | **Open - blocker** | No contract ledger; target symbols do not exist in current source. |
| 3. Schematic routing leakage | **Resolved** | AD-12 assigns topology to `physical-model`, route geometry to `drawing-composition`, and explicitly excludes existing `routing-model`. |
| 4. Trace imports runtime objects | **Resolved** | AD-13 transports typed ids, spans, and digests only; no snapshot object or source bytes. |
| 5. Unchecked physical measurements | **Open - blocker** | AD-5 states invariants but does not bind conversion from unchecked current `PhysicalSize`. |
| 6. Untyped `representationPackageRoots` | **Open - blocker** | Target says one path, but no exact typed owner is named; current duplicate readers remain. |
| 7. Snapshot rewrite hidden | **Resolved in design** | AD-8 names two-phase capture/admission and deletion of current regex scans. Ledger is still needed to mark it new. |
| 8. Two-phase atomicity | **Resolved** | AD-8 binds parsing to staged source bytes and requires before/after resource identity verification. |
| 9. M31 contract aliases | **Open - blocker** | AD-14 still uses names absent from the repository. |
| 10. Interaction subject index | **Open - blocker** | Exact existing registry/resolver boundary is still not named. |
| 11. Cabinet composition host | **Resolved** | AD-18 defines one new cohesive Cabinet compiler path beside the retained M34 schematic regression path. |
| 12. Paint-only renderer status | **Resolved for M35 path** | AD-16 and AD-18 exclude direct SVG/box producers from active Cabinet composition; Kotlin SVG can remain only outside that path. |
| 13. Lock resource/schema fields | **Partially resolved** | Digest ownership is clear; exact lock schema replacement and serializer ownership are not. Included in blocker 3. |
| 14. M35 sample and scripts | **Resolved as target work** | AD-16 and Structural Seed make the sample, startup, smoke, screenshots, and canvas proof deliverables rather than existing substrate. |
| 15. Shared parser corpus | **Resolved brownfield support** | Both ANTLR and Tree-sitter already consume `examples/m23/parser-parity-proof`; M35 can extend that pattern. |
| 16. Stack accuracy | **Open - non-blocking** | Tree-sitter declarations are ranges and lock to 0.26.11; Electron 39.8.7 remains omitted despite normative Electron screenshots. |
| 17. Resource key source coupling | **Open - non-blocking** | AD-9 explicitly makes source path part of identity but still does not state declaration/import and source-move behavior. |

## Non-blocking Corrections Before Handoff

1. Change the Stack wording from exact "ratified repository versions" to declared constraints plus
   resolved versions. `tree-sitter-cli` is declared `>=0.26.1` and resolves 0.26.11;
   `web-tree-sitter` is declared `^0.26.0` and resolves 0.26.11. Add Electron 39.8.7 if Electron E2E
   remains normative.
2. Clarify whether moving the sole resource declaration to another source unit intentionally changes
   `PackageResourceKey`. If yes, state that other source units import/reference the one declaration;
   if no, key by package coordinate plus fully-qualified declaration id and keep `SourceUnitId` as
   provenance only.
3. Name `examples/m23/parser-parity-proof` as the existing cross-parser corpus pattern so stories do
   not create another corpus.

## Freeze Recommendation

Do not freeze yet. Apply the four blocker corrections above, then rerun this brownfield check. No
implementation or Gradle verification is required to close these architecture-document blockers.
