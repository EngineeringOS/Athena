# M45 Architecture Spine Review - Current Reality Lens

## Verdict

**APPROVE.** Corrected spine now gives one executable migration from current repository/package seams to M45. It removes every previously identified route to a parallel package graph, pseudo item digest ledger, dual legacy contract, or false cross-memory atomicity claim.

## Review Basis

Re-reviewed `ARCHITECTURE-SPINE.md` against current on-disk production contracts and CodeGraph call paths through:

- `kernel/repository-model/.../RepositoryContracts.kt`
- `kernel/package-model/.../EngineeringPackageModels.kt`
- `kernel/package-model/.../RepresentationPackageModels.kt`
- `kernel/package-model/.../SymbolYamlDescriptorAdmission.kt`
- `kernel/package-model/.../BindingManifestModels.kt`
- `kernel/package-model/.../RepresentationBindingRuleModels.kt`
- `kernel/package-runtime/.../LocalPackageRegistry.kt`
- `kernel/package-runtime/.../RepresentationPackageSnapshotStager.kt`
- `kernel/compiler/.../AthenaRepositoryContractLoader.kt`
- `kernel/compiler/.../AthenaRepositoryGraphResolver.kt`
- `kernel/compiler/.../AthenaRepositoryLockMaterializer.kt`
- `ide/lsp/.../SourceRevisionService.kt`
- `ide/lsp/.../SourceTransactionEngine.kt`

Pinned stack still matches checked-in Gradle and npm manifests: Kotlin `2.4.0`, LSP4J `0.23.1`, TypeScript `5.9.2`, Node `>=22`, Yarn `1.22.22`, Theia `1.73.1`, Konva `10.3.0`, React `18.3.1`.

## Required Corrections Confirmed

### 1. One `ResolvedPackageGraph` - confirmed

AD-2 now makes `packages/<package-name>/package.yaml` a local catalog only. Root `athena.yaml` remains authored dependency intent; selected direct-child packages feed existing compiler-owned `ResolvedPackageGraph`. Semantic requirements are validation edges on those same nodes. It explicitly deletes `representationPackageRoots` and `LocalPackageRegistry` resolution paths and prohibits another graph/resolver.

This is compatible with extending current `AthenaRepositoryGraphResolver` and `AthenaRepositoryLockMaterializer`; it does not preserve current duplicate discovery routes.

### 2. Lock V3 replacement and no pseudo item ledger - confirmed

AD-5 explicitly replaces Repository Lock V2 with `athena-lock-v3`, deletes V2 parsing/materialization, and deletes pseudo item ids `snapshot`, `source:*`, and `resource:*`. V3 owns exact `(packageId,itemId,itemVersion)` identities, item/resource digests, package manifest digest, and resolved concrete/semantic edges.

This directly replaces current `RepositoryLock(version = 2)` and `SourceRevisionService.packageItemDigests()` pseudo records instead of layering M45 fields beside them.

### 3. Source Revision derivation - confirmed

AD-5 and AD-13 define expected Lock V3/compiler model as sole package digest authority. Source Revision projects detailed package/item/resource/provider/profile inputs from that model and never rescans an independent filesystem ledger.

Source-owned `FunctionPartBinding` and `FunctionRepresentationBinding` remain compiler/source inputs, not package lock facts. This preserves M44 full-input CAS without creating a second package authority.

### 4. Legacy deletion map - confirmed

AD-19 names replacement targets:

- `Repository.PackageIdentifier` becomes cross-module package identity.
- `EngineeringPackageDescriptor` and `RepresentationPackageDescriptor` become `PackageManifest` plus PackageItem contracts.
- `BindingManifest` and `RepresentationBindingRule` become source-persisted Function/Part bindings.
- `symbol.yaml` / `athena-symbol-v1` becomes indexed `athena-package-item-v1` Symbol items.
- `representationPackageRoots`, `LocalPackageRegistry`, and Lock V2 are deleted.

No shim, adapter, fallback parser, or dual-write remains permitted. This is specific enough for story teams to delete old production contracts consistently.

### 5. Recoverable commit semantics - confirmed

AD-12 no longer calls files, journal, snapshot, and scene one atomic transaction. AD-17 defines same-volume staging, durable transaction manifest, before/after digests, ordered file replacement, deterministic startup recovery, journal recovery, scene recompile, and cleanup only after committed-state verification.

This is a valid strengthening of current `NioTransactionWorkspace`, whose per-file atomic moves and best-effort rollback do not provide crash recovery. Ready snapshot and scene remain rebuildable projections after durable source/lock commit.

## Additional Resolutions Confirmed

- AD-3 separates authored metadata from compiler-owned digest, state, resolved references, and diagnostics.
- AD-4 gives `PACKAGE_INCOMPLETE` an explicit immutable `AdmissionReport` surface while READY snapshots remain fail-closed.
- AD-10 separates immutable package provenance from downstream binding/scene usage trace.
- AD-18 keeps Part binding in Engineering Reality, independent from representation projection.
- AD-7 preserves source-owned port semantics while package anchors carry compatibility constraints only.

## Non-Blocking Implementation Watchpoints

1. `package.yaml` admission must require exact package version wherever Lock V3 records package coordinates; current `PackageIdentifier.version` is nullable. Root project package semantics may remain separate, but library package selection cannot be version-ambiguous.
2. AD-17 needs fault-injection tests after each durable-manifest and file-replacement phase on Windows. Staged after-images must remain recoverable until committed-state verification.
3. V3 removal must update lock diagnostics, generated frontend contracts, LSP wire mapping, and stale tests in one story boundary; no `RepositoryLockV2` wording should survive production paths.

## Gate Recommendation

Architecture may finalize and proceed to epic/story reconciliation. Carry three watchpoints into acceptance criteria; none requires another architecture decision.
