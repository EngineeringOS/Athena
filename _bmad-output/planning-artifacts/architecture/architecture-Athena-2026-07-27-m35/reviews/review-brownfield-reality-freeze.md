# M35 Architecture Spine Brownfield Reality Freeze Gate

**Target:** `ARCHITECTURE-SPINE.md`  
**Review type:** final focused brownfield recheck  
**Date:** 2026-07-27  
**Verdict:** **PASS - no freeze blockers remain in the reviewed boundaries.**

## Verified Boundaries

### Authored versus resolved package coordinates

PASS.

- `RepositoryManifest` owns authored package identity/coordinate intent, source roots,
  `representationPackageRoots`, and dependency intent (`ARCHITECTURE-SPINE.md:195-197`).
- `ResolvedPackageCoordinate` is explicitly derived state owned by the resolved graph/snapshot and
  recorded by `RepositoryLockV2`, never by the manifest (`ARCHITECTURE-SPINE.md:197-198`).
- The Brownfield Contract Ledger marks `RepositoryManifest` as `EXTEND`, lock schema v1 as
  `REPLACE`, and `ResolvedPackageCoordinate`, `SourceUnitId`, and `PackageResourceKey` as
  `NEW IN M35` (`ARCHITECTURE-SPINE.md:479-488`).
- This preserves the current repository-model distinction between authored `RepositoryManifest`
  and derived `RepositoryLock`/`ResolvedPackageGraph`
  (`kernel/repository-model/src/main/kotlin/com/engineeringood/athena/repository/RepositoryContracts.kt:68-90`, `134-142`).

No parallel manifest, compatibility reader, duplicate YAML scanner, or lock-dependent snapshot
identity is allowed on the completed M35 path.

### Exact M31 authoring flow

PASS.

The spine now declares:

```text
SemanticActionIntent
  -> governed capability/command translation
  -> AuthoringIntent
  -> SemanticAuthoringTransaction
  -> AuthoringPreview + AuthoringSourceEditEvidence
  -> compile/lint -> accept/reject -> rerender
```

(`ARCHITECTURE-SPINE.md:354-356`). This matches current boundaries:

- `SemanticActionIntent` is the interaction request contract
  (`kernel/interaction-model/src/main/kotlin/com/engineeringood/athena/interaction/InteractionModels.kt:138-145`).
- `AuthoringIntent` is the sealed governed authoring request boundary
  (`kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt:79-89`).
- `SemanticAuthoringTransaction` contains `intent: AuthoringIntent`
  (`kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringTransactionModels.kt:184-197`).

The ledger reuses these exact contracts and permits no aliases (`ARCHITECTURE-SPINE.md:475`).

### Dependency subgraph scope

PASS.

- The diagram is now explicitly an M35 load-bearing dependency subgraph, not a complete Gradle
  graph (`ARCHITECTURE-SPINE.md:415-421`).
- Unshown existing direct dependencies remain code-owned unless an AD or ledger row removes them.
- New M35 edges are explicit in both the graph and ledger: repository-model to package-runtime,
  physical-model to drawing-composition, and physical/package/interaction models to
  presentation-model (`ARCHITECTURE-SPINE.md:423-452`, `482-484`).
- The declared starting edges match current Gradle files for `physical-model`, `package-runtime`,
  `drawing-composition`, `presentation-model`, `compiler`, and `ide:lsp`.
- Forbidden reverse dependencies remain compatible with the current `physical-model ->
  engineering-model` direction.

## New Blocker Scan

No new freeze blockers were found.

- Legacy `PhysicalSize` remains input-only and must be validated into new M35 measurements before
  resolved contracts or IR.
- Repository lock v1, duplicate root scanners, regex package scans, and lock-dependent snapshot
  identity are replacement/deletion work, not retained authorities.
- The M34 schematic path remains regression-only and cannot become a Cabinet authority.
- Old Cabinet producers require zero-caller deletion; no parallel spatial, geometry, routing, or
  rendering IR is admitted.
- Exact interaction selection remains `InteractionSubjectResolver` backed by
  `SemanticCapabilityRegistry`; prefix inference is not fallback authority.

Deterministic `lint_spine.py` result: zero findings.

## Freeze Decision

**PASS.** The M35 architecture spine is brownfield-consistent for these reviewed boundaries and may
be frozen. This verdict approves architecture freeze; implementation readiness remains a separate
BMAD gate.
