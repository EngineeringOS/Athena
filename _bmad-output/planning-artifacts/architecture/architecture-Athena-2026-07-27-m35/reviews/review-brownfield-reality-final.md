# M35 Architecture Spine Brownfield Reality Final Gate

**Target:** `ARCHITECTURE-SPINE.md`  
**Review type:** final brownfield reality gate  
**Date:** 2026-07-27  
**Verdict:** **FAIL - three exact blockers remain before freeze.**

The latest edits close most prior findings. The Brownfield Contract Ledger now distinguishes
existing, extended, replaced, and new contracts. Legacy `PhysicalSize` is explicitly an unchecked
input that must be validated and converted before resolved physical contracts or IR are constructed.
`RepositoryLockV2` has a replacement-only policy, exact M31 and interaction names are mostly used,
and the Cabinet path explicitly deletes or excludes parallel legacy authorities.

The deterministic spine lint passes with zero findings. The remaining failures are semantic
brownfield mismatches that mechanical lint cannot detect.

## Exact Blockers

### 1. `RepositoryManifest` incorrectly owns a resolved coordinate

AD-7 says `RepositoryManifest` is the sole typed owner of `ResolvedPackageCoordinate`
(`ARCHITECTURE-SPINE.md:175-178`), while the same rule says `RepositoryLockV2` contains resolved
coordinates (`ARCHITECTURE-SPINE.md:180-184`). That gives authored manifest intent and derived lock
state overlapping ownership of a resolved fact.

Current brownfield contracts preserve the correct authored/derived distinction:

- `RepositoryManifest` owns `PrimaryPackage` and dependency intent
  (`kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt:68-77`).
- `RepositoryLock` owns derived resolution state (`RepositoryContracts.kt:79-90`).
- No `ResolvedPackageCoordinate` type currently exists, and the ledger does not mark it
  `NEW IN M35`.

Required correction:

- `RepositoryManifest` owns authored package identity/coordinate intent, governed source roots,
  `representationPackageRoots`, and dependency intent.
- `ResolvedPackageCoordinate` is a new `repository-model` value owned by resolved graph/snapshot and
  `RepositoryLockV2`, never by the manifest.
- Add `ResolvedPackageCoordinate`, `SourceUnitId`, and `PackageResourceKey` to the contract ledger or
  explicitly group them under one `NEW IN M35` repository/package identity row.

### 2. The M31 flow still connects two contracts that do not directly connect

AD-14 now uses real names, but states:

```text
SemanticActionIntent -> SemanticAuthoringTransaction -> AuthoringPreview +
AuthoringSourceEditEvidence
```

(`ARCHITECTURE-SPINE.md:328-329`). The current `SemanticAuthoringTransaction` does not consume
`SemanticActionIntent`; it contains `intent: AuthoringIntent`
(`kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt:184-190`).
`AuthoringIntent` is the existing sealed authoring request boundary
(`kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt:79-89`).

Required correction: use the actual boundary, for example:

```text
SemanticActionIntent
  -> governed capability/command translation
  -> AuthoringIntent
  -> SemanticAuthoringTransaction
  -> AuthoringPreview + AuthoringSourceEditEvidence
  -> compile/lint -> accept/reject -> rerender
```

If M35 does not consume `SemanticActionIntent`, start the declared mutation path at
`AuthoringIntent` instead. Do not imply a direct constructor or adapter that does not exist.

### 3. The dependency diagram claims completeness but omits current direct Gradle edges

The Dependency Direction section says it is the "complete M35 target graph for touched kernel/IDE
modules" (`ARCHITECTURE-SPINE.md:390-391`). It is not complete.

Examples from current Gradle configuration:

- `compiler` directly depends on engineering, package-model, representation, projection, routing,
  language, physical, package-runtime, presentation, and drawing-composition
  (`kernel/compiler/build.gradle.kts:11-28`). Several of those direct edges are absent from the
  diagram.
- `ide:lsp` directly depends on interaction, language, package-model, package-runtime,
  physical-model, representation-model, repository-model, routing-model, compiler, and
  presentation-model (`ide/lsp/build.gradle.kts:8-30`). The diagram shows only compiler and
  presentation into LSP.

The ledger correctly records the new package-runtime, drawing-composition, and presentation-model
edges, but it does not make the diagram's completeness claim true.

Required correction: either include every direct dependency for the touched modules, or rename the
diagram to "M35 load-bearing dependency edges" and state that unchanged direct Gradle dependencies
remain code-owned and are intentionally omitted. The latter better matches a lean architecture
spine. Keep every new M35 edge explicit in the ledger.

## Verified Passes

| Gate | Result | Evidence |
| --- | --- | --- |
| Contract status ledger | PASS with blocker 1 exception | Ledger explicitly marks `EXISTING`, `EXTEND`, `REPLACE`, and `NEW IN M35` (`ARCHITECTURE-SPINE.md:433-454`). |
| Legacy `PhysicalSize` conversion | PASS | AD-3 validates legacy fields and constructs new validated measurements; unchecked input cannot enter resolved contract or IR (`ARCHITECTURE-SPINE.md:107-120`). |
| Typed manifest/lock replacement policy | PASS with blocker 1 exception | One repository parser, one canonical V2 codec, compiler-only validation/materialization, v1 deletion, and no compatibility reader are explicit (`ARCHITECTURE-SPINE.md:175-188`). |
| Exact interaction contracts | PASS | `InteractionSubjectResolver` backed by `SemanticCapabilityRegistry` is named, and frontend prefix inference is deleted (`ARCHITECTURE-SPINE.md:315-317`). |
| Exact M31 contracts | FAIL | Correct names are present, but the existing `AuthoringIntent` bridge is missing from the flow. |
| New dependency deltas | PASS | Package-runtime, drawing-composition, and presentation-model additions match current Gradle starting points (`ARCHITECTURE-SPINE.md:447-449`). |
| Complete dependency representation | FAIL | Diagram omits existing direct compiler and LSP edges while claiming completeness. |
| No parallel package authority | PASS | One typed manifest parser; duplicate YAML scanners, v1 lock codec, lock-dependent snapshot identity, and regex source scans are mandatory deletions (`ARCHITECTURE-SPINE.md:175-188`, `205-210`). |
| No parallel Cabinet authority | PASS | M35 has one Cabinet composition path; old Cabinet producers require zero-caller deletion and M34 schematic routing remains regression-only (`ARCHITECTURE-SPINE.md:377-386`, `443`). |
| No SVG metadata authority | PASS | SVG can identify geometry only; legacy role/direction/signal metadata is deleted (`ARCHITECTURE-SPINE.md:253-269`). |
| Mechanical spine lint | PASS | `lint_spine.py`: zero findings. |

## Final Gate

**FAIL.** Freeze after the three corrections above. No implementation or Gradle execution is needed
to close them; they are architecture-document consistency fixes.
